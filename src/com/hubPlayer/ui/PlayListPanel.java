package com.hubPlayer.ui;

import java.awt.CardLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.hubPlayer.player.HigherPlayer;
import com.hubPlayer.ui.tool.AppPanel;
import com.hubPlayer.ui.tool.SongListPanel;

/**
 * 播放列表面板
 * 
 * @date 2014-10-12
 */

public class PlayListPanel extends JPanel {

    private SongListPanel songListPanel;
	private SongListPanel cloudsPanel;
	private SongListPanel downloadPanel;
	private JScrollPane appPanel;

	private JButton[] buttons;
	private JFrame parent;

	private CardLayout card;

	public PlayListPanel(JButton[] buttons, JFrame parent) {

		setOpaque(false);

		card = new CardLayout();
		setLayout(card);

		this.buttons = buttons;
		this.parent = parent;

		initPanel();
	}

	private void initPanel() {
		songListPanel = new SongListPanel("默认列表");
		songListPanel.addPopupMenuToTree();

		cloudsPanel = new SongListPanel("默认列表", "我喜欢");
		cloudsPanel.addPopupMenuToTree();

		downloadPanel = new SongListPanel("下载中", "已下载");

		appPanel = new JScrollPane(new AppPanel(parent));

		add(songListPanel, "0");
		add(cloudsPanel, "1");
		add(downloadPanel, "2");
		add(appPanel, "3");

		setAction();
	}

	private void setAction() {

		for (int i = 0; i < buttons.length; i++) {
			int k = i;
			buttons[i].addActionListener(event -> {
				card.show(this, "" + k);
			});
		}

	}

	public void deliverHigherPlayer(HigherPlayer HigherPlayer) {
		songListPanel.setPlayer(HigherPlayer);
		cloudsPanel.setPlayer(HigherPlayer);
		downloadPanel.setPlayer(HigherPlayer);
	}

	public JTree[] deliverTree() {
		return new JTree[] { songListPanel.getTree(), cloudsPanel.getTree(),
				downloadPanel.getTree() };
	}
}