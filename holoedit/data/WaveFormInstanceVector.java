/*
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

import holoedit.util.AbstractVectorSort;
import java.util.Vector;

/**
 * this class enable to sort a vector of LumSeq by their date value (in our case it's time)
 */
public class WaveFormInstanceVector extends AbstractVectorSort<WaveFormInstance>
{
	public WaveFormInstanceVector(Vector<WaveFormInstance> v)
	{
		super(v);
	}

	public WaveFormInstanceVector(int cap, int inc)
	{
		super(cap,inc);
	}
	
	public boolean lessThan(WaveFormInstance obj1, WaveFormInstance obj2)
	{
		if(obj1.getFirstDate() == obj2.getFirstDate())
			return (obj1.getDuration() < obj2.getDuration());
		return (obj1.getFirstDate() < obj2.getFirstDate());
	}

	public boolean lessThanOrEqual(WaveFormInstance obj1, WaveFormInstance obj2)
	{
		if(obj1.getFirstDate() < obj2.getFirstDate())
			return true;
		else if(obj1.getFirstDate() == obj2.getFirstDate())
			return (obj1.getDuration() <= obj2.getDuration());
		else
			return false;
	}

	public Vector<WaveFormInstance> sort(Vector<WaveFormInstance> v)
	{
		return (new WaveFormInstanceVector(v)).getVector();
	}
	/** retourne l'index de la premiere WaveFormInstance dont la représentation en String (donnée par toString)
	 * est égale à la description 'desc' donnée.
	 */
	public int indexOf(String desc){
		int index = -1;
		for (WaveFormInstance wi : this){
			if (wi.toString().equals(desc))
				index = this.indexOf(wi);
		}		
		return index;
	}
	/** retourne la premiere WaveFormInstance dont la représentation en String (donnée par toString)
	 * est égale à la description 'desc' donnée.
	 */
	public WaveFormInstance get(String desc){
		return this.get(indexOf(desc));
	}
}
