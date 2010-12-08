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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CounterPanel extends JPanel //FloatingWindow
{
	private Counter begin;
	private Counter end;
	private Counter total;
	private Counter delta;
	private Counter pointsNumber;
	private Counter cursor;
	private Counter player;
	private CounterCalculation calculBegin;
	private CounterCalculation calculEnd;
	private CounterCalculation calculTotal;
	private CounterCalculation calculDelta;
	private CounterCalculation calculCursor;
	private CounterCalculation calculPlayer;
	private int nbPoints; // nombre de points intermediaires utilises pour tracer le prochain segment de pistes
	// booleens indiquant si drag souris sur un compteur 
	private boolean dragBegin = false;
	private boolean dragEnd = false;
	private boolean dragCursor = false;
	private boolean dragDelta = false;
	private boolean dragPoint = false;
	private int coordY; // ancienne coordonnee de la souris sur l'axe des ordonnees
	private int timeRefValue; // ancienne valeur dans le compteur temps centisec
	// gestionnaire de mise en page pour les cadres tempsModif et deltaModif                                                                       
	private GridBagLayout grid = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();
	protected Font fo = new Font("Verdana",0, 10);
	private HoloEdit holoEditRef;
	
	public JCheckBox lineMode;

	// constructeur par defaut
	public CounterPanel(HoloEdit owner)
	{
//		super("Counters", owner, owner.wsCountW, owner.wsCountH, owner.wlCountX, owner.wlCountY, owner.wbCount);
		holoEditRef = owner;
//		setResizable(false);
		nbPoints = 4;
		begin = new Counter(4);
		begin.setToolTipText("Time from which tracks are displayed");
		end = new Counter(4);
		end.setToolTipText("Time until which tracks are displayed");
		total = new Counter(5);
		total.setToolTipText("Total end time (not editable)");
		delta = new Counter(2);
		delta.setToolTipText("Time between two inserted points or time for short view mode");
		pointsNumber = new Counter(1);
		pointsNumber.setToolTipText("Number of intermediary points between two inserted points");
		cursor = new Counter(4);
		cursor.setToolTipText("Cursor time");
		player = new Counter(5);
		player.setToolTipText("Play time or start time");
		calculBegin = new CounterCalculation();
		calculEnd = new CounterCalculation();
		calculTotal = new CounterCalculation();
		calculDelta = new CounterCalculation(0, 0, 1, 0);
		calculCursor = new CounterCalculation();
		calculPlayer = new CounterCalculation();
		
		lineMode = new JCheckBox();
		lineMode.setToolTipText("enable line segment OSC transfert");
		
		setLayout(grid);
		JLabel tot, deb, fi, del, nbp, cu, pl,lm;
		tot = new JLabel("  Total :");
		deb = new JLabel("  Begin :");
		fi = new JLabel("  End :");
		cu = new JLabel("  Cursor :");
		pl = new JLabel("  Chrono :");
		del = new JLabel("  Delta :");
		nbp = new JLabel("  Points :");
		lm = new JLabel(" Line Mode :");
		tot.setFont(fo);
		deb.setFont(fo);
		fi.setFont(fo);
		del.setFont(fo);
		nbp.setFont(fo);
		cu.setFont(fo);
		pl.setFont(fo);
		lm.setFont(fo);
		tot.setHorizontalAlignment(JLabel.RIGHT);
		deb.setHorizontalAlignment(JLabel.RIGHT);
		fi.setHorizontalAlignment(JLabel.RIGHT);
		del.setHorizontalAlignment(JLabel.RIGHT);
		nbp.setHorizontalAlignment(JLabel.RIGHT);
		cu.setHorizontalAlignment(JLabel.RIGHT);
		pl.setHorizontalAlignment(JLabel.RIGHT);
		lm.setHorizontalAlignment(JLabel.RIGHT);
		tot.setFocusable(false);
		deb.setFocusable(false);
		fi.setFocusable(false);
		del.setFocusable(false);
		nbp.setFocusable(false);
		cu.setFocusable(false);
		pl.setFocusable(false);
		lm.setFocusable(false);
		total.setFocusable(false);
		begin.setFocusable(false);
		end.setFocusable(false);
		delta.setFocusable(false);
		pointsNumber.setFocusable(false);
		cursor.setFocusable(false);
		player.setFocusable(false);
		lineMode.setFocusable(false);
		lineMode.setHorizontalAlignment(JCheckBox.CENTER);
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		grid.setConstraints(tot, constraints);
		add(tot);
		constraints.gridwidth = 3;
		constraints.weightx = 3;
		constraints.gridx = 1;
		constraints.gridy = 0;
		grid.setConstraints(total, constraints);
		add(total);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 1;
		grid.setConstraints(deb, constraints);
		add(deb);
		constraints.gridwidth = 3;
		constraints.weightx = 3;
		constraints.gridx = 1;
		constraints.gridy = 1;
		grid.setConstraints(begin, constraints);
		add(begin);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		grid.setConstraints(fi, constraints);
		add(fi);
		constraints.gridwidth = 3;
		constraints.weightx = 3;
		constraints.gridx = 1;
		constraints.gridy = 2;
		grid.setConstraints(end, constraints);
		add(end);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 3;
		grid.setConstraints(cu, constraints);
		add(cu);
		constraints.gridwidth = 3;
		constraints.weightx = 3;
		constraints.gridx = 1;
		constraints.gridy = 3;
		grid.setConstraints(cursor, constraints);
		add(cursor);		
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 4;
		grid.setConstraints(pl, constraints);
		add(pl);
		constraints.gridwidth = 3;
		constraints.weightx = 3;
		constraints.gridx = 1;
		constraints.gridy = 4;
		grid.setConstraints(player, constraints);
		add(player);		
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 5;
		grid.setConstraints(del, constraints);
		add(del);
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		constraints.gridx = 1;
		constraints.gridy = 5;
		grid.setConstraints(delta, constraints);
		add(delta);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 6;
		grid.setConstraints(nbp, constraints);
		add(nbp);
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		constraints.gridx = 1;
		constraints.gridy = 6;
		grid.setConstraints(pointsNumber, constraints);
		add(pointsNumber);
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 7;
		grid.setConstraints(lm, constraints);
		add(lm);
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		constraints.gridx = 1;
		constraints.gridy = 7;
		grid.setConstraints(lineMode, constraints);
		add(lineMode);
		//------- GESTION DES DIVERS EVENEMENTS 
		//------- gestion evenements d'action         
		Action action = new Action();
		((JTextField) begin.cptHeure).addActionListener(action);
		((JTextField) begin.cptMinute).addActionListener(action);
		((JTextField) begin.cptSeconde).addActionListener(action);
		((JTextField) begin.cptMilliSeconde).addActionListener(action);
		((JTextField) end.cptHeure).addActionListener(action);
		((JTextField) end.cptMinute).addActionListener(action);
		((JTextField) end.cptSeconde).addActionListener(action);
		((JTextField) end.cptMilliSeconde).addActionListener(action);
		((JTextField) cursor.cptHeure).addActionListener(action);
		((JTextField) cursor.cptMinute).addActionListener(action);
		((JTextField) cursor.cptSeconde).addActionListener(action);
		((JTextField) cursor.cptMilliSeconde).addActionListener(action);
		((JTextField) delta.cptSeconde).addActionListener(action);
		((JTextField) delta.cptMilliSeconde).addActionListener(action);
		((JTextField) pointsNumber.cptPoints).addActionListener(action);
		
		lineMode.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				holoEditRef.connection.setLineMode(((JCheckBox) e.getSource()).isSelected());
			}
		});
		//------ gestion des evenements de focus
		Focus focus = new Focus();
		begin.cptHeure.addFocusListener(focus);
		begin.cptMinute.addFocusListener(focus);
		begin.cptSeconde.addFocusListener(focus);
		begin.cptMilliSeconde.addFocusListener(focus);
		end.cptHeure.addFocusListener(focus);
		end.cptMinute.addFocusListener(focus);
		end.cptSeconde.addFocusListener(focus);
		end.cptMilliSeconde.addFocusListener(focus);
		cursor.cptHeure.addFocusListener(focus);
		cursor.cptMinute.addFocusListener(focus);
		cursor.cptSeconde.addFocusListener(focus);
		cursor.cptMilliSeconde.addFocusListener(focus);
		delta.cptSeconde.addFocusListener(focus);
		delta.cptMilliSeconde.addFocusListener(focus);
		pointsNumber.cptPoints.addFocusListener(focus);
		//------ gestion des evenements souris
		// gestion des deplacements ( drag , move )
		SourisMotion sourisMotion = new SourisMotion();
		begin.cptHeure.addMouseMotionListener(sourisMotion);
		begin.cptMinute.addMouseMotionListener(sourisMotion);
		begin.cptSeconde.addMouseMotionListener(sourisMotion);
		begin.cptMilliSeconde.addMouseMotionListener(sourisMotion);
		end.cptHeure.addMouseMotionListener(sourisMotion);
		end.cptMinute.addMouseMotionListener(sourisMotion);
		end.cptSeconde.addMouseMotionListener(sourisMotion);
		end.cptMilliSeconde.addMouseMotionListener(sourisMotion);
		cursor.cptHeure.addMouseMotionListener(sourisMotion);
		cursor.cptMinute.addMouseMotionListener(sourisMotion);
		cursor.cptSeconde.addMouseMotionListener(sourisMotion);
		cursor.cptMilliSeconde.addMouseMotionListener(sourisMotion);
		delta.cptSeconde.addMouseMotionListener(sourisMotion);
		delta.cptMilliSeconde.addMouseMotionListener(sourisMotion);
		pointsNumber.cptPoints.addMouseMotionListener(sourisMotion);
		// gestion des clics 
		SourisAction sourisAction = new SourisAction();
		begin.cptHeure.addMouseListener(sourisAction);
		begin.cptMinute.addMouseListener(sourisAction);
		begin.cptSeconde.addMouseListener(sourisAction);
		begin.cptMilliSeconde.addMouseListener(sourisAction);
		end.cptHeure.addMouseListener(sourisAction);
		end.cptMinute.addMouseListener(sourisAction);
		end.cptSeconde.addMouseListener(sourisAction);
		end.cptMilliSeconde.addMouseListener(sourisAction);
		cursor.cptHeure.addMouseListener(sourisAction);
		cursor.cptMinute.addMouseListener(sourisAction);
		cursor.cptSeconde.addMouseListener(sourisAction);
		cursor.cptMilliSeconde.addMouseListener(sourisAction);
		delta.cptSeconde.addMouseListener(sourisAction);
		delta.cptMilliSeconde.addMouseListener(sourisAction);
		pointsNumber.cptPoints.addMouseListener(sourisAction);
		// gestion du claver 
		KeyAction keyAction = new KeyAction();
		begin.cptHeure.addKeyListener(keyAction);
		begin.cptMinute.addKeyListener(keyAction);
		begin.cptSeconde.addKeyListener(keyAction);
		begin.cptMilliSeconde.addKeyListener(keyAction);
		end.cptHeure.addKeyListener(keyAction);
		end.cptMinute.addKeyListener(keyAction);
		end.cptSeconde.addKeyListener(keyAction);
		end.cptMilliSeconde.addKeyListener(keyAction);
		cursor.cptHeure.addKeyListener(keyAction);
		cursor.cptMinute.addKeyListener(keyAction);
		cursor.cptSeconde.addKeyListener(keyAction);
		cursor.cptMilliSeconde.addKeyListener(keyAction);
		delta.cptSeconde.addKeyListener(keyAction);
		delta.cptMilliSeconde.addKeyListener(keyAction);
		pointsNumber.cptPoints.addKeyListener(keyAction);
	}

	//----------------- Methodes --------------------/  	
	public void verif()
	{
		/**verification de la saisie des temps des compteurs*/
		if (calculEnd.inferieur(calculBegin))
			calculEnd.setValeur(calculBegin.getValHeure(), calculBegin.getValMinute(), calculBegin.getValSeconde(), calculBegin.getValMilliSeconde());
		if (calculTotal.inferieur(calculEnd))
			calculEnd.setValeur(calculEnd.getValHeure(), calculEnd.getValMinute(), calculEnd.getValSeconde(), calculEnd.getValMilliSeconde());
		if (nbPoints > 99)
			nbPoints = 99;
		if (nbPoints < 0)
			nbPoints = 0;
		if (calculDelta.toMilliSeconde() >= 60000)
			calculDelta.setValeurDelta(60, 00);
		if (calculDelta.toMilliSeconde() < 100)
			calculDelta.setValeurDelta(0, 100);
		affiche(1);
		affiche(2);
		affiche(3);
		affiche(4);
		affiche(5);
		affiche(6);
		affiche(7);
		holoEditRef.score.cursorTime = calculCursor.toMilliSeconde();
		holoEditRef.connection.setBegin(calculBegin.toMilliSeconde());
		holoEditRef.connection.setEnd(calculEnd.toMilliSeconde());
	}

	private int stringToInt(String champ)
	{
		char tabValCpt[]; // a comme valeur "champ" sous forme d'un tableau
		int i, j = 1, tmp, val = 0; //val est un element du tableau tabValCpt
		tabValCpt = champ.toCharArray(); //convertion String --> char[]
		for (i = tabValCpt.length - 1; i >= 0; i--)
		{ //convertion char[] --> int
			tmp = tabValCpt[i] - 48;
			if (tmp >= 0 && tmp <= 9)
			{
				val = val + tmp * j;
				j = j * 10;
			} else
			{
				val = 0;
				break;
			}
		}
		return val;
	}

	// renvoie le nombre de points intermediaire courant
	public int getPointsInter()
	{
		return nbPoints;
	}

	// rafraichissement des compteurs
	public void affiche(int choix)
	{
		switch (choix)
		{
		case 1: //rafraichissement du compteur calculDebut
			((JTextField) begin.cptHeure).setText((new Integer(calculBegin.getValHeure())).toString());
			((JTextField) begin.cptMinute).setText((new Integer(calculBegin.getValMinute())).toString());
			((JTextField) begin.cptSeconde).setText((new Integer(calculBegin.getValSeconde())).toString());
			((JTextField) begin.cptMilliSeconde).setText((new Integer(calculBegin.getValMilliSeconde())).toString());
			break;
		case 2: //rafraichissement du compteur calculFin
			((JTextField) end.cptHeure).setText((new Integer(calculEnd.getValHeure())).toString());
			((JTextField) end.cptMinute).setText((new Integer(calculEnd.getValMinute())).toString());
			((JTextField) end.cptSeconde).setText((new Integer(calculEnd.getValSeconde())).toString());
			((JTextField) end.cptMilliSeconde).setText((new Integer(calculEnd.getValMilliSeconde())).toString());
			break;
		case 3: //rafraichissement du compteur calculTotal
			((JLabel) total.cptHeure).setText((new Integer(calculTotal.getValHeure())).toString());
			((JLabel) total.cptMinute).setText((new Integer(calculTotal.getValMinute())).toString());
			((JLabel) total.cptSeconde).setText((new Integer(calculTotal.getValSeconde())).toString());
			((JLabel) total.cptMilliSeconde).setText((new Integer(calculTotal.getValMilliSeconde())).toString());
			break;
		case 4: //rafraichissement du compteur calculDelta
			((JTextField) delta.cptSeconde).setText((new Integer(calculDelta.getValSeconde())).toString());
			((JTextField) delta.cptMilliSeconde).setText((new Integer(calculDelta.getValMilliSeconde())).toString());
			break;
		case 5: //rafraichissement du compteur points
			((JTextField) pointsNumber.cptPoints).setText(new Integer(nbPoints).toString());
			break;
		case 6: //rafraichissement du compteur cursor
			((JTextField) cursor.cptHeure).setText((new Integer(calculCursor.getValHeure())).toString());
			((JTextField) cursor.cptMinute).setText((new Integer(calculCursor.getValMinute())).toString());
			((JTextField) cursor.cptSeconde).setText((new Integer(calculCursor.getValSeconde())).toString());
			((JTextField) cursor.cptMilliSeconde).setText((new Integer(calculCursor.getValMilliSeconde())).toString());
			break;
		case 7: //rafraichissement du compteur calculTotal
			((JLabel) player.cptHeure).setText((new Integer(calculPlayer.getValHeure())).toString());
			((JLabel) player.cptMinute).setText((new Integer(calculPlayer.getValMinute())).toString());
			((JLabel) player.cptSeconde).setText((new Integer(calculPlayer.getValSeconde())).toString());
			((JLabel) player.cptMilliSeconde).setText((new Integer(calculPlayer.getValMilliSeconde())).toString());
			break;
		}
	}

	// affectation d'un compteur
	public void setCompteur(int choix, int atDate)
	{
		// mise a jour des compteurs en fonction de la date du dernier point de la piste courante
		switch (choix)
		{
		case 1:
			if(calculBegin.toCompteur(atDate))
				holoEditRef.connection.sendBegin();
				affiche(1);
			break;
		case 2:
			if(calculEnd.toCompteur(atDate))
				holoEditRef.connection.sendEnd();
				affiche(2);
			break;
		case 3:
			if(calculTotal.toCompteur(atDate))
				holoEditRef.connection.sendTotal();
				affiche(3);
			break;
		case 4:
			calculPlayer.toCompteur(atDate);
			affiche(6);
			break;
		case 5:
			calculCursor.toCompteur(atDate);
			affiche(7);
			break;
		default:
			if (atDate >= calculEnd.toMilliSeconde())
				calculEnd.toCompteur(atDate);
			affiche(2);
//			calculTotal.toCompteur(atDate);
			break;
		}
		if(choix == 1)
		{
			calculCursor.toCompteur(atDate);
			if(!holoEditRef.connection.isPlaying())
				calculPlayer.toCompteur(atDate);
		}
		if(choix == 5 && !holoEditRef.connection.isPlaying())
			calculPlayer.toCompteur(atDate);
		//verif(); //va rafraichir l'affichage des compteurs
		if(choix == 1)
			holoEditRef.score.cursorTime = calculBegin.toMilliSeconde();
	}
	
	// affectation d'un compteur
	public void setCompteur2(int choix, int atDate)
	{
		// mise a jour des compteurs en fonction de la date du dernier point de la piste courante
		switch (choix)
		{
		case 1:
			calculBegin.toCompteur(atDate);
			break;
		case 2:
			calculEnd.toCompteur(atDate);
			break;
		case 3:
			calculTotal.toCompteur(atDate);
			break;
		case 4:
			calculPlayer.toCompteur(atDate);
			break;
		case 5:
			calculCursor.toCompteur(atDate);
			break;
		default:
			if (atDate >= calculEnd.toMilliSeconde())
				calculEnd.toCompteur(atDate);
//			calculTotal.toCompteur(atDate);
			break;
		}
		if(choix == 1)
		{
			calculCursor.toCompteur(atDate);
			if(!holoEditRef.connection.isPlaying())
				calculPlayer.toCompteur(atDate);
		}
		if(choix == 5 && !holoEditRef.connection.isPlaying())
			calculPlayer.toCompteur(atDate);
		verif(); //va rafraichir l'affichage des compteurs
		if(choix == 1)
			holoEditRef.score.cursorTime = calculBegin.toMilliSeconde();
	}
	
	public void setBegAndEnd(int dateBeg, int dateEnd)
	{
		calculEnd.toCompteur(dateEnd);
		calculBegin.toCompteur(dateBeg);
		calculCursor.toCompteur(dateBeg);
		if(!holoEditRef.connection.isPlaying())
			calculPlayer.toCompteur(dateBeg);
		verif();
		holoEditRef.score.cursorTime = calculBegin.toMilliSeconde();
		holoEditRef.connection.sendBegin();
		holoEditRef.connection.sendEnd();
	}

	// accesseurs pour la date d'un compteur ( retourne un temps en centiemes de seconde )
	public int getDate(int choixCpt)
	{
		switch (choixCpt)
		{
		case 1:
			return calculBegin.toMilliSeconde();
		case 2:
			return calculEnd.toMilliSeconde();
		case 3:
			return calculTotal.toMilliSeconde();
		case 4:
			return calculDelta.toMilliSeconde();
		case 5:
			return calculCursor.toMilliSeconde();
		default:
			return 0;
		}
	}

	// gestion des dates en fonction de delta (pour mode SHORT VIEW)
	public int getDateFinPlusDelta()
	{
		return (calculEnd.toMilliSeconde() + calculDelta.toMilliSeconde());
	}

	public int getDateFinMoinsDelta()
	{
		int res = calculEnd.toMilliSeconde() - calculDelta.toMilliSeconde();
		return (res < 0 ? 0 : res);
	}

	public int getDateDebPlusDelta()
	{
		return (calculBegin.toMilliSeconde() + calculDelta.toMilliSeconde());
	}
	//---------- definition des classes pour la gestion des evenement de souris 
	class SourisAction implements MouseListener
	{ // evenements lies aux boutons
		public void mouseEntered(MouseEvent evt) {}

		public void mouseExited(MouseEvent evt) {}

		public void mouseClicked(MouseEvent evt) {}

		public void mousePressed(MouseEvent evt)
		{
			Object objet = evt.getSource();
			coordY = evt.getY();
			if (objet == begin.cptHeure || objet == begin.cptMinute || objet == begin.cptSeconde || objet == begin.cptMilliSeconde)
				timeRefValue = calculBegin.toMilliSeconde();
			else if (objet == end.cptHeure || objet == end.cptMinute || objet == end.cptSeconde || objet == end.cptMilliSeconde)
				timeRefValue = calculEnd.toMilliSeconde();
			else if (objet == cursor.cptHeure || objet == cursor.cptMinute || objet == cursor.cptSeconde || objet == cursor.cptMilliSeconde)
				timeRefValue = calculCursor.toMilliSeconde();
			else if (objet == delta.cptSeconde || objet == delta.cptMilliSeconde)
				timeRefValue = calculDelta.toMilliSeconde();
		}

		public void mouseReleased(MouseEvent evt)
		{
			// suppression du focus sur les champs texte au relachement de la souris suite a un drag
			if (dragBegin)
			{
				dragBegin = false;
				begin.requestFocus();
			} else if (dragEnd)
			{
				dragEnd = false;
				end.requestFocus();
			} else if (dragCursor)
			{
				dragCursor = false;
				cursor.requestFocus();
			} else if (dragDelta)
			{
				dragDelta = false;
				delta.requestFocus();
			} else if (dragPoint)
			{
				dragPoint = false;
				pointsNumber.requestFocus();
			}
		}
	}
	// gestion des evenement de deplacements de la souris 
	class SourisMotion implements MouseMotionListener
	{ // evenements liees au deplacement ou au drag
		public void mouseMoved(MouseEvent evt) {}

		public void mouseDragged(MouseEvent evt)
		{
			int val;
			Point souris = evt.getPoint();
			Object objet = evt.getSource();
			if (objet == begin.cptHeure)
			{
				val = calculBegin.getValHeure();
				incrOrDecrCompteur(calculBegin, 'h', souris);
				if (val != calculBegin.getValHeure())
					dragBegin = true;
			} else if (objet == begin.cptMinute) {
				val = calculBegin.getValMinute();
				incrOrDecrCompteur(calculBegin, 'm', souris);
				if (val != calculBegin.getValMinute())
					dragBegin = true;
			} else if (objet == begin.cptSeconde) {
				val = calculBegin.getValSeconde();
				incrOrDecrCompteur(calculBegin, 's', souris);
				if (val != calculBegin.getValSeconde())
					dragBegin = true;
			} else if (objet == begin.cptMilliSeconde) {
				val = calculBegin.getValMilliSeconde();
				incrOrDecrCompteur(calculBegin, 'c', souris);
				if (val != calculBegin.getValMilliSeconde())
					dragBegin = true;
			} else if (objet == end.cptHeure) {
				val = calculEnd.getValHeure();
				incrOrDecrCompteur(calculEnd, 'h', souris);
				if (val != calculEnd.getValHeure())
					dragEnd = true;
			} else if (objet == end.cptMinute) {
				val = calculEnd.getValMinute();
				incrOrDecrCompteur(calculEnd, 'm', souris);
				if (val != calculEnd.getValMinute())
					dragEnd = true;
			} else if (objet == end.cptSeconde) {
				val = calculEnd.getValSeconde();
				incrOrDecrCompteur(calculEnd, 's', souris);
				if (val != calculEnd.getValSeconde())
					dragEnd = true;
			} else if (objet == end.cptMilliSeconde) {
				val = calculEnd.getValMilliSeconde();
				incrOrDecrCompteur(calculEnd, 'c', souris);
				if (val != calculEnd.getValMilliSeconde())
					dragEnd = true;
			} else if (objet == cursor.cptHeure) {
				val = calculCursor.getValHeure();
				incrOrDecrCompteur(calculCursor, 'h', souris);
				if (val != calculCursor.getValHeure())
					dragCursor = true;
			} else if (objet == cursor.cptMinute) {
				val = calculCursor.getValMinute();
				incrOrDecrCompteur(calculCursor, 'm', souris);
				if (val != calculCursor.getValMinute())
					dragCursor = true;
			} else if (objet == cursor.cptSeconde) {
				val = calculCursor.getValSeconde();
				incrOrDecrCompteur(calculCursor, 's', souris);
				if (val != calculCursor.getValSeconde())
					dragCursor = true;
			} else if (objet == cursor.cptMilliSeconde) {
				val = calculCursor.getValMilliSeconde();
				incrOrDecrCompteur(calculCursor, 'c', souris);
				if (val != calculCursor.getValMilliSeconde())
					dragCursor = true;
			} else if (objet == delta.cptSeconde) {
				val = calculDelta.getValSeconde();
				incrOrDecrCompteur(calculDelta, 'D', souris);
				if (val != calculDelta.getValSeconde())
					dragDelta = true;
			} else if (objet == delta.cptMilliSeconde) {
				val = calculDelta.getValMilliSeconde();
				incrOrDecrCompteur(calculDelta, 'c', souris);
				if (val != calculDelta.getValMilliSeconde())
					dragDelta = true;
			} else if (objet == pointsNumber.cptPoints) {
				val = stringToInt(((JTextField) pointsNumber.cptPoints).getText());
				incrOrDecrCompteur(null, 'p', souris);
				if (val != stringToInt(((JTextField) pointsNumber.cptPoints).getText()))
					dragPoint = true;
			}
		}
	}
	class KeyAction implements KeyListener
	{
		int sens = 0;
		int val = 0;
		Thread t;
		CTextField tf;

		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e)
		{
			tf = ((CTextField) e.getSource());
			val = stringToInt(tf.getText());
			if (e.getKeyCode() == KeyEvent.VK_UP)
				sens = 1;
			else if (e.getKeyCode() == KeyEvent.VK_DOWN)
				sens = -1;
			t = new Thread()
			{
				public void run()
				{
					try
					{
						while (sens != 0)
						{
							val += sens;
							tf.setText("" + val);
							check(tf);
//							System.out.println("counter-numberbox-waiting");
							sleep(1000);
						}
					}
					catch (InterruptedException ex)
					{
						sens = 0;
					}
				}
			};
			t.setPriority(Thread.MIN_PRIORITY);
			t.setName("Counter-NumberBox");
			t.start();
		}

		public void keyReleased(KeyEvent e)
		{
			if (t != null)
				t.interrupt();
			if(sens == 1 || sens == -1)
			{
				sens = 0;
				tf.setText("" + val);
				check(tf);
			}
		}
	}
	
	public void check(CTextField objet)
	{
		if (objet == begin.cptHeure)
			majFocus(begin, calculBegin, 'h');
		else if (objet == begin.cptMinute)
			majFocus(begin, calculBegin, 'm');
		else if (objet == begin.cptSeconde)
			majFocus(begin, calculBegin, 's');
		else if (objet == begin.cptMilliSeconde)
			majFocus(begin, calculBegin, 'c');
		else if (objet == end.cptHeure)
			majFocus(end, calculEnd, 'h');
		else if (objet == end.cptMinute)
			majFocus(end, calculEnd, 'm');
		else if (objet == end.cptSeconde)
			majFocus(end, calculEnd, 's');
		else if (objet == end.cptMilliSeconde)
			majFocus(end, calculEnd, 'c');
		else if (objet == cursor.cptHeure)
			majFocus(cursor, calculCursor, 'h');
		else if (objet == cursor.cptMinute)
			majFocus(cursor, calculCursor, 'm');
		else if (objet == end.cptSeconde)
			majFocus(cursor, calculCursor, 's');
		else if (objet == cursor.cptMilliSeconde)
			majFocus(cursor, calculCursor, 'c');
		else if (objet == delta.cptSeconde)
			majFocus(delta, calculDelta, 's');
		else if (objet == delta.cptMilliSeconde)
			majFocus(delta, calculDelta, 'c');
		else if (objet == pointsNumber.cptPoints)
			majFocus(pointsNumber, null, 'p');
	}

	// fonction permettant de modifier les compteur lors d'un drag de la souris
	public void incrOrDecrCompteur(CounterCalculation calculCompteur, char type, Point p)
	{
		int nbIncr = coordY - p.y;
		if (type == 'p')
		{
			if (p.y <= coordY)
			{
				nbPoints = nbPoints + nbIncr / 4;
			} else
			{
				nbPoints = nbPoints + nbIncr / 4;
			}
			verif();
			holoEditRef.gestionPistes.setNbPts(nbPoints);
		} else if (nbIncr != 0)
		{
			switch (type)
			{
			case 'h':
				calculCompteur.addHeures(timeRefValue, nbIncr / 4);
				break;
			case 'm':
				calculCompteur.addMinutes(timeRefValue, nbIncr / 4);
				break;
			case 's':
				calculCompteur.addSecondes(timeRefValue, nbIncr / 4);
				break;
			case 'D':
				calculCompteur.addSecondesD(timeRefValue, nbIncr / 4);
				break;
			case 'c':
				calculCompteur.addMilliSecondes(timeRefValue, nbIncr / 4);
				break;
			}
			verif();
			if (calculCompteur == calculDelta)
				holoEditRef.gestionPistes.setDelta(getDate(4));
		}
		holoEditRef.room.display();
	}
	//---------- definition de la classe permettant la gestion des evenements d'action
	class Action implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object objet = evt.getSource();
			if (objet == begin.cptHeure)
				modifierCompteur(begin, 'h');
			else if (objet == begin.cptMinute)
				modifierCompteur(begin, 'm');
			else if (objet == begin.cptSeconde)
				modifierCompteur(begin, 's');
			else if (objet == begin.cptMilliSeconde)
				modifierCompteur(begin, 'c');
			else if (objet == end.cptHeure)
				modifierCompteur(end, 'h');
			else if (objet == end.cptMinute)
				modifierCompteur(end, 'm');
			else if (objet == end.cptSeconde)
				modifierCompteur(end, 's');
			else if (objet == end.cptMilliSeconde)
				modifierCompteur(end, 'c');
			else if (objet == cursor.cptHeure)
				modifierCompteur(cursor, 'h');
			else if (objet == cursor.cptMinute)
				modifierCompteur(cursor, 'm');
			else if (objet == cursor.cptSeconde)
				modifierCompteur(cursor, 's');
			else if (objet == cursor.cptMilliSeconde)
				modifierCompteur(cursor, 'c');
			else if (objet == delta.cptSeconde)
				modifierCompteur(delta, 'D');
			else if (objet == delta.cptMilliSeconde)
				modifierCompteur(delta, 'c');
			else if (objet == pointsNumber.cptPoints)
				modifierCompteur(pointsNumber, 'p');
		}
	}

	// fonction permettant de mettre a jour un compteur suite a une modification de l'utilisateur
	// objet : champ texte du compteur a modifier, calculCompteur : CalculCompteur associe au compteur 
	// type : donnee a modifier ( h->heure , m->minute , s->seconde , c ->centiSeconde , p->nombrePoints )
	// cette fonction est appele suite a un evenement d'action
	private void modifierCompteur(Counter compteur, char type)
	{
		switch (type)
		{
		case 'h':
		case 'c':
		case 'm':
		case 's':
		case 'D':
			compteur.requestFocus();
			break;
		case 'p':
			nbPoints = stringToInt(((JTextField) pointsNumber.cptPoints).getText());
			nbPoints = (nbPoints >= 100 ? 99 : nbPoints);
			pointsNumber.requestFocus();
			holoEditRef.gestionPistes.setNbPts(nbPoints);
			break;
		}
	}
	//---------- gestion des evenements de focus
	public class Focus implements java.awt.event.FocusListener
	{
		public void focusGained(FocusEvent focusEvt)
		{
			// si l'objet prend le focus, on selectionne tout le champ texte
			JTextField champTexte = (JTextField) focusEvt.getSource();
			champTexte.selectAll();
		}

		public void focusLost(FocusEvent focusEvt)
		{
			Object objet = focusEvt.getSource();
			if (objet == begin.cptHeure)
				majFocus(begin, calculBegin, 'h');
			else if (objet == begin.cptMinute)
				majFocus(begin, calculBegin, 'm');
			else if (objet == begin.cptSeconde)
				majFocus(begin, calculBegin, 's');
			else if (objet == begin.cptMilliSeconde)
				majFocus(begin, calculBegin, 'c');
			else if (objet == end.cptHeure)
				majFocus(end, calculEnd, 'h');
			else if (objet == end.cptMinute)
				majFocus(end, calculEnd, 'm');
			else if (objet == end.cptSeconde)
				majFocus(end, calculEnd, 's');
			else if (objet == end.cptMilliSeconde)
				majFocus(end, calculEnd, 'c');
			else if (objet == cursor.cptHeure)
				majFocus(cursor, calculCursor, 'h');
			else if (objet == cursor.cptMinute)
				majFocus(cursor, calculCursor, 'm');
			else if (objet == cursor.cptSeconde)
				majFocus(cursor, calculCursor, 's');
			else if (objet == cursor.cptMilliSeconde)
				majFocus(cursor, calculCursor, 'c');
			else if (objet == delta.cptSeconde)
				majFocus(delta, calculDelta, 's');
			else if (objet == delta.cptMilliSeconde)
				majFocus(delta, calculDelta, 'c');
			else if (objet == pointsNumber.cptPoints)
				majFocus(pointsNumber, null, 'p');
		}
	}

	// fonction de mise a jour des compteur suite a un changement du focus
	// Modif de Leo : suppression des modulos 100 pour pouvoir entrer au clavier des valeurs > 99 dans les compteurs temporels.
	private void majFocus(Counter compteur, CounterCalculation calculCompteur, char type)
	{
		int valeur; // valeur correspondant a la chaine contenu dans objet
		switch (type)
		{
		case 'h':
			valeur = stringToInt(((JTextField) compteur.cptHeure).getText());
			//valeur = valeur%100 ;
			if(valeur != calculCompteur.getValHeure())
				calculCompteur.setValeur(valeur, 0, 0, 0);
			verif();
			break;
		case 'm':
			valeur = stringToInt(((JTextField) compteur.cptMinute).getText());
			//valeur = valeur%100 ;
			if(valeur != calculCompteur.getValMinute())
				calculCompteur.setValeur(calculCompteur.getValHeure(), valeur, 0, 0);
			verif();
			break;
		case 's':
			valeur = stringToInt(((JTextField) compteur.cptSeconde).getText());
			//valeur = valeur%100 ;
			if(valeur != calculCompteur.getValSeconde())
				calculCompteur.setValeur(calculCompteur.getValHeure(), calculCompteur.getValMinute(), valeur, 0);
			verif();
			if (compteur == delta)
			{
				calculCompteur.setValeurDelta(valeur, 0);
				verif();
				holoEditRef.gestionPistes.setDelta(getDate(4));
			}
			break;
		case 'c':
			valeur = stringToInt(((JTextField) compteur.cptMilliSeconde).getText());
			//valeur = valeur%100 ;
			if(valeur != calculCompteur.getValMilliSeconde())
				calculCompteur.setValeur(calculCompteur.getValHeure(), calculCompteur.getValMinute(), calculCompteur.getValSeconde(), valeur);
			verif();
			if (compteur == delta)
				holoEditRef.gestionPistes.setDelta(getDate(4));
			break;
		case 'p':
			nbPoints = stringToInt(((JTextField) pointsNumber.cptPoints).getText());
			// Leo : le nombre de point quant a lui ne doit pas depasser 99.
			nbPoints = nbPoints % 100;
			holoEditRef.gestionPistes.setNbPts(nbPoints);
			break;
		}
		try
		{
			holoEditRef.room.display();
		} catch (Exception e) {}
	}

	public int toCentiSeconde2(CounterCalculation compteur)
	{
		return compteur.toCentiSeconde();
	}

}
