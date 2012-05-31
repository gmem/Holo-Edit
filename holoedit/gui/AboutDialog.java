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
 * Classe pour la fenetre "a propos de..."
 */
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.util.Ut;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class AboutDialog extends FloatingModalWindow implements ActionListener
{
	private JPanel TheTopPanel;
	private JPanel TheCenterPanel;
	private JPanel TheBottomPanel;
	private JButton TheCloseButton;
	// constructeur
	public AboutDialog (HoloEdit owner, String title)
	{
		super(title,owner.wsAboutW,owner.wsAboutH,owner.wlAboutX,owner.wlAboutY,true);

		setLayout(new BorderLayout());
	    TheTopPanel = new JPanel();
	    TheTopPanel.add(new JLabel(Ut.hv));
	    add(TheTopPanel, "North");
	    TheCenterPanel = new JPanel();
		
	    Font f = new Font("courrier", Font.PLAIN, 10);
	    
	    JLabel url,mel;
	    JTextArea lab1,lab2,lab3;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		TheCenterPanel.setLayout(gridbag);
		c.insets = new Insets(5,5,5,5);
		
	    lab1 = new JTextArea("build : "+owner.buildDate +
	    		"\n" +
	    		"\n" +
	    		"Holo-Edit, spatial sound trajectories editor, part of Holophon" +
	    		"\nCopyright (C) 2006 GMEM" +
	    		"\n" +
	    		"\nThis program is free software; you can redistribute it and/or modify" +
	    		"\nit under the terms of the GNU General Public License as published by" +
	    		"\nthe Free Software Foundation; either version 2 of the License, or" +
	    		"\nany later version" +
	    		"\n" +
	    		"\nThis program is distributed in the hope that it will be useful," +
	    		"\nbut WITHOUT ANY WARRANTY; without even the implied warranty of" +
	    		"\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" +
	    		"\nGNU General Public License for more details." +
	    		"\n" +
	    		"\nYou should have received a copy of the GNU General Public License" +
	    		"\nalong with this program; if not, write to the Free Software" +
	    		"\nFoundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n");  
		lab1.setFont(f);
		lab1.setSize(400,480);	
		lab1.setEditable(false);
		lab1.setFocusable(false);
		lab1.setBackground(null);
		lab1.setOpaque(false);
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(lab1, c);
	    TheCenterPanel.add(lab1); 
	
	    lab2 = new JTextArea("Holophon (Holo-Edit & Holo-Spat)\nwas designed and programmed by :");  
		lab2.setFont(f);
		lab2.setSize(400,30);	
		lab2.setEditable(false);
		lab2.setFocusable(false);
		lab2.setBackground(null);
		lab2.setOpaque(false);
		c.gridx = 0;
		c.gridy = 1;
		gridbag.setConstraints(lab2, c);
	    TheCenterPanel.add(lab2); 
	
	    lab3 = new JTextArea("Laurent Pottier, Leopold Frey,\nCharles Bascou, Jerome Decque,\nBenjamin Cabaud and Jerome Hulin.");  
		lab3.setFont(f);
		lab3.setSize(400,30);	
		lab3.setEditable(false);
		lab3.setFocusable(false);
		lab3.setBackground(null);
		lab3.setOpaque(false);
		c.gridx = 0;
		c.gridy = 4;
		gridbag.setConstraints(lab3, c);
	    TheCenterPanel.add(lab3); 
	
		// pour ouvrir la page d'accueil du gmem lors du clic sur l'url
	    url = new JLabel("www.gmem.org");  
		url.setFont(f);
		url.setSize(400,30);	
	 	url.setForeground(Color.blue);
	 	url.addMouseListener(new MouseAdapter()
	 	{
	 		public void mouseClicked(MouseEvent e)
	 		{
	 			Runtime r = Runtime.getRuntime();
	 			try {
	 				if(Ut.MAC)
	 					r.exec("open http://www.gmem.org");
	 				else if(Ut.LINUX)
	 					r.exec("firefox http://www.gmem.org");
	 				else
	 					r.exec("cmd /c start http://www.gmem.org");
	 			} catch (Exception exc) {}
	 			TheCloseCommand();
	 		}
	 	});
		c.gridx = 0;
		c.gridy = 5;
		gridbag.setConstraints(url, c);
	    TheCenterPanel.add(url); 
	
		// pour creer un nouveau mail avec l'outil mail par defaut avec l'adresse "dvlpt@gmem.org"
	    mel = new JLabel("holophon@gmem.org");  
		mel.setFont(f);
		mel.setSize(400,30);	
	 	mel.setForeground(Color.blue);
	 	mel.addMouseListener(new MouseAdapter()
	 	{
	 		public void mouseClicked(MouseEvent e)
	 		{
	 			Runtime r = Runtime.getRuntime();
	 			try {
	 				if(Ut.MAC)
	 					r.exec("open mailto:holophon@gmem.org");
	 				else if(Ut.LINUX)
	 					r.exec("firefox mailto:holophon@gmem.org");
	 				else
	 					r.exec("cmd /c start mailto:holophon@gmem.org");
	 			} catch (Exception exc) {}
	 			TheCloseCommand();
	 		}
	 	});
		c.gridx = 0;
		c.gridy = 6;
		gridbag.setConstraints(mel, c);
	    TheCenterPanel.add(mel); 
	  
	 	try {		
		  	ImageIcon gmem = new ImageIcon ("./images/gmem.gif") ;
	    	TheCloseButton = new JButton(gmem) ;
	    } catch ( NullPointerException npe ) {
		    TheCloseButton = new JButton ( "close" ) ;
		}
		TheCloseButton.setVisible(true);
		TheCloseButton.setEnabled(true);
		TheCloseButton.setToolTipText("close this window");
		TheCloseButton.setSize(new java.awt.Dimension(230, 125));
		TheCloseButton.addActionListener(this);
		TheCloseButton.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent fe)
			{
				dispose();
			}
		});
		
	    add(TheCenterPanel, "Center");
	    TheBottomPanel = new JPanel();
	    TheBottomPanel.add(TheCloseButton);
	    add(TheBottomPanel, "South");

	    this.addKeyListener(new KeyAdapter() {
	    	public void keyPressed(KeyEvent e)
	    	{
	    		TheCloseCommand();
	    	}
	    });
	    
	}
   
	public void actionPerformed(ActionEvent TheActionEvent)
	{
	    Object TheObject = TheActionEvent.getSource();
	      
		if (TheObject instanceof JButton)
		{
			if (TheObject == TheCloseButton)
			{
				this.TheCloseCommand();
			}
		}
	}

	private void TheCloseCommand ()
	{
		this.setVisible(false);
		this.dispose();
	}
}