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
package holoedit;

import holoedit.data.HoloPoint;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.HoloWaveForm;
import holoedit.data.WaveFormInstance;
import holoedit.gui.GestionPistes;
import holoedit.gui.SimpleSoundPool;
import holoedit.rt.Player;
import holoedit.util.Ut;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.cycling74.max.Atom;
import com.cycling74.max.Executable;
import com.cycling74.max.MaxClock;
import com.cycling74.max.MaxObject;
import com.cycling74.max.MaxSystem;

public class HoloPlayer extends MaxObject implements Executable,Player
{
	private static double version = 4.5;
	protected static final boolean VERBOSE_IN = false;
	protected static final boolean VERBOSE_OUT = false;
	
	
	protected static final int CUE_OFFSET = 150; // limit number of tracks to 24 ( because of SPECIAL CUES management )
	public GestionPistes gestionPistes;
	public SimpleSoundPool soundPool;
	protected int tick = 1;
	protected double tack;
	protected double delay;
	protected String vc,wc;
	double mytime, deltatack;
	protected double realcounterf;
	protected int realcounter;
	protected String session;
	protected MaxClock clock;
	protected boolean autostop = false;
	protected boolean playing = false;
	protected boolean paused = false;
	protected boolean loop = false;
	protected int counter;
	protected long oldDate = -1, date = -1;
	protected long baseDate;
	protected int endtime = 100000;
	protected int begtime = 0;
	protected int sellength;
	protected int totaltime;
	protected boolean firstCue;
	protected boolean looping = false;
	protected boolean preloading;
	protected boolean linemode = true;
	protected int loopNum = 0;
	protected HoloTrack currentTrack;
	protected Vector<HoloTraj> recTrajs = new Vector<HoloTraj>();
	protected int recBegDate = 0;
	protected boolean recording = false;
	protected boolean saved = true;
	protected ArrayList<Atom> templist = new ArrayList<Atom>();
	
	String keyOut="/holospat";

	public HoloPlayer()
	{
		initObj();
	}

	protected void initObj()
	{
		// On recupere le nom du system d'exploitation et on affecte les
		// variables en fonction
		Ut.MAC = System.getProperty("os.name").indexOf("Mac") >= 0;
		if (Ut.MAC)
		{
			Ut.dirCar = "/";
		} else {
			Ut.dirCar = "\\";
		}
		
		soundPool = new SimpleSoundPool(this);
		gestionPistes = new GestionPistes(this,false);
		clock = new MaxClock(this);
		declareAttribute("begtime","getBegin","setBegin");
		declareAttribute("endtime","getEnd","setEnd");
		declareAttribute("totaltime","getTotal","setTotal");
		declareAttribute("session","getSession","setSession");
		declareAttribute("linemode","getLineMode","setLineMode");
		
		post("---------------------------------");
		post("");
		post("\t\tHolo-Edit session player v"+version);
		post("");
		post("---------------------------------");
		
	}
	
	// Demander confirmation a l'utilisateur avant de quitter si le fichier a ete modifie depuis sa derniere sauvegarde
	public boolean askForSave()
	{
		int result;
		boolean out = false;
		try
		{
			if (isSaved())
				out = true;
			else
			{
				result = JOptionPane.showConfirmDialog(null, "Do you want to save the changes", "Save ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == 0)
				{
					gestionPistes.writeHoloFile();
					out = true;
				} else if (result == 1)
					out = true;
			}
		}
		catch (NullPointerException npe)
		{}
		return out;
	}

	public void init()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.initTracks2(false);
			}
		});
	}
	
	public void read()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.readHoloFile();
			}
		});
	}
	
	public void read(String fn)
	{
		session = fn;
		File f = new File(session);
		if(!f.exists())
			session = MaxSystem.locateFile(session);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.readHoloFile(session);
			}
		});
	}
	
	public void write()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.writeHoloFileAs();
			}
		});
	}
	
	public void write(String fn)
	{
		session = fn;
		File f = new File(session);
		if(!f.exists())
		{
			session = MaxSystem.locateFile(session);
			System.out.println("correct : "+session);
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.writeHoloFile(session);
			}
		});
	}
	
	public void writeagain()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gestionPistes.writeHoloFile();
			}
		});
	}

	public void osc(Atom[] msg)
	{
		if(msg.length < 1)
			return;
		String addr;
		
		addr = msg[0].getString();
		msg = Atom.removeFirst(msg);
		
		treatPacket(null, addr, msg);

	}
	
	protected void treatPacket(Date msgDate, String key, Atom[] msg)
	{ 
		Vector<String> addr= new Vector<String>(0,1);
		String node;
		String[] keys =  key.split("/");
		
		for(String k : keys)
				addr.add(k);
					
		if(VERBOSE_IN)
		{
			System.out.print("OSCIN : "+key);
			for(String k:keys)
				System.out.print("-"+k+"-");
			System.out.println();
			for(Object a:msg)
				System.out.print(" "+a.getClass());
			System.out.println();
		}
		
		
		if(addr.size()==0)
		{
			System.out.println("Holo-Edit : Unrecognized OSC Message check address ...");
			return;
		}
		
		//remove the first empty element in Vector
		node = addr.remove(0);
		
		//node = addr.remove(0);
		// special to the first key to be compared with main HoloEdit Key
		//node = "/"+ node;

		if(true)//node.equalsIgnoreCase(keyIn))
		{
			
			if(addr.size()==0)
				return;
			node = addr.remove(0);
			
			// ---- recorder ----
			if (node.equalsIgnoreCase("recorder"))
			{
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				int tkNum = Integer.parseInt(node);
				boolean found = false;
				
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				if (node.equalsIgnoreCase("in"))
					treatRecord(tkNum,msg);
				if (node.equalsIgnoreCase("segment"))
					treatRecordSegment(tkNum);
				else if (node.equalsIgnoreCase("enable"))
				{
					boolean tkRecOn = msg[0].getInt() > 0;
					for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
					{
						HoloTrack tk = gestionPistes.getTrack(p);
						if(tk.getNumber()==tkNum)
						{
							found = true;
							tk.setRecEnable(tkRecOn);
							if(tkRecOn)
								tk.setVisible(true);
						}
					}
				}
			}
			// ---- transport ----
			else if(node.equalsIgnoreCase("transport"))
			{
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				if (node.equalsIgnoreCase("play"))
				{	
					//order = ORDER_PLAY;
					play();
				}
				else if (node.equalsIgnoreCase("resume"))
				{
					//order = ORDER_RESUME;
					paused = true;
					pause();
				}
				else if (node.equalsIgnoreCase("pause"))
				{
					//order = ORDER_PAUSE;
					paused = false;
					pause();
				}
				else if (node.equalsIgnoreCase("stop"))
				{
					//order = ORDER_STOP;
					stop();
				}
				else if (node.equalsIgnoreCase("record") && msg.length > 0)
				{
					//order = ORDER_RECORD;
					record(msg[0].getInt());
				}
				
				else if (node.equalsIgnoreCase("position"))
					position();
				else if (node.equalsIgnoreCase("update"))
					update();
				else if (node.equalsIgnoreCase("time") && msg.length > 0)
					setTime(msg[0].toInt());
				else if (node.equalsIgnoreCase("begtime") && msg.length > 0)
					setBegin(msg[0].toInt());
				else if (node.equalsIgnoreCase("endtime") && msg.length > 0)
					setEnd(msg[0].toInt());
				else if (node.equalsIgnoreCase("totaltime") && msg.length > 0)
					setTotal(msg[0].toInt());
				else if (node.equalsIgnoreCase("loop") && msg.length > 0)
					setLoop(msg[0].toInt() > 0);
			}
			
			// ---- connection ----
//			else if(node.equalsIgnoreCase("connection"))
//			{
//				
//				if(addr.size()==0)
//					return;
//				
//				node = addr.remove(0);
//				
//				
//				if (node.equalsIgnoreCase("key") && msg.length > 0)
//				{	
//						keyOut = msg[0].getString();
//				} 
//				else if (node.equalsIgnoreCase("querykey"))
//					send(keyOut+"/connection/holoedit/key",new Object[]{keyIn});
//				else if (node.equalsIgnoreCase("connect"))
//				{	
//					boolean connect = true;
//					if(msg.length > 0)
//						connect = (Integer)msg[0] > 0;
//					if(connect)
//						open();
//					else
//						close();
//				} 
//			}
			
			
			// ---- soundpool ----
			else if(node.equalsIgnoreCase("soundpool"))
			{
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				if (key.equalsIgnoreCase("preload"))
					preload();
			}
			
			// ---- tracks ----
			//TODO : wildcard support pour OSCin
			else if(node.equalsIgnoreCase("track"))
			{
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				int tkNum = Integer.parseInt(node);
				boolean found = false;
				
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				if((node.equalsIgnoreCase("visible")||node.equalsIgnoreCase("on")) && msg.length > 0)
				{
					for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
						if(gestionPistes.getTrack(p).getNumber()==tkNum)
						{
							found = true;
							gestionPistes.changeVisible(p);
							gestionPistes.ts.checkVisible[p].check((msg[0].getInt())>0);
						}
					//room.display();
				}
			}
			
			
			else if (key.equalsIgnoreCase("/speaker/queryspeakers"))
				sendSpeakers();
		}
			else 
				post("Holo-Edit doesn't understand "+key + " " + msg);
		
	}
	
	protected void treatRecord(int tkNum, Atom[] msg)
	{
		
		int loopDur = endtime - begtime;
		int date = counter;
		boolean editable=false;
		if(msg.length < 3 || !recording)
			return;
		float x = msg[0].getFloat();
		float y = msg[1].getFloat();
		float z = msg[2].getFloat();
		if(msg.length > 3)
			editable = msg[3].getInt() > 0;
		if(msg.length > 4)
			date = msg[4].getInt();
			
		boolean found = false, empty = false;
		HoloTraj ht;
		for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
		{
			HoloTrack tk = gestionPistes.getTrack(p);
			if(tk.getNumber()==tkNum && tk.isRecEnable())
			{
				
				found = true;
				ht = recTrajs.get(p);
				empty = ht.isEmpty();
				if(empty)
				{
					ht.add(new HoloPoint(x,y,z,date,true));
					tk.addTraj(ht);
					tk.update();
				}else
					ht.add(new HoloPoint(x,y,z,date,editable));
				
			}
		}
		
	}
	
	
	protected void treatRecordSegment(int tkNum)
	{
		int loopDur = endtime - begtime;
		int date = counter;
		
		boolean found = false;
		for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
		{
			HoloTrack tk = gestionPistes.getTrack(p);
			if(tk.getNumber()==tkNum && tk.isRecEnable())
			{
				HoloTraj ht = new HoloTraj();
				if(!recTrajs.get(p).isEmpty())
					recTrajs.get(p).lastElement().setEditable(true);
				recTrajs.set(p, ht);
			}
		}
		
	}
	
	
	public Atom[] atomList(ArrayList<Atom> list)
	{
		int len = list.size();
		Atom[] out = new Atom[len];
		
		for(int i=0;i<len;i++)
		{
			out[i] = list.get(i);
		}
		
		return out;
	}

	public void send(String s)
	{
		outlet(0,Atom.newAtom(s));
	}
	
	
	public void send(String s, Atom[] atoms)
	{
		outlet(0,Atom.newAtom(s,atoms));
		
	}
	
	public void send(String s, Atom atom)
	{
		outlet(0,Atom.newAtom(s,new Atom[]{atom}));
	}
	
	
	public void done()
	{
		outlet(0,Atom.newAtom("/done"));
	}
	
	public void position()
	{
		HoloPoint p;
		counter = begtime;
		//out("time "+counter);
		send(keyOut+"/transport/time",Atom.newAtom(counter));
		for(int i = 0, last = gestionPistes.getNbTracks() ; i < last ; i++)
		{
			currentTrack = gestionPistes.getTrack(i);
			currentTrack.stop(true);
			if(currentTrack.isVisible())
			{
				if((p = currentTrack.getPointPlay(counter)) != null)
					send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Atom[]{ Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z)});
			
			}
		}
	}
	
	public void sendVisible()
	{
		
		for(HoloTrack t:gestionPistes.tracks)
		{
			send(keyOut+"/track/"+t.getNumber()+"/visible",Atom.newAtom(t.isVisible() ? 1 : 0));
			send(keyOut+"/track/"+t.getNumber()+"/active",Atom.newAtom(t.isVisible() ? 1 : 0));
		}
	}

	public void sendPaths()
	{
		Vector<String> paths = new Vector<String>();
		for(HoloWaveForm hwf:gestionPistes.soundPool.getSounds())
		{	
			String p = hwf.getPathWoQuote();
			if(!paths.contains(p))
				paths.add(p);
		}
		send(keyOut+"/soundpool/paths",Atom.newAtom(paths.size()));
		for(int i = 0 ; i < paths.size() ; i++)
			send(keyOut+"/soundpool/path",new Atom[]{ Atom.newAtom(i),Atom.newAtom(paths.get(i))});
	}
	
	public void play()
	{
		if(!playing)
		{
			// PRELOAD
//			preload();
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {}
			position();
			for (HoloTrack tk : gestionPistes.tracks)
			{
				if (tk.waves.isEmpty())
					totaltime = Ut.max(totaltime, tk.getLastDate());
				else
					totaltime = Ut.max(totaltime, Ut.max(tk.getLastDate(), tk.waves.lastElement().getLastDate()));
			}
			autostop = false;
			playing = true;
			firstCue = true;
			loopNum = 0;
			counter = begtime;
			realcounterf = realcounter = begtime;
			looping = !firstCue;
			baseDate = System.currentTimeMillis() - counter;
			tack = System.currentTimeMillis() - tick;
			clock.delay(0);
		}
	}
	
	public void pause()
	{
		if(paused)
		{
			paused = false;
			baseDate = System.currentTimeMillis() - counter;
			tack = System.currentTimeMillis() - tick;
			clock.delay(0);
		} else {
			paused = true;
			clock.unset();
		}
	}
	
	public void resume()
	{
		if(paused)
		{
			paused = false;
			baseDate = System.currentTimeMillis() - counter;
			tack = System.currentTimeMillis() - tick;
			clock.delay(tick);
		}
	}
	
	public void stop()
	{
		clock.unset();
		if(recording)
		{
			record(0);
			gestionPistes.preparePlay();
		}
		recording = false;
		playing = false;
		paused = false;
	}
	
	protected void setLoop(boolean loop)
	{
		this.loop = loop;
	}
	
	protected boolean isLoop()
	{
		return loop;
	}
	
	public void preload()
	{

		int tkNum;
		int tkNb = gestionPistes.getNbTracks();
		
		preloading = true;
		sendPaths();
		sendVisible();
		for(HoloTrack tk:gestionPistes.tracks)
		{
			tkNum = tk.getNumber();
			for(int i = 1 ; i <= tk.waves.size() ; i++)
				preloadWaveForm(tk.waves.get(i-1),tk,CUE_OFFSET+(i*tkNb)+tkNum,begtime,begtime,endtime);
		}
		//post("preload");
		gestionPistes.preparePlay();
		preloading = false;
	}
	
	public void preloadFirst()
	{
		int tkNum;
		int tkNb = gestionPistes.getNbTracks();
		
		preloading = true;

		sendPaths();
		sendVisible();
		 
		// wait for max to initialize search path
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		for(HoloTrack tk:gestionPistes.tracks)
		{
			tkNum = tk.getNumber();
			for(int i = 1 ; i <= tk.waves.size() ; i++)
				preloadWaveForm(tk.waves.get(i-1),tk,CUE_OFFSET+(i*tkNb)+tkNum,begtime,begtime,endtime);

		}
		//post("preload first");
		gestionPistes.preparePlay();
		preloading = false;
	}
	
	public void preloadWaveForm(WaveFormInstance w,HoloTrack tk, int cueNb, int cursorTime, int begL, int endL)
	{
		HoloWaveForm hwf = w.getWave();
		if(hwf == null)
			w.update();
		if(hwf == null)
			return;
		w.loopDefined = -1;
		w.cue = cueNb;
		w.begLoop = begL;
		w.endLoop = endL;
		//String keyOut = gpRef.mainRef.connection.getKeyOut();
		int tkNb = tk.getNumber();
		int cueOffset = w.SPEC_CUE_OFFSET + w.SPEC_CUE_NB * tkNb;
		if(Ut.between(cursorTime,w.getFirstDate(),w.getLastDate()))
		{
			if(w.getLastDate() > w.endLoop && cursorTime < w.endLoop)
			{
				send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.CURSOR_CUE_LOOP),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((cursorTime-w.getFirstDate())),Atom.newAtom((w.endLoop-w.getFirstDate()))});
				w.loopDefined = w.LOOP_END_CUE;
			}
				send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.CURSOR_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((cursorTime-w.getFirstDate()))});
		}
		if(Ut.between(w.getFirstDate(),w.begLoop,w.endLoop) && !Ut.between(w.getLastDate(),w.begLoop,w.endLoop))
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.LOOP_END_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom(0),Atom.newAtom((w.endLoop-w.getFirstDate()))});
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.LOOP_AFTER_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((w.endLoop-w.getFirstDate()))});
			w.loopDefined = w.LOOP_END_CUE;
		} else if(!Ut.between(w.getFirstDate(),w.begLoop,w.endLoop) && Ut.between(w.getLastDate(),w.begLoop,w.endLoop))
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.LOOP_BEGIN_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((w.begLoop-w.getFirstDate()))});
			w.loopDefined = w.LOOP_BEGIN_CUE;
		} else if(w.getFirstDate() < w.begLoop && w.getLastDate() > w.endLoop)
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.LOOP_IN_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((w.begLoop-w.getFirstDate())),Atom.newAtom((w.endLoop-w.getFirstDate()))});
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueOffset+w.LOOP_AFTER_CUE),Atom.newAtom(hwf.getStripPathWoQuote()),Atom.newAtom((w.endLoop-w.getFirstDate()))});
			w.loopDefined = w.LOOP_IN_CUE;
		}
		
			send(keyOut+"/track/"+tkNb+"/preload",new Atom[]{Atom.newAtom(cueNb),Atom.newAtom(hwf.getStripPathWoQuote())});
		return;
	}
	
	public void playWaveForm(WaveFormInstance w, HoloTrack tk, int time, boolean firstCue, boolean loop, boolean looping)
	{
		if(Ut.between(time,w.getFirstDate(),w.getLastDate()))
		{
			int tkNb = tk.getNumber();
			int cueOffset = w.SPEC_CUE_OFFSET + w.SPEC_CUE_NB * tkNb;
			if(firstCue)
			{
				if(loop && w.loopDefined == w.LOOP_END_CUE)
				{
					send(keyOut+"/track/"+tkNb+"/cue",Atom.newAtom(cueOffset+w.CURSOR_CUE_LOOP));
					return;
				}
				send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(cueOffset+w.CURSOR_CUE));
				return;
			} else if(loop) {
				if(time == w.begLoop && looping) {
					if(w.loopDefined == w.LOOP_BEGIN_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(cueOffset+w.LOOP_BEGIN_CUE));
						return;
					}
					else if(w.loopDefined == w.LOOP_IN_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(cueOffset+w.LOOP_IN_CUE));
						return;
					}
					else if(time == w.begTime)
					{
						send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(w.cue));
						return;
					}
				} else if(time == w.begTime) {
					if(w.loopDefined == w.LOOP_END_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(cueOffset+w.LOOP_END_CUE));
						return;
					}
					send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(w.cue));
					return;
				}
			} else if(time == w.begTime) {
				w.loopDefined = -1;
				send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(w.cue));
				return;
			} else if(time == w.endLoop && (w.loopDefined == w.LOOP_END_CUE || w.loopDefined == w.LOOP_IN_CUE))
			{
				send(keyOut+"/track/"+tkNb+"/cue", Atom.newAtom(cueOffset+w.LOOP_AFTER_CUE));
				return;
			}
		}
		return;
	}
	
	public void update()
	{
		templist.clear();
		
		//templist.toArray().
		
		send(keyOut+"/config/tracknum",Atom.newAtom(gestionPistes.getNbTracks()));
		for(HoloTrack tk:gestionPistes.tracks)
			templist.add(Atom.newAtom(tk.getNumber()));
		send(keyOut+"/config/tracklist",atomList(templist));
			//tmp += " " + tk.getNumber();
	
		for(HoloTrack tk:gestionPistes.tracks)
		{
			Color c = tk.getColor();
			String na = tk.getName();
			/*if(na.indexOf(' ') != -1)
				na = "\""+na+"\"";*/
			send(keyOut+"/track/"+tk.getNumber()+"/name",Atom.newAtom(na));
			send(keyOut+"/track/"+tk.getNumber()+"/color",new Atom[]{Atom.newAtom(c.getRed()),Atom.newAtom(c.getGreen()),Atom.newAtom(c.getBlue())});
			//send("tkup "+tk.getNumber()+" "+na+" "+c.getRed()+" "+c.getGreen()+" "+c.getBlue());
		}
		position();
		preloadFirst();
	}
	
	public void execute()
	{
		//post("Žxecute "+playing + " pause "+paused);
		HoloPoint p,pn;
		while(preloading)
		{
			System.out.println("preloading...");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		if(!paused && playing)
		{
			while (realcounter >= counter)
			{
				if(loop && counter == endtime+1)
				{
					send(keyOut+"/transport/looping",Atom.newAtom("bang"));
					counter = begtime;
					looping = true;
					loopNum++;
					baseDate = System.currentTimeMillis()+counter;
				} else if(counter > totaltime && endtime <= totaltime && !recording) {
					send(keyOut+"/transport/stop",Atom.newAtom("bang"));
					playing = false;
					paused = false;
					autostop = true;
				}
					
				send(keyOut+"/transport/time",Atom.newAtom(counter));
				String c = "cues";
			
				for(int i = 0, last = gestionPistes.getNbTracks() ; i < last ; i++)
				{
					currentTrack = gestionPistes.getTrack(i);
					if(currentTrack.isVisible())
					{
						if((recording && !currentTrack.isRecEnable()) || !recording)
						{
							if((p = currentTrack.getPointPlay(counter)) != null)
							{
								if(linemode) // line mode
								{
									if((pn = p.nextpoint) != null)
										send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Atom[]{Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z),Atom.newAtom(pn.date-p.date),Atom.newAtom(pn.x),Atom.newAtom(pn.y),Atom.newAtom(pn.z)});
									else
										send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Atom[]{Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z)});
								}else send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Atom[]{Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z)});
							}	
						}
						for(WaveFormInstance w : currentTrack.waves)
						{	
							playWaveForm(w, currentTrack, counter, firstCue, loop, looping);
						}
					}
				}
				
				//if(!c.equalsIgnoreCase("cues") && playing)
				//	out(c);
				
				firstCue = false;
				looping = false;
				
				counter++;
				
				/*date = System.currentTimeMillis();
				long delta = (long)(counter - date + baseDate);
				
				post("delta "+delta + " counter " + counter);
				
				
				
				if(delta > 0)
					clock.delay(delta);
				else
					clock.delay(0);*/
				
			
				
			}
			
			if(!autostop)
			{ 
				mytime = System.currentTimeMillis();
				deltatack = mytime - tack;
				realcounterf += deltatack;
				realcounter = (int)realcounterf;
				clock.delay(tick);
				//out("debug "+counter+" "+realcounter+" "+realcounterf+" "+mytime);
				tack = mytime;
				
				
				//out("debug "+System.currentTimeMillis());
			}
			
		}
	}
	
	public void notifyDeleted()
	{
		clock.unset();
	}
	
	public void setTime(int msg)
	{
		try {
			int time = msg;
			if(time >= 0)
			{
				boolean p = playing;
				if(p)
					stop();
				counter = (int)((float)time);
				//mainRef.score.display();
				if(p)
					play();
			}
		} catch (NumberFormatException e) {}
	} 
	
	public void setBegin(int i)
	{
		begtime = i >= 0 ? i : 0;
		send(keyOut+"/transport/begin", Atom.newAtom(begtime));
	}
	
	public int getBegin()
	{
		return begtime;
	}
	
	public void setEnd(int i)
	{
		endtime = i >= 0 ? i : 0;
		send(keyOut+"/transport/end", Atom.newAtom(endtime));
	}
	
	public int getEnd()
	{
		return endtime;
	}
	
	public void setTotal(int i)
	{
		totaltime = i >= 0 ? i : 0;
		send(keyOut+"/transport/total",Atom.newAtom(totaltime));
	}
	
	public int getTotal()
	{
		return totaltime;
	}
	
	public void setSession(String filename)
	{
		session = filename;
		read(session);
	}
	
	public int setSessionName(String filename)
	{
		session = filename;
		return 0;
	}
	
	public String getSession()
	{
		return session;
	}
	
	public void setLineMode(int l)
	{
		linemode = l > 0;
	}
	
	public int getLineMode()
	{
		return linemode?1:0;
	}
	
	public void record(int r)
	{
		setSaved(false);
		if(r == 0)
		{
			// stop recording
			for(int i = 0 ; i < recTrajs.size() ; i++)
			{
				HoloTraj ht = recTrajs.get(i);
				ht.remove(0);
			}
			for(HoloTrack tk:gestionPistes.tracks)
				tk.update();
			recording = false;
			send(keyOut+"/transport/record",Atom.newAtom(0));
			
		} else {
			// start recording
			saved = false;
			recTrajs = new Vector<HoloTraj>();
			recBegDate = isPlaying() ? counter : begtime;
			for(int i = 0 ; i < gestionPistes.getNbTracks() ; i++)
			{
				HoloTraj ht = new HoloTraj();
				currentTrack = gestionPistes.getTrack(i);
				//ht.add(new HoloPoint(0,0,0,recBegDate,true));
				recTrajs.add(ht);
//				if(currentTrack.isVisible() && !currentTrack.isLocked() && currentTrack.isRecEnable())
//				{
//					currentTrack.addTraj(ht);
//					currentTrack.update();
//				}
			}
			recording = true;
			send(keyOut+"/transport/record",Atom.newAtom(1));
		}
	}
	
	public boolean isRecording()
	{
		return recording;
	}
	
	public boolean isPlaying()
	{
		return playing;
	}
	
	protected void treatRecord(String[] msg)
	{
		int loopDur = endtime - begtime;
		int tkNum = Integer.parseInt(msg[0]);
		float x = Float.parseFloat(msg[1]);
		float y = Float.parseFloat(msg[2]);
		float z = Float.parseFloat(msg[3]);
		try
		{
			int date = Integer.parseInt(msg[4]);
			boolean found = false;
			for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
			{
				HoloTrack tk = gestionPistes.getTrack(p);
				if(tk.getNumber()==tkNum && tk.isRecEnable())
				{
					found = true;
					recTrajs.get(p).add(new HoloPoint(x,y,z,date+loopNum*loopDur,false));
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {}
	}
	
	public void setSaved(boolean b)
	{
		saved = b;
	}
	
	public boolean isSaved()
	{
		return saved;
	}
	
	public void anything(String msg, Atom[] args)
	{
		if(msg.equalsIgnoreCase("rec") && recording)
		{
			treatRecord(Atom.toString(args));
		} else if(msg.equalsIgnoreCase("recenable")) {
			if(!args[0].toString().equalsIgnoreCase("#1"))
			{
				int tkNum = args[0].toInt();
				boolean tkRecOn = args[1].toInt() > 0;
				boolean found = false;
				for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
				{
					HoloTrack tk = gestionPistes.getTrack(p);
					if(tk.getNumber()==tkNum)
					{
						found = true;
						tk.setRecEnable(tkRecOn);
					}
				}
			}
		} else if(msg.equalsIgnoreCase("tk")) {
			if(!args[0].toString().equalsIgnoreCase("#1"))
			{
				int i = args[0].toInt();
				boolean o = args[1].toInt() > 0;
				boolean found = false;
				for(int p = 0 ; p < gestionPistes.getNbTracks() && !found ; p++)
				{
					HoloTrack tk = gestionPistes.getTrack(p);
					if(tk.getNumber()==i)
					{
						found = true;
						tk.setVisible(o);
					}
				}
			}
		} else if(msg.equalsIgnoreCase("queryspeakers")) {
			sendSpeakers();
		}
	}

	public void sendSpeakers()
	{
		HoloSpeaker sp;
		for(int i = 0 ; i < gestionPistes.speakers.size() ; i++)
		{
			sp = gestionPistes.speakers.get(i);
			send(keyOut+"/speaker/"+(i+1)+"/xyz",new Atom[]{Atom.newAtom(sp.X),Atom.newAtom(sp.Y),Atom.newAtom(sp.Z)});
		}
	}
	
	public SimpleSoundPool getSoundPool()
	{
		return soundPool;
	}
}