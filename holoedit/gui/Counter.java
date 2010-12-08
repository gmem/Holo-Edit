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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Counter extends JPanel
{
	JComponent cptHeure;
	JComponent cptMinute;
	JComponent cptSeconde;
	JComponent cptMilliSeconde;
	JComponent cptPoints;

	public Counter(int nbChamps)
	{
		// constructeur de compteur
		// creation du nombre de champs correspondant au compteur
		switch (nbChamps)
		{
			case 1:// creation du compteur de point
				cptPoints = new CTextField("4");
				break;
			case 2:// creation du compteur delta
				cptSeconde = new CTextField("1");
				cptMilliSeconde = new CTextField("0");
				break;
			case 5: // creation d'un compteur total
				cptHeure = new CLabel("0");
				cptMinute = new CLabel("0");
				cptSeconde = new CLabel("0");
				cptMilliSeconde = new CLabel("0");
				break;
			
			default: // creation d'un compteur debut, fin
				cptHeure = new CTextField("0");
				cptMinute = new CTextField("0");
				cptSeconde = new CTextField("0");
				cptMilliSeconde = new CTextField("0");
				break;
		}
		initComponents(nbChamps);
		setVisible(true);
	}
	/** initialisation des compteurs */
	public void initComponents(int nbChamps)
	{
		switch (nbChamps)
		{
			case 1: // affichage du compteur de point
				add(cptPoints);
				break;
			case 2: // affichage du compteur delta
				add(cptSeconde);
				add(cptMilliSeconde);
				break;
			default: // affichage du compteur debut, fin ou total
				// 4 champs : heure, minute, seconde , centieme de seconde
				add(cptHeure);
				add(cptMinute);
				add(cptSeconde);
				add(cptMilliSeconde);
				break;
		}
	}
}

//Sous classe pour eviter d'ecrire quarante fois la meme chose....
class CTextField extends JTextField
{
	Color fgCol = new Color(0, 0, 0);
	Color bgCol = Color.lightGray;
	int w = 20;
	int h = 15;
	Font f = new Font("courrier", Font.PLAIN, 10);
	
	public CTextField()
	{
		super(2);
		setEditable(true);
		setVisible(true);
		setAutoscrolls(false);
		setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
		setHorizontalAlignment(JTextField.RIGHT);
		setFont(f);
		setText("0");
		setSize(w, h);
		setPreferredSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w, h));
	}
	
	public CTextField(String text)
	{
		super(2);
		setEditable(true);
		setVisible(true);
		setAutoscrolls(false);
		setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
		setHorizontalAlignment(JTextField.RIGHT);
		setFont(f);
		setText(text);
		setSize(w, h);
		setPreferredSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w, h));
	}
}

// Sous classe pour eviter d'ecrire quarante fois la meme chose....
class CLabel extends JLabel
{
	Color fgCol = new Color(0, 0, 0);
	Color bgCol = Color.lightGray;
	int w = 20;
	int h = 15;
	Font f = new Font("courrier", Font.PLAIN, 10);
	
	public CLabel()
	{
		super("0");
		setVisible(true);
		setOpaque(false);
		setFocusable(false);
		setFont(f);
		setSize(w, h);
		setHorizontalAlignment(JLabel.RIGHT);
		setPreferredSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w, h));
	}
	
	public CLabel(String text)
	{
		super(text);
		setOpaque(false);
		setFocusable(false);
		setFont(f);
		setSize(w, h);
		setHorizontalAlignment(JLabel.RIGHT);
		setPreferredSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w, h));
	}
}