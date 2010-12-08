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

import holoedit.util.Ut;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class TrackSelector extends FloatingWindow
{
	// checkBox d'option (Short View, Infos Mode, Speakers)
	public JCheckBox labelPlusDelta;
	public JCheckBox hpEdition;
	// boutons et etiquettes
	public InitButton boutonInit[];
	public CheckVisible checkVisible[];
	public CheckRecord checkRecord[];
	public LabelAudio labelAudio[];
	// Tous ou Aucun
	public boolean all_none = false;
	// Tous sauf un ou Solo
	public boolean all_but_one = false;
	// Couleur du fond par defaut
	private Color bgColor;
	// reference a gestion pistes
	protected GestionPistes gp;

	public TrackSelector(GestionPistes owner)
	{
		// Track Selector
		super("Tracks", owner.holoEditRef, owner.holoEditRef.wsTrackSelW, owner.holoEditRef.wsTrackSelH, owner.holoEditRef.wlTrackSelX, owner.holoEditRef.wlTrackSelY, owner.holoEditRef.wbTrackSel);
		gp = owner;
		// Mise en forme
		// Couleur du fond par defaut
		bgColor = this.getBackground();
		labelPlusDelta = new JCheckBox();
		labelPlusDelta.setBackground(bgColor);
		/** ****** affichage des boutons et etiquettes ******** */
		boutonInit = new InitButton[gp.getNbTracks()];
		checkVisible = new CheckVisible[gp.getNbTracks()];
		checkRecord = new CheckRecord[gp.getNbTracks()];
		labelAudio = new LabelAudio[gp.getNbTracks()];
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setBackground(bgColor);
		setLayout(gridbag);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.fill = GridBagConstraints.VERTICAL;
		for (int i = 0; i < gp.getNbTracks(); ++i)
		{
			//boutonInit[i] = new InitButton(i, this);
			// Permet la reaction au passage de la souris
			// Mise en forme
			//constraints.gridx = 0;
			//constraints.gridy = i;
			//gridbag.setConstraints(boutonInit[i], constraints);
			//add(boutonInit[i]);
			// Case a cocher pour la visibilite d'une piste
			checkVisible[i] = new CheckVisible(i, this);
			checkVisible[i].check(gp.getTrack(i).isVisible());
			constraints.gridx = 0;
			constraints.gridy = i;
			gridbag.setConstraints(checkVisible[i], constraints);
			add(checkVisible[i]);
			checkRecord[i] = new CheckRecord(i, this);
			checkRecord[i].check(gp.getTrack(i).isRecEnable());
			constraints.gridx = 1;
			constraints.gridy = i;
			gridbag.setConstraints(checkRecord[i], constraints);
			add(checkRecord[i]);
			// etiquette avec numero de piste, reaction au passage de la souris
			labelAudio[i] = new LabelAudio(i, gp.getTrack(i).getColor(), bgColor, this);
			labelAudio[i].setLabelName(gp.getTrack(i).getName());
			constraints.gridx = 2;
			constraints.gridy = i;
			gridbag.setConstraints(labelAudio[i], constraints);
			add(labelAudio[i]);
		}
		initOptions(gridbag, constraints);
		pack();
		sizW = getWidth();
		sizH = getHeight();
	}

	private void initOptions(GridBagLayout gridbag, GridBagConstraints constraints)
	{
		// Case a cocher pour le mode "Short View" (vue uniquement des points entre t=begin et t=begin+delta
		labelPlusDelta = new JCheckBox();
		labelPlusDelta.setToolTipText("view tracks only between begin time and begin time + delta");
		labelPlusDelta.setFocusable(false);
		labelPlusDelta.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (int i = 0; i < gp.getNbTracks(); i++)
					labelAudio[i].Disable();
				gp.holoEditRef.shortViewMode = ((JCheckBox) e.getSource()).isSelected();
				gp.holoEditRef.room.display();
			}
		});
		// Case a cocher pour le mode "Speakers" (edition des haut-parleurs)
		hpEdition = new JCheckBox();
		hpEdition.setToolTipText("enable loudspeakers edition mode");
		hpEdition.setFocusable(false);
		hpEdition.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (int i = 0; i < gp.getNbTracks(); i++)
					labelAudio[i].Disable();
				gp.holoEditRef.hpEditMode = ((JCheckBox) e.getSource()).isSelected();
				gp.holoEditRef.room.initVars(true);
			}
		});
		constraints.gridx = 1;
		constraints.gridy = gp.getNbTracks() + 1;
		gridbag.setConstraints(labelPlusDelta, constraints);
		add(labelPlusDelta);
		// Labels associes a ces cases a cocher
		// Short View
		JLabel label = new JLabel();
		label.setVisible(true);
		label.setOpaque(false);
		label.setFocusable(false);
		label.setText("  Short view");
		label.setToolTipText("view tracks only between begin time and begin time + delta");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("courrier", Font.PLAIN, 10));
		label.setForeground(new Color(40, 40, 100));
		label.setMaximumSize(new Dimension(80, 30));
		label.setMinimumSize(new Dimension(80, 30));
		label.setPreferredSize(new Dimension(80, 30));
		label.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				labelPlusDelta.setSelected(!labelPlusDelta.isSelected());
				gp.holoEditRef.shortViewMode = labelPlusDelta.isSelected();
				for (int i = 0; i < gp.getNbTracks(); i++)
					labelAudio[i].Disable();
				gp.holoEditRef.room.display();
			}

			// reaction au passage de la souris
			public void mouseEntered(MouseEvent e)
			{
				JLabel lab = (JLabel) e.getSource();
				lab.setForeground(Color.black);
			}

			public void mouseExited(MouseEvent e)
			{
				JLabel lab = (JLabel) e.getSource();
				lab.setForeground(new Color(40, 40, 100));
			}
		});
		constraints.gridx = 2;
		constraints.gridy = gp.getNbTracks() + 1;
		gridbag.setConstraints(label, constraints);
		add(label);
		constraints.gridx = 1;
		constraints.gridy = gp.getNbTracks() + 2;
		gridbag.setConstraints(hpEdition, constraints);
		add(hpEdition);
		// Speakers
		JLabel label3 = new JLabel();
		label3.setVisible(true);
		label3.setOpaque(true);
		label3.setText("  Speakers");
		label3.setFocusable(false);
		label3.setToolTipText("enable speakers edition mode");
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		label3.setVerticalAlignment(SwingConstants.CENTER);
		label3.setFont(new Font("courrier", Font.PLAIN, 10));
		label3.setForeground(new Color(40, 40, 100));
		label3.setMaximumSize(new Dimension(80, 25));
		label3.setMinimumSize(new Dimension(80, 25));
		label3.setPreferredSize(new Dimension(80, 25));
		label3.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				for (int i = 0; i < gp.getNbTracks(); i++)
					labelAudio[i].Disable();
				hpEdition.setSelected(!hpEdition.isSelected());
				gp.holoEditRef.hpEditMode = hpEdition.isSelected();
				gp.holoEditRef.room.initVars(true);
			}

			// reaction au passage de la souris
			public void mouseEntered(MouseEvent e)
			{
				JLabel lab = (JLabel) e.getSource();
				lab.setForeground(Color.black);
			}

			public void mouseExited(MouseEvent e)
			{
				JLabel lab = (JLabel) e.getSource();
				lab.setForeground(new Color(40, 40, 100));
			}
		});
		constraints.gridx = 2;
		constraints.gridy = gp.getNbTracks() + 2;
		gridbag.setConstraints(label3, constraints);
		add(label3);
		// Initialisation des ces cases a cocher
		labelPlusDelta.setSelected(false);
		hpEdition.setSelected(false);
	}

	public String toString()
	{
		return "\t<trackselector"+super.toString();
	}

	public void resetPositionAndSize()
	{
		super.resetPositionAndSize();
		pack();
		sizW = getWidth();
		sizH = getHeight();
	}
}
