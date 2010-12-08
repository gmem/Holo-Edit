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
package holoedit.functions;

import holoedit.data.HoloFctPreset;
import holoedit.data.HoloTraj;
import holoedit.fileio.HoloFctPstIO;
import holoedit.gui.GestionPistes;
import holoedit.gui.HoloMenuItem;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

/**
 * 
 * Structure champs algo :
 * 
 * TYPE_TRANS_ATOB
 * 
 * 		APPLYTO -> (ONE / VISIBLE / ALL)
 * 		INPUT TK A
 * 		OUTPUT TK B
 * 		...
 * 
 * TYPE_TRANS_ABTOC
 * 
 * 		INPUT TK A
 * 		INPUT TK B
 * 		OUTPUT TK C
 * 		...
 * 
 * TYPE_GEN
 * 
 * 		APPLYTO -> (ONE / VISIBLE / ALL)
 * 		IN/OUTPUT TK A
 * 		DURATION (S)
 * 		INTERVAL BETW. 2 PTS (S)
 * 		...
 * 
 * TYPE_SPEAKERS
 * 
 * 		SPEAKERS NUMBER
 * 		...
 */
//FEATURE ALGO NEW transformation matrice
//FEATURE ALGO NEW autoresample (resampling dépendant de la distance entre les points)
//FEATURE ALGO NEW création de droite
//FEATURE ALGO NEW remap coords (comme un cube qui tourne)
//FEATURE ALGO NEW blur
public abstract class Algorithm implements Runnable, ActionListener
{
	public static final int TYPE_TRANS_ATOB = 0;
	public static final int TYPE_TRANS_ABTOC = 1;
	public static final int TYPE_GEN = 2;
	public static final int TYPE_SPEAKERS = 3;
	public static final int TYPE_DATA = 4;
	public static final int TO_ONE = 0;
	public static final int TO_VISIBLE = 1;
	public static final int TO_ALL = 2;
	public static final String CAT_TRANS_SPAT = "Spatial Transformations";
	public static final String CAT_TRANS_TIME = "Temporal Transformations";
	public static final String CAT_GEN = "Generative Functions";
	public static final String CAT_GEN_SPEAKERS = "Speakers";
	public static final String CAT_DATA = "From Datas";
	public static final String CAT_OWN = "User Algorithms";
	public static final String CAT_WIP = "Work In Progress";
	public static final String CAT_MAC = "Macros";
	public static final String CAT_SCRIPT = "Script";
	private int type = TYPE_TRANS_ATOB;
	private String category = CAT_OWN;
	private String name;
	private String title;
	private String description = "";
	private Vector<Field> fields = new Vector<Field>(16, 1);
	private Vector<HoloFctPreset> presets = new Vector<HoloFctPreset>(16, 1);
	private int pad = 0;
	private int lineSize = 11;
	private ProgressBar prog;
	private Thread runner;
	private HoloMenuItem menu;
	private HoloFctPstIO pstIO;
	protected int dateBegin, dateEnd;
	protected Object results[];
	private Object[] current;
	protected boolean replace;
	protected GestionPistes gp;

	public Algorithm(GestionPistes gp, int type, String name, String title, String description)
	{
		this.gp = gp;
		this.type = type;
		this.name = name;
		this.title = title;
		this.description = description;
		menu = new HoloMenuItem(this.title);
		menu.addActionListener(this);

		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		calcNOL();
		System.out.println("Loading algorithm "+name+"...");
		readPst();
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int t)
	{
		type = t;
	}
	
	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String s)
	{
		name = s;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String s)
	{
		title = s;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String s)
	{
		description = s;
	}
	
	private void calcNOL()
	{
		pad = (int) Math.floor(description.length() / 70) * lineSize;
		int i = 0;
		String s = getDescription();
		int ind = s.indexOf('\n');
		while (ind != -1 && i < 20)
		{
			pad += lineSize;
			s = s.substring(ind + 1);
			ind = s.indexOf('\n');
		}
	}

	public int getNOL()
	{
		return pad;
	}

	public Vector<Field> getFields()
	{
		return fields;
	}
	
	public void setFields(Vector<Field> f)
	{
		fields = f;
	}
	
	public Vector<HoloFctPreset> getPresets()
	{
		return presets;
	}
	
	public void setPresets(Vector<HoloFctPreset> p)
	{
		presets = p;
	}
	
	public void addPreset(HoloFctPreset p)
	{
		if(p != null && !presets.contains(p))
			presets.add(p);
	}
	
	public void readPst()
	{
		pstIO = new HoloFctPstIO(this,new File(Ut.dir(Ut.APP_PATH)+getClass().getCanonicalName().replace('.', Ut.dirCar.charAt(0))+".pst"));
		pstIO.loadPreset();
	}
	
	public void writePst()
	{
		pstIO.savePresets();
	}
	
	public void initFinal()
	{
		if(current != null)
		{
			setVals(current);
			current = null;
		}
		if(getPresets().isEmpty())
			addPreset(new HoloFctPreset("default",getName()).setVals(getVals()));
	}
	
	/** recuperation du nombre de parametres de la fonction */
	public int getFieldsSize()
	{
		return fields.size();
	}

	/** recuperation des noms des champs */
	public String[] getFieldsName()
	{
		int n = fields.size();
		String[] result = new String[n];
		for (int i = 0; i < n; i++)
			result[i] = fields.elementAt(i).label;
		return result;
	}

	/** recuperation des valeurs de chaque champ */
	public Object[] getVals()
	{
		int n = fields.size();
		Object[] result = new Object[n];
		for (int i = 0; i < n; i++)
			result[i] = fields.elementAt(i).defVal;
		return result;
	}

	/** recuperation des types de chaque champ */
	public int[] getTypes()
	{
		int n = fields.size();
		int[] types = new int[n];
		for (int i = 0; i < n; i++)
			types[i] = fields.elementAt(i).type;
		return types;
	}

	/** recuperation des options des champs */
	public String[] getOptions()
	{
		int n = fields.size();
		String[] options = new String[n];
		for (int i = 0; i < n; i++)
			options[i] = fields.elementAt(i).option;
		return options;
	}

	/** recuperation des minimums */
	public double[] getMins()
	{
		int n = fields.size();
		double[] mins = new double[n];
		for (int i = 0; i < n; i++)
			mins[i] = fields.elementAt(i).resMin;
		return mins;
	}

	/** recuperation des maximums */
	public double[] getMaxs()
	{
		int n = fields.size();
		double[] result = new double[n];
		for (int i = 0; i < n; i++)
			result[i] = fields.elementAt(i).resMax;
		return result;
	}

	/** recuperation des modulos */
	public double[] getMods()
	{
		int n = fields.size();
		double[] result = new double[n];
		for (int i = 0; i < n; i++)
			result[i] = fields.elementAt(i).resMod;
		return result;
	}

	/** affectation des valeurs */
	public void setVals(Object[] result)
	{
		for (int i = 0; i < fields.size(); i++){
			fields.elementAt(i).defVal = result[i];
		}
	}
	
	public void setCurrent(Object[] c)
	{
		current = c;
	}
	
	public void addField(Field f)
	{
		fields.add(f);
	}
	
	public HoloMenuItem getMenu()
	{
		return menu;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		initFinal();
		System.out.println("Command : " + e.getSource());
		if (e.getSource().toString().equalsIgnoreCase("Script"))
			// if (gp.mainRef.score.getSeqSDIF().size()!=0)
			new GroovyWindow(this,gp);
		// on n'ouvre pas la fenetre de dialog algo si l'algo choisi est fromSDIF mais qu'il 'y a pas de sdif selectionné :
		else if (!e.getSource().toString().equalsIgnoreCase("FromSDIF") || gp.holoEditRef.score.getSeqSDIFinAllTrack().size()!=0)
			new AlgorithmDialog(this,gp);
	}
	
	public void run()
	{
		try
		{
			int applyTo,tkNthFrom,tkNthTo,tkNthFromA,tkNthFromB;
			double dur,durPoint;
			
			switch(type)
			{
			case TYPE_SPEAKERS:
				prog.setMaximum((Integer)results[0]);
				treatSpeakers();
				break;
			case TYPE_GEN:
			case TYPE_DATA:
				applyTo = (Integer)results[0];
				tkNthTo = (Integer)results[1]-1;
				dur = (int)((Double)results[2] * 1000);
				durPoint = (int)((Double)results[3] * 1000);
				int nbPtPerTk = (int) Math.round(dur / durPoint);
				switch(applyTo)
				{
				case TO_ONE:
					gp.StoreToUndo(tkNthTo);
					prog.setMaximum(nbPtPerTk);
					treatOneTrack(tkNthTo);
					gp.selectTrack(tkNthTo);
					break;
				case TO_VISIBLE:
				case TO_ALL:
					gp.StoreToUndoAll();
					switch ((Integer) results[0])
					{
					case 1:
						int nbVisi = 0;
						for (int i = 0; i < gp.getNbTracks(); i++)
							if (gp.getTrack(i).isVisible())
								nbVisi++;
						prog.setMaximum(nbPtPerTk * nbVisi);
						break;
					case 2:
						prog.setMaximum(nbPtPerTk * gp.getNbTracks());
						break;
					}
					for (int i = 0; i < gp.getNbTracks(); i++)
					{
						if (gp.getTrack(i).isVisible() || (Integer)results[0] == 2)
						{
							treatOneTrack(i);
							gp.selectTrack(i);
						}
					}
					boolean found = false; // recherche de la premiere piste visible
					int j = -1;
					while (!found && j < gp.getNbTracks())
					{
						j++;
						if (gp.getTrack(j).isVisible())
							found = true;
					}
					gp.selectTrack(j);
					break;
				}
				break;
			case TYPE_TRANS_ATOB:
				applyTo = (Integer)results[0];
				tkNthFrom = (Integer)results[1]-1;
				tkNthTo = (Integer)results[2]-1;
				switch(applyTo)
				{
				case TO_ONE:
					gp.StoreToUndo(tkNthTo);
					prog.setMaximum(gp.getTrack(tkNthFrom).sizeBetween(dateBegin,dateEnd));
					treatOneTrack(tkNthFrom, tkNthTo);
					gp.selectTrack(tkNthTo);
					break;
				case TO_VISIBLE:
				case TO_ALL:
					gp.StoreToUndoAll();
					gp.StoreToUndoAll();
					int nbP = 0;
					for (int i = 0; i < gp.getNbTracks(); i++)
						if ((gp.getTrack(i).isVisible() || (Integer)results[0] == 2) && !gp.getTrack(i).isEmpty())
							nbP += gp.getTrack(i).sizeBetween(dateBegin,dateEnd);
					prog.setMaximum(nbP);
					for (int i = 0; i < gp.getNbTracks(); i++)
						if ((gp.getTrack(i).isVisible() || (Integer)results[0] == 2) && !gp.getTrack(i).isEmpty())
						{
							treatOneTrack(i, i);
							gp.selectTrack(i);
						}
					boolean found = false; // recherche de la premiere piste visible
					int j = -1;
					while (!found && j < gp.getNbTracks())
					{
						j++;
						if (gp.getTrack(j).isVisible())
							found = true;
					}
					gp.selectTrack(j);
					break;
				}
				break;
			case TYPE_TRANS_ABTOC:
				gp.StoreToUndoAll();
				tkNthFromA = (Integer)results[0]-1;
				tkNthFromB = (Integer)results[1]-1;
				tkNthTo = (Integer)results[2]-1;
				prog.setMaximum(gp.getTrack(tkNthFromA).sizeBetween(dateBegin,dateEnd));
				treatOneTrack(tkNthFromA,tkNthFromB,tkNthTo);
				break;
		/*	case TYPE_DATA:
				
				String selectedSDIF = (String) results[2];
				String spl[] = selectedSDIF.split("\\s"+"\\p{Punct}"+"\\s");
				String filename = spl[0];
				String dataType = spl[1];
				HoloSDIFdata holoSDIFdata = gp.externalDataPool.get(filename, dataType);
				System.out.println("holoSDIFdata = "+holoSDIFdata.toString());*/
			}
			stop();
		} catch (Exception e)
		{
			e.printStackTrace();
			cancel();
		}
	}
	
	protected void start(Object[] res, boolean rep)
	{
		dateBegin = gp.holoEditRef.counterPanel.getDate(1);
		dateEnd = gp.holoEditRef.counterPanel.getDate(2);
		results = res;
		setVals(res);
		replace = rep;
		prog = new ProgressBar(title+"...");
		prog.setValue(0);
		prog.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if(runner != null)
				{
					runner.interrupt();
					cancel();
				}
			}
		});
		prog.open();
		// declenchement du thread
		runner = new Thread(this);
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.setName(name+"-Thread");
		runner.start();
	}
	
	protected void stop()
	{
		prog.dispose();
		gp.holoEditRef.room.display();
	}
			
	protected void cancel()
	{
		gp.Undo();
		stop();
	}
	
	protected void inc()
	{
		prog.inc();
	}
	
	protected void inc(int i)
	{
		prog.inc(i);
	}
	
	protected void finalizeTraj(int tkNthTo, HoloTraj ht, int duration)
	{
		ht.lastElement().setEditable(true);
		ht.firstElement().setEditable(true);
		int date;
		if (replace || gp.getTrack(tkNthTo).getLastDate() < dateBegin)
		{
			if (replace)
				gp.getTrack(tkNthTo).cut(dateBegin, ht.getDuration() + dateBegin, false, false);
			gp.getTrack(tkNthTo).addTraj(ht, dateBegin);
			gp.getTrack(tkNthTo).update();
			date = dateBegin + duration;
		} else {
			ht.shiftDates(gp.getTrack(tkNthTo).getLastDate() - dateBegin);
			gp.getTrack(tkNthTo).addTraj(ht, gp.getTrack(tkNthTo).getLastDate());
			gp.getTrack(tkNthTo).update();
			date = gp.getTrack(tkNthTo).getLastDate();
		}
		date = (date > dateEnd ? date : dateEnd);
		gp.update(tkNthTo, date);
	}
	
	protected void finalizeTrajScript(int tkNthTo, HoloTraj ht)
	{
		ht.lastElement().setEditable(true);
		ht.firstElement().setEditable(true);
		int lastdate = ht.getLastDate();
		if (replace || gp.getTrack(tkNthTo).getLastDate() < ht.getFirstDate())
		{
			if (replace)
				gp.getTrack(tkNthTo).cut(ht.getFirstDate(), ht.getLastDate(), false, false);
			gp.getTrack(tkNthTo).addTraj(ht, ht.getFirstDate());
			gp.getTrack(tkNthTo).update();
			
		} else {
			ht.shiftDates(gp.getTrack(tkNthTo).getLastDate() - ht.getFirstDate());
			gp.getTrack(tkNthTo).addTraj(ht, gp.getTrack(tkNthTo).getLastDate());
			gp.getTrack(tkNthTo).update();
			lastdate = gp.getTrack(tkNthTo).getLastDate();
		}
		lastdate = (lastdate > dateEnd ? lastdate : dateEnd);
		gp.update(tkNthTo,lastdate);
	}

	protected void treatSpeakers(){}
	protected abstract void treatOneTrack(int tkNth);
	protected abstract void treatOneTrack(int tkNthFrom,int tkNthTo);
	protected abstract void treatOneTrack(int tkNthFromA, int tkNthFromB, int tkNthTo);
	
	// ------------------ UTILS --------------------------------
	protected double calcAngle(double x, double y)
	{
		double angleResult;
		if (y == 0)
			angleResult = x > 0 ? Math.PI / 2 : Math.PI * 3 / 2;
		else
		{
			angleResult = Math.atan(x / -y);
			if (y > 0)
				angleResult += Math.PI;
		}
		return angleResult;
	}

	/**@return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) **/
	protected double calcDistance(double x, double y) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	// ------ ici dir = 0 est en haut ----------------/
	/**@return d*Math.sin(t) **/
	protected double polX(double t, double d) {
		return d * Math.sin(t);
	}
	/**@return d*-1*Math.cos(t) **/
	protected double polY(double t, double d) {
		return d * -1 * Math.cos(t);
	}

	// ------ ici dir = 0 est a droite ----------------/
	/**@return dist*Math.cos(dir) **/
	protected double polX2(double dir, double dist)	{
		return dist * Math.cos(dir);
	}
	/**@return dist*Math.sin(dir) **/
	protected double polY2(double dir, double dist)	{
		return dist * Math.sin(dir);
	}
}
