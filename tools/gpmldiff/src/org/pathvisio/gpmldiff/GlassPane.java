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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * GlassPane tutorial
 * "A well-behaved GlassPane"
 * http://weblogs.java.net/blog/alexfromsun/
 * <p/>
 * This is the final version of the GlassPane
 * it is transparent for MouseEvents,
 * and respects underneath component's cursors by default,
 * it is also friedly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 *
 * @author Alexander Potochkin
 */
class GlassPane extends JPanel implements AWTEventListener
{
    private final JFrame frame;
    private Point point = new Point();

	// baloon margin is both the horizontal and vertical margin.
	private static final int BALOON_SPACING = 50;
	private static final int BALOON_MARGIN = 20;
	private static final Color BALOON_PAINT = Color.YELLOW;
	private static final int HINT_FONT_SIZE = 11;
	
	private boolean alignTop = true;
	
    public GlassPane(JFrame frame)
	{
        super(null);
        this.frame = frame;
        setOpaque(false);
    }

	boolean showHint = false;
	Map <String, String> hint = null;

    // view coordinates
	double x1, y1, x2, y2;

	/**
	   setHint implies showHint (true)
	 */
	void setHint(Map <String, String> _hint, double _x1, double _y1, double _x2, double _y2)
	{
		hint = _hint;
		x1 = _x1;
		y1 = _y1;
		x2 = _x2;
		y2 = _y2;
		showHint (true);
	}

	/**
	   enable showing of hint.
	 */
	void showHint(boolean value)
	{
		showHint = value;
		repaint();
	}
	
    protected void paintComponent(Graphics g)
	{
		if (!showHint) return;
		
        Graphics2D g2 = (Graphics2D) g;

        //g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		
		FontRenderContext frc = g2.getFontRenderContext();

		// first determine size.
		int textHeight = 0;
		int maxTextWidth = 0;

		Font f = new Font("SansSerif", Font.PLAIN, HINT_FONT_SIZE);
		Font fb = new Font("SansSerif", Font.BOLD, HINT_FONT_SIZE);

		Map <TextLayout, Point> layouts = new HashMap <TextLayout, Point>();

		int ypos = 0;
		for (Map.Entry<String, String> entry : hint.entrySet())
		{
			TextLayout tl0 = new TextLayout (entry.getKey() + ": ", fb, frc);
			TextLayout tl1 = new TextLayout (entry.getValue(), f, frc);
			Rectangle2D b0 = tl0.getBounds();
			Rectangle2D b1 = tl1.getBounds();

			ypos += tl0.getAscent();

			layouts.put (tl0, new Point (0, ypos));
			layouts.put (tl1, new Point (10 + (int)b0.getWidth(), ypos));

			ypos += tl0.getDescent() + 10 + tl0.getLeading();

			int width = (int)(b0.getWidth() + b1.getWidth());
			if (width > maxTextWidth) { maxTextWidth = width; }
		}
		int baloonWidth = maxTextWidth + 2 * BALOON_MARGIN;
		int baloonHeight = ypos + 2 * BALOON_MARGIN;

		// figure out coordinates that are not in the way of the mouse.
		int xpos = (int)((getSize().getWidth() - baloonWidth) / 2);
		ypos = alignTop ? BALOON_SPACING : (int)(getSize().getHeight() - baloonHeight - BALOON_SPACING);

		Shape bg = new RoundRectangle2D.Double (
				xpos, ypos,
				baloonWidth, baloonHeight,
				BALOON_MARGIN, BALOON_MARGIN);
		if (point != null && bg.contains(point))
		{
			// toggle alignTop and calculate new shape
			alignTop = !alignTop;
			ypos = alignTop ? BALOON_SPACING : (int)(getSize().getHeight() - baloonHeight - BALOON_SPACING);
			bg = new RoundRectangle2D.Double (
				xpos, ypos,
				baloonWidth, baloonHeight,
				BALOON_MARGIN, BALOON_MARGIN);
		}

		g2.setPaint (BALOON_PAINT);
		g2.fill (bg);
		g2.setColor (Color.BLACK);
		g2.draw (bg);

		// then do actual drawing
		for (Map.Entry<TextLayout, Point> entry : layouts.entrySet())
		{
			Point p = entry.getValue();
			TextLayout l = entry.getKey();
			l.draw(g2, (float)(xpos + BALOON_MARGIN + p.getX()), (float)(ypos + p.getY() + BALOON_MARGIN));
		}

		g2.dispose();
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) event;
            if (!SwingUtilities.isDescendingFrom(me.getComponent(), frame)) {
                return;
            }
            if (me.getID() == MouseEvent.MOUSE_EXITED && me.getComponent() == frame) {
                point = null;
            } else {
                MouseEvent converted = SwingUtilities.convertMouseEvent(me.getComponent(), me, frame.getGlassPane());
                point = converted.getPoint();
			}
            repaint();
        }
    }

    /**
     * If someone adds a mouseListener to the GlassPane or set a new cursor
     * we expect that he knows what he is doing
     * and return the super.contains(x, y)
     * otherwise we return false to respect the cursors
     * for the underneath components
     */
    public boolean contains(int x, int y) {
        if (getMouseListeners().length == 0 && getMouseMotionListeners().length == 0
                && getMouseWheelListeners().length == 0
                && getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
            return false;
        }
        return super.contains(x, y);
    }
}

