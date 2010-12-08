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

import holoedit.opengl.OpenGLUt;
import holoedit.util.Ut;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;
import java.util.Date;
import java.util.TreeMap;
import java.util.HashMap;
import javax.media.opengl.GL;
import java.lang.String;

public class HoloSDIFdata extends HoloExternalData {
	
	/** Date de début des données 
	 * (firstKey of this HoloSDIFdata sdifTreeMap) */
	private double startTime;
	/** Date de fin des données */
	private double endTime;
	/** nbre de frames décrites pr le SDIF*/	
	private int sdifNbrFrame;
	/** Index sur les values des treeMap
	 * dont on se sert pour dessiner. */
	private int valuesToDrawIndex = -1;
	private int drawStyle; // point or linestrip, ie : GL.GL_POINTS or GL.GL_LINE_STRIP.
	
	private HoloSDIFdata parent;
	public TreeMap<Double, Vector<Vector<Double>>> sdifTreeMap = new TreeMap<Double, Vector<Vector<Double>>>();
	public Vector<HoloSDIFdata> children = new Vector<HoloSDIFdata>();
	private String[] fields;
	
	private float minY = Float.MAX_VALUE; // la plus petite valeur a dessiner
	private float maxY = Float.MIN_VALUE; // la plus grande valeur a dessiner
	
	public int listID = -1;
	public float maximum;
	public int cutBeg = 0;
	public int cutEnd = 0;
	public boolean reversed = false;
	public int initialTreeMapSize = 0;
	private boolean dirty = true;
	
	// options pour l'import (utilisée par HoloFileWriter et HoloFileReader)
	// les index group et draw choisis par l'utilisateur pour chaque type de matrice
	// KEY : String : noms des matrices présentes
	// VALUES : int[0] : group index choisi
	// 			int[1] : draw index choisi
	public HashMap<String, int[]> indexImportOptions = new HashMap<String, int[]>();
	// options pour l'import (utilisée par HoloFileWriter et HoloFileReader)
	// les fields de matrices definis par l'utilisateur pour chaque type de matrice
	// KEY : String : noms des matrices présentes
	// VALUES :String[] : noms des fields/colones
	public HashMap<String, String[]> fieldsImportOptions = new HashMap<String, String[]>();
	
	// un vector contenant les holoSDIFdatas crée à partir de cette holoSDIFdata (lors
	// de la création d'une SDIFdataInstance.
	// on s'en sert pour effacer les sdifDataInstances du score si cette holoSDIFdata est
	// effacée de la soudPool
	public Vector<HoloSDIFdata> createdSDIFvector = new Vector<HoloSDIFdata>();
	
	private HoloSDIFdataStat holoDataStat;
	
	/** Constructs an holoSDIFData from a file and sets its 
	 * {@link HoloExternalData#dataType dataType} by default to "SDIF". */
	public HoloSDIFdata(File f){
		super(f, "SDIF");
	}
	
	/** Constructs an holoSDIFData from a file and sets its 
	 * {@link HoloExternalData#dataType dataType} to the given dataType.*/
	public HoloSDIFdata(File f, String dataType){
		super(f, dataType);
	}
	/** Constructs an holoSDIFData from an existing holoSDIFData, 
	 * and sets its parent to the given parent.*/
	public HoloSDIFdata(HoloSDIFdata h, HoloSDIFdata parent)
	{
		super(h.extDataFile, h.dataType);
		sdifTreeMap = new TreeMap<Double, Vector<Vector<Double>>>();
		// creation d'un nouveau treemap avec de nouvelles Keys, mais values par reference...
		for (Double key : h.sdifTreeMap.keySet())
			sdifTreeMap.put(new Double(key), h.sdifTreeMap.get(key));
		holoDataStat = new HoloSDIFdataStat(h.getHoloDataStat());
		minY = h.minY;
		maxY = h.maxY;
		init(h.startTime, h.dataType, h.valuesToDrawIndex, h.drawStyle, h.fields);
		endTime = h.endTime;
		setExtDataLength(h.getExtDataLength());
		setFine(h.isFine());
		setParent(parent);
		initialTreeMapSize = h.initialTreeMapSize;
		h.createdSDIFvector.add(this);
	}

	/**
	 * Pour l'initialisation des parametres de la holoSDIFdata
	 * @param startTime
	 * @param type
	 * @param drawCol
	 * @param drawStyle
	 * @param sdifFields
	 */
	public void init(double startTime, String type, int drawCol, int drawStyle, String[] sdifFields) {
		this.startTime = startTime;
		this.dataType = type;
		setValuesToDrawIndex(drawCol);
		this.drawStyle = drawStyle;
		this.fields = sdifFields;
	}
	
	/** Pour l'affichage dans la soundpool */	
	public int drawSoundPool(GL gl, float[] color, int ID) {
		OpenGLUt.glColor(gl, color);

		if (sdifTreeMap.isEmpty() && this.children.size()==0){
			System.err.println("Null Data");
			return ID;
		}

		if (listID == -1)
		{
			listID = ID;
			ID++;
			gl.glNewList(this.listID, GL.GL_COMPILE_AND_EXECUTE);
			createList(gl, this);
			gl.glEndList();
		} else if (dirty) {
			gl.glNewList(this.listID, GL.GL_COMPILE_AND_EXECUTE);
			createList(gl, this);
			gl.glEndList();
			dirty = false;
		} else {
			OpenGLUt.glColor(gl, color);
			gl.glCallList(this.listID);
		}		
		return ID;
	}
	/** Create the lists to draw in the soundPool*/
	private void createList(GL gl, HoloSDIFdata holoSDIFdata) {
		TreeMap<Double, Vector<Vector<Double>>> sdifTreeMap = holoSDIFdata.sdifTreeMap;
		
		if (!sdifTreeMap.isEmpty()) {
				gl.glLineWidth(1);
				gl.glPointSize(2);
				if (sdifTreeMap.size()>1) {
					gl.glBegin(holoSDIFdata.drawStyle);
					for (Double key : sdifTreeMap.keySet()){
						if (sdifTreeMap.get(key).size()>1){ // plusieurs Vector<Double> présents au meme temps !
							for (Vector<Double> vect : sdifTreeMap.get(key)){
								OpenGLUt.drawPoint(gl, key.floatValue(), vect.get(valuesToDrawIndex).floatValue());
							}
						}else{							
							OpenGLUt.drawPoint(gl, key.floatValue(), sdifTreeMap.get(key).get(0).get(valuesToDrawIndex).floatValue());		
						}
					}
				} else if (holoSDIFdata.getStartTime()!=holoSDIFdata.getEndTime()) {
					gl.glBegin(GL.GL_LINES);
					for (Double key : sdifTreeMap.keySet()){
						for (Vector<Double> vect : sdifTreeMap.get(key)){
							OpenGLUt.drawPoint(gl, (float) holoSDIFdata.getStartTime(), vect.get(valuesToDrawIndex).floatValue());
							OpenGLUt.drawPoint(gl, (float) holoSDIFdata.getEndTime(), vect.get(valuesToDrawIndex).floatValue());
						}
					}
				} else {
					gl.glBegin(GL.GL_POINTS);
					for (Double key : sdifTreeMap.keySet())
						for (Vector<Double> vect : sdifTreeMap.get(key))
							OpenGLUt.drawPoint(gl, (float) holoSDIFdata.getStartTime(), vect.get(valuesToDrawIndex).floatValue());
				}
				gl.glEnd();
		}else{
			for (HoloSDIFdata child : holoSDIFdata.children){
				child.createList(gl, child);
			}
		}
	}

	public void setSDIFtreeMap(TreeMap<Double, Vector<Vector<Double>>> sdifTreeMap){
		this.sdifTreeMap = sdifTreeMap;
	}
	public TreeMap<Double, Vector<Vector<Double>>> getSDIFtreeMap(){
		return this.sdifTreeMap;
	}
	
	/** Returns false if this holoSDIFmap or if one of this 
	 * holoSDIFdata's child sdifTreeMap contains some key-value mappings.
	 * Returns true otherwise. */
	public boolean isEmpty()
	{
		if (sdifTreeMap.isEmpty())
			return true;
		return false;
	}
	
	// CHILDREN/PARENT METHODS ********************************************///
	/** Adds a child to the HoloSDIFdata instance. 
	 * @param child The child to be added.*/
	public void addChild(HoloSDIFdata child){
		this.children.add(child);
	}
	
	/** Removes a child of the HoloSDIFdata instance.
	* @param child The child to be removed.*/
	public boolean removeChild(HoloSDIFdata child){
		boolean b = this.children.remove(child);
		if (!b && this.getChildrenCount()!=0){
			int size = this.children.size();
			for (int i=0; i< size; i++)
				b = children.get(i).removeChild(child);
		}
		if (this.getChildrenCount()==0 && this.getParent()!=null) // on fait harakiri si on a plus d'enfant
			this.getParent().removeChild(this);	
		return b;
	}

	/** Returns the child at the specified position in the children Vector.*/
	public HoloSDIFdata getChild(int index)
	{
		return this.children.get(index);
	}
	/** Set the children Vector to the specified Vector.
	 * @param children The Vector (containing the children) to be set.*/
	/*public void setChildren(Vector<HoloSDIFdata> children){
		this.children = children;
	} */
	/** Returns the last child of the children Vector. */
	public HoloSDIFdata getLastChild(){
		return children.get(this.children.size()-1);
	}
	/** return the index of a child */
	public int getChildIndex(HoloSDIFdata child){
		return children.indexOf(child);
	}
	
	/** Returns the number of children of the HoloSDIFdata instance. */
	public int getChildrenCount(){
		return children.size();
	}
	/** Returns the parent of the HoloSDIFdata instance. */
	public HoloSDIFdata getParent(){
		return this.parent;
	}
	/** Sets the parent of the HoloSDIFdata instance. */
	public void setParent(HoloSDIFdata parent){
		this.parent = parent;
	}
	
	// TIME METHODS ********************************************///
	/** Returns the {@link HoloSDIFdata#startTime startTime}
	 * of this holoSDIFdata.*/
	public double getStartTime(){
		return this.startTime;
	}
	/** retourne le startTime du SDIF, donc du plus grand parent */
	public double getSDIFstartTime() {
		if (this.getParent()==null)
			return getStartTime();
		return this.getParent().getSDIFstartTime();			
	}
	
	public void setEndTime(double endTime){
		this.endTime = endTime;
	}
	public double getEndTime(){
		return this.endTime;
	}
	public void setSDIFnbrFrame(int sdifNbrFrame) {
		this.sdifNbrFrame = sdifNbrFrame;
	}
	/** retourne le nombre de frame du SDIF, donc du plus grand parent*/
	public int getSDIFnbrFrame() {
		if (this.getParent()==null)
			return sdifNbrFrame;
		return this.getParent().getSDIFnbrFrame();			
	}	
	
	/** Utilisé pour la mise à jour des différents temps,
	 * apres un strech par exemple. */
	public double updateEndTime() {
		if (!sdifTreeMap.isEmpty())
			endTime = sdifTreeMap.lastKey();
		return endTime;
	}

	/** Utilisé pour la mise à jour des différents temps,
	 * apres un strech par exemple. */
	public double updateStartTime() {
		if (!sdifTreeMap.isEmpty())
			startTime = sdifTreeMap.firstKey();
		return startTime;
	}
	
	/** retourne le endTime du SDIF, donc du plus grand parent*/
	public double getSDIFendTime() {		
		if (this.getParent()==null)
			return getEndTime();
		return this.getParent().getSDIFendTime();
	}
	
	/** Returns the length of this holoSDIFdata in centiseconds */
	public double getDataLengthCS() {
		return (endTime-startTime)/10d;
	}
	
	/** Returns the length of this holoSDIFdata in milliseconds */
	public double getDataLength() {
		return (endTime-startTime);
	}
	
	/* DRAW SETTINGS *****************************************/
	
	public int getDrawStyle(){
		return drawStyle;
	}
	
	public void setValuesToDrawIndex(int index){
		valuesToDrawIndex = index;
		try {
			// maj options d'import
			int[] newOptions = {this.getParent().indexImportOptions.get(dataType.split(" ")[1])[0] , index};			
			this.getParent().indexImportOptions.put(dataType.split(" ")[1], newOptions);
		} catch(NullPointerException e) {
			//
		}
		// updating minY and maxY
		updateMinYMaxY();
	}
	
	public int getValuesToDrawIndex(){
		return valuesToDrawIndex;
	}
	
	private void updateMinYMaxY() {
		if (!sdifTreeMap.isEmpty()) {
			minY = this.holoDataStat.minVector.get(valuesToDrawIndex).floatValue();
			maxY = this.holoDataStat.maxVector.get(valuesToDrawIndex).floatValue();
			if (parent!=null && parent.sdifTreeMap!= null && parent.sdifTreeMap.isEmpty())
				parent.updateMinYMaxY();
		} else {
			minY = HoloSDIFdataStat.getSmallestY(children.toArray(new HoloSDIFdata[children.size()]));
			maxY = HoloSDIFdataStat.getBiggestY(children.toArray(new HoloSDIFdata[children.size()]));
		}
	}
	
	public float getMaxY(){
		return maxY;
	}

	public float getMinY(){
		return minY;
	}


	/** Return a String containing some informations about the holoSDIFdata. */
	public String getInfo()	{
		return ("SDIF-file Name : " + getFileName() 
				+ "\nPath : " + extDataFile.getParent() 
				+ "\nFileType : " + getDataType()
				+ "\nLength : " + Ut.msToHMSMS((float) getExtDataLength())
				+ "\nStart Time : " + Ut.msToHMSMS((float) getStartTime()) 
				+ "\nEnd Time : " + Ut.msToHMSMS((float) getEndTime()) 
				+ "\nLast modified : " 
				+ new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(new Date(extDataFile.lastModified())));
	}
	
	/** Returns a String representation of this holoSDIFdata.
	 * NB : la String retournée par toString est affichée dans le Tree de la soundpool. */
	public String toString() {
		if (this.getParent()==null)
			return this.getFileName();
		return this.getDataType();
	}
	
	// TODO
	/** Calcule et retourne la valeur permettant de remettre les ordonnées à l'échelle pour le dessin	*/ 
	public static float getRescaleYallSDIF(HoloSDIFdata[] _hsdifdt)	{
		HoloSDIFdata[] hsdifdtTab = _hsdifdt;
		float rescaleYallSDIF = 1;
		float resc = 1;
		for (HoloSDIFdata h : hsdifdtTab)
			if(h != null) {
				if(h.getChildrenCount()==0){
					if (h.getParent()!=null)
						resc = h.getParent().holoDataStat.maxVector.get(h.valuesToDrawIndex).floatValue();
					else
						resc = h.holoDataStat.maxVector.get(h.valuesToDrawIndex).floatValue();
						
				}else if(h.getChild(0).getChildrenCount()==0){
					resc = h.holoDataStat.maxVector.get(h.valuesToDrawIndex).floatValue();
				}else {	// on prend le max des "rescaleYsdif" des enfants
					float _resc = 1;
					for (HoloSDIFdata hChild : h.children){
						_resc = hChild.holoDataStat.maxVector.get(hChild.valuesToDrawIndex).floatValue();
						if (_resc>resc) resc = _resc;
					}
				}
				if (resc > rescaleYallSDIF)
					rescaleYallSDIF = resc;
			}	
		return rescaleYallSDIF;
	}
	
	/** Surcharge de la méthode equals qui avait été redéfinie dans la classe mere HoloExternalData */
	public boolean equals(Object o)	{
		try	{
			HoloSDIFdata sdifdt = (HoloSDIFdata) o;
			return (this.getFileName().equalsIgnoreCase(sdifdt.getFileName()) && this.getDataType().equalsIgnoreCase(sdifdt.getDataType()));
		}catch (ClassCastException e)
		{
			return false;
		}
	}
	
	public String[] getFields() {
		return fields;
	}
	public void setHoloDataStat() {
		holoDataStat = new HoloSDIFdataStat(this);
		updateMinYMaxY();
	}
	public HoloSDIFdataStat  getHoloDataStat() {
		return holoDataStat;
	}
	
	/** Returns true if the holoSDIFdata contains a value for the given time.<br/>
	 * i.e. if one key of the holoSDIFdata's treeMap is equal to the given time.*/
	public boolean hasTime(double time) {
		if (sdifTreeMap.isEmpty())
			return false;
		if(sdifTreeMap.containsKey(time))
			return true;
		return false;		
	}
	
	/** Returns the values corresponding to the given time. <br/>
	 * if the given time is equal to a key of the holoSDIFdata's sdiftreeMap.<br/>
	 * If the given time is not equal to a key, it returns an interpolation
	 * of the two values corresponding to the two closest key.<br/>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public Vector<Vector<Double>> getDataAtTime(double time) {
		if (sdifTreeMap.isEmpty() || time<sdifTreeMap.firstKey() || time>sdifTreeMap.lastKey())
			return null;		

		// else => interpolation
		Vector<Vector<Double>> arrayToReturn = new Vector<Vector<Double>>();
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, time);
		
		if (pos >= 0)
			return sdifTreeMap.get(time);
		
		else 
		{
			nIndex = -pos - 1;
			pKey = (double) keyList.get(nIndex-1);
			nKey = (double) keyList.get(nIndex);
			pData =   sdifTreeMap.get(pKey);
			nData =   sdifTreeMap.get(nKey);
		
			for(int i= arrayToReturn.size(); i<pData.size(); i++){
				if (i>=nData.size())
					break;
				double step = ((time - pKey) / (nKey - pKey));
				for (int j=0; j<pData.get(i).size() && j<nData.get(i).size(); j++)
					tmp.add(Ut.interpol(pData.get(i).get(j), nData.get(i).get(j) , step));
				arrayToReturn.add(tmp);
			}
		}
		return arrayToReturn;
	}
	

	//TODO : clarify !!! make it safer
	
	/** Returns the field value corresponding to this key 
	 * if the given time is equal to a key of the holoSDIFdata's sdiftreeMap.
	 * If the given time is not equal to a key, it returns an interpolation
	 * of the two values corresponding to the two closest key.
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public double getDataAtTimeField(double time,int field) {
		if (sdifTreeMap.isEmpty() || time<sdifTreeMap.firstKey() || time>sdifTreeMap.lastKey())
			return 0.;
		
		
		// else => interpolation
		double infKey = time;
		//Vector<Vector<Double>> arrayToReturn = new Vector<Vector<Double>>();
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, time);
		
		if (pos >= 0)
			return sdifTreeMap.get(time).get(0).get(field);
		
		else 
		{
			nIndex = -pos - 1;
			pKey = (double) keyList.get(nIndex-1);
			nKey = (double) keyList.get(nIndex);
			pData =   sdifTreeMap.get(pKey);
			nData =   sdifTreeMap.get(nKey);
		
			double step = ((time - pKey) / (nKey - pKey));	
			return Ut.interpol(pData.get(0).get(field), nData.get(0).get(field) , step);
		}

	}
	
	/** Returns	false is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public boolean hasDataAtTime(double time) {
		return !(sdifTreeMap.isEmpty() || time<sdifTreeMap.firstKey() || time>sdifTreeMap.lastKey());
	}
		
		
		
	

	/** Returns the field corresponding to the given time. <br/>
	 * if the given time is equal to a key of the holoSDIFdata's sdiftreeMap.<br/>
	 * If the given time is not equal to a key, it returns an interpolation
	 * of the two field corresponding to the two closest key.<br/>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public Vector<Double> getFieldAtTime(double time, int fieldIndex) {
		if (sdifTreeMap.isEmpty() || time<sdifTreeMap.firstKey() || time>sdifTreeMap.lastKey())
			return null;
		Vector<Double> fieldValues = new Vector<Double>();
		
		double infKey = time;
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, time);
		
		if (pos >= 0)
		{
			for (int i=0 ; i<sdifTreeMap.get(time).size() ; i++){
				fieldValues.add(sdifTreeMap.get(time).get(i).get(fieldIndex));
			}
			return fieldValues;
		}
		else 
		{
			nIndex = -pos - 1;
			pKey = (double) keyList.get(nIndex-1);
			nKey = (double) keyList.get(nIndex);
			pData =   sdifTreeMap.get(pKey);
			nData =   sdifTreeMap.get(nKey);
		
			double step = ((time - pKey) / (nKey - pKey));	
			fieldValues.add(Ut.interpol(pData.get(0).get(fieldIndex), nData.get(0).get(fieldIndex) , step));
			for(int i= 0; i<pData.size() && i < nData.size(); i++){
					fieldValues.add(Ut.interpol(pData.get(i).get(fieldIndex), nData.get(i).get(fieldIndex) , step));
			}
		}
		return fieldValues;
	}
	

	/** Returns a sub-treeMap whose keys are greater than or equal to timeBeg,
	 * and less than or equals to timeEnd.
	 * @param timeBeg - low endpoint (inclusive) of the subMap.
	 * @param timeEnd - high endpoint (inclusive) of the subMap.*/
	public TreeMap<Double, Vector<Vector<Double>>> getBetweenTimes(double timeBeg, double timeEnd) {
		if (sdifTreeMap.isEmpty() || timeBeg > timeEnd)
			return null;

		double keyBeg = timeBeg;
		boolean keyBegFound = false;
		double keyEnd = timeEnd;
		boolean keyEndFound = false;
		
		if (timeBeg>=sdifTreeMap.firstKey() && timeEnd<=sdifTreeMap.lastKey()){
			while (!keyEndFound && !keyBegFound){
				if (sdifTreeMap.containsKey(timeBeg))
					keyBegFound = true;
				if (sdifTreeMap.containsKey(timeEnd))
					keyEndFound = true;
				if (!keyBegFound && !keyEndFound){
					for (Double key : sdifTreeMap.keySet()) {
						if (!keyBegFound && key>timeBeg)
							keyBeg = key;
						else
							keyBegFound = true;
						
						if (keyBegFound && key<timeEnd){
							keyEnd = key;
						}else if (keyBegFound && key>timeEnd){
							keyEndFound = true;
							break;
						}
					}
				}else if (!keyBegFound){
					for (Double key : sdifTreeMap.keySet()) {
						if (key>timeBeg){
							keyBeg = key;
						}else {
							keyBegFound = true;
							break;
						}
					}
				}else if (!keyEndFound){
					for (Double key : sdifTreeMap.keySet()) {
						if  (key<timeEnd){
							keyEnd = key;
						}else {
							keyEndFound = true;
							break;
						}
					}
				}
			}
			return new TreeMap<Double, Vector<Vector<Double>>>(sdifTreeMap.subMap(keyBeg, keyEnd));
		}else if (timeBeg>=sdifTreeMap.firstKey()){
			return new TreeMap<Double, Vector<Vector<Double>>>(sdifTreeMap.tailMap(timeBeg));
		}else {
			TreeMap<Double, Vector<Vector<Double>>> map = new TreeMap<Double, Vector<Vector<Double>>>(sdifTreeMap.headMap(timeEnd));
			if (sdifTreeMap.containsKey(timeEnd)) // cause headMap method excludes the key
				map.put(timeEnd, sdifTreeMap.get(timeEnd));
			return map;
		}
	}
	
	/** Returns a sub-treeMap whose keys are less than or equals to timeEnd.
	 * If timeEnd is greater than the last key of the holoSDIFdata's sdifTreeMap,
	 * then it returns the holoSDIFdata's sdifTreeMap.
	 * @param timeEnd - high endpoint (inclusive) of the subMap.*/
	public TreeMap<Double, Vector<Vector<Double>>> getToTime(double timeEnd){
		if (sdifTreeMap.isEmpty())
			return null;
		if (timeEnd>=sdifTreeMap.lastKey())
			return sdifTreeMap;
	//	TreeMap<Double, Vector<Vector<Double>>> map =  (TreeMap<Double, Vector<Vector<Double>>>) sdifTreeMap.headMap(timeEnd);
		TreeMap<Double, Vector<Vector<Double>>> map = new TreeMap<Double, Vector<Vector<Double>>>(sdifTreeMap.headMap(timeEnd));
		if (sdifTreeMap.containsKey(timeEnd)) // cause headMap method excludes the key
			map.put(timeEnd, sdifTreeMap.get(timeEnd));
		return map;
	}
	
	/** Returns a sub-treeMap whose keys are greater than or equal to timeBeg.
	 * If timeBeg is less than the first key of the holoSDIFdata's sdifTreeMap,
	 * then it returns the holoSDIFdata's sdifTreeMap.
	 * @param timeBeg - low endpoint (inclusive) of the subMap.*/
	public TreeMap<Double, Vector<Vector<Double>>> getFromTime(double timeBeg){
		if (sdifTreeMap.isEmpty())
			return null;
		if (timeBeg<=sdifTreeMap.firstKey())
			return sdifTreeMap;
		return new TreeMap<Double, Vector<Vector<Double>>>(sdifTreeMap.tailMap(timeBeg));
	}
	
	//TODO
	/** Stretching des keys du sdifTreeMap de l'intervalle [oldBegin,oldEnd] vers [newBegin,newEnd] */
	public void stretchDates(int oldBegin, int oldEnd, int newBegin, int newEnd)
	{
		if (sdifTreeMap.isEmpty())
			return;
		// ici on fait le calcul suivant plutot que celui du strechDates de HoloTraj :
		// y = (x-x0)*(yf-y0)/(xf-x0)+y0
		// car la 1ere date n'est pas forcément égale à 0.
	//	System.out.println("stretchDates : old : ["+oldBegin+":"+oldEnd+"], new : ["+newBegin+":"+newEnd+"]");
		float oldDur = oldEnd - oldBegin;
		float newDur = newEnd - newBegin;
		float fact = newDur / oldDur;
		double newKey;
		TreeMap<Double, Vector<Vector<Double>>> treeMapTemp = new TreeMap<Double, Vector<Vector<Double>>>();
		if (oldBegin==newBegin) // moving the end
			for (Double key : sdifTreeMap.keySet()){
				newKey = (key-sdifTreeMap.firstKey())*fact+sdifTreeMap.firstKey();
				treeMapTemp.put(newKey, sdifTreeMap.get(key));
			}
		else if (oldEnd==newEnd) // moving the begining
			for (Double key : sdifTreeMap.keySet()){
				newKey = (key-sdifTreeMap.lastKey())*fact+sdifTreeMap.lastKey();
				treeMapTemp.put(newKey, sdifTreeMap.get(key));
			}
		sdifTreeMap = treeMapTemp;
		updateStartTime();
		updateEndTime();
	}
	
	/** Reverse the key order of the holoSDIFdata's sdifTreeMap.
	 * The greatest becomes the smallest, etc..*/
	public void autoreverse() {
		if (sdifTreeMap.isEmpty())
			return;
		reversed = !reversed;
		TreeMap<Double, Vector<Vector<Double>>> treeMapTemp = new TreeMap<Double, Vector<Vector<Double>>>();
		for (Double key : sdifTreeMap.keySet()) {
			double newKey = sdifTreeMap.lastKey()-key+sdifTreeMap.firstKey();
			treeMapTemp.put(newKey, sdifTreeMap.get(key));
		}
		sdifTreeMap = treeMapTemp;
	}
	
	/** on enleve tous les points entre dateBegin & dateEnd on retourne la fin du sdif. */
	public HoloSDIFdata cut(int dateBegin, int dateEnd)
	{
		boolean rest = false;
		HoloSDIFdata tmp = new HoloSDIFdata(this, null);
		if (dateBegin <= dateEnd)
		{
			cutEnd(dateBegin);
			rest = tmp.cutBegin(dateEnd);
		}
		if (rest==false){
		//	tmp.startTime = dateEnd/10;
			return tmp;
		}
		return null;
	}
	
	
	/** on enleve tous les points à partir de dateEnd.
	 * @return true if the resulting sdifTreeMap is empty, false otherwise. */
	public boolean cutEnd(double dateEnd) {
		if (sdifTreeMap.isEmpty())
			return true;
		if (dateEnd >= endTime)
			return sdifTreeMap.isEmpty();
		
		if (dateEnd > startTime) {
			Vector<Vector<Double>> lastValue = getDataAtTime(dateEnd);
			while (!sdifTreeMap.isEmpty() && sdifTreeMap.lastKey() >= dateEnd){
				sdifTreeMap.remove(sdifTreeMap.lastKey());
				cutEnd += 1;
			}
			sdifTreeMap.put(dateEnd, lastValue);
		} else 
			sdifTreeMap.clear();

		updateStartTime();
		updateEndTime();
		return sdifTreeMap.isEmpty();
	}
	
	/** on enleve tous les points jusqu'à dateBegin.
	 * @return true if the resulting sdifTreeMap is empty, false otherwise. */
	public boolean cutBegin(double dateBegin)
	{
		if (sdifTreeMap.isEmpty())
			return true;
		if (dateBegin <= startTime)
			return sdifTreeMap.isEmpty();
			
		if (dateBegin < endTime) {	
			Vector<Vector<Double>> firstValue = getDataAtTime(dateBegin);
			while (!sdifTreeMap.isEmpty() && sdifTreeMap.firstKey() <= dateBegin){
				sdifTreeMap.remove(sdifTreeMap.firstKey());
				cutBeg += 1;
			}
			sdifTreeMap.put(dateBegin, firstValue);
		} else
			sdifTreeMap.clear();
		
		updateStartTime();
		updateEndTime();
		return sdifTreeMap.isEmpty();
	}

	public void setDirty(boolean dirt){
		dirty = dirt;
	}
}