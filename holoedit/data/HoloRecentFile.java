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
package holoedit.data;

import holoedit.HoloEdit;
import holoedit.gui.HoloMenuItem;
import holoedit.util.Ut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

// Classe permettant de memoriser les fichiers recemment lus
public class HoloRecentFile extends HoloMenuItem implements ActionListener
{
	// on separe repertoire et nom de fichier pour pouvoir n'afficher dans
	// le menu que le nom du fichier
	// repertoire
	public String fd;
	// nom du fichier
	public String fn;
	// type du fichier (differents modes d'ouverture suivant le type)
	public int ft; // (0 : mid, 1 : mid7, 2 : txt, 3 : seq~, 4:holo)
	// caractere de separation de l'arborescence en fonction du systeme
	// d'exploitation
	// (voir Editeur)
	private HoloEdit holoEditRef;

	// constructeur par defaut
	public HoloRecentFile(HoloEdit s)
	{
		super();
		holoEditRef = s;
		fd = null;
		fn = null;
		ft = -1;
		addActionListener(this);
	}

	// constructeur par copie
	public HoloRecentFile(HoloEdit s, HoloRecentFile toClone)
	{
		holoEditRef = s;
		this.fn = toClone.fn;
		this.fd = toClone.fd;
		this.ft = toClone.ft;
		this.menuTitle = toClone.menuTitle;
	}

	// constructeur avec repertoire et nom de fichier (type par defaut midi)
	public HoloRecentFile(HoloEdit s, String fileDir, String fileName)
	{
		super();
		holoEditRef = s;
		fd = fileDir;
		fn = fileName;
		ft = 0;
		menuTitle = Ut.dir(fd) + fn;
		setText(menuTitle);
		addActionListener(this);
	}

	// constructeur avec repertoire, nom de fichier et type
	public HoloRecentFile(HoloEdit s, String fileDir, String fileName, int fileType)
	{
		super();
		holoEditRef = s;
		fd = fileDir;
		fn = fileName;
		ft = fileType;
		menuTitle = Ut.dir(fd) + fn;
		setText(menuTitle);
		addActionListener(this);
	}

	// constructeur a partir d'une string contenant toutes les informations
	// pour la lecture de configuration
	public HoloRecentFile(HoloEdit s, String all)
	{
		// "all" du type : (d)filedirectory(n)filename(t)filetype
		super();
		holoEditRef = s;
		try
		{
			fd = all.substring(3, all.indexOf("(n)"));
			fn = all.substring(all.indexOf("(n)") + 3, all.indexOf("(t)"));
			menuTitle = Ut.dir(fd) + fn;
			setText(menuTitle);
			ft = (new Integer(all.substring(all.indexOf("(t)") + 3, all.length()))).intValue();
		}
		catch (IndexOutOfBoundsException e)
		{}
		addActionListener(this);
	}

	public String toString()
	{
		return "\t<recent dir=\""+fd+"\" name=\""+fn+"\" type=\""+ft+"\"/>\n";
	}

	public String toLastString()
	{
		return "\t<last dir=\""+fd+"\" name=\""+fn+"\" type=\""+ft+"\"/>\n";
	}
	
	// Quand on clique sur un fichier recent (qui est un menu)
	public void actionPerformed(ActionEvent e)
	{
		if(holoEditRef.askForSave())
		{
			// suivant son type, on le lit de maniere differente
			switch (ft)
			{
			case 0: // mid
				holoEditRef.gestionPistes.readFile14b(Ut.dir(fd) + fn);
				holoEditRef.gestionPistes.holoFilename = null;
				break;
			case 1: // midi7
				holoEditRef.gestionPistes.readFile7bp(Ut.dir(fd) + fn);
				holoEditRef.gestionPistes.holoFilename = null;
				break;
			case 2: // txt
				holoEditRef.gestionPistes.readTextFile(Ut.dir(fd) + fn);
				holoEditRef.gestionPistes.holoFilename = null;
				break;
			case 3: // seq~
				holoEditRef.gestionPistes.readSeqFileDirect(Ut.dir(fd) + fn);
				holoEditRef.gestionPistes.holoFilename = null;
				break;
			case 4: // holo
				holoEditRef.gestionPistes.readHoloFile(Ut.dir(fd) + fn);
				holoEditRef.gestionPistes.holoFilename = fn;
				holoEditRef.gestionPistes.holoDirectory = fd;
				break;
			default:
				break;
			}
			// on le memorise comme dernier fichier lu
			holoEditRef.last = new HoloRecentFile(holoEditRef, this);
			// on affiche son titre
			holoEditRef.setSaved(true);
			holoEditRef.setTitle(fn);
		}
	}

	// test d'egalite sur le nom, le repertoire et le type pour pouvoir
	// tester s'il est deja contenu dans un vecteur
	public boolean equals(Object o)
	{
		try{
			HoloRecentFile rf = (HoloRecentFile)o;
			return ((fd.equalsIgnoreCase(rf.fd)) && (fn.equalsIgnoreCase(rf.fn)) && (ft == rf.ft));
		} catch (ClassCastException e)
		{
			return false;
		}
	}

	public boolean exists()
	{
		return new File(Ut.dir(fd) + fn).exists();
	}
}
