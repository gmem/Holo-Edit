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
 * Champ d'entree de type generique des valeurs des fonctions
 */
package holoedit.functions;

import java.awt.event.ActionListener;

public class Field
{
	// Intitule du champ
	String label;
	// Valeur par defaut
	Object defVal;
	// Valeur Minimum
	double resMin = Double.MIN_VALUE;
	// Valeur Max
	double resMax = Double.MAX_VALUE;
	// Modulo
	double resMod = -1;
	// Type de champ (text / combo / check >> voir DialogParam.java)
	int type = Param.TYPE_TEXT;
	// Option (combo : track/applyTo/sym ; 
	String option = "-";
	String[] comboValues;
	ActionListener a = null;

	public Field(String _label, Object _val)
	{
		label = _label;
		defVal = _val;
	}
	
	public Field(String _label, String _text)
	{
		label = _label;
		defVal = _text;
		type = Param.TYPE_TEXT;
	}

	public Field(String _label, int _i)
	{
		label = _label;
		defVal = _i;
		type = Param.TYPE_INT;
	}
	
	public Field(String _label, float _f)
	{
		label = _label;
		defVal = _f;
		type = Param.TYPE_FLOAT;
	}
	
	public Field(String _label, double _d)
	{
		label = _label;
		defVal = _d;
		type = Param.TYPE_DOUBLE;
	}
	
	public Field(String _label, boolean _b)
	{
		label = _label;
		defVal = _b;
		type = Param.TYPE_CHECK;
	}
	
	public Field(String _label, int _type, Object _val)
	{
		label = _label;
		defVal = _val;
		type = _type;
		if (type == Param.TYPE_COMBO)
			option = "track";
	}

	public Field(String _label, int _type, Object _val, ActionListener _a)
	{
		label = _label;
		defVal = _val;
		type = _type;
		if (type == Param.TYPE_COMBO)
			option = "track";
		a = _a;
	}
	
	public Field(String _label, int _type, Object _val, String _option)
	{
		label = _label;
		defVal = _val;
		type = _type;
		option = _option;
	}
	
	//unused
	public Field(String _label, int _type, String[] _items,  Object _val)
	{
		label = _label;
		defVal = _val;
		type = _type;
		comboValues = _items;
		option = "generic";
	}

	public Field(String _label, int _type, Object _val, double min, double max)
	{
		label = _label;
		type = _type;
		defVal = _val;
		resMin = min;
		resMax = max;
	}
	
	public Field(String _label, int _type, Object _val, double min, double max, double mod)
	{
		label = _label;
		type = _type;
		defVal = _val;
		resMin = min;
		resMax = max;
		resMod = mod;
	}
}
