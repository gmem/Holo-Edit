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
package holoedit.fileio;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;

/** Classe permettant la lecture du fichier STYP*/
public class STYPreader {

	private String unknownType;
	private String fileName;
	BufferedReader reader;
	
	/** That constructor is used when we look for the fields of a particular
	 * matrix type in a *.STYP file.
	 * @param fileName : the path+name of the .STYP file to read.
	 * @param unknownType : the type of the matrix that we look for.
	 *  */
	public STYPreader(String fileName, String unknownType) {
		this.unknownType = unknownType;
		this.fileName = fileName;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName)));
		}catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.err.println(" can't find file 'SdifTypes.STYP'");
		}
	}
	
	/** That constructor is used when we look for the fields of a particular
	 * matrix type in a String.
	 * @param TYPframeString : the String to read.
	 * @param unknownType : the type of the matrix that we look for.
	 * @param b : a boolean; just to make the difference with the other constructor.
	 *  */
	public STYPreader(String TYPframeString, String unknownType, boolean b) {
		this.unknownType = unknownType;
		try {
			reader = new BufferedReader(new StringReader(TYPframeString));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Returns the fields of the matrix.
	 * @return An array of Strings that are the matrix fields names, 

	 *  */
	public String[] getData() {
		String[] result = new String[0];	
		if (reader==null)
			return result;
		String line;
		try {
			while((line = reader.readLine())!=null)
			{	
				line = line.replaceAll("\\p{Punct}", ""); // pour enlever les "{", "}" et ","
				line = line.replaceAll("\\s+", " "); // pour enlever les espaces en trop
				line = line.trim();					 // pour enlever les espaces en trop en dŽbut et fin de ligne

				String[] resultSplit = line.split("\\s");

				if (resultSplit.length>2 && resultSplit[0].equals("1MTD") && resultSplit[1].equals(unknownType))	// on observe les 2 premiers mots de chaque ligne
				{	
					 result = new String[resultSplit.length -2];						
					 for (int x=2; x<resultSplit.length; x++) // on saute les 2 premiers mots et on garde les suivants qui correspondent aux champs de la matrice
				         result[x-2] = resultSplit[x];
					 break;
				}
			}
			reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}