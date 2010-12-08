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

import holoedit.data.HoloSpeaker;
import holoedit.data.HoloTrack;
import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class HoloFileWriter implements Runnable
{
	private String filename;
	private int max = 0;
	private GestionPistes gp;
	private Thread runner;
	private ProgressBar pb;

	public HoloFileWriter(GestionPistes _gp, String fi)
	{
		gp = _gp;
		filename = fi;
		for (HoloTrack htk:gp.tracks)
			max += htk.size();
		max += gp.soundPool.size();
		max += gp.externalDataPool.size();
		max += gp.speakers.size();
		
		pb = new ProgressBar("Saving...");
		pb.setMaximum(max);
		pb.open();
		
		runner = new Thread(this);
		runner.setName("holo-writer");
		runner.start();
		try
		{
			while(runner.isAlive())
			{
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
			pb.setValue(0);
			File f = new File(filename);
			String parentPath = f.getParent();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"ISO-8859-1"));
			out.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>\n");
			out.write("<session version=\"" + Ut.version + "\" info=\""+max+"\">\n");
			out.write(gp.soundPool.toString(parentPath));
			pb.inc(gp.soundPool.size());
			out.write(gp.externalDataPool.toString(parentPath));
			pb.inc(gp.externalDataPool.size());
			for (HoloTrack htk:gp.tracks)
			{
				out.write(htk.toString());
				pb.inc(htk.size());
			}
			for (HoloSpeaker sp : gp.speakers)
			{
				out.write(sp.toString());
				pb.inc();
			}
			out.write("</session>");
			out.close();
			pb.dispose();
			Ut.print(" v File was successfully written");
			Ut.print("___________________________________________");
			if(gp.holoEditRef != null)
				gp.holoEditRef.setSaved(true);
			else
			{
				gp.hpRef.setSaved(true);
				gp.hpRef.done();
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			pb.dispose();
		}
	}
}
