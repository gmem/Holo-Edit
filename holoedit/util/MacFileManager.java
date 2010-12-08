package holoedit.util;

import com.apple.eio.FileManager;

import holoedit.HoloEdit;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class MacFileManager extends FileManager{
	
	public final static String supportedSoundType[] = {"AIFF","Sd2f","WAVE"};
	public final static String supportedDataType[] = {"SDIF"};
	private HoloEdit holoEditRef;
	
	public MacFileManager(HoloEdit owner)
	{
		super();
		holoEditRef = owner;
	}
	
	public static String getCode(int code)
    { 
		byte[] bytes = {(byte)(code >> 24), (byte)(code >> 16), (byte)(code >> 8),(byte) code};
		return new String(bytes);
    } // convert
	
	public File[] SoundFileFilter(File[] files)
	{
		Vector<File> acceptedFiles = new Vector<File>(1,1);
		
		String type="";
		for (File f : files)
		{
			try {
				type = getCode(getFileType(f.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(type);
			if(type.equalsIgnoreCase("AIFF") || type.equalsIgnoreCase("Sd2f") || type.equalsIgnoreCase("WAVE"))
				acceptedFiles.add(f);
			// if Mac Filetype is not set
			else if(holoEditRef.gestionPistes.sndFilter.accept(null, f.getName()))
				acceptedFiles.add(f);
		}	
		
		File[] result = new File[acceptedFiles.size()] ;
		acceptedFiles.toArray(result);
		
		return  result;
	}
	
	public File[] DataFileFilter(File[] files)
	{
		Vector<File> acceptedFiles = new Vector<File>(1,1);
		
		String type="";
		for (File f : files)
		{
			try {
				type = getCode(getFileType(f.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(type);
			if(type.equalsIgnoreCase("SDIF") || type.equalsIgnoreCase("txt"))
				acceptedFiles.add(f);
			// if Mac Filetype is not set
			else if(holoEditRef.gestionPistes.dtFilter.accept(null, f.getName()))
				acceptedFiles.add(f);
		}	
		
		File[] result = new File[acceptedFiles.size()] ;
		acceptedFiles.toArray(result);
		
		return  result;
	}
	public File[] SoundAndDataFileFilter(File[] files)
	{
		Vector<File> acceptedFiles = new Vector<File>(1,1);
		
		String type="";
		for (File f : files)
		{
			try {
				type = getCode(getFileType(f.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(type);
			if(type.equalsIgnoreCase("SDIF") || type.equalsIgnoreCase("AIFF") || type.equalsIgnoreCase("Sd2f") || type.equalsIgnoreCase("WAVE"))
				acceptedFiles.add(f);
			// if Mac Filetype is not set
			else if(holoEditRef.gestionPistes.sndFilter.accept(null, f.getName()))
				acceptedFiles.add(f);
		}	
		
		File[] result = new File[acceptedFiles.size()] ;
		acceptedFiles.toArray(result);
		
		return  result;
	}
}
