package com.hubPlayer.song;

import java.io.File;
import java.io.FileFilter;
import java.util.StringTokenizer;

/**
 * AFilter 过滤器  歌曲类型 MP3 WAV MID 歌词类型 LRC
 * 
 * @date 2014-10-15
 */

public class AFilter implements FileFilter {

	private String description;

	public AFilter(String description) {
		this.description = description;
	}

	public boolean accept(File f) {

		String name = f.getName();
		StringTokenizer token = new StringTokenizer(description, ",;");

		while (token.hasMoreTokens()) {
			if (name.toLowerCase().endsWith(token.nextToken().toLowerCase()))
				return true;
		}

		return false;
	}

	public String getDescription() {

		return description;
	}

}
