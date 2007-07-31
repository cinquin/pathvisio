// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.gui.swt;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pathvisio.Engine;
import org.pathvisio.Globals;
import org.pathvisio.data.DBConnector;
import org.pathvisio.data.DBConnectorSwt;
import org.pathvisio.debug.Logger;
import org.pathvisio.debug.Sleak;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.preferences.swt.SwtPreferences.SwtPreference;
import org.pathvisio.util.swt.SwtUtils.SimpleRunnableWithProgress;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayWrapper;
import org.pathvisio.view.swt.VPathwaySwtAwt;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * This class contains the essential parts of the program: the window, drawing and gpml data
 */
public class SwtEngine {
	/**
	 * {@link Pathway} object containing JDOM representation of the gpml pathway 
	 * and handle gpml related actions
	 */
	
	private MainWindow window;
	
	private ImageRegistry imageRegistry;
	
	private File DIR_APPLICATION;
	private File DIR_DATA;
	boolean USE_R;
		
	private static SwtEngine current;
	public static SwtEngine getCurrent() {
		if(current == null) current = new SwtEngine();
		return current;
	}
	
	/**
	 * Get the {@link ApplicationWindow}, the UI of the program
	 */
	public MainWindow getWindow() {
		if(window == null) window = new MainWindow();
		return window;
	}
	
	/**
	   Updates the title of the main window.
	   Call at initialization of the program,
	   whenever the filename of the current document has changed,
	   or the change status has changed.
	*/
	public void updateTitle()
	{
		if (Engine.getCurrent().getActivePathway() == null)
		{
			window.getShell().setText(Globals.APPLICATION_VERSION_NAME);
		}
		else
		{
			// get filename, or (New Pathway) if current pathway hasn't been opened yet
			String fname = (Engine.getCurrent().getActivePathway().getSourceFile() == null) ? "(New Pathway)" :
				Engine.getCurrent().getActivePathway().getSourceFile().getName();
			window.getShell().setText(
				"*" + fname + " - " +
				Globals.APPLICATION_VERSION_NAME
				);
		}
	}
	
	/**
	 * Initiates an instance of {@link MainWindow} that is monitored by Sleak.java,
	 * to monitor what handles (to OS device context) are in use. For debug purposes only 
	 * (to check for undisposed widgets)
	 * @return The {@link MainWindow} monitored by Sleak.java
	 */
	public MainWindow getSleakWindow() {
		//<DEBUG to find undisposed system resources>
		DeviceData data = new DeviceData();
		data.tracking = true;
		Display display = new Display(data);
		Sleak sleak = new Sleak();
		sleak.open();
		
		Shell shell = new Shell(display);
		window = new MainWindow(shell);
		return window;
		//</DEBUG>
	}
	
	private VPathwayWrapper createWrapper() {
		if(window != null) {
//			return new VPathwaySwtAwt(window.sc, SWT.NO_BACKGROUND);
			return new VPathwaySwtAwt(window.swingPathwayComposite.getScrollPane(), window.getShell().getDisplay());
		}
		return null;
	}
		
	public void newPathway() {
		if(canDiscardPathway()) {
			VPathwayWrapper w = createWrapper();
			Engine.getCurrent().newPathway(w);
			updateTitle();
		}
	}
	
	/**
	   Opens a file dialog and lets user select a file.
	   Then the pathways is saved to that file.
	   returns false if the action was cancelled by the user
	 */
	public boolean savePathwayAs()
	{
		Pathway pathway = Engine.getCurrent().getActivePathway();
		VPathway vPathway = Engine.getCurrent().getActiveVPathway();
		
		// Check if a gpml pathway is loaded
		if (pathway != null)
		{
			FileDialog fd = new FileDialog(window.getShell(), SWT.SAVE);
			fd.setText("Save");
			fd.setFilterExtensions(new String[] {"*." + Engine.PATHWAY_FILE_EXTENSION, "*.*"});
			fd.setFilterNames(new String[] {Engine.PATHWAY_FILTER_NAME, "All files (*.*)"});
			
			File xmlFile = pathway.getSourceFile();
			if(xmlFile != null) {
				fd.setFileName(xmlFile.getName());
				fd.setFilterPath(xmlFile.getPath());
			} else {
					fd.setFilterPath(SwtPreference.SWT_DIR_PWFILES.getValue());
			}
			String fileName = fd.open();
			// Only proceed if user selected a file
			
			if(fileName == null) return false;
			
			// Append .gpml extension if not already present
			if(!fileName.endsWith("." + Engine.PATHWAY_FILE_EXTENSION)) 
				fileName += "." + Engine.PATHWAY_FILE_EXTENSION;
			
			File checkFile = new File(fileName);
			boolean confirmed = true;
			// If file exists, ask overwrite permission
			if(checkFile.exists())
			{
				confirmed = MessageDialog.openQuestion(window.getShell(),"",
													   "File already exists, overwrite?");
			}
			if(confirmed)
			{
				double usedZoom = vPathway.getPctZoom();
				// Set zoom to 100%
				vPathway.setPctZoom(100);					
				// Overwrite the existing xml file
				try
				{
					Engine.getCurrent().savePathway(checkFile);
					updateTitle();
					// Set zoom back
					vPathway.setPctZoom(usedZoom);
				}
				catch (ConverterException e)
				{
					String msg = "While writing xml to " 
						+ checkFile.getAbsolutePath();					
					MessageDialog.openError (window.getShell(), "Error", 
											 "Error: " + msg + "\n\n" + 
											 "See the error log for details.");
					Logger.log.error(msg, e);
				}
			}
		}
		else
		{
			MessageDialog.openError (window.getShell(), "Error", 
									 "No gpml file loaded! Open or create a new gpml file first");
		}			
		return true;
	}

	/**
	   Checks if the current pathway has changes, and if so, pops up a dialog
	   offering to save.
	   This should always be called before you change pathway

	   @return returns false if the user pressed cancel. 
	   
	   TODO: Currently always asks, even if there were no changes since last save.
	 */
	public boolean canDiscardPathway()
	{
		Pathway pathway = Engine.getCurrent().getActivePathway();
		// checking not necessary if there is no pathway or if pathway is not changed.
		if (pathway == null || !pathway.hasChanged()) return true;
		String[] opts =
		{
			IDialogConstants.YES_LABEL,
			IDialogConstants.NO_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		MessageDialog msgDlg = new MessageDialog (
			window.getShell(),
			"Save changes?",
			null,
			"Your pathway may have changed. Do you want to save?",
			MessageDialog.QUESTION,
			opts,
			0);
		int result = msgDlg.open();
		if (result == 2) // cancel
		{
			return false;
		}
		else if (result == 0) // yes
		{
			// return false if save is cancelled.
			return (savePathway());
		}
		// no
		return true;
	}
	

	/**
	   Opens a URL in the default webbrowser.  Uses a progress dialog
	   if it takes a long time.  Shows an error message and returns
	   false if it somehow failed to open the web page.
	*/
	public boolean openWebPage(String url, String progressMsg, String errMsg) {
		Shell shell = getWindow().getShell();
		if(shell == null || shell.isDisposed()) return false;
		
		SimpleRunnableWithProgress rwp = new SimpleRunnableWithProgress(
				Engine.class, "doOpenWebPage", new Class[] { String.class }, new Object[] { url }, null);
		SimpleRunnableWithProgress.setMonitorInfo(progressMsg, IProgressMonitor.UNKNOWN);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, rwp);
			return true;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			String msg = cause == null ? null : cause.getMessage();
			MessageDialog.openError(shell, "Error",
			"Unable to open web browser" +
			(msg == null ? "" : ": " + msg) +
			"\n" + errMsg);
			return false;
		} catch (InterruptedException ignore) { return false; }
	}
	
	public void doOpenWebPage(String url) throws BrowserLaunchingInitializingException, BrowserLaunchingExecutionException, UnsupportedOperatingSystemException {
		BrowserLauncher bl = new BrowserLauncher(null);
		bl.openURLinBrowser(url);
	}
	
	/**
	 Open a pathway from a gpml file
	 Asks the user if the old pathway should be discarded, if necessary
	 */
	public void openPathway(String pwf)
	{
		if (canDiscardPathway())
		{
			try { 
				VPathwayWrapper w = createWrapper();
				Engine.getCurrent().openPathway(pwf, w);
				updateTitle();
			} catch(ConverterException e) {		
				if (e.getMessage().contains("Cannot find the declaration of element 'Pathway'"))
				{
					MessageDialog.openError(getWindow().getShell(), 
						"Unable to open Gpml file", 
						"Unable to open Gpml file.\n\n" +
						"The most likely cause for this error is that you are trying to open an old Gpml file. " +
						"Please note that the Gpml format has changed as of March 2007. " +
						"The standard pathway set can be re-downloaded from http://pathvisio.org " +
						"Non-standard pathways need to be recreated or upgraded. " +
						"Please contact the authors at " + Globals.DEVELOPER_EMAIL + " if you need help with this.\n" +
						"\nSee error log for details");
					Logger.log.error("Unable to open Gpml file", e);
				}
				else
				{
					//TODO: refactor these error messages,
					// so it's not redundant with SwingEngine
					MessageDialog.openError(
						getWindow().getShell(), 
						"Unable to open Gpml file",
						"Unable to open Gpml file\n\n" +
						"Check that the file you're trying to open really is a "+
						"Pathway in the Gpml format. If the problem persists, please contact " +
						"the developers at " + Globals.DEVELOPER_EMAIL + ". Please include the " +
						"file you're trying to open and the error log.");
					Logger.log.error("Unable to open Gpml file", e);
				}
			}
		}
	}
	
	/**
	 * Get the {@link ImageRegistry} containing commonly used images
	 */
	public ImageRegistry getImageRegistry() { 
		if(imageRegistry == null) imageRegistry = new ImageRegistry();
		return imageRegistry; 
	}
	
	/**
	 * Set the {@link ImageRegistry} containing commonly used images
	 */
	public void setImageRegistry(ImageRegistry _imageRegistry) {
		imageRegistry = _imageRegistry;
	}
			
	public DBConnectorSwt getSwtDbConnector(int type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		DBConnector dbc = Engine.getCurrent().getDbConnector(type);
		if(dbc instanceof DBConnectorSwt) {
			return (DBConnectorSwt)dbc;
		} else {
			throw new IllegalArgumentException("Not an SWT database connector");
		}
	}
	/**
	 * Get the working directory of this application
	 */
	public File getApplicationDir() {
		if(DIR_APPLICATION == null) {
			DIR_APPLICATION = new File(System.getProperty("user.home"), "." + Globals.APPLICATION_NAME);
			if(!DIR_APPLICATION.exists()) DIR_APPLICATION.mkdir();
		}
		return DIR_APPLICATION;
	}
		
	public File getDataDir() {
		if(DIR_DATA == null) {
			DIR_DATA = new File(System.getProperty("user.home"), Globals.APPLICATION_NAME + "-Data");
			if(!DIR_DATA.exists()) DIR_DATA.mkdir();
		}
		return DIR_DATA;
	}
			
	public boolean isUseR() { return USE_R; }
	
	/**
	   save the current pathway
	   returns false if the action was cancelled by the user
	   
	   Calls savePathwayAs if the filename of the current pathway is unknown,
	   so that the user can set a location for this pathway
	*/
		public boolean savePathway()
		{
			Pathway pathway = Engine.getCurrent().getActivePathway();
			VPathway vPathway = Engine.getCurrent().getActiveVPathway();
			
			boolean result = true;
			
			double usedZoom = vPathway.getPctZoom();
			// Set zoom to 100%
			vPathway.setPctZoom(100);			
			
	        // Overwrite the existing xml file.
			// If the target file is read-only, let the user select a new pathway
			if (pathway.getSourceFile() != null && pathway.getSourceFile().canWrite())
			{
				try
				{
					Engine.getCurrent().savePathway(pathway.getSourceFile());
				}
				catch (ConverterException e)
				{
					String msg = "While writing xml to " 
						+ pathway.getSourceFile().getAbsolutePath();					
					MessageDialog.openError (window.getShell(), "Error", 
											 "Error: " + msg + "\n\n" + 
											 "See the error log for details.");
					Logger.log.error(msg, e);
				}
			}
			else
			{
				result = savePathwayAs();
			}
			// Set zoom back
			vPathway.setPctZoom(usedZoom);

			return result;
		}
}