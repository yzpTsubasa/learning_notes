## android

## android 调试相关命令行
``` bash
# 最近打开的应用
adb shell dumpsys activity recents
adb shell dumpsys activity recents | find "intent={"
adb shell dumpsys activity recents | findstr intent={

# 查看apk信息
%ANDROID_HOME%/build-tools/***/aapt dump badging ***.apk
%ANDROID_HOME%/build-tools/***/aapt dump badging ***.apk | find "package: name="
%ANDROID_HOME%/build-tools/***/aapt dump badging ***.apk | find "launchable-activity"


# 连接mumu模拟器
adb connect 127.0.0.1:7555
# 查看进程(包名等信息)
adb shell ps
# 查看已安装的所有包名
adb shell pm list packages
# 查看已连接的设备
adb devices

# 安装apk
adb install "<apk路径>"
# 安装apk(强制覆盖)
adb install -r "<apk路径>"
# 卸载包
adb uninstall "<包名>"

# 清除之前的日志信息
adb logcat -c
# 打印log的详情日志
adb logcat -v time
# 把日志输出到电脑的上查看，在窗口打印的同时，文件也会同时打印的
adb logcat -v time > ./adb.log

```

## 安卓在Windows下存在 ndk 编译时，尽量把项目放在接近磁盘根目录，减少路径长度，防止编译失败
``` bash
# 超过256个字符的路径（266）
D:/CCC/SVN/Yong/Android/ChengYuXXXcsj/build/jsb-link/frameworks/runtime-src/proj.android-studio/app/build/intermediates/ndkBuild/channel50006/debug/obj/local/armeabi-v7a/objs-debug/cocos2dx_static/scripting/js-bindings/jswrapper/v8/debugger/inspector_socket_server.o
```
## mac下解压bin文件
在mac下要解压Android-ndk-r10e-darwin-x86_64.bin文件。

1、进入文件所在目录，修改文件的读取权限

chmod a+x android-ndk-r10e-darwin-x86_64.bin  
2、解压文件

./android-ndk-r10e-darwin-x86_64.bin  
然后静静地等待解压完成就OK了。
##  Android 打包文件名配置
``` java
// 打包后应用名称
// 新版gradle已经去除outputFile属性
// 无法再通过之前的output.outputFile = new File(dir，newName)输入到指定目录
applicationVariants.all { variant ->
    // 此处为 all,并非 each
    variant.outputs.all { output ->
        def outputFile = output.outputFile
        def fileName
        if (outputFile != null && outputFile.name.endsWith('.apk')) {
            if (variant.buildType.name.equals('release')) {//如果是release包
                fileName = "anjian_release_v${defaultConfig.versionName}.apk"
            } else if (variant.buildType.name.equals('debug')) {//如果是debug包
                fileName = "anjian_debug_v${defaultConfig.versionName}.apk"
            }
            // 此处直接给outputFileName赋值，而非 output.outputFile
            outputFileName = fileName
        }
    }
}
```

## 安卓Unsupported Modules Detected问题解决方法
### 关闭工程及AS。
- 打开工程文件夹。
- 删除.idea文件夹
- 搜索*.iml文件，全删除。
- 重新打开AS，完成建立后问题解决。（嗯我是这么解决的）