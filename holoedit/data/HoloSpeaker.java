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
package holoedit.data;

import holoedit.opengl.RoomIndex;
import holoedit.util.Ut;
import javax.media.opengl.GL;
import com.sun.opengl.util.GLUT;

public class HoloSpeaker
{
	/* position */
	public float X;
	public float Y;
	public float Z;
	/* direction en radian (angle / centre graphe, 0 rad = vers la droite) */
	public float dir;
	/* distance au centre */
	public float dist;
	/* numero */
	public int num;
	private float theta = 0;
	private float phy = 0;
	

	// divers constructeurs
	public HoloSpeaker()
	{
		X = 0;
		Y = 0;
		Z = 0;
		num = 1;
		recalcDist();
	}

	public HoloSpeaker(int i)
	{
		X = 0;
		Y = 0;
		Z = 0;
		num = i;
		recalcDist();
	}

	public HoloSpeaker(float X, float Y, float dir, int num)
	{
		this.X = X;
		this.Y = Y;
		this.Z = 0;
		this.dir = dir;
		this.num = num;
		recalcDist();
	}

	public HoloSpeaker(float X, float Y, int dir, int num)
	{
		this.X = X;
		this.Y = Y;
		this.Z = 0;
		this.dir = dir;
		this.num = num;
		recalcDist();
	}

	public HoloSpeaker(float X, float Y, int num)
	{
		this.X = X;
		this.Y = Y;
		this.Z = 0;
		this.dir = (float) Math.atan2(X, Y);
		this.num = num;
		recalcDist();
	}

	public HoloSpeaker(float X, float Y, float Z, float dir, int num)
	{
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.dir = dir;
		this.num = num;
		recalcDist();
	}

	public HoloSpeaker(float x, float y, float z, int n, @SuppressWarnings("unused") int dummy)
	{
		X = x;
		Y = y;
		Z = z;
		dir = (float) Math.atan2(x, y);
		num = n;
		recalcDist();
	}

	public HoloSpeaker(float dir, float dist, boolean circ, int num)
	{
		if (circ)
		{
			this.dir = HoloConv.deg2Rad(dir);
			this.dist = dist;
			X = (float) (Math.cos(this.dir) * dist);
			Y = (float) (Math.sin(this.dir) * dist);
			Z = 0;
			this.num = num;
			recalcDist();
		}
	}

	public HoloSpeaker(float dir, float dist, boolean circ, int num, float Z)
	{
		if (circ)
		{
			this.dir = HoloConv.deg2Rad(dir);
			this.dist = dist;
			X = (float) (Math.cos(this.dir) * dist);
			Y = (float) (Math.sin(this.dir) * dist);
			this.Z = Z;
			this.num = num;
			recalcDist();
		}
	}

	// fonction permettant de construire un HP a partir d'une chaîne de caractere
	// (sauvegarde dans les preferences des HPs)
	public HoloSpeaker(String all)
	{
		try
		{
			this.num = (new Integer(all.substring(5, all.indexOf("(x)")))).intValue();
			this.X = (new Float(all.substring(all.indexOf("(x)") + 3, all.indexOf("(y)")))).floatValue();
			this.Y = (new Float(all.substring(all.indexOf("(y)") + 3, all.indexOf("(z)")))).floatValue();
			this.Z = (new Float(all.substring(all.indexOf("(z)") + 3, all.indexOf("(d)")))).floatValue();
			this.dir = (new Float(all.substring(all.indexOf("(d)") + 3, all.length()))).floatValue();
		}
		catch (IndexOutOfBoundsException e)
		{}
		recalcDist();
	}

	public String toString()
	{
		return "\t<speaker num=\""+num+"\" x=\""+X+"\" y=\""+Y+"\" z=\""+Z+"\" d=\""+dir+"\"/>\n";
	}

	// recalcule la distance (apres modif dans EditHPTextuel)
	public void recalcDist()
	{
		dist = getModule();
		dir = (float) Math.atan2(X, -Y);
		theta = radToDeg(dir);
		phy = -radToDeg(Math.PI / 2 - Math.acos(Z / dist));
	}
	
	// FEATURE SPEAKER TRANSLATE CONSTANT DIST
	public void translatePolDir(float dDir)
	{
//		System.out.println("dDir : "+dDir);
		dir = dDir;
		X = (float) (Math.cos(dir) * dist);
		Y = -(float) (Math.sin(dir) * dist);
	}
	
	// FEATURE SPEAKER TRANSLATE CONSTANT DIR
	public void translatePolDist(float dDist)
	{
//		System.out.println("dDist : "+dDist);
		dist = dDist;
		X = (float) (Math.cos(dir) * dist);
		Y = -(float) (Math.sin(dir) * dist);
	}
	
	private static float radToDeg(double v)
	{
		return Ut.mod((float) (v / (2 * Math.PI) * 360), 360);
	}

	public float getModule()
	{
		return (float) Math.sqrt(X * X + Y * Y + Z * Z);
	}

	public float getRay()
	{
		return (float) Math.sqrt(X * X + Y * Y);
	}

	public void draw3D(GL gl, int speakerListId)
	{
		gl.glRotatef(theta, 0, 0, 1);
		gl.glRotatef(phy, 0, 1, 0);
		gl.glTranslatef(dist, 0, 0);
		gl.glCallList(speakerListId);
	}

	public void drawProj(GL gl, GLUT glut, int spkNum, int speakerListId, int selSpeakerListId, int speakerSelected, boolean render)
	{
		gl.glRotatef(theta, 0, 0, 1);
		gl.glTranslatef(getRay(), 0, 0);
		float scale = (float) (0.01 * Z + 1);
		gl.glScalef(scale, scale, 0);
		int index = RoomIndex.encode(RoomIndex.TYPE_SPEAKER,0,0,spkNum);
		gl.glLoadName(index);
		if(index == speakerSelected)
			gl.glCallList(selSpeakerListId);
		else
			gl.glCallList(speakerListId);
		if(!render)
			return;
		gl.glColor4f(0, 0, 0, 1);
		gl.glRasterPos2f((X > 0 ? 2.5f : 3.5f), (X > 0 ? -0.5f : 0.5f));
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, String.valueOf(num));
	}
}
