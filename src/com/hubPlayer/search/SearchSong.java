package com.hubPlayer.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hubPlayer.song.SongInfos;

/**
 * 利用JSoup解析百度音乐 从中得到搜索的歌曲信息(SongInfos)
 *
 * @date 2014-11-06
 */

public class SearchSong {

    // http://music.baidu.com/search/song?s=1&key=key&start=00&size=20
	// 上面是百度音乐搜索地址形式, key是关键字,start是以库中的第几条歌曲开始,size为页面显示的歌曲数目(最大为20)

	// 搜索地址及网页编码集 百度音乐是以utf-8编码
	private static final String baseUrl = "http://music.baidu.com";
	private String encode = "utf-8";

	// 歌曲集合
	private Map<String, List<SongInfos>> songLibraryMap;
	private int songNumber;

	// 与搜索和展示面板交互
	private String key;
	private int start;
	private int page;

	// boolean flag;

	public SearchSong() {

		// 第一页
		page = 1;
		// 第一首歌序号
		start = 0;

		songNumber = 20;
	}

	/**
	 * 打开搜索地址，获取HTML
	 */
	public boolean openConnection() {
		if (key == null)
			return false;

		// 拼接搜索地址
		String searchUrl = "";
		if ("百度音乐新歌榜/月榜".equals(key)) {
			// 百度音乐新歌榜/月榜地址
			searchUrl = "http://music.baidu.com/top/new/month/";

		} else {

			String keyEncode = "";
			// 将key关键字转成URL编码
			try {
				keyEncode = URLEncoder.encode(key, encode);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			searchUrl = baseUrl + "/search/song?s=1&key=" + keyEncode
					+ "&start=" + start + "&size=20";
		}

		try {
			// 打开链接,获取HTML文 档
			// 功能用URLConnection一样,下面被注释了的代码
			Document document = Jsoup.connect(searchUrl).get();

			parseHtml(document);

			// // 打开连接
			// URLConnection connection = new URL(searchUrl).openConnection();
			//
			//
			// // 打开输入流
			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// connection.getInputStream(), encode));
			//
			// // 获取HTML
			// StringBuffer stringbuffer = new StringBuffer();
			// String line;
			// while ((line = reader.readLine()) != null) {
			// stringbuffer.append(line + "\n");
			// }
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "网络连接超时", "",
					JOptionPane.PLAIN_MESSAGE);
			return false;
		}

	}

	/**
	 * 解析HTML 获取信息:歌曲、歌手、专辑名及歌曲所在的地址
	 * 
	 * @param document
	 *            HTML文档
	 */
	private void parseHtml(Document document) {
		// 获取HTML中的歌曲列表区域块

		// 获取搜索歌曲数目
		songNumber = 20;
		Element e = document.select("span[class=number]").first();
		if (e != null) {
			String number = e.text();
			songNumber = Integer.parseInt(number);
		}

		// 每个歌曲的区域块
		Elements songDiv = null;
		// 新歌榜 的关键字不一样
		if ("百度音乐新歌榜/月榜".equals(key))
			songDiv = document.select("div[class=song-item]");
		else
			songDiv = document.select("div[class^=song-item clearfix]");

		List<SongInfos> temporaryList = new Vector<SongInfos>();
		// 遍历每个歌曲块
		for (Element aSongNode : songDiv) {
			// 选择class等于以song-title开头的span标签
			Element songTitle = aSongNode.select("span[class^=song-title]")
					.first().select("a[href^=/song]").first();
			if (songTitle == null)
				continue;

			// 获取歌曲所在的绝对地址
			String songUrl = songTitle.attr("abs:href");

			// 获取歌曲名
			String songName = songTitle.text();

			// 搜索列表保存歌曲信息
			temporaryList.add(getSongInfos(songName, songUrl));
		}

		if (songLibraryMap.get(key) == null)
			songLibraryMap.put(key, temporaryList);
		else
			songLibraryMap.get(key).addAll(temporaryList);
	}

	// 深度爬取歌曲信息
	private SongInfos getSongInfos(String songName, String songUrl) {

		SongInfos songInfos = new SongInfos(songName);

		try {
			// 打开歌曲链接,获取其HTML代码
			Document document = Jsoup.connect(songUrl).get();

			// 歌曲资源地址
			// 格式 http://music.baidu.com/song/7319923
			// String songID = songUrl.substring(28, songUrl.length());
			// String dataUrl =
			// "http://music.baidu.com/data/music/file?link=&song_id="
			// + songID;

			// 歌手名
			String singer = "";
			Element SingerElement = document.select("span[class^=author_list]")
					.first();
			if (SingerElement != null) {
				singer = SingerElement.text();

				// 如果歌手名格式形如"x1/x2"则转成"x1、x2"
				if (singer.contains("/")) {
					String[] singers = singer.split("/");
					singer = "";
					for (String s : singers) {
						singer = singer + "、" + s;
					}
					singer = singer.substring(1);
				}
			}

			// 获取专辑信息 格式-所属专辑：album
			String album = "";
			Element albumElement = document.select("li[class^=clearfix]")
					.first();
			if (albumElement != null)
				album = albumElement.text();
			// 去除"所属专辑："
			if (album.length() >= 5)
				album = album.substring(5);

			searchDataURL(songInfos, singer, songName);

			// 百度音乐改版 此节点已经找不到
			// 获取其下载链接,在下载页面获取不到这个地址
			// String downloadUrl = "";
			// Element downloadElement = document.select("a[data_url]").first();
			// if (downloadElement != null)
			// downloadUrl = downloadElement.attr("data_url");

			// if (!flag) {
			// findDataUrl("陈奕迅", "苦瓜");
			// flag = true;
			// }

			// 百度音乐改版 此节点已经找不到
			// // 获取歌曲文件长度
			// int dataSize = 0;
			// // 播放时长
			// int totalTime = 0;
			//
			// Element dataSizeElement =
			// document.select("a[data_size]").first();
			// if (dataSizeElement != null) {
			// // 大概的时间
			// String size = dataSizeElement.attr("data_size");
			// dataSize = Integer.parseInt(size);
			// totalTime = dataSize * 8 / songInfos.getBitRate();
			// }

			// 获取歌词文件地址
			String lrcUrl = "";
			Element lrcUrlElement = document.select("a[data-lyricdata]")
					.first();
			if (lrcUrlElement != null) {
				lrcUrl = lrcUrlElement.attr("data-lyricdata");

				// 正则匹配
				Pattern pattern = Pattern.compile("(/.*\\.lrc)");
				Matcher matcher = pattern.matcher(lrcUrl);
				if (matcher.find())
					lrcUrl = baseUrl + matcher.group();
				else
					lrcUrl = "";
			}

			// 保存歌曲信息
			songInfos.setSinger(singer);
			songInfos.setAlbum(album);
			songInfos.setLrcUrl(lrcUrl);

			// System.out.println("--------A song item--------");
			// System.out.println("song: " + songName + " singer: " + singer
			// + " album: " + songInfos.getAlbum() + " dataSize: "
			// + songInfos.getDataSize() + " bitRate: "
			// + songInfos.getBitRate());
			// System.out.println("songDataUrl: " + songInfos.getSongDataUrl());
			// System.out.println("lrcUrl: " + songInfos.getLrcUrl());
			// System.out.println("---------------------------");

		} catch (IOException e) {
			e.printStackTrace();

			// JOptionPane.showMessageDialog(null, "歌曲地址: " + songUrl +
			// "\n歌曲名: "
			// + songName + "  读取数据异常", "", JOptionPane.PLAIN_MESSAGE);

		}

		return songInfos;
	}

	/**
	 * 由于受百度音乐改版的影响,这里不直接去获取歌曲资源地址和文件总字节数 百度音乐用了登陆和JS加密这个地址
	 * 我们用百度音乐盒http://box.zhangmen.baidu.com/的xml文件间接获取
	 * 
	 * 
	 */

	private void searchDataURL(SongInfos songInfos, String singer, String song) {

		String songBoxUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
				+ song + "$$" + singer + "$$";

		Document document = null;
		try {
			document = Jsoup.connect(songBoxUrl).get();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		/**
		 * 提取durl节点中的encode节点的字符串与decode节点的字符串拼接歌曲资源地址
		 **/
		String dataUrl = "";
		Elements durlNodes = document.select("durl");

		for (Element durlNode : durlNodes) {

			Element encode = durlNode.select("encode").first();
			Element decode = durlNode.select("decode").first();

			if (encode == null || decode == null)
				continue;
			String encodeText = encode.text();
			String decodeText = decode.text();
			encodeText = encodeText.substring(0,
					encodeText.lastIndexOf("/") + 1);
			dataUrl = encodeText + decodeText;

		}

		/**
		 * 获取歌曲文件长度和比特率
		 **/
		int dataSize = 0;
		int totalTime = 0;

		Element p2p = document.select("p2p").first();
		if (p2p != null) {
			// 文件总字节数
			String dataSizeText = p2p.select("size").first().text();
			// 比特率
			int bitRate = Integer.parseInt(p2p.select("bitrate").text()) * 1000;

			dataSize = Integer.parseInt(dataSizeText);
			totalTime = dataSize * 8 / bitRate;
			songInfos.setBitRate(bitRate);

		}

		songInfos.setSongDataUrl(dataUrl);
		songInfos.setDataSize(dataSize);
		songInfos.setTotalTime(totalTime);

	}

	/**
	 * 清除已解析的歌曲信息等
	 */
	public void clear() {

		start = 0;
		key = "";
		page = 1;
		songNumber = 20;
	}

	/**
	 * 设置搜索的关键字 之后需调用search方法进行搜索
	 * 
	 * @param key
	 *            关键字
	 * @return SearchSong this
	 */
	public SearchSong setKey(String key) {
		this.key = key;
		return this;
	}

	public String getKey() {
		return key;
	}

	/**
	 * 设置当前页面
	 * 
	 * @param start
	 *            设置当前页面第一首歌曲在库中的序号,
	 * @return SearchSong this
	 */
	public SearchSong setPage(int page) {
		this.page = page;
		start = (page - 1) * 20;

		return this;
	}

	// 设置页面
	public int getPage() {
		return page;
	}

	public void setSongLibraryMap(Map<String, List<SongInfos>> songLibraryMap) {

		this.songLibraryMap = songLibraryMap;
	}

	public int getSongNumber() {
		return songNumber;
	}

}