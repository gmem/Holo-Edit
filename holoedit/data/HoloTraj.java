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
/**
 * On part ici du principe qu'un segment de trajet est une succession
 * de points dont les dates sont dans le referentiel de la piste et les index
 * dans le referentiel du trajet ainsi qu'un index de debut
 * 
 * attention donc aux differents referentiels
 * (index du point par rapport a la piste, index du point par rapport au trajet)
 * 
 * on peut egalement copier, joindre deux trajets, couper en deux un trajets,
 * "rogner" entre deux dates, stretcher au debut ou a la fin
 */
package holoedit.data;

import holoedit.gui.TimeEditorGUI.CurvePoint;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.RoomIndex;
import holoedit.opengl.ScoreIndex;
import holoedit.opengl.TimeIndex;
import holoedit.util.Ut;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Vector;
import javax.media.opengl.GL;

//import com.sun.tools.example.debug.gui.GUI;

// FEATURE TIMEEDITOR HOLOTRAJ voir déclaration des param?tres supplémentaires des holopoints
// OPTIMISE HOLOTRAJ LISTID -> GENLIST
public class HoloTraj
{
	/* ****** ATTRIBUTS ******** */
	/** liste des points */
	public HoloPointVector points = new HoloPointVector(100, 10);
	// numero du premier point (dans la piste totale)
	private int begNumber = 0;
	public int currentRead = -1;
	private float trajLineWidth = 1;
	boolean isPlaying = false;
	boolean Recording = false; // does it needs finalizing like overdubing on older trajs
	
	// pour display list
	private int GLlistIDscore,GLlistIDroom,GLlistIDroom3D;
	private int GLlistIDTimeX,GLlistIDTimeY,GLlistIDTimeZ,GLlistIDTimeR,GLlistIDTimeT;
	private int GLlistIDTimeXoff,GLlistIDTimeYoff,GLlistIDTimeZoff,GLlistIDTimeRoff,GLlistIDTimeToff;
	
	// variable de demande d'update
	private boolean scoreGUIdirty = true;
	private boolean roomGUIdirty = true;
	private boolean room3DGUIdirty = true;
	private boolean timeEdGUIdirty = true;

	/* ******* CONSTRUCTEURS ******* */
	public HoloTraj()
	{
	}
	
	public HoloTraj(int bn)
	{
		begNumber = (bn > 0 ? bn : 0);
	}

	public HoloTraj(HoloPointVector v, int bn)
	{
		points = v;
		begNumber = (bn > 0 ? bn : 0);
	}

	public HoloTraj dupliquer()
	{
		return new HoloTraj(vectorDup(), begNumber);
	}

	public HoloPointVector vectorDup()
	{
		HoloPointVector tmpPoint = new HoloPointVector(points.size(), 10);
		for (HoloPoint p : points)
			tmpPoint.add(p.dupliquer());
		return tmpPoint;
	}

	public boolean equals(HoloTraj ht)
	{
		return ((points).equals(ht.points)) && (begNumber == ht.begNumber);
	}

	public void clear()
	{
		points.clear();
		setDirty(true);
	}

	public String toString()
	{
		StringBuffer res = new StringBuffer("\t\t<traj begin_number=\"" + getBegNumber() + "\">\n");
		int cpt = 0;
		for (HoloPoint hp : points)
		{
			res.append("\t\t\t" + hp.toString() + "\n");
			cpt++;
		}
		res.append("\t\t</traj>\n");
		return res.toString();
	}
	
	public String toTjFile()
	{
		StringBuffer res = new StringBuffer("\t\t<traj begin_number=\"" + getBegNumber() + "\">\n");
		int cpt = 0;
		for (HoloPoint hp : points)
		{
			res.append("\t\t\t" + hp.toString() + "\n");
			cpt++;
		}
		res.append("\t\t</traj>\n");
		return res.toString();
	}
	
	public String toICSTFile()
	{
		
		DecimalFormat formateur = (DecimalFormat) NumberFormat.getInstance(); 
		DecimalFormatSymbols symboles = new DecimalFormatSymbols(); 
		symboles.setDecimalSeparator('.');
		formateur.setDecimalFormatSymbols(symboles); 
		formateur.setMinimumIntegerDigits(1); 
		formateur.setMinimumFractionDigits(1); 
		formateur.setMaximumFractionDigits(8);
		
		StringBuffer res = new StringBuffer("\n<ambiscore version=\"1.0\">\n");
		res.append("\t<xyz-def handedness=\"right\" x-axis=\"right\" />\n");
		res.append("\t<trajectory>\n");
		int cpt = 0;
		for (HoloPoint hp : points)
		{
			res.append("\t\t<point>\n");
			res.append("\t\t\t<time>"+(hp.date)+"</time>\n");
			res.append("\t\t\t<xyz>"+formateur.format((hp.x)/100)+" "+formateur.format((hp.y)/100)+" "+formateur.format((hp.z)/100)+"</xyz>\n");
			res.append("\t\t\t<editable>"+ (hp.isEditable() ? "true" : "false") +"</editable>\n");
			res.append("\t\t</point>\n");
			cpt++;
		}
		res.append("\t</trajectory>\n");
		res.append("</ambiscore>\n");
		
		return res.toString();
	}
	
	public void print()
	{
		System.out.println("\t<traj begin_number=\"" + getBegNumber() + "\" nb=\"" + size() + "\" beg=\"" + getFirstDate() + "\" end=\"" + getLastDate() + "\"/>");
	}

	/* ************ Modification/Retour des parametres *************** */
	
	/** fonction de demande de mise à jour générale */
	public void setDirty(boolean val)
	{
		scoreGUIdirty = val;
		roomGUIdirty = val;
		room3DGUIdirty = val;
		timeEdGUIdirty = val;
	}
	
	public void setDirty(int mask)
	{
		if((mask & Ut.DIRTY_ALL) > 0)
		{
			setDirty(true);
			return;
		}
		roomGUIdirty = (mask & Ut.DIRTY_ROOM) > 0;
		room3DGUIdirty = (mask & Ut.DIRTY_ROOM3D) > 0;
		scoreGUIdirty = (mask & Ut.DIRTY_SCORE) > 0;
		timeEdGUIdirty = (mask & Ut.DIRTY_TIME) > 0;
		
			
	}
	
	/** */
	public void setRecording(boolean v)
	{
		Recording = v;
	}
	
	public boolean isRecording()
	{
		return Recording;
	}
	/** Modification de l'indice de depart begNumber (par rapport a la piste) */
	public void setBegNumber(int bn)
	{
		begNumber = (bn > 0 ? bn : 0);
		setDirty(true);
	}

	public int getBegNumber()
	{
		return begNumber;
	}

	public void changeBegNumber(int shift)
	{
		int tmp = begNumber + shift;
		begNumber = (tmp > 0 ? tmp : 0);
		setDirty(true);
	}

	public int getEndNumber()
	{
		return begNumber + points.size() - 1;
	}

	public int size()
	{
		return points.size();
	}

	/** Recuperation de tous les points */
	public Vector<HoloPoint> elements()
	{
		return points;
	}

	/** Recuperation d'un point */
	public HoloPoint firstElement()
	{
		if (!isEmpty())
			return points.firstElement();
		return null;
	}

	public HoloPoint lastElement()
	{
		if (!isEmpty())
			return points.lastElement();
		return null;
	}

	public HoloPoint elementAt(int i)
	{
		if (i <= getEndNumber() && i >= getBegNumber())
			return elementAtReal(i - begNumber);
		return null;
	}

	public HoloPoint elementAtReal(int i)
	{
		return points.elementAt(i);
	}

	public HoloPoint get(int i)
	{
		return elementAt(i);
	}

	/** Suppression avec index retourne le nombre d'éléments supprimés */
	public int removeElementAt(int i)
	{
		if (i <= getEndNumber() && i >= getBegNumber())
			return removeElementAtReal(i - begNumber);
		return 0;
	}

	public int removeElementAtReal(int realIndex)
	{
		HoloPoint p = points.get(realIndex);
		if(!p.isEditable())
		{
			points.remove(realIndex);
			return 1;
		}
		
		if(realIndex == 0)
		{
			points.remove(0);
			if(!isEmpty())
				points.firstElement().setEditable(true);
			return 1;
		} else if(realIndex == points.size()-1)
		{
			int j = prevEditPoint(realIndex)+1;
			int cpt = 0;
			for (int k = realIndex; k >= j; k--)
			{
				points.remove(points.size()-1);
				cpt++;
			}
			return cpt;
		} else {
			// suppression avec déplacement XY et changement de date;
			int datej, datei, dur;
			float dx, dy, dz, fact;
			HoloPoint prevPt, actualPt, nthPt;
			int j = prevEditPoint(realIndex); // point editable precedent
			int k = nextEditPoint(realIndex); // point editable suivant
			if(j != -1 && k != -1)
			{
				prevPt = points.elementAt(j);
				actualPt = points.elementAt(realIndex);
				datej = prevPt.date;
				datei = actualPt.date;
				dur = datei - datej;
				dx = prevPt.x - actualPt.x;
				dy = prevPt.y - actualPt.y;
				dz = prevPt.z - actualPt.z;
				for (int nth = realIndex + 1, last = size() ; nth < last ; nth++)
				{
					nthPt = points.elementAt(nth);
					if (nth < k)
					{ // ----- changement de position du point
						fact = (float) (k == realIndex ? 0. : (float) (k - nth) / (float) (k - realIndex));
						nthPt.translater(dx * fact, dy * fact, dz * fact);
					}
					nthPt.date = nthPt.date - dur; // changement de date des points
				}
				int cpt = 0;
				for (int nth = realIndex; nth > j; nth--)
				{
					points.remove(nth);
					cpt++;
				}
				return cpt;
			}

			System.out.println("x removeElementAt error : this should not occur");
		}
		return 0;
	}

	public int removeElementAtReal2(int realIndex)
	{
		HoloPoint p = points.get(realIndex);
		if(!p.isEditable())
		{
			points.remove(realIndex);
			return 1;
		}
		
		if(realIndex == 0)
		{
			points.remove(0);
			int cpt = 1;
			while(!points.isEmpty() && !points.firstElement().isEditable())
			{
				points.remove(0);
				cpt++;
			}
//			if(!isEmpty())
//				points.firstElement().setEditable(true);
			return cpt;
		} else if(realIndex == points.size()-1)
		{
			int j = prevEditPoint(realIndex)+1;
			int cpt = 0;
			for (int k = realIndex; k >= j; k--)
			{
				points.remove(points.size()-1);
				cpt++;
			}
			return cpt;
		} else {
			// suppression avec déplacement XY et changement de date;
			int datej, datei, dur;
			float dx, dy, dz, fact;
			HoloPoint prevPt, actualPt, nthPt;
			int j = prevEditPoint(realIndex); // point editable precedent
			int k = nextEditPoint(realIndex); // point editable suivant
			if(j != -1 && k != -1)
			{
				prevPt = points.elementAt(j);
				actualPt = points.elementAt(realIndex);
				datej = prevPt.date;
				datei = actualPt.date;
				dur = datei - datej;
				dx = prevPt.x - actualPt.x;
				dy = prevPt.y - actualPt.y;
				dz = prevPt.z - actualPt.z;
				for (int nth = realIndex + 1, last = size() ; nth < last ; nth++)
				{
					nthPt = points.elementAt(nth);
					if (nth < k)
					{ // ----- changement de position du point
						fact = (float) (k == realIndex ? 0. : (float) (k - nth) / (float) (k - realIndex));
						nthPt.translater(dx * fact, dy * fact, dz * fact);
					}
					nthPt.date = nthPt.date - dur; // changement de date des points
				}
				int cpt = 0;
				for (int nth = realIndex; nth > j; nth--)
				{
					points.remove(nth);
					cpt++;
				}
				return cpt;
			}
			
			System.out.println("x removeElementAt error : this should not occur");
		}
		return 0;
	}
	
	public void remove(int i)
	{
		removeElementAt(i);
	}

	public void remove(int first, int last)
	{
		if(first == -1 && last != -1) // traject is cut from begin to "last"
		{
			for(int i = last-begNumber ; i >= 0 ; i--)
				points.remove(0);
		} else if(first != -1 && last == -1) // traject is cut from "first" to end
		{
			for(int i = first-begNumber, max = points.size() ; i < max ; i++)
				points.remove(points.lastElement());
		} else{
			
			// METHODE SIMPLE DE SUPPRESSION
//			points.get(last+1-begNumber).setEditable(true);
//			points.get(first-1-begNumber).setEditable(true);
//			while(last >= first) // traject is cut between "first" & "last"
//			{
//				points.remove(first-begNumber);
//				last--;
//			}
			
			int realBeg = first - begNumber;
			int realEnd = last - begNumber;
			
			// suppression avec déplacement XY et changement de date;
			int datej, datei, dur;
			float dx, dy, dz, fact;
			HoloPoint prevPt, actualPt, nthPt;
			int j = prevEditPoint(realBeg); // point editable precedent
			int k = nextEditPoint(realEnd); // point editable suivant
			if(j != -1 && k != -1)
			{
				prevPt = points.elementAt(j);
				actualPt = points.elementAt(realEnd);
				datej = prevPt.date;
				datei = actualPt.date;
				dur = datei - datej;
				dx = prevPt.x - actualPt.x;
				dy = prevPt.y - actualPt.y;
				dz = prevPt.z - actualPt.z;
				for (int nth = realEnd + 1, max = size() ; nth < max ; nth++)
				{
					nthPt = points.elementAt(nth);
					if (nth < k)
					{ 	// ----- changement de position du point
						fact = (float) (k == realEnd ? 0. : (float) (k - nth) / (float) (k - realEnd));
						nthPt.translater(dx * fact, dy * fact, dz * fact);
					}
					nthPt.date = nthPt.date - dur; // changement de date des points
				}
				int cpt = 0;
				for (int nth = realEnd; nth > j; nth--)
				{
					points.remove(nth);
					cpt++;
				}
			}
		}
	}
	
	/** Ajout en queue */
	public void add(HoloPoint hp)
	{
		addElement(hp);
		setDirty(true);
	}

	public void addElement(HoloPoint hp)
	{
		if (hp != null && hp.date >= getLastDate())
			points.add(hp);
		setDirty(true);
	}

	/** Avec l'index d'ajout */
	public void insertElementAt(HoloPoint hp, int i)
	{
		if (i <= getEndNumber() && i >= getBegNumber())
			insertElementAtReal(hp, i - begNumber);
		setDirty(true);
	}

	public void insertElementAtReal(HoloPoint hp, int i)
	{
		points.insertElementAt(hp, i);
		setDirty(true);
	}

	public void add(HoloPoint hp, int i)
	{
		insertElementAt(hp, i);
		setDirty(true);
	}

	public void addElement(HoloPoint hp, int i)
	{
		insertElementAt(hp, i);
		setDirty(true);
	}

	/** Remplacement d'un HoloPoint par un autre */
	public void setElementAt(HoloPoint hp, int i)
	{
		if (i <= getEndNumber() && i >= getBegNumber())
			points.setElementAt(hp, i - begNumber);
		setDirty(true);
	}

	public void set(int i, HoloPoint hp)
	{
		setElementAt(hp, i);
		setDirty(true);
	}

	public boolean isEmpty()
	{
		return points.isEmpty();
	}

	/** renvoie l'indice du dernier point */
	public int getLastPoint()
	{
		return begNumber + size() - 1;
	}

	public int getLastDate()
	{
		if (points.isEmpty())
			return 0;
		return points.lastElement().date;
	}

	public int getFirstDate()
	{
		if (points.isEmpty())
			return 0;
		return points.firstElement().date;
	}

	public int getDate(int n)
	{
		if (n < 0 || n >= points.size())
			return 0;
		return points.elementAt(n).date;
	}

	public int getDuration()
	{
		return (getLastDate() - getFirstDate());
	}

	
	/* ************ Recherche *************** */
	/** donne l'indice du point editable precedent le nieme
	 * on ne renvoie pas sur l'HoloTraj precedent puisque normalement
	 * le premier point d'un holotraj est editable */
	public int prevEditPoint(int n)
	{
		int j = 0;
		if (n <= points.size() && n > 0)
			for (j = n - 1; j >= 0; j--)
				if (points.elementAt(j).isEditable())
					return j;
		return -1;
	}

	/** donne l'indice du point editable suivant le nieme
	 * on ne renvoie pas sur l'HoloTraj precedent puisque
	 * normalement le dernier point d'un holotraj est editable */
	public int nextEditPoint(int n)
	{
		int j = n;
		int last = points.size();
		if (n < last - 1)
			for (j = n + 1 ; j < last ; j++)
				if (points.elementAt(j).isEditable())
					return j;
		return -1;
	}

	/** renvoie l'indice du 1er point situe avant la date passee en parametre
	 * le test d'appartenance d'un tel point de cet HoloTraj est fait dans piste */
	protected int previousPoint(int date)
	{
		int indice = points.size() - 1;
		boolean trouve = false;
		while ((indice >= 0) & !trouve)
		{
			if (points.elementAt(indice).date < date)
				trouve = true;
			indice = indice - 1;
		}
		return (indice + 1);
	}

	/** renvoie l'indice du 1er point situe avant ou a la date passee en parametre
	 * le test d'appartenance d'un tel point de cet HoloTraj est fait dans piste */
	protected int previousPoint2(int date)
	{
		int indice = points.size() - 1;
		boolean trouve = false;
		while ((indice >= 0) & !trouve)
		{
			if (points.elementAt(indice).date <= date)
				trouve = true;
			indice = indice - 1;
		}
		return (indice + 1);
	}

	/** renvoie l'indice du 1er point situe apres la date passee en parametre
	 * le test d'appartenance d'un tel point de cet HoloTraj est fait dans piste */
	protected int nextPoint(int date)
	{
		int indice = 0;
		boolean trouve = false;
		while (indice < points.size() & !trouve)
		{
			if (points.elementAt(indice).date > date)
				trouve = true;
			indice = indice + 1;
		}
		return (indice - 1);
	}

	/** renvoie l'indice du 1er point situe apres ou a la date passee en parametre
	 * le test d'appartenance d'un tel point de cet HoloTraj est fait dans piste */
	protected int nextPoint2(int date)
	{
		int indice = 0;
		boolean trouve = false;
		while (indice < points.size() & !trouve)
		{
			if (points.elementAt(indice).date >= date)
				trouve = true;
			indice = indice + 1;
		}
		return (indice - 1);
	}

	/* ******* Ajout / Suppression *********** */
	// ajout d'un point en bout de trajectoire avec points intermediaires
	/*	public int addPoint(HoloPoint p, double delta, int nbPointsInter, float echelle, int dateBegin)
	{
		double date = 0;
		if (!points.isEmpty())
		{
			
			 * On doit ajouter les points intermediaires et tracer le segment On trace chaque segment intermediaire separement, pour plus de precision dans l'affichage ( sinon certains points intermediaires ne sont pas exactement sur le segment reliant deux points editables )
			 
			HoloPoint dernierPoint = points.lastElement();
			if (dernierPoint.date >= dateBegin)
			{
				float x = dernierPoint.x * echelle;
				float y = dernierPoint.y * echelle;
				date = dernierPoint.date;
				double dt = delta / (nbPointsInter + 1);
				float dx = (p.x - x) / (nbPointsInter + 1);
				float dy = (p.y - y) / (nbPointsInter + 1);
				for (int i = 1; i <= nbPointsInter; ++i)
				{
					x = x + dx;
					y = y + dy;
					date = date + dt;
					 ajout d'un point intermediaire 
					points.addElement(new HoloPoint(x / echelle, y / echelle, (int) date, false));
				}
				date = dernierPoint.date + delta;
			}
		} else
		{
			 premier point 
			date = dateBegin;
		}
		p.diviserCoord(echelle);
		p.date = (int) date;
		points.addElement(p);
		return (int)date;
	}//*/

	public int addPoint(HoloPoint p, double delta, int nbPointsInter, int dateBegin)
	{
		double date = 0;
		if (!points.isEmpty())
		{
			/*
			 * On doit ajouter les points intermediaires et tracer le segment On trace chaque segment intermediaire separement, pour plus de precision dans l'affichage ( sinon certains points intermediaires ne sont pas exactement sur le segment reliant deux points editables )
			 */
			HoloPoint dernierPoint = points.lastElement();
			if (dernierPoint.date >= dateBegin)
			{
				float x = dernierPoint.x;
				float y = dernierPoint.y;
				float z = dernierPoint.z;
				date = dernierPoint.date;
				double dt = delta / (nbPointsInter + 1);
				float dx = (p.x - x) / (nbPointsInter + 1);
				float dy = (p.y - y) / (nbPointsInter + 1);
				float dz = (p.z - z) / (nbPointsInter + 1);
				for (int i = 1; i <= nbPointsInter; ++i)
				{
					x += dx;
					y += dy;
					z += dz;
					date += dt;
					/* ajout d'un point intermediaire */
					points.addElement(new HoloPoint(x, y, z, (int) date, false));
				}
				date = dernierPoint.date + delta;
			}
		} else
		{
			/* premier point */
			date = dateBegin;
		}
		p.date = (int) date;
		points.addElement(p);
		setDirty(true);
		return (int)date;
	}

	public void addPointEnd(HoloPoint p, int nbPointsInter)
	{
		/*
		 * On doit ajouter les points intermediaires et tracer le segment On trace chaque segment intermediaire separement, pour plus de precision dans l'affichage ( sinon certains points intermediaires ne sont pas exactement sur le segment reliant deux points editables )
		 */
		HoloPoint dernierPoint = points.lastElement();
		double date = dernierPoint.date;
		double dt = (p.date - dernierPoint.date) / (nbPointsInter + 1);
		if(dt != 0)
		{
			float x = dernierPoint.x;
			float y = dernierPoint.y;
			float z = dernierPoint.z;
			float dx = (p.x - x) / (nbPointsInter + 1);
			float dy = (p.y - y) / (nbPointsInter + 1);
			float dz = (p.z - z) / (nbPointsInter + 1);
			for (int i = 1; i <= nbPointsInter; ++i)
			{
				x += dx;
				y += dy;
				z += dz;
				date += dt;
				/* ajout d'un point intermediaire */
				points.addElement(new HoloPoint(x, y, z, (int) date, false));
			}
		}
		points.addElement(p);
		setDirty(true);
	}
	
	public void addPointBegin(HoloPoint p, int nbPointsInter)
	{
		/*
		 * On doit ajouter les points intermediaires et tracer le segment On trace chaque segment intermediaire separement, pour plus de precision dans l'affichage ( sinon certains points intermediaires ne sont pas exactement sur le segment reliant deux points editables )
		 */
		HoloPoint first = points.firstElement();
		int date = first.date;
		int dt = (int) ((float)(first.date-p.date) / (nbPointsInter + 1));
		if(dt != 0)
		{
			float x = first.x;
			float y = first.y;
			float z = first.z;
			float dx = (x - p.x) / (nbPointsInter + 1);
			float dy = (y - p.y) / (nbPointsInter + 1);
			float dz = (z - p.z) / (nbPointsInter + 1);
			for (int i = nbPointsInter; i >= 1 ; --i)
			{
				x -= dx;
				y -= dy;
				z -= dz;
				date -= dt;
				points.insertElementAt(new HoloPoint(x, y, z, date, false),0);
			}
		}
		points.insertElementAt(p,0);
		setDirty(true);
	}
	
	public void insertPointAt(CurvePoint p, int param)
	{
		int date = (int)p.getDate();
		int prev = previousPoint2(date);
		int next = nextPoint(date);
		HoloPoint tmp = HoloPoint.interpol(points.get(prev),points.get(next),date);
		switch(param)
		{
		case 0 :
			tmp.x = HoloPoint.limit2D(p.getVal());
			break;
		case 1 :
			tmp.y = HoloPoint.limit2D(p.getVal());
			break;
		case 2 :
			tmp.z = HoloPoint.limitZ(p.getVal());
			break;
		case 3 :
			tmp.setAD(tmp.getTheta(),p.getVal());
//			float r = p.getVal();
//			float d = (float)Math.sqrt(Math.pow(r,2)/2);
//			tmp.x = HoloPoint.limit2D(d);
//			tmp.y = HoloPoint.limit2D(d);
			break;
		case 4 :
			//float t = (float)(p.getVal()/180*Math.PI);
			//float ray = 50;
			tmp.setAD(p.getVal(),tmp.getRay());
			//Ut.post("curvpoint "+p.getVal());
//			tmp.x = HoloPoint.limit2D(ray * (float)Math.cos(t));
//			tmp.y = HoloPoint.limit2D(ray * (float)Math.sin(t));
			break;
		default :
			break;
		}
		points.insertElementAt(tmp,next);
		setDirty(true);
	}
	
	public void insertPointAfter(int date, int ptIndex)
	{
		HoloPoint tmp = HoloPoint.interpol(points.get(ptIndex),points.get(ptIndex+1),date);
		if(tmp != null)
			points.insertElementAt(tmp,ptIndex+1);
		setDirty(true);
	}
	
	public void insertEditablePointAfter(int date, int ptIndex)
	{
		HoloPoint p1 = points.get(ptIndex);
		HoloPoint p2 = points.get(ptIndex+1);
		
		if(date < p1.date || date > p2.date)
			date = (p1.date + p2.date) / 2;
		
		HoloPoint tmp = HoloPoint.interpol(p1,p2,date);
		if(tmp != null)
		{
			points.insertElementAt(tmp,ptIndex+1);
			points.elementAt(ptIndex+1).setEditable(true);
		}
		setDirty(true);
	}
	
	/** **************** EDITION DU SEGMENT-TRAJET ******************* */
	// MODIFICATION DEBUT FIN, COUPER, ROGNER, DEPLACER, STRETCHER, REECHANTILLONNER
	
	/** on enleve tous les points jusqu'à dateBegin*/
	public boolean cutBegin(int dateBegin)
	{
//		System.out.println("cutBegin : "+dateBegin+ " ___ "+getFirstDate()+" "+getLastDate());
		if (dateBegin > getFirstDate() && dateBegin < getLastDate())
		{
//			Main.print("\t\t\tdateBegin > getFirstDate() && dateBegin < getLastDate()");
			HoloPoint first = null;
			int next = nextPoint(dateBegin);
			int prev = previousPoint(dateBegin);
			if(prev == next)
			{
				first = points.get(prev).dupliquer();
			}
			else 
				first = HoloPoint.interpol(points.get(prev),points.get(next),dateBegin);
			
			first.setEditable(true);
			
			while (!points.isEmpty() && points.firstElement().date <= dateBegin)
			{
				points.remove(0);
//				System.out.print(".");
			}
//			System.out.println();
			points.insertElementAt(first,0);
		} else if(dateBegin >= getLastDate())
		{
//			Main.print("\t\t\tdateBegin >= getLastDate()");
			points.clear();
			return true;
		}
		setDirty(true);
		return isEmpty();	
	}

	/** on enleve tous les points à partir de dateEnd */
	public boolean cutEnd(int dateEnd)
	{
//		System.out.println("cutEnd : "+dateEnd+ " ___ "+getFirstDate()+" "+getLastDate());
		if (dateEnd < getLastDate() && dateEnd > getFirstDate())
		{
//			Main.print("\t\t\tdateEnd < getLastDate() && dateEnd > getFirstDate()");
			HoloPoint last = null;
			int next = nextPoint(dateEnd);
			int prev = previousPoint(dateEnd);
			if(prev == next)
			{
				last = points.get(prev).dupliquer();
			}
			else 
				last = HoloPoint.interpol(points.get(prev),points.get(next),dateEnd);
			
			last.setEditable(true);
			while (!points.isEmpty() && points.lastElement().date >= dateEnd)
			{
				points.remove(points.lastElement());
//				System.out.print(".");
			}
//			System.out.println();
			points.add(last);
		} else if(dateEnd <= getFirstDate())
		{
//			Main.print("\t\t\tdateEnd <= getFirstDate()");
			points.clear();
			return true;
		}
		setDirty(true);
		return isEmpty();
	}

	/** on enleve tous les points jusqu'à dateBegin & à partir de dateEnd */
	public void crop(int dateBegin, int dateEnd)
	{
//		Main.print("\tcropSeq: "+dateBegin+" "+dateEnd+" ___ seq:"+getFirstDate()+" "+getLastDate());
		if (dateBegin < dateEnd)
		{
			cutBegin(dateBegin);
			cutEnd(dateEnd);
		}
		setDirty(true);
	}

	/** on enleve tous les points entre dateBegin & dateEnd on retourne la fin du trajet. */
	public HoloTraj cut(int dateBegin, int dateEnd)
	{
//		Main.print("\tcutSeq: "+dateBegin+" "+dateEnd+" ___ seq:"+getFirstDate()+" "+getLastDate());
		boolean rest = false;
		HoloTraj tmp = dupliquer();
		if (dateBegin <= dateEnd)
		{
			cutEnd(dateBegin);
			rest = tmp.cutBegin(dateEnd);
		}
		setDirty(true);
		return rest ? null : tmp;
	}

	public HoloTraj cutAt(int date)
	{
		return cut(date-1, date);
	}

	public void moveBegin(int dateBegin)
	{
//		System.out.println("moveBegin :"+dateBegin+ " ___ "+getFirstDate()+" "+getLastDate());
		if (dateBegin < getLastDate())
			stretchDates(getFirstDate(), getLastDate(), dateBegin, getLastDate());
		else
		{
			autoreverse();
			stretchDates(getFirstDate(), getLastDate(), getLastDate(), dateBegin);
		}
		setDirty(true);
	}

	public void moveEnd(int dateEnd)
	{
//		System.out.println("moveEnd :"+dateEnd+ " ___ "+getFirstDate()+" "+getLastDate());
		if (dateEnd > getFirstDate())
			stretchDates(getFirstDate(), getLastDate(), getFirstDate(), dateEnd);
		else
		{
			autoreverse();
			stretchDates(getFirstDate(), getLastDate(), dateEnd, getFirstDate());
		}
		setDirty(true);
	}

	/** déplacement de tous les points à la date de départ dateBegin */
	public void move(int dateBegin)
	{
		if (dateBegin > 0)
			shiftDates(dateBegin - getFirstDate());
		setDirty(true);
	}

	public HoloTraj reverse()
	{
		if (points.isEmpty())
			return null;
		HoloTraj tmp = new HoloTraj();
		int endDate = getLastDate();
		for (int i = points.size() - 1; i >= 0; i--)
		{
			HoloPoint hp = points.elementAt(i);
			hp.date = endDate - hp.date;
			tmp.add(hp);
		}
		setDirty(true);
		return tmp;
	}

	public void autoreverse()
	{
		if (points.isEmpty())
			return;
		int begDate = getFirstDate();
		int endDate = getLastDate();
		for (int i = points.size() - 1; i >= 0; i--)
		{
			HoloPoint hp = points.elementAt(i);
			hp.date = endDate - (hp.date - begDate);
		}
		points.sort();
		setDirty(true);
	}
	
	/** Decalages de tous les points d'une duree shift */
	public void shiftDates(int shift)
	{
		if (!points.isEmpty())
			for (HoloPoint p : points)
				p.shiftDate(shift);
		setDirty(true);
	}

	/** Stretching de tous les points de l'intervalle [oldBegin,oldEnd] vers [newBegin,newEnd] */
	private void stretchDates(int oldBegin, int oldEnd, int newBegin, int newEnd)
	{
//		System.out.println("stretchDates : old : ["+oldBegin+":"+oldEnd+"], new : ["+newBegin+":"+newEnd+"]");
		if (!points.isEmpty())
		{
			float oldDur = oldEnd - oldBegin;
			float newDur = newEnd - newBegin;
			float fact = newDur / oldDur;
			for (HoloPoint p : points)
				p.date = newBegin + (int) ((p.date - oldBegin) * fact);
		}
		setDirty(true);
	}

	public void resample(double nbPtsPerSec)
	{
		if (points.size() < 2)
			return;
		int dateB = getFirstDate();
		double length = (double) getDuration() / 1000;
		int nbPts = (int) (length * nbPtsPerSec);
		double durPt = 1 / nbPtsPerSec;
		HoloPointVector finalPts = new HoloPointVector(nbPts + 1, 5);
		for (int i = 0; i <= nbPts; i++)
		{
			int date = dateB + (int) (i * durPt * 1000);
			HoloPoint hp1 = points.get(previousPoint2(date));
			HoloPoint hp2 = points.get(nextPoint2(date));
			HoloPoint hp = HoloPoint.interpol(hp1, hp2, date);
			finalPts.add(hp);
		}
		points = finalPts;
		setDirty(true);
	}

	public void join(HoloTraj ht2)
	{
		if(ht2.isEmpty())
			return;
		if(ht2.getLastDate() >= getFirstDate())
		{
			if(ht2.getFirstDate() < getFirstDate())
			{
				if(ht2.getLastDate() > getLastDate())
				{
					HoloTraj rest = ht2.cut(getFirstDate(),getLastDate());
					points.addAll(ht2.points);
					points.addAll(rest.points);
					
				} else {
					ht2.cutAt(getFirstDate());
					points.addAll(ht2.points);
				}
			}
		} else {
			points.addAll(ht2.points);
		}
		
		points.sort();
		setDirty(true);
	}
	
	/* *************** DEPLACEMENT *************** */
	/** DEPLACEMENT ROOM MULTISELECTION SELMODE=TRUE */
	public void calcNewPosSelection(int first, int last, HoloPoint selPos, HoloPoint newPos)
	{
		float dx, dy, fact;
		HoloPoint nthPt;
		dx = newPos.x - selPos.x;
		dy = newPos.y - selPos.y;
		// deplacement des points appartenant a la selection
		for (int nth = first ; nth <= last ; nth++)
		{
			nthPt = elementAtReal(nth);
			nthPt.translater(dx, dy);
		}
		// traitement du segment precedent la selection
		int prev = prevEditPoint(first);
		for (int nth = prev + 1; nth < first ; nth++)
		{
			nthPt = elementAtReal(nth);
			fact = (float) (nth - prev) / (float) (first - prev);
			nthPt.translater(dx * fact, dy * fact);
		}
		// traitement du segment suivant la selection
		int next = nextEditPoint(last);
		for (int nth = last + 1; nth < next ; nth++)
		{
			nthPt = elementAtReal(nth);
			fact = 1 - ((float) (nth - last) / (float) (next - last));
			nthPt.translater(dx * fact, dy * fact);
		}
		setDirty(true);
	}
	
	/** DEPLACEMENT ROOM MULTISELECTION SELMODE=TRUE */
	public void calcNewPosSelectionZ(int first, int last, float dz)
	{
		float fact;
		HoloPoint nthPt;
		// deplacement des points appartenant a la selection
		for (int nth = first ; nth <= last ; nth++)
		{
			nthPt = elementAtReal(nth);
			nthPt.translaterZ(dz);
		}
		// traitement du segment precedent la selection
		int prev = prevEditPoint(first);
		for (int nth = prev + 1; nth < first ; nth++)
		{
			nthPt = elementAtReal(nth);
			fact = (float) (nth - prev) / (float) (first - prev);
			nthPt.translaterZ(dz * fact);
		}
		// traitement du segment suivant la selection
		int next = nextEditPoint(last);
		for (int nth = last + 1; nth < next ; nth++)
		{
			nthPt = elementAtReal(nth);
			fact = 1 - ((float) (nth - last) / (float) (next - last));
			nthPt.translaterZ(dz * fact);
		}
		setDirty(true);
	}
	
	/** DEPLACEMENT POINT EDITABLE SEUL */
	public void calcNewPosSeg(int ptNum, HoloPoint newPos)
	{
		int prev, actual, next;
		float dx, dy, fact;
		actual = ptNum; // n∞ point editable central
		HoloPoint actualPt, nthPt;
		actualPt = elementAtReal(actual);
		prev = prevEditPoint(ptNum); // n∞ point editable precedent
		prev = prev != -1 ? prev : actual;
		next = nextEditPoint(ptNum); // n∞ point editable suivant
		next = next != -1 ? next : actual;
		dx = newPos.x - actualPt.x;
		dy = newPos.y - actualPt.y;
		for (int nth = prev; nth <= next; nth++)
		{
			nthPt = elementAtReal(nth);
			if (nth == actual)
				fact = 1;
			else if (nth < actual)
				fact = (float) (nth - prev) / (float) (actual - prev);
			else
				fact = (float) (nth - next) / (float) (actual - next);
			nthPt.translater(dx * fact, dy * fact);
		}
		setDirty(true);
	}

	/** DEPLACEMENT POINT EDITABLE MULTI SELECTED SELMODE=FALSE */
	public void calcNewPosSeg(int ptNum, float dx, float dy)
	{
		int prev, next;
		float fact;
		HoloPoint nthPt;
		prev = prevEditPoint(ptNum); // n∞ point editable precedent
		prev = prev != -1 ? prev : ptNum;
		next = nextEditPoint(ptNum); // n∞ point editable suivant
		next = next != -1 ? next : ptNum;
		for (int nth = prev; nth <= next; nth++)
		{
			nthPt = elementAtReal(nth);
			if (nth == ptNum)
				fact = 1;
			else if (nth < ptNum)
				fact = (float) (nth - prev) / (float) (ptNum - prev);
			else
				fact = (float) (nth - next) / (float) (ptNum - next);
			nthPt.translater(dx * fact, dy * fact);
		}
		setDirty(true);
	}

	/** DEPLACEMENT XYZ FROM TEXT POINT EDITOR */
	public void calcNewPosSegXYZ(int ptNum, float x, float y, float z)
	{
		int prev, actual, next;
		float dx, dy, dz, fact;
		HoloPoint actualPt, nthPt;
		actual = ptNum; // n∞ point editable central
		actualPt = elementAtReal(actual);
		prev = prevEditPoint(ptNum); // n∞ point editable precedent
		prev = prev != -1 ? prev : actual;
		next = nextEditPoint(ptNum); // n∞ point editable suivant
		next = next != -1 ? next : actual;
		dx = x - actualPt.x;
		dy = y - actualPt.y;
		dz = z - actualPt.z;
		for (int nth = prev; nth <= next; nth++)
		{
			nthPt = elementAtReal(nth);
			if (nth == actual)
				fact = 1;
			else if (nth < actual)
				fact = (float) (nth - prev) / (float) (actual - prev);
			else
				fact = (float) (nth - next) / (float) (actual - next);
			nthPt.translater(dx * fact, dy * fact, dz * fact);
		}
		setDirty(true);
	}
	
	/** DEPLACEMENT EN Z D'UN POINT EDITABLE */
	public void calcNewPosSegZ(int ptNum, double dZ)
	{
		if (dZ >= -100 && dZ != 0 && dZ <= 100)
		{
			int prev, actual, next;
			float fact;
			HoloPoint actualPt, nthPt;
			actual = ptNum; // n∞ point editable central
			actualPt = elementAtReal(actual);
			prev = prevEditPoint(ptNum); // n∞ point editable precedent
			prev = prev != -1 ? prev : actual;
			next = nextEditPoint(ptNum); // n∞ point editable suivant
			next = next != -1 ? next : actual;
			if (((actualPt.z + dZ) > 0) && ((actualPt.z + dZ)) < 100)
			{
				for (int nth = prev; nth <= next; nth++)
				{
					nthPt = elementAtReal(nth);
					if (nth == actual)
						fact = 1;
					else if (nth < actual)
						fact = (float) (nth - prev) / (float) (actual - prev);
					else
						fact = (float) (nth - next) / (float) (actual - next);
					nthPt.translater(0, 0, (float) dZ * fact);
				}
			} else if (actualPt.z + dZ > 100)
			{
				dZ = 100 - actualPt.z;
				for (int nth = prev; nth <= next; nth++)
				{
					nthPt = elementAtReal(nth);
					if (nth == actual)
						fact = 1;
					else if (nth < actual)
						fact = (float) (nth - prev) / (float) (actual - prev);
					else
						fact = (float) (nth - next) / (float) (actual - next);
					nthPt.translater(0, 0, (float) dZ * fact);
				}
			} else if (actualPt.z + dZ < 0)
			{
				dZ = 0 - actualPt.z;
				for (int nth = prev; nth <= next; nth++)
				{
					nthPt = elementAtReal(nth);
					if (nth == actual)
						fact = 1;
					else if (nth < actual)
						fact = (float) (nth - prev) / (float) (actual - prev);
					else
						fact = (float) (nth - next) / (float) (actual - next);
					nthPt.translater(0, 0, (float) dZ * fact);
				}
			}
		}
		setDirty(true);
	}

	/* **************** AFFICHAGE DU SEGMENT-TRAJET ******************* */
	/** DRAW ROOM RENDER PROJ */
	public HoloPoint drawRoomRenderProj(GL gl, boolean onlyEditable, int begin, int end, int tkNum, int seqNum, int selIndex, Vector<Integer> selIndexes, float[] color, HoloPoint lastDrawn)
	{
		if (points.isEmpty())
			return lastDrawn;
		if (begin > this.getLastDate() || end < this.getFirstDate())
			return lastDrawn;
		
		
		if (roomGUIdirty)
		{
			GLlistIDroom = gl.glGenLists(1);
			gl.glNewList(GLlistIDroom, GL.GL_COMPILE);
		
			// Dessin du segment entre les points
			gl.glLineWidth(trajLineWidth);
			gl.glBegin(GL.GL_LINE_STRIP);
			for (int i = 0 ; i < points.size() ; i++)
			{	
				HoloPoint p = points.get(i);
				if (p.date >= begin && p.date <= end)
					p.drawRoomProj(gl);
			}
			gl.glEnd();
			
			// Dessin des points editables
			gl.glPointSize(6);
			gl.glBegin(GL.GL_POINTS);
			for (int i = 0; i < points.size(); i++)
			{
				HoloPoint p = points.get(i);
				if (p.date >= begin && p.date <= end && p.isEditable())
					p.drawRoomProj(gl);
			}
			gl.glEnd();
			
			// Dessin des points non editables
			if(!onlyEditable)
			{
				gl.glPointSize(4);
				gl.glBegin(GL.GL_POINTS);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
					if (p.date >= begin && p.date <= end && !p.isEditable())
						p.drawRoomProj(gl);
				}
				gl.glEnd();
			}
			
			if(selIndex != RoomIndex.getNull() || !selIndexes.isEmpty())
			{
				//if(selIndex)
				// Dessin des points editable selectionnés
				float tmp = color[3];
				color[3] = 0.5f;
				gl.glColor4fv(color,0);
				
				gl.glPointSize(10);
				gl.glBegin(GL.GL_POINTS);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
					if (p.date >= begin && p.date <= end && p.isEditable())
						p.drawRoomProjSelected(gl, tkNum, seqNum, i, selIndex, selIndexes, color);
				}
				gl.glEnd();
				
				// Dessin des points non editable selectionnés
				if(!onlyEditable)
				{
					gl.glPointSize(6);
					gl.glBegin(GL.GL_POINTS);
					for (int i = 0; i < points.size(); i++)
					{
						HoloPoint p = points.get(i);
						if (p.date >= begin && p.date <= end && !p.isEditable())
							p.drawRoomProjSelected(gl, tkNum, seqNum, i, selIndex, selIndexes, color);
					}
					gl.glEnd();
					
					color[3] = tmp;
					gl.glColor4fv(color,0);
				}
			}
			
			// first & last
			gl.glPointSize(8);
			if (begin < this.getFirstDate())
			{
				HoloPoint f = points.firstElement();
				gl.glBegin(GL.GL_POINTS);
				f.drawRoomProj(gl);
				gl.glEnd();
			}
			if(end > this.getLastDate())
			{
				HoloPoint l = points.lastElement();
				
				gl.glBegin(GL.GL_POINTS);
				l.drawRoomProj(gl);
				gl.glEnd();
				gl.glPointSize(4);
				gl.glBegin(GL.GL_POINTS);
				gl.glColor4f(1, 1, 1, 1);
				l.drawRoomProj(gl);
				gl.glEnd();
			}
			
			gl.glEndList();
			
			roomGUIdirty = false;
		}
		
		//gl.glPushMatrix();
		gl.glCallList(GLlistIDroom);
		//gl.glPopMatrix();
		
		return lastDrawn;
	}
	
	/** DRAW ROOM RENDER 3D */
	public void drawRoomRender3D(GL gl, boolean onlyEditable, int begin, int end)
	{
		if (points.isEmpty())
			return;
		if (begin > this.getLastDate() || end < this.getFirstDate())
			return;
		
		// Dessin du segment entre les points
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINE_STRIP);
			for (int i = 0; i < points.size(); i++)
			{
				HoloPoint p = points.get(i);
				if(p.date >= begin && p.date <= end)
					p.drawRoom3d(gl);
			}
		gl.glEnd();
		
		float[] color = new float[4];
		gl.glGetFloatv(GL.GL_CURRENT_COLOR,color,0);


		// Dessin des points éditables
		gl.glPointSize(6);
		gl.glBegin(GL.GL_POINTS);
			for (int i = 0; i < points.size(); i++)
			{
				HoloPoint p = points.get(i);
				if(p.date >= begin && p.date <= end && p.isEditable())
					p.drawRoom3d(gl);
			}
		gl.glEnd();
		
		// Dessin des points non éditables
		if(!onlyEditable)
		{
			gl.glPointSize(4);
			gl.glBegin(GL.GL_POINTS);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
						if(p.date >= begin && p.date <= end && !p.isEditable())
							p.drawRoom3d(gl);
				}
			gl.glEnd();
		}
		
		// first & last
		HoloPoint f = points.firstElement();
		HoloPoint l = points.lastElement();
		boolean b = false;
		gl.glPointSize(8);
		gl.glBegin(GL.GL_POINTS);
		if(f.date >= begin && f.date <= end)
			f.drawRoom3d(gl);
		if(l.date >= begin && l.date <= end)
		{
			l.drawRoom3d(gl);
			b = true;
		}
		gl.glEnd();
		gl.glPointSize(4);
		gl.glBegin(GL.GL_POINTS);
		gl.glColor4f(1, 1, 1, 1);
		if(b)
			l.drawRoom3d(gl);
		gl.glEnd();
	}
	
	/** DRAW ROOM SELECT */
	public void drawRoomSelectProj(GL gl, boolean onlyEditable, int begin, int end, int tkNum, int seqNum)
	{
		if (points.isEmpty())
			return;
		if (begin > this.getLastDate() || end < this.getFirstDate())
			return;
		
		// Segment entre les points
		HoloPoint p1, p2;
		for (int i = 0 ; i < points.size()-1 ; i++)
		{	
			p1 = points.get(i);
			p2 = points.get(i+1);
			if((p1.date >= begin && p1.date <= end) && (p2.date >= begin && p2.date <= end))
			{
				int index = RoomIndex.encode(RoomIndex.TYPE_LINE,tkNum,seqNum,i);
				gl.glLoadName(index);
				gl.glBegin(GL.GL_LINES);
					p1.drawRoomProj(gl);
					p2.drawRoomProj(gl);
				gl.glEnd();
			}
		}
		
		// Dessin des points
		
		for (int i = 0 ; i < points.size() ; i++)
		{	
			HoloPoint p = points.get(i);
			if(p.date >= begin && p.date <= end && (p.isEditable() || !onlyEditable))
				p.drawRoomProjSel(gl, tkNum, seqNum, i);
		}
		
	}
	
	/** DRAW ROOM MULTI SELECT */
	public void drawRoomMultiSelectProj(GL gl, boolean onlyEditable, int begin, int end, int tkNum, int seqNum)
	{
		if (points.isEmpty())
			return;
		if (begin > this.getLastDate() || end < this.getFirstDate())
			return;
		
		// Dessin des points
		for (int i = 0 ; i < points.size() ; i++)
		{	
			HoloPoint p = points.get(i);
			if(p.date >= begin && p.date <= end && (p.isEditable() || !onlyEditable))
				p.drawRoomProjSel(gl, tkNum, seqNum, i);
		}
	}

	public void drawScore(GL gl, Color c, int begin, int end, boolean render, int tkNum, int seqNum, int selIndex, Vector<HoloTraj> selSeqs, boolean nosmoothenabled, int selMode)
	{
		if (points.isEmpty())
			return;
		if (begin > this.getLastDate() || end < this.getFirstDate())
			return;
		int first = getFirstDate();
		int last = getLastDate();
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 0.05f;
		// POLY
		int index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.SEQ_POLY);
		if(selMode != 2)
			gl.glLoadName(index);
		if(index == selIndex)
			cc[3] = 0.1f;
		else if(selSeqs.contains(this))
			cc[3] = 0.2f;
		else
			cc[3] = 0.05f;
		OpenGLUt.glColor(gl, cc);
		gl.glRectf(first, 0, last, 1);
		
		// BEGIN
		gl.glLineWidth(2);
		index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.SEQ_BEGIN);
		gl.glLoadName(index);
		if(index == selIndex)
			cc[3] = 1;
		else
			cc[3] = 0f;
		OpenGLUt.glColor(gl, cc);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(first,0);
			gl.glVertex2f(first,1);
		gl.glEnd();
		
		// END
		index = ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,seqNum,ScoreIndex.SEQ_END);
		gl.glLoadName(index);
		if(index == selIndex)
			cc[3] = 1;
		else
			cc[3] = 0f;
		OpenGLUt.glColor(gl, cc);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(last,0);
			gl.glVertex2f(last,1);
		gl.glEnd();
		if(render)
		{
			if (scoreGUIdirty)
			{
				GLlistIDscore = gl.glGenLists(1);
				gl.glNewList(GLlistIDscore, GL.GL_COMPILE);
				gl.glLineWidth(1);
				OpenGLUt.glColor(gl, c.darker());
				gl.glPushMatrix();
				gl.glTranslatef(0, 0.5f, 0);
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
					OpenGLUt.drawPoint(gl, p.date, Ut.clip(p.x / 100 , -0.5f, 0.5f));
				}
				gl.glEnd();
				OpenGLUt.glColor(gl, c);
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
					OpenGLUt.drawPoint(gl, p.date, Ut.clip(p.y / 100, -0.5f, 0.5f));
				}
				gl.glEnd();
				OpenGLUt.glColor(gl, c.brighter());
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int i = 0; i < points.size(); i++)
				{
					HoloPoint p = points.get(i);
					OpenGLUt.drawPoint(gl, p.date, Ut.clip(p.z / 100 , -0.5f, 0.5f));
				}
				gl.glEnd();
				gl.glPopMatrix();
				OpenGLUt.glColor(gl, Color.BLACK);
				if (nosmoothenabled)
					gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glRectf(getFirstDate(), 0, getLastDate(), 1);
				if (nosmoothenabled)
					gl.glEnable(GL.GL_LINE_SMOOTH);
				
				gl.glEndList();
				
				scoreGUIdirty = false;
				
			}
			gl.glPushMatrix();
			gl.glCallList(GLlistIDscore);
			gl.glPopMatrix();
		}
	}
	
	public void drawScoreSquare(GL gl, Color c, int begin, int end, float dX)
	{
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		gl.glLineWidth(2);
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 1;
		OpenGLUt.glColor(gl, cc);
		gl.glRectf(getFirstDate()+dX, 0, getLastDate()+dX, 1);
		gl.glLineWidth(1);
	}
	
	//TODO : Optim TimeEditor
	/** DRAW TIMEEDITOR **/
	/*public void drawTimeEditor(GL gl, boolean render, boolean query_one_select, int curveType, int tkNum, int seqNum, int curveNum, Color tkColor, int selected, int selMode, Vector<Integer> selIndex, boolean activeTrack)
	{
		if(timeEdGUIdirty)
		{
			float[] cBg = OpenGLUt.convCol(tkColor);
			float[] cc = cBg.clone();
			float[] ccSel = cc.clone();
			ccSel[3] = 0.5f;
			cBg[3] = 0.3f;
			gl.glColor4fv(cBg, 0);
			// Track inactive
			GLlistIDTimeXoff = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeXoff, GL.GL_COMPILE);
			
			if (!isEmpty())
			{
				HoloPoint p1, p2;
				int ll = 0;
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					ll = l + 1;
					p1.drawX(gl);
				}
				p2 = points.get(ll);
				p2.drawX(gl);
				gl.glEnd();
				gl.glPointSize(3);
				gl.glBegin(GL.GL_POINTS);
				firstElement().drawX(gl);
				lastElement().drawX(gl);
				gl.glEnd();
			}
			gl.glEndList();
			
			// Track active
			GLlistIDTimeX = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeX, GL.GL_COMPILE);
			// Segment entre les points
			HoloPoint p1, p2;
			if (render || query_one_select)
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, seqNum, l);
					if (index == selected)
						gl.glLineWidth(2);
					else gl.glLineWidth(1);
					gl.glLoadName(index);
					gl.glBegin(GL.GL_LINES);
					p1.drawT(gl);
					p2.drawT(gl);
					gl.glEnd();
				}
			// Points
			for (int l = 0, last3 = j.points.size(); l < last3; l++)
			{
				HoloPoint p = points.get(l);
				if (p.isEditable() || !mainRef.viewOnlyEditablePoints)
				{
					int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, seqNum, l);
					if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
					{
						gl.glPointSize(p.isEditable() ? 10 : 6);
						OpenGLUt.glColor(gl, ccSel);
						gl.glBegin(GL.GL_POINTS);
						p.drawT(gl);
						gl.glEnd();
						OpenGLUt.glColor(gl, cc);
					}
					gl.glPointSize(p.isEditable() ? 5 : 3);
					gl.glLoadName(index);
					gl.glBegin(GL.GL_POINTS);
					p.drawT(gl);
					gl.glEnd();
				}
			}
		
			gl.glEndList();
			
			timeEdGUIdirty = false;
		}
		if(activeTrack)
			gl.glCallList(GLlistIDTimeX);
		else
			gl.glCallList(GLlistIDTimeXoff);
	}*/
	
	public void drawTimeEditorY(GL gl)
	{
		if(timeEdGUIdirty)
		{
			GLlistIDTimeY = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeY, GL.GL_COMPILE);
			
			if (!isEmpty())
			{
				HoloPoint p1, p2;
				int ll = 0;
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					ll = l + 1;
					p1.drawY(gl);
				}
				p2 = points.get(ll);
				p2.drawY(gl);
				gl.glEnd();
				gl.glPointSize(3);
				gl.glBegin(GL.GL_POINTS);
				firstElement().drawY(gl);
				lastElement().drawY(gl);
				gl.glEnd();
			}
			gl.glEndList();
			
			timeEdGUIdirty = false;
		}
		
		gl.glCallList(GLlistIDTimeY);
	}
	
	public void drawTimeEditorZ(GL gl)
	{
		if(timeEdGUIdirty)
		{
			GLlistIDTimeZ = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeZ, GL.GL_COMPILE);
			
			if (!isEmpty())
			{
				HoloPoint p1, p2;
				int ll = 0;
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					ll = l + 1;
					p1.drawZ(gl);
				}
				p2 = points.get(ll);
				p2.drawZ(gl);
				gl.glEnd();
				gl.glPointSize(3);
				gl.glBegin(GL.GL_POINTS);
				firstElement().drawZ(gl);
				lastElement().drawZ(gl);
				gl.glEnd();
			}
			gl.glEndList();
			
			timeEdGUIdirty = false;
		}
		
		gl.glCallList(GLlistIDTimeZ);
	}
	
	public void drawTimeEditorR(GL gl)
	{
		if(timeEdGUIdirty)
		{
			GLlistIDTimeR = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeR, GL.GL_COMPILE);
			
			if (!isEmpty())
			{
				HoloPoint p1, p2;
				int ll = 0;
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					ll = l + 1;
					p1.drawR(gl);
				}
				p2 = points.get(ll);
				p2.drawR(gl);
				gl.glEnd();
				gl.glPointSize(3);
				gl.glBegin(GL.GL_POINTS);
				firstElement().drawR(gl);
				lastElement().drawR(gl);
				gl.glEnd();
			}
			gl.glEndList();
			
			timeEdGUIdirty = false;
		}
		
		gl.glCallList(GLlistIDTimeR);
	}
	
	public void drawTimeEditorT(GL gl)
	{
		if(timeEdGUIdirty)
		{
			GLlistIDTimeT = gl.glGenLists(1);
			gl.glNewList(GLlistIDTimeT, GL.GL_COMPILE);
			
			if (!isEmpty())
			{
				HoloPoint p1, p2;
				int ll = 0;
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int l = 0, last3 = points.size() - 1; l < last3; l++)
				{
					p1 = points.get(l);
					p2 = points.get(l + 1);
					ll = l + 1;
					p1.drawT(gl);
				}
				p2 = points.get(ll);
				p2.drawT(gl);
				gl.glEnd();
				gl.glPointSize(3);
				gl.glBegin(GL.GL_POINTS);
				firstElement().drawT(gl);
				lastElement().drawT(gl);
				gl.glEnd();
			}
			gl.glEndList();
			
			timeEdGUIdirty = false;
		}
		
		gl.glCallList(GLlistIDTimeT);
	}
	
	/** Pour le dessin de traits de repère durant les strech.*/
	public void drawMovedSquare(GL gl, Color c, int begin, int end, float b, float e, boolean type)
	{
		int first = getFirstDate()+(int)b;
		int last = getLastDate()+(int)e;
		// BEGIN
		gl.glLineWidth(2);
		float[] cc = OpenGLUt.convCol(c);
		cc[3] = 1;
		OpenGLUt.glColor(gl, cc);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(first,0);
			gl.glVertex2f(first,1);
			gl.glVertex2f(last,0);
			gl.glVertex2f(last,1);
		gl.glEnd();
		if(!type)
		{
			gl.glLineWidth(1.5f);
			gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(first,0.5f);
				gl.glVertex2f(last,0.5f);
			gl.glEnd();
		} else {
			gl.glLineWidth(1);
			gl.glBegin(GL.GL_LINES);
				if(b > 0) {
					gl.glVertex2f(getFirstDate(),1);
					gl.glVertex2f(first,0);
					gl.glVertex2f(getFirstDate(),0);
					gl.glVertex2f(first,1);
				} else if(e < 0) {
					gl.glVertex2f(getLastDate(),1);
					gl.glVertex2f(last,0);
					gl.glVertex2f(getLastDate(),0);
					gl.glVertex2f(last,1);
				}
			gl.glEnd();
		}
		gl.glLineWidth(1);
	}
	
	public HoloPoint getPointAt(int counter)
	{
		// TESTING
		currentRead = previousPoint2(counter);
		if(currentRead != -1)
			return points.get(currentRead);
		return null;
		//return points.get(0);
	}
	
	public HoloPoint getLastPointAt(int counter)
	{
		int lastReadInd = previousPoint2(counter);
		if(lastReadInd != -1)
			return points.get(lastReadInd);
		return null;
	}
	
	public void updatePointNext()
	{
		HoloPoint p1,p2;
		if(points.size() == 0)
			return;
		p1 = points.elementAt(0);
		p1.nextpoint = null;
		p1.prevpoint = null;
		
		for(int i=1; i<points.size() ; i++)
		{
			p2 = points.elementAt(i);
			p1.nextpoint = p2;
			p2.prevpoint = p1;
			p2.nextpoint = null;
			p1 = p2;
		}
	}
	
	public void setPlaying(boolean v)
	{
		isPlaying = v;
	}
	
	public void stop()
	{
		currentRead = -1;
	}
}
