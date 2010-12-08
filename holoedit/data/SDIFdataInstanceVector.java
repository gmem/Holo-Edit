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

import holoedit.util.AbstractVectorSort;
import java.util.Vector;

/**
 * this class enable to sort a vector of SDIFdataInstance by their date value (in our case it's time)
 */
public class SDIFdataInstanceVector extends AbstractVectorSort<SDIFdataInstance> {
	public SDIFdataInstanceVector(Vector<SDIFdataInstance> v) {
		super(v);
	}

	public SDIFdataInstanceVector(int cap, int inc) {
		super(cap, inc);
	}

	public boolean lessThan(SDIFdataInstance obj1, SDIFdataInstance obj2) {
		if (obj1.getFirstDate() == obj2.getFirstDate())
			return (obj1.getDuration() < obj2.getDuration());
		return (obj1.getFirstDate() < obj2.getFirstDate());
	}

	public boolean lessThanOrEqual(SDIFdataInstance obj1, SDIFdataInstance obj2) {
		if (obj1.getFirstDate() < obj2.getFirstDate())
			return true;
		else if (obj1.getFirstDate() == obj2.getFirstDate())
			return (obj1.getDuration() <= obj2.getDuration());
		else
			return false;
	}

	public Vector<SDIFdataInstance> sort(Vector<SDIFdataInstance> v) {
		return (new SDIFdataInstanceVector(v)).getVector();
	}
}
