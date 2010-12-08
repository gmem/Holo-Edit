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
 * Boite de dialogue pour l'edition des parametres d'un HoloPoint
 */
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.data.HoloPoint;
import holoedit.data.HoloTrack;
import holoedit.opengl.RoomIndex;
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

public class HoloPointEditor extends FloatingWindow
{
	// HoloPoint a editer
	HoloPoint hpEdit;
	// Piste le contenant 
	HoloTrack piste;
	// Parametre modifiables
	JTextField coordX, coordY, coordZ;
	// Numero du point
	int indicePoint = -1;
	int ptIndex = -1;
	// si on est en coordonnees cartesiennes ou polaires
	int coord;
	// coord polaires
	float angle,dist;
	// reference vers Main pour forcer reaffichage
	private HoloEdit holoEditRef;
	// police
	Font font = new Font("Verdana", Font.PLAIN, 10);
	public HoloPointEditor(int _ptIndex, Point position, FloatingWindow fw, HoloEdit mr)
	{
		super("Point Informations (Cartesian)", mr, 370, 45, position.x, position.y, true);
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(gridBag);
		// initalisation des attributs
		this.ptIndex = _ptIndex;
		RoomIndex.decode(_ptIndex);
		holoEditRef = mr;
		piste = holoEditRef.gestionPistes.tracks.get(RoomIndex.getTrack());
		hpEdit = piste.getHoloTraj(RoomIndex.getSeq()).points.get(RoomIndex.getPt());
		coord = holoEditRef.coordinates;
		coordX = new JTextField(5);
		coordX.setMinimumSize(new Dimension(38,20));
		coordY = new JTextField(5);
		coordY.setMinimumSize(new Dimension(38,20));
		coordZ = new JTextField(5);
		coordZ.setMinimumSize(new Dimension(38,20));
		String stringValX, stringValY, stringValZ;
		if (coord == 1)
		{
			setTitle("Point Informations (Polar)");
			angle = hpEdit.getTheta();
			dist = hpEdit.getModule();
			stringValX = floatToString(dist);
			stringValY = floatToString(angle);
		} else
		{
			stringValX = floatToString(hpEdit.x);
			stringValY = floatToString(hpEdit.y);
		}
		stringValZ = floatToString(hpEdit.z);
		coordX.setText("" + stringValX);
		coordY.setText("" + stringValY);
		coordZ.setText("" + stringValZ);
		// gestion des evenements
		ActionTextField actionTextField = new ActionTextField();
		coordX.addActionListener(actionTextField);
		coordY.addActionListener(actionTextField);
		coordZ.addActionListener(actionTextField);
		MouseTextField mouseTextField = new MouseTextField();
		coordX.addMouseListener(mouseTextField);
		coordY.addMouseListener(mouseTextField);
		coordZ.addMouseListener(mouseTextField);
		FocusTextField focusTextField = new FocusTextField();
		coordX.addFocusListener(focusTextField);
		coordY.addFocusListener(focusTextField);
		coordZ.addFocusListener(focusTextField);
		coordX.setFont(font);
		coordY.setFont(font);
		coordZ.setFont(font);
		// preparation des label
		JLabel coordXLabel, coordYLabel;
		if (coord == 0)
		{
			coordXLabel = new JLabel(" X : ");
			coordYLabel = new JLabel(" Y : ");
		} else
		{
			coordXLabel = new JLabel(" D : ");
			coordYLabel = new JLabel(" A : ");
		}
		coordXLabel.setHorizontalAlignment(JTextField.RIGHT);
		coordYLabel.setHorizontalAlignment(JTextField.RIGHT);
		JLabel coordZLabel = new JLabel(" Z : ");
		coordZLabel.setHorizontalAlignment(JTextField.RIGHT);
		coordXLabel.setFont(font);
		coordYLabel.setFont(font);
		coordZLabel.setFont(font);
		JLabel numLabel;
		if (hpEdit.isEditable())
			numLabel = new JLabel("Tk : " + (RoomIndex.getTrack()+1) + " n" + Ut.numCar + (RoomIndex.getPt()+1) + " ed");
		else
			numLabel = new JLabel("Tk : " + (RoomIndex.getTrack()+1) + " n" + Ut.numCar + (RoomIndex.getPt()+1) + " ne");
		numLabel.setHorizontalAlignment(JTextField.LEFT);
		numLabel.setFont(font);
		JLabel dateLabel = new JLabel(" Date : " + intToDate(hpEdit.date));
		dateLabel.setHorizontalAlignment(JTextField.CENTER);
		dateLabel.setFont(font);
		// mise en page des composants graphiques
		constraints.gridx = 0;
		constraints.gridy = 0;
		gridBag.setConstraints(numLabel, constraints);
		add(numLabel);
		constraints.gridx = 1;
		gridBag.setConstraints(coordXLabel, constraints);
		add(coordXLabel);
		constraints.gridx = 2;
		gridBag.setConstraints(coordX, constraints);
		add(coordX);
		constraints.gridx = 3;
		gridBag.setConstraints(coordYLabel, constraints);
		add(coordYLabel);
		constraints.gridx = 4;
		gridBag.setConstraints(coordY, constraints);
		add(coordY);
		constraints.gridx = 5;
		gridBag.setConstraints(coordZLabel, constraints);
		add(coordZLabel);
		constraints.gridx = 6;
		gridBag.setConstraints(coordZ, constraints);
		add(coordZ);
		constraints.gridx = 7;
		gridBag.setConstraints(dateLabel, constraints);
		add(dateLabel);
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
		if (res.length() < 5)
		{
			while (res.length() < 5)
				res = res + "0";
		}
		if (res.length() > 5)
		{
			res = res.substring(0, 5);
		}
		return res;
	}
	// conversion d'un entier representant des secondes en une chaine representant la meme date sous
	// la forme 'hh : mm : ss : cs' 
	private String intToDate(int dateNum)
	{
		String date;
		int heure = dateNum / 3600000;
		int minute = (dateNum / 60000) % 60;
		int seconde = (dateNum / 1000) % 60;
		int milliSeconde = dateNum % 1000;
		date = "" + heure + ":";
		if (minute < 10)
			date = date + "0";
		date = date + minute + ":";
		if (seconde < 10)
			date = date + "0";
		date = date + seconde + ":";
		if (milliSeconde < 10)
			date = date + "0";
		date = date + milliSeconde;
		return (date);
	}
	// fonction de conversion de coordonnees cartesiennes vers coordonnees polaires

	// pour les actions dans les champs
	public class ActionTextField implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			holoEditRef.gestionPistes.StoreToUndo();
			// maj des parametres
			if (hpEdit.isEditable())
			{
				RoomIndex.decode(ptIndex);
				if (coord == 0)
					holoEditRef.gestionPistes.getTrack(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq()).calcNewPosSegXYZ(RoomIndex.getPt(), stringToFloat(coordX.getText()), stringToFloat(coordY.getText()), stringToFloat(coordZ.getText()));
				else
				{
					HoloPoint tmpP = new HoloPoint();
					tmpP.setADZ( stringToFloat(coordY.getText()),stringToFloat(coordX.getText()), stringToFloat(coordZ.getText()));
					holoEditRef.gestionPistes.getTrack(RoomIndex.getTrack()).getHoloTraj(RoomIndex.getSeq()).calcNewPosSegXYZ(RoomIndex.getPt(), (float) tmpP.getX(), (float) tmpP.getY(), (float) tmpP.getZ());
				}
			} else
			{
				if (coord == 0)
				{
					hpEdit.setXYZ(stringToFloat(coordX.getText()), stringToFloat(coordY.getText()), stringToFloat(coordZ.getText()));
				} else
				{
					hpEdit.setADZ(stringToFloat(coordX.getText()), stringToFloat(coordY.getText()), stringToFloat(coordZ.getText()));
				}
			}
			holoEditRef.room.display();
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