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
package holoedit.functions;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import javax.swing.JTextField;

public class TextParam extends JTextField implements Param, FocusListener
{
	// attributs graphiques
	private int w = 80;
	private int h = 25;
	private Color selColor = Color.white;
	// numero du champ
	private int numero;

	public TextParam(int _numero, Object _val)
	{
		super();
		numero = _numero;
		setValue(_val);
		init();
	}
	
	public void init()
	{
		setFont(f);
		setSelectedTextColor(selColor);
		setAutoscrolls(false);
		setVisible(true);
		setHorizontalAlignment(JTextField.CENTER);
		setSize(w, h);
		addFocusListener(this);
	}

	// pour l'unicite (voir dialogparam)
	public void addItemListener(ItemListener il) {}

	public int getNumero()
	{
		return numero;
	}

	public int getType()
	{
		return TYPE_TEXT;
	}

	public Object getValue()
	{
		return getText();
	}

	public void setValue(Object val)
	{
		setText((String)val);
	}
	
	public void focusGained(FocusEvent e)
	{
		selectAll();
	}

	public void focusLost(FocusEvent e) {}
}
