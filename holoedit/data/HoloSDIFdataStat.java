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

import java.util.Vector;
import java.util.Collections;
import java.lang.Math;

/** This class is used to collect statistical datas about HoloSDIFdata instances. */
public final class HoloSDIFdataStat {

	private HoloSDIFdata holoSDIFdata;
	
	/** Contains the maximal values of an HoloSDIFdata instance. */
	public Vector<Double> maxVector = new Vector<Double>(1);
	/** Contains the minimal values of an HoloSDIFdata instance. */
	public Vector<Double> minVector = new Vector<Double>(1);
	/** Contains the mean values of an HoloSDIFdata instance. */
	public Vector<Double> meanVector = new Vector<Double>(1);
	/** Contains the median values of an HoloSDIFdata instance. */
	public Vector<Double> medianVector = new Vector<Double>(1);
	/** Contains the ranges of an HoloSDIFdata instance. */
	public Vector<Double> rangeVector;
	/** Contains the ecart moyen values of an HoloSDIFdata instance. */
	public Vector<Double> ecartMoyenVector = new Vector<Double>(1);
	/** Contains the variance values of an HoloSDIFdata instance. */
	public Vector<Double> varianceVector = new Vector<Double>(1);
	/** Contains the ecarttype values of an HoloSDIFdata instance. */
	public Vector<Double> ecartTypeVector = new Vector<Double>(1);

	/** Le nombre d'instants pour lesquels on a une valeur = nrb de key du treemap. */
	public int nbrTimes;
		
	// used in the 'calculMean' method.
	private int nbrValues = 0;
	public Vector<Double> addedColValuesArray = new Vector<Double>(1);
	// used in the 'calculMedian' method.
	public Vector<Vector<Double>> colValuesArray = new Vector<Vector<Double>>(1);
	
	public Vector<Vector<Double>> ecartsArray = new Vector<Vector<Double>>(1);
	
	
	public HoloSDIFdataStat(HoloSDIFdata holoSDIFdata) {
		this.holoSDIFdata = holoSDIFdata;
		
		if (holoSDIFdata.getChildrenCount()==0) {		
			doStat(holoSDIFdata);
		}else if (holoSDIFdata.getChildrenCount()==1) {			
			// meme valeur que celle du fils unique
			this.maxVector = holoSDIFdata.getChild(0).getHoloDataStat().maxVector;
			nbrTimes = getNbrTimes(holoSDIFdata);
			this.minVector = holoSDIFdata.getChild(0).getHoloDataStat().minVector;
			this.rangeVector = holoSDIFdata.getChild(0).getHoloDataStat().rangeVector;
			this.meanVector = holoSDIFdata.getChild(0).getHoloDataStat().meanVector;
		} else {			
			this.maxVector = new Vector<Double>(holoSDIFdata.getChild(0).getHoloDataStat().maxVector);
			nbrTimes = getNbrTimes(holoSDIFdata);
			this.minVector = new Vector<Double>(holoSDIFdata.getChild(0).getHoloDataStat().minVector);			
			for (int i=1; i<holoSDIFdata.getChildrenCount() && holoSDIFdata.sdifTreeMap.size()>0; i++){
				try {
					for (int j=0; j< this.maxVector.size(); j++){
						this.maxVector.set(j, Math.max(this.maxVector.get(j), holoSDIFdata.getChild(i).getHoloDataStat().maxVector.get(j)));
						this.minVector.set(j, Math.min(this.minVector.get(j), holoSDIFdata.getChild(i).getHoloDataStat().minVector.get(j)));
					}
				}catch(IndexOutOfBoundsException iobe){
					iobe.printStackTrace();
				}
			}
			calculRange(); // range = max-min
		}
	}
	
	public HoloSDIFdataStat(HoloSDIFdataStat holoSDIFdataStat) {
		this.maxVector = new Vector<Double>(holoSDIFdataStat.maxVector);
		this.minVector = new Vector<Double>(holoSDIFdataStat.minVector);
		this.rangeVector = new Vector<Double>(holoSDIFdataStat.rangeVector);
		this.meanVector = new Vector<Double>(holoSDIFdataStat.meanVector);
		
	}
	/** Calcule les stats d'un HoloSDIFdata. */
	public void doStat(HoloSDIFdata holoSDIFdata)
	{			
		calculMinAndMax(holoSDIFdata);
		nbrTimes = getNbrTimes(holoSDIFdata);
		calculRange(); // range = max-min
		calculMean(holoSDIFdata);
	}
	
	public int getNbrTimes(HoloSDIFdata holoSDIFdata){
		Vector<HoloSDIFdata> children = holoSDIFdata.children;
		if (!holoSDIFdata.sdifTreeMap.isEmpty())
			return holoSDIFdata.sdifTreeMap.keySet().size();
		for(HoloSDIFdata child : children)
			nbrTimes += getNbrTimes(child);
		return nbrTimes;
	}
	

	/** calcul un Vector contenant les valeurs maximales et minimales de toutes les colones des treemaps d'un HoloSDIFdata. */
	private void calculMinAndMax(HoloSDIFdata holoSDIFdata)
	{
		Vector<Double> treemapColValues;		
		if (!holoSDIFdata.sdifTreeMap.isEmpty())
		{	
			int nbrColumns = holoSDIFdata.sdifTreeMap.get(holoSDIFdata.sdifTreeMap.firstKey()).get(0).size();
			for (int i=0; i<nbrColumns; i++){
				treemapColValues = new Vector<Double>();
				for (Vector<Vector<Double>> vect1 : holoSDIFdata.sdifTreeMap.values())
					for (Vector<Double> vect2 : vect1)
						treemapColValues.add(vect2.get(i));
				Double treemapColMax = Collections.max(treemapColValues);
				if (maxVector.size()==i)
					maxVector.add(treemapColMax);
				else if (maxVector.get(i) < treemapColMax)
					maxVector.set(i, treemapColMax);
				Double treemapColMin = Collections.min(treemapColValues);
				if (minVector.size()==i)
					minVector.add(treemapColMin);
				else if (minVector.get(i) > treemapColMin)
					minVector.set(i, treemapColMin);
			}			
		}else
			return;
	}
	
	/** Retourne un Vector contenant les valeurs moyennes de toutes les colones des treemaps d'un HoloSDIFdata.
	 */
	private void calculMean(HoloSDIFdata holoSDIFdata)
	{
		Vector<HoloSDIFdata> children = holoSDIFdata.children;
		Double value = 0d;		
		if (!holoSDIFdata.sdifTreeMap.isEmpty())
		{	
			int nbrColumns = holoSDIFdata.sdifTreeMap.get(holoSDIFdata.sdifTreeMap.firstKey()).get(0).size();
			for (int i=0; i<nbrColumns; i++){
				for (Vector<Vector<Double>> vect1 : holoSDIFdata.sdifTreeMap.values())
					for (Vector<Double> vect2 : vect1){
						nbrValues++;
						value = vect2.get(i);
						if (addedColValuesArray.size()==i)
							addedColValuesArray.add(value);
						else
							addedColValuesArray.set(i, addedColValuesArray.get(i)+value);
					}
			}
			nbrValues /= nbrColumns;
		}else
			for(HoloSDIFdata child : children)
				calculMean(child);
			
		for (int i=0; i<addedColValuesArray.size(); i++){
			if (meanVector.size()==i)
				meanVector.add((addedColValuesArray.get(i))/nbrValues);
			else
				meanVector.set(i, (addedColValuesArray.get(i))/nbrValues);
		}
	}
	
	/** Retourne un Vector contenant les ranges de toutes les colones des treemaps d'un HoloSDIFdata.
	 */
	private void calculRange()
	{
		rangeVector = new Vector<Double>(maxVector);
		for (int i=0; i<maxVector.size(); i++)
			rangeVector.set(i, rangeVector.get(i)-minVector.get(i));		
	}
	
	/** Calcule et retourne le plus grand endTime de tous les fichiers SDIF
	 * dont sont issues des holoSDIFdatas	*/ 
	public static float getBiggestSDIFendTime(HoloSDIFdata[] _hsdifdt)
	{
		HoloSDIFdata[] hsdifdtTab = _hsdifdt;
		float biggestSDIFendTime = 0f;
		for (HoloSDIFdata hsdifdt : hsdifdtTab)
			if(hsdifdt != null){
				biggestSDIFendTime = (float) Math.max(biggestSDIFendTime, hsdifdt.getSDIFendTime());
			}
		return biggestSDIFendTime;
	}
		
	/** Calcule et retourne le plus petit startTime de tous les fichiers SDIF
	 * dont sont issues des holoSDIFdatas	*/ 
	public static float getSmallestSDIFstartTime(HoloSDIFdata[] _hsdifdt)
	{
		HoloSDIFdata[] hsdifdtTab = _hsdifdt;
		float smallestSDIFendTime = 10000f;
		for (HoloSDIFdata hsdifdt : hsdifdtTab)
			if(hsdifdt != null)
				smallestSDIFendTime = (float) Math.min(smallestSDIFendTime, hsdifdt.getSDIFstartTime());
		return smallestSDIFendTime;
	}
	
	/** Retourne la plus grande valeur a dessiner des holoSDIFdatas */ 
	public static float getBiggestY(HoloSDIFdata[] _hsdifdt)
	{
		HoloSDIFdata[] hsdifdtTab = _hsdifdt;
		float biggestY = Float.MIN_VALUE;
		for (HoloSDIFdata hsdifdt : hsdifdtTab)
			if(hsdifdt != null){
				biggestY = (float) Math.max(biggestY, hsdifdt.getMaxY());
			}
		return biggestY;
	}
	
	/** Retourne la plus petie valeur a dessiner des holoSDIFdatas */ 
	public static float getSmallestY(HoloSDIFdata[] _hsdifdt)
	{
		HoloSDIFdata[] hsdifdtTab = _hsdifdt;
		float smallestY = Float.MAX_VALUE;
		for (HoloSDIFdata hsdifdt : hsdifdtTab)
			if(hsdifdt != null){
				smallestY = (float) Math.min(smallestY, hsdifdt.getMinY());
			}
		return smallestY;
	}
}
