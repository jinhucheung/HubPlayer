package com.hubPlayer.ui.tool;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.hubPlayer.expansion.game_2048.My2048;
import com.hubPlayer.expansion.game_snake.MySnake;
import view.ChessFrame;

/**
 * 拓展功能面板，为播放器增加一些游戏程序
 * 
 * @date 2014-10-13
 *
 */

public class AppPanel extends JPanel {

	private JButton game2048;
	private JButton gameSnake;
	private JButton gameGobang;

	private JFrame parent;

	public AppPanel(JFrame parent) {
		this.parent = parent;

		setLayout(new GridLayout(3, 3));
		setSize(300, 500);

		addButtons();
		setButtonsAction();
	}

	private void addButtons() {

		game2048 = new IconButton("2048", "icon/item.png");
		add(game2048);

		gameSnake = new IconButton("贪吃蛇", "icon/item.png");
		add(gameSnake);

		gameGobang = new IconButton("网络五子棋", "icon/item.png");
		add(gameGobang);

		add(new IconButton(null, "icon/item.png"));

		add(new IconButton(null, "icon/item.png"));
		add(new IconButton(null, "icon/item.png"));
		add(new IconButton(null, "icon/item.png"));

		add(new IconButton(null, "icon/item.png"));
		add(new IconButton(null, "icon/item.png"));

	}

	private void setButtonsAction() {

		game2048.addActionListener(event -> {
			new My2048(parent);
		});

		gameSnake.addActionListener(event -> {
			new MySnake(parent);
		});

		gameGobang.addActionListener(event -> {
			new ChessFrame(parent);
		});
	}
}
