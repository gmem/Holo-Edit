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

import holoedit.data.HoloPoint;
import holoedit.data.HoloTrack;
import holoedit.data.HoloTraj;
import holoedit.data.HoloTrajVector;
import holoedit.gui.GestionPistes;
import holoedit.util.Ut;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TkFileReader extends DefaultHandler implements Runnable
{
	private String filename;
	private GestionPistes gp;
	private Thread runner;
	private HoloTrack currentTrack;
	private HoloTraj currentTraj;
	private HoloTrajVector currentVTraj;
	private boolean done;
	private boolean error = false;

	public TkFileReader(GestionPistes _gp, String _f, int toTkNum, int atTime)
	{
		gp = _gp;
		filename = _f;
		done = false;
		error = false;
		if(toTkNum == -1)
			toTkNum = gp.addTrack();
		runner = new Thread(this);
		runner.setName("tk-reader");
		runner.start();
		try {
			while (!done)
			{
//				System.out.println("tk-reader-waiting");
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {}
		if(error)
		{
			Ut.alert("Error!","Error while loading the file, aborted.");
		} else {
			gp.updateTrackSelector(-3);
			gp.selectTrack(toTkNum);
			HoloTrack tk = gp.getTrack(toTkNum);
			for(HoloTraj ht:currentTrack.trajs)
			{
				tk.addTraj(ht,ht.getFirstDate()+atTime);
			}
			tk.update();
			gp.holoEditRef.counterPanel.setCompteur(1, atTime);
			gp.holoEditRef.counterPanel.setCompteur(2, currentTrack.getLastDate());
			gp.holoEditRef.score.zoomAll();
			gp.holoEditRef.modify();
		}
		gp.holoEditRef.room.display();
	}

	public void run()
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser saxParser = factory.newSAXParser();
			File f = new File(filename);
			saxParser.parse(f, this);
		}
		catch (Throwable t)
		{
			error = true;
			t.printStackTrace();
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
		// <track>
		if (qName.equalsIgnoreCase("track"))
		{
			currentTrack = new HoloTrack(new Integer(attrs.getValue("number")), attrs.getValue("name"), new Boolean(attrs.getValue("visible")), new Boolean(attrs.getValue("locked")), new Integer(attrs.getValue("color")));
			currentVTraj = new HoloTrajVector(5,1);
			// <traj>
		} else if (qName.equalsIgnoreCase("traj"))
			currentTraj = new HoloTraj(new Integer(attrs.getValue("begin_number")));
			// <point>
		else if (qName.equalsIgnoreCase("point"))
			currentTraj.add(new HoloPoint(new Float(attrs.getValue("x")), new Float(attrs.getValue("y")), new Float(attrs.getValue("z")), new Integer(attrs.getValue("date")), new Boolean(attrs.getValue("edit"))));
	}

	public void endElement(String uri, String localName, String qName)
	{
		// </traj>
		if (qName.equalsIgnoreCase("traj"))
			currentVTraj.add(currentTraj);
		// </track>
		else if (qName.equalsIgnoreCase("track"))
			currentTrack.setTrajs(currentVTraj);
	}
}
