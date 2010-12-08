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
import holoedit.rt.*;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.GridBagConstraints;

import javax.swing.event.EventListenerList;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import javax.swing.text.DefaultFormatter;
import java.text.NumberFormat;
import java.util.EventObject;
import javax.swing.JFormattedTextField.*;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;


/**
 * A panel containing some info about the osc connection status
 * to be displayed in the transportPanel.
 */
public class ConnectionPanel extends JPanel {

	// reference sur main
	private HoloEdit holoEditRef;
	private Font verdana10 = new Font("Verdana",0, 10);
	private Font verdana14 = new Font("Verdana",0, 14);
	

	private JButton updateButton;
	private JComboBox connectChoice;
	public JFormattedTextField IPtextField, portTextField;
	public JButton connectButton;

	private final EventListenerList ConnectionlistenerList;
	
	public ConnectionPanel(HoloEdit owner) {
		//super(new GridLayout(0, 2));
		super();
		holoEditRef = owner;
		initComponent();
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3,3,3,3),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));	
		ConnectionlistenerList = new EventListenerList();
		this.addConnectionListener(holoEditRef.connection);
	}
	
	private void initComponent() {

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		JPanel updateButtonPan = new JPanel();
		updateButtonPan.setLayout(new GridBagLayout());
		updateButton = new JButton("update services");
		updateButton.setMargin(new Insets(1, 10, 1, 10));
		updateButton.setForeground(Color.darkGray);
		updateButton.setFont(new Font("courrier", Font.PLAIN, 10));
		updateButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				updateConnection();
			}
		});
		updateButtonPan.add(updateButton, c);
		
		JPanel settingsPan = new JPanel();
		settingsPan.setLayout(new GridBagLayout());
		JLabel connectTo = new JLabel("Services : ");
		connectTo.setFont(verdana10);
		connectChoice = new JComboBox();
		connectChoice.setLightWeightPopupEnabled(false);
		connectChoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				try {
					String serviceName = ((JComboBox) event.getSource()).getSelectedItem().toString();
					fireConnectToService(event, serviceName);
				} catch (NullPointerException npe) {
					fireDisconnect(event);
				}
			}
		});

		c.gridx = 2;
		c.gridy = 0;
		settingsPan.add(connectTo, c);

		c.gridx = 10;
		c.gridy = 0;
		settingsPan.add(connectChoice, c);
		JLabel connectIp = new JLabel("Out IP : ");
		connectIp.setFont(verdana10);
		IPtextField = new JFormattedTextField();
		IPtextField.setFont(verdana10);
		IPtextField.setPreferredSize(new java.awt.Dimension(100, 20));
		IPtextField.setSize(new java.awt.Dimension(100, 20));
		IPtextField.setEnabled(true);

		c.gridx = 2;
		c.gridy = 40;
		settingsPan.add(connectIp, c);

		c.gridx = 10;
		c.gridy = 40;
		settingsPan.add(IPtextField, c);
		
		JLabel portLabel = new JLabel("Out port : ");
		portLabel.setFont(verdana10);
		portTextField = new JFormattedTextField();
		portTextField.setFont(verdana10);
		portTextField.setPreferredSize(new java.awt.Dimension(70, 20));
		portTextField.setSize(new java.awt.Dimension(70, 20));
		portTextField.setEnabled(true);

		c.gridx = 2;
		c.gridy = 80;
		settingsPan.add(portLabel, c);

		c.gridx = 10;
		c.gridy = 80;
		settingsPan.add(portTextField, c);

		JPanel connectButtonPan = new JPanel();
		connectButtonPan.setLayout(new GridBagLayout());
		connectButton = new JButton("connect");
		connectButton.setMargin(new Insets(1, 10, 1, 10));
		connectButton.setForeground(Color.darkGray);
		connectButton.setFont(new Font("courrier", Font.PLAIN, 10));
		connectButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				final JButton button = (JButton) event.getSource();
				if (button.getText().equals("connect")){
					if (IPtextField.getText().equals("") || portTextField.getText().equals("")){
						fireDisconnect(event);
						Ut.alert("Connection Error", "Incorrect output IP/port");
						return;
					}
					fireOscAddressChanged(event, IPtextField.getText());
					fireOscPortOutChanged(event, Integer.parseInt(portTextField.getText()));
					updateConnection();				
					button.setText("disconnect");
				}else {
					closeConnection();
					button.setText("connect");				
				}
				Ut.barMenu.update();	
			}
		});
		c.gridx = 0;
		c.gridy = 0;
		connectButtonPan.add(connectButton, c);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		updateButtonPan.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		updateButtonPan.setMaximumSize(new java.awt.Dimension(150, 20));
		add(updateButtonPan);
		settingsPan.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		add(settingsPan);
		connectButtonPan.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
		connectButtonPan.setMaximumSize(new java.awt.Dimension(150, 20));
		add(connectButtonPan);
	}
	
	/**
	 * 
	 * @param serviceName
	 */
	public void removeConnectChoiceItem(String serviceName){
		connectChoice.removeItem(serviceName);
	}
	
	public void addConnectChoiceItem(String serviceName){
	 	connectChoice.addItem(serviceName);
	}
	
	public void addConnectionListener(ConnectionListener listener) {
		listenerList.add(ConnectionListener.class, listener);
	}
	
    public void removeConnectionListener(ConnectionListener listener) {
    	listenerList.remove(ConnectionListener.class, listener);
    }   

    public ConnectionListener[] getConnectionListeners() {
        return listenerList.getListeners(ConnectionListener.class);
    }

    protected void fireOscAddressChanged(EventObject event, String newAdress) {
    	for(ConnectionListener listener : getConnectionListeners()) {
    		try {
	    		listener.oscAddressChanged(event, newAdress);
	 		}catch (java.lang.IllegalArgumentException ex){
				 JOptionPane.showMessageDialog(null, ex.getMessage(), "error", JOptionPane.WARNING_MESSAGE);
			}
    	}
     }
     protected void fireOscPortOutChanged(EventObject event, int newOscPortOut) {
     	for(ConnectionListener listener : getConnectionListeners()) {
     		try {
     			listener.oscPortOutChanged(event, newOscPortOut);
     		}catch (java.lang.IllegalArgumentException ex){
     			 JOptionPane.showMessageDialog(null, ex.getMessage(), "error", JOptionPane.WARNING_MESSAGE);
     		}
     	}
      }
    
     protected void fireConnectToService(EventObject event, String service) {
      	for(ConnectionListener listener : getConnectionListeners()) {
      		try {
      			listener.connectToService(event, service);
      		}catch (java.lang.IllegalArgumentException ex){
      			 JOptionPane.showMessageDialog(null, ex.getMessage(), "error", JOptionPane.WARNING_MESSAGE);
      		}
      	}
     }
    
	protected void fireConnect(EventObject event) {
		holoEditRef.connection.open();
	}
     
	protected void fireDisconnect(EventObject event) {
		holoEditRef.connection.close();
	}
	
	private void updateConnection(){
		holoEditRef.connection.close();
		holoEditRef.connection.open();
	}
	
	private void openConnection(){
		holoEditRef.connection.open();
	}
	
	private void closeConnection(){
		holoEditRef.connection.close();
	}
}
