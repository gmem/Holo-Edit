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
import holoedit.data.HoloWaveForm;
import holoedit.data.HoloExternalData;
import holoedit.data.HoloSDIFdata;
import holoedit.fileio.WaveFormReader;
import holoedit.fileio.SDIFreader;
import holoedit.fileio.TextFileReader;
import holoedit.util.MacFileManager;
import holoedit.util.Ut;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;

// FEATURE SOUNDPOOL PREECOUTE OU PATCH MAX SIMPLE
public class SoundPoolGUI extends FloatingWindow implements DropTargetListener, DragGestureListener {

	private static Boolean supportsDnD;
	private HoloEdit holoEditRef;
	private DynamicTree soundTree;
	private DynamicTree dataTree;
	private DefaultMutableTreeNode selectedSndNode;
	private DefaultMutableTreeNode selectedDtNode;
	private JTextPane info;
	/** the popupMenu opened by a right-click on the soundTree */
	private JPopupMenu popupSound;
	/** the popupMenu opened by a right-click on the dataTree */
	private JPopupMenu popupData;
	private WaveFormRenderer waveView;
	private JSplitPane split, split2, split3;
	private Font font = new Font("Default", Font.PLAIN, 9);
	private JMenuItem importSndMI = new JMenuItem("Import soundfile...");
	private JMenuItem removeSndMI = new JMenuItem("Remove");
	private JMenuItem updateSndMI = new JMenuItem("Update");
	private JMenuItem clearSndMI = new JMenuItem("Clear");
	private JMenuItem importDtMI = new JMenuItem("Import datafile...");
	private JMenuItem removeDtMI = new JMenuItem("Remove");
	private JMenu displayDtMI = new JMenu("Display field...");
	private JMenuItem updateDtMI = new JMenuItem("Update");
	private JMenuItem clearDtMI = new JMenuItem("Clear");
	public boolean fine = false;
	public boolean done = false;
	public int error = WaveFormReader.NO_ERROR;
	public String errorFileName;
	public HoloWaveForm last;
	public HoloExternalData lastXtdt;
	private Vector<File> unfound;
	private File soundFolder;
	private File externalDataFolder;
	private File dataFolder;
	private boolean choosingfolder;
	private boolean acceptDrag = false;
	private HoloExternalData droppedExternalData = null;
	private Vector<HoloSDIFdata> sdifDataToDraw = new Vector<HoloSDIFdata>();
	private Vector<HoloSDIFdata> sdifDataToDrawFromSndTree = new Vector<HoloSDIFdata>();
	public Vector<String> doneAndFineData = new  Vector<String>();

	public SoundPoolGUI(HoloEdit owner) {
		super("Sound Pool", owner, owner.wsSoundPoolW, owner.wsSoundPoolH, owner.wlSoundPoolX, owner.wlSoundPoolY,
				owner.wbSoundPool);
		setResizable(true);
		holoEditRef = owner;
		soundTree = new DynamicTree("Imported Sounds", false);
		dataTree = new DynamicTree("Imported Data", true);
		soundTree.addTreeSelectionListener(new WaveformTreeSelectionListener());
		dataTree.addTreeSelectionListener(new DataTreeSelectionListener());
		popupSound = new JPopupMenu();
		popupSound.add((java.awt.Component) importSndMI);
		popupSound.add(removeSndMI);
		popupSound.addSeparator();
		popupSound.add(updateSndMI);
		popupSound.add(clearSndMI);
		soundTree.add(popupSound);
		popupData = new JPopupMenu();
		popupData.add(importDtMI);
		popupData.add(removeDtMI);
//		popupData.add(new JSeparator(SwingConstants.HORIZONTAL));
		popupData.addSeparator();
		popupData.add(updateDtMI);
		popupData.add(displayDtMI);
		popupData.add(clearDtMI);
		dataTree.add(popupData);

		soundTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// La ligne suivante est impérative pour l'affichage du jpopupmenu sous XP				
				popupSound.setLightWeightPopupEnabled(false);
				if ((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3) {
					if (selectedSndNode == null) {
						removeSndMI.setEnabled(false);
						popupSound.show(e.getComponent().getParent(), e.getX(), e.getY());
						return;
					}
					// remove autorisé si le noeud n'est pas la racine
					removeSndMI.setEnabled(!selectedSndNode.isRoot());
					 popupSound.show(e.getComponent().getParent(), e.getX(), e.getY());
					return;
				}
			}
		});

		dataTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// les 2 lignes suivantes sont impératives pour l'affichage correct du jpopupmenu sous XP
				JPopupMenu.setDefaultLightWeightPopupEnabled(false); // pour les JRadioButtonMenuItem
				popupData.setLightWeightPopupEnabled(false);
				if ((e.isControlDown() && Ut.MAC) || e.getButton() == MouseEvent.BUTTON3) {
					if (selectedDtNode == null) {
						displayDtMI.setEnabled(false);
						removeDtMI.setEnabled(false);
						popupData.show(e.getComponent().getParent(), e.getX(), e.getY());
						return;
					}
					displayDtMI.removeAll();
					if (selectedDtNode.isLeaf()) {
						HoloSDIFdata hsdifdt = (HoloSDIFdata) selectedDtNode.getUserObject();
						ButtonGroup group = new ButtonGroup();
						int nbFields = hsdifdt.getFields().length;
						JRadioButtonMenuItem[] radioButtons = new JRadioButtonMenuItem[nbFields];
						for (int i=0; i<nbFields; i++){
							radioButtons[i] = new JRadioButtonMenuItem(hsdifdt.getFields()[i], true);
							displayDtMI.add(radioButtons[i]);
							group.add(radioButtons[i]);
							final int index = i;
							radioButtons[i].addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									HoloSDIFdata hsdifdt = (HoloSDIFdata) selectedDtNode.getUserObject();
									hsdifdt.setValuesToDrawIndex(index);
									hsdifdt.setDirty(true);
									updateWaveView();
								}
							});
						}
						radioButtons[hsdifdt.getValuesToDrawIndex()].setSelected(true);
						displayDtMI.setEnabled(true);
					}else {
						displayDtMI.setEnabled(false);
					}
					removeDtMI.setEnabled(true);
					popupData.show(e.getComponent().getParent(), e.getX(), e.getY());					
					return;
				}
			}
		});

		removeSndMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popupData.setVisible(false);
				if (!selectedSndNode.isRoot()) {
					int result = -1;
					try{
						// la selection est une waveform
						HoloWaveForm hwf = (HoloWaveForm) selectedSndNode.getUserObject();
						result = JOptionPane
						.showConfirmDialog(
								null,
								"This action will remove all instances of this waveform in the score\nand cannot be undone.\n" +
								"Are you sure ?",
								"Remove Waveform", JOptionPane.OK_CANCEL_OPTION);
						if (result == 0) {
							holoEditRef.modify();
							for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
								tk.remove(hwf);
							for (HoloTrack tk : holoEditRef.gestionPistes.undoTracks)
								tk.remove(hwf);
							for (HoloTrack tk : holoEditRef.gestionPistes.redoTracks)
								tk.remove(hwf);
							holoEditRef.gestionPistes.undoTrack.remove(hwf);
							holoEditRef.gestionPistes.redoTrack.remove(hwf);
							holoEditRef.gestionPistes.soundPool.remove(hwf);
							soundTree.removeCurrentNode();
							selectedSndNode = null;
							holoEditRef.score.display();
						}

						hwf.setWaveBuffer(null);
						hwf = null;	
						System.gc();
					}catch(ClassCastException cce) {
						// la selection est une data						
						HoloExternalData hxtdt = (HoloExternalData) selectedSndNode.getUserObject();
						result = JOptionPane.showConfirmDialog(	null,
								"This action will detach the selected data from the waveform.\n",
								"Detach data", JOptionPane.OK_CANCEL_OPTION);
						if (result == 0) {
							holoEditRef.modify();
							hxtdt = (HoloExternalData) selectedSndNode.getUserObject();
							if (hxtdt.getFileType().equals("SDIF") || hxtdt.getFileType().equals("txt")) {
								HoloSDIFdata hsdifdt = (HoloSDIFdata) hxtdt;
								HoloWaveForm linkedWave = (HoloWaveForm) ((DefaultMutableTreeNode) selectedSndNode.getParent()).getUserObject();
								linkedWave.removeLinkedData(hsdifdt);
						//		sdifDataToDraw.clear();
								sdifDataToDrawFromSndTree.clear();		
								updateSoundTree((DefaultMutableTreeNode) selectedSndNode.getParent());

							} else {
								// TODO if necessary
							}
							holoEditRef.score.display();
						}
					}
				}
			}
		});
		
		removeDtMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(	null,
								"This action will remove all instances of those data in both the soundPool and the score.\n" +
								"and cannot be undone.\n" +
								"Are you sure ?",
								"Remove Data", JOptionPane.OK_CANCEL_OPTION);
				if (result == 0) {
					holoEditRef.modify();
					HoloExternalData hxtdt = (HoloExternalData) selectedDtNode.getUserObject();
					if (hxtdt.getFileType().equals("SDIF") || hxtdt.getFileType().equals("TXT")) {
						HoloSDIFdata hsdifdt = (HoloSDIFdata) hxtdt;

						for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
							tk.remove(hsdifdt, false);
						for (HoloTrack tk : holoEditRef.gestionPistes.undoTracks)
							tk.remove(hsdifdt, false);
						for (HoloTrack tk : holoEditRef.gestionPistes.redoTracks)
							tk.remove(hsdifdt, false);
						holoEditRef.gestionPistes.undoTrack.remove(hsdifdt, false);
						holoEditRef.gestionPistes.redoTrack.remove(hsdifdt, false);
										
						sdifDataToDraw.clear();
						sdifDataToDrawFromSndTree.clear();						
						// remove de la soundPool
						Vector<HoloWaveForm> sounds = holoEditRef.gestionPistes.soundPool.getSounds();
						for (HoloWaveForm h : sounds)
							h.removeLinkedData(hsdifdt);
						// remove de la dataPool
						holoEditRef.gestionPistes.externalDataPool.remove(hsdifdt);
						// TODO virer
						  // On libère l'objet : 
						hsdifdt.sdifTreeMap = null;
						hsdifdt = null;	
						System.gc();					 
					} else {
						// TODO if necessary
					}
					// si la data a remover est selectionnée dans le sound tree
					// TODO pareil avec les peres du dataTree si ca entraine
					// leur remove
					if (selectedSndNode != null && selectedSndNode.getUserObject() == selectedDtNode.getUserObject())
						updateSoundTree(null);
					else
						updateSoundTree(selectedSndNode);
					dataTree.removeCurrentNode();
					updateDataTree();
					selectedDtNode = null;
					holoEditRef.score.display();
					
				}
			}
		});

		importSndMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				holoEditRef.gestionPistes.importSound();
			}
		});

		importDtMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				holoEditRef.gestionPistes.importExternalData();
			}
		});

		updateSndMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				soundTree.clear();
				updateSoundTree(null);
			}
		});

		updateDtMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataTree.clear();
				updateDataTree();
			}
		});
		clearSndMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "This action will remove all the soundpool waveforms\n"
						+ "(waveform instances in the score will be kept)\n" + "Are you sure ?", "Clear Sounds",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == 0) {
					soundTree.clear();
					holoEditRef.gestionPistes.soundPool.clear();
					updateSoundTree(null);
				}
				System.gc();
			}
		});
		clearDtMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "This action will remove all the soundpool data\n"
						+ "(data instances in the score will be kept)\n" + "Are you sure ?", "Clear Data",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == 0) {
					dataTree.clear();
					holoEditRef.gestionPistes.externalDataPool.clear();
					updateDataTree();
				}
				System.gc();
			}
		});
		info = new JTextPane();
		info.setEditable(false);
		info.setText("Sound/Data informations.");
		info.setFont(font);
		info.setOpaque(false);
		info.setEditable(false);
		info.setDragEnabled(true);
		info.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		waveView = new WaveFormRenderer(owner);
		waveView.setBorder(BorderFactory.createLoweredBevelBorder());
		split3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, soundTree, dataTree);
		split3.setOneTouchExpandable(true);
		split3.setBorder(BorderFactory.createEmptyBorder());
		split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split3, info);
		split2.setOneTouchExpandable(true);
		split2.setBorder(BorderFactory.createEmptyBorder());
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, split2, waveView);
		split.setOneTouchExpandable(false);
		split.setDividerSize(0);
		split.setDividerLocation(250);
		split2.setDividerLocation(220);
		split3.setDividerLocation(115);
		add(split, BorderLayout.CENTER);
		if (supportsDnD()) {
			makeDropTarget(soundTree.getTree());
			makeDropTarget(dataTree.getTree());
		} else
			System.out.println("Drag and drop is not supported with this JVM");
		unfound = new Vector<File>();
	}
/**
 * 
 * @param toSelec Si un noeud particulier doit etre sélectionné.
 * 
 */
	public void updateSoundTree(final DefaultMutableTreeNode toSelec) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Vector<HoloWaveForm> sounds = holoEditRef.gestionPistes.soundPool.getSounds();
				soundTree.clear();
				for (int i=0; i< sounds.size(); i++) {
					HoloWaveForm h = sounds.get(i);
					DefaultMutableTreeNode parent = soundTree.addObject(h);
					HoloWaveForm hwf = (HoloWaveForm) h;
					DefaultMutableTreeNode childNode = null;
					for (Object child : hwf.getLinkedDatas()) {
						if (child != null)
							childNode = soundTree.addObject(parent, child);
					}
					// FIXME  try catch vite fait pour corriger un bug. a voir.
					try {
						if (toSelec != null) {
							if (toSelec.toString().equals(parent.toString()))
								soundTree.getTree().getSelectionModel().setSelectionPath(new TreePath(childNode.getPath()));
						}else if (i==(sounds.size()-1)){ // expand du dernier import
							soundTree.getTree().expandPath(new TreePath(parent.getPath()));
							soundTree.getTree().getSelectionModel().setSelectionPath(new TreePath(parent.getPath()));
						}
					} catch(NullPointerException e ) {
						System.err.println("null");
					}
				}
				soundTree.repaint();
			}
		});
	}

	public void updateDataTree() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Vector<HoloExternalData> dataVector = holoEditRef.gestionPistes.externalDataPool.getSDIFdatas();
				dataTree.clear();
				if (dataVector.size()==0) {
					selectedDtNode = null;
				}else {
					for (int i=0; i<dataVector.size(); i++) {
						HoloExternalData data = dataVector.get(i);
						if (data.getFileType().equalsIgnoreCase("SDIF") || data.getFileType().equalsIgnoreCase("txt")) {
							HoloSDIFdata sdifData = (HoloSDIFdata) data;
							DefaultMutableTreeNode granpa = dataTree.addObject(sdifData);
							for (HoloSDIFdata child : sdifData.children) {
								DefaultMutableTreeNode dad = dataTree.addObject(granpa, child);
								for (HoloSDIFdata child2 : child.children)
									dataTree.addObject(dad,child2);
							}
							if (i==dataVector.size()-1){ // expand du dernier import
								dataTree.getTree().expandPath(new TreePath(granpa.getPath()));
								dataTree.getTree().getSelectionModel().setSelectionPath(new TreePath(granpa.getPath()));
							}
						}else{							
							// TODO other format management
						}
					}
				}
				dataTree.repaint();
			}
		});
	}

	public void clear() {
		soundTree.clear();
		Ut.print("...2");
		dataTree.clear();
		Ut.print("...3");
		holoEditRef.gestionPistes.soundPool.clear();
		Ut.print("...4");
		holoEditRef.gestionPistes.externalDataPool.clear();
		Ut.print("...5");
		unfound.clear();
		Ut.print("...6");
	}

	public void repaint() {
		super.repaint();
	}

	private void makeDropTarget(final java.awt.Component c) {
		if (c.getParent() != null) {
			// Make drop target && drag source
			@SuppressWarnings("unused")
			final DropTarget dt = new DropTarget(c, this);
			final DragSource ds = new DragSource();
			ds.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY_OR_MOVE, this);
		}
	}

	public void importSound(File f, boolean verboseError) {
		done = false;
		if (f.getName().length() <= 32) {
			Thread t = new Thread(new SoundFileImporter(f, verboseError));
			t.setPriority(Thread.MAX_PRIORITY);
			t.setName("SF-ImportDeamon");
			t.setDaemon(true);
			t.start();
			
			// to be sure waveform are load in time / session load
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			if (verboseError)
				Ut.alert("Import Error", "Please rename the soundfile, for compatibility reasons with Max/MSP,"
						+ "\nit cannot be longer than 32 characters (including the extension).");
			error = WaveFormReader.PATH_ERROR;
			done = true;
			fine = false;
		}
	}

	public void importData(File f, String importOptions, boolean verboseError) {
		done = false;
		Thread t = new Thread(new DataFileImporter(f, importOptions, verboseError));
		t.setPriority(Thread.MAX_PRIORITY);
		t.setName("SF-ImportDeamon");
		t.setDaemon(true);
		t.start();
		
		// to be sure waveform are load in time / session load
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// SELECTION LISTENER
	// *******************************************************
	class WaveformTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			selectedSndNode = (DefaultMutableTreeNode) soundTree.getLastSelectedPathComponent();
			sdifDataToDrawFromSndTree.clear();
			if (selectedSndNode == null || selectedSndNode.isRoot()) {
				info.setText("Sound informations.");
				waveView.setHoloWaveForm(null);
			} else {

				Object nodeInfo = selectedSndNode.getUserObject();
				try {
					HoloWaveForm hwf = (HoloWaveForm) nodeInfo;
					info.setText(hwf.getInfo());
					waveView.setHoloWaveForm(hwf);
					// on dessine les tous enfants (data) s'il y en a
					if (!selectedSndNode.isLeaf()) {
						for (Enumeration<?> children = selectedSndNode.children(); children.hasMoreElements();) {
							DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
							// si on le dessine pas deja a partir de dataTree
							if (!sdifDataToDraw.contains(childNode.getUserObject()))
								sdifDataToDrawFromSndTree.add((HoloSDIFdata) childNode.getUserObject());
						}
					}
				} catch (ClassCastException cce) {
					try {
						// si on le dessine pas deja a partir de dataTree
						if (!sdifDataToDraw.contains(nodeInfo))
							sdifDataToDrawFromSndTree.add((HoloSDIFdata) nodeInfo);

						// Dessin du pere (une holowaveform)
						try {
							DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedSndNode.getParent();
							HoloWaveForm hwf = (HoloWaveForm) parentNode.getUserObject();
							info.setText(hwf.getInfo());
							waveView.setHoloWaveForm(hwf);
						} catch (ClassCastException cce3) {
							info.setText("Sound informations.");
							waveView.setHoloWaveForm(null);
						}
					} catch (ClassCastException cce2) {
						info.setText("Sound informations.");
						waveView.setHoloWaveForm(null);
					}
				}
			}
			updateWaveView();
		}
	}

	class DataTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath rootPath = new TreePath(dataTree.rootNode);
			boolean b = dataTree.getTree().getSelectionModel().isPathSelected(rootPath);
			if (b == true)
				dataTree.getTree().getSelectionModel().removeSelectionPath(rootPath);

			TreePath[] selectedPaths = dataTree.getSelectionPaths(); // to get every selected node

			sdifDataToDraw.clear();
			if (selectedPaths == null) {
				info.setText("File informations.");
			} else {
				try {
					for (int i = 0; i < selectedPaths.length; i++) {
						selectedDtNode = (DefaultMutableTreeNode) selectedPaths[i].getLastPathComponent();
						HoloSDIFdata hxtdt = (HoloSDIFdata) selectedDtNode.getUserObject();
						if (!sdifDataToDrawFromSndTree.contains(hxtdt))
							sdifDataToDraw.add(hxtdt);
					}

					if (sdifDataToDraw.size() == 1)
						if (sdifDataToDraw.get(0) != null){
							info.setText(sdifDataToDraw.get(0).getInfo());
						}else{
							info.setText("Data informations.");
						}
					else
						info.setText("Multi-Selection..");
				} catch (ClassCastException cce) {
					// other formats
					info.setText("Data informations.");
				}

			}
			updateWaveView();
		}
	}

	// //////////////////////////////////////////////////////////////////////

	public void dragGestureRecognized(DragGestureEvent dge) {
		if (selectedSndNode != null)
			if (dge.getComponent().equals(soundTree.getTree()) && !selectedSndNode.isRoot()) {
				try {
					selectedSndNode = (DefaultMutableTreeNode) soundTree.getLastSelectedPathComponent();
					if (selectedSndNode == null)
						return;
					Object nodeInfo = selectedSndNode.getUserObject();

					HoloWaveForm hwf = (HoloWaveForm) nodeInfo;
					holoEditRef.score.droppedWaveForm = hwf;
					holoEditRef.score.acceptDrag = true;

				} catch (ClassCastException cce) {

					holoEditRef.score.droppedWaveForm = null;

					try {
						selectedSndNode = (DefaultMutableTreeNode) soundTree.getLastSelectedPathComponent();
						Object nodeInfo = selectedSndNode.getUserObject();

						holoEditRef.score.droppedData = (HoloExternalData) nodeInfo;
						holoEditRef.score.acceptDrag = true;

					} catch (ClassCastException cce2) {
						holoEditRef.score.droppedData = null;
						holoEditRef.score.acceptDrag = false;
					}
				}
			}
		// pour drag du dataTree vers le soundTree ou vers le score
		if (selectedDtNode != null)
			if (dge.getComponent().equals(dataTree.getTree())) {
				try {
					selectedDtNode = (DefaultMutableTreeNode) dataTree.getLastSelectedPathComponent();
					if (selectedDtNode == null) {
						return;
					}
					Object nodeInfo = selectedDtNode.getUserObject();
					HoloExternalData data = (HoloExternalData) nodeInfo;
					if (data.getDataType().equalsIgnoreCase("SDIF")) // drag du plus haut pere interdit
						return;
					this.droppedExternalData = data;
					this.acceptDrag = true;

					holoEditRef.score.droppedData = data;
					holoEditRef.score.acceptDrag = true;

				} catch (ClassCastException cce) {
					this.droppedExternalData = null;
					this.acceptDrag = false;
					holoEditRef.score.droppedData = null;
					holoEditRef.score.acceptDrag = false;
				}
			}
	}

	private void updateWaveView() {
		Vector<HoloSDIFdata> ar = new Vector<HoloSDIFdata>(sdifDataToDraw);
		ar.addAll(sdifDataToDrawFromSndTree);
		HoloSDIFdata[] hsdifdt = new HoloSDIFdata[ar.size()];
		hsdifdt = ar.toArray(hsdifdt);
		waveView.setHoloSDIFdataTab(hsdifdt);
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		if (isDragOk(dtde))
			dtde.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
		else if (acceptDrag && droppedExternalData != null) {
			// dtde.getDropTargetContext().dropComplete(true);
		} else
			dtde.rejectDrag();
	}

	public void dragOver(DropTargetDragEvent dtde) {
		if (acceptDrag && droppedExternalData != null)
			try {
				TreePath pathTarget = soundTree.getTree().getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
				soundTree.getTree().setSelectionPath(pathTarget);

			} catch (NullPointerException e) {

			}
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		if (isDragOk(dtde))
			dtde.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
		else
			dtde.rejectDrag();
	}

	public void dragExit(DropTargetEvent dte) {
	}

	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
				List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
				File[] filesTemp = new File[fileList.size()];
				fileList.toArray(filesTemp);
				final File[] files = filesTemp;
				filesDropped(files);
				dtde.getDropTargetContext().dropComplete(true);
			} else if (acceptDrag && droppedExternalData != null) {
				try {
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedSndNode.getParent();
					if (parentNode.equals(soundTree.rootNode))
						parentNode = selectedSndNode;

					if (droppedExternalData.getFileType().equalsIgnoreCase("SDIF") || droppedExternalData.getFileType().equalsIgnoreCase("TXT")) {
						HoloSDIFdata hsdifdt = (HoloSDIFdata) droppedExternalData;
						HoloWaveForm wave = (HoloWaveForm) parentNode.getUserObject();
						// on vérifie que l'enfant n'existe pas deja
						if (!wave.containsLinkedData(hsdifdt)) {
							wave.addLinkedData(hsdifdt);
							// on dé-selectionne le noeud du dataTree :
							dataTree.getTree().getSelectionModel().clearSelection();
							// update du soundTree avec selection du dernier child
							// ajouté à parentNode
							updateSoundTree(parentNode);
						}
					}
				} catch (java.lang.ClassCastException e) {
					this.droppedExternalData = null;
					this.acceptDrag = false;
					dtde.rejectDrop();
				} catch (java.lang.NullPointerException e) {
					this.droppedExternalData = null;
					this.acceptDrag = false;
					dtde.rejectDrop();
				}
				this.droppedExternalData = null;
				this.acceptDrag = false;
			} else
				dtde.rejectDrop();
		} catch (java.io.IOException io) {
			System.out.println("FileDrop: IOException - abort:");
			io.printStackTrace();
			dtde.rejectDrop();
		} catch (java.awt.datatransfer.UnsupportedFlavorException ufe) {
			System.out.println("FileDrop: UnsupportedFlavorException - abort:");
			ufe.printStackTrace();
			dtde.rejectDrop();
		} finally {
		}
	}

	/** Determine if the dragged data is a file list. */
	private boolean isDragOk(final java.awt.dnd.DropTargetDragEvent evt) {
		boolean ok = false;
		DataFlavor[] flavors = evt.getCurrentDataFlavors();
		evt.getSource();

		int i = 0;
		while (!ok && i < flavors.length) {
			// each element is required/guaranteed to be of type java.io.File.
			if (flavors[i].equals(DataFlavor.javaFileListFlavor)) {
				ok = true;
			}
			i++;
		}
		return ok;
	}

	public void filesDropped(File[] files) {
		Vector<File> filesToImport = new Vector<File>(1, 1);
		Vector<File> filesToImport2 = new Vector<File>(1, 1);
		for (File f : files) {
			if (f.isDirectory()) {
				File[] contentFile;
				if (Ut.MAC) {
					MacFileManager OSXFileManager = new MacFileManager(holoEditRef);
					// allowing sound and data files
					contentFile = OSXFileManager.SoundAndDataFileFilter(f.listFiles());
				} else
					// allowing sound and data files
					contentFile = f.listFiles(holoEditRef.gestionPistes.sndNdtFilter);

				for (File cf : contentFile)
					if ((cf.getName().substring(cf.getName().lastIndexOf('.') + 1).equalsIgnoreCase("SDIF")) || (cf.getName().substring(cf.getName().lastIndexOf('.') + 1).equalsIgnoreCase("txt"))) {
						filesToImport2.add(cf);
					} else
						filesToImport.add(cf);
			} else {
				if ((f.getName().substring(f.getName().lastIndexOf('.') + 1).equalsIgnoreCase("SDIF")) || (f.getName().substring(f.getName().lastIndexOf('.') + 1).equalsIgnoreCase("txt"))) {
					filesToImport2.add(f);
				} else
					filesToImport.add(f);
			}
		}
		for (File _files : filesToImport) { // changed
			System.out.println("Importing " + _files.getAbsolutePath());
			importSound(_files, true);
		}
		for (File _files : filesToImport2) { // changed
			System.out.println("Importing " + _files.getAbsolutePath());
			importData(_files, null, true);
		}
		holoEditRef.modify();
	}

	class SoundFileImporter implements Runnable {
		File f;
		boolean verbose = true;
		File cacheFile;
		JFileChooser chooser;

		public SoundFileImporter(File _f, boolean verb) {
			f = _f;
			verbose = verb;
			cacheFile = new File(Ut.dir(Ut.APP_PATH) + WaveFormReader.tmpdir + f.getName() + WaveFormReader.tmpser);
			chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		public void run() {
			if (f != null && ((f.exists() && f.isFile()) || (cacheFile.exists() && cacheFile.isFile()))) {
				done = false;
				fine = false;
				if (!holoEditRef.gestionPistes.soundPool.contains(new HoloWaveForm(f))) {
					WaveFormReader wfr = new WaveFormReader(f, holoEditRef);
					try {
						while (!wfr.isDone()) {
							Thread.sleep(20);
							// System.out.println("soundpool-import-waiting");
							// System.out.print('.');
						}
						// System.out.println();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					done = true;
					if (wfr.isFine()) {
						HoloWaveForm[] arr = wfr.getHoloWaveForms();
						for (HoloWaveForm hwf : arr)
							holoEditRef.gestionPistes.soundPool.add(hwf);
						updateSoundTree(null);
						last = arr[arr.length - 1];
						fine = true;
						if (wfr.getError() == -1)
							Ut.alert("File not found", f.getName() + " can't be found,\nusing it's cached waveform.");
					} else {
						error = wfr.getError();
						errorFileName = wfr.soundFile.getName();
						if (verbose)
							switch (error) {
							case WaveFormReader.NO_ERROR:
								break;
							case WaveFormReader.MONO_ERROR:
								Ut.alert("Error", "Only mono file for instance,\nOnly the left channel has been read.");
								break;
							case WaveFormReader.TYPE_ERROR:
								Ut.alert("Import Error", errorFileName + " is not a supported file type.");
								break;
							case WaveFormReader.PATH_ERROR:
								Ut.alert("Import Error","Please rename the soundfile, for compatibility reasons with Max/MSP," +
										"\nit cannot be longer than 32 characters (including the extension).");
								break;
							case WaveFormReader.PATH2_ERROR:
								Ut.alert("Import Error","Please rename the soundfile, for compatibility reasons with Max/MSP," +
										"\nit cannot" +	" contain blank spaces.");
								break;
							case WaveFormReader.SSDII_NORSRC_ERROR:
								Ut.alert("Import Error", "This Sound Designer II file has no resource and cannot be read.");
								break;
							case WaveFormReader.SSDII_RSRC_ERROR:
								Ut.alert("Import Error", "There are errors in this Sound Designer II resource file.");
								break;
							default:
								Ut.alert("Import Error", "Problem while loading soundfile, aborted.");
								break;
							}
					}
					wfr.stop();
					wfr = null;
					System.gc();
					//System.runFinalization();
				} else {
					done = true;
					fine = true;
					last = new HoloWaveForm(f);
				}
			} else {
				done = true;
				fine = false;
				error = WaveFormReader.FILE_ERROR;
				unfound.add(f);
				if (!choosingfolder) {
					choosingfolder = true;
					Ut.alert("File not found", f.getName() + " can't be found,\neven it's cached waveform.\nLocalize ... ");
				} 
				
				// TODO : choose missing souinds
				//else {
//					try {
//						
//						while (choosingfolder) {
//							// System.out.println("soundpool-folder-localize-waiting");
//							Thread.sleep(500);
//						}
//					} catch (InterruptedException e) {
//					}
//				}
				if (soundFolder != null) {
					File[] sounds = soundFolder.listFiles();
					for (int k = unfound.size() - 1; k >= 0; k--) {
						File fil = unfound.get(k);
						for (int i = 0; i < sounds.length; i++) {
							if (sounds[i].getName().equalsIgnoreCase(fil.getName())) {
								holoEditRef.soundPoolGui.importSound(sounds[i], true);
								unfound.remove(fil);
								holoEditRef.room.display();
								holoEditRef.modify();
							}
						}
					}
				} else {
					//Ut.print("choosing missing file");
					String basedir;
					if(f.getParentFile() != null && f.getParentFile().exists())
						basedir = f.getParent();
					else
						basedir = holoEditRef.gestionPistes.holoDirectory;
					//Ut.print("dir_"+basedir+"_");
							
					
					chooser.setCurrentDirectory(new File(basedir));
					
					chooser.setDialogTitle("Choose a folder for missing sounds ( "+f.getName()+" )");
					//Ut.print("choosing missing file 22");
					
//					Runnable chooser_run = new Runnable() {
//				        public void run() {
//				        	chooser.showDialog(null, "Choose");//
//				        }
//				     
//					};
//					
//					try {
//						SwingUtilities.invokeAndWait(chooser_run);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					// TODO : choose missing souinds
					//chooser.showDialog(holoEditRef.soundPoolGui, "Choose");//
					
					//Ut.print("choosing missing file 23");
					if (chooser.getSelectedFile() != null) {
						File tmp = chooser.getSelectedFile();
						if (tmp.exists() && tmp.isDirectory()) {
							soundFolder = tmp;
							choosingfolder = false;
							File[] sounds = soundFolder.listFiles();
							for (int k = unfound.size() - 1; k >= 0; k--) {
								File fil = unfound.get(k);
								for (int i = 0; i < sounds.length; i++) {
									if (sounds[i].getName().equalsIgnoreCase(fil.getName())) {
										holoEditRef.soundPoolGui.importSound(sounds[i], true);
										unfound.remove(fil);
										holoEditRef.room.display();
										holoEditRef.modify();
									}
								}
							}
						}
					}
				}
				if (!unfound.isEmpty())
					Ut.alert("File not found", f.getName() + " can't still be found, aborted.");
			}
		}
	}
	
	public void clearDoneAndFineData(){
		Vector<String> toremove = new Vector<String>();
		for (String data : doneAndFineData){
			if (!holoEditRef.gestionPistes.externalDataPool.contains(new HoloExternalData(new File(data))))
				toremove.add(data);
		}
		for (String data : toremove)
			doneAndFineData.remove(data);
	}
	
	class DataFileImporter implements Runnable {
		File f;
		String importOptions;
		boolean verbose = true;
		
		public DataFileImporter(File _f, String _importOptions, boolean verb) {
			f = _f;
			importOptions = _importOptions;
			verbose = verb;
		}

		public void run() {
			if (f != null && (f.exists() && f.isFile()) ) {
				done = false;
				fine = false;
				if (!holoEditRef.gestionPistes.externalDataPool.contains(new HoloExternalData(f))) {
					String extension = f.getName().substring(f.getName().lastIndexOf('.') + 1);
					SoftReference<SDIFreader> sdifReaderRef;
					SDIFreader sdifReader = null;
					SoftReference<TextFileReader> textReaderRef;
					TextFileReader textReader = null;
					if (extension.equalsIgnoreCase("SDIF")) {
						sdifReaderRef = new SoftReference<SDIFreader>(new SDIFreader(f, importOptions, holoEditRef));
						sdifReader = sdifReaderRef.get();
					} else {
						textReaderRef = new SoftReference<TextFileReader>(new TextFileReader(f));
						textReader = textReaderRef.get();
					}
					if (sdifReader != null) {
						try {
							while (!sdifReader.isDone()) {
								Thread.sleep(20);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						done = true;
						if (sdifReader.isFine()) {
							HoloSDIFdata[] arr = sdifReader.getHoloSDIFdatas();
							if (arr != null) {
								for (HoloSDIFdata hdt : arr)
									holoEditRef.gestionPistes.externalDataPool.add(hdt);
								updateDataTree();
								lastXtdt = arr[arr.length - 1];
							}
							doneAndFineData.add(f.getName());
							fine = true;
							if (sdifReader.getError() == -1)
								Ut.alert("File not found", f.getName() + " can't be found,\nusing it's cached waveform.");
						} else {
							error = sdifReader.getError();
							errorFileName = sdifReader.sdifFile.getName();
							if (verbose)
								switch (error) {
								case SDIFreader.NO_ERROR:
									break;
								case SDIFreader.TYPE_ERROR:
									Ut.alert("Import Error", errorFileName + " is not a supported file type.");
									break;
								case TextFileReader.TEXT_FORMAT_ERROR:
									Ut.alert("Import Error", "This text file cannot be imported in holo-Edit.\n" +
											"The data it contains are not properly arranged.\n" +
											"(Columns must be separated by whitespace characters only)");
									break;
								default:
									Ut.alert("Import Error", "Problem while loading datafile, aborted.");
									break;
								}
						}
						sdifReader.stop();
						sdifReader = null;
						System.gc();
					//	System.runFinalization();
					} else if (textReader!=null){
						try {
							while (!textReader.isDone()) {
								Thread.sleep(20);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						done = true;
						if (textReader.isFine()) {
							HoloSDIFdata[] arr = textReader.getHoloSDIFdatas();
							if (arr != null) {
								for (HoloSDIFdata hdt : arr)
									holoEditRef.gestionPistes.externalDataPool.add(hdt);
								updateDataTree();
								lastXtdt = arr[arr.length - 1];
							}
							fine = true;
							doneAndFineData.add(f.getName());
							if (textReader.getError() == -1)
								Ut.alert("File not found", f.getName() + " can't be found,\nusing it's cached waveform.");
						} else {
							error = textReader.getError();
							errorFileName = textReader.textFile.getName();
							if (verbose)
								switch (error) {
								case SDIFreader.NO_ERROR:
									break;
								case SDIFreader.TYPE_ERROR:
									Ut.alert("Import Error", errorFileName + " is not a supported file type.");
									break;
								case TextFileReader.TEXT_FORMAT_ERROR:
									Ut.alert("Import Error", "This text file cannot be imported in holo-Edit.\n" +
											"Be sure the data it contains are properly arranged.\n" +
											"(Columns must be separated by whitespace characters only)");
									break;
								default:
									Ut.alert("Import Error", "Problem while loading datafile, aborted.");
									break;
								}
						}
						textReader.stop();
						textReader = null;
						System.gc();
						//System.runFinalization();
					}
				} else {
					doneAndFineData.add(f.getName());
					done = true;
					fine=true;
					lastXtdt = new HoloExternalData(f);
				}
			} else {
				done = true;
				fine = false;
				error = SDIFreader.FILE_ERROR;
				unfound.add(f);
				if (!choosingfolder) {
					choosingfolder = true;
					Ut.alert("File not found", f.getName() + " can't be found.\nLocalize ... ");
				} else {
					try {
						while (choosingfolder) {
							// System.out.println("soundpool-folder-localize-waiting");
							Thread.sleep(500);
						}
					} catch (InterruptedException e) {
					}
				}
				if (externalDataFolder != null) {
					File[] datas = externalDataFolder.listFiles();
					for (int k = unfound.size() - 1; k >= 0; k--) {
						File fil = unfound.get(k);
						for (int i = 0; i < datas.length; i++) {
							if (datas[i].getName().equalsIgnoreCase(fil.getName())) {
								holoEditRef.soundPoolGui.importData(datas[i], importOptions, true);
								unfound.remove(fil);
								holoEditRef.room.display();
								holoEditRef.modify();
							}
						}
					}
				} else {
					JFileChooser chooser = new JFileChooser(f.getParentFile().exists() ? f.getParent()
							: holoEditRef.gestionPistes.holoDirectory);
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Choose a folder for missing data files ( "+f.getName()+" )");
					
					
					// TODO : choose missing SDIF
					//chooser.showDialog(holoEditRef.soundPoolGui, "Choose");
					
					
					if (chooser.getSelectedFile() != null) {
						File tmp = chooser.getSelectedFile();
						if (tmp.exists() && tmp.isDirectory()) {
							dataFolder = tmp;
							choosingfolder = false;
							File[] datas = dataFolder.listFiles();
							for (int k = unfound.size() - 1; k >= 0; k--) {
								File fil = unfound.get(k);
								for (int i = 0; i < datas.length; i++) {
									if (datas[i].getName().equalsIgnoreCase(fil.getName())) {
										holoEditRef.soundPoolGui.importData(datas[i], importOptions, true);
										unfound.remove(fil);
										holoEditRef.room.display();
										holoEditRef.modify();
									}
								}
							}
						}
					}
				}
				if (!unfound.isEmpty())
					Ut.alert("File not found", f.getName() + " can't still be found, aborted.");
			}
		}
	}

	public int getSplitDivPos() {
		return split2.getDividerLocation();
	}

	public void setSplitDivPos(int i) {
		split2.setDividerLocation(i);
	}

	public static boolean supportsDnD() { // Static Boolean
		if (supportsDnD == null) {
			boolean support = false;
			try {
				Class.forName("java.awt.dnd.DnDConstants");
				support = true;
			} // end try
			catch (Exception e) {
				support = false;
			} // end catch
			supportsDnD = new Boolean(support);
		} // end if: first time through
		return supportsDnD.booleanValue();
	} // end supportsDnD

	public String toString() {
		return "\t<soundpool  split=\"" + getSplitDivPos() + "\"" + super.toString();
	}

	public void componentResized(ComponentEvent e) {
		super.componentResized(e);
		split.setDividerLocation(250);
	}
	
	public void focusGained(FocusEvent e) {
		updateMenuBar();
		if (holoEditRef.helpWindowOpened)
			holoEditRef.helpWindow.jumpToIndex("#soundPool", true);
	}
}