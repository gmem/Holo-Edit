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
import holoedit.data.HoloRecentFile;
import holoedit.functions.Algorithm;
import holoedit.opengl.ScoreIndex;
import holoedit.util.Ut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class HoloMenuBar extends JMenuBar
{
	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu trackMenu;
	private JMenu scoreMenu;
	private JMenu optionMenu;
	private JMenu functionsMenu;
	private JMenu transportMenu;
	private JMenu hpMenu;
	private JMenu viewMenu;
	private JMenu helpMenu;
	// FILE
	private HoloMenuItem newMenuItem;
	private HoloMenuItem openMenuItem;
	private HoloMenuItem saveMenuItem;
	private HoloMenuItem saveAsMenuItem;
	private JMenu importMenu;
	private JMenu openMidiMenu;
	private HoloMenuItem openMidi7bMenuItem;
	private HoloMenuItem openMidiMenuItem;
	private HoloMenuItem openTextAllTracksMenuItem;
	private HoloMenuItem openSeqAllTracksMenuItem;
	public JMenu openRecentFilesMenu;
	private HoloMenuItem importSoundMenuItem;
	// EDIT
	private HoloMenuItem undoMenuItem;
	private HoloMenuItem redoMenuItem;
	private HoloMenuItem cutMenuItem;
	private HoloMenuItem copyMenuItem;
	private HoloMenuItem pasteMenuItem;
	private HoloMenuItem replaceMenuItem;
	private HoloMenuItem insertMenuItem;
	private HoloMenuItem eraseMenuItem;
	private HoloMenuItem selectAllMenuItem;
	// TRACK
	private HoloMenuItem init;
	private HoloMenuItem name;
	private HoloMenuItem number;
	private HoloMenuItem color;
	private HoloMenuItem add;
	private HoloMenuItem delete;
	private HoloMenuItem duplicate;
	private HoloMenuItem resetnames;
	private HoloMenuItem resetnumbers;
	private HoloMenuItem renumber;
	private HoloMenuItem reset;
	private HoloMenuItem importTrack;
	private HoloMenuItem exportTrack;
	private HoloMenuItem prevTrack;
	private HoloMenuItem nextTrack;
	private HoloMenuItem soloPrevTrack;
	private HoloMenuItem soloNextTrack;

	// SCORE
	private HoloMenuItem setCursor1;
	private HoloMenuItem setCursor2;
	private HoloMenuItem setCursor3;
	private HoloMenuItem viewAll1;
	private HoloMenuItem zoomAll1;
	private HoloMenuItem viewAll2;
	private HoloMenuItem zoomAll2;
	private HoloMenuItem viewAll3;
	private HoloMenuItem zoomAll3;
	private HoloMenuItem viewTrack;
	private HoloMenuItem viewMultiSeq;
	private HoloMenuItem viewSeq;
	private HoloMenuItem zoomTrack;
	private HoloMenuItem zoomMultiSeq;
	private HoloMenuItem zoomSeq;
	private HoloMenuItem lockTrack;
	private HoloMenuItem joinTrack;
	private HoloMenuItem joinMultiSeq;
	private HoloMenuItem trimTrack;
	private HoloMenuItem trimMultiSeq;
	private HoloMenuItem trimSeq;
	private HoloMenuItem copyTrack;
	private HoloMenuItem copyMultiSeq;
	private HoloMenuItem copySeq;
	private HoloMenuItem cuttTrack;
	private HoloMenuItem cuttMultiSeq;
	private HoloMenuItem cuttSeq;
	private HoloMenuItem paste1;
	private HoloMenuItem paste2;
	private HoloMenuItem paste3;
	private HoloMenuItem eraseTrack;
	private HoloMenuItem eraseMultiSeq;
	private HoloMenuItem eraseSeq;
	private HoloMenuItem moveTrack;
	private HoloMenuItem moveMultiSeq;
	private HoloMenuItem moveSeq;
	private HoloMenuItem cutAtTrack;
	private HoloMenuItem cutAtMultiSeq;
	private HoloMenuItem cutAtSeq;
	private HoloMenuItem importSeq;
	private HoloMenuItem importWave;
	private HoloMenuItem exportSeq;
	// OPTIONS
	private JCheckBoxMenuItem allActiveMenuItem;
	private JCheckBoxMenuItem viewOnlyEditablePointsMenuItem;
	private JCheckBoxMenuItem openLastOnLoadMenuItem;
	private JCheckBoxMenuItem viewHpsMenuItem;
	private JCheckBoxMenuItem viewSDIFoptionsMenuItem;
	private JMenu coordMenuItem;
	private ButtonGroup coord;
	private JRadioButtonMenuItem cartesMenuItem;
	private JRadioButtonMenuItem polarMenuItem;
	private JMenu windowOnTop;
	//private ButtonGroup windowOnTopBg;
	public JCheckBoxMenuItem transportOnTop;
	public JCheckBoxMenuItem tracksOnTop;
	public JCheckBoxMenuItem roomOnTop;
	public JCheckBoxMenuItem room3dOnTop;
	public JCheckBoxMenuItem scoreOnTop;
	public JCheckBoxMenuItem timeOnTop;	
	private JMenu lookAndFeelMenuItem;
	private ButtonGroup bg;
	public JRadioButtonMenuItem basicLAFMenuItem;
	public JRadioButtonMenuItem systemLAFMenuItem;
	public JRadioButtonMenuItem motifLAFMenuItem;
	// TRANSPORT
	private HoloMenuItem playMenuItem;
	private HoloMenuItem pauseMenuItem;
	private HoloMenuItem recMenuItem;
	private HoloMenuItem recPlayMenuItem;
	private HoloMenuItem loopMenuItem;
	private HoloMenuItem spatUpdateMenuItem;
	private JCheckBoxMenuItem openOscMenuItem;
	private JCheckBoxMenuItem bonjourOscMenuItem;
	// SPEAKERS
	private HoloMenuItem addHPMenuItem;
	// VIEW
	private HoloMenuItem tkSelViewMenuItem;
	private HoloMenuItem transportViewMenuItem;
	private HoloMenuItem roomViewMenuItem;
	private HoloMenuItem timeViewMenuItem;
	private HoloMenuItem helpWindowMenuItem;
	private HoloMenuItem soundPoolViewMenuItem;
	private HoloMenuItem room3DViewMenuItem;
	private HoloMenuItem scoreViewMenuItem;
	private HoloMenuItem menuViewMenuItem;
	private HoloMenuItem closeViewMenuItem;
	private HoloMenuItem resetViewMenuItem;
	private HoloMenuItem resetAllViewMenuItem;
	// HELP
	private HoloMenuItem helpMenuItem;
	private HoloMenuItem docFrMenuItem;
	private HoloMenuItem docEnMenuItem;
	// WINDOWS MENUS
	private HoloMenuItem aboutMenuItem;
	private HoloMenuItem prefsMenuItem;
	private HoloMenuItem quitMenuItem;

	private HoloEdit holoEditRef;
	private FloatingWindow parent;
	
	public HoloMenuBar(HoloEdit s, FloatingWindow p)
	{
		holoEditRef = s;
		parent = p;
		HoloMenuAction aAction = new HoloMenuAction();
		// INIT_MENUS
		// Create, configure, and setup the Menubar, Menus, and Menu items.
		// ******** File Menu *******************************/
		fileMenu = new JMenu("File");
		newMenuItem = new HoloMenuItem("New tracks (Init all)", KeyEvent.VK_N, aAction);
		fileMenu.add(newMenuItem);
		fileMenu.addSeparator();
		openMenuItem = new HoloMenuItem("Open...", KeyEvent.VK_O, aAction);
		fileMenu.add(openMenuItem);
		openRecentFilesMenu = new JMenu("Open recent files");
		openRecentFilesMenu.setEnabled(false);
		fileMenu.add(openRecentFilesMenu);
		saveMenuItem = new HoloMenuItem("Save", KeyEvent.VK_S, aAction);
		fileMenu.add(saveMenuItem);
		saveAsMenuItem = new HoloMenuItem("Save As...", KeyEvent.VK_S, true, aAction);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		importMenu = new JMenu("Import old formats");
		// ---> import as midi
		openMidiMenu = new JMenu("Midi");
		openMidiMenuItem = new HoloMenuItem("Midi14bits", aAction);
		openMidi7bMenuItem = new HoloMenuItem("Midi7bits", aAction);
		openMidiMenu.add(openMidiMenuItem);
		openMidiMenu.add(openMidi7bMenuItem);
		importMenu.add(openMidiMenu);
		// ---> import as text
		openTextAllTracksMenuItem = new HoloMenuItem("Import Text...", aAction);
		importMenu.add(openTextAllTracksMenuItem);
		// ---> open as Seq
		openSeqAllTracksMenuItem = new HoloMenuItem("Import Seq~...", aAction);
		importMenu.add(openSeqAllTracksMenuItem);
		fileMenu.add(importMenu);
		fileMenu.addSeparator();
		importSoundMenuItem = new HoloMenuItem("Import Sound...", KeyEvent.VK_I, true, aAction);
		fileMenu.add(importSoundMenuItem);
		// WINDOWS MENUS
		prefsMenuItem = new HoloMenuItem("Preferences...",KeyEvent.VK_COMMA, aAction);
		quitMenuItem = new HoloMenuItem("Quit...",KeyEvent.VK_F4,true,true,true,true, aAction);
		if(!Ut.MAC)
		{
			fileMenu.addSeparator();
			fileMenu.add(prefsMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(quitMenuItem);
		}

		// ******** Edition Menu *****************************/
		editMenu = new JMenu("Edit");
		undoMenuItem = new HoloMenuItem("Undo", KeyEvent.VK_Z, aAction);
		editMenu.add(undoMenuItem);
		redoMenuItem = new HoloMenuItem("Redo", KeyEvent.VK_Z, true, aAction);
		editMenu.add(redoMenuItem);
		editMenu.addSeparator();
		cutMenuItem = new HoloMenuItem("Cut", KeyEvent.VK_X, aAction);
		editMenu.add(cutMenuItem);
		copyMenuItem = new HoloMenuItem("Copy", KeyEvent.VK_C, aAction);
		editMenu.add(copyMenuItem);
		pasteMenuItem = new HoloMenuItem("Paste", KeyEvent.VK_V, aAction);
		editMenu.add(pasteMenuItem);
		replaceMenuItem = new HoloMenuItem("Replace", KeyEvent.VK_R, aAction);
		editMenu.add(replaceMenuItem);
		insertMenuItem = new HoloMenuItem("Insert", KeyEvent.VK_I, aAction);
		editMenu.add(insertMenuItem);
		eraseMenuItem = new HoloMenuItem("Erase", KeyEvent.VK_E, aAction);
		editMenu.add(eraseMenuItem);
		editMenu.addSeparator();
		selectAllMenuItem = new HoloMenuItem("Select All", KeyEvent.VK_A, aAction);
		editMenu.add(selectAllMenuItem);
		
		// ******** Track Menu *************/
		
		trackMenu = new JMenu("Track");
		init = new HoloMenuItem("Init", KeyEvent.VK_I, true, aAction);
		name = new HoloMenuItem("Set Name...", aAction);
		number = new HoloMenuItem("Set Number...", aAction);
		color = new HoloMenuItem("Set Color...", aAction);
		lockTrack = new HoloMenuItem("Lock this track", KeyEvent.VK_L, aAction);
		add = new HoloMenuItem("New Track", KeyEvent.VK_N, true, aAction);
		delete = new HoloMenuItem("Delete", KeyEvent.VK_D, true, aAction);
		duplicate = new HoloMenuItem("Duplicate", KeyEvent.VK_D, aAction);
		resetnames = new HoloMenuItem("Reset all names", aAction);
		resetnumbers = new HoloMenuItem("Reset all numbers", aAction);
		renumber = new HoloMenuItem("Renumber all tracks...", aAction);
		reset = new HoloMenuItem("Reset both", aAction);
		importTrack = new HoloMenuItem("Import Track...", aAction);
		exportTrack = new HoloMenuItem("Export Track...", aAction);
		prevTrack = new HoloMenuItem("Previous Track", KeyEvent.VK_UP, aAction);
		nextTrack = new HoloMenuItem("Next Track", KeyEvent.VK_DOWN, aAction);
		soloPrevTrack = new HoloMenuItem("Solo Previous Track", KeyEvent.VK_UP, false, false, false, aAction);
		soloNextTrack = new HoloMenuItem("Solo Next Track", KeyEvent.VK_DOWN, false, false, false, aAction);
		
		trackMenu.add(init);
		trackMenu.add(name);
		trackMenu.add(number);
		trackMenu.add(color);
		trackMenu.addSeparator();
		trackMenu.add(lockTrack);
		trackMenu.addSeparator();
		trackMenu.add(add);
		trackMenu.add(delete);
		trackMenu.add(duplicate);
		trackMenu.addSeparator();
		trackMenu.add(resetnames);
		trackMenu.add(resetnumbers);
		trackMenu.add(renumber);
		trackMenu.add(reset);
		trackMenu.addSeparator();
		trackMenu.addSeparator();
		trackMenu.add(importTrack);
		trackMenu.add(exportTrack);
		trackMenu.addSeparator();
		trackMenu.add(prevTrack);
		trackMenu.add(nextTrack);
		trackMenu.add(soloPrevTrack);
		trackMenu.add(soloNextTrack);

		// ******** Score Menu *************/
		scoreMenu = new JMenu("Score");
		setCursor1 = new HoloMenuItem("Set cursor at time...", aAction);
		setCursor2 = new HoloMenuItem("Set cursor at time...", aAction);
		setCursor3 = new HoloMenuItem("Set cursor at time...", aAction);
		viewAll1 = new HoloMenuItem("View All", KeyEvent.VK_F, true, aAction);
		viewAll2 = new HoloMenuItem("View All", KeyEvent.VK_F, true, aAction);
		viewAll3 = new HoloMenuItem("View All", KeyEvent.VK_F, true, aAction);
		viewTrack = new HoloMenuItem("View this track", KeyEvent.VK_F, aAction);
		viewMultiSeq = new HoloMenuItem("View this selection", KeyEvent.VK_F, aAction);
		viewSeq = new HoloMenuItem("View this trajectory", KeyEvent.VK_F, aAction);
		zoomAll1 = new HoloMenuItem("Zoom on all", KeyEvent.VK_G, true, aAction);
		zoomAll2 = new HoloMenuItem("Zoom on all", KeyEvent.VK_G, true, aAction);
		zoomAll3 = new HoloMenuItem("Zoom on all", KeyEvent.VK_G, true, aAction);
		zoomTrack = new HoloMenuItem("Zoom on this track", KeyEvent.VK_G, aAction);
		zoomMultiSeq = new HoloMenuItem("Zoom on this selection", KeyEvent.VK_G, aAction);
		zoomSeq = new HoloMenuItem("Zoom on this trajectory", KeyEvent.VK_G, aAction);
		joinTrack = new HoloMenuItem("Join all trajectories in this track", KeyEvent.VK_J, aAction);
		joinMultiSeq = new HoloMenuItem("Join selected trajectories", KeyEvent.VK_J, aAction);
		trimTrack = new HoloMenuItem("Trim this track to current time selection", KeyEvent.VK_T, aAction);
		trimMultiSeq = new HoloMenuItem("Trim this selection to current time selection", KeyEvent.VK_T, aAction);
		trimSeq = new HoloMenuItem("Trim this trajectory to current time selection", KeyEvent.VK_T, aAction);
		copyTrack = new HoloMenuItem("Copy this track", KeyEvent.VK_C, true, aAction);
		copyMultiSeq = new HoloMenuItem("Copy this selection", KeyEvent.VK_C, true, aAction);
		copySeq = new HoloMenuItem("Copy this trajectory", KeyEvent.VK_C, true, aAction);
		cuttTrack = new HoloMenuItem("Cut this track", KeyEvent.VK_X, true, aAction);
		cuttMultiSeq = new HoloMenuItem("Cut this selection", KeyEvent.VK_X, true, aAction);
		cuttSeq = new HoloMenuItem("Cut this trajectory", KeyEvent.VK_X, true, aAction);
		paste1 = new HoloMenuItem("Paste", KeyEvent.VK_V, true, aAction);
		paste2 = new HoloMenuItem("Paste", KeyEvent.VK_V, true, aAction);
		paste3 = new HoloMenuItem("Paste", KeyEvent.VK_V, true, aAction);
		eraseTrack = new HoloMenuItem("Erase this track", KeyEvent.VK_E, true, aAction);
		eraseMultiSeq = new HoloMenuItem("Erase this selection", KeyEvent.VK_E, true, aAction);
		eraseSeq = new HoloMenuItem("Erase this trajectory", KeyEvent.VK_E, true, aAction);
		moveTrack = new HoloMenuItem("Move this track to time...", KeyEvent.VK_M, aAction);
		moveMultiSeq = new HoloMenuItem("Move this selection to time...", KeyEvent.VK_M, aAction);
		moveSeq = new HoloMenuItem("Move this trajectory to time...", KeyEvent.VK_M, aAction);
		cutAtTrack = new HoloMenuItem("Cut this track at time...",KeyEvent.VK_K, aAction);
		cutAtMultiSeq = new HoloMenuItem("Cut this selection at time...",KeyEvent.VK_K, aAction);
		cutAtSeq = new HoloMenuItem("Cut this trajectory at time...",KeyEvent.VK_K, aAction);
		importSeq = new HoloMenuItem("Import Trajectory...", aAction);
		importWave = new HoloMenuItem("Import WaveForm from SoundPool...", aAction);
		exportSeq = new HoloMenuItem("Export Trajectory...", aAction);
		scoreMenu.add(setCursor1);
		scoreMenu.addSeparator();
		scoreMenu.add(viewAll1);
		scoreMenu.add(zoomAll1);
		scoreMenu.addSeparator();
		scoreMenu.add(viewTrack);
		scoreMenu.add(zoomTrack);
		scoreMenu.addSeparator();
		scoreMenu.add(joinTrack);
		scoreMenu.add(trimTrack);
		scoreMenu.add(moveTrack);
		scoreMenu.add(cutAtTrack);
		scoreMenu.addSeparator();
		scoreMenu.add(copyTrack);
		scoreMenu.add(cuttTrack);
		scoreMenu.add(paste1);
		scoreMenu.add(eraseTrack);
		scoreMenu.addSeparator();
		scoreMenu.add(importSeq);
		scoreMenu.add(importWave);
		// ******** Option Menu *************/
		optionMenu = new JMenu("Options");
		allActiveMenuItem = new JCheckBoxMenuItem("Automatic track selection", false);
		allActiveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 8));
		optionMenu.add(allActiveMenuItem);
		viewOnlyEditablePointsMenuItem = new JCheckBoxMenuItem("View only editable points", false);
		viewOnlyEditablePointsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 8));
		optionMenu.add(viewOnlyEditablePointsMenuItem);
		viewHpsMenuItem = new JCheckBoxMenuItem("View Speakers", true);
		viewHpsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 8));
		optionMenu.add(viewHpsMenuItem);
		viewSDIFoptionsMenuItem = new JCheckBoxMenuItem("SDIF import options", false);
	//	viewSDIFoptionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 8));
		optionMenu.add(viewSDIFoptionsMenuItem);
		optionMenu.addSeparator();
		openLastOnLoadMenuItem = new JCheckBoxMenuItem("Load last file on startup", false);
		optionMenu.add(openLastOnLoadMenuItem);
		/* cartesian menu */
		optionMenu.addSeparator();
		coordMenuItem = new JMenu("Coordinates");
		coord = new ButtonGroup();
		cartesMenuItem = new JRadioButtonMenuItem("Cartesian", true);
		cartesMenuItem.addActionListener(aAction);
		polarMenuItem = new JRadioButtonMenuItem("Polar", false);
		polarMenuItem.addActionListener(aAction);
		coord.add(cartesMenuItem);
		coord.add(polarMenuItem);
		coordMenuItem.add(cartesMenuItem);
		coordMenuItem.add(polarMenuItem);
		optionMenu.add(coordMenuItem);
		
//		private JMenu windowOnTop;
//		private ButtonGroup windowOnTopBg;
//		public JRadioButtonMenuItem transportOnTop;
//		public JRadioButtonMenuItem tracksOnTop;
//		public JRadioButtonMenuItem roomOnTop;
//		public JRadioButtonMenuItem room3dOnTop;
//		public JRadioButtonMenuItem scoreOnTop;
//		public JRadioButtonMenuItem timeOnTop;
		
		/* Ontop menu */
		
		optionMenu.addSeparator();
		windowOnTop = new JMenu("Windows on top");
		//windowOnTopBg = new ButtonGroup();
		transportOnTop = new JCheckBoxMenuItem("Transport", false);
		tracksOnTop = new JCheckBoxMenuItem("Tracks", false);
		roomOnTop = new JCheckBoxMenuItem("Room", false);
		room3dOnTop = new JCheckBoxMenuItem("3D Room", false);
		scoreOnTop = new JCheckBoxMenuItem("Score", false);
		timeOnTop = new JCheckBoxMenuItem("Time Editor", false);
		
		windowOnTop.add(transportOnTop);
		windowOnTop.add(tracksOnTop);
		windowOnTop.add(roomOnTop);
		windowOnTop.add(room3dOnTop);
		windowOnTop.add(scoreOnTop);
		windowOnTop.add(timeOnTop);
		
		optionMenu.add(windowOnTop);
		
		
		transportOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.transport.setAlwaysOnTop(true);
				else holoEditRef.transport.setAlwaysOnTop(false);
			}
		});
		
		tracksOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.gestionPistes.ts.setAlwaysOnTop(true);
				else holoEditRef.gestionPistes.ts.setAlwaysOnTop(false);
			}
		});
		
		roomOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.room.setAlwaysOnTop(true);
				else holoEditRef.room.setAlwaysOnTop(false);
			}
		});
		
		room3dOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.room3d.setAlwaysOnTop(true);
				else holoEditRef.room3d.setAlwaysOnTop(false);
			}
		});
		
		scoreOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.score.setAlwaysOnTop(true);
				else holoEditRef.score.setAlwaysOnTop(false);
			}
		});
		
		timeOnTop.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.timeEditor.setAlwaysOnTop(true);
				else holoEditRef.timeEditor.setAlwaysOnTop(false);
			}
		});
		
		/* Look&Feel menu */
		
		lookAndFeelMenuItem = new JMenu("Change Look And Feel");
		bg = new ButtonGroup();
		basicLAFMenuItem = new JRadioButtonMenuItem("Metal L&F", true);
		basicLAFMenuItem.addActionListener(aAction);
		systemLAFMenuItem = new JRadioButtonMenuItem("System L&F (Default)", false);
		systemLAFMenuItem.addActionListener(aAction);
		motifLAFMenuItem = new JRadioButtonMenuItem("Motif L&F", false);
		motifLAFMenuItem.addActionListener(aAction);
		bg.add(basicLAFMenuItem);
		bg.add(systemLAFMenuItem);
		bg.add(motifLAFMenuItem);
		lookAndFeelMenuItem.add(basicLAFMenuItem);
		lookAndFeelMenuItem.add(systemLAFMenuItem);
		lookAndFeelMenuItem.add(motifLAFMenuItem);
		if(!Ut.MAC)
		{
			optionMenu.addSeparator();
			optionMenu.add(lookAndFeelMenuItem);
		}
		// ******** Functions & Speakers Menu *************/
		functionsMenu = new JMenu("Functions");
		hpMenu = new JMenu("Speakers");
		String currentCat = "";
		JMenuItem catTitle = null;
		boolean first = true;
		for(Algorithm a:holoEditRef.gestionPistes.algos)
		{
			if(!a.getCategory().equalsIgnoreCase("Speakers"))
			{
				if(!a.getCategory().equalsIgnoreCase(currentCat))
				{
					currentCat = a.getCategory();
					if(!first)
						functionsMenu.addSeparator();
					else 
						first = false;
					if (!currentCat.equalsIgnoreCase("Script"))
						catTitle = new JMenu(currentCat);
					catTitle.setEnabled(true);
					if (!currentCat.equalsIgnoreCase("Script"))
							functionsMenu.add(catTitle);
						else
							functionsMenu.add(a.getMenu());
				}
				if (!currentCat.equalsIgnoreCase("Script"))
					catTitle.add(a.getMenu());
			} else {
				hpMenu.add(a.getMenu());
			}
		}
		hpMenu.addSeparator();
		addHPMenuItem = new HoloMenuItem("Add a speaker", aAction);
		hpMenu.add(addHPMenuItem);

		// ******** Transport Menu *************/
		transportMenu = new JMenu("Transport");
	/*	openOscMenuItem = new JCheckBoxMenuItem("Enable Transport", false);
		if (Ut.MAC)
			openOscMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 5));
		else openOscMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 1));
		transportMenu.add(openOscMenuItem);
		openOscMenuItem.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					mainRef.connection.open();
				} else {
					mainRef.connection.close();
				}
			}
		}); */
/*		bonjourOscMenuItem = new JCheckBoxMenuItem("Enable Bonjour/Zeroconf", true);
		bonjourOscMenuItem.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					boolean wasOpen = mainRef.connection.isOpen();
					if (wasOpen)
						mainRef.connection.close();
					//mainRef.bonjour = true;
					if(wasOpen)
						mainRef.connection.open();
					boolean isNowOpen = mainRef.connection.isOpen();
				/*	if (isNowOpen)
						mainRef.transport.activer();
					else mainRef.transport.desactiver();*/
		/*	} else {
					boolean wasOpen = mainRef.connection.isOpen();
					if (wasOpen)
						mainRef.connection.close();
				//	mainRef.bonjour = false;
					if(wasOpen)
						mainRef.connection.open();
					boolean isNowOpen = mainRef.connection.isOpen();
			/*		if (isNowOpen)
						mainRef.transport.activer();
					else mainRef.transport.desactiver();*/
	/*			}
				update();
			}
		});		
		bonjourOscMenuItem.setEnabled(mainRef.bonjourInstalled);*/
	//	transportMenu.add(bonjourOscMenuItem);
	//	transportMenu.addSeparator();
		spatUpdateMenuItem = new HoloMenuItem("Holo-Spat update", aAction);
		transportMenu.add(spatUpdateMenuItem);
		transportMenu.addSeparator();
		playMenuItem = new HoloMenuItem("Play / Stop", KeyEvent.VK_SPACE, false, aAction);
		pauseMenuItem = new HoloMenuItem("Pause / Resume", KeyEvent.VK_SPACE, true, false, aAction);
		loopMenuItem = new HoloMenuItem("Loop", KeyEvent.VK_L, true, false, aAction);		
		recPlayMenuItem = new HoloMenuItem("Record & Play", KeyEvent.VK_SPACE, aAction); // ð+<space> 
		recMenuItem = new HoloMenuItem("Record", KeyEvent.VK_R, true, false, aAction);		
		transportMenu.add(playMenuItem);
		transportMenu.add(pauseMenuItem);
		transportMenu.add(loopMenuItem);
		transportMenu.add(recPlayMenuItem);
		transportMenu.add(recMenuItem);
		transportMenu.setVisible(true);
		// ******** View Menu *************/
		viewMenu = new JMenu("View");
		tkSelViewMenuItem = new HoloMenuItem("Tracks", KeyEvent.VK_F3, false, aAction);
		roomViewMenuItem = new HoloMenuItem("Room Editor", KeyEvent.VK_F4, false, aAction);
		room3DViewMenuItem = new HoloMenuItem("3D Room", KeyEvent.VK_F4, true, true, aAction);
		scoreViewMenuItem = new HoloMenuItem("Score", KeyEvent.VK_F5, false, aAction);
		soundPoolViewMenuItem = new HoloMenuItem("Sound Pool", KeyEvent.VK_F5, true, true, aAction);
		transportViewMenuItem = new HoloMenuItem("Transport", KeyEvent.VK_F6, false, aAction);
		timeViewMenuItem = new HoloMenuItem("Time Editor", KeyEvent.VK_F7, false, aAction);
		helpWindowMenuItem = new HoloMenuItem("Help Window", aAction);
		menuViewMenuItem = new HoloMenuItem("Menu", KeyEvent.VK_F8, false, aAction);
		closeViewMenuItem = new HoloMenuItem("Close current window", KeyEvent.VK_W, aAction);
		resetViewMenuItem = new HoloMenuItem("Reset current window position & size", aAction);
		resetAllViewMenuItem = new HoloMenuItem("Reset all windows positions & sizes", aAction);
		viewMenu.add(tkSelViewMenuItem);
		viewMenu.add(roomViewMenuItem);
		viewMenu.add(room3DViewMenuItem);
		viewMenu.add(scoreViewMenuItem);
		viewMenu.add(soundPoolViewMenuItem);
		viewMenu.add(transportViewMenuItem);
		viewMenu.add(timeViewMenuItem);
		viewMenu.add(helpWindowMenuItem);
		viewMenu.addSeparator();
		if(!Ut.MAC)
		{
			viewMenu.add(menuViewMenuItem);
			viewMenu.addSeparator();
		}
		viewMenu.add(closeViewMenuItem);
		viewMenu.add(resetViewMenuItem);
		viewMenu.add(resetAllViewMenuItem);
		// ******** Help Menu *************/
		helpMenu = new JMenu("Help");
		aboutMenuItem = new HoloMenuItem("About "+Ut.hv, aAction);
		helpMenuItem = new HoloMenuItem("Quick Help", KeyEvent.VK_F1, false, aAction);
		docFrMenuItem = new HoloMenuItem("Documentation (Fr)", aAction);
		docEnMenuItem = new HoloMenuItem("Documentation (En)", aAction);
		if(!Ut.MAC)
		{
			helpMenu.add(aboutMenuItem);
			helpMenu.addSeparator();
		}
		helpMenu.add(helpMenuItem);
		helpMenu.add(docFrMenuItem);
		helpMenu.add(docEnMenuItem);
		add(fileMenu);
		add(editMenu);
		add(trackMenu);
		add(scoreMenu);
		add(optionMenu);
		add(functionsMenu);
		add(transportMenu);
		add(hpMenu);
		add(viewMenu);
		add(helpMenu);

		// OPTIONS
		// Check Box Automatic Track Selection
		allActiveMenuItem.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.allTrackActive = true;
				else holoEditRef.allTrackActive = false;
				holoEditRef.gestionPistes.setDirty(true);
				holoEditRef.room.display();
			}
		});
		// Check Box Only Editable Points
		viewOnlyEditablePointsMenuItem.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				holoEditRef.viewOnlyEditablePoints = (e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				holoEditRef.gestionPistes.setDirty(true);
				holoEditRef.room.display();
			}
		});
		// Check Box Only View Speakers
		viewHpsMenuItem.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				holoEditRef.viewSpeakers = (e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				holoEditRef.room.display();
			}
		});
		
		// Check Box SDIF options
		viewSDIFoptionsMenuItem.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				holoEditRef.sdifExpert = (e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				holoEditRef.room.display();
			}
		});
		
		// Check Box Open Last on load
		openLastOnLoadMenuItem.addItemListener(new java.awt.event.ItemListener()
		{
			public void itemStateChanged(java.awt.event.ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
					holoEditRef.openLastOnLoad = true;
				else
					holoEditRef.openLastOnLoad = false;
			}
		});
	}
	class HoloMenuAction implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			// Impression du nom du menu pour tracage d'informations
			Object o = event.getSource();
			if (!o.getClass().toString().equalsIgnoreCase("class javax.swing.JRadioButtonMenuItem"))
				System.out.println("Command : " + o);
			if (o == newMenuItem)
			{
				holoEditRef.gestionPistes.reset();
				holoEditRef.gestionPistes.initTracks(false);
			}
			else if (o == openMenuItem)
				holoEditRef.gestionPistes.readHoloFile();
			else if (o == openMidiMenuItem)
				holoEditRef.gestionPistes.readFile("midi14");
			else if (o == openMidi7bMenuItem)
				holoEditRef.gestionPistes.readFile("midi7");
			else if (o == openTextAllTracksMenuItem)
				holoEditRef.gestionPistes.readFile("txt");
			else if (o == saveMenuItem)
				holoEditRef.gestionPistes.writeHoloFile();
			else if (o == saveAsMenuItem)
				holoEditRef.gestionPistes.writeHoloFileAs();
			else if (o == openSeqAllTracksMenuItem)
				holoEditRef.gestionPistes.readFile("seq");
			else if (o == undoMenuItem)
				holoEditRef.gestionPistes.Undo();
			else if (o == redoMenuItem)
				holoEditRef.gestionPistes.Redo();
			else if (o == cutMenuItem)
				holoEditRef.gestionPistes.Cut();
			else if (o == copyMenuItem)
				holoEditRef.gestionPistes.Copy();
			else if (o == pasteMenuItem)
				holoEditRef.gestionPistes.Paste();
			else if (o == replaceMenuItem)
				holoEditRef.gestionPistes.Replace();
			else if (o == insertMenuItem)
				holoEditRef.gestionPistes.Insert();
			else if (o == eraseMenuItem)
				holoEditRef.gestionPistes.Erase();
			else if (o == selectAllMenuItem)
				holoEditRef.gestionPistes.selectAll();
			else if (o == docFrMenuItem)
				openDoc("Fr");
			else if (o == docEnMenuItem)
				openDoc("En");
			else if (o == helpMenuItem)
				openHelp();
			else if (o == basicLAFMenuItem) {
				// Changement du Look and Feel
				System.out.println("Command : Metal Look & Feel");
				try {
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					holoEditRef.updateUI();
					updateUI();
					holoEditRef.laf = 0;
					holoEditRef.room.display();
				} catch (Exception e) {}
			} else if (o == systemLAFMenuItem) {
				// Changement du Look and Feel
				System.out.println("Command : System Look & Feel");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					holoEditRef.updateUI();
					updateUI();
					holoEditRef.laf = 1;
					holoEditRef.room.display();
				} catch (Exception e) {}
			} else if (o == motifLAFMenuItem) {
				// Changement du Look and Feel
				System.out.println("Command : Motif Look & Feel");
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
					holoEditRef.updateUI();
					updateUI();
					holoEditRef.laf = 2;
					holoEditRef.room.display();
				} catch (Exception e){}
			} else if (o == playMenuItem)
				holoEditRef.connection.playstop();
			else if (o == loopMenuItem)
				holoEditRef.connection.loop(!holoEditRef.connection.getLoop());
			else if (o == recPlayMenuItem)
			{
				holoEditRef.connection.record(true);
				holoEditRef.connection.playstop();
			}
			else if (o == recMenuItem)
				holoEditRef.connection.record(!holoEditRef.connection.isRecording());
			else if (o == pauseMenuItem)
				holoEditRef.connection.pause();
			else if (o == spatUpdateMenuItem)
				holoEditRef.connection.spatUpdate();
			else if (o == polarMenuItem) {
				// Changement du mode de coordonnees (polaire)
				System.out.println("Command : Polar coordinates");
				holoEditRef.coordinates = 1;
			} else if (o == cartesMenuItem) {
				// Changement du mode de coordonnees (cartesiennes)
				System.out.println("Command : Cartesian coordinates");
				holoEditRef.coordinates = 0;
			} else if (o == addHPMenuItem)
				holoEditRef.gestionPistes.addSpeaker();
			else if (o == tkSelViewMenuItem)
				holoEditRef.gestionPistes.ts.open();
			else if (o == transportViewMenuItem)
				holoEditRef.transport.open();
			else if (o == roomViewMenuItem) {
				setVisible(true);
				holoEditRef.room.open();
			} else if (o == timeViewMenuItem)
				holoEditRef.timeEditor.open();
			else if (o == helpWindowMenuItem){
				holoEditRef.helpWindow.open();
				holoEditRef.helpWindowOpened = true;
			} else if (o == soundPoolViewMenuItem)
				holoEditRef.soundPoolGui.open();
			else if (o == room3DViewMenuItem)
				holoEditRef.room3d.open();
			else if (o == importSoundMenuItem)
				holoEditRef.gestionPistes.importSound();
			else if (o == scoreViewMenuItem)
				holoEditRef.score.open();
			else if (o == closeViewMenuItem)
			{
				if (parent != null)
				{
					parent.close();
					holoEditRef.checkWindows();
				}
			} else if (o == resetAllViewMenuItem) {
				holoEditRef.room.resetPositionAndSize();
				holoEditRef.room3d.resetPositionAndSize();
				holoEditRef.score.resetPositionAndSize();
				holoEditRef.transport.resetPositionAndSize();
				holoEditRef.gestionPistes.ts.resetPositionAndSize();
				holoEditRef.timeEditor.resetPositionAndSize();
				holoEditRef.soundPoolGui.resetPositionAndSize();
			} else if (o == resetViewMenuItem) {
				if (parent != null)
					parent.resetPositionAndSize();
			}	
			else if (o == setCursor1 || o == setCursor2 || o == setCursor3)
				holoEditRef.score.setCursor();
			else if (o == viewAll1 || o == viewAll2 || o == viewAll3)
				holoEditRef.score.viewAll();
			else if (o == zoomAll1 || o == zoomAll2 || o == zoomAll3)
				holoEditRef.score.zoomAll();
			else if (o == viewTrack)
				holoEditRef.score.viewTrack();
			else if (o == zoomTrack)
				holoEditRef.score.zoomTrack();
			else if (o == viewMultiSeq)
				holoEditRef.score.viewMultiSeq();
			else if (o == zoomMultiSeq)
				holoEditRef.score.zoomMultiSeq();
			else if (o == viewSeq)
				holoEditRef.score.viewSeq();
			else if (o == zoomSeq)
				holoEditRef.score.zoomSeq();
			else if (o == joinTrack)
				holoEditRef.score.joinTrack();
			else if (o == joinMultiSeq)
				holoEditRef.score.joinMultiSeq();
			else if (o == trimTrack)
				holoEditRef.score.trimTrack();
			else if (o == trimMultiSeq)
				holoEditRef.score.trimMultiSeq();
			else if (o == trimSeq)
				holoEditRef.score.trimSeq();
			else if (o == eraseTrack)
				holoEditRef.score.eraseTrack();
			else if (o == eraseMultiSeq)
				holoEditRef.score.eraseMultiSeq();
			else if (o == eraseSeq)
				holoEditRef.score.eraseSeq();
			else if (o == copyTrack) {
				holoEditRef.score.copyTrack();
				holoEditRef.score.copyTrack();
			} else if (o == copyMultiSeq)
				holoEditRef.score.copyMultiSeq();
			else if (o == copySeq)
				holoEditRef.score.copySeq();
			else if (o == paste1 || o == paste2 || o == paste3)
				holoEditRef.score.paste(-1);
			else if (o == cuttTrack) {
				holoEditRef.score.copyTrack();
				holoEditRef.score.eraseTrack();
			} else if (o == cuttMultiSeq) {
				holoEditRef.score.copyMultiSeq();
				holoEditRef.score.eraseMultiSeq();
			} else if (o == cuttSeq) {
				holoEditRef.score.copySeq();
				holoEditRef.score.eraseSeq();
			} else if (o == moveTrack)
				holoEditRef.score.moveTrack();
			else if (o == moveMultiSeq)
				holoEditRef.score.moveMultiSeq();
			else if (o == moveSeq)
				holoEditRef.score.moveSeq();
			else if (o == cutAtTrack)
				holoEditRef.score.cutAtTrack();
			else if (o == cutAtMultiSeq)
				holoEditRef.score.cutAtMultiSeq();
			else if (o == cutAtSeq)
				holoEditRef.score.cutAtSeq();
			else if (o == init)
				holoEditRef.gestionPistes.initOneTrack(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == name)
				holoEditRef.gestionPistes.changeName(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == number)
				holoEditRef.gestionPistes.changeNumber(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == color)
				holoEditRef.gestionPistes.changeColor(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == lockTrack)
				holoEditRef.gestionPistes.changeLock(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == add)
				holoEditRef.gestionPistes.addTrack();
			else if (o == delete)
				holoEditRef.gestionPistes.deleteTrack(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == duplicate)
				holoEditRef.gestionPistes.duplicateTrack(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == resetnames)
				holoEditRef.gestionPistes.resetnames();
			else if (o == resetnumbers)
				holoEditRef.gestionPistes.resetnumbers();
			else if (o == renumber)
				holoEditRef.gestionPistes.changeNumbers();
			else if (o == reset)
				holoEditRef.gestionPistes.reset();
			else if (o == importTrack)
				holoEditRef.gestionPistes.importTrack(-1,0);
			else if (o == exportTrack)
				holoEditRef.gestionPistes.exportTrack(holoEditRef.gestionPistes.activeTrack);
			else if (o == importSeq)
				holoEditRef.gestionPistes.importSeq(holoEditRef.gestionPistes.getActiveTrackNb(),holoEditRef.gestionPistes.getActiveTrack().getLastDate());
			else if (o == importWave)
				holoEditRef.score.importWave(holoEditRef.gestionPistes.getActiveTrackNb());
			else if (o == exportSeq)
				holoEditRef.score.exportSeq();
			else if (o == prevTrack)
				holoEditRef.gestionPistes.prevTrack();
			else if (o == nextTrack)
				holoEditRef.gestionPistes.nextTrack();
			else if (o == soloPrevTrack)
				holoEditRef.gestionPistes.soloPrevTrack();
			else if (o == soloNextTrack)
				holoEditRef.gestionPistes.soloNextTrack();
			else if (o == aboutMenuItem)
				holoEditRef.about();
			else if (o == prefsMenuItem)
				holoEditRef.openPrefs();
			else if (o == quitMenuItem)
				holoEditRef.close();
			else if (o == menuViewMenuItem)
				holoEditRef.mainFrameOpen();
			holoEditRef.room.display();
			update();
		}
	};

	// --------------------------- Help -----------------------------------/
	public void openHelp()
	{
		Runtime r = Runtime.getRuntime();
		try
		{
			if (Ut.MAC)
				r.exec("open ./Documentations/help.html");
			else r.exec("cmd /c start .\\Documentations\\help.html");
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}

	public void openDoc(String lang)
	{
		Runtime r = Runtime.getRuntime();
		try
		{
			if (!Ut.MAC)
				r.exec("cmd /c start .\\Documentations\\Doc-Holophon-"+lang+"-V4.0.pdf");
			else r.exec("open ./Documentations/Doc-Holophon-"+lang+"-V4.0.pdf");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	// Mise ˆ jour du menu
	public void update()
	{
		if (holoEditRef != null)
		{
			boolean activ_non_empty = ((holoEditRef.gestionPistes.getActiveTrackNb() != -1) && !holoEditRef.gestionPistes.getActiveTrack().isEmpty()) || !holoEditRef.score.selIndex.isEmpty();
			cutMenuItem.setEnabled(activ_non_empty);
			copyMenuItem.setEnabled(activ_non_empty);
			eraseMenuItem.setEnabled(activ_non_empty);
			selectAllMenuItem.setEnabled(activ_non_empty);
			boolean clip_non_empty = (!holoEditRef.gestionPistes.copyTrack.isEmpty() || !holoEditRef.gestionPistes.copySeqs.isEmpty());
			pasteMenuItem.setEnabled(clip_non_empty);
			insertMenuItem.setEnabled(clip_non_empty);
			replaceMenuItem.setEnabled(clip_non_empty);
			undoMenuItem.setEnabled(!holoEditRef.gestionPistes.undoTrack.isEmpty() || holoEditRef.gestionPistes.undoAllTracks);
			redoMenuItem.setEnabled(!holoEditRef.gestionPistes.redoTrack.isEmpty() || holoEditRef.gestionPistes.redoAllTracks);
			boolean oscOpen = holoEditRef.connection.isOpen();
		//	bonjourOscMenuItem.setSelected(mainRef.bonjour);
		//	bonjourOscMenuItem.setEnabled(mainRef.bonjourInstalled);
	//		openOscMenuItem.setSelected(oscOpen);
			playMenuItem.setEnabled(true);
			pauseMenuItem.setEnabled(true);
			recMenuItem.setEnabled(oscOpen);
			loopMenuItem.setEnabled(true);
			loopMenuItem.setSelected(holoEditRef.connection.getLoop());
			spatUpdateMenuItem.setEnabled(oscOpen);
			functionsMenu.setEnabled(holoEditRef.gestionPistes.getActiveTrackNb() != -1);
			switch (holoEditRef.coordinates) {
				case 0:
					cartesMenuItem.setSelected(true);
					break;
				case 1:
					polarMenuItem.setSelected(true);
					break;
				default:
					break;
			}
			viewHpsMenuItem.setSelected(holoEditRef.viewSpeakers);
			viewSDIFoptionsMenuItem.setSelected(holoEditRef.sdifExpert);
			openLastOnLoadMenuItem.setSelected(holoEditRef.openLastOnLoad);
			allActiveMenuItem.setSelected(holoEditRef.allTrackActive);
			viewOnlyEditablePointsMenuItem.setSelected(holoEditRef.viewOnlyEditablePoints);
			openRecentFilesMenu.setEnabled(!holoEditRef.recentFiles.isEmpty());
			for (HoloRecentFile h : holoEditRef.recentFiles)
				openRecentFilesMenu.add(h);
			lockTrack.setText(holoEditRef.gestionPistes.getActiveTrackNb() != -1 && holoEditRef.gestionPistes.getActiveTrack().isLocked() ? "Unlock this track" : "Lock this track");
			scoreMenu.removeAll();
			if (holoEditRef.score.selIndex.isEmpty()) {
				ScoreIndex.decode(holoEditRef.score.selected);
				if (ScoreIndex.isSeq() || ScoreIndex.isWave()) {
					boolean seq = ScoreIndex.isSeq();
					trimSeq.setEnabled(seq);
					cutAtSeq.setEnabled(seq);
					exportSeq.setEnabled(seq);
					viewSeq.setText(seq ? "View this trajectory" : "View this waveform");
					zoomSeq.setText(seq ? "Zoom on this trajectory" : "Zoom on this waveform");
					moveSeq.setText(seq ? "Move this trajectory to time..." : "Move this waveform to time...");
					trimSeq.setText(seq ? "Trim this trajectory to current time selection" : "Cannot trim waveforms");
					cutAtSeq.setText(seq ? "Cut this selection at time..." : "Cannot \"cut at time\" waveforms");
					copySeq.setText(seq ? "Copy this trajectory" : "Copy this waveform");
					cuttSeq.setText(seq ? "Cut this trajectory" : "Cut this waveform");
					eraseSeq.setText(seq ? "Erase this trajectory" : "Erase this waveform");
					exportSeq.setText(seq ? "Export this trajectory..." : "Cannot export waveforms");
					scoreMenu.add(setCursor3);
					scoreMenu.addSeparator();
					scoreMenu.add(viewAll3);
					scoreMenu.add(zoomAll3);
					scoreMenu.addSeparator();
					scoreMenu.add(viewSeq);
					scoreMenu.add(zoomSeq);
					scoreMenu.addSeparator();
					scoreMenu.add(trimSeq);
					scoreMenu.add(moveSeq);
					scoreMenu.add(cutAtSeq);
					scoreMenu.addSeparator();
					scoreMenu.add(copySeq);
					scoreMenu.add(cuttSeq);
					scoreMenu.add(paste3);
					scoreMenu.add(eraseSeq);
					scoreMenu.addSeparator();
					scoreMenu.add(importSeq);
					scoreMenu.add(importWave);
					scoreMenu.add(exportSeq);
				} else {
					scoreMenu.add(setCursor1);
					scoreMenu.addSeparator();
					scoreMenu.add(viewAll1);
					scoreMenu.add(zoomAll1);
					scoreMenu.addSeparator();
					scoreMenu.add(viewTrack);
					scoreMenu.add(zoomTrack);
					scoreMenu.addSeparator();
					scoreMenu.add(joinTrack);
					scoreMenu.add(trimTrack);
					scoreMenu.add(moveTrack);
					scoreMenu.add(cutAtTrack);
					scoreMenu.addSeparator();
					scoreMenu.add(copyTrack);
					scoreMenu.add(cuttTrack);
					scoreMenu.add(paste1);
					scoreMenu.add(eraseTrack);
					scoreMenu.addSeparator();
					scoreMenu.add(importSeq);
					scoreMenu.add(importWave);
				}
			} else {
				scoreMenu.add(setCursor2);
				scoreMenu.addSeparator();
				scoreMenu.add(viewAll2);
				scoreMenu.add(zoomAll2);
				scoreMenu.addSeparator();
				scoreMenu.add(viewMultiSeq);
				scoreMenu.add(zoomMultiSeq);
				scoreMenu.addSeparator();
				scoreMenu.add(joinMultiSeq);
				scoreMenu.add(trimMultiSeq);
				scoreMenu.add(moveMultiSeq);
				scoreMenu.add(cutAtMultiSeq);
				scoreMenu.addSeparator();
				scoreMenu.add(copyMultiSeq);
				scoreMenu.add(cuttMultiSeq);
				scoreMenu.add(paste2);
				scoreMenu.add(eraseMultiSeq);
				scoreMenu.addSeparator();
				scoreMenu.add(importSeq);
				scoreMenu.add(importWave);
			}
		}
	}

	public void setParent(FloatingWindow f)
	{
		parent = f;
	}
}