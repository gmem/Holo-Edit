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
/**
 * Boite de dialogue pour l'edition des parametres d'un HP
 */
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.data.HoloSpeaker;
import holoedit.util.Formatter;
import holoedit.util.Ut;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SpeakerEditor extends FloatingWindow
{
	// HP a editer
	HoloSpeaker hpEdit;
	// parametres modifiables
	JTextField coordX, coordY, coordZ;
	// label de distance au centre du HP
	JLabel dir,distLabel;
	// numero du HP
	int numHP;
	// si polaire ou cartesien
	int coord;
	// police
	Font font = new Font("Verdana",Font.PLAIN,10);
	// reference vers main pour forcer reaffichage
	protected HoloEdit holoEditRef;
	public SpeakerEditor(int numHP, Point position, GestionPistes gp)
	{
		super("Speakers Informations (Cartesian)", gp.holoEditRef, 400, 45, position.x, position.y, true);
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(gridBag);
		// initalisation des attributs
		this.numHP = numHP;
		holoEditRef = gp.holoEditRef;
		hpEdit = gp.speakers.get(numHP);
		coord = holoEditRef.coordinates;
		coordX = new JTextField(0);
		coordY = new JTextField(0);
		coordZ = new JTextField(0);
		dir = new JLabel();
		String stringValX, stringValY, stringValZ, stringValDir;
		if (coord == 1)
		{
			setTitle("Speakers Informations (Polar)");
			Point p = new Point();
			p.setLocation(hpEdit.X, hpEdit.Y);
			p = carToPol(p);
			stringValX = floatToString(p.x);
			stringValY = floatToString(p.y);
			polToCar(p);
		} else
		{
			stringValX = floatToString(hpEdit.X);
			stringValY = floatToString(hpEdit.Y);
		}
		stringValZ = floatToString(hpEdit.Z);
		stringValDir = floatToString(hpEdit.dir);
		coordX.setText("" + stringValX);
		coordY.setText("" + stringValY);
		coordZ.setText("" + stringValZ);
		dir.setText("" + stringValDir);
		coordX.setFont(font);
		coordY.setFont(font);
		coordZ.setFont(font);
		
		coordX.setPreferredSize(new Dimension(70, 12));
		coordY.setPreferredSize(new Dimension(70, 12));
		coordZ.setPreferredSize(new Dimension(70, 12));
		
		dir.setFont(font);
		// gestion des evenements
		ActionTextField actionTextField = new ActionTextField();
		coordX.addActionListener(actionTextField);
		coordY.addActionListener(actionTextField);
		coordZ.addActionListener(actionTextField);
		MouseTextField mouseTextField = new MouseTextField();
		coordX.addMouseListener(mouseTextField);
		coordY.addMouseListener(mouseTextField);
		coordZ.addMouseListener(mouseTextField);
		dir.addMouseListener(mouseTextField);
		FocusTextField focusTextField = new FocusTextField();
		coordX.addFocusListener(focusTextField);
		coordY.addFocusListener(focusTextField);
		coordZ.addFocusListener(focusTextField);
//		dir.addFocusListener(focusTextField);
		// preparation des labels
		JLabel coordXLabel, coordYLabel;
		if (coord == 0)
		{
			coordXLabel = new JLabel(" X : ");
			coordYLabel = new JLabel(" Y : ");
		} else
		{
			coordXLabel = new JLabel(" R : ");
			coordYLabel = new JLabel(" T : ");
		}
		JLabel coordZLabel = new JLabel(" Z : ");
		coordXLabel.setHorizontalAlignment(JTextField.RIGHT);
		coordYLabel.setHorizontalAlignment(JTextField.RIGHT);
		coordZLabel.setHorizontalAlignment(JTextField.RIGHT);
		coordXLabel.setFont(font);
		coordYLabel.setFont(font);
		coordZLabel.setFont(font);
		
		
		
		JLabel dirLabel = new JLabel(" Dir : ");
		dirLabel.setHorizontalAlignment(JTextField.RIGHT);
		dirLabel	.setFont(font);
		JLabel numLabel = new JLabel("LS n" + Ut.numCar + gp.speakers.get(numHP).num + " ");
		numLabel.setHorizontalAlignment(JTextField.LEFT);
		// pour avoir la distance avec 2 nombres apres la virgule
//		RealFormatter rF = new RealFormatter();
//		rF.max_right_digits = 2;
//		rF.min_right_digits = 2;
		distLabel = new JLabel(" Dist : " + new Formatter(-1,-1,2,2).format(hpEdit.dist));
		distLabel.setHorizontalAlignment(JTextField.CENTER);
		distLabel.setSize(70, 12);
		distLabel.setFont(font);
		distLabel.setPreferredSize(new Dimension(70, 12));
		distLabel.setMaximumSize(new Dimension(70, 12));
		distLabel.setMinimumSize(new Dimension(70, 12));
		// mise en page des composants
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(numLabel, constraints);
		add(numLabel);
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordXLabel, constraints);
		add(coordXLabel);
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordX, constraints);
		add(coordX);
		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordYLabel, constraints);
		add(coordYLabel);
		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordY, constraints);
		add(coordY);
		constraints.gridx = 5;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordZLabel, constraints);
		add(coordZLabel);
		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(coordZ, constraints);
		add(coordZ);
		constraints.gridx = 7;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(dirLabel, constraints);
		add(dirLabel);
		constraints.gridx = 8;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(dir, constraints);
		add(dir);
		constraints.gridx = 9;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		gridBag.setConstraints(distLabel, constraints);
		add(distLabel);
		addWindowListener(new WindowAdapter()
		{
			public void windowDeactivated(WindowEvent e)
			{
				e.getWindow().dispose();
			}
		});
		
		open();
	}
	
	// conversion d'un string en float
	private float stringToFloat(String s)
	{
		float res = 0;
		Float temp = new Float(s);
		res = temp.floatValue();
		return res;
	}
	// conversion d'un float en chaine de caractere de longueur 6
	private String floatToString(float f)
	{
		String res = "" + f;
		if (res.length() < 6)
		{
			while (res.length() < 6)
				res = res + "0";
		}
		if (res.length() > 6)
		{
			res = res.substring(0, 6);
		}
		return res;
	}
	// fonction de conversion de coordonnees cartesiennes vers coordonnees polaires
	public Point carToPol(Point p)
	{
		double XX = p.x;
		double YY = p.y;
		double dist = Math.sqrt(Math.pow(XX,2) + Math.pow(YY, 2));
		double arg = 0;
		if (dist != 0)
		{
			arg = Math.asin(YY / dist) / Math.PI * 180;
			if ((XX <= 0 && YY >= 0) || (XX <= 0 && YY <= 0))
			{
				arg = 180 - arg;
			} else if (XX > 0 && YY < 0)
			{
				arg = 360 + arg;
			}
		}
		p.setLocation(dist, arg);
		return p;
	}
	// fonction de conversion de coordonnees polaires vers coordonnees cartesiennes 
	public Point polToCar(Point p)
	{
		double XX = 0;
		double YY = 0;
		double dist = p.x;
		double arg = p.y;
		if (arg == 0)
		{
			XX = dist;
			YY = 0;
		} else if (arg == 90)
		{
			XX = 0;
			YY = dist;
		} else if (arg == 180)
		{
			XX = -1 * dist;
			YY = 0;
		} else if (arg == 270)
		{
			XX = 0;
			YY = -1 * dist;
		} else
		{
			YY = Math.sin(arg / 180 * Math.PI) * dist;
			if (arg < 90)
			{
				YY = Math.abs(YY);
				XX = Math.sqrt(Math.pow(dist,2) - Math.pow(YY,2));
			} else if (arg < 180)
			{
				YY = Math.abs(YY);
				XX = -1 * Math.sqrt(Math.pow(dist,2) - Math.pow(YY,2));
			} else if (arg < 270)
			{
				XX = -1 * Math.sqrt(Math.pow(dist,2) - Math.pow(YY,2));
			} else
			{
				XX = Math.sqrt(Math.pow(dist,2) - Math.pow(YY,2));
			}
		}
		p.setLocation(XX, YY);
		return p;
	}
	// pour les actions dans les champs
	public class ActionTextField implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			// affectation des parametres
			if (coord == 0)
			{
				hpEdit.X = stringToFloat(coordX.getText());
				hpEdit.Y = stringToFloat(coordY.getText());
			} else
			{
				Point p = new Point();
				p.setLocation(stringToFloat(coordX.getText()),stringToFloat(coordY.getText()));
				p = polToCar(p);
				hpEdit.X = (float) p.getX();
				hpEdit.Y = (float) p.getY();
			}
			hpEdit.Z = stringToFloat(coordZ.getText());
			hpEdit.dir = stringToFloat(dir.getText());
			// on recalcule la distance au centre
			hpEdit.recalcDist();
			// pour avoir cette distance avec 2 nombres apres la virgule
//			RealFormatter rF = new RealFormatter();
//			rF.max_right_digits = 2;
//			rF.min_right_digits = 2;
			String s = " Dist : " + new Formatter(-1,-1,2,2).format(hpEdit.dist) + " ";
			distLabel.setText(s);
			holoEditRef.room.display();
			holoEditRef.modify();
		}
	}
	public class MouseTextField implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			((JTextField) e.getSource()).selectAll();
		}
		public void mouseEntered(MouseEvent e)
		{}
		public void mouseExited(MouseEvent e)
		{}
		public void mousePressed(MouseEvent e)
		{}
		public void mouseReleased(MouseEvent e)
		{}
	}
	public class FocusTextField implements FocusListener
	{
		public void focusGained(FocusEvent e)
		{
			((JTextField) e.getSource()).selectAll();
		}
		public void focusLost(FocusEvent e)
		{}
	}
}