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
/***************      classe PresetCombo        *******************/
/* Champ d'entree de type liste de choix des presets */

package holoedit.gui;

import holoedit.data.HoloFctPreset;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;

public class PresetCombo extends Choice
{
	// attributs graphiques
	private int w = 100;
	private int h = 25;
	Font f = new Font("courrier", Font.PLAIN, 10);

	// les presets
    private Vector<HoloFctPreset> presets = new Vector<HoloFctPreset>(16,1); 
	// fonction auquel se rapporte les presets
    private String algor;
     					
	// Constructeur par defaut
	public PresetCombo(String alg, Vector<HoloFctPreset> inPresets)
	{
		super();
		algor = alg;
		setFont(f);
		// on choisit parmi tous les presets ceux qui correspondent a l'algorithme en cours
      	for (HoloFctPreset fct:inPresets)
      	{
      		if(fct.pAlgor.equalsIgnoreCase(algor))
      		{
      			addItem(fct.pName) ;
      			presets.add(fct);
      		}
       	}
       	
  		try
		{
			select(0);
		}
		catch (IllegalArgumentException e) {}
  		
       	// attributs graphiques
       	setPreferredSize(new Dimension(w,h));
       	setMinimumSize(new Dimension(w,h));
       	setMaximumSize(new Dimension(w,h));
       	setSize(w,h);
       	setVisible(true);
		setEnabled(!presets.isEmpty());
	}	

	// retourne les valeurs du preset selectionnee
	public Object[] getVals()
	{
		if(!presets.isEmpty() && getSelectedIndex() != -1)
			return presets.elementAt(getSelectedIndex()).getVals();
		return null;
	}

	public HoloFctPreset getCurrentPst()
	{
		if(!presets.isEmpty() && getSelectedIndex() != -1)
			return presets.elementAt(getSelectedIndex());
		return null;
	}
	
	// retourne le nombre de parametre du preset selectionnee
	public int getValSize()
	{
		if(!presets.isEmpty() && getSelectedIndex() != -1)
			return presets.elementAt(getSelectedIndex()).getSize();
		return 0;
	}

	// ajout d'un preset
	public void addPreset(HoloFctPreset hPreset)
	{
		addItem(hPreset.pName) ;
		presets.add(hPreset);
		setEnabled(true);
	}

	// ajout d'un preset
	public void updatePreset(Object[] vals)
	{
		presets.elementAt(getSelectedIndex()).setVals(vals);
	}
	
	// ajout d'un preset
	public void deletePreset()
	{
		int selected = getSelectedIndex();
		remove(selected);
		presets.remove(selected);
		setEnabled(!presets.isEmpty());
	}
	
	// ajout d'un preset
	public void addPreset(String presetName, Vector<Object> vals)
	{
		HoloFctPreset hfp = new HoloFctPreset(presetName, algor, vals);
		addItem(hfp.pName) ;
		presets.add(hfp);
		setEnabled(true);
	}

	public void setSelectedIndex(int i)
	{
		select(i);
	}
}