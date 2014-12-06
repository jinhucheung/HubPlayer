package com.hubPlayer.player;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import com.hubPlayer.ui.tool.TimeProgressBar;

/**
 * 底层播放器，主要控制播放
 * 
 * @date 2014-10-18
 */

public class BasicPlayer {

	public SourceDataLine sourceDataLine;
	private AudioInputStream audioInputStream;
	public URL audio;
	public boolean HTTPFlag;

	public Thread playThread;

	public boolean IsPause = true;// 是否停止播放状态
	public boolean NeedContinue;// 当播放同一首歌曲 是否继续播放状态
	public boolean IsComplete;
	public boolean IsEnd;

	// 检测输入流是否阻塞
	private boolean IsChoke;

	private Timer checkConnection;

	// 音量控制
	private FloatControl floatVoiceControl;
	public TimeProgressBar timerProgressBar;

	public synchronized void play() {

		try {

			// 获取网络音频输入流
			if (HTTPFlag) {

				try {

					HttpURLConnection urlConnection = (HttpURLConnection) audio
							.openConnection();
					audioInputStream = AudioSystem
							.getAudioInputStream(urlConnection.getInputStream());

					// 用计时器监测歌曲连接状态 初始启动计时器
					checkConnectionSchedule();
					

				} catch (ConnectException e) {

					// 进度条清零
					timerProgressBar.cleanTimer();

					// 连接超时
					JOptionPane.showMessageDialog(null, "网络资源连接异常", "",
							JOptionPane.PLAIN_MESSAGE);

					return;
				}

			}

			//
			else
				// 获取本地音频输入流
				audioInputStream = AudioSystem.getAudioInputStream(audio);

			// 获取音频编码格式
			AudioFormat audioFormat = audioInputStream.getFormat();
			// MPEG1L3转PCM_SIGNED
			if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						audioFormat.getSampleRate(), 16,
						audioFormat.getChannels(),
						audioFormat.getChannels() * 2,
						audioFormat.getSampleRate(), false);
				audioInputStream = AudioSystem.getAudioInputStream(audioFormat,
						audioInputStream);
			}

			// 根据上面的音频格式获取输出设备信息
			DataLine.Info info = new Info(SourceDataLine.class, audioFormat);
			// 获取输出设备对象
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);

			// 打开输出管道
			sourceDataLine.open();
			// 允许此管道执行数据 I/O
			sourceDataLine.start();

			// 获取总音量的控件
			floatVoiceControl = (FloatControl) sourceDataLine
					.getControl(FloatControl.Type.MASTER_GAIN);

			// 音量minValue -80 maxValue 6
			// 设合适的初始音量
			floatVoiceControl.setValue(-20);

			byte[] buf = new byte[1024];
			int onceReadDataSize = 0;

			while ((onceReadDataSize = audioInputStream
					.read(buf, 0, buf.length)) != -1) {
				// 输入流没有阻塞
				IsChoke = false;

				if (IsEnd) {
					return;
				}

				// 是否暂停
				if (IsPause)
					pause();

				// 将数据写入混频器中 至输出端口写完前阻塞
				sourceDataLine.write(buf, 0, onceReadDataSize);

				// 预设输入流阻塞
				IsChoke = true;
			}

			IsChoke = false;
			// 冲刷缓冲区数据
			sourceDataLine.drain();

			sourceDataLine.close();
			audioInputStream.close();

			if (checkConnection != null) {
				checkConnection.cancel();
				checkConnection.purge();
				checkConnection = null;
				// System.out.println("EndTimeOutControl");
			}

		} catch (UnsupportedAudioFileException | IOException
				| LineUnavailableException | InterruptedException e) {

			e.printStackTrace();

		}
	}

	public void load(URL url) {
		this.audio = url;
	}

	public void checkConnectionSchedule() {

		checkConnection = new Timer(true);

		checkConnection.schedule(new TimerTask() {

			// 阻塞计数
			int times = 0;

			@Override
			public void run() {

				if (IsChoke) {
					times++;

					// 如果检测到阻塞次数有20次
					if (times == 20) {
						try {

							// 进度条清零
							timerProgressBar.cleanTimer();

							// 使playThread自然执行完
							IsEnd = false;

							// 输入流关闭
							audioInputStream.close();

							JOptionPane.showMessageDialog(null, "连接异常中断", "",
									JOptionPane.PLAIN_MESSAGE);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else
					times = 0;
				// System.out.println(times);
			}

		}, 2000, 500);

	}

	public synchronized void resume() {

		IsPause = false;
		NeedContinue = false;
		this.notify();

	}

	private synchronized void pause() throws InterruptedException {
		NeedContinue = true;
		this.wait();

	}

	public void end() {
		try {

			if (playThread == null)
				return;

			IsPause = true;
			NeedContinue = false;
			IsComplete = false;
			IsEnd = true;

			// 关闭当前数据输入管道
			sourceDataLine.close();
			audioInputStream.close();

			playThread = null;

		} catch (Exception e) {
			System.out.println("中断播放当前歌曲");
			IsPause = true;
			NeedContinue = false;
			IsComplete = false;
			IsEnd = true;
		}

	}

	// 获取音频文件的长度 秒数
	public int getAudioTrackLength(URL url) {
		try {

			// 只能获得本地歌曲文件的信息
			AudioFile file = AudioFileIO.read(new File(url.toURI()));

			// 获取音频文件的头信息
			AudioHeader audioHeader = file.getAudioHeader();
			// 文件长度 转换成时间
			return audioHeader.getTrackLength();
		} catch (CannotReadException | IOException | TagException
				| ReadOnlyFileException | InvalidAudioFrameException
				| URISyntaxException e) {
			e.printStackTrace();
			return -1;
		}

	}

	public String getAudioTotalTime(int sec) {
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

	public SourceDataLine getSourceDataLine() {
		return sourceDataLine;
	}

	public FloatControl getFloatControl() {
		return floatVoiceControl;
	}

}
