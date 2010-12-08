package holoedit.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.File;
import java.io.LineNumberReader;
import javax.swing.JTextArea;

public class Text
{
	
	/**
	 * @param name The complete name of the file ("./algo/groovyFile.groovy" for example)
	 * @param textArea The JTextArea where the text of the file has to be copied
	 * @return the textArea after the copy
	 */
	public static BufferedReader appendFileToTextArea(String name, JTextArea textArea, String stop) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		try {
			while((line = reader.readLine())!=null){
				if (line.matches(stop+".*"))
					return reader;
				textArea.append(line+"\n");
			}
			reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}
	
	/**
	 * @param file The file to append in the jTextArea
	 * @param textArea The JTextArea where the text of the file has to be copied
	 * @return the textArea after the copy
	 */
	public static BufferedReader appendFileToTextArea(File file, JTextArea textArea) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		try {
			while((line = reader.readLine())!=null){
				textArea.append(line+"\n");
			}
			reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}
	
	/**
	 * @param name The complete name of the file ("./algo/groovyFile.groovy" for example)
	 * @param textArea The JTextArea where the text of the file has to be copied
	 * @return the textArea after the copy
	 */
	public static BufferedReader appendFileToTextArea(String name, JTextArea textArea, BufferedReader _reader) {
		BufferedReader reader = null;
		try {
			if(_reader==null)
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
			else
				reader = _reader;
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line;
		try {
			while((line = reader.readLine())!=null)
				textArea.append(line+"\n");
			reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}		
	/**
	 * @param name The complete name of the file ("./algo/groovyFile.groovy" for example)
	 * @param textArea The JTextArea where the text of the file has to be copied
	 * @return the textArea after the copy
	 */
	public static BufferedReader appendFileToTextArea(String name, JTextArea textArea) {
		BufferedReader reader = null;
		return appendFileToTextArea(name, textArea, reader);
	}


	public static void textAreaToFile(File file, JTextArea textArea) {
		 try {
			 FileWriter outFile = new FileWriter(file);
			 PrintWriter out = new PrintWriter(outFile);
			 BufferedReader textAreaReader;
			 textAreaReader = new BufferedReader(new StringReader(textArea.getText()));
			 String taLine;
			 while((taLine = textAreaReader.readLine())!=null) {
				 out.println(taLine);
			}			
			out.close();
			textAreaReader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void textToFile(File file, String text) {
		 try {
			 FileWriter outFile = new FileWriter(file);
			 PrintWriter out = new PrintWriter(outFile);
			 BufferedReader textAreaReader;
			 textAreaReader = new BufferedReader(new StringReader(text));
			 String taLine;
			 while((taLine = textAreaReader.readLine())!=null) {
				 out.println(taLine);
			}			
			out.close();
			textAreaReader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retourne le nombre de ligne d'un fichier.
	 * @return
	 */
	public static int countLines(File file){
		try {
			FileInputStream fis = new FileInputStream(file.getCanonicalFile());
			LineNumberReader l = new LineNumberReader(new BufferedReader(new InputStreamReader(fis)));
			int count=0;
			String line = "";
			while ((line = l.readLine()) != null) {
				count = l.getLineNumber();
			}
			return count;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
}
