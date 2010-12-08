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
package holoedit.data;

import java.util.*;

public class HoloFctPreset
{
	public String pName; // nom du preset
	public String pAlgor; // fonction auquel le preset se rapporte;
	public Vector<Object> pVals; // valeurs

	// divers constructeurs
	public HoloFctPreset()
	{}

	public HoloFctPreset(String presetName, String presetAlgor)
	{
		this.pName = presetName;
		this.pAlgor = presetAlgor;
		this.pVals = new Vector<Object>(16, 1);
	}

	public HoloFctPreset(String presetName, String presetAlgor, Vector<Object> vals)
	{
		this.pName = presetName;
		this.pAlgor = presetAlgor;
		this.pVals = vals;
	}

	// retourne les valeurs du preset
	public Object[] getVals()
	{
		int n = this.pVals.size();
		Object[] outVals = new Object[n];
		for (int i = 0; i < n; i++)
			outVals[i] = pVals.elementAt(i);
		return outVals;
	}

	// affecte les valeurs
	public HoloFctPreset setVals(Object[] inVals)
	{
		pVals = new Vector<Object>(16, 1);
		for (Object d : inVals)
			pVals.addElement(d);
		return this;
	}

	// retourne le nombre valeurs memorisees dans le preset
	public int getSize()
	{
		return this.pVals.size();
	}
	
	public void update(String name, Object[] vals)
	{
		pName = name;
		pVals = new Vector<Object>(16, 1);
		for (Object d : vals)
			pVals.addElement(d);
	}
	
	public String toString()
	{
//		String tmp = "\t<preset name=\""+pName+"\" algo=\""+pAlgor+"\" params=\"";
		String tmp = "\t<preset name=\""+pName+"\" algo=\""+pAlgor+"\" params=\"";
		for (Object d : pVals)
			tmp = tmp + d + "#";
		tmp = tmp.substring(0,tmp.length()-1);
		tmp += "\"/>\n";
		return tmp;
	}
	
	// test d'egalite entre deux presets "==" (si pour chacun des presets de part et d'autre de l'egalite
	// le nom et l'algorithme associe sont egaux
	public boolean equals(Object o)
	{
		try
		{
			HoloFctPreset comPreset = (HoloFctPreset) o;
			return equals(comPreset);
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	public boolean equals(HoloFctPreset comPreset)
	{
		return ((this.pName.equalsIgnoreCase(comPreset.pName)) && (this.pAlgor.equalsIgnoreCase(comPreset.pAlgor)));
	}
}
