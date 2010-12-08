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
package holoedit.fileio;

import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class TjFileWriter implements Runnable
{
	private String filename;
	private int max = 0;
	private GestionPistes gp;
	private Thread runner;
	private ProgressBar pb;
	private int tkNum;
	private int tjNum;
	private boolean showpb=true;

	public TjFileWriter(GestionPistes _gp, String fi, int _tk, int _seq)
	{
		gp = _gp;
		filename = fi;
		tkNum = _tk;
		tjNum = _seq;
		max = gp.tracks.get(tkNum).getHoloTraj(tjNum).size();
		
		pb = new ProgressBar("Exporting...");
		pb.setMaximum(max);
		pb.open();
		
		runner = new Thread(this);
		runner.setName("tj-writer");
		runner.start();
		try
		{
			while(runner.isAlive())
			{
//				System.out.println("tj-writer-waiting");
				pb.repaint();
					Thread.sleep(100);
			}
		}
		catch (InterruptedException e) {}
	}
	
	public void run()
	{
		try
		{
			Ut.print("___________________________________________");
			Ut.print(" o Writing file : " + filename + " in progress...");
			Ut.print("...");
			if(showpb)
				pb.setValue(0);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)),"ISO-8859-1"));
			out.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>\n");
			out.write("<tjplayer version=\"" + Ut.version +"\">\n");
			HoloTrack tk = gp.tracks.get(tkNum);
			HoloTraj ht = tk.getHoloTraj(tjNum).dupliquer();
			ht.shiftDates(-ht.getFirstDate());
			out.write(ht.toTjFile());
			if(showpb)
				pb.inc(gp.tracks.get(tkNum).getHoloTraj(tjNum).size());
			
			out.write("</tjplayer>\n");
			out.close();
			if(showpb)
				pb.dispose();
			Ut.print(" v File was successfully written");
			Ut.print("___________________________________________");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public TjFileWriter(GestionPistes _gp, String fi, int _tk, int _seq,boolean showpb)
	{
		gp = _gp;
		filename = fi;
		tkNum = _tk;
		tjNum = _seq;
		max = gp.tracks.get(tkNum).getHoloTraj(tjNum).size();
		this.showpb = showpb;
		if(showpb)
		{
			pb = new ProgressBar("Exporting...");
			pb.setMaximum(max);
			pb.open();
		}
		
		runner = new Thread(this);
		runner.setName("tj-writer");
		runner.start();
		try {
			runner.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
