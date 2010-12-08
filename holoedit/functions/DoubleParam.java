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
/** ************* DoubleParam ****************** */
/**
 * Champ d'entree de type double dans les fonctions 
 */
package holoedit.functions;

import holoedit.util.Formatter;
import holoedit.util.Ut;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

class DoubleParam extends JTextField implements Param, FocusListener, KeyListener
{
	private static Formatter rf = new Formatter(-1,-1,-1,4);
	private static Formatter rf2 = new Formatter(-1,-1,-1,2);
	// attributs graphiques
	private int w = 80;
	private int h = 25;
	private Color selColor = Color.white;
	// numero du champ
	private int numero;
	private double defVal;
	private double min = Double.MIN_VALUE, max = Double.MAX_VALUE;
	private double mod = -1;
	//
	double tsens = 0;
	double tval = 0;
	static Thread t;

	public DoubleParam(int _numero, Object _val)
	{
		numero = _numero;
		if(_val.getClass().equals(String.class))
		{
			String s = (String)_val;
			defVal = Double.parseDouble(s);
			setText(s);
		} else {
			defVal = (Double)_val;
			setText(doubleToString(defVal));
		}
		init();
	}

	public DoubleParam(int _numero, Object _val, double _min, double _max)
	{
		numero = _numero;
		if(_val.getClass().equals(String.class))
		{
			String s = (String)_val;
			defVal = Double.parseDouble(s);
			setText(s);
		} else {
			defVal = (Double)_val;
			setText(doubleToString(defVal));
		}
		min = _min;
		max = _max;
		init();
	}

	public DoubleParam(int _numero, Object _val, double _min, double _max, double _mod)
	{
		numero = _numero;
		if(_val.getClass().equals(String.class))
		{
			String s = (String)_val;
			defVal = Double.parseDouble(s);
			setText(s);
		} else {
			defVal = (Double)_val;
			setText(""+defVal);
		}
		min = _min;
		max = _max;
		mod = _mod > 0 ? _mod : -1;
		init();
	}

	public void init()
	{
		setFont(f);
		setSelectedTextColor(selColor);
		setAutoscrolls(false);
		setVisible(true);
		setHorizontalAlignment(JTextField.CENTER);
		setSize(w, h);
		addFocusListener(this);
		addKeyListener(this);
	}

	public int getNumero()
	{
		return (numero);
	}

	public int getType()
	{
		return TYPE_DOUBLE;
	}

	public Object getValue()
	{
		return check();
	}

	public double check()
	{
		double v;
		try
		{
			v = stringToDouble(getText());
			if (mod != -1)
				v = Ut.modSigned(v, mod);
			if (min != Double.MIN_VALUE)
				v = Ut.clipL(v, min);
			if (max != Double.MAX_VALUE)
				v = Ut.clipU(v, max);
		}
		catch (NumberFormatException e)
		{
			v = defVal;
		}
		return v;
	}

	public void setValue(Object _val)
	{
		if(_val.getClass().equals(String.class))
			setText((String)_val);
		else
			setText(doubleToString((Double)_val));
	}

	public void focusLost(FocusEvent e)
	{
		if(t != null && t.isAlive())
			t.interrupt();
		if(tsens == 1 || tsens == -1)
		{
			setText(""+rf.format(tval));
		}
		setText(doubleToString(check()));
	}

	public void focusGained(FocusEvent e)
	{
		selectAll();
	}

	// pour l'unicite (voir dialogparam)
	public void addItemListener(ItemListener il) {}

	public static String doubleToString(double d)
	{
		return rf.format(d);
	}

	public static double stringToDouble(String s) throws NumberFormatException
	{
		return Double.valueOf(s).doubleValue();
	}

	public void keyTyped(KeyEvent e){}

	public void keyPressed(KeyEvent e)
	{
		tval = check();
		if (e.getKeyCode() == KeyEvent.VK_UP && !e.isShiftDown())
			tsens = 1;
		else if (e.getKeyCode() == KeyEvent.VK_DOWN && !e.isShiftDown())
			tsens = -1;
		if (e.getKeyCode() == KeyEvent.VK_UP && e.isShiftDown())
			tsens = 0.01;
		else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.isShiftDown())
			tsens = -0.01;
		
		if(tsens != 0)
		{
			if(t == null || !t.isAlive())
			{
				t = new Thread()
				{
					public void run()
					{
						try
						{
							while (tsens != 0)
							{
								tval += tsens;
								setText(""+rf2.format(tval));
								if(Math.abs(tsens) == 1)
									sleep(100);
								else
									sleep(50);
							}
						}
						catch (InterruptedException ex)
						{
							tsens = 0;
						}
					}
				};
				t.setPriority(Thread.MIN_PRIORITY);
				t.setName("Key-NumberBox"+numero);
				t.start();
			}
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if(t != null && t.isAlive())
			t.interrupt();
		if(tsens == 1 || tsens == -1)
		{
			tsens = 0;
			setText(""+rf.format(tval));
			setText(doubleToString(check()));
		}
	}
}
