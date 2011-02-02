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

package holoedit.data;

import holoedit.util.Ut;

import java.io.File;

public class HoloExternalData { //implements Serializable{
	// ATTRIBUTS 
	/** associated data file */
	protected File extDataFile;
	/** etat pour l'import */
	private boolean fine = false;
	/** duree du fichier (ms) */
	private double fileLength;
	/** The type of data contained by this holoExternalData. */
	protected String dataType;
		
	/** Constructs an holoExternalData from a file. */
	public HoloExternalData(File f){
		extDataFile=f;
	}
	/** Constructs an holoExternalData from a file.
	 * whith a given dataType. */
	public HoloExternalData(File f, String dataType){
		extDataFile=f;
		this.dataType = dataType;
	}
	/** Indicates whether some other object is "equal to" this one.
	 * Returns true if the object o is an holoExternalData whose filename
	 * is the same as the filename of this one.
	 * @param o - the reference object with which to compare.
	 * */
	public boolean equals(Object o) {
		try {
			HoloExternalData hxtdt = (HoloExternalData) o;
			// egalité basée sur le nom de fichier
			return this.getFileName().equalsIgnoreCase(hxtdt.getFileName());
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	/** Returns the length of this holoExternalData in milliseconds */
	public double getExtDataLength() {
		return fileLength;
	}
	/** Returns the length of this holoExternalData in centiseconds */
	public int getExtDataLengthCS() {
		return (int)(fileLength/10);
	}
	/** Sets the length of this holoExternalData in milliseconds */
	public void setExtDataLength(double fileLength)	{
		this.fileLength = fileLength;
	}
	/** Returns the name of the file from which this holoExternalData has been constructed.*/
	public String getFileName()
	{
		String tmp = extDataFile.getName();
		return tmp;
	}

	/** return the extension of the file from which this holoExternalData has been constructed. */
	public String getFileType(){
		return extDataFile.getName().substring(extDataFile.getName().lastIndexOf('.') + 1).toUpperCase();		
	}

	/** Returns the {@link HoloExternalData#dataType dataType} for this holoExternalData. */
	public String getDataType(){
		return dataType;
	}
	
	public boolean isFine(){
		return fine;
	}

	public void setFine(boolean b){
		fine = b;
	}
	/**
	 * used to write in .holo file.
	 * see HoloExternalDataPool.toString() and HoloFileWriter
	 * @param parentPath
	 * @return
	 */
	public String toString2(String parentPath)
	{
		if (Ut.MAC || Ut.LINUX)
		{
			// EN DESSOUS DANS LA MEME ARBORESCENCE
			if (extDataFile.getAbsolutePath().startsWith(parentPath))
				return "\t<data file=\""+(parentPath.equalsIgnoreCase("/") ? "/" : ".") + extDataFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
			String path = "../";
			String path2 = "..";
			parentPath = new File(parentPath).getParentFile().getAbsolutePath();
			while (!parentPath.equalsIgnoreCase("/"))
			{
				// MEME PARTITION
				if (extDataFile.getAbsolutePath().startsWith(parentPath))
					return "\t<data file=\"" + (parentPath.equalsIgnoreCase("/") ? path : path2) + extDataFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
				path = "../" + path;
				path2 = "../" + path2;
				parentPath = new File(parentPath).getParentFile().getAbsolutePath();
			}
		} else {
			// EN DESSOUS DANS LA MEME ARBORESCENCE
			if (extDataFile.getAbsolutePath().startsWith(parentPath))
				return "\t<data file=\""+(parentPath.endsWith(":\\") ? ".\\" : ".") + extDataFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
			String path = "..\\";
			String path2 = "..";
			while (!parentPath.endsWith(":\\"))
			{
				// MEME PARTITION
				parentPath = new File(parentPath).getParentFile().getAbsolutePath();
				if (extDataFile.getAbsolutePath().startsWith(parentPath))
					return "\t<data file=\"" + (parentPath.endsWith(":\\") ? path : path2) + extDataFile.getAbsolutePath().substring(parentPath.length()) + "\"/>\n";
				path = "..\\" + path;
				path2 = "..\\" + path2;
			}
		}
		// AUTRE PARTITION
		if (dataType.equalsIgnoreCase("SDIF")){
			HoloSDIFdata sdif = (HoloSDIFdata) this;
			String importOptions = "\t\t<importOptions optionMat=\"";
			
			for (String key : sdif.indexImportOptions.keySet()){
				importOptions += key +";"+ sdif.indexImportOptions.get(key)[0] +";"+ sdif.indexImportOptions.get(key)[1]+";";			
				for (String field : sdif.fieldsImportOptions.get(key)){
					importOptions += field+";";
				}
				importOptions += ":";
			}
			importOptions += "\"/>\n";
			return "\t<data file=\"" + extDataFile.getAbsolutePath()+ "\">\n" + importOptions+ "\t</data>\n";
		}
		return "\t<data file=\"" + extDataFile.getAbsolutePath() + "\">\n \t</data>\n";
	}
}