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
/** *********** classe GestionPistes ************** */
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.data.HoloPoint;
import holoedit.data.HoloRecentFile;
import holoedit.data.HoloSoundPool;
import holoedit.data.HoloExternalDataPool;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.WaveFormInstance;
import holoedit.data.SDIFdataInstance;
import holoedit.fileio.HoloFileReader;
import holoedit.fileio.HoloFileWriter;
import holoedit.fileio.HoloFilenameFilter;
import holoedit.fileio.HoloFilenameFilterXP;
import holoedit.fileio.MidiRead;
import holoedit.fileio.SeqRead;
import holoedit.fileio.TextRead;
import holoedit.fileio.TjFileReader;
import holoedit.fileio.TjFileWriter;
import holoedit.fileio.ICSTFileWriter;
import holoedit.fileio.SDIFFileWriter;
import holoedit.fileio.TkFileReader;
import holoedit.fileio.TkFileWriter;
import holoedit.functions.Algorithm;
import holoedit.functions.Algors;
import holoedit.opengl.RoomIndex;
import holoedit.opengl.ScoreIndex;
import holoedit.opengl.TimeIndex;
import holoedit.rt.Connection;
import holoedit.rt.Player;
import holoedit.util.AlgoVector;
import holoedit.util.IntegerVector;
import holoedit.util.Ut;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Point;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GestionPistes
{
	// liste des pistes
	public Vector<HoloTrack> tracks;
	// presse-papier
	public HoloTrack copyTrack;
	public HashMap<Integer,HoloTraj> copySeqs = new HashMap<Integer,HoloTraj>();
	public HashMap<Integer,WaveFormInstance> copyWaves = new HashMap<Integer,WaveFormInstance>();
	public HashMap<Integer,SDIFdataInstance> copySDIFs = new HashMap<Integer,SDIFdataInstance>();
	// undo
	public boolean undoAllTracks = false;
	public boolean redoAllTracks = false;
	// sur Toutes pistes
	public Vector<HoloTrack> undoTracks;
	public Vector<HoloTrack> redoTracks;
	// sur une seule piste
	public HoloTrack undoTrack;
	public HoloTrack redoTrack;
	// numero des pistes concernees par undo et redo
	public int nthTrackUndo = -1;
	public int nthTrackRedo = -1;
	// numero de la piste activee. Si il est nul, aucune piste n'est activee
	public int activeTrack = -1;
	// Classe de calcul des transformations/creations de trajectoires
	private Algors algor;
	/* Nom des derniers fichiers et repertoires lus pour memorisation */
	public String holoFilename = "";
	public String holoDirectory = "";
	public String tkFilename = "";
	public String tkDirectory = "";
	public String tjFilename = "";
	public String tjDirectory = "";
	public String midiFilename = "";
	public String midi7Filename = "";
	public String midiDirectory = "";
	public String midi7Directory = "";
	public String textFilename = "";
	public String textDirectory = "";
	public String seqFilename = "";
	public String seqDirectory = "";
	public String sndFilename = "";
	public String dtFilename = "";
	public File sndFile;
	public File dtFile;
	public String sndDirectory = "";
	public String dtDirectory = "";
	// filtre de fichier Holo
	private HoloFilenameFilter holoFilter = new HoloFilenameFilter(".holo .holo~", "Holo-Edit Session (*.holo)", true);
	// filtre de fichier Mid 14bits
	private HoloFilenameFilter midFilter = new HoloFilenameFilter(".mid", "Midi Files - 14bits (*.mid)");
	// filtre de fichier Mid 7bits
	private HoloFilenameFilter mid7Filter = new HoloFilenameFilter(".mid", "Midi Files - 7bits (*.7.mid)");
	// filtre de fichier Txt
	private HoloFilenameFilter txtFilter = new HoloFilenameFilter(".txt", "Text Files (*.txt)");
	// filtre de fichier Seq~
	private HoloFilenameFilter seqFilter = new HoloFilenameFilter(".txt", "Seq~ Files (*~.txt)");
	// filtre de fichier Sons
	public HoloFilenameFilter sndFilter = new HoloFilenameFilter(".aif .aiff .wav .sd2", "Sound Files (*.aif,*.aiff,*.wav,*.sd2)", true);
	// filtre de fichier data/SDIF
	public HoloFilenameFilter dtFilter = new HoloFilenameFilter(".sdif .txt", "data Files (*.sdif, *.txt)", true);
	// filtre de fichier Sons et data/SDIF
	public HoloFilenameFilter sndNdtFilter = new HoloFilenameFilter(".aif .aiff .wav .sd2 .sdif .txt", "Sound and Data Files (*.aif,*.aiff,*.wav,*.sd2,*.sdif, *.txt)", true);
	// filtre de fichier Track
	private HoloFilenameFilter tkFilter = new HoloFilenameFilter(".tk", "Holo-Edit Track File (*.tk)", true);
	// filtre de fichier Seq
	public HoloFilenameFilter tjFilter = new HoloFilenameFilter(".tj", "Holo-Edit Trajectory File (*.tj)", true);
	
	// filtre de fichier Holo
	private HoloFilenameFilterXP holoFilterXP = new HoloFilenameFilterXP(".holo .holo~", "Holo-Edit Session (*.holo)", true);
	// filtre de fichier Mid 14bits
	private HoloFilenameFilterXP midFilterXP = new HoloFilenameFilterXP(".mid", "Midi Files - 14bits (*.mid)");
	// filtre de fichier Mid 7bits
	private HoloFilenameFilterXP mid7FilterXP = new HoloFilenameFilterXP(".mid", "Midi Files - 7bits (*.7.mid)");
	// filtre de fichier Txt
	private HoloFilenameFilterXP txtFilterXP = new HoloFilenameFilterXP(".txt", "Text Files (*.txt)");
	// filtre de fichier Seq~
	private HoloFilenameFilterXP seqFilterXP = new HoloFilenameFilterXP(".txt", "Seq~ Files (*~.txt)");
	// filtre de fichier Sons
	public HoloFilenameFilterXP sndFilterXP = new HoloFilenameFilterXP(".aif .aiff .wav .sd2", "Sound Files (*.aif,*.aiff,*.wav,*.sd2)", true);
	// filtre de fichier data/SDIF
	public HoloFilenameFilterXP dtFilterXP = new HoloFilenameFilterXP(".sdif .txt", "data Files (*.sdif *.txt)", true);
	// filtre de fichier Track
	private HoloFilenameFilterXP tkFilterXP = new HoloFilenameFilterXP(".tk", "Holo-Edit Track File (*.tk)", true);
	// filtre de fichier Seq
	private HoloFilenameFilterXP tjFilterXP = new HoloFilenameFilterXP(".tj", "Holo-Edit Trajectory File (*.tj)", true);
	public int delta;
	// nombre de points non editables entre deux points editables
	public int nbPointsInter;
	// vecteur des pistes visibles
	private Vector<Integer> pistesV;
	// reference sur applic superieure de maniere a pouvoir
	// controler les menus (griser/degriser)
	public HoloEdit holoEditRef;
	public Player hpRef;
	// Reference sur les compteurs
	public TrackSelector ts;
	public HoloSoundPool soundPool;
	public HoloExternalDataPool externalDataPool;
	// liste des couleurs des pistes
	public final static Color primary[] = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW.darker().darker(), Color.PINK.darker().darker(), Color.ORANGE, Color.CYAN.darker().darker(), Color.MAGENTA };
	public Color couleurs[];
	public Vector<HoloSpeaker> speakers;
	private HoloTrack tk;
	private HoloTraj ht;
	private WaveFormInstance w;
	private SDIFdataInstance sdif;
	public AlgoVector algos;

	public GestionPistes(HoloEdit owner)
	{
		holoEditRef = owner;
		soundPool = new HoloSoundPool();
		externalDataPool = new HoloExternalDataPool();
		tracks = new Vector<HoloTrack>(Ut.INIT_TRACK_NB);
		copyTrack = new HoloTrack();
		copySeqs = new HashMap<Integer,HoloTraj>(5,1);
		undoTrack = new HoloTrack();
		redoTrack = new HoloTrack();
		undoTracks = new Vector<HoloTrack>(Ut.INIT_TRACK_NB);
		redoTracks = new Vector<HoloTrack>(Ut.INIT_TRACK_NB);
		delta = 1000;
		nbPointsInter = 4;
		initSpeakers();
		initColors(Ut.INIT_TRACK_NB);
		for (int i = 0; i < Ut.INIT_TRACK_NB; i++)
			tracks.add(new HoloTrack(i+1, couleurs[i]));
		ts = new TrackSelector(this);
		algos = new AlgoVector(20,1);
		loadAlgos();
		
	}

	public GestionPistes(Player owner, boolean tj)
	{
		if(tj)
		{
			hpRef = owner;
			tracks = new Vector<HoloTrack>(1);
			delta = 1000;
			nbPointsInter = 4;
			tracks.add(new HoloTrack(1,Color.black,true));			
		} else {
			hpRef = owner;
			soundPool = new HoloSoundPool();
			externalDataPool = new HoloExternalDataPool();
			tracks = new Vector<HoloTrack>(Ut.INIT_TRACK_NB);
			delta = 1000;
			nbPointsInter = 4;
			initSpeakers();
			initColors(Ut.INIT_TRACK_NB);
			for (int i = 0; i < Ut.INIT_TRACK_NB; i++)
				tracks.add(new HoloTrack(i+1, couleurs[i],true));
		}
	}

	// ------------------- COLORS ---------------
	
	
	public void initColors(int tkNum)
	{
		float f = (float) (tkNum-1) / primary.length;
		int steps = f > 1 ? (int)(f+1) : 1 ;
		couleurs = new Color[(steps + 1) * primary.length];
		for (int s = 0; s < steps; s++)
		{
			if (s == 0)
			{
				int tmp = 0;
				for (int i = 0; i < (steps + 1) * primary.length; i += steps)
				{
					couleurs[i] = primary[tmp];
					tmp++;
					tmp = Ut.mod(tmp, primary.length);
				}
			} else
			{
				for (int i = s; i < tkNum - 1; i += steps)
				{
					// int prev = i-s;
					// int next = i+(steps-s);
					couleurs[i] = interColor(couleurs[i - s], couleurs[i + (steps - s)], steps + 1, s + 1);
				}
			}
		}
		couleurs[tkNum - 1] = Color.BLACK;
	}

	private Color interColor(Color c1, Color c2, int steps, int currentStep)
	{
		int r, g, b;
		r = g = b = 0;
		r = interInt(c1.getRed(), c2.getRed(), steps, currentStep);
		g = interInt(c1.getGreen(), c2.getGreen(), steps, currentStep);
		b = interInt(c1.getBlue(), c2.getBlue(), steps, currentStep);
		return new Color(r, g, b);
	}

	private int interInt(int i1, int i2, int steps, int currentStep)
	{
		if (i1 == i2)
			return i1;
		return Ut.clip(i1 + (int) ((double) (i2 - i1) / steps * currentStep), 0, 255);
	}

	// ---------------- SETTERS ----------------
	
	public void setDelta(double nouveauDelta)
	{
		delta = (int) nouveauDelta;
	}

	public void setNbPts(int nouveauNb)
	{
		nbPointsInter = nouveauNb;
	}

	// --------------- ALGORS -----------------------------/

	public void addPoint(HoloPoint p)
	{
		// On verifie qu'il y a bien une piste selectionnee
		if (activeTrack != -1)
		{
			StoreToUndo();
			int nDate = getActiveTrack().addPoint(p, delta, nbPointsInter, holoEditRef.counterPanel.getDate(1), holoEditRef.counterPanel.getDate(2));
			if(nDate > holoEditRef.counterPanel.getDate(2))
				holoEditRef.counterPanel.setCompteur(0, nDate);
		}
	}
	
	public void update(int track, int date)
	{
		selectTrack(track);
		if (date >= 0)
			holoEditRef.counterPanel.setCompteur(0, date);
		holoEditRef.room.display();
	}

	// Fonction generique d'annulation
	public void StoreToUndo(boolean all)
	{
		// Suivant le booleen, memorisation de toutes les pistes ou d'une seule
		holoEditRef.setSaved(false);
		if (all)
			StoreToUndoAll();
		else
			StoreToUndo();
	}

	// Annulation de la piste active
	public void StoreToUndo()
	{
		StoreToUndo(activeTrack);
	}

	// Annulation d'une seule piste
	public void StoreToUndo(int track)
	{
		// Meme algo que precedemment avec n'importe quelle piste
		holoEditRef.modify();
		undoAllTracks = false;
		nthTrackUndo = track;
		nthTrackRedo = -1;
		undoTrack = getTrack(track).dupliquer();
		Ut.barMenu.update();
	}

	// Annulation sur toutes les pistes (apres une fonction par exemple)
	public void StoreToUndoAll()
	{
		// on passe la booleen saved a false et on ajoute * au titre
		holoEditRef.modify();
		// On memorise qu'il s'agit d'une annulation sur toutes les pistes
		undoAllTracks = true;
		nthTrackUndo = -1;
		nthTrackRedo = -1;
		// On duplique toutes les pistes
		undoTracks = new Vector<HoloTrack>(tracks.size());
		for(HoloTrack h:tracks)
			undoTracks.add(h.dupliquer());
		Ut.barMenu.update();
	}

	// ANNULATION
	public void Undo()
	{
		holoEditRef.modify();
		// Dans le cas d'une annulation sur une seule piste
		if (!undoAllTracks)
		{
			if (nthTrackUndo != -1)
			{
				// On memorise pour le "redo"
				nthTrackRedo = nthTrackUndo;
				redoTrack = getTrack(nthTrackUndo).dupliquer();
				// On retablit la piste
				tracks.set(nthTrackUndo, undoTrack.dupliquer());
				// Maj
				selectTrack(nthTrackUndo);
				redoAllTracks = false;
			}
			// Dans le cas d'une annulation sur toutes les pistes
		} else
		{
			// On memorise "redo" sur toutes les pistes et on retablit toutes les pistes
			redoTracks = new Vector<HoloTrack>(tracks.size());
			for (int i = 0, last = undoTracks.size(); i < last; i++)
			{
				redoTracks.add(getTrack(i).dupliquer());
				tracks.set(i, undoTracks.get(i).dupliquer());
			}
			// Maj
			selectTrack(activeTrack);
			redoAllTracks = true;
		}
		holoEditRef.score.initVars();
		holoEditRef.timeEditor.initVars();
		holoEditRef.room.initVars(false);
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	// ANNULER L'ANNULATION
	public void Redo()
	{ // Dans le cas d'une annulation sur une seule piste
		if (!redoAllTracks)
		{
			if (nthTrackRedo != -1)
			{
				// On retablit la piste
				tracks.set(nthTrackRedo, redoTrack.dupliquer());
				// Maj
				selectTrack(nthTrackRedo);
				holoEditRef.counterPanel.setCompteur(0, getTrack(nthTrackRedo).getLastDate());
				holoEditRef.score.initVars();
				holoEditRef.timeEditor.initVars();
				holoEditRef.room.initVars(false);
				holoEditRef.room.display();
			}
			// Dans le cas d'une annulation sur toutes les pistes
		} else
		{
			// On retablit toutes les pistes
			for (int i = 0, last = redoTracks.size(); i < last; i++)
				tracks.set(i, redoTracks.get(i).dupliquer());
			// Maj
			selectTrack(activeTrack);
			holoEditRef.score.initVars();
			holoEditRef.timeEditor.initVars();
			holoEditRef.room.initVars(false);
			holoEditRef.room.display();
		}
		Ut.barMenu.update();
	}

	// COUPER
	public void Cut()
	{
		System.out.println("appel CUT");
		if (activeTrack != -1)
		{
			Copy();
			Erase();
		}
		Ut.barMenu.update();
	}

	// COPIE
	public void Copy()
	{
		if (activeTrack != -1)
		{
			if (holoEditRef.timeEditor.hasFocus() && !holoEditRef.timeEditor.selIndex.isEmpty()) {
				copySeqs = new HashMap<Integer,HoloTraj>();
				copyWaves = new HashMap<Integer,WaveFormInstance>();
				copySDIFs = new HashMap<Integer,SDIFdataInstance>();
				// On initialise Algor aux dates de debut et de fin des compteurs
				int bb = 1000000000;
				int ee = 0;
				HoloPoint p;
				for(int k:holoEditRef.timeEditor.selIndex)
				{
					int[] iA = TimeIndex.decode(k);
					p = getActiveTrack().getHoloTraj(iA[2]).points.get(iA[3]);
					bb = Ut.min(p.date,bb);
					ee = Ut.max(p.date,ee);
				}
				algor = new Algors();
				algor.dateBegin = bb;
				algor.dateEnd = ee;
				// On copie la piste dans la piste de copie
				copyTrack = algor.Copy(getTrack(activeTrack), holoEditRef.score.hasFocus());
			} else if (holoEditRef.room.hasFocus() && !holoEditRef.room.selIndex.isEmpty()) {
				IntegerVector sortSelVector = new IntegerVector(holoEditRef.room.selIndex);
				sortSelVector.sort();
				
				if(holoEditRef.allTrackActive)
				{
					holoEditRef.room.selIndex = new Vector<Integer>();
					int activ = getActiveTrackNb();
					for(int ind : sortSelVector)
					{
						int[] iA = RoomIndex.decode(ind);
						if(iA[1] == activ)
							holoEditRef.room.selIndex.add(ind);
					}
				} else {
					holoEditRef.room.selIndex = sortSelVector.getVector();
				}
				
				int first = sortSelVector.firstElement();
				int last = sortSelVector.lastElement();
				int[] fA = RoomIndex.decode(first);
				int[] lA = RoomIndex.decode(last);
				algor = new Algors();
				// On initialise Algor aux dates de debut et de fin de la selection
				algor.dateBegin = getTrack(fA[1]).getHoloTraj(fA[2]).points.get(fA[3]).date;
				algor.dateEnd = getTrack(lA[1]).getHoloTraj(lA[2]).points.get(lA[3]).date;
				// On copie la piste dans la piste de copie
				copyTrack = algor.Copy(getTrack(activeTrack), false);
				copySeqs = new HashMap<Integer,HoloTraj>();
				copyWaves = new HashMap<Integer,WaveFormInstance>();
				copySDIFs = new HashMap<Integer,SDIFdataInstance>();
			} else if (holoEditRef.score.hasFocus() && !holoEditRef.score.selIndex.isEmpty()) {
				copySeqs = new HashMap<Integer,HoloTraj>();
				copyWaves = new HashMap<Integer,WaveFormInstance>();
				copySDIFs = new HashMap<Integer,SDIFdataInstance>();
				int minTk = 1000;
				for(int i:holoEditRef.score.selIndex)
				{
					int beg = holoEditRef.counterPanel.getDate(1);
					int[] iA = ScoreIndex.decode(i);
					minTk = Ut.min(minTk,iA[1]);
					if(ScoreIndex.isSeq())
					{
						HoloTraj cropped = getTrack(iA[1]).getHoloTraj(iA[2]).dupliquer();
						cropped.crop(beg,holoEditRef.counterPanel.getDate(2));
						cropped.shiftDates(-beg);
						copySeqs.put(i,cropped);
					} else if(ScoreIndex.isWave())
					{
						w = getTrack(iA[1]).getWave(iA[2]).dupliquer();
						if(w.getFirstDate() >= holoEditRef.counterPanel.getDate(1) && w.getLastDate() <= holoEditRef.counterPanel.getDate(2))
						{
							w.shiftDates(-beg);
							copyWaves.put(i,w);
						}
					} else if(ScoreIndex.isData())
					{
						sdif = getTrack(iA[1]).getSDIFinst(iA[2]).dupliquer();
						if(sdif.getFirstDate() >= holoEditRef.counterPanel.getDate(1) && sdif.getLastDate() <= holoEditRef.counterPanel.getDate(2))
						{
							sdif.shiftDates(-beg);
							copySDIFs.put(i,sdif);
						}
					} 
				}
				holoEditRef.gestionPistes.selectTrack(minTk);
				holoEditRef.score.oldActiveTrack = activeTrack;
				copyTrack = new HoloTrack();
			} else {
				copySeqs = new HashMap<Integer,HoloTraj>();
				copyWaves = new HashMap<Integer,WaveFormInstance>();
				copySDIFs = new HashMap<Integer,SDIFdataInstance>();
				// On initialise Algor aux dates de debut et de fin des compteurs
				algor = new Algors();
				algor.dateBegin = holoEditRef.counterPanel.getDate(1);
				algor.dateEnd = holoEditRef.counterPanel.getDate(2);
				// On copie la piste dans la piste de copie
				System.err.println("activeTrack="+activeTrack);
				copyTrack = algor.Copy(getTrack(activeTrack), holoEditRef.score.hasFocus());
				System.err.println("copyTrack="+copyTrack);
			}
		}
		Ut.barMenu.update();
	}

	public void Copy(int track)
	{
		// On initialise Algor aux dates de debut et de fin des compteurs
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		algor.dateEnd = holoEditRef.counterPanel.getDate(2);
		// On copie la piste dans la piste de copie
		copyTrack = algor.Copy(getTrack(track), false);
		Ut.barMenu.update();
	}

	public void Copy(int track, int begin, int end)
	{
		// On initialise Algor aux dates de debut et de fin des compteurs
		algor = new Algors();
		algor.dateBegin = begin;
		algor.dateEnd = end;
		// On copie la piste dans la piste de copie
		copyTrack = algor.Copy(getTrack(track), false);
		Ut.barMenu.update();
	}
	
	// COLLER
	// ----- place le trajet copie sur la piste destination apres les autres points ou
	// ----- a la position beg-time si elle est sup aux derniers points de la piste dest.
	public void Paste()
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		if (activeTrack != -1)
		{
			if (!copyTrack.isEmpty())
			{
				// On memorise la piste active
				StoreToUndo();
				// On colle
				algor.Paste(getTrack(activeTrack), copyTrack, holoEditRef.score.hasFocus());
				// Maj
				if(holoEditRef.score.hasFocus() && !getActiveTrack().waves.isEmpty())
					holoEditRef.counterPanel.setCompteur(0, Ut.max(getActiveTrack().waves.lastElement().getLastDate(),getActiveTrack().getLastDate()));
				else 
					holoEditRef.counterPanel.setCompteur(0, getActiveTrack().getLastDate());
				holoEditRef.room.display();
			} else if(!copySeqs.isEmpty() || !copyWaves.isEmpty()) {
				StoreToUndoAll();
				int beg = holoEditRef.counterPanel.getDate(1);
				if(/*mainRef.score.trackSelected == mainRef.score.oldTrackSelected ||*/ holoEditRef.score.oldActiveTrack == holoEditRef.gestionPistes.getActiveTrackNb())
				{
					int maxDate = 0;
					int maxEnd = 0;
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i));
							tk = getTrack(iA[1]);
							int date = tk.getLastDate();
							if(tk.isEmpty() || date < beg)
								maxDate = Ut.max(beg,maxDate);
							else
								maxDate = Ut.max(date,maxDate);
						}
					}
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i));
							tk = getTrack(iA[1]);
							if(!tk.waves.isEmpty())
							{
								int date = tk.waves.lastElement().getLastDate();
								if(tk.isEmpty() || date < beg)
									maxDate = Ut.max(beg,maxDate);
								else
									maxDate = Ut.max(date,maxDate);
							}
						}
						
					}
					if(!copySDIFs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySDIFs.keySet());
						for(int i = 0, last = copySDIFs.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i)); // a voir
							tk = getTrack(iA[1]); // a voir
							if(!tk.sdifdataInstanceVector.isEmpty())
							{
								int date = tk.sdifdataInstanceVector.lastElement().getLastDate();
								if(tk.isEmpty() || date < beg)
									maxDate = Ut.max(beg,maxDate);
								else
									maxDate = Ut.max(date,maxDate);
							}
						}
						
					}
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						for(HoloTraj ht2:tjs)
							maxEnd = Ut.max(ht2.getFirstDate() + ht2.getDuration(), maxEnd);
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							ht = tjs.get(i);
							tk.addTraj(ht.dupliquer(), maxDate+ht.getFirstDate());
							tk.update();
						}
					}
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						for(WaveFormInstance w2:waves)
							maxEnd = Ut.max(w2.getFirstDate() + w2.getDuration(), maxEnd);
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							w = waves.get(i).dupliquer();
							w.setBegTime(maxDate+w.getFirstDate());
							tk.addWave(w);
							tk.update();
						}
					}
					if(!copySDIFs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySDIFs.keySet());
						Vector<SDIFdataInstance> sdifdataInstanceVector = new Vector<SDIFdataInstance>(copySDIFs.values());
						for(SDIFdataInstance sdif2:sdifdataInstanceVector)
							maxEnd = Ut.max(sdif2.getFirstDate() + sdif2.getDuration(), maxEnd);
						for(int i = 0, last = copySDIFs.size() ; i < last ; i++)
						{
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							sdif = sdifdataInstanceVector.get(i).dupliquer();
							sdif.setBegTime(maxDate+sdif.getFirstDate());
							tk.addSDIF(sdif);
							tk.update();
						}
					}
					holoEditRef.counterPanel.setCompteur(2,maxDate+maxEnd);
				} else {
					int deltaTk;
//					if(mainRef.score.oldTrackSelected == -1)
						deltaTk = activeTrack - holoEditRef.score.oldActiveTrack;
//					else
//						deltaTk = mainRef.score.trackSelected - mainRef.score.oldTrackSelected;
					int maxDate = 0;
					int maxEnd = 0;
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i));
							int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							int date = tk.getLastDate();
							if(tk.isEmpty() || date < beg)
								maxDate = Ut.max(beg,maxDate);
							else
								maxDate = Ut.max(date,maxDate);
						}
					}
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i));
							int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							if(!tk.waves.isEmpty())
							{
								int date = tk.waves.lastElement().getLastDate();
								if(tk.isEmpty() || date < beg)
									maxDate = Ut.max(beg,maxDate);
								else
									maxDate = Ut.max(date,maxDate);
							}
						}
					}
					if(!copySDIFs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySDIFs.keySet());
						for(int i = 0, last = copySDIFs.size() ; i < last ; i++)
						{
							int[] iA = ScoreIndex.decode(keys.get(i));
							int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							if(!tk.sdifdataInstanceVector.isEmpty())
							{
								int date = tk.sdifdataInstanceVector.lastElement().getLastDate();
								if(tk.isEmpty() || date < beg)
									maxDate = Ut.max(beg,maxDate);
								else
									maxDate = Ut.max(date,maxDate);
							}
						}
					}
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						for(HoloTraj ht2:tjs)
							maxEnd = Ut.max(ht2.getFirstDate() + ht2.getDuration(), maxEnd);
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							ht = tjs.get(i);
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							tk.addTraj(ht.dupliquer(), maxDate+ht.getFirstDate());
							tk.update();
						}
					}
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						for(WaveFormInstance w2:waves)
							maxEnd = Ut.max(w2.getFirstDate() + w2.getDuration(), maxEnd);
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							System.out.println("paste oldTK "+ScoreIndex.tk+" d "+deltaTk+" "+nTkNum);
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							w = waves.get(i).dupliquer();
							w.setBegTime(maxDate+w.getFirstDate());
							tk.addWave(w);
							tk.update();
						}
					}
					if(!copySDIFs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySDIFs.keySet());
						Vector<SDIFdataInstance> sdifs = new Vector<SDIFdataInstance>(copySDIFs.values());
						for(SDIFdataInstance sdif2:sdifs)
							maxEnd = Ut.max(sdif2.getFirstDate() + sdif2.getDuration(), maxEnd);
						for(int i = 0, last = copySDIFs.size() ; i < last ; i++)
						{
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							System.out.println("paste oldTK "+ScoreIndex.tk+" d "+deltaTk+" "+nTkNum);
							tk = holoEditRef.gestionPistes.getTrack(nTkNum);
							sdif = sdifs.get(i).dupliquer();
							sdif.setBegTime(maxDate + sdif.getFirstDate());
							tk.addSDIF(sdif);
							tk.update();
						}
					}
					holoEditRef.counterPanel.setCompteur(2,maxDate+maxEnd);
				}
				holoEditRef.room.display();
			}
		}
		Ut.barMenu.update();
	}
	
	// Pour les fonctions qui sont deja dans sauvegardees Undo (application d'une fonction a plusieurs pistes)
	public void PasteWithoutStore()
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		if (activeTrack != -1)
		{
			if (!copyTrack.isEmpty())
			{
				// On colle
				algor.Paste(getTrack(activeTrack), copyTrack, false);
				// Maj
				holoEditRef.counterPanel.setCompteur(0, getTrack(activeTrack).getLastDate());
				holoEditRef.room.display();
			}
		}
		Ut.barMenu.update();
	}

	// Pour les fonctions qui sont deja dans sauvegardees Undo (application d'une fonction a plusieurs pistes)
	public void PasteWithoutStore(int tkNum)
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		algor.Paste(getTrack(tkNum), copyTrack, false);
		Ut.barMenu.update();
	}

	// REMPLACER
	// ----- inserre le presse papier a la date BeginTime et decale les points de la piste
	// ----- qui etaient situes apres le BeginTime de la taille du presse papier
	public void Replace()
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		if (activeTrack != -1)
		{
			if (!copyTrack.isEmpty())
			{
				// On memorise la piste active
				StoreToUndo();
				// On recupere la duree de la piste de copie
				int dateFinCopy;
				if(holoEditRef.score.hasFocus() && !copyTrack.waves.isEmpty())
					dateFinCopy = holoEditRef.counterPanel.getDate(1) + Ut.max(copyTrack.getDuration(),copyTrack.waves.lastElement().getLastDate()-copyTrack.waves.firstElement().getFirstDate());
				else 
					dateFinCopy = holoEditRef.counterPanel.getDate(1) + copyTrack.getDuration();
				// On remplace
				algor.Replace(getTrack(activeTrack), copyTrack, holoEditRef.score.hasFocus());
				// Maj des compteurs avec la duree de la piste de copie
				if (dateFinCopy > holoEditRef.counterPanel.getDate(2))
					holoEditRef.counterPanel.setCompteur(0, dateFinCopy);
				holoEditRef.room.display();
			} else if(!copySeqs.isEmpty()) {
				StoreToUndoAll();
				Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
				Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
				int beg = holoEditRef.counterPanel.getDate(1);
				int end = holoEditRef.counterPanel.getDate(2);
				if(holoEditRef.score.oldActiveTrack == holoEditRef.gestionPistes.getActiveTrackNb())
				{
					int maxDate = 0;
					boolean crop = false;
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						ht = tjs.get(i);
						maxDate = Ut.max(ht.getLastDate(),maxDate);
					}
					if(end - beg == 0)
						end = beg+maxDate;
					if(maxDate > end-beg)
						crop = true;
					Vector<HoloTrack> tkToCut = new Vector<HoloTrack>();
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
						if(!tkToCut.contains(tk))
						{
							tkToCut.add(tk);
							if(!tk.isEmpty())
								tk.cut(beg, end, false, false);
						}
					}
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						int index = keys.get(i);
						ht = tjs.get(i);
						int[] iA = ScoreIndex.decode(index);
						tk = getTrack(iA[1]);
						if(crop)
						{
							HoloTraj ht2 = ht.dupliquer();
							ht2.crop(0,end-beg);
							tk.addTraj(ht2, beg+ht.getFirstDate());
						} else {
							tk.addTraj(ht.dupliquer(), beg+ht.getFirstDate());
						}
						tk.update();
					}
				} else {
					int deltaTk;
					deltaTk = activeTrack - holoEditRef.score.oldActiveTrack;
					int maxDate = 0;
					boolean crop = false;
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						ht = tjs.get(i);
						maxDate = Ut.max(ht.getLastDate(),maxDate);
					}
					if(end - beg == 0)
						end = beg+maxDate;
					if(maxDate > end-beg)
						crop = true;
					Vector<HoloTrack> tkToCut = new Vector<HoloTrack>();
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
						tk = getTrack(nTkNum);
						if(!tkToCut.contains(tk))
						{
							tkToCut.add(tk);
							if(!tk.isEmpty())
								tk.cut(beg,end,false, false);
						}
					}
					for(int i = 0, last = copySeqs.size() ; i < last ; i++)
					{
						ht = tjs.get(i);
						int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
						tk = getTrack(nTkNum);
						if(crop)
						{
							HoloTraj ht2 = ht.dupliquer();
							ht2.crop(0,end-beg);
							tk.addTraj(ht2, beg+ht.getFirstDate());
						} else {
							tk.addTraj(ht.dupliquer(), beg+ht.getFirstDate());
						}
						tk.update();
					}
				}
				holoEditRef.room.display();
			}
		}
		Ut.barMenu.update();
	}

	// Pour les fonctions qui sont deja sauvegardees dans Undo
	public void ReplaceWithoutStore()
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		if (activeTrack != -1)
		{
			if (!copyTrack.isEmpty())
			{
				if (copyTrack.getFirstDate() > algor.dateBegin)
					algor.dateBegin = copyTrack.getFirstDate();
				// On recupere la duree de la piste de copie
				int dateFinCopy = holoEditRef.counterPanel.getDate(1) + copyTrack.getDuration();
				// On remplace
				algor.Replace(getTrack(activeTrack), copyTrack, false);
				// Maj des compteurs avec la duree de la piste de copie
				if (dateFinCopy > holoEditRef.counterPanel.getDate(2))
					holoEditRef.counterPanel.setCompteur(0, dateFinCopy);
				holoEditRef.room.display();
			}
		}
		Ut.barMenu.update();
	}

	public void ReplaceWithoutStore(int track)
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		algor.Replace(getTrack(track), copyTrack, false);
		Ut.barMenu.update();
	}

	public void whatIsInTheCopyTrack()
	{
		System.out.println("________________whatIsInTheCopyTrack________________________");
		if(copySeqs.isEmpty() && copyWaves.isEmpty())
			System.out.println("copied nothing !");
		if(!copySeqs.isEmpty())
		{
			System.out.println("trajs : ");
			Vector<Integer> indexes = new Vector<Integer>(copySeqs.keySet());
			for(int i:indexes)
			{
				ScoreIndex.decode(i);
				System.out.println("\t"+ScoreIndex.toStr());
			}
		}
		if(!copyWaves.isEmpty())
		{
			System.out.println("waves : ");
			Vector<Integer> indexes = new Vector<Integer>(copyWaves.keySet());
			for(int i:indexes)
			{
				ScoreIndex.decode(i);
				System.out.println("\t"+ScoreIndex.toStr());
			}
		}
		if(!copySDIFs.isEmpty())
		{
			System.out.println("sdif : ");
			Vector<Integer> indexes = new Vector<Integer>(copySDIFs.keySet());
			for(int i:indexes)
			{
				ScoreIndex.decode(i);
				System.out.println("\t"+ScoreIndex.toStr());
			}
		}
	}
	
	// TODO SDIF
	// INSERER
	// ----- inserre le presse papier a la date BeginTime et decale les points de la piste
	// ----- qui etaient situes apres le BeginTime de la taille du presse papier
	public void Insert()
	{
		algor = new Algors();
		algor.dateBegin = holoEditRef.counterPanel.getDate(1);
		if (activeTrack != -1)
		{
			if (!copyTrack.isEmpty())
			{
				// On memorise la piste active
				StoreToUndo();
				// On recupere la duree de la piste de copie
				int dateFinCopy;
				if(holoEditRef.score.hasFocus() && !copyTrack.waves.isEmpty())
					dateFinCopy = holoEditRef.counterPanel.getDate(1) + Ut.max(copyTrack.getDuration(),copyTrack.waves.lastElement().getLastDate()-copyTrack.waves.firstElement().getFirstDate());
				else 
					dateFinCopy = holoEditRef.counterPanel.getDate(1) + copyTrack.getDuration();
				// On insere
				algor.Insert(getTrack(activeTrack), copyTrack, holoEditRef.score.hasFocus());
				// Maj des compteurs avec la duree de la piste de copie
				if (dateFinCopy > holoEditRef.counterPanel.getDate(2))
					holoEditRef.counterPanel.setCompteur(2, dateFinCopy);
				holoEditRef.room.display();
			} else if(!copySeqs.isEmpty() || !copyWaves.isEmpty()) {
				StoreToUndoAll();
				int beg = holoEditRef.counterPanel.getDate(1);
				int end = holoEditRef.counterPanel.getDate(2);
				if(holoEditRef.score.oldActiveTrack == holoEditRef.gestionPistes.getActiveTrackNb())
				{
					int maxDate = 0;
					boolean crop = false;
					if(!copySeqs.isEmpty())
					{
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
							maxDate = Ut.max(tjs.get(i).getLastDate(),maxDate);
					}
					if(!copyWaves.isEmpty())
					{
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
							maxDate = Ut.max(waves.get(i).getLastDate(),maxDate);
					}
					if(end - beg == 0)
						end = beg+maxDate;
					if(maxDate > end-beg)
						crop = true;
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						Vector<HoloTrack> tkToShift = new Vector<HoloTrack>();
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							if(!tkToShift.contains(tk))
							{
								tkToShift.add(tk);
								if(!tk.isEmpty())
								{
									int ind = tk.cutAt(beg);
									if (ind != -1)
									{
										tk.trajs.get(ind).firstElement().date += 1;
										if(crop)
											for (int k = ind, last2 = tk.trajs.size() ; k < last2; k++)
												tk.trajs.get(k).shiftDates(end-beg);
										else
											for (int k = ind, last2 = tk.trajs.size() ; k < last2; k++)
												tk.trajs.get(k).shiftDates(maxDate);
									}
								}
							}
						}
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							ht = tjs.get(i);
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							if(crop)
							{
								HoloTraj ht2 = ht.dupliquer();
								ht2.crop(0,end-beg);
								tk.addTraj(ht2, beg+ht.getFirstDate());
							} else
								tk.addTraj(ht.dupliquer(), beg+ht.getFirstDate());
							tk.update();
						}
					}
					
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						Vector<HoloTrack> tkToShift = new Vector<HoloTrack>();
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							if(!tkToShift.contains(tk))
							{
								tkToShift.add(tk);
								for(WaveFormInstance w2:tk.waves)
									if(w2.getFirstDate() >= beg)
										w2.shiftDates(end-beg);
							}
						}
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							w = waves.get(i).dupliquer();
							tk = getTrack(ScoreIndex.decode(keys.get(i))[1]);
							w.setBegTime(beg+w.getFirstDate());
							tk.addWave(w);
							tk.update();
						}
					}
				} else {
					int deltaTk;
					deltaTk = activeTrack - holoEditRef.score.oldActiveTrack;
					int maxDate = 0;
					boolean crop = false;
					if(!copySeqs.isEmpty())
					{
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
							maxDate = Ut.max(tjs.get(i).getLastDate(),maxDate);
					}
					if(!copyWaves.isEmpty())
					{
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
							maxDate = Ut.max(waves.get(i).getLastDate(),maxDate);
					}
					if(end - beg == 0)
						end = beg+maxDate;
					if(maxDate > end-beg)
						crop = true;
					if(!copySeqs.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copySeqs.keySet());
						Vector<HoloTraj> tjs = new Vector<HoloTraj>(copySeqs.values());
						Vector<HoloTrack> tkToShift = new Vector<HoloTrack>();
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = getTrack(nTkNum);
							if(!tkToShift.contains(tk))
							{
								tkToShift.add(tk);
								if(!tk.isEmpty())
								{
									int ind = tk.cutAt(beg);
									if (ind != -1)
									{
										tk.trajs.get(ind).firstElement().date += 1;
										if(crop)
											for (int k = ind, last2 = tk.trajs.size() ; k < last2; k++)
												tk.trajs.get(k).shiftDates(end-beg);
										else
											for (int k = ind, last2 = tk.trajs.size() ; k < last2; k++)
												tk.trajs.get(k).shiftDates(maxDate);
									}
								}
							}
						}
						// insertions des nouveaux trajets
						for(int i = 0, last = copySeqs.size() ; i < last ; i++)
						{
							ht = tjs.get(i);
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = getTrack(nTkNum);
							if(crop)
							{
								HoloTraj ht2 = ht.dupliquer();
								ht2.crop(0,end-beg);
								tk.addTraj(ht2, beg+ht.getFirstDate());
							} else
								tk.addTraj(ht.dupliquer(), beg+ht.getFirstDate());
							tk.update();
						}
					}
					
					if(!copyWaves.isEmpty())
					{
						Vector<Integer> keys = new Vector<Integer>(copyWaves.keySet());
						Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(copyWaves.values());
						Vector<HoloTrack> tkToShift = new Vector<HoloTrack>();
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = getTrack(nTkNum);
							if(!tkToShift.contains(tk))
							{
								tkToShift.add(tk);
								for(WaveFormInstance w2:tk.waves)
									if(w2.getFirstDate() >= beg)
										w2.shiftDates(end-beg);
							}
						}
						// insertion des nouvelles formes d'ondes
						for(int i = 0, last = copyWaves.size() ; i < last ; i++)
						{
							w = waves.get(i).dupliquer();
							int nTkNum = Ut.modabs(ScoreIndex.decode(keys.get(i))[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
							tk = getTrack(nTkNum);
							w.setBegTime(beg+w.getFirstDate());
							tk.addWave(w);
							tk.update();
						}
					}
				}
				holoEditRef.room.display();
			}
		}
		Ut.barMenu.update();
	}

	// ECRASER
	public void Erase()
	{
		if (activeTrack != -1)
		{
			// Memorisation de la piste active
			// S'il y a une selection
			if (holoEditRef.timeEditor.hasFocus() && !holoEditRef.timeEditor.selIndex.isEmpty()) {
				StoreToUndo();
				// On initialise Algor aux dates de debut et de fin des compteurs
				int bb = 1000000000;
				int ee = 0;
				HoloPoint p;
				for(int k:holoEditRef.timeEditor.selIndex)
				{
					int[] iA = TimeIndex.decode(k);
					p = getActiveTrack().getHoloTraj(iA[2]).points.get(iA[3]);
					bb = Ut.min(p.date,bb);
					ee = Ut.max(p.date,ee);
				}
				algor = new Algors();
				algor.dateBegin = bb;
				algor.dateEnd = ee;
				// On ecrase la piste
				if (!getActiveTrack().isEmpty())
				{
					algor.Erase(getActiveTrack(), false);
					holoEditRef.room.display();
					holoEditRef.timeEditor.initVars();
				}
			} else if (holoEditRef.room.hasFocus() && !holoEditRef.room.selIndex.isEmpty()) {
				StoreToUndoAll();
				holoEditRef.room.removePoints();
				holoEditRef.room.display();
			} else if (holoEditRef.score.hasFocus() && !holoEditRef.score.selIndex.isEmpty()) {
				StoreToUndoAll();
				HashMap<Integer,HoloTraj> eraseSeqs = new HashMap<Integer,HoloTraj>();
				HashMap<Integer,WaveFormInstance> eraseWaves = new HashMap<Integer,WaveFormInstance>();
				HashMap<Integer,SDIFdataInstance> eraseSDIFs = new HashMap<Integer,SDIFdataInstance>();
				for(int i:holoEditRef.score.selIndex)
				{
					int[] iA = ScoreIndex.decode(i);
					if(ScoreIndex.isSeq())
						eraseSeqs.put(i,getTrack(iA[1]).getHoloTraj(iA[2]));
					else if(ScoreIndex.isWave())
						eraseWaves.put(i,getTrack(iA[1]).getWave(iA[2]));
					else if(ScoreIndex.isData())
						eraseSDIFs.put(i,getTrack(iA[1]).getSDIFinst(iA[2]));
				}
				int beg = holoEditRef.counterPanel.getDate(1);
				int end = holoEditRef.counterPanel.getDate(2);
				Vector<Integer> indexes = new Vector<Integer>(eraseSeqs.keySet());
				Vector<HoloTraj> tjs = new Vector<HoloTraj>(eraseSeqs.values());
				for(int i = 0, last = eraseSeqs.size() ; i < last ; i++)
				{
					int[] iA = ScoreIndex.decode(indexes.get(i));
					ht = tjs.get(i);
					tk = getTrack(iA[1]);
					if(ht.getFirstDate() >= beg && ht.getLastDate() <= end) {
						tk.trajs.remove(ht);
					} else if(ht.getFirstDate() <= beg && ht.getLastDate() >= beg ||
								ht.getLastDate() >= end && ht.getFirstDate() <= end) {
						HoloTraj rest = ht.cut(beg,end);
						if(rest != null)
							tk.addTraj(rest,end);
					}
					tk.update();
				}
				indexes = new Vector<Integer>(eraseWaves.keySet());
				Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(eraseWaves.values());
				for(int i = 0, last = eraseWaves.size() ; i < last ; i++)
				{
					int[] iA = ScoreIndex.decode(indexes.get(i));
					w = waves.get(i);
					tk = getTrack(iA[1]);
					if(w.getFirstDate() >= beg && w.getLastDate() <= end){
						if (w.getSDIFvector() != null)
							for (SDIFdataInstance sdif : w.getSDIFvector())
								tk.sdifdataInstanceVector.remove(sdif);
						tk.waves.remove(w);
					}
					tk.update();
				}
				indexes = new Vector<Integer>(eraseSDIFs.keySet());
				Vector<SDIFdataInstance> sdifs = new Vector<SDIFdataInstance>(eraseSDIFs.values());
				for(int i = 0, last = eraseSDIFs.size() ; i < last ; i++)
				{
					int[] iA = ScoreIndex.decode(indexes.get(i));
					sdif = sdifs.get(i);
					tk = getTrack(iA[1]);
					if(sdif.getFirstDate() >= beg && sdif.getLastDate() <= end) {
						tk.sdifdataInstanceVector.remove(sdif);
					} else if(sdif.getFirstDate() <= beg && sdif.getLastDate() >= beg ||
							sdif.getLastDate() >= end && sdif.getFirstDate() <= end) {
						SDIFdataInstance rest = sdif.cut(beg,end);
						if(rest != null)
							tk.addSDIF(rest, end);
					}
					tk.update();
				}
				holoEditRef.score.initVars();
				holoEditRef.room.display();
			} else {
				StoreToUndo();
				// On initialise Algor aux dates de debut et de fin des compteurs
				algor = new Algors();
				algor.dateBegin = holoEditRef.counterPanel.getDate(1);
				algor.dateEnd = holoEditRef.counterPanel.getDate(2);
				// On ecrase la piste
				if (!getTrack(activeTrack).isEmpty())
				{
					algor.Erase(getTrack(activeTrack), holoEditRef.score.hasFocus());
					holoEditRef.room.display();
				}
			}
		}
		Ut.barMenu.update();
	}

	// SELECT ALL
	public void selectAll()
	{
		if (activeTrack != -1)
			if (!getActiveTrack().isEmpty())
			{
				if(holoEditRef.score.hasFocus())
				{
					holoEditRef.score.initVars();
					holoEditRef.score.selIndex.addAll(getActiveTrack().getAllTrajs(getActiveTrackNb(),holoEditRef.score.minTime,holoEditRef.score.maxTime));
					holoEditRef.score.selIndex.addAll(getActiveTrack().getAllWaves(getActiveTrackNb(),holoEditRef.score.minTime,holoEditRef.score.maxTime));
					holoEditRef.score.treatSeqIndex();
				} else if(holoEditRef.room.hasFocus()){
					holoEditRef.room.initVars(false);
					holoEditRef.room.selIndex.addAll(getActiveTrack().getAllRoomPoints(getActiveTrackNb(),holoEditRef.counterPanel.getDate(1),holoEditRef.counterPanel.getDate(2),holoEditRef.viewOnlyEditablePoints));
					holoEditRef.room.treatSel();
				} else if(holoEditRef.timeEditor.hasFocus()){
					holoEditRef.timeEditor.initVars();
					holoEditRef.timeEditor.selIndex.addAll(getActiveTrack().getAllTimePoints(0,holoEditRef.timeEditor.beg,holoEditRef.timeEditor.end,holoEditRef.viewOnlyEditablePoints));
					holoEditRef.timeEditor.treatSelIndex();
				}
				holoEditRef.room.display();
			}
		Ut.barMenu.update();
	}

	public void loadAlgos()
	{
		String algDir = "algo";
		File algPath = new File(algDir);
		if (algPath.exists())
		{
			String[] algList = algPath.list(new HoloFilenameFilter(".class"));
			for (String a : algList)
			{
				// Pour Žviter le chargement des class nommŽes Example$1.class (listener...)
				if (a.indexOf('$') == -1)
				{
					try
					{
						algos.add((Algorithm) ClassLoader.getSystemClassLoader().loadClass(algDir + "." + a.substring(0, a.lastIndexOf('.'))).getConstructor(new Class[] { GestionPistes.class }).newInstance(new Object[] { this }));
					}
					catch (Exception e1)
					{
						System.out.println("Can't load " + a + ", please add \".\" to your classpath");
					}
				}
			}
		}
		algos.sort();
	}

	public void saveAlgos()
	{
		for(Algorithm a:algos)
			a.writePst();
	}
	
	// --------------- FILE I/O ----------------
	
	public static File chooseFile(String title, String dir, HoloFilenameFilter filt, HoloFilenameFilterXP filtXP, boolean open_save)
	{
		File selFile;
		if(Ut.MAC)
		{
			FileDialog chooser = new FileDialog(new JFrame(), title, open_save ? FileDialog.SAVE : FileDialog.LOAD);
			chooser.setFilenameFilter(filt);
			chooser.setDirectory(dir);
			chooser.setVisible(true);
			if(chooser.getFile() != null)
			{
				selFile = new File(chooser.getDirectory()+chooser.getFile());
				if(open_save)
				{
					if(selFile.exists())
					{
						if(selFile.canWrite())
							return selFile;
						Ut.alert("Permission Error","You don't have the permissions to overwrite this file.");
						return null;
					}
					if(selFile.getParentFile().canWrite())
						return selFile;
					Ut.alert("Permission Error","You don't have the permissions to write in this directory.");
					return null;
				}
				if(selFile.canRead())
					return selFile;
				Ut.alert("Permission Error","You don't have the permissions to read this file.");
			}
			return null;
		}
		JFileChooser jfc = new JFileChooser(dir);
		jfc.setFileFilter(filtXP);
		jfc.setDialogTitle(title);
		int returnVal = open_save ? jfc.showSaveDialog(new JFrame()) : jfc.showOpenDialog(new JFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			selFile = jfc.getSelectedFile(); 
			if(open_save)
			{
				if(selFile.exists())
				{
					if(selFile.canWrite())
						return selFile;
					Ut.alert("Permission Error","You don't have the permissions to overwrite this file.");
					return null;
				}
				if(selFile.getParentFile().canWrite())
					return selFile;
				Ut.alert("Permission Error","You don't have the permissions to write in this directory.");
				return null;
			}
			if(selFile.canRead())
				return selFile;
			Ut.alert("Permission Error","You don't have permissions to read this file.");
		}
		return null;
	}

	public boolean getHoloFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Open a Holo-Edit Session",holoDirectory,holoFilter,holoFilterXP,false);
		if (fileIn != null)
		{
			holoFilename = fileIn.getName();
			holoDirectory = Ut.dir(fileIn.getParent());
			if(holoEditRef != null)
				holoEditRef.setTitle(holoFilename);
			return true;
		}
		return false;
	}

	public boolean getTkFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import a Holo-Edit Track",tkDirectory,tkFilter,tkFilterXP,false);
		if (fileIn != null)
		{
			tkFilename = fileIn.getName();
			tkDirectory = Ut.dir(fileIn.getParent());
			return true;
		}
		return false;
	}
	
	public boolean getTjFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import a Holo-Edit Trajectory",tjDirectory,tjFilter,tjFilterXP,false);
		if (fileIn != null)
		{
			tjFilename = fileIn.getName();
			tjDirectory = Ut.dir(fileIn.getParent());
			return true;
		}
		return false;
	}
	
	public boolean setTjFileInName(String fn)
	{
		File fileIn = new File(fn);
		if (fileIn.exists())
		{
			tjFilename = fileIn.getName();
			tjDirectory = Ut.dir(fileIn.getParent());
			return true;
		}
		return false;
	}
	
	
	/** Choix d'un fichier midi14 in */
	public boolean getMidiFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import an Holo-Edit v3 Midi File (14bits)",midiDirectory,midFilter,midFilterXP,false);
		if (fileIn != null)
		{
			midiFilename = fileIn.getName();
			midiDirectory = Ut.dir(fileIn.getParent());
			holoEditRef.setTitle(midiFilename);
			return true;
		}
		return false;
	}

	/** Choix d'un fichier midi7 in */
	public boolean getMidiFile7InName()
	{
		File fileIn = GestionPistes.chooseFile("Import an Holo-Edit v3 Midi File (7bits)",midi7Directory,mid7Filter,mid7FilterXP,false);
		if (fileIn != null)
		{
			midi7Filename = fileIn.getName();
			midi7Directory = Ut.dir(fileIn.getParent());
			holoEditRef.setTitle(midi7Filename);
			return true;
		}
		return false;
	}

	/** Choix d'un fichier texte in */
	public boolean getTextFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import an Holo-Edit Text File",textDirectory,txtFilter,txtFilterXP,false);
		if (fileIn != null)
		{
			textFilename = fileIn.getName();
			textDirectory = Ut.dir(fileIn.getParent());
			holoEditRef.setTitle(textFilename);
			return true;
		}
		return false;
	}

	/** Choix d'un fichier seq~ in */
	public boolean getSeqFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import an Holo-Edit Seq~-File",seqDirectory,seqFilter,seqFilterXP,false);
		if (fileIn != null)
		{
			seqFilename = fileIn.getName();
			seqDirectory = Ut.dir(fileIn.getParent());
			holoEditRef.setTitle(seqFilename);
			return true;
		}
		return false;
	}

	public boolean getSoundFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import a sound...",sndDirectory,sndFilter,sndFilterXP,false);
		if (fileIn != null)
		{
			sndFilename = fileIn.getName();
			sndDirectory = Ut.dir(fileIn.getParent());
			sndFile = fileIn;
			return true;
		}
		return false;
	}

	public boolean getExternalDataFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Import a dataFile...",dtDirectory,dtFilter,dtFilterXP,false);
		if (fileIn != null)
		{
			dtFilename = fileIn.getName();
			dtDirectory = Ut.dir(fileIn.getParent());
			dtFile = fileIn;
			return true;
		}
		return false;
	}
	
	/** Choix d'un fichier holo out */
	public boolean getHoloFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Save as Holo-Edit Session",holoDirectory,holoFilter,holoFilterXP,true);
		if (fileOut != null)
		{
			holoFilename = fileOut.getName();
			holoDirectory = Ut.dir(fileOut.getParent());
			if (!holoFilename.endsWith(".holo"))
				if(!holoFilename.endsWith(".holo~"))
					holoFilename = holoFilename.concat(".holo");
			if (!Ut.MAC)
			{
				File fi = new File(Ut.dir(holoDirectory) + holoFilename);
				if(fi.exists())
				{
					int returnVal2 = JOptionPane.showConfirmDialog(null, "File " + holoFilename + " already exists, overwrite ?", "Overwrite ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (returnVal2 == JOptionPane.YES_OPTION)
					{
						holoEditRef.setTitle(holoFilename);
						return true;
					}
					holoFilename = null;
					return false;
				}
			}
			if(holoEditRef != null)
				holoEditRef.setTitle(holoFilename);
			return true;
		}
		holoFilename = null;
		return false;
	}

	/** Choix d'un fichier Track out*/
	public boolean getTkFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Export a Holo-Edit Track",tkDirectory,tkFilter,tkFilterXP,true);
		if (fileOut != null)
		{
			tkFilename = fileOut.getName();
			tkDirectory = Ut.dir(fileOut.getParent());
			if (!tkFilename.endsWith(".tk"))
				tkFilename = tkFilename.concat(".tk");
			if(!Ut.MAC)
			{
				File fi = new File(Ut.dir(tkDirectory) + tkFilename);
				if (fi.exists())
				{
					int returnVal2 = JOptionPane.showConfirmDialog(null, "File " + tkFilename + " already exists, overwrite ?", "Overwrite ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (returnVal2 == JOptionPane.YES_OPTION)
						return true;
					tkFilename = null;
					return false;
				}
			}
			return true;
		}
		tkFilename = null;
		return false;
	}
	
	/** Choix d'un fichier Trajectory out*/
	public boolean getTjFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Export a Holo-Edit Trajectory",tjDirectory,tjFilter,tjFilterXP,true);
		if (fileOut != null)
		{
			tjFilename = fileOut.getName();
			tjDirectory = Ut.dir(fileOut.getParent());
			if (!tjFilename.endsWith(".tj"))
				tjFilename = tjFilename.concat(".tj");
			if (!Ut.MAC)
			{
				File fi = new File(Ut.dir(tjDirectory) + tjFilename);
				if(fi.exists())
				{
					int returnVal2 = JOptionPane.showConfirmDialog(null, "File " + tjFilename + " already exists, overwrite ?", "Overwrite ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (returnVal2 == JOptionPane.YES_OPTION)
						return true;
					tjFilename = null;
					return false;
				}
			}
			return true;
		}
		tjFilename = null;
		return false;
	}
	
	/** Choix d'un fichier ICST Trajectory out*/
	public boolean getICSTFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Export a Holo-Edit Trajectory",tjDirectory,tjFilter,tjFilterXP,true);
		if (fileOut != null)
		{
			tjFilename = fileOut.getName();
			tjDirectory = Ut.dir(fileOut.getParent());
			if (!tjFilename.endsWith(".xml"))
				tjFilename = tjFilename.concat(".xml");
			if (!Ut.MAC)
			{
				File fi = new File(Ut.dir(tjDirectory) + tjFilename);
				if(fi.exists())
				{
					int returnVal2 = JOptionPane.showConfirmDialog(null, "File " + tjFilename + " already exists, overwrite ?", "Overwrite ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (returnVal2 == JOptionPane.YES_OPTION)
						return true;
					tjFilename = null;
					return false;
				}
			}
			return true;
		}
		tjFilename = null;
		return false;
	}
	
	/** Choix d'un fichier SDIF Trajectory out*/
	public boolean getSDIFFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Export a Holo-Edit Trajectory",tjDirectory,tjFilter,tjFilterXP,true);
		if (fileOut != null)
		{
			tjFilename = fileOut.getName();
			tjDirectory = Ut.dir(fileOut.getParent());
			if (!tjFilename.endsWith(".sdif"))
				tjFilename = tjFilename.concat(".sdif");
			if (!Ut.MAC)
			{
				File fi = new File(Ut.dir(tjDirectory) + tjFilename);
				if(fi.exists())
				{
					int returnVal2 = JOptionPane.showConfirmDialog(null, "File " + tjFilename + " already exists, overwrite ?", "Overwrite ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (returnVal2 == JOptionPane.YES_OPTION)
						return true;
					tjFilename = null;
					return false;
				}
			}
			return true;
		}
		tjFilename = null;
		return false;
	}
	
	public void readHoloFile()
	{
		if(holoEditRef == null)
		{
			if(hpRef.askForSave())
			{
				if (getHoloFileInName())
					readHoloFile(Ut.dir(holoDirectory) + holoFilename);
			}
		} else {
			if (holoEditRef.askForSave())
			{
				if (getHoloFileInName())
				{
					readHoloFile(Ut.dir(holoDirectory) + holoFilename);
					HoloRecentFile rf = new HoloRecentFile(holoEditRef, holoDirectory, holoFilename, 4);
					holoEditRef.insertRecent(rf);
				}
			}
		}
		
		preparePlay();
	}

	/** lecture holo */
	public void readHoloFile(String filename)
	{
		if(holoEditRef != null)
		{	
			if(holoEditRef.connection.isPlaying())
				holoEditRef.connection.stop();
			holoEditRef.setSaved(true);
		} else {
			if(hpRef.isPlaying())
				hpRef.stop();
			hpRef.setSaved(true);
		}
		new HoloFileReader(this, filename);
		preparePlay();
	}

	public void readDroppedFile(String droppedFileName)
	{
		if(droppedFileName.endsWith(".holo") || droppedFileName.endsWith(".holo~"))
		{
			if (holoEditRef.askForSave())
			{
				holoFilename = new File(droppedFileName).getName();
				holoDirectory = new File(droppedFileName).getParent();
				readHoloFile(Ut.dir(holoDirectory) + holoFilename);
				HoloRecentFile rf = new HoloRecentFile(holoEditRef, holoDirectory, holoFilename, 4);
				holoEditRef.insertRecent(rf);
				holoEditRef.setTitle(holoFilename);
			}
		} else {
			// FEATURE dropped wav / tk / tj
		}
	}

	/** Import des anciens formats de fichier (midi/midi7/txt/seq~) */
	public void readFile(String type)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(!holoEditRef.askForSave()) return;
		// Lecture midi 14
		if (type.equalsIgnoreCase("midi14"))
		{
			if (getMidiFileInName())
			{
				readFile14b(Ut.dir(midiDirectory) + midiFilename);
				HoloRecentFile rf = new HoloRecentFile(holoEditRef, midiDirectory, midiFilename);
				holoEditRef.insertRecent(rf);
				holoFilename = null;
			}
		} else if (type.equalsIgnoreCase("midi7")) {
			if (getMidiFile7InName())
			{
				readFile7bp(Ut.dir(midi7Directory) + midi7Filename);
				HoloRecentFile rf = new HoloRecentFile(holoEditRef, midi7Directory, midi7Filename, 1);
				holoEditRef.insertRecent(rf);
				holoFilename = null;
			}
		} else if (type.equalsIgnoreCase("txt")) {
			if (getTextFileInName())
			{
				readTextFile(Ut.dir(textDirectory) + textFilename);
				HoloRecentFile rf = new HoloRecentFile(holoEditRef, textDirectory, textFilename, 2);
				holoEditRef.insertRecent(rf);
				holoFilename = null;
			}
		} else if (type.equalsIgnoreCase("seq")) {
			if (getSeqFileInName())
			{
				readSeqFile(Ut.dir(seqDirectory) + seqFilename);
				HoloRecentFile rf = new HoloRecentFile(holoEditRef, seqDirectory, seqFilename, 3);
				holoEditRef.insertRecent(rf);
				holoFilename = null;
			}
		} else {
			Ut.print("Read : unresolved type : " + type);
		}
	}

	/** lecture midi 14 */
	public void readFile14b(String Nomfichier){
		holoEditRef.setSaved(true);
		new MidiRead(this).readFile(Nomfichier, true);
	}

	/** lecture Midi 7 */
	public void readFile7bp(String Nomfichier){
		holoEditRef.setSaved(true);
		new MidiRead(this).readFile(Nomfichier, false);
	}

	/** lecture fichier texte */
	public void readTextFile(String Nomfichier)
	{
		TextRead textRead = new TextRead(this);
		holoEditRef.setSaved(true);
		textRead.readTextFile(Nomfichier);
	}
	
	/** lecture fichier texte sans confirmation */
	public void readTextFile2(String Nomfichier){
		TextRead textRead = new TextRead(this);
		textRead.readTextFileDirect(Nomfichier);
	}

	/** lecture fichier seq~ */
	public void readSeqFile(String Nomfichier)
	{
		SeqRead seqRead = new SeqRead(this);
		holoEditRef.setSaved(true);
		seqRead.readSeqFile(Nomfichier);
	}

	/** lecture fichier seq~ sans confirmation */
	public void readSeqFileDirect(String Nomfichier){
		SeqRead seqRead = new SeqRead(this);
		seqRead.readSeqFileDirect(Nomfichier);
	}

	/** importation d'un fichier son */
	public void importSound(){
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if (getSoundFileInName()){
			holoEditRef.soundPoolGui.importSound(sndFile, true);
			holoEditRef.modify();
		}
	}
	
	// ADDED
	/** Importing external data */
	public void importExternalData(){
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if (getExternalDataFileInName()){
			holoEditRef.soundPoolGui.importData(dtFile, null, true);
			holoEditRef.modify();
		}
	}
	// END ADDED
	
	/** import Track */
	public void importTrack(int toTkNum, int atTime){
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(getTkFileInName())
			new TkFileReader(this, Ut.dir(tkDirectory) + tkFilename, toTkNum, atTime);
	}
	
	/** export Track */
	public void exportTrack(int tkNum){
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(getTkFileOutName())
			new TkFileWriter(this, Ut.dir(tkDirectory) + tkFilename, tkNum);
	}
	
	/** import Trajectory */
	public void importSeq(int toTkNum, int atTime)
	{
		if(holoEditRef != null)
		{	
			if(holoEditRef.connection.isPlaying())
				holoEditRef.connection.stop();
		} else {
			if(hpRef.isPlaying())
				hpRef.stop();
		}
		if(getTjFileInName())
			new TjFileReader(this, Ut.dir(tjDirectory) + tjFilename, toTkNum, atTime);
		
	}
	
	/** import Trajectory with filename already set */
	public void importSeq2(int toTkNum, int atTime)
	{
		if(holoEditRef != null)
		{	
			if(holoEditRef.connection.isPlaying())
				holoEditRef.connection.stop();
		} else {
			if(hpRef.isPlaying())
				hpRef.stop();
		}
		new TjFileReader(this, Ut.dir(tjDirectory) + tjFilename, toTkNum, atTime);
	}
	
	/** export Trajectory */
	public void exportSeq(int tkNum, int seqNum)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(getTjFileOutName())
			new TjFileWriter(this, Ut.dir(tjDirectory) + tjFilename, tkNum, seqNum);
	}
	
	/** export Trajectory with filename already set */
	public void exportSeq2(int tkNum, int seqNum)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
			new TjFileWriter(this, Ut.dir(tjDirectory) + tjFilename, tkNum, seqNum);
	}
	
	
	/** export Trajectory */
	public void exportSeqICST(int tkNum, int seqNum)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(getICSTFileOutName())
			new ICSTFileWriter(this, Ut.dir(tjDirectory) + tjFilename, tkNum, seqNum);
	}
	
	/** export Trajectory */
	public void exportSeqSDIF(int tkNum, int seqNum)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if(getSDIFFileOutName())
			new SDIFFileWriter(this, Ut.dir(tjDirectory) + tjFilename, tkNum, seqNum);
	}
	
	
	public void writeHoloFile()
	{
		if (holoFilename == null || holoFilename.equalsIgnoreCase(""))
		{
			writeHoloFileAs();
			return;
		}
		if (!holoFilename.endsWith(".holo"))
			if(!holoFilename.endsWith(".holo~"))
				holoFilename = holoFilename.concat(".holo");
		String filename = Ut.dir(holoDirectory) + holoFilename;
		File f = new File(filename);
		if(f.exists())
			f.renameTo(new File(filename+"~"));
		writeHoloFile(filename);
		HoloRecentFile rf = new HoloRecentFile(holoEditRef, holoDirectory, holoFilename, 4);
		if(holoEditRef == null)
		{
			hpRef.setSessionName(holoFilename);
		} else {
			holoEditRef.setTitle(holoFilename);
			holoEditRef.insertRecent(rf);
		}
	}

	public void writeHoloFileAs()
	{
		if (getHoloFileOutName())
		{
			writeHoloFile(Ut.dir(holoDirectory) + holoFilename);
			HoloRecentFile rf = new HoloRecentFile(holoEditRef, holoDirectory, holoFilename, 4);
			if(holoEditRef == null)
			{
				hpRef.setSessionName(holoFilename);
			} else {
				holoEditRef.setTitle(holoFilename);
				holoEditRef.insertRecent(rf);
			}
		}
	}

	public void writeHoloFile(String filename)
	{
		if(holoEditRef == null)
		{
			if(hpRef.isPlaying())
				hpRef.stop();
		} else {
			if(holoEditRef.connection.isPlaying())
				holoEditRef.connection.stop();
		}
		new HoloFileWriter(this, filename);
	}

	// --------------- TRACK MANAGEMENT ----------------------
	
	public HoloTrack getTrack(int i){
		return i < tracks.size() && i >= 0 ? tracks.get(i) : null;
	}

	public HoloTrack getActiveTrack(){
		if(activeTrack != -1)
			return getTrack(activeTrack);
		return null;
	}

	public int getActiveTrackNb(){
		return activeTrack;
	}

	public void setActiveTrack(int i){
		activeTrack = Ut.clip(i, -1, tracks.size());
	}

	public int getNbTracks(){
		return tracks.size();
	}

	public boolean getVisibleTks()
	{ 
		// On ajoute au vecteur pistesV toutes les pistes visibles non-vides
		pistesV = new Vector<Integer>();
		if (activeTrack != -1)
			for (int i = 0; i < tracks.size(); i++)
				if (getTrack(i).isVisible())
					if (!getTrack(i).isEmpty())
						pistesV.add(i);
		return !pistesV.isEmpty();
	}
	
	public void changeVisible(int numeroPiste)
	{
		getTrack(numeroPiste).changeVisible();
		if(holoEditRef == null)
			return;
		holoEditRef.room.initVars(false);
		if (!getTrack(numeroPiste).isVisible())
		{ // decocher
			if (activeTrack == numeroPiste)
			{
				ts.labelAudio[numeroPiste].desactivate();
				// on doit chercher la nouvelle piste a selectionner :
				// on cherche la premiere piste visible, s'il y en a une
				int pisterec = 0;
				while ((pisterec < tracks.size()) && (!getTrack(pisterec).isVisible()))
					++pisterec;
				if (pisterec == tracks.size())
				{ // aucune piste n'est visible
					activeTrack = -1;
				} else
					selectTrack(pisterec);
			}
		}
		// cocher
		else
		{
			selectTrack(numeroPiste);
		}
		holoEditRef.room.display();
	}

	public void selectTrack(int tkNum)
	{
		// On a des changements uniquement si on clique sur une piste differente de la piste active
		if (tkNum != activeTrack)
		{
			if (activeTrack != -1) // si il y avait deja une piste active, on la desactive
			{
				ts.labelAudio[activeTrack].desactivate();
			}
			
			activeTrack = tkNum;
			if (activeTrack != -1)
			{
				ts.labelAudio[activeTrack].activate();
				ts.checkVisible[activeTrack].check(true);
				getTrack(activeTrack).setVisible(true);
			}
			holoEditRef.room.initVars(false);
			holoEditRef.timeEditor.initVars();
		}
		if(holoEditRef.connection.isOpen())
			holoEditRef.connection.sendVisible();
	}

	public void updateTrackSelector(int shift)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		int tmpActiv = getActiveTrackNb();
		setActiveTrack(-1);
		boolean tsOpen = ts.visible;
		int x = ts.posX;
		int y = ts.posY;
		if(tsOpen) ts.close();
		ts = new TrackSelector(this);
		ts.setLocation(x,y);
		if(tsOpen) ts.open();
		for(int i = 0, last = ts.checkVisible.length ; i < last ; i++)
			ts.checkVisible[i].check(tracks.get(i).isVisible());
		switch(shift)
		{
		case -1:
			selectTrack(getNbTracks()-1);
			break;
		case -2:
			if(tmpActiv < shift)
				selectTrack(tmpActiv);
			else
				selectTrack(Ut.clipL(--tmpActiv,0));
			break;
		case -3:
			viewAll();
			selectTrack(0);
			break;
		default:
			selectTrack(shift);
			break;
		}
		holoEditRef.gestionPistes.ts.hpEdition.setSelected(holoEditRef.hpEditMode);
		holoEditRef.gestionPistes.ts.labelPlusDelta.setSelected(holoEditRef.shortViewMode);
		holoEditRef.score.initSizes();
	}

	public void reset()
	{
		for (int i = 0, last = tracks.size() ; i < last ; i++)
		{
			tk = getTrack(i);
			tk.setNumber(i+1);
			tk.setName("Track "+tk.getNumber());
		}
		updateTrackSelector(getActiveTrackNb());
		holoEditRef.modify();
	}

	public void resetnames()
	{
		for (int i = 0, last = tracks.size() ; i < last ; i++)
		{
			tk = getTrack(i);
			tk.setName("Track "+tk.getNumber());
		}
		updateTrackSelector(getActiveTrackNb());
		holoEditRef.modify();
	}

	public void resetnumbers()
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		for (int i = 0, last = tracks.size() ; i < last ; i++)
			getTrack(i).setNumber(i+1);
		updateTrackSelector(getActiveTrackNb());
		holoEditRef.modify();
	}

	public void setDirty(boolean dirty)
	{
		for(HoloTrack t : tracks)
		{
			t.setDirty(dirty);
		}
	}
	
	public void setDirty(int mask)
	{
		for(HoloTrack t : tracks)
		{
			t.setDirty(mask);
		}
	}
	
	public void preparePlay()
	{
		for(HoloTrack tk : tracks)
		{
			tk.preparePlay();
		}
	}
	
	public void stop()
	{
		for(HoloTrack tk : tracks)
		{
			tk.stop(true);
		}
	}
	
	public void initOneTrack(int numero)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		if (!getTrack(numero).isEmpty())
		{
			int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise the track \"" + getTrack(numero).getName() + "\" ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			// System.out.println("result : " + result);
			if (result == 0)
			{
				init(numero);
				holoEditRef.room.display();
				holoEditRef.modify();
			}
		}
	}

	public void init(int tkNum)
	{
		if (getTrack(tkNum).init())
			holoEditRef.room.initVars(true);
	}

	public void initVisibleTracks()
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		// Construction de la chaine de caractere pour le message.
		String tmp = new String();
		for (int k = 0, last = getNbTracks(); k < last; k++)
			if (getTrack(k).isVisible())
				if (!getTrack(k).isEmpty())
					tmp = tmp + "\"" + getTrack(k).getName() + "\", ";
		if (tmp.length() > 0)
		{
			tmp = tmp.substring(0, tmp.length() - 2);
			int ind = tmp.lastIndexOf(",");
			if (ind != -1)
			{
				String tmp2 = tmp.substring(0, ind);
				tmp = tmp2 + " and" + tmp.substring(ind + 1);
			}
			int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise the tracks " + tmp + " ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			// Si validation par l'utilisateur
			if (result == 0)
			{
				// Toutes les pistes visibles sont initialisees
				for (int k = 0, last = getNbTracks(); k < last; k++)
					if (getTrack(k).isVisible())
						init(k);
				// Mise a jour
//				majEchelleTemps(getActiveTrackNb());
				holoEditRef.room.display();
				holoEditRef.modify();
			}
		}
	}

	public void initTracks(boolean force)
	{
		if(holoEditRef.connection.isPlaying())
			holoEditRef.connection.stop();
		int result = 0;
		if(!force) result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise all tracks ?", "Init All", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0)
		{
			for (int i = 0; i < getNbTracks() ; i++)
			{
				getTrack(i).init();
				getTrack(i).setVisible(false);
				ts.labelAudio[i].desactivate();
				ts.checkVisible[i].check(false);
			}
			undoTracks = new Vector<HoloTrack>(tracks.size());
			redoTracks = new Vector<HoloTrack>(tracks.size());
			activeTrack = 0;
			getTrack(0).setVisible(true);
			ts.labelAudio[0].activate();
			ts.checkVisible[0].check(true);
			redoTrack = new HoloTrack();
			undoTrack = new HoloTrack();
			undoAllTracks = false;
			redoAllTracks = false;
			nthTrackUndo = -1;
			nthTrackRedo = -1;
			holoFilename = null;
			holoEditRef.setTitle("Untitled");
			holoEditRef.setSaved(true);
			holoEditRef.counterPanel.setCompteur(1, 0);
			holoEditRef.counterPanel.setCompteur(2, 100);
			holoEditRef.counterPanel.setCompteur(3, 0);
			holoEditRef.score.initSizes();
			holoEditRef.score.initVars();
			holoEditRef.timeEditor.initVars();
			holoEditRef.room.initVars(true);
		}
	}

	public void initTracks2(boolean force)
	{
		if(hpRef.isPlaying())
			hpRef.stop();
		int result = 0;
		if(!force) result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise all tracks ?", "Init All", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0)
		{
			for (int i = 0; i < getNbTracks() ; i++)
			{
				getTrack(i).init();
				getTrack(i).setVisible(true);
			}
			hpRef.setSessionName(null);
			hpRef.setSaved(true);
		}
	}
	
	public void viewAllButOne(int numero)
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{
			// si la piste n'est pas celle cliquee on la rend visible et cochee
			if (k != numero)
			{
				getTrack(k).setVisible(true);
				ts.labelAudio[k].desactivate();
				ts.checkVisible[k].check(true);
				// sinon on la decoche et on la rend invisible.
			} else
			{
				getTrack(k).setVisible(false);
				ts.labelAudio[k].desactivate();
				ts.checkVisible[k].check(false);
			}
		}
		// on affecte ensuite comme piste active la premiere piste sauf dans le cas ou on l'a decochee
		if (numero != 0)
		{
			selectTrack(0);
			ts.labelAudio[0].activate();
		} else
		{
			selectTrack(1);
			ts.labelAudio[1].activate();
		}
		holoEditRef.room.display();
	}

	public void viewSolo(int numero)
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{ // On desactive toutes les pistes, on les decoche et rend invisible
			if (k == numero)
			{
				// sauf celle sur laquelle on clique
				getTrack(k).setVisible(true);
				ts.labelAudio[k].desactivate();
				ts.checkVisible[k].check(true);
			} else
			{
				getTrack(k).setVisible(false);
				ts.labelAudio[k].desactivate();
				ts.checkVisible[k].check(false);
			}
		}
		selectTrack(numero);
		holoEditRef.room.display();
	}
	
	public void viewSolo2(int numero)
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
			if (k == numero)
				getTrack(k).setVisible(true);
			else
				getTrack(k).setVisible(false);
		setActiveTrack(numero);
	}

	public void viewTrack(int numero, boolean visible)
	{
		if(visible)
			selectTrack(numero);
		else {
			if(numero == activeTrack)
				activeTrack = -1;
			ts.labelAudio[numero].desactivate();
			tracks.get(numero).setVisible(false);
			ts.checkVisible[numero].check(false);
		}
	}
	
	public void viewAll()
	{
		for (int k = 0, last = tracks.size() ; k < last; k++)
		{
			tracks.get(k).setVisible(true);
			ts.labelAudio[k].desactivate();
			ts.checkVisible[k].check(true);
		}
		holoEditRef.room.display();
	}
	
	public void viewAll2()
	{
		for (int k = 0, last = tracks.size() ; k < last; k++)
			tracks.get(k).setVisible(true);
		setActiveTrack(0);
	}


	public void viewNone()
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{
			getTrack(k).setVisible(false);
			ts.labelAudio[k].desactivate();
			ts.checkVisible[k].check(false);
		}
		setActiveTrack(-1);
		holoEditRef.room.display();
		holoEditRef.connection.sendVisible();
	}
	
	
	public void setRecord(int tknum, boolean rec)
	{
		getTrack(tknum).setRecEnable(rec);
		ts.checkRecord[tknum].check(rec);
		holoEditRef.connection.sendRecord(tknum);
	}
	
	public void recordAllButOne(int numero)
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{
			// si la piste n'est pas celle cliquee on la rend visible et cochee
			if (k != numero)
			{
				setRecord(k,true);
				viewTrack(k, true);
			}
			else
			{
				setRecord(k,false);
			}
		}

	}

	public void recordSolo(int numero)
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{ // On desactive toutes les pistes, on les decoche et rend invisible
			if (k == numero)
			{
				// sauf celle sur laquelle on clique
				setRecord(k,true);
				viewTrack(k, true);

			} else
			{
				setRecord(k,false);

			}
		}

	}

	
	public void recordTrack(int numero, boolean rec)
	{
		if(numero < tracks.size());
		{
			setRecord(numero,rec);
			if(rec)
				viewTrack(numero, rec);
				
		}
	}
	
	public void recordAll()
	{
		for (int k = 0, last = tracks.size() ; k < last; k++)
		{
			setRecord(k,true);
			viewTrack(k, true);
		}
	}
	
	public void recordNone()
	{
		for (int k = 0, last = getNbTracks(); k < last; k++)
		{
			setRecord(k,false);

		}

	}

	public void changeName(int tkNum)
	{
		String s = JOptionPane.showInputDialog(null, "Name this track : ", "Track Name", JOptionPane.QUESTION_MESSAGE);
		if (s != null)
		{
			ts.labelAudio[tkNum].setLabelName(s);
			getTrack(tkNum).setName(s);
			holoEditRef.modify();
		}
	}

	public void changeNumber(int tkNum)
	{
		String n = JOptionPane.showInputDialog(null, "Track number : ", "Track number : ", JOptionPane.QUESTION_MESSAGE);
		if (n != null)
		{
			try
			{
				int nn = Integer.parseInt(n);
				getTrack(tkNum).setNumber(nn);
				holoEditRef.modify();
			}
			catch (NumberFormatException e1)
			{
				JOptionPane.showMessageDialog(null, "This isn't a number.", "NumberFormatException", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void changeNumbers()
	{
		String n = JOptionPane.showInputDialog(null, "Change all tracks numbers starting at :", "Renumber Tracks ", JOptionPane.QUESTION_MESSAGE);
		if (n != null)
		{
			try
			{
				int nn = Integer.parseInt(n);
				for(HoloTrack tk2:tracks)
				{
					tk2.setNumber(nn);
					nn++;
				}
				holoEditRef.modify();
			}
			catch (NumberFormatException e1)
			{
				JOptionPane.showMessageDialog(null, "This isn't a number.", "NumberFormatException", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void changeColor(int tkNum)
	{
		ts.labelAudio[tkNum].Disable();
		Color tmp = JColorChooser.showDialog(null, "Choose a color for track \"" + getTrack(tkNum).getName() + "\"", getTrack(tkNum).getColor());
		if (tmp != null)
		{
			ts.labelAudio[tkNum].color = tmp;
			getTrack(tkNum).setColor(tmp);
			ts.labelAudio[tkNum].delight();
			holoEditRef.room.display();
			holoEditRef.modify();
		}
	}

	public void changeLock(int tkNum)
	{
		if(tkNum == -1) return;
		boolean lockState = !getTrack(tkNum).isLocked();
		ts.labelAudio[tkNum].Disable();
		ts.labelAudio[tkNum].setOpaque(lockState);
		getTrack(tkNum).setLocked(lockState);
		ts.labelAudio[tkNum].delight();
		holoEditRef.room.display();
		Ut.barMenu.update();
	}
	
	public int addTrack()
	{
		int newTkNum = -1;
		for (HoloTrack h : tracks)
			newTkNum = Ut.max(newTkNum, h.getNumber());
		tracks.add(new HoloTrack(++newTkNum, GestionPistes.primary[Ut.mod(newTkNum, GestionPistes.primary.length)], true));
		System.out.println(tracks.lastElement().getName()+" "+tracks.lastElement().getNumber());
		updateTrackSelector(-1);
		holoEditRef.modify();
		
		return tracks.size() - 1;
	}

	public void deleteTrack(int tkNum)
	{
		if(tracks.size() > 1)
		{
			int result = JOptionPane.showConfirmDialog(null, "Do you really want to delete the track \"" + getTrack(tkNum).getName() + "\" ?\nNote that this action can't be repaired by calling \"undo\".", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == 0)
			{
				tracks.remove(tkNum);
				updateTrackSelector(-2);
				holoEditRef.modify();
			}
		} else {
			Ut.alert("Operation not permitted","At least one track is needed.\nPlease init this track if you want to delete it's content.");
		}
	}

	public void duplicateTrack(int tkNum)
	{
		int newTkNum = -1;
		for (HoloTrack h : tracks)
			newTkNum = Ut.max(newTkNum, h.getNumber());
		tk = getTrack(tkNum).dupliquer();
		tk.setNumber(++newTkNum);
		tk.setColor(tk.getColor().darker());
		tk.setName(getTrack(tkNum).getName()+"*");
		tracks.insertElementAt(tk,tkNum+1);
		updateTrackSelector(tkNum+1);
		holoEditRef.modify();
	}

	public void prevTrack()
	{
		if(activeTrack > 0)
			selectTrack(activeTrack-1);
		else
			selectTrack(getNbTracks()-1);
	}
	
	public void nextTrack()
	{
		if(activeTrack < getNbTracks() - 1)
			selectTrack(activeTrack+1);
		else
			selectTrack(0);
	}
	
	public void soloPrevTrack()
	{
		if(activeTrack > 0)
			viewSolo(activeTrack-1);
		else
			viewSolo(getNbTracks()-1);
	}
	
	public void soloNextTrack()
	{
		if(activeTrack < getNbTracks() - 1)
			viewSolo(activeTrack+1);
		else
			viewSolo(0);
	}

	// ----------------- SPEAKERS -------------------
	
	private void initSpeakers()
	{
		speakers = new Vector<HoloSpeaker>();
		// initialisation des HPs (valeurs par defaut, modifiees par la lecture du fichier de config s'il existe)
		double dist = 40;
		int nHP = 8;
		double dir, dir0 = 112.5;
		float height = 0;
		int sens = -1;
		// Creation des HPs en fonction de ces parametres
		for (int i = 1; i <= nHP; i++)
		{
			dir = ((dir0 + (float) (i - 1) * sens * (360. / nHP)) % 360.) * Math.PI / 180.;
			speakers.add(new HoloSpeaker((float) dir, (float) dist, true, i, height));
		}
	}
	
	public void addSpeaker()
	{
		speakers.add(new HoloSpeaker(speakers.size()+1));
		new SpeakerEditor(speakers.size()-1, new Point(200, 400), this);
		holoEditRef.modify();
	}

	// initialisation des Hps a partir d'un fichier`
	public void initSpeakers(HoloSpeaker[] nHPIn)
	{
		if(nHPIn != null)
		{
			speakers = new Vector<HoloSpeaker>(nHPIn.length);
			for (int i = 0; i < nHPIn.length; i++)
				speakers.add(new HoloSpeaker(nHPIn[i].X, nHPIn[i].Y, nHPIn[i].Z, i + 1,-1));
		}
	}
}
