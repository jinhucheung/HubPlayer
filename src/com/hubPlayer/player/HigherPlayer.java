package com.hubPlayer.player;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.hubPlayer.song.SongNode;
import com.hubPlayer.ui.tool.TimeProgressBar;

/**
 * 高层播放器 主要控制与外部信息交互
 * 
 * @date 2014-10-18
 */

public class HigherPlayer extends BasicPlayer {

	private JTree tree;

	private SongNode loadSong;
	private SongNode playingSong;

	private String loadSongName;// 与loadSong对应
	private String playingSongName;// 与playedSong对应

	// 当前播放歌曲所在的目录
	private TreePath currentListPath;

	private JButton play;

	private JLabel songNameLabel;
	private JLabel audioTotalTimeLabel;

	// 播放模式
	public int mode;
	public boolean IsPlayNextSong;

	// 当前音频的总时间
	public int audioTotalTime;

	public HigherPlayer() {
	}

	// 本地资源
	public void load(TreeNode node) {
		this.loadSong = (SongNode) node;
		DefaultMutableTreeNode mutablenode = (DefaultMutableTreeNode) node;
		File songFile = (File) mutablenode.getUserObject();
		loadSongName = songFile.getName();
		try {
			audio = songFile.toURI().toURL();
			this.HTTPFlag = false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	// 网络资源
	public void load(SongNode song, String dataURL) {
		try {

			if (dataURL==null || dataURL.length() == 0) {
				JOptionPane.showMessageDialog(null, "没有找到歌曲资源链接地址", "",
						JOptionPane.PLAIN_MESSAGE);
				loadSongName = null;
				return;
			}

			loadSongName = song.toString();
			loadSong = song;
			audio = new URL(dataURL);
			this.HTTPFlag = true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void open() {
		playingSongName = loadSongName;
		playingSong = loadSong;

		IsComplete = false;

		// 网络资源播放时间
		if (playingSong.getHTTPFlag())
			audioTotalTime = playingSong.getTotalTime();
		// 本地资源播放时间
		else
			audioTotalTime = getAudioTrackLength(audio);

		audioTotalTimeLabel.setText(getAudioTotalTime(audioTotalTime));

		// 重置计时器
		timerProgressBar.cleanTimer();

		// 启动新的计时器
		timerProgressBar.setAudioTotalTime(audioTotalTime);
		timerProgressBar.setCurrentPlayedSongLrcInfo(playingSong.getLrcInfo());

		timerProgressBar.startTimer();

		// 因为要与播放面板交互 所以让监听歌曲状态的线程在高层播放器初始化
		playThread = new Thread(() -> {
			// 播放结束前, 线程在此阻塞 不能解码的歌曲 这不阻塞
				super.play();

				if (IsEnd) {
					IsEnd = false;
					return;
				}

				// 播放结束 play按钮显示"播放"状态
				play.doClick();

				// 播放模式决定接着进行地播放
				playSwitch();
			});
		playThread.start();

	}

	private void playSwitch() {

		IsComplete = true;
		// 初始化播放状态
		switch (mode) {
		// 单曲播放
		case 0:
			return;
			// 单曲循环
		case 1:
			break;

		// 顺序播放 当触发播放按钮时,因为要播放新歌曲 所以进入
		// if(!player.getAfterSong().equals(player.getCurrentSong())) {}
		// 使得当前线程被终止,往下的播放操作被终止
		case 2:
			IsPlayNextSong = true;
			next();
			break;
		// 列表播放 情况同顺序播放
		case 3:
			cycle();
			break;
		// 随机播放 情况同顺序播放
		case 4:
			random();
			break;
		}

		play.doClick();
	}

	// 接着进行地播放
	public void next() {
		DefaultMutableTreeNode list = (DefaultMutableTreeNode) playingSong
				.getParent();
		SongNode songNode = null;

		if (!IsPlayNextSong) {
			songNode = (SongNode) list.getChildBefore(playingSong);

		} else {
			songNode = (SongNode) list.getChildAfter(playingSong);
		}
		if (songNode == null) {
			IsPause = false;

			return;
		}
		// 在当前所在的歌曲列表路径中加入待播放的歌曲 形成待播放歌曲的路径
		TreePath songPath = currentListPath.pathByAddingChild(songNode);
		tree.setSelectionPath(songPath);

		if (songNode.getHTTPFlag())
			load(songNode, songNode.getDataURL());
		else
			load(songNode);
	}

	// 列表循环播放
	private void cycle() {
		DefaultMutableTreeNode list = (DefaultMutableTreeNode) playingSong
				.getParent();
		SongNode songNode = null;

		songNode = (SongNode) list.getChildAfter(playingSong);

		if (songNode == null) {
			songNode = (SongNode) list.getFirstChild();
		}

		// 在当前所在的歌曲列表路径中加入待播放的歌曲 形成待播放歌曲的路径
		TreePath songPath = currentListPath.pathByAddingChild(songNode);
		tree.setSelectionPath(songPath);

		if (songNode.getHTTPFlag())
			load(songNode, songNode.getDataURL());
		else
			load(songNode);

	}

	// 随机播放
	private void random() {
		DefaultMutableTreeNode list = (DefaultMutableTreeNode) playingSong
				.getParent();
		int songnum = list.getChildCount();

		// 随机歌曲
		int songindex = (int) Math.round(Math.random() * songnum) - 1;
		if (songindex < 0)
			songindex = 0;

		SongNode songNode = (SongNode) list.getChildAt(songindex);
		// 在当前所在的歌曲列表路径中加入待播放的歌曲 形成待播放歌曲的路径
		TreePath songPath = currentListPath.pathByAddingChild(songNode);
		tree.setSelectionPath(songPath);

		if (songNode.getHTTPFlag())
			load(songNode, songNode.getDataURL());
		else
			load(songNode);

	}

	// 提供网络资源加入列表及播放的接口
	public void setSelectTreeNodeInCurrentList(SongNode songNode, String dataURL) {

		TreePath songPath = currentListPath.pathByAddingChild(songNode);
		tree.setSelectionPath(songPath);

		load(songNode, dataURL);
	}

	public void end() {
		super.end();
		timerProgressBar.cleanTimer();
	}

	public TreeNode getloadSong() {
		return loadSong;
	}

	public TreeNode getPlayingSong() {
		return playingSong;
	}

	public String getPlayingSongName() {
		return playingSongName;
	}

	public void setPlayingSongName(String song) {
		playingSongName = song;
	}

	public String getLoadSongName() {
		return loadSongName;
	}

	public JButton getPlayButton() {
		return play;
	}

	public void setPlayButton(JButton button) {
		this.play = button;

	}

	public void setCurrentListPath(TreePath currentListPath) {
		this.currentListPath = currentListPath;
	}

	public void setJTree(JTree tree) {
		this.tree = tree;
	}

	public JTree getJTree() {
		return this.tree;
	}

	public JLabel getSongNameLabel() {
		return songNameLabel;
	}

	public void setSongNameLabel(JLabel songNameLabel) {
		this.songNameLabel = songNameLabel;
	}

	public void setVoiceValue(float voiceValue) {
		super.getFloatControl().setValue(voiceValue);
	}

	public float getVoiceValue() {
		return getFloatControl().getValue();
	}

	public void setAudioTotalTimeLabel(JLabel label) {
		audioTotalTimeLabel = label;
	}

	public void setCurrentTimeCountLabel(JLabel label) {
	}

	public void setTimerProgressBar(TimeProgressBar timerProgressBar) {
		this.timerProgressBar = timerProgressBar;
	}
}
