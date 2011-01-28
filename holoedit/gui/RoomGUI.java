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
import holoedit.data.HoloPoint;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.WaveFormInstance;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.RoomIndex;
import holoedit.opengl.ScoreIndex;
import holoedit.opengl.TimeIndex;
import holoedit.util.Formatter;
import holoedit.util.IntegerVector;
import holoedit.util.Ut;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.IntBuffer;
import java.util.Vector;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

public class RoomGUI extends FloatingWindow
{
	private final static float ZOOM_IN_MIN_PAD = 10;
	private final int H_X_SCALE = 25;
	private final int H_TIME_SCALE = 25;
	private final int W_TIME_SCALE_MARGIN = 16;
	private final int W_TIME_SCALE_BF = 10;
	private final int H_X_SCROLL = 10;
	private final int H_BLANK_ZONE = 5;
	private final int W_Y_SCALE = 30;
	private final int W_Y_SCROLL = 10;
	private final static int MOUSE_SELECT_SIZE = 10;
	private float[] bgColor = { 0.95f, 0.95f, 0.95f, 1 };
	private float[] bgColor2 = { 1, 1, 1, 1 };
	private float mouseFollowLineWidth = 1;
	private float axesLineWidth = 1;
	private float selZoneLineWidth = 1;
	private float scaleLineWidth = 1;
	private float[] mouseFollowColor = { 0.5f, 0.5f, 0.5f, 0.5f };
	private float[] axes1Color = { 0.6f, 0.6f, 1, 0.75f };
	private float[] axes2Color = { 0.3f, 0.3f, 0.75f, 0.75f };
	private float[] speakerColor = { 0.7f, 0.7f, 0.7f, 1 };
	private float[] speaker2Color = { 0.5f, 0.5f, 0.5f, 1 };
	private float[] axes3Color = { 0, 0, 0, 1 };
	private float[] selZoneColor = { 0.5f, 0.5f, 0.5f, 0.1f };
	private float[] selZoneBorderColor = { 0, 0, 0.5f, 0.3f };
	private float[] borderColor = { 0.95f, 0.95f, 0.95f, 1 };
	private float[] scaleLineColor = { 0, 0, 0, 1 };
	private float[] scrollBgColor = { 0.5f, 0.5f, 0.5f, 1 };
	private float[] scrollSelBgColor = { 0.4f, 0.4f, 0.4f, 1 };
	private float[] scrollFgColor = { 0.8f, 0.8f, 0.8f, 1 };
	private float[] scrollSelFgColor = { 0.9f, 0.9f, 0.9f, 1 };
	private float[] timeSelColor = {0.8f,0.8f,0.8f,1};
	private float[] timeSelSelColor = {0.65f,0.65f,0.65f,1};
	private int[] viewport = new int[4];
	private double[] mvmatrix = new double[16];
	private double[] projmatrix = new double[16];
	private int width;
	private int height;
	private float minX = -51;
	private float maxX = 51;
	private float minY = -51;
	private float maxY = 51;
	private int minTime = 0;
	private int maxTime = 50000;
	private int totalTime = 0;
	private RoomGLCanvas proj_glp;
	private HoloEdit holoEditRef;
	private boolean drawMousePos = true;
	private boolean drawSelZone = false;
	private float mousex, mousey, mousex1, mousey1, mousex2, mousey2, posW = 0, posH = 0;
	private HoloPoint selZonePt1, selZonePt2;
	private Formatter rF = new Formatter(-1,-1,2,2);
	private Formatter rF2 = new Formatter(2,2,0,0);
	private int axesListId = 0;
	private int speakerListId = 0;
	private int selSpeakerListId = 0;
	private int scalexListId = 0;
	private int scaleyListId = 0;
	private int GLlistIDscalescroll = 0;
	private PopupMenu popup;
	private MenuItem reset;
	private boolean query_one_select = false;
	private boolean query_multi_select = false;
	private boolean query_speaker_select = false;
	private int selected = RoomIndex.getNull();
	private int scrollHSelected = RoomIndex.getNull();
	private int scrollVSelected = RoomIndex.getNull();
	private int speakerSelected = RoomIndex.getNull();
	private int scaleSelected = RoomIndex.getNull();
	private int scaleBackSelected = RoomIndex.getNull();
	private int scaleForwSelected = RoomIndex.getNull();
	public Vector<Integer> selIndex = new Vector<Integer>(5, 1);
	private HoloTrack currentTrack, currentTrack2, activ;
	private HoloTraj currentSeq;
	private HoloPoint currentPoint;
	private HoloSpeaker currentSpeaker;
	private HoloSpeaker currentSpeaker2;
	private int currentTrackNum;
	private int currentSeqNum;
	private int currentPointNum;
	private int currentSpeakerNum;
	private boolean draggedPoint = false;
	private boolean draggedPointZ = false;
	private boolean draggedPointS = false;
	private boolean draggedPointSZ = false;
	private boolean draggedSelZone = false;
	private boolean draggedView = false;
	private boolean draggedSpeaker = false;
	private boolean draggedSpeakerZ = false;
	private boolean draggedScrollH = false;
	private boolean draggedScrollV = false;
	private boolean draggedTimeScale = false;
	private boolean draggedTimeScaleSel = false;
	private boolean draggedTimeScaleBeg = false;
	private boolean draggedTimeScaleEnd = false;
	private boolean draggedPointRecord = false;
	@SuppressWarnings("unused")
	private float oldCurrentX, oldCurrentY, oldCurrentZ, oldCurrentDir, oldCurrentDist;
	private boolean selMode = false;
	private Thread status;
	private class RoomGLCanvas extends GLCanvas implements GLEventListener, MouseWheelListener, MouseListener, MouseMotionListener, KeyListener
	{
		private GL gl;
		private GLU glu = new GLU();
		private GLUT glut = new GLUT();
		private GLUquadric gluquad;
		private FloatingWindow fwRef;
		public int W, H;
		private int keyDown = -1;
		private IntBuffer selectBuf;
		public boolean scalescrollGUIdirty = true;
		Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		Cursor writeCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		

		RoomGLCanvas(FloatingWindow fw)
		{
			super(holoEditRef.glcap, null, holoEditRef.glpb.getContext(), null);
			fwRef = fw;
			addKeyListener(this);
			addMouseMotionListener(this);
			addMouseListener(this);
			addMouseWheelListener(this);
			addGLEventListener(this);
			addFocusListener(fwRef);
			add(popup);
			setFocusable(true);
			newStatus();
		}

		public void init(GLAutoDrawable drawable)
		{
			gl = drawable.getGL();
			glu = new GLU();
			gl.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]); // White Background
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
			H = height - (H_BLANK_ZONE + H_X_SCROLL + H_X_SCALE + H_TIME_SCALE);
			W = width - (W_Y_SCALE + W_Y_SCROLL);
			scalescrollGUIdirty = true;
			holoEditRef.rtDisplay.add(drawable);
		}

		@SuppressWarnings("deprecation")
		public void display(GLAutoDrawable drawable)
		{
			if(!visible)return;
			gl = drawable.getGL();
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glLoadIdentity();
			gl.glViewport(W_Y_SCALE, H_BLANK_ZONE + H_X_SCROLL + H_TIME_SCALE, W, H);
			glu.gluOrtho2D(minX, maxX, minY, maxY);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glColor4fv(bgColor2, 0);
			gl.glRectf(minX, minY, maxX, maxY);
			getVars();
			drawRules();
			drawMousePos();
			if(!draggedSpeaker && !draggedSpeakerZ && !draggedScrollV && !draggedScrollH && !draggedPoint
					&& !draggedPointZ && !draggedPointS && !draggedPointSZ && !draggedView && !draggedTimeScale && !draggedTimeScaleSel && !draggedTimeScaleBeg && !draggedTimeScaleEnd) 
			{
				if (query_one_select)
				{
					getObjectFromMouse();
					query_one_select = false;
				}
				else if (query_multi_select)
				{
					getObjectsFromMouseSel();
					query_multi_select = false;
				}
				else if (query_speaker_select)
				{
					getSpeakerFromMouse();
					query_speaker_select = false;
				}
			}
			drawSpeakers(true);
			drawTracks(true, false);
			drawSelZone();
			// DRAW INFOS
			drawScalesAndScrolls();
			if(status != null && status.getState().equals(Thread.State.RUNNABLE))
			{
				status.stop();
				newStatus();
			}
		}

		public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
		{
			width = w;
			height = h;
			H = height - (H_BLANK_ZONE + H_X_SCROLL + H_X_SCALE + H_TIME_SCALE);
			W = width - (W_Y_SCALE + W_Y_SCROLL);
			gl.glDeleteLists(axesListId, 1);
			axesListId = 0;
			scalescrollGUIdirty = true;
		}

		public void displayChanged(GLAutoDrawable drawable, boolean arg1, boolean arg2)
		{
		}

		public void mouseWheelMoved(MouseWheelEvent e)
		{
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			float currentZoomX = convPosW(posW);
			float padMinX = (currentZoomX - minX);
			float padMaxX = (maxX - currentZoomX);
			float currentZoomY = convPosH(posH);
			float padMinY = (currentZoomY - minY);
			float padMaxY = (maxY - currentZoomY);
			float zoom_factor = (1 - (e.getUnitsToScroll() / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)));
			padMinX = Ut.clipL(padMinX * zoom_factor, ZOOM_IN_MIN_PAD);
			padMaxX = Ut.clipL(padMaxX * zoom_factor, ZOOM_IN_MIN_PAD);
			padMinY = Ut.clipL(padMinY * zoom_factor, ZOOM_IN_MIN_PAD);
			padMaxY = Ut.clipL(padMaxY * zoom_factor, ZOOM_IN_MIN_PAD);
			minX = Ut.clip(currentZoomX - padMinX, -820, 820);
			maxX = Ut.clip(currentZoomX + padMaxX, -820, 820);
			minY = Ut.clip(currentZoomY - padMinY, -820, 820);
			maxY = Ut.clip(currentZoomY + padMaxY, -820, 820);
			if ((maxY - minY) != (maxX - minX))
				maxY = minY + (maxX - minX);
			selected = ScoreIndex.getNull();
			if (holoEditRef.hpEditMode)
				query_speaker_select = true;
			else query_one_select = true;
			scalescrollGUIdirty = true;
			display();
		}

		public void mouseClicked(MouseEvent e)
		{
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			if(scrollHSelected != RoomIndex.getNull() || scrollVSelected != RoomIndex.getNull() || scaleSelected != RoomIndex.getNull())
			{
			} else if (e.getClickCount() >= 2) {
				selMode = !e.isShiftDown();
				int begTime, endTime;
				if (!holoEditRef.shortViewMode)
				{
					begTime = holoEditRef.counterPanel.getDate(1);
					endTime = holoEditRef.counterPanel.getDate(2);
				} else {
					// Si on est en mode "Short View", 
					// on affiche debut +/- Delta si la piste est vide ˆ cet endroit
					// sinon fin du trajet en cours +/- delta
					begTime = holoEditRef.counterPanel.getDate(1);
					endTime = holoEditRef.counterPanel.getDate(2);
			
					HoloTrack at = holoEditRef.gestionPistes.getActiveTrack();
					if(at == null)
					{
						holoEditRef.gestionPistes.setActiveTrack(1);
						at = holoEditRef.gestionPistes.getActiveTrack();
					}
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
				selIndex = new Vector<Integer>(5, 1);
				if (holoEditRef.allTrackActive)
					for (int i = 0, last = holoEditRef.gestionPistes.tracks.size(); i < last; i++)
					{
						selIndex.addAll(holoEditRef.gestionPistes.tracks.get(i).getAllRoomPoints(i, begTime, endTime, selMode || holoEditRef.viewOnlyEditablePoints));
						holoEditRef.gestionPistes.tracks.get(i).setDirty(true);
					}
				else 
				{
					selIndex.addAll(holoEditRef.gestionPistes.getActiveTrack().getAllRoomPoints(holoEditRef.gestionPistes.getActiveTrackNb(), begTime, endTime, selMode || holoEditRef.viewOnlyEditablePoints));
					holoEditRef.gestionPistes.getActiveTrack().setDirty(true);
				}
				
			}
			else
			{
				selIndex = new Vector<Integer>(5, 1);
				selected = RoomIndex.getNull();
				holoEditRef.gestionPistes.setDirty(true);
			}
			display();
		}

		public void mouseEntered(MouseEvent e)
		{
			if(holoEditRef.connection.isRecording())
				setCursor(writeCursor);
			else
				setCursor(defaultCursor);
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mouseMoved(MouseEvent e)
		{
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			if(holoEditRef.hpEditMode)
				query_speaker_select = true;
			else
				query_one_select = true;
			display();
		}

		public void mousePressed(MouseEvent e)
		{
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			
			if(scrollHSelected != RoomIndex.getNull())
			{
				float pad = (maxX-minX)/2;
				RoomIndex.decode(scrollHSelected);
				if(RoomIndex.isScrollHLeft())
				{
					minX -= pad;
					maxX -= pad;
					if(minX < -820)
					{
						minX = -820;
						maxX = -820 + (2 * pad);
					}
				} else if(RoomIndex.isScrollHRight())
				{
					minX += pad;
					maxX += pad;
					if(maxX > 820)
					{
						minX = 820 - (2 * pad);
						maxX = 820;
					}
				} else if(RoomIndex.isScrollH())
				{
					draggedScrollH = true;
					drawMousePos = false;
					oldCurrentX = mousex;
				}
				scalescrollGUIdirty = true;
			} else if(scrollVSelected != RoomIndex.getNull()) {
				float pad = (maxY-minY)/2;
				RoomIndex.decode(scrollVSelected);
				if(RoomIndex.isScrollVLeft())
				{
					minY -= pad;
					maxY -= pad;
					if(minY < -820)
					{
						minY = -820;
						maxY = -820 + (2*pad);
					}
				} else if(RoomIndex.isScrollVRight())
				{
					minY += pad;
					maxY += pad;
					if(maxY > 820)
					{
						minY = 820 - (2*pad);
						maxY = 820;
					}
				} else if(RoomIndex.isScrollV())
				{
					draggedScrollV = true;
					drawMousePos = false;
					oldCurrentY = mousey;
				}
				scalescrollGUIdirty = true;
			} else if(scaleBackSelected != RoomIndex.getNull()) {
				RoomIndex.decode(scaleBackSelected);
				if(RoomIndex.isTimeScaleBack())
					holoEditRef.counterPanel.setBegAndEnd(Ut.clipL(holoEditRef.counterPanel.getDate(1)-holoEditRef.gestionPistes.delta,0),Ut.clipL(holoEditRef.counterPanel.getDate(2)-holoEditRef.gestionPistes.delta,holoEditRef.counterPanel.getDate(2)-holoEditRef.counterPanel.getDate(1)));
			} else if(scaleForwSelected != RoomIndex.getNull()) {
				RoomIndex.decode(scaleForwSelected);
				if(RoomIndex.isTimeScaleForw())
						holoEditRef.counterPanel.setBegAndEnd(holoEditRef.counterPanel.getDate(1)+holoEditRef.gestionPistes.delta,holoEditRef.counterPanel.getDate(2)+holoEditRef.gestionPistes.delta);
			} else if(scaleSelected != RoomIndex.getNull()) {
				RoomIndex.decode(scaleSelected);
				if(e.getClickCount() >= 2)
					holoEditRef.counterPanel.setBegAndEnd(0,totalTime);
				else if(RoomIndex.isTimeScaleBg())
				{
					oldCurrentX = convPosTime(e.getX());
					draggedTimeScale = true;
					int init = Ut.clipL((int)oldCurrentX,0);
					holoEditRef.counterPanel.setBegAndEnd(init,init);
				} else if(RoomIndex.isTimeScaleBeg())
				{
					draggedTimeScaleBeg = true;
					oldCurrentX = convPosTime(e.getX());
				} else if(RoomIndex.isTimeScaleEnd())
				{
					draggedTimeScaleEnd = true;
					oldCurrentX = convPosTime(e.getX());
				} else if(RoomIndex.isTimeScaleBloc())
				{	
					if(e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
					{
						draggedTimeScale = true;
						oldCurrentX = convPosTime(e.getX());
						int init = Ut.clipL((int)oldCurrentX,0);
						holoEditRef.counterPanel.setBegAndEnd(init,init);
					} else {
						draggedTimeScaleSel = true;
						oldCurrentX = convPosTime(e.getX());
					}
				} 
			} else if (holoEditRef.connection.isRecording())
			{
				// RECORD POINT
				draggedPointRecord = true;
				drawMousePos = false;
				oldCurrentX = mousex;
				oldCurrentY = mousey;
				
				HoloPoint newPoint = convPosPt(posW, posH);
				newPoint.setEditable(false);
				holoEditRef.connection.treatRecordPoint(newPoint);
				
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
				
			} else if(speakerSelected != RoomIndex.getNull()) {
				RoomIndex.decode(speakerSelected);
				currentSpeakerNum = RoomIndex.getPt();
				currentSpeaker = holoEditRef.gestionPistes.speakers.get(currentSpeakerNum);
				if((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3)
				{
					// SPEAKER TEXT EDITOR
					new SpeakerEditor(currentSpeakerNum, new Point(e.getX(),e.getY()), holoEditRef.gestionPistes);
				}
				else if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				{
					// DELETE CURRENT SPEAKER
					holoEditRef.gestionPistes.speakers.remove(currentSpeakerNum);
					speakerSelected = RoomIndex.getNull();
					holoEditRef.modify();
				}
				else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.isShiftDown())
				{
					holoEditRef.modify();
					oldCurrentZ = convPosZ(posH);
					draggedSpeakerZ = true;
				}
				else
				{
					holoEditRef.modify();
					currentSpeaker.recalcDist();
					oldCurrentX = currentSpeaker.X;
					oldCurrentY = currentSpeaker.Y;
					oldCurrentDist = currentSpeaker.dist;
					oldCurrentDir = currentSpeaker.dir;
					draggedSpeaker = true;
				}
			} else {
				RoomIndex.decode(selected);
				if (RoomIndex.isPoint())
				{
					currentTrackNum = RoomIndex.getTrack();
					currentTrack = holoEditRef.gestionPistes.tracks.get(currentTrackNum);
					currentSeqNum = RoomIndex.getSeq();
					currentSeq = currentTrack.getHoloTraj(currentSeqNum);
					currentPointNum = RoomIndex.getPt();
					currentPoint = currentSeq.points.get(currentPointNum);
					if (holoEditRef.allTrackActive)
						holoEditRef.gestionPistes.selectTrack(currentTrackNum);
					if (!selIndex.isEmpty() && selIndex.contains(selected))
					{
						// MULTI SELECT
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							// REMOVE POINTS
							holoEditRef.gestionPistes.StoreToUndoAll();
							removePoints();
						}
						else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.isShiftDown())
						{
							holoEditRef.gestionPistes.StoreToUndoAll();
							draggedPointSZ = true;
							oldCurrentZ = convPosZ(posH);
						}
						else
						{
							// DISPLACE POINTS
							holoEditRef.gestionPistes.StoreToUndoAll();
							draggedPointS = true;
							oldCurrentX = currentPoint.getX();
							oldCurrentY = currentPoint.getY();
						}
					}
					else
					{
						// ONE POINT SELECT
						selIndex = new Vector<Integer>(5, 1);
						if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && !e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							// CHANGE EDITABLE
							holoEditRef.gestionPistes.StoreToUndo(currentTrackNum);
							currentPoint.setEditable(!currentPoint.isEditable());
						}
						else if (e.isAltDown() && !e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							// REMOVE POINT
							holoEditRef.gestionPistes.StoreToUndo(currentTrackNum);
							currentSeq.removeElementAtReal(currentPointNum);
							currentTrack.update();
							selected = RoomIndex.getNull();
						}
						else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.isShiftDown())
						{
							holoEditRef.gestionPistes.StoreToUndoAll();
							draggedPointZ = true;
							oldCurrentZ = convPosZ(posH);
						}
						else if ((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3)
						{
							// SHOW POINT TEXT EDITOR
							new HoloPointEditor(selected, new Point(e.getX(), e.getY()), fwRef, holoEditRef);
						}
						else
						{
							// DISPLACE POINT
							holoEditRef.gestionPistes.StoreToUndo(currentTrackNum);
							draggedPoint = true;
							oldCurrentX = currentPoint.getX();
							oldCurrentY = currentPoint.getY();
						}
					}
				}
				else if (RoomIndex.isLine())
				{
					// MOUSE ON SEGMENT
					if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && !(e.getButton() == MouseEvent.BUTTON3) && !holoEditRef.viewOnlyEditablePoints)
					{
						// NEW POINT ON SEGMENT
						currentTrackNum = RoomIndex.getTrack();
						currentTrack = holoEditRef.gestionPistes.tracks.get(currentTrackNum);
						currentSeqNum = RoomIndex.getSeq();
						currentSeq = currentTrack.getHoloTraj(currentSeqNum);
						currentPointNum = RoomIndex.getPt();
						currentPoint = currentSeq.points.get(currentPointNum);
						HoloPoint newPoint = convPosPt(posW, posH);
						insertPointOnLine(newPoint);
					}
					else
					{
						// PREPARE SELECTION
						prepMultiSel(e.isShiftDown());
					}
				}
				else if ((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3)
				{
					// POPUP
					popup.show(this, e.getX(), e.getY());
				}
				else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)))
				{
					if(!holoEditRef.hpEditMode)
					{
						// ADD A POINT
						holoEditRef.gestionPistes.addPoint(convPosPt(posW, posH));
					}
					else
					{
						// ADD A SPEAKER
						HoloSpeaker hs = new HoloSpeaker(convPosW(posW),convPosH(posH),holoEditRef.gestionPistes.speakers.size()+1);
						holoEditRef.gestionPistes.speakers.add(hs);
						holoEditRef.modify();
					}
				}
				else if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				{
					// DEPLACEMENT DE LA VUE
					draggedView = true;
					drawMousePos = false;
					oldCurrentX = mousex;
					oldCurrentY = mousey;
				}
				else if(!holoEditRef.hpEditMode)
				{
					// PREPARE SELECTION
					prepMultiSel(e.isShiftDown());
				}
			}
			disp();
		}

		public void mouseDragged(MouseEvent e)
		{
			boolean antiDispRecord = false;
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			
			if(draggedPointRecord)
			{
				// TODO : RECORD DANS ROOM
				HoloPoint newPoint = convPosPt(posW, posH);
				newPoint.setEditable(false);
				holoEditRef.connection.treatRecordPoint(newPoint);
				
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
				
				antiDispRecord = true; // prevent from drawing each drag event when recording
			}
			else if (draggedPoint)
			{
				HoloPoint newPoint = convPosPt(posW, posH);
				if (currentPoint.isEditable())
				{
					if (e.isShiftDown())
						newPoint.setX(oldCurrentX);
					else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)))
						newPoint.setY(oldCurrentY);
					currentSeq.calcNewPosSeg(currentPointNum, newPoint);
				}
				else
				{
					if (e.isShiftDown())
					{
						currentPoint.setX(oldCurrentX);
						currentPoint.setY(newPoint.getY());
					}
					else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)))
					{
						currentPoint.setX(newPoint.getX());
						currentPoint.setY(oldCurrentY);
					}
					else
					{
						currentPoint.setX(newPoint.getX());
						currentPoint.setY(newPoint.getY());
					}
				}
				
				currentTrack.setDirty(Ut.DIRTY_ROOM);
			}
			else if (draggedPointZ)
			{
				float Z = convPosZ(posH);
				float dZ = Z - oldCurrentZ;
				if (currentPoint.isEditable())
					currentSeq.calcNewPosSegZ(currentPointNum, dZ);
				else
					currentPoint.translaterZ(dZ);
				oldCurrentZ = Z;
				
				currentTrack.setDirty(Ut.DIRTY_ROOM);
			}
			else if (draggedSelZone)
			{
				mousex2 = mousex;
				mousey2 = mousey;
				selIndex = new Vector<Integer>(5, 1);
				selZonePt2 = convPosPt(posW, posH);
				query_multi_select = true;
				selMode = !e.isShiftDown();
				
				//mainRef.gestionPistes.setDirty(true);
			}
			else if (draggedPointS)
			{
				if (!selMode)
				{
					float ocx = currentPoint.getX();
					float ocy = currentPoint.getY();
					float ncx = convPosW(posW);
					float ncy = convPosH(posH);
					float dX = ncx - ocx, dY = ncy - ocy;
					if (e.isShiftDown())
						dX = oldCurrentX - ocx;
					else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && !(e.getButton() == MouseEvent.BUTTON3))
						dY = oldCurrentY - ocy;
					if (holoEditRef.viewOnlyEditablePoints)
					{
						HoloTraj eT;
						int ePNum;
						for (int i = 0, last = selIndex.size(); i < last; i++)
						{
							RoomIndex.decode(selIndex.get(i));
							eT = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
							ePNum = RoomIndex.getPt();
							if (eT.points.get(ePNum).isEditable())
							{
								eT.calcNewPosSeg(ePNum, dX, dY);
								eT.setDirty(Ut.DIRTY_ROOM);
							}
						}
					}
					else
					{
						HoloTraj eT;
						for (int i = 0, last = selIndex.size(); i < last; i++)
						{
							RoomIndex.decode(selIndex.get(i));
							eT = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
							eT.points.get(RoomIndex.getPt()).translater(dX, dY);
							eT.setDirty(Ut.DIRTY_ROOM);
							
						}
					}
				}
				else
				{
					HoloPoint newPoint = convPosPt(posW, posH);
					if (e.isShiftDown())
						newPoint.setX(oldCurrentX);
					else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)))
						newPoint.setY(oldCurrentY);
					dragSelIndex(newPoint);
				}
				
			}
			else if (draggedPointSZ)
			{
				float Z = convPosZ(posH);
				float dZ = Z - oldCurrentZ;
				if (!selMode)
				{
					if (holoEditRef.viewOnlyEditablePoints)
					{
						HoloTraj eT;
						int ePNum;
						for (int i = 0, last = selIndex.size(); i < last; i++)
						{
							RoomIndex.decode(selIndex.get(i));
							eT = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
							ePNum = RoomIndex.getPt();
							if (eT.points.get(ePNum).isEditable())
								eT.calcNewPosSegZ(ePNum, dZ);
						}
					}
					else
					{
						HoloTraj eT;
						for (int i = 0, last = selIndex.size(); i < last; i++)
						{
							RoomIndex.decode(selIndex.get(i));
							eT = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
							eT.points.get(RoomIndex.getPt()).translaterZ(dZ);
							eT.setDirty(true);
						}
					}
				}
				else dragSelIndexZ(dZ);
				oldCurrentZ = Z;
			} else if(draggedScrollH)
			{
				float padPerCent = (mousex - oldCurrentX) / W;
				float oldZoom = (maxX - minX);
				float pad = 1640 * padPerCent;
				minX = minX + pad;
				maxX = maxX + pad;
				if (minX < -820)
				{
					minX = -820;
					maxX = -820 + oldZoom;
				}
				else if (maxX > 820)
				{
					maxX = 820;
					minX = 820 - oldZoom;
				}
				oldCurrentX = mousex;
				scalescrollGUIdirty = true;
			} else if(draggedScrollV)
			{
				float padPerCent = (mousey - oldCurrentY) / H;
				float oldZoom = (maxY - minY);
				float pad = 1640 * padPerCent;
				minY = minY + pad;
				maxY = maxY + pad;
				if (minY < -820)
				{
					minY = -820;
					maxY = -820 + oldZoom;
				}
				else if (maxY > 820)
				{
					maxY = 820;
					minY = 820 - oldZoom;
				}
				oldCurrentY = mousey;
				scalescrollGUIdirty = true;
			} else if(draggedView)
			{
				float padPerCentX = (mousex - oldCurrentX) / W;
				float padPerCentY = (mousey - oldCurrentY) / H;
				float oldZoomX = (maxX - minX);
				float oldZoomY = (maxY - minY);
				float padX = (maxX - minX) * padPerCentX;
				float padY = (maxY - minY) * padPerCentY;
				minX -= padX;
				maxX -= padX;
				minY -= padY;
				maxY -= padY;
				
				if (minX < -820)
				{
					minX = -820;
					maxX = -820 + oldZoomX;
				}
				else if (maxX > 820)
				{
					maxX = 820;
					minX = 820 - oldZoomX;
				}
				if (minY < -820)
				{
					minY = -820;
					maxY = -820 + oldZoomY;
				}
				else if (maxY > 820)
				{
					maxY = 820;
					minY = 820 - oldZoomY;
				}
				
				oldCurrentX = mousex;
				oldCurrentY = mousey;
				scalescrollGUIdirty = true;
			} else if(draggedSpeaker)
			{
				HoloPoint newPoint = convPosPt(posW, posH);
				// FEATURE ROOM PROJ DISPLACE SPEAKER DRAGGED CONSTANT DIST/DIR
				// if(e.isAltDown())
				// {
				// currentSpeaker.dir = oldCurrentDir;
				// currentSpeaker.translatePolDist(convPosH(posH));
				// } else if ((e.isControlDown() && Main.MAC)) {
				// currentSpeaker.dist = oldCurrentDist;
				// currentSpeaker.translatePolDir(convPosH(posH)/10);
				// } else
				if (e.isShiftDown()) {
					currentSpeaker.X = oldCurrentX;
					currentSpeaker.Y = newPoint.getY();
				} else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))) {
					currentSpeaker.X = newPoint.getX();
					currentSpeaker.Y = oldCurrentY;
				} else {
					currentSpeaker.X = newPoint.getX();
					currentSpeaker.Y = newPoint.getY();
				}
				currentSpeaker.recalcDist();
			} else if(draggedSpeakerZ)
			{
				float dZ = convPosZ(posH) - oldCurrentZ;
				currentSpeaker.Z = Ut.clip(currentSpeaker.Z + dZ, 0, 100);
				currentSpeaker.recalcDist();
			} else if(draggedTimeScale)
			{
				float X = convPosTime(e.getX());
				float dX = X - oldCurrentX;
				if(dX > 0)
					holoEditRef.counterPanel.setCompteur(2,Ut.clipL((int)X,0));
				else
					holoEditRef.counterPanel.setBegAndEnd(Ut.clipL((int)X,0),Ut.clipL((int)oldCurrentX,0));
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
			} else if(draggedTimeScaleBeg)
			{
				float X = convPosTime(e.getX());
				float dX = X - oldCurrentX;
				if(e.isShiftDown())
					dX *= 0.1;
				int n = holoEditRef.counterPanel.getDate(1)+(int)dX;
				if(n <= holoEditRef.counterPanel.getDate(2))
					holoEditRef.counterPanel.setCompteur(1,n);
				else
				{
					holoEditRef.counterPanel.setCompteur(1,holoEditRef.counterPanel.getDate(2));
					holoEditRef.counterPanel.setCompteur(2,Ut.clipL(n,0));
					draggedTimeScaleBeg = false;
					draggedTimeScaleEnd = true;
					scaleSelected = RoomIndex.TIMESCALE_END_IND;
				}
				oldCurrentX = X;
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
			} else if(draggedTimeScaleEnd)
			{
				float X = convPosTime(e.getX());
				float dX = X - oldCurrentX;
				if(e.isShiftDown())
					dX *= 0.1;
				int n = holoEditRef.counterPanel.getDate(2)+(int)dX;
				if(n >= holoEditRef.counterPanel.getDate(1))
					holoEditRef.counterPanel.setCompteur(2,n);
				else
				{	
					holoEditRef.counterPanel.setCompteur(2,holoEditRef.counterPanel.getDate(1));
					holoEditRef.counterPanel.setCompteur(1,Ut.clipL(n,0));
					draggedTimeScaleBeg = true;
					draggedTimeScaleEnd = false;
					scaleSelected = RoomIndex.TIMESCALE_BEG_IND;
				}
				oldCurrentX = X;
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
			} else if(draggedTimeScaleSel)
			{
				float X = convPosTime(e.getX());
				float dX = X - oldCurrentX;
				if(e.isShiftDown())
					dX *= 0.1;
				holoEditRef.counterPanel.setBegAndEnd(Ut.clipL(holoEditRef.counterPanel.getDate(1)+(int)dX,0),Ut.clipL(holoEditRef.counterPanel.getDate(2)+(int)dX,0));
				oldCurrentX = X;
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
			}
			
			if( ! antiDispRecord )
				disp();
		}

		public void mouseReleased(MouseEvent e)
		{
			mousex = e.getX();
			mousey = height - e.getY();
			posW = e.getX() - W_Y_SCALE;
			posH = (height - H_BLANK_ZONE - H_X_SCROLL - H_TIME_SCALE) - e.getY();
			
			if(draggedPointRecord)
			{
				HoloPoint newPoint = convPosPt(posW, posH);
				newPoint.setEditable(false);
				holoEditRef.connection.treatRecordPoint(newPoint);
				
				holoEditRef.connection.treatRecordSegmentAll();
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
			}
			
			if(draggedSelZone && selMode)
			{
				treatSelIndex();
				holoEditRef.gestionPistes.setDirty(true);
			}

			if(draggedPoint || draggedPointZ || draggedPointS || draggedPointSZ || draggedTimeScale || draggedTimeScaleSel || draggedTimeScaleBeg || draggedTimeScaleEnd)
				holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM3D | Ut.DIRTY_SCORE | Ut.DIRTY_TIME);
			draggedPoint = false;
			draggedPointZ = false;
			draggedSelZone = false;
			drawSelZone = false;
			draggedPointS = false;
			draggedPointSZ = false;
			draggedScrollH = false;
			draggedScrollV = false;
			draggedView = false;
			draggedSpeaker = false;
			draggedSpeakerZ = false;
			draggedTimeScale = false;
			draggedTimeScaleSel = false;
			draggedTimeScaleBeg = false;
			draggedTimeScaleEnd = false;
			draggedPointRecord = false;
			drawMousePos = true;
			
			disp();
		}

		public void keyPressed(KeyEvent e)
		{
			boolean close = false;
			float pad = e.isShiftDown() ? 1 : 10;
			float oldMinX = minX;
			float oldMaxX = maxX;
			float oldMinY = minY;
			float oldMaxY = maxY;
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DOWN:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					minX -= pad;
					maxX += pad;
					minY -= pad;
					maxY += pad;
					break;
				case KeyEvent.VK_D:
					minY -= pad;
					maxY -= pad;
					break;
				}
				break;
			case KeyEvent.VK_UP:
				switch(keyDown)
				{
				case KeyEvent.VK_Z:
					minX += pad;
					maxX -= pad;
					minY += pad;
					maxY -= pad;
					if (minX >= maxX)
					{
						minX = oldMinX;
						maxX = oldMaxX;
						minY = oldMinY;
						maxY = oldMaxY;
					}
					break;
				case KeyEvent.VK_D:
					minY += pad;
					maxY += pad;	
					break;
				}
				break;
			case KeyEvent.VK_LEFT:
				if(keyDown == KeyEvent.VK_D)
				{
					minX -= pad;
					maxX -= pad;
				}
				break;
			case KeyEvent.VK_RIGHT:
				if(keyDown == KeyEvent.VK_D)
				{
					minX += pad;
					maxX += pad;
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
			minX = Ut.clip(minX, -820, 820);
			maxX = Ut.clip(maxX, -820, 820);
			minY = Ut.clip(minY, -820, 820);
			maxY = Ut.clip(maxY, -820, 820);
			scalescrollGUIdirty = true;
			if (!close)
				display();
			else {
				close();
				holoEditRef.checkWindows();
			}
		}

		public void keyTyped(KeyEvent e) {}

		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == keyDown)
				keyDown = -1;
		}

		private void getObjectFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			glu.gluOrtho2D(minX, maxX, minY, maxY);
			drawTracks(false, false);
			gl.glPopMatrix();
			gl.glFlush();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				selected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						selected = selectBuf.get(offset);
					offset++;
				}
			}
		}

		private void getSpeakerFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			glu.gluOrtho2D(minX, maxX, minY, maxY);
			drawSpeakers(false);
			gl.glPopMatrix();
			gl.glFlush();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				speakerSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
					{
						speakerSelected = selectBuf.get(offset);
					}
					offset++;
				}
			}
		}

		private void getObjectsFromMouseSel()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(4096);//ByteBuffer.allocateDirect(10000).asIntBuffer();
			double midPtX = (mousex1 + mousex2) / 2;
			double midPtY = (mousey1 + mousey2) / 2;
			double w = Math.abs(mousex2 - mousex1);
			double h = Math.abs(mousey2 - mousey1);
			if (w != 0 && h != 0)
			{
				gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
				gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
				gl.glRenderMode(GL.GL_SELECT);
				gl.glInitNames();
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPushName(-1);
				gl.glPushMatrix();
				gl.glLoadIdentity();
				// !!! leave gluPickMatrix after glloadidentity
				glu.gluPickMatrix(midPtX, midPtY, w, h, vPort, 0);
				// !!! leave gluOrtho after glupickmatrix
				glu.gluOrtho2D(minX, maxX, minY, maxY);
				drawTracks(false, true);
				gl.glPopMatrix();
				hits = gl.glRenderMode(GL.GL_RENDER);
				if (hits == 0)
				{
					selected = RoomIndex.getNull();
					selIndex = new Vector<Integer>(5, 1);
					return;
				}
				int offset = 0;
				int names = -1;
				for (int i = 0; i < hits; i++)
				{
					names = selectBuf.get(offset);
					offset++;
					offset++;
					offset++;
					for (int j = 0; j < names; j++)
					{
						if (j == (names - 1))
						{
							int p = selectBuf.get(offset);
							if (p >= 0)
							{
								selIndex.add(p);
							}
						}
						offset++;
					}
				}
			}
		}

		private void getScrollHFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			drawXScroll(false);
			gl.glLoadIdentity();
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				scrollHSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						scrollHSelected = selectBuf.get(offset);
					offset++;
				}
			}
		}
		
		private void getScrollVFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			drawYScroll(false);
			gl.glLoadIdentity();
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				scrollVSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						scrollVSelected = selectBuf.get(offset);
					offset++;
				}
			}
		}
		
		private void getScaleFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			drawTimeScale(false);
			gl.glLoadIdentity();
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				scaleSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						scaleSelected = selectBuf.get(offset);
					offset++;
				}
			}
		}
		
		private void getScaleBackFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			drawScaleBack(false);
			gl.glLoadIdentity();
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
			if (hits == 0)
			{
				scaleBackSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						scaleBackSelected = selectBuf.get(offset);
					offset++;
				}
			}
		}
		
		private void getScaleForwFromMouse()
		{
			int hits = 0;
			int[] vPort = new int[4];
			selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
			gl.glGetIntegerv(GL.GL_VIEWPORT, vPort, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
			glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
			// !!! leave gluOrtho after glupickmatrix
			drawScaleForw(false);
			gl.glLoadIdentity();
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
			// System.out.println("scroll H hits : "+hits);
			if (hits == 0)
			{
				scaleForwSelected = RoomIndex.getNull();
				return;
			}
			int offset = 0;
			int names = -1;
			for (int i = 0; i < hits; i++)
			{
				names = selectBuf.get(offset);
				offset++;
				offset++;
				offset++;
				for (int j = 0; j < names; j++)
				{
					if (j == (names - 1))
						scaleForwSelected = selectBuf.get(offset);
					offset++;
				}
			}
		}
		
		private void drawScalesAndScrolls()
		{
			boolean select = !draggedSpeaker && !draggedSpeakerZ && !draggedScrollV && !draggedScrollH && !draggedPoint
			&& !draggedPointZ && !draggedPointS && !draggedPointSZ && !draggedView && !draggedTimeScale && !draggedTimeScaleSel
			&& !draggedTimeScaleBeg && !draggedTimeScaleEnd;
			
			// RENDER SELECT
			gl.glViewport(W_Y_SCALE, H_BLANK_ZONE + H_TIME_SCALE, width - W_Y_SCALE - W_Y_SCROLL, H_X_SCROLL);
			if(select) 
				getScrollHFromMouse();
			gl.glViewport(width - W_Y_SCROLL, H_X_SCROLL + H_BLANK_ZONE + H_TIME_SCALE, W_Y_SCROLL, height - (H_X_SCROLL + H_BLANK_ZONE + H_X_SCALE + H_TIME_SCALE));
			if(select) 
				getScrollVFromMouse();
			
			if(scalescrollGUIdirty)
			{
				GLlistIDscalescroll = gl.glGenLists(1);
				gl.glNewList(GLlistIDscalescroll, GL.GL_COMPILE);
				// DRAW X SCALE
				drawXScale(true);
				// DRAW X SCROLL
				gl.glViewport(W_Y_SCALE, H_BLANK_ZONE + H_TIME_SCALE, width - W_Y_SCALE - W_Y_SCROLL, H_X_SCROLL);
				drawXScroll(true);
				// DRAW Y SCALE
				drawYScale(true);
				// DRAW Y SCROLL
				gl.glViewport(width - W_Y_SCROLL, H_X_SCROLL + H_BLANK_ZONE + H_TIME_SCALE, W_Y_SCROLL, height - (H_X_SCROLL + H_BLANK_ZONE + H_X_SCALE + H_TIME_SCALE));
				drawYScroll(true);
				gl.glEndList();
				scalescrollGUIdirty = false;
			}
			gl.glPushMatrix();
			gl.glCallList(GLlistIDscalescroll);
			gl.glPopMatrix();

			// DRAW TIME SCALE
			gl.glViewport(W_TIME_SCALE_MARGIN/2, 1, W_TIME_SCALE_BF, H_TIME_SCALE-2);
			if(select) 
				getScaleBackFromMouse();
			drawScaleBack(true);
			gl.glViewport(width - (W_TIME_SCALE_MARGIN+W_TIME_SCALE_BF) + W_TIME_SCALE_MARGIN/2, 1, W_TIME_SCALE_BF, H_TIME_SCALE-2);
			if(select) 
				getScaleForwFromMouse();
			drawScaleForw(true);
			gl.glViewport(W_TIME_SCALE_MARGIN+W_TIME_SCALE_BF, 1, width - (2*(W_TIME_SCALE_MARGIN+W_TIME_SCALE_BF)), H_TIME_SCALE-2);
			totalTime = holoEditRef.connection.getTotalTime();
			for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
			{
				totalTime = Ut.max(tk.getLastDate(),totalTime);
				if(!tk.waves.isEmpty())
					for(WaveFormInstance w:tk.waves)
						totalTime = Ut.max(totalTime,w.getLastDate());
			}
			minTime = 0;
			maxTime = Ut.max(totalTime,1000);
			holoEditRef.counterPanel.setCompteur(3,totalTime);
			if(select) 
				getScaleFromMouse();
			drawTimeScale(true);
		}

		private void drawScaleBack(boolean render)
		{
			glu.gluOrtho2D(0, 1, 0, 1);
			if(scaleBackSelected == RoomIndex.TIMESCALE_BACK_IND)
				OpenGLUt.glColor(gl,Color.GRAY.darker());
			else
				OpenGLUt.glColor(gl,Color.GRAY);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glLoadName(RoomIndex.TIMESCALE_BACK_IND);
			gl.glBegin(GL.GL_POLYGON);
				gl.glVertex2f(0,0.5f);
				gl.glVertex2f(1,0.75f);
				gl.glVertex2f(1,0.25f);
			gl.glEnd();
			if(render)
			{
				gl.glLineWidth(1);
				OpenGLUt.glColor(gl,Color.BLACK);
				gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(0,0.5f);
				gl.glVertex2f(1,0.75f);
				gl.glVertex2f(1,0.25f);
				gl.glEnd();
			}
			gl.glLoadIdentity();
		}

		private void drawScaleForw(boolean render)
		{
			glu.gluOrtho2D(0, 1, 0, 1);
			if(scaleForwSelected == RoomIndex.TIMESCALE_FORW_IND)
				OpenGLUt.glColor(gl,Color.GRAY.darker());
			else
				OpenGLUt.glColor(gl,Color.GRAY);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glLoadName(RoomIndex.TIMESCALE_FORW_IND);
			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex2f(1,0.5f);
			gl.glVertex2f(0,0.75f);
			gl.glVertex2f(0,0.25f);
			gl.glEnd();
			if(render)
			{
				gl.glLineWidth(1);
				OpenGLUt.glColor(gl,Color.BLACK);
				gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex2f(1,0.5f);
				gl.glVertex2f(0,0.75f);
				gl.glVertex2f(0,0.25f);
				gl.glEnd();
			}
			gl.glLoadIdentity();
		}
		
		private void drawTimeScale(boolean render)
		{
			// TIME SCALE
			glu.gluOrtho2D(minTime, maxTime, 0, 1.5f);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glColor4fv(borderColor, 0);
			gl.glLoadName(RoomIndex.TIMESCALE_BG_IND);
			gl.glRectf(minTime, 0, maxTime, 1.5f);
			int index = RoomIndex.TIMESCALE_BLOC_IND;
			if (index == scaleSelected)
				gl.glColor4fv(timeSelSelColor, 0);
			else gl.glColor4fv(timeSelColor, 0);
			gl.glLoadName(index);
			gl.glRectf(holoEditRef.counterPanel.getDate(1), 0, holoEditRef.counterPanel.getDate(2), 1.5f);
			index = RoomIndex.TIMESCALE_BEG_IND;
			gl.glLoadName(index);
			if (index == scaleSelected)
				gl.glLineWidth(2);
			else gl.glLineWidth(1);
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(holoEditRef.counterPanel.getDate(1), 0);
			gl.glVertex2f(holoEditRef.counterPanel.getDate(1), 1.5f);
			gl.glEnd();
			index = RoomIndex.TIMESCALE_END_IND;
			gl.glLoadName(index);
			if (index == scaleSelected)
				gl.glLineWidth(2);
			else gl.glLineWidth(1);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(holoEditRef.counterPanel.getDate(2), 0);
			gl.glVertex2f(holoEditRef.counterPanel.getDate(2), 1.5f);
			gl.glEnd();
			gl.glLineWidth(1);
			if (render)
			{
				if(holoEditRef.gestionPistes.getActiveTrackNb() != -1)
				{
					activ = holoEditRef.gestionPistes.getActiveTrack();
					float[] cc1 = OpenGLUt.glColor(gl,activ.getColor());
					float[] cc2 = OpenGLUt.glColor(gl,activ.getColor());
					cc1[3] = 0.25f;
					cc2[3] = 0.5f;
					for(HoloTraj ht:activ.trajs)
					{
						OpenGLUt.glColor(gl,cc1);
						gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
						gl.glRectf(ht.getFirstDate(), 0.5f, ht.getLastDate(), 1);
						gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
						OpenGLUt.glColor(gl,cc2);
						gl.glRectf(ht.getFirstDate(), 0.5f, ht.getLastDate(), 1);
					}
				}
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
				OpenGLUt.glColor(gl, Color.BLACK);
				int delta = maxTime - minTime;
				int modulo = 120000;
				if (delta <= 1000)
					modulo = 100;
				else if (delta <= 2000)
					modulo = 200;
				else if (delta <= 6000)
					modulo = 500;
				else if (delta <= 12000)
					modulo = 1000;
				else if (delta <= 24000)
					modulo = 2000;
				else if (delta <= 60000)
					modulo = 3000;
				else if (delta <= 90000)
					modulo = 6000;
				else if (delta <= 180000)
					modulo = 12000;
				else if (delta <= 240000)
					modulo = 15000;
				else if (delta <= 360000)
					modulo = 30000;
				else if (delta <= 720000)
					modulo = 60000;
				else if (delta <= 1440000)
					modulo = 120000;
				else modulo = 180000;
				for (int v = minTime - Ut.mod(minTime, modulo), last = maxTime - Ut.mod(maxTime, modulo); v <= last; v += modulo)
				{
					gl.glRasterPos2f(v + 0.25f, 0.8f);
					int sec = Ut.mod(v / 1000, 60);
					int mn = Ut.mod(v / 60000, 60);
					int h = v / 3600000;
					if (h != 0)
						glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, h + ":" + rF2.format(mn) + ":" + rF2.format(sec) + "\'");
					else glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, mn + ":" + rF2.format(sec) + "\'");
				}
				gl.glBegin(GL.GL_LINES);
				if (delta <= 3000)
					for (int v = minTime - Ut.mod(minTime, 10), last = maxTime - Ut.mod(maxTime, 10); v <= last; v += 10)
					{
						gl.glVertex2f(v, 0);
						gl.glVertex2f(v, 0.1f);
					}
				if (delta <= 36000)
					for (int v = minTime - Ut.mod(minTime, 100), last = maxTime - Ut.mod(maxTime, 100); v <= last; v += 100)
					{
						gl.glVertex2f(v, 0);
						gl.glVertex2f(v, 0.15f);
					}
				if (delta <= 360000)
					for (int v = minTime - Ut.mod(minTime, 1000), last = maxTime - Ut.mod(maxTime, 1000); v <= last; v += 1000)
					{
						gl.glVertex2f(v, 0);
						gl.glVertex2f(v, 0.3f);
					}
				if (delta <= 720000)
					for (int v = minTime - Ut.mod(minTime, 3000), last = maxTime - Ut.mod(maxTime, 3000); v <= last; v += 3000)
					{
						gl.glVertex2f(v, 0);
						gl.glVertex2f(v, 0.45f);
					}
				for (int v = minTime - Ut.mod(minTime, 6000), last = maxTime - Ut.mod(maxTime, 6000); v <= last; v += 6000)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.6f);
				}
				gl.glEnd();
				gl.glLineWidth(1);
				OpenGLUt.glColor(gl, Color.BLACK);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glRectf(minTime, 0, maxTime, 1.5f);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			}
			gl.glLoadIdentity();
		}
		
		private void newStatus()
		{
			status = new Thread()
			{
				public void run()
				{
					try
					{
						while(true)
						{
							if(visible)
								statusBarInfos();
							Thread.sleep(50);
						}
					} catch(Exception e) {
						System.err.println("-");
//						e.printStackTrace();
					}
				}
			};
			status.setName("score-status");
			status.setPriority(Thread.MIN_PRIORITY);
			status.setDaemon(true);
			status.start();
		}

		private void statusBarInfos()
		{
			try
			{
				if(!RoomIndex.isNull(selected))
				{
					RoomIndex.decode(selected);
					if(RoomIndex.isPoint())
					{
						int tkn = RoomIndex.getTrack();
						int tjn = RoomIndex.getSeq();
						int ptn = RoomIndex.getPt();
						HoloPoint pt = holoEditRef.gestionPistes.getTrack(tkn).getHoloTraj(tjn).points.get(ptn);
						toStatus("Track : "+tkn+"   Traj n"+Ut.numCar+":"+tjn+"   Point n"+Ut.numCar+":"+ptn+"   date : "+Ut.msToHMSMS(pt.date)+"   ed : "+pt.isEditable()+"   x : "+rF.format(pt.x)+"   y : "+rF.format(pt.y)+"   z : "+rF.format(pt.z)+"   ray : "+rF.format(pt.getModule())+"   theta : "+rF.format(pt.getTheta()));
					}
				} else if(!RoomIndex.isNull(speakerSelected)) {
					RoomIndex.decode(speakerSelected);
					if(RoomIndex.isSpeaker())
					{
						int spN = RoomIndex.getPt();
						HoloSpeaker sp = holoEditRef.gestionPistes.speakers.get(spN);
						toStatus("Speaker n"+Ut.numCar+":"+(spN+1)+"   x :"+rF.format(sp.X)+"   y :"+rF.format(sp.Y)+"   z :"+rF.format(sp.Z)+"   ray :"+rF.format(sp.dist)+"   theta :"+rF.format(sp.dir));
					}
				} else if(scaleSelected != TimeIndex.getNull()) {
					toStatus("Begin Time : "+Ut.msToHMSMS(holoEditRef.counterPanel.getDate(1))+"   End Time : "+Ut.msToHMSMS(holoEditRef.counterPanel.getDate(2)));
				} else {
					toStatus("x : "+rF.format(convPosW(posW))+"   y : "+rF.format(convPosH(posH)));
				}
			
			} catch (NullPointerException npe) {
			} catch (ArrayIndexOutOfBoundsException aioobe) {
			} catch (NumberFormatException nfe) {}
			
		}

		private void drawXScale(boolean render)
		{
			float TPOS = 0.9f;
			gl.glViewport(W_Y_SCALE, height - H_X_SCALE, width - W_Y_SCALE - W_Y_SCROLL, H_X_SCALE);
			glu.gluOrtho2D(minX, maxX, 0, 1.5f);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glColor4fv(borderColor, 0);
			gl.glRectf(minX, 0, maxX, 1.5f);
			gl.glLineWidth(scaleLineWidth);
			gl.glColor4fv(scaleLineColor, 0);
			if(maxX - minX <= 100)
			{	
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i++)
					{
						gl.glVertex2f(i, 0);
						gl.glVertex2f(i, 0.15f);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=5)
				{
					gl.glRasterPos2f(i, TPOS);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 200)
			{	
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i+=5)
					{
						gl.glVertex2f(i, 0);
						gl.glVertex2f(i, 0.2f);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=10)
				{
					gl.glRasterPos2f(i, TPOS);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 400)
			{
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i+=5)
					{
						gl.glVertex2f(i, 0);
						gl.glVertex2f(i, 0.2f);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=20)
				{
					gl.glRasterPos2f(i, TPOS);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 820)
				for(int i = -800 ; i <= 800 ; i+=50)
				{
					gl.glRasterPos2f(i, TPOS);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			else
				for(int i = -800 ; i <= 800 ; i+=100)
				{
					gl.glRasterPos2f(i, TPOS);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}

			if (scalexListId == 0)
			{
				scalexListId = gl.glGenLists(1);
				gl.glNewList(scalexListId, GL.GL_COMPILE_AND_EXECUTE);
					gl.glBegin(GL.GL_LINES);
						for (int i = -820; i <= 820; i += 10)
						{
							gl.glVertex2f(i, 0);
							gl.glVertex2f(i, 0.3f);
						}
						for (int i = -800; i <= 800; i += 50)
						{
							gl.glVertex2f(i, 0);
							gl.glVertex2f(i, 0.6f);
						}
						for (int i = -800; i <= 800; i += 100)
						{
							gl.glVertex2f(i, 0);
							gl.glVertex2f(i, 0.75f);
						}
					gl.glEnd();
				gl.glEndList();
			}
			else
			{
				gl.glCallList(scalexListId);
			}
			gl.glBegin(GL.GL_LINE_STRIP);
				gl.glVertex2f(minX, 1.5f);
				gl.glVertex2f(minX, 0);
				gl.glVertex2f(maxX, 0);
				gl.glVertex2f(maxX, 1.5f);
			gl.glEnd();
			gl.glLoadIdentity();
		}

		private void drawXScroll(boolean render)
		{
			glu.gluOrtho2D(-820, 820, 0, 1);

			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			// SCROLL BG
			if(scrollHSelected == RoomIndex.SCROLL_HL_IND)
				gl.glColor4fv(scrollSelBgColor, 0);
			else 
				gl.glColor4fv(scrollBgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_HL_IND);
			gl.glRectf(-820, 0, minX, 1);
			if(scrollHSelected == RoomIndex.SCROLL_HR_IND)
				gl.glColor4fv(scrollSelBgColor, 0);
			else 
				gl.glColor4fv(scrollBgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_HR_IND);
			gl.glRectf(maxX, 0, 820, 1);
			if(render)
			{
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glLineWidth(scaleLineWidth);
				gl.glColor4fv(scaleLineColor, 0);
				gl.glRectf(-820, 0, 820, 1);
			}
			// SCROLL FG
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			if(scrollHSelected == RoomIndex.SCROLL_H_IND)
				gl.glColor4fv(scrollSelFgColor, 0);
			else 
				gl.glColor4fv(scrollFgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_H_IND);
			gl.glRectf(minX, 0, maxX, 1);
			if(render)
			{
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glLineWidth(scaleLineWidth);
				gl.glColor4fv(scaleLineColor, 0);
				gl.glRectf(minX, 0, maxX, 1);
			}
			gl.glLoadIdentity();
		}

		private void drawYScale(boolean render)
		{
			float TPOS = 0.15f;
			gl.glViewport(0, H_X_SCROLL + H_BLANK_ZONE + H_TIME_SCALE, W_Y_SCALE, height - (H_X_SCROLL + H_BLANK_ZONE + H_X_SCALE + H_TIME_SCALE));
			glu.gluOrtho2D(0, 1.5f, minY, maxY);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glColor4fv(borderColor, 0);
			gl.glRectf(-0.5f, minY, 1, maxY);
			gl.glLineWidth(scaleLineWidth);
			gl.glColor4fv(scaleLineColor, 0);
			if(maxX - minX <= 100)
			{	
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i++)
					{
						gl.glVertex2f(1.5f, i);
						gl.glVertex2f(1.35f, i);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=5)
				{
					gl.glRasterPos2f(TPOS, i);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 200)
			{	
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i+=5)
					{
						gl.glVertex2f(1.5f, i);
						gl.glVertex2f(1.3f, i);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=10)
				{
					gl.glRasterPos2f(TPOS, i);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 400)
			{
				gl.glBegin(GL.GL_LINES);
					for (int i = -820; i <= 820; i+=5)
					{
						gl.glVertex2f(1.5f, i);
						gl.glVertex2f(1.2f, i);
					}
				gl.glEnd();
				for(int i = -820 ; i <= 820 ; i+=20)
				{
					gl.glRasterPos2f(TPOS, i);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			}
			else if(maxX - minX <= 820)
				for(int i = -800 ; i <= 800 ; i+=50)
				{
					gl.glRasterPos2f(TPOS, i);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			else
				for(int i = -800 ; i <= 800 ; i+=100)
				{
					gl.glRasterPos2f(TPOS, i);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, ""+i);
				}
			
			if (scaleyListId == 0)
			{
				scaleyListId = gl.glGenLists(1);
				gl.glNewList(scaleyListId, GL.GL_COMPILE_AND_EXECUTE);
					gl.glBegin(GL.GL_LINES);
						for (int i = -820; i <= 820; i += 10)
						{
							gl.glVertex2f(1.5f, i);
							gl.glVertex2f(1.2f, i);
						}
						for (int i = -800; i <= 800; i += 50)
						{
							gl.glVertex2f(1.5f, i);
							gl.glVertex2f(0.9f, i);
						}
						for (int i = -800; i <= 800; i += 100)
						{
							gl.glVertex2f(1.5f, i);
							gl.glVertex2f(0.75f, i);
						}
					gl.glEnd();
				gl.glEndList();
			}
			else
			{
				gl.glCallList(scaleyListId);
			}
			gl.glBegin(GL.GL_LINE_STRIP);
				gl.glVertex2f(0, minY);
				gl.glVertex2f(1.5f, minY);
				gl.glVertex2f(1.5f, maxY);
				gl.glVertex2f(0, maxY);
			gl.glEnd();
			gl.glLoadIdentity();
		}

		private void drawYScroll(boolean render)
		{
			glu.gluOrtho2D(0, 1, -820, 820);

			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			// SCROLL BG
			if(scrollVSelected == RoomIndex.SCROLL_VL_IND)
				gl.glColor4fv(scrollSelBgColor, 0);
			else 
				gl.glColor4fv(scrollBgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_VL_IND);
			gl.glRectf(0, -820, 1, minY);
			if(scrollVSelected == RoomIndex.SCROLL_VR_IND)
				gl.glColor4fv(scrollSelBgColor, 0);
			else 
				gl.glColor4fv(scrollBgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_VR_IND);
			gl.glRectf(0, maxY, 1, 820);
			if(render)
			{
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glLineWidth(scaleLineWidth);
				gl.glColor4fv(scaleLineColor, 0);
				gl.glRectf(0, -820, 1, 820);
			}
			// SCROLL FG
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			if(scrollVSelected == RoomIndex.SCROLL_V_IND)
				gl.glColor4fv(scrollSelFgColor, 0);
			else 
				gl.glColor4fv(scrollFgColor, 0);
			gl.glLoadName(RoomIndex.SCROLL_V_IND);
			gl.glRectf(0, minY, 1, maxY);
			if(render)
			{
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				gl.glLineWidth(scaleLineWidth);
				gl.glColor4fv(scaleLineColor, 0);
				gl.glRectf(0, minY, 1, maxY);
			}
			gl.glLoadIdentity();
		}

		private void drawMousePos()
		{
			if(drawMousePos)
			{
				HoloPoint mouseCursor = winToGL((int)mousex,(int)mousey);
				HoloPoint mouseMarginP = winToGL((int)mousex+10,(int)mousey+10);
				HoloPoint mouseMarginM = winToGL((int)mousex-10,(int)mousey-10);
				
				float XP = mouseMarginP.getX() ;
				float YP = mouseMarginP.getY() ;
				float XM = mouseMarginM.getX() ;
				float YM = mouseMarginM.getY() ;
				float X = mouseCursor.getX();
				float Y = mouseCursor.getY();
				
				gl.glLineWidth(mouseFollowLineWidth);
				gl.glColor4fv(mouseFollowColor,0);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex2f(XM,Y);
					gl.glVertex2f(-800,Y);
					gl.glVertex2f(X,YP);
					gl.glVertex2f(X,800);
					gl.glVertex2f(XP,Y);
					gl.glVertex2f(800,Y);
					gl.glVertex2f(X,YM);
					gl.glVertex2f(X,-800);
				gl.glEnd();
			}
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
				gl.glColor4fv(axes1Color,0);
				for (int i = 1; i <= 64; i = i * 2)
				{
					if (i != 3)
					{
						int k = i * 10;
						glu.gluDisk(gluquad, k, k, 50, 0);
						gl.glRectf(-1 * k, -1 * k, k, k);
					}
				}
				gl.glColor4fv(axes2Color,0);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex2f(-1 * 800, -1 * 800);
					gl.glVertex2f(800, 800);
					gl.glVertex2f(-1 * 800, 800);
					gl.glVertex2f(800, -1 * 800);
				gl.glEnd();
				gl.glColor4fv(axes3Color,0);
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
					gl.glColor4fv(speakerColor, 0);
					gl.glRectf(2, 2, 4, -2);
					gl.glColor4fv(speaker2Color, 0);
					gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
					gl.glBegin(GL.GL_POLYGON);
					gl.glVertex2f(0, 5);
					gl.glVertex2f(2, 2);
					gl.glVertex2f(2, -2);
					gl.glVertex2f(0, -5);
					gl.glEnd();
					gl.glEndList();
					selSpeakerListId = gl.glGenLists(1);
					gl.glNewList(selSpeakerListId, GL.GL_COMPILE);
					gl.glColor4fv(speaker2Color, 0);
					gl.glRectf(2, 2, 4, -2);
					gl.glColor4fv(speakerColor, 0);
					gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
					gl.glBegin(GL.GL_POLYGON);
					gl.glVertex2f(0, 5);
					gl.glVertex2f(2, 2);
					gl.glVertex2f(2, -2);
					gl.glVertex2f(0, -5);
					gl.glEnd();
					gl.glEndList();
				}
				gl.glRotatef(-90, 0, 0, 1);
				try
				{
					for (int i = 0, last = holoEditRef.gestionPistes.speakers.size() ; i < last ; i++)
					{
						currentSpeaker2 = holoEditRef.gestionPistes.speakers.get(i);
						gl.glPushMatrix();
						currentSpeaker2.drawProj(gl, glut, i, speakerListId, selSpeakerListId, speakerSelected, render);
						gl.glPopMatrix();
					}
				} catch (NullPointerException e) {}
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
			} else {
				// Si on est en mode "Short View", 
				// on affiche debut +/- Delta si la piste est vide ˆ cet endroit
				// sinon fin du trajet en cours +/- delta
				begTime = holoEditRef.counterPanel.getDate(1);
				endTime = holoEditRef.counterPanel.getDate(2);
		
				HoloTrack at = holoEditRef.gestionPistes.getActiveTrack();
				if(at == null)
				{
					holoEditRef.gestionPistes.setActiveTrack(1);
					at = holoEditRef.gestionPistes.getActiveTrack();
				}
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
			
			
			if (render)
				for (int i = 0; i < holoEditRef.gestionPistes.tracks.size(); i++)
					holoEditRef.gestionPistes.tracks.get(i).drawRoomRenderProj(gl, glut, holoEditRef.viewOnlyEditablePoints, begTime, endTime, i, render, selected, selIndex);
			else if (onlyPoints) {
				if (holoEditRef.allTrackActive)
					for (int i = 0; i < holoEditRef.gestionPistes.tracks.size(); i++)
						holoEditRef.gestionPistes.tracks.get(i).drawRoomMultiSelectProj(gl, holoEditRef.viewOnlyEditablePoints || selMode, begTime, endTime, i);
				else {
					currentTrack2 = holoEditRef.gestionPistes.getActiveTrack();
					if (currentTrack2 != null)
						currentTrack2.drawRoomMultiSelectProj(gl, holoEditRef.viewOnlyEditablePoints || selMode, begTime, endTime, holoEditRef.gestionPistes.getActiveTrackNb());
				}
			} else {
				if (holoEditRef.allTrackActive)
					for (int i = 0; i < holoEditRef.gestionPistes.tracks.size(); i++)
						holoEditRef.gestionPistes.tracks.get(i).drawRoomSelectProj(gl, holoEditRef.viewOnlyEditablePoints, begTime, endTime, i);
				else {
					currentTrack2 = holoEditRef.gestionPistes.getActiveTrack();
					if (currentTrack2 != null)
						currentTrack2.drawRoomSelectProj(gl, holoEditRef.viewOnlyEditablePoints, begTime, endTime, holoEditRef.gestionPistes.getActiveTrackNb());
				}
			}
			gl.glLoadIdentity();
			if(HoloEdit.smooth())
			{
				gl.glDisable(GL.GL_POINT_SMOOTH);
				gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glDisable(GL.GL_POLYGON_SMOOTH);
			}
		}

		private void drawSelZone()
		{
			if (drawSelZone)
			{
				glu.gluOrtho2D(minX, maxX, minY, maxY);
				gl.glColor4fv(selZoneBorderColor,0);
				gl.glLineWidth(selZoneLineWidth);
				gl.glBegin(GL.GL_LINE_LOOP);
					gl.glVertex2f(selZonePt1.getX(), selZonePt1.getY());
					gl.glVertex2f(selZonePt1.getX(), selZonePt2.getY());
					gl.glVertex2f(selZonePt2.getX(), selZonePt2.getY());
					gl.glVertex2f(selZonePt2.getX(), selZonePt1.getY());
				gl.glEnd();
				gl.glColor4fv(selZoneColor,0);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
				gl.glRectf(selZonePt1.getX(), selZonePt1.getY(), selZonePt2.getX(), selZonePt2.getY());
				gl.glLoadIdentity();
			}
		}

		private void getVars()
		{
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
			gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);
		}

		private void treatSelIndex()
		{
			if (selIndex.isEmpty())
				return;
			IntegerVector sortSelVector = new IntegerVector(selIndex);
			sortSelVector.sort();
			selIndex = new Vector<Integer>(5, 1);
			int a = sortSelVector.firstElement(), b = -1, oldTrackNum = -1;
			for (int i : sortSelVector)
			{
				RoomIndex.decode(i);
				currentTrackNum = RoomIndex.getTrack();
				currentPoint = holoEditRef.gestionPistes.tracks.get(currentTrackNum).getHoloTraj(RoomIndex.getSeq()).points.get(RoomIndex.getPt());
				if (currentPoint.isEditable() && currentTrackNum != oldTrackNum)
				{
					if (b != -1)
					{
						selIndex.addAll(holoEditRef.gestionPistes.tracks.get(oldTrackNum).getRoomPointsFromTo(oldTrackNum, a, b));
						a = i;
					}
				}
				b = i;
				oldTrackNum = currentTrackNum;
			}
			selIndex.addAll(holoEditRef.gestionPistes.tracks.get(oldTrackNum).getRoomPointsFromTo(oldTrackNum, a, b));
		}

		private void dragSelIndex(HoloPoint newPoint)
		{
			if (selIndex.isEmpty())
				return;
			IntegerVector sortSelVector = new IntegerVector(selIndex);
			sortSelVector.sort();
			RoomIndex.decode(selected);
			HoloPoint selPoint = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq()).points.get(RoomIndex.getPt()).dupliquer();
			RoomIndex.decode(sortSelVector.firstElement());
			int a = RoomIndex.getPt(), b = -1, oldSeqNum = -1, oldTrackNum = -1;
			HoloTraj oldSeq = null;
			for (int i : sortSelVector)
			{
				RoomIndex.decode(i);
				currentTrackNum = RoomIndex.getTrack();
				currentSeqNum = RoomIndex.getSeq();
				currentSeq = holoEditRef.gestionPistes.tracks.get(currentTrackNum).getHoloTraj(currentSeqNum);
				currentPoint = currentSeq.points.get(RoomIndex.getPt());
				if (currentPoint.isEditable() && (currentSeqNum != oldSeqNum || currentTrackNum != oldTrackNum))
				{
					if (b != -1)
					{
						oldSeq.calcNewPosSelection(a, b, selPoint, newPoint);
						a = RoomIndex.getPt();
					}
				}
				b = RoomIndex.getPt();
				oldSeq = currentSeq;
				oldSeqNum = currentSeqNum;
				oldTrackNum = currentTrackNum;
			}
			oldSeq.calcNewPosSelection(a, b, selPoint, newPoint);
		}

		private void dragSelIndexZ(float dZ)
		{
			if (selIndex.isEmpty())
				return;
			IntegerVector sortSelVector = new IntegerVector(selIndex);
			sortSelVector.sort();
			RoomIndex.decode(sortSelVector.firstElement());
			int a = RoomIndex.getPt(), b = -1, oldSeqNum = -1, oldTrackNum = -1;
			HoloTraj oldSeq = null;
			for (int i : sortSelVector)
			{
				RoomIndex.decode(i);
				currentTrackNum = RoomIndex.getTrack();
				currentSeqNum = RoomIndex.getSeq();
				currentSeq = holoEditRef.gestionPistes.tracks.get(currentTrackNum).getHoloTraj(currentSeqNum);
				currentPoint = currentSeq.points.get(RoomIndex.getPt());
				if (currentPoint.isEditable() && (currentSeqNum != oldSeqNum || currentTrackNum != oldTrackNum))
				{
					if (b != -1)
					{
						oldSeq.calcNewPosSelectionZ(a, b, dZ);
						a = RoomIndex.getPt();
					}
				}
				b = RoomIndex.getPt();
				oldSeq = currentSeq;
				oldSeqNum = currentSeqNum;
				oldTrackNum = currentTrackNum;
			}
			oldSeq.calcNewPosSelectionZ(a, b, dZ);
		}

		private void prepMultiSel(boolean b)
		{
			mousex2 = mousex1 = mousex;
			mousey2 = mousey1 = mousey;
			selIndex = new Vector<Integer>(5, 1);
			selZonePt2 = selZonePt1 = convPosPt(posW, posH);
			draggedSelZone = true;
			drawSelZone = true;
			query_multi_select = true;
			selMode = !b;
		}

		private void insertPointOnLine(HoloPoint newPoint)
		{
			HoloPoint p1, p2;
			p1 = currentPoint;
			p2 = currentSeq.points.get(currentPointNum + 1);
			double DIST = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
			int DD = p2.date - p1.date;
			float dZ = p2.z - p1.z;
			double dist = Math.sqrt(Math.pow(newPoint.x - p1.x, 2) + Math.pow(newPoint.y - p1.y, 2));
			double rapport = dist / DIST;
			newPoint.date = (int) (DD * rapport) + p1.date;
			newPoint.z = (float) (dZ * rapport) + p1.z;
			newPoint.setEditable(false);
			currentSeq.insertElementAtReal(newPoint, currentPointNum + 1);
			currentTrack.update();
		}

		private HoloPoint convPosPt(float xx, float yy)
		{
			return new HoloPoint(convPosW(xx), convPosH(yy), 0, 0, true);
		}

		private float convPosW(float xx)
		{
			return xx / W * (maxX - minX) + minX;
		}

		private float convPosH(float yy)
		{
			return yy / H * (maxY - minY) + minY;
		}

		private float convPosZ(float yy)
		{
			return yy / H * 100;
		}

		private float convPosTime(float xx)
		{
			return Ut.clipL((xx-(W_TIME_SCALE_BF+W_TIME_SCALE_MARGIN)) / (width - 2*(W_TIME_SCALE_BF+W_TIME_SCALE_MARGIN)) * (maxTime - minTime) + minTime, 0);
		}

		private HoloPoint winToGL(int x, int y)
		{
			double[] pos = new double[3];
			glu.gluUnProject(x, y, 0, mvmatrix, 0, projmatrix, 0, viewport, 0, pos, 0);
			return new HoloPoint(pos);
		}
	}

	public RoomGUI(HoloEdit owner)
	{
		super("Room", owner, owner.wsRoomW, owner.wsRoomH, owner.wlRoomX, owner.wlRoomY, owner.wbRoom);
		setTitle("Untitled");
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
		proj_glp = new RoomGLCanvas(this);
		add(proj_glp, BorderLayout.CENTER);
		addKeyListener(proj_glp);
	    proj_glp.requestFocus();
	}

	private void resetView()
	{
		minX = -50;
		maxX = 50;
		minY = -50;
		maxY = 50;
		proj_glp.scalescrollGUIdirty = true;
		display();
	}

	public void setProjView(String view)
	{
		String[] viewArray = view.split(" ");
		if (viewArray.length != 4)
			return;
		minX = Float.parseFloat(viewArray[0]);
		maxX = Float.parseFloat(viewArray[1]);
		minY = Float.parseFloat(viewArray[2]);
		maxY = Float.parseFloat(viewArray[3]);
		proj_glp.scalescrollGUIdirty = true;
	}

	private String getProjView()
	{
		return minX + " " + maxX + " " + minY + " " + maxY;
	}

	public void updateGUI()
	{
		// Constraints for having always a square drawing area
		int w = sizW;
		int h = sizH - (W_TIME_SCALE_MARGIN + H_BLANK_ZONE + H_TIME_SCALE);
		int glpSize = Math.min(Math.max(w, h), 750);
		sizW = glpSize;
		sizH = glpSize + (W_TIME_SCALE_MARGIN + H_BLANK_ZONE + H_TIME_SCALE);
		proj_glp.scalescrollGUIdirty = true;
		setSize(sizW, sizH);
		
	}

	private void disp()
	{
		display();
		holoEditRef.room3d.display();
	}
	
	public void display()
	{
		if(holoEditRef.connection.getAutostop() && holoEditRef.rtDisplay.isAnimating())
		{
			holoEditRef.rtDisplay.stop();
			holoEditRef.connection.setAutostop(false);
			for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
				tk.stop(true);
		}
		if(visible)
			proj_glp.display();
		holoEditRef.room3d.display();
		holoEditRef.score.display();
		holoEditRef.timeEditor.display();
	}
	
	public void open()
	{
		super.open();
		proj_glp.requestFocus();
	}

	public String toString()
	{
		return "\t<room viewProj=\"" + getProjView() + "\"" + super.toString();
	}

	public void initVars(boolean repaint)
	{
		selected = RoomIndex.getNull();
		selIndex = new Vector<Integer>(5,1);
		speakerSelected = RoomIndex.getNull();
		scaleSelected = RoomIndex.getNull();
		scaleBackSelected = RoomIndex.getNull();
		scaleForwSelected = RoomIndex.getNull();
		scrollHSelected = ScoreIndex.getNull();
		scrollVSelected = ScoreIndex.getNull();
		query_one_select = false;
		query_multi_select = false;
		query_speaker_select = false;
		if(repaint)
			display();
	}

	public void removePoints()
	{
		IntegerVector sortSelVector = new IntegerVector(selIndex);
		sortSelVector.sort();
		if (selMode)
		{
			for (int i = sortSelVector.size() - 1; i >= 0; i--)
			{
				int v = sortSelVector.get(i);
				RoomIndex.decode(v);
				currentSeq = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
				currentPointNum = RoomIndex.getPt();
				try
				{
					currentPoint = currentSeq.elementAtReal(currentPointNum);
					if (currentPoint.isEditable())
						currentSeq.removeElementAtReal2(currentPointNum);
				}
				catch (ArrayIndexOutOfBoundsException aioobe) {}
			}
		}
		else
		{
			// REMOVE POINTS 2
			for (int i = sortSelVector.size() - 1; i >= 0; i--)
			{
				RoomIndex.decode(sortSelVector.get(i));
				currentSeq = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq());
				currentPointNum = RoomIndex.getPt();
				currentSeq.points.remove(currentPointNum);
			}
		}
		selIndex = new Vector<Integer>(5, 1);
		selected = RoomIndex.getNull();
	}

	public void setTitle(String title)
	{
		super.setTitle("Room - "+title);
	}
	
	public String getTitle()
	{
		return super.getTitle();
	}
	
	public boolean hasFocus()
	{
		return super.hasFocus() || proj_glp.hasFocus();
	}
	
	public void treatSel()
	{
		proj_glp.treatSelIndex();
	}
	
	public void componentResized(ComponentEvent e)
	{
		sizW = this.getWidth();
		sizH = this.getHeight();
		int w = sizW;
		int h = sizH - (W_TIME_SCALE_MARGIN + H_BLANK_ZONE + H_TIME_SCALE);
		int glpSize = Math.min(Math.max(w, h), 750);
		sizW = glpSize;
		sizH = glpSize + (W_TIME_SCALE_MARGIN + H_BLANK_ZONE + H_TIME_SCALE);
		proj_glp.scalescrollGUIdirty = true;
		setSize(sizW, sizH);
	}
	public void focusGained(FocusEvent e)
	{
		updateMenuBar();
		if (holoEditRef.helpWindowOpened)
			holoEditRef.helpWindow.jumpToIndex("#room", true);
	}
}