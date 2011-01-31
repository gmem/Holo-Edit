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
package holoedit.gui;

import groovy.util.slurpersupport.GPathResult;
import holoedit.HoloEdit;
import holoedit.data.HoloPoint;
import holoedit.data.HoloPointBackRef;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.WaveFormInstance;
import holoedit.data.SDIFdataInstance;
import holoedit.functions.Algors;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.RoomIndex;
import holoedit.opengl.TimeIndex;
import holoedit.util.Formatter;
import holoedit.util.IntegerVector;
import holoedit.util.Ut;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

public class TimeEditorGUI extends FloatingWindow implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	private PopupMenu curvePopup;
	private final int MODE_LIN = 0;
	private final int MODE_ACC = 1;
	private final int MODE_TAN = 2;
	private String timeModes[] = { "Linear", "Curvilinear", "Tangential" };
	private JComboBox timeModeCombo = new JComboBox(timeModes);
	public int timeMode = MODE_LIN; // type de modification ( lineaire,tangentielle,acceleration ) lors du deplacement d'un point dans le temps
	private HoloEdit holoEditRef;
	private JPanel innerPanel;
	private GLCanvas glp;
	private GL gl;
	private GLU glu;
	private GLUT glut;
	private final static int MOUSE_SELECT_SIZE = 5;
	private final int H_LOCAL_TIME_SCALE = 20;
	private final int H_TIME_SCALE = 30;
	private final int H_TIME_SCROLL = 10;
	private final int W_TIME_SCALE_MARGIN = 16;
	private final int W_TIME_SCALE_BF = 10;
	private final int W_CURVE_SCROLL = 10;
	private final int W_CURVE_HEADER = 75;
	private final int CURVE_X = 0;
	private final int CURVE_Y = 1;
	private final int CURVE_Z = 2;
	private final int CURVE_R = 3;
	private final int CURVE_T = 4;
	private final int CURVE_W = 5;
	private final int CURVE_D = 6;
	// private final int CURVE_P1 = 11;
	// private final int CURVE_P2 = 12;
	// private final int CURVE_P3 = 13;
	// private final int CURVE_P4 = 14;
	// private final int CURVE_P5 = 15;
	// private final int CURVE_P6 = 16;
	// private final int CURVE_P7 = 17;
	// private final int CURVE_P8 = 18;
	// private final int CURVE_P9 = 19;
	// private final int CURVE_P10 = 20;
	private final int SCALE_XY = 101;
	private final int SCALE_ZRP = 102;
	private final int SCALE_THETA1 = 103;
	private final int SCALE_THETA2 = 104;
	private final int SCALE_SOUND = 105;
	private final int SCALE_DATA = 106;
	private String[] curveNames = { "X", "Y", "Z", "Ray", "Angle", "Sound", "Data" };
	private int[] curveParams = { CURVE_X, CURVE_Y, CURVE_Z, CURVE_R, CURVE_T, CURVE_W, CURVE_D };
	private int[] curveScales = { SCALE_XY, SCALE_XY, SCALE_ZRP, SCALE_ZRP, SCALE_THETA1, SCALE_SOUND, SCALE_DATA };
	private float[] bgColor = { 1, 1, 1, 1 };
	private float[] borderColor = { 0.95f, 0.95f, 0.95f, 1 };
	private float[] timeSelColor = { 0.8f, 0.8f, 0.8f, 1 };
	private float[] timeSelSelColor = { 0.65f, 0.65f, 0.65f, 1 };
	private float[] scrollBgColor = { 0.5f, 0.5f, 0.5f, 1 };
	private float[] scrollSelBgColor = { 0.4f, 0.4f, 0.4f, 1 };
	private float[] scrollFgColor = { 0.8f, 0.8f, 0.8f, 1 };
	public float[] scrollSelFgColor = { 0.9f, 0.9f, 0.9f, 1 };
	private float[] mouseColor = { 0.5f, 0.5f, 0.5f, 0.7f };
	private float[] selZoneColor = { 0.5f, 0.5f, 0.5f, 0.1f };
	private float[] selZoneBorderColor = { 0, 0, 0.5f, 0.3f };
	private float[] cursorColor = { 0, 0, 0, 0.5f };
	private int width;
	private int height;
	private int curvesGlobalHeight;
	private Vector<Curve> curves;
	public int totalTime = 0;
	public int minTime = 0;
	public int maxTime = 50000;
	public int beg = 0;
	public int end = 50000;
	public float zoom = 1;
	public int zoomTime = 25000;
	public int dT = 0;
	private int W, H;
	private float mousex, mousey, mousex1, mousex2;
	private CurvePoint mouseCursor = new CurvePoint(0, 0);
	private CurvePoint selZonePt1 = new CurvePoint(0, 0);
	private CurvePoint selZonePt2 = new CurvePoint(0, 0);
	private float oldCurrentX;
	private float oldCurrentY;
	private Formatter rf = new Formatter(2, 2, 0, 0);
	private Formatter rf2 = new Formatter(-1, -1, 2, 2);
	private boolean query_one_select = false;
	private boolean query_sel_select = false;
	private int selected = TimeIndex.getNull();
	private int curveSelected = -1;
	public Vector<Integer> selIndex = new Vector<Integer>();
	public Vector<HoloPoint> selPoints = new Vector<HoloPoint>();
	public Vector<HoloPointBackRef> selRefs = new Vector<HoloPointBackRef>();
	private int scaleSelected = TimeIndex.getNull();
	private int scaleBackSelected = TimeIndex.getNull();
	private int scaleForwSelected = TimeIndex.getNull();
	private int scrollHSelected = TimeIndex.getNull();
	private int localScaleSelected = TimeIndex.getNull();
	private int headerSelected = TimeIndex.getNull();
	private HoloTrack activTrack, currentTrack;
	private HoloTraj currentSeq;
	private HoloPoint currentPoint, prevPt, nextPt;
	private int current, current2, prev, next, seqNum, seqPrev, seqNext, nbPtMinus, nbPtPlus;
	private boolean globalDrag = false;
	private boolean drawMousePos = true;
	private boolean draggedSelZone;
	private boolean draggedSelEnd;
	private boolean draggedSelBegin;
	private boolean draggedSel;
	private boolean draggedScroll;
	private boolean draggedScale;
	private boolean draggedLocalScale;
	private boolean draggedMultiPointTimeLin;
	private boolean draggedMultiPointTimeAcc;
	private boolean draggedMultiPointTimeTan;
	private boolean draggedMultiPointTimeBeginLin;
	private boolean draggedMultiPointTimeBeginTan;
	private boolean draggedMultiPointTimeEndLin;
	private boolean draggedMultiPointTimeEndTan;
	private boolean draggedPointTimeLin;
	private boolean draggedPointTimeAcc;
	private boolean draggedPointTimeTan;
	private boolean draggedPointTimeBeginLin;
	private boolean draggedPointTimeBeginTan;
	private boolean draggedPointTimeEndLin;
	private boolean draggedPointTimeEndTan;
	private boolean draggedPointTimeAlone;
	private boolean draggedPointOrd;
	private boolean draggedRealPointsOrd;
	private boolean draggedMultiPointOrd;
	private boolean draggedHeaderScale;
	private boolean selMode;
	private boolean select;
	private int keyDown;
	private static int textPlus, textMinus;
	private Thread status;
	private Curve currentCurve;
	private IntBuffer selectBuf;
	
	public TimeEditorGUI(HoloEdit owner)
	{
		super("Time Editor", owner, owner.wsTimeW, owner.wsTimeH, owner.wlTimeX, owner.wlTimeY, owner.wbTime);
		setTitle("Untitled");
		setResizable(true);
		holoEditRef = owner;
		innerPanel = new JPanel();
		glp = new GLCanvas(holoEditRef.glcap, null, holoEditRef.glpb.getContext(), null);
		curves = new Vector<Curve>(3, 1);
		curves.add(new Curve(CURVE_X));
		curves.add(new Curve(CURVE_Y));
		curves.add(new Curve(CURVE_Z));
		curves.add(new Curve(CURVE_W));
		curves.add(new Curve(CURVE_D));
		initSizes();
		curvePopup = new PopupMenu();
		for (String s : curveNames)
			curvePopup.add(s);
		curvePopup.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String cmd = e.getActionCommand();
				for (int i = 0; i < curveNames.length; i++)
				{
					if (cmd.equalsIgnoreCase(curveNames[i]))
					{
						curves.get(curveSelected).changeParam(curveParams[i]);
						display();
						return;
					}
				}
			}
		});
		newStatus();
		glp.addGLEventListener(this);
		glp.addMouseListener(this);
		glp.addMouseMotionListener(this);
		glp.addMouseWheelListener(this);
		glp.addFocusListener(this);
		glp.addKeyListener(this);
		addKeyListener(this);
		glp.add(curvePopup);
		innerPanel.setLayout(new BorderLayout());
		innerPanel.add(glp, BorderLayout.CENTER);
		initOptions();
		add(innerPanel, BorderLayout.CENTER);
		minTime = holoEditRef.counterPanel.getDate(1);
		maxTime = holoEditRef.counterPanel.getDate(2);
		maxTime = Ut.clipL(maxTime, minTime + 1000);
		zoomTime = (maxTime + minTime) / 2;
	}

	private void initOptions()
	{
		timeModeCombo.setSelectedIndex(0);
		Font f = new Font("courrier", 0, 10);
		timeModeCombo.setFont(f);
		timeModeCombo.setFocusable(false);
		JPanel jp2 = new JPanel();
		jp2.setLayout(new BorderLayout());
		jp2.add(timeModeCombo, BorderLayout.WEST);
		innerPanel.add(jp2, BorderLayout.SOUTH);
		timeModeCombo.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				switch (timeModeCombo.getSelectedIndex())
				{
				case 0:
					timeMode = MODE_LIN;
					break;
				case 1:
					timeMode = MODE_ACC;
					break;
				case 2:
					timeMode = MODE_TAN;
					break;
				default:
					break;
				}
			}
		});
	}

	public void updateGUI()
	{
		setSize(sizW, sizH);
	}

	public void init(GLAutoDrawable drawable)
	{
		gl = drawable.getGL();
		glu = new GLU();
		glut = new GLUT();
		gl.glClearColor(borderColor[0], borderColor[1], borderColor[2], borderColor[3]); // White Background
		gl.glViewport(0, 0, width, height);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glEnable(GL.GL_TEXTURE_2D);
		textPlus = OpenGLUt.textLoad(gl, glu, "bp.png");
		textMinus = OpenGLUt.textLoad(gl, glu, "bm.png");
		curvesGlobalHeight = height - H_LOCAL_TIME_SCALE - H_TIME_SCALE - H_TIME_SCROLL;
		H = curvesGlobalHeight;
		W = width - W_CURVE_HEADER - W_CURVE_SCROLL;
		holoEditRef.rtDisplay.add(drawable);
	}

	@SuppressWarnings("deprecation")
	public void display(GLAutoDrawable drawable)
	{
		if(!visible)return;
		
		gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glLoadIdentity();
		minTime = holoEditRef.counterPanel.getDate(1);
		maxTime = holoEditRef.counterPanel.getDate(2) + 1;
		maxTime = Ut.clipL(maxTime, minTime + 1000);
		float delta = (int) ((maxTime - minTime) * zoom);
		float ratio = (float) (zoomTime - beg) / (end - beg);
		beg = zoomTime - (int) (ratio * delta) + dT;
		end = zoomTime + (int) ((1 - ratio) * delta) + dT;
		dT = 0;
		if (beg < minTime)
		{
			int d = minTime - beg;
			beg += d;
			end += d;
		}
		if (end > maxTime)
		{
			int d = end - maxTime;
			end -= d;
			beg -= d;
		}
		gl.glLineWidth(1);
		int totalHeight = sumSizes();
		int currentHeight = curvesGlobalHeight - totalHeight;
		int rest = currentHeight;
		drawCurveRest(rest);
		activTrack = holoEditRef.gestionPistes.getActiveTrack();
		if (activTrack == null)
			activTrack = holoEditRef.gestionPistes.getTrack(0);
		select = !draggedScroll && !draggedScale && !draggedSel && !draggedSelBegin && !draggedSelEnd && !draggedLocalScale && !draggedSelZone && !draggedHeaderScale;
		if (query_sel_select)
		{
			selIndex = new Vector<Integer>(5, 1);
			selPoints = new Vector<HoloPoint>(5, 1);
		}
		headerSelected = TimeIndex.getNull();
		
		//Ut.print("display with "+selIndex.size()+"-"+selPoints.size());
		
		for (int i = curves.size() - 1; i >= 0; i--)
		{
			currentCurve = curves.get(i);
			int tkH = (int) (currentCurve.hpercent * curvesGlobalHeight);
			currentCurve.combo.setLocation(2, H_LOCAL_TIME_SCALE + (H - currentHeight) - tkH / 2 - currentCurve.combo.getHeight() / 2);
			gl.glViewport(W_CURVE_HEADER, H_TIME_SCALE + H_TIME_SCROLL + currentHeight, W, tkH - 5);
			if (!globalDrag || draggedSelZone)
			{
				if (query_one_select)
					getObjectFromMouse(currentCurve, i, tkH);
				if (query_sel_select)
					getObjectsFromMouseSel(currentCurve, i, currentHeight, tkH, rest);
			}
			try
			{
				if(HoloEdit.smooth())
				{
					gl.glEnable(GL.GL_POINT_SMOOTH);
					gl.glEnable(GL.GL_LINE_SMOOTH);
					gl.glEnable(GL.GL_POLYGON_SMOOTH);
				}
				currentCurve.draw(true, i, tkH);
				if(HoloEdit.smooth())
				{
					gl.glDisable(GL.GL_POINT_SMOOTH);
					gl.glDisable(GL.GL_LINE_SMOOTH);
					gl.glDisable(GL.GL_POLYGON_SMOOTH);
				}
			}
			catch (Exception e)
			{
				System.err.println("TimeEditor Draw Curve Exception");
				e.printStackTrace();
			}
			gl.glViewport(0, H_TIME_SCALE + H_TIME_SCROLL + currentHeight, W_CURVE_HEADER, tkH - 5);
			if (select)
				getHeaderFromMouse(currentCurve, i, tkH - 5);
			currentCurve.drawCurveHeader(i, true, tkH - 5);
			currentHeight += tkH;
		}
		query_one_select = false;
		query_sel_select = false;
		// DRAW UPPER LOCAL TIME SCALE
		gl.glViewport(W_CURVE_HEADER, height - H_LOCAL_TIME_SCALE, width - W_CURVE_HEADER - W_CURVE_SCROLL, H_LOCAL_TIME_SCALE);
		if (select)
			getLocalTimeScale();
		drawLocalTimeScale(true);
		// DRAW CURVE SCROLL (NOT IMPLEMENTED YET)
		gl.glViewport(width - W_CURVE_SCROLL, H_TIME_SCALE + H_TIME_SCROLL, W_CURVE_SCROLL, height - H_TIME_SCALE - H_LOCAL_TIME_SCALE - H_TIME_SCROLL);
		drawCurveScroll(true);
		// DRAW TIME SCROLL
		gl.glViewport(W_CURVE_HEADER, H_TIME_SCALE, width - W_CURVE_HEADER - W_CURVE_SCROLL, H_TIME_SCROLL);
		if (select)
			getScrollHFromMouse();
		drawTimeScroll(true);
		// DRAW TIME SCALE
		gl.glViewport(W_TIME_SCALE_MARGIN / 2, 1, W_TIME_SCALE_BF, H_TIME_SCALE - 7);
		if (select)
			getScaleBackFromMouse();
		drawScaleBack(true);
		gl.glViewport(width - (W_TIME_SCALE_MARGIN + W_TIME_SCALE_BF) + W_TIME_SCALE_MARGIN / 2, 1, W_TIME_SCALE_BF, H_TIME_SCALE - 7);
		if (select)
			getScaleForwFromMouse();
		drawScaleForw(true);
		totalTime = holoEditRef.connection.getTotalTime();
		for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
		{
			totalTime = Ut.max(tk.getLastDate(), totalTime);
			if (!tk.waves.isEmpty())
				for (WaveFormInstance w : tk.waves)
					totalTime = Ut.max(totalTime, w.getLastDate());
			if (!tk.sdifdataInstanceVector.isEmpty())
				for (SDIFdataInstance s : tk.sdifdataInstanceVector)
					totalTime = Ut.max(totalTime, s.getLastDate());
		}
		int fakeEnd = Ut.max(totalTime, 10000);
		holoEditRef.counterPanel.setCompteur(3, totalTime);
		gl.glViewport(W_TIME_SCALE_MARGIN + W_TIME_SCALE_BF, 1, width - (2 * (W_TIME_SCALE_MARGIN + W_TIME_SCALE_BF)), H_TIME_SCALE - 7);
		if (select)
			getScaleFromMouse(fakeEnd);
		drawTimeScale(true, fakeEnd);
		drawDummyCorners(rest);
		if (status != null && status.getState().equals(Thread.State.RUNNABLE))
		{
			status.stop();
			newStatus();
		}
	}
	
	private void newStatus()
	{
		status = new Thread()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						if (visible)
							statusBarInfos();
						Thread.sleep(50);
					}
				}
				catch (Exception e)
				{
					System.err.println("-");
					// e.printStackTrace();
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
			if (selected != TimeIndex.getNull())
			{
				int[] iA = TimeIndex.decode(selected);
				if (TimeIndex.isPoint())
				{
					HoloPoint pt = currentPoint;// mainRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]).points.get(iA[3]);
					if (pt != null)
						toStatus("Track : " + iA[1] + "   Traj n" + Ut.numCar + ":" + iA[2] + "   Point n" + Ut.numCar + ":" + iA[3] + "   date : " + Ut.msToHMSMS(pt.date) + "   x : " + rf2.format(pt.x) + "   y : " + rf2.format(pt.y) + "   z : " + rf2.format(pt.z) + "   ray : "
								+ rf2.format(pt.getModule()) + "   theta : " + rf2.format(pt.getTheta()));
				}
				else
				{
					int p = curves.get(curveSelected).param;
					toStatus("Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate()) + "\t   " + curveNames[p] + " : " + (mouseCursor.getVal() * (p != CURVE_W ? 1 : 2)));
				}
			}
			else if (scaleSelected != TimeIndex.getNull())
			{
				toStatus("Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate())/* +"\t Cursor Time : "+Ut.msToHMSMS(mainRef.score.cursorTime*10) */+ "   Begin Time : " + Ut.msToHMSMS(holoEditRef.counterPanel.getDate(1)) + "   End Time : " + Ut.msToHMSMS(holoEditRef.counterPanel.getDate(2)));
			}
		}
		catch (Exception npe)
		{
		}
	}

	private void drawScaleBack(boolean render)
	{
		glu.gluOrtho2D(0, 1, 0, 1);
		if (scaleBackSelected == TimeIndex.TIMESCALE_BACK_IND)
			OpenGLUt.glColor(gl, Color.GRAY.darker());
		else OpenGLUt.glColor(gl, Color.GRAY);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glLoadName(TimeIndex.TIMESCALE_BACK_IND);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex2f(0, 0.5f);
		gl.glVertex2f(1, 0.75f);
		gl.glVertex2f(1, 0.25f);
		gl.glEnd();
		if (render)
		{
			gl.glLineWidth(1);
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2f(0, 0.5f);
			gl.glVertex2f(1, 0.75f);
			gl.glVertex2f(1, 0.25f);
			gl.glEnd();
		}
		gl.glLoadIdentity();
	}

	private void drawScaleForw(boolean render)
	{
		glu.gluOrtho2D(0, 1, 0, 1);
		if (scaleForwSelected == TimeIndex.TIMESCALE_FORW_IND)
			OpenGLUt.glColor(gl, Color.GRAY.darker());
		else OpenGLUt.glColor(gl, Color.GRAY);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glLoadName(TimeIndex.TIMESCALE_FORW_IND);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex2f(1, 0.5f);
		gl.glVertex2f(0, 0.75f);
		gl.glVertex2f(0, 0.25f);
		gl.glEnd();
		if (render)
		{
			gl.glLineWidth(1);
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2f(1, 0.5f);
			gl.glVertex2f(0, 0.75f);
			gl.glVertex2f(0, 0.25f);
			gl.glEnd();
		}
		gl.glLoadIdentity();
	}

	private void drawTimeScale(boolean render, int totalEndTime)
	{
		// TIME SCALE
		glu.gluOrtho2D(0, totalEndTime, 0, 1.5f);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glColor4fv(borderColor, 0);
		gl.glLoadName(TimeIndex.TIMESCALE_BG_IND);
		gl.glRectf(0, 0, totalEndTime, 1.5f);
		int index = TimeIndex.TIMESCALE_BLOC_IND;
		if (index == scaleSelected)
			gl.glColor4fv(timeSelSelColor, 0);
		else gl.glColor4fv(timeSelColor, 0);
		gl.glLoadName(index);
		gl.glRectf(holoEditRef.counterPanel.getDate(1), 0, holoEditRef.counterPanel.getDate(2), 1.5f);
		index = TimeIndex.TIMESCALE_BEG_IND;
		gl.glLoadName(index);
		if (index == scaleSelected)
			gl.glLineWidth(2);
		else gl.glLineWidth(1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(holoEditRef.counterPanel.getDate(1), 0);
		gl.glVertex2f(holoEditRef.counterPanel.getDate(1), 1.5f);
		gl.glEnd();
		index = TimeIndex.TIMESCALE_END_IND;
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
			if (holoEditRef.gestionPistes.getActiveTrackNb() != -1)
			{
				float[] cc1 = OpenGLUt.glColor(gl, activTrack.getColor());
				float[] cc2 = OpenGLUt.glColor(gl, activTrack.getColor());
				cc1[3] = 0.25f;
				cc2[3] = 0.5f;
				for (HoloTraj ht : activTrack.trajs)
				{
					OpenGLUt.glColor(gl, cc1);
					gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
					gl.glRectf(ht.getFirstDate(), 0.5f, ht.getLastDate(), 1);
					gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
					OpenGLUt.glColor(gl, cc2);
					gl.glRectf(ht.getFirstDate(), 0.5f, ht.getLastDate(), 1);
				}
			}
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			OpenGLUt.glColor(gl, Color.BLACK);
			int delta = totalEndTime;
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
			for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, modulo); v <= last; v += modulo)
			{
				gl.glRasterPos2f(v + 0.25f, 0.8f);
				int sec = Ut.mod(v / 1000, 60);
				int mn = Ut.mod(v / 60000, 60);
				int h = v / 3600000;
				if (h != 0)
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, h + ":" + rf.format(mn) + ":" + rf.format(sec) + "\'");
				else glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, mn + ":" + rf.format(sec) + "\'");
			}
			gl.glBegin(GL.GL_LINES);
			if (delta <= 3000)
				for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, 10); v <= last; v += 10)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.1f);
				}
			if (delta <= 36000)
				for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, 100); v <= last; v += 100)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.15f);
				}
			if (delta <= 360000)
				for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, 1000); v <= last; v += 1000)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.3f);
				}
			if (delta <= 720000)
				for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, 3000); v <= last; v += 3000)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.45f);
				}
			for (int v = 0, last = totalEndTime - Ut.mod(totalEndTime, 6000); v <= last; v += 6000)
			{
				gl.glVertex2f(v, 0);
				gl.glVertex2f(v, 0.6f);
			}
			gl.glEnd();
			gl.glLineWidth(1);
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glRectf(0, 0, totalEndTime, 1.5f);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		}
		gl.glLoadIdentity();
	}

	private void drawLocalTimeScale(boolean render)
	{
		glu.gluOrtho2D(beg, end, 0, 1.5f);
		OpenGLUt.glColor(gl, borderColor);
		gl.glLoadName(TimeIndex.TIMESCALE_LOCAL_IND);
		gl.glRectf(beg, 0, end, 1.5f);
		OpenGLUt.glColor(gl, Color.BLACK);
		if (render)
		{
			gl.glLineWidth(1);
			int delta = end - beg;
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
			for (int v = beg - Ut.mod(beg, modulo), last = end - Ut.mod(end, modulo); v <= last; v += modulo)
			{
				gl.glRasterPos2f(v + 0.25f, 0.8f);
				int sec = Ut.mod(v / 1000, 60);
				int mn = Ut.mod(v / 60000, 60);
				int h = v / 3600000;
				if (h != 0)
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, h + ":" + rf.format(mn) + ":" + rf.format(sec) + "\'");
				else glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, mn + ":" + rf.format(sec) + "\'");
			}
			gl.glBegin(GL.GL_LINES);
			if (delta <= 3000)
				for (int v = beg - Ut.mod(beg, 10), last = end - Ut.mod(end, 10); v <= last; v += 10)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.1f);
				}
			if (delta <= 36000)
				for (int v = beg - Ut.mod(beg, 100), last = end - Ut.mod(end, 100); v <= last; v += 100)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.15f);
				}
			if (delta <= 360000)
				for (int v = beg - Ut.mod(beg, 1000), last = end - Ut.mod(end, 1000); v <= last; v += 1000)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.3f);
				}
			if (delta <= 720000)
				for (int v = beg - Ut.mod(beg, 3000), last = end - Ut.mod(end, 3000); v <= last; v += 3000)
				{
					gl.glVertex2f(v, 0);
					gl.glVertex2f(v, 0.45f);
				}
			for (int v = beg - Ut.mod(beg, 6000), last = end - Ut.mod(end, 6000); v <= last; v += 6000)
			{
				gl.glVertex2f(v, 0);
				gl.glVertex2f(v, 0.6f);
			}
			gl.glEnd();
		}
		gl.glLoadName(TimeIndex.NULL);
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex2f(beg, 1.5f);
		gl.glVertex2f(beg, 0);
		gl.glVertex2f(end, 0);
		gl.glVertex2f(end, 1.5f);
		gl.glEnd();
		gl.glLoadIdentity();
	}

	private void drawTimeScroll(boolean render)
	{
		// TIME SCROLL
		glu.gluOrtho2D(minTime, maxTime, 0, 1);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		int index = TimeIndex.SCROLL_HL_IND;
		if (scrollHSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(minTime, 0, beg, 1);
		index = TimeIndex.SCROLL_HR_IND;
		if (scrollHSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(end, 0, maxTime, 1);
		index = TimeIndex.SCROLL_H_IND;
		if (index == scrollHSelected || draggedScroll)
			OpenGLUt.glColor(gl, scrollSelFgColor);
		else OpenGLUt.glColor(gl, scrollFgColor);
		gl.glLoadName(index);
		gl.glRectf(beg, 0, end, 1);
		if (render)
		{
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glRectf(minTime, 0, maxTime, 1);
			gl.glRectf(beg, 0, end, 1);
		}
		gl.glLoadIdentity();
	}

	private void drawCurveScroll(boolean render)
	{
		// CURVE SCROLL
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glLoadName(TimeIndex.SCROLL_V_IND);
		gl.glRectf(0, 0, 1, 1);
		if (render)
		{
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(0, 0);
			gl.glVertex2f(0, 1);
			gl.glEnd();
		}
		gl.glLoadIdentity();
	}

	private void drawCurveRest(int rest)
	{
		// CURVE DISPLAY_REST
		gl.glViewport(0, H_TIME_SCALE + H_TIME_SCROLL, width - W_CURVE_SCROLL - 1, rest - 1);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		gl.glLoadIdentity();
	}

	private void drawDummyCorners(int rest)
	{
		gl.glLineWidth(1);
		// DUMMY CORNERS
		// BOTTOM / LEFT
		gl.glViewport(0, H_TIME_SCALE, W_CURVE_HEADER, H_TIME_SCROLL);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, 1);
		gl.glVertex2f(1, 1);
		gl.glVertex2f(1, 0);
		gl.glVertex2f(1, 1);
		gl.glEnd();
		gl.glLoadIdentity();
		// BOTTOM / RIGHT
		gl.glViewport(width - W_CURVE_SCROLL, H_TIME_SCALE, W_CURVE_SCROLL, H_TIME_SCROLL);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, 1);
		gl.glVertex2f(1, 1);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(0, 1);
		gl.glEnd();
		gl.glLoadIdentity();
		// TOP / LEFT
		gl.glViewport(0, height - H_LOCAL_TIME_SCALE, W_CURVE_HEADER, H_LOCAL_TIME_SCALE);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(1, 0);
		gl.glVertex2f(1, 0);
		gl.glVertex2f(1, 1);
		gl.glEnd();
		if (activTrack != null)
		{
			gl.glRasterPos2f(0.05f, 0.25f);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, activTrack.getName());
		}
		gl.glLoadIdentity();
		// TOP / RIGHT
		gl.glViewport(width - W_CURVE_SCROLL, height - H_LOCAL_TIME_SCALE, W_CURVE_SCROLL, H_LOCAL_TIME_SCALE);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(0, 1);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(1, 0);
		gl.glEnd();
		gl.glLoadIdentity();
	}

	private void getObjectFromMouse(Curve c, int curveNum, int tkH)
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
		gl.glRenderMode(GL.GL_SELECT);
		gl.glInitNames();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushName(-1);
		gl.glPushMatrix();
		gl.glLoadIdentity(); // reset the proj. matrix
		// !!! leave gluPickMatrix after glloadidentity
		/* create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location */
		glu.gluPickMatrix(mousex, mousey, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		c.draw(false, curveNum, tkH);
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		int offset = 0;
		int names = -1;
		int tmp = -1;
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
					tmp = selectBuf.get(offset);
				}
				else
				{
				}
				offset++;
			}
			TimeIndex.decode(tmp);
			if (TimeIndex.isCurve())
				curveSelected = TimeIndex.getCurve();
		}
		if (tmp != -1)
			selected = tmp;
	}

	private void getObjectsFromMouseSel(Curve c, int curveNum, int currentHeight, int tkH, int rest)
	{
		int hits = 0;
		int[] vPort = new int[4];
		selectBuf = BufferUtil.newIntBuffer(4096);// ByteBuffer.allocateDirect(10000).asIntBuffer();
		double midPtX = (mousex1 + mousex2) / 2;
		double midPtY = rest + currentHeight + tkH / 2 + H_TIME_SCALE + H_TIME_SCROLL;
		double w = Math.abs(mousex2 - mousex1);
		double h = tkH;
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
			c.draw(false, curveNum, tkH);
			gl.glPopMatrix();
			hits = gl.glRenderMode(GL.GL_RENDER);
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

	private void getHeaderFromMouse(Curve c, int curveNum, int h)
	{
		int hits = 0;
		int[] vPort = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		c.drawCurveHeader(curveNum, false, h);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
			return;
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
					headerSelected = selectBuf.get(offset);
				offset++;
			}
		}
		TimeIndex.decode(headerSelected);
		if (TimeIndex.isHeaderPoly() || TimeIndex.isHeaderScale() || TimeIndex.isPlus() || TimeIndex.isMinus())
			curveSelected = TimeIndex.getCurve();
	}

	private void getLocalTimeScale()
	{
		int hits = 0;
		int[] vPort = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		drawLocalTimeScale(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			localScaleSelected = TimeIndex.getNull();
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
					localScaleSelected = selectBuf.get(offset);
				offset++;
			}
		}
	}

	private void getScrollHFromMouse()
	{
		int hits = 0;
		int[] vPort = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		drawTimeScroll(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			scrollHSelected = TimeIndex.getNull();
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

	private void getScaleFromMouse(int totalEndTime)
	{
		int hits = 0;
		int[] vPort = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		drawTimeScale(false, totalEndTime);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			scaleSelected = TimeIndex.getNull();
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
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
			scaleBackSelected = TimeIndex.getNull();
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
		selectBuf = BufferUtil.newIntBuffer(512);// ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		if (hits == 0)
		{
			scaleForwSelected = TimeIndex.getNull();
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

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		width = w;
		height = h;
		curvesGlobalHeight = height - H_LOCAL_TIME_SCALE - H_TIME_SCALE - H_TIME_SCROLL;
		H = curvesGlobalHeight;
		W = width - W_CURVE_HEADER - W_CURVE_SCROLL;
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	public void initSizes()
	{
		float dh = (float) 1 / curves.size();
		for (Curve c : curves)
			c.hpercent = dh;
	}

	public int sumSizes()
	{
		int tmp = 0;
		for (Curve c : curves)
			tmp += (int) (c.hpercent * curvesGlobalHeight);
		return tmp;
	}

	public void addCurve()
	{
		String result = (String) JOptionPane.showInputDialog(this, "Add a new curve", "New Curve", JOptionPane.QUESTION_MESSAGE, null, curveNames, curveNames[0]);
		if (result != null)
		{
			int sel = 0;
			for (int k = 0; k < curveNames.length; k++)
				if (curveNames[k].equalsIgnoreCase(result))
					sel = k;
			Curve c = new Curve(curveParams[sel]);
			if (curveSelected == -1)
				curves.add(c);
			else curves.insertElementAt(c, curveSelected + 1);
			initSizes();
		}
	}

	public void removeCurve(int c)
	{
		if (c >= 0 && c < curves.size() && curves.size() >= 2)
		{
			// gljp.remove(curves.get(c).combo);
			curves.remove(c);
			initSizes();
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		TimeIndex.decode(selected);
		if (TimeIndex.isCurve())
		{
			if (e.getClickCount() >= 2)
			{
			}
			else
			{
				//selIndex = new Vector<Integer>();
				//selPoints = new Vector<HoloPoint>();
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		
		if (!TimeIndex.isNull(selected))
		{
			int[] iA = TimeIndex.decode(selected);
			if (TimeIndex.isCurve())
			{
				if (e.getClickCount() >= 2 && curves.get(iA[1]).param != CURVE_W)
				{
					selMode = !e.isShiftDown();
					selIndex = new Vector<Integer>(5, 1);
					selPoints = new Vector<HoloPoint>(5, 1);
					selIndex.addAll(activTrack.getAllTimePoints(0, beg, end, selMode || holoEditRef.viewOnlyEditablePoints));
					treatSelIndex();
				}
				else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && !e.isShiftDown() && curves.get(iA[1]).param != CURVE_W && e.getButton() != MouseEvent.BUTTON3)
				{
					holoEditRef.gestionPistes.StoreToUndo();
					curves.get(iA[1]).addPoint(mouseCursor);
				}
				else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.isShiftDown() && curves.get(iA[1]).param != CURVE_W && e.getButton() != MouseEvent.BUTTON3)
				{
					holoEditRef.gestionPistes.StoreToUndo();
					curves.get(iA[1]).addPointSolo(mouseCursor);
				}
				else if (e.isAltDown() && e.getButton() != MouseEvent.BUTTON2)
				{
					holoEditRef.counterPanel.setCompteur(5, (int) mouseCursor.getDate());
					holoEditRef.connection.setCurrentTime(holoEditRef.score.cursorTime);
					if (holoEditRef.connection.isPlaying())
						holoEditRef.connection.stopAndPlay();
				}
				else
				{
					prepareDragSelZone(e, posW, posH);
				}
			}
			else if (TimeIndex.isPoint())
			{
				currentSeq = activTrack.getHoloTraj(iA[2]);
				currentPoint = currentSeq.points.get(iA[3]);
				holoEditRef.gestionPistes.StoreToUndo();
				if (!selPoints.isEmpty() && selPoints.contains(currentPoint))
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
					{
						removePoints();
					}
					else if((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))
					{
						changeEditable(!currentPoint.isEditable());
					}
					else
					{
						if (e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							prepareDraggMultiPointsOrd();
						}
						else
						{
							prepareDraggMultiPointsTime();
						}
					}
				}
				else
				{
					if (TimeIndex.isPoint())
					{
						selIndex = new Vector<Integer>();
						selPoints = new Vector<HoloPoint>();
						
						HoloPoint p = activTrack.getHoloTraj(iA[2]).points.get(iA[3]);
						
						if (!selPoints.contains(p))
						{
							selPoints.add(p);
							selIndex.add(selected);
						}
						
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							currentSeq.removeElementAtReal(iA[3]);
							activTrack.update();
							selected = TimeIndex.getNull();
						}
						else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.getButton() != MouseEvent.BUTTON3)
						{
							if (currentPoint != currentSeq.firstElement() && currentPoint != currentSeq.lastElement())
								currentPoint.setEditable(!currentPoint.isEditable());
						}
						else if ((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3)
						{
							int room = RoomIndex.encode(RoomIndex.TYPE_PT, holoEditRef.gestionPistes.getActiveTrackNb(), iA[2], iA[3]);
							new HoloPointEditor(room, new Point(e.getX(), e.getY()), this, holoEditRef);
						}
						else
						{
							if (e.isShiftDown() && e.getButton() != MouseEvent.BUTTON3)
							{
								prepareDraggPointOrd();
							}
							else
							{
								prepareDraggPointTime();
							}
						}
						
						updateSelRefs();
					}
				}
			}
			else if (TimeIndex.isLine())
			{				
				if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC)) && e.getButton() != MouseEvent.BUTTON3)
				{
					holoEditRef.gestionPistes.StoreToUndo();
					if( holoEditRef.viewOnlyEditablePoints )
						holoEditRef.gestionPistes.getActiveTrack().getHoloTraj(iA[2]).insertEditablePointAfter((int) mouseCursor.getDate(), iA[3]);
					else
						holoEditRef.gestionPistes.getActiveTrack().getHoloTraj(iA[2]).insertPointAfter((int) mouseCursor.getDate(), iA[3]);
				}
				else prepareDragSelZone(e, posW, posH);
			}
		}
		else if (!TimeIndex.isNull(scrollHSelected))
		{
			float pad = (end - beg) / 2;
			TimeIndex.decode(scrollHSelected);
			if (TimeIndex.isScrollHLeft())
			{
				dT = -1 * (int) pad;
				mouseCursor.shift(dT);
			}
			else if (TimeIndex.isScrollHRight())
			{
				dT = (int) pad;
				mouseCursor.shift(dT);
			}
			else if (TimeIndex.isScrollH())
			{
				draggedScroll = true;
				drawMousePos = false;
				oldCurrentX = convPosTime2(posW);
			}
		}
		else if (!TimeIndex.isNull(scaleBackSelected))
		{
			TimeIndex.decode(scaleBackSelected);
			if (TimeIndex.isTimeScaleBack())
				holoEditRef.counterPanel.setBegAndEnd(Ut.clipL(holoEditRef.counterPanel.getDate(1) - holoEditRef.gestionPistes.delta, 0), Ut.clipL(holoEditRef.counterPanel.getDate(2) - holoEditRef.gestionPistes.delta, holoEditRef.counterPanel.getDate(2) - holoEditRef.counterPanel.getDate(1)));
		}
		else if (!TimeIndex.isNull(scaleForwSelected))
		{
			TimeIndex.decode(scaleForwSelected);
			if (TimeIndex.isTimeScaleForw())
				holoEditRef.counterPanel.setBegAndEnd(holoEditRef.counterPanel.getDate(1) + holoEditRef.gestionPistes.delta, holoEditRef.counterPanel.getDate(2) + holoEditRef.gestionPistes.delta);
		}
		else if (!TimeIndex.isNull(scaleSelected))
		{
			TimeIndex.decode(scaleSelected);
			if (e.getClickCount() >= 2)
				holoEditRef.counterPanel.setBegAndEnd(0, totalTime);
			else if (TimeIndex.isTimeScaleBg())
			{
				oldCurrentX = convPosTime(e.getX());
				draggedScale = true;
				drawMousePos = false;
				int init = Ut.clipL((int) oldCurrentX, 0);
				holoEditRef.counterPanel.setBegAndEnd(init, init);
			}
			else if (TimeIndex.isTimeScaleBeg())
			{
				draggedSelBegin = true;
				drawMousePos = false;
				oldCurrentX = convPosTime(e.getX());
			}
			else if (TimeIndex.isTimeScaleEnd())
			{
				draggedSelEnd = true;
				drawMousePos = false;
				oldCurrentX = convPosTime(e.getX());
			}
			else if (TimeIndex.isTimeScaleBloc())
			{
				if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				{
					draggedScale = true;
					drawMousePos = false;
					oldCurrentX = convPosTime(e.getX());
					int init = Ut.clipL((int) oldCurrentX, 0);
					holoEditRef.counterPanel.setBegAndEnd(init, init);
				}
				else
				{
					draggedSel = true;
					drawMousePos = false;
					oldCurrentX = convPosTime(e.getX());
				}
			}
		}
		else if (!TimeIndex.isNull(localScaleSelected))
		{
			TimeIndex.decode(localScaleSelected);
			if (TimeIndex.isTimeScaleLocal())
			{
				if (e.getClickCount() >= 2)
				{
					zoom = 1;
					zoomTime = (maxTime + minTime) / 2;
				}
				else
				{
					draggedLocalScale = true;
					drawMousePos = false;
					oldCurrentY = e.getY();
					zoomTime = (int) convPosLocalTime(posW);
				}
			}
		}
		else if (!TimeIndex.isNull(headerSelected))
		{
			TimeIndex.decode(headerSelected);
			if (TimeIndex.isHeaderPoly())
			{
				curvePopup.show(glp, e.getX(), e.getY());
			}
			else if (TimeIndex.isPlus())
				addCurve();
			else if (TimeIndex.isMinus())
				removeCurve(curveSelected);
			else if (TimeIndex.isHeaderScale())
			{
				if (e.getClickCount() >= 2)
					curves.get(curveSelected).initScale();
				else
				{
					oldCurrentY = e.getY();
					draggedHeaderScale = true;
				}
			}
		}
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	public void mouseReleased(MouseEvent e)
	{
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		if (draggedSelZone)
			treatSelIndex();
		draggedHeaderScale = false;
		draggedSelZone = false;
		draggedLocalScale = false;
		draggedScale = false;
		draggedScroll = false;
		draggedSel = false;
		draggedSelBegin = false;
		draggedSelEnd = false;
		drawMousePos = true;
		draggedMultiPointTimeLin = false;
		draggedMultiPointTimeAcc = false;
		draggedMultiPointTimeTan = false;
		draggedMultiPointTimeBeginLin = false;
		draggedMultiPointTimeBeginTan = false;
		draggedMultiPointTimeEndLin = false;
		draggedMultiPointTimeEndTan = false;
		draggedPointTimeLin = false;
		draggedPointTimeAcc = false;
		draggedPointTimeTan = false;
		draggedPointTimeBeginLin = false;
		draggedPointTimeBeginTan = false;
		draggedPointTimeEndLin = false;
		draggedPointTimeEndTan = false;
		draggedPointTimeAlone = false;
		draggedPointOrd = false;
		draggedRealPointsOrd = false;
		draggedMultiPointOrd = false;
		globalDrag = false;
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
		globalDrag = true;
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		if (draggedSelZone)
		{
			mousex2 = mousex;
			selIndex = new Vector<Integer>(5, 1);
			selPoints = new Vector<HoloPoint>(5, 1);
			selZonePt2 = convPosPt(posW, posH);
			query_sel_select = true;
			selMode = !e.isShiftDown();
		}
		else if (draggedScale)
		{
			float X = convPosTime(e.getX());
			float dX = X - oldCurrentX;
			if (dX > 0)
				holoEditRef.counterPanel.setCompteur(2, Ut.clipL((int) X, 0));
			else holoEditRef.counterPanel.setBegAndEnd(Ut.clipL((int) X, 0), Ut.clipL((int) oldCurrentX, 0));
		}
		else if (draggedSel)
		{
			float X = convPosTime(e.getX());
			float dX = X - oldCurrentX;
			if (e.isShiftDown())
				dX *= 0.1;
			holoEditRef.counterPanel.setBegAndEnd(Ut.clipL(holoEditRef.counterPanel.getDate(1) + (int) dX, 0), Ut.clipL(holoEditRef.counterPanel.getDate(2) + (int) dX, 0));
			oldCurrentX = X;
		}
		else if (draggedSelBegin)
		{
			float X = convPosTime(e.getX());
			float dX = X - oldCurrentX;
			if (e.isShiftDown())
				dX *= 0.1;
			int n = holoEditRef.counterPanel.getDate(1) + (int) dX;
			if (n <= holoEditRef.counterPanel.getDate(2))
				holoEditRef.counterPanel.setCompteur(1, n);
			else
			{
				holoEditRef.counterPanel.setCompteur(1, holoEditRef.counterPanel.getDate(2));
				holoEditRef.counterPanel.setCompteur(2, Ut.clipL(n, 0));
				draggedSelBegin = false;
				draggedSelEnd = true;
				scaleSelected = TimeIndex.TIMESCALE_END_IND;
			}
			oldCurrentX = X;
		}
		else if (draggedSelEnd)
		{
			float X = convPosTime(e.getX());
			float dX = X - oldCurrentX;
			if (e.isShiftDown())
				dX *= 0.1;
			int n = holoEditRef.counterPanel.getDate(2) + (int) dX;
			if (n >= holoEditRef.counterPanel.getDate(1))
				holoEditRef.counterPanel.setCompteur(2, n);
			else
			{
				holoEditRef.counterPanel.setCompteur(2, holoEditRef.counterPanel.getDate(1));
				holoEditRef.counterPanel.setCompteur(1, Ut.clipL(n, 0));
				draggedSelBegin = true;
				draggedSelEnd = false;
				scaleSelected = TimeIndex.TIMESCALE_BEG_IND;
			}
			oldCurrentX = X;
		}
		else if (draggedScroll)
		{
			float nx = convPosTime2(posW);
			dT = (int) (nx - oldCurrentX);
			oldCurrentX = nx;
		}
		else if (draggedLocalScale)
		{
			zoom = Ut.clip(zoom - (e.getY() - oldCurrentY) / 200, 0.025f, 1);
			oldCurrentY = e.getY();
		}
		else if (draggedHeaderScale)
		{
			Curve c = curves.get(curveSelected);
			if (c.param != CURVE_W)
			{
				c.scaleID = -1;
				if (e.isShiftDown())
				{
					c.minY += e.getY() - oldCurrentY;
					c.maxY -= e.getY() - oldCurrentY;
				}
				else
				{
					c.minY += e.getY() - oldCurrentY;
					c.maxY += e.getY() - oldCurrentY;
				}
				if (c.maxY < c.minY + 10)
				{
					c.minY = c.maxY - 10;
					draggedHeaderScale = false;
				}
				c.limitScale();
				if (c.maxY < c.minY + 10)
				{
					c.maxY = c.minY + 10;
					draggedHeaderScale = false;
				}
			}
			oldCurrentY = e.getY();
		}
		else if (draggedPointTimeLin)
		{
			draggedPointTimeLin();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeAcc)
		{
			draggedPointTimeAcc();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeTan)
		{
			draggedPointTimeTan();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeBeginLin)
		{
			draggedPointTimeBeginLin();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeBeginTan)
		{
			draggedPointTimeBeginTan();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeEndLin)
		{
			draggedPointTimeEndLin();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeEndTan)
		{
			draggedPointTimeEndTan();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointTimeAlone)
		{
			draggedTimeAlone();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeLin)
		{
			draggedMultiPointTimeLin();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeBeginLin)
		{
			draggedMultiPointTimeBeginLin();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeEndLin)
		{
			draggedMultiPointTimeEndLin();
			mouseCursor.x = currentPoint.date;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeAcc)
		{
			draggedMultiPointTimeAcc();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeTan)
		{
			draggedMultiPointTimeTan();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeBeginTan)
		{
			draggedMultiPointTimeBeginTan();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedMultiPointTimeEndTan)
		{
			draggedMultiPointTimeEndTan();
			mouseCursor.x = currentPoint.date;
			mouseCursor.y = oldCurrentY;
			oldCurrentX = mouseCursor.getDate();
		}
		else if (draggedPointOrd)
		{
			draggedPointOrd();
			mouseCursor.x = oldCurrentX;
			oldCurrentY = mouseCursor.getVal();
		}
		else if (draggedRealPointsOrd)
		{
			draggedRealPointsOrd();
			mouseCursor.x = oldCurrentX;
			oldCurrentY = mouseCursor.getVal();
		}
		else if (draggedMultiPointOrd)
		{
			draggedMultiPointOrd();
			mouseCursor.x = oldCurrentX;
			oldCurrentY = mouseCursor.getVal();
		}
		if (draggedPointTimeLin || draggedPointTimeAcc || draggedPointTimeTan || draggedPointTimeBeginLin || draggedPointTimeEndLin || draggedPointTimeBeginTan || draggedPointTimeEndTan || draggedPointTimeAlone)
		{
			if (e.isShiftDown() && currentPoint.isEditable())
			{
				draggedPointOrd();
				oldCurrentY = mouseCursor.getVal();
			}
			else mouseCursor.y = oldCurrentY;
			
			currentSeq.setDirty(true);
		}
		holoEditRef.gestionPistes.setDirty(true);
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	private void prepareDragSelZone(MouseEvent e, float posW, float posH)
	{
		mousex2 = mousex1 = mousex;
		selIndex = new Vector<Integer>(5, 1);
		selPoints = new Vector<HoloPoint>(5, 1);
		selZonePt2 = selZonePt1 = convPosPt(posW, posH);
		query_sel_select = true;
		draggedSelZone = true;
		drawMousePos = false;
		selMode = !e.isShiftDown();
	}

	private void prepareDraggPointTime()
	{
		int[] iA = TimeIndex.decode(selected);
		HoloPoint p;
		current = iA[3];
		seqNum = iA[2];
		prev = currentSeq.prevEditPoint(current);
		next = currentSeq.nextEditPoint(current);
		nbPtMinus = current - prev;
		nbPtPlus = next - current;
		oldCurrentX = mouseCursor.getDate();
		oldCurrentY = mouseCursor.getVal();
		prevPt = null;
		nextPt = null;
		if (prev != -1 && next != -1)
		{
			prevPt = currentSeq.points.get(prev);
			nextPt = currentSeq.points.get(next);
			
			for (int i = prev; i <= next; i++)
			{
				p = currentSeq.points.get(i);
				p.updateDDate();
			}
			
			switch (timeMode)
			{
			case MODE_LIN:
				draggedPointTimeLin = true;
				break;
			case MODE_ACC:
				draggedPointTimeAcc = true;
				break;
			case MODE_TAN:
				draggedPointTimeTan = true;
				break;
			default:
				break;
			}
		}
		else if (prev != -1)
		{
			// PAS DE SUIVANT
			prevPt = currentSeq.points.get(prev);
			
			for (int i = prev; i <= current; i++)
			{
				p = currentSeq.points.get(i);
				p.updateDDate();
			}
			
			switch (timeMode)
			{
			case MODE_LIN:
			case MODE_ACC:
				draggedPointTimeEndLin = true;
				break;
			case MODE_TAN:
				draggedPointTimeEndTan = true;
				break;
			default:
				break;
			}
		}
		else if (next != -1)
		{
			// PAS DE PRECEDENT
			nextPt = currentSeq.points.get(next);
			
			for (int i = current; i <= next; i++)
			{
				p = currentSeq.points.get(i);
				p.updateDDate();
			}
			
			switch (timeMode)
			{
			case MODE_LIN:
			case MODE_ACC:
				draggedPointTimeBeginLin = true;
				break;
			case MODE_TAN:
				draggedPointTimeBeginTan = true;
				break;
			default:
				break;
			}
		}
		else
		{
			
			p = currentSeq.points.get(current);
			p.updateDDate();
			draggedPointTimeAlone = true;
			
		}
	}

	private void prepareDraggPointOrd()
	{
		int[] iA = TimeIndex.decode(selected);
		current = iA[3];
		seqNum = iA[2];
		prev = currentSeq.prevEditPoint(current);
		next = currentSeq.nextEditPoint(current);
		oldCurrentX = mouseCursor.getDate();
		oldCurrentY = mouseCursor.getVal();
		draggedPointOrd = true;
		// currentXpositif = currentPoint.x >= 0;
		// currentYpositif = currentPoint.y >= 0;
	}

	private void prepareDraggMultiPointsTime()
	{
		int[] iA = TimeIndex.decode(selected);
		current = iA[3];
		seqNum = iA[2];
		int[] iF = TimeIndex.decode(selIndex.firstElement());
		seqPrev = iF[2];
		int[] iL = TimeIndex.decode(selIndex.lastElement());
		seqNext = iL[2];
		int f = iF[3];
		int l = iL[3];
		prevPt = null;
		nextPt = null;
		if (seqPrev == seqNext)
		{
			prev = currentSeq.prevEditPoint(f);
			next = currentSeq.nextEditPoint(l);
			if (prev == -1)
				prev = f;
			if (next == -1)
				next = l;
			nbPtMinus = current - prev;
			nbPtPlus = next - current;
			oldCurrentX = mouseCursor.getDate();
			oldCurrentY = mouseCursor.getVal();
			if (prev != current && next != current)
			{
				prevPt = currentSeq.points.get(prev);
				nextPt = currentSeq.points.get(next);
				switch (timeMode)
				{
				case MODE_LIN:
					draggedPointTimeLin = true;
					break;
				case MODE_ACC:
					draggedPointTimeAcc = true;
					break;
				case MODE_TAN:
					draggedPointTimeTan = true;
					break;
				default:
					break;
				}
			}
			else if (next == current)
			{
				// PAS DE SUIVANT
				prevPt = currentSeq.points.get(prev);
				switch (timeMode)
				{
				case MODE_LIN:
				case MODE_ACC:
					draggedPointTimeEndLin = true;
					break;
				case MODE_TAN:
					draggedPointTimeEndTan = true;
					break;
				default:
					break;
				}
			}
			else if (prev == current)
			{
				// PAS DE PRECEDENT
				nextPt = currentSeq.points.get(next);
				switch (timeMode)
				{
				case MODE_LIN:
				case MODE_ACC:
					draggedPointTimeBeginLin = true;
					break;
				case MODE_TAN:
					draggedPointTimeBeginTan = true;
					break;
				default:
					break;
				}
			}
		}
		else
		{
			prev = activTrack.getHoloTraj(seqPrev).prevEditPoint(f);
			next = activTrack.getHoloTraj(seqNext).nextEditPoint(l);
			if (prev == -1)
				prev = f;
			if (next == -1)
				next = l;
			if (seqNum != seqPrev)
			{
				nbPtMinus = activTrack.getHoloTraj(seqPrev).size() - prev;
				for (int i = seqPrev + 1; i < seqNum; i++)
					nbPtMinus += activTrack.getHoloTraj(i).size();
				nbPtMinus += current;
			}
			else
			{
				nbPtMinus = current - prev;
			}
			if (seqNext != seqNum)
			{
				nbPtPlus = activTrack.getHoloTraj(seqNum).size() - current;
				for (int i = seqNum + 1; i < seqNext; i++)
					nbPtPlus += activTrack.getHoloTraj(i).size();
				nbPtPlus += next;
			}
			else
			{
				nbPtPlus = next - current;
			}
			oldCurrentX = mouseCursor.getDate();
			oldCurrentY = mouseCursor.getVal();
			if ((seqPrev != seqNum || current != prev) && (seqNext != seqNum || current != next))
			{
				prevPt = activTrack.getHoloTraj(seqPrev).points.get(prev);
				nextPt = activTrack.getHoloTraj(seqNext).points.get(next);
				switch (timeMode)
				{
				case MODE_LIN:
					draggedMultiPointTimeLin = true;
					break;
				case MODE_ACC:
					draggedMultiPointTimeAcc = true;
					break;
				case MODE_TAN:
					draggedMultiPointTimeTan = true;
					break;
				default:
					break;
				}
			}
			else if (next == current && seqNum == seqNext)
			{
				// PAS DE SUIVANT
				prevPt = activTrack.getHoloTraj(seqPrev).points.get(prev);
				switch (timeMode)
				{
				case MODE_LIN:
				case MODE_ACC:
					draggedMultiPointTimeEndLin = true;
					break;
				case MODE_TAN:
					draggedMultiPointTimeEndTan = true;
					break;
				default:
					break;
				}
			}
			else if (prev == current && seqNum == seqPrev)
			{
				// PAS DE PRECEDENT
				nextPt = activTrack.getHoloTraj(seqNext).points.get(next);
				switch (timeMode)
				{
				case MODE_LIN:
				case MODE_ACC:
					draggedMultiPointTimeBeginLin = true;
					break;
				case MODE_TAN:
					draggedMultiPointTimeBeginTan = true;
					break;
				default:
					break;
				}
			}
		}
	}

	private void prepareDraggMultiPointsOrd()
	{
		int[] iA = TimeIndex.decode(selected);
		current = iA[3];
		seqNum = iA[2];
		int[] iF = TimeIndex.decode(selIndex.firstElement());
		seqPrev = iF[2];
		int[] iL = TimeIndex.decode(selIndex.lastElement());
		seqNext = iL[2];
		int f = iF[3];
		int l = iL[3];
		prev = -1;
		next = -1;
		oldCurrentX = mouseCursor.getDate();
		oldCurrentY = mouseCursor.getVal();
		if (seqPrev == seqNext)
		{
			prev = currentSeq.prevEditPoint(f);
			next = currentSeq.nextEditPoint(l);
			current = f;
			current2 = l;
			draggedRealPointsOrd = true;
		}
		else
		{
			prev = activTrack.getHoloTraj(seqPrev).prevEditPoint(f);
			next = activTrack.getHoloTraj(seqNext).nextEditPoint(l);
			current = f;
			current2 = l;
			draggedMultiPointOrd = true;
		}
	}

	private void draggedPointTimeLin()
	{
		double dt,prevEditdt,Editdt,dtratio;
		HoloPoint p;
		double olddate = currentPoint.ddate;
		
		double ddelta = mouseCursor.getDate() - olddate;
		
		currentPoint.setDDate(Ut.clip(currentPoint.ddate + ddelta, prevPt.ddate + (nbPtMinus - 1), nextPt.ddate - (nbPtPlus - 1)));;
		prevEditdt = olddate - prevPt.ddate ;
		Editdt = currentPoint.ddate - prevPt.ddate;
		dtratio =  Editdt / prevEditdt;
		
		for (int i = prev + 1; i < current; i++)
		{
			p = currentSeq.points.get(i);
			p.setDDate(prevPt.ddate + ( p.ddate - prevPt.ddate ) * dtratio);
		}
		prevEditdt = nextPt.ddate - olddate  ;
		Editdt = nextPt.ddate - currentPoint.ddate;
		dtratio =  Editdt / prevEditdt;
		
		for (int i = current + 1; i < next; i++)
		{
			p = currentSeq.points.get(i);
			p.setDDate(currentPoint.ddate + ( p.ddate - olddate ) * dtratio) ;
		}
	}

	private void draggedPointTimeAcc()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		currentPoint.date = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), nextPt.date - (nbPtPlus - 1));
		float rapportTemps = (float) (currentPoint.date - prevPt.date) / (float) (nextPt.date - prevPt.date);
		float rapportIndice = (float) (current - prev) / (float) (next - prev);
		float coeffHyperbole = calculCoeffHyperbole(rapportIndice, rapportTemps);
		for (int i = prev + 1; i < next; i++)
		{
			float rapportIndice2 = (float) (i - prev) / (float) (next - prev);
			float valHyperbole = calculValHyperbole(rapportIndice2, coeffHyperbole);
			currentSeq.points.get(i).date = prevPt.date + (int) (valHyperbole * (nextPt.date - prevPt.date));
		}
	}

	private void draggedPointTimeTan()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), nextPt.date - (nbPtPlus - 1));
		float rapportTempsTan = (float) (newDate - prevPt.date) / (float) (nextPt.date - prevPt.date);
		float rapportIndiceTan = (float) (current - prev) / (float) (next - prev);
		float coeffHyperboleTan = calculCoeffHyperbole(rapportIndiceTan, rapportTempsTan);
		for (int i = prev + 1; i < current; i++)
		{
			float rapportIndiceTan2 = (float) (i - prev) / (float) (current - prev);
			float valHyperboleTan = calculValHyperbole(rapportIndiceTan2, coeffHyperboleTan);
			currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * (currentPoint.date - prevPt.date));
		}
		for (int i = current + 1; i < next; i++)
		{
			float rapportIndiceTan2 = (float) (i - current) / (float) (next - current);
			float valHyperboleTan = calculValHyperbole(1 - rapportIndiceTan2, coeffHyperboleTan);
			currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * (nextPt.date - currentPoint.date));
		}
	}

	private void draggedPointTimeBeginLin()
	{
		// PAS DE PRECEDENT		
		double dt,prevEditdt,Editdt,dtratio;
		HoloPoint p;
		double olddate = currentPoint.ddate;
		
		double ddelta = mouseCursor.getDate() - olddate;
		
		if (seqNum > 0)
			currentPoint.setDDate(Ut.clip(currentPoint.ddate + ddelta, activTrack.getHoloTraj(seqNum - 1).lastElement().date + 1, nextPt.ddate - (nbPtPlus - 1)));
		else
			currentPoint.setDDate(Ut.clip(currentPoint.ddate + ddelta, 0, nextPt.ddate - (nbPtPlus - 1)));
		prevEditdt = nextPt.ddate - olddate  ;
		Editdt = nextPt.ddate - currentPoint.ddate;
		dtratio =  Editdt / prevEditdt;
		
		for (int i = current + 1; i < next; i++)
		{
			p = currentSeq.points.get(i);
			p.setDDate(currentPoint.ddate + ( p.ddate - olddate ) * dtratio) ;
		}
				
	}

	private void draggedPointTimeBeginTan()
	{
		// PAS DE PRECEDENT
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = currentPoint.date + delta;
		float a = Ut.max(seqNum > 0 ? activTrack.getHoloTraj(seqNum - 1).lastElement().date : 0, beg);
		float b = Ut.min(nextPt.date, end);
		float coeffHyperboleTan = calculCoeffHyperbole2(newDate, currentPoint.date, a, b);
		for (int i = current + 1; i < next; i++)
		{
			float rapportIndiceTan2 = (float) (i - current) / (float) (next - current);
			float valHyperboleTan = calculValHyperbole(1 - rapportIndiceTan2, coeffHyperboleTan);
			currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * (nextPt.date - currentPoint.date));
		}
	}
	
	private void draggedPointTimeEndLin()
	{
		// PAS DE SUIVANT
		
		double dt,prevEditdt,Editdt,dtratio;
		HoloPoint p;
		double olddate = currentPoint.ddate;
		
		double ddelta = mouseCursor.getDate() - olddate;
		if (seqNum < activTrack.trajs.size() - 1)
			currentPoint.setDDate(Ut.clip(currentPoint.ddate + ddelta, prevPt.ddate + (nbPtMinus - 1), activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1));
		else
			currentPoint.setDDate(Ut.clipL(currentPoint.ddate + ddelta, prevPt.ddate + (nbPtMinus - 1)));
		prevEditdt = olddate - prevPt.ddate ;
		Editdt = currentPoint.ddate - prevPt.ddate;
		dtratio =  Editdt / prevEditdt;
		
		for (int i = prev + 1; i < current; i++)
		{
			p = currentSeq.points.get(i);
			p.setDDate(prevPt.ddate + ( p.ddate - prevPt.ddate ) * dtratio);
		}
		

	}
	
	private void draggedPointTimeEndTan()
	{
		// PAS DE SUIVANT
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = currentPoint.date + delta;
		float a = Ut.max(prevPt.date, beg);
		float b = Ut.min(seqNum < activTrack.trajs.size() - 1 ? activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1 : end, end);
		float coeffHyperboleTan = calculCoeffHyperbole2(newDate, currentPoint.date, a, b);
		for (int i = prev + 1; i < current; i++)
		{
			float rapportIndiceTan2 = (float) (i - prev) / (float) (current - prev);
			float valHyperboleTan = calculValHyperbole(rapportIndiceTan2, coeffHyperboleTan);
			currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * (currentPoint.date - prevPt.date));
		}
	}

	private void draggedMultiPointTimeLin()
	{
		float date, dt;
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		currentPoint.date = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), nextPt.date - (nbPtPlus - 1));
		dt = (float) (currentPoint.date - prevPt.date) / nbPtMinus;
		date = prevPt.date + dt;
		if (seqPrev != seqNum)
		{
			// seqPrev
			HoloTraj ps = activTrack.getHoloTraj(seqPrev);
			for (int i = prev + 1; i < ps.size(); i++)
			{
				ps.points.get(i).date = (int) date;
				date += dt;
			}
			// betwween seqPrev & seqNum
			for (int k = seqPrev + 1; k < seqNum; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					t.points.get(i).date = (int) date;
					date += dt;
				}
			}
			// seqNum < current
			for (int i = 0; i < current; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
		else
		{
			// seqNum < current
			for (int i = prev + 1; i < current; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
		dt = (float) (nextPt.date - currentPoint.date) / nbPtPlus;
		date = currentPoint.date + dt;
		if (seqNext != seqNum)
		{
			// seqNum > current
			for (int i = current + 1; i < currentSeq.size(); i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
			// between seqNum & seqNext
			for (int k = seqNum + 1; k < seqNext; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					t.points.get(i).date = (int) date;
					date += dt;
				}
			}
			// seqNext < next
			HoloTraj ns = activTrack.getHoloTraj(seqNext);
			for (int i = 0; i < next; i++)
			{
				ns.points.get(i).date = (int) date;
				date += dt;
			}
		}
		else
		{
			for (int i = current + 1; i < next; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
	}

	private void draggedMultiPointTimeBeginLin()
	{
		float date, dt;
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		if (seqNum > 0)
			currentPoint.date = Ut.clip(currentPoint.date + delta, activTrack.getHoloTraj(seqNum - 1).lastElement().date + 1, nextPt.date - (nbPtPlus - 1));
		else currentPoint.date = Ut.clip(currentPoint.date + delta, 0, nextPt.date - (nbPtPlus - 1));
		dt = (float) (nextPt.date - currentPoint.date) / nbPtPlus;
		date = currentPoint.date + dt;
		if (seqNext != seqNum)
		{
			// seqNum > current
			for (int i = current + 1; i < currentSeq.size(); i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
			// between seqNum & seqNext
			for (int k = seqNum + 1; k < seqNext; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					t.points.get(i).date = (int) date;
					date += dt;
				}
			}
			// seqNext < next
			HoloTraj ns = activTrack.getHoloTraj(seqNext);
			for (int i = 0; i < next; i++)
			{
				ns.points.get(i).date = (int) date;
				date += dt;
			}
		}
		else
		{
			for (int i = current + 1; i < next; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
	}

	private void draggedMultiPointTimeEndLin()
	{
		float date, dt;
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		if (seqNum < activTrack.trajs.size() - 1)
			currentPoint.date = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1);
		else currentPoint.date = Ut.clipL(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1));
		dt = (float) (currentPoint.date - prevPt.date) / nbPtMinus;
		date = prevPt.date + dt;
		if (seqPrev != seqNum)
		{
			// seqPrev
			HoloTraj ps = activTrack.getHoloTraj(seqPrev);
			for (int i = prev + 1; i < ps.size(); i++)
			{
				ps.points.get(i).date = (int) date;
				date += dt;
			}
			// betwween seqPrev & seqNum
			for (int k = seqPrev + 1; k < seqNum; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					t.points.get(i).date = (int) date;
					date += dt;
				}
			}
			// seqNum < current
			for (int i = 0; i < current; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
		else
		{
			// seqNum < current
			for (int i = prev + 1; i < current; i++)
			{
				currentSeq.points.get(i).date = (int) date;
				date += dt;
			}
		}
	}

	private void draggedMultiPointTimeAcc()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		currentPoint.date = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), nextPt.date - (nbPtPlus - 1));
		float rapportTemps = (float) (currentPoint.date - prevPt.date) / (float) (nextPt.date - prevPt.date);
		float totalNbPoints = nbPtMinus + nbPtPlus;
		float rapportIndice = nbPtMinus / totalNbPoints;
		float coeffHyperbole = calculCoeffHyperbole(rapportIndice, rapportTemps);
		float cpt = 1;
		int totalDelta = nextPt.date - prevPt.date;
		if (seqPrev != seqNum)
		{
			// seqPrev
			HoloTraj ps = activTrack.getHoloTraj(seqPrev);
			for (int i = prev + 1; i < ps.size(); i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				ps.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
			// betwween seqPrev & seqNum
			for (int k = seqPrev + 1; k < seqNum; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
					t.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
					cpt++;
				}
			}
			// seqNum < current
			for (int i = 0; i < current; i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
		}
		else
		{
			// seqNum < current
			for (int i = prev + 1; i < current; i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
		}
		cpt++;
		if (seqNext != seqNum)
		{
			// seqNum > current
			for (int i = current + 1; i < currentSeq.size(); i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
			// between seqNum & seqNext
			for (int k = seqNum + 1; k < seqNext; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
					t.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
					cpt++;
				}
			}
			// seqNext < next
			HoloTraj ns = activTrack.getHoloTraj(seqNext);
			for (int i = 0; i < next; i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				ns.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
		}
		else
		{
			for (int i = current + 1; i < next; i++)
			{
				float valHyperbole = calculValHyperbole(cpt / totalNbPoints, coeffHyperbole);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperbole * totalDelta);
				cpt++;
			}
		}
	}

	private void draggedMultiPointTimeTan()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = Ut.clip(currentPoint.date + delta, prevPt.date + (nbPtMinus - 1), nextPt.date - (nbPtPlus - 1));
		float totalNbPoints = nbPtMinus + nbPtPlus;
		float rapportTempsTan = (float) (newDate - prevPt.date) / (float) (nextPt.date - prevPt.date);
		float rapportIndiceTan = nbPtMinus / totalNbPoints;
		float coeffHyperboleTan = calculCoeffHyperbole(rapportIndiceTan, rapportTempsTan);
		float cpt = 1;
		int totalDelta = currentPoint.date - prevPt.date;
		if (seqPrev != seqNum)
		{
			// seqPrev
			HoloTraj ps = activTrack.getHoloTraj(seqPrev);
			for (int i = prev + 1; i < ps.size(); i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				ps.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
			// betwween seqPrev & seqNum
			for (int k = seqPrev + 1; k < seqNum; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
					t.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
					cpt++;
				}
			}
			// seqNum < current
			for (int i = 0; i < current; i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
		else
		{
			// seqNum < current
			for (int i = prev + 1; i < current; i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
		cpt = 1;
		totalDelta = nextPt.date - currentPoint.date;
		if (seqNext != seqNum)
		{
			// seqNum > current
			for (int i = current + 1; i < currentSeq.size(); i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
			// between seqNum & seqNext
			for (int k = seqNum + 1; k < seqNext; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
					t.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
					cpt++;
				}
			}
			// seqNext < next
			HoloTraj ns = activTrack.getHoloTraj(seqNext);
			for (int i = 0; i < next; i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				ns.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
		else
		{
			for (int i = current + 1; i < next; i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
	}

	private void draggedMultiPointTimeBeginTan()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = currentPoint.date + delta;
		float a = Ut.max(seqNum > 0 ? activTrack.getHoloTraj(seqNum - 1).lastElement().date : 0, beg);
		float b = Ut.min(nextPt.date, end);
		float coeffHyperboleTan = calculCoeffHyperbole2(newDate, currentPoint.date, a, b);
		float cpt = 1;
		float totalDelta = nextPt.date - currentPoint.date;
		if (seqNext != seqNum)
		{
			// seqNum > current
			for (int i = current + 1; i < currentSeq.size(); i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
			// between seqNum & seqNext
			for (int k = seqNum + 1; k < seqNext; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
					t.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
					cpt++;
				}
			}
			// seqNext < next
			HoloTraj ns = activTrack.getHoloTraj(seqNext);
			for (int i = 0; i < next; i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				ns.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
		else
		{
			for (int i = current + 1; i < next; i++)
			{
				float valHyperboleTan = calculValHyperbole((nbPtPlus - cpt) / nbPtPlus, coeffHyperboleTan);
				currentSeq.points.get(i).date = nextPt.date - (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
	}

	private void draggedMultiPointTimeEndTan()
	{
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		int newDate = currentPoint.date + delta;
		float a = Ut.max(prevPt.date, beg);
		float b = Ut.min(seqNum < activTrack.trajs.size() - 1 ? activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1 : end, end);
		float coeffHyperboleTan = calculCoeffHyperbole2(newDate, currentPoint.date, a, b);
		float cpt = 1;
		int totalDelta = currentPoint.date - prevPt.date;
		if (seqPrev != seqNum)
		{
			// seqPrev
			HoloTraj ps = activTrack.getHoloTraj(seqPrev);
			for (int i = prev + 1; i < ps.size(); i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				ps.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
			// betwween seqPrev & seqNum
			for (int k = seqPrev + 1; k < seqNum; k++)
			{
				HoloTraj t = activTrack.getHoloTraj(k);
				for (int i = 0; i < t.size(); i++)
				{
					float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
					t.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
					cpt++;
				}
			}
			// seqNum < current
			for (int i = 0; i < current; i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
		else
		{
			// seqNum < current
			for (int i = prev + 1; i < current; i++)
			{
				float valHyperboleTan = calculValHyperbole(cpt / nbPtMinus, coeffHyperboleTan);
				currentSeq.points.get(i).date = prevPt.date + (int) (valHyperboleTan * totalDelta);
				cpt++;
			}
		}
	}

	private void draggedTimeAlone()
	{
		// NI SUIVANT NI PRECEDENT;
		int delta = (int) (mouseCursor.getDate() - oldCurrentX);
		if (seqNum > 0 && seqNum < activTrack.trajs.size() - 1)
			currentPoint.date = Ut.clip(currentPoint.date + delta, activTrack.getHoloTraj(seqNum - 1).lastElement().date + 1, activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1);
		else if (seqNum > 0)
			currentPoint.date = Ut.clipL(currentPoint.date + delta, activTrack.getHoloTraj(seqNum - 1).lastElement().date + 1);
		else if (seqNum < activTrack.trajs.size() - 1)
			currentPoint.date = Ut.clip(currentPoint.date + delta, 0, activTrack.getHoloTraj(seqNum + 1).firstElement().date - 1);
		else currentPoint.date = Ut.clipL(currentPoint.date + delta, 0);
	}

	private void draggedPointOrd()
	{
		curves.get(curveSelected).dragPoint();
	}

	private void draggedRealPointsOrd()
	{
		curves.get(curveSelected).dragRealPoints();
	}

	private void draggedMultiPointOrd()
	{
		curves.get(curveSelected).dragMultiPoints();
	}

	private float calculCoeffHyperbole(float x, float y)
	{
		return (y * (x - 1)) / (x * (y - 1));
	}

	private float calculCoeffHyperbole2(float n, float c, float a, float b)
	{
		float d = n - c;
		if (d == 0)
			return 1;
		float co;
		if (d > 0)
			co = 1 + (float) Math.pow((n - c) / (b - c), 3) * 49;
		else co = 1 - (float) Math.pow((c - n) / (c - a), 0.33333333);
		return Ut.clip(co, 0.02f, 50);
	}

	private float calculValHyperbole(float x, float n)
	{
		if (n == 0)
			return x;
		return (n * x) / (1 + x * (n - 1));
	}

	public void mouseMoved(MouseEvent e)
	{
		query_one_select = true;
		selected = TimeIndex.getNull();
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		query_one_select = true;
		selected = TimeIndex.getNull();
		float posW = e.getX() - W_CURVE_HEADER;
		float posH = H - (e.getY() - H_LOCAL_TIME_SCALE);
		mousex = e.getX();
		mousey = height - e.getY();
		mouseCursor = convPosPt(posW, posH);
		if ((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))
		{
			dT = (int) (e.getUnitsToScroll() * (end - beg) / (e.isShiftDown() ? 20 * holoEditRef.scrollSpeed : 2 * holoEditRef.scrollSpeed));
		}
		else
		{
			zoomTime = (int) convPosLocalTime(posW);
			zoom = Ut.clip(zoom - (e.getUnitsToScroll() / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)), 0.025f, 1);
		}
		holoEditRef.room.display();
		Ut.barMenu.update();
	}

	public void treatSelIndex()
	{
		/*if (selIndex.isEmpty())
			return;
		*/
		
		IntegerVector v = new IntegerVector(selIndex);
		v.sort();
		selIndex = new Vector<Integer>();
		selPoints = new Vector<HoloPoint>();
		
		for (int i : v)
		{
			int[] iA = TimeIndex.decode(i);
			if (TimeIndex.isPoint())
			{
				HoloPoint p = activTrack.getHoloTraj(iA[2]).points.get(iA[3]);
				if (!selPoints.contains(p) && (p.isEditable() || !selMode))
				{
					selPoints.add(p);
					selIndex.add(i);
					
				}
			}
		}
		
		updateSelRefs();
	}
	
	public void updateSelRefs()
	{
		HoloPointBackRef hp_ref;
		selRefs.clear();
		IntegerVector v = new IntegerVector(selIndex);
		v.sort();
		
		for (int i : v)
		{
			int[] iA = TimeIndex.decode(i);
			if (TimeIndex.isPoint())
			{
				HoloPoint p = activTrack.getHoloTraj(iA[2]).points.get(iA[3]);
				hp_ref = new HoloPointBackRef(p);
				hp_ref.setFromTime(i, activTrack.getNumber()-1);
				selRefs.add(hp_ref);
					
			}
		}
		
		holoEditRef.gestionPistes.setDirty(Ut.DIRTY_ROOM);
		holoEditRef.room.setSelectedPoints(selRefs);
	}
	
	public void setSelectedPoints(Vector<HoloPointBackRef> _selRefs)
	{
		
		selIndex.clear();
		selPoints.clear();
		
		if(activTrack == null)
			activTrack = holoEditRef.gestionPistes.getActiveTrack();
		
		for(HoloPointBackRef r : _selRefs)
		{
			if(holoEditRef.gestionPistes.tracks.get(r.tkNum).getNumber() == activTrack.getNumber() )
			{		
				selIndex.add(r.encodeTime());
				selPoints.add(r.p);
			}
		}
		
		//Ut.print("setSel with "+_selRefs.size()+" ---> "+selIndex.size()+"-"+selPoints.size());
		
		display();
	}

	private void removePoints()
	{
		if (selIndex.isEmpty())
			return;
		int bb = 1000000000;
		int ee = 0;
		HoloPoint p;
		for (int k : selIndex)
		{
			int[] iA = TimeIndex.decode(k);
			p = activTrack.getHoloTraj(iA[2]).points.get(iA[3]);
			bb = Ut.min(p.date, bb);
			ee = Ut.max(p.date, ee);
		}
		Algors a = new Algors(holoEditRef.gestionPistes);
		a.dateBegin = bb;
		a.dateEnd = ee;
		a.Erase(activTrack, false);
		activTrack.update();
		selIndex = new Vector<Integer>();
		selPoints = new Vector<HoloPoint>();
		updateSelRefs();
		
		selected = TimeIndex.getNull();
	}
	
	private void changeEditable(boolean ed)
	{
		if (selIndex.isEmpty())
			return;
		int bb = 1000000000;
		int ee = 0;
		HoloPoint p;
		for (int k : selIndex)
		{
			int[] iA = TimeIndex.decode(k);
			p = activTrack.getHoloTraj(iA[2]).points.get(iA[3]);
			if(!p.equals(activTrack.getHoloTraj(iA[2]).firstElement()) && !p.equals(activTrack.getHoloTraj(iA[2]).lastElement()))
			p.setEditable(ed);
		}
	}
	
	private class Curve
	{
		int param;
		float hpercent;
		float minY = -100;
		float maxY = 100;
		int scale;
		int scaleID = -1;
		JComboBox combo = new JComboBox(curveNames);

		public Curve(int curveParam)
		{
			param = curveParam;
			initScale();
		}

		public void changeParam(int i)
		{
			param = i;
			initScale();
		}

		public void addPoint(CurvePoint p)
		{
			int date = (int) p.getDate();
			int prevpi = activTrack.previousPoint(date);
			HoloPoint pp;
			if(prevpi > 0)
				pp = activTrack.get(prevpi);
			else
				pp = new HoloPoint(0.f,50.f,0.f,false);
			
			currentPoint = convPoint(p,pp);
			int ind = activTrack.prevTraj2(date);
			if (ind == -1)
			{
				ind = activTrack.nextTraj(date);
			}
			if (ind != -1)
			{
				HoloTraj tj = activTrack.getHoloTraj(ind);
				if (tj.getLastDate() < beg)
					ind = -1;
				else
				{
					if (Ut.between(date, tj.getFirstDate(), tj.getLastDate()))
					{
						tj.insertPointAt(p, param);
					}
					else if (date < tj.getFirstDate())
					{
						currentPoint.setEditable(true);
						tj.addPointBegin(currentPoint, holoEditRef.gestionPistes.nbPointsInter);
					}
					else if (date > tj.getLastDate())
					{
						currentPoint.setEditable(true);
						tj.addPointEnd(currentPoint, holoEditRef.gestionPistes.nbPointsInter);
					}
				}
			}
			if (ind == -1)
			{
				HoloTraj ht = new HoloTraj();
				currentPoint.setEditable(true);
				ht.add(currentPoint);
				activTrack.addTraj(ht, ht.getFirstDate());
			}
			activTrack.update();
		}

		public void addPointSolo(CurvePoint p)
		{
			int date = (int) p.getDate();
			int prevpi = activTrack.previousPoint(date);
			HoloPoint pp;
			if(prevpi > 0)
				pp = activTrack.get(prevpi);
			else
				pp = new HoloPoint(0.f,50.f,0.f,false);
			currentPoint = convPoint(p,pp);
			int ind = activTrack.prevTraj2(date);
			if (ind == -1)
			{
				ind = activTrack.nextTraj(date);
			}
			if (ind != -1)
			{
				HoloTraj tj = activTrack.getHoloTraj(ind);
				if (tj.getLastDate() < beg)
					ind = -1;
				else
				{
					if (Ut.between(date, tj.getFirstDate(), tj.getLastDate()))
					{
						tj.insertPointAt(p, param);
					}
					else
					{
						HoloTraj ht = new HoloTraj();
						currentPoint.setEditable(true);
						ht.add(currentPoint);
						activTrack.addTraj(ht, ht.getFirstDate());
					}
				}
			}
			if (ind == -1)
			{
				HoloTraj ht = new HoloTraj();
				currentPoint.setEditable(true);
				ht.add(currentPoint);
				activTrack.addTraj(ht, ht.getFirstDate());
			}
			activTrack.update();
		}

		
		public void dragPoint()
		{
			boolean polar = false;
			float oldX = currentPoint.x;
			float oldY = currentPoint.y;
			float oldZ = currentPoint.z;
			float oldT = currentPoint.getTheta();
			float oldR = currentPoint.getRay();
			switch (param)
			{
			case CURVE_X:
				currentPoint.x = HoloPoint.limit2D(mouseCursor.getVal());
				polar = false;
				break;
			case CURVE_Y:
				currentPoint.y = HoloPoint.limit2D(mouseCursor.getVal());
				polar = false;
				break;
			case CURVE_Z:
				currentPoint.z = HoloPoint.limitZ(mouseCursor.getVal());
				polar = false;
				break;
			case CURVE_R:
				currentPoint.setRay(Ut.clipL(mouseCursor.getVal(), 0));
				polar = true;
				break;
			case CURVE_T:
				currentPoint.setTheta(mouseCursor.getVal());
				polar = true;
				break;
			}
			if (currentPoint.isEditable() && !polar)
			{
				float deltaX = currentPoint.x - oldX;
				if (deltaX != 0)
				{
					float dx, X;
					if (prev != -1)
					{
						dx = deltaX / (current - prev);
						X = dx;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X += dx;
						}
					}
					if (next != -1)
					{
						dx = deltaX / (next - current);
						X = deltaX - dx;
						for (int i = current + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X -= dx;
						}
					}
				}
				float deltaY = currentPoint.y - oldY;
				if (deltaY != 0)
				{
					float dy, Y;
					if (prev != -1)
					{
						dy = deltaY / (current - prev);
						Y = dy;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y += dy;
						}
					}
					if (next != -1)
					{
						dy = deltaY / (next - current);
						Y = deltaY - dy;
						for (int i = current + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y -= dy;
						}
					}
				}
				float deltaZ = currentPoint.z - oldZ;
				if (deltaZ != 0)
				{
					float dz, Z;
					if (prev != -1)
					{
						dz = deltaZ / (current - prev);
						Z = dz;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z += dz;
						}
					}
					if (next != -1)
					{
						dz = deltaZ / (next - current);
						Z = deltaZ - dz;
						for (int i = current + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z -= dz;
						}
					}
				}
			}
			else if (currentPoint.isEditable() && polar)
			{
				float deltaT = currentPoint.getTheta() - oldT;
				//Ut.print(" delta : "+deltaT);
				//T edit wrapping
				if(deltaT < -180.)
					deltaT += 360.;
				else if (deltaT > 180.)
					deltaT-= 360.;
				
				if (deltaT != 0)
				{
					float dT, T;
					if (prev != -1)
					{
						dT = deltaT / (current - prev);
						T = dT;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setTheta(p.getTheta()+T);
							T += dT;
						}
					}
					if (next != -1)
					{
						dT = deltaT / (next - current);
						T = deltaT - dT;
						for (int i = current + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setTheta(p.getTheta()+T);
							T -= dT;
						}
					}
				}
				
				float deltaR = currentPoint.getRay() - oldR;
				if (deltaR != 0)
				{
					float dR, R;
					if (prev != -1)
					{
						dR = deltaR / (current - prev);
						R = dR;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setRay(Ut.clipL(p.getRay()+R,0));
							R += dR;
						}
					}
					if (next != -1)
					{
						dR = deltaR / (next - current);
						R = deltaR - dR;
						for (int i = current + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setRay(Ut.clipL(p.getRay()+R,0));
							R -= dR;
						}
					}
				}
			}
		}

		public void dragRealPoints()
		{
			switch (param)
			{
			case CURVE_X:
				float oldX = currentPoint.x;
				currentPoint.x = HoloPoint.limit2D(mouseCursor.getVal());
				float deltaX = currentPoint.x - oldX;
				if (deltaX != 0)
				{
					float dx, X;
					if (prev != -1)
					{
						dx = deltaX / (current - prev);
						X = dx;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X += dx;
						}
					}
					for (int i = current; i <= current2; i++)
					{
						HoloPoint p = currentSeq.points.get(i);
						if (p != currentPoint)
							p.x = HoloPoint.limit2D(p.x + deltaX);
					}
					if (next != -1)
					{
						dx = deltaX / (next - current2);
						X = deltaX - dx;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X -= dx;
						}
					}
				}
				break;
			case CURVE_Y:
				float oldY = currentPoint.y;
				currentPoint.y = HoloPoint.limit2D(mouseCursor.getVal());
				float deltaY = currentPoint.y - oldY;
				if (deltaY != 0)
				{
					float dy, Y;
					if (prev != -1)
					{
						dy = deltaY / (current - prev);
						Y = dy;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y += dy;
						}
					}
					for (int i = current; i <= current2; i++)
					{
						HoloPoint p = currentSeq.points.get(i);
						if (p != currentPoint)
							p.y = HoloPoint.limit2D(p.y + deltaY);
					}
					if (next != -1)
					{
						dy = deltaY / (next - current2);
						Y = deltaY - dy;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y -= dy;
						}
					}
				}
				break;
			case CURVE_Z:
				float oldZ = currentPoint.z;
				currentPoint.z = HoloPoint.limitZ(mouseCursor.getVal());
				float deltaZ = currentPoint.z - oldZ;
				if (deltaZ != 0)
				{
					float dz, Z;
					if (prev != -1)
					{
						dz = deltaZ / (current - prev);
						Z = dz;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z += dz;
						}
					}
					for (int i = current; i <= current2; i++)
					{
						HoloPoint p = currentSeq.points.get(i);
						if (p != currentPoint)
							p.z = HoloPoint.limitZ(p.z + deltaZ);
					}
					if (next != -1)
					{
						dz = deltaZ / (next - current2);
						Z = deltaZ - dz;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z -= dz;
						}
					}
				}
				break;
			case CURVE_R:
				float oldRay = currentPoint.getModule();
				currentPoint.setRay(Ut.clipL(mouseCursor.getVal(), 0));
				float deltaR = currentPoint.getModule() - oldRay;
				if (deltaR != 0)
				{
					float dr, R;
					if (prev != -1)
					{
						dr = deltaR / (current - prev);
						R = dr;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setRay(p.getModule()+R);
							R += dr;
						}
					}
					for (int i = current; i <= current2; i++)
					{
						HoloPoint p = currentSeq.points.get(i);
						if (p != currentPoint)
							p.setRay(p.getModule()+deltaR);
					}
					if (next != -1)
					{
						dr = deltaR / (next - current2);
						R = deltaR - dr;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setRay(p.getModule()+R);
							R -= dr;
						}
					}
				}
				break;
			case CURVE_T:
				float oldTheta = currentPoint.getTheta();
				currentPoint.setTheta(mouseCursor.getVal());
				float deltaT = currentPoint.getTheta() - oldTheta;
				if (deltaT != 0)
				{
					float dt, T;
					if (prev != -1)
					{
						dt = deltaT / (current - prev);
						T = dt;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setTheta(p.getTheta()+T);
							T += dt;
						}
					}
					for (int i = current; i <= current2; i++)
					{
						HoloPoint p = currentSeq.points.get(i);
						if (p != currentPoint)
							p.setTheta(p.getTheta()+deltaT);
					}
					if (next != -1)
					{
						dt = deltaT / (next - current2);
						T = deltaT - dt;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = currentSeq.points.get(i);
							p.setTheta(p.getTheta()+T);
							T -= dt;
						}
					}
				}
				break;
			}
		}

		public void dragMultiPoints()
		{
			switch (param)
			{
			case CURVE_X:
				float oldX = currentPoint.x;
				currentPoint.x = HoloPoint.limit2D(mouseCursor.getVal());
				float deltaX = currentPoint.x - oldX;
				if (deltaX != 0)
				{
					float dx, X;
					HoloTraj ps = activTrack.getHoloTraj(seqPrev);
					if (prev != -1)
					{
						dx = deltaX / (current - prev);
						X = dx;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = ps.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X += dx;
						}
					}
					for (int i = current; i < ps.size(); i++)
					{
						HoloPoint p = ps.points.get(i);
						if (p != currentPoint)
							p.x = HoloPoint.limit2D(p.x + deltaX);
					}
					for (int k = seqPrev + 1; k < seqNext; k++)
					{
						HoloTraj t = activTrack.getHoloTraj(k);
						for (int i = 0; i < t.size(); i++)
						{
							HoloPoint p = t.points.get(i);
							if (p != currentPoint)
								p.x = HoloPoint.limit2D(p.x + deltaX);
						}
					}
					HoloTraj ns = activTrack.getHoloTraj(seqNext);
					for (int i = 0; i <= current2; i++)
					{
						HoloPoint p = ns.points.get(i);
						if (p != currentPoint)
							p.x = HoloPoint.limit2D(p.x + deltaX);
					}
					if (next != -1)
					{
						dx = deltaX / (next - current2);
						X = deltaX - dx;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = ns.points.get(i);
							p.x = HoloPoint.limit2D(p.x + X);
							X -= dx;
						}
					}
				}
				break;
			case CURVE_Y:
				float oldY = currentPoint.y;
				currentPoint.y = HoloPoint.limit2D(mouseCursor.getVal());
				float deltaY = currentPoint.y - oldY;
				if (deltaY != 0)
				{
					float dy, Y;
					HoloTraj ps = activTrack.getHoloTraj(seqPrev);
					if (prev != -1)
					{
						dy = deltaY / (current - prev);
						Y = dy;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = ps.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y += dy;
						}
					}
					for (int i = current; i < ps.size(); i++)
					{
						HoloPoint p = ps.points.get(i);
						if (p != currentPoint)
							p.y = HoloPoint.limit2D(p.y + deltaY);
					}
					for (int k = seqPrev + 1; k < seqNext; k++)
					{
						HoloTraj t = activTrack.getHoloTraj(k);
						for (int i = 0; i < t.size(); i++)
						{
							HoloPoint p = t.points.get(i);
							if (p != currentPoint)
								p.y = HoloPoint.limit2D(p.y + deltaY);
						}
					}
					HoloTraj ns = activTrack.getHoloTraj(seqNext);
					for (int i = 0; i <= current2; i++)
					{
						HoloPoint p = ns.points.get(i);
						if (p != currentPoint)
							p.y = HoloPoint.limit2D(p.y + deltaY);
					}
					if (next != -1)
					{
						dy = deltaY / (next - current2);
						Y = deltaY - dy;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = ns.points.get(i);
							p.y = HoloPoint.limit2D(p.y + Y);
							Y -= dy;
						}
					}
				}
				break;
			case CURVE_Z:
				float oldZ = currentPoint.z;
				currentPoint.z = HoloPoint.limitZ(mouseCursor.getVal());
				float deltaZ = currentPoint.z - oldZ;
				if (deltaZ != 0)
				{
					float dz, Z;
					HoloTraj ps = activTrack.getHoloTraj(seqPrev);
					if (prev != -1)
					{
						dz = deltaZ / (current - prev);
						Z = dz;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = ps.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z += dz;
						}
					}
					for (int i = current; i < ps.size(); i++)
					{
						HoloPoint p = ps.points.get(i);
						if (p != currentPoint)
							p.z = HoloPoint.limitZ(p.z + deltaZ);
					}
					for (int k = seqPrev + 1; k < seqNext; k++)
					{
						HoloTraj t = activTrack.getHoloTraj(k);
						for (int i = 0; i < t.size(); i++)
						{
							HoloPoint p = t.points.get(i);
							if (p != currentPoint)
								p.z = HoloPoint.limitZ(p.z + deltaZ);
						}
					}
					HoloTraj ns = activTrack.getHoloTraj(seqNext);
					for (int i = 0; i <= current2; i++)
					{
						HoloPoint p = ns.points.get(i);
						if (p != currentPoint)
							p.z = HoloPoint.limitZ(p.z + deltaZ);
					}
					if (next != -1)
					{
						dz = deltaZ / (next - current2);
						Z = deltaZ - dz;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = ns.points.get(i);
							p.z = HoloPoint.limitZ(p.z + Z);
							Z -= dz;
						}
					}
				}
				break;
			case CURVE_R:
				float oldRay = currentPoint.getModule();
				currentPoint.setRay(Ut.clipL(mouseCursor.getVal(), 0));
				float deltaRay = currentPoint.getModule() - oldRay;
				if (deltaRay != 0)
				{
					float dr, R;
					HoloTraj ps = activTrack.getHoloTraj(seqPrev);
					if (prev != -1)
					{
						dr = deltaRay / (current - prev);
						R = dr;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = ps.points.get(i);
							p.setRay(p.getModule()+R);
							R += dr;
						}
					}
					for (int i = current; i < ps.size(); i++)
					{
						HoloPoint p = ps.points.get(i);
						if (p != currentPoint)
							p.setRay(p.getModule()+deltaRay);
					}
					for (int k = seqPrev + 1; k < seqNext; k++)
					{
						HoloTraj t = activTrack.getHoloTraj(k);
						for (int i = 0; i < t.size(); i++)
						{
							HoloPoint p = t.points.get(i);
							if (p != currentPoint)
								p.setRay(p.getModule()+deltaRay);
						}
					}
					HoloTraj ns = activTrack.getHoloTraj(seqNext);
					for (int i = 0; i <= current2; i++)
					{
						HoloPoint p = ns.points.get(i);
						if (p != currentPoint)
							p.setRay(p.getModule()+deltaRay);
					}
					if (next != -1)
					{
						dr = deltaRay / (next - current2);
						R = deltaRay - dr;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = ns.points.get(i);
							p.setRay(p.getModule()+R);
							R -= dr;
						}
					}
				}
				break;
			case CURVE_T:
				float oldTheta = currentPoint.getTheta();
				currentPoint.setTheta(mouseCursor.getVal());
				float deltaT = currentPoint.getTheta() - oldTheta;
				if (deltaT != 0)
				{
					float dt, T;
					HoloTraj ps = activTrack.getHoloTraj(seqPrev);
					if (prev != -1)
					{
						dt = deltaT / (current - prev);
						T = dt;
						for (int i = prev + 1; i < current; i++)
						{
							HoloPoint p = ps.points.get(i);
							p.setTheta(p.getTheta()+T);
							T += dt;
						}
					}
					for (int i = current; i < ps.size(); i++)
					{
						HoloPoint p = ps.points.get(i);
						if (p != currentPoint)
							p.setTheta(p.getTheta()+deltaT);
					}
					for (int k = seqPrev + 1; k < seqNext; k++)
					{
						HoloTraj t = activTrack.getHoloTraj(k);
						for (int i = 0; i < t.size(); i++)
						{
							HoloPoint p = t.points.get(i);
							if (p != currentPoint)
								p.setTheta(p.getTheta()+deltaT);
						}
					}
					HoloTraj ns = activTrack.getHoloTraj(seqNext);
					for (int i = 0; i <= current2; i++)
					{
						HoloPoint p = ns.points.get(i);
						if (p != currentPoint)
							p.setTheta(p.getTheta()+deltaT);
					}
					if (next != -1)
					{
						dt = deltaT / (next - current2);
						T = deltaT - dt;
						for (int i = current2 + 1; i < next; i++)
						{
							HoloPoint p = ns.points.get(i);
							p.setTheta(p.getTheta()+T);
							T -= dt;
						}
					}
				}
				break;
			}
		}

		private HoloPoint convPoint(CurvePoint p,HoloPoint pp)
		{
			HoloPoint tmp = new HoloPoint(0, 0, 0, (int) p.getDate());
			switch (param)
			{
			case CURVE_X:
				tmp.x = HoloPoint.limit2D(p.getVal());
				tmp.y = pp.y;
				break;
			case CURVE_Y:
				tmp.y = HoloPoint.limit2D(p.getVal());
				tmp.x = pp.x;
				break;
			case CURVE_Z:
				tmp.z = HoloPoint.limitZ(p.getVal());
				tmp.z = pp.z;
				break;
			case CURVE_R:
				tmp.setAD(pp.getTheta(),p.getVal());
				break;
			case CURVE_T:
				tmp.setAD(p.getVal(), pp.getRay());
				Ut.post("convPoint "+p.getVal());
				break;
			}
			return tmp;
		}
		
		private void initScale()
		{
			scale = curveScales[param];
			scaleID = -1;
			switch (scale)
			{
			case SCALE_DATA:
				minY = 0f;
				maxY = 1f;
				break;
			case SCALE_SOUND:
				minY = -0.5f;
				maxY = 0.5f;
				break;
			case SCALE_THETA1:
				minY = 0;
				maxY = 360;
				break;
			case SCALE_THETA2:
				minY = -180;
				maxY = 180;
				break;
			case SCALE_ZRP:
				minY = 0;
				maxY = 100;
				break;
			case SCALE_XY:
				minY = -100;
				maxY = 100;
				break;
			default:
				minY = 0;
				maxY = 100;
				break;
			}
		}

		public void draw(boolean render, int curveNum, int pixelSize)
		{
			// CURVE
			glu.gluOrtho2D(beg, end, minY, maxY);
			OpenGLUt.glColor(gl, bgColor);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glLoadName(TimeIndex.encode(TimeIndex.TYPE_CURVE, curveNum, 0, 0));
			gl.glRectf(beg, minY, end, maxY);
			if (render)
			{
				OpenGLUt.glColor(gl, Color.BLACK);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(beg, minY);
				gl.glVertex2f(end, minY);
				gl.glVertex2f(beg, maxY);
				gl.glVertex2f(end, maxY);
				gl.glEnd();
				if (drawMousePos)
				{
					OpenGLUt.glColor(gl, mouseColor);
					gl.glBegin(GL.GL_LINES);
					if (curveNum == curveSelected)
					{
						gl.glVertex2f(beg, mouseCursor.getVal());
						gl.glVertex2f(end, mouseCursor.getVal());
					}
					gl.glVertex2f(mouseCursor.getDate(), minY);
					gl.glVertex2f(mouseCursor.getDate(), maxY);
					gl.glEnd();
				}
				if (draggedSelZone)
				{
					OpenGLUt.glColor(gl, selZoneColor);
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
					gl.glRectf(selZonePt1.getDate(), minY, selZonePt2.getDate(), maxY);
					OpenGLUt.glColor(gl, selZoneBorderColor);
					gl.glBegin(GL.GL_LINES);
					gl.glVertex2f(selZonePt1.getDate(), minY);
					gl.glVertex2f(selZonePt1.getDate(), maxY);
					gl.glVertex2f(selZonePt2.getDate(), minY);
					gl.glVertex2f(selZonePt2.getDate(), maxY);
					gl.glEnd();
				}
				int cursor = holoEditRef.connection.isPlaying() ? holoEditRef.connection.getCurrentTime() : holoEditRef.score.cursorTime;
				gl.glColor4fv(cursorColor, 0);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(cursor, minY);
				gl.glVertex2f(cursor, maxY);
				gl.glEnd();
			}
			if (activTrack != null)
				switch (param)
				{
				case CURVE_X:
					drawX(render, curveNum);
					break;
				case CURVE_Y:
					drawY(render, curveNum);
					break;
				case CURVE_Z:
					drawZ(render, curveNum);
					break;
				case CURVE_R:
					drawR(render, curveNum);
					break;
				case CURVE_T:
					drawT(render, curveNum);
					break;
				case CURVE_W:
					drawSound(render);
					break;
				case CURVE_D:
					drawData(render);
					break;
				}
			gl.glLoadIdentity();
		}

		private void drawSound(boolean render)
		{
			if (render && activTrack.isVisible() && !activTrack.waves.isEmpty())
				for (int k = 0, last2 = activTrack.waves.size(); k < last2; k++)
					activTrack.waves.get(k).drawTime(gl, activTrack.getColor(), beg, end, W);
		}

		private void drawData(boolean render)
		{
			if (render && activTrack.isVisible() && !activTrack.sdifdataInstanceVector.isEmpty())
				for (int k = 0; k < activTrack.sdifdataInstanceVector.size(); k++)
					activTrack.sdifdataInstanceVector.get(k).drawTime(gl, activTrack.getColor(), beg, end, W);
		}
		
		private void drawX(boolean render, int curveNum)
		{
			gl.glLineWidth(1);
			if (render)
				for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					if (currentTrack != activTrack && currentTrack.isVisible() && !currentTrack.trajs.isEmpty())
					{
						float[] cBg = OpenGLUt.convCol(currentTrack.getColor());
						cBg[3] = 0.3f;
						gl.glColor4fv(cBg, 0);
						for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
						{
							HoloTraj j = currentTrack.trajs.get(k);
							if (!j.isEmpty())
							{
								HoloPoint p1, p2;
								int ll = 0;
								gl.glBegin(GL.GL_LINE_STRIP);
								for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
								{
									p1 = j.points.get(l);
									p2 = j.points.get(l + 1);
									ll = l + 1;
									if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
										p1.drawX(gl);
								}
								p2 = j.points.get(ll);
								p2.drawX(gl);
								gl.glEnd();
								gl.glPointSize(3);
								gl.glBegin(GL.GL_POINTS);
								j.firstElement().drawX(gl);
								j.lastElement().drawX(gl);
								gl.glEnd();
							}
						}
					}
				}
			float[] cc = OpenGLUt.glColor(gl, activTrack.getColor());
			float[] ccSel = cc.clone();
			ccSel[3] = 0.3f;
			if (activTrack.isVisible() && !activTrack.trajs.isEmpty())
			{
				for (int k = 0, last2 = activTrack.trajs.size(); k < last2; k++)
				{
					HoloTraj j = activTrack.trajs.get(k);
					if (!j.isEmpty())
					{
						// Segment entre les points
						HoloPoint p1, p2;
						if (render || query_one_select)
							for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
							{
								p1 = j.points.get(l);
								p2 = j.points.get(l + 1);
								if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
								{
									int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, k, l);
									if (index == selected)
										gl.glLineWidth(2);
									else gl.glLineWidth(1);
									gl.glLoadName(index);
									gl.glBegin(GL.GL_LINES);
									p1.drawX(gl);
									p2.drawX(gl);
									gl.glEnd();
								}
							}
						// Points
						for (int l = 0, last3 = j.points.size(); l < last3; l++)
						{
							HoloPoint p = j.points.get(l);
							if ((p.date <= end && p.date >= beg) && p.isEditable() || !holoEditRef.viewOnlyEditablePoints)
							{
								int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, k, l);
								if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
								{
									gl.glPointSize(p.isEditable() ? 14 : 10);
									OpenGLUt.glColor(gl, ccSel);
									gl.glBegin(GL.GL_POINTS);
									p.drawX(gl);
									gl.glEnd();
									OpenGLUt.glColor(gl, cc);
								}
								gl.glPointSize(p.isEditable() ? 6 : 4);
								gl.glLoadName(index);
								gl.glBegin(GL.GL_POINTS);
								p.drawX(gl);
								gl.glEnd();
							}
						}
					}
				}
			}
		}

		private void drawY(boolean render, int curveNum)
		{
			gl.glLineWidth(1);
			if (render)
				for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					if (currentTrack != activTrack && currentTrack.isVisible() && !currentTrack.trajs.isEmpty())
					{
						float[] cBg = OpenGLUt.convCol(currentTrack.getColor());
						cBg[3] = 0.3f;
						gl.glColor4fv(cBg, 0);
						for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
						{
							HoloTraj j = currentTrack.trajs.get(k);
							if (!j.isEmpty())
							{
								HoloPoint p1, p2;
								int ll = 0;
								gl.glBegin(GL.GL_LINE_STRIP);
								for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
								{
									p1 = j.points.get(l);
									p2 = j.points.get(l + 1);
									ll = l + 1;
									if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
										p1.drawY(gl);
								}
								p2 = j.points.get(ll);
								p2.drawY(gl);
								gl.glEnd();
								gl.glPointSize(3);
								gl.glBegin(GL.GL_POINTS);
								j.firstElement().drawY(gl);
								j.lastElement().drawY(gl);
								gl.glEnd();
							}
						}
					}
				}
			float[] cc = OpenGLUt.glColor(gl, activTrack.getColor());
			float[] ccSel = cc.clone();
			ccSel[3] = 0.3f;
			if (activTrack.isVisible() && !activTrack.trajs.isEmpty())
			{
				for (int k = 0, last2 = activTrack.trajs.size(); k < last2; k++)
				{
					HoloTraj j = activTrack.trajs.get(k);
					if (!j.isEmpty())
					{
						// Segment entre les points
						HoloPoint p1, p2;
						if (render || query_one_select)
							for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
							{
								p1 = j.points.get(l);
								p2 = j.points.get(l + 1);
								if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
								{
									int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, k, l);
									if (index == selected)
										gl.glLineWidth(2);
									else gl.glLineWidth(1);
									gl.glLoadName(index);
									gl.glBegin(GL.GL_LINES);
									p1.drawY(gl);
									p2.drawY(gl);
									gl.glEnd();
								}
							}
						// Points
						for (int l = 0, last3 = j.points.size(); l < last3; l++)
						{
							HoloPoint p = j.points.get(l);
							if ((p.date <= end && p.date >= beg) && p.isEditable() || !holoEditRef.viewOnlyEditablePoints)
							{
								int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, k, l);
								if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
								{
									gl.glPointSize(p.isEditable() ? 14 : 10);
									OpenGLUt.glColor(gl, ccSel);
									gl.glBegin(GL.GL_POINTS);
									p.drawY(gl);
									gl.glEnd();
									OpenGLUt.glColor(gl, cc);
								}
								gl.glPointSize(p.isEditable() ? 6 : 4);
								gl.glLoadName(index);
								gl.glBegin(GL.GL_POINTS);
								p.drawY(gl);
								gl.glEnd();
							}
						}
					}
				}
			}
		}

		private void drawZ(boolean render, int curveNum)
		{
			gl.glLineWidth(1);
			if (render)
				for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					if (currentTrack != activTrack && currentTrack.isVisible() && !currentTrack.trajs.isEmpty())
					{
						float[] cBg = OpenGLUt.convCol(currentTrack.getColor());
						cBg[3] = 0.3f;
						gl.glColor4fv(cBg, 0);
						for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
						{
							HoloTraj j = currentTrack.trajs.get(k);
							if (!j.isEmpty())
							{
								HoloPoint p1, p2;
								int ll = 0;
								gl.glBegin(GL.GL_LINE_STRIP);
								for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
								{
									p1 = j.points.get(l);
									p2 = j.points.get(l + 1);
									ll = l + 1;
									if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
										p1.drawZ(gl);
								}
								p2 = j.points.get(ll);
								p2.drawZ(gl);
								gl.glEnd();
								gl.glPointSize(3);
								gl.glBegin(GL.GL_POINTS);
								j.firstElement().drawZ(gl);
								j.lastElement().drawZ(gl);
								gl.glEnd();
							}
						}
					}
				}
			float[] cc = OpenGLUt.glColor(gl, activTrack.getColor());
			float[] ccSel = cc.clone();
			ccSel[3] = 0.3f;
			if (activTrack.isVisible() && !activTrack.trajs.isEmpty())
			{
				for (int k = 0, last2 = activTrack.trajs.size(); k < last2; k++)
				{
					HoloTraj j = activTrack.trajs.get(k);
					if (!j.isEmpty())
					{
						// Segment entre les points
						HoloPoint p1, p2;
						if (render || query_one_select)
							for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
							{
								p1 = j.points.get(l);
								p2 = j.points.get(l + 1);
								if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
								{
									int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, k, l);
									if (index == selected)
										gl.glLineWidth(2);
									else gl.glLineWidth(1);
									gl.glLoadName(index);
									gl.glBegin(GL.GL_LINES);
									p1.drawZ(gl);
									p2.drawZ(gl);
									gl.glEnd();
								}
							}
						// Points
						for (int l = 0, last3 = j.points.size(); l < last3; l++)
						{
							HoloPoint p = j.points.get(l);
							if ((p.date <= end && p.date >= beg) && p.isEditable() || !holoEditRef.viewOnlyEditablePoints)
							{
								int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, k, l);
								if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
								{
									gl.glPointSize(p.isEditable() ? 14 : 10);
									OpenGLUt.glColor(gl, ccSel);
									gl.glBegin(GL.GL_POINTS);
									p.drawZ(gl);
									gl.glEnd();
									OpenGLUt.glColor(gl, cc);
								}
								gl.glPointSize(p.isEditable() ? 6 : 4);
								gl.glLoadName(index);
								gl.glBegin(GL.GL_POINTS);
								p.drawZ(gl);
								gl.glEnd();
							}
						}
					}
				}
			}
		}

		private void drawR(boolean render, int curveNum)
		{
			gl.glLineWidth(1);
			if (render)
				for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					if (currentTrack != activTrack && currentTrack.isVisible() && !currentTrack.trajs.isEmpty())
					{
						float[] cBg = OpenGLUt.convCol(currentTrack.getColor());
						cBg[3] = 0.3f;
						gl.glColor4fv(cBg, 0);
						for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
						{
							HoloTraj j = currentTrack.trajs.get(k);
							if (!j.isEmpty())
							{
								HoloPoint p1, p2;
								int ll = 0;
								gl.glBegin(GL.GL_LINE_STRIP);
								for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
								{
									p1 = j.points.get(l);
									p2 = j.points.get(l + 1);
									ll = l + 1;
									if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
										p1.drawR(gl);
								}
								p2 = j.points.get(ll);
								p2.drawR(gl);
								gl.glEnd();
								gl.glPointSize(3);
								gl.glBegin(GL.GL_POINTS);
								j.firstElement().drawR(gl);
								j.lastElement().drawR(gl);
								gl.glEnd();
							}
						}
					}
				}
			float[] cc = OpenGLUt.glColor(gl, activTrack.getColor());
			float[] ccSel = cc.clone();
			ccSel[3] = 0.3f;
			if (activTrack.isVisible() && !activTrack.trajs.isEmpty())
			{
				for (int k = 0, last2 = activTrack.trajs.size(); k < last2; k++)
				{
					HoloTraj j = activTrack.trajs.get(k);
					if (!j.isEmpty())
					{
						// Segment entre les points
						HoloPoint p1, p2;
						if (render || query_one_select)
							for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
							{
								p1 = j.points.get(l);
								p2 = j.points.get(l + 1);
								if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
								{
									int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, k, l);
									if (index == selected)
										gl.glLineWidth(2);
									else gl.glLineWidth(1);
									gl.glLoadName(index);
									gl.glBegin(GL.GL_LINES);
									p1.drawR(gl);
									p2.drawR(gl);
									gl.glEnd();
								}
							}
						// Points
						for (int l = 0, last3 = j.points.size(); l < last3; l++)
						{
							HoloPoint p = j.points.get(l);
							if ((p.date <= end && p.date >= beg) && p.isEditable() || !holoEditRef.viewOnlyEditablePoints)
							{
								int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, k, l);
								if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
								{
									gl.glPointSize(p.isEditable() ? 14 : 10);
									OpenGLUt.glColor(gl, ccSel);
									gl.glBegin(GL.GL_POINTS);
									p.drawR(gl);
									gl.glEnd();
									OpenGLUt.glColor(gl, cc);
								}
								gl.glPointSize(p.isEditable() ? 5 : 3);
								gl.glLoadName(index);
								gl.glBegin(GL.GL_POINTS);
								p.drawR(gl);
								gl.glEnd();
							}
						}
					}
				}
			}
		}

		private void drawT(boolean render, int curveNum)
		{
			gl.glLineWidth(1);
			if (render)
				for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					if (currentTrack != activTrack && currentTrack.isVisible() && !currentTrack.trajs.isEmpty())
					{
						float[] cBg = OpenGLUt.convCol(currentTrack.getColor());
						cBg[3] = 0.3f;
						gl.glColor4fv(cBg, 0);
						for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
						{
							HoloTraj j = currentTrack.trajs.get(k);
							if (!j.isEmpty())
							{
								HoloPoint p1, p2;
								int ll = 0;
								gl.glBegin(GL.GL_LINE_STRIP);
								for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
								{
									p1 = j.points.get(l);
									p2 = j.points.get(l + 1);
									ll = l + 1;
									if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
										p1.drawT(gl);
								}
								p2 = j.points.get(ll);
								p2.drawT(gl);
								gl.glEnd();
								gl.glPointSize(3);
								gl.glBegin(GL.GL_POINTS);
								j.firstElement().drawT(gl);
								j.lastElement().drawT(gl);
								gl.glEnd();
							}
						}
					}
				}
			float[] cc = OpenGLUt.glColor(gl, activTrack.getColor());
			float[] ccSel = cc.clone();
			ccSel[3] = 0.3f;
			if (activTrack.isVisible() && !activTrack.trajs.isEmpty())
			{
				for (int k = 0, last2 = activTrack.trajs.size(); k < last2; k++)
				{
					HoloTraj j = activTrack.trajs.get(k);
					if (!j.isEmpty())
					{
						// Segment entre les points
						HoloPoint p1, p2;
						if (render || query_one_select)
							for (int l = 0, last3 = j.points.size() - 1; l < last3; l++)
							{
								p1 = j.points.get(l);
								p2 = j.points.get(l + 1);
								if ((p1.date >= beg && p1.date <= end) || (p2.date >= beg && p2.date <= end))
								{
									int index = TimeIndex.encode(TimeIndex.TYPE_LINE, curveNum, k, l);
									if (index == selected)
										gl.glLineWidth(2);
									else gl.glLineWidth(1);
									gl.glLoadName(index);
									gl.glBegin(GL.GL_LINES);
									p1.drawT(gl);
									p2.drawT(gl);
									gl.glEnd();
								}
							}
						// Points
						for (int l = 0, last3 = j.points.size(); l < last3; l++)
						{
							HoloPoint p = j.points.get(l);
							if ((p.date <= end && p.date >= beg) && p.isEditable() || !holoEditRef.viewOnlyEditablePoints)
							{
								int index = TimeIndex.encode(TimeIndex.TYPE_PT, curveNum, k, l);
								if (index == selected || selPoints.contains(p) || (selIndex.contains(index) && (!selMode || p.isEditable())))
								{
									gl.glPointSize(p.isEditable() ? 14 : 10);
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
					}
				}
			}
		}

		private void drawCurveHeader(int curveNum, boolean render, float h)
		{
			float sizeW = 12;
			float sizeH = (12 / h) * (maxY - minY);
			float padW = 5;
			float padH = (5 / h) * (maxY - minY);

			glu.gluOrtho2D(0, W_CURVE_HEADER, minY, maxY);
			OpenGLUt.glColor(gl, borderColor);
			gl.glLoadName(TimeIndex.encode(TimeIndex.TYPE_OT, curveNum, TimeIndex.HEADER, TimeIndex.HEADER_POLY));
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			OpenGLUt.glColor(gl, borderColor);
			gl.glRectf(0, minY, W_CURVE_HEADER, maxY);
			if (render)
			{
				OpenGLUt.glColor(gl, Color.BLACK);
				gl.glBegin(GL.GL_LINE_STRIP);
				gl.glVertex2f(0, minY);
				gl.glVertex2f(W_CURVE_HEADER, minY);
				gl.glVertex2f(W_CURVE_HEADER, maxY);
				gl.glEnd();
				OpenGLUt.glColor(gl, mouseColor);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
				OpenGLUt.drawTexture(gl, textMinus, padW, maxY - sizeH - padH, sizeW, sizeH);
				OpenGLUt.drawTexture(gl, textPlus, padW, minY + padH, sizeW, sizeH);
				OpenGLUt.glColor(gl, Color.BLACK);
				// CURVE SCALE
				if (scaleID == -1)
					createScaleList();
				gl.glCallList(scaleID);
				if (scale != SCALE_SOUND && scale != SCALE_DATA)
				{
					gl.glRasterPos2f(W_CURVE_HEADER/2, minY + 0.02f * (maxY - minY));
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "" + minY);
					gl.glRasterPos2f(W_CURVE_HEADER/2, maxY - 0.1f * (maxY - minY));
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "" + maxY);
				}
				gl.glRasterPos2f(20, minY + 0.5f * (maxY - minY));
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, curveNames[param]);
			} else {
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
				// MINUS
				gl.glLoadName(TimeIndex.encode(TimeIndex.TYPE_OT, curveNum, TimeIndex.HEADER, TimeIndex.HEADER_MINUS));
				gl.glRectf(padW, maxY - sizeH - padH, padW + sizeW, maxY - padH);
				// PLUS
				gl.glLoadName(TimeIndex.encode(TimeIndex.TYPE_OT, curveNum, TimeIndex.HEADER, TimeIndex.HEADER_PLUS));
				gl.glRectf(padW, minY + padH, padW + sizeW, minY + padH + sizeH);
				// SCALE
				gl.glLoadName(TimeIndex.encode(TimeIndex.TYPE_OT, curveNum, TimeIndex.HEADER, TimeIndex.HEADER_SCALE));
				gl.glRectf(W_CURVE_HEADER-15, minY, W_CURVE_HEADER, maxY);
			}
			gl.glLoadIdentity();
		}

		private void createScaleList()
		{
			scaleID = gl.glGenLists(1);
			switch (scale)
			{
			case SCALE_SOUND:
				gl.glNewList(scaleID, GL.GL_COMPILE);
				gl.glBegin(GL.GL_LINES);
				for (float i = minY; i <= maxY; i += 0.1f)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-7.5f, i);
				}
				for (float i = minY; i <= maxY; i += 0.5f)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-15, i);
				}
				gl.glEnd();
				gl.glEndList();
				break;
			case SCALE_DATA:
				gl.glNewList(scaleID, GL.GL_COMPILE);
				gl.glBegin(GL.GL_LINES);
				for (float i = minY; i <= maxY; i += 0.1f)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-7.5f, i);
				}
				for (float i = minY; i <= maxY; i += 0.5f)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-15, i);
				}
				gl.glEnd();
				gl.glEndList();
				break;
			case SCALE_THETA1:
			case SCALE_THETA2:
				gl.glNewList(scaleID, GL.GL_COMPILE);
				gl.glBegin(GL.GL_LINES);
				for (int i = (int) (minY - Ut.modSigned(minY, 15)), last = (int) (maxY - Ut.modSigned(maxY, 15)); i <= last; i += 15)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-7.5f, i);
				}
				for (int i = (int) (minY - Ut.modSigned(minY, 30)), last = (int) (maxY - Ut.modSigned(maxY, 30)); i <= last; i += 30)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-12.125f, i);
				}
				for (int i = (int) (minY - Ut.modSigned(minY, 90)), last = (int) (maxY - Ut.modSigned(maxY, 90)); i <= last; i += 90)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-15, i);
				}
				gl.glEnd();
				gl.glEndList();
				break;
			case SCALE_ZRP:
			case SCALE_XY:
			default:
				gl.glNewList(scaleID, GL.GL_COMPILE);
				gl.glBegin(GL.GL_LINES);
				if (maxY - minY <= 10)
					for (int i = (int) minY, last = (int) maxY; i <= last; i += 1)
					{
						gl.glVertex2f(W_CURVE_HEADER, i);
						gl.glVertex2f(W_CURVE_HEADER-1.875f, i);
					}
				if (maxY - minY <= 100)
					for (int i = (int) (minY - Ut.modSigned(minY, 5)), last = (int) (maxY - Ut.modSigned(maxY, 5)); i <= last; i += 5)
					{
						gl.glVertex2f(W_CURVE_HEADER, i);
						gl.glVertex2f(W_CURVE_HEADER-3.75f, i);
					}
				for (int i = (int) (minY - Ut.modSigned(minY, 10)), last = (int) (maxY - Ut.modSigned(maxY, 10)); i <= last; i += 10)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-7.5f, i);
				}
				for (int i = (int) (minY - Ut.modSigned(minY, 50)), last = (int) (maxY - Ut.modSigned(maxY, 50)); i <= last; i += 50)
				{
					gl.glVertex2f(W_CURVE_HEADER, i);
					gl.glVertex2f(W_CURVE_HEADER-15, i);
				}
				gl.glEnd();
				gl.glEndList();
				break;
			}
		}

		public float scale(float posH, float HH)
		{
			return posH / HH * (maxY - minY) + minY;
		}

		public void limitScale()
		{
			switch (param)
			{
			case CURVE_X:
			case CURVE_Y:
			case CURVE_Z:
				break;
			case CURVE_R:
				minY = Ut.clipL(minY, 0);
				break;
			case CURVE_T:
				switch (scale)
				{
				case SCALE_THETA1:
					minY = Ut.clipL(minY, 0);
					maxY = Ut.clipU(maxY, 360);
					break;
				case SCALE_THETA2:
					minY = Ut.clipL(minY, -180);
					maxY = Ut.clipU(maxY, 180);
					break;
				}
				break;
			case CURVE_W:
				minY = -0.5f;
				maxY = 0.5f;
				break;
			default:
				minY = Ut.clipL(minY, 0);
				maxY = Ut.clipU(maxY, 100);
				break;
			}
		}
	}
	public class CurvePoint extends Point2D.Float
	{
		CurvePoint(float x, float y)
		{
			super(x, y);
		}

		public float getDate()
		{
			return (float) getX();
		}

		public float getVal()
		{
			return (float) getY();
		}

		public void shift(float date)
		{
			x += date;
		}
	}

	private CurvePoint convPosPt(float posW, float posH)
	{
		return new CurvePoint(convPosW(posW), convPosH(posH));
	}

	private float convPosW(float posW)
	{
		return Ut.clipL(posW / W * (end - beg) + beg, 0);
	}

	private float convPosH(float posH)
	{
		if (curveSelected != -1 && curveSelected < curves.size())
		{
			float HH = curves.get(curveSelected).hpercent * curvesGlobalHeight - 5;
			for (int i = curves.size() - 1; i > curveSelected; i--)
				posH -= curves.get(i).hpercent * curvesGlobalHeight;
			return curves.get(curveSelected).scale(posH, HH);
		}
		return 0;
	}

	private float convPosTime(float xx)
	{
		return Ut.clipL((xx - (W_TIME_SCALE_BF + W_TIME_SCALE_MARGIN)) / (width - 2 * (W_TIME_SCALE_BF + W_TIME_SCALE_MARGIN)) * totalTime, 0);
	}

	private float convPosTime2(float xx)
	{
		return xx / W * (maxTime - minTime) + minTime;
	}

	private float convPosLocalTime(float xx)
	{
		return xx / W * (end - beg) + beg;
	}

	public void display()
	{
		if (visible)
			glp.display();
	}

	public String toString()
	{
		return "\t<time mode=\"" + timeMode + "\" curves=\"" + getCurves() + "\"" + super.toString();
	}

	public void setTimeMode(int i)
	{
		timeMode = Ut.clip(i, 0, 2);
		timeModeCombo.setSelectedIndex(timeMode);
	}

	public String getCurves()
	{
		String tmp = "";
		for (Curve c : curves)
			tmp = tmp + c.param + " ";
		return tmp.substring(0, tmp.length() - 1);
	}

	public void setCurves(String c)
	{
		String[] tmp = c.split(" ");
		if (tmp.length < 1)
			return;
		curves = new Vector<Curve>(tmp.length, 1);
		try
		{
			for (String s : tmp)
			{
				int type = Integer.parseInt(s);
				curves.add(new Curve(curveParams[type]));
			}
		}
		catch (NumberFormatException e)
		{
		}
		initSizes();
	}

	public void initVars()
	{
		query_one_select = false;
		query_sel_select = false;
		selected = TimeIndex.getNull();
		curveSelected = -1;
		selIndex = new Vector<Integer>();
		selPoints = new Vector<HoloPoint>();
		updateSelRefs();
		scaleSelected = TimeIndex.getNull();
		scrollHSelected = TimeIndex.getNull();
		draggedSelZone = false;
		draggedLocalScale = false;
		draggedScale = false;
		draggedScroll = false;
		draggedSel = false;
		draggedSelBegin = false;
		draggedSelEnd = false;
		draggedPointTimeLin = false;
		draggedPointTimeAcc = false;
		draggedPointTimeTan = false;
		draggedPointTimeBeginLin = false;
		draggedPointTimeBeginTan = false;
		draggedPointTimeEndLin = false;
		draggedPointTimeEndTan = false;
		draggedMultiPointTimeLin = false;
		draggedMultiPointTimeAcc = false;
		draggedMultiPointTimeTan = false;
		draggedMultiPointTimeBeginLin = false;
		draggedMultiPointTimeBeginTan = false;
		draggedMultiPointTimeEndLin = false;
		draggedMultiPointTimeEndTan = false;
		draggedPointTimeAlone = false;
		draggedPointOrd = false;
		draggedMultiPointOrd = false;
		draggedPointOrd = false;
		holoEditRef.room.display();
	}

	public boolean hasFocus()
	{
		return super.hasFocus() || glp.hasFocus();
	}

	public void setTitle(String title)
	{
		super.setTitle("Time Editor - " + title);
	}

	public String getTitle()
	{
		return super.getTitle();
	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		float pad = e.isShiftDown() ? 0.01f : 0.1f;
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_DOWN:
			if (keyDown != -1)
				zoom += pad;
			break;
		case KeyEvent.VK_UP:
			if (keyDown != -1)
				zoom -= pad;
			break;
		case KeyEvent.VK_LEFT:
			if (keyDown != -1)
				dT = -1 * (end - beg) / (e.isShiftDown() ? 20 : 2);
			break;
		case KeyEvent.VK_RIGHT:
			if (keyDown != -1)
				dT = (end - beg) / (e.isShiftDown() ? 20 : 2);
			break;
		case KeyEvent.VK_Z:
		case KeyEvent.VK_D:
			keyDown = e.getKeyCode();
		default:
			break;
		}
		zoomTime = (beg + end) / 2;
		zoom = Ut.clip(zoom, 0.025f, 1);
		display();
	}

	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == keyDown)
			keyDown = -1;
	}
	public void focusGained(FocusEvent e)
	{
		updateMenuBar();
		if (holoEditRef.helpWindowOpened)
			holoEditRef.helpWindow.jumpToIndex("#timeEditor", true);
	}
}
