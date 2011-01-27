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
package algo;

import holoedit.data.HoloPoint;
import holoedit.data.HoloTrack;
import holoedit.functions.Algorithm;
import holoedit.functions.Field;
import holoedit.functions.Param;
import holoedit.gui.GestionPistes;
import holoedit.util.Ut;

public class CoordRemap extends Algorithm
{
	public CoordRemap(GestionPistes gp)
	{
		super(gp,TYPE_TRANS_ATOB,"CoordRemap","Coord Remap","Perform a remap of the xyz coordinates.");
		addField(new Field("Apply To", Param.TYPE_COMBO, 0, "applyTo"));
		addField(new Field("Input track n" + Ut.numCar, Param.TYPE_COMBO, 1));
		addField(new Field("Output track n" + Ut.numCar, Param.TYPE_COMBO, 2));
		addField(new Field("X replaced by", Param.TYPE_COMBO, 0, "coord"));
		addField(new Field("Y replaced by", Param.TYPE_COMBO, 1, "coord"));
		addField(new Field("Z replaced by", Param.TYPE_COMBO, 2, "coord"));
		setCategory(CAT_TRANS_SPAT);
	}
	
	protected void treatOneTrack(int tkNth) {}

	protected void treatOneTrack(int tkNthFrom, int tkNthTo)
	{
		HoloTrack tmpTrack = gp.copyTrack.dupliquer(); // on remplace temporairement
		gp.Copy(tkNthFrom);
		HoloPoint tempPt, cpPt;
		// --- parametres fournis par l'utilisateur : X Y Z
		int Xsc = (Integer) results[3] ;
		int Ysc = (Integer) results[4] ;
		int Zsc = (Integer) results[5] ;
		for (int i = 0; i < gp.copyTrack.size(); i++)
		{
			tempPt = gp.copyTrack.elementAt(i);
			cpPt = tempPt.dupliquer();
			if(Xsc != 0 )
				tempPt.x = Xsc == 1 ? cpPt.y : cpPt.z;
			
			if(Ysc != 1 )
				tempPt.y = Ysc == 0 ? cpPt.x : cpPt.z;
			
			if(Zsc != 2 )
				tempPt.z = Zsc == 0 ? cpPt.x : cpPt.y;

			inc();
		}
		gp.ReplaceWithoutStore(tkNthTo);
		gp.copyTrack = tmpTrack.dupliquer();
		gp.update(tkNthTo, -1);
	}

	protected void treatOneTrack(int tkNthFromA, int tkNthFromB, int tkNthTo) {}
}
