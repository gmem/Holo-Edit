/**
 *  -----------------------------------------------------------------------------
 *  
 *  Holo-Edit, spatial sound trajectories editor, part of Holophon
 *  Copyright (C) 2006 GMEM
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 *  
 *  -----------------------------------------------------------------------------
 */
package holoedit.gui;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JTextField;

public class NumberBox extends JTextField implements MouseListener, FocusListener, MouseMotionListener
{
	public int w = 30;
	public int h = 20	;
	public final static Font f = new Font("Verdana", Font.PLAIN, 12);
	private boolean dragPoint = false;
	
	private int coordY; // ancienne coordonnee de la souris sur l'axe des ordonnees
	
	public int val;
	
	public NumberBox(int defaultValue)
	{
		super(2);
		setEditable(true);
		setVisible(true);
		setAutoscrolls(false);
		setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
		setFont(f);
		setText((new Integer(defaultValue)).toString());
		val = defaultValue;
		setSize(w, h);
		setVisible(true);
		addFocusListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public int getVal()
	{
		val = stringToInt(getText());
		return val;
	}

	public void mousePressed(MouseEvent e)
	{
		coordY = e.getY();
	}
	public void mouseReleased(MouseEvent e)
	{
		if (dragPoint)
		{
			dragPoint = false;
			requestFocus();
		}
	}
	public void focusGained(FocusEvent e)
	{
		// si l'objet prend le focus, on selectionne tout le champ texte
		selectAll();
	}
	public void mouseDragged(MouseEvent e)
	{
		if(dragPoint)
		{
			int nbIncr = coordY - e.getY();
			val += nbIncr / 4;
			val = (val < 0 ? 0 : val);
			coordY = e.getY();
			setText((new Integer(val)).toString());
		} else {
			dragPoint = true;
			coordY = e.getY();
		}
	}

	public void focusLost(FocusEvent e)
	{
		val = stringToInt(getText());
	}
	
	private int stringToInt(String champ)
	{
		try
		{
			return Integer.valueOf(champ).intValue();
		} catch (NumberFormatException e)
		{
			return val;
		}
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}

}