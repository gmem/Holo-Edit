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
/******************           classe   CheckVisible        ****************/
/* case a cocher pour rendre une piste visible ou non */

package holoedit.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;

public class CheckRecord extends JToggleButton implements ActionListener
{
	/* numero de la piste */
	private int numero;
	private TrackSelector tks;
	private static ImageIcon onIcon = new ImageIcon("./images/recon.gif");
	private static ImageIcon offIcon = new ImageIcon("./images/recoff.gif");
	
	public CheckRecord(int numero, TrackSelector tks)
	{
		this.numero = numero;
		this.tks = tks;
		this.setSize(35, 20);
		this.setIcon(onIcon);
		this.setToolTipText("rec on/off");
		this.setBorderPainted(false);
		
		setFocusable(false);
		addActionListener(this);
	}

	public int getNumero()
	{
		return (numero);
	}

	public void check(boolean b)
	{
		setSelected(b);
		if(b)
			setIcon(onIcon);
		else
			setIcon(offIcon);
	}

	public boolean isChecked()
	{
		return isSelected();
	}

	public void actionPerformed(ActionEvent e)
	{
		int mod = e.getModifiers();// (permet de savoir si alt/ctrl on ete enfonces lors de l'action)
		// on selectionne alternativement toutes les pistes sauf celle-ci ou juste celle-ci >>>> ALT <<<<
		if (mod == 24) {
			// Si il n'est pas selectionne > Tous sauf un
			if (!isSelected())
				tks.gp.recordAllButOne(getNumero());
				// sinon Solo
			else
				tks.gp.recordSolo(getNumero());
			// on selectionne toutes ou aucune >>>> Ctrl <<<<
		} else if (mod == 18) {
			// Toutes
			if (isSelected())
			{
				tks.gp.recordAll();
				// Aucune
			} else
				tks.gp.recordNone();
			// changer la visibilite de la piste >>>> click <<<< sans touche
		} else
			tks.gp.recordTrack(getNumero(), isSelected());
		
		if(isSelected())
			setIcon(onIcon);
		else
			setIcon(offIcon);
		
		tks.gp.holoEditRef.room.display();
	}
}