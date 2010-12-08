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

import holoedit.data.HoloWaveForm;
import holoedit.fileio.WaveFormReader.Resource;
import holoedit.fileio.WaveFormReader.ResourceType;
import holoedit.util.Ut;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

public class WaveFormSimpleReader implements Runnable
{
	private final static boolean VERBOSE = false;
	// Pour l'affichage d'une barre de progression pendant l'ouverture du fichier
	private Thread runner;
	// waveforms
	private HoloWaveForm[] hwfs;
	// snd file
	public File soundFile;
	// state
	private boolean fine = false;
	private boolean done = false;
	private int error = NO_ERROR;
	public final static int NO_ERROR = 0;
	public final static int MONO_ERROR = 1;
	public final static int TYPE_ERROR = 2;
	public final static int LOAD_ERROR = 3;
	public final static int OCTET_ERROR = 4;
	public final static int INDEX_ERROR = 5;
	public final static int FILE_ERROR = 6;
	public final static int PATH_ERROR = 7;
	public final static int PATH2_ERROR = 8;
	public final static int SSDII_NORSRC_ERROR = 9;
	public final static int SSDII_RSRC_ERROR = 10;
	// format
	private int sampleSize;
	private int octets;
	private boolean bigEndian;
	private int byteLength;
	private int frameLength;
	private int chanNum;
	private float sampleRate;
	private float sampleRateMs;
	private float fileLength;
	private String fileType;

	public WaveFormSimpleReader(File f)
	{
		soundFile = f;
		runner = new Thread(this);
		runner.setName("SF-Import");
		runner.setPriority(Thread.MAX_PRIORITY);
		runner.start();
	}

	public void run()
	{
		try
		{
			AudioFileFormat audioFileFormat;
			try
			{
				audioFileFormat = AudioSystem.getAudioFileFormat(soundFile);
			}
			catch (UnsupportedAudioFileException uaf)
			{
				if(HoloFilenameFilterXP.getExtension(soundFile).equalsIgnoreCase(".sd2"))
					readSd2File();						
				else {
					verboseln(soundFile.getName() + " is not a supported file type");
					error = TYPE_ERROR;
					done = true;
					fine = false;
				}
				return;
			}
			
			AudioFormat audioFormat = audioFileFormat.getFormat();
			chanNum = audioFormat.getChannels();
			if(chanNum != 1)
			{
				verboseln(soundFile.getName() + " is not a mono file");
				error = MONO_ERROR;
			}
			Encoding encoding = audioFormat.getEncoding();
			sampleSize = audioFormat.getSampleSizeInBits();
			bigEndian = audioFormat.isBigEndian();
			byteLength = audioFileFormat.getByteLength();
			frameLength = audioFileFormat.getFrameLength();
			sampleRate = audioFormat.getSampleRate();
			sampleRateMs = sampleRate / 1000;
			fileLength = frameLength / sampleRateMs;
			fileType = audioFileFormat.getType().toString();
			System.out.println(" o Reading " + soundFile.getAbsolutePath() + " ...");
			verboseln("   File type : " + fileType);
			verboseln("   Number of channels : " + chanNum);
			verboseln("   Encoding Type : " + encoding);
			verboseln("   Sample size : " + sampleSize + " bits");
			verboseln("   Is BigEndian : " + bigEndian);
			verboseln("   File length : " + byteLength + " bytes");
			verboseln("   Samplerate : " + sampleRateMs + "kHz");
			verboseln("   File length: " + fileLength + " ms <=> " + msToHMSMSSmp(fileLength) + "\n");
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			octets = sampleSize / 8;
			verboseln("   Octets : " + octets);
			audioInputStream.close();
			verboseln("\n>>> File read. <<<\n");
			if(chanNum == 1)
				createMonoWaveforms();
			else
				createWaveforms();
			fine = true;
			done = true;
		} catch (Exception e) {
			error = LOAD_ERROR;
			done = true;
			fine = false;
			e.printStackTrace();
			return;
		}
	}

	private void readSd2File()
	{
		try
		{
			String rsrcname = Ut.dir(soundFile.getAbsolutePath())+"rsrc";
			if(!Ut.MAC)
				rsrcname = soundFile.getParent()+"._"+soundFile.getName();
			File rsrc = new File(rsrcname);
			if(rsrc.exists())
			{
				verboseln("Parsing SdII resource file...");
				// Parse resource file
				InputStream is = new FileInputStream(rsrc);
				// Parse resource header
				byte[] header = new byte[16];
				is.read(header,0,16);
				int offset = 0;
				int dataOffset = Ut.readInt4(header, offset);
				offset += 4;
				int mapOffset = Ut.readInt4(header, offset);
				offset += 4;
				int dataLength = Ut.readInt4(header, offset);
				offset += 4;
				int mapLength = Ut.readInt4(header, offset);
				verboseln("\tdata offset = "+dataOffset);
				verboseln("\tmap offset = "+mapOffset);
				verboseln("\tdata length = "+dataLength);
				verboseln("\tmap length = "+mapLength);
				
				if(dataOffset != 0)
				{
					// Parse resource data
					is.skip(dataOffset-header.length);
					byte[] data = new byte[dataLength];
					is.read(data,0,dataLength);
					verboseln("\tdatas : "+data[0]+" ... "+data[dataLength-1]);
					
					// Parse resource map
					byte[] map = new byte[mapLength];
					is.read(map);
					verboseln("\tmap : "+map[0]+" ... "+map[mapLength-1]);
					
					// SKIP RESOURCE HEADER COPY + HANDLE TO NEXT RESOURCE MAP + FILE REFERENCE NUMBER + RESOURCE FORK ATTRIBUTE
					offset = 16 + 4 + 2 + 2;
					
					int resourceTypeListOffset = Ut.readInt2(map,offset);
					verboseln("\tResource Type List Offset : "+resourceTypeListOffset);
					offset += 2;
					int nameListOffset = Ut.readInt2(map,offset);
					verboseln("\tResource Name List Offset : "+nameListOffset);
					offset += 2;
					int typesNb = Ut.readInt2(map,offset)+1;
					verboseln("\tNumber of types in the map : "+typesNb);
					offset += 2;

					ResourceType[] resTypes = new ResourceType[typesNb];
					
					int resMapOffset = 0;
					int resNameOffset = 0;
					int resNameLength = 0;
					int resDataOffset = 0;
					int resDataLength = 0;
					for(int r = 0 ; r < typesNb ; r++)
					{
						ResourceType resTypeTmp = new ResourceType();
						resTypeTmp.type = Ut.readString(map,offset,4);
						offset += 4;
						resTypeTmp.occNb = Ut.readInt2(map,offset)+1;
						offset += 2;
						resTypeTmp.mapOffset = Ut.readInt2(map,offset);
						offset += 2;
						verboseln("\tResType : "+resTypeTmp.toString());
						resMapOffset = resourceTypeListOffset + resTypeTmp.mapOffset;
						for(int rd = 0 ; rd < resTypeTmp.occNb ; rd++)
						{
							Resource resTmp = new Resource();
							resTmp.id = Ut.readInt2(map,resMapOffset);
							resNameOffset = nameListOffset + Ut.readInt2(map,resMapOffset+2);
							if(resNameOffset < mapLength)
							{
								resNameLength = Ut.readInt1(map,resNameOffset);
								resTmp.name = Ut.readString(map,resNameOffset+1,resNameLength);
							}
							resDataOffset = Ut.readInt3(map,resMapOffset+5);
							resDataLength = Ut.readInt4(data,resDataOffset);
							resDataOffset += 4;
							resTmp.datas = new byte[resDataLength];
							for(int rdb = 0 ; rdb < resDataLength ; rdb++)
								resTmp.datas[rdb] = data[resDataOffset+rdb];
							verboseln("\t\tRes"+rd+" : "+resTmp.toString());
							resMapOffset += 12;
							resTypeTmp.addResource(resTmp);
						}
						resTypes[r] = resTypeTmp;
					}
					is.close();
					// Parsing done
					verboseln("...done");
					
					for(ResourceType resT:resTypes)
					{
						if(resT.type.equalsIgnoreCase("STR "))
						{
							int strNum = resT.resources.size();
							Resource res;
							for(int k = 0 ; k < strNum ; k++)
							{
								res = resT.getResources(k);
								if(res != null)
								{
									if(res.name.equalsIgnoreCase("channels")) {
										chanNum = Integer.parseInt(res.getStringData());
									} else if(res.name.equalsIgnoreCase("sample-rate")) {
										sampleRate = Float.parseFloat(res.getStringData());
									} else if(res.name.equalsIgnoreCase("sample-size")) {
										sampleSize = Integer.parseInt(res.getStringData()) * 8;
									}
								}
							}
						}
					}

					if(chanNum != 1)
					{
						verboseln(soundFile.getName() + " is not a mono file");
						error = MONO_ERROR;
//						pb.dispose();
//						done = true;
//						fine = false;
//						return;
					}
					bigEndian = true;
					sampleRateMs = sampleRate / 1000;
					is = new FileInputStream(soundFile);
					
					byteLength = is.available();
					frameLength = byteLength / chanNum / sampleSize * 8;
					fileLength = frameLength / sampleRateMs;
					fileType = "Sd2f";
					System.out.println(" o Reading " + soundFile.getAbsolutePath() + " ...");
					verboseln("   Frame Length : " + frameLength);
					verboseln("   File type : " + fileType);
					verboseln("   Number of channels : " + chanNum);
					verboseln("   Sample size : " + sampleSize + " bits");
					verboseln("   Is BigEndian : " + bigEndian);
					verboseln("   File length : " + byteLength + " bytes");
					verboseln("   Samplerate : " + sampleRateMs + "kHz");
					verboseln("   File length: " + fileLength + " ms <=> " + msToHMSMSSmp(fileLength) + "\n");
					octets = sampleSize / 8;
					verboseln("   Octets : " + octets);

					is.close();
					if(error > 1)
					{
						done = true;
						fine = false;
						return;
					}
					verboseln("\n>>> File read. <<<\n");
					if(chanNum == 1)
						createMonoWaveforms();
					else
						createWaveforms();
					fine = true;
					done = true;
				} else {
					error = SSDII_RSRC_ERROR;
					is.close();
					done = true;
					fine = false;
				}
			} else {
				error = SSDII_NORSRC_ERROR;
				done = true;
				fine = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = SSDII_RSRC_ERROR;
			done = true;
			fine = false;
		}
	}

	private void createWaveforms()
	{
		hwfs = new HoloWaveForm[1];
		HoloWaveForm hwf = new HoloWaveForm();
		hwf.setSampleRate(sampleRate);
		hwf.setSampleRateMs(sampleRateMs);
		hwf.setFileLength(fileLength);
		hwf.setFileType(fileType);
		hwf.setSoundFile(soundFile);
		hwf.setFine(true);
		hwfs[0] = hwf;
	}

	private void createMonoWaveforms()
	{
		hwfs = new HoloWaveForm[1];
		HoloWaveForm hwf = new HoloWaveForm();
		hwf.setSampleRate(sampleRate);
		hwf.setSampleRateMs(sampleRateMs);
		hwf.setFileLength(fileLength);
		hwf.setFileType(fileType);
		hwf.setSoundFile(soundFile);
		hwf.setFine(true);
		hwfs[0] = hwf;
	}

	
	/********* UTILS *********/
	public String msToHMSMSSmp(float ms)
	{
		float hf = ms / 3600000;
		int h = (int) hf;
		float mf = (hf - h) * 60;
		int m = (int) mf;
		float sf = (mf - m) * 60;
		int s = (int) sf;
		int rms = (int) ((sf - s) * 1000);
		int samples = (int) ((ms - (int) ms) * sampleRateMs);
		return h + ":" + m + ":" + s + "'" + rms + "\"" + samples + "samples";
	}

	public boolean isFine()
	{
		return fine;
	}

	public boolean isDone()
	{
		return done;
	}
	
	public int getError()
	{
		return error;
	}
	
	public void verbose(String s)
	{
		if (VERBOSE)
			System.out.print(s);
	}

	public void verboseln(String s)
	{
		if (VERBOSE)
			System.out.println(s);
	}

	public HoloWaveForm[] getHoloWaveForms()
	{
		return hwfs;
	}

	@SuppressWarnings("deprecation")
	public void stop()
	{
		runner.stop();
	}
}
