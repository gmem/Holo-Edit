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

import java.util.Vector;
import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class Smooth extends MaxObject
{
	protected int[] INLET_TYPES = {};
	protected int[] OUTLET_TYPES = {};
	protected String[] INLET_ASSIST = {};
	protected String[] OUTLET_ASSIST = {};

	private int smooth = 1;
	private Vector<Double> stack = new Vector<Double>(10,10);
	
	public Smooth(Atom[] atoms)
	{
		INLET_TYPES = new int[] { DataTypes.ALL, DataTypes.ALL };
		OUTLET_TYPES = new int[] { DataTypes.ALL };
		INLET_ASSIST = new String[] { "ints/floats/lists flow to be smooth.","smooth value" };
		OUTLET_ASSIST = new String[] { "Result." };
		
		declareInlets(INLET_TYPES);
		declareOutlets(OUTLET_TYPES);
		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);
		createInfoOutlet(false);
		setName(this.getClass().getName().toString());
		
		switch(atoms.length)
		{
		case 1 :
			smooth(atoms[0].toInt());
		case 0:
			break;
		}
		declareAttribute("smooth");
	}
	
	public void clear()
	{
		stack.clear();
	}
	
	public void smooth(int i)
	{
		smooth = Ut.clipL(i,1);
	}
		
	public void inlet(int i)
	{
		if(getInlet() == 1)
			smooth(i);
		inlet((float)i);
	}
	
	public void inlet(float f)
	{
		if(getInlet() != 0)
			return;
		
		stack.add(new Double(f));

		calcSmooth(f);
	}

	private void calcSmooth(double d)
	{
		if(smooth<=stack.size())
		{
			double tmp = 0;
			int max = stack.size() - 1;
			for(int i = 0 ; i < smooth ; i++)
			{
				tmp += stack.get(max-i).doubleValue();
			}
			tmp /= smooth;
			outlet(0,tmp);
		} else if(!stack.isEmpty()) {
			double tmp = 0;
			int max = stack.size() - 1;
			for(int i = 0 ; i <= max ; i++)
			{
				tmp += stack.get(max-i).doubleValue();
			}
			tmp /= max+1;
			outlet(0,tmp);
		} else {
			outlet(0,d);
		}
	}
}
