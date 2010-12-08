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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class PrefPane extends FloatingModalWindow implements KeyListener
{
	protected JButton okButton,cancelButton;
	protected JLabel prefsText;
	private String[] coordsChoice = { "Cartesian", "Polar" };
	private NumberBox maxRecentCpt;
	private JCheckBox openLastChk;
	private JCheckBox autoTkSel;
	private JCheckBox onlyEdPts;
	private JCheckBox viewHPs;
	private JCheckBox sdifImpOpt;
	private JComboBox coords;
	private NumberBox lastPointsNb;
	private FloatNumberBox scrollSpeedNb;
	private JCheckBox oscOpen;
	private JCheckBox oscBonjour;
	private JCheckBox smooth;
	private JTextField oscPortIn;
	private JTextField oscPortOut;
	private JTextField oscAddress;
	private JTextField oscKeyIn;
	private JTextField oscKeyOut;
	private HoloEdit holoEditRef;

	public PrefPane(HoloEdit m)
	{
		super("Preferences", m.wsPrefW, m.wsPrefH, m.wlPrefX, m.wlPrefY, true);
		holoEditRef = m;
		setLayout(new BorderLayout(10, 10));
		prefsText = new JLabel(Ut.hv + " Preferences");
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		textPanel.add(prefsText);
		add(textPanel, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		// OPTION SETTINGS
		JPanel optionSettings = new JPanel(new GridLayout(10, 2));
		optionSettings.addKeyListener(this);
		
		maxRecentCpt = new NumberBox(m.maxRecentFiles);
		maxRecentCpt.setHorizontalAlignment(NumberBox.CENTER);
		maxRecentCpt.addKeyListener(this);
		
		openLastChk = new JCheckBox();
		openLastChk.setHorizontalAlignment(JCheckBox.CENTER);
		openLastChk.setSelected(m.openLastOnLoad);
		openLastChk.addKeyListener(this);
		
		autoTkSel = new JCheckBox();
		autoTkSel.setHorizontalAlignment(JCheckBox.CENTER);
		autoTkSel.setSelected(m.allTrackActive);
		autoTkSel.addKeyListener(this);
		
		onlyEdPts = new JCheckBox();
		onlyEdPts.setSelected(m.viewOnlyEditablePoints);
		onlyEdPts.setHorizontalAlignment(JCheckBox.CENTER);
		onlyEdPts.addKeyListener(this);
		
		viewHPs = new JCheckBox();
		viewHPs.setSelected(m.viewSpeakers);
		viewHPs.setHorizontalAlignment(JCheckBox.CENTER);
		viewHPs.addKeyListener(this);
		
		sdifImpOpt = new JCheckBox();
		sdifImpOpt.setSelected(m.sdifExpert);
		sdifImpOpt.setHorizontalAlignment(JCheckBox.CENTER);
		sdifImpOpt.addKeyListener(this);		
		
		coords = new JComboBox(coordsChoice);
		coords.setSelectedIndex(m.coordinates);
		coords.setSize(50, 10);
		coords.addKeyListener(this);
		
		lastPointsNb = new NumberBox(Ut.drawPtsNb);
		lastPointsNb.setHorizontalAlignment(NumberBox.CENTER);
		lastPointsNb.addKeyListener(this);
		
		scrollSpeedNb = new FloatNumberBox(10000.0f / m.scrollSpeed);
		scrollSpeedNb.setHorizontalAlignment(NumberBox.CENTER);
		scrollSpeedNb.addKeyListener(this);
		
		smooth = new JCheckBox();
		smooth.setSelected(HoloEdit.SMOOTH);
		smooth.setHorizontalAlignment(JCheckBox.CENTER);
		smooth.addKeyListener(this);

		optionSettings.add(new JLabel("Maximum recent files : ",JLabel.RIGHT));
		optionSettings.add(maxRecentCpt);
		optionSettings.add(new JLabel("Open last file on startup : ",JLabel.RIGHT));
		optionSettings.add(openLastChk);
		optionSettings.add(new JLabel("Automatic track selection : ",JLabel.RIGHT));
		optionSettings.add(autoTkSel);
		optionSettings.add(new JLabel("View only editable points : ",JLabel.RIGHT));
		optionSettings.add(onlyEdPts);
		optionSettings.add(new JLabel("View speakers : ",JLabel.RIGHT));
		optionSettings.add(viewHPs);
		optionSettings.add(new JLabel("SDIF import options : ",JLabel.RIGHT));
		optionSettings.add(sdifImpOpt);
		optionSettings.add(new JLabel("Coordinates : ",JLabel.RIGHT));
		optionSettings.add(coords);
		optionSettings.add(new JLabel("Draw points number : ", JLabel.RIGHT));
		optionSettings.add(lastPointsNb);
		optionSettings.add(new JLabel("Scroll speed (%) : ", JLabel.RIGHT));
		optionSettings.add(scrollSpeedNb);
		optionSettings.add(new JLabel("Smooth : ",JLabel.RIGHT));
		optionSettings.add(smooth);

		tabbedPane.addTab("Options", null, optionSettings, "Options settings");
		
		// OSC Settings
		JPanel oscSettings = new JPanel(new GridLayout(7, 2));
		
		oscOpen = new JCheckBox();
		oscOpen.setHorizontalAlignment(JCheckBox.CENTER);
	//	oscOpen.setSelected(mainRef.connection.isOpen());
		oscOpen.setSelected(true);
		oscOpen.addKeyListener(this);
		
		oscBonjour = new JCheckBox();
		oscBonjour.setHorizontalAlignment(JCheckBox.CENTER);
		oscBonjour.setSelected(holoEditRef.bonjour);
		oscBonjour.setEnabled(holoEditRef.bonjourInstalled);
		oscBonjour.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean sel = ((JCheckBox)e.getSource()).isSelected();
				oscPortOut.setEnabled(!sel);
				oscAddress.setEnabled(!sel);
				oscKeyOut.setEnabled(!sel);
			}
		});
		oscBonjour.addKeyListener(this);
		
		oscPortIn = new JTextField(""+m.connection.getIn());
		oscPortIn.setHorizontalAlignment(JTextField.CENTER);
		oscPortIn.setFont(NumberBox.f);
		oscPortIn.setPreferredSize(new Dimension(60,20));
		oscPortIn.setMinimumSize(new Dimension(60,20));
		oscPortIn.setMaximumSize(new Dimension(60,20));
		oscPortIn.addKeyListener(this);
		
		oscPortOut = new JTextField(""+m.connection.getOut());
		oscPortOut.setHorizontalAlignment(JTextField.CENTER);
		oscPortOut.setFont(NumberBox.f);
		oscPortOut.setPreferredSize(new Dimension(60,20));
		oscPortOut.setMinimumSize(new Dimension(60,20));
		oscPortOut.setMaximumSize(new Dimension(60,20));
		oscPortOut.setEnabled(!holoEditRef.bonjour);
		oscPortOut.addKeyListener(this);
		
		oscAddress = new JTextField(m.connection.getAddress());
		oscAddress.setHorizontalAlignment(JTextField.CENTER);
		oscAddress.setFont(NumberBox.f);
		oscAddress.setPreferredSize(new Dimension(150,20));
		oscAddress.setMinimumSize(new Dimension(150,20));
		oscAddress.setMaximumSize(new Dimension(150,20));
		oscAddress.setEnabled(!holoEditRef.bonjour);
		oscAddress.addKeyListener(this);
		
		oscKeyIn = new JTextField(m.connection.getKeyIn());
		oscKeyIn.setHorizontalAlignment(JTextField.CENTER);
		oscKeyIn.setFont(NumberBox.f);
		oscKeyIn.setPreferredSize(new Dimension(100,20));
		oscKeyIn.setMinimumSize(new Dimension(100,20));
		oscKeyIn.setMaximumSize(new Dimension(100,20));
		oscKeyIn.addKeyListener(this);
		
		oscKeyOut = new JTextField(m.connection.getKeyOut());
		oscKeyOut.setHorizontalAlignment(JTextField.CENTER);
		oscKeyOut.setFont(NumberBox.f);
		oscKeyOut.setPreferredSize(new Dimension(100,20));
		oscKeyOut.setMinimumSize(new Dimension(100,20));
		oscKeyOut.setMaximumSize(new Dimension(100,20));
		oscKeyOut.setEnabled(!holoEditRef.bonjour);
		oscKeyOut.addKeyListener(this);
		
		oscSettings.add(new JLabel("OSC enable : ", JLabel.RIGHT));
		oscSettings.add(oscOpen);
		oscSettings.add(new JLabel("Bonjour enabled : ", JLabel.RIGHT));
		oscSettings.add(oscBonjour);
		oscSettings.add(new JLabel("Input port : ", JLabel.RIGHT));
		oscSettings.add(oscPortIn);
		oscSettings.add(new JLabel("Output port : ", JLabel.RIGHT));
		oscSettings.add(oscPortOut);
		oscSettings.add(new JLabel("Output IP : ", JLabel.RIGHT));
		oscSettings.add(oscAddress);
		oscSettings.add(new JLabel("OSC key in : ", JLabel.RIGHT));
		oscSettings.add(oscKeyIn);
		oscSettings.add(new JLabel("OSC key out : ", JLabel.RIGHT));
		oscSettings.add(oscKeyOut);
		
		tabbedPane.addTab("OSC ", null, oscSettings, "Osc Settings");
		
		add(tabbedPane, BorderLayout.CENTER);
		okButton = new JButton("OK");
		okButton.addKeyListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addKeyListener(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeOk();
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});
		add(buttonPanel, BorderLayout.SOUTH);

		addKeyListener(this);
	}

	public void setOscPortOut(String text){
		oscPortOut.setText(text);
		closeOk();
	}
	
	public void setOscAddress(String text){
		oscAddress.setText(text);
		closeOk();
	}
	
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			close();
		else if(e.getKeyCode() == KeyEvent.VK_ENTER)
			closeOk();
	}

	public void closeOk()
	{
		close();
		holoEditRef.maxRecentFiles = maxRecentCpt.getVal();
		holoEditRef.openLastOnLoad = openLastChk.isSelected();
		holoEditRef.allTrackActive = autoTkSel.isSelected();
		holoEditRef.viewOnlyEditablePoints = onlyEdPts.isSelected();
		holoEditRef.viewSpeakers = viewHPs.isSelected();
		holoEditRef.sdifExpert = sdifImpOpt.isSelected();
		holoEditRef.coordinates = coords.getSelectedIndex();
		holoEditRef.scrollSpeed = 10000.0f / scrollSpeedNb.getVal();
		HoloEdit.SMOOTH = smooth.isSelected();
		Ut.drawPtsNb = lastPointsNb.getVal();
		boolean wasOpen = holoEditRef.connection.isOpen();
		boolean changes = false;
		if(holoEditRef.bonjour != oscBonjour.isSelected())
		{
			System.out.println("in pref pan : mainRef.bonjour="+holoEditRef.bonjour);
			holoEditRef.bonjour = oscBonjour.isSelected();
			changes = true;
		}

		try
		{
			int i = Integer.parseInt(oscPortIn.getText());
			if(i != holoEditRef.connection.getIn())
			{
				holoEditRef.connection.setIn(i);
				changes = true;
			}
			int o = Integer.parseInt(oscPortOut.getText());
			if(o != holoEditRef.connection.getOut())
			{
				holoEditRef.connection.setOut(o);
				changes = true;
			}
		} catch (NumberFormatException e){}
		
		if(!oscAddress.getText().equalsIgnoreCase(holoEditRef.connection.getAddress()))
		{
			holoEditRef.connection.setAddress(oscAddress.getText());
			changes = true;
		}
		if(!oscKeyIn.getText().equalsIgnoreCase(holoEditRef.connection.getKeyIn()))
		{
			holoEditRef.connection.setKeyIn(oscKeyIn.getText());
			changes = true;
		}
		if(!oscKeyOut.getText().equalsIgnoreCase(holoEditRef.connection.getKeyOut()))
		{
			holoEditRef.connection.setKeyOut(oscKeyOut.getText());
			changes = true;
		}
		if(oscOpen.isSelected() == wasOpen)
		{
			System.out.println("oscOpen.isSelected() == wasOpen="+wasOpen);
			if(wasOpen && changes)
			{
				holoEditRef.connection.close();
				holoEditRef.connection.open();
			}
		} else {
			if(oscOpen.isSelected())
			{
				System.out.println("oscOpen.isSelected()");
				holoEditRef.connection.open();
	//			mainRef.transport.activer();
			} else {
				holoEditRef.connection.close();
		//		mainRef.transport.desactiver();
			}
		}
		
		Ut.barMenu.update();
	}

	public void keyTyped(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}
}
