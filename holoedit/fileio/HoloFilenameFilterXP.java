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
/**
 * Classe des filtres de fichiers (holo, mid, txt, seq~) pour les JFileChooser (XP)
 */
package holoedit.fileio;

import java.io.File;
import java.util.Vector;
import javax.swing.filechooser.FileFilter;

public class HoloFilenameFilterXP extends FileFilter
{
	// extension du type de fichier
	private String extension = null;
	// description du type de fichier
	private String description = null;
	// si plusieurs extensions sont acceptées (séparées par des espaces)
	private boolean multi = false;
	private Vector<String> extVector = new Vector<String>();

	// divers constructeurs
	public HoloFilenameFilterXP()
	{
		this.extension = ".holo";
	}

	public HoloFilenameFilterXP(String _extension)
	{
		this.extension = _extension;
	}

	public HoloFilenameFilterXP(String _extension, String _description)
	{
		this.extension = _extension;
		this.description = _description;
	}

	public HoloFilenameFilterXP(String _extension, String _description, boolean b)
	{
		multi = b;
		this.extension = _extension;
		this.description = _description;
		if (multi)
		{
			String[] exts = extension.split(" ");
			for(String s:exts)
				extVector.add(s);
		}
	}

	// pour recuperer l'extension
	public static String getExtension(File f)
	{
		if (f != null)
		{
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i != -1)
			{
				return filename.substring(i).toLowerCase();
			}
		}
		return null;
	}

	// pour recuperer l'extension
	public static String getExtension(String filename)
	{
		if (filename != null)
		{
			int i = filename.lastIndexOf('.');
			if (i != -1)
			{
				return filename.substring(i).toLowerCase();
			}
		}
		return null;
	}

	// pour affecter l'extension
	public void setExtension(String _extension)
	{
		this.extension = _extension;
	}

	// pour recuperer la description
	public String getDescription()
	{
		return this.description;
	}

	// pour affecter la description
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	// fonction permettant de tester si un fichier ou un repertoire repond au filtre
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return true;
		if (!multi)
		{
			if (f != null)
			{
				String fileExt = getExtension(f);
				if (fileExt != null)
					if (fileExt.equalsIgnoreCase(extension))
						return true;
			}
		} else {
			if (f != null)
			{
				String fileExt = getExtension(f);
				// System.out.println("text ext:"+fileExt);
				if (fileExt != null && !extVector.isEmpty())
					if (extVector.contains(fileExt))
						return true;
			}
		}
		return false;
	}
}
