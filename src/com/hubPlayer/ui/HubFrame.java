package com.hubPlayer.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.JTree;

import com.hubPlayer.player.HigherPlayer;
import com.hubPlayer.search.SongLibraryMap;
import com.hubPlayer.song.SongInfos;
import com.hubPlayer.ui.tool.ButtonToolBar;
import com.hubPlayer.ui.tool.IconButton;

/**
 * 以 KuGou布局为框架
 * 
 * @date 2014-10-1
 */

public class HubFrame extends JFrame {

    private static final int InitialWidth;
	private static final int InitialHeight;
	private static final Point InitialPoint;

	private static final int ChangedWidth;

	private static final String savaPath;
	private static final double bufferedTime;

	private PlayPanel playPanel;
	private PlayListPanel playListPanel;
	private SearchPanel searchPanel;
	private ShowPanel showPanel;
	private ButtonToolBar toolBar;

	private SongLibraryMap<String, List<SongInfos>> songLibrary;

	static {
		Dimension dime = Toolkit.getDefaultToolkit().getScreenSize();
		InitialWidth = 975;
		InitialHeight = 670;
		ChangedWidth = 365;
		InitialPoint = new Point((dime.width - InitialWidth) / 2,
				(dime.height - InitialHeight) / 2);

		savaPath = "E:/Hub/download";
		bufferedTime = 2 * Math.pow(10, 8);
	}

	public HubFrame() {

		setTitle("Hub");
		setSize(InitialWidth, InitialHeight);

		setLocation(InitialPoint);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 保存下载歌曲文件夹
		File savefolder = new File(savaPath);

		// 创建已下载歌曲的存储文件夹
		if (!savefolder.exists())
			savefolder.mkdirs();

		readSongLibrary();

		buildPanel();

		setVisible(true);

		requestFocus();
	}

	private void buildPanel() {
		playPanel = new PlayPanel();

		// ToolBar:the left of the Frame
		toolBar = new ButtonToolBar(JToolBar.VERTICAL, 4);

		JButton[] toolBarButtons = new JButton[] {
				new IconButton("本地列表", "icon/note.png"),
				new IconButton("网络收藏", "icon/clouds.png"),
				new IconButton("我的下载", "icon/download.png"),
				new IconButton("更多", "icon/app.png") };

		toolBar.addButtons(toolBarButtons);

		playListPanel = new PlayListPanel(toolBar.getButtons(), this);

		searchPanel = new SearchPanel();

		showPanel = new ShowPanel();

		// 传递给各面板的属性
		JTree[] listTree = playListPanel.deliverTree();
		HigherPlayer player = playPanel.getHigherPlayer();

		// 沟通播放面板与歌曲列表面板
		playPanel.setTrees(listTree);
		player.setJTree(listTree[0]);
		playListPanel.deliverHigherPlayer(player);

		// 沟通乐库面板与歌曲列表面板
		showPanel.setListTree(listTree);

		// 沟通搜索面板与展示面板
		searchPanel.setShowPanel(showPanel);

		// 沟通搜索面板与本地
		searchPanel.setSongLibraryMap(songLibrary);

		// 沟通播放面板与歌词面板
		playPanel.setLrcPanelTextArea(showPanel.getTextArea());

		// 沟通乐库面板与播放面板
		showPanel.setPlayer(player);

		playPanel.setParentFrame(this);

		// Set the preferredSize of those panels
		playPanel.setPreferredSize(new Dimension(350, 115));
		playListPanel.setPreferredSize(new Dimension(305, 520));
		toolBar.setPreferredSize(new Dimension(47, 520));
		searchPanel.setPreferredSize(new Dimension(610, 115));
		showPanel.setPreferredSize(new Dimension(610, 520));

		buildLayout();

		setAction();
	}

	private void setAction() {

		// 设置最大化事件 即展开窗体
		this.addWindowStateListener(event -> {
			if (event.getNewState() == JFrame.MAXIMIZED_BOTH) {
				searchPanel.setVisible(true);
				showPanel.setVisible(true);
				setSize(InitialWidth, InitialHeight);
				setLocation(InitialPoint);
				setVisible(true);

			}
		});

		// 折叠窗体
		searchPanel.getCollapseButton().addActionListener(event -> {
			searchPanel.setVisible(false);
			showPanel.setVisible(false);
			setSize(ChangedWidth, InitialHeight);
			setVisible(true);
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				saveSongLibrary();

				System.exit(0);
			}
		});
	}

	private void buildLayout() {
		Box topBox = Box.createHorizontalBox();

		topBox.add(playPanel);
		topBox.add(searchPanel);

		Box bottomBox = Box.createHorizontalBox();
		bottomBox.add(toolBar);
		bottomBox.add(playListPanel);
		bottomBox.add(showPanel);

		Container mainPanel = getContentPane();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		mainPanel.add(topBox);
		mainPanel.add(bottomBox);
		mainPanel.add(Box.createVerticalStrut(15));
	}

	@SuppressWarnings({ "unchecked" })
	private void readSongLibrary() {

		File library = new File("E:/Hub/SongLibrary.dat");
		if (!library.exists()) {
			songLibrary = new SongLibraryMap<String, List<SongInfos>>();
		} else {

			try {
				ObjectInputStream inputStream = new ObjectInputStream(
						new FileInputStream(library));
				songLibrary = (SongLibraryMap<String, List<SongInfos>>) inputStream
						.readObject();

				// 歌曲库缓存超过约两天,将清除
				long bufferedDateTime = songLibrary.getBufferedDateTime();
				if ((new Date().getTime() - bufferedDateTime) > bufferedTime) {
					songLibrary.clear();
				}

				inputStream.close();
			} catch (IOException | ClassNotFoundException e) {
				songLibrary = new SongLibraryMap<String, List<SongInfos>>();
				e.printStackTrace();
			}
		}
	}

	private void saveSongLibrary() {

		if (songLibrary != null) {
			songLibrary.setBufferedDateTime(new Date().getTime());
			try {
				ObjectOutputStream outputStream = new ObjectOutputStream(
						new FileOutputStream(new File("E:/Hub/SongLibrary.dat")));
				outputStream.writeObject(songLibrary);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}