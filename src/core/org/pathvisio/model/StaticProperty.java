// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
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

package org.pathvisio.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Static properties for ObjectTypes, linked in {@link PathwayElement}.
 */
public enum StaticProperty implements Property
{
	// all
	COMMENTS ("Comments", "Comments", StaticPropertyType.COMMENTS, 101),

	// line, shape, datanode, label
	COLOR ("Color", "Color", StaticPropertyType.COLOR, 202),
	// shape, datanode, label
	CENTERX ("CenterX", "Center X", StaticPropertyType.DOUBLE, 103, true, false, false),
	CENTERY ("CenterY", "Center Y", StaticPropertyType.DOUBLE, 104, true, false, false),

	// shape, datanode, label, modification
	WIDTH ("Width", "Width", StaticPropertyType.DOUBLE, 105, true, false, false),
	HEIGHT ("Height", "Height", StaticPropertyType.DOUBLE, 106, true, false, false),

	// modification
	RELX ("relX", "Relative X", StaticPropertyType.DOUBLE, 107, true, false, false),
	RELY ("relY", "Relative Y", StaticPropertyType.DOUBLE, 108, true, false, false),
	GRAPHREF ("GraphRef", "GraphRef", StaticPropertyType.STRING, 109, false, true, false),

	// shape, modification
	TRANSPARENT ("Transparent", "Transparent", StaticPropertyType.BOOLEAN, 210),
	FILLCOLOR ("FillColor", "Fill Color", StaticPropertyType.COLOR, 211),
	SHAPETYPE ("ShapeType", "Shape Type", StaticPropertyType.SHAPETYPE, 112),

	// shape
	ROTATION ("Rotation", "Rotation", StaticPropertyType.ANGLE, 113, true, false, false),

	// line
	STARTX ("StartX", "Start X", StaticPropertyType.DOUBLE, 114, true, false, false),
	STARTY ("StartY", "Start Y", StaticPropertyType.DOUBLE, 115, true, false, false),
	ENDX ("EndX", "End X", StaticPropertyType.DOUBLE, 116, true, false, false),
	ENDY ("EndY", "End Y", StaticPropertyType.DOUBLE, 117, true, false, false),

	STARTLINETYPE ("StartLineType", "Start Line Type", StaticPropertyType.LINETYPE, 118),
	ENDLINETYPE ("EndLineType", "End Line Type", StaticPropertyType.LINETYPE, 119),

	// line, shape and modification
	LINESTYLE ("LineStyle", "Line Style", StaticPropertyType.LINESTYLE, 120),

	// brace
	ORIENTATION ("Orientation", "Orientation", StaticPropertyType.ORIENTATION, 121),

	// datanode
	GENEID ("GeneID", "Database Identifier", StaticPropertyType.DB_ID, 122),
	DATASOURCE ("SystemCode", "Database Name", StaticPropertyType.DATASOURCE, 123),
	GENMAPP_XREF ("Xref", "Xref", StaticPropertyType.STRING, 124), // deprecated, maintained for backward compatibility with GenMAPP.
	BACKPAGEHEAD ("BackpageHead", "Backpage head", StaticPropertyType.STRING, 125),
	TYPE ("Type", "Type", StaticPropertyType.GENETYPE, 126),

	MODIFICATIONTYPE ("ModificationType", "ModificationType", StaticPropertyType.STRING, 127),

	// label, modification, datanode
	TEXTLABEL ("TextLabel", "Text Label", StaticPropertyType.STRING, 128),

	// label
	FONTNAME ("FontName", "Font Name", StaticPropertyType.FONT, 129),
	FONTWEIGHT ("FontWeight", "Bold", StaticPropertyType.BOOLEAN, 130),
	FONTSTYLE ("FontStyle", "Italic", StaticPropertyType.BOOLEAN, 131),
	FONTSIZE ("FontSize", "Font Size", StaticPropertyType.DOUBLE, 132),
	OUTLINE ("Outline", "Outline", StaticPropertyType.OUTLINETYPE, 133),

	// mappinfo
	MAPINFONAME ("MapInfoName", "Title", StaticPropertyType.STRING, 134),
	ORGANISM ("Organism", "Organism", StaticPropertyType.ORGANISM, 135),
	MAPINFO_DATASOURCE ("Data-Source", "Data-Source", StaticPropertyType.STRING, 136),
	VERSION ("Version", "Version", StaticPropertyType.STRING, 137),
	AUTHOR ("Author", "Author", StaticPropertyType.STRING, 138),
	MAINTAINED_BY ("Maintained-By", "Maintainer", StaticPropertyType.STRING, 139),
	EMAIL ("Email", "Email", StaticPropertyType.STRING, 140),
	LAST_MODIFIED ("Last-Modified", "Last Modified", StaticPropertyType.STRING, 141),
	AVAILABILITY ("Availability", "Availability", StaticPropertyType.STRING, 142),
	BOARDWIDTH ("BoardWidth", "Board Width", StaticPropertyType.DOUBLE, 143, true, true, false),
	BOARDHEIGHT ("BoardHeight", "Board Height", StaticPropertyType.DOUBLE, 144, true, true, false),
	WINDOWWIDTH ("WindowWidth", "Window Width", StaticPropertyType.DOUBLE, 145, true, true, true),
	WINDOWHEIGHT ("WindowHeight", "Window Height", StaticPropertyType.DOUBLE, 146, true, true, true),

	// other
	GRAPHID ("GraphId", "GraphId", StaticPropertyType.STRING, 147, false, true, false),
	STARTGRAPHREF ("StartGraphRef", "StartGraphRef", StaticPropertyType.STRING, 148, false, true, false),
	ENDGRAPHREF ("EndGraphRef", "EndGraphRef", StaticPropertyType.STRING, 149, false, true, false),
	GROUPID ("GroupId", "GroupId", StaticPropertyType.STRING, 150, false, true, false),
	GROUPREF ("GroupRef", "GroupRef", StaticPropertyType.STRING, 151, false, true, false),
	GROUPSTYLE ("GroupStyle", "Group style", StaticPropertyType.GROUPSTYLETYPE, 152),
	BIOPAXREF( "BiopaxRef", "BiopaxRef", StaticPropertyType.BIOPAXREF, 153, false, true, false),
	ZORDER ( "Z order", "ZOrder", StaticPropertyType.INTEGER, 154, false, true, false);

	private String tag, name;
	private StaticPropertyType type;
	private boolean isCoordinate;
	private boolean isAdvanced;
	private boolean hidden;
	private int order;

	private StaticProperty (String aTag, String aName, StaticPropertyType aType, int anOrder, boolean aIsCoordinate,
			boolean aIsAdvanced, boolean isHidden)
	{
		tag = aTag;
		type = aType;
		name = aName;
		isCoordinate = aIsCoordinate;
		isAdvanced = aIsAdvanced;
		hidden = isHidden;
		order = anOrder;
		PropertyManager.registerProperty(this);
	}

	private StaticProperty (String aTag, String aDesc, StaticPropertyType aType, int anOrder)
	{
		this(aTag, aDesc, aType, anOrder, false, false, false);
	}

	/**
	 * @return Name of GPML attribute related to this property.
	 */
	public String tag()
	{
		return tag;
	}

	/**
	 * @deprecated use getDescription() instead.
	 */
	public String desc()
	{
		return name;
	}

	/**
	 * @return Data type of this property
	 * @deprecated use getType() instead.
	 */
	public StaticPropertyType type()
	{
		return type;
	}

	/**
	 * @return true if this property causes coordinate changes.
	 */
	public boolean isCoordinateChange() {
		return isCoordinate;
	}

	/**
	 * @return true if this attribute should be hidden unless the "Show advanced properties" preference is set to true
	 */
	public boolean isAdvanced() {
		return isAdvanced;
	}

	/**
	 * @return true if this is attribute should not be shown in property table
	 */
	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hide) {
		hidden = hide;
	}

	/**
	 * @return Logical sort order for display in Property table. Related properties sort together
	 */
	public int getOrder()
	{
		return order;
	}

	public static StaticProperty getByTag(String value)
	{
		return tagMapping.get (value);
	}

	static private Map<String, StaticProperty> tagMapping = initTagMapping();

	static private Map<String, StaticProperty> initTagMapping()
	{
		Map<String, StaticProperty> result = new HashMap<String, StaticProperty>();
		for (StaticProperty o : StaticProperty.values())
		{
			result.put (o.tag(), o);
		}
		return result;
	}


	//-- Property methods --//

	public String getId() {
		return "core." + tag;
	}

	/** @{inheritDoc} */
	public String getName() {
		return name;
	}

	/** @{inheritDoc} */
	public String getDescription() {
		return null;
	}

	public PropertyType getType() {
		return type;
	}

	public boolean isCollection() {
		return false;
	}
}