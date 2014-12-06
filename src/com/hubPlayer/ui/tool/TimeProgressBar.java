package com.hubPlayer.ui.tool;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.hubPlayer.song.LrcInfos;

/**
 * 播放面板的进度条，绑定一个计时器 计时打印歌词
 *
 * @date 2014-10-23
 */

public class TimeProgressBar extends JProgressBar {

	private boolean timerPause;
	private int audioTotalTime;
	private int counter;

	private Timer timer;
	private Task task;

	private JLabel currentTimeCountLabel;

	private LrcInfos lrcInfos;
	private Map<Integer, String> lrcInfosMap;
	private int nextLrcTime;

	// 歌词面板
	private JTextArea textArea;

	public TimeProgressBar() {
		counter = 0;
		setMinimum(0);
		setStringPainted(false);

	}

	// 启动计时器
	public void startTimer() {

		// 初始显示23条歌词
		if (lrcInfosMap.size() != 0)
			printNextLrcInTheTime(0, 23);

		timer = new Timer(true);
		task = new Task();
		timer.schedule(task, 500, 1000);

	}

	// 重启计时器
	public void resumeTimer() {
		synchronized (timer) {
			timer.notify();
		}
	}

	// 重置计时器
	public void cleanTimer() {
		counter = 0;

		if (timer != null) {
			timer.cancel();
			timer.purge();
		}

		currentTimeCountLabel.setText("0:00");
		this.setValue(0);

		textArea.setText("\n");

	}

	public void setAudioTotalTime(int n) {
		audioTotalTime = n;
		super.setMaximum(n);
	}

	public void setTimerControl(boolean IsPause) {
		this.timerPause = IsPause;
	}

	public void setCurrentTimeCountLabel(JLabel currentTimeCountLabel) {
		this.currentTimeCountLabel = currentTimeCountLabel;
	}

	public void setCurrentPlayedSongLrcInfo(LrcInfos lrcInfos) {
		this.lrcInfos = lrcInfos;
		lrcInfosMap = lrcInfos.getInfos();
	}

	public void setLrcPanelTextArea(JTextArea textArea) {
		this.textArea = textArea;

	}

	/**
	 * 打印time时间后的line条歌词 audioTotalTime当前播放歌曲的时长 nextLrcTime搜寻下一条歌词的开始时间
	 */

	private void printNextLrcInTheTime(int time, int line) {

		for (int i = 0; i < line && time <= audioTotalTime; time++) {
			String content = lrcInfos.getInfos().get(time);
			if (content != null) {
				textArea.append(content + "\n");
				nextLrcTime = time + 1;
				i++;
			}
		}
	}

	class Task extends TimerTask {

		@Override
		public void run() {
			synchronized (timer) {
				if (counter == audioTotalTime) {
					// 终止计数器 但当前任务还会被执行
					cleanTimer();
					return;
				}

				if (timerPause) {
					try {
						timer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {

					if (lrcInfosMap.size() != 0) {
						String content = lrcInfosMap.get(counter);
						if (content != null) {

							// 剪去第一行歌词，同时整体上移
							textArea.select(textArea.getLineStartOffset(1),
									textArea.getLineEndOffset(textArea
											.getLineCount() - 1));
							textArea.setText(textArea.getSelectedText());

							// 显示最后一条歌词的下一条
							printNextLrcInTheTime(nextLrcTime, 1);
						}
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

				counter += 1;
				TimeProgressBar.this.setValue(counter);
				TimeProgressBar.this.currentTimeCountLabel
						.setText(getCurrentTime(counter));
			}
		}

		// 转换时间
		public String getCurrentTime(int sec) {
			String time = "0:00";

			if (sec <= 0)
				return time;

			int minute = sec / 60;
			int second = sec % 60;
			int hour = minute / 60;

			if (second < 10)
				time = minute + ":0" + second;
			else
				time = minute + ":" + second;

			if (hour != 0)
				time = hour + ":" + time;

			return time;
		}

	}

}