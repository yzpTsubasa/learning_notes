## ffmpeg

##  ffmpeg工具
http://ffmpeg.org/ffmpeg.html#Main-options
```
ffmpeg -i test.mp4 -vcodec copy -acodec copy -ss 00:00:10 -to 00:00:15 out.mp4
```

##  音频转换
```
ffmpeg -i my_audio.wav  my_audio.mp3
```
`-i` 后为要转换的音频文件,`my_audio.mp3`为目的音频文件

##  视频转换
```
ffmpeg -i my_video.mpeg -s 500×500 my_video.flv
```
`-i` 后为源视频文件, `-s` 表示设置目标视频文件的分辨率   `my_video.flv`为目的视频文件

##  从视频中截取图片
```
ffmpeg -i test.mpg image%d.jpg
```
默认1s截取25张图片,可以通过`-r`设置每秒截取的图片数量
`-r fps` 设置帧率,也就是每秒截取图片的数量(默认25)
```
ffmpeg -i test.mpg -r 1 image%d.jpg
```
这样子每1s截取1张图片
还可以设置截取间隔,起止
> `-ss `设定时间位置,语法:hh:mm:ss[.xxx]
> 
> `-t `时长:限制转码/捕获视频的时间,语法:hh:mm:ss[.xxx]
```
ffmpeg -i test.mpg -r 25 -ss 00:00:10 -t 00:00:05 images%05d.png
```
在第10秒开始,以每秒截取25张图片的速度,截取5秒时长的图片

##  从视频中采集音频
```
ffmpeg -i video.avi -f mp3 audio.mp3
```
> -f 强制选择格式
```
ffmpeg -i video.avi -vn audio.mp3
```
> -vn 取消截取视频(也就是只输出音频文件)

##  创建截屏视频
```
ffmpeg -f x11grab -r 25 -s wxga -i :0.0 /tmp/outputFile.mpg
```
> 0.0 是你X11 server的屏幕显示号吗,和DISPLAY一样样的.
> 
> 此条命令以每秒25帧的速率来截取wxga屏幕视频,当然这里可以用-s 来设置视频分辨率,输出文件是/tmp/outputFile.mpg

##  用图片制作视频
```
ffmpeg -f image2 -i img%d.jpg /tmp/a.mpg
```
将`img001.jpg`, `img002.jpg`这种顺序排列的图片文件转制作为视频

##  从webcam中截取视频
```
ffmpeg -f video4linux2 -s 320x240 -i /dev/video0 out.mpg
```
同时截取音频和视频:
```
ffmpeg -f oss -i /dev/dsp -f video4linux2 -s 320x240 -i /dev/video0 out.mpg
```
`/dev/video0`为视频设备 `/dev/dsp`为音频设备

## 拼接音频
``` sh
ffmpeg -i test1.mp3 -i test2.mp3 -filter_complex 'concat=n=2:v=0:a=1[a]' -map '[a]' output.mp3
```