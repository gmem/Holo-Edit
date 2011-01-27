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
import holoedit.data.HoloTraj;
import holoedit.functions.Algorithm;
import holoedit.functions.Field;
import holoedit.functions.Param;
import holoedit.gui.GestionPistes;
import holoedit.util.Ut;

public class Circular extends Algorithm
{
	public Circular(GestionPistes gp)
	{
		super(gp, TYPE_GEN, "circular", "Circular", "Generates a circular trajectory with different radius on the x and y axis (ellipse).\n(Continue pastes it at the end, Replace replaces it)");
		addField(new Field("Apply To", Param.TYPE_COMBO, 0, "applyTo"));
		addField(new Field("Track n" + Ut.numCar, Param.TYPE_COMBO, 1));
		addField(new Field("Duration (sec)", Param.TYPE_DOUBLE, 20., 0.001, Double.MAX_VALUE));
		addField(new Field("Time resolution (sec)", Param.TYPE_DOUBLE, 0.01, 0.001, Double.MAX_VALUE));
		addField(new Field("Radius", Param.TYPE_DOUBLE, 100., 0., Double.MAX_VALUE));
		addField(new Field("Ellipsoid %", Param.TYPE_DOUBLE, 0., -100., 100.));
		addField(new Field("Circles per second", Param.TYPE_DOUBLE, 0.5, 0.01, Double.MAX_VALUE));
		addField(new Field("Initial angle (" + Ut.numCar + "d)", Param.TYPE_DOUBLE, 0., -360, 360, 360));
		addField(new Field("Direction", Param.TYPE_COMBO, 0, "clock"));
		setCategory(Algorithm.CAT_GEN);
	}

	protected void treatOneTrack(int tkNth)
	{
		HoloTraj ht = new HoloTraj();
		HoloPoint curPt = new HoloPoint();
		double dur = (Double)results[2] * 1000; // 1/100e sec
		double durPoint = (Double)results[3] * 1000; // 1/100e sec
		double angle, angleElt;
		// --- parametres fournis par l'utilisateur :
		double Radius = (Double)results[4];
		double Ellipse = (Double)results[5];
		Ellipse = (Ellipse/100.)+1.;
		double Xi = Ellipse * Radius;
		double Yi = (2-Ellipse) * Radius;
		double tourParSec = (Double)results[6];
		double angleInit = (Double)results[7];
		int sens = (Integer) results[8] > 0 ? 1 : -1;
		// -----------------------------------------------------
		// -----------------------------------------------------
		angleElt = (sens * tourParSec * durPoint * 2. * Math.PI / 1000.);
		angle = angleInit = (angleInit * Math.PI / 180.);
		double curTime = 0;
		int nbPoints = (int) (dur / durPoint);
		for (int n = 0; n <= nbPoints; n++)
		{
			curTime = n * durPoint;
			curPt = new HoloPoint();
			curPt.date = (int) curTime + dateBegin;
			angle = angleInit + (n * angleElt);
			curPt.x = HoloPoint.limit2D((float) polX(angle, Xi));
			curPt.y = HoloPoint.limit2D((float) polY(angle, Yi));
			curPt.z = 0;
			ht.addElement(curPt);
			inc();
		}
		finalizeTraj(tkNth, ht, (int) dur);
	}

	protected void treatOneTrack(int tkNthFrom, int tkNthTo){}

	protected void treatOneTrack(int tkNthFromA, int tkNthFromB, int tkNthTo){}	
}
