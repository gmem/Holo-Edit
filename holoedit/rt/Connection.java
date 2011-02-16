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
package holoedit.rt;

import holoedit.HoloEdit;
import holoedit.data.HoloPoint;
import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.HoloWaveForm;
import holoedit.data.WaveFormInstance;
import holoedit.util.Ut;
import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.JComboBox;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCBundle;

public abstract class Connection implements Runnable, OSCListener, ConnectionListener {
	public String PROTOCOL_VERSION = "0.1";
	protected static final boolean VERBOSE_IN = false;
	protected static final boolean VERBOSE_OUT = false;
	protected static final int ORDER_NONE = -1;
	protected static final int ORDER_STOP = 0;
	protected static final int ORDER_PLAY = 1;
	protected static final int ORDER_PAUSE = 2;
	protected static final int ORDER_RESUME = 3;
	protected static final int ORDER_RECORD = 4;
	protected static final int CUE_OFFSET = 150; // limit number of tracks to 24 ( because of SPECIAL CUES management )
	protected boolean open = false;
	protected boolean autostop = false;
	protected boolean playing = false;
	protected boolean recording = false;
	protected boolean paused = false;
	protected boolean loop = false;
	protected boolean linemode = false;
	protected int out = 13008;
	protected int in = 13005;
	protected String address = "localhost";
	protected OSCPortOut sender;
	protected OSCPortIn receiver;
	protected OSCBundle bundle;
	protected String keyOut = "/holospat";
	protected String keyIn = "/holoedit";
	protected Thread runner;
	protected int counter;
	protected int end;
	protected int beg;
	protected int total;
	protected boolean firstCue;
	protected HoloEdit holoEditRef;
	protected boolean looping = false;
	protected boolean preloading;
	protected boolean preloadAbsolute = false;
	protected long oldDate = -1, date = -1;
	protected int oversleep = 0;
	protected long baseDate;
	protected String currentEditName = "holo.edit";
	protected String currentSpatDomain = "";
	protected String currentSpatName = "";
	protected Vector<HoloTraj> recTrajs = new Vector<HoloTraj>();
	protected int recBegDate = 0;
	protected int loopNum = 0;
	protected int order;
	protected HoloTrack currentTrack;
	protected ArrayList<Object> templist = new ArrayList<Object>();

	public Connection(HoloEdit m)
	{
		holoEditRef = m;
		bundle = new OSCBundle(com.illposed.osc.OSCBundle.TIMESTAMP_IMMEDIATE);
		
	}
		
	public abstract void open();

	public abstract void close();

	protected abstract void newSender();
	
	protected abstract void newReceiver();	
	
	protected void treatPacket(Date msgDate, String key, Object[] msg)
	{ 
		Vector<String> addr= new Vector<String>(0,1);
		String node;
		String[] keys =  key.split("/");
		
		for(String k : keys)
				addr.add(k);
					
		if(VERBOSE_IN)
		{
			System.out.print("OSCIN : "+key);
			//for(String k:keys)
			//	System.out.print("-"+k+"-");
			//System.out.println();
			for(Object a:msg)
				System.out.print(" "+a.toString());
			System.out.println();
		}
		
		
		if(addr.size()==0)
		{
			System.out.println("Holo-Edit : Unrecognized OSC Message check address ...");
			return;
		}
		
		//remove the first empty element in Vector
		node = addr.remove(0);
		
		node = addr.remove(0);
		// special to the first key to be compared with main HoloEdit Key
		node = "/"+ node;

		if(node.equalsIgnoreCase(keyIn))
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
					boolean tkRecOn = oscToInt(msg[0]) > 0;
					for(int p = 0 ; p < holoEditRef.gestionPistes.getNbTracks() && !found ; p++)
					{
						HoloTrack tk = holoEditRef.gestionPistes.getTrack(p);
						if(tk.getNumber()==tkNum)
						{
							found = true;
							holoEditRef.gestionPistes.recordTrack(tk.getNumber()-1, tkRecOn);
//							if(tkRecOn)
//								tk.setVisible(true);
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
					order = ORDER_PLAY;
					play();
				}
				else if (node.equalsIgnoreCase("resume"))
				{
					order = ORDER_RESUME;
					paused = true;
					pause();
				}
				else if (node.equalsIgnoreCase("pause"))
				{
					order = ORDER_PAUSE;
					paused = false;
					pause();
				}
				else if (node.equalsIgnoreCase("stop"))
				{
					order = ORDER_STOP;
					stop();
				}
				else if (node.equalsIgnoreCase("record") && msg.length > 0)
				{
					order = ORDER_RECORD;
					record(oscToInt(msg[0]) > 0);
				}
				
				else if (node.equalsIgnoreCase("position"))
					position();
				else if (node.equalsIgnoreCase("update"))
					spatUpdate();
				else if (node.equalsIgnoreCase("time") && msg.length > 0)
					setTime(oscToInt(msg[0]));
				else if (node.equalsIgnoreCase("begtime") && msg.length > 0)
					setBegTime(oscToInt(msg[0]));
				else if (node.equalsIgnoreCase("endtime") && msg.length > 0)
					setEndTime(oscToInt(msg[0]));
				else if (node.equalsIgnoreCase("totaltime") && msg.length > 0)
					setTotalTime(oscToInt(msg[0]));
				else if (node.equalsIgnoreCase("loop") && msg.length > 0)
					setLoop(oscToInt(msg[0]));
			}
			
			// ---- connection ----
			else if(node.equalsIgnoreCase("connection"))
			{
				
				if(addr.size()==0)
					return;
				
				node = addr.remove(0);
				
				
				if (node.equalsIgnoreCase("key") && msg.length > 0)
				{	
						keyOut = (String)msg[0];
				} 
				else if (node.equalsIgnoreCase("querykey"))
					send(keyOut+"/connection/holoedit/key",new Object[]{keyIn});
				else if (node.equalsIgnoreCase("connect"))
				{	
					boolean connect = true;
					if(msg.length > 0)
						connect = oscToInt(msg[0]) > 0;
					if(connect)
						open();
					else
						close();
				} 
			}
			
			
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
					for(int p = 0 ; p < holoEditRef.gestionPistes.getNbTracks() && !found ; p++)
						if(holoEditRef.gestionPistes.getTrack(p).getNumber()==tkNum)
						{
							found = true;
							holoEditRef.gestionPistes.changeVisible(p);
							holoEditRef.gestionPistes.ts.checkVisible[p].check((oscToInt(msg[0]))>0);
						}
					holoEditRef.room.display();
				}
			}
			
			else if(node.equalsIgnoreCase("speaker"))
			{
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				
				if(node.equalsIgnoreCase("queryspeakers"))
				{
					sendSpeakers();
					return;
				}
				
				if(node.equalsIgnoreCase("clearspeakers"))
				{
					if(addr.size()==0)
					{
						clearSpeakers(0);
						return;
					}else
					{
						node = addr.remove(0);
						int spkNum = Integer.parseInt(node);
						
						clearSpeakers(spkNum);
						return;
					}
				}
				
				
				int spkNum;
				try {
					spkNum = Integer.parseInt(node);
				} catch (NumberFormatException e) {
					System.out.println("Holo-Edit doesn't understand "+key+ " " + msg);
					return;
				}
				boolean found = false;
				
				if(addr.size()==0)
					return;
				node = addr.remove(0);
				
				if((node.equalsIgnoreCase("xyz")) && msg.length > 1)
				{
					float x = oscToFloat(msg[0]);
					float y = oscToFloat(msg[1]);
					float z ;
					HoloSpeaker sp;
					boolean spfound = false;
					
					if(msg.length>2)
						z = oscToFloat(msg[2]);
					else
						z = 0;
					
					for(int i = 0 ; i < holoEditRef.gestionPistes.speakers.size(); i++)
					{
						sp = holoEditRef.gestionPistes.speakers.get(i);
						if(sp.num == spkNum)
						{
							sp.X = x;
							sp.Y = y;
							sp.Z = z;
							sp.recalcDist();
							holoEditRef.room.display();
							return;
						}
					}
					if(!found)
					{
						holoEditRef.gestionPistes.speakers.add(new HoloSpeaker(x,y,z,spkNum,0));
						holoEditRef.room.display();
					}
					
					holoEditRef.room.display();
				}
			}
			
		}
			else 
				System.out.println("Holo-Edit doesn't understand "+key + " " + msg);
		
	}
	


	protected void treatRecord(int tkNum, Object[] msg)
	{

		int date = counter;
		boolean editable=false;
		if(msg.length < 3 || !(recording && playing))
			return;
		float x = oscToFloat(msg[0]);
		float y = oscToFloat(msg[1]);
		float z = oscToFloat(msg[2]);
		if(msg.length > 3)
			editable = oscToInt(msg[3]) > 0;
		if(msg.length > 4)
			date = oscToInt(msg[4]);
		
			
		boolean found = false, empty = false;
		HoloTraj ht;
		for(int p = 0 ; p < holoEditRef.gestionPistes.getNbTracks() && !found ; p++)
		{
			HoloTrack tk = holoEditRef.gestionPistes.getTrack(p);
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
				
				tk.setRecording(true);
				
			}
		}
		
	}
	
	
	protected void treatRecordSegment(int tkNum)
	{
		
		if(!(recording && playing))
			return;

		
		boolean found = false;
		for(int p = 0 ; p < holoEditRef.gestionPistes.getNbTracks() && !found ; p++)
		{
			HoloTrack tk = holoEditRef.gestionPistes.getTrack(p);
			if(tk.getNumber()==tkNum && tk.isRecEnable())
			{
				HoloTraj ht = new HoloTraj(), lht = recTrajs.get(p);
				if(!lht.isEmpty())
				{
					lht.lastElement().setEditable(true);
					//tk.finalizeRecord();
					tk.setRecording(false);

				}
				ht.setRecording(true);
				recTrajs.set(p, ht);
			}
		}
		
	}
	
	/*
	  For recording from mouse gestures in room editor
	  It records on all tracks with armed recording.
	  It sends the position by the OSC out port
	 */
	public void treatRecordPoint(HoloPoint p)
	{
		if(!(recording && playing))
			return;
		int date = counter;
		boolean editable=p.isEditable();	
			
		boolean empty = false;
		HoloTraj ht;
		HoloPoint np;
		for(int i = 0 ; i < holoEditRef.gestionPistes.getNbTracks(); i++)
		{
			HoloTrack tk = holoEditRef.gestionPistes.getTrack(i);
			
			if(tk.isRecEnable())
			{
				ht = recTrajs.get(i);
				empty = ht.isEmpty();
				if(empty)
				{
					ht.add(np = new HoloPoint(p.x,p.y,p.z,date,true));
					tk.addTraj(ht);
					tk.update();
				}else
					ht.add(np = new HoloPoint(p.x,p.y,p.z,date,editable));
				
				tk.setRecording(true);
				
				sendQ(keyOut+"/track/"+tk.getNumber()+"/xyz",new Object[]{np.x,np.y,np.z});
			}
			
		}
		sendBundle();
	}
	
	public void treatRecordSegmentAll()
	{	
		if(!(recording && playing))
			return;

		for(int i = 0 ; i < holoEditRef.gestionPistes.getNbTracks() ; i++)
		{
			HoloTrack tk = holoEditRef.gestionPistes.getTrack(i);
			if(tk.isRecEnable())
			{
				HoloTraj ht = new HoloTraj(), lht = recTrajs.get(i);
				if(!lht.isEmpty())
				{
					lht.lastElement().setEditable(true);
					//tk.finalizeRecord();
					tk.setRecording(false);

				}
				ht.setRecording(true);
				recTrajs.set(i, ht);
			}
		}
		
	}
	
	public void finalizeRecordAll()
	{
		if(!recording)
			return;
		for(int i = 0 ; i < holoEditRef.gestionPistes.getNbTracks() ; i++)
		{
			HoloTrack tk = holoEditRef.gestionPistes.getTrack(i);
			if(tk.isRecEnable())
				tk.finalizeRecord();
		}
		
		holoEditRef.gestionPistes.preparePlay();
	}
	
	protected void send(String key, String msg) {
		if (open)
			try
			{
				if(sender != null)
					sender.send(new OSCMessage(key,new Object[]{msg}));
				if(VERBOSE_OUT)
					System.out.println("OSCOUT : "+key+" "+msg);
			} catch (IOException e) {}
	}
	
	public void send(String key, Object[] msg) {
		if (open)
			try
			{
				if(sender != null)
					sender.send(new OSCMessage(key,msg));
				if(VERBOSE_OUT){
					System.out.print("OSCOUT : "+key);
					for(Object a:msg)
						System.out.print(" "+a.toString());
					System.out.println();
				}
				holoEditRef.transport.setConnectionStatus("@"+address+":"+out, true);
			} catch (IOException e) {
				System.err.println("send io exception");
				holoEditRef.transport.setConnectionStatus(" -- ", false);
			}
	}
	
	public void send(String msg) {
		if (open)
			send(keyOut,msg);
	}
	
	public void initBundle()
	{
		bundle = new OSCBundle(com.illposed.osc.OSCBundle.TIMESTAMP_IMMEDIATE);
	}
	
	public void sendQ(String key, Object[] msg)
	{
		bundle.addPacket(new OSCMessage(key,msg));
		
		if(VERBOSE_OUT){
			System.out.print("OSCOUT : "+key);
			for(Object a:msg)
				System.out.print(" "+a.toString());
			System.out.println();
		}
	}
	
	public void sendBundle()
	{
		if (open)
			try
			{
				if(sender != null)
					sender.send(bundle);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void acceptMessage(Date msgDate, OSCMessage msg) {
		if (open)
			treatPacket(msgDate,msg.getAddress(),msg.getArguments());	
	}

	public int getIn() {
		return in;
	}

	public void setIn(int in) {
		if(this.in != in)
		{
			this.in = in;
			newReceiver();
		}
	}

	public int getOut()	{
		return out;
	}

	public void setOut(int out)
	{
		if(this.out != out)
		{
			this.out = out;
			newSender();
		}
	}
	
	public void setAddress(String a)
	{
		if(!this.address.equalsIgnoreCase(a))
		{
			try
			{
				InetAddress.getByName(address);
				address = a;
			} catch (UnknownHostException e)
			{
				address = "localhost";
				holoEditRef.transport.setConnectionStatus(" -- ", false);
				System.err.println(e.getMessage());
			}
			newSender();
		}
	}

	public void setKeyIn(String k)
	{
		if(!this.keyIn.equalsIgnoreCase(k))
		{
			keyIn = k;
			newReceiver();
		}
	}
	
	public String getKeyIn()
	{
		return keyIn;
	}
	
	public void setKeyOut(String k)
	{
		if(!this.keyOut.equalsIgnoreCase(k))
		{
			keyOut = k;
			newSender();
		}
	}
	
	public String getKeyOut()
	{
		return keyOut;
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
				holoEditRef.score.cursorTime = (int)((float)time / 10);
				holoEditRef.score.display();
				if(p)
					play();
			}
		} catch (NumberFormatException e) {}
	}
	
	public void setTotalTime(int msg)
	{
		try {
			int time = msg;
			if(time >= 0)
			{
				total = Ut.max(time,holoEditRef.counterPanel.getDate(3));
				holoEditRef.counterPanel.setCompteur2(3,total);
				holoEditRef.score.display();
			}
		} catch (NumberFormatException e) {}
	}
	
	public int getTotalTime()
	{
		return total;
	}
	
	public void setBegin(int b)
	{
		beg = b;
	}
	
	public void setBegTime(int msg)
	{
		try {
			int time = msg;
			if(time >= 0)
			{
				boolean p = playing;
				if(p)
					stop();
				holoEditRef.counterPanel.setCompteur2(1,time);
				holoEditRef.room.display();
				if(p)
					play();
			}
		} catch (NumberFormatException e) {
		}
	}
	
	public void setEnd(int e)
	{
		end = e;
	}
	
	public void setEndTime(int msg)
	{
		try {
			int time = msg;
			if(time >= 0)
			{
				boolean p = playing;
				if(p)
					stop();
				holoEditRef.counterPanel.setCompteur2(2,time);
				holoEditRef.room.display();
				if(p)
					play();
			}
		} catch (NumberFormatException e) {}
	}
	
	public int getCurrentTime()
	{
		return counter;
	}
	
	public void setCurrentTime(int c)
	{
		counter = c;
	}
	
	public void sendBegin()
	{
		if(open)
			send(keyOut+"/transport/begin",new Object[]{holoEditRef.counterPanel.getDate(1)});
	}
	
	public void sendEnd()
	{
		if(open)
			send(keyOut+"/transport/end",new Object[]{holoEditRef.counterPanel.getDate(2)});
	}
	
	public void sendTotal()
	{
		if(open)
			send(keyOut+"/transport/total",new Object[]{holoEditRef.counterPanel.getDate(3)});
	}
	
	public void setLoop(int msg)
	{
		try {
			int l = msg;
			loop(l > 0);
		} catch (NumberFormatException e) {}
	}
	
	public void setLineMode(boolean b)
	{
		linemode = b;
	}
	
	public boolean getLineMode()
	{
		return linemode;
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public boolean isPlaying()
	{
		return playing;
	}
	
	public boolean isRecording()
	{
		return recording;
	}
	
	public boolean getAutostop()
	{
		return autostop;
	}
	
	public void setAutostop(boolean a)
	{
		autostop = a;
	}
	
	public void record(boolean b)
	{
		if(open)
		{
			if(!b)
			{
				// stop recording
				for(int i = 0 ; i < recTrajs.size() ; i++)
				{
					HoloTraj ht = recTrajs.get(i);
					ht.remove(0);
				}
				for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
					tk.update();
					
				treatRecordSegmentAll();
				finalizeRecordAll();
				
				recording = false;
				holoEditRef.transport.rec.setIcon(holoEditRef.transport.recOffState);
				if(order != ORDER_RECORD)
					send(keyOut+"/transport/record",new Object[]{0});
				else order = ORDER_NONE;
			} else {
				// start recording
				holoEditRef.gestionPistes.StoreToUndoAll();
				holoEditRef.modify();
				recTrajs = new Vector<HoloTraj>();
				if(playing)
				{	
					recBegDate = counter;
				} else {
					recBegDate = holoEditRef.score.cursorTime;
				}	
				for(int i = 0 ; i < holoEditRef.gestionPistes.getNbTracks() ; i++)
				{
					HoloTraj ht = new HoloTraj();
					currentTrack = holoEditRef.gestionPistes.getTrack(i);
					//ht.add(new HoloPoint(0,0,0,recBegDate,true));
					ht.setRecording(true);
					recTrajs.add(ht);
					/*if(currentTrack.isVisible() && !currentTrack.isLocked() && currentTrack.isRecEnable())
					{
						currentTrack.addTraj(ht);
						currentTrack.update();
					}*/
				}
				recording = true;
				holoEditRef.transport.rec.setIcon(holoEditRef.transport.recOnState);
				if(order != ORDER_RECORD)
					send(keyOut+"/transport/record",new Object[]{1});
				else order = ORDER_NONE;
			}
		}
	}
	
	public void play()
	{
		//if(open && !playing)
		if(!playing)
		{
			playing = true;
			runner = new Thread(this);
			runner.setName("RT-player");
			runner.setPriority(Thread.MAX_PRIORITY);
			// PRELOAD
			preload();
			
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
			}
			position();
			sendTotal();
			for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
			{
				if (tk.waves.isEmpty())
					total = Ut.max(total, tk.getLastDate());
				else
					total = Ut.max(total, Ut.max(tk.getLastDate(), tk.waves.lastElement().getLastDate()));
			}
			if(order != ORDER_PLAY)
				send(keyOut+"/transport/play",new Object[]{"bang"});
			else 
				order = ORDER_NONE;
			autostop = false;
			firstCue = true;
			loopNum = 0;
			counter = holoEditRef.score.cursorTime;
			looping = !firstCue;
			oversleep = 0;
			baseDate = new Date().getTime()-counter;
			
			holoEditRef.transport.setPlay(true);
			
			runner.start();
			if (!holoEditRef.rtDisplay.isAnimating())
				holoEditRef.rtDisplay.start();

		}
	}
	
	public void stop()
	{
		for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
			tk.stop(true);
//		if(open && (playing || paused || recording))
		if(playing || paused || recording)
		{
			if(runner != null)
				runner.interrupt();
			if(recording)
				record(false);
			if(order != ORDER_STOP)
				send(keyOut+"/transport/stop",new Object[]{"bang"});
			else order = ORDER_NONE;
			recording = false;
			playing = false;
			paused = false;
			if(holoEditRef.rtDisplay.isAnimating())
				holoEditRef.rtDisplay.stop();
			
			
			holoEditRef.transport.setPlay(false);
			
			send(keyOut+"/track/*/cue",new Object[]{0});
		}
		holoEditRef.room.display();
	}
	
	public void stopAndPlay()
	{
		// STOP
		runner.interrupt();
		send(keyOut+"/transport/jump",new Object[]{"bang"});
		send(keyOut+"/track/*/cue",new Object[]{0});
		runner = new Thread(this);
		runner.setName("RT-player");
		runner.setPriority(Thread.MAX_PRIORITY);
		// PRELOAD
		preload();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e) {}
		holoEditRef.transport.start.setIcon(holoEditRef.transport.playState);
		// PLAY
		for (HoloTrack tk : holoEditRef.gestionPistes.tracks)
		{
			if (tk.waves.isEmpty())
				total = Ut.max(total, tk.getLastDate());
			else
				total = Ut.max(total, Ut.max(tk.getLastDate(), tk.waves.lastElement().getLastDate()));
		}
		firstCue = true;
		counter = holoEditRef.score.cursorTime;
		looping = !firstCue;
		loopNum = 0;
		oversleep = 0;
		baseDate = new Date().getTime()-counter;
		runner.start();
	}
	
	public void pause()
	{
	//	if(open)
	//	{
			if(paused)
			{
				if(order != ORDER_RESUME)
					send(keyOut+"/transport/resume",new Object[]{"bang"});
				else order = ORDER_NONE;
				holoEditRef.transport.resume.setIcon(holoEditRef.transport.resumeState);
				paused = false;
				baseDate = new Date().getTime()-counter;
			} else {
				if(order != ORDER_PAUSE)
					send(keyOut+"/transport/pause",new Object[]{"bang"});
				else order = ORDER_NONE;
				holoEditRef.transport.resume.setIcon(holoEditRef.transport.pauseState);
				paused = true;
			}
	//	}
	}
	
	public void preload()
	{
		int tkNum;
		int tkNb = holoEditRef.gestionPistes.getNbTracks();
		preloading = true;
		beg = holoEditRef.counterPanel.getDate(1);
		end = holoEditRef.counterPanel.getDate(2);
		if(open)
		{
			try{
				sendPaths();
				sendVisible();
				 
					Thread.sleep(10);
				
				for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
				{
					tkNum = tk.getNumber();
					for(int i = 1 ; i <= tk.waves.size() ; i++)
						//tk.waves.get(i-1).preload(tk,100+(i*tkNb)+tkNum,mainRef.score.cursorTime, beg,end);
						preloadWaveForm(tk.waves.get(i-1),tk,CUE_OFFSET+(i*tkNb)+tkNum,holoEditRef.score.cursorTime, beg,end);
				}
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		holoEditRef.gestionPistes.preparePlay();
		preloading = false;
	}
	
	public void preloadFirst()
	{
		int tkNum;
		int tkNb = holoEditRef.gestionPistes.getNbTracks();
		preloading = true;
		beg = holoEditRef.counterPanel.getDate(1);
		end = holoEditRef.counterPanel.getDate(2);
		if(open)
		{
			try{
				sendPaths();
				sendVisible();
				 
					Thread.sleep(100);
				
				for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
				{
					tkNum = tk.getNumber();
					for(int i = 1 ; i <= tk.waves.size() ; i++)
						preloadWaveForm(tk.waves.get(i-1),tk,CUE_OFFSET+(i*tkNb)+tkNum,holoEditRef.score.cursorTime, beg,end);
				}
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		holoEditRef.gestionPistes.preparePlay();
		preloading = false;
	}
	
	public void preloadWaveForm(WaveFormInstance w,HoloTrack tk, int cueNb, int cursorTime, int begL, int endL)
	{
		HoloWaveForm hwf = w.getWave();

		if(hwf == null)
			w.update();
		if(hwf == null)
			return;
		
		String hwfName;
		if(preloadAbsolute)
			hwfName = hwf.getCompletePathWoQuote();
		else
			hwfName = hwf.getStripPathWoQuote();
		
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
				send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.CURSOR_CUE_LOOP, hwfName,(cursorTime-w.getFirstDate()),(w.endLoop-w.getFirstDate())});
				w.loopDefined = w.LOOP_END_CUE;
			}
				send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.CURSOR_CUE, hwfName,(cursorTime-w.getFirstDate())});
		}
		if(Ut.between(w.getFirstDate(),w.begLoop,w.endLoop) && !Ut.between(w.getLastDate(),w.begLoop,w.endLoop))
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.LOOP_END_CUE, hwfName,0,(w.endLoop-w.getFirstDate())});
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.LOOP_AFTER_CUE, hwfName,(w.endLoop-w.getFirstDate())});
			w.loopDefined = w.LOOP_END_CUE;
		} else if(!Ut.between(w.getFirstDate(),w.begLoop,w.endLoop) && Ut.between(w.getLastDate(),w.begLoop,w.endLoop))
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.LOOP_BEGIN_CUE, hwfName,(w.begLoop-w.getFirstDate())});
			w.loopDefined = w.LOOP_BEGIN_CUE;
		} else if(w.getFirstDate() < w.begLoop && w.getLastDate() > w.endLoop)
		{
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.LOOP_IN_CUE, hwfName,(w.begLoop-w.getFirstDate()),(w.endLoop-w.getFirstDate())});
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueOffset+w.LOOP_AFTER_CUE, hwfName,(w.endLoop-w.getFirstDate())  });
			w.loopDefined = w.LOOP_IN_CUE;
		}
		
			send(keyOut+"/track/"+tkNb+"/preload",new Object[]{cueNb, hwfName});
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
					send(keyOut+"/track/"+tkNb+"/cue",new Object[]{cueOffset+w.CURSOR_CUE_LOOP});
					return;
				}
				send(keyOut+"/track/"+tkNb+"/cue", new Object[]{cueOffset+w.CURSOR_CUE});
				return;
			} else if(loop) {
				if(time == w.begLoop && looping) {
					if(w.loopDefined == w.LOOP_BEGIN_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", new Object[]{cueOffset+w.LOOP_BEGIN_CUE});
						return;
					}
					else if(w.loopDefined == w.LOOP_IN_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", new Object[]{cueOffset+w.LOOP_IN_CUE});
						return;
					}
					else if(time == w.begTime)
					{
						send(keyOut+"/track/"+tkNb+"/cue", new Object[]{w.cue});
						return;
					}
				} else if(time == w.begTime) {
					if(w.loopDefined == w.LOOP_END_CUE)
					{
						send(keyOut+"/track/"+tkNb+"/cue", new Object[]{cueOffset+w.LOOP_END_CUE});
						return;
					}
					send(keyOut+"/track/"+tkNb+"/cue", new Object[]{w.cue});
					return;
				}
			} else if(time == w.begTime) {
				w.loopDefined = -1;
				send(keyOut+"/track/"+tkNb+"/cue", new Object[]{w.cue});
				return;
			} else if(time == w.endLoop && (w.loopDefined == w.LOOP_END_CUE || w.loopDefined == w.LOOP_IN_CUE))
			{
				send(keyOut+"/track/"+tkNb+"/cue", new Object[]{cueOffset+w.LOOP_AFTER_CUE});
				return;
			}
		}
		return;
	}
	
	public void spatUpdate()
	{
		//String tmp = "tks "+mainRef.gestionPistes.getNbTracks();
		templist.clear();
		
		send(keyOut+"/config/tracknum",new Object[]{holoEditRef.gestionPistes.getNbTracks()});
		
		for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
			templist.add(tk.getNumber());
		send(keyOut+"/config/tracklist",templist.toArray());
			//tmp += " " + tk.getNumber();

		for(HoloTrack tk:holoEditRef.gestionPistes.tracks)
		{
			Color c = tk.getColor();
			String na = tk.getName();
			/*if(na.indexOf(' ') != -1)
				na = "\""+na+"\"";*/
			send(keyOut+"/track/"+tk.getNumber()+"/name",new Object[]{na});
			send(keyOut+"/track/"+tk.getNumber()+"/color",new Object[]{c.getRed(),c.getGreen(),c.getBlue()});
			//send("tkup "+tk.getNumber()+" "+na+" "+c.getRed()+" "+c.getGreen()+" "+c.getBlue());
		}
		position();
		preloadFirst();
	}
	
	public void loop(boolean l)
	{
		loop = l;
//		if(open)
	//	{
			send(keyOut+"/transport/loop",new Object[]{(loop ? 1 : 0)});
			if (loop)
				holoEditRef.transport.loop.setIcon(holoEditRef.transport.loopOnState);
			else
				holoEditRef.transport.loop.setIcon(holoEditRef.transport.loopOffState);

	//	}
	}
	
	public boolean getLoop()
	{
		return loop;
	}
	
	public boolean changeLoop()
	{
		loop(!loop);
		return getLoop();
	}

	public void playstop()
	{
	//	if(open)
	//	{
			if(playing || autostop) stop();
			else play();
	//	}
	}

	public void position()
	{
		HoloPoint p;
		counter = holoEditRef.score.cursorTime;
		send(keyOut+"/transport/time",new Object[]{counter});
		for(int i = 0, last = holoEditRef.gestionPistes.getNbTracks() ; i < last ; i++)
		{
			currentTrack = holoEditRef.gestionPistes.getTrack(i);
			currentTrack.stop(true);
			if(currentTrack.isVisible())
			{
				if((p = currentTrack.getPointPlay(counter)) != null)
					send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Object[]{p.x,p.y,p.z});
				/*String vc = currentTrack.getLastValueAt(counter);
				if(!vc.equalsIgnoreCase("none"))
					send(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Object[]{vc});*/
			}
		}
		holoEditRef.room.display();
	}
	
	public void sendVisible()
	{
		for(HoloTrack t:holoEditRef.gestionPistes.tracks)
		{
			send(keyOut+"/track/"+t.getNumber()+"/visible",new Object[]{(t.isVisible() ? 1 : 0)});
			send(keyOut+"/track/"+t.getNumber()+"/active",new Object[]{(t.isVisible() ? 1 : 0)});
			send(keyOut+"/recorder/"+t.getNumber()+"/enable",new Object[]{(t.isRecEnable() ? 1 : 0)});
		}
		
	}
	
	public void sendRecord(int tknum)
	{
		HoloTrack t = holoEditRef.gestionPistes.getTrack(tknum);
		
		send(keyOut+"/recorder/"+t.getNumber()+"/enable",new Object[]{(t.isRecEnable() ? 1 : 0)});
		
	}

	public void setPreloadAbs(boolean v)
	{
		preloadAbsolute = v;
	}
	
	public boolean getPreloadAbs()
	{
		return preloadAbsolute;
	}
	
	public void sendPaths()
	{
		Vector<String> paths = new Vector<String>();
		for(HoloWaveForm hwf:holoEditRef.gestionPistes.soundPool.getSounds())
		{	
			String p = hwf.getPathWoQuote();
			if(!paths.contains(p))
				paths.add(p);
		}
		send(keyOut+"/soundpool/paths",new Object[]{paths.size()});
		for(int i = 0 ; i < paths.size() ; i++)
			send(keyOut+"/soundpool/path",new Object[]{i,paths.get(i)});
		
		paths.clear();
		
		for(HoloWaveForm hwf:holoEditRef.gestionPistes.soundPool.getSounds())
		{	
			String p = hwf.getCompletePathWoQuote();
			if(!paths.contains(p))
				paths.add(p);
		}
		
		for(int i = 0 ; i < paths.size() ; i++)
			send(keyOut+"/soundpool/abspath",new Object[]{i,paths.get(i)});
		
		
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public void run()
	{
		// si modif, modifier �galement HoloPlayer
		try
		{
			while(playing)
			{
				HoloPoint p,pn;
				initBundle();
				while(preloading)
				{
					System.out.println("preloading...");
					Thread.sleep(10);
				}
				if(!paused)
				{
					oldDate = new Date().getTime();
					if(loop && counter >= end+1)
					{
						finalizeRecordAll();
						sendQ(keyOut+"/transport/looping",new Object[]{"bang"});
						counter = beg;
						looping = true;
						loopNum++;
						baseDate = new Date().getTime()-counter;
					} else if(!loop && counter > total && !recording){
						sendQ(keyOut+"/transport/stop",new Object[]{"bang"});
						playing = false;
						paused = false;
						autostop = true;
					}else if (!loop && counter >= total && recording)
					{
						setTotalTime(total+5000); // add 5 sec to total time when recording
						//Ut.print("total "+total);
						end = total;
						holoEditRef.counterPanel.setCompteur2(2,total);
					}
					if(counter%10 == 0)
						sendQ(keyOut+"/transport/time",new Object[]{counter});
					
					for(int i = 0, last = holoEditRef.gestionPistes.getNbTracks() ; i < last ; i++)
					{
						currentTrack = holoEditRef.gestionPistes.getTrack(i);
						if(currentTrack.isVisible())
						{
							if(!recording || !currentTrack.isRecEnable() || !currentTrack.isRecording())
							{
								if((p = currentTrack.getPointPlay(counter)) != null)
								{	
									if(linemode) // line mode
									{
										if((pn = p.nextpoint) != null)
											sendQ(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Object[]{p.x,p.y,p.z,pn.date-p.date,pn.x,pn.y,pn.z});
										else
											sendQ(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Object[]{p.x,p.y,p.z,0,p.x,p.y,p.z});
									}else sendQ(keyOut+"/track/"+currentTrack.getNumber()+"/xyz",new Object[]{p.x,p.y,p.z});
								}
								
							}
							for(WaveFormInstance w : currentTrack.waves)
							{	
								playWaveForm(w,currentTrack, counter, firstCue, loop, looping);
							}
						}
					}
					firstCue = false;
					looping = false;
					
					sendBundle();
					
					date = new Date().getTime();
					long delta = (long)(counter - date + baseDate);
					
					delta++;
					
					//Ut.print("delta "+delta);
					
					if(delta > 0)
					{
						Thread.sleep(delta);
					}
						
					counter ++;
					
				}else
				{
					Thread.sleep(1);
				}
			}
		} catch (InterruptedException e) {}
		holoEditRef.transport.start.setIcon(holoEditRef.transport.stopState);
	}
	
	public String toString()
	{
		return "\t<osc portIn=\"" + holoEditRef.connection.getIn() + "\"" + " portOut=\"" + holoEditRef.connection.getOut() + "\""
		+ " address=\"" + holoEditRef.connection.getAddress() + "\"" + " open=\"" + holoEditRef.connection.isOpen() + "\""
		+ " keyIn=\"" + keyIn + "\"" + " keyOut=\"" + keyOut + "\"" + " linemode=\"" + holoEditRef.connection.getLineMode() + "\"" 
		+ " preloadAbs=\"" + holoEditRef.connection.getPreloadAbs() + "\"" + "/>\n";		
	}

	public void sendSpeakers()
	{
		HoloSpeaker sp;
		for(int i = 0 ; i < holoEditRef.gestionPistes.speakers.size() ; i++)
		{
			sp = holoEditRef.gestionPistes.speakers.get(i);
			send(keyOut+"/speaker/"+sp.num+"/xyz",new Object[]{sp.X,sp.Y,sp.Z});
		}
	}
	
	private void clearSpeakers(int num) 
	{
		HoloSpeaker sp;
		if(num <= 0)
		{
			holoEditRef.gestionPistes.speakers.clear();
			holoEditRef.room.display();
		}
		else
		{
			for(int i = 0 ; i < holoEditRef.gestionPistes.speakers.size() ; i++)
			{
				sp = holoEditRef.gestionPistes.speakers.get(i);
				if(sp.num == num)
				{
					holoEditRef.gestionPistes.speakers.remove(i);
					holoEditRef.room.display();
					return;
				}
			}
		}
		
	}
	
	public void oscAddressChanged(EventObject event, String newAdress){
		setAddress(newAdress);
	}	
	public void oscPortOutChanged(EventObject event, int newOscPortOut){
		setOut(newOscPortOut);
	}
	public void connectToService(EventObject event, String serviceName){
//		if (holoEditRef.bonjour){
//			try {
//				BonjourConnection bonjourConnection =  (BonjourConnection) holoEditRef.connection;
//				DNSSD.resolve(0, 0, serviceName, bonjourConnection.registeringService 
//						, bonjourConnection.spatmap.get(serviceName) , bonjourConnection);
//				
//			} catch (DNSSDException e) {
//				System.err.println("OSCConnection : DNSSDException while trying to register.");
//				e.printStackTrace();
//			}
//		}
	}	
	
	public void disconnectCalled(EventObject event){
		close();
	}
	
	public float oscToFloat(Object o)
	{
		if(o.getClass().equals(Float.class))
			return (Float)o;
		else if(o.getClass().equals(Integer.class))
			return (float)((Integer)o);		
		else
		{
			Ut.print("OSC in : error parsing Float");
			return 0.f;
		}
			
	}
	
	public int oscToInt(Object o)
	{
		if(o.getClass().equals(Integer.class))
			return (Integer)o;
		else if(o.getClass().equals(Float.class))
			return ((Float)o).intValue();		
		
		Ut.print("OSC in : error parsing Int");
		return 0;
	}
	
}