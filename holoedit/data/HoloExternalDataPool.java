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

import java.io.File;
import java.util.Vector;

/**
 * Biblioth?que des sons importés
 */
public class HoloExternalDataPool
{
	private Vector<HoloExternalData> sdifDataPool;
	
	public HoloExternalDataPool()
	{
		sdifDataPool = new Vector<HoloExternalData>(1, 1);
	}
	
	public void add(HoloExternalData hSDIFdt){
		sdifDataPool.add(hSDIFdt);
	}

	public HoloExternalData getSDIFdata(int i){
		return sdifDataPool.get(i);
	}
	
	/** TODO */
	public HoloSDIFdata get(String fileName, String dataType){
		if(fileName == null) return null;
		if(fileName.equalsIgnoreCase("")) return null;
			
		try{
			HoloSDIFdata hsdifDt = new HoloSDIFdata(new File(fileName), dataType);
			if(sdifDataPool.contains(hsdifDt))
				return (HoloSDIFdata) sdifDataPool.get(sdifDataPool.indexOf(hsdifDt));

			Vector<HoloExternalData> vector = sdifDataPool;
			for (HoloExternalData xtdt : vector){
				try{
					HoloSDIFdata hsdif = (HoloSDIFdata) xtdt;
					if (hsdif.children.contains(hsdifDt))
						return hsdif.getChild(hsdif.getChildIndex(hsdifDt));
					for (HoloSDIFdata hsdifChild : hsdif.children)
						if (hsdifChild.children.contains(hsdifDt))
							return hsdifChild.getChild(hsdifChild.getChildIndex(hsdifDt));	
				}catch(ClassCastException cce){
					System.out.println(cce);
				}
			}
		}catch(ClassCastException cce){
			System.out.println(cce);
		}
		return null;
	}
	
	public boolean remove(HoloSDIFdata hsdifDt){
		boolean b = sdifDataPool.remove(hsdifDt);
		if (!b) {
			HoloSDIFdata parent = hsdifDt.getParent();
			while (parent!= null && !sdifDataPool.contains(parent))
				parent = parent.getParent();	

			if (parent!=null){
				b = parent.removeChild(hsdifDt);
				if (b && parent.getChildrenCount()==0)
					this.remove(parent);
			}
		}		
		return b;
	}

	public Vector<HoloExternalData> getSDIFdatas(){
		return sdifDataPool;
	}
	
	/** Returns true if this collection contains the specified element. 
	 *	More formally, returns true if and only if this collection contains
	 *  at least one element e such that (h==null ? e==null : h.equals(e)).
	 *  @param h - element whose presence in this sdifDataPool is to be tested.
	 *  @return true if this collection contains the specified element
	 *  @see holoedit.data.HoloExternalData#equals(Object o)
	 *  */
	public boolean contains(HoloExternalData h){
		return sdifDataPool.contains(h);
	}
		
	public void clear()	{
		for (HoloExternalData dt : sdifDataPool) {
			try {
				((HoloSDIFdata) dt).sdifTreeMap = null;
			} catch (ClassCastException cce){
				// not a holoSDIFdata
			} finally {
				dt = null;
			}
		}
		sdifDataPool.clear();
	}
	/**
	 * used to write ".holo" file.
	 * see holoedit.fileio.HoloFileWriter
	 * @param parentPath
	 * @return
	 */
	public String toString(String parentPath){
		if(sdifDataPool.isEmpty()) return "";
		
		String tmp = "";
		for (HoloExternalData hdt : sdifDataPool)
			tmp += hdt.toString2(parentPath);
		return tmp;
	}
	
	public int size(){
		return sdifDataPool.size();
	}
}
