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

import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import holoedit.util.Ut;

public class HoloMenuItem extends JMenuItem
{
	protected String menuTitle;

	public HoloMenuItem()
	{
		super();
		menuTitle = "";
	}
	// pas de raccourci
	public HoloMenuItem(String t)
	{
		super(t);
		menuTitle = t;
	}
	public HoloMenuItem(String t, ActionListener aAction)
	{
		super(t);
		menuTitle = t;
		addActionListener(aAction);
	}
	// pomme + Key // ctrl + key
	public HoloMenuItem(String t, int s, ActionListener aAction)
	{
		super(t);

		if(Ut.MAC)
			setAccelerator(KeyStroke.getKeyStroke(s,4));
		else setAccelerator(KeyStroke.getKeyStroke(s,2));

		menuTitle = t;
		addActionListener(aAction);
	}

	// pomme + shift + Key // ctrl + shift + key
	public HoloMenuItem(String t, int s, boolean b, ActionListener aAction)
	{
		super(t);
		if(b)
		{
			if(Ut.MAC)
				setAccelerator(KeyStroke.getKeyStroke(s,5));
			else setAccelerator(KeyStroke.getKeyStroke(s,3));
		} else {
			setAccelerator(KeyStroke.getKeyStroke(s,0));
		}
		menuTitle = t;
		addActionListener(aAction);
	}
	
	// shift + key
	public HoloMenuItem(String t, int s, boolean b, boolean dummy, ActionListener aAction)
	{
		super(t);
		if(b)
			setAccelerator(KeyStroke.getKeyStroke(s,1));
		else
			setAccelerator(KeyStroke.getKeyStroke(s,0));

		menuTitle = t;
		addActionListener(aAction);
	}
	
	// pomme + alt + key // ctrl + alt + key
	public HoloMenuItem(String t, int s, boolean dummy1, boolean dummy2, boolean dummy3, ActionListener aAction)
	{
		super(t);
		
		if(Ut.MAC)
			setAccelerator(KeyStroke.getKeyStroke(s,12));
		else setAccelerator(KeyStroke.getKeyStroke(s,10));
		
		menuTitle = t;
		addActionListener(aAction);
	}

	// alt + key
	public HoloMenuItem(String t, int s, boolean dummy1, boolean dummy2, boolean dummy3, boolean dummy4, ActionListener aAction)
	{
		super(t);
		setAccelerator(KeyStroke.getKeyStroke(s,8));
		menuTitle = t;
		addActionListener(aAction);
	}
	
  	public String toString()
    {
    	return menuTitle;
    }
}