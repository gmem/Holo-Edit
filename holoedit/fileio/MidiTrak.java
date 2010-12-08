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
/**
 * Classe de definition d'une piste midi pour l'ecriture des fichiers midi
 */
package holoedit.fileio;

import holoedit.data.HoloPoint;
import holoedit.data.HoloPointVector;

public class MidiTrak
{
	// le vecteur de points
	HoloPointVector points;
	// le numero du canal
	int channel;

	// constructeur et initialisation
	public MidiTrak()
	{
		points = new HoloPointVector(100, 10);
		channel = 0;
	}

	// ajout d'un point au vecteur
	void ajouterPoint(MidiPoint MidiPt) //------> 14 bits
	{
		boolean editable;
		float x, y, date;
		short xl, xm, yl, ym, z;
		editable = MidiPt.edit;
		xl = MidiPt.Xlsb;
		xm = MidiPt.Xmsb;
		yl = MidiPt.Ylsb;
		ym = MidiPt.Ymsb;
		z = MidiPt.Z;
		if (!MidiPt.empty)
		{
			//System.out.println("________________");
			//System.out.println(xm+" "+xl+" "+ym+" "+yl+" "+z);
			//System.out.println((((xm << 7) + xl + -8192 ))+" "+((ym << 7)+yl + -8192));
			x = (float) (((xm << 7) + xl + -8192) / 5.);
			y = (float) (((ym << 7) + yl + -8192) / 5.);
			date = (float) (MidiPt.date / 1.);
			points.addElement(new HoloPoint(x, -y, z, (int) date, editable));
			//System.out.println(x+" "+y+" "+z);
		}
	}

	void ajouterPoint2(MidiPoint MidiPt) //------> 7 bits
	{
		boolean editable;
		float xm, ym, z, date;
		editable = MidiPt.edit;
		xm = (float) MidiPt.Xmsb - 64;
		ym = (float) MidiPt.Ymsb - 64;
		z = MidiPt.Z;
		if (!MidiPt.empty)
		{
			if (xm >= -20. && xm <= 20.)
				xm *= 1.25;
			else
			{
				if (xm > 0)
					xm = (float) (xm * xm / 16.);
				else
					xm = (float) (xm * xm / -16.);
			}
			if (ym >= -20. && ym <= 20.)
				ym *= 1.25;
			else
			{
				if (ym > 0)
					ym = (float) (ym * ym / 16.);
				else
					ym = (float) (ym * ym / -16.);
			}
			date = (float) (MidiPt.date / 1.);
			points.addElement(new HoloPoint(xm, -ym, z, (int) date, editable));
		}
	}

	// pour le debugage...
	void printTrak()
	{
		for (HoloPoint hp:points)
			System.out.println("ch:"+channel+" "+hp.toString());
	}
}
