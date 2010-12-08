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

import holoedit.gui.GestionPistes;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.ScoreIndex;
import holoedit.util.Ut;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Vector;
import javax.media.opengl.GL;

public class SDIFdataInstance {
	
	public static final int LOOP_BEGIN_CUE = 0;
	public static final int LOOP_IN_CUE = 1;
	public static final int LOOP_END_CUE = 2;
	public static final int LOOP_AFTER_CUE = 3;
	public static final int CURSOR_CUE_LOOP = 4;
	public static final int CURSOR_CUE = 5;
	
	public static final int SPEC_CUE_NB = 6;
	
	public static final int SPEC_CUE_OFFSET = 4;
	
	private HoloSDIFdata hsdifdt;
	private int begTime;
	private String hxtdtName;
	private GestionPistes gpRef;
	private int listIDScore = -1;
	private int listIDTimeEditor = -1;
	// la waveFormInstance a laquelle est attachée
	// la SDIFdataInstance. null si pas d'attache
	private WaveFormInstance linkedWaveFormInstance;
	
	// variable de demande d'update
	private boolean timeEdGUIdirty = true;
	
	// boolean utilisé lors de la lecture d'une file .holo pour indiqué que le treemap
	// de la holosdifdata a peut etre subit des cut, strech...
	private boolean performChangeOnTreeMap = false;
	// Utilisés pour faire les changement si performChangeOnTreeMap==true
	private int indexFirstKey = 0;
	private int indexLastKey = 0;
	private double firstKey = 0;
	private double lastKey = 0;
	private boolean reversed = false; 
	
	// used by HoloFileReader
	public SDIFdataInstance(GestionPistes gp, String hdtName, int begin, int indexFirstKey,
							int indexLastKey, double firstKey, double lastKey, boolean reversed, WaveFormInstance linkedWaveFormInstance) {	
		gpRef = gp;
		hxtdtName = hdtName;
		performChangeOnTreeMap = true;
		this.indexFirstKey = indexFirstKey;
		this.indexLastKey = indexLastKey;
		this.firstKey = firstKey;
		this.lastKey = lastKey;
		this.reversed = reversed;
		begTime = begin;		
		update();
		// on link la waveformInstance, et on ajoute this a la waveformInstance
		// en tant que linkSDIFinstance :
		// (lie les instances dans le score)
		this.linkedWaveFormInstance = linkedWaveFormInstance;
		if (linkedWaveFormInstance!=null) {
			this.linkedWaveFormInstance.addLinkedSDIFinstance(this);
			// on linked le sdifData de this a la waveform correspondante :
			// (lie la data au son dans la soundPool)
			this.linkedWaveFormInstance.getWave().addLinkedData(this.hsdifdt);
		}
	}

	public SDIFdataInstance(GestionPistes gp, HoloSDIFdata hdt, int _beg)
	{	
		gpRef = gp;
		hxtdtName = hdt.toString();
		hsdifdt = new HoloSDIFdata(hdt, null);
		begTime = _beg-(int)hsdifdt.getStartTime();	
		linkedWaveFormInstance = null;
	}
	
	public SDIFdataInstance(GestionPistes gp, HoloSDIFdata hdt, int _beg, WaveFormInstance waveFormInstance)
	{	
		gpRef = gp;
		hxtdtName = hdt.toString();
		hsdifdt = new HoloSDIFdata(hdt, null);
		begTime = _beg-(int)hsdifdt.getStartTime();
		linkedWaveFormInstance = waveFormInstance;	
	}
	
	public SDIFdataInstance dupliquer()
	{
		if (hsdifdt == null)
			update();
		SDIFdataInstance s = new SDIFdataInstance(gpRef,hsdifdt,begTime+(int)hsdifdt.getStartTime());
		if (linkedWaveFormInstance!=null)
			s.linkedWaveFormInstance = linkedWaveFormInstance.dupliquer(s);
		s.listIDScore = listIDScore;
		s.listIDTimeEditor = listIDTimeEditor;
		s.timeEdGUIdirty = timeEdGUIdirty;
		return s;
	}

	public SDIFdataInstance dupliquer(WaveFormInstance wave)
	{
		SDIFdataInstance s = new SDIFdataInstance(gpRef,hsdifdt,begTime+(int)hsdifdt.getStartTime());
		s.linkedWaveFormInstance = wave;
		s.listIDScore = listIDScore;
		s.listIDTimeEditor = listIDTimeEditor;
		s.timeEdGUIdirty = timeEdGUIdirty;
		return s;
	}
	public void update()
	{
		if (hsdifdt==null){
			String[] splitStr = hxtdtName.split("\\s"+"-"+"\\s"); // espace+tiret+espace
			String filename = splitStr[0];
			String dataType = splitStr[1];
			hsdifdt = gpRef.externalDataPool.get(filename, dataType);
		}
		if (!performChangeOnTreeMap || hsdifdt==null)
			return;
		performStrechNcutNreverse(begTime, indexFirstKey, indexLastKey, firstKey, lastKey, reversed);
		performChangeOnTreeMap = !performChangeOnTreeMap;
	}
	
	public HoloSDIFdata getData()
	{
		if(hsdifdt == null) update();
		return hsdifdt;
	}
	
	/** Returns true if this SDIFdataInstance is empty.
	 * An SDIFdataInstance is considered to be empty if the holoSDIFdata
	 * it refers to is empty.
	 * @return true if this SDIFdataInstance is empty.
	 * @see HoloSDIFdata#isEmpty()  */
	public boolean isEmpty() {
		if (hsdifdt==null) update();
		if (hsdifdt==null) return true;
		return hsdifdt.isEmpty();
	}
	
	
	public double scoreToLocalTime(double stime)
	{
		return stime - ((double)begTime) ; //+ firstKey; // FIXME : compatible with cut stretch ... etc
	}
	
	public double localToScoreTime(double ltime)
	{
		return ltime + ((double)begTime) ;//- firstKey; // FIXME : compatible with cut stretch ... etc
	}
	
	/** Returns the values corresponding to the given SCORE time in ms. <br/>
	 * if the given time is equal to a key of the holoSDIFdata's sdiftreeMap.<br/>
	 * If the given time is not equal to a key, it returns an interpolation
	 * of the two values corresponding to the two closest key.<br/>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public Vector<Vector<Double>> getDataAtTime(double time) {
		
		double ltime = scoreToLocalTime(time);
		TreeMap<Double,Vector<Vector<Double>>> sdifTreeMap = hsdifdt.sdifTreeMap;
		if (sdifTreeMap.isEmpty() || ltime<sdifTreeMap.firstKey() || ltime>sdifTreeMap.lastKey())
			return null;
		
		
		// else => interpolation
		double infKey = ltime;
		Vector<Vector<Double>> arrayToReturn = new Vector<Vector<Double>>();
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, ltime);
		
		if (pos >= 0)
			return sdifTreeMap.get(ltime);
		
		else 
		{
			nIndex = -pos - 1;
			pKey = (double) keyList.get(nIndex-1);
			nKey = (double) keyList.get(nIndex);
			pData =   sdifTreeMap.get(pKey);
			nData =   sdifTreeMap.get(nKey);
		
		
//		for (Double key : sdifTreeMap.keySet()) {
//			if (key<time){
//				infKey = key;
//			}else{
				for(int i= arrayToReturn.size(); i<pData.size(); i++){
					if (i>=nData.size())
						break;
					double step = ((ltime - pKey) / (nKey - pKey));
					for (int j=0; j<pData.get(i).size() && j<nData.get(i).size(); j++)
						tmp.add(Ut.interpol(pData.get(i).get(j), nData.get(i).get(j) , step));
					arrayToReturn.add(tmp);
				}
//			}
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
		double ltime = scoreToLocalTime(time);
		TreeMap<Double,Vector<Vector<Double>>> sdifTreeMap = hsdifdt.sdifTreeMap;
		if (sdifTreeMap.isEmpty() || ltime<sdifTreeMap.firstKey() || ltime>sdifTreeMap.lastKey())
			return 0.;
		
		
		// else => interpolation
		double infKey = ltime;
		//Vector<Vector<Double>> arrayToReturn = new Vector<Vector<Double>>();
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, ltime);
		
		if (pos >= 0)
			return sdifTreeMap.get(ltime).get(0).get(field);
		
		else 
		{
			nIndex = -pos - 1;
			pKey = (double) keyList.get(nIndex-1);
			nKey = (double) keyList.get(nIndex);
			pData =   sdifTreeMap.get(pKey);
			nData =   sdifTreeMap.get(nKey);
		
			double step = ((ltime - pKey) / (nKey - pKey));	
			return Ut.interpol(pData.get(0).get(field), nData.get(0).get(field) , step);
		}

	}
	
	/** Returns	false is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public boolean hasDataAtTime(double time) {
		double ltime = scoreToLocalTime(time);
		TreeMap<Double,Vector<Vector<Double>>> sdifTreeMap = hsdifdt.sdifTreeMap;
		return !(sdifTreeMap.isEmpty() || ltime<sdifTreeMap.firstKey() || ltime>sdifTreeMap.lastKey());
	}	
	
	public Double[] getTimes() {
		Double times[] = new Double[hsdifdt.sdifTreeMap.size()];
		int i = 0;
		
		for(Double dataTime : hsdifdt.sdifTreeMap.keySet())
		{
			times[i] = localToScoreTime(dataTime);
			i++;
		}
		return times;
	}
	
	

	/** Returns the field corresponding to the given time. <br/>
	 * if the given time is equal to a key of the holoSDIFdata's sdiftreeMap.<br/>
	 * If the given time is not equal to a key, it returns an interpolation
	 * of the two field corresponding to the two closest key.<br/>
	 * 'null' is returned if the sdifTreeMap is empty or if the time
	 * we look for is out of the treeMap keyset range.*/
	public Vector<Double> getFieldAtTime(double time, int fieldIndex) {
		double ltime = scoreToLocalTime(time);
		TreeMap<Double,Vector<Vector<Double>>> sdifTreeMap = hsdifdt.sdifTreeMap;
		if (sdifTreeMap.isEmpty() || ltime<sdifTreeMap.firstKey() || ltime>sdifTreeMap.lastKey())
			return null;
		Vector<Double> fieldValues = new Vector<Double>();
		
		double infKey = ltime;
		Vector<Vector<Double>> pData,nData;
		int nIndex;
		double pKey,nKey;
		Vector<Double> tmp = new Vector<Double>();
		ArrayList<Double> keyList = new ArrayList<Double>(sdifTreeMap.keySet());
		
		int pos = Collections.binarySearch(keyList, ltime);
		
		if (pos >= 0)
		{
			for (int i=0 ; i<sdifTreeMap.get(ltime).size() ; i++){
				fieldValues.add(sdifTreeMap.get(ltime).get(i).get(fieldIndex));
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
		
			double step = ((ltime - pKey) / (nKey - pKey));	
			fieldValues.add(Ut.interpol(pData.get(0).get(fieldIndex), nData.get(0).get(fieldIndex) , step));
			for(int i= 0; i<pData.size() && i < nData.size(); i++){
					fieldValues.add(Ut.interpol(pData.get(i).get(fieldIndex), nData.get(i).get(fieldIndex) , step));
			}
		}
		return fieldValues;
	}
	
	public WaveFormInstance getLinkedWaveForm() {
		return linkedWaveFormInstance;
	}
	
	public String getName()	{
		if(hsdifdt == null) update();
		return hsdifdt.getFileName();
	}
	
	/** return the date of the score at which the data begins */
	public int getFirstDate() {
		return begTime+(int) hsdifdt.getStartTime();
	//	return begTime;
	}
	
	public int getLastDate() {
		if(hsdifdt == null)
			return begTime;
		return begTime+(int)hsdifdt.getEndTime();
	}
	
	public void setBegTime(int i)
	{
		if (i<=0)
			begTime=0;
		else if (begTime!=i){
			begTime = i;
			if (linkedWaveFormInstance!=null)
				linkedWaveFormInstance.setBegTime(i);
		}
	}
	
	/** déplacement de tous les points à la date de départ dateBegin */
	public void move(int dateBegin)
	{
		if (dateBegin > 0)
			shiftDates(dateBegin - getFirstDate());
		setDirty(true);
	}
	
	public void shiftDates(int date)
	{
		if ((begTime+date)<=0)
			begTime=0;
		else{
			begTime += date;
			if (linkedWaveFormInstance!=null)
				linkedWaveFormInstance.shiftDates(date);
		}
	}
	
	public int getDuration()
	{
		if(hsdifdt == null) 
		{
			update();
			return 0;
		}
		return (int) hsdifdt.getDataLength();
	}

	public String toString() {
		return getData().getFileName()+" - "+getData().getDataType()+" - begin time="+Ut.msToHMSMS(getFirstDate()).replaceAll("\"", "");
	}

	/** used to write in the .holo file */
	public String toString2() {
		String desc = "\t\t<sdifdatainstance name=\""+getData().getFileName()+" - "+getData().getDataType()+"\" " +
		"begin=\""+getFirstDate()+"\" " +	// le temps de debut dans le score
		"indexFirstKey=\""+this.getData().cutBeg+"\" " +	// le numero de la premiere clé par rapport à une instance semblable non modifiée (cut)
		"indexLastKey=\""+(this.getData().initialTreeMapSize-1-this.getData().cutEnd)+"\" " +	// le numero de la derniere clé par rapport à une instance semblable non modifiée (cut)
		"firstKey=\""+this.getData().sdifTreeMap.firstKey()+"\" " +	// valeur de la premiere clé
		"lastKey=\""+this.getData().sdifTreeMap.lastKey()+"\" " +	// valeur de la derniere clé
		"reversed=\""+this.getData().reversed+"\" " +				// si le sdif treemap a été renversé 
		"linkedWaveform=\"";
		if (this.getLinkedWaveForm() == null)
			desc += "null"+"\"" +"/>\n";
		else	
			desc += this.getLinkedWaveForm().getName()+" - "+this.getLinkedWaveForm().getFirstDate()+"\"" +		// si le sdif est attaché à un son
			"/>\n";
		return desc;
	}
	
	public void drawScoreSquare(GL gl, HoloTrack tk, float dX)
	{
		if (isEmpty()) return;
		if(hsdifdt == null) update();
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		gl.glLineWidth(2);
		float[] cc = OpenGLUt.convCol(tk.getColor());
		cc[3] = 1;
		OpenGLUt.glColor(gl, cc);
		gl.glRectf(getFirstDate() + dX, 1, getLastDate() + dX, 1.5f);
		gl.glLineWidth(1);
	}
	
	public void drawMovedSquare(GL gl, Color c, int begin, int end, float b, float e, boolean type)
	{
		if (isEmpty()) return;
		int first = getFirstDate()+(int)b;
		int last = getLastDate()+(int)e;
		// BEGIN
		gl.glLineWidth(2);
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 1;
		OpenGLUt.glColor(gl, cc);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(first,1f);
			gl.glVertex2f(first,1.5f);
			gl.glVertex2f(last,1f);
			gl.glVertex2f(last,1.5f);
		gl.glEnd();
		if(!type)
		{
			gl.glLineWidth(1.5f);
			gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(first,1.25f);
				gl.glVertex2f(last,1.25f);
			gl.glEnd();
		} else {
			gl.glLineWidth(1);
			gl.glBegin(GL.GL_LINES);
				if(b > 0) {
					gl.glVertex2f(getFirstDate(),1.5f);
					gl.glVertex2f(first,1f);
					gl.glVertex2f(getFirstDate(),1f);
					gl.glVertex2f(first,1.5f);
				} else if(e < 0) {
					gl.glVertex2f(getLastDate(),1.5f);
					gl.glVertex2f(last,1f);
					gl.glVertex2f(getLastDate(),1f);
					gl.glVertex2f(last,1.5f);
				}
			gl.glEnd();
		}
		gl.glLineWidth(1);
	}
	
	public void drawScore(GL gl, Color c, int begin, int end, boolean render, int tkNum, int seqNum, int selIndex, Vector<SDIFdataInstance> selDatas, int pixelNum, boolean nosmoothenabled, int selMode)
	{
		if(hsdifdt == null) update();
		
		if (hsdifdt==null || isEmpty() || !hsdifdt.isFine() || getFirstDate() > end || getLastDate() < begin)
			return;
		
		int first = getFirstDate();
		int last = getLastDate();
		
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 0.05f;
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		int index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.DATA_POLY);
		if(index == selIndex)
			cc[3] = 0.1f;
		else if(selDatas.contains(this))
			cc[3] = 0.2f;
		else
			cc[3] = 0.05f;
		OpenGLUt.glColor(gl, cc);
		// Le rectangle de couleur
		if(selMode != 1 && linkedWaveFormInstance==null)
			gl.glLoadName(index);
		else gl.glLoadName(-1);
		gl.glRectf(first, 1f, last, 1.5f);

		// BEGIN
		gl.glLineWidth(2);
		index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.DATA_BEGIN);
		if(index == selIndex)
			cc[3] = 1;
		else
			cc[3] = 0f;
		OpenGLUt.glColor(gl, cc);
		if(linkedWaveFormInstance==null)
			gl.glLoadName(index);
		else gl.glLoadName(-1);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(first,1f);
			gl.glVertex2f(first,1.5f);
		gl.glEnd();

		// END
		index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.DATA_END);
		if(index == selIndex)
			cc[3] = 1;
		else
			cc[3] = 0f;
		OpenGLUt.glColor(gl, cc);
		if(linkedWaveFormInstance==null)
			gl.glLoadName(index);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(last,1f);
			gl.glVertex2f(last,1.5f);
		gl.glEnd();

		if(render)
		{
			if (linkedWaveFormInstance!= null){
				gl.glLineWidth(4);
				OpenGLUt.glColor(gl, c.darker());
			}else{
				gl.glLineWidth(1);
				OpenGLUt.glColor(gl, Color.BLACK);
			}
			if(nosmoothenabled)
				gl.glDisable(GL.GL_LINE_SMOOTH);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			// L'encadré en noir
			gl.glRectf(first, 1, last, 1.5f);
			if(nosmoothenabled)
				gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glLineWidth(1);
			
			OpenGLUt.glColor(gl, c.darker());
			// A REMETTRE SI ON VEUT LE DESSIN DES SDIF DANS LE SCORE...
			// Eenlevé pour l'instant car trop lourd
		/*	gl.glPushMatrix();
			gl.glTranslatef(begTime,1f,0f);
			gl.glScalef(1f,0.5f,1f);
			if(listIDTimeEditor == -1 || timeEdGUIdirty){
				listIDTimeEditor = gl.glGenLists(1);
				gl.glNewList(listIDTimeEditor, GL.GL_COMPILE);
				createList(gl, hsdifdt);
				gl.glEndList();
				setDirty(false);
			}
			gl.glCallList(listIDTimeEditor);
			gl.glPopMatrix();		*/	
		}
	}
	
	// DESSIN POUR TIME EDITOR
	public void drawTime(GL gl, Color color, int begin, int end, int pixelNum)
	{	
		if(hsdifdt == null) return;
		if (isEmpty()) return;
		
		gl.glPushMatrix();
		gl.glTranslatef(begTime,0,0);
		OpenGLUt.glColor(gl, color);
		if(listIDTimeEditor == -1){
			listIDTimeEditor = gl.glGenLists(1);
			gl.glNewList(listIDTimeEditor, GL.GL_COMPILE);
			createList(gl, hsdifdt);
			gl.glEndList();
		}
		gl.glCallList(listIDTimeEditor);
		gl.glPopMatrix();
	}
	
	private boolean createList(GL gl, HoloSDIFdata holosdifdata) {
		boolean done = false;
		TreeMap<Double, Vector<Vector<Double>>> sdifTreeMap = holosdifdata.sdifTreeMap;
		if (!holosdifdata.sdifTreeMap.isEmpty()){
			gl.glLineWidth(1);
			gl.glPointSize(2);
			int valuesToDrawIndex = holosdifdata.getValuesToDrawIndex();
			if (sdifTreeMap.size()>1) {
				gl.glBegin(holosdifdata.getDrawStyle());
				for (Double key : sdifTreeMap.keySet())
					for (Vector<Double> vect : sdifTreeMap.get(key)){
						OpenGLUt.drawPoint(gl, key.floatValue(), vect.get(valuesToDrawIndex).floatValue()/hsdifdt.getMaxY());
					}
			} else if (holosdifdata.getStartTime()!=holosdifdata.getEndTime()) {
				gl.glBegin(GL.GL_LINES);
				for (Double key : sdifTreeMap.keySet()) {
					for (Vector<Double> vect : sdifTreeMap.get(key)) {
						OpenGLUt.drawPoint(gl, (float) holosdifdata.getStartTime(), vect.get(valuesToDrawIndex).floatValue()/hsdifdt.getMaxY());
						OpenGLUt.drawPoint(gl, (float) holosdifdata.getEndTime(), vect.get(valuesToDrawIndex).floatValue()/hsdifdt.getMaxY());
					}
				}
			} else {
				gl.glBegin(GL.GL_POINTS);
				for (Double key : sdifTreeMap.keySet())
					for (Vector<Double> vect : sdifTreeMap.get(key))
						OpenGLUt.drawPoint(gl, (float) holosdifdata.getStartTime(), vect.get(valuesToDrawIndex).floatValue()/hsdifdt.getMaxY());
			}
			gl.glEnd();
		}
		done = true;
		return done;
	}
	
	
	/** fonction de demande de mise à jour générale */
	public void setDirty(boolean val)
	{
		timeEdGUIdirty = val;
	}
	
	public void setDirty(int mask)
	{
		if((mask & Ut.DIRTY_ALL) > 0)
		{
			setDirty(true);
			return;
		}
		timeEdGUIdirty = (mask & Ut.DIRTY_TIME) > 0;			
	}
	
	public void moveBegin(int dateBegin)
	{
		if (dateBegin < getLastDate()){
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), dateBegin, getLastDate());
		}else
		{
			int lastDate = getLastDate();
			hsdifdt.autoreverse(); // inversion des keys
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), getFirstDate(), dateBegin); // strech de la fin a la nouvelle valeur 
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), lastDate, getLastDate());	// strech du debut à l'ancienne fin
		}
		setDirty(true);
	}
	
	public void moveEnd(int dateEnd)
	{
		if (dateEnd > getFirstDate()){
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), getFirstDate(), dateEnd);
		}else
		{
			int firstDate = getFirstDate();
			hsdifdt.autoreverse(); // inversion des keys
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), dateEnd, getLastDate()); // strech du debut a la nouvelle valeur 
			hsdifdt.stretchDates(getFirstDate(), getLastDate(), getFirstDate(), firstDate); // strech de la fin a l'ancien debut
		}
		setDirty(true);
	}
	
	/** on enleve tous les points entre dateBegin & dateEnd on retourne une nouvelle instance
	 * basée sur la fin du sdif. */
	public SDIFdataInstance cut(int dateBegin, int dateEnd)
	{
		setDirty(true);
		HoloSDIFdata h = hsdifdt.cut((dateBegin-begTime), (dateEnd-begTime));
		if (h!=null)
			return new SDIFdataInstance(this.gpRef, h, dateEnd);//+(int)h.getStartTime());
		return null;
	}
	
	public SDIFdataInstance cutAt(int date)
	{
		return cut(date-1, date);
	}
	
	/** on enleve toutes les valeurs jusqu'à dateBegin. */
	public boolean cutBegin(int dateBegin) {
		setDirty(true);
		return hsdifdt.cutBegin((dateBegin-begTime));
	}

	/** on enleve toutes les valeurs à partir de dateEnd. */
	public boolean cutEnd(int dateEnd) {
		setDirty(true);
		return hsdifdt.cutEnd((dateEnd-begTime)); 
	}
	
	/** used when creating an instance from a .holo File
	 * 
	 * @param begin
	 * @param indexFirstKey
	 * @param indexLastKey
	 * @param firstKey
	 * @param lastKey
	 * @param reversed
	 * @throws NullPointerException
	 */
	public void performStrechNcutNreverse(int begin, int indexFirstKey,	int indexLastKey, double firstKey,
			double lastKey, boolean reversed) throws NullPointerException
	{	
		try {
			// the original sdif characteristics to be compare with the streched/cuted sdif-to-import ones :
			int sdifLastKeyIndex = hsdifdt.sdifTreeMap.size()-1;
			double sdifLastKeyValue = hsdifdt.sdifTreeMap.lastKey();
			double sdifFirstKeyValue = hsdifdt.sdifTreeMap.firstKey();
			
			if (indexFirstKey==0 && indexLastKey==sdifLastKeyIndex && firstKey==sdifFirstKeyValue
					&& lastKey==sdifLastKeyValue && !reversed) {
				begTime = begin - (int)firstKey;
				return; // no differences => nothing to do
			}
			
			if (indexFirstKey>0) // have to cutBegin
				hsdifdt.sdifTreeMap = hsdifdt.getFromTime(hsdifdt.sdifTreeMap.keySet().toArray(new Double[hsdifdt.sdifTreeMap.size()])[indexFirstKey]);
	
			if (indexLastKey<sdifLastKeyIndex) // have to cutEnd
				hsdifdt.sdifTreeMap = hsdifdt.getToTime(hsdifdt.sdifTreeMap.keySet().toArray(new Double[hsdifdt.sdifTreeMap.size()])[hsdifdt.sdifTreeMap.size()-1-(sdifLastKeyIndex-indexLastKey)]);
	
			if (firstKey!=hsdifdt.sdifTreeMap.firstKey()) // have to strechBegin
				hsdifdt.stretchDates(hsdifdt.sdifTreeMap.firstKey().intValue(), hsdifdt.sdifTreeMap.lastKey().intValue(), (int)firstKey, hsdifdt.sdifTreeMap.lastKey().intValue());
	
			if (lastKey!=hsdifdt.sdifTreeMap.lastKey()) // have to strechEnd
				hsdifdt.stretchDates(hsdifdt.sdifTreeMap.firstKey().intValue(), hsdifdt.sdifTreeMap.lastKey().intValue(), hsdifdt.sdifTreeMap.firstKey().intValue(), (int)lastKey);
	
			if (reversed) // have to reverse
				hsdifdt.autoreverse();

			begTime = begin - (int)firstKey;
		}catch(NullPointerException npe){
			throw npe;			
		}
	}
}