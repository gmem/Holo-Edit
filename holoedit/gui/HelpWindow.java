package holoedit.gui;

import holoedit.HoloEdit;
import holoedit.util.Ut;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.io.IOException;

import javax.swing.*;
 
/** La fenetre d'aide accessible par menu View -> Help Window*/
public class HelpWindow extends FloatingWindow {

	
	/*
	 * Available help index (see helpWindowDoc.html) :
	 * RoomEditor
	 * DrawingZone
	 * room
	 * tracks
	 * transport
	 * timeEditor
	 * score
	 * soundPool
	 * options
	 */
	private HoloEdit holoEditRef;
	private JEditorPane editorPane;
	private JComboBox comboBox;
	
	public HelpWindow(HoloEdit owner){
		super("Help", owner, owner.helpWindowW, owner.helpWindowH, owner.helpWindowX, owner.helpWindowY, false);
		setTitle("Help");
		holoEditRef = owner;		
		initComponent();
	    this.setResizable(true);
	    jumpToIndex("", false);
	}
	
	
	private void initComponent(){
		editorPane = new JEditorPane ();
		comboBox = new JComboBox();
		comboBox.addItem("Score");
		comboBox.addItem("Sound Pool");
		comboBox.addItem("Time Editor");
		comboBox.addItem("Room");
		//comboBox.addItem("3D-Room");
		comboBox.addItem("Tracks");
		comboBox.addItem("Transport");
		comboBox.addItem("Options");
		comboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				final String name = ((JComboBox) event.getSource()).getSelectedItem().toString();
				SwingUtilities.invokeLater(new Runnable() {
					 public void run() {
						 if (name.equals("")){
							 jumpToIndex("", false);
						 }else if (name.equals("Score")){
							 jumpToIndex("#score", false);
						 }else if (name.equals("Sound Pool")){
							 jumpToIndex("#soundPool", false);
						 }else if (name.equals("Time Editor")){
							 jumpToIndex("#timeEditor", false);
						 }else if (name.equals("Room")){
							 jumpToIndex("#room", false);
						 }else if(name.equals("3D-Room")){
						//	 jumpToIndex("");			 
						 }else if(name.equals("Tracks")){
							 jumpToIndex("#tracks", false);			 
						 }else if(name.equals("Transport")){
							 jumpToIndex("#transport", false);			 
						 }else if(name.equals("Options")){
							 jumpToIndex("#options", false);			 
						 }
					 }
				});
			}
		});
		JScrollPane scrollPane = new JScrollPane (editorPane);
		this.add(comboBox, BorderLayout.NORTH);
		this.add (scrollPane, BorderLayout.CENTER);
		editorPane.setEditable (false);
	}
	/** charge Documentations/helpWindowDoc.html#index dans cette fentre d'aide.
	 * */
	public void jumpToIndex(String index, boolean updateComboBox){
		if (Ut.MAC) {
			loadPage("file://"+Ut.dir(Ut.APP_PATH)+"Documentations/helpWindowDoc.html"+index);
		} else {
			loadPage("file:///"+Ut.dir(Ut.APP_PATH)+"Documentations\\helpWindowDoc.html"+index);			
		}
		
		if (updateComboBox)
			updateComboBox(index);
	}
	
	private void updateComboBox(String index){
		 if (index.equals("")){
			comboBox.setSelectedItem("");
		 }else if (index.equals("#score")){
			comboBox.setSelectedItem("Score");
		 }else if (index.equals("#soundPool")){
			 comboBox.setSelectedItem("Sound Pool");
		 }else if (index.equals("#timeEditor")){
			 comboBox.setSelectedItem("Time Editor");
		 }else if (index.equals("#room")){
			 comboBox.setSelectedItem("Room");
		 }else if(index.equals("#3D-Room")){
		//	 comboBox.setSelectedItem("3D-Room");			 
		 }else if(index.equals("#tracks")){
			 comboBox.setSelectedItem("Tracks");			 
		 }else if(index.equals("#transport")){
			 comboBox.setSelectedItem("Transport");			 
		 }else if(index.equals("#options")){
			 comboBox.setSelectedItem("Options");			 
		 }
	}
	
	private void loadPage (String urlString) {
	    try {
	    	URL url = new URL (urlString);
	    	editorPane.setPage(url);
	    } catch (IOException ex) {
	    	//ex.printStackTrace();
	    	System.err.println ("cannot open "+urlString);
	    }
	}

	public void windowClosed(WindowEvent e) {
		holoEditRef.helpWindowOpened = false;
	}
}
