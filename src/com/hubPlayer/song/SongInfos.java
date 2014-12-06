package com.hubPlayer.song;

import java.io.Serializable;

/**
 * 网络爬取得 歌曲信息:歌曲名、歌手、专辑、播放时间、歌词地址、资源地址
 * 
 * @date 2014-11-06
 */

public class SongInfos implements Serializable{

	private String song;
	private String singer;
	private String album;

	// 时间=文件长度/比特率*8
	private int totalTime;
	private int dataSize;

	private String songDataUrl;
	private String lrcUrl;

	// 大概的比特率
	private int bitRate;

	public SongInfos(String song) {
		this.song = song;
		totalTime = 0;
		lrcUrl = "";

		bitRate = 128000;
	}

	public SongInfos(String song, String singer) {
		this(song);
		this.singer = singer;
	}

	public SongInfos(String song, String singer, String album) {
		this(song, singer);
		this.album = album;
	}

	public SongInfos(String song, String singer, String album,
			String songDataUrl) {
		this(song, singer, album);
		this.songDataUrl = songDataUrl;
	}

	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getSongDataUrl() {
		return songDataUrl;
	}

	public void setSongDataUrl(String songDataUrl) {
		this.songDataUrl = songDataUrl;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}

	public String getLrcUrl() {
		return lrcUrl;
	}

	public void setLrcUrl(String lrcUrl) {
		this.lrcUrl = lrcUrl;
	}

	public int getBitRate() {
		return bitRate;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	public int getDataSize() {
		return dataSize;
	}

	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public String toString() {
		return "Song: " + song + " Singer: " + singer + " Album: " + album
				+ "\ndownload URL: " + songDataUrl;
	}

	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (getClass() != object.getClass())
			return false;
		SongInfos objectSongInfos = (SongInfos) object;

		if (songDataUrl == null)
			return song.equals(objectSongInfos.getSong())
					&& singer.equals(objectSongInfos.getSinger())
					&& album.equals(objectSongInfos.getAlbum());

		return songDataUrl.equals(objectSongInfos.getSongDataUrl());
	}
}
