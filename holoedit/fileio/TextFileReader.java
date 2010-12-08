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
package holoedit.fileio;

import holoedit.data.HoloSDIFdata;
import holoedit.gui.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import javax.media.opengl.GL;

/**
 * Permet la création d'une holoData à partir d'un fichier texte.
 * Le fichier doit etre constitué de plusieurs colones,
 * la première correspond au temps, les autres aux différents fields.
 * Les nombres à virgules doivent etre avec des points, pas des virgules.
 * Les colonnes doivent etre séparées par des espaces ou tabulations (1 ou +).
 */
public class TextFileReader implements Runnable
{
	private int error = NO_ERROR;
	public final static int NO_ERROR = 0;
	public final static int TYPE_ERROR = 1;
	public final static int LOAD_ERROR = 2;
	public final static int FILE_ERROR = 3;
	public final static int TEXT_FORMAT_ERROR = 4;

	/** Pour l'affichage d'une barre de progression pendant l'ouverture du fichier*/
	private ProgressBar progressBar;
	private Thread runner;
	// SDIFdatas
	private HoloSDIFdata[] hSDIFdts;
	// state
	private boolean fine = false;
	private boolean done = false;
	// HoloSDIFdata attributs
	/** associated text file */
	public File textFile;
	private HashMap<Double, Vector<Vector<Double>>> hashMap = new HashMap<Double, Vector<Vector<Double>>>();
	
	public TextFileReader(File f) {
		textFile = f;
		progressBar = new ProgressBar("Importing text file...");
		// progressBar.setMaximum(Text.countLines(f));
		progressBar.setIndeterminate(true);
		progressBar.open();
		runner = new Thread(this);
		runner.setName("SF-Import");
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();
	}
	
	public void run() {
		try {
			if (!done) {
				HoloSDIFdata data = new HoloSDIFdata(textFile);
				String[] st = {""};
				data.init(0, "TXT" , 0 , GL.GL_LINE_STRIP , st);
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
					String line;
					Vector<Double> vector;
					Vector<Vector<Double>> vector2;
					//int numLine = 0;
					while((line = reader.readLine())!=null){
						String[] values = line.split("\\s+");
						vector = new Vector<Double>();
						vector2 = new Vector<Vector<Double>>();
						for (int i=1; i<values.length; i++)
							vector.add(Double.parseDouble(values[i]));
						vector2.add(vector);
						hashMap.put(Double.parseDouble(values[0])*1000.0d, vector2);
						//	numLine++;
						//	progressBar.setValue(numLine);
					}
					createSDIFdata(data);
					reader.close();
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			fine = true;
		} catch(java.lang.NumberFormatException nfe) {
			error = TEXT_FORMAT_ERROR;
			fine = false;
			nfe.printStackTrace();
		} catch (Exception e) {
			error = LOAD_ERROR;
			fine = false;
			e.printStackTrace();
		} finally{
			progressBar.dispose();
			done = true;
		}
	}
	
	private void createSDIFdata(HoloSDIFdata data) {
		data.sdifTreeMap = new TreeMap<Double, Vector<Vector<Double>>>(hashMap);
		hSDIFdts = new HoloSDIFdata[1];
		int nbField = data.sdifTreeMap.get(data.sdifTreeMap.firstKey()).get(0).size();
		String[] fields = new String[nbField];
		for (int i=0; i<nbField; i++)
			fields[i] = "field"+i;
		double debut = data.sdifTreeMap.firstKey();
		double fin = data.sdifTreeMap.lastKey();
		data.setHoloDataStat();
		data.init(debut, "TXT", 0 , GL.GL_LINE_STRIP , fields);
		data.setEndTime(fin);
		data.setExtDataLength(fin - debut);
		data.initialTreeMapSize = data.sdifTreeMap.size();
		data.setFine(true);
		hSDIFdts[0] = data;
	}
	
	public boolean isFine() {
		return fine;
	}
	public boolean isDone() {
		return done;
	}
	public int getError() {
		return error;
	}
	public HoloSDIFdata[] getHoloSDIFdatas() {
		return hSDIFdts;
	}
	@SuppressWarnings("deprecation")
	public void stop() {
		runner.stop();
	}
}
