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

import javax.swing.JCheckBox;

public class CheckParam extends JCheckBox implements Param
{
	private int numero;

	public CheckParam(int _numero, Object o)
	{
		super();
		numero = _numero;
		setValue(o);
		setSize(20, 20);
		setVisible(true);
	}

	public int getNumero()
	{
		return numero;
	}

	public int getType()
	{
		return TYPE_CHECK;
	}

	public Object getValue()
	{
		return isSelected();
	}

	public void setValue(Object val)
	{
		if(val.getClass().equals(String.class))
			setSelected(Boolean.parseBoolean((String)val));
		else
			setSelected((Boolean)val);
	}
}
