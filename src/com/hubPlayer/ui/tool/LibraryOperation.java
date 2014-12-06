package com.hubPlayer.ui.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.hubPlayer.player.HigherPlayer;
import com.hubPlayer.song.SongInfos;
import com.hubPlayer.song.SongNode;

/**
 * HubLibraryOperation处理外层交互 乐库数据表格中的操作面板(OperationPanel),主要包含3个按钮:播放,加到列表,下载
 * 
 * @date 2014-11-07
 */

public class LibraryOperation {

	private JTree[] trees;

	// 播放器
	private HigherPlayer player;

	// 下载完成提示 只为第一次下载作提示
	private static boolean TipFlag = true;

	private final static String savePath = "E:/Hub/download";



	public void setListTree(JTree[] trees) {
		this.trees = trees;
	}

	public void setPlayer(HigherPlayer player) {
		this.player = player;
	}

	public class OperationPanel extends JPanel {

		private JButton play;
		private JButton toList;
		private JButton download;

		private String song;
		private String singer;
		private String dataURL;

		private SongNode songNode;

		public OperationPanel() {
			initComponent();
			setAction();
		}

		// 接收歌曲信息
		public OperationPanel(SongInfos songInfos) {

			this();

			song = songInfos.getSong();

			singer = songInfos.getSinger();

			dataURL = songInfos.getSongDataUrl();

			songNode = new SongNode(singer + "-" + song,
					songInfos.getTotalTime(), songInfos.getDataSize(),
					songInfos.getLrcUrl(), dataURL);
		}

		private void initComponent() {
			play = new IconButton("播放", "icon/note2.png");
			toList = new IconButton("添加到列表", "icon/add.png");
			download = new IconButton("下载", "icon/download2.png");

			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			Box box = Box.createHorizontalBox();
			box.add(play);
			box.add(toList);
			box.add(download);

			add(Box.createHorizontalStrut(40));
			add(box);
		}

		private void setAction() {

			// 播放
			play.addActionListener(event -> {

				// 选中默认播放列表
				trees[0].setSelectionRow(0);
				
				addTreeList(trees[0], 0);

				player.setSelectTreeNodeInCurrentList(songNode, dataURL);

				// 播放按钮播放
				player.getPlayButton().doClick();
			});

			// 加到播放列表
			toList.addActionListener(event -> {
				addTreeList(trees[0], 0);
			});

			// 下载
			download.addActionListener(event -> {

				if (dataURL == null || dataURL.length() == 0) {
					JOptionPane.showMessageDialog(null, "没有找到资源相应的下载链接", "",
							JOptionPane.PLAIN_MESSAGE);
					return;
				}

				new Thread(() -> {
					try {

						// 打开资源链接
						HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(
								dataURL).openConnection();

						// 开启IO流 读写数据
						BufferedInputStream inputStream = new BufferedInputStream(
								httpURLConnection.getInputStream());

						String songName = savePath + "/" + singer + "-" + song;
						if (!songName.endsWith(".mp3"))
							songName += ".mp3";

						BufferedOutputStream outputStream = new BufferedOutputStream(
								new FileOutputStream(new File(songName)));

						// 加入到下载面板 的"下载中"节点
						addTreeList(trees[2], 0);

						byte[] buff = new byte[1024];
						int onceRead = 0;
						while ((onceRead = inputStream.read(buff, 0,
								buff.length)) > 0) {
							outputStream.write(buff, 0, onceRead);
						}

						outputStream.flush();
						outputStream.close();
						inputStream.close();

						// 移除"下载中"的歌曲信息
						removeSongNodeInTreeList(trees[2], 0);
						// 将歌曲信息移入"已下载"
						addTreeList(trees[2], 1);

						// 下载完成提示
						if (TipFlag) {

							JOptionPane.showMessageDialog(null, "下载完成,文件存至  "
									+ savePath, "", JOptionPane.PLAIN_MESSAGE);
							TipFlag = false;
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}).start();

			});

		}

		public void addTreeList(JTree tree, int index) {

			DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
					.getModel().getRoot();
			DefaultMutableTreeNode list = (DefaultMutableTreeNode) root
					.getChildAt(index);

			list.add(songNode);

			// 列表名更新
			String listName = (String) list.getUserObject();
			listName = listName.substring(0, listName.lastIndexOf("[")) + "["
					+ list.getChildCount() + "]";
			list.setUserObject(listName);

			// 如果这里不更新树的话 会不正确显示
			tree.updateUI();

		}

		public void removeSongNodeInTreeList(JTree tree, int index) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
					.getModel().getRoot();
			DefaultMutableTreeNode list = (DefaultMutableTreeNode) root
					.getChildAt(index);

			list.remove(songNode);

			// 列表名更新
			String listName = (String) list.getUserObject();
			listName = listName.substring(0, listName.lastIndexOf("[")) + "["
					+ list.getChildCount() + "]";
			list.setUserObject(listName);

			// 如果这里不更新树的话 会不正确显示
			tree.updateUI();

		}

		public void setSong(String song) {
			this.song = song;

		}

		public void setSinger(String singer) {
			this.singer = singer;
		}

		public void setDataURL(String dataURL) {
			this.dataURL = dataURL;
		}

	}

}
