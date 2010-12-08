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

import holoedit.gui.GestionPistes;
import holoedit.gui.ProgressBar;
import holoedit.util.Ut;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class TkFileWriter implements Runnable
{
	private String filename;
	private int max = 0;
	private GestionPistes gp;
	private Thread runner;
	private ProgressBar pb;
	private int tkNum;

	public TkFileWriter(GestionPistes _gp, String fi, int _tk)
	{
		gp = _gp;
		filename = fi;
		tkNum = _tk;
		max = gp.tracks.get(tkNum).size();
		
		pb = new ProgressBar("Exporting...");
		pb.setMaximum(max);
		pb.open();
		
		runner = new Thread(this);
		runner.setName("tk-writer");
		runner.start();
		try
		{
			while(runner.isAlive())
			{
				pb.repaint();
//				System.out.println("tk-writer-waiting");
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
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)),"ISO-8859-1"));
			out.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>\n");
			out.write(gp.tracks.get(tkNum).toString());
			pb.inc(gp.tracks.get(tkNum).size());
			out.close();
			pb.dispose();
			Ut.print(" v File was successfully written");
			Ut.print("___________________________________________");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
