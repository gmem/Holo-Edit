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
package holoedit.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class TessCallback extends GLUtessellatorCallbackAdapter
{
	GL gl;
	GLU glu;

	public TessCallback(GL gl, GLU glu)
	{
		// System.out.println("new Tess!");
		this.gl = gl;
		this.glu = glu;
	}

	public void begin(int type)
	{
		gl.glBegin(type);
		// System.out.print("Tess - begin");
		// switch (type)
		// {
		// case (GL.GL_TRIANGLE_FAN):
		// System.out.print(" FAN");
		// break;
		// case (GL.GL_TRIANGLE_STRIP):
		// System.out.print(" STRIP");
		// break;
		// case (GL.GL_TRIANGLES):
		// System.out.print(" TRIANGLES");
		// break;
		// default :
		// System.out.print(" ERROR -tess being not reconised");
		// }
		// System.out.println();
	}

	public void end()
	{
		gl.glEnd();
		// System.out.println("Tess - end");
	}

	public void vertex(Object data)
	{
		double[] d = (double[]) data;
		if (d != null)
			if (d.length >= 2)
				gl.glVertex2f((float) d[0], (float) d[1]);
	}

	public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
	{
		int i;
		double[] vertex = new double[6];
		vertex[0] = coords[0];
		vertex[1] = coords[1];
		vertex[2] = coords[2];
		for (i = 3; i < 6; i++)
			vertex[i] = weight[0] * ((double[]) data[0])[i] + weight[1] * ((double[]) data[1])[i] + weight[2];// * ((double[]) data[2])[i] + weight[3];
		outData[0] = vertex;
	}

	public void error(int errnum)
	{
		System.out.println("Tess - GLU TESS ERROR!!!");// + glu.gluErrorString(errnum));
	}
}
