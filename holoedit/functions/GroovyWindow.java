package holoedit.functions;

import holoedit.gui.FloatingWindow;
import holoedit.gui.GestionPistes;
import holoedit.gui.ScriptTextArea;
import holoedit.gui.ProgressBar;
import holoedit.util.Text;
import holoedit.util.Ut;
import holoedit.data.SDIFdataInstance;
import holoedit.fileio.HoloFilenameFilter;
import holoedit.fileio.HoloFilenameFilterXP;
import holoedit.functions.ComboParam;
import algo.GroovyConnector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Font;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.HashMap;
import java.util.Vector;
import java.io.File;
import java.awt.event.*;

public class GroovyWindow extends FloatingWindow implements KeyListener{

	private ScriptTextArea scriptTextArea = new ScriptTextArea();
	private boolean fine = false;
	protected GestionPistes gp;	
	private HashMap<Integer, KeyStroke> acceleratorHashMap = new HashMap<Integer, KeyStroke>();
	protected int results[] = {0, 0, 1, 1, 1};	//results[0] <=> type (gen/trans)
												//results[1] <=> applyTo (one/visible/all)
												//results[2] <=> track n° (used when type==gen)
												//results[3] <=> inputTrack n° (used when type==trans)
												//results[4] <=> outputTrack n° (used when type==trans)
	protected boolean replace = false;
	private JComboBox  genTransCombo;
	private ComboParam trackCombo;
	private ComboParam inputTrackCombo;
	private ComboParam outputTrackCombo;
	private ComboParam applyToCombo;
	private File scriptFile;
	public static String scriptDirectory = System.getProperty("user.dir")+"/scripts/";// filtre de fichier data/SDIF
	public static String scriptTemplatesDirectory = System.getProperty("user.dir")+"/scripts/templates/";// filtre de fichier data/SDIF
	public static String scriptUserDirectory = System.getProperty("user.dir")+"/scripts/user/";// filtre de fichier data/SDIF
	// filtre de fichiers algo script
	public HoloFilenameFilter scriptFilter = new HoloFilenameFilter(".algo", "algo script Files (*.algo)", true);
	private HoloFilenameFilterXP scriptFilterXP = new HoloFilenameFilterXP(".algo", "algo script Files (*.algo)", true);
	private boolean upToDate = true; // si modifiÈ depuis la sauvegarde
	
	private ProgressBar progressBar;
	
	public GroovyWindow(Algorithm a, GestionPistes _gp) {
		super("groovy algo", _gp.holoEditRef, 850, 700, 100, 100, true);
		setFine(false);
		gp = _gp;
		scriptFile = null;
		initComponents();
		disableTransportAccelerator();	 // disable keyAccelerators of the transport Menu
	}
	
	// init components
	private void initComponents() {
		setTitle("groovy algo");
		setLayout(new BorderLayout(10, 5));

		JTextArea defaultValueTextArea = new JTextArea();
		defaultValueTextArea.setEditable(false);
		defaultValueTextArea.setFont( new Font("Verdana", 0, 11) );
		defaultValueTextArea.setVisible(true);
		defaultValueTextArea.setTabSize(2);
		defaultValueTextArea.setLineWrap(false);
		appendAvailableSDIFInTextArea(defaultValueTextArea);	// ecriture du contenu du textArea
		appendDefaultValueInTextArea(defaultValueTextArea);	// ecriture du contenu du textArea
		JScrollPane defValScrollPane = new JScrollPane(defaultValueTextArea);
		defValScrollPane.setPreferredSize(new Dimension(250, 155));
		defValScrollPane.setMinimumSize(new Dimension(10, 10));
		defValScrollPane.setBorder(BorderFactory.createTitledBorder("Values from score	- you may copy/paste them in your script"));		
		scriptTextArea.addKeyListener(this);
		if(gp.holoEditRef.lastScriptFile.exists()){
			try{
				scriptTextArea.initText(gp.holoEditRef.lastScriptFile);
				setTitle(gp.holoEditRef.lastScriptFile.getName());
				upToDate = true;
			}catch(Exception e){
				System.err.println("Could not load last script file");
			}
		} else{
			try{
				scriptTextArea.initText(new File(scriptTemplatesDirectory+"generation.algo"));
				setTitle("defaultTemplate.algo");
				upToDate = true;
			}catch(Exception e){
				scriptTextArea.setText("");	// clear
			}
		}
		
		JScrollPane scriptScrollPane = new JScrollPane(scriptTextArea);
		scriptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scriptScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scriptScrollPane.setPreferredSize(new Dimension(250, 155));
		scriptScrollPane.setMinimumSize(new Dimension(10, 10));
		scriptScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5,5,5,5),
				BorderFactory.createTitledBorder("SCRIPT")));		
		
		JSplitPane panScrollPanes = new JSplitPane(JSplitPane.VERTICAL_SPLIT , defValScrollPane, scriptScrollPane);
		panScrollPanes.setDividerLocation(0.2);
		
		// BUTTONS
		JButton jbuttonReplace = new JButton("REPLACE");
		jbuttonReplace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				chooReplace();								
			}
		});
		JButton jbuttonContinue = new JButton("CONTINUE");
		jbuttonContinue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptTextArea.setEditable(false);
				progressBar = new ProgressBar("running...");
				progressBar.setIndeterminate(true);
				progressBar.open();
				replace = false;
				boolean everythingOK = startScript(scriptTextArea);
				if (everythingOK){
			 		enableTransportAccelerator(acceleratorHashMap); // enable keyAccelerators of the transport Menu
					setFine(true);
					dispose();
				} else{
					scriptTextArea.setEditable(true);
				}
				progressBar.dispose();
			}
		});
		
		JButton jbuttonCancel = new JButton("CANCEL");
		jbuttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTransportAccelerator(acceleratorHashMap); // enable keyAccelerators of the transport Menu
				setFine(true);
				dispose();
			}
		});
		JPanel jpanProcessButtons = new JPanel();
		jpanProcessButtons.setLayout(new BoxLayout(jpanProcessButtons, BoxLayout.X_AXIS));
		jpanProcessButtons.add(javax.swing.Box.createRigidArea(new Dimension(120, 0)));
		jpanProcessButtons.add(jbuttonCancel);
		jpanProcessButtons.add(javax.swing.Box.createRigidArea(new Dimension(15, 0)));
		jpanProcessButtons.add(jbuttonReplace);
		jpanProcessButtons.add(javax.swing.Box.createRigidArea(new Dimension(15, 0)));
		jpanProcessButtons.add(jbuttonContinue);

		JButton jbuttonSave = new JButton("Save");
		jbuttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (upToDate==false &&(scriptFile!=null || getScriptFileOutName())){
					scriptTextArea.setEditable(false);
					Text.textToFile(scriptFile, scriptTextArea.getText());
					setTitle(scriptFile.getName());
					upToDate = true;
					gp.holoEditRef.modify();
					scriptTextArea.setEditable(true);
				}
			}
		});
		
		JButton jbuttonOpen = new JButton("Open...");
		jbuttonOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getScriptFileInName()){
					scriptTextArea.setEditable(false);
					scriptTextArea.initText(scriptFile);
					setTitle(scriptFile.getName());
					upToDate = true;
					gp.holoEditRef.modify();
				}
				scriptTextArea.setEditable(true);
			}
		});
		
		JButton jbuttonSaveAs = new JButton("Save as...");
		jbuttonSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getScriptFileOutName()){
					if(!scriptFile.getName().substring(scriptFile.getName().lastIndexOf('.') + 1).equalsIgnoreCase("algo"))
						scriptFile = new File(scriptFile.toString()+".algo");
					scriptTextArea.setEditable(false);
					Text.textToFile(scriptFile, scriptTextArea.getText());
					setTitle(scriptFile.getName());
					upToDate = true;
					gp.holoEditRef.modify();
				}
				scriptTextArea.setEditable(true);
			}
		});
		
		JPanel jpanOpenSave = new JPanel();
		jpanOpenSave = new JPanel();
		jpanOpenSave.setLayout(new BoxLayout(jpanOpenSave, BoxLayout.X_AXIS));
		jpanOpenSave.add(javax.swing.Box.createRigidArea(new Dimension(120, 0)));
		jpanOpenSave.add(jbuttonSave);
		jpanOpenSave.add(javax.swing.Box.createRigidArea(new Dimension(15, 0)));
		jpanOpenSave.add(jbuttonOpen);
		jpanOpenSave.add(javax.swing.Box.createRigidArea(new Dimension(15, 0)));
		jpanOpenSave.add(jbuttonSaveAs);
		
		JPanel jpanButton = new JPanel();
		jpanButton.setLayout(new BorderLayout());		
		jpanButton.add(jpanProcessButtons, BorderLayout.LINE_START);
		jpanButton.add(jpanOpenSave, BorderLayout.LINE_END);
		jpanButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	
		//////*******************************************
		
		JLabel trackLabel = new JLabel("Track n" + Ut.numCar);
		trackLabel.setAlignmentX(0.2f);
		trackCombo = new ComboParam(gp, 0, "track", 0);
		trackCombo.setAlignmentX(0.2f);
		trackCombo.setEnabled(true);
		trackCombo.setSelectedIndex(gp.getActiveTrackNb()+1); // +1 cause first item is : "n°"
		results[2] = gp.getActiveTrackNb()+1;	
		trackCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				ComboParam c = (ComboParam) e.getSource();
				int index = c.getSelectedIndex();
				if (index>=1)
					results[2] = index;			
			}
		});
		
		JLabel applyToLabel = new JLabel("Apply to :");
		applyToLabel.setAlignmentX(0.2f);
		applyToCombo = new ComboParam(gp, 0, "applyTo", 0);
		applyToCombo.setAlignmentX(0.2f);
		applyToCombo.setEnabled(true);
		applyToCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				// si on choisit le combo apply to sur all ou visible alors les champ de
				// selection des numeros de pistes sont grises
				ComboParam c = (ComboParam) e.getSource();
				int index = c.getSelectedIndex();
				results[1] = index;
				switch (index)
				{
				case 1: // Apply to visible
				case 2:	// Apply to all
					trackCombo.setEnabled(false);
					inputTrackCombo.setEnabled(false);
					outputTrackCombo.setEnabled(false);
					break;
				default: // Apply to one
					// trackCombo enabled si generation
					trackCombo.setEnabled(genTransCombo.getSelectedIndex()==0);
					// inputTrack et outputTrack enabled si transformation
					inputTrackCombo.setEnabled(genTransCombo.getSelectedIndex()==1);
					outputTrackCombo.setEnabled(genTransCombo.getSelectedIndex()==1);
					break;
				}
			}
		});

		JLabel inputTrackLabel = new JLabel("Input Track n" + Ut.numCar);
		inputTrackLabel.setAlignmentX(0.2f);
		inputTrackCombo = new ComboParam(gp, 0, "track", 0);
		inputTrackCombo.setAlignmentX(0.2f);
		inputTrackCombo.setEnabled(false);
		inputTrackCombo.setSelectedIndex(gp.getActiveTrackNb()+1);
		results[3] = gp.getActiveTrackNb()+1;
		inputTrackCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				ComboParam c = (ComboParam) e.getSource();
				int index = c.getSelectedIndex();
				results[3] = index;
			}
		});

		JLabel outputTrackLabel = new JLabel("Output Track  n" + Ut.numCar);
		outputTrackLabel.setAlignmentX(0.2f);
		outputTrackCombo = new ComboParam(gp, 0, "track", 0);
		outputTrackCombo.setAlignmentX(0.2f);
		outputTrackCombo.setEnabled(false);
		outputTrackCombo.setSelectedIndex(gp.getActiveTrackNb()+1);
		results[4] = gp.getActiveTrackNb()+1;
		outputTrackCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				ComboParam c = (ComboParam) e.getSource();
				int index = c.getSelectedIndex();
				results[4] = index;
			}
		});
		
		// selection Generative / trasformation
		JLabel genTransLabel = new JLabel("Algorithm Type :");
		genTransLabel.setAlignmentX(0.2f);
		genTransCombo = new JComboBox();
		genTransCombo.addItem("Generation");
		genTransCombo.addItem("Transformation");
		genTransCombo.setSelectedIndex(0);
		results[0] = 0;
		genTransCombo.setAlignmentX(0.2f);
		genTransCombo.setEnabled(true);
		genTransCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				JComboBox c = (JComboBox) e.getSource();
				int index = c.getSelectedIndex();
				results[0] = index;
				if (index==0){ // generation
					// trackCombo enabled si applyTo==one
					trackCombo.setEnabled(applyToCombo.getSelectedIndex()==0);
					inputTrackCombo.setEnabled(false);
					outputTrackCombo.setEnabled(false);					
				}else if (index==1){ // transformation
					trackCombo.setEnabled(false);
					// inputTrack et outputTrack enabled si applyTo==one
					inputTrackCombo.setEnabled(applyToCombo.getSelectedIndex()==0);
					outputTrackCombo.setEnabled(applyToCombo.getSelectedIndex()==0);
				}
			}
		});
		
		
		JButton jbuttonOpenHelp = new JButton("See available functions");
		jbuttonOpenHelp.setAlignmentX(0.2f);
		jbuttonOpenHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					openHelp();
			}
		});
		
		JPanel panTracks = new JPanel();
		panTracks.setVisible(true);
		panTracks.setFocusable(false);
		panTracks.setSize((new java.awt.Dimension(300, 500)));
		panTracks.setLayout(new BoxLayout(panTracks, BoxLayout.PAGE_AXIS));
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 20)));
		panTracks.add(genTransLabel);
		panTracks.add(genTransCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 20)));
		panTracks.add(applyToLabel);
		panTracks.add(applyToCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 30)));
		panTracks.add(trackLabel);
		panTracks.add(trackCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 30)));
		panTracks.add(inputTrackLabel);
		panTracks.add(inputTrackCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
		panTracks.add(outputTrackLabel);
		panTracks.add(outputTrackCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
		panTracks.add(outputTrackLabel);
		panTracks.add(outputTrackCombo);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
		panTracks.add(jbuttonOpenHelp);
		panTracks.add(javax.swing.Box.createRigidArea(new Dimension(0, 400)));

		panTracks.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder(""),
							BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
		
	    add(panTracks, BorderLayout.LINE_START);
	    add(panScrollPanes, BorderLayout.CENTER);
		add(jpanButton, BorderLayout.SOUTH);
	        
		repaint();
		setResizable(true);
		setVisible(true);
		toFront();
	}
	
	/**
	 * Ecrit le contenu du textArea.
	 * on ecrit qq infos en commentaire, qq valeurs par defaut,
	 * et le contenu de algo/defaultTemplate.algo
	 */
	private void appendAvailableSDIFInTextArea(JTextArea textArea){
		// ecriture des SDIFinstances dispos dans le textArea :
		if (gp.holoEditRef.score.getSeqSDIFinAllTrack().isEmpty())
			return;
		textArea.append("\nAVAILABLE SDIFdataInstances :"); 	// s'il y a des sdif dans le score on les liste
		for (int trk=0; trk<gp.tracks.size(); trk++){
			Vector<SDIFdataInstance> vectorSDIF = gp.holoEditRef.score.getSeqSDIFinTrack(trk);
			for (SDIFdataInstance dataInstance : vectorSDIF){
				// Writing some info about available SDIFdata in the textArea
				textArea.append("\n\""+dataInstance.toString()+" - Track:"+trk+"\"");
				textArea.append("\n\t with fields : ");
				for (int i=0; i<dataInstance.getData().getFields().length; i++)
					textArea.append("["+i+"="+dataInstance.getData().getFields()[i]+"] ; ");
			}					
		}
	}
	private void appendDefaultValueInTextArea(JTextArea textArea){
		textArea.append("\n");
		if (!gp.holoEditRef.score.getSeqSDIFinAllTrack().isEmpty()) {
			SDIFdataInstance defaultInstance = null;
			int defaultTrackNumber=0;
			for (int trk=0; trk<gp.tracks.size(); trk++){
				Vector<SDIFdataInstance> vectorSDIF = gp.holoEditRef.score.getSeqSDIFinTrack(trk);
				for (SDIFdataInstance dataInstance : vectorSDIF){
					if (trk==gp.getActiveTrackNb() && defaultInstance==null){ // si le track selectionné comporte une sdif instance on la prend par defaut
						defaultInstance = dataInstance;
						defaultTrackNumber = gp.getActiveTrackNb();
					}
				}
			}
			if (defaultInstance==null){ // si toujours pas de SDIF par defaut, on prend la 1ere qu'on, trouve
				while (gp.holoEditRef.score.getSeqSDIFinTrack(defaultTrackNumber).size()<=0)
					defaultTrackNumber++;
				defaultInstance = gp.holoEditRef.score.getSeqSDIFinTrack(defaultTrackNumber).get(0);
			}
			String desc = defaultInstance.toString();
			textArea.append("\nmySDIFdata = getSDIFdata(\""+desc+" - Track:"+defaultTrackNumber+"\")");
		}
		// ecriture des initialisations de 'dateBegin' et 'dur' par defaut
		textArea.append("\nint dateBegin = "+gp.holoEditRef.counterPanel.getDate(1)+"; // = the begining of the score selection");
		textArea.append("\nint dateEnd = "+gp.holoEditRef.counterPanel.getDate(2)+"; // = the end date of the score selection");
		textArea.append("\ndouble dur = "+(gp.holoEditRef.counterPanel.getDate(2)-gp.holoEditRef.counterPanel.getDate(1))+"; // = the length of the score selection");
	}
	/**
	 * Starts the Groovy script
	 * @param textArea
	 * @return
	 */
    private boolean startScript(JTextArea textArea) {
        GroovyConnector groovyConnector = new GroovyConnector(gp);   // instanciate the connector ...
        groovyConnector.setReplace(replace);
        if (scriptFile==null || upToDate==false){
        	// the script hasn't been saved or has been modified since the last save, so we create a file called algoGroovyFile
        	scriptFile = new File(scriptUserDirectory+"algoGroovyFile.algo");
        	Text.textToFile(scriptFile, textArea.getText()); // save the textArea script in a file
        }
        gp.holoEditRef.lastScriptFile = scriptFile;
		return groovyConnector.startGroovyScript(scriptFile, results);  // ... and run the script
    }
    
    private boolean getScriptFileInName()
	{
		File fileIn = GestionPistes.chooseFile("Open a script...",scriptDirectory,scriptFilter,scriptFilterXP,false);
		if (fileIn != null)	{
			scriptUserDirectory = Ut.dir(fileIn.getParent());
			scriptFile = fileIn;
			return true;
		}
		return false;
	}
    
    private boolean getScriptFileOutName()
	{
		File fileOut = GestionPistes.chooseFile("Save script as...",scriptUserDirectory,scriptFilter,scriptFilterXP,true);
		if (fileOut != null)
		{
			scriptUserDirectory = Ut.dir(fileOut.getParent());
			scriptFile = fileOut;
			return true;
		}
		return false;
	}
    
	/**
	 * Deletes the accelerator keys of the "Transport" JMenu.
	 * this is done because those keys might be used in the JTextArea of this groovyWindow.
	 * @return An hashMap whose values are the previous accelerators keys, and whose keys are
	 * the indexes of the corresponding JMenuItem of the "Transport" JMenu. 
	 */
	private HashMap<Integer, KeyStroke> disableTransportAccelerator(){
		acceleratorHashMap.clear();
		for (int i=0; i<Ut.barMenu.getMenuCount(); i++){
			if (Ut.barMenu.getMenu(i).getText().equalsIgnoreCase("Transport")){
				for (int j=0; j<Ut.barMenu.getMenu(i).getMenuComponentCount(); j++){
					try {
						KeyStroke key = ((JMenuItem)Ut.barMenu.getMenu(i).getMenuComponent(j)).getAccelerator();
						if (key!=null){
							acceleratorHashMap.put(j, key);
							((JMenuItem)Ut.barMenu.getMenu(i).getMenuComponent(j)).setAccelerator(null);
						}
					}catch (java.lang.ClassCastException cce){
						// Not a JMenuItem (i.e the Ut.barMenu.getMenu(i).getMenuComponent(j)) might be a separator)
					}
				}
				break;
			}
		}
		return acceleratorHashMap;
	}
	/**
	 * Set accelerators for the JMenuItems of the "Transport" JMenu.
	 * @param acceleratorHashMap  An hashMap whose values are the accelerators keys to set,
	 * and whose keys are the indexes of the corresponding JMenuItem of the "transport" JMenu. 
	 */
	private void enableTransportAccelerator(HashMap<Integer, KeyStroke> acceleratorHashMap){
		
		for (int i=0; i<Ut.barMenu.getMenuCount(); i++)
			if (Ut.barMenu.getMenu(i).getText().equalsIgnoreCase("Transport")){
				for (Integer index : acceleratorHashMap.keySet())
					((JMenuItem)Ut.barMenu.getMenu(i).getMenuComponent(index)).setAccelerator(acceleratorHashMap.get(index));
				break;
			}
	}
	
	private void setFine(boolean b) {
		fine = b;
	}

	public boolean isFine() {
		return fine;
	}
	
	private void chooReplace(){
		if (JOptionPane.showConfirmDialog(this, "Do you really want to replace ? ", "alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == 0)
		{
			scriptTextArea.setEditable(false);
			progressBar = new ProgressBar("running...");
			progressBar.setIndeterminate(true);
			progressBar.open();
			replace = true;
            boolean everythingOK = startScript(scriptTextArea);
            if (everythingOK){
		 		enableTransportAccelerator(acceleratorHashMap); // enable keyAccelerators of the transport Menu
				setFine(true);
				dispose();
			} else
				scriptTextArea.setEditable(true);
            progressBar.dispose();
		}
	}
	
	private void openHelp()
	{
		Runtime r = Runtime.getRuntime();
		try
		{
			if (Ut.MAC) {
				r.exec("open ./Documentations/ScriptEditorFunctions.html");
				r.exec("open ./Documentations/HoloPoint.html");
			}else if (Ut.LINUX) {
				r.exec("firefox ./Documentations/ScriptEditorFunctions.html");
				r.exec("firefox ./Documentations/HoloPoint.html");
			}else {
				 r.exec("cmd /c start .\\Documentations\\ScriptEditorFunctions.html");
				 r.exec("cmd /c start .\\Documentations\\HoloPoint.html");				
			}
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}
	
	public void close()
	{
		enableTransportAccelerator(acceleratorHashMap);
		visible = false;
		setVisible(visible);
	}
	
	public void keyReleased(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){}
	public void keyTyped(KeyEvent e){
		if (upToDate==true){
			setTitle(this.getTitle()+" *");
			upToDate = false;
		}
	}	
}
