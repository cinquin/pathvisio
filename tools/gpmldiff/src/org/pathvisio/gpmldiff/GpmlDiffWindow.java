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
package org.pathvisio.gpmldiff;

import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.pathvisio.debug.Logger;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.Pathway;
import org.pathvisio.view.VPathway;
import org.pathvisio.view.VPathwayEvent;
import org.pathvisio.view.VPathwayListener;
import org.pathvisio.view.swing.VPathwaySwing;

class GpmlDiffWindow extends JFrame implements VPathwayListener
{
	private static final int WINDOW_WIDTH = 1000;
	private static final int WINDOW_HEIGHT = 500;

	private JScrollPane[] pwyPane = new JScrollPane[2];
	private JMenuBar menubar;

	public static final int PWY_OLD = 0;
	public static final int PWY_NEW = 1;

	private VPathwaySwing[] wrapper = { null, null };
	private VPathway[] view = { null, null };
	private PwyDoc[] doc = { null, null };

	private PanelOutputter outputter = null;

	double zoomFactor = 100;
	JPanel centerPanel = null;
	

	public void setFile (int pwyType, File f)
	{
		Pathway pwy = new Pathway();
		doc[pwyType] = PwyDoc.read (f);
		assert (doc[pwyType] != null);
					
		wrapper[pwyType] = new VPathwaySwing(pwyPane[pwyType]);

		view[pwyType] = wrapper[pwyType].createVPathway();
		view[pwyType].addVPathwayListener(this);
		view[pwyType].setSelectionEnabled (false);
		view[pwyType].fromGmmlData(doc[pwyType].getPathway());
		view[pwyType].setPctZoom (zoomFactor);

		outputter = null; // invalidate putative remaining outputter
		
		if (view[PWY_OLD] != null && view[PWY_NEW] != null)
		{
			SearchNode result = doc[PWY_OLD].findCorrespondence (doc[PWY_NEW], new BetterSim(), new BasicCost());
			outputter = new PanelOutputter(view[PWY_OLD], view[PWY_NEW]);
			doc[PWY_OLD].writeResult (result, doc[PWY_NEW], outputter);
			try
			{
				outputter.flush();
			}
			catch (IOException ex) { ex.printStackTrace(); }

			// merge models of the two pathways
			// TODO: find solution for inequal sized pathways.
			pwyPane[0].getHorizontalScrollBar().setModel(
				pwyPane[1].getHorizontalScrollBar().getModel());
			pwyPane[0].getVerticalScrollBar().setModel(
				pwyPane[1].getVerticalScrollBar().getModel());
		}
				
		/*catch (ConverterException ce)
		  {
		  JOptionPane.showMessageDialog (
		  parent,
		  "Exception while opening gpml file.\n" +
		  "Please check that the file you opened is a valid Gpml file.",
		  "Open Error", JOptionPane.ERROR_MESSAGE);
		  Logger.log.error ("Error opening gpml", ce);
		  }*/
		
	}
	
	private class LoadPwyAction extends AbstractAction
	{
		private int pwyType;
		private JFrame parent;
		
		public LoadPwyAction (JFrame window, int value)
		{
			super ("Load " + ((value == PWY_OLD) ? "old" : "new")  + " pathway");
			String s = (value == PWY_OLD) ? "old" : "new";
			putValue (Action.SHORT_DESCRIPTION, "Load the " + s + " pathway");
			putValue (Action.LONG_DESCRIPTION, "Load the " + s + " pathway");
			pwyType = value;
			parent = window;
		}

		public void actionPerformed(ActionEvent e)
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Load a pathway");
			jfc.setDialogType (JFileChooser.OPEN_DIALOG);
			
			int status = jfc.showDialog (parent, "Load");
			if (status == JFileChooser.APPROVE_OPTION)
			{
				setFile (pwyType, jfc.getSelectedFile());
			}
		}
	}

	class CloseAction extends AbstractAction
	{
		JFrame parent;
		public CloseAction(JFrame _parent)
		{
			super("Close");
			parent = _parent;
		}

		public void actionPerformed (ActionEvent e)
		{
			parent.dispose();
			System.exit(0);
		}
	}

	class CenterAction extends AbstractAction
	{
		public CenterAction()
		{
			super ("Center");
		}

		public void actionPerformed (ActionEvent e)
		{
		}
	}

	private class ZoomAction extends AbstractAction
	{
		double actionZoomFactor;
		
		public ZoomAction(double zf)
		{
			actionZoomFactor = zf;
			String descr = "Set zoom to " + (int)zf + "%";
			putValue(Action.NAME, toString());
			putValue(Action.SHORT_DESCRIPTION, descr);
			putValue(Action.LONG_DESCRIPTION, descr);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			zoomFactor = actionZoomFactor;
			if(view[0] != null)
			{			   
				view[0].setPctZoom(zoomFactor);
			}
			if(view[1] != null)
			{
				view[1].setPctZoom(zoomFactor);
			}
		}
		
		public String toString()
		{
			if(actionZoomFactor == VPathway.ZOOM_TO_FIT)
			{
				return "Fit to window";
			}
			return (int)actionZoomFactor + "%";
		}
	}

	void addMenuActions()
	{
		JMenu filemenu = new JMenu ("File");
		filemenu.add (new LoadPwyAction(this, PWY_OLD));
		filemenu.add (new LoadPwyAction(this, PWY_NEW));
		filemenu.add (new CloseAction(this));

		JMenu viewmenu = new JMenu ("View");
		viewmenu.add (new ZoomAction(VPathway.ZOOM_TO_FIT));
		viewmenu.add (new ZoomAction(30));
		viewmenu.add (new ZoomAction(50));
		viewmenu.add (new ZoomAction(75));
		viewmenu.add (new ZoomAction(100));
		viewmenu.add (new ZoomAction(120));
		viewmenu.addSeparator();
		viewmenu.add (new CenterAction());
		
		menubar.add (filemenu);
		menubar.add (viewmenu);					  
	}
	
	GpmlDiffWindow ()
	{
		super ();

		setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
		Container contents = getContentPane();
		contents.setLayout (new BoxLayout(contents, BoxLayout.X_AXIS));
		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		
		menubar = new JMenuBar();
		addMenuActions ();
		setJMenuBar (menubar);

		for (int i = 0; i < 2; ++i)
		{
			pwyPane[i] = new JScrollPane();
		}

		centerPanel = new JPanel();
		
		contents.add (pwyPane[PWY_OLD]);
		pwyPane[PWY_OLD].setPreferredSize (new Dimension (400, 300));
		contents.add (centerPanel);
		centerPanel.setPreferredSize (new Dimension (200, 300));
		centerPanel.setMinimumSize (new Dimension (200, 300));
		contents.add (pwyPane[PWY_NEW]);
		pwyPane[PWY_NEW].setPreferredSize (new Dimension (400, 300));
		validate();
	}

	public void vPathwayEvent (VPathwayEvent e)
	{
		if (e.getType() == VPathwayEvent.ELEMENT_CLICKED_DOWN)
		{
			if (e.getSource() == view[PWY_OLD])
			{
				System.out.println ("Element clicked in old pathway");
			}
			else if (e.getSource() == view[PWY_NEW])
			{
				System.out.println ("Element clicked in new pathway");
			}
			if (outputter != null)
			{
				PanelOutputter.ModData mod = outputter.modsByElt.get (e.getAffectedElement());
				if (mod != null)
				{
					System.out.println ("Modifiied! " + mod.hint);
				}
			}		
		}
	}
}