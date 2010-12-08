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
 * Panel contenant les boutons pour le transport
 */
package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.rt.Connection;
import holoedit.util.Ut;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

public class TransportPanel extends FloatingWindow
{
	// reference sur main
	HoloEdit holoEditRef;
	// bouton de controle du transport
	public JButton preload;
	public JButton stop;
	public JButton start;
	public JButton resume;
	public JButton loop;
	public JButton rec;
	public ImageIcon playState, stopState, pauseState, resumeState, loopOffState, loopOnState, recOnState, recOffState, collapseIcon, expandIcon;
	// booleen d'etat du mode loop
	public boolean looping = false;
	// reference aux images pour le mode loop (car elles changent suivant l'etat du mode contrairement aux autres boutons.
	public Connection conRef;

	private Font verdana10 = new Font("Verdana",0, 10);
	private Font verdana14 = new Font("Verdana",0, 14);
	private JLabel whatsConnectedLabel;

	public TransportPanel(HoloEdit owner)
	{
		super("Transport", owner, owner.wsTransW, owner.wsTransH*2, owner.wlTransX, owner.wlTransY, owner.wbTrans);
	//	setResizable(true);
		holoEditRef = owner;
		conRef = holoEditRef.connection;
		JPanel transp = new JPanel();
		Insets border = new Insets(0, 0, 0, 0);
		transp.setLayout(new FlowLayout(FlowLayout.CENTER, 1 ,3));
		// ------------------ bouton static ---------------------------/
		try
		{
			ImageIcon positionButtonIcon = new ImageIcon("./images/preload.gif");
			preload = new JButton(positionButtonIcon);
		}
		catch (NullPointerException npe)
		{
			preload = new JButton("Position");
		}
		preload.setMargin(border);
		preload.setVisible(true);
		preload.setEnabled(true);
		preload.setToolTipText("position at begin time");
		preload.setSize(new java.awt.Dimension(60, 30));
		preload.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				conRef.preloadFirst();
				conRef.position();
			}
		});
		transp.add(preload);
		// ------------------ bouton stop ---------------------------/
		try
		{
			ImageIcon stopButtonIcon = new ImageIcon("./images/stop.gif");
			stop = new JButton(stopButtonIcon);
		}
		catch (NullPointerException npe)
		{
			stop = new JButton("Stop");
		}
		stop.setMargin(border);
		stop.setVisible(true);
		stop.setEnabled(true);
		stop.setToolTipText("stop");
		stop.setSize(new java.awt.Dimension(60, 30));
		stop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				conRef.stop();
			}
		});
		transp.add(stop);
		// ------------------ bouton start ---------------------------/
		try
		{
			stopState = new ImageIcon("./images/play.gif");
			playState = new ImageIcon("./images/play2.gif");
			start = new JButton(stopState);
		}
		catch (NullPointerException npe)
		{
			start = new JButton("Start");
		}
		start.setMargin(border);
		start.setVisible(true);
		start.setEnabled(true);
		start.setToolTipText("start");
		start.setSize(new java.awt.Dimension(60, 30));
		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				conRef.play();
				start.setIcon(playState);
			}
		});
		transp.add(start);
		// ------------------ bouton continu ---------------------------/
		try
		{
			resumeState = new ImageIcon("./images/resume.gif");
			pauseState = new ImageIcon("./images/pause.gif");
			resume = new JButton(resumeState);
		}
		catch (NullPointerException npe)
		{
			resume = new JButton("Continu");
		}
		resume.setMargin(border);
		resume.setVisible(true);
		resume.setEnabled(true);
		resume.setToolTipText("pause/resume");
		resume.setSize(new java.awt.Dimension(60, 30));
		resume.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				conRef.pause();
			}
		});
		transp.add(resume);
		// ------------------ bouton loop ---------------------------/
		try
		{
			loopOffState = new ImageIcon("./images/loop.gif");
			loopOnState = new ImageIcon("./images/loop2.gif");
			loop = new JButton(loopOffState);
		}
		catch (NullPointerException npe)
		{
			loop = new JButton("loop");
		}
		loop.setMargin(border);
		loop.setVisible(true);
		loop.setEnabled(true);
		loop.setToolTipText("loop on/off");
		loop.setSize(new java.awt.Dimension(60, 30));
		loop.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)
			{
				conRef.changeLoop();
			}
		});
		transp.add(loop);
		// ------------------ bouton rec ---------------------------/
		try
		{
			recOffState = new ImageIcon("./images/rec.gif");
			recOnState = new ImageIcon("./images/rec2.gif");
			rec = new JButton(recOffState);
		}
		catch (NullPointerException npe)
		{
			rec = new JButton("rec");
		}
		rec.setMargin(border);
		rec.setVisible(true);
		rec.setEnabled(true);
		rec.setToolTipText("Record on/off");
		rec.setSize(new java.awt.Dimension(60, 30));
		rec.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				conRef.record(!conRef.isRecording());
			}
		});
		transp.add(rec);
		setLayout(new BorderLayout());
		add(transp, BorderLayout.NORTH);
		add(owner.counterPanel, BorderLayout.CENTER);
		
		// DETAILS DE LA CONNECTION : 
		JPanel detailsButtonPanel = new JPanel();
		detailsButtonPanel.setLayout(new BoxLayout(detailsButtonPanel, BoxLayout.Y_AXIS));	

		detailsButtonPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 20)));
		JLabel statusLabel = new JLabel("Connected to : ");
		statusLabel.setFont(verdana10);
		whatsConnectedLabel = new JLabel(" -- ");
		whatsConnectedLabel.setFont(verdana14);;
		detailsButtonPanel.add(statusLabel);
		detailsButtonPanel.add(whatsConnectedLabel);
		detailsButtonPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 20)));
		final ConnectionPanel connectionSettingsPanel = holoEditRef.connectionPanel;
		connectionSettingsPanel.setVisible(false);

		expandIcon = new ImageIcon("./images/expand.png");
		collapseIcon = new ImageIcon("./images/collapse.png");
		detailsButtonPanel.add(connectionSettingsPanel);
		JButton connectionSettings = new JButton("Show connection settings...");
		connectionSettings.setIcon(expandIcon);
		connectionSettings.setMargin(new Insets(1, 10, 1, 10));
		connectionSettings.setBorderPainted(false); // pour ressembler a un simple label

		
		connectionSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JButton button = (JButton) e.getSource();
				if (connectionSettingsPanel.isVisible()) {
					button.setText("Show connection settings...");
					button.setIcon(expandIcon);
					connectionSettingsPanel.setVisible(false);
					setSize(getWidth() , (int) (getHeight()/1.5));
				} else {
					button.setText("Hide connection settings..."); 
					button.setIcon(collapseIcon);
					connectionSettingsPanel.setVisible(true);
					setSize(getWidth() , (int) (getHeight()*1.5));
				}
				pack();
			}
		});

		detailsButtonPanel.add(connectionSettings);
		detailsButtonPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
		
		add(detailsButtonPanel, BorderLayout.SOUTH);
		pack();
	}

	public void setConnectionStatus(String whatsConnected, boolean updateTextFields){
		
		if (whatsConnected==null) {
			whatsConnectedLabel.setText(" -- ");
			holoEditRef.connectionPanel.connectButton.setText("connect");				
		} else {
			whatsConnectedLabel.setText(whatsConnected);
			holoEditRef.connectionPanel.connectButton.setText("disconnect");
		}
	
		if (updateTextFields){
			String port = holoEditRef.connection.getOut()+"";
			String address = holoEditRef.connection.getAddress();
			holoEditRef.connectionPanel.IPtextField.setText(address);
			holoEditRef.connectionPanel.portTextField.setText(port);
		}
	}
/*
	public void activer()
	{
		preload.setEnabled(true);
		stop.setEnabled(true);
		start.setEnabled(true);
		resume.setEnabled(true);
		loop.setEnabled(true);
		rec.setEnabled(true);
		this.repaint();
	}

	public void desactiver()
	{
		preload.setEnabled(false);
		stop.setEnabled(false);
		start.setEnabled(false);
		resume.setEnabled(false);
		loop.setEnabled(false);
		rec.setEnabled(false);
		this.repaint();
	}
	*/
	public String toString()
	{
		return "\t<transport"+super.toString();
	}
	
	public void resetPositionAndSize()
	{
		setLocation(oposX, oposY);
		pack();
	}
}
