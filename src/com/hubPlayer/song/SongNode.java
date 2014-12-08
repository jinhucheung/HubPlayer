package com.hubPlayer.song;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * SongNode 显示歌曲名字 不显示歌曲路径
 * 
 * @date 2014-10-15
 */

public class SongNode extends DefaultMutableTreeNode {

	private File song;
	private String dataUrl;
	// 标记歌曲是否为网络资源
	private boolean HTTPFlag;
	// 播放时长
	private int totalTime;
	// 文件长度
	private int dataSize;

	// 本地与网络歌词资源
	private File lrcFile;
	// 解析的歌曲信息
	private LrcInfos lrcInfo;

	// 歌曲的上级路径
	private String parentPath;
	// 无扩展名的歌曲名
	private String songName;

	public SongNode(File song) {
		super(song, false);
		this.song = song;

		parentPath = song.getParent();
		songName = song.getName();
		songName = songName.substring(0, songName.lastIndexOf("."));

		File f = new File(parentPath + "\\" + songName + ".lrc");
		lrcInfo = new LrcInfos();
		if (f.exists()) {
			lrcFile = f;
			lrcInfo.read(lrcFile);
		}
	}

	// 网络资源
	public SongNode(String songName, int totalTime, int dataSize,
			String lrcUrl, String dataUrl) {
		try {
			song = new File(dataUrl);
		} catch (NullPointerException e) {
			song = null;
		}
		this.dataUrl = dataUrl;
		this.songName = songName;
		this.dataSize = dataSize;

		HTTPFlag = true;

		// 大概的播放时间 真正播放时长大概是算出的时间-3
		this.totalTime = totalTime - 3;

		lrcInfo = new LrcInfos();

		// 存在歌曲链接
		if (lrcUrl.length() > 0) {
			lrcInfo.read(lrcUrl);
			// 利用歌词来得到精确的播放时间
			int time = lrcInfo.getTotalTime();
			if (this.totalTime < time) {
				this.totalTime = time;
			}
		}

	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setLrc(File lrcFile) {
		this.lrcFile = lrcFile;
		lrcInfo.read(lrcFile);
	}

	public File getLrc() {
		return lrcFile;
	}

	public LrcInfos getLrcInfo() {
		return lrcInfo;
	}

	public String getSongName() {
		return songName;
	}

	public File getSong() {
		return song;
	}

	public String getDataURL() {
		return dataUrl;
	}

	public boolean getHTTPFlag() {
		return HTTPFlag;
	}

	public int getDataSize() {
		return dataSize;
	}

	@Override
	public String toString() {
		if (HTTPFlag)
			return songName + ".mp3";

		return song.getName();

	}

	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;

		if (song == null)
			return false;

		SongNode objectNode = (SongNode) object;

		return song.equals(objectNode.getSong());
	}
}
