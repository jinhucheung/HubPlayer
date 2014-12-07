#HubPlayer
以酷狗音乐播放器的用户界面为原型，一个基于Java Sound,实现播放、搜索、下载歌曲的音乐播放器。
此音乐播放器支持音乐格式较少，只有MID、WMA、MP3。最后，为音乐播放器置入一些自己和同学写的游戏，增强了一点娱乐性。

![image](http://git.oschina.net/JHuZhang/HubPlayer/HubPlayer.jpg)

##功能说明
程序启动时，会在E盘创建一个Hub文件夹，Hub/SongLibrary.dat保存了播放器抓取到的歌曲数据，Hub/download保存了下载的歌曲。
播放器的进度条没有实现跳帧功能。
##使用的外包
Java Zoom：Java Zoom正是提供了一个兼容JavaSound的纯Java解码器，为Javax.Sound扩展MP3的SPI支持库。
Jaudiotagger：提供一个Java类库用于编辑音频文件的tag信息。
JSoup：一款Java 的HTML解析器，可直接解析某个URL地址、HTML文本内容。
Substance：观感包
##时间
2014-12-6