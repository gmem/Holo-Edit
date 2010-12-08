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

import java.awt.Color;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;

public class OpenGLUt
{
	public static void glColor(GL gl, float[] color)
	{
		gl.glColor4fv(color, 0);
	}

	public static float[] glColor(GL gl, Color c)
	{
		float[] tmp = new float[4];
		tmp[0] = (float) c.getRed() / 255;
		tmp[1] = (float) c.getGreen() / 255;
		tmp[2] = (float) c.getBlue() / 255;
		tmp[3] = 1;
		
		gl.glColor4fv(tmp,0);
		return tmp;
	}

	public static void drawPoint(GL gl, float x, float y)
	{
		gl.glVertex2f(x, y);
	}

	public static void drawPoint(GL gl, float x, float y, float z)
	{
		gl.glVertex3f(x, y, z);
	}

	public static float[] convCol(Color c)
	{
		return new float[] { (float)c.getRed() / 255, (float)c.getGreen() / 255, (float)c.getBlue() / 255, (float)c.getAlpha() / 255 };
	}
	
	public static int textLoad(GL gl, GLU glu, String filename)
	{
		int[] tmp = new int[1];
		gl.glGenTextures(1, tmp, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, tmp[0]);
		makeRGBTexture(gl, glu, readPNGImage(filename), GL.GL_TEXTURE_2D,true);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		return tmp[0];
	}
	

	// TEXTURES
	private static BufferedImage readPNGImage(String resourceName)
	{
		try {
			File f = new File("./images/" + resourceName);
			if (f.exists()) {
				BufferedImage img = ImageIO.read(f);
				java.awt.geom.AffineTransform tx = java.awt.geom.AffineTransform.getScaleInstance(1, -1);
				tx.translate(0, -img.getHeight(null));
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				img = op.filter(img, null);
				return img;
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void makeRGBTexture(GL gl, GLU glu, BufferedImage img, int target, boolean mipmapped)
	{
		if (img == null)
			return;
		ByteBuffer dest = null;
		switch (img.getType())
		{
		case BufferedImage.TYPE_3BYTE_BGR:
		case BufferedImage.TYPE_CUSTOM:
		{
			byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			dest = ByteBuffer.allocateDirect(data.length);
			dest.order(ByteOrder.nativeOrder());
			dest.put(data, 0, data.length);
			dest.rewind();
			break;
		}
		case BufferedImage.TYPE_INT_RGB:
		{
			int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			dest = ByteBuffer.allocateDirect(data.length * BufferUtil.SIZEOF_INT);
			dest.order(ByteOrder.nativeOrder());
			dest.asIntBuffer().put(data, 0, data.length);
			dest.rewind();
			break;
		}
		default:
			throw new RuntimeException("Unsupported image type " + img.getType());
		}
		try
		{
			if (mipmapped) {
				glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(), img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dest);
			} else {
				gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(), img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dest);
			}
		} catch(java.nio.BufferUnderflowException e) {}
	}
	
	public static void drawTexture(GL gl, int textNum, float x, float y, float w, float h)
	{
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		gl.glColor4f(1, 1, 1, 1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textNum);
		gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f);
			gl.glVertex2f(x, y);
			gl.glTexCoord2f(1.0f, 0.0f);
			gl.glVertex2f(x + w, y);
			gl.glTexCoord2f(1.0f, 1.0f);
			gl.glVertex2f(x + w, y + h);
			gl.glTexCoord2f(0.0f, 1.0f);
			gl.glVertex2f(x, y + h);
		gl.glEnd();
	}
	
}
