package com.hubPlayer.ui.tool;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * 图片按钮
 * 
 * @date 2014-10-7
 */

public class IconButton extends JButton {

    public IconButton(String tip) {
		setToolTipText(tip);
		setBorderPainted(false);
		setOpaque(false);
		setContentAreaFilled(false);

	}

	public IconButton(String tip, String imgUrl) {
		this(tip);
		setIcon(new ImageIcon(imgUrl));
	}

	public String toString() {
		return getToolTipText();
	}
}