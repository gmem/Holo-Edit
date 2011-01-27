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

/**
 * Fenetre de dialogue permettant a l'utilisateur de modifier les parametres des fonctions Ici sont geres 
 * l'initialisation et divers actions ergonomiques (echap ferme la fenetre, cancel, grisage automatique... 
 * L'activation et la recuperation des valeurs se fait en dehors de cette classe (GestionPistes pour les 
 * algorithmes de traitement des trajectoires).
 */
package holoedit.functions;

import holoedit.HoloEdit;
import holoedit.data.HoloFctPreset;
import holoedit.gui.FloatingWindow;
import holoedit.gui.GestionPistes;
import holoedit.gui.PresetCombo;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class AlgorithmDialog extends FloatingWindow implements KeyListener, ItemListener
{
	// size
	private static final int fieldH = 25;
	private static final int fieldD = 5;
	private static final int butshift = 8;
	private static final int minDesc = 50;
	// boutons
	public JButton Cancel = new JButton();
	public JButton Replace = new JButton();
	public JButton Continue = new JButton();
	public JButton OK = new JButton();
	JLabel[] paramTitles;
	PresetCombo presetCombo;
	// reference sur gestionPiste
	GestionPistes gp;
	// tableau des champs d'entree dont les types et les valeurs sont variables
	// suivant les fonctions
	public Param[] paramFields;
	// Algo
	Algorithm alg;
	// tableau des noms des champs
	public String[] fields;
	// tableau des valeurs des champs
	public Object[] results;
	// tableau de types de champs
	public int[] types;
	// tableau des options de ces champs
	public String[] options;
	// tableau des minimums de ces champs
	public double[] mins;
	// tableau des maxs de ces champs
	public double[] maxs;
	// tableau des modulos de ces champs
	public double[] mods;
	// booleen determinant s'il faut ou non remplacer la piste
	// ou ajouter le trajet cree en fin de piste (pour les fonctions de creation
	// et time reverse)
	private boolean replace;
	// numero de la piste active
	private int pisteActive = 0;

	public AlgorithmDialog(Algorithm a, GestionPistes _gp)
	{
		super(a.getTitle(), _gp.holoEditRef, 320, calcHeight(a), (HoloEdit.screenSize.width - 320) / 2, (HoloEdit.screenSize.height - calcHeight(a)) / 2, true);
		// initialisation
		gp = _gp;
		alg = a;
		fields = alg.getFieldsName();
		results = alg.getVals();
		types = alg.getTypes();
		options = alg.getOptions();
		mins = alg.getMins();
		maxs = alg.getMaxs();
		mods = alg.getMods();
		pisteActive = gp.getActiveTrackNb();
		presetCombo = new PresetCombo(alg.getName(), alg.getPresets());
		replace = (alg.getType() == Algorithm.TYPE_GEN || alg.getType() == Algorithm.TYPE_DATA || alg.getType() == Algorithm.TYPE_SPEAKERS || alg.getName().equalsIgnoreCase("timereverse"));
		initComponents();
	}

	private static int calcHeight(Algorithm alg)
	{
		return ((alg.getFieldsSize() + 1) * (fieldH + fieldD) + 60 + butshift + minDesc + alg.getNOL());
	}

	// init components
	@SuppressWarnings("deprecation")
	public void initComponents()
	{
		setTitle(alg.getTitle());
		unsetLayout();
		// Creation des boutons
		// dans le cas d'une fonction permettant un remplacement de la piste
		// (circle, alea, brownian, lissajou et time reverse)
		// trois boutons : cancel / replace / continue (par defaut (ENTREE)
		// continue
		if (replace)
		{
			Cancel.setText("Cancel");
			Cancel.setLocation(new java.awt.Point(13, (fields.length + 1) * (fieldH + fieldD) + butshift));
			Cancel.setVisible(true);
			Cancel.setSize(new java.awt.Dimension(90, 30));
			Cancel.addKeyListener(this);
			add(Cancel);
			Replace.setText("Replace");
			Replace.setLocation(new java.awt.Point(115, (fields.length + 1) * (fieldH + fieldD) + butshift));
			Replace.setVisible(true);
			Replace.setSize(new java.awt.Dimension(90, 30));
			Replace.addKeyListener(this);
			add(Replace);
			Replace.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					chooReplace();
				}
			});
			Continue.setText(alg.getType() == Algorithm.TYPE_SPEAKERS ? "Add" : "Continue");
			Continue.setLocation(new java.awt.Point(217, (fields.length + 1) * (fieldH + fieldD) + butshift));
			Continue.setVisible(true);
			Continue.setSize(new java.awt.Dimension(90, 30));
			Continue.addKeyListener(this);
			add(Continue);
			Continue.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					chooContinue();
				}
			});
		} else
		{
			// pour toutes les autres fonctions : boutons ok/cancel
			Cancel.setText("Cancel");
			Cancel.setLocation(new java.awt.Point(30, (fields.length + 1) * (fieldH + fieldD) + butshift));
			Cancel.setVisible(true);
			Cancel.setSize(new java.awt.Dimension(90, 30));
			Cancel.addKeyListener(this);
			add(Cancel);
			OK.setText("OK");
			OK.setLocation(new java.awt.Point(205, (fields.length + 1) * (fieldH + fieldD) + butshift));
			OK.setVisible(true);
			OK.setSize(new java.awt.Dimension(90, 30));
			OK.addKeyListener(this);
			add(OK);
			OK.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					chooContinue();
				}
			});
		}
		// PRESETS
		// label "preset"
		JLabel presetLabel = new JLabel("Presets :");
		presetLabel.setLocation(new java.awt.Point(72, 10));
		presetLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		presetLabel.setForeground(Color.black);
		presetLabel.setSize(new java.awt.Dimension(60, 20));
		presetLabel.setVisible(true);
		// ajout du presetCombo
		presetCombo.setLocation(new java.awt.Point(130, 10));
		presetCombo.addKeyListener(this);
		presetCombo.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				recallPreset();
			}
		});
		// bouton d'ajout d'un preset
		JButton addPstBut, updPstBut, delPstBut;
		try
		{
			addPstBut = new JButton(new ImageIcon("./images/preset.gif"));
			updPstBut = new JButton(new ImageIcon("./images/presetR.gif"));
			delPstBut = new JButton(new ImageIcon("./images/presetX.gif"));
		}
		catch (Exception e)
		{
			addPstBut = new JButton("Add");
			updPstBut = new JButton("Add");
			delPstBut = new JButton("Add");
		}
		addPstBut.setToolTipText("new...");
		addPstBut.setLocation(new java.awt.Point(230, 11));
		addPstBut.setSize(new java.awt.Dimension(20, 20));
		addPstBut.setEnabled(true);
		addPstBut.setVisible(true);
		addPstBut.addKeyListener(this);
		addPstBut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				createPreset();
			}
		});
		updPstBut.setToolTipText("update");
		updPstBut.setLocation(new java.awt.Point(250, 11));
		updPstBut.setSize(new java.awt.Dimension(20, 20));
		updPstBut.setEnabled(true);
		updPstBut.setVisible(true);
		updPstBut.addKeyListener(this);
		updPstBut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updatePreset();
			}
		});
		delPstBut.setToolTipText("delete");
		delPstBut.setLocation(new java.awt.Point(270, 11));
		delPstBut.setSize(new java.awt.Dimension(20, 20));
		delPstBut.setEnabled(true);
		delPstBut.setVisible(true);
		delPstBut.addKeyListener(this);
		delPstBut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deletePreset();
			}
		});
		add(presetLabel);
		add(presetCombo);
		add(addPstBut);
		add(updPstBut);
		add(delPstBut);
		// FIN PRESETS
		// -------------------------------------------------------------------------------/
		// --------------------- creation des champs
		// ---------------------------/
		paramTitles = new FieldLabel[fields.length];
		paramFields = new Param[fields.length];
		
		float maxTimeDur = (float)(gp.holoEditRef.counterPanel.getDate(2)-gp.holoEditRef.counterPanel.getDate(1))/1000;
		// boucle sur le nombre de champ de la fonction
		for (int i = 0; i < fields.length; i++)
		{
			// ----------- LABELS -----------------/
			paramTitles[i] = new FieldLabel(fields[i]);
			paramTitles[i].setLocation(new java.awt.Point(10, 12 + ((i + 1) * (fieldH + fieldD))));
			paramTitles[i].addKeyListener(this);
			add(paramTitles[i]);
			// champs de type texte
			if (types[i] == Param.TYPE_TEXT)
			{
				paramFields[i] = new TextParam(i, results[i]);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((TextParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
			// champs de type Int
			else if (types[i] == Param.TYPE_INT)
			{
				paramFields[i] = new IntParam(i, results[i], mins[i], maxs[i], mods[i]);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((IntParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
			// champs de type Float
			else if (types[i] == Param.TYPE_FLOAT)
			{
				paramFields[i] = new FloatParam(i, paramTitles[i].getText().startsWith("Duration") ? ""+maxTimeDur : results[i], mins[i], maxs[i], mods[i]);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((FloatParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
			// champs de type Double
			else if (types[i] == Param.TYPE_DOUBLE)
			{
				paramFields[i] = new DoubleParam(i, paramTitles[i].getText().startsWith("Duration") ? ""+maxTimeDur : results[i], mins[i], maxs[i], mods[i]);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((DoubleParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
			// champs de type checkBox >> donnees booleennes
			else if (types[i] == Param.TYPE_CHECK)
			{
				paramFields[i] = new CheckParam(i, results[i]);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((CheckParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
			// champs de type comboBox >> liste de choix (n° de piste...)
			else if (types[i] == Param.TYPE_COMBO)
			{
				if (options[i].equalsIgnoreCase("track"))
				{
					// affectation des numeros de pistes automatiquement en
					// fonction de la piste selectionnee
					switch(alg.getType())
					{
					case Algorithm.TYPE_GEN:
						results[1] = pisteActive+1;
						break;
					case Algorithm.TYPE_TRANS_ATOB:
						results[1] = pisteActive+1;
						results[2] = pisteActive+1;
						break;
					case Algorithm.TYPE_TRANS_ABTOC:
						results[0] = pisteActive+1;
						if (pisteActive < gp.getNbTracks() - 2)
						{
							results[1] = pisteActive + 2;
							results[2] = pisteActive + 3;
						} else if (pisteActive == gp.getNbTracks() - 2)
						{
							results[1] = pisteActive + 2;
							results[2] = 1;
						} else
						{
							results[1] = 1;
							results[2] = 2;
						}
						break;
					}
					paramFields[i] = new ComboParam(gp, i, results[i]);
					paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
					switch(alg.getType())
					{
					case Algorithm.TYPE_GEN:
					case Algorithm.TYPE_TRANS_ATOB:
						int sw;
						if(results[0].getClass().equals(String.class))
							sw = Integer.parseInt((String)results[0]);
						else
							sw = (Integer) results[0];
						switch (sw)
						{
						case 1:
						case 2:
							paramFields[i].setEnabled(false);
							break;
						default:
							paramFields[i].setEnabled(true);
							break;
						}
						break;
					case Algorithm.TYPE_TRANS_ABTOC:
						paramFields[i].setEnabled(true);
						break;
					}
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				} else if (options[i].equalsIgnoreCase("sym")) {
					paramFields[i] = new ComboParam(gp, i, "sym", results[i]);
					paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				} else if (options[i].equalsIgnoreCase("applyTo")) {
					paramFields[i] = new ComboParam(gp, i, "applyTo", results[i]);
					paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addItemListener(this);
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				} else if (options[i].equalsIgnoreCase("clock")) {
					paramFields[i] = new ComboParam(gp, i, "clock", results[i]);
					paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				}else if (options[i].equalsIgnoreCase("sdif")) {
					paramFields[i] = new ComboParam(gp, i, "sdif", results[i]);
					paramFields[i].setLocation(new java.awt.Point(160, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
					final int j = i;
					paramFields[i].addItemListener(new ItemListener(){
						public void itemStateChanged(ItemEvent e) {
							try {
								ComboParam cp = (ComboParam) paramFields[j+1];
								if (cp.getComboType().equalsIgnoreCase("sdifFields")){
									cp.updateFields(((ComboParam) paramFields[j]).getSelectedItem().toString());
								}
							}catch(ClassCastException cce){
								System.out.println(cce);
							}
						}
					});
				}else if (options[i].equalsIgnoreCase("sdifFields")) {
					paramFields[i] = new ComboParam(gp, i, "sdifFields", results[i]);
					paramFields[i].setLocation(new java.awt.Point(160, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				}else if (options[i].equalsIgnoreCase("coord")) {
					paramFields[i] = new ComboParam(gp, i, "coord", results[i]);
					paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
					paramFields[i].addKeyListener(this);
					add((ComboParam)paramFields[i]);
				}
			}
			// champs de type Button >> implementer l'actionlistener
			else if (types[i] == Param.TYPE_BUTTON)
			{
				paramFields[i] = new ButtonParam(i, results[i],alg.getFields().elementAt(i).a);
				paramFields[i].setLocation(new java.awt.Point(210, 12 + ((i + 1) * (fieldH + fieldD))));
				add((ButtonParam)paramFields[i]);
				paramFields[i].addKeyListener(this);
			}
		}
		JTextArea description = new JTextArea(alg.getDescription());
		TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Description");
		b.setTitleJustification(TitledBorder.LEFT);
		b.setTitleFont(fo);
		description.setBorder(b);
		description.setEditable(false);
		description.setOpaque(false);
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.setFont(this.fo);
		description.setVisible(true);
		description.setSize(new java.awt.Dimension(300, minDesc + alg.getNOL()));
		description.setLocation(new java.awt.Point(10, (fields.length + 1) * (fieldH + fieldD) + 40));
		description.setFocusable(false);
		add(description);
		// --------------------------------------------------------------------------/
		Cancel.setToolTipText("return to main window without any changes");
		Cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CancelMouseClicked();
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				CancelMouseClicked();
			}
		});
		JRootPane rpane = getRootPane();
		if (replace)
		{
			rpane.setDefaultButton(Continue);
			Continue.setNextFocusableComponent(presetCombo);
			Continue.setToolTipText("insert created trajectories at the end of current ones");
			Replace.setToolTipText("replace current trajectories with created ones");
		} else {
			rpane.setDefaultButton(OK);
			OK.setToolTipText("apply function");
			OK.setNextFocusableComponent(presetCombo);
		}
		setVisible(true);
		toFront();
		paramFields[0].requestFocus();
	}

	// -------------- quand on clique sur cancel ou close X ---------------/
	public void CancelMouseClicked()
	{
		setVisible(false);
		dispose();
	}

	public void close()
	{
		super.close();
		CancelMouseClicked();
	}

	public void keyReleased(KeyEvent ke)
	{
		// pour fermer la fenetre en frappant la touche echap
		switch (ke.getKeyCode())
		{
		case 27:
			CancelMouseClicked();
			break;
		default: // System.out.println("other : "+ke.getKeyCode());
			break;
		}
	}

	public void keyPressed(KeyEvent ke)
	{}

	public void keyTyped(KeyEvent ke)
	{}

	public void itemStateChanged(ItemEvent e)
	{
		// si on choisit le combo apply to sur all ou visible alors les champ de
		// selection des numeros de pistes sont grises
		ComboParam c = (ComboParam) e.getSource();
		int index = c.getSelectedIndex();
		int num = c.getNumero();
		switch (index)
		{
		case 1:
		case 2:
			paramFields[num + 1].setEnabled(false);
			if (alg.getType() == Algorithm.TYPE_TRANS_ATOB)
				paramFields[num + 2].setEnabled(false);
			break;
		default:
			paramFields[num + 1].setEnabled(true);
			if (alg.getType() == Algorithm.TYPE_TRANS_ATOB)
				paramFields[num + 2].setEnabled(true);
			break;
		}
	}

	private void createPreset()
	{
		// creation d'une fenetre de choix du nom pour le nouveau preset
		String psName = JOptionPane.showInputDialog(this, "Name of the preset : ", "Add a preset", JOptionPane.QUESTION_MESSAGE);
		if (psName != null)
		{
			if (!psName.equalsIgnoreCase("") && !psName.equalsIgnoreCase(" "))
			{
				// creation du preset
				HoloFctPreset h = new HoloFctPreset(psName, alg.getName());
				if (!alg.getPresets().contains(h)) // test s'il n'existe pas deja : meme nom et meme algo (voir HoloFctPreset)
				{
					// on cree le preset
					Object res[] = new Object[fields.length];
					for (int i = 0; i < fields.length; i++)
						res[i] = paramFields[i].getValue();
					h.setVals(res);
					// on ajoute le preset dans la combo
					presetCombo.addPreset(h);
					presetCombo.setSelectedIndex(presetCombo.getItemCount()-1);
					// dans le vecteur global des presets de gestion piste pour la sauvegarde
					alg.addPreset(h);
				} else {
					// si le preset existe on affiche un message, l'utilisateur doit de nouveau refaire la manip...
					JOptionPane.showMessageDialog(this, "A preset with the same name already exists !", "Preset name ...", JOptionPane.ERROR_MESSAGE);
				}
				// si pas de nom entre, on affiche un message.
			} else
				JOptionPane.showMessageDialog(this, "You must enter a name for this preset !", "Preset name ...", JOptionPane.ERROR_MESSAGE);
		} else
			JOptionPane.showMessageDialog(this, "You must enter a name for this preset !", "Preset name ...", JOptionPane.ERROR_MESSAGE);
	}

	private void updatePreset()
	{
		if(presetCombo.getItemCount() <= 0)
			return;
		// on cree le preset
		Object res[] = new Object[fields.length];
		for (int i = 0; i < fields.length; i++)
			res[i] = paramFields[i].getValue();
		int index = alg.getPresets().indexOf(presetCombo.getCurrentPst());
		presetCombo.updatePreset(res);
		alg.getPresets().setElementAt(presetCombo.getCurrentPst(),index);
	}
	
	private void deletePreset()
	{
		if(presetCombo.getCurrentPst() != null)
		{
			alg.getPresets().remove(alg.getPresets().indexOf(presetCombo.getCurrentPst()));
			presetCombo.deletePreset();
		}
	}
	
	public void recallPreset()
	{
		// s'il est actionne
		int size = presetCombo.getValSize();
		if (size != 0)
		{
			// on recupere les valeurs du preset.
			Object res[] = presetCombo.getVals();
			for (int i = 0; i < size; i++)
				// alors on affecte les valeurs au vecteur result.
				paramFields[i].setValue(res[i]);
			if(paramFields[0].getType() == Param.TYPE_COMBO)
			{
				ComboParam cdp = ((ComboParam) paramFields[0]);
				if(cdp.getComboType().equalsIgnoreCase("applyTo"))
				{
					switch (cdp.getSelectedIndex())
					{
					case 1:
					case 2:
						paramFields[1].setEnabled(false);
						if (alg.getType() == Algorithm.TYPE_TRANS_ATOB)
							paramFields[2].setEnabled(false);
						break;
					default:
						paramFields[1].setEnabled(true);
					if (alg.getType() == Algorithm.TYPE_TRANS_ATOB)
						paramFields[2].setEnabled(true);
					break;
					}
				}
			}
		}
	}

	public Object[] getResults()
	{
		Object[] tmp = new Object[paramFields.length];
		for (int i = 0; i < paramFields.length; i++)
			tmp[i] = paramFields[i].getValue();
		return tmp;
	}

	private void chooReplace()
	{
		if (JOptionPane.showConfirmDialog(this, "Do you really want to replace ? ", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == 0)
		{
			// On lance l'algorithme approprie avec l'option replace vraie
			alg.start(getResults(),true);
			// fermeture de la fenetre
			dispose();
		}
	}

	// Si on choisit continu, on insere la piste a la suite de l'existante
	private void chooContinue()
	{
		// On lance l'algorithme approprie avec l'option replace fausse
		alg.start(getResults(),false);
		// fermeture de la fenetre
		dispose();
	}

}
