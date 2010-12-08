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
package holoedit.gui;

import holoedit.HoloPlayer;
import holoedit.data.HoloExternalData;
import holoedit.data.HoloSDIFdata;
import holoedit.data.HoloWaveForm;
import holoedit.fileio.SDIFreader;
import holoedit.fileio.WaveFormReader;
import holoedit.fileio.TextFileReader;
import holoedit.fileio.WaveFormSimpleReader;
import holoedit.util.Ut;
import java.io.File;
import java.util.Vector;
import javax.swing.JFileChooser;

public class SimpleSoundPool
{
	private HoloPlayer hpRef;
	public boolean fine = false;
	public boolean done = false;
	public int error = WaveFormReader.NO_ERROR;
	public String errorFileName;
	public HoloWaveForm last;
	private Vector<File> unfound;
	private File soundFolder;
	private File externalDataFolder;
	private boolean choosingfolder;
	public Vector<String> doneAndFineData = new  Vector<String>();
	
	public SimpleSoundPool(HoloPlayer owner)
	{
		hpRef = owner;
		unfound = new Vector<File>();
	}

	public void clear()
	{
		hpRef.gestionPistes.soundPool.clear();
		unfound.clear();
	}

	public void importSound(File f, boolean verboseError)
	{
		if(f.getName().indexOf(' ') == -1)
		{
			if(f.getName().length() <= 32)
			{
				Thread t = new Thread(new SoundFileImporter(f, verboseError));
				t.setPriority(Thread.MAX_PRIORITY);
				t.setName("SF-ImportDeamon");
				t.setDaemon(true);
				t.start();
				
				// to be sure waveform are load in time / session load
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				if(verboseError)
					Ut.alert("Import Error", "Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot be longer than 32 characters (including the extension).");
				error = WaveFormReader.PATH_ERROR;
				done = true;
				fine = false;
			}
		} else {
			if(verboseError)
				Ut.alert("Import Error", "Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot contain blank spaces.");
			error = WaveFormReader.PATH_ERROR;
			done = true;
			fine = false;
		}
	}

	public void importData(File f, String importOptions, boolean verboseError) {
		if(f.getName().indexOf(' ') == -1)
		{
			if(f.getName().length() <= 32)
			{
				Thread t = new Thread(new DataFileImporter(f, importOptions, verboseError));
				t.setPriority(Thread.MAX_PRIORITY);
				t.setName("SF-ImportDeamon");
				t.setDaemon(true);
				t.start();
			} else {
				if(verboseError)
					Ut.alert("Import Error", "Please rename the datafile, for compatibility reasons with Max/MSP,\nit cannot be longer than 32 characters (including the extension).");
				error = WaveFormReader.PATH_ERROR;
				done = true;
				fine = false;
			}
		} else {
			if(verboseError)
				Ut.alert("Import Error", "Please rename the datafile, for compatibility reasons with Max/MSP,\nit cannot contain blank spaces.");
			error = WaveFormReader.PATH_ERROR;
			done = true;
			fine = false;
		}
	}
	
	class SoundFileImporter implements Runnable
	{
		File f;
		boolean verbose = true;

		public SoundFileImporter(File _f, boolean verb)
		{
			f = _f;
			verbose = verb;
		}

		public void run()
		{
			if (f != null && ((f.exists() && f.isFile())))
			{
				done = false;
				fine = false;
				if (!hpRef.gestionPistes.soundPool.contains(new HoloWaveForm(f)))
				{
					WaveFormSimpleReader wfr = new WaveFormSimpleReader(f);
					try
					{
						while (!wfr.isDone())
						{
							Thread.sleep(250);
//							System.out.println("soundpool-import-waiting");
//							System.out.print('.');
						}
//						System.out.println();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					done = true;
					if (wfr.isFine())
					{
						HoloWaveForm[] arr = wfr.getHoloWaveForms();
						for (HoloWaveForm hwf : arr)
							hpRef.gestionPistes.soundPool.add(hwf);
						last = arr[arr.length-1];
						fine = true;
					} else {
						error = wfr.getError();
						errorFileName = wfr.soundFile.getName();
						if(verbose)
							switch(error)
							{
							case WaveFormReader.NO_ERROR :
								break;
							case WaveFormReader.MONO_ERROR :
								HoloPlayer.error("Only mono file for instance,\nOnly the left channel has been read.");
								break;
							case WaveFormReader.TYPE_ERROR :
								HoloPlayer.error(errorFileName + " is not a supported file type.");
								break;
							case WaveFormReader.PATH_ERROR :
								HoloPlayer.error("Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot be longer than 32 characters (including the extension).");
								break;
							case WaveFormReader.PATH2_ERROR :
								HoloPlayer.error("Please rename the soundfile, for compatibility reasons with Max/MSP,\nit cannot contain blank spaces.");
								break;
							case WaveFormReader.SSDII_NORSRC_ERROR :
								HoloPlayer.error("This Sound Designer II file has no resource and cannot be read.");
								break;
							case WaveFormReader.SSDII_RSRC_ERROR :
								HoloPlayer.error("There are errors in this Sound Designer II resource file.");
								break;
							default :
								HoloPlayer.error("Problem while loading soundfile, aborted.");
								break;
							}
					}
					wfr.stop();
					wfr = null;
					System.gc();
				//	System.runFinalization();
				} else {
					done = true;
					fine = true;
					last = new HoloWaveForm(f);
				}
			} else {
				done = true;
				fine = false;
				error = WaveFormReader.FILE_ERROR;
				unfound.add(f);
				if(!choosingfolder)
				{
					choosingfolder = true;
					HoloPlayer.error(f.getName() + " can't be found,\neven it's cached waveform.\nLocalize ... ");
				} else {
					try{
						while(choosingfolder)
						{
//							System.out.println("soundpool-folder-localize-waiting");
							Thread.sleep(500);
						}
					} catch (InterruptedException e){}
				}
				if(soundFolder != null)
				{
					File[] sounds = soundFolder.listFiles();
					for (int k = unfound.size() - 1; k >= 0; k--)
					{
						File fil = unfound.get(k);
						for (int i = 0; i < sounds.length; i++)
						{
							if (sounds[i].getName().equalsIgnoreCase(fil.getName()))
							{
								hpRef.soundPool.importSound(sounds[i], true);
								unfound.remove(fil);
							}
						}
					}		
				} else {
					JFileChooser chooser = new JFileChooser(f.getParentFile().exists() ? f.getParent() : hpRef.gestionPistes.holoDirectory);
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Choose a folder for missing sounds ( "+f.getName()+" )");
					chooser.showDialog(null, "Choose a folder for missing sounds ( "+f.getName()+" )");
					if (chooser.getSelectedFile() != null)
					{
						File tmp = chooser.getSelectedFile();
						if (tmp.exists() && tmp.isDirectory())
						{
							soundFolder = tmp;
							choosingfolder = false;
							File[] sounds = soundFolder.listFiles();
							for (int k = unfound.size() - 1; k >= 0; k--)
							{
								File fil = unfound.get(k);
								for (int i = 0; i < sounds.length; i++)
								{
									if (sounds[i].getName().equalsIgnoreCase(fil.getName()))
									{
										hpRef.soundPool.importSound(sounds[i], true);
										unfound.remove(fil);
									}
								}
							}		
						}
					}
				}
				if(!unfound.isEmpty())
					HoloPlayer.error(f.getName() + " can't still be found, aborted.");
			}
		}
	}

	public void clearDoneAndFineData(){
		Vector<String> toremove = new Vector<String>();
		for (String data : doneAndFineData){
			if (!hpRef.gestionPistes.externalDataPool.contains(new HoloExternalData(new File(data))))
				toremove.add(data);
		}
		for (String data : toremove)
			doneAndFineData.remove(data);
	}
	
	class DataFileImporter implements Runnable {
		File f;
		boolean verbose = true;
		String importOptions;
		
		public DataFileImporter(File _f, String _importOptions, boolean verb) {
			f = _f;
			importOptions = _importOptions;
			verbose = verb;
		}
		
		public void run() {
			if (f != null && f.exists() && f.isFile()) {
				done = false;
				fine = false;
				if (!hpRef.gestionPistes.externalDataPool.contains(new HoloExternalData(f))) {
					String extension = f.getName().substring(f.getName().lastIndexOf('.') + 1);
					SDIFreader sdifReader = null;
					TextFileReader textReader = null;
					if (extension.equalsIgnoreCase("SDIF")) {
						sdifReader = new SDIFreader(f, importOptions, null);
					} else {
						textReader = new TextFileReader(f);
					}
					
					if (sdifReader != null) {
						try {
							while (!sdifReader.isDone()) {
								Thread.sleep(20);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						done = true;
						if (sdifReader.isFine()) {
							HoloSDIFdata[] arr = sdifReader.getHoloSDIFdatas();
							if (arr != null) {
								for (HoloSDIFdata hdt : arr)
									hpRef.gestionPistes.externalDataPool.add(hdt);
							}
							fine = true;
							doneAndFineData.add(f.getName());
							if (sdifReader.getError() == -1)
								Ut.alert("File not found", f.getName() + " can't be found,\nusing it's cached waveform.");
						} else {
							error = sdifReader.getError();
							errorFileName = sdifReader.sdifFile.getName();
							if (verbose)
								switch (error) {
								case SDIFreader.NO_ERROR:
									break;
								case SDIFreader.TYPE_ERROR:
									Ut.alert("Import Error", errorFileName + " is not a supported file type.");
									break;
								case TextFileReader.TEXT_FORMAT_ERROR:
									Ut.alert("Import Error", "This text file cannot be imported in holo-Edit.\n" +
											"The data it contains are not properly arranged.\n" +
											"(Columns must be separated by whitespace characters only)");
									break;
								default:
									Ut.alert("Import Error", "Problem while loading datafile, aborted.");
									break;
								}
						}
						sdifReader.stop();
						sdifReader = null;
						System.gc();
						//System.runFinalization();
					} else if (textReader!=null){
						try {
							while (!textReader.isDone()) {
								Thread.sleep(20);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						done = true;
						if (textReader.isFine()) {
							HoloSDIFdata[] arr = textReader.getHoloSDIFdatas();
							if (arr != null) {
								for (HoloSDIFdata hdt : arr)
									hpRef.gestionPistes.externalDataPool.add(hdt);
							}
							fine = true;
							doneAndFineData.add(f.getName());
							if (textReader.getError() == -1)
								Ut.alert("File not found", f.getName() + " can't be found,\nusing it's cached waveform.");
						} else {
							error = textReader.getError();
							errorFileName = textReader.textFile.getName();
							if (verbose)
								switch (error) {
								case SDIFreader.NO_ERROR:
									break;
								case SDIFreader.TYPE_ERROR:
									Ut.alert("Import Error", errorFileName + " is not a supported file type.");
									break;
								case TextFileReader.TEXT_FORMAT_ERROR:
									Ut.alert("Import Error", "This text file cannot be imported in holo-Edit.\n" +
											"Be sure the data it contains are properly arranged.\n" +
											"(Columns must be separated by whitespace characters only)");
									break;
								default:
									Ut.alert("Import Error", "Problem while loading datafile, aborted.");
									break;
								}
						}
						textReader.stop();
						textReader = null;
						System.gc();
						//System.runFinalization();
					} else {
						doneAndFineData.add(f.getName());
						done = true;
						fine = true;
					}
				} else {
					done = true;
					fine = false;
					error = SDIFreader.FILE_ERROR;
					unfound.add(f);
					if (!choosingfolder) {
						choosingfolder = true;
						Ut.alert("File not found", f.getName() + " can't be found,\neven it's cached waveform.\nLocalize ... ");
					} else {
						try {
							while (choosingfolder) {
								// System.out.println("soundpool-folder-localize-waiting");
								Thread.sleep(500);
							}
						} catch (InterruptedException e) {
						}
					}
					if (externalDataFolder != null) {
						File[] datas = externalDataFolder.listFiles();
						for (int k = unfound.size() - 1; k >= 0; k--) {
							File fil = unfound.get(k);
							for (int i = 0; i < datas.length; i++) {
								if (datas[i].getName().equalsIgnoreCase(fil.getName())) {
									hpRef.soundPool.importData(datas[i], importOptions, true);
									unfound.remove(fil);
								}
							}
						}
					} else {
						JFileChooser chooser = new JFileChooser(f.getParentFile().exists() ? f.getParent()
								: hpRef.gestionPistes.holoDirectory);
						chooser.setAcceptAllFileFilterUsed(false);
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setDialogTitle("Choose a folder for missing sounds ( "+f.getName()+" )");
						chooser.showDialog(null, "Choose");
						if (chooser.getSelectedFile() != null) {						
								File tmp = chooser.getSelectedFile();
								
								if (tmp.exists() && tmp.isDirectory()) {
									externalDataFolder = tmp;
									choosingfolder = false;
									File[] datas = externalDataFolder.listFiles();
									for (int k = unfound.size() - 1; k >= 0; k--) {
										File fil = unfound.get(k);
										for (int i = 0; i < datas.length; i++) {
											if (datas[i].getName().equalsIgnoreCase(fil.getName()))	{
												hpRef.soundPool.importData(datas[i], importOptions, true);
												unfound.remove(fil);
											}
										}
									}		
								}
							}
					}
					if(!unfound.isEmpty())
						HoloPlayer.error(f.getName() + " can't still be found, aborted.");
				}
			}
		}
	}
}
