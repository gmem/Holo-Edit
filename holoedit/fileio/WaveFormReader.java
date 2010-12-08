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
import holoedit.data.HoloWaveForm;
import holoedit.gui.ProgressBar;
import holoedit.util.MacFileManager;
import holoedit.util.Ut;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;



public class WaveFormReader implements Runnable, Serializable
{
	private static int DOWN_SAMPLING_FACTOR = 576;
	private static final int FRAME_SIZE = 2304; //1152; // must be a multiple of 2 & 3 & 4 & 8 & 16 & 24 & 32
	private final static int VERBOSE = 0;
	private final static int DS_PICK_LAST = 0;
	private final static int DS_MAX = 1;
	private final static int DS_MIN = 2;
	private final static int DS_MEAN = 3;
	private final static int DS_MIN_MAX = 4;
	private final static int DOWN_SAMPLING_TYPE = DS_MIN_MAX;
	private byte[][] multiChannelBuffers = null;
	private byte[] monoBuffer = null;
	/** Pour l'affichage d'une barre de progression pendant l'ouverture du fichier*/
	private transient ProgressBar pb;
	private transient Thread runner;
	// waveforms
	private transient HoloWaveForm[] hwfs;
	// snd file
	public File soundFile;
	private transient File cacheFile;
	/** file last modified */
	private long fileDate;
	// serialisation / tmpfiles
	public transient String cacheDirectory;
	public static final transient String tmpdir = "cache/";
	public static final transient String tmpser = ".cache";
	// state
	private transient boolean fine = false;
	private transient boolean done = false;
	private transient int error = NO_ERROR;
	public final static int USING_CACHE_ERROR = -1;
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
	
	
	private transient HoloEdit holoEditRef;
	// DO NOT MODIFY THIS STRING / USED FOR PARSING SD2F HEADERS
	public static final String resString = "                                !\"#$%&\'()*+,-./0123456789 ;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}  ";

	public WaveFormReader(File f, HoloEdit owner)
	{
		holoEditRef = owner;
		soundFile = f;
		cacheDirectory = Ut.dir(Ut.APP_PATH);
		if(cacheDirectory != null)
		{
			File d = new File(cacheDirectory);
			if(d.exists())
			{
				cacheDirectory += tmpdir;
				d = new File(cacheDirectory);
				if(!d.exists())
					d.mkdir();
			}
			if(soundFile.getName().length() > 32)
			{
				done = true;
				fine = false;
				error = PATH_ERROR;
				return;
			}
			
			cacheFile = new File(soundFile.getAbsoluteFile() + tmpser);
			if(!cacheFile.exists())
				cacheFile = new File(holoEditRef.gestionPistes.holoDirectory + soundFile.getName() + tmpser);
			if(!cacheFile.exists())
				cacheFile = new File(cacheDirectory + soundFile.getName() + tmpser);
			if(!soundFile.exists() && cacheFile.exists())
				error = USING_CACHE_ERROR;
			pb = new ProgressBar("Importing Soundfile...");
			pb.open();
			runner = new Thread(this);
			runner.setName("SF-Import");
			runner.setPriority(Thread.MAX_PRIORITY);
			runner.start();
		}
	}

	public void run()
	{
		long d = Calendar.getInstance().getTimeInMillis();
		try
		{
			if (cacheFile.exists() && cacheFile.isFile())
			{
				System.out.println(" o Reading Tmp File " + cacheFile.getAbsolutePath() + " ...");
				try
				{
					FileInputStream fis = new FileInputStream(cacheFile);
					ObjectInputStream ois = new ObjectInputStream(fis);
					WaveFormReader wfr = (WaveFormReader) ois.readObject();
					ois.close();
					if (soundFile.lastModified() == wfr.fileDate || !soundFile.exists())
					{
						this.fileDate = wfr.fileDate;
						this.fileType = wfr.fileType;
						this.chanNum = wfr.chanNum;
						this.sampleRate = wfr.sampleRate;
						this.sampleRateMs = wfr.sampleRateMs;
						this.sampleSize = wfr.sampleSize;
						this.frameLength = wfr.frameLength;
						this.bigEndian = wfr.bigEndian;
						this.byteLength = wfr.byteLength;
						this.multiChannelBuffers = wfr.multiChannelBuffers;
						this.monoBuffer = wfr.monoBuffer;
						this.octets = wfr.octets;
						this.fileLength = wfr.fileLength;
						if(chanNum == 1)
							createMonoWaveforms();
						else
							createWaveforms();
						done = true;
						fine = true;
						verboseln("   File type : " + fileType);
						verboseln("   Number of channels : " + chanNum);
						verboseln("   Samplerate : " + sampleRateMs + "kHz");
						verboseln("   File length: " + fileLength + " ms <=> " + msToHMSMSSmp(fileLength) + "\n");
						verboseln(">>> File read. <<<\n");
						pb.dispose();
					} else
						verboseln("The file was modified since last import. Importing...\n");
				} catch (InvalidClassException e) {}
			}
			if (!done)
			{
				fileDate = soundFile.lastModified();
				AudioFileFormat audioFileFormat;
				boolean isMacSd2f = false;
				try
				{
					audioFileFormat = AudioSystem.getAudioFileFormat(soundFile);
				}
				catch (UnsupportedAudioFileException uaf)
				{
					if(Ut.MAC)
					{
						MacFileManager OSXFileManager = new MacFileManager(holoEditRef);
						int type = OSXFileManager.getFileType(soundFile.getAbsolutePath());
						String typeString = OSXFileManager.getCode(type);
						//System.out.println("------ type "+OSXFileManager.getCode(type));
						if(typeString.equalsIgnoreCase("Sd2f"))
						{
							isMacSd2f = true;
						}
					}
					if(isMacSd2f || HoloFilenameFilterXP.getExtension(soundFile).equalsIgnoreCase(".sd2"))
					{
						readSd2File();						
						if(error < 2)
						{
							System.out.println(" v "+soundFile.getAbsolutePath() + " imported in " + Ut.msToHMSMS(Calendar.getInstance().getTimeInMillis() - d));
							holoEditRef.room.display();
						}
					} else {
						verboseln(soundFile.getName() + " is not a supported file type");
						error = TYPE_ERROR;
						pb.dispose();
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
//					pb.dispose();
//					done = true;
//					fine = false;
//					return;
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
				verboseln("   Frame Lentgh : " + frameLength);
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
				int byteNum = octets * chanNum * frameLength;
				int finalByteNum = frameLength / DOWN_SAMPLING_FACTOR * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
				if (finalByteNum == 0)
				{
					DOWN_SAMPLING_FACTOR = DOWN_SAMPLING_FACTOR / 10;
					finalByteNum = frameLength / DOWN_SAMPLING_FACTOR * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
				}
				if (finalByteNum == 0)
				{
					pb.dispose();
					return;
				}
				pb.setMaximum(byteNum * 3);
				verboseln("   Total number of bytes : " + byteNum);
				verboseln("   Final total number of bytes : " + finalByteNum);
				try
				{
					if(chanNum == 1)
						monoBuffer = new byte[finalByteNum];
					else
						multiChannelBuffers = new byte[chanNum][finalByteNum];
				}
				catch (OutOfMemoryError e)
				{
					verboseln("   OutOfMemoryError multiChannelBuffers : " + (float) (chanNum * finalByteNum) / 1024 / 1024);
					e.printStackTrace();
					pb.dispose();
					done = true;
					fine = false;
					return;
				}
				int frameNum = 0;
				if(chanNum == 1)
				{
					while (FRAME_SIZE * 2 < audioInputStream.available())
						error = treatMonoFrame(audioInputStream, frameNum++);
					if (audioInputStream.available() > 0)
						error = treatMonoFrame(audioInputStream, frameNum);
				} else {
					while (FRAME_SIZE * 2 * chanNum < audioInputStream.available())
						error = treatFrame(audioInputStream, frameNum++);
					if (audioInputStream.available() > 0)
						error = treatFrame(audioInputStream, frameNum);
				}
				audioInputStream.close();
				if(error > 1)
				{
					pb.dispose();
					done = true;
					fine = false;
					return;
				}
				verboseln("\n>>> File read. <<<\n");
				verbose("   >>> Saving datas ... ");
				FileOutputStream fos = new FileOutputStream(cacheFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(this);
				oos.flush();
				oos.close();
				verboseln("done.\n");
				verboseln("   WaveForm datas saved in : " + cacheFile.getPath() + "\n");
				if(chanNum == 1)
					createMonoWaveforms();
				else
					createWaveforms();
				fine = true;
				done = true;
				pb.dispose();
			}
			if(error < 2)
			{
				System.out.println(" v "+soundFile.getAbsolutePath() + " imported in " + Ut.msToHMSMS(Calendar.getInstance().getTimeInMillis() - d));
				holoEditRef.room.display();
			}
		}
		catch (Exception e)
		{
			error = LOAD_ERROR;
			pb.dispose();
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
			// TODOT test %s.AppleDouble/%s
			String rsrcname = Ut.dir(soundFile.getAbsolutePath())+"rsrc";
			if(!Ut.MAC)
				rsrcname = soundFile.getParent()+"._"+soundFile.getName();
			File rsrc = new File(rsrcname);
			if(rsrc.exists())
			{
				verboseln("Parsing SdII resource file...");
				// Parse resource file
				InputStream is = new FileInputStream(rsrc);
//				int cpt = 0;
//				verboseln("__________________________");
//				while(is.available()>0)
//				{
////					String tt = Integer.toHexString(is.read()).toUpperCase();
////					if(tt.length() == 1) tt = '0'+tt;
//					String tt = " "+is.read();
//					if(cpt==15)
//					{
//						verboseln(tt);
//						cpt = 0;
//					} else {
//						verbose(tt);
//						cpt++;
//					}
//				}
//				verboseln("");
//				verboseln("__________________________");
//				is.close();
//				is = new FileInputStream(rsrc);
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
				verboseln2("\tdata offset = "+dataOffset);
				verboseln2("\tmap offset = "+mapOffset);
				verboseln2("\tdata length = "+dataLength);
				verboseln2("\tmap length = "+mapLength);
				
				if(dataOffset != 0)
				{
					// Parse resource data
					is.skip(dataOffset-header.length);
					byte[] data = new byte[dataLength];
					is.read(data,0,dataLength);
					verboseln2("\tdatas : "+data[0]+" ... "+data[dataLength-1]);
					
					// Parse resource map
					byte[] map = new byte[mapLength];
					is.read(map);
					verboseln2("\tmap : "+map[0]+" ... "+map[mapLength-1]);
					
					// SKIP RESOURCE HEADER COPY + HANDLE TO NEXT RESOURCE MAP + FILE REFERENCE NUMBER + RESOURCE FORK ATTRIBUTE
					offset = 16 + 4 + 2 + 2;
					
					int resourceTypeListOffset = Ut.readInt2(map,offset);
					verboseln2("\tResource Type List Offset : "+resourceTypeListOffset);
					offset += 2;
					int nameListOffset = Ut.readInt2(map,offset);
					verboseln2("\tResource Name List Offset : "+nameListOffset);
					offset += 2;
					int typesNb = Ut.readInt2(map,offset)+1;
					verboseln2("\tNumber of types in the map : "+typesNb);
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
						verboseln2("\tResType : "+resTypeTmp.toString());
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
							verboseln2("\t\tRes"+rd+" : "+resTmp.toString());
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
					int byteNum = octets * chanNum * frameLength;
					int finalByteNum = frameLength / DOWN_SAMPLING_FACTOR * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
					if (finalByteNum == 0)
					{
						DOWN_SAMPLING_FACTOR = DOWN_SAMPLING_FACTOR / 10;
						finalByteNum = frameLength / DOWN_SAMPLING_FACTOR * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
					}
					if (finalByteNum == 0)
					{
						pb.dispose();
						return;
					}
					pb.setMaximum(byteNum * 3);
					verboseln("   Total number of bytes : " + byteNum);
					verboseln("   Final total number of bytes : " + finalByteNum);
					try
					{
						if(chanNum == 1)
							monoBuffer = new byte[finalByteNum];
						else
							multiChannelBuffers = new byte[chanNum][finalByteNum];
					}
					catch (OutOfMemoryError e)
					{
						verboseln("   OutOfMemoryError multiChannelBuffers : " + (float) (chanNum * finalByteNum) / 1024 / 1024);
						e.printStackTrace();
						pb.dispose();
						done = true;
						fine = false;
						return;
					}
					int frameNum = 0;
					if(chanNum == 1)
					{
						while (FRAME_SIZE * 2 < is.available())
							error = treatSd2MonoFrame(is, frameNum++);
						if (is.available() > 0)
							error = treatSd2MonoFrame(is, frameNum);
					} else {
						while (FRAME_SIZE * 2 * chanNum < is.available())
							error = treatSd2Frame(is, frameNum++);
						if (is.available() > 0)
							error = treatSd2Frame(is, frameNum);
					}
					is.close();
					if(error > 1)
					{
						pb.dispose();
						done = true;
						fine = false;
						return;
					}
					verboseln("\n>>> File read. <<<\n");
					verbose("   >>> Saving datas ... ");
					FileOutputStream fos = new FileOutputStream(cacheFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(this);
					oos.flush();
					oos.close();
					verboseln("done.\n");
					verboseln("   WaveForm datas saved in : " + cacheFile.getPath() + "\n");
					if(chanNum == 1)
						createMonoWaveforms();
					else
						createWaveforms();
					fine = true;
					done = true;
					pb.dispose();
				} else {
					error = SSDII_RSRC_ERROR;
					is.close();
					pb.dispose();
					done = true;
					fine = false;
				}
			} else {
				error = SSDII_NORSRC_ERROR;
				pb.dispose();
				done = true;
				fine = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = SSDII_RSRC_ERROR;
			pb.dispose();
			done = true;
			fine = false;
		}
	}

	private void createWaveforms()
	{
//		hwfs = new HoloWaveForm[chanNum];
//		for (int i = 0; i < chanNum; i++)
//		{
//			HoloWaveForm hwf = new HoloWaveForm();
//			hwf.setChannel(i + 1);
//			hwf.setSampleRate(sampleRate);
//			hwf.setSampleRateMs(sampleRateMs);
//			hwf.setFileLength(fileLength);
//			hwf.setFileType(fileType);
//			hwf.setWaveBuffer(multiChannelBuffers[i]);
//			hwf.setSoundFile(soundFile);
//			hwf.setFine(true);
//			hwfs[i] = hwf;
//		}
		hwfs = new HoloWaveForm[1];
		HoloWaveForm hwf = new HoloWaveForm();
//		hwf.setChannel(1);
		hwf.setSampleRate(sampleRate);
		hwf.setSampleRateMs(sampleRateMs);
		hwf.setFileLength(fileLength);
		hwf.setFileType(fileType);
		// In Sound Designer II file format, the first track is the right one
		// In any case we read the left track.
		hwf.setWaveBuffer(multiChannelBuffers[chanNum == 2 && fileType.equalsIgnoreCase("Sd2f") ? 1 : 0]);
		hwf.setSoundFile(soundFile);
		hwf.setFine(true);
		hwfs[0] = hwf;
	}

	private void createMonoWaveforms()
	{
		hwfs = new HoloWaveForm[1];
		HoloWaveForm hwf = new HoloWaveForm();
//		hwf.setChannel(1);
		hwf.setSampleRate(sampleRate);
		hwf.setSampleRateMs(sampleRateMs);
		hwf.setFileLength(fileLength);
		hwf.setFileType(fileType);
		hwf.setWaveBuffer(monoBuffer);
		hwf.setSoundFile(soundFile);
		hwf.setFine(true);
		hwfs[0] = hwf;
	}
	
	private int treatFrame(AudioInputStream audioInputStream, int frameNum) throws IOException
	{
		byte[] byteBuffer;
		byte[] multiplexedAudioData;
		byte[][] bigMultiChannelBuffers;
		AudioFormat format = audioInputStream.getFormat();
		int size;
		int[] offset;
		size = (FRAME_SIZE * chanNum) < audioInputStream.available() ? (FRAME_SIZE * chanNum) : audioInputStream.available();
		if (size == 0)
			return 0;
		int offcount = (int) ((float) (frameNum * FRAME_SIZE) / DOWN_SAMPLING_FACTOR) * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
		offset = new int[chanNum];
		verboseln2("\n  Treating frame " + frameNum + " ...");
		for (int i = 0; i < chanNum; i++)
			offset[i] = offcount;
		byteBuffer = new byte[size];
		verboseln2("\n  Size " + size);
		verbose2("   >>> Reading bytes ... ");
		audioInputStream.read(byteBuffer, 0, size);
		verboseln2("done.\n");
		pb.inc(size);
		verboseln2("   Final number of bits per samples : 8");
		verbose2("   >>> Down Quantization in progress ... ");
		int nlengthInSamples;
		double mult;
		switch (octets)
		{
		case 4:
			//			verboseln2("32 bits");
			nlengthInSamples = byteBuffer.length / 4;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 32);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 4;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i + 0] << 24) | ((byteBuffer[4 * i + 1] & 0xFF) << 16) | ((byteBuffer[4 * i + 2] & 0xFF) << 8) | (byteBuffer[4 * i + 3] & 0xFF);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i] & 0xFF) | ((byteBuffer[4 * i + 1] & 0xFF) << 8) | ((byteBuffer[4 * i + 2] & 0xFF) << 16) | (byteBuffer[4 * i + 3] << 24);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			}
			break;
		case 3:
			//			verboseln2("24 bits");
			nlengthInSamples = byteBuffer.length / 3;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 24);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 3;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] << 16) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] & 0xFF);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] & 0xFF) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] << 16);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			}
			break;
		case 2:
			nlengthInSamples = byteBuffer.length / 2;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 16);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 2;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is MSB (high order) */
					int MSB = byteBuffer[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = byteBuffer[2 * i + 1];
					multiplexedAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is LSB (low order) */
					int LSB = byteBuffer[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = byteBuffer[2 * i + 1];
					multiplexedAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			}
			break;
		case 1:
			nlengthInSamples = byteBuffer.length;
			multiplexedAudioData = new byte[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN"))
			{
				for (int i = 0; i < byteBuffer.length; i++)
				{
					multiplexedAudioData[i] = byteBuffer[i];
					pb.inc();
				}
			} else
			{
				for (int i = 0; i < byteBuffer.length; i++)
				{
					multiplexedAudioData[i] = (byte) (byteBuffer[i] - 128);
					pb.inc();
				}
			}
			break;
		default:
			return OCTET_ERROR;
		}
		verboseln2("done.\n");
		verboseln2("   Number of channels : " + chanNum);
		int chanRealSize = (int) ((float) multiplexedAudioData.length / chanNum);
		verboseln2("   Total number of samples : " + multiplexedAudioData.length + " samples (byte)");
		verboseln2("   Real number of samples per channel : " + chanRealSize + " samples (byte)");
		verbose2("   >>> MultiChannel Extraction in progress ... ");
		bigMultiChannelBuffers = new byte[chanNum][chanRealSize];
		int cpt = 0;
		for (int i = 0; i + chanNum <= chanRealSize * chanNum; i += chanNum)
		{
			for (int k = 0; k < chanNum; k++)
			{
				bigMultiChannelBuffers[k][cpt] = multiplexedAudioData[i + k];
				pb.inc();
			}
			cpt++;
		}
		verboseln2("done.\n");
		int chanSize = chanRealSize / DOWN_SAMPLING_FACTOR;
		if (DOWN_SAMPLING_TYPE == DS_MIN_MAX)
			chanSize = chanSize * 2;
		verboseln2("   Final number of samples per channel : " + chanSize + " samples (bytes)");
		verboseln2("   Downsampling factor : " + DOWN_SAMPLING_FACTOR);
		verboseln2("   Downsampling type : " + DOWN_SAMPLING_TYPE);
		verboseln2("   >>> Down Sampling in progress ... ");
		for (int ch = 0; ch < chanNum; ch++)
		{
			if (DOWN_SAMPLING_TYPE != DS_MIN_MAX)
			{
				for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i += DOWN_SAMPLING_FACTOR)
				{
					byte tmp = 0;
					if (DOWN_SAMPLING_TYPE == DS_PICK_LAST)
					{
						tmp = bigMultiChannelBuffers[ch][i + DOWN_SAMPLING_FACTOR - 1];
						pb.inc(DOWN_SAMPLING_FACTOR);
					} else
						for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
						{
							switch (DOWN_SAMPLING_TYPE)
							{
							case DS_PICK_LAST:
								break;
							case DS_MAX:
								tmp = max(tmp, abs(bigMultiChannelBuffers[ch][i + p]));
								break;
							case DS_MIN:
								tmp = min(tmp, abs(bigMultiChannelBuffers[ch][i + p]));
							case DS_MEAN:
								tmp = mean(tmp, bigMultiChannelBuffers[ch][i + p]);
								break;
							}
							pb.inc();
						}
					try
					{
						multiChannelBuffers[ch][offset[ch]] = tmp;
					}
					catch (ArrayIndexOutOfBoundsException e)
					{
						verboseln2("ArrayIndexOutOfBoundsException ch:" + ch + " offset:" + offset[ch] + " l:" + multiChannelBuffers.length + " l.l:" + multiChannelBuffers[ch].length);
						e.printStackTrace();
						return INDEX_ERROR;
					}
					offset[ch]++;
				}
			} else
			{
				for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i = i + DOWN_SAMPLING_FACTOR)
				{
					byte min = 127, max = -127;
					for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
					{
						min = min(min, bigMultiChannelBuffers[ch][i + p]);
						max = max(max, bigMultiChannelBuffers[ch][i + p]);
					}
					pb.inc(DOWN_SAMPLING_FACTOR);
//					System.out.println("offset[ch] : "+offset[ch]+" Min : "+min+" Max : "+max);
					try
					{
						multiChannelBuffers[ch][offset[ch]] = min;
						offset[ch]++;
						multiChannelBuffers[ch][offset[ch]] = max;
						offset[ch]++;
					}
					catch (ArrayIndexOutOfBoundsException e)
					{
						verboseln2("ArrayIndexOutOfBoundsException ch:" + ch + " offset:" + offset[ch] + " l:" + multiChannelBuffers.length + " l.l:" + multiChannelBuffers[ch].length);
						e.printStackTrace();
						return INDEX_ERROR;
					}
					
				}
			}
		}
		verboseln2("   ... done.\n");
		verboseln2("  Frame " + frameNum + " treatment done.\n");
		return 0;
	}

	private int treatSd2Frame(InputStream is, int frameNum) throws IOException
	{
		byte[] byteBuffer;
		byte[] multiplexedAudioData;
		byte[][] bigMultiChannelBuffers;
		int size;
		int[] offset;
		size = (FRAME_SIZE * chanNum) < is.available() ? (FRAME_SIZE * chanNum) : is.available();
		if (size == 0)
			return 0;
		int offcount = (int) ((float) (frameNum * FRAME_SIZE) / DOWN_SAMPLING_FACTOR) * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
		offset = new int[chanNum];
		verboseln2("\n  Treating frame " + frameNum + " ...");
		for (int i = 0; i < chanNum; i++)
			offset[i] = offcount;
		byteBuffer = new byte[size];
		verboseln2("\n  Size " + size);
		verbose2("   >>> Reading bytes ... ");
		is.read(byteBuffer, 0, size);
		verboseln2("done.\n");
		pb.inc(size);
		verboseln2("   Final number of bits per samples : 8");
		verbose2("   >>> Down Quantization in progress ... ");
		int nlengthInSamples;
		double mult;
		switch (octets)
		{
		case 4:
			//			verboseln2("32 bits");
			nlengthInSamples = byteBuffer.length / 4;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 32);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 4;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i + 0] << 24) | ((byteBuffer[4 * i + 1] & 0xFF) << 16) | ((byteBuffer[4 * i + 2] & 0xFF) << 8) | (byteBuffer[4 * i + 3] & 0xFF);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i] & 0xFF) | ((byteBuffer[4 * i + 1] & 0xFF) << 8) | ((byteBuffer[4 * i + 2] & 0xFF) << 16) | (byteBuffer[4 * i + 3] << 24);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			}
			break;
		case 3:
			//			verboseln2("24 bits");
			nlengthInSamples = byteBuffer.length / 3;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 24);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 3;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] << 16) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] & 0xFF);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] & 0xFF) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] << 16);
					multiplexedAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			}
			break;
		case 2:
			nlengthInSamples = byteBuffer.length / 2;
			multiplexedAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 16);
			for (int i = 0; i < chanNum; i++)
				offset[i] = offset[i] / 2;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is MSB (high order) */
					int MSB = byteBuffer[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = byteBuffer[2 * i + 1];
					multiplexedAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is LSB (low order) */
					int LSB = byteBuffer[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = byteBuffer[2 * i + 1];
					multiplexedAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			}
			break;
		case 1:
			nlengthInSamples = byteBuffer.length;
			multiplexedAudioData = new byte[nlengthInSamples];
//			// SIGNED
//			for (int i = 0; i < byteBuffer.length; i++)
//			{
//				multiplexedAudioData[i] = byteBuffer[i];
//				pb.inc();
//			}
			for (int i = 0; i < byteBuffer.length; i++)
			{
				multiplexedAudioData[i] = (byte) (byteBuffer[i] - 128);
				pb.inc();
			}
			break;
		default:
			return OCTET_ERROR;
		}
		verboseln2("done.\n");
		verboseln2("   Number of channels : " + chanNum);
		int chanRealSize = (int) ((float) multiplexedAudioData.length / chanNum);
		verboseln2("   Total number of samples : " + multiplexedAudioData.length + " samples (byte)");
		verboseln2("   Real number of samples per channel : " + chanRealSize + " samples (byte)");
		verbose2("   >>> MultiChannel Extraction in progress ... ");
		bigMultiChannelBuffers = new byte[chanNum][chanRealSize];
		int cpt = 0;
		for (int i = 0; i + chanNum <= chanRealSize * chanNum; i += chanNum)
		{
			for (int k = 0; k < chanNum; k++)
			{
				bigMultiChannelBuffers[k][cpt] = multiplexedAudioData[i + k];
				pb.inc();
			}
			cpt++;
		}
		verboseln2("done.\n");
		int chanSize = chanRealSize / DOWN_SAMPLING_FACTOR;
		if (DOWN_SAMPLING_TYPE == DS_MIN_MAX)
			chanSize = chanSize * 2;
		verboseln2("   Final number of samples per channel : " + chanSize + " samples (bytes)");
		verboseln2("   Downsampling factor : " + DOWN_SAMPLING_FACTOR);
		verboseln2("   Downsampling type : " + DOWN_SAMPLING_TYPE);
		verboseln2("   >>> Down Sampling in progress ... ");
		for (int ch = 0; ch < chanNum; ch++)
		{
			if (DOWN_SAMPLING_TYPE != DS_MIN_MAX)
			{
				for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i += DOWN_SAMPLING_FACTOR)
				{
					byte tmp = 0;
					if (DOWN_SAMPLING_TYPE == DS_PICK_LAST)
					{
						tmp = bigMultiChannelBuffers[ch][i + DOWN_SAMPLING_FACTOR - 1];
						pb.inc(DOWN_SAMPLING_FACTOR);
					} else
						for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
						{
							switch (DOWN_SAMPLING_TYPE)
							{
							case DS_PICK_LAST:
								break;
							case DS_MAX:
								tmp = max(tmp, abs(bigMultiChannelBuffers[ch][i + p]));
								break;
							case DS_MIN:
								tmp = min(tmp, abs(bigMultiChannelBuffers[ch][i + p]));
							case DS_MEAN:
								tmp = mean(tmp, bigMultiChannelBuffers[ch][i + p]);
								break;
							}
							pb.inc();
						}
					try
					{
						multiChannelBuffers[ch][offset[ch]] = tmp;
					}
					catch (ArrayIndexOutOfBoundsException e)
					{
						verboseln2("ArrayIndexOutOfBoundsException ch:" + ch + " offset:" + offset[ch] + " l:" + multiChannelBuffers.length + " l.l:" + multiChannelBuffers[ch].length);
						e.printStackTrace();
						return INDEX_ERROR;
					}
					offset[ch]++;
				}
			} else
			{
				for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i = i + DOWN_SAMPLING_FACTOR)
				{
					byte min = 127, max = -127;
					for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
					{
						min = min(min, bigMultiChannelBuffers[ch][i + p]);
						max = max(max, bigMultiChannelBuffers[ch][i + p]);
					}
					pb.inc(DOWN_SAMPLING_FACTOR);
//					System.out.println("offset[ch] : "+offset[ch]+" Min : "+min+" Max : "+max);
					try
					{
						multiChannelBuffers[ch][offset[ch]] = min;
						offset[ch]++;
						multiChannelBuffers[ch][offset[ch]] = max;
						offset[ch]++;
					}
					catch (ArrayIndexOutOfBoundsException e)
					{
						verboseln2("ArrayIndexOutOfBoundsException ch:" + ch + " offset:" + offset[ch] + " l:" + multiChannelBuffers.length + " l.l:" + multiChannelBuffers[ch].length);
						e.printStackTrace();
						return INDEX_ERROR;
					}
					
				}
			}
		}
		verboseln2("   ... done.\n");
		verboseln2("  Frame " + frameNum + " treatment done.\n");
		return 0;
	}
	
	private int treatMonoFrame(AudioInputStream audioInputStream, int frameNum) throws IOException
	{
		byte[] byteBuffer;
		byte[] monoAudioData;
		AudioFormat format = audioInputStream.getFormat();
		int size;
		int offset;
		size = FRAME_SIZE < audioInputStream.available() ? FRAME_SIZE : audioInputStream.available();
		if (size == 0)
			return 0;
		int offcount = (int) ((float) (frameNum * FRAME_SIZE) / DOWN_SAMPLING_FACTOR) * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
		verboseln2("\n  Treating frame " + frameNum + " ...");
		offset = offcount;
		byteBuffer = new byte[size];
		verboseln2("\n  Size " + size);
		verbose2("   >>> Reading bytes ... ");
		audioInputStream.read(byteBuffer, 0, size);
		verboseln2("done.\n");
		pb.inc(size);
		verboseln2("   Final number of bits per samples : 8");
		verbose2("   >>> Down Quantization in progress ... ");
		int nlengthInSamples;
		double mult;
		switch (octets)
		{
		case 4:
			nlengthInSamples = byteBuffer.length / 4;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 32);
			offset = offset / 4;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i + 0] << 24) | ((byteBuffer[4 * i + 1] & 0xFF) << 16) | ((byteBuffer[4 * i + 2] & 0xFF) << 8) | (byteBuffer[4 * i + 3] & 0xFF);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i] & 0xFF) | ((byteBuffer[4 * i + 1] & 0xFF) << 8) | ((byteBuffer[4 * i + 2] & 0xFF) << 16) | (byteBuffer[4 * i + 3] << 24);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			}
			break;
		case 3:
						verboseln2("24 bits");
			nlengthInSamples = byteBuffer.length / 3;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 24);
			offset = offset / 3;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] << 16) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] & 0xFF);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] & 0xFF) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] << 16);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			}
			break;
		case 2:
			nlengthInSamples = byteBuffer.length / 2;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 16);
			offset = offset / 2;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is MSB (high order) */
					int MSB = byteBuffer[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = byteBuffer[2 * i + 1];
					monoAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is LSB (low order) */
					int LSB = byteBuffer[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = byteBuffer[2 * i + 1];
					monoAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			}
			break;
		case 1:
			nlengthInSamples = byteBuffer.length;
			monoAudioData = new byte[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN"))
			{
				for (int i = 0; i < byteBuffer.length; i++)
				{
					monoAudioData[i] = byteBuffer[i];
					pb.inc();
				}
			} else
			{
				for (int i = 0; i < byteBuffer.length; i++)
				{
					monoAudioData[i] = (byte) (byteBuffer[i] - 128);
					pb.inc();
				}
			}
			break;
		default:
			return OCTET_ERROR;
		}
		verboseln2("done.\n");
		int chanRealSize = monoAudioData.length;
		verboseln("           chanrealsize"+chanRealSize);
		verboseln2("   Total number of samples : " + monoAudioData.length + " samples (byte)");
		verboseln2("   Real number of samples per channel : " + chanRealSize + " samples (byte)");
		verboseln2("done.\n");
		int chanSize = chanRealSize / DOWN_SAMPLING_FACTOR;
		if (DOWN_SAMPLING_TYPE == DS_MIN_MAX)
			chanSize = chanSize * 2;
		verboseln2("   Final number of samples per channel : " + chanSize + " samples (bytes)");
		verboseln2("   Downsampling factor : " + DOWN_SAMPLING_FACTOR);
		verboseln2("   Downsampling type : " + DOWN_SAMPLING_TYPE);
		verboseln2("   >>> Down Sampling in progress ... ");
		if (DOWN_SAMPLING_TYPE != DS_MIN_MAX)
		{
			for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i += DOWN_SAMPLING_FACTOR)
			{
				byte tmp = 0;
				if (DOWN_SAMPLING_TYPE == DS_PICK_LAST)
				{
					tmp = monoAudioData[i + DOWN_SAMPLING_FACTOR - 1];
					pb.inc(DOWN_SAMPLING_FACTOR);
				} else
					for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
					{
						switch (DOWN_SAMPLING_TYPE)
						{
						case DS_PICK_LAST:
							break;
						case DS_MAX:
							tmp = max(tmp, abs(monoAudioData[i + p]));
							break;
						case DS_MIN:
							tmp = min(tmp, abs(monoAudioData[i + p]));
						case DS_MEAN:
							tmp = mean(tmp, monoAudioData[i + p]);
							break;
						}
						pb.inc();
					}
				try
				{
					monoBuffer[offset] = tmp;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					verboseln2("ArrayIndexOutOfBoundsException offset:" + offset + " l:" + monoBuffer.length);
					e.printStackTrace();
					return INDEX_ERROR;
				}
				offset++;
			}
		} else
		{	
			for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i = i + DOWN_SAMPLING_FACTOR)
			{
				verboseln(" diandpoaindoandaozndonodazn");
				byte min = 127, max = -127;
				for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
				{
					min = min(min, monoAudioData[i + p]);
					max = max(max, monoAudioData[i + p]);
				}
				pb.inc(DOWN_SAMPLING_FACTOR);
//					System.out.println("offset[ch] : "+offset[ch]+" Min : "+min+" Max : "+max);
				try
				{
					monoBuffer[offset] = min;
					offset++;
					monoBuffer[offset] = max;
					offset++;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					verboseln2("ArrayIndexOutOfBoundsException offset:" + offset + " l:" + monoBuffer.length);
					e.printStackTrace();
					return INDEX_ERROR;
				}
				verboseln("min max" + min + " " + max);	
			}
		}
		verboseln2("   ... done.\n");
		verboseln2("  Frame " + frameNum + " treatment done.\n");
		
		return 0;
	}
	
	private int treatSd2MonoFrame(InputStream is, int frameNum) throws IOException
	{
		byte[] byteBuffer;
		byte[] monoAudioData;
		int size;
		int offset;
		size = FRAME_SIZE < is.available() ? FRAME_SIZE : is.available();
		if (size == 0)
			return 0;
		int offcount = (int) ((float) (frameNum * FRAME_SIZE) / DOWN_SAMPLING_FACTOR) * (DOWN_SAMPLING_TYPE == DS_MIN_MAX ? 2 : 1);
		verboseln2("\n  Treating frame " + frameNum + " ...");
		offset = offcount;
		byteBuffer = new byte[size];
		verboseln2("\n  Size " + size);
		verbose2("   >>> Reading bytes ... ");
		is.read(byteBuffer, 0, size);
		verboseln2("done.\n");
		pb.inc(size);
		verboseln2("   Final number of bits per samples : 8");
		verbose2("   >>> Down Quantization in progress ... ");
		int nlengthInSamples;
		double mult;
		switch (octets)
		{
		case 4:
			nlengthInSamples = byteBuffer.length / 4;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 32);
			offset = offset / 4;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i + 0] << 24) | ((byteBuffer[4 * i + 1] & 0xFF) << 16) | ((byteBuffer[4 * i + 2] & 0xFF) << 8) | (byteBuffer[4 * i + 3] & 0xFF);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[4 * i] & 0xFF) | ((byteBuffer[4 * i + 1] & 0xFF) << 8) | ((byteBuffer[4 * i + 2] & 0xFF) << 16) | (byteBuffer[4 * i + 3] << 24);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(4);
				}
			}
			break;
		case 3:
			//	verboseln2("24 bits");
			nlengthInSamples = byteBuffer.length / 3;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 24);
			offset = offset / 3;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] << 16) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] & 0xFF);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			} else
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					int sample = (byteBuffer[3 * i] & 0xFF) | ((byteBuffer[3 * i + 1] & 0xFF) << 8) | (byteBuffer[3 * i + 2] << 16);
					monoAudioData[i] = (byte) (sample * mult);
					pb.inc(3);
				}
			}
			break;
		case 2:
			nlengthInSamples = byteBuffer.length / 2;
			monoAudioData = new byte[nlengthInSamples];
			mult = Math.pow(2, 8) / Math.pow(2, 16);
			offset = offset / 2;
			if (bigEndian)
			{
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is MSB (high order) */
					int MSB = byteBuffer[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = byteBuffer[2 * i + 1];
					monoAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			} else {
				for (int i = 0; i < nlengthInSamples; i++)
				{
					/* First byte is LSB (low order) */
					int LSB = byteBuffer[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = byteBuffer[2 * i + 1];
					monoAudioData[i] = (byte) ((MSB << 8 | (255 & LSB)) * mult);
					pb.inc(2);
				}
			}
			break;
		case 1:
			nlengthInSamples = byteBuffer.length;
			monoAudioData = new byte[nlengthInSamples];
//			SIGNED
//			for (int i = 0; i < byteBuffer.length; i++)
//			{
//				monoAudioData[i] = byteBuffer[i];
//				pb.inc();
//			}
			for (int i = 0; i < byteBuffer.length; i++)
			{
				monoAudioData[i] = (byte) (byteBuffer[i] - 128);
				pb.inc();
			}
			break;
		default:
			return OCTET_ERROR;
		}
		verboseln2("done.\n");
		int chanRealSize = monoAudioData.length;
		verboseln2("   Total number of samples : " + monoAudioData.length + " samples (byte)");
		verboseln2("   Real number of samples per channel : " + chanRealSize + " samples (byte)");
		verboseln2("done.\n");
		int chanSize = chanRealSize / DOWN_SAMPLING_FACTOR;
		if (DOWN_SAMPLING_TYPE == DS_MIN_MAX)
			chanSize = chanSize * 2;
		verboseln2("   Final number of samples per channel : " + chanSize + " samples (bytes)");
		verboseln2("   Downsampling factor : " + DOWN_SAMPLING_FACTOR);
		verboseln2("   Downsampling type : " + DOWN_SAMPLING_TYPE);
		verboseln2("   >>> Down Sampling in progress ... ");
		if (DOWN_SAMPLING_TYPE != DS_MIN_MAX)
		{
			for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i += DOWN_SAMPLING_FACTOR)
			{
				byte tmp = 0;
				if (DOWN_SAMPLING_TYPE == DS_PICK_LAST)
				{
					tmp = monoAudioData[i + DOWN_SAMPLING_FACTOR - 1];
					pb.inc(DOWN_SAMPLING_FACTOR);
				} else
					for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
					{
						switch (DOWN_SAMPLING_TYPE)
						{
						case DS_PICK_LAST:
							break;
						case DS_MAX:
							tmp = max(tmp, abs(monoAudioData[i + p]));
							break;
						case DS_MIN:
							tmp = min(tmp, abs(monoAudioData[i + p]));
						case DS_MEAN:
							tmp = mean(tmp, monoAudioData[i + p]);
							break;
						}
						pb.inc();
					}
				try
				{
					monoBuffer[offset] = tmp;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					verboseln2("ArrayIndexOutOfBoundsException offset:" + offset + " l:" + monoBuffer.length);
					e.printStackTrace();
					return INDEX_ERROR;
				}
				offset++;
			}
		} else {
			for (int i = 0; i + DOWN_SAMPLING_FACTOR <= chanRealSize; i = i + DOWN_SAMPLING_FACTOR)
			{
				byte min = 127, max = -127;
				for (int p = 0; p < DOWN_SAMPLING_FACTOR; p++)
				{
					min = min(min, monoAudioData[i + p]);
					max = max(max, monoAudioData[i + p]);
				}
				pb.inc(DOWN_SAMPLING_FACTOR);
//				System.out.println("offset[ch] : "+offset[ch]+" Min : "+min+" Max : "+max);
				try
				{
					monoBuffer[offset] = min;
					offset++;
					monoBuffer[offset] = max;
					offset++;
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					verboseln2("ArrayIndexOutOfBoundsException offset:" + offset + " l:" + monoBuffer.length);
					e.printStackTrace();
					return INDEX_ERROR;
				}
				
			}
		}
		verboseln2("   ... done.\n");
		verboseln2("  Frame " + frameNum + " treatment done.\n");
		return 0;
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
		if (VERBOSE > 0)
			System.out.print(s);
	}

	public void verbose2(String s)
	{
		if (VERBOSE > 1)
			System.out.print(s);
	}
	
	public void verboseln(String s)
	{
		if (VERBOSE > 0)
			System.out.println(s);
	}

	public void verboseln2(String s)
	{
		if (VERBOSE > 1)
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
	
	public static byte abs(byte b)
	{
		return (b >= 0 ? b : (byte) (-1 * b));
	}

	public static byte max(byte b1, byte b2)
	{
		return (b1 >= b2 ? b1 : b2);
	}

	public static byte min(byte b1, byte b2)
	{
		return (b1 <= b2 ? b1 : b2);
	}

	public static byte mean(byte b1, byte b2)
	{
		return (byte) ((float) (b1 + b2) / 2);
	}

	public static class ResourceType
	{
		String type;
		int occNb;
		int mapOffset;
		HashMap<Integer,Resource> resources = new HashMap<Integer,Resource>();
		
		public String toString()
		{
			return "\'"+type+"\' "+occNb+" "+mapOffset;
		}
		
		public void addResource(Resource res)
		{
			resources.put(resources.size(),res);
		}
		
		public Resource getResources(int i)
		{
			return resources.get(i);
		}
	}
	
	public static class Resource
	{
		int id;
		String name = "unnamed";
		byte[] datas;
		
		public String toString()
		{
			return id + " <" + name + "> <" + getStringData() +">";
		}

		public String getStringData()
		{
			String tmp = "";
			boolean throwFirst = true;
			for(byte b:datas)
			{
				if(!throwFirst)
				{
					int hexV = b >= 0 ? b : 256 + b;
					tmp += hexV < resString.length() && hexV > 0 ? resString.substring(hexV-1,hexV) : " ";
				} else
					throwFirst = false;
			}
			return tmp;
		}
	}
}