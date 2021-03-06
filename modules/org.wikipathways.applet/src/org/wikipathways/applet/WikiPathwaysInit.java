// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.wikipathways.applet;

import java.net.URL;

import org.pathvisio.core.Engine;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.GpmlFormat;
import org.pathvisio.core.model.ImageExporter;
import org.pathvisio.core.model.MappFormat;
import org.pathvisio.core.model.RasterImageExporter;
import org.pathvisio.core.model.StaticProperty;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.Utils;
import org.pathvisio.core.view.MIMShapes;

/**
 * Static utility class that contains a collection of global methods for {@link WikiPathways}.
 * @author thomas
 */
public class WikiPathwaysInit
{
	static void init(Engine engine, PreferenceManager preferences) throws Exception
	{
		String logDest = preferences.get(GlobalPreference.WP_FILE_LOG);
		Logger.log.setDest (logDest);
		Logger.log.setLogLevel(true, true, true, true, true, true);//Modify this to adjust log level

		MIMShapes.registerShapes();

		StaticProperty.CENTERX.setHidden(true);
		StaticProperty.CENTERY.setHidden(true);
		StaticProperty.ENDX.setHidden(true);
		StaticProperty.ENDY.setHidden(true);
		StaticProperty.HEIGHT.setHidden(true);
		StaticProperty.LAST_MODIFIED.setHidden(true);
		StaticProperty.ORGANISM.setHidden(true);
		StaticProperty.ROTATION.setHidden(true);
		StaticProperty.STARTX.setHidden(true);
		StaticProperty.STARTY.setHidden(true);
		StaticProperty.WIDTH.setHidden(true);

		engine.addPathwayExporter(new GpmlFormat());
		engine.addPathwayImporter(new GpmlFormat());
		if(Utils.getOS() == Utils.OS_WINDOWS) {
			engine.addPathwayImporter(new MappFormat());
		}

		engine.setApplicationName(WikiPathways.APPLICATION_NAME);
	}

	public static void registerXmlRpcExporters(URL rpcUrl, Engine engine) {
		engine.addPathwayExporter(new RasterImageExporter(ImageExporter.TYPE_PNG));
//		disabled mapp export see ticket 1555
//		if(Utils.getOS() == Utils.OS_WINDOWS) {
//			engine.addPathwayExporter(new MappFormat());
//		}
//		engine.addPathwayExporter(new WikiPathwaysExporter(rpcUrl, ImageExporter.TYPE_TIFF)); disabled, see bug #166
	}
}
