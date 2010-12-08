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
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.util.Ut;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

public class Room3DGUI extends FloatingWindow implements MouseMotionListener
{
	private float[] bgColor2 = { 1, 1, 1, 1 };
	private float axesLineWidth = 1;
	private float[] axes1Color = { 0.6f, 0.6f, 1, 0.75f };
	private float[] axes2Color = { 0.3f, 0.3f, 0.75f, 0.75f };
	private float[] speakerColor = { 0.7f, 0.7f, 0.7f, 1 };
	private float[] speaker2Color = { 0.5f, 0.5f, 0.5f, 1 };
	private float[] speaker3Color = { 0.3f, 0.3f, 0.3f, 1 };
	private float[] axes3Color = { 0, 0, 0, 1 };
	private int[] viewport = new int[4];
	private double[] mvmatrix = new double[16];
	private double[] projmatrix = new double[16];
	private int width;
	private int height;
	private Room3DCanvas threed_glp;
	private HoloEdit holoEditRef;
	private int axesListId = 0;
	private int speakerListId = 0;
	private float xrot = -50;
	private float yrot = 0;
	private float zrot = 30;
	private float xtrans = 0;
	private float ytrans = -10;
	private float zzoom = -150;
	private int pmousex,pmousey;
	private PopupMenu popup;
	private MenuItem reset;
	private class Room3DCanvas extends GLCanvas implements GLEventListener, MouseWheelListener, MouseListener, MouseMotionListener, KeyListener
	{
		private GL gl;
		private GLU glu = new GLU();
		private GLUquadric gluquad;
		private int keyDown = -1;
		private boolean altButton;

		Room3DCanvas(FloatingWindow fw)
		{
			super(holoEditRef.glcap, null, holoEditRef.glpb.getContext(), null);
			addKeyListener(this);
			addMouseListener(this);
			addMouseWheelListener(this);
			addMouseMotionListener(this);
			addGLEventListener(this);
			addFocusListener(fw);
			add(popup);
			setFocusable(true);
		}

		public void init(GLAutoDrawable drawable)
		{
			gl = drawable.getGL();
			glu = new GLU();
			gl.glClearColor(bgColor2[0], bgColor2[1], bgColor2[2], bgColor2[3]); // White Background
			gl.glViewport(0, 0, width, height);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gluquad = glu.gluNewQuadric();
			gl.glClearDepth(1.0f); // Depth Buffer Setup
			gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
			gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
			gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Really Nice Perspective Calculations
			gl.glMatrixMode(GL.GL_PROJECTION);
			holoEditRef.rtDisplay.add(drawable);
		}

		public void display(GLAutoDrawable drawable)
		{
			if(!visible)return;
			gl = drawable.getGL();
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();
			gl.glTranslatef(xtrans, ytrans, zzoom);
			gl.glRotatef(xrot, 1, 0, 0);
			gl.glRotatef(yrot, 0, 1, 0);
			gl.glRotatef(zrot, 0, 0, 1);
			getVars();
			drawRules();
			drawSpeakers(true);
			drawTracks(true, false);
		}

		public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
		{
			width = w;
			height = h;
			gl = drawable.getGL();
			if (height <= 0) // avoid a divide by zero error!
				height = 1;
			float aspect = (float) width / (float) height;
			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluPerspective(45.0f, aspect, 1, 1000);
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glDeleteLists(axesListId, 1);
			axesListId = 0;
		}

		public void displayChanged(GLAutoDrawable drawable, boolean arg1, boolean arg2)
		{
		}

		public void mouseWheelMoved(MouseWheelEvent e)
		{
			float zoom_factor = (1 - (e.getUnitsToScroll() / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)));
			zzoom = Ut.clip(zzoom * zoom_factor, -750, -50);
			display();
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() > 1)
			{
				xtrans = 0;
				ytrans = -10;
				zrot = 0;
				display();
			}
				
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
			pmousex = e.getX();
			pmousey = e.getY();
			
			if (e.isControlDown() || e.getButton() == MouseEvent.BUTTON3)
			{
				// POPUP
				popup.show(this, e.getX(), e.getY());
			}
			if(e.isAltDown())
				altButton = true;
						
			display();
		}

		public void mouseReleased(MouseEvent e)
		{
				altButton = false;
		}
		
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
		    int y = e.getY();
		    Dimension size = e.getComponent().getSize();
		    if(altButton)
		    {
		    	xtrans += 400*((float)(x-pmousex)/(float)size.width);
		    	ytrans -= 400*((float)(y-pmousey)/(float)size.width);
		    	
		    	xtrans = Ut.clip(xtrans, -800, 800);
				ytrans = Ut.clip(ytrans, 0, 800);
		    }else{
		    	
			    float thetaZ = 360.0f * ( (float)(x-pmousex)/(float)size.width);
			    float thetaX = 360.0f * ( (float)(y-pmousey)/(float)size.height);
	
				
			    xrot += thetaX;
			    zrot += thetaZ;
			    
			    xrot = Ut.clip(xrot, -90, 90);
				yrot = Ut.clip(yrot, -90, 90);
				zrot = Ut.mod(zrot, 360);
		    }
		    
		    pmousex = x;
		    pmousey = y;
		    display();
		}

		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		public void keyPressed(KeyEvent e)
		{
			boolean close = false;
			float pad = e.isShiftDown() ? 1 : 10;
			float padZ = e.isShiftDown() ? 2 : 25;
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DOWN:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					zzoom -= padZ;
					break;
				case KeyEvent.VK_R:
					xrot -= pad;
					break;
				case KeyEvent.VK_D:
					ytrans -= pad;
					break;
				}
				break;
			case KeyEvent.VK_UP:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					zzoom += padZ;
					break;
				case KeyEvent.VK_R:
					xrot += pad;
					break;
				case KeyEvent.VK_D:
					ytrans += pad;
					break;
				}
				break;
			case KeyEvent.VK_LEFT:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					zrot -= pad;
					break;
				case KeyEvent.VK_R:
					if(!e.isAltDown())
						yrot -= pad;
					else
						zrot -= pad;
					break;
				case KeyEvent.VK_D:
					xtrans -= pad;
					break;
				}
				break;
			case KeyEvent.VK_RIGHT:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					zrot += pad;
					break;
				case KeyEvent.VK_R:
					if(!e.isAltDown())
						yrot += pad;
					else
						zrot += pad;
					break;
				case KeyEvent.VK_D:
					xtrans += pad;
					break;
				}
				break;
			case KeyEvent.VK_W:
				close = true;
				break;
			case KeyEvent.VK_Z:
			case KeyEvent.VK_D:
			case KeyEvent.VK_R:
				keyDown = e.getKeyCode();
			default :
				break;
			}
			xrot = Ut.clip(xrot, -90, 90);
			yrot = Ut.clip(yrot, -90, 90);
			zrot = Ut.mod(zrot, 360);
			zzoom = Ut.clip(zzoom, -750, -50);
			if (!close)
				display();
			else {
				close();
				holoEditRef.checkWindows();
			}
		}

		public void keyTyped(KeyEvent e)
		{
		}

		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == keyDown)
				keyDown = -1;
		}

		private void drawRules()
		{
			if (axesListId == 0)
			{
				axesListId = gl.glGenLists(1);
				if (axesListId == 0)
					return;
				gl.glNewList(axesListId, GL.GL_COMPILE_AND_EXECUTE);
				glu.gluQuadricDrawStyle(gluquad, GLU.GLU_SILHOUETTE);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glLineWidth(axesLineWidth);
				gl.glColor4fv(axes1Color, 0);
				for (int i = 1; i <= 64; i = i * 2)
				{
					if (i != 3)
					{
						int k = i * 10;
						glu.gluDisk(gluquad, k, k, 50, 0);
						gl.glRectf(-1 * k, -1 * k, k, k);
					}
				}
				gl.glColor4fv(axes2Color, 0);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(-1 * 800, -1 * 800);
				gl.glVertex2f(800, 800);
				gl.glVertex2f(-1 * 800, 800);
				gl.glVertex2f(800, -1 * 800);
				gl.glEnd();
				gl.glColor4fv(axes3Color, 0);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(-1 * 800, 0);
				gl.glVertex2f(800, 0);
				gl.glVertex2f(0, 800);
				gl.glVertex2f(0, -1 * 800);
				gl.glVertex3f(0, 0, 0);
				gl.glVertex3f(0, 0, 100);
				gl.glEnd();
				gl.glEndList();
			}
			else
			{
				gl.glCallList(axesListId);
			}
		}

		private void drawSpeakers(boolean render)
		{
			if (holoEditRef.viewSpeakers)
			{
				if (speakerListId == 0)
				{
					speakerListId = gl.glGenLists(1);
					gl.glNewList(speakerListId, GL.GL_COMPILE);
					gl.glTranslatef(4, 0, 0);
					gl.glRotatef(-90, 0, 1, 0);
					gl.glColor4fv(speakerColor, 0);
					glu.gluQuadricDrawStyle(gluquad, GLU.GLU_FILL);
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
					glu.gluDisk(gluquad, 0, 2, 20, 10);
					glu.gluCylinder(gluquad, 2, 2, 2, 20, 10);
					gl.glTranslatef(0, 0, 2);
					gl.glColor4fv(speaker2Color, 0);
					glu.gluDisk(gluquad, 0, 2, 20, 10);
					glu.gluCylinder(gluquad, 2, 5, 2, 20, 10);
					gl.glColor4fv(speaker3Color, 0);
					glu.gluSphere(gluquad, 1.8f, 20, 20);
					gl.glTranslatef(0, 0, -2);
					gl.glRotatef(90, 0, 1, 0);
					gl.glTranslatef(-4, 0, 0);
					gl.glEndList();
				}
				gl.glRotatef(-90, 0, 0, 1);
				try
				{
					for (HoloSpeaker sp : holoEditRef.gestionPistes.speakers)
					{
						gl.glPushMatrix();
						sp.draw3D(gl, speakerListId);
						gl.glPopMatrix();
					}
				}
				catch (NullPointerException e)
				{
				}
				gl.glRotatef(90, 0, 0, 1);
			}
		}

		private void drawTracks(boolean render, boolean onlyPoints)
		{
			if(HoloEdit.smooth())
			{
				gl.glEnable(GL.GL_POINT_SMOOTH);
				gl.glEnable(GL.GL_LINE_SMOOTH);
				gl.glEnable(GL.GL_POLYGON_SMOOTH);
			}
			int begTime, endTime;
			if (!holoEditRef.shortViewMode)
			{
				begTime = holoEditRef.counterPanel.getDate(1);
				endTime = holoEditRef.counterPanel.getDate(2);
			}
			else
			{
				// Si on est en mode "Short View", 
				// on affiche debut +/- Delta si la piste est vide ˆ cet endroit
				// sinon fin du trajet en cours +/- delta
				begTime = holoEditRef.counterPanel.getDate(1);
				endTime = holoEditRef.counterPanel.getDate(2);
		
				HoloTrack at = holoEditRef.gestionPistes.getActiveTrack();
				int num = at.nextTraj2(begTime);
				if(num != -1)
				{
					HoloTraj atj = at.getHoloTraj(num);
					int num2 = at.prevTraj2(endTime);
					if(num2 != num)
						atj = at.getHoloTraj(num2);
					if(Ut.between(atj.getLastDate(), begTime, endTime))
					{
						begTime = atj.getLastDate() - holoEditRef.gestionPistes.delta;
						endTime = atj.getLastDate() + holoEditRef.gestionPistes.delta;
					} else {
						begTime = holoEditRef.counterPanel.getDate(1) - holoEditRef.gestionPistes.delta;
						endTime = holoEditRef.counterPanel.getDate(1) + holoEditRef.gestionPistes.delta;
					}
				} else {
					begTime = holoEditRef.counterPanel.getDate(1) - holoEditRef.gestionPistes.delta;
					endTime = holoEditRef.counterPanel.getDate(1) + holoEditRef.gestionPistes.delta;
				}
			}
			for (HoloTrack tk : holoEditRef.gestionPistes.tracks){
				tk.drawRoomRender3D(gl, holoEditRef.viewOnlyEditablePoints, begTime, endTime);
			}
			gl.glLoadIdentity();
			
			if(HoloEdit.smooth())
			{
				gl.glDisable(GL.GL_POINT_SMOOTH);
				gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glDisable(GL.GL_POLYGON_SMOOTH);
			}
		}

		private void getVars()
		{
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
			gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);
		}


	}

	public Room3DGUI(HoloEdit owner)
	{
		super("3D Room", owner, owner.wsRoomW3D, owner.wsRoomH3D, owner.wlRoomX3D, owner.wlRoomY3D, owner.wbRoom3D);
		setResizable(true);
		holoEditRef = owner;
		popup = new PopupMenu();
		reset = new MenuItem("Reset View");
		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetView();
			}
		});
		popup.add(reset);
		threed_glp = new Room3DCanvas(this);
		add(threed_glp, BorderLayout.CENTER);
		addKeyListener(threed_glp);
		threed_glp.requestFocus();
	}

	private void resetView()
	{
		xrot = 0;
		yrot = 0;
		zrot = 0;
		zzoom = -150;
		xtrans = 0;
		ytrans = 0;
		display();
	}

	public void set3DView(String view)
	{
		String[] viewArray = view.split(" ");
		if (viewArray.length != 6)
			return;
		xrot = Float.parseFloat(viewArray[0]);
		yrot = Float.parseFloat(viewArray[1]);
		zrot = Float.parseFloat(viewArray[2]);
		xtrans = Float.parseFloat(viewArray[3]);
		ytrans = Float.parseFloat(viewArray[4]);
		zzoom = Float.parseFloat(viewArray[5]);
	}

	private String get3DView()
	{
		return xrot + " " + yrot + " " + zrot + " " + xtrans + " " + ytrans + " " + zzoom;
	}

//	public void updateGUI()
//	{
//		// Constraints for having always a square drawing area
//		int w = sizW;
//		int h = sizH - 16;
//		int glpSize = Math.min(Math.max(w, h), 750);
//		sizW = glpSize;
//		sizH = glpSize + 16;
//		setSize(sizW, sizH);
//	}

	public void display()
	{
		if (visible)
			threed_glp.display();
	}

	public void open()
	{
		super.open();
		threed_glp.requestFocus();
	}

	public boolean hasFocus()
	{
		return super.hasFocus() || threed_glp.hasFocus();
	}
	
	public String toString()
	{
		return "\t<room3d view3D=\"" + get3DView() + "\"" + super.toString();
	}

	public void close()
	{
		super.close();
	}

	public void mouseDragged(MouseEvent e) {
		threed_glp.mouseDragged(e);
		
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
