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
import javax.media.opengl.GL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class HoloWaveForm
{
	// ATTRIBUTS 
	/** associated sound file */
	private File soundFile;
	/** etat pour l'import */
	private boolean fine = false;
	private String fileType;
	/** duree du fichier (ms) */
	private float fileLength;
	/** sample rate */
	private float sampleRate;
	private float sampleRateMs;
	/** sample size */
	private int sampleSize;
	/** datas */
	byte[] waveBuffer;
	private int listID = -1;
	// pour les sdif data Attachées
	private Vector<HoloExternalData> linkedDatas = new  Vector<HoloExternalData>(0);
	
	/* ******* CONSTRUCTEURS ******* */
	public HoloWaveForm()
	{}
	
	public HoloWaveForm(File f)
	{
		soundFile = f;
	}
	
	public HoloWaveForm dupliquer()
	{
		HoloWaveForm tmpHwf = new HoloWaveForm();
		tmpHwf.fileLength = this.fileLength;
		tmpHwf.sampleRate = this.sampleRate;
		tmpHwf.sampleRateMs = this.sampleRateMs;
		tmpHwf.fine = this.fine;
		tmpHwf.soundFile = this.soundFile;
		return null;
	}

	public boolean equals(Object o)
	{
		try
		{
			HoloWaveForm hwf = (HoloWaveForm) o;
			return this.soundFile.getName().equalsIgnoreCase(hwf.soundFile.getName());// && this.channel == hwf.channel);
		} catch (ClassCastException e)
		{
			return false;
		}
	}

	public String toString2(String parentPath)
	{
		if (Ut.MAC)
		{
			// EN DESSOUS DANS LA MEME ARBORESCENCE
			if (soundFile.getAbsolutePath().startsWith(parentPath))
				return "\t<waveform file=\""+(parentPath.equalsIgnoreCase("/") ? "/" : ".") + soundFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
			String path = "../";
			String path2 = "..";
			parentPath = new File(parentPath).getParentFile().getAbsolutePath();
			while (!parentPath.equalsIgnoreCase("/"))
			{
				// MEME PARTITION
				if (soundFile.getAbsolutePath().startsWith(parentPath))
					return "\t<waveform file=\"" + (parentPath.equalsIgnoreCase("/") ? path : path2) + soundFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
				path = "../" + path;
				path2 = "../" + path2;
				parentPath = new File(parentPath).getParentFile().getAbsolutePath();
			}
		} else {
			// EN DESSOUS DANS LA MEME ARBORESCENCE
			if (soundFile.getAbsolutePath().startsWith(parentPath))
				return "\t<waveform file=\""+(parentPath.endsWith(":\\") ? ".\\" : ".") + soundFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
			String path = "..\\";
			String path2 = "..";
			while (!parentPath.endsWith(":\\"))
			{
				// MEME PARTITION
				parentPath = new File(parentPath).getParentFile().getAbsolutePath();
				if (soundFile.getAbsolutePath().startsWith(parentPath))
					return "\t<waveform file=\"" + (parentPath.endsWith(":\\") ? path : path2) + soundFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
				path = "..\\" + path;
				path2 = "..\\" + path2;
			}
		}
		// AUTRE PARTITION
		return "\t<waveform file=\"" + soundFile.getAbsolutePath() + "\"/>\n";
	}

	public String toString()
	{
		return soundFile.getName();// + "." + channel;
	}

	/** ******* Modification/Retour des parametres ******* */
	public void setSoundFile(File f)
	{
		this.soundFile = f;
	}

	public File getSoundFile()
	{
		return soundFile;
	}

	public String getSoundFileName()
	{
		String tmp = soundFile.getName();
		if(tmp.indexOf(' ') != -1)
			tmp = "\""+tmp+"\"";
		return tmp;
	}

	public String getStripPath()
	{
		String tmp = soundFile.getName();
		if(tmp.indexOf(' ') != -1)
			tmp = "\""+tmp+"\"";
		return tmp;
	}
	
	public String getStripPathWoQuote()
	{
		String tmp = soundFile.getName();
		return tmp;
	}
	
	public String getPath()
	{
		String tmp = soundFile.getParent();
		if(tmp.startsWith("/Volumes/") && Ut.MAC)
			tmp = tmp.substring("/Volumes/".length(),tmp.length()).replaceFirst("/",":");
		if(tmp.indexOf(' ') != -1 && !(tmp.startsWith("\"") && tmp.endsWith("\"")))
			tmp = "\""+tmp+"\"";
		return Ut.MAC ? tmp : tmp.replace('\\','/');
	}
	
	public String getPathWoQuote()
	{
		String tmp = soundFile.getParent();
		if(tmp.startsWith("/Volumes/") && Ut.MAC)
			tmp = tmp.substring("/Volumes/".length(),tmp.length()).replaceFirst("/",":");
		return Ut.MAC ? tmp : tmp.replace('\\','/');
	}
	
	
	public String getCompletePath()
	{
		String tmp = soundFile.getAbsolutePath();
		if(tmp.startsWith("/Volumes/") && Ut.MAC)
			tmp = tmp.substring("/Volumes/".length(),tmp.length()).replaceFirst("/",":");
		if(tmp.indexOf(' ') != -1 && !(tmp.startsWith("\"") && tmp.endsWith("\"")))
			tmp = "\""+tmp+"\"";
		return Ut.MAC ? tmp : tmp.replace('\\','/');
		
	}
	
	
	public String getCompletePathWoQuote()
	{
		String tmp = soundFile.getAbsolutePath();
		if(tmp.startsWith("/Volumes/") && Ut.MAC)
			tmp = tmp.substring("/Volumes/".length(),tmp.length()).replaceFirst("/",":");
		return Ut.MAC ? tmp : tmp.replace('\\','/');
		
	}
	
	
	
	public float getSampleRate()
	{
		return sampleRate;
	}

	public void setSampleRate(float hz)
	{
		sampleRate = hz;
		sampleRateMs = sampleRate / 1000;
	}

	public boolean isEmpty()
	{
		return (soundFile == null || getFileLength() == 0 || waveBuffer == null || waveBuffer.length == 0);
	}

	/* ************* AFFICHAGE DE LA FORME D'ONDE ******************** */
	/** pour dessiner dans la piscine de son ! on dessine à t=0 */
	public int drawSoundPool(GL gl,float[] color, int ID)
	{
		OpenGLUt.glColor(gl, color);
		if (this.waveBuffer.length == 0)
		{
			System.err.println("Null Waveform");
			return ID;
		}
		if (listID == -1)
		{
			listID = ID++;
			createList(gl);
		} else
			gl.glCallList(listID);
		return ID;
	}

	private void createList(GL gl)
	{
		try
		{
			if (listID != 0)
			{
				gl.glNewList(listID, GL.GL_COMPILE_AND_EXECUTE);
				gl.glLineWidth(1);
				double stepSize = (double) fileLength / waveBuffer.length;
				gl.glBegin(GL.GL_LINE_STRIP);
				OpenGLUt.drawPoint(gl, 0, (float) waveBuffer[0]);
				for (int i = 0; i < waveBuffer.length; i++)
					OpenGLUt.drawPoint(gl, (float) (i * stepSize), (float) waveBuffer[i]);
				OpenGLUt.drawPoint(gl, fileLength, (float) waveBuffer[waveBuffer.length - 1]);
				gl.glEnd();
				gl.glEndList();
			}
		}
		catch (java.lang.UnsatisfiedLinkError e)
		{
			e.printStackTrace();
		}
	}

	public float getFileLength()
	{
		return fileLength;
	}

	public void setFileLength(float fileLength)
	{
		this.fileLength = fileLength;
	}
	
	public int getFileLengthCS()
	{
		return (int)(fileLength/10);
	}

	public byte[] getWaveBuffer()
	{
		return waveBuffer;
	}

	public void setWaveBuffer(byte[] _buf)
	{
		this.waveBuffer = _buf;
	}

	public float getSampleRateMs()
	{
		return sampleRateMs;
	}

	public void setSampleRateMs(float sampleRateMs)
	{
		this.sampleRateMs = sampleRateMs;
	}

	public String getFileType()
	{
		return fileType;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public boolean isFine()
	{
		return fine;
	}

	public void setFine(boolean b)
	{
		fine = b;
	}
	
	public String getInfo()
	{
		return ("Name : " + getSoundFileName() + "\nPath : " + getSoundFile().getParent() 
				+ /*"\nChannel n° : " + hwf.getChannel() +*/ "\nFileType : " 
				+ getFileType() + "\nSampleRate : " + getSampleRateMs()
				+ "kHz" + "\nLength : "	+ Ut.msToHMSMS(getFileLength()) + "\nLast modified : " 
				+ new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(new Date(getSoundFile().lastModified())));
	}

	//children methods
	public boolean addLinkedData(HoloSDIFdata holoData){
		return linkedDatas.add(holoData);		
	}
	/**
	 * 
	 * @param holoData
	 * @return false si la HoloSDIFdata n'était pas présente dans le
	 * vecteur de datas attachées. 
	 */
	public boolean removeLinkedData(HoloSDIFdata holoData){		
		if (holoData.children!=null)
			for (int i=0; i<holoData.children.size(); i++)
				this.removeLinkedData(holoData.children.get(i));
		return linkedDatas.remove(holoData);
	}
	
	public boolean containsLinkedData(HoloSDIFdata holoSDIFdatasdifDt){
		return linkedDatas.contains(holoSDIFdatasdifDt);	
	}
	
	public int getLinkedDatasCount(){
		return this.linkedDatas.size();
	}

	public Vector<HoloExternalData> getLinkedDatas(){
		return this.linkedDatas;
	}
}
