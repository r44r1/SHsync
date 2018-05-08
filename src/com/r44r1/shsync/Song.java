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

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.*;

public class Song {
	private String Path = "";
	protected String Title = "Unknown Title";
	protected String Artist = "Unknown Artist";
	protected String Album = "Unknown Album";
	private String Duration = "";
	private String Bitrate = "";
	protected String Genre = "Unknown Genre";
	private String Size;
	protected static final String FILETYPES = "mp3|MP3|wma|WMA|wav|WAV|m4a|M4A|flac|FLAC|ogg|OGG|oga|OGA";

	Song(File fsPath){
		Path = fsPath.getAbsolutePath();
		//skips tag reading for unsupported by jAudiotagger
		int dot = Path.lastIndexOf('.');
		String ext = Path.substring(dot+1);
		// buggy MP3 reader
		TagOptionSingleton.getInstance().setAndroid(true);
		if (dot > 0 && ext.matches(FILETYPES)) {
				AudioFile song = new AudioFile();
				try {
					song = AudioFileIO.read(fsPath);
				} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
						| InvalidAudioFrameException e) {
					e.printStackTrace();
				}
				Tag tag = song.getTag();
				AudioHeader header = song.getAudioHeader();
				
				String buffer = new String("");
				
				// for by ENUM is overkill here
				buffer = tag.getFirst(FieldKey.ARTIST);
				if (!buffer.isEmpty()) this.Artist = buffer;
				buffer = tag.getFirst(FieldKey.TITLE);
				if (!buffer.isEmpty()) this.Title = buffer;				
				buffer = tag.getFirst(FieldKey.ALBUM);
				if (!buffer.isEmpty()) this.Album = buffer;				
				buffer = tag.getFirst(FieldKey.GENRE);
				if (!buffer.isEmpty()) this.Genre = buffer;				
				
				Duration = FileOps.secondsToTime(header.getTrackLength());
				Bitrate = header.getBitRate();
		}
		Size = FileOps.bytesToSize(fsPath.length());
	}
	
	public String getPath() {
		return this.Path;
	}
	public String getArtist() {
		return this.Artist;
	}
	public String getTitle() {
		return this.Title;
	}
	public String getAlbum() {
		return this.Album;
	}
	public String getGenre() {
		return this.Genre;
	}
	public String getDuration() {
		return this.Duration;
	}
	public String getBitrate() {
		return this.Bitrate+"k";
	}
	public int getRate() {
		String rate = this.Bitrate;
		return Integer.parseInt(rate);
	}
	public String getSize() {
		return this.Size;
	}
}
