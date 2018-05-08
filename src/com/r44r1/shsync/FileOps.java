/*
 * © Copyright 2018 r44r1 [claie@aol.jp]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.r44r1.shsync;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.Statement;
import java.text.DecimalFormat;

public class FileOps {
	private static final String FILETYPES = "wma|WMA|mp3|MP3|aac|AAC|wav|WAV|ac3|AC3|amr|AMR|m4a|M4A|ape|APE|flac|FLAC|ogg|OGG|oga|OGA";
	private static String dbPath;
	private static String songsRoot;
	private static final String fsS = File.separator;

	public static String openSD (Shell shell) {
		String sdPath;
		DirectoryDialog dialog = new DirectoryDialog(shell);
		sdPath = dialog.open();
		while (sdPath == null) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Error");
			messageBox.setMessage("You must set phone path");
			messageBox.open();
			sdPath = dialog.open();
		};
		return sdPath;
	}
	
	public static String getPath (String type){
		// USAGE: getPath("songs") or getPath("db") - returns path to file (OS-specific)
		String path = new String ();
		String root = MainWindow.getPhonePath();
		//Java 6 doesn't support switch with strings
		if (type.equals("songs")) path = root+"PRIVATE"+fsS+"DOCOMO"+fsS+"MMFILE"+fsS+"WM"+fsS;
			else if (type.equals("db")) path = root+"PRIVATE"+fsS+"DOCOMO"+fsS+"MMFILE"+fsS+"WM_SYSTEM"+fsS+"ldb.db";
		return path;
	}
	
	public static String getConnectorPath () {
		// Returns path to DB file in format required by SQLite driver
		String path = new String();
		path = MainWindow.getPhonePath();
		StringBuilder sqlroot = new StringBuilder(path);
		//SQLite connector expects / regardless of OS
		sqlroot.replace(path.lastIndexOf(fsS), path.lastIndexOf(fsS) + 1, "/");
		path = sqlroot.toString();
		path += "PRIVATE/DOCOMO/MMFILE/WM_SYSTEM/ldb.db";
		return path;
	}
	
	public static Statement checkDB (Shell shell) {
		File songsDB = new File(getPath("db"));
		if (!songsDB.exists()) {
			//TODO: copy empty DB file
		};
		return DBOps.attachDB(shell);
	}
	
	public static String[] getFileList (Table fileTable) {
		TableItem[] filePaths = fileTable.getItems();
		String[] fileList = new String[filePaths.length];
		for(int i=0; i<filePaths.length; i++) {
			TableItem currentFile = filePaths[i];
			fileList[i] = currentFile.getText(1);
		}
		return fileList;
	}
	
	public static String secondsToTime (int seconds) {
		int mm = (seconds % 3600) / 60;
		int ss = seconds % 60;
		String timeString = String.format("%02d:%02d",mm,ss);
		return timeString;
	}
	
	public static String bytesToSize (long bytes) {
		if (bytes < 1048576 ) return new DecimalFormat("#.##").format((double)bytes/ 1024)+" kB";
				else return new DecimalFormat("#.##").format((double)bytes / 1048576)+" MB";
	}
	
	public static void populateFileTable (Table fileTable, String[] fileList) {
    	File[] fullPathList = new File[fileList.length];
    	for (int i =0; i<fileList.length;i++) {
    		// fileList contains all files, we just skip processing for unsupported
    		int dot = fileList[i].lastIndexOf('.');
    		if (dot > 0 && fileList[i].substring(dot+1).matches(FILETYPES)) {
        		TableItem item = new TableItem(fileTable, SWT.NONE);
      			fullPathList[i] = new File(fileList[i]);
      			Song currentSong = new Song(fullPathList[i]);
        		item.setText(new String[] {"", currentSong.getPath(), currentSong.getArtist(), currentSong.getTitle(), 
        				currentSong.getAlbum(), currentSong.getBitrate(), currentSong.getDuration(), currentSong.getSize()
        		});
    		}
    	}
	}

	public static boolean mkdirs (String path) {
		File newDir = new File(path);
		// mkdirs output is too ambiguous, can't rely on it
		newDir.mkdirs();
		if (newDir.exists()) {
			// canWrite lies on Windows - but this trick not
			File touchTest = new File(newDir.getAbsolutePath()+fsS+"touchtest");
			try {
				new FileOutputStream(touchTest,true).close();
				touchTest.delete();
				return true;
			} catch (IOException e) {
				return false;
			}
		} else return false;
	}
	
	public static String cp (File srcFile, String cpPath) throws IOException {
	    FileChannel srcChannel = null;
	    FileChannel dstChannel = null;
	    File dstFile = new File(cpPath+fsS+srcFile.getName());
	    if (dstFile.exists()) return "skip";
	    try {
	        srcChannel = new FileInputStream(srcFile).getChannel();
	        dstChannel = new FileOutputStream(dstFile).getChannel();
	        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	return "fail";
	    } finally {
	           srcChannel.close();
	           dstChannel.close();
	    }
	    if (!dstFile.exists()) return "fail";
	   		else return dstFile.getAbsolutePath();
	}
	
	public static String copyFile(Song srcSong, File songFile) {

		String copyPath = getPath("songs")+srcSong.getArtist()+fsS+srcSong.getAlbum();
		MainWindow.debugPrint("Copy to: "+copyPath+fsS+songFile.getName());

	    if (mkdirs(copyPath) == false) {
	    	return "fail";
	    } else {
	    	try {
	    		return cp(songFile,copyPath);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		return "fail";
	    	}
	    }
	}
	
	public static String encode(Shell shell, File inputFile, Song inputSong) {
		
		//TODO: wrap in generic "Result" class with boolean returnFunctions(OK|ERR|SKIP) 
		//      instead of string returns; but should get rid of statics first.
		//      Not sure if possible.
		
		// skip encoding for WMA < 256K
		int dot = inputFile.getName().lastIndexOf('.');
		int bRate = inputSong.getRate();
		String mapVariant = new String("");
		String ext = inputFile.getName().substring(dot+1);
		String artist = inputSong.getArtist();
		String album = inputSong.getAlbum();
		String basename = inputFile.getName().substring(0, dot);
		if (dot > 0) {
			// Implementing Kludge for the Kludge God
			// Forgot about CP SKP while designing encode & copy
			
			// Assume that if WMA file with same name & tags(!) as input (any extension) already 
			// present on SD, it's a result of previous encoding. It may be not, but it will 
			// be in most cases. We do not want to encode it again.
			File checkForSameOnSD = new File(getPath("songs")+artist+fsS+album+fsS+
					inputFile.getName().substring(0, dot+1)+"wma");
			System.out.print("Check if file exist: "+checkForSameOnSD.getAbsolutePath());
			if (checkForSameOnSD.exists()) {
				MainWindow.debugPrint(" - YES");
				return "deny";
			}
			MainWindow.debugPrint(" - NO");
			if (ext.matches("wma|WMA") && bRate <= 256) return "copy";
				// strip WAV metadata, unsupported by tagger
				else if (ext.matches("wav|WAV")) mapVariant="-1";
					// workaround for OGG metadata 
					else if (ext.matches("ogg|OGG|oga|OGA")) mapVariant="0:s:0";
						else mapVariant="0:g";		
		}
		
		// create tmp dir
		String cwd = System.getProperty("user.dir");				
		if(mkdirs(cwd+fsS+"tmp") == false) return "fail";
		
		//create filename for outfile
		String outFile = cwd+fsS+"tmp"+fsS+basename+".wma";
		
		//check for FFmpeg binary
		File intFF = new File(cwd+fsS+"ffmpeg.exe");
		String ffBin = new String();
		if (intFF.exists()) {
			ffBin = intFF.getAbsolutePath();
		} else {
			if (!MainWindow.getFF().equals("none")) {
				ffBin = MainWindow.getFF();
			} else {
				return "deny";
			}
		}
		
		// build options string for FFmpeg
		int threads = Runtime.getRuntime().availableProcessors();
		// SH phones doesn't support rates more than 256k
		if( bRate > 256 ) bRate = 256;
		
		String[] options = new String [] {ffBin,"-i",inputFile.getAbsolutePath(),"-y","-ac","2","-ar","44100","-acodec","wmav1", 
				"-map_metadata",mapVariant,"-ab",bRate+"000","-threads",String.valueOf(threads),outFile};
		
		// call encoder
		Process ffWorker;

		try {
			ProcessBuilder ffThread = new ProcessBuilder(options);
			ffThread.redirectErrorStream(true);
			ffWorker = ffThread.start();
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(ffWorker.getInputStream(),"UTF-8"));
			String s;
			while ((s = stdOut.readLine()) != null) {
				MainWindow.addLog("\n[ENCODER]: "+s);
				shell.layout(); shell.redraw(); shell.update();
			} 
			stdOut.close();
			//Probably not needed, but
			ffWorker.waitFor();
			if (ffWorker.exitValue() != 0) return "fail";

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "fail";
		}
				
		File encodedSong = new File(outFile);
		if (!encodedSong.exists()) return "fail";
			else return encodedSong.getAbsolutePath();
	}

}
