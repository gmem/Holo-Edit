/*
 *  -----------------------------------------------------------------------------
 *  
 *  Holo-Edit, spatial sound trajectories editor, part of Holophon
z *  Copyright (C) 2006 GMEM
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
package holoedit.fileio;

import holoedit.data.HoloPoint;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.HoloTrajVector;
import holoedit.data.WaveFormInstance;
import holoedit.data.WaveFormInstanceVector;
import holoedit.data.SDIFdataInstance;
import holoedit.data.SDIFdataInstanceVector;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.lang.Math;
import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.cycling74.max.Atom;

public class HoloFileReader extends DefaultHandler implements Runnable {
	
	private String filename;
	private File fileToRead;
	private double fileversion = 0;
	private int max = 0;
	private GestionPistes gp;
	private Thread runner;
	private ProgressBar pb;
	private Vector<HoloTrack> tracks;
	private HoloTrack currentTrack;
	private HoloTraj currentTraj;
	private HoloTrajVector currentVTraj;
	private WaveFormInstanceVector currentVWave;
	private SDIFdataInstanceVector currentVSDIF;
	private Vector<HoloSpeaker> speakers;
	private boolean done;
	private boolean error = false;
	private int datemax = 0;
	private String dfn;
	private String importOptions;
	private boolean cstimeunit;
	private int timemult;
	
	private int nbWaveInstances = 0;
	
	public HoloFileReader(GestionPistes _gp, String _f)
	{
		gp = _gp;
		clearDoneAndFineData();
		if (currentVWave!= null)
			nbWaveInstances = currentVWave.size();
		filename = _f;
		fileToRead = new File(_f);
		if(!fileToRead.exists())
		{
			Ut.alert("Error","File not found");
			return;
		}
		if(!fileToRead.canRead())
		{
			Ut.alert("Permission Error","You don't have permissions to read this file.");
			return;
		}
		pb = new ProgressBar("Loading...");
		pb.open();
		done = false;
		error = false;
		runner = new Thread(this);
		runner.setName("holo-reader");
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();

		
		try
		{
			while (!done)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{ pb.repaint(); }
				});
//				System.out.println("holo-reader-waiting");
				Thread.sleep(100);
			}	
		}
		catch (InterruptedException e) {}
		if(error)
		{
			Ut.alert("Error!","Error while loading the file, aborted.");
			gp.initTracks(true);
		} else {
			pb.setTitle("Rendering...");
			if(gp.holoEditRef != null)
				gp.updateTrackSelector(-3);
			datemax = 0;
			for (HoloTrack h : gp.tracks)
			{
				if(h.waves.isEmpty())
					datemax = Math.max(datemax, h.getLastDate());
				else
					datemax = Math.max(datemax, Math.max(h.getLastDate(),h.waves.lastElement().getLastDate()));
				if(!h.sdifdataInstanceVector.isEmpty())
					datemax = Math.max(datemax, h.sdifdataInstanceVector.lastElement().getLastDate());
			}
			datemax = Math.max(datemax,1000);
			if(gp.holoEditRef != null)
			{
				gp.holoEditRef.score.setMaxTime(datemax);
				gp.holoEditRef.counterPanel.setCompteur(1, 0);
				gp.holoEditRef.counterPanel.setCompteur(2, datemax);
				gp.holoEditRef.counterPanel.setCompteur(3, datemax);
			} else {
				gp.hpRef.setBegin(0);
				gp.hpRef.setEnd(datemax);
				gp.hpRef.setTotal(datemax);
				gp.hpRef.setSessionName(filename);
				gp.hpRef.update();
				
			}
		}
		if(gp.holoEditRef != null)
			gp.holoEditRef.room.display();
		else
			gp.hpRef.done();
		pb.dispose();
	}

	public void run()
	{
		
		try
		{
			SAXParserFactory.newInstance().newSAXParser().parse(fileToRead, this);
		} catch (Throwable t) {
			error = true;
			t.printStackTrace();
			pb.dispose();
			done = true;
		}
		
		
	}

	public void startDocument()
	{
		Ut.print("___________________________________________");
		Ut.print(" o Reading file : " + filename + " in progress...");
	}

	public void endDocument()
	{
		Ut.print(" v File was successfully read");
		Ut.print("___________________________________________");
		done = true;
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs)
	{
		// <session>
		if (qName.equalsIgnoreCase("session"))
		{
			String v = "v";
			fileversion = new Double(attrs.getValue(attrs.getIndex("version")));
			max = Integer.parseInt(attrs.getValue(attrs.getIndex("info")));
			pb.setMaximum(max);
				if (fileversion > Ut.version)
				{
					Ut.print(" x WARNING : This file was created with a more recent version of Holo-Edit, " + "it's possible that it won't be compatible with the current version.");
					v = "+";
				} else if (fileversion < Ut.version)
				{
					Ut.print(" x WARNING : This file was created with an older version of Holo-Edit, " + "if you modify it, it's possible that it won't be compatible with older versions of Holo-Edit.");
					v = "-";
				}
			Ut.print(" " + v + " Session - version " + fileversion);
			Ut.print("...");
			
			if(fileversion < 4.3)
			{
				cstimeunit = true;
				timemult = 10;
			}
			else
			{
				cstimeunit = false;
				timemult = 1;
			}
			
			
			tracks = new Vector<HoloTrack>();
			speakers = new Vector<HoloSpeaker>();
			//gp.initTracks(true); // FIXME : necessaire ?
			//if(gp.mainRef != null)
				//gp.mainRef.soundPoolGui.clear();
			//Ut.print("...1");
			// <track>
		} else if (qName.equalsIgnoreCase("track"))
		{
			currentTrack = new HoloTrack(new Integer(attrs.getValue("number")), attrs.getValue("name"), new Boolean(attrs.getValue("visible")), new Boolean(attrs.getValue("locked")), new Integer(attrs.getValue("color")));
			currentVTraj = new HoloTrajVector(5,1);
			currentVWave = new WaveFormInstanceVector(5,1);
			currentVSDIF = new SDIFdataInstanceVector(5,1);
			// <traj>
		} else if (qName.equalsIgnoreCase("traj"))
		{
			currentTraj = new HoloTraj(new Integer(attrs.getValue("begin_number")));
			// <point>
		} else if (qName.equalsIgnoreCase("point"))
		{
			currentTraj.add(new HoloPoint(new Float(attrs.getValue("x")), new Float(attrs.getValue("y")), new Float(attrs.getValue("z")), timemult * new Integer(attrs.getValue("date")), new Boolean(attrs.getValue("edit"))));
			pb.inc();
			// <waveinstance>
		} else if (qName.equalsIgnoreCase("waveinstance"))
		{
			nbWaveInstances +=1;
			currentVWave.add(new WaveFormInstance(gp, attrs.getValue("name"),timemult * new Integer(attrs.getValue("begin"))));
			pb.inc();
			// <speaker>
		} else if (qName.equalsIgnoreCase("sdifdatainstance"))
		{	
			if(gp.holoEditRef != null)
			{
				WaveFormInstance linkedWaveFormInstance;
				try {
					String linkedWaveString = new String(attrs.getValue("linkedWaveform"));
					
					if (linkedWaveString.equals("null")) {
						linkedWaveFormInstance = null;
					}else {
						// si une sdifinstance a une waveforminstance liée, on attend que toute les waveformInstances ait été crées
						while (nbWaveInstances < currentVWave.size()){
							System.out.println("nbWaveInstances");
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						String split[] = linkedWaveString.split(" - ");
						linkedWaveFormInstance = currentVWave.get((new WaveFormInstance(gp, split[0], timemult * new Integer(split[1]))).toString());
					}
				}
				catch(NullPointerException e)
				{
					linkedWaveFormInstance = null;
				}
				
				
				String name = attrs.getValue("name");
				String[] splitStr = name.split("\\s"+"-"+"\\s"); // espace+tiret+espace
				String filename = splitStr[0];
			
				currentVSDIF.add(new SDIFdataInstance(gp, name, timemult * new Integer(attrs.getValue("begin")),
						new Integer(attrs.getValue("indexFirstKey")), new Integer(attrs.getValue("indexLastKey")),
						new Double(attrs.getValue("firstKey")), new Double(attrs.getValue("lastKey")),
						new Boolean(attrs.getValue("reversed")), linkedWaveFormInstance));
				pb.inc();
				

		}// <speaker>
		} else if (qName.equalsIgnoreCase("speaker"))
		{
			speakers.add(new HoloSpeaker(new Float(attrs.getValue("x")), new Float(attrs.getValue("y")), new Float(attrs.getValue("z")), new Float(attrs.getValue("d")), new Integer(attrs.getValue("num"))));
			pb.inc();
			// <waveform>
		} else if (qName.equalsIgnoreCase("waveform"))
		{
			String sfn = attrs.getValue("file");
			
			sfn = fileSearch(sfn, qName);
			
			if(sfn == null) // TODO : fichier pas trouvé
			{
				sfn = attrs.getValue("file");
			}
			
			if(gp.holoEditRef ==  null)				
				gp.hpRef.getSoundPool().importSound(new File(sfn), true);
			else
				gp.holoEditRef.soundPoolGui.importSound(new File(sfn), true);
			pb.inc();
		}  else if (qName.equalsIgnoreCase("data"))
		{
			if(gp.holoEditRef != null)
			{
				dfn = attrs.getValue("file");
				
				dfn = fileSearch(dfn,qName);

				if(dfn == null) // TODO : fichier pas trouvé
				{
					dfn = attrs.getValue("file");
				}
				gp.holoEditRef.soundPoolGui.importData(new File(dfn), importOptions, true);
				pb.inc();
			}
			
		} else if (qName.equalsIgnoreCase("importOptions")) {
			importOptions = attrs.getValue("optionMat");
		} else
		{
			Ut.print("This file wasn't created with Holo-Edit\nor with a more recent version of Holo-Edit !");
		}
	}

	public void endElement(String uri, String localName, String qName)
	{
		// </session>
		if (qName.equalsIgnoreCase("session"))
		{
			gp.tracks = tracks;
			gp.speakers = speakers;
		} else if (qName.equalsIgnoreCase("waveform") || qName.equalsIgnoreCase("speaker") || qName.equalsIgnoreCase("point") || qName.equalsIgnoreCase("waveinstance") || qName.equalsIgnoreCase("sdifdatainstance") || qName.equalsIgnoreCase("importOptions"))
		{
			// nothing to do
			// </traj>
		} else if (qName.equalsIgnoreCase("traj"))
		{
			currentVTraj.add(currentTraj);
			// </track>
		} else if (qName.equalsIgnoreCase("track"))
		{
			currentTrack.setTrajs(currentVTraj);
			currentTrack.setWaves(currentVWave);
			currentTrack.setSDIFs(currentVSDIF);			
			tracks.add(currentTrack);
			// </data>
		} else if (qName.equalsIgnoreCase("data"))
		{
			
		} else
		{
			Ut.print("This file wasn't created with Holo-Edit\nor with a more recent version of Holo-Edit !");
//			Main.print(" L'éditeur ne comprend pas l'entrée : " + qName);
		}
	}
	private void clearDoneAndFineData(){
		if(gp.holoEditRef == null){
			gp.hpRef.getSoundPool().clearDoneAndFineData();
		}else{
			gp.holoEditRef.soundPoolGui.clearDoneAndFineData();	
		}
	}


	private String fileSearch(String name, String type)
	{
			String sfn = name;
			String[] splitname;
			String stripname;
			File f = new File(filename);
			String fdir = f.getParentFile().getAbsolutePath();
			File sf;
			if(Ut.MAC)
			{
				sfn = sfn.replace('\\','/');
				splitname = sfn.split("/");
				stripname = splitname[splitname.length - 1];
				if(sfn.startsWith(".."))
				{
					File parent = f.getParentFile().getParentFile();
					sfn = sfn.substring(2);
					while(sfn.startsWith("/..") && parent.exists() && parent.isDirectory())
					{
						sfn = sfn.substring(3);
						parent = parent.getParentFile();
					}
					sfn = parent.getAbsolutePath() + sfn;
				} else if(sfn.startsWith(".")) {
					sfn = fdir + sfn.substring(1);
				}
				// il existe ?
				sf = new File(sfn);
				if (!sf.exists()) // alors cherche dans le même repertoire que celui de la session
				{
					sf = new File(fdir + "/" + stripname);
					if(!sf.exists()) // alors dans un repertoire sound ou data accolé à la session
					{
						if(type.equalsIgnoreCase("waveform"))
							sf = new File(fdir + "/sound/" + stripname);
						else
							sf = new File(fdir + "/data/" + stripname);
					}
				}
				
				if(!sf.exists()) // TODO : on demande à l'utilisateur
				{
					return null;
				}
				
					
			} else //TODO : fix for windows fn
			{
				sfn = sfn.replace('/','\\');
				splitname = sfn.split("\\\\");
				stripname = splitname[splitname.length - 1];
				if (sfn.startsWith(".."))
				{
					File parent = new File(filename).getParentFile().getParentFile();
					sfn = sfn.substring(2);
					while ((sfn.startsWith("/..") || sfn.startsWith("\\..")) && parent.exists() && parent.isDirectory())
					{
						sfn = sfn.substring(3);
						parent = parent.getParentFile();
					}
					sfn = parent.getAbsolutePath() + (parent.getAbsolutePath().endsWith(":\\") ? sfn.substring(1) : sfn);
				}
				else if (sfn.startsWith("."))
				{
					File parent = new File(filename).getParentFile();
					sfn = parent.getAbsolutePath() + (parent.getAbsolutePath().endsWith(":\\") ? sfn.substring(2) : sfn.substring(1));
				}
				// il existe ?
				sf = new File(sfn);
				if (!sf.exists()) // alors cherche dans le même repertoire que celui de la session
				{
					sf = new File(fdir + "\\" + stripname);
					if(!sf.exists()) // alors dans un repertoire sound ou data accolé à la session
					{
						if(type.equalsIgnoreCase("waveform"))
							sf = new File(fdir + "\\sound\\" + stripname);
						else
							sf = new File(fdir + "\\data\\" + stripname);
					}
				}
				
				if(!sf.exists()) // TODO : on demande à l'utilisateur
				{
					return null;
				}
			}
			
				
		return sf.getAbsolutePath();
	}

}
