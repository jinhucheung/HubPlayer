package com.hubPlayer.ui.tool;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.hubPlayer.player.HigherPlayer;
import com.hubPlayer.song.AFilter;
import com.hubPlayer.song.SongNode;

/**
 * 歌曲列表面板:播放列表，标记列表、下载列表
 * 
 * @date 2014-10-12
 */

public class SongListPanel extends JScrollPane {
	// 菜单项相关
	private JMenuItem newList;
	private JMenuItem removeList;
	private JMenuItem cleanList;

	private JMenuItem addSongFile;
	private JMenuItem addSongFolder;

	private JMenuItem removeSong;

	private JMenuItem addLrcFile;
	private JMenuItem addLrcFloder;

	private JPopupMenu popupMenu;

	// 歌曲、歌词列表相关
	private JTree tree;
	private DefaultMutableTreeNode topNode;
	private int defaultNodes;
	private List<SongNode> songlist;

	// 文件对话框相关
	private JFileChooser fileChooser;
	private FileNameExtensionFilter songFilter;
	private FileNameExtensionFilter lrcFilter;

	// 播放器
	private HigherPlayer higherPlayer;

	public SongListPanel(String... defaultNodes) {
		this.defaultNodes = defaultNodes.length;

		initComponent(defaultNodes);
		createPopupmenu();
		createAction();

	}

	private void initComponent(String... defaultNodes) {

		// 树组件
		topNode = new DefaultMutableTreeNode();

		// node表默认的列表 [0]表示列表中的歌曲数
		for (int i = 0; i < defaultNodes.length; i++) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					defaultNodes[i] + "[0]");
			topNode.add(node);
		}

		tree = new JTree(topNode);
		// tree.setPreferredSize(new Dimension(290, 400));
		tree.startEditingAtPath(new TreePath(new Object[] { topNode,
				topNode.getFirstChild() }));

		// 隐藏根节点
		tree.setRootVisible(false);

		getViewport().add(tree);

		// 文件选择器处理
		fileChooser = new JFileChooser();
		songFilter = new FileNameExtensionFilter("音频文件(*.mid;*.mp3;*.wav)",
				"mid", "MID", "mp3", "MP3", "wav", "WAV");
		lrcFilter = new FileNameExtensionFilter("歌词文件(*.lrc)", "lrc", "LRC");

		songlist = new Vector<SongNode>();
	}

	private void createPopupmenu() {
		popupMenu = new JPopupMenu();

		newList = new JMenuItem("新建列表");
		removeList = new JMenuItem("移除列表");
		cleanList = new JMenuItem("清空列表");

		addSongFile = new JMenuItem("添加本地歌曲");
		addSongFolder = new JMenuItem("添加本地歌曲文件夹");

		JMenu addSong = new JMenu("添加歌曲");
		addSong.add(addSongFile);
		addSong.add(addSongFolder);

		addLrcFile = new JMenuItem("添加本地歌词");
		addLrcFloder = new JMenuItem("添加本地歌词文件夹");

		addLrcFile.setEnabled(false);
		addLrcFloder.setEnabled(false);

		JMenu addLrc = new JMenu("添加歌词");
		addLrc.add(addLrcFile);
		addLrc.add(addLrcFloder);

		removeSong = new JMenuItem("删除歌曲");

		popupMenu.add(newList);
		popupMenu.add(removeList);
		popupMenu.add(cleanList);

		popupMenu.addSeparator();
		popupMenu.add(addSong);

		popupMenu.addSeparator();
		popupMenu.add(addLrc);

		popupMenu.addSeparator();
		popupMenu.add(removeSong);

	}

	private void createAction() {

		// 新建列表
		newList.addActionListener(event -> {
			String listName = JOptionPane.showInputDialog(this, "请输入新建列表的名称",
					null, JOptionPane.DEFAULT_OPTION);

			if (listName == null)
				return;

			addList(listName);

		});

		// 移除列表
		removeList
				.addActionListener(event -> {

					TreePath path = tree.getSelectionPath();

					if (path == null)
						return;

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getLastPathComponent();

					// 判断是否在歌曲上触发事件 是返回此歌曲的目录
					if (path.getPathCount() == 3) {
						node = (DefaultMutableTreeNode) node.getParent();
					}

					// 该目录是否为默认目录
					int nodeIndex = topNode.getIndex(node);
					if (nodeIndex < defaultNodes && nodeIndex != -1)
						return;

					// 该目录是否含歌曲 不含则返回true
					if (node.isLeaf())
						node.removeFromParent();

					// 改模录含有歌曲 询问是否移除
					else if (JOptionPane
							.showConfirmDialog(this, "列表内包含歌曲,是否删除?", null,
									JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
						;

					// 终止当前歌曲播放
					TreeNode playedSong = higherPlayer.getPlayingSong();
					if (playedSong != null && node.getIndex(playedSong) != -1
							&& playedSong != null) {

						higherPlayer.end();

						// 触发下播放按钮 使它处于待播放状态
						higherPlayer.IsPause = false;
						higherPlayer.getPlayButton().doClick();
						higherPlayer.getSongNameLabel().setText("");
					}

					// 清空集合中的歌曲
					Enumeration<SongNode> e = node.children();
					while (e.hasMoreElements()) {
						songlist.remove(e.nextElement());
					}

					if (songlist.isEmpty()) {
						addLrcFile.setEnabled(false);
						addLrcFloder.setEnabled(false);
					}

					node.removeFromParent();
					tree.updateUI();
				});

		// 清空列表
		cleanList.addActionListener(event -> {
			// 返回选中节点的路径
				TreePath path = tree.getSelectionPath();

				if (path == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();

				if (node.getChildCount() == 0)
					node = (DefaultMutableTreeNode) node.getParent();

				if (node == topNode)
					return;

				// 终止当前歌曲播放
				TreeNode playedSong = higherPlayer.getPlayingSong();

				if (playedSong != null && node.getIndex(playedSong) != -1
						&& playedSong != null) {

					higherPlayer.end();
					higherPlayer.IsPause = false;
					higherPlayer.getPlayButton().doClick();
					higherPlayer.getSongNameLabel().setText("");
				}

				Enumeration<SongNode> e = node.children();
				while (e.hasMoreElements()) {
					songlist.remove(e.nextElement());
				}

				if (songlist.isEmpty()) {
					addLrcFile.setEnabled(false);
					addLrcFloder.setEnabled(false);
				}

				node.removeAllChildren();

				// 设歌曲列表的歌曲数为0
				updateSongNumInList(node);

				tree.updateUI();
			});

		// 删除歌曲
		removeSong.addActionListener(event -> {
			TreePath path = tree.getSelectionPath();

			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();

			// 该选中节点是歌曲目录
				if (path.getPathCount() == 2)
					return;

				// 获取当前歌曲的列表 用于更新列表中歌曲数
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
						.getParent();

				// 终止当前歌曲播放
				if (node == higherPlayer.getPlayingSong()
						&& higherPlayer.playThread != null) {

					higherPlayer.end();
					higherPlayer.IsPause = false;
					higherPlayer.getPlayButton().doClick();
					higherPlayer.getSongNameLabel().setText("");
				}

				songlist.remove(node);
				if (songlist.isEmpty()) {
					addLrcFile.setEnabled(false);
					addLrcFloder.setEnabled(false);
				}

				node.removeFromParent();
				updateSongNumInList(parent);
				tree.updateUI();
			});

		// 增加本地歌曲
		addSongFile.addActionListener(event -> {
			TreePath path = tree.getSelectionPath();

			if (path == null)
				return;

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();

			// 选中节点是第3级
				if (path.getPathCount() == 3)
					node = (DefaultMutableTreeNode) node.getParent();

				// 设置JFileChooser可多选音频文件
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(songFilter);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
					return;

				File[] files = fileChooser.getSelectedFiles();

				addSongs(node, files);

			});

		// 增加本地歌曲文件夹
		addSongFolder.addActionListener(event -> {

			// JTree的处理和"增加本地歌曲"一样
				TreePath path = tree.getSelectionPath();

				if (path == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();

				// 选中节点是第3级
				if (path.getPathCount() == 3)
					node = (DefaultMutableTreeNode) node.getParent();

				// 设置JFileChooser只选文件夹
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);

				if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
					return;

				// 获取选中的文件夹
				File directory = fileChooser.getSelectedFile();

				// 获取文件夹中的文件,以SongFilter过滤
				File[] files = directory
						.listFiles(new AFilter(".mid;.mp3;.wav"));

				if (files.length == 0)
					return;

				addSongs(node, files);

			});

		// 歌词文件
		addLrcFile
				.addActionListener(event -> {
					fileChooser.setMultiSelectionEnabled(true);
					fileChooser.setFileFilter(lrcFilter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
						return;

					File[] files = fileChooser.getSelectedFiles();

					addLrcs(files);

				});

		// 歌词文件夹
		addLrcFloder
				.addActionListener(event -> {

					fileChooser
							.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.setMultiSelectionEnabled(false);

					if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
						return;

					// 获取选中的文件夹
					File directory = fileChooser.getSelectedFile();

					// 获取文件夹中的文件,以SongFilter过滤
					File[] files = directory.listFiles(new AFilter(".lrc"));

					if (files.length == 0)
						return;

					addLrcs(files);
				});

		tree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				// 右击选中歌曲
				if (e.getButton() == MouseEvent.BUTTON3) {

					// 获得一个最接近击点的节点路径
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());

					if (path != null)
						tree.setSelectionPath(path);

				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				// 鼠标左击两次 播放歌曲
				if (e.getButton() == MouseEvent.BUTTON1) {

					if (e.getClickCount() == 2) {
						TreePath path = tree.getSelectionPath();

						// 该选中节点是否为歌曲
						if (path != null && path.getPathCount() == 3) {

							SongNode songNode = (SongNode) path
									.getLastPathComponent();

							if (higherPlayer.getJTree() == null
									|| !tree.equals(higherPlayer.getJTree())) {
								higherPlayer.setJTree(tree);
							}

							// 如果此歌曲节点是网络资源,则载入其URL
							if (songNode.getHTTPFlag())
								higherPlayer.load(songNode,
										songNode.getDataURL());
							else
								higherPlayer.load(songNode);

							higherPlayer.getPlayButton().doClick();

						}
					}
				}
			}
		});

		tree.addTreeSelectionListener(event -> {

			TreePath path = tree.getSelectionPath();

			// 选中的节点是列表
			if (path.getPathCount() == 2) {
				// 从当前选中的列表开始操作

				higherPlayer.setCurrentListPath(path);
				tree.startEditingAtPath(path);

			}
		});

		tree.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				if (KeyEvent.VK_SPACE == e.getKeyCode())
					higherPlayer.getPlayButton().doClick();
			}
		});

	}

	public DefaultMutableTreeNode addList(String listName) {
		listName = listName + "[0]";
		DefaultMutableTreeNode songList = new DefaultMutableTreeNode(listName);
		topNode.add(songList);

		tree.updateUI();

		return songList;
	}

	public void addSongs(DefaultMutableTreeNode parent, File... files) {

		for (File f : files) {

			if (!f.exists())
				continue;

			SongNode node = new SongNode(f);

			// 如果当前列表存在此首歌,count不等于0,则不加入当前列表
			long count = songlist.stream()
					.filter(each -> parent.equals(each.getParent()))
					.filter(each -> each.equals(node)).count();
			// 不用判断 if(parent.isNodeChild(node))
			// 因为前面new了一个节点,同时TreeNode没调用它子节点的equals判断
			if (count != 0)
				continue;

			parent.add(node);
			// 歌曲集合加入文件
			songlist.add(node);
		}

		if (!songlist.isEmpty()) {
			addLrcFile.setEnabled(true);
			addLrcFloder.setEnabled(true);
		}

		updateSongNumInList(parent);

		tree.updateUI();
	}

	public void addLrcs(File... files) {

		for (File f : files) {

			if (!f.exists())
				continue;
			String name = f.getName();
			String lrcName = name.substring(0, name.lastIndexOf("."));

			// 当前歌曲集合中转成流式,选出那些与歌词名相同的歌曲作为一个子集合,设置子集合中歌曲的歌词
			songlist.stream()
					.filter(each -> each.getSongName().equals(lrcName))
					.forEach(each -> {
						if (each.getLrc() == null)
							each.setLrc(f);

					});

		}

	}

	// 设置列表中的歌曲数
	private void updateSongNumInList(DefaultMutableTreeNode node) {
		String listName = (String) node.getUserObject();
		listName = listName.substring(0, listName.lastIndexOf("[")) + "["
				+ node.getChildCount() + "]";
		node.setUserObject(listName);
	}

	public void addPopupMenuToTree() {
		tree.setComponentPopupMenu(popupMenu);
		tree.updateUI();
	}

	public void setPlayer(HigherPlayer player) {
		this.higherPlayer = player;
	}

	public JTree getTree() {
		return tree;
	}
}
