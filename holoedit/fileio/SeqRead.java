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
 * Classe permettant la lecture des fichiers au format seq~
 */
package holoedit.fileio;

import holoedit.data.HoloPoint;
import holoedit.data.HoloTrack;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;

//  
// -----------------------------------------------------------------------------------------------------------------------------
public class SeqRead implements Runnable
{
	GestionPistes gp;
	private String fichier;
	private String contenu2;
	private int nbPistes = Ut.INIT_TRACK_NB;
	private float conv = 1000000; // conversion pour avoir la date entre 0 et 1 dans seq~
	private int numPisteLecture = -1; // numero de la piste a lire si une seule piste a charger
	private int numPisteEcriture = -1; // numero de la piste a ecrire si une seule piste a charger
	private int compteur; // num du point en cours de traitement sur l'ensemble des 16 pistes
	private ProgressBar barreProgression;
	private int nbReadableTracks = 0; // nombre de pistes non-vides
	private Vector<Integer> pisteReadable = new Vector<Integer>(); // vecteur de pistes non-vides
	Vector<Integer> nbPtsPerPiste = new Vector<Integer>(); // tableau du nombre de pts par piste non-vide
	private Thread runner;

	// constructeur par defaut
	public SeqRead(GestionPistes _gp)
	{
		fichier = new String();
		barreProgression = new ProgressBar("Loading...");
		gp = _gp;
		compteur = 1;
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// methode de lecture du fichier
	public void readSeqFile(String nomFichier)
	{ // all tracks
		int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise all tracks ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (result == 0)
		{
			gp.initColors(Ut.OLD_TRACK_NB);
			gp.holoEditRef.counterPanel.setCompteur(1, 0);
			gp.holoEditRef.counterPanel.setCompteur(2, 0);
			gp.holoEditRef.counterPanel.setCompteur(3, 0);
			gp.tracks = new Vector<HoloTrack>();
			nbPtsPerPiste = new Vector<Integer>();
			for (int i = 0; i < Ut.OLD_TRACK_NB; i++)
			{
				gp.tracks.add(new HoloTrack(i,gp.couleurs[i]));
				nbPtsPerPiste.add(0);
				gp.getTrack(i).init();
				gp.getTrack(i).setVisible(false);
			}
			gp.updateTrackSelector(-3);
			numPisteLecture = -1;
			numPisteEcriture = -1;
			fichier = nomFichier;
			barreProgression.open();
			barreProgression.setValue(0);
			gp.setActiveTrack(-1);
			gp.holoEditRef.room.initVars(true);
			runner = new Thread(this);
			runner.start();
		}
	}

	// methode de lecture du fichier
	// all tracks
	public void readSeqFileDirect(String nomFichier)
	{
		gp.initColors(Ut.OLD_TRACK_NB);
		gp.holoEditRef.counterPanel.setCompteur(1, 0);
		gp.holoEditRef.counterPanel.setCompteur(2, 0);
		gp.holoEditRef.counterPanel.setCompteur(3, 0);
		gp.tracks = new Vector<HoloTrack>();
		nbPtsPerPiste = new Vector<Integer>();
		for (int i = 0; i < Ut.OLD_TRACK_NB; i++)
		{
			gp.tracks.add(new HoloTrack(i,gp.couleurs[i]));
			nbPtsPerPiste.add(0);
			gp.getTrack(i).init();
			gp.getTrack(i).setVisible(false);
		}
		gp.updateTrackSelector(-3);
		numPisteLecture = -1;
		numPisteEcriture = -1;
		fichier = nomFichier;
		barreProgression.open();
		barreProgression.setValue(0);
		gp.setActiveTrack(-1);
		gp.holoEditRef.room.initVars(true);
		runner = new Thread(this);
		runner.start();
	}

	// one track
	public void readSeqFileOneTrack(String nomFichier, int numPin, int numPout)
	{
		if (gp.getNbTracks() > numPout && !gp.getTrack(numPout).isEmpty())
		{
			int result = JOptionPane.showConfirmDialog(null, "Do you really want to initialise the track " + numPout + " ?", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == 0)
			{
				gp.getTrack(numPout).init();
				gp.holoEditRef.counterPanel.setCompteur(1, 0);
				gp.holoEditRef.counterPanel.setCompteur(2, 0);
				gp.holoEditRef.counterPanel.setCompteur(3, 0);
				numPisteLecture = numPin;
				numPisteEcriture = numPout;
				fichier = nomFichier;
				barreProgression.open();
				barreProgression.setValue(0);
				gp.setActiveTrack(-1);
				gp.holoEditRef.room.initVars(true);
				runner = new Thread(this);
				runner.start();
			}
		} else
		{
			gp.getTrack(numPout).init();
			gp.holoEditRef.counterPanel.setCompteur(1, 0);
			gp.holoEditRef.counterPanel.setCompteur(2, 0);
			gp.holoEditRef.counterPanel.setCompteur(3, 0);
			numPisteLecture = numPin;
			numPisteEcriture = numPout;
			fichier = nomFichier;
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
	// lecture du fichier
	private void chargement()
	{
		// System.out.println("W : "+this.numPisteEcriture+" - R : "+this.numPisteLecture);
		String nomFichier = new String(fichier);
		try
		{
			if (numPisteLecture <= 0)
			{
				getReadableTracks(nomFichier);
				compteur = getTotalNbPts();
			} else
			{
				compteur = nbPtsPerPiste.get(numPisteLecture);
			}
			barreProgression.setMaximum(compteur);
			compteur = 0;
			// creation du flux d'entree
			FileReader seqFile = new FileReader(nomFichier);
			BufferedReader inBuff = new BufferedReader(seqFile);
			boolean finFichier = false;
			String ligne = new String();
			String currPt = new String();
			int currDate = 0;
			int dateMax = 0; // derniere date lue pour la piste
			while (!finFichier)
			{
				ligne = inBuff.readLine();
				if (ligne == null)
				{
					finFichier = true;
				} else
				{
					if (ligne.startsWith("id"))
					{
						ligne = inBuff.readLine();
					}
					if (ligne == null)
					{
						finFichier = true;
					} else
					{
						int index = ligne.indexOf(" ");
						int index2;
						currDate = Math.round((Float.valueOf(ligne.substring(0, index)).floatValue()) * conv);
						// System.out.println("date : "+currDate);
						ligne = ligne.substring(index + 1);
						// System.out.println("ligne : "+ligne);
						boolean ptFound = true;
						while (ptFound)
						{
							index = ligne.indexOf("ed");// ,index);
							index2 = ligne.indexOf("ne");// ,index);
							if ((index != -1) && (index2 != -1))
							{
								index = Math.min(index, index2);
								ptFound = true;
							} else if ((index == -1) && (index2 != -1))
							{
								index = index2;
								ptFound = true;
							} else if ((index2 == -1) && (index != -1))
							{
								// index = index;
								ptFound = true;
							} else
							{
								ptFound = false;
							}
							// System.out.println("ind : "+index+" "+index2);
							if (ptFound)
							{
								currPt = ligne.substring(0, index + 3);
								if (traiterPt(currPt, currDate))
								{
									compteur++;
									barreProgression.setValue(compteur);
									dateMax = currDate; // derniere date lue pour la piste.
								}
								ligne = ligne.substring(index + 3);
								// System.out.println("nv ligne : "+ligne);
							}
						}
					}
				}
			}
			// fermeture du flux d'entree
			inBuff.close();
			barreProgression.dispose();
			// recherche de la premiere piste visible pour l'activer
			if (numPisteEcriture == -1)
			{
				gp.holoEditRef.counterPanel.setCompteur(2, currDate); // si toutes piste, la derniere date lue est la derniere date a afficher.
				gp.holoEditRef.counterPanel.setCompteur(3, Math.max(gp.holoEditRef.counterPanel.getDate(3), currDate));
				boolean found = false;
				int k = 1;
				while ((!found) && (k < gp.getNbTracks()))
				{
					if (!gp.getTrack(k).isEmpty())
					{
						found = true;
						gp.ts.labelAudio[k].activate();
						gp.setActiveTrack(k);
					}
					k++;
				}
				for (k = 0; k < gp.getNbTracks() ; k++)
				{
					HoloTrack h = gp.getTrack(k);
					if (!h.isEmpty())
					{
						h.elementAt(h.getLastPoint()).setEditable(true);
						h.elementAt(0).setEditable(true);
					}
				}
			} else if (numPisteEcriture == 0)
			{
				// on met juste a jour les compteurs.
				gp.holoEditRef.counterPanel.setCompteur(2, Math.max(gp.holoEditRef.counterPanel.getDate(2), dateMax));
				gp.holoEditRef.counterPanel.setCompteur(3, Math.max(gp.holoEditRef.counterPanel.getDate(3), dateMax));
				int k = 1;
				for (k = 0; k < gp.getNbTracks() ; k++)
				{
					HoloTrack h = gp.getTrack(k);
					if (!h.isEmpty())
					{
						h.elementAt(h.getLastPoint()).setEditable(true);
						h.elementAt(0).setEditable(true);
					}
				}
			} else
			{
				// on met la date de fin au maximum entre la date en cours de l'editeur et la derniere date lue de la piste.
				gp.holoEditRef.counterPanel.setCompteur(2, Math.max(gp.holoEditRef.counterPanel.getDate(2), dateMax));
				gp.holoEditRef.counterPanel.setCompteur(3, Math.max(gp.holoEditRef.counterPanel.getDate(3), dateMax));
				gp.ts.labelAudio[numPisteEcriture].activate();
				gp.setActiveTrack(numPisteEcriture);
				HoloTrack h = gp.getTrack(numPisteEcriture);
				h.elementAt(h.getLastPoint()).setEditable(true);
				h.elementAt(0).setEditable(true);
			}
			// seqReadFrame = new TextReadFrame ( nomFichier , contenu ) ; //*/
			for(HoloTrack h:gp.tracks)
				if(h.isEmpty())
					h.setVisible(false);
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

	// lecture d'un point
	private boolean traiterPt(String currPt, int currDate)
	{
		// On extrait les valeurs de la cha”ne de caractere
		int currInd = currPt.indexOf(" ");
		// numero de piste
		while (currInd == 0)
		{
			currPt = currPt.substring(1);
			currInd = currPt.indexOf(" ");
		}
		int numPiste = Integer.valueOf(currPt.substring(0, currInd)).intValue();
		currPt = currPt.substring(currInd + 1);
		currInd = currPt.indexOf(" ");
		// coord X
		float x = Float.valueOf(currPt.substring(0, currInd)).floatValue() + (float) 819.2;
		currPt = currPt.substring(currInd + 1);
		currInd = currPt.indexOf(" ");
		// coord Y
		float y = Float.valueOf(currPt.substring(0, currInd)).floatValue() + (float) 819.2;
		currPt = currPt.substring(currInd + 1);
		currInd = currPt.indexOf(" ");
		// coord Z
		float z = Float.valueOf(currPt.substring(0, currInd)).floatValue() + 100;
		currPt = currPt.substring(currInd + 1);
		// point editable ou non-editable
		boolean edit = false;
		if (currPt.indexOf("ed") != -1)
		{
			edit = true;
		}
		// System.out.println(currDate+"_"+numPiste+"_"+x+"_"+y+"_"+z+"_"+edit);
		HoloPoint hP = new HoloPoint(limit(x), limit(y), z, currDate, edit);
		HoloTrack h = gp.getTrack(numPiste-1);
		if (this.numPisteLecture == -1)
		{
			h.addElement(hP);
			if (!h.isVisible())
			{
				h.setVisible(true);
				gp.ts.checkVisible[numPiste].check(true);
			}
			return true;
		} else if (numPisteLecture == 0)
		{
			if (h.isVisible())
			{
				h.addElement(hP);
				return true;
			}
			return false;
		} else if (this.numPisteLecture == numPiste)
		{
			gp.getTrack(numPisteEcriture-1).addElement(hP);
			return true;
		} else
			return false;
	}

	// fonction recensant les pistes non vide
	public String[] getReadableTracks(String nomFichier)
	{
		pisteReadable = new Vector<Integer>();
		int cptPiste = 0; // nombre de pistes lisibles;
		for (int i = 0; i < gp.getNbTracks() ; i++)
		{
			nbPtsPerPiste.set(i,0);
		}
		String contenu = new String();
		int numPiste = 0;// new Integer(0);
		String nP = new String();
		String ligne = new String();
		boolean finFichier = false;
		try
		{
			FileReader seqFile = new FileReader(nomFichier);
			BufferedReader inBuff = new BufferedReader(seqFile);
			while (!finFichier)
			{
				ligne = inBuff.readLine();
				if (ligne == null)
					finFichier = true;
				else
				{
					if (ligne.startsWith("id"))
					{
						ligne = inBuff.readLine();
					}
					if (ligne == null)
					{
						finFichier = true;
					} else
					{
						ligne = ligne.substring(ligne.indexOf(" ") + 1); // on passe la date
						while (ligne.indexOf(";") > Ut.INIT_TRACK_NB + 1) // tant qu'on est pas a la derniere piste
						{
							if (ligne.indexOf(" ") == 0)
								ligne = ligne.substring(1);
							nP = ligne.substring(0, ligne.indexOf(" ")); // numero de piste
							numPiste = new Integer(nP).intValue();
							if (!pisteReadable.contains(numPiste))
							{
								pisteReadable.add(numPiste);
								cptPiste++;
								nbPtsPerPiste.set(numPiste,1);
							} else
								nbPtsPerPiste.set(numPiste,nbPtsPerPiste.get(numPiste)+1);
							ligne = ligne.substring(ligne.indexOf(" ") + 1); // -x
							ligne = ligne.substring(ligne.indexOf(" ") + 1); // -y
							ligne = ligne.substring(ligne.indexOf(" ") + 1); // -z
							ligne = ligne.substring(ligne.indexOf(" ") + 1); // ed
							ligne = ligne.substring(ligne.indexOf(" ") + 1); // prochain numero de piste
						}
					}
				}
			}
		}
		catch (IOException ioe)
		{
			System.out.println("erreur lecture du fichier : " + ioe.toString());
		}
		// on recupere les numeros de pistes non-vides ainsi que leurs nombres de points respectifs.
		String[] tmp = new String[cptPiste + 1];
		tmp[0] = "n" + Ut.numCar + "?";
		int c = 1;
		contenu = "Content of the file :\n" + nomFichier + "\n\n";
		for (int np : pisteReadable)
		{
			tmp[c] = "" + np;
			contenu = contenu + "Track : " + np + " - nbPoints : " + nbPtsPerPiste.get(np) + "\n";
			c++;
		}
		contenu2 = contenu;
		this.nbReadableTracks = c;
		return tmp;
	}

	// retourne le nb total de pts
	private int getTotalNbPts()
	{
		int total = 0;
		for (int i = 1; i <= nbPistes; i++)
		{
			if (numPisteLecture == -1)
				total = total + nbPtsPerPiste.get(i);
			else if (numPisteLecture == 0)
				if (gp.getTrack(i).isVisible())
					total = total + nbPtsPerPiste.get(i);
		}
		return total;
	}

	// retourne le nb total de pistes lisibles (non-vides)
	public int getNbReadableTracks()
	{
		// pour recuperer le nombre de pistes non-vides.
		return this.nbReadableTracks;
	}

	// retourne le contenu du fichier (nb de pistes, nb de pts/piste...)
	public String getContenu()
	{
		return this.contenu2;
	}

	// fonction permettant de limiter les points a [-819.2 819.2]
	private float limit(float d)
	{
		if (d > 819.1)
		{
			return (float) 819.1;
		} else if (d < -819.2)
		{
			return (float) -819.2;
		} else
		{
			return d;
		}
	}

	private void stopThread()
	{
		if (runner != null)
		{
			runner = null;
		}
	}
}
