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

import holoedit.gui.GestionPistes;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.ScoreIndex;
import holoedit.util.Ut;
import java.awt.Color;
import java.util.Vector;
import javax.media.opengl.GL;

public class WaveFormInstance
{
	public static final int LOOP_BEGIN_CUE = 0;
	public static final int LOOP_IN_CUE = 1;
	public static final int LOOP_END_CUE = 2;
	public static final int LOOP_AFTER_CUE = 3;
	public static final int CURSOR_CUE_LOOP = 4;
	public static final int CURSOR_CUE = 5;
	
	public static final int SPEC_CUE_NB = 6;
	
	public static final int SPEC_CUE_OFFSET = 4;
	
	private HoloWaveForm hwf;
	public int begTime;
	private String hwfName;
	private GestionPistes gpRef;
	private int listID = -1;
	public int cue = -1;
	public int begLoop = -1;
	public int endLoop = -1;
	public int loopDefined = -1;
	
	/** pour indexer les sdif instances attachées. */
	private Vector<SDIFdataInstance> sdifVector;
	
	public WaveFormInstance(GestionPistes gp, String na, int _beg)
	{
		gpRef = gp;
		hwfName = na;
		begTime = _beg;
		update();
	}
	
	public WaveFormInstance dupliquer()
	{
		WaveFormInstance w = new WaveFormInstance(gpRef,hwfName,begTime);
		w.hwf = hwf;
		w.listID = listID;
		if (sdifVector!=null){
			w.sdifVector = new Vector<SDIFdataInstance>(sdifVector.size());
			for (SDIFdataInstance inst : sdifVector){
				SDIFdataInstance sdifinst = inst.dupliquer(w);
				w.sdifVector.add(sdifinst);
			}
		}
		return w;
	}
	
	public WaveFormInstance dupliquer(SDIFdataInstance sdif)
	{
		WaveFormInstance w = new WaveFormInstance(gpRef,hwfName,begTime);
		w.hwf = hwf;
		w.listID = listID;
		if (sdifVector!=null){
			w.sdifVector = new Vector<SDIFdataInstance>(sdifVector.size());
			for (SDIFdataInstance inst : sdifVector){
				if (inst!=sdif) {
					SDIFdataInstance sdifinst = inst.dupliquer(w);
					w.sdifVector.add(sdifinst);
				}
			}
		}
		return w;
	}
	
	public void update()
	{
		hwf = gpRef.soundPool.get(hwfName);
		if (hwf!=null) {
			// on attache les SDIF s'il y en a
			if (hwf.getLinkedDatasCount()==0)
				return;
			if(sdifVector==null || hwf.getLinkedDatasCount()!=sdifVector.size()){
				sdifVector = new Vector<SDIFdataInstance>(1);
				for (HoloExternalData data : hwf.getLinkedDatas())
					if (data.getFileType().equalsIgnoreCase("sdif") || data.getFileType().equalsIgnoreCase("txt"))
						sdifVector.add(new SDIFdataInstance(gpRef, (HoloSDIFdata) data, this.getFirstDate(), this));
			}
		}
	}
	
	public HoloWaveForm getWave()
	{
		if(hwf == null) update();
		return hwf;
	}
	
	public Vector<SDIFdataInstance> getSDIFvector() {
		return sdifVector;
	}
	
	public void addLinkedSDIFinstance(SDIFdataInstance sdifDataInstance) {
		if(sdifVector==null){
			sdifVector = new Vector<SDIFdataInstance>(1);
		}
		sdifVector.add(sdifDataInstance);
	}
	
	public String getName()
	{
		if(hwf == null) update();
		return hwf.getSoundFileName();
	}
	
	public int getFirstDate()
	{
		return begTime;
	}
	
	public int getLastDate()
	{
		if(hwf == null)
			return begTime;
		return begTime + (int)hwf.getFileLength();
	}
	
	public void setBegTime(int i)
	{
		begTime = Ut.clipL(i,0);
		// Pour les sdifInstances attachées
		if(sdifVector!=null)
			for (SDIFdataInstance inst : sdifVector){
				inst.setBegTime(i);
			}
	}
	
	public void shiftDates(int date)
	{
		begTime = Ut.clipL(begTime+date,0);
		// Pour les sdifInstances attachées
		if(sdifVector!=null)
			for (SDIFdataInstance inst : sdifVector)
				inst.setBegTime(begTime);
	}
	
	public int getDuration()
	{
		if(hwf == null) 
		{
			update();
			return 0;
		}
		return (int)hwf.getFileLength();
	}

	public String toString()
	{
		return "\t\t<waveinstance name=\""+hwfName+"\" begin=\""+begTime+"\"/>\n";
	}

	public void drawScoreSquare(GL gl, HoloTrack tk, int minTime, int maxTime, float dX)
	{
		if(hwf == null) update();
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		gl.glLineWidth(2);
		float[] cc = OpenGLUt.convCol(tk.getColor());
		cc[3] = 1;
		OpenGLUt.glColor(gl, cc);
		gl.glRectf(begTime + dX, 1, begTime + dX + getDuration(), 1.5f);
		gl.glLineWidth(1);		
	}
	
	public void drawScore(GL gl, Color c, int begin, int end, boolean render, int tkNum, int seqNum, int selIndex, Vector<WaveFormInstance> selWaves, Vector<SDIFdataInstance> selDatas, int pixelNum, boolean nosmoothenabled, int selMode)
	{
		if(hwf == null) update();
		if(hwf == null || !hwf.isFine() || getFirstDate() > end || getLastDate() < begin) return;

		int rapport = Ut.clip((int)((float) (end - begin) / getDuration() / pixelNum * hwf.waveBuffer.length),1,200);;
			
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 0.05f;
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		// POLY

		int index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.WAVE_POLY);
		if(selMode != 1)
			gl.glLoadName(index);
		if(index == selIndex)
			cc[3] = 0.1f;
		else if(selWaves.contains(this))
			cc[3] = 0.2f;
		else
			cc[3] = 0.05f;
		OpenGLUt.glColor(gl, cc);
		gl.glRectf(getFirstDate(), 1, getLastDate(), 1.5f);
		if(render)
		{
			OpenGLUt.glColor(gl, c.darker());
			gl.glPushMatrix();
			gl.glScalef(1,0.5f,1);
			gl.glTranslatef(begTime,2.5f,0);
		
			if(listID == -1)
				createList(gl);
			gl.glCallList(listID+(rapport-1));
			gl.glPopMatrix();
			gl.glLineWidth(1);
			OpenGLUt.glColor(gl, Color.BLACK);

			if(nosmoothenabled)
				gl.glDisable(GL.GL_LINE_SMOOTH);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glRectf(getFirstDate(), 1, getLastDate(), 1.5f);
			if(nosmoothenabled)
				gl.glEnable(GL.GL_LINE_SMOOTH);
		}
	}
	
	public void drawTime(GL gl, Color c, int begin, int end, int pixelNum)
	{
		if(hwf == null) update();
		if(hwf == null || !hwf.isFine() || getFirstDate() > end || getLastDate() < begin) return;
		
		int rapport = Ut.clip((int)((float) (end - begin) / getDuration() / pixelNum * hwf.waveBuffer.length),1,200);;

		OpenGLUt.glColor(gl, c);
		gl.glPushMatrix();
		gl.glTranslatef(begTime,0,0);
		if(listID == -1)
			createList(gl);
		gl.glCallList(listID+(rapport-1));
		gl.glPopMatrix();
	}
	
	private void createList(GL gl)
	{
		listID = gl.glGenLists(200);
		for(int R = 1 ; R <= 200 ; R++)
		{
			gl.glNewList(listID+(R-1), GL.GL_COMPILE);
			gl.glLineWidth(1);
			double stepSize = (double) getDuration() / hwf.waveBuffer.length;
			gl.glBegin(GL.GL_LINE_STRIP);
			OpenGLUt.drawPoint(gl, 0, (float) hwf.waveBuffer[0] / 256);
			for (int i = 0; i + R < hwf.waveBuffer.length; i+= R)
			{
				float min = 1;
				float max = -1;
				for(int k = i ; k < i + R ; k++)
				{
					float v = (float) hwf.waveBuffer[k] / 256;
					min = Ut.min(min,v);
					max = Ut.max(max,v);
				}
				OpenGLUt.drawPoint(gl, (float) (i * stepSize), max);
				OpenGLUt.drawPoint(gl, (float) (i * stepSize), min);
			}
			OpenGLUt.drawPoint(gl, getDuration(), (float) hwf.waveBuffer[hwf.waveBuffer.length - 1] / 256);
			gl.glEnd();
			gl.glEndList();
		}
	}
	
	public int getCue()
	{
		return cue;
	}
	
}