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
/* ********* classe HoloPoint ************ */
/**
 * Les nouveaux Holopoint contienne desormais un tableau supplementaire
 * qui permet de stocker d'autres donnees de type float il permettra
 * dans le futur a l'utilisateur d'editer des courbes de son choix
 * pour un parametres quelconque par exemple gain, reverb, doppler, ...
 */
package holoedit.data;

import holoedit.opengl.RoomIndex;
import holoedit.util.Ut;
import holoedit.data.HoloConv;
import java.awt.Point;
import java.util.Vector;
import javax.media.opengl.GL;

public class HoloPoint
{
	// FEATURE HOLOPOINT voir vecteur de param supplémentaires
	public static final float UPLIM2D = 820;
	public static final float LOWLIM2D = -820;
	public static final float UPLIMZ = 820;
	public static final float LOWLIMZ = -820;
	/** ****** ATTRIBUTS ******** */
	/** X coordinate of the point. */
	public float x;
	/** Y coordinate of the point. */
	public float y;
	/** Z coordinate of the point. */
	public float z;
	/** Point date in milliseconds. */
	public int date;
	public double ddate;
	/** point editable ou pas */
	private boolean editable;
	/** pour les parametres supplementaires (gain, reverb, doppler...) */
	public Vector<Float> param;
	/** HoloPoint suivant dans la traj **/
	public HoloPoint nextpoint;
	public HoloPoint prevpoint;
	
	/** ******* CONSTRUCTEURS ******* */
	public HoloPoint()
	{
		x = 0;
		y = 0;
		z = 0;
		date = 0;
		////ddate = 0.;
		editable = false;
		initParams();
	}

	public HoloPoint(float x, float y, boolean editable)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = 0;
		this.date = 0;
		ddate = 0.;
		this.editable = editable;
		initParams();
	}

	public HoloPoint(float x, float y, float z, boolean editable)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = limitZ(z);
		this.date = 0;
		ddate = 0.;
		this.editable = editable;
		initParams();
	}

	public HoloPoint(float x, float y, int date)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = 0;
		this.date = date;
		ddate = date;
		this.editable = false;
		initParams();
	}

	public HoloPoint(float x, float y, float z, int date)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = limitZ(z);
		this.date = date;
		ddate = date;
		this.editable = false;
		initParams();
	}

	public HoloPoint(float x, float y)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = 0;
		this.date = 0;
		ddate = 0.;
		this.editable = false;
		initParams();
	}

	public HoloPoint(float x, float y, float z)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = limitZ(z);
		this.date = 0;
		ddate = 0;
		this.editable = false;
		initParams();
	}

	public HoloPoint(double[] pos)
	{
		x = 0;
		y = 0;
		z = 0;
		date = 0;
		ddate = 0;
		editable = false;
		initParams();
		switch (pos.length)
		{
		case 3:
			z = limitZ((float) pos[2]);
		case 2:
			y = limit2D((float) pos[1]);
		case 1:
			x = limit2D((float) pos[0]);
		default:
			break;
		}
	}

	public HoloPoint(Point pt, int date)
	{
		this.x = limit2D(pt.x);
		this.y = limit2D(pt.y);
		this.date = date;
		ddate = date;
		initParams();
	}

	public HoloPoint(Point pt, boolean editable)
	{
		this.x = limit2D(pt.x);
		this.y = limit2D(pt.y);
		this.editable = editable;
		initParams();
	}

	public HoloPoint(float x, float y, int date, boolean editable)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = 0;
		this.date = date;
		this.ddate = date;
		this.editable = editable;
		initParams();
	}

	public HoloPoint(float x, float y, float z, int date, boolean editable)
	{
		this.x = limit2D(x);
		this.y = limit2D(y);
		this.z = limitZ(z);
		this.date = date;
		this.ddate = date;
		this.editable = editable;
		initParams();
	}

	public HoloPoint(double x, double y, double z, int date, boolean editable)
	{
		this.x = limit2D((float) x);
		this.y = limit2D((float) y);
		this.z = limitZ((float) z);
		this.date = date;
		this.ddate = date;
		this.editable = editable;
		initParams();
	}

	// copie
	public HoloPoint(HoloPoint p)
	{
		x = limit2D(p.x);
		y = limit2D(p.y);
		z = limitZ(p.z);
		date = p.date;
		ddate = date;
		editable = p.editable;
		initParams();
		for (float f : p.param)
			param.add(f);
	}

	/** duplique un holoPoint */
	public HoloPoint dupliquer()
	{
		return new HoloPoint(this);
	}

	/** Indicates whether some holoPoint is "equal to" this one.
	 * @param p The reference holoPoint with which to compare.
	 * @return true if this holoPoint is the same as the p argument; false otherwise.
	 */
	public boolean equals(HoloPoint p)
	{
		if( p == null )
			return false;
		if (this.date == p.date && this.editable == p.editable && this.x == p.x && this.y == p.y && this.z == p.z && p.param.size() == param.size())
		{
			boolean tmp = true;
			for (int i = 0, last = param.size(); i < last; i++)
				if (param.get(i) != p.param.get(i))
					tmp = false;
			return tmp;
		}
		return false;
	}

	/** Returns a string representation of the holoPoint */
	public String toString()
	{
		String res = "<point date=\"" + date + "\" x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" edit=\"" + editable;
		if(!param.isEmpty())
		{	
			res += "\" params=\"";
			for (float f : param)
				res += f + " ";
			res.substring(0,res.length()-1);
		}
		res += "\"/>";
		return res;
	}
	

	/** conversion en string pour transmission osc. */
	public String posString()
	{
		return x+" "+y+" "+z;
	}
	
	/* ************ Modification/Retour des parametres *************** */
	/** changement de l'etat editable/non editable. */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public void chgeEditable()
	{
		editable = !editable;
	}

	public boolean isEditable()
	{
		return editable;
	}

	/** retourne la distance XY par rapport au centre (0,0) */
	public float distance(HoloPoint p)
	{
		float dist, dx, dy;
		dx = (this.x - p.x);
		dy = (this.y - p.y);
		dist = (float) Math.sqrt(dx * dx + dy * dy);
		return dist;
	}

	/** retourne la distance XYZ par rapport au centre (0,0) */
	public float distance3D(HoloPoint p)
	{
		float dist, dx, dy, dz;
		dx = (this.x - p.x);
		dy = (this.y - p.y);
		dz = (this.z - p.z);
		dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		return dist;
	}

	/** Methodes de calcul du module d'un point
	 * a partir de ses coordonnees cartesiennes */
	public float getModule()
	{
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public float getRay()
	{
		return (float) Math.sqrt(x * x + y * y);
	}

	/** Methodes de calcul de l'argument en degrés d'un point
	 * a partir de ses coordonnees cartesiennes.
	 * @return L'argument en degrés. */
	public float getTheta()
	{
		float mod = getModule();
		if(mod == 0)
			return 0;
		float res = HoloConv.rad2Deg((float)Math.atan2(y ,x));
		
		if (y > 180)
		{
			res -= 360.;
		}
		return (res);
	}

	/** Methodes de calcul de l'argument en radians d'un point
	 * a partir de ses coordonnees cartesiennes.
	 * @return L'argument en radians. */
	public float getThetaRad()
	{
		float mod = getModule();
		if(mod == 0)
			return 0;
		return (float) Math.acos(x / mod);
	}
	
	/** Calcule la date du point, intermediaire entre celle de A et B
	*	en fonction des rapports de distances */
	public void calcDate(HoloPoint pA, HoloPoint pB)
	{
		float dateA, dateB;
		float distAB, distAM;
		dateA = pA.date;
		dateB = pB.date;
		distAB = pA.distance(pB);
		distAM = distance(pA);
		if (distAB == 0)
			this.date = (int) dateA;
		else
			this.date = (int) (dateA + ((dateB - dateA) * (distAM / distAB)));
		//ddate = date;
	}

	/** Decalage de la date d'une duree shift */
	public void shiftDate(int shift)
	{
		date = (date + shift > 0 ? date + shift : 0);
		//ddate = date;
	}
	
	/** mise à jour des dates int **/
	public void setDDate(double d)
	{
		ddate = d;
		date = (int)(ddate+0.5);
	}

	public void updateDDate()
	{
		ddate = date;
	}
	
	/** Initialisation des autres parametres (gain, reverb, doppler, ...) */
	public void initParams()
	{
		param = new Vector<Float>();
	}

	public void setParam(int paramNb, float paramValue)
	{
		if (paramNb < param.size())
			param.set(paramNb, paramValue);
	}

	public void addParam(float paramValue)
	{
		param.add(paramValue);
	}

	public float getParam(int paramNb)
	{
		if (paramNb < param.size())
			return param.get(paramNb);
		return -1;
	}

	/* **************** EDITION DU POINT ******************* */
	
	
	public void translateVec(HoloVec3 vec)
	{
		x = x + (float) vec.x;
		y = y + (float) vec.y;
		z = z + (float) vec.z;
	}
	
	public void scaleVec(HoloVec3 vec)
	{
		x = x * (float) vec.x;
		y = y * (float) vec.y;
		z = z * (float) vec.z;
	}
	
	public HoloPoint copyScaleTrans(HoloVec3 scale, HoloVec3 offset)
	{
		HoloPoint p = this.dupliquer();
		p.scaleVec(scale);
		p.translateVec(offset);
		return p;
	}
	
	/** translation XY (int) */
	public void translater(int dx, int dy)
	{
		x = limit2D(x + dx);
		y = limit2D(y + dy);
	}

	/** translation XYZ (int) */
	public void translater(int dx, int dy, int dz)
	{
		x = limit2D(x + dx);
		y = limit2D(y + dy);
		z = limitZ(z + dz);
		z = z < 0 ? 0 : z;
		z = z > 100 ? 100 : z;
	}

	/** translation XY (float) */
	public void translater(float dx, float dy)
	{
		x = limit2D(x + dx);
		y = limit2D(y + dy);
	}

	/** translation XYZ (float) */
	public void translater(float dx, float dy, float dz)
	{
		x = limit2D(x + dx);
		y = limit2D(y + dy);
		z = limitZ(z + dz);
	}

	/** translation XYZ (float) */
	public void translaterZ(float dz)
	{
		z = limitZ(z + dz);
	}
	
	/** proportion % XY ( et Z @charles) */
	public void diviserCoord(float div)
	{
		if (div != 0)
		{
			x = x / div;
			y = y / div;
			z = z / div;
		}
	}

	/** proportion * XY */
	public void multipliCoord(float div)
	{
		x = limit2D(x * div);
		y = limit2D(y * div);
	}

	/* ******* UTILS ******** */
	/** fonction permettant de limiter le point aux limites de l'espace 2D. */
	public static float limit2D(float d)
	{
		return Ut.clip(d, LOWLIM2D, UPLIM2D);
	}

	public static float limitZ(float d)
	{
		return Ut.clip(d, LOWLIMZ, UPLIMZ);
	}

	public static HoloPoint interpol(HoloPoint hp1, HoloPoint hp2, int date)
	{
		if (hp2.date < hp1.date)
			return interpol(hp2, hp1, date);
		if (date == hp1.date)
			return hp1.dupliquer();
		if (date == hp2.date)
			return hp2.dupliquer();
		if (hp1.date == hp2.date)
			return mean(hp1, hp2, date);
		if (date > hp1.date && date < hp2.date && hp1.date != hp2.date)
		{
			double step = (double) (date - hp1.date) / (hp2.date - hp1.date);
			HoloPoint tmp = new HoloPoint();
			tmp.date = date;
			//tmp.ddate = date;
			tmp.x = Ut.interpol(hp1.x, hp2.x, step);
			tmp.y = Ut.interpol(hp1.y, hp2.y, step);
			tmp.z = Ut.interpol(hp1.z, hp2.z, step);
			tmp.setEditable(hp1.isEditable() && hp2.isEditable());
			return tmp;
		}
		return null;
	}

	public static HoloPoint mean(HoloPoint hp1, HoloPoint hp2, int date)
	{
		return new HoloPoint(Ut.interpol(hp1.x, hp2.x, 0.5), Ut.interpol(hp1.y, hp2.y, 0.5), Ut.interpol(hp1.z, hp2.z, 0.5), date, hp1.isEditable() && hp2.isEditable());
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public float getZ()
	{
		return z;
	}

	public void setZ(float z)
	{
		this.z = z;
	}

	public void setRay(float ray)
	{
		if(ray != 0)
		{
			float mod = getModule();
			setX(getX() * ray/mod);
			setY(getY() * ray/mod);
		}
	}
		
	public void setThetaRad(float theta)
	{
		float ray = getModule();
		setX((float)(ray * Math.cos(theta)));
		setY((float)(ray * Math.sin(theta)));
	}
	
	public void setThetaRad(double theta)
	{
		float ray = getModule();
		setX((float)(ray * Math.cos(theta)));
		setY((float)(ray * Math.sin(theta)));
	}
	
	public void setTheta(float theta)
	{
		float thetaRad = HoloConv.deg2Rad(theta);
		float ray = getModule();
		setX((float)(ray * Math.cos(thetaRad)));
		setY((float)(ray * Math.sin(thetaRad)));
	}
	
	public void setTheta(double theta)
	{
		double thetaRad = HoloConv.deg2Rad(theta);
		float ray = getModule();
		setX((float)(ray * Math.cos(thetaRad)));
		setY((float)(ray * Math.sin(thetaRad)));
	}
	
	public void setAD(float angle,float dist)
	{
		float thetaRad = HoloConv.deg2Rad(angle);
		setX((float)(dist * Math.cos(thetaRad)));
		setY((float)(dist * Math.sin(thetaRad)));	
	}
	
	public void setAD(double angle,double dist)
	{
		float thetaRad = (float) HoloConv.deg2Rad(angle);
		setX((float)(dist * Math.cos(thetaRad)));
		setY((float)(dist * Math.sin(thetaRad)));	
	}
	
	public void setADZ(float angle,float dist,float z)
	{
		setAD(angle,dist);
		setZ(z);
	}

	public void setADZ(double angle, double dist, double z)
	{
		setAD(angle,dist);
		setZ((float) z);
	}
	
	public void setXYZ(float x, float y, float z)
	{
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public void setXYZ(double x, double y, double z)
	{
		setX((float) x);
		setY((float) y);
		setZ((float) z);
	}
	
	public void drawX(GL gl)
	{
		gl.glVertex2f(date, x);
	}
	
	public void drawY(GL gl)
	{
		gl.glVertex2f(date, y);
	}
	
	public void drawZ(GL gl)
	{
		gl.glVertex2f(date, z);
	}
	
	public void drawR(GL gl)
	{
		gl.glVertex2f(date, getModule());
	}
	
	public void drawT(GL gl)
	{
		gl.glVertex2f(date, getTheta());
	}
	
	public void drawRoomProj(GL gl)
	{
		gl.glVertex2f(x, y);
	}
	
	public void drawRoomProjSel(GL gl, int tkNum, int seqNum, int ptNum)
	{
		gl.glPointSize(editable ? 6 : 4);
		int index = RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,seqNum,ptNum);
		/*gl.glPushMatrix();
		if(editable)
			gl.glTranslated(0., 0., 1.);*/

		gl.glLoadName(index);
		gl.glBegin(GL.GL_POINTS);
			gl.glVertex2f(x, y);
		gl.glEnd();
		//gl.glPopMatrix();
	}
	
	public HoloPoint drawRoomProjSel(GL gl, int tkNum, int seqNum, int ptNum, int selected, Vector<Integer> selIndex, float[] color)
	{
		gl.glPointSize(editable ? 6 : 4);
		int index = RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,seqNum,ptNum);
		gl.glLoadName(index);
		gl.glBegin(GL.GL_POINTS);
			gl.glVertex2f(x, y);
		gl.glEnd();
		
		if(selected == index || selIndex.contains(index))
		{
			float tmp = color[3];
			color[3] = 0.5f;
			gl.glColor4fv(color,0);
			gl.glPointSize(editable ? 10 : 6);
			gl.glBegin(GL.GL_POINTS);
				gl.glVertex2f(x, y);
			gl.glEnd();
			
			color[3] = tmp;
			gl.glColor4fv(color,0);
		}
		return this;
	}
	
	public HoloPoint drawRoomProjSelected(GL gl, int tkNum, int seqNum, int ptNum, int selected, Vector<Integer> selIndex, float[] color)
	{

		int index = RoomIndex.encode(RoomIndex.TYPE_PT,tkNum,seqNum,ptNum);
		
		if(selected == index || selIndex.contains(index))
		{
				gl.glVertex2f(x, y);

		}
		return this;
	}
	
	public void drawRoom3d(GL gl)
	{
		gl.glVertex3f(x, y, z);
	}
		
}
