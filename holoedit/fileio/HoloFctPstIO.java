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

import holoedit.data.HoloFctPreset;
import holoedit.functions.Algorithm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class HoloFctPstIO extends DefaultHandler
{
	Algorithm alg;
	File pstFile;
	Vector<HoloFctPreset> presets;
	
	public HoloFctPstIO(Algorithm algo, File f)
	{
		alg = algo;
		pstFile = f;
	}
	
	public void loadPreset()
	{
		if(!pstFile.exists()) return;
		if (alg.getName().equalsIgnoreCase("Script")) return;
		try
		{
			SAXParserFactory.newInstance().newSAXParser().parse(pstFile, this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void savePresets()
	{
		try
		{
			String filename = pstFile.getAbsolutePath();
			if(pstFile.exists())
				pstFile.renameTo(new File(filename+"~"));
			pstFile = new File(filename);
			FileWriter fw = new FileWriter(pstFile);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>\n");
			out.write("<algo name=\""+alg.getName()+"\">\n");
			out.write(new HoloFctPreset("#current",alg.getName()).setVals(alg.getVals()).toString());
			for(HoloFctPreset p:alg.getPresets())
				out.write(p.toString());
			out.write("</algo>");
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void startDocument() {}
	
	public void endDocument() {}
	
	public void startElement(String uri, String localName, String qName, Attributes attrs)
	{
		if (qName.equalsIgnoreCase("preset")) {
			String name = attrs.getValue("name");
			String algo = attrs.getValue("algo");
			String[] paramStr = attrs.getValue("params").split("#");
			Object[] paramd = new Object[paramStr.length];
			Vector<Object> paramv = new Vector<Object>(paramStr.length);
			for (int i = 0; i < paramStr.length; i++)
			{
				paramd[i] = paramStr[i];
				paramv.add(paramd[i]);
			}
			if (name.equalsIgnoreCase("#current"))
				alg.setCurrent(paramd);
			else
				alg.addPreset(new HoloFctPreset(name, algo, paramv));
		}
	}
	
	public void endElement(String uri, String localName, String qName){}
}
