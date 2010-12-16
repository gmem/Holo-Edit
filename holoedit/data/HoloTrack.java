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
 * La nouvelle piste contient un vecteur d'HoloTraj
 * ainsi qu'un vecteur d'HoloWaveForm qui sont respectivement
 * les trajets et les formes d'ondes
 */
package holoedit.data;

import holoedit.opengl.OpenGLUt;
import holoedit.opengl.RoomIndex;
import holoedit.opengl.ScoreIndex;
import holoedit.opengl.TimeIndex;
import holoedit.util.Ut;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import javax.media.opengl.GL;
import com.sun.opengl.util.GLUT;

// FEATURE HOLOTRACK EVENTTRACK
public class HoloTrack
{
	// taille des points
	private final int demiTaillePetitPoint = 1;
	private final int taillePetitPoint = demiTaillePetitPoint * 2 + 1;
	private final int demiTailleGrosPoint = 2;
	private final int tailleGrosPoint = demiTailleGrosPoint * 2 + 1;
	// numero de la piste
	protected int number = -1;
	// liste de trajets
	public HoloTrajVector trajs;
	// liste d'ID des formes d'ondes dans soundpool+begtime
	public WaveFormInstanceVector waves;
	// liste d'ID des external datas dans externalDataPool+begtime
	public SDIFdataInstanceVector sdifdataInstanceVector;
	// couleur affichee
	protected Color color = Color.darkGray;
	// piste visible ou non
	protected boolean visible;
	// nom de la piste
	protected String name = "unnamed";
	// cadenas
	protected boolean locked = false;
	// record enable
	protected boolean recEnable = false;
	private float[] lockedTrackColor = {0.6f,0.6f,0.6f,0.6f};
	private float[] selZoneColor = { 0.5f, 0.5f, 1, 0.1f };
	private float[] selZoneBorderColor = { 0, 0, 0.5f, 0.1f };
	private float[] cursorColor = { 0, 0, 0, 0.5f };
	public HoloPoint lastRead;
	// playing traverser
	public int nextDate;
	public int nextTrajIndex;
	public int nextPointIndex;
	// time indexed tree set for playing
	public TreeMap<Integer, HoloPoint> sequence;
	public SortedMap<Integer, HoloPoint> subsequence;
	// played point queue
	public int[] playedPointsDate;
	int playedIndex = 0;
	boolean playing = false;


	/** ******************* Constructeurs ******************* */
	public HoloTrack()
	{
		trajs = new HoloTrajVector(1, 2);
		waves = new WaveFormInstanceVector(1, 1);
		sdifdataInstanceVector = new SDIFdataInstanceVector(1, 1);
		sequence = new TreeMap<Integer, HoloPoint>();
		playedPointsDate = new int[Ut.drawPtsNb];
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
	}

	public HoloTrack(int numero, Color couleur)
	{
		this.color = couleur;
		this.number = numero;
		this.name = "Track " + number;
		this.visible = false;
		// capacite initiale = 1 ; increment = 2
		trajs = new HoloTrajVector(1, 2);
		waves = new WaveFormInstanceVector(1, 1);
		sdifdataInstanceVector = new SDIFdataInstanceVector(1, 1);
		sequence = new TreeMap<Integer, HoloPoint>();
		playedPointsDate = new int[Ut.drawPtsNb];
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
	}

	public HoloTrack(int n, Color c, boolean v)
	{
		color = c;
		number = n;
		name = "Track " + number;
		visible = v;
		// capacite initiale = 1 ; increment = 2
		trajs = new HoloTrajVector(1, 2);
		waves = new WaveFormInstanceVector(1, 1);
		sdifdataInstanceVector = new SDIFdataInstanceVector(1, 1);
		sequence = new TreeMap<Integer, HoloPoint>();
		playedPointsDate = new int[Ut.drawPtsNb];
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
	}
	
	public HoloTrack(int n, String na, boolean v, boolean l, int rgb)
	{
		number = n;
		color = new Color(rgb);
		name = na;
		visible = v;
		locked = l;
		// capacite initiale = 1 ; increment = 2
		trajs = new HoloTrajVector(1, 2);
		waves = new WaveFormInstanceVector(1, 1);
		sdifdataInstanceVector = new SDIFdataInstanceVector(1, 1);
		sequence = new TreeMap<Integer, HoloPoint>();
		playedPointsDate = new int[Ut.drawPtsNb];
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
	}

	// dupliquer une piste
	public HoloTrack dupliquer()
	{
		HoloTrack newPiste = new HoloTrack(number, color, visible);
		for (HoloTraj p : trajs)
			newPiste.trajs.addElement(p.dupliquer());
		for (WaveFormInstance p : waves)
			newPiste.waves.addElement(p.dupliquer());
		for (SDIFdataInstance s : sdifdataInstanceVector)
			newPiste.sdifdataInstanceVector.addElement(s.dupliquer());
		return newPiste;
	}

	// retourne la piste sous forme de string.
	public String toString()
	{
//		if(isEmpty()) return "";
			
		String tmp = "\t<track number=\"" + number + "\" name=\"" + name + "\" visible=\"" + visible + "\" locked=\"" + locked + "\" color=\""+color.getRGB()+"\">\n";
		if(!trajs.isEmpty())
			for (HoloTraj ht : trajs)
			{
				tmp += ht.toString();
			}
		if(!waves.isEmpty())
			for (WaveFormInstance wfi : waves)
				tmp += wfi.toString();
		if(!sdifdataInstanceVector.isEmpty())
			for (SDIFdataInstance dti : sdifdataInstanceVector)
				tmp += dti.toString2();
		tmp += "\t</track>\n";
		return tmp;
	}

	public void print()
	{
		System.out.println("<track number=\"" + number + "\" name=\"" + name + "\">");
		for(HoloTraj t:trajs)
			t.print();
		System.out.println("</track>");
	}
	
	public boolean init()
	{
		if (!trajs.isEmpty() && !waves.isEmpty() && !sdifdataInstanceVector.isEmpty())
		{
			trajs.clear();
			waves.clear();
			sdifdataInstanceVector.clear();
			if (visible)
				return true;
		}
		if (!waves.isEmpty())
		{
			waves.clear();
			if (visible)
				return true;
		}
		if (!sdifdataInstanceVector.isEmpty())
		{
			sdifdataInstanceVector.clear();
			if (visible)
				return true;
		}
		if (!trajs.isEmpty())
		{
			trajs.clear();
			if (visible)
				return true;
		}
		return false;
	}

	public boolean clear()
	{
		return init();
	}

	/** ************* Changement de parametres ************** */
	// affectation du nom de la piste
	public void setName(String n)
	{
		name = n;
	}

	// recuperation du nom de la piste
	public String getName()
	{
		return name;
	}

	public boolean isEmpty()
	{
		if (trajs.isEmpty() && sdifdataInstanceVector.isEmpty())
			return true;
		boolean found = true;
		for (HoloTraj ht : trajs)
			found = found && ht.isEmpty();
		for (SDIFdataInstance sdif : sdifdataInstanceVector)
			found = found && sdif.isEmpty();
		return found;
	}

	// changer la visibilite de la piste, retourne
	public boolean changeVisible()
	{
		visible = !visible;
		return (!isEmpty());
	}

	public void setVisible(boolean b)
	{
		visible = b;
	}

	public boolean isVisible()
	{
		return visible;
	}

	// renvoie le numero de la piste
	public int getNumber()
	{
		return (number);
	}

	public void setTrajs(HoloTrajVector v)
	{
		trajs = v;
	}
	
	public void setWaves(WaveFormInstanceVector v)
	{
		waves = v;
	}
	
	public void setSDIFs(SDIFdataInstanceVector v)
	{
		sdifdataInstanceVector = v;
	}
	
	public void setDirty(boolean dirty)
	{
		for(HoloTraj tj : trajs)
		{
			tj.setDirty(dirty);
		}
		for(SDIFdataInstance sdif : sdifdataInstanceVector)
		{
			sdif.setDirty(dirty);
		}
	}
	
	public void setDirty(int mask)
	{
		for(HoloTraj tj : trajs)
		{
			tj.setDirty(mask);
		}
		for(SDIFdataInstance sdif : sdifdataInstanceVector)
		{
			sdif.setDirty(mask);
		}
	}
	
	/* ********************** AFFICHAGE ************************* */
	/** affichage de la piste */
	public void afficher(int debut, int fin, Graphics g, float echelle, boolean onlyEditable)
	{
		// OPTIMISER EN APPELLANT LES FONCTIONS DE DESSINS DE HOLOTRAJ ET HOLOWAVEFORM
		if (visible && !isEmpty())
		{
			int nbPoints = size();
			if (nbPoints >= 1)
			{
				g.setColor(color);
				HoloPoint p1, p2;
				// recherche du debut de la periode a afficher
				int i = 1;
				HoloPoint firstTest = elementAt(i);
				if(firstTest != null)
					while ((i < nbPoints) && (elementAt(i).date < debut))
						i++;
				else return;
				if (i < nbPoints)
				{
					// il y a des points a tracer
					// point juste avant le premier a tracer
					p1 = p2 = elementAt(i - 1);
					// on trace les points jusqu'a ce qu'on arrive au bout de la piste ou qu'on atteigne la fin de la periode a afficher
					try
					{
						while ((i < nbPoints) && (elementAt(i).date <= fin))
						{
							p1 = elementAt(i); // point a tracer
							// trace du point
							if (p1.isEditable())
								g.fillRect((int) (p1.x * echelle) - demiTailleGrosPoint, (int) (p1.y * echelle) - demiTailleGrosPoint, tailleGrosPoint, tailleGrosPoint);
							else if (!onlyEditable)
								g.fillRect((int) (p1.x * echelle) - demiTaillePetitPoint, (int) (p1.y * echelle) - demiTaillePetitPoint, taillePetitPoint, taillePetitPoint);
							// trace de la ligne
							g.drawLine((int) (p2.x * echelle), (int) (p2.y * echelle), (int) (p1.x * echelle), (int) (p1.y * echelle));
							p2 = p1;
							i = i + 1;
						}
					}
					catch (Exception e)
					{}
					g.setColor(Color.white);
					g.fillOval((int) ((p1.x * echelle) - tailleGrosPoint), (int) ((p1.y * echelle) - tailleGrosPoint), 2 * tailleGrosPoint + 1, 2 * tailleGrosPoint + 1);
					g.setColor(color);
					g.drawOval((int) ((p1.x * echelle) - tailleGrosPoint), (int) ((p1.y * echelle) - tailleGrosPoint), 2 * tailleGrosPoint + 1, 2 * tailleGrosPoint + 1);
					g.setFont(new Font("Arial", Font.PLAIN, 9));
					g.drawString("" + (number+1), (int) (p1.x * echelle) - demiTaillePetitPoint - 1 - (2 * number / 10), (int) (p1.y * echelle) + tailleGrosPoint - 1);
				}
				// trace du premier point
				p1 = firstElement();
				if (debut <= p1.date && fin >= p1.date)
				{
					g.setColor(Color.white);
					g.fillOval((int) (p1.x * echelle) - demiTaillePetitPoint - 2, (int) (p1.y * echelle) - demiTaillePetitPoint - 2, tailleGrosPoint + 1, tailleGrosPoint + 1);
					g.setColor(color);
					g.setFont(new Font("Arial", Font.PLAIN, 7));
					g.drawString("" + (number+1), (int) (p1.x * echelle) - demiTaillePetitPoint - 1 - (2 * number / 10), (int) (p1.y * echelle) + tailleGrosPoint - 3);
				}
			}
		}
	}

	/** affichage des points a lire */
	public void afficherMidiALire(int debut, int fin, Graphics g, float echelle, int readPointIndex, boolean onlyEditable)
	{
		if (visible && !isEmpty())
		{
			if ((debut < getLastDate()) && fin > getFirstDate())
			{
				int endInd = previousPoint2(fin);
				int begInd = nextPoint2(debut);
				HoloPoint p1, p2;
				if (begInd == 0)
				{
					p2 = elementAt(0);
					p1 = elementAt(1);
					begInd = 1;
				} else
				{
					p1 = elementAt(begInd);
					p2 = elementAt(begInd - 1);
				}
				for (int i = begInd; i <= endInd; i++)
				{
					p1 = elementAt(i);
					g.setColor(color);
					g.drawLine((int) (p2.x * echelle), (int) (p2.y * echelle), (int) (p1.x * echelle), (int) (p1.y * echelle));
					if (i > readPointIndex)
					{
						if (p1.isEditable())
							g.fillRect((int) (p1.x * echelle) - demiTailleGrosPoint, (int) (p1.y * echelle) - demiTailleGrosPoint, tailleGrosPoint, tailleGrosPoint);
						else if (!onlyEditable)
							g.fillRect((int) (p1.x * echelle) - demiTaillePetitPoint, (int) (p1.y * echelle) - demiTaillePetitPoint, taillePetitPoint, taillePetitPoint);
					}
					p2 = p1;
				}
				// Affichage du dernier point
				g.setColor(Color.white);
				g.fillOval((int) ((p1.x * echelle) - tailleGrosPoint), (int) ((p1.y * echelle) - tailleGrosPoint), 2 * tailleGrosPoint + 1, 2 * tailleGrosPoint + 1);
				g.setColor(color);
				g.drawOval((int) ((p1.x * echelle) - tailleGrosPoint), (int) ((p1.y * echelle) - tailleGrosPoint), 2 * tailleGrosPoint + 1, 2 * tailleGrosPoint + 1);
				g.setFont(new Font("Arial", Font.PLAIN, 9));
				g.drawString("" + number, (int) (p1.x * echelle) - demiTaillePetitPoint - 1 - (2 * number / 10), (int) (p1.y * echelle) + tailleGrosPoint - 1);
			}
		}
	}

	/* affichage du point en cours de lecture midi **/
	public void afficherMidi(int debut, int fin, Graphics g, float echelle, int readPointIndex)
	{
		if (readPointIndex >= 0)
			if (visible && !isEmpty())
			{
				int endInd = previousPoint2(fin);
				if ((debut <= getLastDate()) && fin > getFirstDate())
				{
					if (readPointIndex > endInd)
					{
						readPointIndex = endInd;
					}
					HoloPoint p = elementAt(readPointIndex);
					int taille = (int) (200 / (p.z + 20));
					g.setColor(color);
					g.fillOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
				}
			}
	}

	/** affichage des points lus */
	public void afficherMidiLu(int debut, int fin, Graphics g, float echelle, int readPointIndex)
	{
		if (readPointIndex >= 0)
			if (visible && !isEmpty())
			{
				if ((debut <= getLastDate()) && fin > getFirstDate())
				{
					int begInd = this.nextPoint2(debut);
					int endInd = this.previousPoint2(fin);
					if (endInd > readPointIndex)
						endInd = readPointIndex - 1;
					for (int i = begInd; i <= endInd; i++)
					{
						HoloPoint p = elementAt(i);
						int taille = (int) (200 / (p.z + 20));
						g.setColor(Color.white);
						g.fillOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
						g.setColor(color);
						g.drawOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
					}
				}
			}
	}

	/** affichage des x derniers points lus */
	public void afficherMidiLuNb(int debut, int fin, Graphics g, float echelle, int readPointIndex, int ptsNb)
	{
		ptsNb--;
		if (readPointIndex >= 0)
			if (visible && !isEmpty())
			{
				if ((debut <= getLastDate()) && fin > getFirstDate())
				{
					int begInd;
					if (readPointIndex <= (ptsNb + this.nextPoint2(debut)))
						begInd = this.nextPoint2(debut);
					else
						begInd = readPointIndex - ptsNb;
					int endInd = this.previousPoint2(fin);
					if (endInd > readPointIndex)
						endInd = readPointIndex - 1;
					// System.out.println(begInd+" "+endInd);
					for (int i = begInd; i <= endInd; i++)
					{
						HoloPoint p = elementAt(i);
						int taille = (int) (200 / (p.z + 20));
						g.setColor(Color.white);
						g.fillOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
						g.setColor(color);
						g.drawOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
					}
				}
			}
	}

	/** affichage du premier point de la piste (T) */
	public void afficherT(int debut, Graphics g, float echelle)
	{
		if (visible && !isEmpty())
		{
			if (debut <= getLastDate())
			{
				HoloPoint p = elementAt(previousPoint(debut));
				int taille = (int) (200 / (p.z + 20));
				g.setColor(color);
				g.fillOval((int) (p.x * echelle) - taille, (int) (p.y * echelle) - taille, 2 * taille, 2 * taille);
			}
		}
	}

	/** DRAW ROOM RENDER PROJ */
	public void drawRoomRenderProj(GL gl, GLUT glut, boolean onlyEditable, int begin, int end, int tkNum, boolean render, int selected, Vector<Integer> selIndex)
	{
		if (!visible || isEmpty())
			return;
		if (!trajs.isEmpty() && !(begin > getLastDate() || end < getFirstDate()))
		{
			float[] c = OpenGLUt.glColor(gl, color);
			HoloPoint first = trajs.firstElement().points.firstElement();
			HoloPoint last = trajs.lastElement().points.lastElement();
			boolean b1 = false, b2 = false;
			gl.glPointSize(15);
			gl.glBegin(GL.GL_POINTS);
			if(first.date >= begin)
			{
				first.drawRoomProj(gl);
				b1 = true;
			}
			if(last.date <= end)
			{
				last.drawRoomProj(gl);
				b2 = true;
			}
			gl.glEnd();	
			OpenGLUt.glColor(gl, Color.WHITE);
			gl.glPointSize(13);
			gl.glBegin(GL.GL_POINTS);
			if(b1)
				first.drawRoomProj(gl);
			if(b2)
				last.drawRoomProj(gl);
			gl.glEnd();	
			HoloPoint lastDrawn = null;
			
			drawPlayedPoints(gl, c);
			
			for (int i = 0 ; i < trajs.size() ; i++)
			{
				OpenGLUt.glColor(gl,c);
				lastDrawn = trajs.get(i).drawRoomRenderProj(gl, onlyEditable, begin, end, tkNum, i, selected, selIndex, c, lastDrawn);
			}
			if(lastDrawn != null)
			{
				if(!b2)
				{
					OpenGLUt.glColor(gl, color);
					gl.glPointSize(6);
					gl.glBegin(GL.GL_POINTS);
						lastDrawn.drawRoomProj(gl);
					gl.glEnd();	
					OpenGLUt.glColor(gl, Color.WHITE);
					gl.glPointSize(4);
					gl.glBegin(GL.GL_POINTS);
					lastDrawn.drawRoomProj(gl);
					gl.glEnd();	
				}
			}
		} 
		if(lastRead != null)
		{
			try
			{
				float[] cc = OpenGLUt.convCol(color);
				cc[3] = 0.7f;
				gl.glColor4fv(cc, 0);
				gl.glPointSize(15);
				gl.glBegin(GL.GL_POINTS);
					lastRead.drawRoomProj(gl);
				gl.glEnd();
			} catch(NullPointerException e) {
				lastRead = null;
			}
		}
	}
	
	/** DRAW ROOM RENDER 3D */
	public void drawRoomRender3D(GL gl, boolean onlyEditable, int begin, int end)
	{
		if (!visible || isEmpty())
			return;

		if (!trajs.isEmpty() && !(begin > getLastDate() || end < getFirstDate()))
		{
			float[] c = OpenGLUt.glColor(gl, color);
			HoloPoint first = trajs.firstElement().points.firstElement();
			HoloPoint last = trajs.lastElement().points.lastElement();
			boolean b1 = false, b2 = false;
			gl.glPointSize(15);
			gl.glBegin(GL.GL_POINTS);
			if(first.date >= begin)
			{
				first.drawRoom3d(gl);
				b1 = true;
			}
			if(last.date <= end)
			{
				last.drawRoom3d(gl);
				b2 = true;
			}
			gl.glEnd();	
			OpenGLUt.glColor(gl, Color.WHITE);
			gl.glPointSize(13);
			gl.glBegin(GL.GL_POINTS);
			if(b1)
				first.drawRoom3d(gl);
			if(b2)
				last.drawRoom3d(gl);
			gl.glEnd();	
			
			drawPlayedPoints3D(gl, c);
			for (int i = 0 ; i < trajs.size() ; i++)
			{
				OpenGLUt.glColor(gl,c);
				trajs.get(i).drawRoomRender3D(gl, onlyEditable, begin, end);
			}
		}
		if(lastRead != null)
		{
			try
			{
				float[] cc = OpenGLUt.convCol(color);
				cc[3] = 0.7f;
				gl.glColor4fv(cc, 0);
				gl.glPointSize(15);
				gl.glBegin(GL.GL_POINTS);
					lastRead.drawRoom3d(gl);
				gl.glEnd();
			} catch(NullPointerException e) {
				lastRead = null;
			}
		}
	}
	
	/** DRAW ROOM SELECT */
	public void drawRoomSelectProj(GL gl, boolean onlyEditable, int begin, int end, int tkNum)
	{
		if (!visible || isEmpty() || isLocked())
			return;
		if (!(begin <= getLastDate() && end > getFirstDate()))
			return;
		float[] c = OpenGLUt.glColor(gl, color);
		for (int i = 0 ; i < trajs.size() ; i++)
		{
			OpenGLUt.glColor(gl,c);
			trajs.get(i).drawRoomSelectProj(gl, onlyEditable, begin, end, tkNum, i);
		}
	}
	
	/** DRAW ROOM MULTI SELECT */
	public void drawRoomMultiSelectProj(GL gl, boolean onlyEditable, int begin, int end, int tkNum)
	{
		if (!visible || isEmpty() || isLocked())
			return;
		if (!(begin <= getLastDate() && end > getFirstDate()))
			return;
		float[] c = OpenGLUt.glColor(gl, color);
		for (int i = 0 ; i < trajs.size() ; i++)
		{
			OpenGLUt.glColor(gl,c);
			trajs.get(i).drawRoomMultiSelectProj(gl, onlyEditable, begin, end, tkNum, i);
		}
	}
	
	public void drawScore(GL gl, int begin, int end, int bSel, int eSel, boolean render, int selected, Vector<HoloTraj> selSeqs, Vector<WaveFormInstance> selWaves, Vector<SDIFdataInstance> selDatas, int tkNum, int rtCursor, int pixelNum, boolean nosmoothenabled, int selMode)
	{
		if(nosmoothenabled)
			gl.glDisable(GL.GL_LINE_SMOOTH);
		drawDummyBg(gl, begin, end, tkNum);
		if(nosmoothenabled)
			gl.glEnable(GL.GL_LINE_SMOOTH);
		if((render || !isLocked()) && !(isEmpty() && waves.isEmpty() && sdifdataInstanceVector.isEmpty()))
		{
			for (int i = 0, last = sdifdataInstanceVector.size() ; i < last ; i++){
				sdifdataInstanceVector.get(i).drawScore(gl,color, begin, end, render, tkNum, i, selected, selDatas, pixelNum, nosmoothenabled, selMode);
			}for (int i = 0, last = waves.size() ; i < last ; i++)
				waves.get(i).drawScore(gl,color, begin, end, render, tkNum, i, selected, selWaves, selDatas, pixelNum,nosmoothenabled, selMode);
			for (int i = 0, last = trajs.size() ; i < last ; i++)
				trajs.get(i).drawScore(gl, color, begin, end, render, tkNum, i, selected, selSeqs,nosmoothenabled, selMode);
		}
		if(nosmoothenabled)
			gl.glDisable(GL.GL_LINE_SMOOTH);
		drawScoreTimeSel(gl,bSel,eSel);
		drawScoreRTCursor(gl,rtCursor);
		if(nosmoothenabled)
			gl.glEnable(GL.GL_LINE_SMOOTH);
	}

	public void drawDummyBg(GL gl, int begin, int end, int tkNum)
	{
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glColor4fv(isLocked() ? lockedTrackColor : OpenGLUt.convCol(Color.WHITE),0);
		gl.glLoadName(ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,0,ScoreIndex.TK_DISP));
		gl.glRectf(begin,0,end,1.5f);
		gl.glLineWidth(1);
		OpenGLUt.glColor(gl,Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(begin,1.5f);
		gl.glVertex2f(end,1.5f);
		gl.glEnd();
	}

	public void drawScoreTimeSel(GL gl, int begin, int end)
	{
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glColor4fv(selZoneColor,0);
		gl.glRectf(begin,0,end,1.5f);
		gl.glColor4fv(selZoneBorderColor,0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(begin,0);
		gl.glVertex2f(begin,1.5f);
		gl.glVertex2f(end,0);
		gl.glVertex2f(end,1.5f);
		gl.glEnd();
	}

	public void drawScoreRTCursor(GL gl, int cursorTime)
	{
		gl.glColor4fv(cursorColor,0);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(cursorTime,0);
			gl.glVertex2f(cursorTime,1.5f);
		gl.glEnd();
	}
	
	public void drawDraggedSeq(GL gl, int begin, int end, int seqNum, float dX)
	{
		if(seqNum != -1 && seqNum < trajs.size())
			trajs.get(seqNum).drawScoreSquare(gl,color,begin,end,dX);
	}
	
	public void drawMovedSeq(GL gl, int begin, int end, int seqNum, float b, float e, boolean type)
	{
		if(seqNum != -1 && seqNum < trajs.size())
			trajs.get(seqNum).drawMovedSquare(gl,color,begin,end,b,e,type);
	}
	
	public void drawMovedSDIF(GL gl, int begin, int end, int seqNum, float b, float e, boolean type)
	{
		if(seqNum != -1 && seqNum < sdifdataInstanceVector.size())
			sdifdataInstanceVector.get(seqNum).drawMovedSquare(gl,color,begin,end,b,e,type);
	}
	public void drawPlayedPoints(GL gl, float[] color)
	{
		HoloPoint p;
		int i = playedIndex,j=0;
		float tmp = color[3];
		color[3] = 0.7f;
		gl.glColor4fv(color, 0);
		gl.glPushMatrix();
		gl.glTranslated(0., 0., 0.1);
		int minPtSize = 1;
		int maxPtSize = 18;
		float delta = (float) (maxPtSize - minPtSize) / Ut.drawPtsNb;
		float currentSize = minPtSize;
			while(j < Ut.drawPtsNb)
			{
				
				if(playedPointsDate[i]>0 && (p = (HoloPoint)sequence.get(playedPointsDate[i]))!=null)
				{
					gl.glPointSize(currentSize);
					gl.glBegin(GL.GL_POINTS);
					p.drawRoomProj(gl);
					gl.glEnd();
				}
				currentSize += delta;
				i = (i + 1) % playedPointsDate.length;
				j++;
			}
		
		color[3] = tmp;
		gl.glColor4fv(color, 0);
		gl.glPopMatrix();
	
	}
	
	public void drawPlayedPoints3D(GL gl, float[] color)
	{
		HoloPoint p;
		int i = playedIndex,j=0;
		float tmp = color[3];
		color[3] = 0.7f;
		gl.glColor4fv(color, 0);
		int minPtSize = 1;
		int maxPtSize = 18;
		float delta = (float) (maxPtSize - minPtSize) / Ut.drawPtsNb;
		float currentSize = minPtSize;
			while(j < Ut.drawPtsNb)
			{
				
				if(playedPointsDate[i]>0 && (p = (HoloPoint)sequence.get(playedPointsDate[i]))!=null)
				{
					gl.glPointSize(currentSize);
					gl.glBegin(GL.GL_POINTS);
					p.drawRoom3d(gl);
					gl.glEnd();
				}
				currentSize += delta;
				i = (i + 1) % playedPointsDate.length;
				j++;
			}
		color[3] = tmp;
		gl.glColor4fv(color, 0);
	
	}
	/** ************* PLAYING **************** */
	
	public void preparePlay()
	{
		sequence.clear();
		for(HoloTraj tj : trajs)
		{
			tj.updatePointNext();
			for(HoloPoint p : tj.points)
				sequence.put(p.date,p);
		}
		
		playedPointsDate = new int[Ut.drawPtsNb];
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
		playedIndex = 0;
			
	}
	
	public HoloPoint getPointPlay(int date)
	{
		HoloPoint p = (HoloPoint) sequence.get(date);
		if(p!=null)
		{
			if(playedIndex >= playedPointsDate.length)
				playedIndex = 0;
			playedPointsDate[playedIndex] = date;
			playedIndex = (playedIndex + 1) % playedPointsDate.length;
		}
		return	p ;
	}
	
	public HoloPoint getPointPlaySub(int date,int lastdate)
	{
		HoloPoint p = null;
		if(date == lastdate)
		{
			p = (HoloPoint) sequence.get(date);
		}
		else if(date > lastdate)
		{
			subsequence = sequence.subMap(lastdate+1, date+1);
			if(!subsequence.isEmpty())
			{
				p = subsequence.get(subsequence.lastKey());
			}
		}
		else // date < lastdate
		{
			subsequence = sequence.subMap(date, lastdate);
			if(!subsequence.isEmpty())
			{
				p = subsequence.get(subsequence.firstKey());
			}
		}
		if(p!=null)
		{
			if(playedIndex >= playedPointsDate.length)
				playedIndex = 0;
			playedPointsDate[playedIndex] = date;
			playedIndex = (playedIndex + 1) % playedPointsDate.length;
		}
		return	p;
	}
	
	/** ************* RECHERCHE/AJOUT/SUPPRESSION **************** */
	/** ************* RECHERCHE AJOUT SUPPRESSION **************** */
	
	/** ************ Play Traverser *************** */
	public  HoloPoint initTraverser(int date)
	{
		if(isEmpty())
			return null;
		if(date < getFirstDate())
		{
			int i = 0;
			nextDate = -1;
			while(i<trajs.size() && trajs.get(i).isEmpty())
				i++;
			if(i<trajs.size())
			{
				nextTrajIndex = i;
				nextPointIndex = 0;
				nextDate = trajs.get(i).firstElement().date;
			}
			
			return null;
		}
		
		if(date > getLastDate())
		{
			nextDate = -1;
			return trajs.lastElement().lastElement();
		}
		
		//recherche du pervious point et next point`
		HoloPoint prevPoint = trajs.firstElement().firstElement();
		int indiceTraj = 0;
		boolean trouve = false,trouve2 = false;
		for(HoloTraj ht : trajs)
		{
			int indice = ht.points.size() - 1;
			
			while ((indice >= 0) & !trouve)
			{
				if ((prevPoint = ht.points.elementAt(indice)).date<= date)
					trouve = true;
				indice = indice - 1;
			}
			if(trouve)
			{
				indice = indice + 2;
				if(indice < ht.points.size())
				{
					nextPointIndex = indice;
					nextTrajIndex = indiceTraj;
					nextDate = ht.get(nextPointIndex).date;
					trouve2 = true;
				}else
				{
					indiceTraj++;
					if(indiceTraj <  trajs.size())
					{
						nextPointIndex = 0;
						nextTrajIndex = indiceTraj;
						nextDate = trajs.get(nextTrajIndex).get(nextPointIndex).date;
						trouve2 = true;
					}
				}
				break;
			}
				
			indiceTraj++;
		}
		
		if(!(trouve && trouve2))
		{
			nextDate = -1;
			return null;
		}else
		{
			return prevPoint;
		}
		
	}
	
	
	public HoloPoint getTraverser(int date)
	{
		if(date < nextDate || nextDate < 0)
			return null;
		HoloTraj ht;
		HoloPoint p = (ht = trajs.get(nextTrajIndex)).get(nextPointIndex);
		ht.currentRead = nextPointIndex;
		
		// recherche de prochaine date
		nextDate = -1;
		int pointIndex = nextPointIndex+1;
		boolean trouve = false;
		for(int i = nextTrajIndex,l = trajs.size(); i<l && !trouve ; i++)
		{
			while(pointIndex < trajs.get(i).size())
			{
				if(trajs.get(i).get(pointIndex) != null)
				{
					if(trajs.get(i).get(pointIndex).date > date)
					{
					nextTrajIndex = i;
					nextPointIndex = pointIndex;
					nextDate = trajs.get(i).get(pointIndex).date;
					trouve = true;
					break;
					}
				}
				pointIndex ++;
			}
			//trajs.get(i).stop();
		}
		
		return p;
	}
	
	/** ************ Recuperation, Recherche de points *************** */
	public Vector<HoloPoint> elements()
	{
		Vector<HoloPoint> elem = new Vector<HoloPoint>();
		for (HoloTraj ht : trajs)
			elem.addAll(ht.elements());
		// On retourne la somme des elements des HoloTraj
		return elem;
	}

	public HoloPoint firstElement()
	{
		if (trajs.isEmpty())
			return null;
		return trajs.firstElement().firstElement();
	}

	public HoloPoint lastElement()
	{
		if (trajs.isEmpty())
			return null;
		return trajs.lastElement().lastElement();
	}

	// fonction de recuperation du point a l'indice i
	// OPTIMISE HOLOTRACK elementAt (dicho)
	public HoloPoint elementAt(int i)
	{
		if (!trajs.isEmpty() && i >= 0)
		{
			if (i == 0)
			{
				return firstElement();
			} else if (i == size() - 1)
			{
				return lastElement();
			} else if (i < size())
			{
				boolean found = false;
				int k = 0;
				while (k < trajs.size() && !found)
				{
					HoloTraj ht = trajs.get(k);
					if (ht.getBegNumber() <= i && ht.getEndNumber() >= i)
						return ht.elementAt(i);
					k++;
				}
				update();
			}
		}
		return null;
	}
	
	public HoloPoint get(int i)
	{
		return elementAt(i);
	}

	public void removeElementAt(int i)
	{
		if (!trajs.isEmpty() && i >= 0 &&i < size())
		{
			boolean found = false;
			int shift = 0;
			int k = 0;
			while (k < trajs.size() && !found)
			{
				HoloTraj ht = trajs.get(k);
				if (ht.getBegNumber() <= i && ht.getEndNumber() >= i)
				{
					shift = ht.removeElementAt(i);
					found = true;
					if (ht.isEmpty())
						trajs.remove(ht);
				} else
					k++;
			}
			if (found)
				shiftBegNumbers(k + 1, -shift);
		}
	}

	public void remove(int i)
	{
		removeElementAt(i);
	}

	public int size()
	{
		if (trajs.isEmpty())
			return 0;
		int size = 0;
		for (HoloTraj ht : trajs)
			size += ht.size();
		return size;
	}

	public int sizeBetween(int b, int e)
	{
		int tmp = 0;
		for(HoloTraj tj:trajs)
			for(HoloPoint p:tj.points)
				if(Ut.between(p.date,b,e))
					tmp++;
		return tmp;
	}
	
	public void insertElementAt(HoloPoint hp, int i)
	{
		if (!trajs.isEmpty() && i >= 0)
		{
			if (i < size())
			{
				boolean found = false;
				int k = 0;
				while (k < trajs.size() && !found)
				{
					HoloTraj ht = trajs.get(k);
					// System.out.println("insertElementAt "+ht.getBegNumber()+" < "+i+" < "+ht.getEndNumber());
					if (ht.getBegNumber() <= i && ht.getEndNumber() >= i)
					{
						ht.addElement(hp, i);
						found = true;
					} else
						k++;
				}
				if (found)
					shiftBegNumbers(k + 1, 1);
				else
					System.out.println("insertElementAt Not Found");
			} else
				addElement(hp);
		}
	}

	public void addElement(HoloPoint hp)
	{
		if (!trajs.isEmpty())
			trajs.lastElement().addElement(hp);
		else
		{
			HoloTraj n = new HoloTraj(0);
			n.addElement(hp);
			trajs.add(n);
		}
	}

	public void add(HoloPoint hp, int i)
	{
		insertElementAt(hp, i);
	}

	public void addElement(HoloPoint hp, int i)
	{
		insertElementAt(hp, i);
	}

	public void add(HoloPoint hp)
	{
		addElement(hp);
	}

	public void setElementAt(HoloPoint hp, int i)
	{
		if (!trajs.isEmpty() && i >= 0)
		{
			if (i < size())
			{
				boolean found = false;
				int k = 0;
				while (k < trajs.size() && !found)
				{
					HoloTraj ht = trajs.get(k);
					if (ht.getBegNumber() <= i && ht.getEndNumber() >= i)
					{
						ht.setElementAt(hp, i);
						found = true;
					} else
						k++;
				}
			}
		}
	}

	public void set(int i, HoloPoint hp)
	{
		setElementAt(hp, i);
	}

	/** renvoie l'indice du 1er point situe avant la date passee en parametre */
	public int previousPoint(int date)
	{
		int indice = size() - 1;
		boolean trouve = false;
		while ((indice >= 0) & !trouve)
		{
			if (elementAt(indice).date < date)
				trouve = true;
			indice--;
		}
		return indice + 1;
	}

	/** renvoie l'indice du 1er point situe avant ou a la date passee en parametre */
	public int previousPoint2(int date)
	{
		int indice = size() - 1;
		boolean trouve = false;
		while ((indice >= 0) & !trouve)
		{
			if (elementAt(indice).date <= date)
				trouve = true;
			indice--;
		}
		return indice + 1;
	}

	/** renvoie l'indice du 1er point situe apres la date passee en parametre */
	public int nextPoint(int date)
	{
		int indice = 0;
		boolean trouve = false;
		while ((indice < size()) & !trouve)
		{
			if (elementAt(indice).date > date)
				trouve = true;
			indice++;
		}
		return indice - 1;
	}

	/** renvoie l'indice du 1er point situe apres ou a la date passee en parametre */
	public int nextPoint2(int date)
	{
		int indice = 0;
		boolean trouve = false;
		while ((indice < size()) & !trouve)
		{
			if (elementAt(indice).date >= date)
				trouve = true;
			indice++;
		}
		return indice - 1;
	}

	public int getLastDateSDIFandTraj()
	{
		if (trajs.isEmpty() && sdifdataInstanceVector.isEmpty())
			return 0;
		if (sdifdataInstanceVector.isEmpty())
			return trajs.lastElement().getLastDate();
		if (trajs.isEmpty())
			return sdifdataInstanceVector.lastElement().getLastDate();
		return Math.min(trajs.lastElement().getLastDate(), sdifdataInstanceVector.lastElement().getLastDate());
	}
	
	public int getLastDateSDIF()
	{
		if (sdifdataInstanceVector.isEmpty())
			return 0;
		return sdifdataInstanceVector.lastElement().getLastDate();
	}
	
	public int getLastDate()
	{
		if (trajs.isEmpty())
			return 0;
		return trajs.lastElement().getLastDate();
	}
	
	public int getFirstDateSDIFandTraj()
	{
		if (trajs.isEmpty() && sdifdataInstanceVector.isEmpty())
			return 0;
		if (sdifdataInstanceVector.isEmpty())
			return trajs.firstElement().getFirstDate();
		if (trajs.isEmpty())
			return sdifdataInstanceVector.firstElement().getFirstDate();
		return Math.min(trajs.firstElement().getFirstDate(), sdifdataInstanceVector.firstElement().getFirstDate());	
	}
	
	public int getFirstDateSDIF()
	{
		if (sdifdataInstanceVector.isEmpty())
			return 0;
		return sdifdataInstanceVector.firstElement().getFirstDate();
	}
	
	public int getFirstDate()
	{
		if (trajs.isEmpty())
			return 0;
		return trajs.firstElement().getFirstDate();
	}

	public int getDuration()
	{
		return (getLastDate() - getFirstDate());
	}

	/** renvoie l'indice du dernier point */
	public int getLastPoint()
	{
		if (!trajs.isEmpty())
			return (size() - 1);
		return -1;
	}

	public int prevEditPoint(int n)
	{
		// OPTIMISER on recherche (recherche dichotomique)
		// d'abord dans quel HoloTraj on est puis on test editable
		int j = 0;
		if (n <= size() && n > 0)
			for (j = n - 1; j >= 0; j--)
				if (elementAt(j).isEditable())
					break;
		if (j < 0)
			j = 0;
		return (j);
	}

	/** donne l'indice du point editable suivant le nieme */
	public int nextEditPoint(int n)
	{
		// OPTIMISER on recherche (recherche dichotomique)
		// d'abord dans quel HoloTraj on est puis on test editable
		int j = n;
		if (n < size() - 1)
			for (j = n + 1; j < size(); j++)
				if (elementAt(j).isEditable())
					break;
		if (j > size() - 1)
			j = size() - 1;
		return (j);
	}

	public int getDate(int n)
	{
		// OPTIMISER on recherche (recherche dichotomique)
		// d'abord dans quel HoloTraj on est puis on test editable
		if (n < 0 || n >= size())
			return (0);
		return (elementAt(n).date);
	}

	public void deletePoints(int begin, int end)
	{
		if(begin != -1 && end != -1 && begin < end)
		{
			int first = getTraj(begin);
			int last = getTraj(end);
			
			if(first == last)
				trajs.get(first).remove(begin,end);
			else
			{
				trajs.get(first).remove(begin,-1);
				trajs.get(last).remove(-1,end);
				first++;
				last--;
				while(last >= first)
				{
					delTraj(trajs.get(last));
					last--;
				}
			}
		}
	}
	
	/* ********** Ajout / Modifications / Suppression de Trajets ************ */
	/** Ajouter un trajet existant (coller, deplacer entre pistes, fonctions generatrices) */
	public int addTraj(HoloTraj ht, int insertTime)
	{
		if (ht != null)
		{
			// System.out.println("add traj : " + ht.getDuree() + " t:" + insertTime);
			if (!trajs.isEmpty())
			{
				// On utilise insertTime < 0 pour inserer directement en fin de piste
				HoloTraj prevHt;
				if (insertTime >= 0)
				{
					int k = prevTraj(insertTime);
					ht.move(insertTime);
					trajs.insertElementAt(ht, k + 1);
					return k + 1;
				}
				prevHt = trajs.lastElement();
				ht.move(prevHt.getLastDate());
				trajs.addElement(ht);
				return trajs.size() - 1;
			}
			ht.move(insertTime);
			trajs.add(ht);
			return 0;
		}
		return -1;
	}
	
	public void addTraj(HoloTraj ht)
	{
		if(ht != null)
			trajs.add(ht);
	}

	/** suppression d'un trajet */
	public void delTraj(HoloTraj ht)
	{
		if (trajs.contains(ht))
			trajs.remove(ht);
	}

	/** on coupe dans la piste à la date donnée */
	public int cutAt(int date)
	{
		if (!(date >= 0))
			return -1;
		if (trajs.isEmpty())
			return cutSDIFat(date);
		if (sdifdataInstanceVector.isEmpty())
			return cutTrajAt(date);
	
		int toreturn = -1;
		int indT = searchTraj(date);
		if (indT != -1)
			toreturn = addTraj(trajs.get(indT).cutAt(date), date);

		int indS = searchSDIF(date);
		if (indS != -1){
			addSDIF(sdifdataInstanceVector.get(indS).cutAt(date));
			if (indT==-1)
				return sdifdataInstanceVector.get(indS).getFirstDate();
			else
				return Math.min(toreturn, (sdifdataInstanceVector.get(indS).getFirstDate()));
		} else if (indT==-1){
			return nextSDIF(date);
		} else{
			return Math.min(nextTraj(date), nextSDIF(date));
		}
	}

	/** on coupe dans la piste à la date donnée */
	public int cutTrajAt(int date)
	{
		if (trajs.isEmpty() || !(date >= 0))
			return -1;
		int ind = searchTraj(date);
		if (ind != -1)
			return addTraj(trajs.get(ind).cutAt(date), date);
		return nextTraj(date);
	}
	
	/** on coupe dans la piste à la date donnée */
	public int cutSDIFat(int date)
	{
		if (sdifdataInstanceVector.isEmpty() || !(date >= 0))
			return -1;
		int ind = searchSDIF(date);
		if (ind != -1){
			addSDIF(sdifdataInstanceVector.get(ind).cutAt(date));
			return sdifdataInstanceVector.get(ind).getFirstDate();
		}
		return nextSDIF(date);
	}
	
	/** on coupe dans la piste entre dateBegin et dateEnd */
	public void cut(int dateBegin, int dateEnd, boolean wavesToo, boolean sdifsToo)
	{
		if ((trajs.isEmpty() && waves.isEmpty() && sdifdataInstanceVector.isEmpty()) || dateBegin > dateEnd)
			return;
		
		Vector<HoloTraj> toAdd = new Vector<HoloTraj>();
		for(HoloTraj t:trajs)
		{
			HoloTraj ta = t.cut(dateBegin,dateEnd);
			if (ta != null)
				toAdd.add(ta.dupliquer());
		}
		for(HoloTraj ht:toAdd)
			addTraj(ht,ht.getFirstDate());	
		if(!waves.isEmpty() && wavesToo)
			for(int k = waves.size()-1 ; k >= 0 ; k--)
			{
				WaveFormInstance w = waves.get(k);
				if(w.getFirstDate() >= dateBegin && w.getLastDate() <= dateEnd)
					waves.remove(w);
			}
		if (sdifsToo) {
			Vector<SDIFdataInstance> sdifToAdd = new Vector<SDIFdataInstance>();
			for(SDIFdataInstance sdif:sdifdataInstanceVector)
			{
				SDIFdataInstance sdifa = sdif.cut(dateBegin,dateEnd);
				if (sdifa != null)
					sdifToAdd.add(sdifa.dupliquer());
			}
			for(SDIFdataInstance sd: sdifToAdd)
				this.addSDIF(sd);//, sd.getFirstDate());	
		}
	/*	if(!sdifdataInstanceVector.isEmpty() && wavesToo)
			for(int k = waves.size()-1 ; k >= 0 ; k--)
			{
				WaveFormInstance w = waves.get(k);
				if(w.getFirstDate() >= dateBegin && w.getLastDate() <= dateEnd)
					waves.remove(w);
			}*/
		update();
	}

	// TODO
	/** rogne la piste autour des dates begin & end */
	public void crop(int dateBegin, int dateEnd, boolean wavesToo)
	{
		if ((trajs.isEmpty() && waves.isEmpty()) && sdifdataInstanceVector.isEmpty() || dateBegin > dateEnd)
			return;
		
		for(HoloTraj t:trajs)
			t.crop(dateBegin,dateEnd);
		if(waves.isEmpty() || !wavesToo)
			return;
		for(int k = waves.size()-1 ; k >= 0 ; k--)
		{
			WaveFormInstance w = waves.get(k);
			if((w.getFirstDate() < dateBegin || w.getLastDate() > dateEnd))
				waves.remove(w);
		}
	}

	/** supprimer les trajets et sdif vides et corriger les indexs */
	public boolean update()
	{
		int tmpSize = 0;
		int k = trajs.size() - 1;
		while (k >= 0)
		{
			if (trajs.get(k).isEmpty())
				trajs.remove(k);
			k--;
		}
		
		int j = sdifdataInstanceVector.size() - 1;
		while (j >= 0)
		{
			if (sdifdataInstanceVector.get(j).isEmpty())
				sdifdataInstanceVector.remove(j);
			j--;
		}
		boolean b = trajs.sort() || waves.sort() || sdifdataInstanceVector.sort();
		for (HoloTraj ht : trajs)
		{
			ht.setBegNumber(tmpSize);
			ht.firstElement().setEditable(true);
			ht.lastElement().setEditable(true);
			ht.updatePointNext();
			tmpSize += ht.size();
		}
		return b;
	}

	/** rechercher le trajet à la date t */
	public int searchTraj(int date)
	{
		if (trajs.isEmpty() || date < getFirstDate() || date > getLastDate())
			return -1;
		int k = 0;
		while (k < trajs.size())
		{
			HoloTraj ht = trajs.get(k);
			if (ht.getFirstDate() <= date && ht.getLastDate() >= date)
				return k;
			k++;
		}
		return -1;
	}

	/** rechercher le sdif à la date t */
	public int searchSDIF(int date)
	{
		if (sdifdataInstanceVector.isEmpty() || date < getFirstDateSDIF() || date > getLastDateSDIF())
			return -1;
		int k = 0;
		while (k < sdifdataInstanceVector.size())
		{
			SDIFdataInstance sdif = sdifdataInstanceVector.get(k);
			if (sdif.getFirstDate() <= date && sdif.getLastDate() >= date)
				return k;
			k++;
		}
		return -1;
	}
	
	/** rechercher le premier trajet apres la date t */
	public int nextTraj(int date)
	{
		if (trajs.isEmpty() || date > getLastDate())
			return -1;
		int k = 0;
		while (k < trajs.size())
		{
			HoloTraj ht = trajs.get(k);
			if (ht.getFirstDate() >= date)
				return k;
			k++;
		}
		return -1;
	}

	/** rechercher le premier trajet apres la date t */
	public int nextSDIF(int date)
	{
		if (sdifdataInstanceVector.isEmpty() || date > getLastDateSDIF())
			return -1;
		int k = 0;
		while (k < sdifdataInstanceVector.size())
		{
			SDIFdataInstance sdif = sdifdataInstanceVector.get(k);
			if (sdif.getFirstDate() >= date)
				return k;
			k++;
		}
		return -1;
	}
	
	/** rechercher le premier trajet apr?s ou comprenant la date t */
	public int nextTraj2(int date)
	{
		if (trajs.isEmpty() || date > getLastDate())
			return -1;
		int k = 0;
		while (k < trajs.size())
		{
			HoloTraj ht = trajs.get(k);
			if (Ut.between(date,ht.getFirstDate(),ht.getLastDate()) || ht.getFirstDate() > date)
				return k;
			k++;
		}
		return -1;
	}

	/** rechercher le premier trajet avant la date t */
	public int prevTraj(int date)
	{
		if (trajs.isEmpty() || date < getFirstDate())
			return -1;
		int k = trajs.size() - 1;
		while (k >= 0)
		{
			HoloTraj ht = trajs.get(k);
			if (ht.getLastDate() <= date)
				return k;
			k--;
		}
		return -1;
	}
	
	/** rechercher le premier sdif avant la date t */
	public int prevSDIF(int date)
	{
		if (sdifdataInstanceVector.isEmpty() || date < getFirstDateSDIF())
			return -1;
		int k = sdifdataInstanceVector.size() - 1;
		while (k >= 0)
		{
			SDIFdataInstance sdif = sdifdataInstanceVector.get(k);
			if (sdif.getLastDate() <= date)
				return k;
			k--;
		}
		return -1;
	}
	
	/** rechercher le premier trajet avant la date t */
	public int prevTraj2(int date)
	{
		if (trajs.isEmpty() || date < getFirstDate())
			return -1;
		int k = trajs.size() - 1;
		while (k >= 0)
		{
			HoloTraj ht = trajs.get(k);
			if (Ut.between(date,ht.getFirstDate(),ht.getLastDate()) || ht.getLastDate() < date)
				return k;
			k--;
		}
		return -1;
	}
	
	public HoloTraj getHoloTraj(int seqNum)
	{
		if(seqNum != -1 && seqNum < trajs.size())
			return trajs.get(seqNum);
		return null;
	}
	
	public Vector<Integer> getAllTrajs(int tkNum, int begin, int end)
	{
		Vector<Integer> tjs = new Vector<Integer>(5,1);
		for(int i = 0, last = trajs.size() ; i < last ; i++)
		{
			HoloTraj tj = trajs.get(i);
			if((tj.getFirstDate() >= begin || tj.getLastDate() <= end) && !(tj.getFirstDate() >= end || tj.getLastDate() <= begin))
				tjs.add(ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,i,ScoreIndex.SEQ_POLY));
		}
		return tjs;
	}
	
	public Vector<Integer> getAllWaves(int tkNum, int begin, int end)
	{
		Vector<Integer> wavs = new Vector<Integer>(5,1);
		for(int i = 0, last = waves.size() ; i < last ; i++)
		{
			WaveFormInstance w = waves.get(i);
			if((w.getFirstDate() >= begin || w.getLastDate() <= end) && !(w.getFirstDate() >= end || w.getLastDate() <= begin)){
				wavs.add(ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,i,ScoreIndex.WAVE_POLY));
			}
		}
		return wavs;
	}
	
	public Vector<Integer> getAllSDIFs(int begin, int end)
	{
		Vector<Integer> sdifs = new Vector<Integer>(5,1);
		for(int i = 0, last = sdifdataInstanceVector.size() ; i < last ; i++)
		{
			SDIFdataInstance s = sdifdataInstanceVector.get(i);
			if((s.getFirstDate() >= begin || s.getLastDate() <= end) && !(s.getFirstDate() >= end || s.getLastDate() <= begin))
				sdifs.add(ScoreIndex.encode(ScoreIndex.TYPE_TK, this.number , i, ScoreIndex.DATA_POLY));
		}
		return sdifs;
	}
	// TODO a voir
	/**
	* Retourne les SDIFdataInstances présente dans la sélection définie
	* par les temps "begin" et "end".
	* @param begin le temps de debut de la selection.
	* @param end le temps de fin de la selection.
	*/
	public Vector<SDIFdataInstance> getAllSDIFsInSelection(int begin, int end)
	{
		Vector<SDIFdataInstance> sdifs = new Vector<SDIFdataInstance>(5,1);
		for(int i = 0; i<sdifdataInstanceVector.size() ; i++)
		{
			SDIFdataInstance s = sdifdataInstanceVector.get(i);
			if((s.getFirstDate()<=begin && begin<=s.getLastDate()) || (s.getFirstDate()<=end && end<=s.getLastDate())
					|| (begin<=s.getFirstDate() && s.getLastDate()<=end)) {
				if (!sdifs.contains(s))
					sdifs.add(s);
			}
		}
		return sdifs;
	}
	/**
	* Retourne la SDIFinstance correspondant à la description donnée.
	* retourne null si pas trouvée.
	* @param sdifDescription = sdifRecherchée.toString()
	*/
	public SDIFdataInstance getSDIFinstance(String sdifDescription){
		sdifDescription = sdifDescription.replaceAll("\"", "");
		for(SDIFdataInstance sdifinstance : sdifdataInstanceVector){
			String sdifinstanceString = sdifinstance.toString().replaceAll("\"", "");
			if (sdifDescription.equals(sdifinstanceString))
				return sdifinstance;
		}
		return null;
	}
	
	public Vector<Integer> getAllRoomPoints(int tkNum, int begin, int end, boolean edit)
	{
		Vector<Integer> pts = new Vector<Integer>(5,1);
		for(int i = 0, last = trajs.size() ; i < last ; i++)
		{	
			HoloTraj ht = trajs.get(i);
			for(int j = 0, last2 = ht.points.size() ; j < last2 ; j++)
			{
				HoloPoint p = ht.points.get(j);
				if(p.date >= begin && p.date <= end && (!edit || p.isEditable()))
					pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,i,j));
			}
		}
		return pts;
	}
	
	public Vector<Integer> getAllTimePoints(int curveNum, int begin, int end, boolean edit)
	{
		Vector<Integer> pts = new Vector<Integer>(5,1);
		for(int i = 0, last = trajs.size() ; i < last ; i++)
		{	
			HoloTraj ht = trajs.get(i);
			for(int j = 0, last2 = ht.points.size() ; j < last2 ; j++)
			{
				HoloPoint p = ht.points.get(j);
				if(p.date >= begin && p.date <= end && (!edit || p.isEditable()))
					pts.add(TimeIndex.encode(TimeIndex.TYPE_PT,curveNum,i,j));
			}
		}
		return pts;
	}
	
	public Vector<Integer> getRoomPointsFromTo(int tkNum, int from, int to)
	{
		Vector<Integer> pts = new Vector<Integer>(5,1);
		RoomIndex.decode(from);
		int t1 = RoomIndex.getTrack();
		int s1 = RoomIndex.getSeq();
		int p1 = RoomIndex.getPt();
//		System.out.print(RoomIndex.toStr2());
		RoomIndex.decode(to);
//		System.out.println(" "+RoomIndex.toStr2());
		int t2 = RoomIndex.getTrack();
		int s2 = RoomIndex.getSeq();
		int p2 = RoomIndex.getPt();
		
		if (t1 != t2 || t1 != tkNum)
			return null;
		
		if(s1 == s2)
		{
			for(int i = p1 ; i <= p2 ; i++)
				pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s1,i));
			return pts;
		} else if(s1 == s2-1)
		{
			for(int i = p1, last = trajs.get(s1).points.size() ; i < last ; i++)
				pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s1,i));
			for(int i = 0 ; i <= p2 ; i++)
				pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s2,i));
			return pts;
		} else {
			for(int i = p1, last = trajs.get(s1).points.size() ; i < last ; i++)
				pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s1,i));
			for(int s = s1+1 ; s < s2 ; s++)
				for(int j = 0, last2 = trajs.get(s).points.size() ; j < last2 ; j++)
					pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s,j));
			for(int i = 0 ; i <= p2 ; i++)
				pts.add(RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,s2,i));
		}
		return pts;
	}
	
	public void removeTraj(int seqNum)
	{
		if(seqNum != -1 && seqNum < trajs.size())
			trajs.remove(seqNum);
	}
	
	public void removeWave(int waveNum)
	{
		if(waveNum != -1 && waveNum < waves.size()){			
			// Pour les sdifInstances attachées
			WaveFormInstance w = waves.get(waveNum);
			if(w.getSDIFvector()!=null)
				for (int j=0; j<w.getSDIFvector().size(); j++)
					sdifdataInstanceVector.remove(w.getSDIFvector().get(j));
			// remove de la wave
			waves.remove(waveNum);
		}
	}
	
	public void removeSdif(int sdifNum)
	{
		if(sdifNum != -1 && sdifNum < sdifdataInstanceVector.size()){
			SDIFdataInstance s = sdifdataInstanceVector.get(sdifNum);
			// On remove la waveform attachée si elle existe	
			if(s.getLinkedWaveForm()!=null)
				remove(s.getLinkedWaveForm().getWave(), s);
			else
				sdifdataInstanceVector.remove(sdifNum);
		}
	}
	
	/** Utilise lors de la modification manuel (EditeurPointTextuel) d'un point editable */
	public void calcNewPosSegXYZ(int MovePtNth, float x, float y, float z)
	{
		int prev, actual, next;
		float dx, dy, dz, fact;
		HoloPoint actualPt, nthPt;
		prev = prevEditPoint(MovePtNth); // n∞ point editable precedent
		actual = MovePtNth; // n∞ point editable central
		next = nextEditPoint(MovePtNth); // n∞ point editable suivant
		actualPt = elementAt(actual);
		dx = x - actualPt.x;
		dy = y - actualPt.y;
		dz = z - actualPt.z;
		for (int nth = prev; nth <= next; nth++)
		{
			nthPt = elementAt(nth);
			if (nth == actual)
				fact = 1;
			else if (nth < actual)
				fact = (float) (nth - prev) / (float) (actual - prev);
			else
				fact = (float) (nth - next) / (float) (actual - next);
			nthPt.translater(dx * fact, dy * fact, dz * fact);
		}
	}
	
	/** OPTIMISE HOLOTRACK getTraj OPTIMISER (dicho) */
	public int getTraj(int pointIndex)
	{
		if (pointIndex < 0 || trajs.isEmpty())
			return -1;
		for (int i = 0, last = trajs.size(); i < last; i++)
		{
			HoloTraj ht = trajs.get(i);
			if (Ut.between(pointIndex, ht.getBegNumber(), ht.getEndNumber()))
				return i;
		}
		return -1;
	}

	/** rechercher le dernier trajet entre dateBegin et dateEnd */
	public int lastTraj(int dateBegin, int dateEnd)
	{
		int ind2 = prevTraj2(dateEnd);
		if (ind2 != -1)
			if(trajs.get(ind2).getLastDate() >= dateBegin && trajs.get(ind2).getFirstDate() <= dateEnd)
				return ind2;
		return -1;
	}

	public void timeShift(int shift)
	{
		for (HoloTraj ht : trajs)
			ht.shiftDates(shift);
		for (WaveFormInstance w : waves)
			w.shiftDates(shift);
		for (SDIFdataInstance sdif : sdifdataInstanceVector)
			sdif.shiftDates(shift);
	}

	/** decalage des begNumber de l'HoloTraj i jusqu'a la fin d'un shift */
	public void shiftBegNumbers(int htIndex, int shift)
	{
		if (!trajs.isEmpty())
			if (htIndex < trajs.size())
				for (int i = htIndex; i < trajs.size(); i++)
					trajs.get(i).changeBegNumber(shift);
	}

	/** ******* Ajout / Modifications / Suppression de points ********* */
	/*	public int addPoint(HoloPoint p, double delta, int nbPointsInter, float echelle, int dateBegin, int dateEnd)
	{
		int date;
		if (!trajs.isEmpty() && p != null && dateBegin >= 0)
		{
			int ind = lastTraj(dateBegin, dateEnd);
			if (ind != -1)
			{
//				System.out.println(" insertion ind "+ind+" @ "+dateBegin);
				if(trajs.size() >= ind+2)
				{
					if(!(trajs.get(ind+1).getFirstDate() > (trajs.get(ind).getLastDate()+delta)))
						delta--;
					boolean b = false;
					while(trajs.size() >= ind+2 && (!(trajs.get(ind+1).getFirstDate() > (trajs.get(ind).getLastDate()+delta))))
					{
						ind++;
						b = true;
					}
					if(b) delta++;
				}
					
				date = trajs.get(ind).addPoint(p, delta, nbPointsInter, echelle, dateBegin);
			} else
			{
//				System.out.println("nouveau trajet @ "+dateBegin);
				// nouveau trajet
				HoloTraj ht2 = new HoloTraj();
				addTraj(ht2, dateBegin);
				date = ht2.addPoint(p, delta, nbPointsInter, echelle, dateBegin);
			}
		} else
		{
//			System.out.println("1er point sur la piste @ "+dateBegin);
			// 1er point sur la piste
			HoloTraj ht = new HoloTraj();
			addTraj(ht, dateBegin);
			date = ht.addPoint(p, delta, nbPointsInter, echelle, dateBegin);
		}
		update();
		return date;
	}//*/

	// OPTIMISE HOLOTRACK ADD POINT
	public int addPoint(HoloPoint p, double delta, int nbPointsInter, int dateBegin, int dateEnd)
	{
		int date;
		if (!trajs.isEmpty() && p != null && dateBegin >= 0)
		{
			int ind = lastTraj(dateBegin, dateEnd);
			if (ind != -1)
			{
//				System.out.println(" insertion ind "+ind+" @ "+dateBegin);
				if(trajs.size() >= ind+2)
				{
					if(!(trajs.get(ind+1).getFirstDate() > (trajs.get(ind).getLastDate()+delta)))
						delta--;
					boolean b = false;
					while(trajs.size() >= ind+2 && (!(trajs.get(ind+1).getFirstDate() > (trajs.get(ind).getLastDate()+delta))))
					{
						ind++;
						b = true;
					}
					if(b) delta++;
				}
					
				date = trajs.get(ind).addPoint(p, delta, nbPointsInter, dateBegin);
			} else
			{
//				System.out.println("nouveau trajet @ "+dateBegin);
				// nouveau trajet
				HoloTraj ht2 = new HoloTraj();
				addTraj(ht2, dateBegin);
				date = ht2.addPoint(p, delta, nbPointsInter, dateBegin);
			}
		} else
		{
//			System.out.println("1er point sur la piste @ "+dateBegin);
			// 1er point sur la piste
			HoloTraj ht = new HoloTraj();
			addTraj(ht, dateBegin);
			date = ht.addPoint(p, delta, nbPointsInter, dateBegin);
		}
		update();
		return date;
	}

	public Color getColor(){
		return color;
	}

	public void setColor(Color color){
		this.color = color;
		setDirty(true);
	}

	public void setNumber(int number){
		this.number = number;
	}

	public boolean isLocked(){
		return locked;
	}

	public void setLocked(boolean locked){
		this.locked = locked;
	}

	public String getValueAt(int counter, boolean autostop, boolean looping){	
		if(isEmpty())
			return "none";
		if(autostop || looping){	
			stop(looping);
			return "none";
		}
		if(counter < getFirstDate())
			return "none";
		if(counter > getLastDate()){
			trajs.lastElement().currentRead++;
			return "none";
		}
		int nextInd = nextTraj2(counter);
		HoloTraj ht = trajs.get(nextInd);
		if(ht == null)
			return "none";
			
		if(ht.isEmpty() || ht.getFirstDate() > counter){
			if(nextInd > 0)
				trajs.get(nextInd-1).currentRead++;
			ht.stop();
			return "none";
		}
		// TESTING
		if(nextInd > 0)
			trajs.get(nextInd-1).stop();
		lastRead = ht.getPointAt(counter); ///// HUGE
		if(lastRead != null)
			return lastRead.posString();
		return "none";
	}
	
	
	public HoloPoint getPointAt(int counter, boolean autostop, boolean looping){	
		if(isEmpty())
			return null;
		if(autostop || looping){	
			stop(looping);
			return null;
		}
		if(counter < getFirstDate())
			return null;
		if(counter > getLastDate()){
			trajs.lastElement().currentRead++;
			return null;
		}
		int nextInd = nextTraj2(counter);
		HoloTraj ht = trajs.get(nextInd);
		if(ht == null)
			return null;
			
		if(ht.isEmpty() || ht.getFirstDate() > counter){
			if(nextInd > 0)
				trajs.get(nextInd-1).currentRead++;
			ht.stop();
			return null;
		}
		// TESTING
		if(nextInd > 0)
			trajs.get(nextInd-1).stop();
		lastRead = ht.getPointAt(counter); ///// HUGE
		if(lastRead != null)
			return lastRead;
		return null;
	}
	
	public String getLastValueAt(int counter)
	{
		if(isEmpty())
			return "none";
		if(counter < getFirstDate())
			return "none";
			
		if(counter > getLastDate())
		{
			lastRead = trajs.lastElement().getLastPointAt(counter);
			return lastRead.posString();
		}

		int ind = prevTraj2(counter);
		if(ind == -1)
			return "none";
		lastRead = trajs.get(ind).getLastPointAt(counter);
		if(lastRead != null)
			return lastRead.posString();
		return "none";
	}

	public void addWave(WaveFormInstance currentWave)
	{		
		waves.add(currentWave);
		currentWave.update();
		// Pour les sdifInstances attachées
		if(currentWave.getSDIFvector()!=null){
			for (SDIFdataInstance hdt : currentWave.getSDIFvector())
				addSDIF(hdt);
		}
	}
	
	/** Ajouter un sdif existant (coller, deplacer entre pistes, fonctions generatrices) */
	public int addSDIF(SDIFdataInstance sdifInst, int insertTime)
	{
		if (sdifInst != null)
		{
			// System.out.println("add traj : " + ht.getDuree() + " t:" + insertTime);
			if (!sdifdataInstanceVector.isEmpty())
			{
				// On utilise insertTime < 0 pour inserer directement en fin de piste
				SDIFdataInstance prevSDIF;
				if (insertTime >= 0)
				{
					int k = prevSDIF(insertTime);
					sdifInst.move(insertTime);
					sdifdataInstanceVector.insertElementAt(sdifInst, k + 1);
					return k + 1;
				}
				prevSDIF = sdifdataInstanceVector.lastElement();
				sdifInst.move(prevSDIF.getLastDate());
				sdifdataInstanceVector.addElement(sdifInst);
				return sdifdataInstanceVector.size() - 1;
			}
			sdifInst.move(insertTime);
			sdifdataInstanceVector.add(sdifInst);
			return 0;
		}
		return -1;
	}

	public void addSDIF(SDIFdataInstance currentSDIF)
	{
		// On ajoute la waveform attachée si elle existe
		if(currentSDIF.getLinkedWaveForm()!=null && !waves.contains(currentSDIF.getLinkedWaveForm()))
				addWave(currentSDIF.getLinkedWaveForm());
		else sdifdataInstanceVector.add(currentSDIF);
		currentSDIF.update();
	}
	
	public WaveFormInstance getWave(int waveNum)
	{
		if(waveNum != -1 && waveNum < waves.size())
			return waves.get(waveNum);
		return null;
	}
	
	public SDIFdataInstance getSDIFinst(int sdifNum)
	{
		if(sdifNum != -1 && sdifNum < sdifdataInstanceVector.size())
			return sdifdataInstanceVector.get(sdifNum);
		return null;
	}
	
	public void remove(HoloWaveForm hwf)
	{
		for(int i = waves.size()-1 ; i >= 0 ; i--)
		{
			WaveFormInstance w = waves.get(i);
			if(w.getWave().equals(hwf)){
				waves.remove(w);
				// Pour les sdifInstances attachées
				if(w.getSDIFvector()!=null)
					for (int j=0; j<w.getSDIFvector().size(); j++)
						if (w.getSDIFvector().get(j)!=null)
						sdifdataInstanceVector.remove(w.getSDIFvector().get(j));	
			}
		}
		update();
	}
	
	public void remove(HoloWaveForm hwf, SDIFdataInstance sdifInst)
	{
		for(int i = waves.size()-1 ; i >= 0 ; i--)
		{
			WaveFormInstance w = waves.get(i);
			if(w.getWave().equals(hwf) && w.getSDIFvector().contains(sdifInst)){
				waves.remove(w);
				// Pour les sdifInstances attachées
				if(w.getSDIFvector()!=null)
					for (int j=0; j<w.getSDIFvector().size(); j++)
						if (w.getSDIFvector().get(j)!=null)
						sdifdataInstanceVector.remove(w.getSDIFvector().get(j));	
			}
		}
		update();
	}

	public void remove(HoloSDIFdata hsdifdt, boolean parent) {
		for(int i = sdifdataInstanceVector.size()-1 ; i >= 0 ; i--)	{
			SDIFdataInstance sdifDataInst = sdifdataInstanceVector.get(i);
			if(sdifDataInst.getData() == hsdifdt || hsdifdt.createdSDIFvector.contains(sdifDataInst.getData())) {
				// On remove la waveform attachée si elle existe
				if(sdifDataInst.getLinkedWaveForm()!=null)
					remove(sdifDataInst.getLinkedWaveForm().getWave(), sdifDataInst);
				else 
					sdifdataInstanceVector.remove(sdifDataInst);
			}
		}
		if (!parent)
			for (HoloSDIFdata hsdifdtChild : hsdifdt.children)
				this.remove(hsdifdtChild, false);
		if (hsdifdt.getParent()!=null && hsdifdt.getParent().getChildrenCount()==1)
			this.remove(hsdifdt.getParent(), true);

		update();		
	}
	
	public void stop(boolean resetLastRead)	{
		if(resetLastRead)
			lastRead = null;
		for(HoloTraj tj:trajs)
			tj.stop();
		
		for(int i = 0; i<Ut.drawPtsNb ; i++)
			playedPointsDate[i]=-1;
	}

	public boolean isRecEnable() {
		return recEnable;
	}

	public void setRecEnable(boolean recEnable)	{
		this.recEnable = recEnable;
	}
}