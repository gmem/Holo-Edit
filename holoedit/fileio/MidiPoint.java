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
 * Classe de definition d'un point midi pour l'ecriture des fichiers midi
 */
package holoedit.fileio;

public class MidiPoint
{
	boolean edit, empty;
	byte CH, nvals;
	short Xlsb, Xmsb, Ylsb, Ymsb, Z;
	float date;

	public MidiPoint()
	{
		this.empty = true;
		this.edit = false;
		this.nvals = 0;
		this.Z = 0;
		this.Xlsb = -10000;
		this.Xmsb = -10000;
		this.Ylsb = -10000;
		this.Ymsb = -10000;
	}

	public boolean isReady()
	{
		if ((Xlsb != -10000) && (Xmsb != -10000) && (Ylsb != -10000) && (Ymsb != -10000))
			return true;
		return false;
	}
}
