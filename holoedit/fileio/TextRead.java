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
package holoedit.fileio;

import holoedit.data.HoloPoint;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;

//-----------------------------------------------------------------------------------------------------------------------------
public class TextRead implements Runnable
{
	// private TextReadFrame textReadFrame ;
	private GestionPistes gp;
	private HoloTrack piste;
	private HoloSpeaker[] hp;
	private int numPiste; // numero de la piste traite
	private int numPoint; // numero du point traite
	private int nbPoints; // nombre de points a traiter dans la piste
	private int nbHP; // nombre de haut parleur
	private boolean attenteNumPiste = false;// indique si la ligne contient un numero de piste
	private boolean attenteNbPoints = false;// indique si la ligne contient un nombre de point
	private boolean attenteNomPiste = false;// indique si le fichier text contient le nom des pistes ou pas
	private boolean attentePoint = false;// indique si il y a encore des points a traiter
	private boolean flagWithZ = false;// indique si le fichier text contient des Z ou non
	private boolean zHP = false;// indique si le fichier text contient des Z ou non pour les HPs
	private boolean attenteNbHP = false;// indique si la ligne doit contenir le nb de haut parleur
	private boolean attenteHP = false;// indique si la ligne doit contenir un haut parleur
	private String fichier;
	private int dateMax;
	private String contenu;
	private String contenu2;
	private int numPisteLecture = -1; // numero de la piste a lire si une
	// seule piste a charger
	private int numPisteEcriture = -1; // numero de la piste a ecrire si une
	// seule piste a charger
	private boolean readHP = true;
	private int nbPistes = Ut.INIT_TRACK_NB;
	private int nbReadableTracks = 0; // nombre de pistes non-vides
	private Vector<Integer> pisteReadable = new Vector<Integer>(); // vecteur de pistes
	// non-vides
	Vector<Integer> nbPtsPerPiste = new Vector<Integer>(); // tableau du nombre de pts par piste non-vide
	// de pts par piste
	// non-vide
	// private int nbPointsTotal ; // nb de points total sur l'ensemble des pistes
	private int compteur = 1; // num du point en cours de traitement sur l'ensemble des pistes
	private ProgressBar barreProgression;
	private Thread runner;

	// -----------------------------------------------------------------------------------------------------------------------------
	// constructeur par defaut
	public TextRead(GestionPistes _gp)
	{
		fichier = new String();
		barreProgression = new ProgressBar("Loading...");
		gp = _gp;
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// methode de lecture du fichier
	// renvoie un tableau de piste correspondant au contenu du fichier texte
	// ce tableau est ensuite utilise dans la fonction readTextFile de la classe
	// GestionPistes
	public void readTextFile(String nomFichier)
	{
		int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise all tracks ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0)
		{
			gp.initColors(17);
			gp.holoEditRef.counterPanel.setCompteur(1, 0);
			gp.holoEditRef.counterPanel.setCompteur(2, 0);
			gp.holoEditRef.counterPanel.setCompteur(3, 0);
			gp.tracks = new Vector<HoloTrack>();
			nbPtsPerPiste = new Vector<Integer>();
			for (int i = 0; i < 17; i++)
			{
				gp.tracks.add(new HoloTrack(i,gp.couleurs[i]));
				nbPtsPerPiste.add(0);
				gp.getTrack(i).init();
				gp.getTrack(i).setVisible(false);
			}
			gp.updateTrackSelector(-3);
			numPisteLecture = -1;
			numPisteEcriture = -1;
			readHP = true;
			fichier = nomFichier;
			barreProgression.open();
			barreProgression.setValue(0);
			gp.setActiveTrack(-1);
			gp.holoEditRef.room.initVars(true);
			runner = new Thread(this);
			runner.start();
		}
	}

	public void readTextFileDirect(String nomFichier)
	{
		gp.initColors(17);
		gp.holoEditRef.counterPanel.setCompteur(1, 0);
		gp.holoEditRef.counterPanel.setCompteur(2, 0);
		gp.holoEditRef.counterPanel.setCompteur(3, 0);
		gp.tracks = new Vector<HoloTrack>();
		nbPtsPerPiste = new Vector<Integer>();
		for (int i = 0; i < 17; i++)
		{
			gp.tracks.add(new HoloTrack(i,gp.couleurs[i]));
			nbPtsPerPiste.add(0);
			gp.getTrack(i).init();
			gp.getTrack(i).setVisible(false);
		}
		gp.updateTrackSelector(-3);
		numPisteLecture = -1;
		numPisteEcriture = -1;
		readHP = true;
		fichier = nomFichier;
		barreProgression.open();
		barreProgression.setValue(0);
		gp.setActiveTrack(-1);
		gp.holoEditRef.room.initVars(true);
		runner = new Thread(this);
		runner.start();
	}

	// one track
	public void readTextFileOneTrack(String nomFichier, int numPin, int numPout)
	{
		if (gp.getNbTracks() <= numPout)
			return;
		HoloTrack out = gp.getTrack(numPout);
		if(!out.isEmpty())
		{
			int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise the track " + numPout + " ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == 0)
			{
				out.init();
				gp.holoEditRef.counterPanel.setCompteur(1, 0);
				gp.holoEditRef.counterPanel.setCompteur(2, 0);
				gp.holoEditRef.counterPanel.setCompteur(3, 0);
				numPisteLecture = numPin;
				numPisteEcriture = numPout;
				fichier = nomFichier;
				readHP = false;
				barreProgression.open();
				barreProgression.setValue(0);
				gp.setActiveTrack(-1);
				gp.holoEditRef.room.initVars(true);
				runner = new Thread(this);
				runner.start();
			}
		} else {
			out.init();
			gp.holoEditRef.counterPanel.setCompteur(1, 0);
			gp.holoEditRef.counterPanel.setCompteur(2, 0);
			gp.holoEditRef.counterPanel.setCompteur(3, 0);
			numPisteLecture = numPin;
			numPisteEcriture = numPout;
			fichier = nomFichier;
			readHP = false;
			barreProgression.open();
			barreProgression.setValue(0);
			gp.setActiveTrack(-1);
			gp.holoEditRef.room.initVars(true);
			runner = new Thread(this);
			runner.start();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// thread de lecture
	public void run()
	{
		this.chargement();
		stopThread();
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	private void chargement()
	{
		String nomFichier = new String(fichier);
		contenu = new String();
		try
		{
			// creation du flux d'entree
			FileReader textFile = new FileReader(nomFichier);
			BufferedReader inBuff = new BufferedReader(textFile);
			if (numPisteLecture == -1)
			{
				barreProgression.setMaximum(calculNbPointsTotal(inBuff));
			} else if (numPisteLecture == 0)
			{
				barreProgression.setMaximum(calculNbPointsSel(inBuff));
			} else
			{
				barreProgression.setMaximum(getNbPtsForTrack(numPisteLecture));
			}
			inBuff.close();
			textFile = new FileReader(nomFichier);
			inBuff = new BufferedReader(textFile);
			boolean lireLigne = true;
			boolean finFichier = false;
			String ligne = new String();
			// System.out.println ( "test vers piste n"+Salle.numCar+" :
			// "+numPisteEcriture) ;
			while (!finFichier)
			{
				if (lireLigne)
				{
					ligne = inBuff.readLine();
					// System.out.println ( "ligne : "+ligne) ;
				} else
					lireLigne = true;
				if (ligne == null)
					finFichier = true;
				else
				{
					if (attentePoint)
					{
						while (attentePoint)
						{
							HoloPoint holoPoint = lirePoint(ligne);
							piste.addElement(holoPoint);
							ligne = inBuff.readLine();
						}
						lireLigne = false; // FIN DE LECTURE
						if (numPisteEcriture > 0)
						{// ecriture d'une piste unique
							numPiste = numPisteEcriture;
							gp.tracks.setElementAt(piste,numPiste);// sauvegarde de la piste
							if (piste.getLastDate() > dateMax)
								dateMax = piste.getLastDate();
							gp.ts.checkVisible[numPiste].check(true); // sortie
							gp.getTrack(numPiste).setVisible(true); // sortie
						} else if (numPisteEcriture == 0)
						{
							if (gp.getTrack(numPiste).isVisible())
							{
								gp.tracks.setElementAt(piste,numPiste);// sauvegarde de la piste
								if (piste.getLastDate() > dateMax)
									dateMax = piste.getLastDate();
								gp.ts.checkVisible[numPiste].check(true);
								gp.getTrack(numPiste).setVisible(true);
							}
						} else
						{
							gp.tracks.setElementAt(piste,numPiste);// sauvegarde de la piste
							if (piste.getLastDate() > dateMax)
								dateMax = piste.getLastDate();
							gp.ts.checkVisible[numPiste].check(true); // sortie
							gp.getTrack(numPiste).setVisible(true); // sortie
						}
					} else
					{
						traiterLigne(ligne);
					}
				}
			}
			gp.holoEditRef.counterPanel.setCompteur(2, Math.max(gp.holoEditRef.counterPanel.getDate(2), dateMax));
			gp.holoEditRef.counterPanel.setCompteur(3, Math.max(gp.holoEditRef.counterPanel.getDate(3), dateMax));
			// fermeture du flux d'entree
			inBuff.close();
			barreProgression.dispose();
			if (numPisteEcriture == -1)
			{
				boolean found = false;
				for (int k = 0; k < gp.getNbTracks(); k++)
				{
					if (!gp.getTrack(k).isEmpty())
					{
						if (!found)
						{
							found = true;
							gp.ts.labelAudio[k].activate();
							gp.setActiveTrack(k);
						}
						gp.getTrack(k).setVisible(true);
						gp.ts.checkVisible[k].check(true);
					} else
					{
						gp.getTrack(k).setVisible(false);
						gp.ts.checkVisible[k].check(false);
						gp.ts.labelAudio[k].desactivate();
					}
				}
			} else if (numPisteEcriture != 0)
			{
				gp.ts.labelAudio[numPisteEcriture].activate();
				gp.setActiveTrack(numPisteEcriture);
			} else
			{
				boolean found = false;
				for (int k = 0; k < gp.getNbTracks(); k++)
				{
					if (gp.getTrack(k).isVisible())
					{
						if (!found)
						{
							found = true;
							gp.ts.labelAudio[k].activate();
							gp.setActiveTrack(k);
						}
						gp.getTrack(k).setVisible(true);
						gp.ts.checkVisible[k].check(true);
					} else
					{
						gp.getTrack(k).setVisible(false);
						gp.ts.checkVisible[k].check(false);
						gp.ts.labelAudio[k].desactivate();
					}
				}
			}
			gp.holoEditRef.room.display();
		}
		catch (IOException ioe)
		{
			if (ioe.getClass().getName().equalsIgnoreCase("java.io.FileNotFoundException"))
			{
				System.out.println("File " + nomFichier + " not found !");
			} else
			{
				System.out.println("erreur lecture du fichier pendant le chargement : " + ioe.toString());
			}
			barreProgression.dispose();
		}
		catch (SecurityException se)
		{
			System.out.println("erreur securite : " + se.toString());
			barreProgression.dispose();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	private int calculNbPointsTotal(BufferedReader inBuff)
	{
		int cpt = 0;
		boolean lireNbPoints = false;
		boolean finFichier = false;
		try
		{
			while (!finFichier)
			{
				String ligne = inBuff.readLine();
				if (ligne == null)
					finFichier = true;
				else
				{
					if (lireNbPoints)
					{
						int nbpts = (Integer.valueOf(ligne)).intValue();
						cpt += nbpts;
						lireNbPoints = false;
					}
					if (ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?) :"))
					{
						lireNbPoints = true; // le nombre de points de la
						// piste est sur la ligne
						// suivante
						flagWithZ = true;
					} else if (ligne.equalsIgnoreCase("npts num (X, Y, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, time, edit?) :"))
					{
						lireNbPoints = true; // le nombre de points de la
						// piste est sur la ligne
						// suivante
						flagWithZ = false;
					}
				}
			}
		}
		catch (IOException ioe)
		{
			System.out.println("erreur lecture du fichier : " + ioe.toString());
		}
		return cpt;
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	private int calculNbPointsSel(BufferedReader inBuff)
	{
		int cpt = 0;
		boolean lireNbPoints = false;
		boolean permetLireNbPoints = false;
		boolean lireNumPiste = false;
		boolean finFichier = false;
		int nP = 0;
		try
		{
			while (!finFichier)
			{
				String ligne = inBuff.readLine();
				if (ligne == null)
					finFichier = true;
				else
				{
					if (lireNbPoints)
					{
						int nbpts = (Integer.valueOf(ligne)).intValue();
						cpt += nbpts;
						lireNbPoints = false;
					}
					if (lireNumPiste)
					{
						nP = (Integer.valueOf(ligne)).intValue();
						if (gp.getTrack(nP).isVisible())
						{
							permetLireNbPoints = true;
						}
						lireNumPiste = false;
					}
					if (permetLireNbPoints)
					{
						if (ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?) :"))
						{
							lireNbPoints = true; // le nombre de points de la
							// piste est sur la ligne
							// suivante
							flagWithZ = true;
							permetLireNbPoints = false;
						} else if (ligne.equalsIgnoreCase("npts num (X, Y, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, time, edit?) :"))
						{
							lireNbPoints = true; // le nombre de points de la
							// piste est sur la ligne
							// suivante
							flagWithZ = false;
							permetLireNbPoints = false;
						}
					}
					if (ligne.startsWith("piste"))
					{
						lireNumPiste = true;
					}
				}
			}
		}
		catch (IOException ioe)
		{
			System.out.println("erreur lecture du fichier : " + ioe.toString());
		}
		return cpt;
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	private void traiterLigne(String ligne)
	{
		// System.out.println(ligne);
		if (attenteNumPiste)
		{
			// lecture du numero de piste
			// System.out.println("piste destination : "+ numPiste+ " piste
			// lecture : "+(Integer.valueOf ( ligne )).intValue()) ;
			// numPiste = (Integer.valueOf ( ligne )).intValue() ;
			numPiste = (Integer.valueOf(ligne)).intValue()-1;
			contenu = contenu + "Piste N" + Ut.numCar + numPiste;
			attenteNumPiste = false;
			// System.out.println("lecture du numero de piste "+numPiste);
		} else if (attenteNomPiste)
		{
			gp.getTrack(numPiste).setName(ligne);
			gp.ts.labelAudio[numPiste].setLabelName(ligne);
			attenteNomPiste = false;
		} else if (attenteNbPoints)
		{
			// lecture nombre de points de la piste
			nbPoints = (Integer.valueOf(ligne)).intValue();
			contenu = contenu + " : " + nbPoints + " points.\n";
			attenteNbPoints = false;
			// System.out.println("lecture nombre de points de la piste
			// "+nbPoints);
			// System.out.println(nbPoints +" "+ numPisteLecture +" "+ numPiste
			// + " " + numPisteEcriture);
			// System.out.println(ligne);
			if (nbPoints > 0 & (numPisteLecture == -1 | numPiste == numPisteLecture | (numPisteLecture == 0 & gp.getTrack(numPiste).isVisible())))
			{
				// / System.out.println("on lit le fichier et on ecrit dans
				// l'editeur");
				attentePoint = true;
				numPoint = 0;
				piste = new HoloTrack();
				if (numPisteEcriture > 0)
					numPiste = numPisteEcriture;// ecriture d'une piste unique
				piste.setNumber(numPiste);
				piste.setColor(gp.couleurs[numPiste]);
			} else
			{ // la piste ne contient pas de points
				attentePoint = false;
			}
		} else if (attenteNbHP)
		{
			// lecture du nombre de HPs
			// System.out.println("lecture du nombre de HPs");
			nbHP = (Integer.valueOf(ligne)).intValue();
			if (nbHP == 0)
			{
				attenteNbHP = false;
				attenteHP = false;
			} else
			{
				hp = new HoloSpeaker[nbHP];
				attenteNbHP = false;
				attenteHP = true;
			}
		} else if (attenteHP)
		{
			// chargement Hautparleurs
			// System.out.println("chargement HP");
			float positionX, positionY, positionZ;
			int numHP;
			String temp = new String();
			int i = 0;
			// lecture du numero du haut parleur
			while (ligne.charAt(i) != '\t')
			{
				temp = temp + ligne.charAt(i);
				i++;
			}
			numHP = (Integer.valueOf(temp)).intValue();
			// lecture de la position selon l'axe X
			temp = "";
			i++;
			while (ligne.charAt(i) != '\t')
			{
				temp = temp + ligne.charAt(i);
				i++;
			}
			positionX = (Float.valueOf(temp)).floatValue();
			// lecture de la position selon l'axe Y
			temp = "";
			i++;
			while (i < ligne.length() && ligne.charAt(i) != '\t')
			{
				temp = temp + ligne.charAt(i);
				i++;
			}
			positionY = (Float.valueOf(temp)).floatValue();
			if (!zHP)
			{
				hp[numHP - 1] = new HoloSpeaker(positionX, positionY, numHP);
			} else
			{
				// lecture de la position selon l'axe Z
				temp = "";
				i++;
				while (i < ligne.length() && (ligne.charAt(i) != '\n' || ligne.charAt(i) != '\r'))
				{
					temp = temp + ligne.charAt(i);
					i++;
				}
				positionZ = (Float.valueOf(temp)).floatValue();
				hp[numHP - 1] = new HoloSpeaker(positionX, positionY, positionZ, numHP,-1);
				// System.out.println("lire hps w Z "+positionZ+"
				// "+hp[numHP-1]);
			}
			if (numHP == nbHP)
			{
				if (readHP)
					gp.initSpeakers(hp);
				attenteHP = false;
				// System.out.println("fin chargement haut parleur") ;
			}
		}
		// Cette partie permet de connaîtrel'informatinon se trouvant sur la
		// ligne suivant du texte.
		else if (ligne.equalsIgnoreCase("piste:") || ligne.equalsIgnoreCase("piste :") || ligne.equalsIgnoreCase("piste  :"))
		{
			attenteNumPiste = true; // le numero de la piste est sur la ligne
			// suivante
		} else if (ligne.equalsIgnoreCase("name :"))
		{
			attenteNomPiste = true; // nom de la piste
		} else if (ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, Z, time, edit?) :"))
		{
			attenteNbPoints = true; // le nombre de points de la piste est sur
			// la ligne suivante
			flagWithZ = true;
			// System.out.println("lire nbpts w Z");
		} else if (ligne.equalsIgnoreCase("npts num (X, Y, time, edit?):") || ligne.equalsIgnoreCase("npts num (X, Y, time, edit?) :"))
		{
			attenteNbPoints = true; // le nombre de points de la piste est sur
			// la ligne suivante
			flagWithZ = false;
			// System.out.println("lire nbpts wo Z");
		} else if (ligne.equalsIgnoreCase("HP num (n" + Ut.numCar + ", pos X, pos Y):") || ligne.equalsIgnoreCase("HP num (n" + Ut.numCar + ", pos X, pos Y) :") || ligne.equalsIgnoreCase("HP num (n" + Ut.numCar2 + ", pos X, pos Y):")
				|| ligne.equalsIgnoreCase("HP num (n" + Ut.numCar2 + ", pos X, pos Y) :"))
		{
			attenteNbHP = true;
			zHP = false;
			// System.out.println("lire hps wo Z");
		} else if (ligne.equalsIgnoreCase("HP num (n" + Ut.numCar + ", pos X, pos Y, pos Z):") || ligne.equalsIgnoreCase("HP num (n" + Ut.numCar2 + ", pos X, pos Y, pos Z):"))
		{
			attenteNbHP = true;
			zHP = true;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	public void stopThread()
	{
		if (runner != null)
		{
			runner = null;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	private HoloPoint lirePoint(String ligne)
	{
		// lecture d'un point
		float positionX, positionY, positionZ;
		int date;
		boolean editable;
		// mise a jour barre de progression
		compteur++;
		try
		{
			barreProgression.setValue(compteur);
		}
		catch (Exception e)
		{
			System.out.println("erreur barre de progression : " + e.toString());
		}
		int indEsp = ligne.indexOf("\t");
		// lecture de la position selon l'axe X
		positionX = (Float.valueOf(ligne.substring(0, indEsp))).floatValue();
		ligne = ligne.substring(indEsp + 1);
		indEsp = ligne.indexOf("\t");
		// lecture de la position selon l'axe Y
		positionY = (Float.valueOf(ligne.substring(0, indEsp))).floatValue();
		ligne = ligne.substring(indEsp + 1);
		indEsp = ligne.indexOf("\t");
		if (flagWithZ)
		{
			// lecture de la position selon l'axe Z
			positionZ = (Float.valueOf(ligne.substring(0, indEsp))).floatValue();
			// lecture de la date
			ligne = ligne.substring(indEsp + 1);
			indEsp = ligne.indexOf("\t");
			float datefloat = (Float.valueOf(ligne.substring(0, indEsp))).floatValue();
			date = Math.round(1000 * datefloat);
		} else
		{
			positionZ = (float) 0.;
			float datefloat = (Float.valueOf(ligne.substring(0, indEsp))).floatValue();
			date = Math.round(1000 * datefloat);
		}
		// lecture de la possibilite d'edition
		if (ligne.indexOf("ed") != -1)
		{
			editable = true;
		} else
		{
			editable = false;
		}
		// construction de l'holopoint correspondant
		HoloPoint holoPoint = new HoloPoint(limit(positionX), limit(positionY), positionZ, date);
		holoPoint.setEditable(editable);
		// incrementation du compteur de point
		numPoint++;
		if (numPoint >= nbPoints)
		{ // tous les points de la piste ont ete traites
			attentePoint = false;
			nbPoints = 0;
		}
		return (holoPoint);
	}

	public String[] getReadableTracks(String nomFichier)
	{
		int cptPiste = 0; // nombre de pistes lisibles;
		for (int i = 0; i < nbPistes + 1; i++)
		{
			nbPtsPerPiste.setElementAt(0,i);
		}
		String tmpContenu = new String();
		String ligne = new String();
		boolean finFichier = false;
		try
		{
			FileReader textFile = new FileReader(nomFichier);
			BufferedReader inBuff = new BufferedReader(textFile);
			while (!finFichier)
			{
				ligne = inBuff.readLine();
				if (ligne == null)
				{
					finFichier = true;
				} else
				{
					boolean pFound = false;
					while (!pFound && !finFichier)
					{
						if (ligne == null)
						{
							finFichier = true;
						} else if (ligne.startsWith("piste"))
						{
							pFound = true;
						} else
						{
							ligne = inBuff.readLine();
						}
					}
					if (!finFichier)
					{
						ligne = inBuff.readLine(); // on passe la ligne "piste :"
						int nPis = new Integer(ligne).intValue();
						pisteReadable.add(nPis);
						ligne = inBuff.readLine(); // on passe la ligne "npts num...."
						if (ligne.startsWith("name"))
						{
							ligne = inBuff.readLine();
							ligne = inBuff.readLine();
						}
						ligne = inBuff.readLine();
						Integer nbP = new Integer(ligne);
						int nbPis = nbP.intValue(); // on recupere le nombre de points
						nbPtsPerPiste.setElementAt(nbPis,nPis);
						if (nbPis != 0)
							cptPiste++;
					}
				}
			}
		}
		catch (IOException ioe)
		{
			System.out.println("erreur lecture du fichier : " + ioe.toString());
		}
		// on recupere les numeros de pistes non-vides ainsi que leurs nombres
		// de points respectifs.
		String[] tmp = new String[cptPiste + 1];
		tmp[0] = "n" + Ut.numCar + "?";
		int c = 1;
		tmpContenu = "Content of the file :\n" + nomFichier + "\n\n";
		for (int np : pisteReadable)
			if (nbPtsPerPiste.get(np) != 0)
			{
				tmp[c] = "" + np;
				tmpContenu = tmpContenu + "Track : " + np + " - nbPoints : " + nbPtsPerPiste.get(np) + "\n";
				c++;
			}
		contenu2 = tmpContenu;
		this.nbReadableTracks = c;
		return tmp;
	}

	public int getNbReadableTracks()
	{
		// pour recuperer le nombre de pistes non-vides.
		return this.nbReadableTracks;
	}

	public String getContenu()
	{
		return this.contenu2;
	}

	public int getNbPtsForTrack(int numTrack)
	{
		// pour recuperer le nombre de pistes non-vides.
		return this.nbPtsPerPiste.get(numTrack);
	}

	private float limit(float d)
	{
		if (d > 819.1)
			return (float) 819.1;
		else if (d < -819.2)
			return (float) -819.2;
		else
			return d;
	}
}
