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
package holoedit.functions;

import holoedit.gui.GestionPistes;
import holoedit.util.Ut;
import holoedit.data.HoloSDIFdata;
import holoedit.data.SDIFdataInstance;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.JComboBox;

public class ComboParam extends JComboBox implements Param, KeyListener
{
	// attributs graphiques
	private int w = 100;
	private int h = 25;
	private String[] trackTitles;
	private ArrayList<String> sdifNames;
	// numero du champ
	private int numero;
	/** type de combo : 
	 "track" 	(numero de piste de 1 a Main.TRACK_NB),
	 "applyTo" 	(choix entre piste active / pistes visibles / toutes pistes),
	 "sym"		(choix de symetrie centrale / horizontale / verticale)
	 "clock"	(choix de l'orientation clockwise (-) / trigonometric (+))
	 "sdif"		(choix de la holoSDIFdata)
	 "sdifFields" (choix du field d'une holoSDIFdata à partir duquel on génère la trajectoire)
	 "generic"	( les champs sont donnés par un vector à la création )
	 "coord"	( choix de X Y Z )
	 */
	private String comboType;
	// temps d'attente et boolean apres la frappe de la touche 1, pour pouvoir rentrer 12, 15 etc.. au clavier voir keyPressed
	private long time = 0;
	private boolean waitForNb = false;
	private GestionPistes gpRef;

	private String selected = "";
	
	public ComboParam(GestionPistes gp, int _numero, Object _val)
	{
		super();
		gpRef = gp;
		initTrackTitles();
		numero = _numero;
		comboType = "track";
		setFont(f2);
		for (String s:trackTitles)
			addItem(s);
		setPreferredSize(new Dimension(w, h));
		setMinimumSize(new Dimension(w, h));
		setMaximumSize(new Dimension(w, h));
		setSize(w, h);
		setValue(_val);
		addKeyListener(this);
	}
	//generic type
	public ComboParam(GestionPistes gp, int _numero, String[] _items, Object _val)
	{
		super();
		gpRef = gp;
		numero = _numero;
		comboType = "generic";
		setFont(f2);
		
		for(String s : _items)
		{
			addItem(s);
		}
		//if (!comboType.equalsIgnoreCase("sdif"))
			setValue(_val);
		addKeyListener(this);
	}
	// initialisation
	public ComboParam(GestionPistes gp, int _numero, String _comboType, Object _val)
	{
		super();
		gpRef = gp;
		numero = _numero;
		comboType = _comboType;
		setFont(f2);
		if (comboType.equalsIgnoreCase("track"))
		{
			initTrackTitles();
			for (String s:trackTitles)
				addItem(s);
		} else if (comboType.equalsIgnoreCase("applyTo")) {
			addItem("one");
			addItem("visible");
			addItem("all");
		} else if (comboType.equalsIgnoreCase("sym")) {
			addItem("central");
			addItem("horizontal");
			addItem("vertical");
		} else if (comboType.equalsIgnoreCase("clock")) {
			addItem("clockwise (+)");
			addItem("anti-clockwise (-)");
		} else if (comboType.equalsIgnoreCase("sdif")) {
			addItem("None");
			initSDIFNames();
			for (String s:sdifNames)
				addItem(s);
		} else if (comboType.equalsIgnoreCase("sdifFields")) {
			initSDIFNames();
			updateFields(sdifNames.get(0));
		} else if (comboType.equalsIgnoreCase("coord")) {
			addItem("X");
			addItem("Y");
			addItem("Z");
		}
		if (comboType.equalsIgnoreCase("sdif") || comboType.equalsIgnoreCase("sdifFields")){
			setPreferredSize(new Dimension(2*w, h));
			setMinimumSize(new Dimension(2*w, h));
			setMaximumSize(new Dimension(2*w, h));
			setSize(2*w, h);
		} else {
			setPreferredSize(new Dimension(w, h));
			setMinimumSize(new Dimension(w, h));
			setMaximumSize(new Dimension(w, h));
			setSize(w, h);
		}
		//if (!comboType.equalsIgnoreCase("sdif"))
			setValue(_val);
		addKeyListener(this);
	}

	
	private void initTrackTitles()
	{
		trackTitles = new String[gpRef.getNbTracks()+1];
		trackTitles[0] = "n" + Ut.numCar + " ?";
		for (int i = 1; i <= gpRef.getNbTracks(); i++)
			trackTitles[i] = gpRef.getTrack(i-1).getName();
	}
	
	private void initSDIFNames()
	{
		sdifNames = new ArrayList<String>();
		for (int trk=0; trk<gpRef.tracks.size(); trk++){
			Vector<SDIFdataInstance> vectorSDIF = gpRef.holoEditRef.score.getSeqSDIFinTrack(trk);
			for (SDIFdataInstance dataInstance : vectorSDIF)
				sdifNames.add(dataInstance.toString()+" - Track:"+trk);
		}
	}
	
	public void updateFields(String _selectedSDIF)
	{	
		removeAllItems();
		if (!selected.equals(_selectedSDIF)) {
			selected = _selectedSDIF;
			if (!_selectedSDIF.equalsIgnoreCase("none")){
				String selectedSDIF[] = selected.split("\\s"+"\\p{Punct}"+"\\s");
				String filename = selectedSDIF[0];
				String dataType = selectedSDIF[1];
 				HoloSDIFdata holoSDIFdata = gpRef.externalDataPool.get(filename, dataType);
				for (int i=0; i<holoSDIFdata.getFields().length; i++) {
					addItem(holoSDIFdata.getFields()[i]);
				}
			} else {
				addItem(" - ");
			}				
		}
	}
	
	/** retourne le numero du champ. */
	public int getNumero(){
		return numero;
	}

	public int getType(){
		return TYPE_COMBO;
	}

	public Object getValue()
	{
		if (comboType.equalsIgnoreCase("track"))
		{
			if (getSelectedIndex() > 0) // on retourne le numero de la piste
			{
				return getSelectedIndex();
			}
			return -1;
		} else if  (comboType.equalsIgnoreCase("sdif"))
			return getSelectedItem(); // return a String
		return getSelectedIndex();
	}

	public void setValue(Object _val)
	{
		if (comboType.equalsIgnoreCase("sdif")){
			if (this.getItemCount()>1)
				setSelectedIndex(1);
			else
				setSelectedIndex(0);
			return;
		}
		int val;
		if(_val.getClass().equals(String.class)){
			val = Integer.parseInt((String)_val);
		}else
			val = (Integer) _val;
		
		if (val != -2)
		{
			if (comboType.equalsIgnoreCase("track"))
			{
				if ((val >= 0) && (val < getItemCount()))
					setSelectedIndex(val); // numero de piste
				else setSelectedIndex(0); // "n° ?"
			}
			else if (comboType.equalsIgnoreCase("applyTo"))
			{
				if ((val >= 0) && (val <= 2))
				{
					// 0 : piste active , 1 : pistes visibles, 2 : toutes les pistes
					setSelectedIndex(val);
				}
				else setSelectedIndex(0); // piste active
			}
			else if (comboType.equalsIgnoreCase("sym"))
			{
				if ((val >= 0) && (val <= 2))
				{
					// symetrie
					// 0 : centrale , 1 : horizontale , 2 : verticale
					setSelectedIndex(val);
				}
				else setSelectedIndex(0); // centrale
			}
			else if (comboType.equalsIgnoreCase("clock"))
			{
				if (val > 0)
					setSelectedIndex(1);
				else setSelectedIndex(0);
			}
			else if (comboType.equalsIgnoreCase("coord"))
			{
				if ((val >= 0) && (val <= 2))
				{
					// 0 : X , 1 : Y , 2 : Z
					setSelectedIndex(val);
				}
				else setSelectedIndex(0); // X
			}
		}
	}
	
	/** retourne le type du combo. */
	public String getComboType()
	{
		return comboType;
	}

	/** affecte le type du combo. */
	public void setComboType(String _comboType)
	{
		comboType = _comboType;
	}
	
	/** pour pouvoir selectionner directement au clavier le numero de piste voulu. */
	public void keyPressed(KeyEvent e)
	{
		int itemToSel = 0;
		char c = e.getKeyChar();
		if (comboType.equalsIgnoreCase("track"))
		{
			// dans le cas d'un combo de type track (numero de pistes)
			if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')
			{
				// on accepte que les nombres
				if (c == '1' && !waitForNb) //
				{
					// si c'est un alors on attend un nombre derriere (10,11,12,13...)
					// on selectionne 1
					itemToSel = 1;
					// on note le temps de frappe du 1
					time = e.getWhen();
					// on met l'objet en attente d'un autre nombre
					waitForNb = true;
				} else
				{
					// si different de 1
					// on note le temps de frappe du nombre
					long oTime = time;
					time = e.getWhen();
					if (waitForNb)
					{
						// si on attend un nombre derriere 1
						// on calcule le temps entre la frappe du 1
						if ((time - oTime) <= 500)
						{
							// on calcule le numero de la piste
							char[] ct = new char[1];
							ct[0] = c;
							String str = new String(ct);
							itemToSel = 10 + Integer.valueOf(str).intValue();
						}
						waitForNb = false;
					} else
					{
						// si on attend pas de nombre...
						if (c != '0')
						{
							char[] ct = new char[1];
							ct[0] = c;
							String str = new String(ct);
							itemToSel = Integer.valueOf(str).intValue();
						}
					}
				}
				// par surete on on verifie
				if ((itemToSel < getItemCount()) && (itemToSel > 0))
					setSelectedIndex(itemToSel);
			}
		} else if (comboType.equalsIgnoreCase("sym"))
		{
			// si c'est de type symetrie
			if (c == '0' || c == '1' || c == '2' || c == 'c' || c == 'h' || c == 'v')
			{
				switch (c)
				{
				case '0': // centrale
				case 'c':
					setSelectedIndex(0);
					break;
				case '1': // horizontale
				case 'h':
					setSelectedIndex(1);
					break;
				case '2': // verticale
				case 'v':
					setSelectedIndex(2);
					break;
				default:
					break;
				}
			}
		} else if (comboType.equalsIgnoreCase("applyTo"))
		{
			// si c'est de type apply to (all, visible, one)
			if (c == '0' || c == '1' || c == '2' || c == 'a' || c == 'v' || c == 'o')
			{
				switch (c)
				{
				case '0': // one
				case 'o':
					setSelectedIndex(0);
					break;
				case '1': // visible
				case 'v':
					setSelectedIndex(1);
					break;
				case '2': // all
				case 'a':
					setSelectedIndex(2);
					break;
				default:
					break;
				}
			}
		} else if (comboType.equalsIgnoreCase("clock"))
		{
			// si c'est de type symetrie
			if (c == '0' || c == '1' || c == 'c' || c == 't')
			{
				switch (c)
				{
				case '0': // trigonometric
				case 't':
					setSelectedIndex(0);
					break;
				case '1': // clockwise
				case 'c':
					setSelectedIndex(1);
					break;
				default:
					break;
				}
			}
		}
	}

	public void keyTyped(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}
}
