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
/*******************        class  LabelAudio             *******************/
/***************** etiquette portant le nom d'une piste ******************/
package holoedit.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.Caret;

public class LabelAudio extends JTextField implements KeyListener, MouseListener
{
	/* numero de piste */
	int number;
	/* couleur de la piste */
	Color color;
	Color selCouleur;// = new Color(180,180,180);
	Color selCouleur2;
	/* couleur du fond */
	Color bg;
	/* nom de la piste */
	String name;
	Border b;
	// taille
	int MAX = 10;
	// etat du label
	boolean edit = false;
	boolean active = false;
	TrackSelector tks;
	TrackPopup tkPopup;
	// fonte
	Font ft = new Font("courrier", Font.BOLD, 14);

	public LabelAudio(int numero, Color couleur, Color bg, TrackSelector tks)
	{
		// initialisation
		super();
		this.number = numero;
		this.color = couleur;
		this.bg = bg;
		this.tks = tks;
		setForeground(couleur);
		setBackground(Color.GRAY);
		setOpaque(tks.gp.getTrack(number).isLocked());
		setFont(ft);
		name = "Track " + (numero + 1);
		setText(name);
		setHorizontalAlignment(JTextField.CENTER);
		setMaximumSize(new Dimension(100, 25));
		setMinimumSize(new Dimension(100, 25));
		setPreferredSize(new Dimension(100, 25));
		setToolTipText("click to make this track active & visible");
		setEditable(false);
		setDragEnabled(false);
		setFocusable(false);
		selCouleur = getBackground();
		selCouleur2 = getSelectionColor();
		setSelectionColor(selCouleur);
		b = getBorder();
		setBorder(null);
		addKeyListener(this);
		addMouseListener(this);
		tkPopup = new TrackPopup(tks.gp,numero);
		add(tkPopup);
	}

	public int getNumber()
	{
		return (number);
	}

	public String getName()
	{
		return (name);
	}

	public void setLabelName(String s)
	{
		name = s;
		setText(s);
	}

	// quand la piste est active
	public void activate()
	{
		Disable();
		active = true;
		setFont(new Font("courrier", Font.BOLD, 18));
	}

	// quand la piste est inactive
	public void desactivate()
	{
		Disable();
		active = false;
		setFont(new Font("courrier", Font.BOLD, 14));
	}

	// quand le focus clavier ou souris est sur cette piste (fleche haut et bas)
	public void highlight()
	{
		setForeground(color.darker());
		setSelectedTextColor(color.darker());
	}

	// quand le focus clavier ou souris est sur une autre piste (fleche haut et bas)
	public void delight()
	{
		setForeground(color);
		setSelectedTextColor(color);
	}

	// passer en mode editable pour modifier le nom de la piste
	public void Enable()
	{
		edit = true;
		setFocusable(true);
		requestFocus();
		for (int i = 0; i < tks.labelAudio.length; i++)
			if (i != number)
				tks.labelAudio[i].Disable();
		setEditable(true);
		setSelectionColor(selCouleur2);
		selectAll();
		setBorder(b);
		Caret c = getCaret();
		c.setVisible(true);
	}

	// passer en mode non editable
	public void Disable()
	{
		edit = false;
		setFocusable(true);
		if (getText().length() > MAX)
		{
			name = getText().substring(0, MAX);
		} else
			name = getText();
		setText(name);
		tks.gp.getTrack(number).setName(name);
		setEditable(false);
		setSelectionColor(selCouleur);
		setBorder(null);
		Caret c = getCaret();
		c.setVisible(false);
	}

	public int getColor()
	{
		return color.getRGB();
	}

	public void setColor(int rgb)
	{
		color = new Color(rgb);
		tks.gp.tracks.get(number).setColor(color);
		delight();
	}

	// reaction au touches du clavier pour modifier le nom de la piste
	// ENTREE rend la piste active
	// HAUT et BAS donne le "focus" a la piste precedante/suivante
	public void keyPressed(KeyEvent e)
	{
		// Si on est en mode edition
		if (edit)
		{
			// ENTREE ferme l'edition
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				Disable();
				tks.gp.holoEditRef.room.display();
				// UP de meme mais passe le focus a la piste precedante
			} else if (e.getKeyCode() == KeyEvent.VK_UP)
			{
				Disable();
				if (number > 0)
					tks.labelAudio[number - 1].Enable();
				else
					tks.labelAudio[tks.gp.getNbTracks() - 1].Enable();
				// DOWN de meme mais passe le focus a la piste suivante
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				Disable();
				if (number < tks.gp.getNbTracks() - 1)
					tks.labelAudio[number + 1].Enable();
				else
					tks.labelAudio[0].Enable();
			}
			// Si on est pas en mode edition
		} else
		{
			// ENTREE rend la piste active
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				activate();
				tks.gp.selectTrack(number);
				// UP de meme mais passe le focus a la piste precedante
			} else if (e.getKeyCode() == KeyEvent.VK_UP)
			{
				delight();
				if (number > 0)
				{
					tks.labelAudio[number - 1].highlight();
					tks.labelAudio[number - 1].requestFocus();
				} else
				{
					tks.labelAudio[tks.gp.getNbTracks() - 1].highlight();
					tks.labelAudio[tks.gp.getNbTracks() - 1].requestFocus();
				}
				// DOWN de meme mais passe le focus a la piste suivante
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				delight();
				if (number < tks.gp.getNbTracks() - 1)
				{
					tks.labelAudio[number + 1].highlight();
					tks.labelAudio[number + 1].requestFocus();
				} else
				{
					tks.labelAudio[0].highlight();
					tks.labelAudio[0].requestFocus();
				}
				// Si F2, on renomme la piste
			} else if (e.getKeyCode() == KeyEvent.VK_F2)
				Enable();
			else if (e.getKeyCode() == KeyEvent.VK_W)
			{
//				Main.print("LabelAudio"+number+" send a key "+KeyEvent.getKeyText(e.getKeyCode()));
//				boolean c = tks.gp.mainRef.sendKey(e.getKeyCode(), e.getModifiers());
//				if (c)
					tks.close();
			}
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		// Si ALT enfonce ou double clic, on edite le texte du label
		if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1)
		{
			Enable();
			// Sinon on selectionne la piste
		} else if (e.isShiftDown())
		{
			tks.gp.changeColor(getNumber());
		} else if (e.isControlDown() || e.getButton() == MouseEvent.BUTTON3){
			tkPopup.show(this,e.getX(),e.getY());
		} else
		{
			tks.gp.selectTrack(getNumber());
		}
		tks.gp.holoEditRef.room.display();
	}

	// Reaction au survol de la souris
	public void mouseEntered(MouseEvent e)
	{
		highlight();
	}

	public void mouseExited(MouseEvent e)
	{
		delight();
	}
	
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}

	public String toString()
	{
		return "\t<label num=\""+number+"\" name=\""+name+"\" color=\""+color.getRGB()+"\"/>\n";
	}
}