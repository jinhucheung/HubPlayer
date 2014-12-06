package com.hubPlayer.main;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

import com.hubPlayer.ui.HubFrame;

/**
 * 以kugou音乐播放器为布局原型，编写一个实现播放、搜索歌曲的播放器
 *
 */

public class Main {
	public static void main(String[] args) {

		try {
			// 设置观感
			UIManager
					.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel");
			// 设置水印
			SubstanceLookAndFeel
					.setCurrentWatermark("org.jvnet.substance.watermark.SubstanceMosaicWatermark");
			// 设置渐变渲染
			SubstanceLookAndFeel
					.setCurrentGradientPainter("org.jvnet.substance.painter.WaveGradientPainter");

			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(() -> {
			new HubFrame();
		});

	}

}
