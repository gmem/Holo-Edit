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
/***************      classe BoutonInit        *******************/
/* bouton d'initialisation d'une piste */
package holoedit.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

class InitButton extends JButton implements ActionListener
{
	/* numero de la piste */
	private int numero;
	// attribut graphiques
	private int h = 25;
	private int w = 50;
	private TrackSelector tks;

	public InitButton(int numero, TrackSelector tks)
	{
		this.numero = numero;
		this.tks = tks;
		setForeground(Color.darkGray);
		setFont(new Font("courrier", Font.PLAIN, 10));
		setText("init");
		this.setMaximumSize(new Dimension(w, h));
		this.setMinimumSize(new Dimension(w, h));
		this.setPreferredSize(new Dimension(w, h));
		this.setToolTipText("init this track");
		setFocusable(false);
		addActionListener(this);
	}

	public int getNumero()
	{
		return (numero);
	}

	// Si control enfonce lors du clique sur un des boutons init : initialisation de toutes les pistes.
	// Si alt alors initialisation de toutes les pistes visibles.
	// Sinon initialisation de la piste concernee
	public void actionPerformed(ActionEvent e)
	{
		for (int c = 0; c < tks.gp.getNbTracks(); c++)
			tks.labelAudio[c].Disable();
		int mod = e.getModifiers() - 16;
		if (mod == ActionEvent.ALT_MASK)
		{
			tks.gp.initVisibleTracks();
		} else if (mod == ActionEvent.CTRL_MASK)
		{
			tks.gp.initTracks(false);
		} else
		{
			tks.gp.initOneTrack(getNumero());
		}
	}
}
