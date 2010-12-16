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

import holoedit.data.HoloTrack;
import holoedit.data.HoloPoint;
import holoedit.data.HoloTraj;
import holoedit.data.HoloVec3;
import holoedit.fileio.TjFileWriter;
import holoedit.gui.GestionPistes;
import holoedit.util.Ut;
import java.awt.Color;
import java.io.File;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.SwingUtilities;
import com.cycling74.max.Atom;
import com.cycling74.max.MaxClock;
import com.cycling74.max.MaxSystem;

public class TjPlayer extends HoloPlayer
{
	private static boolean init=false;
	private static String version = "4.5§4";
	private boolean cuemode = true;
	private float speed  = 1.f;
	private boolean share = false;
	private String shared_name = "default";
	private int lastcue = 1;
	private int recordcue = 0;
	private HoloTrack recTrack;
	private int lastcounter = 0;
	private float fcounter = 0.f;
	private static TreeMap<String,GestionPistes> shared_gp_map = new TreeMap<String,GestionPistes>();
	private GestionPistes shared_gp;
	private Vector<String> cue = new Vector<String>();
	private static TreeMap<String,Vector<String>> shared_cue_map = new TreeMap<String,Vector<String>>();
	private Vector<String> shared_cue;
	private HoloVec3 pscale = new HoloVec3(1.,1.,1.);
	private HoloVec3 poffset = new HoloVec3(0.,0.,0.);
	private float[] p_ret = new float[3];
	
	private HoloPoint hp,nhp,ohp;
	
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
		
		clock = new MaxClock(this);
		createInfoOutlet(false);
		
		declareAttribute("cuemode","isCuemode","setCuemode");
		declareAttribute("loop","isLoop","setLoop");
		declareAttribute("speed","getSpeed","setSpeed");
		declareAttribute("offset","getOffset","setOffset");
		declareAttribute("scale","getScale","setScale");
		declareAttribute("share","isShare","setShare");
		declareAttribute("sharename","getShareName","setShareName");
		declareAttribute("linemode","getLineMode","setLineMode");
		
		if(share)
		{
			if(shared_gp_map.get(shared_name) == null)
			{
				shared_gp_map.put(shared_name,new GestionPistes(this,true));
				shared_cue_map.put(shared_name,new Vector<String>());
			}
		shared_gp = shared_gp_map.get(shared_name);
		shared_cue = shared_cue_map.get(shared_name);
		} else {
			gestionPistes = new GestionPistes(this,true);
		}
		if(!init)
			version();
		init=true;
	}

	public void version()
	{
		post("---------------------------------");
		post("");
		post("\t\tHolo-Edit trajectory player v"+version);
		post("");
		post("---------------------------------");
	}
	
	public void read()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(share)
				{
					shared_gp.initTracks2(true);
					shared_gp.setActiveTrack(0);
					shared_gp.importSeq(0, 0);
					shared_cue.clear();
					shared_cue.add(session);
				} else {
					gestionPistes.initTracks2(true);
					gestionPistes.setActiveTrack(0);
					gestionPistes.importSeq(0, 0);
					cue.clear();
					cue.add(session);
				}
			}
		});
	}
	
	public void read(String fn)
	{
		session = fn;
		File f = new File(session);
		if(!f.exists())
			session = MaxSystem.locateFile(session);
		if(session == null)
		{
			error("TjPlayer can't find file "+fn);
			return;
		}
		if(share)
		{
			if(shared_gp.setTjFileInName(session))
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						shared_gp.initTracks2(true);
						shared_gp.setActiveTrack(0);
						shared_gp.importSeq(0, 0);
						shared_cue.clear();
						shared_cue.add(session);
					}
				});
			}
		} else {
			if(gestionPistes.setTjFileInName(session))
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						gestionPistes.initTracks2(true);
						gestionPistes.setActiveTrack(0);
						gestionPistes.importSeq(0, 0);
						cue.clear();
						cue.add(session);
					}
				});
			}
		}
	}
	
	public void init()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(share)
				{
					shared_gp.initTracks2(true);
					shared_gp.setActiveTrack(0);
					shared_cue.clear();
				} else {
					gestionPistes.initTracks2(true);
					gestionPistes.setActiveTrack(0);
					cue.clear();
				}
			}
		});
	}
	
	public void preload(Atom[] args)
	{
		if(args.length >= 2)
		{
			int tocue = args[0].toInt() - 1;
			session = args[1].toString();
			File f = new File(session);
			if(!f.exists())
				session = MaxSystem.locateFile(session);
			if(session == null)
			{
				error("TjPlayer can't find file "+args[1].toString());
				return;
			}
			if(share)
			{
				if(shared_gp.setTjFileInName(session))
				{
					System.out.println("Import "+session+" to cue/tk "+(tocue+1));
					if(tocue >= shared_gp.getNbTracks())
						for(int i = shared_gp.getNbTracks() ; i <= tocue ; i++)
							shared_gp.tracks.add(new HoloTrack(i+1, Color.black, true));
					if(tocue >= shared_cue.size())
						for(int i = shared_cue.size() ; i <= tocue ; i++)
							shared_cue.add("none");
					
					shared_gp.setActiveTrack(tocue);
					shared_gp.tracks.get(tocue).clear();
					shared_gp.importSeq2(tocue, 0);
					shared_cue.set(tocue, session);
				}
			} else {
				if(gestionPistes.setTjFileInName(session))
				{
					System.out.println("Import "+session+" to cue/tk "+(tocue+1));
					if(tocue >= gestionPistes.getNbTracks())
						for(int i = gestionPistes.getNbTracks() ; i <= tocue ; i++)
							gestionPistes.tracks.add(new HoloTrack(i+1, Color.black, true));
					if(tocue >= cue.size())
						for(int i = cue.size() ; i <= tocue ; i++)
							cue.add("none");
					
					gestionPistes.setActiveTrack(tocue);
					gestionPistes.tracks.get(tocue).clear();
					gestionPistes.importSeq2(tocue, 0);
					cue.set(tocue, session);
				}
			}
		}
	}
	
	public void printcue()
	{
		if(share)
		{
			post("current trajectory cue shared \""+shared_name+"\": ("+shared_cue.size()+" trajectories)");
			for(int i = 0 ; i < shared_cue.size() ; i++)
				post("   "+(i+1)+" "+shared_cue.get(i));
		} else {
			post("current trajectory cue : ("+cue.size()+" trajectories)");
			for(int i = 0 ; i < cue.size() ; i++)
				post("   "+(i+1)+" "+cue.get(i));
		}
	}
	
	public void write(int n, String fn)
	{
		if (!fn.endsWith(".tj"))
			fn = fn.concat(".tj");
		
		File f = new File(fn);
		File d;
		if(f.getParent() != null)
			d = new File(f.getParent());
		else
		{
			post("¥ Warning : parent directory doesn't exist ... writing to "+MaxSystem.getDefaultPath());
			d = new File(MaxSystem.getDefaultPath());
		}
		String fname = f.getName();
		String dname ;
		GestionPistes gp;
		
		if(share)
			gp = shared_gp;
		else
			gp = gestionPistes;
		
		n = n -1;
		
		if(n<0 || n>=gp.getNbTracks() || gp.getTrack(n).isEmpty())
		{
			post("cue #"+(n+1)+" doesn't exist");
			return;
		}
		
		if(d.exists())
		{
			dname = Ut.dir(d.getAbsolutePath());
			new TjFileWriter(gp, dname + fname, n, 0,false);
		}else
		{
			dname = Ut.dir(MaxSystem.getDefaultPath());
			post("¥ Warning : parent directory doesn't exist ... writing to "+MaxSystem.getDefaultPath());
			new TjFileWriter(gp, dname + fname, n, 0,false);
			
		}
		
		

		//post(f.getName()+ " " + f.getParent());
		//post(MaxSystem.getDefaultPath());
	}
	
	public void writeall(String dn,String bankn)
	{
		File d = new File(dn);
		File d2;
		String fname;
		String dname;
		String dname2;
		
		GestionPistes gp;
		Vector<String> tcue;
		
		if(!d.isDirectory())
		{
			post("¥ Warning : directory name exists and is not a directory .. using default path : "+MaxSystem.getDefaultPath());
			dname = Ut.dir(MaxSystem.getDefaultPath());
		}else
			dname = Ut.dir(d.getAbsolutePath());
		
		dname2 = dname + bankn;
		d2 = new File(dname2);
		
		if(!d2.exists())
			d2.mkdir();
		
		dname2 = Ut.dir(dname2);
		
		if(share)
		{
			gp = shared_gp;
			tcue = shared_cue;
		}
		else
		{
			gp = gestionPistes;
			tcue = cue;
		}
		
		
		for(int i = 0 ; i < tcue.size() ; i++)
		{
			if(!gp.getTrack(i).isEmpty())
			{
				fname = bankn +(i+1)+".tj";
				new TjFileWriter(gp, dname2 + fname, i, 0,false);
			}
		}
		
	}
	
	public void stop()
	{
		super.stop();
		recordcue = 0;
		send("stop");
	}
	
	public void inlet(int i)
	{
		if(i <= 0)
			stop();
		else if(!cuemode)
			play();
		else if((share && i <= shared_cue.size()) || i <= cue.size()) {
			lastcue = i-1;
			play();
		}
	}
	
	public void play()
	{
		autostop = false;
		playing = true;
		firstCue = true;
		loopNum = 0;
		recordcue = 0;
		fcounter = counter = begtime;
		
		if(share)
			shared_gp.preparePlay();
		else
			gestionPistes.preparePlay();
		
		if(cuemode)
		{
			if( share )
				endtime = shared_gp.getTrack(lastcue).getLastDate();
			else
				endtime = gestionPistes.getTrack(lastcue).getLastDate();
			
		} else {
			if(share)
			{
				endtime = 0;
				for(int i = 0 ; i < shared_gp.getNbTracks() ; i++)
					endtime = Ut.max(endtime, shared_gp.getTrack(i).getLastDate());
			} else {
				endtime = 0;
				for(int i = 0 ; i < gestionPistes.getNbTracks() ; i++)
					endtime = Ut.max(endtime, gestionPistes.getTrack(i).getLastDate());
			}
		}
		looping = !firstCue;
		clock.delay(0);
	}
	
	public void execute()
	{
		if(!paused && playing)
		{
			if(recordcue>0)
			{
				looping = false;
				fcounter += 1.f;
				counter	= (int) (fcounter + 0.5f);
				if(counter%10 == 0) send("time",Atom.newAtom(counter));
				clock.delay(tick);
			}else
			{
				if(counter > endtime || counter < begtime)
				{
					if(loop)
					{
						send("looping");
						sellength = endtime - begtime;
						counter = begtime + Ut.modabs((counter - begtime),sellength);
						fcounter = counter;
						looping = true;
						loopNum++;
					} else {
						send("stop");
						playing = false;
						paused = false;
						autostop = true;
					}
				}
	
				
				
				if(share)
				{
					if (cuemode)
					{
						
						currentTrack = shared_gp.getTrack(lastcue);
						hp = currentTrack.getPointPlaySub(counter,lastcounter);//, autostop, looping);
						if (hp != null && !hp.equals(ohp))
						{
							if(linemode && speed != 0.)
							{
								if(speed > 0.)
									nhp = hp.nextpoint;
								else
									nhp = hp.prevpoint;
								
								hp = hp.copyScaleTrans(pscale, poffset);
								if(nhp != null)
								{
									nhp = nhp.copyScaleTrans(pscale, poffset);
									send("time",Atom.newAtom(counter));
									send("cue",Atom.newAtom(lastcue + 1));
									send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z),Atom.newAtom(Math.abs((nhp.date-hp.date)/speed)),Atom.newAtom(nhp.x),Atom.newAtom(nhp.y),Atom.newAtom(nhp.z)});
								}
								else 
								{
									hp = hp.copyScaleTrans(pscale, poffset);
									send("time",Atom.newAtom(counter));
									send("cue",Atom.newAtom(lastcue + 1));
									send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});
								}
									
							}
							else 
							{
								hp = hp.copyScaleTrans(pscale, poffset);
								send("time",Atom.newAtom(counter));
								send("cue",Atom.newAtom(lastcue + 1));
								send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});
							}
						
						ohp = hp;
						
						}
					}
					else
					{
						for (int i = 0, last = shared_gp.getNbTracks(); i < last; i++)
						{
							currentTrack = shared_gp.getTrack(i);
							if (currentTrack.isVisible())
							{
								hp = currentTrack.getPointPlaySub(counter,lastcounter);//, autostop, looping);
								if (hp != null && !hp.equals(ohp))
								{
									if(linemode && speed != 0.)
									{
										if(speed > 0.)
											nhp = hp.nextpoint;
										else
											nhp = hp.prevpoint;
										
										hp = hp.copyScaleTrans(pscale, poffset);
										if(nhp != null)
										{
											nhp = nhp.copyScaleTrans(pscale, poffset);
											send("time",Atom.newAtom(counter));
											send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z),Atom.newAtom(Math.abs((nhp.date-hp.date)/speed)),Atom.newAtom(nhp.x),Atom.newAtom(nhp.y),Atom.newAtom(nhp.z)});
										}
										else 
										{
											send("time",Atom.newAtom(counter));
											send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});
										}
											
									}
									else 
									{
										hp = hp.copyScaleTrans(pscale, poffset);
										send("time",Atom.newAtom(counter));
										send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});								
									}
								
									ohp = hp;
								}

							}
						}
					}
				}
				else
				{
					// CUEMODE 1 SHARE 0
					if (cuemode)
					{
						currentTrack = gestionPistes.getTrack(lastcue);
						hp = currentTrack.getPointPlaySub(counter,lastcounter);//, autostop, looping);
						//hp = currentTrack.getPointPlay(counter);//, autostop, looping);
						if (hp != null && !hp.equals(ohp))
						{
							
							if(linemode && speed != 0.)
							{
								if(speed > 0.)
									nhp = hp.nextpoint;
								else
									nhp = hp.prevpoint;
								
								hp = hp.copyScaleTrans(pscale, poffset);
								if(nhp != null)
								{
									nhp = nhp.copyScaleTrans(pscale, poffset);
									send("time",Atom.newAtom(counter));
									send("cue",Atom.newAtom(lastcue + 1));
									send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z),Atom.newAtom(Math.abs((nhp.date-hp.date)/speed)),Atom.newAtom(nhp.x),Atom.newAtom(nhp.y),Atom.newAtom(nhp.z)});
								}
								else
								{
									send("time",Atom.newAtom(counter));
									send("cue",Atom.newAtom(lastcue + 1));
									send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});
								}
									
							}
							else
							{
								hp = hp.copyScaleTrans(pscale, poffset);
								send("time",Atom.newAtom(counter));
								send("cue",Atom.newAtom(lastcue + 1));
								send("pos",new Atom[]{Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});						
							}
						
							ohp = hp;
						}
					}
					else
					{
						for (int i = 0, last = gestionPistes.getNbTracks(); i < last; i++)
						{
							currentTrack = gestionPistes.getTrack(i);
							if (currentTrack.isVisible())
							{
								hp = currentTrack.getPointPlaySub(counter,lastcounter);//, autostop, looping);
								if (hp != null && !hp.equals(ohp))
								{
									if(linemode && speed != 0.)
									{
										if(speed > 0.)
											nhp = hp.nextpoint;
										else
											nhp = hp.prevpoint;
										
										hp = hp.copyScaleTrans(pscale, poffset);
										if(nhp != null)
										{
											nhp = nhp.copyScaleTrans(pscale, poffset);
											send("time",Atom.newAtom(counter));
											send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z),Atom.newAtom(Math.abs((nhp.date-hp.date)/speed)),Atom.newAtom(nhp.x),Atom.newAtom(nhp.y),Atom.newAtom(nhp.z)});
										}
										else 
										{
											send("time",Atom.newAtom(counter));
											send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});
										}
											
									}
									else {
										hp = hp.copyScaleTrans(pscale, poffset);
										send("time",Atom.newAtom(counter));
										send("tk",new Atom[]{Atom.newAtom(i+1),Atom.newAtom("pos"),Atom.newAtom(hp.x),Atom.newAtom(hp.y),Atom.newAtom(hp.z)});								
									}
									
									ohp = hp;
								}	
							}
						}
					}
				}
			
				//post(hp.hashCode());
				
			looping = false;
			fcounter += speed;
			lastcounter = counter;
			counter	= (int) (fcounter + 0.5f);
			if(!autostop)
				clock.delay(tick);
		}
		}
	}
	
	public void write(){}
	
	public void writeagain(){}
	
	public void preload(){}
	
	public void update(){}
	
	public void queryspeakers(){}
	
	public void record(int r){
		
		r = r>=0 ? r : 0;
		if(r == 0)
		{
			playing = false;
			
			if(recordcue>0 && recTrack != null)
			{
				if(!recTrack.isEmpty())
				{
					HoloPoint hp = new HoloPoint(recTrack.lastElement());
					hp.date = counter;
					recTrack.add(hp);
				}
				lastcue = recordcue - 1;
			}
			
			recordcue = 0;
				
		}else
		{
			if(recordcue > 0)
				record(0);
			playing = true;
			autostop = false;
			playing = true;
			firstCue = true;
			loopNum = 0;
			fcounter = counter = 0;
			
			recordcue = r;
			
			int tocue = r - 1;
			
			if(share)
			{
					if(tocue >= shared_gp.getNbTracks())
						for(int i = shared_gp.getNbTracks() ; i <= tocue ; i++)
							shared_gp.tracks.add(new HoloTrack(i+1, Color.black, true));
					if(tocue >= shared_cue.size())
						for(int i = shared_cue.size() ; i <= tocue ; i++)
							shared_cue.add("none");
					
					shared_gp.setActiveTrack(tocue);
					recTrack = shared_gp.getTrack(tocue);
					recTrack.clear();
					recTrack.addTraj(new HoloTraj());
					shared_cue.set(tocue, "recorded_#"+(tocue+1));
					
			} else {
				
					if(tocue >= gestionPistes.getNbTracks())
						for(int i = gestionPistes.getNbTracks() ; i <= tocue ; i++)
							gestionPistes.tracks.add(new HoloTrack(i+1, Color.black, true));
					if(tocue >= cue.size())
						for(int i = cue.size() ; i <= tocue ; i++)
							cue.add("none");
					
					gestionPistes.setActiveTrack(tocue);
					recTrack = gestionPistes.getTrack(tocue);
					recTrack.clear();
					recTrack.addTraj(new HoloTraj());
					cue.set(tocue, "recorded_#"+(tocue+1));
				
			}
			
			clock.delay(0);
		}
		
	}

	public void xyz(float x,float y,float z)
	{
		if(recordcue>0 && recTrack != null)
		{
			recTrack.add(new HoloPoint(x,y,z,counter));
		}
	}
	
	private boolean isCuemode()
	{
		return cuemode;
	}

	private void setCuemode(boolean cuemode)
	{
//		System.out.println("cuemode : "+cuemode);
		stop();
		this.cuemode = cuemode;
	}
	
	protected boolean isLoop()
	{
		return super.isLoop();
	}
	
	protected void setLoop(boolean loop)
	{
//		System.out.println("loop : "+loop);
		super.setLoop(loop);
	}
	
	public void dump()
	{
		for(int i = 0 ; i < cue.size() ; i++)
			dump(i+1);
	}
	
	public void dump(int i)
	{
		if(share)
		{
			int currentCue;
			if (i > shared_cue.size() || i < 1)
			{
				post("TjPlayer : no cue " + i + " to dump");
				return;
			}
			currentCue = i - 1;
			for (HoloPoint p : shared_gp.getTrack(currentCue).elements())
				send("dump",new Atom[]{Atom.newAtom(i),Atom.newAtom(p.date),Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z),Atom.newAtom(p.isEditable())});
				//send("dump " + i + " " + p.date + " " + p.x + " " + p.y + " " + p.z + " " + p.isEditable());
		}
		else
		{
			int currentCue;
			if (i > cue.size() || i < 1)
			{
				post("TjPlayer : no cue " + i + " to dump");
				return;
			}
			currentCue = i - 1;
			for (HoloPoint p : gestionPistes.getTrack(currentCue).elements())
				send("dump",new Atom[]{Atom.newAtom(i),Atom.newAtom(p.date),Atom.newAtom(p.x),Atom.newAtom(p.y),Atom.newAtom(p.z),Atom.newAtom(p.isEditable())});
		}
	}
	
	private void setSpeed(float speed)
	{
//		System.out.println("speed : "+speed);
		this.speed = speed;
	}
	
	private float getSpeed()
	{
		return speed;
	}

	
	private void setOffset(float x, float y, float z)
	{
		this.poffset.x = x;
		this.poffset.y = y;
		this.poffset.z = z;
	}
	
	private float[] getOffset()
	{
		p_ret[0] = (float) poffset.x;
		p_ret[1] = (float) poffset.y;
		p_ret[2] = (float) poffset.z;
		return p_ret;
	}
	
	private void setScale(float x, float y, float z)
	{
		this.pscale.x = x;
		this.pscale.y = y;
		this.pscale.z = z;
	}
	
	private float[] getScale()
	{
		p_ret[0] = (float) pscale.x;
		p_ret[1] = (float) pscale.y;
		p_ret[2] = (float) pscale.z;
		return p_ret;
	}
	
	private boolean isShare()
	{
		return share;
	}

	private void setShare(boolean share)
	{
//		System.out.println("share : "+share);
		stop();
		this.share = share;
		
		if(share)
		{
			gestionPistes = null;
			cue.clear();
			
			if(shared_gp_map.get(shared_name) == null)
			{
				shared_gp_map.put(shared_name,new GestionPistes(this,true));
				shared_cue_map.put(shared_name,new Vector<String>());
			}
		shared_gp = shared_gp_map.get(shared_name);
		shared_cue = shared_cue_map.get(shared_name);
		} else {
			gestionPistes = new GestionPistes(this,true);
			cue.clear();
		}
	}
	
	private String getShareName()
	{
		return shared_name;
	}
	
	private void setShareName(String name)
	{
		shared_name = name;
		setShare(true);
	}
	
}