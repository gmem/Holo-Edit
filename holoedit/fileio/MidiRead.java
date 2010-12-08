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
/**
 * Classe permettant la lecture des fichiers midi7 et midi14 bits
 */
package holoedit.fileio;

import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class MidiRead implements Runnable
{
	// variables locales pour la lecture midi
	String ChunkName;
	boolean runningStatus, printFlag = false;
	short ndata, ntracks, format, value, bitsParBeat = 480;
	short eventType, runningType = 0, eventChannel, runningChannel = 1;
	// long tempo = 4800 ;
	long tempo = 60;
	double curTime, lastTime;
	int ThchunkLength, TrchunkLength;
	String Nomfichier = new String();
	boolean flag = true;
	// Pour l'affichage d'une barre de progression pendant l'ouverture du fichier
	private int compteur = 0;
	private ProgressBar barreProgression;
	private Thread runner;
	// references sur les autres objets de l'application
	private GestionPistes gestionPistes;
	/** * HPs ** */
	public HoloSpeaker spks[];
	MidiTrak[] tks;
	MidiPoint[] curMidiPt;
	String[] names;

	// Initialisation
	public MidiRead(GestionPistes gp)
	{
		barreProgression = new ProgressBar("Loading...");
		barreProgression.open();
		gestionPistes = gp;
	}

	// Recuperation de la longueur du fichier pour barreProg
	public void getFileInfo(String fileName)
	{
		// System.out.println("Getting File Informations...");
		try
		{
			InputStream fluxFichier1 = new FileInputStream(fileName);
			DataInputStream fluxFichier2 = new DataInputStream(fluxFichier1);
			compteur = fluxFichier2.available();
			barreProgression.setMaximum(compteur);
			// Fermeture du fichier
			fluxFichier2.close();
			fluxFichier1.close();
		}
		catch (Exception e)
		{
			if (!e.getClass().getName().equalsIgnoreCase("java.io.FileNotFoundException"))
			{
				System.out.println(e.toString());
			}
		}
	}

	// lecture
	public void readFile(String fichier, boolean f)
	{
		Nomfichier = fichier;
		flag = f;
		gestionPistes.setActiveTrack(-1);
		// lancement du thread (necessaire pour la barre de progression
		runner = new Thread(this);
		runner.start();
	}

	// thread de lecture du fichier
	public void run()
	{
		// if flag = true => 14 bits else 7 bits
		// System.out.println("flag = true => 14 bits else 7 bits : " + flag);
		lastTime = curTime = 0;
		tks = new MidiTrak[Ut.OLD_TRACK_NB + 1];
		curMidiPt = new MidiPoint[Ut.OLD_TRACK_NB + 1];
		names = new String[Ut.OLD_TRACK_NB + 1];
		byte i;
		// recuperation de la longueur du fichier
		getFileInfo(Nomfichier);
		barreProgression.setValue(0);
		// System.out.println("Reading Midi 14 bits File...");
		try
		{
			InputStream fluxFichier1 = new FileInputStream(Nomfichier);
			DataInputStream fluxFichier2 = new DataInputStream(fluxFichier1);
			// creation des point MIDI pour stocker les valeurs des diffrts controleurs lus
			for (i = 1; i <= Ut.OLD_TRACK_NB; i++)
			{
				curMidiPt[i] = new MidiPoint();
			}
			// affectation d'un num de canal MIDI
			for (i = 1; i <= Ut.OLD_TRACK_NB; i++)
			{
				tks[i] = new MidiTrak();
				tks[i].channel = i;
			}
			// ********** Lecture de l'entete de fichier *********************//
			// System.out.println("Reading Header File...");
			byte contenuFichier[] = new byte[4];
			fluxFichier2.read(contenuFichier);
			ChunkName = new String(contenuFichier);
			// System.out.println ("en tete : " + ChunkName);
			if (ChunkName.equals("MThd"))
			{} else
				System.out.println("error, header non detecte");
			// lecture de la taille de l'entete
			ThchunkLength = fluxFichier2.readInt();
			// System.out.println (" ThchunkLength : " + ThchunkLength);
			if (ThchunkLength != 6)
				System.out.println("error, ThchunkLength != 6");
			// format
			format = (short) fluxFichier2.readChar();
			// System.out.println (" format : " + format);
			if (format != 0)
				System.out.println("error, format != 0");
			// lecture du nombre de pistes
			ntracks = (short) fluxFichier2.readChar();
			// System.out.println (" ntracks : " + ntracks);
			// lecture du time div
			bitsParBeat = (short) fluxFichier2.readChar();
			tempo = 120; // valeur initiale si absente du fichier
			// ---------------------------------------------------------------/
			// ------------ Lecture de l'entete de piste ---------------------/
			// ---------------------------------------------------------------/
			fluxFichier2.read(contenuFichier);
			ChunkName = new String(contenuFichier);
			// System.out.println ("en tete : " + ChunkName);
			if (ChunkName.equals("MTrk"))
			{} else
				System.out.println("error, ChunkName non detecte");
			// lecture de la taille de l'entete
			TrchunkLength = fluxFichier2.readInt();
			// System.out.println (" TrchunkLength : " + TrchunkLength);
			ndata = 1;
			barreProgression.setValue(compteur - fluxFichier2.available());
			// System.out.println("Header File Read.");
			// ------------ Lecture des pistes ----------------------------/
			// System.out.println("Reading Tracks...");
			boolean EOF = false;
			if (flag) // 14 bits = true
				for (; (ndata < TrchunkLength) && !EOF;)
				// for (; (ndata < 64) && !EOF ;)
				{
					// **** lecture du temps **************
					curTime = curTime + 100 * getVarLengthQuantity(fluxFichier2);
					// System.out.println ("curent time = " + curTime); //---> ok
					// System.out.println ("time reel = " +(float)(curTime * (60. / tempo) / bitsParBeat)) ;
					// **** lecture d'un event **************
					value = (short) fluxFichier2.readUnsignedByte();
					incNdata();
					// System.out.println ("eventtypebyte = " + value); //---> ok
					switch (value)
					{
					case 0xff:
					{
						EOF = readMetaEvent(fluxFichier2);
						break;
					}
					case 0xF0:
					{
						readSysex(fluxFichier2);
						break;
					}
					case 0xF7:
					{
						System.out.println("glop F7");
						break;
					}
					default:
					{
						runningStatus = (value < 128 ? true : false);
						if (runningStatus)
						{
							eventType = runningType;
							eventChannel = runningChannel;
							readMidiEvent14r(fluxFichier2);
						} else
						{
							eventType = (short) ((value - 128) >> 4);
							eventChannel = (short) ((value % 16) + 1);
							runningType = eventType;
							runningChannel = eventChannel;
							// System.out.println ("------> lecture d'un event, type : " + eventType + " canal : " + eventChannel); //---> ok
							readMidiEvent14(fluxFichier2);
						}
					}
					}
					barreProgression.setValue(compteur - fluxFichier2.available());
				}
			else
				// false = 7 bits
				for (; (ndata < TrchunkLength) && !EOF;)
				// for (; (ndata < 64) && !EOF ;)
				{
					// **** lecture du temps **************
					curTime = curTime + 100 * getVarLengthQuantity(fluxFichier2);
					// System.out.println ("curent time = " + curTime); //---> ok
					// System.out.println ("time reel = " +(float)(curTime * (60. / tempo) / bitsParBeat)) ;
					// **** lecture d'un event **************
					value = (short) fluxFichier2.readUnsignedByte();
					incNdata();
					// System.out.println ("eventtypebyte = " + value); //---> ok
					switch (value)
					{
					case 0xff:
					{
						EOF = readMetaEvent(fluxFichier2);
						break;
					}
					case 0xF0:
					{
						readSysex(fluxFichier2);
						break;
					}
					case 0xF7:
					{
						System.out.println("glop F7");
						break;
					}
					default:
					{
						runningStatus = (value < 128 ? true : false);
						if (runningStatus)
						{
							eventType = runningType;
							eventChannel = runningChannel;
							readMidiEvent7r(fluxFichier2);
						} else
						{
							eventType = (short) ((value - 128) >> 4);
							eventChannel = (short) ((value % 16) + 1);
							runningType = eventType;
							runningChannel = eventChannel;
							// System.out.println ("------> lecture d'un event, type : " + eventType + " canal : " + eventChannel); //---> ok
							readMidiEvent7(fluxFichier2);
						}
					}
					}
					barreProgression.setValue(compteur - fluxFichier2.available());
				}
			purgePts(flag);
			barreProgression.setValue(compteur - fluxFichier2.available());
			// Fermeture du fichier
			fluxFichier2.close();
			fluxFichier1.close();
			gestionPistes.initSpeakers(spks);
			gestionPistes.tracks = new Vector<HoloTrack>(Ut.OLD_TRACK_NB);
			gestionPistes.initColors(Ut.OLD_TRACK_NB);
			int date, datemax = 0;
			for (i = 0; i < Ut.OLD_TRACK_NB; i++)
			{
				HoloTraj ht = new HoloTraj(tks[i+1].points, 0);
				HoloTrack htk = new HoloTrack(i, gestionPistes.couleurs[i], true);
				htk.setName(names[i+1]);
				htk.addTraj(ht, 0);
				if (!htk.isEmpty())
				{
					gestionPistes.tracks.add(htk);
					date = htk.lastElement().date;
					datemax = (date > datemax ? date : datemax);
					htk.setVisible(true);
					// lp 06/02
					htk.lastElement().setEditable(true);
					htk.firstElement().setEditable(true);
				}
			}
			gestionPistes.updateTrackSelector(-3);
			gestionPistes.holoEditRef.counterPanel.setCompteur(0, datemax);
			gestionPistes.holoEditRef.counterPanel.setCompteur(3, datemax);
			barreProgression.dispose();
		}
		catch (IOException e)
		{
			// Exception declenchee si un probleme survient pendant l'acces au fichier
			if (e.getClass().getName().equalsIgnoreCase("java.io.FileNotFoundException"))
			{
				System.out.println("File " + Nomfichier + " not found !");
				barreProgression.dispose();
			} else
			{
				System.out.println("catch1 :  " + e);
			}
		}
		// System.out.println("The file is read...");
	}

	// ----------------------------------------------------------------------------------------------------//
	// -------------------------------------- running -----------------------------------------------------//
	private void readMidiEvent14r(DataInputStream fluxFichier2)
	{
		byte byt1, byt2;
		try
		{
			// System.out.println ("warning !! , not yet , event lu (mode running), type : " + eventType + ", val : " + value);
			switch (eventType)
			{
			case 0:
			{
				fluxFichier2.read(); // noteoff
				incNdata();
				break;
			}
			case 1:
			{
				fluxFichier2.read(); // noteon
				incNdata();
				break;
			}
			case 2:
			{
				break;
			} // polyp
			case 4:
			{
				break;
			} // prgchg
			case 5:
			{
				break;
			} // chp
			case 6:
			{
				fluxFichier2.read(); // pbd
				incNdata();
				break;
			}
			case 3:
			{ // controler
				// System.out.println ("controleur detecte : num : " + fluxFichier2.read() + " valeur : " + fluxFichier2.read());
				byt1 = (byte) value;
				byt2 = (byte) fluxFichier2.read();
				incNdata();
				addData(curTime, byt1, byt2, (byte) eventChannel);
				break;
			}
			}
		}
		catch (IOException e)
		{
			System.out.println("catch2 :  " + e);
			;
		}
	}

	// ----------------------------------------------------------------------------------------------------//
	// -------------------------------------- 14 not running -------------------------------------------------//
	private void readMidiEvent14(DataInputStream fluxFichier2)
	{
		byte byt1, byt2;
		try
		{
			// System.out.println ("event lu (mode normal), type : " + eventType);
			switch (eventType)
			{
			case 0:
			{
				fluxFichier2.readChar(); // noteoff
				incNdata();
				incNdata();
				break;
			}
			case 1:
			{
				fluxFichier2.readChar(); // noteon
				incNdata();
				incNdata();
				break;
			}
			case 2:
			{
				fluxFichier2.read(); // polyp
				incNdata();
				break;
			}
			case 4:
			{
				fluxFichier2.read(); // prgchg
				incNdata();
				break;
			}
			case 5:
			{
				fluxFichier2.read(); // chp
				incNdata();
				break;
			}
			case 6:
			{
				fluxFichier2.readChar(); // pbd
				incNdata();
				incNdata();
				break;
			}
			case 3:
			{ // controler
				// System.out.println ("controleur detecte : num : " + fluxFichier2.read() + " valeur : " + fluxFichier2.read());
				byt1 = (byte) fluxFichier2.read();
				incNdata();
				byt2 = (byte) fluxFichier2.read();
				incNdata();
				// System.out.println ("controleur detecte2 : num : " + byt1 + " valeur : " + byt2);
				addData(curTime, byt1, byt2, (byte) eventChannel);
				break;
			}
			}
		}
		catch (IOException e)
		{
			System.out.println("catch3 :  " + e);
			;
		}
		// System.out.println ("-- fin event lu (mode normal), type : " + eventType);
	}

	// -------------------------------------- 7 running -----------------------------------------------------//
	private void readMidiEvent7r(DataInputStream fluxFichier2)
	{
		byte byt1, byt2;
		try
		{
			// System.out.println ("warning !! , not yet , event lu (mode running), type : " + eventType + ", val : " + value);
			switch (eventType)
			{
			case 0:
			{
				fluxFichier2.read(); // noteoff
				incNdata();
				break;
			}
			case 1:
			{
				fluxFichier2.read(); // noteon
				incNdata();
				break;
			}
			case 2:
			{
				break;
			} // polyp
			case 4:
			{
				break;
			} // prgchg
			case 5:
			{
				break;
			} // chp
			case 6:
			{
				fluxFichier2.read(); // pbd
				incNdata();
				break;
			}
			case 3:
			{ // controler
				// System.out.println ("controleur detecte : num : " + fluxFichier2.read() + " valeur : " + fluxFichier2.read());
				byt1 = (byte) value;
				byt2 = (byte) fluxFichier2.read();
				incNdata();
				addData2(curTime, byt1, byt2, (byte) eventChannel);
				break;
			}
			}
		}
		catch (IOException e)
		{
			System.out.println("catch2 :  " + e);
			;
		}
	}

	// ----------------------------------------------------------------------------------------------------//
	// -------------------------------------- 7 not running -------------------------------------------------//
	private void readMidiEvent7(DataInputStream fluxFichier2)
	{
		byte byt1, byt2;
		try
		{
			// System.out.println ("event lu (mode normal), type : " + eventType);
			switch (eventType)
			{
			case 0:
			{
				fluxFichier2.readChar(); // noteoff
				incNdata();
				incNdata();
				break;
			}
			case 1:
			{
				fluxFichier2.readChar(); // noteon
				incNdata();
				incNdata();
				break;
			}
			case 2:
			{
				fluxFichier2.read(); // polyp
				incNdata();
				break;
			}
			case 4:
			{
				fluxFichier2.read(); // prgchg
				incNdata();
				break;
			}
			case 5:
			{
				fluxFichier2.read(); // chp
				incNdata();
				break;
			}
			case 6:
			{
				fluxFichier2.readChar(); // pbd
				incNdata();
				incNdata();
				break;
			}
			case 3:
			{ // controler
				// System.out.println ("controleur detecte : num : " + fluxFichier2.read() + " valeur : " + fluxFichier2.read());
				byt1 = (byte) fluxFichier2.read();
				incNdata();
				byt2 = (byte) fluxFichier2.read();
				incNdata();
				addData2(curTime, byt1, byt2, (byte) eventChannel);
				break;
			}
			}
		}
		catch (IOException e)
		{
			System.out.println("catch3 :  " + e);
			;
		}
		// System.out.println ("-- fin event lu (mode normal), type : " + eventType);
	}

	// ---------------------------------------------------------------------------------------------------//
	// ---------------------------------------------------------------------------------------------------//
	private boolean readMetaEvent(DataInputStream fluxFichier2)
	{
		short metaType = 0;
		boolean EOF = false;
		try
		{
			metaType = (short) fluxFichier2.readUnsignedByte();
			incNdata();
			// System.out.println("-------------------> metatype : " + metaType);
			switch (metaType)
			{
			case 0x51:
			{
				readTempo(fluxFichier2);
				break;
			}
			case 0x7F:
			{ // HPs, HPz and Labels
				readHPs(fluxFichier2);
				break;
			}
			// ANCIEN NON COMMERCIALISE ajout leo pour lire la hauteur Z des HPs
			// FICHIERS ILLISIBLES PAR MAX A REENREGISTER
			case 0x7E:
			{
				readHPz(fluxFichier2);
				break;
			}
			// ANCIEN NON COMMERCIALISE ajout leo pour lire les noms des pistes
			// FICHIERS ILLISIBLES PAR MAX A REENREGISTER
			case 0x7D:
			{
				readLabels(fluxFichier2);
				break;
			}
			// */
			case 47:
			{
				// System.out.println ("-------- Fin de fichier MIDI --------");
				EOF = true;
				break;
			}
			default:
			{
				readGarbageMetaEvent(fluxFichier2);
			}
			}
		}
		catch (IOException e)
		{
			// Exception declenchee si un probleme survient pendant l'acces au fichier
			System.out.println("catch4 :  " + e);
			;
		}
		// System.out.println ("EOF : " + EOF);
		return EOF;
	}

	private void readSysex(DataInputStream fluxFichier2)
	{
		byte i;
		try
		{
			for (i = (byte) fluxFichier2.readUnsignedByte(); i == 0xf7; i = (byte) fluxFichier2.readUnsignedByte())
			{
				incNdata();
			}
		}
		catch (IOException e)
		{
			System.out.println("catch5 :  " + e);
			;
		}
		System.out.println("Sysex detecte");
	}

	// ------- methode generale, pour les meta a sauter --------/
	private void readGarbageMetaEvent(DataInputStream fluxFichier2)
	{
		byte i, n;
		try
		{
			n = (byte) fluxFichier2.readUnsignedByte();
			incNdata();
			for (i = 0; i < n; i++)
			{
				fluxFichier2.read();
				incNdata();
			}
		}
		catch (IOException e)
		{
			System.out.println("catch6 :  " + e);
			;
		}
	}

	private void readTempo(DataInputStream fluxFichier2)
	{
		byte n;
		long pretempo = 0;
		try
		{
			fluxFichier2.readByte();
			n = 3;
			pretempo = nBytesToNum(fluxFichier2, n);
			tempo = 60000000 / pretempo;
			// System.out.println ("tempo "+tempo);
		}
		catch (IOException e)
		{
			System.out.println("catch7 :  " + e);
			;
		}
	}

	private void readHPs(DataInputStream fluxFichier2)
	{
		byte i, n, n2;
		// int numHP;
		float X, Y, Z = 0;
		try
		{
			n = (byte) (fluxFichier2.readUnsignedByte());
			n2 = (byte) (fluxFichier2.readUnsignedByte());
			incNdata();
			if (n2 == (byte) 200)
			{
				readHPz(fluxFichier2, n);
			} else if (n2 == (byte) 180)
			{
				readLabels2(fluxFichier2, n);
			} else
			{
				n = (byte) (n / 3);
				spks = new HoloSpeaker[n];
				for (i = 0; i < n; i++)
				{
					// numHP =
					fluxFichier2.read();
					incNdata();
					X = (float) calcFromByte(fluxFichier2.readByte());
					incNdata();
					Y = (float) calcFromByte(fluxFichier2.readByte());
					Z = 0;
					spks[i] = new HoloSpeaker(X, -Y, Z, i + 1,-1);
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("catch7 :  " + e);
		}
	}

	// ANCIENNES METHODE NON COMMERCIALISE
	// CAR ILLISIBLE PAR MAX
	// MAIS CONSERVEE POUR COMPATIBILITE
	// ajout leo pour lire la hauteur Z des HPs
	private void readHPz(DataInputStream fluxFichier2, byte n)
	{
		byte i;
		// int numHP;
		float X, Y, Z = 0;
		try
		{
			n = (byte) (n / 4);
			incNdata();
			spks = new HoloSpeaker[n];
			for (i = 0; i < n; i++)
			{
				// numHP =
				fluxFichier2.read();
				incNdata();
				X = (float) calcFromByte(fluxFichier2.readByte());
				incNdata();
				Y = (float) calcFromByte(fluxFichier2.readByte());
				incNdata();
				Z = (float) calcFromByte(fluxFichier2.readByte());
				spks[i] = new HoloSpeaker(X, -Y, Z, i + 1,-1);
			}
		}
		catch (IOException e)
		{
			System.out.println("catch7z :  " + e);
		}
	}

	private void readHPz(DataInputStream fluxFichier2)
	{
		byte i, n;
		// int numHP;
		float X, Y, Z = 0;
		try
		{
			n = (byte) (fluxFichier2.readUnsignedByte() / 4);
			incNdata();
			spks = new HoloSpeaker[n];
			for (i = 0; i < n; i++)
			{
				// numHP =
				fluxFichier2.read();
				incNdata();
				X = (float) calcFromByte(fluxFichier2.readByte());
				incNdata();
				Y = (float) calcFromByte(fluxFichier2.readByte());
				incNdata();
				Z = (float) calcFromByte(fluxFichier2.readByte());
				spks[i] = new HoloSpeaker(X, -Y, Z, i + 1,-1);
			}
		}
		catch (IOException e)
		{
			System.out.println("catch7z :  " + e);
		}
	}

	// ANCIENNES METHODE NON COMMERCIALISE
	// CAR ILLISIBLE PAR MAX
	// MAIS CONSERVEE POUR COMPATIBILITE
	// ajout leo pour lire les noms des pistes
	private void readLabels(DataInputStream fluxFichier2)
	{
		byte i = 0;
		int length;
		try
		{
			for (i = 1; i <= Ut.OLD_TRACK_NB; i++)
			{
				// conversion du flux de byte en string et affectation
				length = fluxFichier2.readUnsignedByte();
				incNdata();
				byte[] b = new byte[length];
				fluxFichier2.read(b, 0, length);
				names[i] = new String(b);
				ndata = (short) (ndata + length);
			}
		}
		catch (IOException e)
		{
			System.out.println("catch7c :  " + e);
		}
	}

	private void readLabels2(DataInputStream fluxFichier2, byte n)
	{
		// byte i = 0;
		int length, numPiste;
		try
		{
			length = n - 2;
			// conversion du flux de byte en string et affectation
			numPiste = fluxFichier2.readUnsignedByte();
			incNdata();
			byte[] b = new byte[length];
			fluxFichier2.read(b, 0, length);
			names[numPiste] = new String(b);
			ndata = (short) (ndata + length);
		}
		catch (IOException e)
		{
			System.out.println("catch7c :  " + e);
		}
	}

	// --------- teste le nombre de bytes a lire ------------/
	private short getVarLengthQuantity(DataInputStream fluxFichier2)
	{
		short sum = 0, nval = 0;
		try
		{
			for (nval = (short) fluxFichier2.readUnsignedByte(), incNdata(); nval > 127;)
			{
				sum = (short) ((sum << 7) + (nval > 127 ? nval - 128 : nval));
				nval = (short) fluxFichier2.readUnsignedByte();
				incNdata();
			}
			sum = (short) ((sum << 7) + nval);
		}
		catch (IOException e)
		{
			System.out.println("catch8 :  " + e);
			;
		}
		return sum;
	}

	// --------- teste le nombre de bytes a lire ------------/
	private long nBytesToNum(DataInputStream fluxFichier2, byte n)
	{
		long sum = 0, i;
		try
		{
			for (i = 0; i < n; i++)
			{
				sum = fluxFichier2.readUnsignedByte() + (sum << 8);
				incNdata();
			}
		}
		catch (IOException e)
		{
			System.out.println("catch9 :  " + e);
			;
		}
		return sum;
	}

	private void incNdata()
	{
		if (printFlag)
			System.out.println("n = " + ndata++);
		else
			ndata++;
	}

	// -----------------------------------------------------------------------/
	// -----------------------------------------------------------------------/
	private void purgePts(boolean purge)
	{
		if (purge)
			for (int i = 1; i <= Ut.OLD_TRACK_NB ; i++)
			{
				if (curMidiPt[i].isReady())
				{
					tks[i].ajouterPoint(curMidiPt[i]);
					curMidiPt[i] = new MidiPoint();
				}
			}
		else
			for (int i = 1; i <= Ut.OLD_TRACK_NB ; i++)
			{
				/*
				 * if(CurMidiPt[i].isReady()) {//
				 */
				tks[i].ajouterPoint2(curMidiPt[i]);
				curMidiPt[i] = new MidiPoint();
				/* }// */
			}
	}

	private void addData(double time, byte byt1, byte byt2, byte channel)
	{
		// System.out.println ("adddata " + time + " tempo : " + tempo);
		// System.out.println ("time reel = " +(float)(time * (600. / tempo) / bitsParBeat)) ;
		// System.out.println (" final tempo : " + tempo);
		// System.out.println(time+" "+byt1+" "+byt2+" "+channel);
		if (tempo == 0)
			tempo = 60;
		// if
		if (((curMidiPt[channel].nvals >= 5) && (curMidiPt[channel].isReady())) || ((curTime != lastTime) && (curMidiPt[channel].nvals >= 4) && (curMidiPt[channel].isReady())))
		{
			purgePts(true);
			lastTime = curTime;
		}
		if (curMidiPt[channel].empty)
		{
			curMidiPt[channel].date = (float) (time * (60. / tempo) / bitsParBeat);
			curMidiPt[channel].empty = false;
		}
		switch (byt1)
		{
		case 44:
		{
			// System.out.println("44 : "+byt2);
			curMidiPt[channel].edit = true;
			curMidiPt[channel].Ylsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 43:
		{
			// System.out.println("43 : "+byt2);
			curMidiPt[channel].Ylsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 42:
		{
			// System.out.println("42 : "+byt2);
			curMidiPt[channel].Xlsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 22:
		{
			// System.out.println("22 : "+byt2);
			curMidiPt[channel].Z = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 21:
		{
			// System.out.println("21 : "+byt2);
			curMidiPt[channel].Ymsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 20:
		{
			// System.out.println("20 : "+byt2);
			curMidiPt[channel].Xmsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		}
	}

	private void addData2(double time, byte byt1, byte byt2, byte channel)
	{
		// System.out.println ("adddata " + time);
		// System.out.println ("adddata " + time + " tempo : " + tempo);
		// System.out.println ("time reel = " +(float)(time * (600. / tempo) / bitsParBeat)) ;
		if (tempo == 0)
			tempo = 60;
		// if((curTime != lastTime) && (CurMidiPt[channel].nvals >= 4) && (CurMidiPt[channel].isReady()))
		if (curTime != lastTime)
		{
			purgePts(false);
			// System.out.println ("curTime " + curTime);
			lastTime = curTime;
		}
		if (curMidiPt[channel].empty)
		{
			curMidiPt[channel].date = (float) (time * (60. / tempo) / bitsParBeat);
			curMidiPt[channel].empty = false;
		}
		switch (byt1)
		{
		case 22:
		{
			// System.out.println("22 : "+byt2);
			curMidiPt[channel].Z = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 26:
		{
			// System.out.println("26 : "+byt2);
			curMidiPt[channel].edit = true;
			curMidiPt[channel].Xmsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 27:
		{
			// System.out.println("27 : "+byt2);
			curMidiPt[channel].Ymsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 28:
		{
			// System.out.println("28 : "+byt2);
			curMidiPt[channel].Xmsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		case 29:
		{
			// System.out.println("29 : "+byt2);
			curMidiPt[channel].Ymsb = byt2;
			curMidiPt[channel].nvals++;
			break;
		}
		}
	}

	// ----------------------------------------------------------------------------/
	private double calcFromByte(byte by)
	{
		double result;
		if (by >= 0)
		{
			result = by / 8.;
			result = result * result;
		} else
		{
			result = by / 8.;
			result = result * -result;
		}
		// System.out.println("value (byte)result result: " +value+" "+result + " "+(byte)result);
		return result;
	}
}
