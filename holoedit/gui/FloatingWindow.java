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
import holoedit.util.Ut;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

public class FloatingWindow extends JFrame implements FocusListener, WindowListener, ComponentListener
{
	public int sizW,sizH,posX,posY;
	public int osizW,osizH,oposX,oposY;
	public boolean visible = false;
	private JRootPane jrp;
	private JPanel pan;
	protected Font fo = new Font("Verdana",0, 10);
	private JLabel statusBar;
	private HoloEdit mr;

	public FloatingWindow(String title, HoloEdit owner, int sizeW, int sizeH, int posiX, int posiY, boolean visi)
	{
		super(title);

		mr = owner;
		
		osizW = sizW = sizeW;
		osizH = sizH = sizeH;
		oposX = posX = posiX;
		oposY = posY = posiY;
		visible = visi;
		
		jrp = getRootPane();
		jrp.setLayout(new BorderLayout());
		jrp.setFocusable(false);
		
		pan = new JPanel();
		pan.setVisible(true);
		pan.setFocusable(false);
		pan.setLayout(new BorderLayout());
		statusBar = new JLabel();
		statusBar.setFont(statusBar.getFont().deriveFont(10f));
		statusBar.setVisible(true);
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(statusBar,BorderLayout.WEST);
		add(jp, BorderLayout.SOUTH);		
		jrp.add(pan, BorderLayout.CENTER);
		
		setJMenuBar(Ut.barMenu);
		setResizable(false);
		setSize(sizW,sizH);
		setLocation(posX,posY);
		setFocusable(true);
		addFocusListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		addComponentListener(this);
	}
	
	public Component add(Component c)
	{
		return pan.add(c);
	}
	
	public void add(Component c, String constraints) 
	{
		pan.add(c,constraints);
	}
	
	public void remove(Component c)
	{
		pan.remove(c);
	}
	
	public void unsetLayout()
	{
		pan.setLayout(null);
	}

	public void setLayout(FlowLayout fl)
	{
		pan.setLayout(fl);
	}
	
	public void setLayout(GridBagLayout gbl)
	{
		pan.setLayout(gbl);
	}
	
	public void setLayout(GridLayout gl)
	{
		pan.setLayout(gl);
	}
	
	public void setLayout(BorderLayout bl)
	{
		pan.setLayout(bl);
	}
	
	public void setBoxLayout(int axis)
	{
		pan.setLayout(new BoxLayout(pan ,axis));
	}
	
	public void close()
	{
		visible = false;
		setVisible(visible);
	}
	
	public void open()
	{
		visible = true;
		setVisible(visible);
		toFront();
	}
	
	public void setVisi(boolean visi)
	{
		visible = false;
		setVisible(visible);
	}

	public void setLocation(int x,int y)
	{
		posX = x;
		posY = y;
		super.setLocation(posX,posY);
	}
	
	public void setSize(int w, int h)
	{
		sizW = w;
		sizH = h;
		super.setSize(sizW,sizH);
	}

	public void setResizable(boolean resizable)
	{
		super.setResizable(resizable);
	}
	
	protected void toStatus(String s)
	{
		statusBar.setText("\t"+s);
	}

	public void focusGained(FocusEvent e)
	{
		updateMenuBar();
	}

	protected void updateMenuBar()
	{
		try
		{
			Ut.barMenu.update();
			setJMenuBar(Ut.barMenu);
			Ut.barMenu.setParent(this);
		} catch( NullPointerException e) {
		}
	}

	public void focusLost(FocusEvent e)
	{
		try
		{
			for (LabelAudio lab:mr.gestionPistes.ts.labelAudio)
				lab.Disable();
		} catch (Exception eee) {}
	}

	public String toString()
	{
		return " x=\""+posX+"\" y=\""+posY+"\" w=\""+sizW+"\" h=\""+sizH+"\" v=\""+visible+"\"/>\n";
	}

	public void setStarred(boolean b)
	{
		if(Ut.MAC)
			jrp.putClientProperty("windowModified", b);
		else {
			String title = getTitle();
			if(b) {
				if(!title.endsWith("*"))
					title += "*";
			} else {
				if(title.endsWith("*"))
					title = title.substring(0,title.length() - 1);
			}
			super.setTitle(title);
		}
	}
	
	public void resetPositionAndSize()
	{
		setLocation(oposX, oposY);
		setSize(osizW, osizH);
	}

	public void windowOpened(WindowEvent e)
	{
		updateMenuBar();
	}

	public void windowClosing(WindowEvent e)
	{
		close();
		if(mr != null)
			mr.checkWindows();
	}

	public void windowClosed(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e)
	{
		updateMenuBar();
	}

	public void windowActivated(WindowEvent e)
	{
		updateMenuBar();
	}

	public void windowDeactivated(WindowEvent e) {}
	
	public void componentResized(ComponentEvent e)
	{
		sizW = getWidth();
		sizH = getHeight();
	}

	public void componentMoved(ComponentEvent e)
	{
		posX = (int)getLocation().getX();
		posY = (int)getLocation().getY();
	}

	public void componentShown(ComponentEvent e) {}

	public void componentHidden(ComponentEvent e) {}
}
