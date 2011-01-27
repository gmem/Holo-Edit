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
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.JButton;

public class ButtonParam extends JButton implements Param
{
	// attributs graphiques
	private int w = 90;
	private int h = 25;
	private Color selColor = Color.white;
	// numero du champ
	private int numero;
	// valeur
	private String value;

	public ButtonParam(int _numero, Object _val,ActionListener a)
	{
		super();
		numero = _numero;
		setValue(_val);
		addActionListener(a);
		init();
	}
	
	public void init()
	{
		setFont(f);
		setVisible(true);
		setHorizontalAlignment(JButton.CENTER);
		setSize(w, h);
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
		return value;
	}

	public void setValue(Object val)
	{
		value = (String)val;
		setText(value);
	}
}
