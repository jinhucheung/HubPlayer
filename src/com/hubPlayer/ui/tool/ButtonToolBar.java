package com.hubPlayer.ui.tool;

import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * The HubToolBar is on the left of the HubFrame. It extends JToolBar. There are
 * four HubButtons (note\download\application\clouds) in the ToolBar
 * 
 * @date 2014-10-1
 */

public class ButtonToolBar extends JToolBar {


	private JButton[] buttons;


	public ButtonToolBar() {
		setBorderPainted(false);

		setMargin(new Insets(0, 5, 5, 5));
		setFloatable(false);
		setOpaque(false);
		setBorderPainted(false);

	}

	public ButtonToolBar(int orentation, int buttonNum) {
		this();

		setOrientation(orentation);

		if (orentation == JToolBar.VERTICAL)
			setLayout(new GridLayout(buttonNum, 1));

		else
			setLayout(new GridLayout(1, buttonNum));

	}

	public void addButtons(JButton[] buttons) {

		this.buttons = buttons;

		for (int i = 0; i < buttons.length; i++) {
			add(buttons[i]);
		}

		
	}


	public JButton[] getButtons() {
		return buttons;
	}
}
