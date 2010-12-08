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

import java.io.File;
import java.util.Vector;

/**
 * Bibliothèque des sons importés
 */
public class HoloSoundPool
{
	private Vector<HoloWaveForm> soundPool = new Vector<HoloWaveForm>(1, 1);

	public HoloSoundPool()
	{
		soundPool = new Vector<HoloWaveForm>(1, 1);
	}

	public void add(HoloWaveForm hwf)
	{
		soundPool.add(hwf);
	}

	public HoloWaveForm get(int i)
	{
		return soundPool.get(i);
	}

	public HoloWaveForm get(String s)
	{
		if(s == null) return null;
		if(s.equalsIgnoreCase("")) return null;
		HoloWaveForm dumHwf = new HoloWaveForm(new File(s));

		if(soundPool.contains(dumHwf))
			return soundPool.get(soundPool.indexOf(dumHwf));
		return null;
	}
	
	public void remove(int i)
	{
		soundPool.remove(i);
	}

	public void remove(HoloWaveForm hwf)
	{
		soundPool.remove(hwf);
	}

	public int size()
	{
		return soundPool.size();
	}

	public String describe(int i)
	{
		return soundPool.get(i).toString();
	}

	public String toString(String parentPath)
	{
		if(soundPool.isEmpty()) return "";
		
		String tmp = "";
		for (HoloWaveForm hwf : soundPool)
			tmp += hwf.toString2(parentPath);
		return tmp;
	}

	public Vector<HoloWaveForm> getSounds()
	{
		return soundPool;
	}

	public boolean contains(HoloWaveForm h)
	{
		return soundPool.contains(h);
	}
	
	public void clear()
	{
		soundPool.clear();
	}
}
