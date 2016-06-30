package com.hubPlayer.main;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

import com.hubPlayer.ui.HubFrame;

/**
 * 以酷狗音乐播放器的用户界面为原型，制作的一个基于Java Sound，实现播放、搜索、下载的音乐播放器，支持的音乐格式只有WAV、MP3、MID。
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