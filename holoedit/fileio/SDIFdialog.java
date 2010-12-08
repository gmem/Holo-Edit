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
 * Fenetre de dialogue permettant a l'utilisateur de définir les parametres
 * d'importation de fichiers SDIF lorsqu'un type de matrice est inconnnu.
 */
package holoedit.fileio;

import holoedit.HoloEdit;
import holoedit.functions.Param;
import holoedit.gui.FloatingWindow;
import holoedit.gui.GestionPistes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BoxLayout;
import java.util.ArrayList;

public class SDIFdialog extends FloatingWindow {

	// boutons
	public JButton Abort = new JButton();
	public JButton jbuttonOK = new JButton("OK");
	public JButton jbuttonIGNORE = new JButton("IGNORE");
	public JRadioButton drawRadio;
	public ArrayList<JRadioButton> drawRadioButtons;
	public JRadioButton indexRadio;
	public ArrayList<JRadioButton> indexRadioButtons;
	public JTextField textField;
	public JTextArea textArea;
	public JTextArea textAreaTitles;
	public ArrayList<JTextField> textFieldArray;
	JLabel[] paramTitles;
	// tableau des noms des champs
	public Param[] paramFields;
	public String[] sdifFields;
	public String matrixType;
	private JPanel jpanButton, jpan2, jpan;
	private ButtonGroup bgDraw, bgGroup;
	private int[] selection = new int[2];
	private boolean fine = false;
	private int nrow;
	private int ncol;
	private int nbrButtons;

	public SDIFdialog(String matrixType, String[] fields, GestionPistes _gp, int nrow, int ncol) {
		super("matrix informations", _gp.holoEditRef, 400, 190 + (fields.length + 1) * 25, (HoloEdit.screenSize.width - 320) / 2,
				(HoloEdit.screenSize.height) / 2, true);
		this.matrixType = matrixType;
		sdifFields = fields;
		this.nrow = nrow;
		this.ncol = ncol;
		setFine(false);
		initComponents();
	}

	// init components
	@SuppressWarnings("deprecation")
	public void initComponents() {
		setTitle(matrixType + " matrix informations");
		JPanel pan = new JPanel();

		bgDraw = new ButtonGroup();
		bgGroup = new ButtonGroup();
		// nbr de radioButton selon le nombre de fields définis /présents
		nbrButtons = ncol < sdifFields.length ? ncol : sdifFields.length;
		// des arrayList pour faciliter la création des buttons et textfields
		drawRadioButtons = new ArrayList<JRadioButton>(nbrButtons);
		indexRadioButtons = new ArrayList<JRadioButton>(nbrButtons);
		textFieldArray = new ArrayList<JTextField>(nbrButtons);

		// on crée tout et on met dans les arraylists
		for (int i = 0; i < nbrButtons; i++) {
			drawRadio = new JRadioButton();
			drawRadio.setSize(5, 5);
			drawRadioButtons.add(drawRadio);
			drawRadio.setActionCommand(sdifFields[i]);

			indexRadio = new JRadioButton();
			indexRadio.setSize(5, 5);
			indexRadio.setActionCommand(sdifFields[i]);

			indexRadioButtons.add(indexRadio);
			textField = new JTextField(sdifFields[i]);
			textField.setSize(new Dimension(50, 22));
			textField.setPreferredSize(new Dimension(50, 22));
			textFieldArray.add(textField);
		}

		// Une ligne de plus pour pouvoir selectionner "pas d'index"  :
		indexRadio = new JRadioButton("none         ");
		indexRadio.setSize(5, 5);
		indexRadio.setActionCommand("none");
		indexRadioButtons.add(indexRadio);
		// Une ligne de plus pour pouvoir selectionner index par numero de ROW (genre f0) :
		indexRadio = new JRadioButton("row numbers");
		indexRadio.setSize(5, 5);
		indexRadio.setActionCommand("row");
		indexRadioButtons.add(indexRadio);
		// Une ligne de plus pour pouvoir selectionner index par Frame ..? :
		indexRadio = new JRadioButton("frame times ");
		indexRadio.setSize(5, 5);
		indexRadio.setActionCommand("frame");
		indexRadioButtons.add(indexRadio);

		jpan2 = new JPanel();
		jpan2.setLayout(new BoxLayout(jpan2, BoxLayout.Y_AXIS));

		textAreaTitles = new JTextArea(" Group  Draw    Matrix Fields");
		textAreaTitles.setEditable(false);
		textAreaTitles.setOpaque(false);
		textAreaTitles.setWrapStyleWord(true);
		textAreaTitles.setLineWrap(true);
		textAreaTitles.setVisible(true);
		textAreaTitles.setSize(new java.awt.Dimension(190, 50));
		textAreaTitles.setFocusable(false);
		jpan2.add(javax.swing.Box.createRigidArea(new Dimension(0, 15)));
		jpan2.add(textAreaTitles);

		for (int i = 0; i < drawRadioButtons.size(); i++) {
			bgDraw.add(drawRadioButtons.get(i));
			bgGroup.add(indexRadioButtons.get(i));
			jpan = new JPanel();
			jpan.setLayout(new BoxLayout(jpan, BoxLayout.X_AXIS));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
			jpan.add(indexRadioButtons.get(i));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(20, 0)));
			jpan.add(drawRadioButtons.get(i));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(20, 0)));
			jpan.add(textFieldArray.get(i));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(20, 0)));
			jpan.setSize(new Dimension(200, 22));
			jpan.setPreferredSize(new Dimension(200, 22));
			jpan2.add(javax.swing.Box.createRigidArea(new Dimension(0, 5)));
			jpan2.add(jpan);
		}
		// buttons
		jbuttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < nbrButtons; i++) {
					if (bgDraw.getSelection().getActionCommand() == sdifFields[i])
						selection[1] = i; // draw choice
				}
				if (bgGroup.getSelection().getActionCommand() == "none")
					selection[0] = 0;
				else if (bgGroup.getSelection().getActionCommand() == "row")
					selection[0] = -1;
				else if (bgGroup.getSelection().getActionCommand() == "frame")
					selection[0] = -2;
				else
					for (int i = 0; i < nbrButtons; i++)
						if (bgGroup.getSelection().getActionCommand() == sdifFields[i])
							selection[0] = i+1; // group choice

				sdifFields = new String[nbrButtons];
				for (int i = 0; i < nbrButtons; i++) {
					sdifFields[i] = textFieldArray.get(i).getText();
				}
				
				setFine(true);
				dispose();
			}
		});
		jbuttonIGNORE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selection[0] = -3; // index
				selection[1] = -1; // to draw column
				setFine(true);
				dispose();
			}
		});

		// pour rajouter les 3 supplémentaires
		for (int i = indexRadioButtons.size() - 3; i < indexRadioButtons.size(); i++) {
			bgGroup.add(indexRadioButtons.get(i));
			jpan = new JPanel();
			jpan.setLayout(new BoxLayout(jpan, BoxLayout.X_AXIS));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
			jpan.add(indexRadioButtons.get(i));
			jpan.add(javax.swing.Box.createRigidArea(new Dimension(185, 0)));
			jpan.setSize(new Dimension(200, 22));
			jpan.setPreferredSize(new Dimension(200, 22));
			jpan2.add(javax.swing.Box.createRigidArea(new Dimension(0, 5)));
			jpan2.add(jpan);
		}

		setDefaultSelection();

		jpan2.add(javax.swing.Box.createRigidArea(new Dimension(0, 15)));

		textArea = new JTextArea("Those fields were found in the current matrix of type " + matrixType + ".\n"
				+ "Please adjust the settings as you wish and press OK, \n " + "or press IGNORE to ignore every " + matrixType
				+ " matrix found in the sdif file. \n\n" + "SETTINGS :\n\n"
				+ "'Group' : To create different sdifData from the chosen value.\n"
				+ "'Draw' : The field to draw against time. This only affects drawing "
				+ "(every fields will be kept for eventual computations).");
		TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Informations");
		b.setTitleJustification(TitledBorder.LEFT);
		b.setTitleFont(fo);
		textArea.setBorder(b);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setFont(this.fo);
		textArea.setVisible(true);
		textArea.setSize(new java.awt.Dimension(190, 50));
		textArea.setFocusable(false);
		pan = new JPanel();
		pan.add(textArea);

		jpanButton = new JPanel();
		jpanButton.setLayout(new BoxLayout(jpanButton, BoxLayout.X_AXIS));
		jpanButton.add(javax.swing.Box.createRigidArea(new Dimension(120, 0)));
		jpanButton.add(jbuttonOK);
		jpanButton.add(javax.swing.Box.createRigidArea(new Dimension(15, 0)));
		jpanButton.add(jbuttonIGNORE);
		jpanButton.setSize((new java.awt.Dimension(200, 20)));

		setLayout(new BorderLayout());
		add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
		add(jpan2, BorderLayout.WEST);
		add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
		add(pan, BorderLayout.EAST);
		add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
		add(jpanButton, BorderLayout.SOUTH);
		setVisible(true);
		toFront();
	}

	public int[] getSelection() {
		return selection;
	}

	public String[] getFields() {
		return sdifFields;
	}

	private void setFine(boolean b) {
		fine = b;
	}

	public boolean isFine() {
		return fine;
	}

	private void setDefaultSelection() {
		// Setting default selection for 'group' choice :
		boolean gotIndexField = false;
		for (int i = 0; i < nbrButtons; i++)
			if (sdifFields[i].equalsIgnoreCase("index")) {
				bgGroup.setSelected(indexRadioButtons.get(i).getModel(), true);
				gotIndexField = true;
				break;
			}
		if (gotIndexField == false) {
			if (nrow < 2)
				bgGroup.setSelected(indexRadioButtons.get(indexRadioButtons.size() - 2).getModel(), true); // by index
			else
				bgGroup.setSelected(indexRadioButtons.get(indexRadioButtons.size() - 3).getModel(), true); // no index
		}
		// Setting default selection for 'draw' choice :
		boolean gotFrequencyField = false;
		for (int i = 0; i < nbrButtons; i++)
			if (sdifFields[i].equalsIgnoreCase("frequency")) {
				bgDraw.setSelected(drawRadioButtons.get(i).getModel(), true);
				gotFrequencyField = true;
				break;
			}
		if (gotFrequencyField == false)
			bgDraw.setSelected(drawRadioButtons.get(0).getModel(), true);
	}
}
