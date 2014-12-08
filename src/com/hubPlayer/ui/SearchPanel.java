package com.hubPlayer.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.hubPlayer.search.SearchSong;
import com.hubPlayer.song.SongInfos;
import com.hubPlayer.ui.tool.ButtonToolBar;
import com.hubPlayer.ui.tool.IconButton;
import com.hubPlayer.ui.tool.LibraryPanel;
import com.hubPlayer.ui.tool.LibraryTableModel;

/**
 * 搜索功能面板
 * 
 * @date 2014-10-26
 */

public class SearchPanel extends JPanel {

	// 搜索功能相关
	private JTextField textField;
	private JButton searchButton;
	// 记录前一次输入的文本
	private String beforeKey;
	// 解析由关键字产生的搜索网页
	private SearchSong searchSong;
	private Thread searchThread;

	private JButton userButton;

	// 主要用于切换展示面板页面
	private ButtonToolBar hubToolBar;

	private JButton[] toolBarButtons;

	// 与展示面板相关
	private ShowPanel showPanel;
	private CardLayout cardLayout;

	private LibraryPanel libraryPanel;
	private LibraryTableModel libraryTableModel;

	// 最大搜索页数
	private int maxPage;

	// 音乐库数据集
	private Map<String, List<SongInfos>> songLibraryMap;

	public SearchPanel() {

		setLayout(new BorderLayout());
		setOpaque(false);

		init();
		setAction();
		createLayout();
	}

	private void init() {
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(200, 30));

		searchButton = new IconButton("搜索", "icon/search.png");
		userButton = new IconButton("用户", "icon/user.png");

		searchButton.setPreferredSize(new Dimension(50, 30));
		userButton.setPreferredSize(new Dimension(50, 30));

		hubToolBar = new ButtonToolBar(JToolBar.HORIZONTAL, 6);
		hubToolBar.setPreferredSize(new Dimension(300, 65));

		toolBarButtons = new JButton[6];

		toolBarButtons[0] = new IconButton("折叠", "icon/collapse.png");
		toolBarButtons[1] = new IconButton("乐库");
		toolBarButtons[1].setText("乐库");
		toolBarButtons[2] = new IconButton("MV");
		toolBarButtons[2].setText("MV");
		toolBarButtons[3] = new IconButton("歌词");
		toolBarButtons[3].setText("歌词");
		toolBarButtons[4] = new IconButton("电台");
		toolBarButtons[4].setText("电台");
		toolBarButtons[5] = new IconButton("直播");
		toolBarButtons[5].setText("直播");

		hubToolBar.addButtons(toolBarButtons);

		searchSong = new SearchSong();
		maxPage = 100;
	}

	private void createLayout() {

		Box Box1 = Box.createHorizontalBox();
		Box1.add(Box.createHorizontalStrut(10));
		Box1.add(userButton);
		Box1.add(Box.createHorizontalStrut(20));
		Box1.add(textField);
		Box1.add(Box.createHorizontalStrut(5));
		Box1.add(searchButton);
		Box1.add(Box.createHorizontalStrut(10));

		Box Box2 = Box.createVerticalBox();
		Box2.add(Box.createVerticalStrut(7));
		Box2.add(Box1);
		Box2.add(Box.createVerticalStrut(5));

		add(Box2, BorderLayout.NORTH);
		add(hubToolBar, BorderLayout.CENTER);
	}

	private void setAction() {

		for (int i = 1; i < toolBarButtons.length; i++) {
			int k = i;
			toolBarButtons[i].addActionListener(event -> {
				cardLayout.show(showPanel, String.valueOf(k));
			});

		}

		searchButton.addActionListener(event -> {

			String key = textField.getText();

			if (!prejudgeForSearchButton(key))
				return;

			searchThread = new Thread(() -> {
				searchForSearchButton(key);
			});

			searchThread.start();

		});

		// 初始显示百度音乐新歌
		// textField.setText("百度音乐新歌榜/月榜");
		// searchButton.doClick();
	}

	private void setMoreSearchAction(JButton moreSearch) {

		moreSearch.addActionListener(event -> {

			String key = textField.getText();

			if (!prejudgeForMoreSearch(key))
				return;

			if (!key.equals(beforeKey)) {
				textField.setText(beforeKey);
				key = beforeKey;
			}

			// 判断歌曲库映射是否包含此关键字,没有则进行searchButton的搜索
				if (songLibraryMap.containsKey(key)) {

					int searchPage = searchSong.getPage() + 1;

					if (searchPage > maxPage) {
						JOptionPane.showMessageDialog(null, "已经没有更多数据", "",
								JOptionPane.PLAIN_MESSAGE);
						return;
					}

					String searchKey = key;
					searchThread = new Thread(() -> {

						searchForMoreSearch(searchKey, searchPage);

					});
					searchThread.start();
				}

				else {
					searchButton.doClick();
				}

			});
	}

	// 计算歌曲分页
	private int countPage(int songNumber) {
		int page = songNumber / 20;
		if (songNumber % 20 != 0)
			page++;
		return page;
	}

	// 预判搜索关键字
	private boolean prejudgeForSearchButton(String key) {
		if (searchThread != null && searchThread.isAlive()) {
			JOptionPane.showMessageDialog(null, "正在搜索数据中,请耐心等待", "",
					JOptionPane.PLAIN_MESSAGE);
			return false;
		}

		if (key == null || key.length() == 0)
			return false;

		// 搜索关键字没变且不是要进行分页搜索
		if (key.equals(beforeKey))
			return false;

		return true;

	}

	// 预判搜索关键字
	private boolean prejudgeForMoreSearch(String key) {
		// 正在搜索中
		if (searchThread != null && searchThread.isAlive()) {
			JOptionPane.showMessageDialog(null, "正在搜索数据中,请耐心等待", "",
					JOptionPane.PLAIN_MESSAGE);
			return false;
		}

		if (beforeKey == null || beforeKey.length() == 0)
			return false;

		return true;
	}

	// 搜索过程
	private void searchForSearchButton(String key) {
		// <-----------搜索数据------------>
		if (!songLibraryMap.containsKey(key)) {
			// 清除前次解析的信息
			searchSong.clear();

			// 设置关键字 进行此次解析
			if (!searchSong.setKey(key).openConnection()) {
				// 连接失败
				beforeKey = "";
				return;
			}

			if (songLibraryMap.containsKey(key)) {

				// 获取它的上限页数
				maxPage = countPage(searchSong.getSongNumber());
			}
		}

		// <-----------数据加入表格------------>
		// 表格选中数据和取消选中，是为了去掉一个Bug:在当前页面操作单元格时，当换新页时，那个被操作的单元格数据不更新
		libraryPanel.getDataTable().selectAll();

		// 表格数据清空
		libraryTableModel.deleteTableData();
	

		List<SongInfos> songList = songLibraryMap.get(key);
		int addSongNum = songList.size();

		// 更新乐库表格数据
		songList.subList(0, addSongNum).forEach(
				each -> libraryTableModel.updateData(each));

		libraryPanel.getDataTable().clearSelection();

		// 保留此次搜索的关键字
		beforeKey = key;

		int page = countPage(addSongNum);
		searchSong.setPage(page);

	}

	// 搜索过程
	private void searchForMoreSearch(String key, int page) {

		searchSong.setPage(page);

		List<SongInfos> songList = songLibraryMap.get(key);

		int songNum = songList.size();
		// 设置关键字 进行此次解析
		if (!searchSong.setKey(key).openConnection()) {
			// 连接失败
			return;
		}

		maxPage = countPage(searchSong.getSongNumber());

		songList.subList(songNum, songList.size()).forEach(
				each -> libraryTableModel.updateData(each));

		// libraryPanel.getTableScrollBar()
		// .setValue(
		// libraryPanel.getTableScrollBar().getMaximum()+1);

	}

	public void setShowPanel(ShowPanel showPanel) {
		this.showPanel = showPanel;
		this.cardLayout = (CardLayout) showPanel.getLayout();

		// 沟通乐库面板与搜索歌曲信息
		libraryPanel = showPanel.getLibraryPanel();
		libraryTableModel = libraryPanel.getLibraryTableModel();

		setMoreSearchAction(libraryPanel.getMoreSearch());

	}

	// HubFrame传进来的歌曲库集合
	public void setSongLibraryMap(Map<String, List<SongInfos>> songLibraryMap) {

		this.songLibraryMap = songLibraryMap;
		searchSong.setSongLibraryMap(songLibraryMap);
	}

	// 折叠面板按钮
	public JButton getCollapseButton() {
		return toolBarButtons[0];
	}

}
