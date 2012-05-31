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

import holoedit.HoloEdit;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.HoloWaveForm;
import holoedit.data.HoloSDIFdata;
import holoedit.data.HoloExternalData;
import holoedit.data.WaveFormInstance;
import holoedit.data.SDIFdataInstance;
import holoedit.fileio.TjFileReader;
import holoedit.fileio.WaveFormReader;
import holoedit.opengl.OpenGLUt;
import holoedit.opengl.ScoreIndex;
import holoedit.util.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

// FEATURE SCORE CUELIST EXPORT/VISU
// FEATURE SCORE MARKERS & AIMANTATION SUR MARKERS
public class ScoreGUI extends FloatingWindow implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, DropTargetListener, KeyListener
{
	private PopupMenu trackPopup;
	private PopupMenu multiSeqPopup;
	private PopupMenu seqPopup;
	private TrackPopup headerPopup;
	public boolean popupVisible = false;
	private MenuItem setCursor1;
	private MenuItem setCursor2;
	private MenuItem setCursor3;
	private MenuItem viewAll1;
	private MenuItem zoomAll1;
	private MenuItem viewAll2;
	private MenuItem zoomAll2;
	private MenuItem viewAll3;
	private MenuItem zoomAll3;
	private MenuItem viewTrack;
	private MenuItem viewMultiSeq;
	private MenuItem viewSeq;
	private MenuItem zoomTrack;
	private MenuItem zoomMultiSeq;
	private MenuItem zoomSeq;
	private MenuItem lockTrack;
	private MenuItem joinTrack;
	private MenuItem joinMultiSeq;
	private MenuItem trimTrack;
	private MenuItem trimMultiSeq;
	private MenuItem trimSeq;
	private MenuItem cutAtTrack;
	private MenuItem cutAtMultiSeq;
	private MenuItem cutAtSeq;
	private MenuItem moveTrack;
	private MenuItem moveMultiSeq;
	private MenuItem moveSeq;
	private MenuItem copyTrack;
	private MenuItem copyMultiSeq;
	private MenuItem copySeq;
	private MenuItem cuttTrack;
	private MenuItem cuttMultiSeq;
	private MenuItem cuttSeq;
	private MenuItem paste1;
	private MenuItem paste2;
	private MenuItem paste3;
	private MenuItem eraseTrack;
	private MenuItem eraseMultiSeq;
	private MenuItem eraseSeq;
	private MenuItem importSeq1;
	private MenuItem importSeq2;
	private MenuItem importSeq3;
	private MenuItem exportSeq;
	private MenuItem exportSeqICST;
	private MenuItem importTrack1;
	private MenuItem importTrack2;
	private MenuItem importTrack3;
	private MenuItem importWave1;
	private MenuItem importWave2;
	private MenuItem importWave3;
	private MenuItem exportTrack;
	private final static float ZOOM_OUT_MAX_PAD = 10000;
	private final static float ZOOM_IN_MIN_PAD = 100;
	private final static int MOUSE_SELECT_SIZE = 5;
	private final int H_TIME_SCALE = 20;
	private final int H_TIME_SCROLL = 10;
	private final int W_TRACK_SCROLL = 10;
	private final int W_TRACK_HEADER = 100;
	private final int H_BOTTOM_BORDER1 = 10; // au dessus du timescroll
	private final int H_BOTTOM_BORDER2 = 5; // au dessous du timescroll
	private final int H_BOTTOM_BORDER12 = H_BOTTOM_BORDER1 + H_BOTTOM_BORDER2; // au dessous du timescroll
	private final int W_RIGHT_BORDER1 = 10; // a gauche du trackscroll
	private final int W_RIGHT_BORDER2 = 10; // a droite du trackscroll
	private final int W_RIGHT_BORDER12 = W_RIGHT_BORDER1 + W_RIGHT_BORDER2;
	private float[] borderColor = { 0.95f, 0.95f, 0.95f, 1 };
	private float[] lockedTrackColor = { 0.6f, 0.6f, 0.6f, 1 };
	private float[] activTrackColor = { 0.8f, 0.8f, 0.8f, 0.8f };
	private float[] timeSelColor = { 0.8f, 0.8f, 0.8f, 1 };
	private float[] timeSelSelColor = { 0.65f, 0.65f, 0.65f, 1 };
	private float[] scrollBgColor = { 0.5f, 0.5f, 0.5f, 1 };
	private float[] scrollSelBgColor = { 0.4f, 0.4f, 0.4f, 1 };
	private float[] scrollFgColor = { 0.8f, 0.8f, 0.8f, 1 };
	private float[] scrollSelFgColor = { 0.9f, 0.9f, 0.9f, 1 };
	private float[] selZoneColor = { 0.5f, 0.5f, 0.5f, 0.1f };
	private float[] selZoneBorderColor = { 0, 0, 0.5f, 0.3f };
	private static int textCross;
	private static int textCross2;
	private static int textPlus;
	private static int textMinus;
	private static int textPlusBright;
	private static int textMinusBright;
	private Vector<Float> size = new Vector<Float>(Ut.INIT_TRACK_NB, 1);
	private int width;
	private int height;
	private GLCanvas glp;
	private GL gl;
	private GLU glu;
	private GLUT glut;
	private final float minY = 0;
	private final float maxY = 1.5f;
	private int tracksGlobalHeight;
	private int tracksGlobalWidth;
	private HoloEdit holoEditRef;
	private boolean query_one_select = false;
	private boolean query_track_select = false;
	private boolean query_sel_select = false;
	public int selected = ScoreIndex.getNull();
	public Vector<Integer> selIndex = new Vector<Integer>();
	public Vector<HoloTraj> selSeqs = new Vector<HoloTraj>();
	public Vector<WaveFormInstance> selWaves = new Vector<WaveFormInstance>();
	public Vector<SDIFdataInstance> selSDIFs = new Vector<SDIFdataInstance>();
	public Vector<HoloTrack> selTracks = new Vector<HoloTrack>();
	private int wavSelected = -1;
	private int sdifSelected = -1;
	private int seqSelected = -1;
	public int trackSelected = -1;
	private int oldTrackSelected = -1;
	public int oldActiveTrack = -1;
	private int scaleSelected = ScoreIndex.getNull();
	private int scrollHSelected = ScoreIndex.getNull();
	private int scrollVSelected = ScoreIndex.getNull();
	private int headerSelected = ScoreIndex.getNull();
	private int timeZoomButtons = ScoreIndex.getNull();
	private int trackZoomButtons = ScoreIndex.getNull();
	private double mousex;
	private double mousey;
	private float mousex1;
	private float mousex2;
	public int minTime = 0;
	public int maxTime = 50000;
	public int cursorTime = 0;
	private ScorePoint mouseCursor = new ScorePoint(minTime, minY);
	private ScorePoint selPt1 = new ScorePoint(minTime, minY), selPt2 = new ScorePoint(minTime, minY);
	private boolean draggedSeq = false;
	private boolean draggedSeqBegin = false;
	private boolean draggedSeqEnd = false;
	private boolean draggedMultiSeq = false;
	private boolean draggedMultiSeqBegin = false;
	private boolean draggedMultiSeqEnd = false;
	private boolean draggedScale = false;
	private boolean draggedTimeScroll = false;
	private boolean draggedTrackScroll = false;
	private boolean draggedSel = false;
	private boolean draggedSelBegin = false;
	private boolean draggedSelEnd = false;
	private boolean draggedSelZone = false;
	private static final int NO_SEL = -1;
	private static final int SEL_BOTH = 0;
	private static final int SEL_TRAJ = 1;
	private static final int SEL_WAVE = 2;
	private int draggedSelMode = NO_SEL;
	private boolean draggedWave = false;
	private boolean draggedData = false;
	private boolean dragCtrl = false;
	private boolean cutSeq = false;
	private float oldCurrentX = 0;
	private float dX = 0;
	private int totalTime = 0;
	// 2 int pour le track scroll vertical
	private int maxTrackH = tracksGlobalHeight;
	private int minTrackH = 0;
	private Formatter rf = new Formatter(2,2,0,0);
	private ActionListener aa;
	public boolean acceptDrag = false;
	public HoloWaveForm droppedWaveForm = null;
	public HoloExternalData droppedExternalData = null;
	public HoloExternalData droppedData = null;
	private WaveFormInstance currentDraggedWave = null;
	private SDIFdataInstance currentDraggedSDIF = null;
	private boolean select;
	private Thread status;
	private HoloTrack currentTrack, currentDrawnTrack, otherTrack;
	private HoloTraj currentSeq;
	private WaveFormInstance currentWave;
	private SDIFdataInstance currentSDIF;
	private int keyDown = -1;
	private IntBuffer selectBuf;
		
	public ScoreGUI(HoloEdit owner)
	{
		super("Score", owner, owner.wsScoreW, owner.wsScoreH, owner.wlScoreX, owner.wlScoreY, owner.wbScore);
		setTitle("Untitled");
		holoEditRef = owner;
		setResizable(true);
		initSizes();
		glp = new GLCanvas(holoEditRef.glcap, null, holoEditRef.glpb.getContext(),null);
		glp.addGLEventListener(this);
		glp.addMouseListener(this);
		glp.addMouseMotionListener(this);
		glp.addMouseWheelListener(this);
		glp.addFocusListener(this);
		glp.addKeyListener(this);
		addKeyListener(this);
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(glp, BorderLayout.CENTER);
		add(jp, BorderLayout.CENTER);
		createPopups();
		newStatus();
		trackSelected = 0;
		if(SoundPoolGUI.supportsDnD())
			makeDropTarget(glp);
		else
			System.out.println("Drag and drop is not supported with this JVM");
	}

	private void createPopups()
	{
		aa = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object o = e.getSource();
				if (o == viewAll1 || o == viewAll2 || o == viewAll3)
					viewAll();
				else if (o == zoomAll1 || o == zoomAll2 || o == zoomAll3)
					zoomAll();
				else if (o == viewTrack)
					viewTrack();
				else if (o == zoomTrack)
					zoomTrack();
				else if (o == viewMultiSeq)
					viewMultiSeq();
				else if (o == zoomMultiSeq)
					zoomMultiSeq();
				else if (o == viewSeq)
					viewSeq();
				else if (o == zoomSeq)
					zoomSeq();
				else if (o == lockTrack)
					changeLock();
				else if (o == joinTrack)
					joinTrack();
				else if (o == joinMultiSeq)
					joinMultiSeq();
				else if (o == trimTrack)
					trimTrack();
				else if (o == trimMultiSeq)
					trimMultiSeq();
				else if (o == trimSeq)
					trimSeq();
				else if (o == eraseTrack)
					eraseTrack();
				else if (o == eraseMultiSeq)
					eraseMultiSeq();
				else if (o == eraseSeq)
					eraseSeq();
				else if (o == copyTrack)
					copyTrack();
				else if (o == copyMultiSeq)
					copyMultiSeq();
				else if (o == copySeq)
					copySeq();
				else if (o == paste1 || o == paste2 || o == paste3)
					paste((int)oldCurrentX);
				else if (o == cuttTrack)
				{
					copyTrack();
					eraseTrack();
				}
				else if (o == cuttMultiSeq)
				{
					copyMultiSeq();
					eraseMultiSeq();
				}
				else if (o == cuttSeq)
				{
					copySeq();
					eraseSeq();
				}
				else if (o == moveTrack)
					moveTrack();
				else if (o == moveMultiSeq)
					moveMultiSeq();
				else if (o == moveSeq)
					moveSeq();
				else if (o == cutAtTrack)
					cutAtTrack();
				else if (o == cutAtMultiSeq)
					cutAtMultiSeq();
				else if (o == cutAtSeq)
					cutAtSeq();
				else if (o == exportTrack)
					holoEditRef.gestionPistes.exportTrack(trackSelected);
				else if (o == exportSeq)
					exportSeq();
				else if (o == exportSeqICST)
					exportSeqICST();
				else if (o == importTrack1 || o == importTrack2 ||o == importTrack3)
					holoEditRef.gestionPistes.importTrack(trackSelected,(int)oldCurrentX);
				else if (o == importSeq1 || o == importSeq2 || o == importSeq3)
					holoEditRef.gestionPistes.importSeq(trackSelected,(int)oldCurrentX);
				else if (o == importWave1 || o == importWave2 || o == importWave3)
					importWave();
				else if (o == setCursor1 || o == setCursor2 || o == setCursor3)
					setCursor();
				popupVisible = false;
				holoEditRef.room.display();
			}
		};
		trackPopup = new PopupMenu("Track");
		multiSeqPopup = new PopupMenu("Selection");
		seqPopup = new PopupMenu("Trajectory");
		setCursor1 = new MenuItem("Set Cursor at time...");
		setCursor2 = new MenuItem("Set Cursor at time...");
		setCursor3 = new MenuItem("Set Cursor at time...");
		viewAll1 = new MenuItem("View All");
		viewAll2 = new MenuItem("View All");
		viewAll3 = new MenuItem("View All");
		viewTrack = new MenuItem("View this track");
		viewMultiSeq = new MenuItem("View this selection");
		viewSeq = new MenuItem("View this trajectory");
		zoomAll1 = new MenuItem("Zoom on all");
		zoomAll2 = new MenuItem("Zoom on all");
		zoomAll3 = new MenuItem("Zoom on all");
		zoomTrack = new MenuItem("Zoom on this track");
		zoomMultiSeq = new MenuItem("Zoom on this selection");
		zoomSeq = new MenuItem("Zoom on this trajectory");
		lockTrack = new MenuItem("Lock this track");
		joinTrack = new MenuItem("Join all trajectories in this track");
		joinMultiSeq = new MenuItem("Join selected trajectories");
		trimTrack = new MenuItem("Trim this track to current time selection");
		trimMultiSeq = new MenuItem("Trim this selection to current time selection");
		trimSeq = new MenuItem("Trim this trajectory to current time selection");
		moveTrack = new MenuItem("Move this track to time...");
		moveMultiSeq = new MenuItem("Move this selection to time...");
		moveSeq = new MenuItem("Move this trajectory to time...");
		cutAtTrack = new MenuItem("Cut this track at time...");
		cutAtMultiSeq = new MenuItem("Cut this selection at time...");
		cutAtSeq = new MenuItem("Cut this trajectory at time...");
		copyTrack = new MenuItem("Copy this track");
		copyMultiSeq = new MenuItem("Copy this selection");
		copySeq = new MenuItem("Copy this trajectory");
		cuttTrack = new MenuItem("Cut this track");
		cuttMultiSeq = new MenuItem("Cut this selection");
		cuttSeq = new MenuItem("Cut this trajectory");
		paste1 = new MenuItem("Paste");
		paste2 = new MenuItem("Paste");
		paste3 = new MenuItem("Paste");
		eraseTrack = new MenuItem("Erase this track");
		eraseMultiSeq = new MenuItem("Erase this selection");
		eraseSeq = new MenuItem("Erase this trajectory");
		importTrack1 = new MenuItem("Import Track...");
		importTrack2 = new MenuItem("Import Track...");
		importTrack3 = new MenuItem("Import Track...");
		exportTrack = new MenuItem("Export Track...");
		importSeq1 = new MenuItem("Import Trajectory...");
		importSeq2 = new MenuItem("Import Trajectory...");
		importSeq3 = new MenuItem("Import Trajectory...");
		importWave1 = new MenuItem("Import WaveForm from SoundPool...");
		importWave2 = new MenuItem("Import WaveForm from SoundPool...");
		importWave3 = new MenuItem("Import WaveForm from SoundPool...");
		exportSeq = new MenuItem("Export Trajectory...");
		exportSeqICST = new MenuItem("Export ICST Trajectory...");
		setCursor1.addActionListener(aa);
		setCursor2.addActionListener(aa);
		setCursor3.addActionListener(aa);
		viewAll1.addActionListener(aa);
		viewAll2.addActionListener(aa);
		viewAll3.addActionListener(aa);
		viewTrack.addActionListener(aa);
		viewMultiSeq.addActionListener(aa);
		viewSeq.addActionListener(aa);
		zoomAll1.addActionListener(aa);
		zoomAll2.addActionListener(aa);
		zoomAll3.addActionListener(aa);
		zoomTrack.addActionListener(aa);
		zoomMultiSeq.addActionListener(aa);
		zoomSeq.addActionListener(aa);
		lockTrack.addActionListener(aa);
		joinTrack.addActionListener(aa);
		joinMultiSeq.addActionListener(aa);
		trimTrack.addActionListener(aa);
		trimMultiSeq.addActionListener(aa);
		trimSeq.addActionListener(aa);
		copyTrack.addActionListener(aa);
		copyMultiSeq.addActionListener(aa);
		copySeq.addActionListener(aa);
		cuttTrack.addActionListener(aa);
		cuttMultiSeq.addActionListener(aa);
		cuttSeq.addActionListener(aa);
		paste1.addActionListener(aa);
		paste2.addActionListener(aa);
		paste3.addActionListener(aa);
		eraseTrack.addActionListener(aa);
		eraseMultiSeq.addActionListener(aa);
		eraseSeq.addActionListener(aa);
		moveTrack.addActionListener(aa);
		moveMultiSeq.addActionListener(aa);
		moveSeq.addActionListener(aa);
		cutAtTrack.addActionListener(aa);
		cutAtMultiSeq.addActionListener(aa);
		cutAtSeq.addActionListener(aa);
		importTrack1.addActionListener(aa);
		importTrack2.addActionListener(aa);
		importTrack3.addActionListener(aa);
		exportTrack.addActionListener(aa);
		importSeq1.addActionListener(aa);
		importSeq2.addActionListener(aa);
		importSeq3.addActionListener(aa);
		importWave1.addActionListener(aa);
		importWave2.addActionListener(aa);
		importWave3.addActionListener(aa);
		exportSeq.addActionListener(aa);
		exportSeqICST.addActionListener(aa);
		
		trackPopup.add(setCursor1);
		trackPopup.addSeparator();
		trackPopup.add(viewAll1);
		trackPopup.add(zoomAll1);
		trackPopup.addSeparator();
		trackPopup.add(viewTrack);
		trackPopup.add(zoomTrack);
		trackPopup.add(lockTrack);
		trackPopup.addSeparator();
		trackPopup.add(joinTrack);
		trackPopup.add(trimTrack);
		trackPopup.add(moveTrack);
		trackPopup.add(cutAtTrack);
		trackPopup.addSeparator();
		trackPopup.add(copyTrack);
		trackPopup.add(cuttTrack);
		trackPopup.add(paste1);
		trackPopup.add(eraseTrack);
		trackPopup.addSeparator();
		trackPopup.add(importTrack1);
		trackPopup.add(importSeq1);
		trackPopup.add(importWave1);
		trackPopup.add(exportTrack);
		multiSeqPopup.add(setCursor2);
		multiSeqPopup.addSeparator();
		multiSeqPopup.add(viewAll2);
		multiSeqPopup.add(zoomAll2);
		multiSeqPopup.addSeparator();
		multiSeqPopup.add(viewMultiSeq);
		multiSeqPopup.add(zoomMultiSeq);
		multiSeqPopup.addSeparator();
		multiSeqPopup.add(joinMultiSeq);
		multiSeqPopup.add(trimMultiSeq);
		multiSeqPopup.add(moveMultiSeq);
		multiSeqPopup.add(cutAtMultiSeq);
		multiSeqPopup.addSeparator();
		multiSeqPopup.add(copyMultiSeq);
		multiSeqPopup.add(cuttMultiSeq);
		multiSeqPopup.add(paste2);
		multiSeqPopup.add(eraseMultiSeq);
		multiSeqPopup.addSeparator();
		multiSeqPopup.add(importTrack2);
		multiSeqPopup.add(importSeq2);
		multiSeqPopup.add(importWave2);
		multiSeqPopup.add(exportSeq);
		multiSeqPopup.add(exportSeqICST);
		seqPopup.add(setCursor3);
		seqPopup.addSeparator();
		seqPopup.add(viewAll3);
		seqPopup.add(zoomAll3);
		seqPopup.addSeparator();
		seqPopup.add(viewSeq);
		seqPopup.add(zoomSeq);
		seqPopup.addSeparator();
		seqPopup.add(trimSeq);
		seqPopup.add(moveSeq);
		seqPopup.add(cutAtSeq);
		seqPopup.addSeparator();
		seqPopup.add(copySeq);
		seqPopup.add(cuttSeq);
		seqPopup.add(paste3);
		seqPopup.add(eraseSeq);
		seqPopup.addSeparator();
		seqPopup.add(importTrack3);
		seqPopup.add(importSeq3);
		seqPopup.add(importWave3);
		seqPopup.add(exportSeq);
		seqPopup.add(exportSeqICST);
		add(trackPopup);
		add(multiSeqPopup);
		add(seqPopup);
		
		headerPopup = new TrackPopup(holoEditRef.gestionPistes,0);
		add(headerPopup);
	}

	public void updateGUI()
	{
		initSizes();
		setSize(sizW, sizH);
	}

	public void display()
	{
		if (visible)
			glp.display();
	}

	public void init(GLAutoDrawable drawable)
	{
		gl = drawable.getGL();
		glu = new GLU();
		glut = new GLUT();
		gl.glClearColor(borderColor[0], borderColor[1], borderColor[2], borderColor[3]); // White Background
		gl.glViewport(0, 0, width, height);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_TEXTURE_2D);
		textCross = OpenGLUt.textLoad(gl, glu, "but.png");
		textCross2 = OpenGLUt.textLoad(gl, glu, "but2.png");
		textPlus = OpenGLUt.textLoad(gl, glu, "bp.png");
		textMinus = OpenGLUt.textLoad(gl, glu, "bm.png");
		textPlusBright = OpenGLUt.textLoad(gl, glu, "bpBright.png");
		textMinusBright = OpenGLUt.textLoad(gl, glu, "bmBright.png");
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
		gl.glMatrixMode(GL.GL_PROJECTION);
		tracksGlobalHeight = height - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		tracksGlobalWidth = width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12;
		maxTrackH = tracksGlobalHeight;
		holoEditRef.rtDisplay.add(drawable);
	}

	@SuppressWarnings("deprecation")
	public void display(GLAutoDrawable drawable)
	{
		int cursTime = !holoEditRef.connection.isPlaying() ? cursorTime : holoEditRef.connection.getCurrentTime();
		//mainRef.counterPanel.setCompteur(4,cursTime);
		if(popupVisible) return;
		if(!visible) return;
		gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glLoadIdentity();
		// TRACK DISPLAY
		int tkNb = holoEditRef.gestionPistes.getNbTracks();
		// int defaultTrackHeightPix = tracksGlobalHeight / tkNb;
		//float defaultTrackHeightPct = (float) defaultTrackHeightPix / tracksGlobalHeight;
		float defaultTrackHeightPct = 1 / (float)tkNb;
		while (size.size() < tkNb)
			size.add(defaultTrackHeightPct);
		OpenGLUt.glColor(gl, Color.BLACK);
		int rest = tracksGlobalHeight - sumSizes();
		// int currentHeight = rest;
		if (draggedSelZone)
		{
			selIndex = new Vector<Integer>();
			selSeqs = new Vector<HoloTraj>();
			selWaves = new Vector<WaveFormInstance>();
			selSDIFs = new Vector<SDIFdataInstance>();
			selTracks = new Vector<HoloTrack>();
		}
		select = !draggedScale && !draggedTimeScroll && !draggedTrackScroll  && !draggedSel && !draggedSelBegin
				&& !draggedSelEnd && !draggedSeq && !draggedSeqBegin && !draggedSeqEnd && !draggedSelZone && !popupVisible;
		try	{
			drawTracksAndHeaders(tkNb, rest);
		} catch (Exception e)
		{
			// TRACK HAS BEEN MODIFIED DURING DISPLAY
			System.err.println("Score Draw Tracks and Headers Exception");
			e.printStackTrace();
		}
		query_one_select = false;
		query_track_select = false;
		query_sel_select = false;
		
	//	drawTrackRest(rest);
		// TRACK SCROLL
//		gl.glViewport(width - W_TRACK_SCROLL-W_RIGHT_BORDER2, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest, W_TRACK_SCROLL, height - H_TIME_SCROLL - H_TIME_SCALE - H_BOTTOM_BORDER12 - rest);
//		drawTrackScroll(true);
		// TIME SCALE
		gl.glViewport(W_TRACK_HEADER, height - H_TIME_SCALE, width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12, H_TIME_SCALE);
		if (select)
			getScaleHFromMouse();
		drawTimeScale(true);

		// TIME SCROLL
		totalTime = holoEditRef.connection.getTotalTime();
		for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
		{
			totalTime = Ut.max(tk.getLastDate(), totalTime);
			if(!tk.waves.isEmpty())
					for(WaveFormInstance w:tk.waves)
						totalTime = Ut.max(totalTime,w.getLastDate());
			if(!tk.sdifdataInstanceVector.isEmpty())
				for(SDIFdataInstance s:tk.sdifdataInstanceVector)
					totalTime = Ut.max(totalTime,s.getLastDate());
		}
		//mainRef.counterPanel.setCompteur(3,totalTime);
		int buttonSize = (int) (H_TIME_SCROLL*1.5);
		int buttonsTotal = (int) (buttonSize*3.5);
		gl.glViewport(W_TRACK_HEADER, H_BOTTOM_BORDER2+rest, width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12-buttonsTotal, H_TIME_SCROLL);
		if (select &&  timeZoomButtons==ScoreIndex.getNull())
			getScrollHFromMouse();
		drawTimeScroll(true);
		gl.glViewport(width - W_TRACK_SCROLL - W_RIGHT_BORDER12-buttonsTotal, H_BOTTOM_BORDER2+rest-H_TIME_SCROLL/4, buttonsTotal, buttonSize);
		if (select)
			getBPBMtimeFromMouse();
		drawTimeScrollButtons(true);
		
		//gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		buttonSize = (int) (W_TRACK_SCROLL*1.5);
		buttonsTotal = (int) (buttonSize*3.5);
		gl.glViewport(width - W_TRACK_SCROLL - W_RIGHT_BORDER2, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest + buttonsTotal, W_TRACK_SCROLL, height-H_TIME_SCALE-H_TIME_SCROLL - H_BOTTOM_BORDER12 - rest - buttonsTotal);
		if (select && trackZoomButtons==ScoreIndex.getNull())
			getScrollVFromMouse();
		drawTrackScroll(true);
		gl.glViewport(width - W_TRACK_SCROLL - W_RIGHT_BORDER2 - W_TRACK_SCROLL/4, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest, buttonSize, buttonsTotal);
		if (select)
			getBPBMtrackFromMouse();
		drawTrackScrollButtons(true);
		
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		drawDummyCorners(rest);
		// SELZONE
		drawSelZone(trackToDraw(), rest);
		
		if(status != null && status.getState().equals(Thread.State.RUNNABLE))
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
					while(true)
					{
						if(visible)
							statusBarInfos();
						Thread.sleep(50);
					}
				} catch(Exception e) {
					System.err.println("-");
//					e.printStackTrace();
				}
			}
		};
		status.setName("score-status");
		status.setPriority(Thread.MIN_PRIORITY);
		status.setDaemon(true);
		status.start();
	}

	/** Affichage d'informations dans la statusBar selon la selection. */
	private void statusBarInfos()
	{
		try
		{
			if (select && selected != ScoreIndex.getNull())
			{
				int[] iA = ScoreIndex.decode(selected);
				if (ScoreIndex.isWave())
				{
					currentWave = holoEditRef.gestionPistes.getTrack(iA[1]).getWave(iA[2]);
					if(currentWave != null)
						toStatus("Track : " + iA[1] + "   Wave n" + Ut.numCar + ":" + iA[2] + "   from : " + Ut.msToHMSMS(currentWave.getFirstDate()) + "   to : " + Ut.msToHMSMS(currentWave.getLastDate()) + "  duration : " + Ut.msToHMSMS(currentWave.getWave().getFileLength())
							+ "   Filename : " + currentWave.getName() + "  Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate()));
				}
				else if (ScoreIndex.isSeq())
				{
					currentSeq = holoEditRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]);
					if(currentSeq != null)
						toStatus("Track : " + iA[1] + "   Traj n" + Ut.numCar + ":" + iA[2] + "   from : " + Ut.msToHMSMS(currentSeq.getFirstDate()) + "   to : " + Ut.msToHMSMS(currentSeq.getLastDate()) + "  duration : " + Ut.msToHMSMS(currentSeq.getDuration())
							+ "   points number : " + currentSeq.size() + "  Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate()));
				}
				else if (ScoreIndex.isData())
				{
					currentSDIF = holoEditRef.gestionPistes.getTrack(iA[1]).getSDIFinst(iA[2]);
					if(currentSDIF != null)
						toStatus("Track : " + iA[1] + "   SDIF n" + Ut.numCar + ":" + iA[2] + "   from : " + Ut.msToHMSMS(currentSDIF.getFirstDate()) + "   to : " + Ut.msToHMSMS(currentSDIF.getLastDate()) + "  duration : " + Ut.msToHMSMS(currentSDIF.getDuration())
							+ "   Filename : " + currentSDIF.getName() + "  Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate()));
				}
				else {
					toStatus("Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate()));// + "\t   Cursor Time : " + Ut.msToHMSMS(cursorTime * 10));
				}
			} else if (draggedSeq || draggedMultiSeq) {
				if (wavSelected != -1 && seqSelected == -1 && sdifSelected == -1)
				{
					currentWave = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getWave(wavSelected);
					if(currentWave != null)
						toStatus("Track : " + trackSelected + "   Wave n" + Ut.numCar + ":" + wavSelected + "   from : " + Ut.msToHMSMS((dX + currentWave.getFirstDate()) ) + "   to : " + Ut.msToHMSMS((dX + currentWave.getLastDate())) + "  duration : "
							+ Ut.msToHMSMS(currentWave.getWave().getFileLength()) + "   Filename : " + currentWave.getName());
				}
				else if (seqSelected != -1 && wavSelected == -1 &&  sdifSelected == -1)
				{
					currentSeq = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getHoloTraj(seqSelected);
					if(currentSeq != null)
						toStatus("Track : " + trackSelected + "   Traj n" + Ut.numCar + ":" + seqSelected + "   from : " + Ut.msToHMSMS((dX + currentSeq.getFirstDate())) + "   to : " + Ut.msToHMSMS((dX + currentSeq.getLastDate())) + "  duration : "
							+ Ut.msToHMSMS(currentSeq.getDuration()) + "   points number : " + currentSeq.size());
				}
				else if (sdifSelected != -1 && seqSelected == -1 && wavSelected == -1)
				{
					currentSDIF = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getSDIFinst(sdifSelected);
					if(currentSDIF != null)
						toStatus("Track : " + trackSelected + "   SDIF n" + Ut.numCar + ":" + sdifSelected + "   from : " + Ut.msToHMSMS((dX + currentSDIF.getFirstDate())) + "   to : " + Ut.msToHMSMS((dX + currentSDIF.getLastDate())) + "  duration : "
							+ Ut.msToHMSMS(currentSDIF.getDuration()) + "   Filename : " + currentSDIF.getName());
				}
			}
			else if (draggedSeqBegin || draggedMultiSeqBegin || draggedSeqEnd || draggedMultiSeqEnd)
			{
				if (seqSelected != -1){
					currentSeq = holoEditRef.gestionPistes.getTrack(trackSelected).getHoloTraj(seqSelected);
					if(currentSeq != null)
						toStatus("Track : " + trackSelected + "   Traj n" + Ut.numCar + ":" + seqSelected + "   from : " + Ut.msToHMSMS(currentSeq.getFirstDate()) + "   to : " + Ut.msToHMSMS((dX + currentSeq.getLastDate())) + "  duration : "
							+ Ut.msToHMSMS((dX + currentSeq.getDuration())) + "   points number : " + currentSeq.size());
				}else if (sdifSelected != -1){
					currentSDIF = holoEditRef.gestionPistes.getTrack(trackSelected).getSDIFinst(sdifSelected);
					if(currentSDIF != null)
						toStatus("Track : " + trackSelected + "   SDIF n" + Ut.numCar + ":" + sdifSelected + "   from : " + Ut.msToHMSMS((dX + currentSDIF.getFirstDate())) + "   to : " + Ut.msToHMSMS(currentSDIF.getLastDate()) + "  duration : "
							+ Ut.msToHMSMS((dX + currentSDIF.getDuration())) + "    " + currentSDIF.getData().sdifTreeMap.size()+" values");
				}
			}
			else if (scaleSelected != ScoreIndex.getNull())
			{
				toStatus("Mouse Time : " + Ut.msToHMSMS(mouseCursor.getDate() * 10)/* + "\t   Cursor Time : " + Ut.msToHMSMS(cursorTime * 10)*/ + "   Begin Time : " + Ut.msToHMSMS(holoEditRef.counterPanel.getDate(1)) + "   End Time : "
						+ Ut.msToHMSMS(holoEditRef.counterPanel.getDate(2)));
			}else {
				toStatus("");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** Dessine la zone de selection.
	 * @param tkNb Le nombre de tracks dessin�s dans le score
	 * @param rest
	 */
	private void drawSelZone(int tkNb, int rest)
	{
		if (draggedSelZone)
		{
			gl.glViewport(W_TRACK_HEADER, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest, width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12, tracksGlobalHeight - rest);
			glu.gluOrtho2D(minTime, maxTime, 0, tkNb);
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
			float ti1 = selPt1.getDate();
			float ti2 = selPt2.getDate();
			int tr1 = (int) selPt1.getVal()-firstTrackToDraw();
			int tr2 = (int) selPt2.getVal()-firstTrackToDraw();

			float time1 = ti1 <= ti2 ? ti1 : ti2;
			float time2 = ti1 <= ti2 ? ti2 : ti1;
			if (tr1 <= tr2)
			{
				gl.glColor4fv(selZoneColor, 0);
				gl.glRectf(time1, tkNb - tr1, time2, tkNb - (tr2 + 1));
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
				gl.glColor4fv(selZoneBorderColor, 0);
				gl.glRectf(time1, tkNb - tr1, time2, tkNb - (tr2 + 1));
			} else {
				gl.glColor4fv(selZoneColor, 0);
				gl.glRectf(time1, tkNb - tr1 - 1, time2, tkNb - tr2);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
				gl.glColor4fv(selZoneBorderColor, 0);
				gl.glRectf(time1, tkNb - tr1 - 1, time2, tkNb - tr2);
			}
			gl.glLoadIdentity();
		}
	}
	
	/** @return index of the first track to be drawn. */	
	private int firstTrackToDraw() {
		float y = tracksGlobalHeight-maxTrackH;
		float div = y/tracksGlobalHeight;
		return (int) Math.floor(div*holoEditRef.gestionPistes.getNbTracks());
	}
	
	/** @return how many track have to be drawn. */
	private int trackToDraw() {
		float track=  holoEditRef.gestionPistes.getNbTracks() / (tracksGlobalHeight/(float) (maxTrackH-minTrackH));
		return (int) Math.ceil(track);
	}
	
	/** @return The zoom to apply on track height. */	
	private float trackZoom() {
		return  1+ (holoEditRef.gestionPistes.getNbTracks() - trackToDraw())/(float)trackToDraw();
	}
	
	
	private void drawTracksAndHeaders(int tkNb, int rest)
	{
		int cursTime = !holoEditRef.connection.isPlaying() ? cursorTime : holoEditRef.connection.getCurrentTime();
		//mainRef.counterPanel.setCompteur(4,cursTime);
		int w = width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12;
		
		boolean headOrScrollSelected = scrollVSelected != ScoreIndex.getNull() || headerSelected != ScoreIndex.getNull() || timeZoomButtons != ScoreIndex.getNull();
		headerSelected = ScoreIndex.getNull();
		for (int i = firstTrackToDraw()+trackToDraw()-1; i >= firstTrackToDraw(); i--)
		{
			currentDrawnTrack = holoEditRef.gestionPistes.getTrack(i);
			if(currentDrawnTrack == null)
				break;
			int trackHeight = (int) (size.get(i) * tracksGlobalHeight*trackZoom());
			gl.glViewport(W_TRACK_HEADER, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest, w, trackHeight - 5);
			if (!draggedScale && !draggedTimeScroll && !draggedTrackScroll && !draggedSel && !draggedSelBegin && !draggedSelEnd && !draggedSeqBegin && !draggedSeqEnd && !popupVisible && !headOrScrollSelected )
			{
				if (query_one_select)
					getObjectFromMouse(currentDrawnTrack, i, minTime, maxTime);
				if (query_track_select)
					getTrackFromMouse(currentDrawnTrack, i, minTime, maxTime);
				if (query_sel_select)
					getSeqsFromMouse(currentDrawnTrack, i, minTime, maxTime, rest, trackHeight);
			}
			glu.gluOrtho2D(minTime, maxTime, minY, maxY);
			OpenGLUt.glColor(gl, Color.BLACK);
			if(HoloEdit.smooth())
			{
				gl.glEnable(GL.GL_POINT_SMOOTH);
				gl.glEnable(GL.GL_LINE_SMOOTH);
				gl.glEnable(GL.GL_POLYGON_SMOOTH);
			}
			currentDrawnTrack.drawScore(gl, minTime, maxTime, holoEditRef.counterPanel.getDate(1), holoEditRef.counterPanel.getDate(2), true, selected, selSeqs, selWaves, selSDIFs, i, cursTime, w, HoloEdit.smooth(), draggedSelMode);
			// FEATURE DRAW DRAGGED MULTI SEQS (POLY BEGIN END)
			if(dragCtrl)
			{
				if ((draggedSeq || draggedMultiSeq) && trackSelected == i)
				{
					if (wavSelected != -1 && seqSelected == -1 && sdifSelected == -1)
					{
						if (holoEditRef.gestionPistes.getTrack(oldTrackSelected).getWave(wavSelected)!=null)
							holoEditRef.gestionPistes.getTrack(oldTrackSelected).getWave(wavSelected).drawScoreSquare(gl,currentDrawnTrack,minTime,maxTime, dX);
					} else if (sdifSelected != -1 && seqSelected == -1 && wavSelected == -1)
					{
						if (holoEditRef.gestionPistes.getTrack(oldTrackSelected).getSDIFinst(sdifSelected)!=null)
							holoEditRef.gestionPistes.getTrack(oldTrackSelected).getSDIFinst(sdifSelected).drawScoreSquare(gl,currentDrawnTrack, dX);
					} else  {
						if (oldTrackSelected == trackSelected)
							currentDrawnTrack.drawDraggedSeq(gl, minTime, maxTime, seqSelected, dX);
						else holoEditRef.gestionPistes.getTrack(oldTrackSelected).drawDraggedSeq(gl, minTime, maxTime, seqSelected, dX);
					}
				}
				else if ((draggedSeqBegin || draggedSeqEnd || draggedMultiSeqBegin || draggedMultiSeqEnd) && trackSelected == i)
				{
					if (draggedSeqBegin || draggedMultiSeqBegin){
						if (ScoreIndex.isSeqBegin())
							currentDrawnTrack.drawMovedSeq(gl, minTime, maxTime, seqSelected, dX, 0f, cutSeq);
						else if (ScoreIndex.isDataBegin())
							currentDrawnTrack.drawMovedSDIF(gl, minTime, maxTime, sdifSelected, dX, 0f, cutSeq);
					}else if (draggedSeqEnd || draggedMultiSeqEnd){
						if (ScoreIndex.isSeqEnd())
							currentDrawnTrack.drawMovedSeq(gl, minTime, maxTime, seqSelected, 0f, dX, cutSeq);
						else if (ScoreIndex.isDataEnd())
							currentDrawnTrack.drawMovedSDIF(gl, minTime, maxTime, sdifSelected, 0f, dX, cutSeq);
					}
				}
				else if(draggedWave && trackSelected == i)
				{
					currentDraggedWave.drawScoreSquare(gl,currentDrawnTrack,minTime,maxTime, 0f);
				}
				else if(draggedData && trackSelected == i)
				{
					currentDraggedSDIF.drawScoreSquare(gl,currentDrawnTrack, 0f);
				}
			}
			if(HoloEdit.smooth())
			{
				gl.glDisable(GL.GL_POINT_SMOOTH);
				gl.glDisable(GL.GL_LINE_SMOOTH);
				gl.glDisable(GL.GL_POLYGON_SMOOTH);
			}
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(minTime, minY);
			gl.glVertex2f(maxTime, minY);
			gl.glVertex2f(maxTime-0.1f, minY);
			gl.glVertex2f(maxTime-0.1f, maxY);
			gl.glEnd();

			gl.glLoadIdentity();
			// TRACK HEADER
			gl.glViewport(0, H_TIME_SCROLL + H_BOTTOM_BORDER12 + rest, W_TRACK_HEADER, trackHeight - 5);
			if(select)
				getHeaderFromMouse(currentDrawnTrack,i,trackHeight - 5);
			drawHeader(currentDrawnTrack, i, true,trackHeight - 5);
			rest += trackHeight;
		}
	}

	private void drawHeader(HoloTrack currentTk, int tkNum, boolean render, int h)
	{
		glu.gluOrtho2D(0, W_TRACK_HEADER, 0, h);
		if (currentTk.isLocked())
			OpenGLUt.glColor(gl, lockedTrackColor);
		else if (holoEditRef.gestionPistes.getActiveTrackNb() == tkNum)
			OpenGLUt.glColor(gl,activTrackColor);
		else OpenGLUt.glColor(gl, borderColor);
		gl.glLoadName(ScoreIndex.encode(ScoreIndex.TYPE_TK,tkNum,0,ScoreIndex.TK_HEAD));
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glRectf(0, 0, W_TRACK_HEADER, h);
		if(render)
		{
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(0, 0);
				gl.glVertex2f(W_TRACK_HEADER, 0);
				gl.glVertex2f(0, h);
				gl.glVertex2f(W_TRACK_HEADER, h);
			gl.glEnd();
			gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(W_TRACK_HEADER-0.1f, 0);
				gl.glVertex2f(W_TRACK_HEADER-0.1f, h);
				for (int k = 0; k <= 10; k++)
				{
					if (k == 0 || k == 5 || k == 10)
						gl.glVertex2f(W_TRACK_HEADER-10, (float) k * h / 15);
					else gl.glVertex2f(W_TRACK_HEADER-5, (float) k * h / 15);
					gl.glVertex2f(W_TRACK_HEADER, (float) k * h / 15);
				}
			gl.glEnd();
			OpenGLUt.drawTexture(gl, currentTk.isVisible() ? textCross2 : textCross, 5, h-17, 12, 12);
			OpenGLUt.glColor(gl, currentTk.getColor());
			gl.glRasterPos2f(22, h-15);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, currentTk.getName());
		}
		gl.glLoadIdentity();
	}

	public void open()
	{
		super.open();
		glp.display();
	}

	/** Dessin de l'echelle de temps. */
	private void drawTimeScale(boolean render)
	{
		// TIME SCALE
		glu.gluOrtho2D(minTime, maxTime, 0, 1.5f);
		OpenGLUt.glColor(gl, borderColor);
		gl.glLoadName(ScoreIndex.OT_TIMESCALE_IND);
		gl.glRectf(minTime, 0, maxTime, 1.5f);
		int begin = holoEditRef.counterPanel.getDate(1);
		int end = holoEditRef.counterPanel.getDate(2);
		int index = ScoreIndex.OT_TIMESEL_IND;
		if (index == scaleSelected)
			OpenGLUt.glColor(gl, timeSelSelColor);
		else OpenGLUt.glColor(gl, timeSelColor);
		gl.glLoadName(index);
		gl.glRectf(begin, 0, end, 1.5f);
		index = ScoreIndex.OT_TIMESEL_BEG_IND;
		gl.glLoadName(index);
		if (index == scaleSelected)
			gl.glLineWidth(2);
		else gl.glLineWidth(1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(begin, 0);
		gl.glVertex2f(begin, 1.5f);
		gl.glEnd();
		index = ScoreIndex.OT_TIMESEL_END_IND;
		gl.glLoadName(index);
		if (index == scaleSelected)
			gl.glLineWidth(2);
		else gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(end, 0);
		gl.glVertex2f(end, 1.5f);
		gl.glEnd();
		if (render)
		{
			gl.glLineWidth(1);
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
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, h + ":" + rf.format(mn) + ":" + rf.format(sec) + "\'");
				else glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, mn + ":" + rf.format(sec) + "\'");
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
		}
		gl.glLoadName(ScoreIndex.NULL);
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex2f(minTime, 1.5f);
		gl.glVertex2f(minTime, 0);
		gl.glVertex2f(maxTime, 0);
		gl.glVertex2f(maxTime, 1.5f);
		gl.glEnd();
		gl.glLoadIdentity();
	}	

	/** Dessin de la scrollBar servant � zoomer verticalement.
	 * (fait varier le nombre de track � dessiner). */
	private void drawTrackScroll(boolean render)
	{
		// TRACK SCROLL
		glu.gluOrtho2D(0, 1, 0, tracksGlobalHeight);
		int index = ScoreIndex.OT_SCROLL_VDOWN_IND;
		if (scrollVSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(0, 0, 1, minTrackH);

		index = ScoreIndex.OT_SCROLL_VUP_IND;
		if (scrollVSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(0, maxTrackH, 1, tracksGlobalHeight);
		
		index = ScoreIndex.OT_SCROLL_V_IND;
		if (index == scrollVSelected || draggedTrackScroll)
			OpenGLUt.glColor(gl, scrollSelFgColor);
		else OpenGLUt.glColor(gl, scrollFgColor);
		gl.glLoadName(index);
		gl.glRectf(0, minTrackH, 1, maxTrackH);
		
		// contour
		if (render)	{
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex2f(0, 0);
			gl.glVertex2f(0.99f, 0);
			gl.glVertex2f(0.99f, tracksGlobalHeight-0.1f);
			gl.glVertex2f(0, tracksGlobalHeight-0.1f);
			gl.glEnd();
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(0, minTrackH);
			gl.glVertex2f(0.99f, minTrackH);
			gl.glVertex2f(0, maxTrackH);
			gl.glVertex2f(0.99f, maxTrackH);
			gl.glEnd();
		}
		gl.glLoadIdentity();
	}
	
	/** Dessin de la scrollBar servant � zoomer dans le temps. */
	private void drawTimeScroll(boolean render)
	{
		// TIME SCROLL
		glu.gluOrtho2D(0, totalTime + ZOOM_OUT_MAX_PAD, 0, 1);
		int index = ScoreIndex.OT_SCROLL_HL_IND;
		if (scrollHSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(0, 0, minTime, 1);
		
		index = ScoreIndex.OT_SCROLL_HR_IND;
		if (scrollHSelected == index)
			OpenGLUt.glColor(gl, scrollSelBgColor);
		else OpenGLUt.glColor(gl, scrollBgColor);
		gl.glLoadName(index);
		gl.glRectf(maxTime, 0, totalTime + ZOOM_OUT_MAX_PAD, 1);
		
		index = ScoreIndex.OT_SCROLL_H_IND;
		if (index == scrollHSelected || draggedTimeScroll)
			OpenGLUt.glColor(gl, scrollSelFgColor);
		else OpenGLUt.glColor(gl, scrollFgColor);
		gl.glLoadName(index);
		gl.glRectf(minTime, 0, maxTime, 1);
		if (render)
		{
			OpenGLUt.glColor(gl, Color.BLACK);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			gl.glRectf(0, 0, totalTime + ZOOM_OUT_MAX_PAD-0.1f, 0.99f);
			gl.glRectf(minTime, 0, maxTime, 0.99f);
		}
		gl.glLoadIdentity();
	}

	private void drawTimeScrollButtons(boolean render)
	{
		// TIME SCROLL
		glu.gluOrtho2D(0, 35, 0, 10);
		int index = ScoreIndex.OT_SCROLL_H_BM;
		int pic;
		if (timeZoomButtons == index){
			pic = textMinusBright;
		}
		else pic = textMinus;
		gl.glLoadName(index);
		OpenGLUt.drawTexture(gl, pic, 5, 0, 10, 10);
		
		index = ScoreIndex.OT_SCROLL_H_BP;
		if (timeZoomButtons == index) {
			pic = textPlusBright;
		}
		else pic = textPlus;
		gl.glLoadName(index);
		OpenGLUt.drawTexture(gl, pic, 20, 0, 10, 10); 
		gl.glLoadIdentity();
	}
	
	private void drawTrackScrollButtons(boolean render)
	{
		// TIME SCROLL
		glu.gluOrtho2D(0, 10, 0, 35);
		//TODO boutons zoom
		int index = ScoreIndex.OT_SCROLL_V_BM;
		int pic;
		if (trackZoomButtons == index){
			pic = textMinusBright;
		} else pic = textMinus;		
		gl.glLoadName(index);
		OpenGLUt.drawTexture(gl, pic, 0, 5, 10, 10);
		
		index = ScoreIndex.OT_SCROLL_V_BP;
		if (trackZoomButtons == index) {
			pic = textPlusBright;
		} else pic = textPlus;		
		gl.glLoadName(index);
		
		OpenGLUt.drawTexture(gl, pic, 0, 20, 10, 10); 
		gl.glLoadIdentity();
	}
	
	private void drawTrackRest(int rest)
	{
		// TRACK DISPLAY_REST
		gl.glViewport(W_TRACK_HEADER + 1, H_BOTTOM_BORDER2 , width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12 - 1, rest - 1);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		gl.glLoadIdentity();
		// TRACK_HEADER_REST
		gl.glViewport(0, H_BOTTOM_BORDER2 , W_TRACK_HEADER, rest - 1);
		glu.gluOrtho2D(0, 1, 0, 1);
		OpenGLUt.glColor(gl, borderColor);
		gl.glRectf(0, 0, 1, 1);
		OpenGLUt.glColor(gl, Color.BLACK);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(1, 0);
		gl.glVertex2f(1, 1);
		gl.glEnd();
		gl.glLoadIdentity();
	}

	private void drawDummyCorners(int rest)
	{
		gl.glLineWidth(1);
		// DUMMY CORNERS
		// BOTTOM / LEFT
//		gl.glViewport(0, 0, W_TRACK_HEADER, H_TIME_SCROLL);
//		glu.gluOrtho2D(0, 1, 0, 1);
//		OpenGLUt.glColor(gl, borderColor);
//		gl.glRectf(0, 0, 1, 1);
//		OpenGLUt.glColor(gl, Color.BLACK);
//		gl.glBegin(GL.GL_LINES);
//		gl.glVertex2f(0, 1);
//		gl.glVertex2f(1, 1);
//		gl.glVertex2f(1, 0);
//		gl.glVertex2f(1, 1);
//		gl.glEnd();
//		gl.glLoadIdentity();
//		// BOTTOM / RIGHT
//		gl.glViewport(width - W_TRACK_SCROLL, 0, W_TRACK_SCROLL, H_TIME_SCROLL);
//		glu.gluOrtho2D(0, 1, 0, 1);
//		OpenGLUt.glColor(gl, borderColor);
//		gl.glRectf(0, 0, 1, 1);
//		OpenGLUt.glColor(gl, Color.BLACK);
//		gl.glBegin(GL.GL_LINES);
//		gl.glVertex2f(0, 1);
//		gl.glVertex2f(1, 1);
//		gl.glVertex2f(0, 0);
//		gl.glVertex2f(0, 1);
//		gl.glEnd();
//		gl.glLoadIdentity();
		// TOP / LEFT
		gl.glViewport(0, height - H_TIME_SCALE, W_TRACK_HEADER, H_TIME_SCALE);
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
		gl.glLoadIdentity();
		// TOP / RIGHT
//		gl.glViewport(width - W_TRACK_SCROLL, height - H_TIME_SCALE, W_TRACK_SCROLL, H_TIME_SCALE);
//		glu.gluOrtho2D(0, 1, 0, 1);
//		OpenGLUt.glColor(gl, borderColor);
//		gl.glRectf(0, 0, 1, 1);
//		OpenGLUt.glColor(gl, Color.BLACK);
//		gl.glBegin(GL.GL_LINES);
//		gl.glVertex2f(0, 0);
//		gl.glVertex2f(0, 1);
//		gl.glVertex2f(0, 0);
//		gl.glVertex2f(1, 0);
//		gl.glEnd();
//		gl.glLoadIdentity();
	}
	

	public void initSizes()
	{
		size = new Vector<Float>(1, 1);
		int defaultTrackHeightPix = tracksGlobalHeight / holoEditRef.gestionPistes.getNbTracks();
		float defaultTrackHeightPct = (float) defaultTrackHeightPix / tracksGlobalHeight;
		while (size.size() < holoEditRef.gestionPistes.getNbTracks())
			size.add(defaultTrackHeightPct);
	}

	public int sumSizes()
	{
		int tmp = 0;
		for (float f : size)
			tmp += (int) (f * tracksGlobalHeight);
		return tmp;
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		width = w;
		height = h;
		maxTrackH = (height - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12) * maxTrackH / tracksGlobalHeight;
		minTrackH = (height - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12) * minTrackH / tracksGlobalHeight;
		tracksGlobalHeight = height - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		tracksGlobalWidth = width - W_TRACK_HEADER - W_TRACK_SCROLL - W_RIGHT_BORDER12;
		initSizes();
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

	public String toString() {
		return "\t<score" + super.toString();
	}

	public void getObjectFromMouse(HoloTrack ht, int htNum, int beginTime, int endTime)
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		glu.gluOrtho2D(beginTime, endTime, minY, maxY);
		gl.glMatrixMode(GL.GL_PROJECTION);
		ht.drawScore(gl, beginTime, endTime, -1, -1, false, selected, selSeqs, selWaves, selSDIFs, htNum, -1, viewport[2],false, draggedSelMode);
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
			ScoreIndex.decode(tmp);
			if (ScoreIndex.isTrack())
				trackSelected = ScoreIndex.getTrack();
		}
		if (tmp != -1)
			selected = tmp;
	}

	public void getSeqsFromMouse(HoloTrack ht, int htNum, int beginTime, int endTime, int currentH, int tkH)
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
		double midPtX = (mousex1 + mousex2) / 2;
		double midPtY = currentH + tkH / 2;
		double w = Math.abs(mousex2 - mousex1);
		boolean ok = false;
		int tr1 = (int) selPt1.getVal();
		int tr2 = (int) selPt2.getVal();
		if (tr1 <= tr2)
			ok = Ut.between(htNum, tr1, tr2);
		else ok = Ut.between(htNum, tr2, tr1);
		if (w != 0 && ok)
		{
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glInitNames();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glPushName(-1);
			gl.glPushMatrix();
			gl.glLoadIdentity(); // reset the proj. matrix
			// !!! leave gluPickMatrix after glloadidentity
			glu.gluPickMatrix(midPtX, midPtY, w, tkH, viewport, 0);
			// !!! leave gluOrtho after glupickmatrix
			glu.gluOrtho2D(beginTime, endTime, minY, maxY);
			gl.glMatrixMode(GL.GL_PROJECTION);
			ht.drawScore(gl, beginTime, endTime, -1, -1, false, selected, selSeqs, selWaves, selSDIFs, htNum, -1, viewport[2], false, draggedSelMode);
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
				ScoreIndex.decode(tmp);
				if ((ScoreIndex.isSeq() || ScoreIndex.isWave() || ScoreIndex.isData()) && !selIndex.contains(tmp))
					selIndex.add(tmp);
			}
			treatSeqIndex();
		}
	}

	public void getTrackFromMouse(HoloTrack ht, int htNum, int beginTime, int endTime)
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		glu.gluOrtho2D(beginTime, endTime, minY, maxY);
		gl.glMatrixMode(GL.GL_PROJECTION);
		ht.drawDummyBg(gl, beginTime, endTime, htNum);
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
					tmp = selectBuf.get(offset);
				offset++;
			}
		}
		ScoreIndex.decode(tmp);
		if (ScoreIndex.isTrack())
			trackSelected = ScoreIndex.getTrack();
	}

	private void getHeaderFromMouse(HoloTrack ht, int htNum, int h)
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, vPort, 0);
		// !!! leave gluOrtho after glupickmatrix
		drawHeader(ht, htNum, false, h);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if(hits == 0)
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
	}

	public void getScaleHFromMouse()
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		drawTimeScale(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			scaleSelected = ScoreIndex.getNull();
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
					scaleSelected = selectBuf.get(offset);
				}
				offset++;
			}
		}
	}

	public void getScrollHFromMouse()
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		drawTimeScroll(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			scrollHSelected = ScoreIndex.getNull();
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
					scrollHSelected = selectBuf.get(offset);
				}
				offset++;
			}
		}
	}

	public void getBPBMtimeFromMouse()
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		drawTimeScrollButtons(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			timeZoomButtons = ScoreIndex.getNull();
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
					timeZoomButtons = selectBuf.get(offset);
				}
				offset++;
			}
		}
	}
	public void getBPBMtrackFromMouse()
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
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
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		// !!! leave gluOrtho after glupickmatrix
		drawTrackScrollButtons(false);
		gl.glLoadIdentity();
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			trackZoomButtons = ScoreIndex.getNull();
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
					trackZoomButtons = selectBuf.get(offset);
				}
				offset++;
			}
		}
	}
	
	 public void getScrollVFromMouse()
	{
		int hits = 0;
		int[] viewport = new int[4];
		selectBuf = BufferUtil.newIntBuffer(512);//ByteBuffer.allocateDirect(2048).asIntBuffer();
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glSelectBuffer(selectBuf.capacity(), selectBuf);
		gl.glRenderMode(GL.GL_SELECT);
		gl.glInitNames();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushName(-1);
		gl.glPushMatrix();
		gl.glLoadIdentity(); // reset the proj. matrix
		// !!! leave gluPickMatrix after glloadidentity
	//	 create MOUSE_SELECT_SIZExMOUSE_SELECT_SIZE pixel picking region near cursor location 
		double my = height - (mousey + H_TIME_SCALE + H_TIME_SCROLL + H_BOTTOM_BORDER12);
		glu.gluPickMatrix(mousex, my, MOUSE_SELECT_SIZE, MOUSE_SELECT_SIZE, viewport, 0);
		
		// !!! leave gluOrtho after glupickmatrix
		drawTrackScroll(false);
		gl.glPopMatrix();
		hits = gl.glRenderMode(GL.GL_RENDER);
		if (hits == 0)
		{
			scrollVSelected = ScoreIndex.getNull();
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
					scrollVSelected = selectBuf.get(offset);
				}
				offset++;
			}
			
		}
		
		//System.out.println("vSel "+scrollVSelected);
	}
	 
	
	public void mouseClicked(MouseEvent e)
	{
		ScoreIndex.decode(selected);
		if ((ScoreIndex.isNull(selected) || ScoreIndex.isTrack()) && !(e.getClickCount() >= 2))
		{
			selIndex = new Vector<Integer>();
			selSeqs = new Vector<HoloTraj>();
			selWaves = new Vector<WaveFormInstance>();
			selSDIFs = new Vector<SDIFdataInstance>();
			selTracks = new Vector<HoloTrack>();
		}
	}


	public void mousePressed(MouseEvent e)
	{
		float posW = e.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
		dragCtrl = false;
		ScoreIndex.decode(selected);
		if (ScoreIndex.isNull(selected))
		{
		} else if (ScoreIndex.isTrack()) {
			if (holoEditRef.allTrackActive)
				holoEditRef.gestionPistes.selectTrack(trackSelected);
			if (e.getButton() == MouseEvent.BUTTON3 || (e.isControlDown() && Ut.MAC))
			{
				boolean lockState = holoEditRef.gestionPistes.getTrack(trackSelected).isLocked();
				lockTrack.setLabel(lockState ? "Unlock this track" : "Lock this track");
				joinTrack.setEnabled(!lockState);
				trimTrack.setEnabled(!lockState);
				cuttTrack.setEnabled(!lockState);
				paste1.setEnabled(!lockState);
				eraseTrack.setEnabled(!lockState);
				oldCurrentX = convPosW(posW);
				popupVisible = true;
				holoEditRef.gestionPistes.selectTrack(trackSelected);
				trackPopup.show(this, e.getX(), e.getY() + 16);
			} else if (e.getClickCount() >= 2) {
				holoEditRef.gestionPistes.selectTrack(trackSelected);
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
				selIndex.addAll(holoEditRef.gestionPistes.getActiveTrack().getAllTrajs(holoEditRef.gestionPistes.getActiveTrackNb(), minTime, maxTime));
				selIndex.addAll(holoEditRef.gestionPistes.getActiveTrack().getAllWaves(holoEditRef.gestionPistes.getActiveTrackNb(), minTime, maxTime));
				selIndex.addAll(holoEditRef.gestionPistes.getActiveTrack().getAllSDIFs(minTime, maxTime));
				treatSeqIndex();
			} else if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1) {
				cursorTime = (int)convPosW(posW);
				holoEditRef.connection.setCurrentTime(cursorTime);
				holoEditRef.counterPanel.setCompteur(5,cursorTime);
				if(holoEditRef.connection.isPlaying())
					holoEditRef.connection.stopAndPlay();
			} else {
				if(!holoEditRef.connection.isPlaying())
				{
					draggedSelZone = true;
					draggedSelMode = SEL_BOTH;
					selIndex = new Vector<Integer>();
					selSeqs = new Vector<HoloTraj>();
					selWaves = new Vector<WaveFormInstance>();
					selSDIFs = new Vector<SDIFdataInstance>();
					selTracks = new Vector<HoloTrack>();
					selPt2 = selPt1 = new ScorePoint(convPosW(posW), trackSelected);
					mousex2 = mousex1 = e.getX();
					query_track_select = true;
				}
			}
		} else if (ScoreIndex.isSeq() || ScoreIndex.isWave() || ScoreIndex.isData()) {

			if (holoEditRef.allTrackActive)
				holoEditRef.gestionPistes.selectTrack(trackSelected);
			if (!selIndex.isEmpty() && !selIndex.contains(selected))
			{
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
			}
			oldTrackSelected = trackSelected;
			if(ScoreIndex.isSeq())
			{
				seqSelected = ScoreIndex.getSeq();
				wavSelected = -1;
				sdifSelected = -1;
			} else if(ScoreIndex.isWave()) {
				seqSelected = -1;
				wavSelected = ScoreIndex.getSeq();
				sdifSelected = -1;
			} else if(ScoreIndex.isData()) {
				seqSelected = -1;
				wavSelected = -1;
				sdifSelected = ScoreIndex.getSeq();
			}
			mouseCursor = convPosPt(posW, posH);
			if (e.getClickCount() >= 2) {
				if(ScoreIndex.isSeq()) {
					seqSelected = ScoreIndex.getSeq();
					wavSelected = -1;
					sdifSelected = -1;
				} else if(ScoreIndex.isWave()) {
					wavSelected = ScoreIndex.getSeq();
					seqSelected = -1;
					sdifSelected = -1;
				} else if(ScoreIndex.isData()) {
					sdifSelected = ScoreIndex.getSeq();
					seqSelected = -1;
					wavSelected = -1;
				}
				holoEditRef.gestionPistes.selectTrack(ScoreIndex.getTrack());
				if(selIndex.isEmpty())
					viewSeq();
				else
					viewMultiSeq();
				holoEditRef.timeEditor.open();
		 	} else	if (e.getButton() == MouseEvent.BUTTON3 || (e.isControlDown() && Ut.MAC)) {
				if (selIndex.isEmpty())
				{
					boolean seq = ScoreIndex.isSeq();
					trimSeq.setEnabled(seq);
					cutAtSeq.setEnabled(seq);
					exportSeq.setEnabled(seq);
					exportSeqICST.setEnabled(seq);
					viewSeq.setLabel(seq ? "View this trajectory" : "View this waveform");
					zoomSeq.setLabel(seq ? "Zoom on this trajectory" : "Zoom on this waveform");
					moveSeq.setLabel(seq ? "Move this trajectory to time..." : "Move this waveform to time...");
					trimSeq.setLabel(seq ? "Trim this trajectory to current time selection" : "Cannot trim waveforms");
					cutAtSeq.setLabel(seq ? "Cut this selection at time..." : "Cannot \"cut at time\" waveforms");
					copySeq.setLabel(seq ? "Copy this trajectory" : "Copy this waveform");
					cuttSeq.setLabel(seq ? "Cut this trajectory" : "Cut this waveform");
					eraseSeq.setLabel(seq ? "Erase this trajectory" : "Erase this waveform");
					exportSeq.setLabel(seq ? "Export this trajectory..." : "Cannot export waveforms");
					exportSeqICST.setLabel(seq ? "Export this trajectory (ICST)..." : "Cannot export waveforms");
					popupVisible = true;
					holoEditRef.gestionPistes.selectTrack(ScoreIndex.getTrack());
					seqPopup.show(this, e.getX(), e.getY() + 16);
				} else {
					popupVisible = true;
					holoEditRef.gestionPistes.selectTrack(ScoreIndex.getTrack());
					multiSeqPopup.show(this, e.getX(), e.getY() + 16);
				}
				
				/*
				 if(ScoreIndex.isWave()) {
						wavSelected = ScoreIndex.getSeq();
						seqSelected = -1;
						sdifSelected = -1;
					} else if(ScoreIndex.isData()) {
						sdifSelected = ScoreIndex.getSeq();
						seqSelected = -1;
						wavSelected = -1;
					}
					mainRef.gestionPistes.selectTrack(ScoreIndex.getTrack());
					if(selIndex.isEmpty())
						viewSeq();
					else
						viewMultiSeq();
					mainRef.timeEditor.open();
				*/
				
				
				
			} else if (e.isShiftDown() && ((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))) {// crtl/pomme +shift +click => supprime
				if (selIndex.isEmpty())
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
					if(ScoreIndex.isSeq())
						currentTrack.removeTraj(seqSelected);
					else if(ScoreIndex.isWave())
						currentTrack.removeWave(wavSelected);
					else if(ScoreIndex.isData())
						currentTrack.removeSdif(sdifSelected);
					currentTrack.update();
				} else {
					holoEditRef.gestionPistes.StoreToUndoAll();
					for (int k = selIndex.size() - 1; k >= 0; k--)
					{
						int[] iA = ScoreIndex.decode(selIndex.get(k));
						currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
						if(ScoreIndex.isSeq())
							currentTrack.removeTraj(iA[2]);
						else if(ScoreIndex.isWave()) // added
							currentTrack.removeWave(iA[2]);
						else if(ScoreIndex.isData())
							currentTrack.removeSdif(iA[2]);
						currentTrack.update();
					}
				}
				selected = ScoreIndex.getNull();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
			} else if (((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))) { // crtl/pomme +click => cut
				if (selIndex.isEmpty())
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
					if (e.isAltDown())
						currentTrack.cutAt((int) mouseCursor.getDate());						
					else if (ScoreIndex.isSeq())
						currentTrack.cutTrajAt((int) mouseCursor.getDate());
					else if (ScoreIndex.isData())
						currentTrack.cutSDIFat((int) mouseCursor.getDate());
					currentTrack.update();
				} else {
					holoEditRef.gestionPistes.StoreToUndoAll();
					for (HoloTrack tk : selTracks)
					{
						tk.cutAt((int) mouseCursor.getDate());
						tk.update();
					}
				}
				selected = ScoreIndex.getNull();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
			} else {
				if(ScoreIndex.isSeq())
				{
					seqSelected = ScoreIndex.getSeq();
					wavSelected = -1;
					sdifSelected = -1;
				}
				else if(ScoreIndex.isWave())
				{
					seqSelected = -1;
					wavSelected = ScoreIndex.getSeq();
					sdifSelected = -1;
				}
				else if(ScoreIndex.isData())
				{
					seqSelected = -1;
					wavSelected = -1;
					sdifSelected = ScoreIndex.getSeq();
				}
				mouseCursor = convPosPt(posW, posH);
				oldCurrentX = mouseCursor.getDate();
				dX = 0;
				mousex = e.getX();
				mousey = e.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
				query_track_select = true;
				if (selIndex.isEmpty())
					draggedSeq = true;
				else
					draggedMultiSeq = true;
			}
		} else if (ScoreIndex.isSeqBegin() || ScoreIndex.isDataBegin()) {
			if (ScoreIndex.isSeqBegin()) {
				seqSelected = ScoreIndex.getSeq();
				currentSeq = holoEditRef.gestionPistes.getTrack(trackSelected).getHoloTraj(seqSelected);
				if (!selIndex.isEmpty() && !selSeqs.contains(currentSeq))
				{
					selIndex = new Vector<Integer>();
					selSeqs = new Vector<HoloTraj>();
					selWaves = new Vector<WaveFormInstance>();
					selSDIFs = new Vector<SDIFdataInstance>();
					selTracks = new Vector<HoloTrack>();
				}
			} else {
				sdifSelected = ScoreIndex.getSeq();
				currentSDIF = holoEditRef.gestionPistes.getTrack(trackSelected).getSDIFinst(sdifSelected);
				
				if (currentSDIF.getLinkedWaveForm() != null) return;
				
				if (!selIndex.isEmpty() && !selSDIFs.contains(currentSDIF))
				{
					selIndex = new Vector<Integer>();
					selSeqs = new Vector<HoloTraj>();
					selWaves = new Vector<WaveFormInstance>();
					selSDIFs = new Vector<SDIFdataInstance>();
					selTracks = new Vector<HoloTrack>();
				}
			}
			
			mouseCursor = convPosPt(posW, posH);
			oldCurrentX = mouseCursor.getDate();
			dX = 0;
			if (selIndex.isEmpty())
				draggedSeqBegin = true;
			else draggedMultiSeqBegin = true;
			if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				cutSeq = true;
		} else if (ScoreIndex.isSeqEnd()) {
			seqSelected = ScoreIndex.getSeq();
			currentSeq = holoEditRef.gestionPistes.getTrack(trackSelected).getHoloTraj(seqSelected);
			if (!selIndex.isEmpty() && !selSeqs.contains(currentSeq))
			{
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
			}
			mouseCursor = convPosPt(posW, posH);
			oldCurrentX = mouseCursor.getDate();
			dX = 0;
			if (selIndex.isEmpty()){
				draggedSeqEnd = true;				
			}else{ 
				draggedMultiSeqEnd = true;
			}
			if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				cutSeq = true;
		} else if (ScoreIndex.isDataEnd()) {
			sdifSelected = ScoreIndex.getSeq();
			currentSDIF = holoEditRef.gestionPistes.getTrack(trackSelected).getSDIFinst(sdifSelected);
			
			if (currentSDIF.getLinkedWaveForm() != null) return;
			
			if (!selIndex.isEmpty() && !selSDIFs.contains(currentSDIF))
			{
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
			}
			mouseCursor = convPosPt(posW, posH);
			oldCurrentX = mouseCursor.getDate();
			dX = 0;
			if (selIndex.isEmpty()){
				draggedSeqEnd = true;
				
			}else{ 
				draggedMultiSeqEnd = true;
			}
			if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
				cutSeq = true;
		}
		if(headerSelected != ScoreIndex.getNull())
		{
			int[] iA = ScoreIndex.decode(headerSelected);
			if(ScoreIndex.isHeader())
			{
				if((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3)
				{
					headerPopup.setNumber(iA[1]);
					popupVisible = true;
					headerPopup.show(this,e.getX(),e.getY());
				} else if(e.isAltDown() && e.getButton() != MouseEvent.BUTTON2) {
					if(holoEditRef.gestionPistes.getTrack(iA[1]).isVisible())
						holoEditRef.gestionPistes.viewAllButOne(iA[1]);
					else
						holoEditRef.gestionPistes.viewSolo(iA[1]);
				} else if(e.isControlDown() && e.getButton() != MouseEvent.BUTTON3) {
					if(holoEditRef.gestionPistes.getTrack(iA[1]).isVisible())
						holoEditRef.gestionPistes.viewAll();
					else
						holoEditRef.gestionPistes.viewNone();
				} else {
					if(iA[1] != holoEditRef.gestionPistes.getActiveTrackNb())
						holoEditRef.gestionPistes.selectTrack(iA[1]);
					else
						holoEditRef.gestionPistes.viewTrack(iA[1],!holoEditRef.gestionPistes.getTrack(iA[1]).isVisible());
				}
			}
			headerSelected = ScoreIndex.getNull();
		}
		if (scaleSelected != ScoreIndex.getNull())
		{
			if (e.getClickCount() >= 2)
			{
				holoEditRef.counterPanel.setBegAndEnd(0, totalTime);
			} else {
				ScoreIndex.decode(scaleSelected);
				if (ScoreIndex.isTimeScale())
				{
					draggedScale = true;
					mouseCursor = convPosPt(posW, posH);
					oldCurrentX = mouseCursor.getDate();
					int init = Ut.clipL((int) mouseCursor.getDate(), 0);
					holoEditRef.counterPanel.setBegAndEnd(init, init);
				}
				else if (ScoreIndex.isTimeSelBeg())
				{
					draggedSelBegin = true;
					mouseCursor = convPosPt(posW, posH);
				}
				else if (ScoreIndex.isTimeSelEnd())
				{
					draggedSelEnd = true;
					mouseCursor = convPosPt(posW, posH);
				}
				else if (ScoreIndex.isTimeSel())
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
					{
						draggedScale = true;
						mouseCursor = convPosPt(posW, posH);
						oldCurrentX = mouseCursor.getDate();
						int init = Ut.clipL((int) mouseCursor.getDate(), 0);
						holoEditRef.counterPanel.setBegAndEnd(init, init);
					}
					else
					{
						draggedSel = true;
						mouseCursor = convPosPt(posW, posH);
					}
				}
			}
		}
		if (scrollHSelected != ScoreIndex.getNull())
		{
			ScoreIndex.decode(scrollHSelected);
			int pad = (maxTime - minTime) / 2;
			if (ScoreIndex.isTimeScroll())
			{
				draggedTimeScroll = true;
				mouseCursor = convPosPt(posW, posH);
				mousex = e.getX() - W_TRACK_HEADER;
				mousey = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
			}
			else if (ScoreIndex.isTimeScrollLeft())
			{
				minTime -= pad;
				maxTime -= pad;
				if (minTime < 0)
				{
					minTime = 0;
					maxTime = 2 * pad;
				}
			}
			else if (ScoreIndex.isTimeScrollRight())
			{
				minTime += pad;
				maxTime += pad;
				if (maxTime > totalTime + ZOOM_OUT_MAX_PAD)
				{
					minTime = (int) (totalTime + ZOOM_OUT_MAX_PAD - 2 * pad);
					maxTime = (int) (totalTime + ZOOM_OUT_MAX_PAD);
				}
			}
		}

		if (scrollVSelected != ScoreIndex.getNull())
		{
			ScoreIndex.decode(scrollVSelected);
			int pad = (maxTrackH - minTrackH) / 2;
			if (ScoreIndex.isTrackScroll())
			{
				draggedTrackScroll = true;
				mouseCursor = convPosPt(posW, posH);
				mousex = e.getX() - W_TRACK_HEADER;
				mousey = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
			}
			else if (ScoreIndex.isTrackScrollDown())
			{
				minTrackH -= pad;
				maxTrackH -= pad;
				if (minTrackH < 0)
				{
					minTrackH = 0;
					maxTrackH = 2 * pad;
				}
			}
			else if (ScoreIndex.isTrackScrollUp())
			{
				minTrackH += pad;
				maxTrackH += pad;
				if (maxTrackH > tracksGlobalHeight)
				{
					minTrackH = (int) (tracksGlobalHeight - 2 * pad);
					maxTrackH = (int) (tracksGlobalHeight);
				}
			}
		}
		
		if (timeZoomButtons != ScoreIndex.getNull())
		{
			ScoreIndex.decode(timeZoomButtons);
			float currentZoomTime = (maxTime + minTime) / 2;
			float padMax = (maxTime - currentZoomTime);
			float padMin = (currentZoomTime - minTime);
			float zoomFactor = 1;
			
			if (ScoreIndex.isButtonPlus()) {
				zoomFactor = 0.5f;
			} else if (ScoreIndex.isButtonMinus()) {			
				zoomFactor = 2;
			}
			padMin *= zoomFactor;
			padMax *= zoomFactor;
			padMin = Ut.clipL(padMin, ZOOM_IN_MIN_PAD);
			padMax = Ut.clipL(padMax, ZOOM_IN_MIN_PAD);
			minTime = (int) Ut.clipL(currentZoomTime - padMin, 0);
			maxTime = (int) Ut.clip(currentZoomTime + padMax, 0, totalTime + ZOOM_OUT_MAX_PAD);
			mouseCursor = convPosPt(posW, posH);
			selected = ScoreIndex.getNull();
			query_one_select = true;
		}

		if (trackZoomButtons != ScoreIndex.getNull())
		{
			ScoreIndex.decode(trackZoomButtons);
			int diff = 0;
			diff = maxTrackH - minTrackH;
			int nbTrackToDraw = trackToDraw();
			if (ScoreIndex.isButtonPlus()) {
				if (nbTrackToDraw > 1){
						minTrackH += diff/nbTrackToDraw+2;
				}
			} else if (ScoreIndex.isButtonMinus()) {
				if (nbTrackToDraw < holoEditRef.gestionPistes.getNbTracks()) {
					if (nbTrackToDraw ==  holoEditRef.gestionPistes.getNbTracks()-1) {
						minTrackH = 0;
						maxTrackH = tracksGlobalHeight;
					} else if (minTrackH > 0) {
						minTrackH -= diff*(1/(float)(nbTrackToDraw+1));
					} else {
						maxTrackH += diff*(1/(float)(nbTrackToDraw+1));
					}
				}
			}
			maxTrackH = (int) Math.min(maxTrackH, tracksGlobalHeight);
			minTrackH = (int) Math.max(minTrackH, 0);
			display();
		}
		
		holoEditRef.room.display();
	}

	public void mouseReleased(MouseEvent e)
	{
		float posW = e.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
		boolean sort = false;
		if(dragCtrl)
		{
			if (draggedSeq)
			{
				mouseCursor = convPosPt(posW, posH);
				if(e.isControlDown())
					dX = 0;
				else
					dX = mouseCursor.getDate() - oldCurrentX;
				currentTrack = null;
				currentSeq = null;
				currentWave = null;
				currentSDIF = null;
				if (e.isShiftDown())
					dX *= 0.1;
				if (trackSelected == oldTrackSelected)
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
					if(seqSelected == -1 && sdifSelected == -1 && wavSelected != -1)
					{
						currentWave = currentTrack.getWave(wavSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							WaveFormInstance w2 = currentWave.dupliquer();
							w2.shiftDates((int) dX);
							currentTrack.addWave(w2);
						}
						else
							currentWave.shiftDates((int) dX);
					} else if(seqSelected != -1 && sdifSelected == -1 && wavSelected == -1){
						currentSeq = currentTrack.getHoloTraj(seqSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							HoloTraj ht2 = currentSeq.dupliquer();
							ht2.shiftDates((int) dX);
							currentTrack.addTraj(ht2);
						}
						else
							currentSeq.shiftDates((int) dX);
					} else if(seqSelected == -1 && sdifSelected != -1 && wavSelected == -1){
						currentSDIF = currentTrack.getSDIFinst(sdifSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							SDIFdataInstance sdif2 = currentSDIF.dupliquer();
							sdif2.shiftDates((int) dX);
							currentTrack.addSDIF(sdif2);
						}
						else
							currentSDIF.shiftDates((int) dX);
					}
					currentTrack.update();
				} else {
					holoEditRef.gestionPistes.StoreToUndoAll();
					if (wavSelected != -1 && seqSelected == -1 && sdifSelected == -1)
					{
						currentWave = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getWave(wavSelected);
						currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
						otherTrack = holoEditRef.gestionPistes.getTrack(oldTrackSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							WaveFormInstance w2 = currentWave.dupliquer();
							w2.shiftDates((int) dX);
							currentTrack.addWave(w2);
						} else {
							currentWave.shiftDates((int) dX);
							otherTrack.removeWave(wavSelected);
							currentTrack.addWave(currentWave);
							otherTrack.update();
						}
					} else if(seqSelected != -1 && wavSelected == -1 && sdifSelected == -1)
					{
						currentSeq = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getHoloTraj(seqSelected);
						currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
						otherTrack = holoEditRef.gestionPistes.getTrack(oldTrackSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							HoloTraj ht2 = currentSeq.dupliquer();
							ht2.shiftDates((int) dX);
							currentTrack.addTraj(ht2, 0);
						} else {
							currentSeq.shiftDates((int) dX);
							otherTrack.removeTraj(seqSelected);
							currentTrack.addTraj(currentSeq, 0);
							otherTrack.update();
						}
					} else if (sdifSelected != -1 && seqSelected == -1 && wavSelected == -1)
					{
						currentSDIF = holoEditRef.gestionPistes.getTrack(oldTrackSelected).getSDIFinst(sdifSelected);
						currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
						otherTrack = holoEditRef.gestionPistes.getTrack(oldTrackSelected);
						if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						{
							SDIFdataInstance sdif2 = currentSDIF.dupliquer();
							sdif2.shiftDates((int) dX);
							currentTrack.addSDIF(sdif2);
						} else {
							currentSDIF.shiftDates((int) dX);
							otherTrack.removeSdif(sdifSelected);
							currentTrack.addSDIF(currentSDIF);
							otherTrack.update();
						}
					}
					if (holoEditRef.allTrackActive)
						holoEditRef.gestionPistes.selectTrack(trackSelected);
				}
				currentTrack.update();
				holoEditRef.counterPanel.setCompteur(0, currentTrack.getLastDate());
				if(wavSelected != -1 && seqSelected == -1 && sdifSelected == -1)
				{
					wavSelected = currentTrack.waves.indexOf(currentWave);
					selected = ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, wavSelected, ScoreIndex.WAVE_POLY);
				}
				else if(seqSelected != -1 && wavSelected == -1 && sdifSelected == -1)
				{
					seqSelected = currentTrack.trajs.indexOf(currentSeq);
					selected = ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, seqSelected, ScoreIndex.SEQ_POLY);
				}
				else if(sdifSelected != -1 && seqSelected == -1 && wavSelected == -1)
				{
					sdifSelected = currentTrack.sdifdataInstanceVector.indexOf(currentSDIF);
					selected = ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, sdifSelected, ScoreIndex.DATA_POLY);
				}
			} else if (draggedSeqBegin) {
				
				mouseCursor = convPosPt(posW, posH);
				dX = mouseCursor.getDate() - oldCurrentX;
				if (e.isShiftDown())
					dX *= 0.1;
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				currentSeq = currentTrack.getHoloTraj(seqSelected);
				currentSDIF = currentTrack.getSDIFinst(sdifSelected);
				
				if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1){
					if (ScoreIndex.isSeqBegin())
						currentSeq.cutBegin(currentSeq.getFirstDate() + (int) dX);
					else if (ScoreIndex.isDataBegin())
						currentSDIF.cutBegin(currentSDIF.getFirstDate() + (int) dX);						
				}else {
					if (ScoreIndex.isSeqBegin())
						currentSeq.moveBegin(currentSeq.getFirstDate() + (int) dX);
					else if (ScoreIndex.isDataBegin())
						currentSDIF.moveBegin(currentSDIF.getFirstDate() + (int) dX);
				}
				
				currentTrack.update();
				holoEditRef.counterPanel.setCompteur(0, currentTrack.getLastDate());
			} else if (draggedSeqEnd) {
				mouseCursor = convPosPt(posW, posH);
				dX = mouseCursor.getDate() - oldCurrentX;
				if (e.isShiftDown())
					dX *= 0.1;
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				currentSeq = currentTrack.getHoloTraj(seqSelected);
				currentSDIF = currentTrack.getSDIFinst(sdifSelected);
								
				if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1){
					if (ScoreIndex.isSeqEnd())
						currentSeq.cutEnd(currentSeq.getLastDate() + (int) dX);
					else if (ScoreIndex.isDataEnd())
						currentSDIF.cutEnd(currentSDIF.getLastDate() + (int) dX);						
				}else {
					if (ScoreIndex.isSeqEnd())
						currentSeq.moveEnd(currentSeq.getLastDate() + (int) dX);
					else if (ScoreIndex.isDataEnd())
						currentSDIF.moveEnd(currentSDIF.getLastDate() + (int) dX);
				}
				holoEditRef.counterPanel.setCompteur(0, currentTrack.getLastDate());
			} else if (draggedMultiSeq) {
				if(e.isControlDown())
					dX = 0;
				else
					dX = mouseCursor.getDate() - oldCurrentX;
				holoEditRef.gestionPistes.StoreToUndoAll();
				if(trackSelected == oldTrackSelected)
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
					{
						selSeqs = new Vector<HoloTraj>();
						selWaves = new Vector<WaveFormInstance>();
						selSDIFs = new Vector<SDIFdataInstance>();
						for (int i : selIndex)
						{
							int[] iA = ScoreIndex.decode(i);
							currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
							if(ScoreIndex.isSeq())
							{
								currentSeq = currentTrack.getHoloTraj(iA[2]).dupliquer();
								currentSeq.shiftDates((int) dX);
								currentTrack.addTraj(currentSeq);
								selSeqs.add(currentSeq);
							} else if(ScoreIndex.isWave()) {
								currentWave = currentTrack.getWave(iA[2]).dupliquer();
								currentWave.shiftDates((int) dX);
								currentTrack.addWave(currentWave);
								selWaves.add(currentWave);
							} else if(ScoreIndex.isData()) {
								currentSDIF = currentTrack.getSDIFinst(iA[2]).dupliquer();
								currentSDIF.shiftDates((int) dX);
								currentTrack.addSDIF(currentSDIF);
								selSDIFs.add(currentSDIF);
							}
						}
						sort = true;
						for(HoloTrack tk:selTracks)
							holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
					} else {
						for (HoloTraj ht : selSeqs)
							ht.shiftDates((int) dX);
						for (WaveFormInstance w : selWaves)
							w.shiftDates((int) dX);
						for (SDIFdataInstance s : selSDIFs)
							s.shiftDates((int) dX);
						for (HoloTrack tk : selTracks)
						{
							sort = sort || tk.update();
							holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
						}
					}
				} else {
					if (e.isAltDown())
					{
						selSeqs = new Vector<HoloTraj>();
						selWaves = new Vector<WaveFormInstance>();
						selSDIFs = new Vector<SDIFdataInstance>();
						selTracks = new Vector<HoloTrack>();
						HashMap<HoloTraj,HoloTrack> trajToAdd = new HashMap<HoloTraj,HoloTrack>();
						HashMap<WaveFormInstance,HoloTrack> waveToAdd = new HashMap<WaveFormInstance,HoloTrack>();
						HashMap<SDIFdataInstance,HoloTrack> sdifToAdd = new HashMap<SDIFdataInstance,HoloTrack>();
						for (int i : selIndex)
						{
							int[] iA = ScoreIndex.decode(i);
							currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
							int nTkNum = Ut.modabs(iA[1]+(trackSelected-oldTrackSelected),holoEditRef.gestionPistes.getNbTracks());
							otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
							if(!selTracks.contains(otherTrack))
								selTracks.add(otherTrack);
							if(ScoreIndex.isSeq())
							{
								currentSeq = currentTrack.getHoloTraj(iA[2]).dupliquer();
								currentSeq.shiftDates((int) dX);
								trajToAdd.put(currentSeq,otherTrack);
								selSeqs.add(currentSeq);
							} else if(ScoreIndex.isWave()) {
								currentWave = currentTrack.getWave(iA[2]).dupliquer();
								currentWave.shiftDates((int) dX);
								selWaves.add(currentWave);
								waveToAdd.put(currentWave,otherTrack);
							}  else if(ScoreIndex.isData()) {
								currentSDIF = currentTrack.getSDIFinst(iA[2]).dupliquer();
								currentSDIF.shiftDates((int) dX);
								selSDIFs.add(currentSDIF);
								sdifToAdd.put(currentSDIF,otherTrack);
							}
						}
						for(HoloTraj ht : selSeqs)
							trajToAdd.get(ht).addTraj(ht);
						for(WaveFormInstance w: selWaves)
							waveToAdd.get(w).addWave(w);
						for(SDIFdataInstance s: selSDIFs)
							sdifToAdd.get(s).addSDIF(s);
						sort = true;
						for(HoloTrack tk:selTracks)
							holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
					} else {
						selSeqs = new Vector<HoloTraj>();
						selWaves = new Vector<WaveFormInstance>();
						selSDIFs = new Vector<SDIFdataInstance>();
						selTracks = new Vector<HoloTrack>();
						HashMap<HoloTraj,HoloTrack> trajToRemove = new HashMap<HoloTraj,HoloTrack>();
						HashMap<WaveFormInstance,HoloTrack> waveToRemove = new HashMap<WaveFormInstance,HoloTrack>();
						HashMap<SDIFdataInstance,HoloTrack> sdifToRemove = new HashMap<SDIFdataInstance,HoloTrack>();
						for (int i : selIndex)
						{
							int[] iA = ScoreIndex.decode(i);
							currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
							int nTkNum = Ut.modabs(iA[1]+(trackSelected-oldTrackSelected),holoEditRef.gestionPistes.getNbTracks());
							otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
							if(!selTracks.contains(otherTrack))
								selTracks.add(otherTrack);
							if(ScoreIndex.isSeq())
							{
								currentSeq = currentTrack.getHoloTraj(iA[2]);
								currentSeq.shiftDates((int) dX);
								otherTrack.addTraj(currentSeq);
								selSeqs.add(currentSeq);
								trajToRemove.put(currentSeq,currentTrack);
							} else if(ScoreIndex.isWave()) {
								currentWave = currentTrack.getWave(iA[2]);
								currentWave.shiftDates((int) dX);
								otherTrack.addWave(currentWave);
								selWaves.add(currentWave);
								waveToRemove.put(currentWave,currentTrack);
							} else if(ScoreIndex.isData()) {
								currentSDIF = currentTrack.getSDIFinst(iA[2]);
								currentSDIF.shiftDates((int) dX);
								otherTrack.addSDIF(currentSDIF);
								selSDIFs.add(currentSDIF);
								sdifToRemove.put(currentSDIF,currentTrack);
							}
						}
						for(HoloTraj ht:selSeqs)
							trajToRemove.get(ht).trajs.remove(ht);
						for(WaveFormInstance w:selWaves)
							waveToRemove.get(w).waves.remove(w);
						for(SDIFdataInstance s:selSDIFs)
							sdifToRemove.get(s).sdifdataInstanceVector.remove(s);
						sort = true;
						for(HoloTrack tk:selTracks)
							holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
					}
				}
				selected = ScoreIndex.getNull();
				query_one_select = true;
			}
			else if (draggedMultiSeqBegin)
			{
				mouseCursor = convPosPt(posW, posH);
				dX = mouseCursor.getDate() - oldCurrentX;
				if (e.isShiftDown())
					dX *= 0.1;
				holoEditRef.gestionPistes.StoreToUndoAll();
				for (HoloTraj ht : selSeqs)
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						ht.cutBegin(ht.getFirstDate() + (int) dX);
					else ht.moveBegin(ht.getFirstDate() + (int) dX);
				}
				for (HoloTrack tk : selTracks)
				{
					holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
					sort = sort || tk.update();
				}
			}
			else if (draggedMultiSeqEnd)
			{
				mouseCursor = convPosPt(posW, posH);
				dX = mouseCursor.getDate() - oldCurrentX;
				if (e.isShiftDown())
					dX *= 0.1;
				holoEditRef.gestionPistes.StoreToUndoAll();
				for (HoloTraj ht : selSeqs)
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						ht.cutEnd(ht.getLastDate() + (int) dX);
					else ht.moveEnd(ht.getLastDate() + (int) dX);
				}

				for (SDIFdataInstance data : selSDIFs)
				{
					if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
						data.cutEnd(data.getLastDate() + (int) dX);
					else data.moveEnd(data.getLastDate() + (int) dX);
				}
				for (HoloTrack tk : selTracks)
				{
					holoEditRef.counterPanel.setCompteur(0, tk.getLastDate());
					sort = sort || tk.update();
				}
			}
			if (sort)
				rebuildSelIndex();
		}
		dragCtrl = false;
		draggedSeq = false;
		draggedSeqBegin = false;
		draggedSeqEnd = false;
		cutSeq = false;
		draggedMultiSeq = false;
		draggedMultiSeqBegin = false;
		draggedMultiSeqEnd = false;
		draggedSelZone = false;
		draggedSelMode = NO_SEL;
		draggedScale = false;
		draggedSel = false;
		draggedSelBegin = false;
		draggedSelEnd = false;
		draggedTimeScroll = false;
		draggedTrackScroll = false;
		popupVisible = false;
		scaleSelected = ScoreIndex.getNull();
		scrollHSelected = ScoreIndex.getNull();
		scrollVSelected = ScoreIndex.getNull();
		headerSelected = ScoreIndex.getNull();
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
		float posW = e.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
		dragCtrl = true;
		if (draggedSeq || draggedMultiSeq)
		{
			mouseCursor = convPosPt(posW, posH);
			if(!e.isControlDown())
			{
				dX = mouseCursor.getDate() - oldCurrentX;
				if (e.isShiftDown())
					dX *= 0.1;
			} else {
				dX = 0;
			}
			mousex = e.getX();
			mousey = e.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
			query_track_select = true;
		}
		else if (draggedSeqBegin || draggedSeqEnd || draggedMultiSeqBegin || draggedMultiSeqEnd)
		{
			mouseCursor = convPosPt(posW, posH);
			dX = mouseCursor.getDate() - oldCurrentX;
			if (e.isShiftDown())
				dX *= 0.1;
			cutSeq = (e.getModifiersEx() == (InputEvent.BUTTON1_DOWN_MASK+InputEvent.ALT_DOWN_MASK));//e.isAltDown() && e.getButton() == MouseEvent.BUTTON1;
		}
		else if (draggedScale)
		{
			mouseCursor = convPosPt(posW, posH);
			dX = mouseCursor.getDate() - oldCurrentX;
			if (dX > 0)
				holoEditRef.counterPanel.setCompteur(2, Ut.clipL((int) mouseCursor.getDate(), 0));
			else holoEditRef.counterPanel.setBegAndEnd(Ut.clipL((int) mouseCursor.getDate(), 0), Ut.clipL((int) oldCurrentX, 0));
		}
		else if (draggedSelBegin)
		{
			float oldX = mouseCursor.getDate();
			mouseCursor = convPosPt(posW, posH);
			dX = mouseCursor.getDate() - oldX;
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
				scaleSelected = ScoreIndex.OT_TIMESEL_END_IND;
			}
		}
		else if (draggedSelEnd)
		{
			float oldX = mouseCursor.getDate();
			mouseCursor = convPosPt(posW, posH);
			dX = mouseCursor.getDate() - oldX;
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
				scaleSelected = ScoreIndex.OT_TIMESEL_BEG_IND;
			}
		}
		else if (draggedSel)
		{
			float oldX = mouseCursor.getDate();
			mouseCursor = convPosPt(posW, posH);
			dX = mouseCursor.getDate() - oldX;
			if (e.isShiftDown())
				dX *= 0.1;
			holoEditRef.counterPanel.setBegAndEnd(Ut.clipL(holoEditRef.counterPanel.getDate(1) + (int) dX, 0), Ut.clipL(holoEditRef.counterPanel.getDate(2) + (int) dX, 0));
		}
		else if (draggedTimeScroll)
		{
			if (!e.isAltDown()){
				float padPerCent = (float) (posW - mousex) / tracksGlobalWidth;
				mouseCursor = convPosPt(posW, posH);
				int oldTime = (maxTime - minTime);
				float maxi = totalTime + ZOOM_OUT_MAX_PAD;
				float pad = maxi * padPerCent;
				minTime = minTime + (int) pad;
				maxTime = maxTime + (int) pad;
				if (minTime < 0)
				{
					minTime = 0;
					maxTime = oldTime;
				}
				else if (maxTime > maxi)
				{
					maxTime = (int) maxi;
					minTime = (int) maxi - oldTime;
				}
				mousex = posW;
			} else { // avec alt, on zoom en deplacant la souris sur les y (=zoom wheel)
				mouseCursor = convPosPt(posW, posH);
				float currentZoomTime = convPosW(posW);
				float padMax = (maxTime - currentZoomTime);
				float padMin = (currentZoomTime - minTime);
				float zoom_factor = (1 - ((float)(posH - mousey) / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)));
				padMin *= zoom_factor;
				padMax *= zoom_factor;
				padMin = Ut.clipL(padMin, ZOOM_IN_MIN_PAD);
				padMax = Ut.clipL(padMax, ZOOM_IN_MIN_PAD);
				minTime = (int) Ut.clipL(currentZoomTime - padMin, 0);
				maxTime = (int) Ut.clip(currentZoomTime + padMax, 0, totalTime + ZOOM_OUT_MAX_PAD);
				mouseCursor = convPosPt(posW, posH);
				selected = ScoreIndex.getNull();
				query_one_select = true;
				mousey = posH;
				display();
			}
		}else if (draggedTrackScroll)
		{
			if (!e.isAltDown()){
				int oldTrackH = (maxTrackH - minTrackH);
				minTrackH = minTrackH + (int) (posH - mousey);
				maxTrackH = maxTrackH + (int) (posH - mousey);
				int maxi = tracksGlobalHeight;
				if (minTrackH < 0)
				{
					minTrackH = 0;
					maxTrackH = oldTrackH;
				}
				else if (maxTrackH > maxi)
				{
					maxTrackH = maxi;
					minTrackH = maxi - oldTrackH;
				}
				mousey = posH;
			} else { // avec alt, on zoom en deplacant la souris sur les x
				float currentZoomTrack = convGlobalPosH(posH);
				float padMax = (maxTrackH - currentZoomTrack);
				float padMin = (currentZoomTrack - minTrackH);
				float zoom_factor = (1 - ((float)(mousex - posW) / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)));
				padMin *= zoom_factor;
				padMax *= zoom_factor;
				int limit = tracksGlobalHeight/(2*holoEditRef.gestionPistes.getNbTracks());
				padMin = Ut.clipL(padMin, limit);
				padMax = Ut.clipL(padMax, limit);
				minTrackH = (int) Ut.clipL(currentZoomTrack - padMin, 0);
				maxTrackH = (int) Ut.clip(currentZoomTrack + padMax, 0, tracksGlobalHeight);
				mouseCursor = convPosPt(posW, posH);
				selected = ScoreIndex.getNull();
				query_one_select = true;
				mousex = posW;
				display();
			}
		}
		else if (draggedSelZone)
		{
			if((Ut.MAC && e.isMetaDown()) || (e.isControlDown() && !Ut.MAC))
			{
				draggedSelMode = SEL_TRAJ;
			} else if(e.isShiftDown())
			{
				draggedSelMode = SEL_WAVE;
			} else {
				draggedSelMode = SEL_BOTH;
			}
			mousex2 = e.getX();
//			mousey2 = height - e.getY();
			mousex = e.getX();
			mousey = e.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
			selPt2 = new ScorePoint(convPosW(posW), trackSelected);
			query_sel_select = true;
			query_track_select = true;
		}
		holoEditRef.room.display();
	}

	public void mouseMoved(MouseEvent e)
	{
		if(popupVisible) return;
		query_one_select = true;
		selected = ScoreIndex.getNull();
		float posW = e.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
		mousex = e.getX();
		mousey = e.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		mouseCursor = convPosPt(posW, posH);
		display();
		//Ut.barMenu.update();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		float posW = e.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - (e.getY() - H_TIME_SCALE);
		mousex = e.getX();
		mousey = e.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		if((e.isMetaDown() && Ut.MAC) || (e.isControlDown() && !Ut.MAC))
		{
			int pad = (maxTime - minTime) / 2;
			float num = (float)e.getUnitsToScroll()/100;// * mainRef.scrollSpeed / (e.isShiftDown() ? 10 : 100);
			minTime = minTime + (int)(pad * num);
			maxTime = maxTime + (int)(pad * num);
			if (minTime < 0)
			{
				minTime = 0;
				maxTime = 2 * pad;
			}
			if (maxTime > totalTime + ZOOM_OUT_MAX_PAD)
			{
				minTime = (int) (totalTime + ZOOM_OUT_MAX_PAD - 2 * pad);
				maxTime = (int) (totalTime + ZOOM_OUT_MAX_PAD);
			}
		} else {
			float currentZoomTime = convPosW(posW);
			float padMax = (maxTime - currentZoomTime);
			float padMin = (currentZoomTime - minTime);
			float zoom_factor = (1 - (e.getUnitsToScroll() / (e.isShiftDown() ? 10 * holoEditRef.scrollSpeed : holoEditRef.scrollSpeed)));
			padMin *= zoom_factor;
			padMax *= zoom_factor;
			padMin = Ut.clipL(padMin, ZOOM_IN_MIN_PAD);
			padMax = Ut.clipL(padMax, ZOOM_IN_MIN_PAD);
			minTime = (int) Ut.clipL(currentZoomTime - padMin, 0);
			maxTime = (int) Ut.clip(currentZoomTime + padMax, 0, totalTime + ZOOM_OUT_MAX_PAD);
		}
		mouseCursor = convPosPt(posW, posH);
		selected = ScoreIndex.getNull();
		query_one_select = true;
		display();
	}
	

	public void treatSeqIndex()
	{
		IntegerVector sorting = new IntegerVector(selIndex);
		sorting.sort();
		selIndex = sorting.getVector();
		if (selIndex.isEmpty())
			return;
		selSeqs = new Vector<HoloTraj>();
		selWaves = new Vector<WaveFormInstance>();
		selSDIFs = new Vector<SDIFdataInstance>();
		selTracks = new Vector<HoloTrack>();
		for (int i : selIndex)
		{
			int[] iA = ScoreIndex.decode(i);
			currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
			if(ScoreIndex.isSeq())
			{
				currentSeq = currentTrack.getHoloTraj(iA[2]);
				if (!selSeqs.contains(currentSeq))
					selSeqs.add(currentSeq);
			} else if(ScoreIndex.isWave())
			{
				currentWave = currentTrack.getWave(iA[2]);
				if (!selWaves.contains(currentWave))
					selWaves.add(currentWave);
			} else if(ScoreIndex.isData())
			{
				currentSDIF = currentTrack.getSDIFinst(iA[2]);
				if (!selSDIFs.contains(currentSDIF))
					selSDIFs.add(currentSDIF);
			}
			if (!selTracks.contains(currentTrack))
				selTracks.add(currentTrack);
		}
	}

	private void rebuildSelIndex()
	{
		selIndex = new Vector<Integer>(selSeqs.size()+selWaves.size());
		for (int i = 0, last = holoEditRef.gestionPistes.getNbTracks(); i < last; i++)
		{
			currentTrack = holoEditRef.gestionPistes.getTrack(i);
			if (selTracks.contains(currentTrack))
			{	
				for (int k = 0, last2 = currentTrack.trajs.size(); k < last2; k++)
				{
					currentSeq = currentTrack.trajs.get(k);
					if (selSeqs.contains(currentSeq))
						selIndex.add(ScoreIndex.encode(ScoreIndex.TYPE_TK, i, k, ScoreIndex.SEQ_POLY));
				}
				for (int k = 0, last2 = currentTrack.waves.size(); k < last2; k++)
				{
					currentWave = currentTrack.waves.get(k);
					if (selWaves.contains(currentSeq))
						selIndex.add(ScoreIndex.encode(ScoreIndex.TYPE_TK, i, k, ScoreIndex.WAVE_POLY));
				}
				for (int k = 0, last2 = currentTrack.sdifdataInstanceVector.size(); k < last2; k++)
				{
					currentSDIF = currentTrack.sdifdataInstanceVector.get(k);
					if (selSDIFs.contains(currentSeq))
						selIndex.add(ScoreIndex.encode(ScoreIndex.TYPE_TK, i, k, ScoreIndex.DATA_POLY));
				}				
			}
		}
	}

	private ScorePoint convPosPt(float posW, float posH)
	{
		return new ScorePoint(convPosW(posW), convPosH(posH));
	}

	private float convPosW(float posW)
	{
		return Ut.clipL(posW / tracksGlobalWidth * (maxTime - minTime) + minTime, 0);
	}

	private float convPosH(float posH)
	{
		if (trackSelected != -1 && trackSelected < size.size())
		{
			float HH = size.get(trackSelected) * tracksGlobalHeight;
			for (int i = size.size() - 1; i > trackSelected; i--)
				posH -= size.get(i) * tracksGlobalHeight;
			return Ut.clip(posH / HH * (maxY - minY) + minY, minY, maxY);
		}
		return 0;
	}
	
	private float convGlobalPosH(float posH)
	{
		return Ut.clipL(posH / tracksGlobalHeight * (maxTrackH - minTrackH) + minTrackH, 0);
	}
	public class ScorePoint extends Point2D.Float
	{
		ScorePoint(float x, float y)
		{
			super(x, y);
		}

		float getDate()
		{
			return (float) getX();
		}

		float getVal()
		{
			return (float) getY();
		}
	}

	public void setMaxTime(int max)
	{
		minTime = 0;
		maxTime = max;
	}
	
	public void initVars()
	{
		query_one_select = false;
		query_track_select = false;
		query_sel_select = false;
		selected = ScoreIndex.getNull();
		selIndex = new Vector<Integer>();
		selSeqs = new Vector<HoloTraj>();
		selWaves = new Vector<WaveFormInstance>();
		selSDIFs = new Vector<SDIFdataInstance>();
		selTracks = new Vector<HoloTrack>();
		scaleSelected = ScoreIndex.getNull();
		scrollHSelected = ScoreIndex.getNull();
		scrollVSelected = ScoreIndex.getNull();
		wavSelected = -1;
		sdifSelected = -1;
		seqSelected = -1;
		draggedSeq = false;
		draggedSeqBegin = false;
		draggedSeqEnd = false;
		draggedMultiSeq = false;
		draggedMultiSeqBegin = false;
		draggedMultiSeqEnd = false;
		draggedScale = false;
		draggedTimeScroll = false;
		draggedTrackScroll = false;
		draggedSel = false;
		draggedSelBegin = false;
		draggedSelEnd = false;
		draggedSelZone = false;
		draggedSelMode = NO_SEL;
		draggedWave = false;
		draggedData= false;
		cutSeq = false;
		display();
	}

	public void joinTrack()
	{
		holoEditRef.gestionPistes.StoreToUndo(trackSelected);
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		if (currentTrack.isLocked() || currentTrack.isEmpty())
			return;
		if (currentTrack.trajs.isEmpty())
			return;
		HoloTraj last = currentTrack.trajs.lastElement();
		for (int i = currentTrack.trajs.size() - 2; i >= 0; i--)
		{
			currentSeq = currentTrack.trajs.get(i);
			last.join(currentSeq);
			currentTrack.trajs.remove(currentSeq);
		}
		currentTrack.update();
	}

	public void joinMultiSeq()
	{
		holoEditRef.gestionPistes.StoreToUndoAll();
		int otkNum = -2, tkNum = -1;
		HoloTraj last = null;
		currentTrack = null;
		currentSeq = null;
		for (int k = selIndex.size() - 1; k >= 0; k--)
		{
			int[] iA = ScoreIndex.decode(selIndex.get(k));
			if(ScoreIndex.isSeq())
			{
				tkNum = iA[1];
				if (tkNum != otkNum)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(tkNum);
					last = currentTrack.trajs.get(iA[2]);
				}
				else
				{
					currentSeq = currentTrack.trajs.get(iA[2]);
					last.join(currentSeq);
					currentTrack.trajs.remove(currentSeq);
					currentTrack.update();
				}
				otkNum = tkNum;
			}
		}
		selIndex = new Vector<Integer>();
		selSeqs = new Vector<HoloTraj>();
		selWaves = new Vector<WaveFormInstance>();
		selSDIFs = new Vector<SDIFdataInstance>();
		selTracks = new Vector<HoloTrack>();
	}

	public void trimTrack()
	{
		holoEditRef.gestionPistes.StoreToUndo(trackSelected);
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		if (!currentTrack.isLocked())
		{
			currentTrack.crop(holoEditRef.counterPanel.getDate(1), holoEditRef.counterPanel.getDate(2), true);
			currentTrack.update();
		}
	}

	public void trimMultiSeq()
	{
		holoEditRef.gestionPistes.StoreToUndoAll();
		int beg = holoEditRef.counterPanel.getDate(1);
		int end = holoEditRef.counterPanel.getDate(2);
		for (HoloTraj ht : selSeqs)
			ht.crop(beg, end);
		for (HoloTrack tk : selTracks)
			tk.update();
	}

	public void trimSeq()
	{
		int[] iA = ScoreIndex.decode(selected);
		if(ScoreIndex.isSeq())
		{
			currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
			currentSeq = currentTrack.getHoloTraj(iA[2]);
			holoEditRef.gestionPistes.StoreToUndo(iA[1]);
			currentSeq.crop(holoEditRef.counterPanel.getDate(1), holoEditRef.counterPanel.getDate(2));
			currentTrack.update();
		}
	}

	public void eraseTrack()
	{
		holoEditRef.gestionPistes.StoreToUndo(trackSelected);
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		currentTrack.trajs.clear();
		currentTrack.waves.clear();
		currentTrack.sdifdataInstanceVector.clear();
		selIndex = new Vector<Integer>();
		selSeqs = new Vector<HoloTraj>();
		selWaves = new Vector<WaveFormInstance>();
		selSDIFs = new Vector<SDIFdataInstance>();
		selTracks = new Vector<HoloTrack>();
		Ut.barMenu.update();
	}

	public void eraseMultiSeq()
	{
		holoEditRef.gestionPistes.StoreToUndoAll();
		for (int k = selIndex.size() - 1; k >= 0; k--)
		{
			int[] iA = ScoreIndex.decode(selIndex.get(k));
			currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
			if(ScoreIndex.isSeq())
				currentTrack.trajs.remove(iA[2]);
			else if(ScoreIndex.isWave()){
				WaveFormInstance wav = currentTrack.waves.get(iA[2]);
				if (wav.getSDIFvector() != null)
					for (SDIFdataInstance sdif : wav.getSDIFvector())
						currentTrack.sdifdataInstanceVector.remove(sdif);
				currentTrack.waves.remove(iA[2]);
			}else if(ScoreIndex.isData())
				currentTrack.sdifdataInstanceVector.remove(iA[2]);
		}
		for (HoloTrack tk : selTracks)
			tk.update();
		selIndex = new Vector<Integer>();
		selSeqs = new Vector<HoloTraj>();
		selWaves = new Vector<WaveFormInstance>();
		selSDIFs = new Vector<SDIFdataInstance>();
		selTracks = new Vector<HoloTrack>();
		Ut.barMenu.update();
	}

	public void eraseSeq()
	{
		holoEditRef.gestionPistes.StoreToUndo(trackSelected);
		int[] iA = ScoreIndex.decode(selected);
		currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
		if(ScoreIndex.isSeq())
			currentTrack.trajs.remove(iA[2]);
		else if(ScoreIndex.isWave()) {
			WaveFormInstance wav = currentTrack.waves.get(iA[2]);
			if (wav.getSDIFvector() != null)
					for (SDIFdataInstance sdif : wav.getSDIFvector())
						currentTrack.sdifdataInstanceVector.remove(sdif);
			currentTrack.waves.remove(iA[2]);
		}else if(ScoreIndex.isData())
			currentTrack.sdifdataInstanceVector.remove(iA[2]);
		currentTrack.update();
		selected = ScoreIndex.getNull();
		Ut.barMenu.update();
	}

	public void copyTrack()
	{
		holoEditRef.gestionPistes.copySeqs = new HashMap<Integer, HoloTraj>();
		holoEditRef.gestionPistes.copyWaves = new HashMap<Integer, WaveFormInstance>();
		holoEditRef.gestionPistes.copySDIFs = new HashMap<Integer, SDIFdataInstance>();
		holoEditRef.gestionPistes.copyTrack = new HoloTrack();
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		int min = currentTrack.getFirstDate();
		if(!currentTrack.waves.isEmpty())
			min = Ut.min(currentTrack.waves.firstElement().getFirstDate(), min);
		if(!currentTrack.waves.isEmpty())
			min = Ut.min(currentTrack.sdifdataInstanceVector.firstElement().getFirstDate(), min);
		for (int i = 0, last = currentTrack.trajs.size(); i < last; i++)
		{
			currentSeq = currentTrack.trajs.get(i).dupliquer();
			currentSeq.shiftDates(-min);
			holoEditRef.gestionPistes.copySeqs.put(ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, i, ScoreIndex.SEQ_POLY), currentSeq);
		}
		for (int i = 0, last = currentTrack.waves.size(); i < last; i++)
		{
			currentWave = currentTrack.waves.get(i).dupliquer();
			currentWave.shiftDates(-min);
			holoEditRef.gestionPistes.copyWaves.put(ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, i, ScoreIndex.WAVE_POLY), currentWave);
		}
		for (int i = 0, last = currentTrack.sdifdataInstanceVector.size(); i < last; i++)
		{
			currentSDIF = currentTrack.sdifdataInstanceVector.get(i).dupliquer();
			currentSDIF.shiftDates(-min);
			holoEditRef.gestionPistes.copySDIFs.put(ScoreIndex.encode(ScoreIndex.TYPE_TK, trackSelected, i, ScoreIndex.DATA_POLY), currentSDIF);
		}
		holoEditRef.gestionPistes.selectTrack(trackSelected);
		oldActiveTrack = holoEditRef.gestionPistes.getActiveTrackNb();
//		oldTrackSelected = trackSelected;
		Ut.barMenu.update();
	}

	public void copyMultiSeq()
	{
		holoEditRef.gestionPistes.copySeqs = new HashMap<Integer, HoloTraj>();
		holoEditRef.gestionPistes.copyTrack = new HoloTrack();
		holoEditRef.gestionPistes.copyWaves = new HashMap<Integer, WaveFormInstance>();
		holoEditRef.gestionPistes.copySDIFs = new HashMap<Integer, SDIFdataInstance>();
		int min = 100000000;
		int minTk = 1000;
		for (HoloTraj ht : selSeqs)
			min = Ut.min(ht.getFirstDate(), min);
		for (WaveFormInstance w : selWaves)
			min = Ut.min(w.getFirstDate(), min);
		for (SDIFdataInstance s : selSDIFs)
				min = Ut.min(s.getFirstDate(), min);
		for (int i : holoEditRef.score.selIndex)
		{
			int[] iA = ScoreIndex.decode(i);
			minTk = Ut.min(minTk,iA[1]);
			if(ScoreIndex.isSeq())
			{
				currentSeq = holoEditRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]).dupliquer();
				currentSeq.shiftDates(-min);
				holoEditRef.gestionPistes.copySeqs.put(i, currentSeq);
			} else if(ScoreIndex.isWave()){
				currentWave = holoEditRef.gestionPistes.getTrack(iA[1]).getWave(iA[2]).dupliquer();
				currentWave.shiftDates(-min);
				holoEditRef.gestionPistes.copyWaves.put(i, currentWave);
			} else if(ScoreIndex.isData()){
				currentSDIF = holoEditRef.gestionPistes.getTrack(iA[1]).getSDIFinst(iA[2]).dupliquer();
				currentSDIF.shiftDates(-min);
				holoEditRef.gestionPistes.copySDIFs.put(i, currentSDIF);
			}
		}
		System.out.println("MinTk "+minTk);
		holoEditRef.gestionPistes.selectTrack(minTk);
//		oldTrackSelected = -1;
		oldActiveTrack = holoEditRef.gestionPistes.getActiveTrackNb();
		Ut.barMenu.update();
	}

	public void copySeq()
	{
		holoEditRef.gestionPistes.copySeqs = new HashMap<Integer, HoloTraj>();
		holoEditRef.gestionPistes.copyWaves = new HashMap<Integer, WaveFormInstance>();
		holoEditRef.gestionPistes.copySDIFs = new HashMap<Integer, SDIFdataInstance>();
		holoEditRef.gestionPistes.copyTrack = new HoloTrack();
		int[] iA = ScoreIndex.decode(selected);
		if(ScoreIndex.isSeq())
		{
			currentSeq = holoEditRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]).dupliquer();
			currentSeq.shiftDates(-currentSeq.getFirstDate());
			holoEditRef.gestionPistes.copySeqs.put(selected, currentSeq);
		} else if(ScoreIndex.isWave())
		{
			currentWave = holoEditRef.gestionPistes.getTrack(iA[1]).getWave(iA[2]).dupliquer();
			currentWave.shiftDates(-currentWave.getFirstDate());
			holoEditRef.gestionPistes.copyWaves.put(selected, currentWave);
		} else if(ScoreIndex.isData())
		{
			currentSDIF = holoEditRef.gestionPistes.getTrack(iA[1]).getSDIFinst(iA[2]).dupliquer();
			currentSDIF.shiftDates(-currentSDIF.getFirstDate());
			holoEditRef.gestionPistes.copySDIFs.put(selected, currentSDIF);
		}
		holoEditRef.gestionPistes.selectTrack(iA[1]);
		oldActiveTrack = holoEditRef.gestionPistes.getActiveTrackNb();
//		oldTrackSelected = iA[1];
		Ut.barMenu.update();
	}

	public void paste(int atTime)
	{
		holoEditRef.gestionPistes.StoreToUndoAll();
		if(/*trackSelected == oldTrackSelected || */oldActiveTrack == holoEditRef.gestionPistes.getActiveTrackNb())
		{
			int maxDate = 0;
			int maxEnd = 0;
			if(!holoEditRef.gestionPistes.copySeqs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySeqs.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copySeqs.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					int date = currentTrack.getLastDate();
					if (currentTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copyWaves.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copyWaves.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copyWaves.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					int date = 0;
					if(!currentTrack.waves.isEmpty())
						date = currentTrack.waves.lastElement().getLastDate();
					if (currentTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copySDIFs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySDIFs.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copySDIFs.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					int date = 0;
					if(!currentTrack.sdifdataInstanceVector.isEmpty())
						date = currentTrack.sdifdataInstanceVector.lastElement().getLastDate();
					if (currentTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copySeqs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySeqs.keySet());
				Vector<HoloTraj> tjs = new Vector<HoloTraj>(holoEditRef.gestionPistes.copySeqs.values());
				for (HoloTraj ht : tjs)
					maxEnd = Ut.max(ht.getFirstDate() + ht.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copySeqs.size(); i < last; i++)
				{
					currentSeq = tjs.get(i);
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentTrack.addTraj(currentSeq.dupliquer(), maxDate + currentSeq.getFirstDate());
					currentTrack.update();
				}
			}
			if(!holoEditRef.gestionPistes.copyWaves.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copyWaves.keySet());
				Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(holoEditRef.gestionPistes.copyWaves.values());
				for (WaveFormInstance w : waves)
					maxEnd = Ut.max(w.getFirstDate() + w.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copyWaves.size(); i < last; i++)
				{
					currentWave = waves.get(i).dupliquer();
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentWave.setBegTime(maxDate + currentWave.getFirstDate());
					currentTrack.addWave(currentWave);
					currentTrack.update();
				}
			}
			if(!holoEditRef.gestionPistes.copySDIFs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySDIFs.keySet());
				Vector<SDIFdataInstance> sdifdataInstanceVector = new Vector<SDIFdataInstance>(holoEditRef.gestionPistes.copySDIFs.values());
				for (SDIFdataInstance s : sdifdataInstanceVector)
					maxEnd = Ut.max(s.getFirstDate() + s.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copySDIFs.size(); i < last; i++)
				{
					currentSDIF = sdifdataInstanceVector.get(i).dupliquer();
					int[] iA = ScoreIndex.decode(keys.get(i));
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentSDIF.setBegTime(maxDate + currentSDIF.getFirstDate());
					currentTrack.addSDIF(currentSDIF);
					currentTrack.update();
				}
			}
			holoEditRef.counterPanel.setCompteur(2, maxDate + maxEnd);
		} else {
			int deltaTk;
//			if(oldTrackSelected == -1)
				deltaTk = holoEditRef.gestionPistes.getActiveTrackNb() - oldActiveTrack;
//			else
//				deltaTk = trackSelected - oldTrackSelected;
			int maxDate = 0;
			int maxEnd = 0;
			if(!holoEditRef.gestionPistes.copySeqs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySeqs.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copySeqs.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					int date = otherTrack.getLastDate();
					if (otherTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copyWaves.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copyWaves.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copyWaves.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					int date = 0;
					if(!otherTrack.waves.isEmpty())
						date = otherTrack.waves.lastElement().getLastDate();
					if (otherTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copySDIFs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySDIFs.keySet());
				int beg = atTime != -1 ? atTime : holoEditRef.counterPanel.getDate(1);
				for (int i = 0, last = holoEditRef.gestionPistes.copySDIFs.size(); i < last; i++)
				{
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					int date = 0;
					if(!otherTrack.sdifdataInstanceVector.isEmpty())
						date = otherTrack.sdifdataInstanceVector.lastElement().getLastDate();
					if (otherTrack.isEmpty() || date < beg)
						maxDate = Ut.max(beg, maxDate);
					else maxDate = Ut.max(date, maxDate);
				}
			}
			if(!holoEditRef.gestionPistes.copySeqs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySeqs.keySet());
				Vector<HoloTraj> tjs = new Vector<HoloTraj>(holoEditRef.gestionPistes.copySeqs.values());
				for (HoloTraj ht : tjs)
					maxEnd = Ut.max(ht.getFirstDate() + ht.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copySeqs.size(); i < last; i++)
				{
					currentSeq = tjs.get(i);
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					otherTrack.addTraj(currentSeq.dupliquer(), maxDate + currentSeq.getFirstDate());
					otherTrack.update();
				}
			}
			if(!holoEditRef.gestionPistes.copyWaves.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copyWaves.keySet());
				Vector<WaveFormInstance> waves = new Vector<WaveFormInstance>(holoEditRef.gestionPistes.copyWaves.values());
				for (WaveFormInstance w : waves)
					maxEnd = Ut.max(w.getFirstDate() + w.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copyWaves.size(); i < last; i++)
				{
					currentWave = waves.get(i).dupliquer();
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					currentWave.setBegTime(maxDate + currentWave.getFirstDate());
					otherTrack.addWave(currentWave);
					otherTrack.update();
				}
			}
			if(!holoEditRef.gestionPistes.copySDIFs.isEmpty())
			{
				Vector<Integer> keys = new Vector<Integer>(holoEditRef.gestionPistes.copySDIFs.keySet());
				Vector<SDIFdataInstance> sdifs = new Vector<SDIFdataInstance>(holoEditRef.gestionPistes.copySDIFs.values());
				for (SDIFdataInstance s : sdifs)
					maxEnd = Ut.max(s.getFirstDate() + s.getDuration(), maxEnd);
				for (int i = 0, last = holoEditRef.gestionPistes.copySDIFs.size(); i < last; i++)
				{
					currentSDIF = sdifs.get(i).dupliquer();
					int[] iA = ScoreIndex.decode(keys.get(i));
					int nTkNum = Ut.modabs(iA[1]+(deltaTk),holoEditRef.gestionPistes.getNbTracks());
					otherTrack = holoEditRef.gestionPistes.getTrack(nTkNum);
					currentSDIF.setBegTime(maxDate + currentSDIF.getFirstDate());
					otherTrack.addSDIF(currentSDIF);
					otherTrack.update();
				}
			}
			holoEditRef.counterPanel.setCompteur(2, maxDate + maxEnd);
		}
	}

	public int treatTimeString(String timeStr)
	{
		try
		{
			String[] strA = timeStr.split(":");
			switch(strA.length)
			{
			case 4 :
				// h:mn:sec:ms
				return Integer.parseInt(strA[0])*3600000+Integer.parseInt(strA[1])*60000+Integer.parseInt(strA[2])*1000+Integer.parseInt(strA[3]);
			case 3 :
				// mn:sec:ms
				return Integer.parseInt(strA[0])*60000+Integer.parseInt(strA[1])*1000+Integer.parseInt(strA[2].substring(0,2));
			case 2 :
				// mn:sec,ms
				return (int)(Integer.parseInt(strA[0])*60000+Float.parseFloat(strA[1])*1000);
			case 1 :
				// sec,ms
				return (int)(Float.parseFloat(strA[0])*1000);
			default :
			case 0 :
				return -1;
			}
		} catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(this, "\""+timeStr + "\" can't be converted to a time (h:mn:sec:ms), operation aborted", "Error", JOptionPane.ERROR_MESSAGE);
			return -1;
		}
	}
	
	public void moveTrack()
	{
		TimeDialog timeDialog = new TimeDialog(this, "Move this track to time (h:mn:sec:ms)");
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setVisible(true);
        int[] result = timeDialog.getNumbers();
		if(result != null)
		{
			int newTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
			//int newTime = treatTimeString(result);
			if(newTime != -1)
			{
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				int oldTime;
				if(!currentTrack.waves.isEmpty() && !currentTrack.sdifdataInstanceVector.isEmpty()){
					int min = Ut.min(currentTrack.sdifdataInstanceVector.firstElement().getFirstDate(),currentTrack.waves.firstElement().getFirstDate());
					oldTime = Ut.min(currentTrack.getFirstDate(),min);
				}else if(!currentTrack.waves.isEmpty())
					oldTime = Ut.min(currentTrack.getFirstDate(),currentTrack.waves.firstElement().getFirstDate());
				else if(!currentTrack.sdifdataInstanceVector.isEmpty())
					oldTime = Ut.min(currentTrack.getFirstDate(),currentTrack.sdifdataInstanceVector.firstElement().getFirstDate());
				else oldTime = currentTrack.getFirstDate();
				
				for (HoloTraj ht : currentTrack.trajs)
					ht.shiftDates(newTime - oldTime);
				for (WaveFormInstance w : currentTrack.waves)
					w.shiftDates(newTime - oldTime);
			
			 	for (SDIFdataInstance sdif : currentTrack.sdifdataInstanceVector)
			 		if (sdif.getLinkedWaveForm()==null) // sinon l'appel et fait par la wav
						sdif.shiftDates(newTime - oldTime);
				currentTrack.update();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
				Ut.barMenu.update();
			}
		}
	}

	public void moveMultiSeq()
	{
		TimeDialog timeDialog = new TimeDialog(this, "Move this selection to time (h:mn:sec:ms)");
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setVisible(true);
        int[] result = timeDialog.getNumbers();
		if(result != null)
		{
			int newTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
			if(newTime != -1)
			{
				holoEditRef.gestionPistes.StoreToUndoAll();
				int oldTime = 1000000;
				for (HoloTraj ht : selSeqs)
					oldTime = Ut.min(ht.getFirstDate(), oldTime);
				for (WaveFormInstance w : selWaves)
					oldTime = Ut.min(w.getFirstDate(), oldTime);
				for (SDIFdataInstance sdif : selSDIFs)
					oldTime = Ut.min(sdif.getFirstDate(), oldTime);
				
				for (HoloTraj ht : selSeqs)
					ht.shiftDates(newTime - oldTime);
				for (WaveFormInstance w : selWaves)
					w.shiftDates(newTime - oldTime);
				for (SDIFdataInstance sdif : selSDIFs)
					sdif.shiftDates(newTime - oldTime);
				
				for (HoloTrack tk : selTracks)
					tk.update();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
				Ut.barMenu.update();
			}
		}
	}

	public void moveSeq()
	{
		int[] iA = ScoreIndex.decode(selected);
		if(ScoreIndex.isSeq())
		{
			TimeDialog timeDialog = new TimeDialog(this, "Move this trajectory to time (h:mn:sec:ms)");
	        timeDialog.setLocationRelativeTo(this);
	        timeDialog.setVisible(true);
	        int[] result = timeDialog.getNumbers();
			if(result != null)
			{
				int newTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
				if(newTime != -1)
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentSeq = currentTrack.getHoloTraj(iA[2]);
					currentSeq.shiftDates(newTime - currentSeq.getFirstDate());
					currentTrack.update();
					selected = ScoreIndex.getNull();
					Ut.barMenu.update();
				}
			}
		} else if(ScoreIndex.isWave()) {
			TimeDialog timeDialog = new TimeDialog(this, "Move this waveform to time (h:mn:sec:ms)");
	        timeDialog.setLocationRelativeTo(this);
	        timeDialog.setVisible(true);
	        int[] result = timeDialog.getNumbers();
			if(result != null)
			{
				int newTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
				if(newTime != -1)
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentWave = currentTrack.getWave(iA[2]);
					currentWave.shiftDates(newTime - currentWave.getFirstDate());
					currentTrack.update();
					selected = ScoreIndex.getNull();
					Ut.barMenu.update();
				}
			}
		} else if(ScoreIndex.isData()) {
			TimeDialog timeDialog = new TimeDialog(this, "Move this data to time (h:mn:sec:ms)");
	        timeDialog.setLocationRelativeTo(this);
	        timeDialog.setVisible(true);
	        int[] result = timeDialog.getNumbers();
			if(result != null)
			{
				int newTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
				if(newTime != -1)
				{
					holoEditRef.gestionPistes.StoreToUndo(trackSelected);
					currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
					currentSDIF = currentTrack.getSDIFinst(iA[2]);
					currentSDIF.shiftDates(newTime - currentSDIF.getFirstDate());
					currentTrack.update();
					selected = ScoreIndex.getNull();
					Ut.barMenu.update();
				}
			}
		}
	}

	public void cutAtTrack()
	{
		TimeDialog timeDialog = new TimeDialog(this, "Cut this track to time (h:mn:sec:cs)");
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setVisible(true);
        int[] result = timeDialog.getNumbers();
		if(result != null)
		{
			int cutTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
			if(cutTime != -1)
			{
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				currentTrack.cutAt(cutTime);
				currentTrack.update();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
				Ut.barMenu.update();
			}
		}
	}
	
	public void cutAtMultiSeq()
	{
		TimeDialog timeDialog = new TimeDialog(this, "Cut this selection to time (h:mn:sec:cs)");
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setVisible(true);
        int[] result = timeDialog.getNumbers();
		if(result != null)
		{
			int cutTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
			if(cutTime != -1)
			{
				holoEditRef.gestionPistes.StoreToUndoAll();
				for (HoloTrack tk : selTracks)
				{
					tk.cutAt(cutTime);
					tk.update();
				}
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selSDIFs = new Vector<SDIFdataInstance>();
				selTracks = new Vector<HoloTrack>();
				Ut.barMenu.update();
			}
		}
	}
	
	public void cutAtSeq()
	{
		TimeDialog timeDialog = new TimeDialog(this, "Cut this trajectory at time (h:mn:sec:cs)");
        timeDialog.setLocationRelativeTo(this);
        timeDialog.setVisible(true);
        int[] result = timeDialog.getNumbers();
		if(result != null)
		{
			int cutTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
			if(cutTime != -1)
			{
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				int[] iA = ScoreIndex.decode(selected);
				currentTrack = holoEditRef.gestionPistes.getTrack(iA[1]);
				currentTrack.cutTrajAt(cutTime);
				currentTrack.update();
				selected = ScoreIndex.getNull();
				Ut.barMenu.update();
			}
		}
	}
	
	public void setCursor()
	{
		String result = JOptionPane.showInputDialog(this, "Set cursor at time (h:mn:sec:cs),\nenter \"begin\" for begin time.", "Cursor", JOptionPane.QUESTION_MESSAGE);
		if(result != null)
		{
			if(result.equalsIgnoreCase("begin"))
				cursorTime = holoEditRef.counterPanel.getDate(1);
			else
			{
				int tmpTime = treatTimeString(result);
				if(tmpTime != -1)
					cursorTime = tmpTime;
			}
		}
	}
	public Vector<SDIFdataInstance> getSeqSDIFinAllTrack() {
		Vector<SDIFdataInstance> sdifDataInTimeSelection = new Vector<SDIFdataInstance>();
		int nbtr = holoEditRef.gestionPistes.getNbTracks();
		int beg = holoEditRef.counterPanel.getDate(1);
		int end = holoEditRef.counterPanel.getDate(2);
		if (beg==end){
			beg = minTime;
			end = maxTime;
		}
		for (int i=0; i<nbtr; i++){
			for (SDIFdataInstance hsdifdt : holoEditRef.gestionPistes.getTrack(i).getAllSDIFsInSelection(beg, end)){
			//	if (!sdifDataInTimeSelection.contains(hsdifdt))	
				sdifDataInTimeSelection.add(hsdifdt);
			}
			
		}
		return sdifDataInTimeSelection;
	}
	
	public Vector<SDIFdataInstance> getSeqSDIFinTrack(int trackNumber) {
		Vector<SDIFdataInstance> sdifDataInTimeSelection = new Vector<SDIFdataInstance>();
		int beg = holoEditRef.counterPanel.getDate(1);
		int end = holoEditRef.counterPanel.getDate(2);
		if (beg==end){
			beg = minTime;
			end = maxTime;
		}
		for (SDIFdataInstance hsdifdt : holoEditRef.gestionPistes.getTrack(trackNumber).getAllSDIFsInSelection(beg, end))
			sdifDataInTimeSelection.add(hsdifdt);
		return sdifDataInTimeSelection;
	}
	
	public void changeLock()
	{
		boolean lockState = !holoEditRef.gestionPistes.getTrack(trackSelected).isLocked();
		holoEditRef.gestionPistes.getTrack(trackSelected).setLocked(lockState);
		holoEditRef.gestionPistes.ts.labelAudio[trackSelected].setOpaque(lockState);
		holoEditRef.gestionPistes.ts.repaint();
		Ut.barMenu.update();
	}

	public void viewAll()
	{
		int min = 100000;
		int max = 0;
		for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
		{		
			if(!tk.isEmpty() || !tk.waves.isEmpty() || !tk.sdifdataInstanceVector.isEmpty())
			{
				if(!tk.waves.isEmpty() && !tk.sdifdataInstanceVector.isEmpty())
				{
					int min1 = Ut.min(min,Ut.min(tk.waves.firstElement().getFirstDate(), tk.sdifdataInstanceVector.firstElement().getFirstDate()));
					int max1 = Ut.max(max,Ut.max(tk.waves.lastElement().getLastDate(), tk.sdifdataInstanceVector.lastElement().getLastDate()));
					min = Ut.min(min,Ut.min(tk.getFirstDate(), min1));
					max = Ut.max(max,Ut.max(tk.getLastDate(), max1));			
				}
				else if (!tk.waves.isEmpty())
				{
					min = Ut.min(min,Ut.min(tk.getFirstDate(), tk.waves.firstElement().getFirstDate()));
					max = Ut.max(max,Ut.max(tk.getLastDate(), tk.waves.lastElement().getLastDate()));
				}
				else if (!tk.sdifdataInstanceVector.isEmpty())
				{
					min = Ut.min(min,Ut.min(tk.getFirstDate(), tk.sdifdataInstanceVector.firstElement().getFirstDate()));
					max = Ut.max(max,Ut.max(tk.getLastDate(), tk.sdifdataInstanceVector.lastElement().getLastDate()));
				}
				else
				{
					min = Ut.min(min,tk.getFirstDate());
					max = Ut.max(max,tk.getLastDate());
				}
			}
		}
		holoEditRef.counterPanel.setCompteur(1, min);
		holoEditRef.counterPanel.setCompteur(2, max);
	}

	public void zoomAll()
	{
		minTime = 100000;
		maxTime = 0;
		for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
		{
			if(!tk.isEmpty() || !tk.waves.isEmpty() || !tk.sdifdataInstanceVector.isEmpty())
			{
				if(!tk.waves.isEmpty() && !tk.sdifdataInstanceVector.isEmpty())
				{
					int min1 = Ut.min(minTime,Ut.min(tk.waves.firstElement().getFirstDate(), tk.sdifdataInstanceVector.firstElement().getFirstDate()));
					int max1 = Ut.max(maxTime,Ut.max(tk.waves.lastElement().getLastDate(), tk.sdifdataInstanceVector.lastElement().getLastDate()));	
					minTime = Ut.min(minTime,Ut.min(tk.getFirstDate(), min1));
					maxTime = Ut.max(maxTime,Ut.max(tk.getLastDate(), max1));
				}
				else if(!tk.waves.isEmpty())
				{
					minTime = Ut.min(minTime, tk.getFirstDate(), tk.waves.firstElement().getFirstDate());
					maxTime = Ut.max(maxTime, tk.getLastDate(), tk.waves.lastElement().getLastDate());
				} 
				else if(!tk.sdifdataInstanceVector.isEmpty())
				{
					minTime = Ut.min(minTime,Ut.min(tk.getFirstDate(), tk.sdifdataInstanceVector.firstElement().getFirstDate()));
					maxTime = Ut.max(maxTime,Ut.max(tk.getLastDate(), tk.sdifdataInstanceVector.lastElement().getLastDate()));
				}
				else {
					minTime = Ut.min(minTime,tk.getFirstDate());
					maxTime = Ut.max(maxTime,tk.getLastDate());
				}
			}
		}
	}

	public void zoomTrack()
	{
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		if(!currentTrack.isEmpty() || !currentTrack.waves.isEmpty() || !currentTrack.sdifdataInstanceVector.isEmpty())
		{
			if(!currentTrack.waves.isEmpty() && !currentTrack.sdifdataInstanceVector.isEmpty())
			{
				int min1 = Ut.min(minTime, currentTrack.waves.firstElement().getFirstDate(), currentTrack.sdifdataInstanceVector.firstElement().getFirstDate());
				int max1 = Ut.max(maxTime, currentTrack.waves.lastElement().getLastDate(), currentTrack.sdifdataInstanceVector.lastElement().getLastDate());					
				minTime = Ut.min(currentTrack.getFirstDate(), min1);
				maxTime = Ut.max(currentTrack.getLastDate(), max1);
			}
			else if(!currentTrack.waves.isEmpty())
			{
				minTime = Ut.min(currentTrack.getFirstDate(), currentTrack.waves.firstElement().getFirstDate());
				maxTime = Ut.max(currentTrack.getLastDate(), currentTrack.waves.lastElement().getLastDate());
			} 
			else if(!currentTrack.sdifdataInstanceVector.isEmpty())
			{
				minTime = Ut.min(currentTrack.getFirstDate(), currentTrack.sdifdataInstanceVector.firstElement().getFirstDate());
				maxTime = Ut.max(currentTrack.getLastDate(), currentTrack.sdifdataInstanceVector.lastElement().getLastDate());
			} 
			else {
				minTime = currentTrack.getFirstDate();
				maxTime = currentTrack.getLastDate();
			}
		}
	}
	
	public void viewTrack()
	{
		currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
		if(!currentTrack.isEmpty() || !currentTrack.waves.isEmpty() || !currentTrack.sdifdataInstanceVector.isEmpty())
		{
			if(!currentTrack.waves.isEmpty() && !currentTrack.sdifdataInstanceVector.isEmpty())
			{
				int min1 = Ut.min(minTime, currentTrack.waves.firstElement().getFirstDate(), currentTrack.sdifdataInstanceVector.firstElement().getFirstDate());
				int max1 = Ut.max(maxTime, currentTrack.waves.lastElement().getLastDate(), currentTrack.sdifdataInstanceVector.lastElement().getLastDate());
				holoEditRef.counterPanel.setCompteur(1, Ut.min(currentTrack.getFirstDate(), min1));
				holoEditRef.counterPanel.setCompteur(2, Ut.max(currentTrack.getLastDate(), max1));
			} else if(!currentTrack.waves.isEmpty())
			{
				holoEditRef.counterPanel.setCompteur(1, Ut.min(currentTrack.getFirstDate(), currentTrack.waves.firstElement().getFirstDate()));
				holoEditRef.counterPanel.setCompteur(2, Ut.max(currentTrack.getLastDate(), currentTrack.waves.lastElement().getLastDate()));
			} else if(!currentTrack.sdifdataInstanceVector.isEmpty())
			{
				holoEditRef.counterPanel.setCompteur(1, Ut.min(currentTrack.getFirstDate(), currentTrack.sdifdataInstanceVector.firstElement().getFirstDate()));
				holoEditRef.counterPanel.setCompteur(2, Ut.max(currentTrack.getLastDate(), currentTrack.sdifdataInstanceVector.lastElement().getLastDate()));
			} else {
				holoEditRef.counterPanel.setCompteur(1, currentTrack.getFirstDate());
				holoEditRef.counterPanel.setCompteur(2, currentTrack.getLastDate());
			}
		}
	}
	
	public void viewMultiSeq()
	{
		int min = 10000000;
		int max = 0;
		for (HoloTraj ht : selSeqs)
		{
			min = Ut.min(ht.getFirstDate(), min);
			max = Ut.max(ht.getLastDate(), max);
		}
		for (WaveFormInstance w : selWaves)
		{
			min = Ut.min(w.getFirstDate(), min);
			max = Ut.max(w.getLastDate(), max);
		}
		for (SDIFdataInstance s : selSDIFs)
		{
			min = Ut.min(s.getFirstDate(), min);
			max = Ut.max(s.getLastDate(), max);
		}
		holoEditRef.counterPanel.setCompteur(1, min);
		holoEditRef.counterPanel.setCompteur(2, max);
	}

	public void zoomMultiSeq()
	{
		int min = 10000000;
		int max = 0;
		for (HoloTraj ht : selSeqs)
		{
			min = Ut.min(ht.getFirstDate(), min);
			max = Ut.max(ht.getLastDate(), max);
		}
		for (WaveFormInstance w : selWaves)
		{
			min = Ut.min(w.getFirstDate(), min);
			max = Ut.max(w.getLastDate(), max);
		}
		for (SDIFdataInstance s : selSDIFs)
		{
			min = Ut.min(s.getFirstDate(), min);
			max = Ut.max(s.getLastDate(), max);
		}
		minTime = min;
		maxTime = max;
	}

	public void viewSeq()
	{
		int[] iA = ScoreIndex.decode(selected);
		if (ScoreIndex.isSeq())
		{
			currentSeq = holoEditRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]);
			holoEditRef.counterPanel.setCompteur(1, currentSeq.getFirstDate());
			holoEditRef.counterPanel.setCompteur(2, currentSeq.getLastDate());
		} else if(ScoreIndex.isWave())
		{
			currentWave = holoEditRef.gestionPistes.getTrack(iA[1]).getWave(iA[2]);
			holoEditRef.counterPanel.setCompteur(1, currentWave.getFirstDate());
			holoEditRef.counterPanel.setCompteur(2, currentWave.getLastDate());
		} else if(ScoreIndex.isData())
		{
			currentSDIF = holoEditRef.gestionPistes.getTrack(iA[1]).getSDIFinst(iA[2]);
			holoEditRef.counterPanel.setCompteur(1, currentSDIF.getFirstDate());
			holoEditRef.counterPanel.setCompteur(2, currentSDIF.getLastDate());
		}
	}
	
	public void zoomSeq()
	{
		int[] iA = ScoreIndex.decode(selected);
		if (ScoreIndex.isSeq())
		{
			currentSeq = holoEditRef.gestionPistes.getTrack(iA[1]).getHoloTraj(iA[2]);
			minTime = currentSeq.getFirstDate();
			maxTime = currentSeq.getLastDate();
		} else if(ScoreIndex.isWave())
		{
			currentWave = holoEditRef.gestionPistes.getTrack(iA[1]).getWave(iA[2]);
			minTime = currentWave.getFirstDate();
			maxTime = currentWave.getLastDate();
		} else if(ScoreIndex.isData())
		{
			currentSDIF = holoEditRef.gestionPistes.getTrack(iA[1]).getSDIFinst(iA[2]);
			minTime = currentSDIF.getFirstDate();
			maxTime = currentSDIF.getLastDate();
		}
	}

	public void exportSeq()
	{
		int[] iA = ScoreIndex.decode(selected);
		if(ScoreIndex.isSeq())
			holoEditRef.gestionPistes.exportSeq(iA[1],iA[2]);
	}
	
	public void exportSeqICST()
	{
		int[] iA = ScoreIndex.decode(selected);
		if(ScoreIndex.isSeq())
			holoEditRef.gestionPistes.exportSeqICST(iA[1],iA[2]);
	}
	
	public void importWave()
	{
		try {
			HoloWaveForm hwf = (HoloWaveForm) JOptionPane.showInputDialog(this, "Import a Waveform from SoundPool", "Import WaveForm", JOptionPane.INFORMATION_MESSAGE, null, holoEditRef.gestionPistes.soundPool.getSounds().toArray(), holoEditRef.gestionPistes.soundPool.get(0));
			if(hwf != null)
			{
				holoEditRef.gestionPistes.StoreToUndo(trackSelected);
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				currentTrack.addWave(new WaveFormInstance(holoEditRef.gestionPistes, hwf.toString(), (int) oldCurrentX));
				currentTrack.update();
				selIndex = new Vector<Integer>();
				selSeqs = new Vector<HoloTraj>();
				selWaves = new Vector<WaveFormInstance>();
				selTracks = new Vector<HoloTrack>();
				Ut.barMenu.update();
			}
		}catch(ArrayIndexOutOfBoundsException aiobe){
			// soundPool does not contain any sound
		}
	}
	
	public void importWave(int tkNum)
	{
		HoloWaveForm hwf = (HoloWaveForm) JOptionPane.showInputDialog(this, "Import a Waveform from SoundPool", "Import WaveForm", JOptionPane.INFORMATION_MESSAGE, null, holoEditRef.gestionPistes.soundPool.getSounds().toArray(), holoEditRef.gestionPistes.soundPool.get(0));
		if(hwf != null)
		{
			TimeDialog timeDialog = new TimeDialog(this, "Add this waveformat that time (h:mn:sec:ms)");
	        timeDialog.setLocationRelativeTo(this);
	        timeDialog.setVisible(true);
	       int[] result = timeDialog.getNumbers();
			if(result != null)
			{
				int addTime = result[0]*3600000+result[1]*60000+result[2]*1000+result[3];
				if(addTime != -1)
				{
					holoEditRef.gestionPistes.StoreToUndo(tkNum);
					currentTrack = holoEditRef.gestionPistes.getTrack(tkNum);
					currentTrack.addWave(new WaveFormInstance(holoEditRef.gestionPistes, hwf.toString(), addTime));
					currentTrack.update();
					selIndex = new Vector<Integer>();
					selSeqs = new Vector<HoloTraj>();
					selWaves = new Vector<WaveFormInstance>();
					selTracks = new Vector<HoloTrack>();
					Ut.barMenu.update();
				}
			}
		}
	}
	
	private void makeDropTarget(final java.awt.Component c)
	{
		// Make drop target
		if (c.getParent() != null)
		{
			@SuppressWarnings("unused")
			final DropTarget dt = new DropTarget(c, this);
		}
	}
	
	public void dragEnter(DropTargetDragEvent dtde)
	{
		Point mouse = dtde.getLocation();
		query_track_select = true;
		selected = ScoreIndex.getNull();
		float posW = (float)mouse.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - ((float)mouse.getY() - H_TIME_SCALE);
		dragCtrl = true;
		mousex = (float)mouse.getX();
		mousey = (float)mouse.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		mouseCursor = convPosPt(posW, posH);
		oldCurrentX = mouseCursor.getDate();
		if (acceptDrag && droppedWaveForm != null)
		{
			dtde.getDropTargetContext().dropComplete(true);
			draggedWave = true;
			currentDraggedWave = new WaveFormInstance(holoEditRef.gestionPistes,droppedWaveForm.toString(),(int)oldCurrentX);
		}
		else if (acceptDrag && droppedData != null)
		{
			dtde.getDropTargetContext().dropComplete(true);
			draggedData = true;
			try {
				HoloSDIFdata hsdifdt = (HoloSDIFdata) droppedData;
				currentDraggedSDIF = new SDIFdataInstance(holoEditRef.gestionPistes,hsdifdt,(int)oldCurrentX);
			}catch(ClassCastException cce){
				System.out.println("ClassCastException on dragged Data.. not an sdif");
			}
		} else if(isDragOk(dtde)){
			dtde.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
		} else
			dtde.rejectDrag();
		display();
	}

	public void dragOver(DropTargetDragEvent dtde)
	{
		Point mouse = dtde.getLocation();
		float posW = (float)mouse.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - ((float)mouse.getY() - H_TIME_SCALE);
		mousex = (float)mouse.getX();
		mousey = (float)mouse.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		mouseCursor = convPosPt(posW, posH);
		query_track_select = true;
		if(draggedWave)
			currentDraggedWave.setBegTime((int)mouseCursor.getDate());
		//add
		else if(draggedData)
			currentDraggedSDIF.setBegTime((int)mouseCursor.getDate());
		display();
	}

	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	public void dragExit(DropTargetEvent dte)
	{
		dragCtrl = false;
		draggedWave = false;
		draggedData = false;
		currentDraggedWave = null;
		currentDraggedSDIF = null;
	}

	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent dtde)
	{
		Point mouse = dtde.getLocation();
		float posW = (float)mouse.getX() - W_TRACK_HEADER;
		float posH = tracksGlobalHeight - ((float)mouse.getY() - H_TIME_SCALE);
		mousex = (float)mouse.getX();
		mousey = (float)mouse.getY() - H_TIME_SCALE - H_TIME_SCROLL - H_BOTTOM_BORDER12;
		mouseCursor = convPosPt(posW, posH);
		if (draggedWave)
		{
			currentDraggedWave.setBegTime((int)mouseCursor.getDate());
			currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
			currentTrack.addWave(currentDraggedWave);
			currentTrack.update();
			dtde.getDropTargetContext().dropComplete(true);
			draggedWave = false;
			draggedData = false;
			acceptDrag = false;
			dragCtrl = false;
			droppedWaveForm = null;
			droppedData = null;
			holoEditRef.modify();
		} else if (draggedData)
		{
			currentDraggedSDIF.setBegTime((int)mouseCursor.getDate());
			currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
			currentTrack.addSDIF(currentDraggedSDIF);
			currentTrack.update();
			dtde.getDropTargetContext().dropComplete(true);
			draggedData = false;
			acceptDrag = false;
			dragCtrl = false;
			droppedData = null;
			holoEditRef.modify();
		}
		else {
			try
			{
				Transferable tr = dtde.getTransferable();
				if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
					List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
					final File[] filesTemp = new File[fileList.size()];
					fileList.toArray(filesTemp);
					dtde.getDropTargetContext().dropComplete(filesDropped(filesTemp));
				}
				else{ dtde.getDropTargetContext().dropComplete(false);
					System.err.println("DataFlavor Not Supported !");
				}
			}
			catch (java.io.IOException io)
			{
				System.out.println("FileDrop: IOException - abort:");
				io.printStackTrace();
				dtde.rejectDrop();
			}
			catch (java.awt.datatransfer.UnsupportedFlavorException ufe)
			{
				System.out.println("FileDrop: UnsupportedFlavorException - abort:");
				ufe.printStackTrace();
				dtde.rejectDrop();
			}
			finally {}
		}
		display();
	}

	private boolean isDragOk(DropTargetDragEvent evt)
	{
		boolean ok = false;
		DataFlavor[] flavors = evt.getCurrentDataFlavors();
		int i = 0;
		while (!ok && i < flavors.length)
		{
			if (flavors[i].equals(DataFlavor.javaFileListFlavor))
				ok = true;
			i++;
		}
		return ok;
	}

	public boolean filesDropped(File[] files)
	{
		Vector<File> filesToImport = new Vector<File>(1, 1);
		for (File f : files)
		{
			if (f.isDirectory())
			{
				File[] contentFile;
					
				contentFile = f.listFiles(holoEditRef.gestionPistes.sndFilter);
				
				for (File cf : contentFile)
					filesToImport.add(cf);
			}
			else
				filesToImport.add(f);
		}
		boolean b = true;
		int where = (int)mouseCursor.getDate();
		for (File dropFile : filesToImport)
		{
			// sound dropped
			if(holoEditRef.gestionPistes.sndFilter.accept(dropFile))
			{
				System.out.println("Importing " + dropFile.getAbsolutePath());
				holoEditRef.soundPoolGui.importSound(dropFile, false);
				try
				{
					while(!holoEditRef.soundPoolGui.done)
					{
//						System.out.println("score-import-waiting");
						Thread.sleep(500);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				if(holoEditRef.soundPoolGui.fine)
				{
					currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
					currentTrack.addWave(new WaveFormInstance(holoEditRef.gestionPistes,holoEditRef.soundPoolGui.last.toString(),where));
					where = where + holoEditRef.soundPoolGui.last.getFileLengthCS();
					currentTrack.update();
					holoEditRef.modify();
					b = b && true;
					
				} else {
					switch(holoEditRef.soundPoolGui.error)
					{
					case WaveFormReader.NO_ERROR :
						break;
					case WaveFormReader.MONO_ERROR :
						Ut.alert("Error","Only mono file for instance,\nOnly the left channel has been read.");
						break;
					case WaveFormReader.TYPE_ERROR :
						Ut.alert("Import Error", holoEditRef.soundPoolGui.errorFileName + " is not a supported file type.");
						break;
					case WaveFormReader.PATH_ERROR :
						Ut.alert("Import Error", "Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot be longer than 32 characters (including the extension).");
						break;
					case WaveFormReader.PATH2_ERROR :
						Ut.alert("Import Error", "Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot contain blank spaces.");
						break;
					default :
						Ut.alert("Import Error", "Problem while loading soundfile, aborted.");
						break;
					}
					b = b && false;
				}
			}
			
			else if (holoEditRef.gestionPistes.tjFilter.accept(dropFile))
			{
				System.out.println("Importing traj " + dropFile.getAbsolutePath());
				
				currentTrack = holoEditRef.gestionPistes.getTrack(trackSelected);
				
				new TjFileReader(holoEditRef.gestionPistes, dropFile.getAbsolutePath(), currentTrack.getNumber()-1, where);
				
			}
			
		}
		return b;
	}

	public boolean hasFocus()
	{
		return super.hasFocus() || glp.hasFocus();
	}
	
	public void setTitle(String title)
	{
		super.setTitle("Score - "+title);
	}
	
	public String getTitle()
	{
		return super.getTitle();
	}
	
	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e)
	{
		float zoom_factor = 0;
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_DOWN:
			if(keyDown != -1)
				zoom_factor = e.isShiftDown() ? 1.1f : 1.5f; 
			break;
		case KeyEvent.VK_UP:
			if(keyDown != -1)
				zoom_factor = e.isShiftDown() ? 0.95f : 0.75f;
			break;
		case KeyEvent.VK_LEFT:
			if(keyDown != -1)
			{
				int pad = (maxTime - minTime) / 2;
				minTime -= !e.isShiftDown() ? pad : pad/10;
				maxTime -= !e.isShiftDown() ? pad : pad/10;
				if (minTime < 0)
				{
					minTime = 0;
					maxTime = 2 * pad;
				}
				display();
			}
			break;
		case KeyEvent.VK_RIGHT:
			if(keyDown != -1)
			{
				int pad = (maxTime - minTime) / 2;
				minTime += !e.isShiftDown() ? pad : pad/10;
				maxTime += !e.isShiftDown() ? pad : pad/10;
				if (maxTime > totalTime + ZOOM_OUT_MAX_PAD)
				{
					minTime = (int) (totalTime + ZOOM_OUT_MAX_PAD - 2 * pad);
					maxTime = (int) (totalTime + ZOOM_OUT_MAX_PAD);
				}
				display();
			}
			break;
		case KeyEvent.VK_Z:
		case KeyEvent.VK_D:
			keyDown = e.getKeyCode();
		default :
			break;
		}
		if(zoom_factor != 0)
		{
			float currentZoomTime = (maxTime+minTime)/2;
			float padMin = (currentZoomTime - minTime);
			float padMax = (maxTime - currentZoomTime);
			padMin *= zoom_factor;
			padMax *= zoom_factor;
			padMin = Ut.clipL(padMin, ZOOM_IN_MIN_PAD);
			padMax = Ut.clipL(padMax, ZOOM_IN_MIN_PAD);
			minTime = (int) Ut.clipL(currentZoomTime - padMin, 0);
			maxTime = (int) Ut.clip(currentZoomTime + padMax, 0, totalTime + ZOOM_OUT_MAX_PAD);
			display();
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode() == keyDown)
			keyDown = -1;
	}
	
	public void windowDeactivated(WindowEvent e)
	{
		popupVisible = false;
	}
	public void focusGained(FocusEvent e)
	{
		updateMenuBar();
		if (holoEditRef.helpWindowOpened)
			holoEditRef.helpWindow.jumpToIndex("#score", true);
	}
}