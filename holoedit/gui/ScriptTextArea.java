package holoedit.gui;

import holoedit.util.Text;
import holoedit.util.Ut;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.io.File;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

public class ScriptTextArea extends JTextArea {
  //  JTextPane textPane;
    Document doc;
    static final int MAX_CHARACTERS = 300;
    String newline = "\n";
    HashMap<Object, Action> actions;

    protected UndoManager undo = new UndoManager();
    
    public ScriptTextArea() {

    	doc = this.getDocument();
    	// Change the tab size (to 4 instead of default=8)
   	    this.setTabSize(4);
    	    
    	this.setFont( new Font("Verdana", 0, 11) );

        //Start watching for undoable edits and caret changes.
    	// Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        
        // Create an undo action and add it to the text component
        this.getActionMap().put("Undo", new AbstractAction("Undo") {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canUndo()) {
                            undo.undo();
                        }
                    } catch (CannotUndoException e) {
                    }
                }
        });   
        
        // Bind the undo action to ctl-Z /pomme-Z
        if(Ut.MAC)
        	this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 4), "Undo");
		else this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 2), "Undo");
        
        // Create a redo action and add it to the text component
        this.getActionMap().put("Redo",
            new AbstractAction("Redo") {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canRedo()) {
                            undo.redo();
                        }
                    } catch (CannotRedoException e) {
                    }
                }
            });
        
        // Bind the redo action to ctl-shift-Z /pomme-shift-Z
        if(Ut.MAC)
        	this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 5), "Redo");
		else this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 3), "Redo");
    }
    
    public void initText(File file) {
     		setText(""); // clear
    		Text.appendFileToTextArea(file, this);
    }
}