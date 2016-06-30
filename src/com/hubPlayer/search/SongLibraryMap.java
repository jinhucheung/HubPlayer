package com.hubPlayer.search;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 歌曲库集合 保存到本地
 * 
 * @param <V>
 *            String searchKey
 * @param <K>
 *            List<SongInfos> songList
 * 
 * @date 2014-12-6
 */
public class SongLibraryMap<K, V> extends HashMap<K, V> implements Serializable {

    // 歌曲缓存时间
	private Long bufferedDatetime;

	public long getBufferedDateTime() {
		return bufferedDatetime;
	}

	public void setBufferedDateTime(long bufferedDatetime) {
		this.bufferedDatetime = bufferedDatetime;
	}

}