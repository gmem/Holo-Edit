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
package holoedit.util;

import holoedit.functions.Algorithm;
import java.util.Vector;

public class AlgoVector extends AbstractVectorSort<Algorithm>
{
	public AlgoVector(Vector<Algorithm> v)
	{
		super(v);
	}

	public AlgoVector(int cap, int inc)
	{
		super(cap,inc);
	}
	
	public boolean lessThan(Algorithm obj1, Algorithm obj2)
	{
		if(obj1.getCategory().equalsIgnoreCase(obj2.getCategory()))
		{
			String n1 = obj1.getName();
			String n2 = obj2.getName();
			int ind = 0;
			boolean equals = true;
			while(n1.charAt(ind) == n2.charAt(0))
				ind++;
			return n1.charAt(ind) < n2.charAt(ind);
		}
		return (obj1.getCategory().compareTo(obj2.getCategory()) < 0);
	}

	public boolean lessThanOrEqual(Algorithm obj1, Algorithm obj2)
	{
		if(obj1.getCategory().equalsIgnoreCase(obj2.getCategory()))
		{
			String n1 = obj1.getName();
			String n2 = obj2.getName();
			int ind = 0;
			boolean equals = true;
			while(n1.charAt(ind) == n2.charAt(0))
				ind++;
			return n1.charAt(ind) <= n2.charAt(ind);
		}
		return (obj1.getCategory().compareTo(obj2.getCategory()) <= 0);
	}

	public Vector<Algorithm> sort(Vector<Algorithm> v)
	{
		return (new AlgoVector(v)).getVector();
	}
}
