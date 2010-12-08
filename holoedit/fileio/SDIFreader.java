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

import sdif.*;

import holoedit.HoloEdit;
import holoedit.data.HoloSDIFdata;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import javax.media.opengl.GL;

public class SDIFreader implements Runnable
{
	private int error = NO_ERROR;
	public final static int NO_ERROR = 0;
	public final static int TYPE_ERROR = 1;
	public final static int LOAD_ERROR = 2;
	public final static int FILE_ERROR = 3;
	

	/** Pour l'affichage d'une barre de progression pendant l'ouverture du fichier*/
	private ProgressBar pb;
	private Thread runner;
	// SDIFdatas
	private HoloSDIFdata[] hSDIFdts;
	/** file last modified */

	// state
	private boolean fine = false;
	private boolean done = false;

	// HoloSDIFdata attributs
	/** associated SDIF file */
	public File sdifFile;
	/** Type des données (FQ0/ TRC/ etc..) */
	//	private String sdifDataType;
	/** Date de début des données */
	private double startTime = -1;
	/** Date de fin des données */
	private double endTime;
	private long frameID; 
	/** une hashMap pour contenir une hashMap par flux (dc selon les ID des streams; une hashMap par ID). */
	private HashMap<Long, HashMap<String, HoloSDIFdata>> streamIDmap = new HashMap<Long, HashMap<String, HoloSDIFdata>>();
	/** une hashMap pour contenir des holoSDIFdata (1 par type de matrix présent: FQ0, TRC, etc.) */
	private HashMap<String, HoloSDIFdata> matrixMap;
	/** une hashMap pour contenir les booleans des types de matrix inconnus. */
	private HashMap<String, Boolean> booleanHashMap = new HashMap<String, Boolean>();
	/** une hashMap pour contenir les colonnes choisis par l'utilisateur pour les types de matrix inconnus */
	private HashMap<String, int[]> columnsHashMap = new HashMap<String, int[]>();
	/** une hashMap pour contenir les fields lus ou choisis par l'utilisateur pour tous les types de matrix */
	private HashMap<String, String[]> fieldsHashMap = new HashMap<String, String[]>();
	/** une hashMap pour contenir les offsets des index de chaque type de matrix si un index doit etre lu pour ces matrix. */
	private HashMap<String, Integer> indexOffsetHashMap = new HashMap<String, Integer>();
	
	private double frameTime;
	
	private HoloSDIFdata hsdifdata;
	private Matrix mat;
	private int nbrFrames = 0;
	private HoloEdit holoEditRef;
	
	public SDIFreader(File f, String importOptions, HoloEdit owner) {
		holoEditRef = owner;
		sdifFile = f;
		if (importOptions!=null)
			extractImportOptions(importOptions);
		System.loadLibrary("eaSDIFjava");
		eaSDIF.EasdifInit("");

		pb = new ProgressBar("Importing SDIF file...");
		pb.setIndeterminate(true);
		pb.open();
		runner = new Thread(this);
		runner.setName("SF-Import");
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();

	}
	
	public void run() {
		long d = Calendar.getInstance().getTimeInMillis();

		try {
			if (!done) {
				
				Entity entity = new Entity();
				Frame frame = new Frame();
				/* 	TODO  Check if file is ok ??*/
				
				if (!entity.OpenRead(sdifFile.getPath())){
					System.out.println("Could not open sdif file");
				}
				entity.EnableFrameDir();
				
				while (!entity.eof())
				{
					entity.ReadNextFrame(frame);
					if (! frame.GetSignature().equalsIgnoreCase("1NVT")){
						nbrFrames ++;
						frameTime = frame.GetTime() * 1000.0d; // *1000 to convert to ms
						if (startTime == -1)
							startTime = frameTime;
						
						long nbMat = frame.GetNbMatrix(); //gets the number of matrix in the frame
						frameID = frame.GetStreamID(); //gets the stream ID
	
						// reading every matrix of the current frame :
						for (int i = 0; i < nbMat; i++) {
							mat = frame.GetMatrix(i);
							String msig = mat.GetSignature();
							int nrow = mat.GetNbRows();
							int ncol = mat.GetNbCols();
							int[] selectedColumns = { -1, -1 };
							
							if (nrow>0) {
								if (!booleanHashMap.containsKey(msig)) { // on a jamais vu ce type de matrice
									
									String[] sdifFields = new String[ncol];
									STYPreader stypReader;									
									// Lecture fichier "SdifTypes.STYP"
									stypReader = new STYPreader("SdifTypes.STYP", msig);;									
									String[] STYPfields = stypReader.getData();
									// Lecture frame TYP si présente
									String[] TYPfields = new String[0];
									if (entity.TestFrameSelection("1TYP")) {
										stypReader = new STYPreader(entity.GetTypeString(), msig, true);
										TYPfields = stypReader.getData();
									}
									if (STYPfields.length == ncol) {
										sdifFields = STYPfields;
									} else if (STYPfields.length < ncol) {
										int f=0;
										int f2=0;
										while ((f+f2)<ncol) {
											for (f=0; f<STYPfields.length; f++){
												sdifFields[f] = STYPfields[f];
											}
											for (f2=0; f2<TYPfields.length && f<ncol; f2++, f++){
												sdifFields[f] = TYPfields[f2];
											}
											for (int n = f; n < ncol; n++, f++){
												sdifFields[f] = ("field " + n + "       ");
											}
										}
									} else { 
										for (int n = 0; n < ncol; n++){
											sdifFields[n] = STYPfields[n];
										}
									}
									
									if (holoEditRef!=null && holoEditRef.sdifExpert){ // option import en mode expert
										SDIFdialog sdifdialog = new SDIFdialog(msig, sdifFields, holoEditRef.gestionPistes, nrow, ncol);
										while (!sdifdialog.isFine())
											Thread.sleep(250);
										selectedColumns = sdifdialog.getSelection();
										sdifFields = sdifdialog.getFields();
									} else selectedColumns = defaultValues(sdifFields, nrow);
									
									columnsHashMap.put(msig, selectedColumns);
									fieldsHashMap.put(msig, sdifFields);
									booleanHashMap.put(msig, false);
									if (selectedColumns[1] != -1) // si colonne draw selectionnée/ matrice non ignorée
										if (holoEditRef!=null && holoEditRef.sdifExpert){
											matrixToSDIFdata(msig, nrow, ncol, selectedColumns[0], selectedColumns[1], false, GL.GL_POINTS, sdifFields);
										}else {
											simpleModeMatrixToSDIFdata(msig, nrow, ncol, selectedColumns[1], false, GL.GL_POINTS, sdifFields);	
										}
								} else if (columnsHashMap.get(msig)[1] != -1) // si colonne draw selectionnée/ matrice non ignorée
									if (holoEditRef==null || holoEditRef.sdifExpert){
										matrixToSDIFdata(msig, nrow, ncol, columnsHashMap.get(msig)[0], columnsHashMap.get(msig)[1],
												booleanHashMap.get(msig), GL.GL_POINTS, fieldsHashMap.get(msig));
									}else {
										simpleModeMatrixToSDIFdata(msig, nrow, ncol, columnsHashMap.get(msig)[1],
												booleanHashMap.get(msig), GL.GL_POINTS, fieldsHashMap.get(msig));	
									}
							}
						}
					}					
				}
				endTime = frameTime;
				entity.Close();
				if (streamIDmap.values().size() > 0)
					createSDIFdata();
				fine = true;
				done = true;
				pb.dispose();
			}
			if (error < 2) {
				System.out.println(" v " + sdifFile.getAbsolutePath() + " imported in "
						+ Ut.msToHMSMS(Calendar.getInstance().getTimeInMillis() - d));
				if (holoEditRef!=null)
					holoEditRef.room.display();
			}
		} catch (Exception e) {
			error = LOAD_ERROR;
			pb.dispose();
			done = true;
			fine = false;
			e.printStackTrace();
			return;
		}
	}

	/** Permet la selection automatique des options d'importation d'un type de matrix SDIF.
	 * Méthode utilisée quand l'option "SDIF import" est dé-selectionnée dans les
	 * préérences.
	 * @param sdifFields Les noms des champs de la matrix
	 * @param nrow le nombre de lignes de la matrix
	 */
	private int[] defaultValues(String[] sdifFields, int nrow){
		// Setting default selection for 'group' choice :
		int[] selectedColumns = new int[2];
		boolean gotIndexField = false;
		for (int i=0; i<sdifFields.length ; i++)
			if (sdifFields[i].equalsIgnoreCase("index")) {
				selectedColumns[0] = i+1; // par index
				gotIndexField = true;
				break;
			}
		
		if (gotIndexField == false) {
			if (nrow < 2)
				selectedColumns[0] = -1; // par ligne
			else
				selectedColumns[0] = 0; // aucun group
		}
		
		// Setting default selection for 'draw' choice :
		boolean gotFrequencyField = false;
		for (int i=0; i<sdifFields.length ; i++)
			if (sdifFields[i].equalsIgnoreCase("frequency")) {
				selectedColumns[1] = i; // on dessine la frequence
				gotFrequencyField = true;
				break;
			}
		if (gotFrequencyField == false)
			selectedColumns[1] = 0; // ou la 1ere colonne si pas de freq			
		return selectedColumns;
	}
	
	private void matrixToSDIFdata(String mType, int nrow, int ncol, int indexCol, int drawCol, boolean bool, int drawStyle, String[] sdifFields)
	{
		if (!streamIDmap.containsKey(frameID))
			streamIDmap.put(frameID, new HashMap<String, HoloSDIFdata>());
		matrixMap = streamIDmap.get(frameID);
		
		String streamIDstring = "st."+frameID+" ";
		if (bool==false)  // 1ere fois qu'on lit un certain type de matrice.
		{
			matrixMap.put(mType, new HoloSDIFdata(sdifFile)); // dc creation de la data dans le treemap
			hsdifdata = matrixMap.get(mType);
			if (mType.equalsIgnoreCase("1FQ0"))
				drawStyle = GL.GL_LINE_STRIP;
			hsdifdata.init(frameTime, streamIDstring+mType , drawCol, drawStyle, sdifFields);
			hsdifdata.setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
			booleanHashMap.put(mType, true);
		} else if(!matrixMap.containsKey(mType)){
			matrixMap.put(mType, new HoloSDIFdata(sdifFile)); // dc creation de la data dans le treemap
			hsdifdata = matrixMap.get(mType);
			if (mType.equalsIgnoreCase("1FQ0"))
				drawStyle = GL.GL_LINE_STRIP;
			hsdifdata.init(frameTime, streamIDstring+mType , columnsHashMap.get(mType)[1], drawStyle, fieldsHashMap.get(mType));
			indexCol = columnsHashMap.get(mType)[0];
			hsdifdata.setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
		}
				
		matrixMap = streamIDmap.get(frameID);	
		hsdifdata = matrixMap.get(mType);
		for (int r = 0; r < nrow; r++) {
			if (holoEditRef.sdifExpert){
				if (indexCol > 0) { // un index doit etre regardé
					int index = (int) mat.GetDouble(r, indexCol - 1);
					if (!indexOffsetHashMap.containsKey(mType)){
						if (index<1)
							indexOffsetHashMap.put(mType, 1-index); // permettra de ramener le 1er index a 1 si inferieur a 1
						else
							indexOffsetHashMap.put(mType, 0);
					}
					while (index+indexOffsetHashMap.get(mType) > hsdifdata.getChildrenCount()) // on a jamais vu cet index (la taille doit correspondre au nombre d'index existants)
					{
						hsdifdata.addChild(new HoloSDIFdata(sdifFile));
						hsdifdata.getLastChild().init(frameTime, streamIDstring+mType+": index "+(hsdifdata.getChildrenCount()), drawCol, GL.GL_LINE_STRIP, sdifFields);
						hsdifdata.getLastChild().setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>()); // donc on crée un nouveau treeMap pour lui.
					}
				} else if (indexCol == -1) { // "groupement" par numero de ligne
					while (nrow > hsdifdata.getChildrenCount()) // on a jamais vu cet index (la taille doit correspondre au nombre d'index existants)
					{
						hsdifdata.addChild(new HoloSDIFdata(sdifFile));
						hsdifdata.getLastChild().init(frameTime, streamIDstring+mType+" : row "+(hsdifdata.getChildrenCount()), drawCol, GL.GL_LINE_STRIP , sdifFields);
						hsdifdata.getLastChild().setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
					}
				} else {
					hsdifdata.addChild(new HoloSDIFdata(sdifFile));
					hsdifdata.getLastChild().init(frameTime, streamIDstring+mType+" : t "+frameTime , drawCol, GL.GL_LINE_STRIP, sdifFields);
					hsdifdata.getLastChild().setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
				}
			}
			Vector<Double> myvalues = new Vector<Double>();
			Vector<Vector<Double>> myvaluesVector = new Vector<Vector<Double>>();
			for (int c = 0; c < ncol; c++)
				myvalues.add(mat.GetDouble(r, c));
			myvaluesVector.add(myvalues);
			if (holoEditRef.sdifExpert){
				int index;
				if (indexCol > 0)
					index = (int) mat.GetDouble(r, indexCol - 1)+indexOffsetHashMap.get(mType)-1;
				else if (indexCol == -1)
					index = r;
				else
					index = hsdifdata.children.size()-1;
				hsdifdata.getChild(index).sdifTreeMap.put(frameTime, myvaluesVector); // on rajoute la ligne dans le treeMat correnspondant à l'index
				hsdifdata.getChild(index).setEndTime(frameTime); // on remet a jour le timing
			}
			if (!hsdifdata.sdifTreeMap.containsKey(frameTime)){
				hsdifdata.sdifTreeMap.put(frameTime, new Vector<Vector<Double>>(myvaluesVector));
			}else{
				hsdifdata.sdifTreeMap.get(frameTime).add(myvalues);
			}
		}
		hsdifdata.setEndTime(frameTime); // endtime update
	}

	private void simpleModeMatrixToSDIFdata(String mType, int nrow, int ncol, int drawCol, boolean bool, int drawStyle, String[] sdifFields)
	{
		if (!streamIDmap.containsKey(frameID))
			streamIDmap.put(frameID, new HashMap<String, HoloSDIFdata>());
		matrixMap = streamIDmap.get(frameID);	

		String streamIDstring = "st."+frameID+" ";
		if (bool==false)  // 1ere fois qu'on lit un certain type de matrice.
		{
			matrixMap.put(mType, new HoloSDIFdata(sdifFile)); // dc creation de la data dans le treemap
			hsdifdata = matrixMap.get(mType);
			if (mType.equalsIgnoreCase("1FQ0"))
				drawStyle = GL.GL_LINE_STRIP;
			hsdifdata.init(frameTime, streamIDstring+mType , drawCol, drawStyle, sdifFields);
			hsdifdata.setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
			booleanHashMap.put(mType, true);
		} else if(!matrixMap.containsKey(mType)){
			matrixMap.put(mType, new HoloSDIFdata(sdifFile)); // dc creation de la data dans le treemap
			hsdifdata = matrixMap.get(mType);
			if (mType.equalsIgnoreCase("1FQ0"))
				drawStyle = GL.GL_LINE_STRIP;
			hsdifdata.init(frameTime, streamIDstring+mType , columnsHashMap.get(mType)[1], drawStyle, fieldsHashMap.get(mType));
			hsdifdata.setSDIFtreeMap(new TreeMap<Double, Vector<Vector<Double>>>());
		}

		matrixMap = streamIDmap.get(frameID);	
		hsdifdata = matrixMap.get(mType);
		for (int r = 0; r < nrow; r++) {
			Vector<Double> myvalues = new Vector<Double>();
			Vector<Vector<Double>> myvaluesVector = new Vector<Vector<Double>>();
			for (int c = 0; c < ncol; c++)
				myvalues.add(mat.GetDouble(r, c));
			myvaluesVector.add(myvalues);

			if (!hsdifdata.sdifTreeMap.containsKey(frameTime)){
				hsdifdata.sdifTreeMap.put(frameTime, new Vector<Vector<Double>>(myvaluesVector));
			}else{
				hsdifdata.sdifTreeMap.get(frameTime).add(myvalues);
			}
		}
		hsdifdata.setEndTime(frameTime); // endtime update
	}
	
	private void createSDIFdata() {
		hSDIFdts = new HoloSDIFdata[1];
		HoloSDIFdata hSDIFdt = new HoloSDIFdata(sdifFile);
		
		for (HashMap<String, HoloSDIFdata> streamData :streamIDmap.values())
			for (HoloSDIFdata matrixData : streamData.values()) {
				for (HoloSDIFdata child : matrixData.children) {
					child.initialTreeMapSize = child.sdifTreeMap.size();
					child.setParent(matrixData);
					child.setExtDataLength(endTime - startTime);
					child.setHoloDataStat();
					child.setFine(true);
				}
				matrixData.initialTreeMapSize = matrixData.sdifTreeMap.size();
				matrixData.setExtDataLength(endTime - startTime);
				matrixData.setParent(hSDIFdt);
				matrixData.setHoloDataStat();
				matrixData.setFine(true);
			}
		hSDIFdt.initialTreeMapSize = hSDIFdt.sdifTreeMap.size();
		for (HashMap<String, HoloSDIFdata> streamData :streamIDmap.values())
			for (HoloSDIFdata matrixData : streamData.values())
				hSDIFdt.addChild(matrixData);
		hSDIFdt.indexImportOptions = columnsHashMap;
		hSDIFdt.fieldsImportOptions = fieldsHashMap;
		// initialisation avec par défault les fields du 1er enfant. !! si plusieurs types de matrix !
		// le parametre drawCol=0 n'a pas d'importance ici
		hSDIFdt.init(startTime, "SDIF" , 0 , GL.GL_POINTS , hSDIFdt.getChild(0).getFields());
		hSDIFdt.setEndTime(endTime);
		hSDIFdt.setExtDataLength(endTime - startTime);
		hSDIFdt.setSDIFnbrFrame(nbrFrames);
		hSDIFdt.setHoloDataStat();
		hSDIFdt.setFine(true);
		hSDIFdts[0] = hSDIFdt;
	}
	
	private void extractImportOptions(String importOptions) {
		String[] allMat = importOptions.split(":");
		for (String mat : allMat) {
			String[] string = mat.trim().split(";");
			booleanHashMap.put(string[0], false);
			int[] indexes =  {Integer.parseInt(string[1]) , Integer.parseInt(string[2])};
			columnsHashMap.put(string[0], indexes);
			String[] fields = new String[string.length-3]; //-3 car 0=matriceName ; 1=groupCol et 2=drawCol;
			for (int i=3; i< string.length; i++)
				fields[i-3] = string[i];
			fieldsHashMap.put(string[0], fields);
		}
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
