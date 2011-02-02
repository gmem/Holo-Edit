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

import holoedit.HoloEdit;
import holoedit.data.HoloRecentFile;
import holoedit.data.HoloSpeaker;
import holoedit.gui.LabelAudio;
import holoedit.util.Ut;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class HoloSettings extends DefaultHandler
{
	// BOOLEEN POUR TRAVAILLER AVEC OU SANS SAUVEGARDE DES SETTINGS
	final public static boolean SETTINGS = true;
	private double fileversion = 0;
	private HoloEdit holoEditRef;
	// Nom du fichier de configuration
	public String fileSettings = "HoloSettings.cfg";
	private HoloRecentFile lastOne;
	private int fctCpt;
	public String focus = "room";

	public HoloSettings(HoloEdit owner)
	{
		holoEditRef = owner;
	}

	public void save()
	{
		if (SETTINGS)
			try
			{
				new File(fileSettings).renameTo(new File(fileSettings.concat("~")));
				File f = new File(fileSettings);
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>\n");
				out.write("<settings version=\"" + Ut.version + "\">\n");
				// OPTIONS SETTINGS
				out.write("\t<options " + "maxRecentFiles=\"" + holoEditRef.maxRecentFiles + "\"" + " allTrackActive=\"" + holoEditRef.allTrackActive + "\""
						+ " viewOnlyEditablePoints=\"" + holoEditRef.viewOnlyEditablePoints + "\"" + " viewSpeakers=\"" + holoEditRef.viewSpeakers + "\""
						+ " openLastOnLoad=\"" + holoEditRef.openLastOnLoad + "\"" + " drawPtsNb=\"" + Ut.drawPtsNb + "\""
						+ " coordinates=\"" + holoEditRef.coordinates + "\"" + " laf=\"" + holoEditRef.laf + "\""
						+ " shortView=\"" + holoEditRef.shortViewMode + "\""
						+ " focus=\"" + (holoEditRef.room.hasFocus() ? "room" : (holoEditRef.score.hasFocus() ? "score" : (holoEditRef.timeEditor.hasFocus() ? "time" : (holoEditRef.room3d.hasFocus() ? "room3d" : "room")))) + "\""
						+ " bonjour=\"" + holoEditRef.bonjour + "\""
						+ " scrollspeed=\"" + holoEditRef.scrollSpeed + "\""
						+ " smooth=\"" + HoloEdit.SMOOTH + "\""
						+ " sdifExpert=\"" + holoEditRef.sdifExpert + "\""
						+ " lastScriptFile=\"" + holoEditRef.lastScriptFile + "\""
						+ "/>\n");
				// OSC SETTINGS
				out.write(holoEditRef.connection.toString());
				// DIRECTORIES
				out.write("\t<holo dir=\"" + holoEditRef.gestionPistes.holoDirectory + "\"/>\n");
				out.write("\t<midi dir=\"" + holoEditRef.gestionPistes.midiDirectory + "\"/>\n");
				out.write("\t<midi7 dir=\"" + holoEditRef.gestionPistes.midi7Directory + "\"/>\n");
				out.write("\t<txt dir=\"" + holoEditRef.gestionPistes.textDirectory + "\"/>\n");
				out.write("\t<seq dir=\"" + holoEditRef.gestionPistes.seqDirectory + "\"/>\n");
				out.write("\t<snd dir=\"" + holoEditRef.gestionPistes.sndDirectory + "\"/>\n");
				out.write("\t<dt dir=\"" + holoEditRef.gestionPistes.dtDirectory + "\"/>\n");
				out.write("\t<script dir=\"" + 	holoedit.functions.GroovyWindow.scriptDirectory + "\"/>\n");
				out.write("\t<tk dir=\"" + holoEditRef.gestionPistes.tkDirectory + "\"/>\n");
				out.write("\t<tj dir=\"" + holoEditRef.gestionPistes.tjDirectory + "\"/>\n");
				// LABELS AUDIO
				for (LabelAudio lab : holoEditRef.gestionPistes.ts.labelAudio)
					out.write(lab.toString());
				// RECENT FILES
				// last
				out.write((holoEditRef.last != null ? holoEditRef.last.toLastString() : ""));
				int cpt = 1;
				// recent
				for (HoloRecentFile h : holoEditRef.recentFiles)
				{
					if (cpt++ > holoEditRef.maxRecentFiles)
						break;
					out.write(h.toString());
				}
				// HPs
				for (HoloSpeaker sp : holoEditRef.gestionPistes.speakers)
					out.write(sp.toString());
				// WINDOWS SIZE & LOCATION
				out.write(holoEditRef.gestionPistes.ts.toString());
				out.write(holoEditRef.transport.toString());
				out.write(holoEditRef.soundPoolGui.toString());
				out.write(holoEditRef.score.toString());
				out.write(holoEditRef.room.toString());
				out.write(holoEditRef.room3d.toString());
				out.write(holoEditRef.timeEditor.toString());
				// END
				out.write("</settings>\n");
				out.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch (SecurityException se)
			{
				se.printStackTrace();
			}
	}

	public void load()
	{
		if (SETTINGS)
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try
			{
				SAXParser saxParser = factory.newSAXParser();
				File f = new File(fileSettings);
				saxParser.parse(f, this);
			}
			catch (Exception e)
			{
				if (e.getClass().getName().equalsIgnoreCase("java.io.FileNotFoundException"))
				{
					System.out.println("Warning : Can't find file HoloSettings.cfg");
				} else {
					Ut.print(" x Error while loading the file, aborted.");
					e.printStackTrace();
				}
				if(!Ut.MAC)
				{
					try
					{
						switch (holoEditRef.laf)
						{
						case 0:
							UIManager.setLookAndFeel(new MetalLookAndFeel());
							holoEditRef.updateUI();
							Ut.barMenu.basicLAFMenuItem.setSelected(true);
							break;
						case 1:
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
							holoEditRef.updateUI();
							Ut.barMenu.systemLAFMenuItem.setSelected(true);
							break;
						case 2:
							UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
							holoEditRef.updateUI();
							Ut.barMenu.motifLAFMenuItem.setSelected(true);
						default:
							break;
						}
					}
					catch (Exception e1)
					{
						System.err.println("Look and Feel error :");
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public void startDocument()
	{
		Ut.print("___________________________________________");
		Ut.print(" o Reading file : " + fileSettings + " in progress...");
		holoEditRef.gestionPistes.speakers.clear();
	}

	public void endDocument()
	{
		holoEditRef.last = lastOne;
		Ut.barMenu.update();
		Ut.print(" v File was successfully read");
		Ut.print("___________________________________________");
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs)
	{
		// <settings>
		if (qName.equalsIgnoreCase("settings"))
		{
			String v = "v";
			fileversion = new Double(attrs.getValue(attrs.getIndex("version")));
			if (fileversion > Ut.version)
			{
				Ut.print(" x ATTENTION : Ce fichier a été créé avec une version plus récente de Luminaria, " + "il est possible qu'il ne soit pas compatible avec la présente version.");
				v = "+";
			}
			else if (fileversion < Ut.version)
			{
				Ut.print(" x ATTENTION : Ce fichier a été créé avec une version antérieure de Holo-Edit, " + "si vous le modifiez, il est possible qu'il ne soit plus compatible avec les versions antérieures du logiciel.");
				v = "-";
			}
			Ut.print(" " + v + " Settings - version " + fileversion);
			Ut.print("...");
		}
		else if (qName.equalsIgnoreCase("options"))
		{
			holoEditRef.maxRecentFiles = new Integer(attrs.getValue("maxRecentFiles"));
			holoEditRef.allTrackActive = new Boolean(attrs.getValue("allTrackActive"));
			holoEditRef.viewOnlyEditablePoints = new Boolean(attrs.getValue("viewOnlyEditablePoints"));
			holoEditRef.viewSpeakers = new Boolean(attrs.getValue("viewSpeakers"));
			holoEditRef.openLastOnLoad = new Boolean(attrs.getValue("openLastOnLoad"));
			holoEditRef.bonjour = holoEditRef.bonjourInstalled ? new Boolean(attrs.getValue("bonjour")) : false;
			Ut.drawPtsNb = new Integer(attrs.getValue("drawPtsNb"));
			holoEditRef.coordinates = new Integer(attrs.getValue("coordinates"));
			holoEditRef.scrollSpeed = new Float(attrs.getValue("scrollspeed"));
			holoEditRef.sdifExpert = new Boolean(attrs.getValue("sdifExpert"));
			holoEditRef.lastScriptFile = new File(attrs.getValue("lastScriptFile"));
			
			HoloEdit.SMOOTH = new Boolean(attrs.getValue("smooth"));
			if(new File("nosmooth").exists())
				HoloEdit.SMOOTH = false;			
			
			if(!Ut.MAC)
			{
				holoEditRef.laf = new Integer(attrs.getValue("laf"));
				try
				{
					switch (holoEditRef.laf)
					{
					case 0:
						UIManager.setLookAndFeel(new MetalLookAndFeel());
						holoEditRef.updateUI();
						Ut.barMenu.basicLAFMenuItem.setSelected(true);
						break;
					case 1:
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						holoEditRef.updateUI();
						Ut.barMenu.systemLAFMenuItem.setSelected(true);
						break;
					case 2:
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
						holoEditRef.updateUI();
						Ut.barMenu.motifLAFMenuItem.setSelected(true);
					default:
						break;
					}
				}
				catch (Exception e)
				{
					System.err.println("Look and Feel error :");
					e.printStackTrace();
				}
			}
			holoEditRef.shortViewMode = new Boolean(attrs.getValue("shortView"));
			holoEditRef.gestionPistes.ts.labelPlusDelta.setSelected(holoEditRef.shortViewMode);
			focus = attrs.getValue("focus");
		}
		else if (qName.equalsIgnoreCase("osc"))
		{
			holoEditRef.connection.setIn(new Integer(attrs.getValue("portIn")));
			holoEditRef.connection.setOut(new Integer(attrs.getValue("portOut")));
			holoEditRef.connection.setAddress(attrs.getValue("address"));
			holoEditRef.connection.setKeyIn(attrs.getValue("keyIn"));
			holoEditRef.connection.setKeyOut(attrs.getValue("keyOut"));
			if (new Boolean(attrs.getValue("open")))
			{
				holoEditRef.connection.open();
			}
			if (new Boolean(attrs.getValue("linemode")))
			{
				holoEditRef.connection.setLineMode(true);
				holoEditRef.counterPanel.lineMode.setSelected(true);
			}
			
			if (new Boolean(attrs.getValue("preloadAbs")))
			{
				holoEditRef.connection.setPreloadAbs(true);
				//holoEditRef.counterPanel.lineMode.setSelected(true);
			}
		}
		else if (qName.equalsIgnoreCase("holo"))
		{
			holoEditRef.gestionPistes.holoDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("midi"))
		{
			holoEditRef.gestionPistes.midiDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("midi7"))
		{
			holoEditRef.gestionPistes.midi7Directory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("txt"))
		{
			holoEditRef.gestionPistes.textDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("seq"))
		{
			holoEditRef.gestionPistes.seqDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("snd"))
		{
			holoEditRef.gestionPistes.sndDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("dt"))
		{
			holoEditRef.gestionPistes.dtDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("script"))
		{
			holoedit.functions.GroovyWindow.scriptDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("tk"))
		{
			holoEditRef.gestionPistes.tkDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("tj"))
		{
			holoEditRef.gestionPistes.tjDirectory = attrs.getValue("dir");
		}
		else if (qName.equalsIgnoreCase("label"))
		{
			int number = new Integer(attrs.getValue("num"));
			String name = attrs.getValue("name");
			int color = new Integer(attrs.getValue("color"));
			if (number < Ut.INIT_TRACK_NB && !name.startsWith("Track"))
			{
				holoEditRef.gestionPistes.ts.labelAudio[number].setLabelName(name);
				holoEditRef.gestionPistes.ts.labelAudio[number].setColor(color);
				holoEditRef.gestionPistes.getTrack(number).setName(name);
			}
		}
		else if (qName.equalsIgnoreCase("last"))
		{
			lastOne = new HoloRecentFile(holoEditRef, attrs.getValue("dir"), attrs.getValue("name"), new Integer(attrs.getValue("type")));
			lastOne = lastOne.exists() ? lastOne : null;
		}
		else if (qName.equalsIgnoreCase("recent"))
		{
			holoEditRef.addRecent(new HoloRecentFile(holoEditRef, attrs.getValue("dir"), attrs.getValue("name"), new Integer(attrs.getValue("type"))));
		}
		else if (qName.equalsIgnoreCase("speaker"))
		{
			holoEditRef.gestionPistes.speakers.add(new HoloSpeaker(new Float(attrs.getValue("x")), new Float(attrs.getValue("y")), new Float(attrs.getValue("z")), new Float(attrs.getValue("d")), new Integer(attrs.getValue("num"))));
		}
		else if (qName.equalsIgnoreCase("trackselector"))
		{
			holoEditRef.gestionPistes.ts.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.gestionPistes.ts.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.wbTrackSel = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("transport"))
		{
			holoEditRef.transport.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.transport.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.wbTrans = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("soundpool"))
		{
			holoEditRef.soundPoolGui.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.soundPoolGui.setSize(new Integer(attrs.getValue("w")), new Integer(attrs.getValue("h")));
			holoEditRef.soundPoolGui.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.soundPoolGui.setSplitDivPos(new Integer(attrs.getValue("split")));
			holoEditRef.wbSoundPool = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("score"))
		{
			holoEditRef.score.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.score.setSize(new Integer(attrs.getValue("w")), new Integer(attrs.getValue("h")));
			holoEditRef.score.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.wbScore = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("room"))
		{
			holoEditRef.room.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.room.setSize(new Integer(attrs.getValue("w")), new Integer(attrs.getValue("h")));
			holoEditRef.room.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.room.setProjView(attrs.getValue("viewProj"));
			holoEditRef.wbRoom = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("room3d"))
		{
			holoEditRef.room3d.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.room3d.setSize(new Integer(attrs.getValue("w")), new Integer(attrs.getValue("h")));
			holoEditRef.room3d.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.room3d.set3DView(attrs.getValue("view3D"));
			holoEditRef.wbRoom3D = new Boolean(attrs.getValue("v")).booleanValue();
		}
		else if (qName.equalsIgnoreCase("time"))
		{
			holoEditRef.timeEditor.setLocation(new Integer(attrs.getValue("x")), new Integer(attrs.getValue("y")));
			holoEditRef.timeEditor.setSize(new Integer(attrs.getValue("w")), new Integer(attrs.getValue("h")));
			holoEditRef.timeEditor.setVisi(new Boolean(attrs.getValue("v")).booleanValue());
			holoEditRef.wbTime = new Boolean(attrs.getValue("v")).booleanValue();
			holoEditRef.timeEditor.setCurves(attrs.getValue("curves"));
			holoEditRef.timeEditor.setTimeMode(new Integer(attrs.getValue("mode")));
		}
		else
		{
			Ut.print(" x Ce fichier n'a pas été créé avec Holo-Edit ou avec une version ultérieure de Holo-Edit !");
			Ut.print(" L'éditeur ne comprend pas l'entrée : " + qName);
		}
	}

	public void endElement(String uri, String localName, String qName)
	{
	}
}
