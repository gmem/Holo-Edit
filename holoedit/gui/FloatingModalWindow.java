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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class FloatingModalWindow extends JDialog implements MouseListener, MouseMotionListener
{
	public int sizW,sizH,posX,posY;
	public boolean visible = false;
	private Container cp;
//	private TitleBar tb;
	private JPanel pan;
	protected Font fo = new Font("Verdana",0, 10);
	private int dTX, dTY;
	private boolean displaced = false;
	
	public FloatingModalWindow(String title, int sizeW, int sizeH, int posiX, int posiY, boolean visi)
	{
		super(new Frame(),title);
		
		sizW = sizeW;
		sizH = sizeH;
		posX = posiX;
		posY = posiY;
		visible = visi;

		cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.setFocusable(false);
		
//		tb = new TitleBar(title,sizW);
//		tb.addMouseListener(this);
//		tb.addMouseMotionListener(this);
//		tb.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent arg0)
//			{
//				close();
//			}
//		});
		
		pan = new JPanel();
		pan.setVisible(true);
		pan.setFocusable(false);
		
//		cp.add(tb, BorderLayout.NORTH);
		cp.add(pan, BorderLayout.CENTER);
		
		setModal(true);
//		setUndecorated(true);
		setResizable(false);
		setSize(sizW,sizH);
		setLocation(posX,posY);
		setFocusable(true);
	}

	public Component add(Component c)
	{
		return pan.add(c);
	}
	
	public void add(Component c,  String constraints) 
	{
		pan.add(c,constraints);
	}
	
	public void setLayout(FlowLayout fl)
	{
		pan.setLayout(fl);
	}
	
	public void setLayout(GridBagLayout gbl)
	{
		pan.setLayout(gbl);
	}
	
	public void setLayout(BorderLayout bl)
	{
		pan.setLayout(bl);
	}
	
	public void setLayout(GridLayout gl)
	{
		pan.setLayout(gl);
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
	
	public void mousePressed(MouseEvent arg0)
	{
		dTX = arg0.getX();
		dTY = arg0.getY();
		displaced = true;
	}

	public void mouseDragged(MouseEvent arg0)
	{
		if(displaced)
		{
			displace(arg0);
		}
	}

	public void displace(MouseEvent e)
	{
		if(displaced)
		{
			int tmpX = e.getX() - dTX;
			int tmpY = e.getY() - dTY;
			posX = this.getLocation().x;
			posY = this.getLocation().y;
			posX += tmpX;
			posY += tmpY;
			this.setLocation(posX, posY);
		} else {
			dTX = e.getX();
			dTY = e.getY();
			displaced = true;
		}
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
//		if(tb != null)
//			tb.setWidth(sizW);
		super.setSize(sizW,sizH);
	}
	
	public void mouseReleased(MouseEvent arg0){}
	public void mouseClicked(MouseEvent arg0){}
	public void mouseEntered(MouseEvent arg0){}
	public void mouseExited(MouseEvent arg0){}
	public void mouseMoved(MouseEvent arg0){}

//	public String getTitle()
//	{
//		return tb.getTitle();
//	}
//
//	public void setTitle(String title)
//	{
//		tb.setTitle(title);
//	}
}