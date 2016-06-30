package com.hubPlayer.ui.tool;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.hubPlayer.song.SongInfos;

/**
 * 一个歌曲信息表格模型,继续默认表格模式DefaultTableModel,实现单元格渲染器TableCellRenderer
 * 表格头为歌曲(String),歌手(String),专辑(String),操作(JPanel) 定义渲染器显示操作面板
 * 其中操作面板包含3个按钮:播放,加入列表,下载
 * 
 * @date 2014-11-7
 */

public class LibraryTableModel extends DefaultTableModel {

    // 标题栏
	private static final String[] title = new String[] { "歌曲", "歌手", "专辑", "操作" };
	// 标题栏对应的数据类型
	private static final Class[] types = new Class[] { String.class,
			String.class, String.class, Object.class };

	// 标题栏对应的可编辑状态
	private static final boolean[] canEdit = new boolean[] { false, false,
			false, true };

	// 表格初始数据
	private static final Object[][] initData = new Object[][] {
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null } };

	// 操作面板
	private LibraryOperation libraryOperation;

	public LibraryTableModel() {

		super(initData, title);

	}

	public void deleteTableData() {

		// 通知表格，数据要改变
		 fireTableDataChanged();
		// 清除表格行
		for (int i = getRowCount() - 1; i >= 0; i--) {
			removeRow(i);
		}
		// 清除表格数据
		getDataVector().clear();
        // 垃圾回收
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	// 检测每列对应数据类型
	public Class getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	// 检测单元格对应的可编辑状态
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit[columnIndex];
	}

	public void updateData(SongInfos songInfos) {

		String song = songInfos.getSong();
		String singer = songInfos.getSinger();
		String album = songInfos.getAlbum();

		// 操作面板,接受歌曲信息
		JPanel panel = libraryOperation.new OperationPanel(songInfos);

		Object[] data = new Object[] { song, singer, album, panel };

		addRow(data);

	}

	public void setLibraryOperation(LibraryOperation libraryOperation) {
		this.libraryOperation = libraryOperation;
	}

}