/*
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

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.text.DefaultFormatter;


class TimeDialog extends JDialog implements ActionListener, PropertyChangeListener {
	
    private int typedNumber[] = null; // that is for {H , MN, S, CS}
    private CustomTextField textFieldH, textFieldMN, textFieldS, textFieldMS;

    private JOptionPane optionPane;

    private String btnString1 = "OK";
    private String btnString2 = "Cancel";

    /** Returns the strings entered by the user. */
    public int[] getNumbers() {
        return typedNumber;
    }

    /** Creates the dialog. */
    public TimeDialog(Frame aFrame, String title) {
        super(aFrame, true);
        setTitle(title);
		
        textFieldH = new CustomTextField();
        textFieldMN = new CustomTextField();
        textFieldS = new CustomTextField();
        textFieldMS = new CustomTextField();

        JLabel labelH = new JLabel("h");
        JLabel labelMN = new JLabel("mn");
        JLabel labelS = new JLabel("s");
        JLabel labelCS = new JLabel("ms");
        
        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.X_AXIS));
        jpanel.add(labelH);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(4, 0)));
        jpanel.add(textFieldH);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        jpanel.add(labelMN);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(4, 0)));
        jpanel.add(textFieldMN);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        jpanel.add(labelS);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(4, 0)));
        jpanel.add(textFieldS);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));  jpanel.add(labelCS);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(4, 0)));
        jpanel.add(textFieldMS);
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        jpanel.add(javax.swing.Box.createRigidArea(new Dimension(10, 0)));
        
        //Create an array specifying the number of dialog buttons and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(jpanel,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
        // no icon
        optionPane.setIcon(new javax.swing.ImageIcon());
        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /* Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property. */
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
            	//Ensure the 'seconds' textfield finally gets the focus first.
            	textFieldS.requestFocusInWindow();
                textFieldS.setSelectionStart(0);
                
            }
        });

        //Register an event handler that puts the text into the option pane.
        textFieldH.addActionListener(this);
        textFieldMN.addActionListener(this);
        textFieldS.addActionListener(this);
        textFieldMS.addActionListener(this);

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        this.setSize(400, 100);
        this.setResizable(false);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
        	
            Object value = optionPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }
            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
            	typedNumber = new int[4];
            	typedNumber[0] = ((Number) textFieldH.getValue()).intValue();
            	typedNumber[1] = ((Number) textFieldMN.getValue()).intValue();
                typedNumber[2] = ((Number) textFieldS.getValue()).intValue();
                typedNumber[3] = ((Number) textFieldMS.getValue()).intValue();
                clearAndHide();
            } else { //user closed dialog or clicked cancel
            	typedNumber = null;
	   			clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
    	textFieldH.setValue(0);
    	textFieldMN.setValue(0);
    	textFieldS.setValue(0);
        textFieldMS.setValue(0);
        setVisible(false);
    }
    
    
    private class CustomTextField extends JFormattedTextField implements FocusListener {
    	
    	public CustomTextField() {
    		super(new NumberFormatter());
    		NumberFormat nf = NumberFormat.getNumberInstance();
    		nf.setParseIntegerOnly (true);
    		NumberFormatter numberFormatter = new NumberFormatter(nf);
    		this.setFormatter(numberFormatter);
    	    this.setValue(0);
    	    this.addFocusListener(this);
    		((DefaultFormatter) this.getFormatter ()).setAllowsInvalid (false);
    	}
    	
    	public void focusGained(FocusEvent e) {
    		javax.swing.SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
   	        		selectAll();
    			}
			}); 
    	}
	    public void focusLost(FocusEvent e) {}
    }
}

