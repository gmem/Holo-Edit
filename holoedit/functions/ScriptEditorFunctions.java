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

package holoedit.functions;

import holoedit.data.SDIFdataInstance;
import holoedit.data.HoloPoint;
import holoedit.gui.GestionPistes;
import holoedit.util.Ut;

import java.util.Vector;

/** This class provides some methods whose names are easily understandable,
 * and that can be used directly in the textArea of the script editor window.*/
public final class ScriptEditorFunctions {
	
	// astuce : gp est un objet static, mais pas gp[0]
	public static GestionPistes[] gp = new GestionPistes[1];
	
	/** ScriptEditorFunctions is non-instantiable.*/
	private ScriptEditorFunctions() {}
	
	/**
	 * Returns the holoSDIFdata that corresponds to the given description
	 * sdifdataDesc.
	 * @param sdifdataDesc the description of an holoSDIFdata located in the score.
	 * @return The holoSDIFdata that corresponds to the given description.
	 */
	public static SDIFdataInstance getSDIFdata(String sdifdataDesc){
		String[] split = sdifdataDesc.split(" - Track:", 2);
		holoedit.data.HoloTrack ht = gp[0].getTrack(Integer.valueOf(split[1]));
		return ht.getSDIFinstance(split[0]);
	}
	
	/** Returns the values corresponding to the given SCORE time in ms 
	 * if the given time is equal to a key of the holoSDIFdata's map.<br>
	 * If the given time is not equal to a key, it returns a linear interpolation
	 * of the values corresponding to the two closest key.<br>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the map keyset range.*/
	public static Vector<Vector<Double>> getAllDataAtTime(SDIFdataInstance sdifdata, double scoreTime) {
		return sdifdata.getDataAtTime(scoreTime);
	}
	
	/** Returns The field corresponding to the given field index at the given SCORE time in ms 
	 * if the given time is equal to a key of the holoSDIFdata's map.<br>
	 * If the given time is not equal to a key, it returns a linear interpolation
	 * of the field values corresponding to the two closest key.<br>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the map keyset range.*/
	public static Vector<Double> getFieldAtTime(SDIFdataInstance sdifdata, double scoreTime, int fieldIndex) {
		return sdifdata.getFieldAtTime(scoreTime, fieldIndex);
	}
	
	/** Returns the number of values contained by the sdifdata at the given SCORE time in ms
	 * */
	public static int howManyDataAtTime(SDIFdataInstance sdifdata, double scoreTime) {
		try {
			return sdifdata.getDataAtTime(scoreTime).size();
		} catch (NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * Returns an array of double values corresponding to the frame times
	 * of the given sdifdata, and listed in natural order.
	 * @return An array containing all the times of the holoSDIFdata, listed in natural order.
	 */
	public static Double[] getTimes(SDIFdataInstance sdifdata) {
		return sdifdata.getTimes();
	}
	
	/**
	 * @return A boolean if time is within range of the treeMap keyset
	 */
	public static boolean hasDataAtTime(SDIFdataInstance sdifdata,double time) {
		return sdifdata.hasDataAtTime(time);
	}
	
	/**
	 * Returns the number of time samples that are contained
	 * by the given HoloSDIFdata.
	 * @return The number of time samples contained by the given holoSDIFdata.
	 */
	public static double getTimesCount(SDIFdataInstance sdifdata){
		return sdifdata.getData().sdifTreeMap.size();
	}
	
	/**
	 * Returns the smallest value obtained from the sdifdata
	 * for the field corresponding to the given fieldIndex.
	 */
	public static double minFieldValue(SDIFdataInstance sdifdata, int fieldIndex) {
		return sdifdata.getData().getHoloDataStat().minVector.get(fieldIndex);
	}
	
	/**
	 * Returns the largest value obtained from the sdifdata's field
	 * that corresponds to the given fieldIndex. 
	 */
	public static double maxFieldValue(SDIFdataInstance sdifdata, int fieldIndex) {
		return sdifdata.getData().getHoloDataStat().maxVector.get(fieldIndex);
	}
	
	/**
	 * Returns the mean value obtained from the sdifdata's field
	 * that corresponds to the given fieldIndex.
	 */
	public static double meanFieldValue(SDIFdataInstance sdifdata, int fieldIndex) {
		return sdifdata.getData().getHoloDataStat().meanVector.get(fieldIndex);
	}
	
	/**
	 * Returns the range (i.e. maxValue-minValue) of the sdifdata's field
	 * that corresponds to the given fieldIndex.
	 * @return A double value giving the range of the specified field of the specified holoSDIFdata.
	 */
	public static double rangeFieldValue(SDIFdataInstance sdifdata, int fieldIndex) {
		return sdifdata.getData().getHoloDataStat().rangeVector.get(fieldIndex);
	}
	
	/**
	 * Returns the date at which the score selection begins.<br>
	 * Usage in a script :<br>
	 * int dateBegin = getBeginDate(gp); <br>
	 * @return The date at which the score selection begins. 
	 */
	public static int getBeginDate(){		
		return gp[0].holoEditRef.counterPanel.getDate(1);
	}
	
	/**
	 * Returns the date at which the score selection is ending.<br>
	 * Usage in a script :<br>
	 * int endDate = getEndDate(gp);<br>
	 * @return The date at which the score selection is ending. 
	 */
	public static int getEndDate(){		
		return gp[0].holoEditRef.counterPanel.getDate(2);
	}
	
	/**
	 * Returns the length of the score selection.<br>
	 * Usage in a script :<br>
	 * int dur = getDuration(gp);<br>
	 * @return The length of the score selection. 
	 */
	public static int getDuration(){
		return gp[0].holoEditRef.counterPanel.getDate(2) - gp[0].holoEditRef.counterPanel.getDate(1);
	}
	
	/**
	 * Returns A point of the selected trajectory, according to the given index.
	 * The trackNumber is defined by the ComboBoxes of the left panel of the ScriptEditor Window.
	 * @param pointIndex Index of the point.
	 * @return  A point of the selected trajectory.
	 */
	public static HoloPoint getTrajectoryPoint(int pointIndex){
		return gp[0].copyTrack.elementAt(pointIndex);
	}
	
	/**
	 * Returns A point of the selected trajectory, according to the given time.
	 * The trackNumber is defined by the ComboBoxes of the left panel of the ScriptEditor Window.
	 * @param pointTime time of the point.
	 * @return  A point of the selected trajectory.
	 */
	public static HoloPoint getTrajectoryPointTime(int pointTime){
		return gp[0].copyTrack.elementAt(gp[0].copyTrack.previousPoint(pointTime-gp[0].holoEditRef.counterPanel.getDate(1))); // parce que copytrack est decalée
	}
	
	/**
	 * Returns A point of the selected trajectory, according to the given time.
	 * The trackNumber is defined by the ComboBoxes of the left panel of the ScriptEditor Window.
	 * @param pointTime time of the point.
	 * @return  A point of the selected trajectory.
	 */
	public static HoloPoint getTrajectoryPointTimePol(float pointTime){
		int a = gp[0].copyTrack.previousPoint((int)pointTime-gp[0].holoEditRef.counterPanel.getDate(1));
		int b = a+1;
		if(b>=gp[0].copyTrack.size())
			b = gp[0].copyTrack.size();
		HoloPoint pa = gp[0].copyTrack.elementAt(a);
		HoloPoint pb = gp[0].copyTrack.elementAt(b);
		if(pa == null || pb == null)
			return null;
		return HoloPoint.interpol(pa,pb,(int) pointTime-gp[0].holoEditRef.counterPanel.getDate(1)); // parce que copytrack est decalée
	}
	
	/**
	 * Returns The number of points of the selected trajectory.
	 * The trackNumber is defined by the ComboBoxes of the left panel of the ScriptEditor Window.
	 * @return How many points are contained by the selected part of a trajectory.
	 */
	public static int getTrajectorySize(){
		return gp[0].copyTrack.size();
	}
	
	public static void post(String s)
	{
		System.out.println(s);
	}
}
