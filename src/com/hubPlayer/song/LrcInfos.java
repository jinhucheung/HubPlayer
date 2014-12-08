package com.hubPlayer.song;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 歌词信息:标题/歌手/专辑/时间-歌词文本组
 * 
 * @date 2014-10-31
 */

public class LrcInfos {

	private String title;
	private String singer;
	private String album;
	private int totalTime;

	private List<Integer> timeList;
	private Map<Integer, String> infos;

	private String regex;
	private Pattern pattern;

	public LrcInfos() {
		infos = new HashMap<Integer, String>();
		timeList = new ArrayList<Integer>();
		regex = "\\[\\d{2}:\\d{2}\\.\\d{2}\\]";
		pattern = Pattern.compile(regex);
	}

	// 读入本地歌词文件
	public void read(File file) {

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));

			String line = "";

			while ((line = reader.readLine()) != null) {
				match(line);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 读入网络资源
	public void read(String lrcUrl) {

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(
					lrcUrl).openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "utf-8"));

			String line = "";

			while ((line = reader.readLine()) != null) {
				match(line);
			}

			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 匹配一行字符
	private void match(String line) {

		// 判断当前行是否为歌手、标题、专辑 不是 满足
		if (!line.endsWith("]")) {

			// 匹配当前的时间格式
			Matcher matcher = pattern.matcher(line);

			int endIndex = 0;
			// 满足则
			while (matcher.find()) {
				// 获取内容
				String group = matcher.group();

				group = group.substring(1, group.length() - 1);

				timeList.add(toCurrentTime(group));
				endIndex = matcher.end();
			}

			String strInfos = line.substring(endIndex, line.length());
			timeList.forEach(each -> infos.put(each, strInfos));
			timeList.clear();

		} else if (line.startsWith("[ti:")) {
			title = line.substring(4, line.length() - 1);
		} else if (line.startsWith("[ar:")) {
			singer = line.substring(4, line.length() - 1);
		} else if (line.startsWith("[al:")) {
			album = line.substring(4, line.length() - 1);
		}
	}

	// 解析当前信息时间 秒制
	private int toCurrentTime(String time) {
		int currentTime = 0;

		String[] spliter = time.split(":");
		currentTime += Integer.parseInt(spliter[0]) * 60;
		spliter = spliter[1].split("\\.");
		currentTime += Integer.parseInt(spliter[0]);
		if (Integer.parseInt(spliter[1]) >= 50)
			currentTime++;

		if (totalTime < currentTime)
			totalTime = currentTime;

		return currentTime;
	}

	public String getTitle() {
		return title;
	}

	public String getSinger() {
		return singer;
	}

	public String getAlbum() {
		return album;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public Map<Integer, String> getInfos() {
		return infos;
	}
}
