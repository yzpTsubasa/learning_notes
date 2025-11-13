## android

## APK加固
> 目前使用 [梆梆加固](https://dev.bangcle.com/apps/index) 加固 APK
> 加固完成后的 APK 是没有签名的，而且可能没有对齐，需要先对齐，然后签名。
> 命令`zipalign` 和 `apksigner` 可以在 `%APPDATA%\..\local/Android\Sdk\build-tools` 目录下的指定一个版本的SDK文件夹(如：`%APPDATA%\..\local/Android\Sdk\build-tools\36.0.0`)中找到
> 命令`adb` 可以在 `%APPDATA%\..\local/Android\Sdk\platform-tools` 目录下找到

```sh
# 对齐
zipalign -p -f -v 4 [reinforce_apk_path] [reinforce_aligned_apk_path]
# 对齐验证
zipalign -c -v 4 [reinforce_aligned_apk_path]

# apk 签名
apksigner sign --ks "[keystore_path]" --ks-key-alias "[key_alias]" --ks-pass pass:[keystore_password] --key-pass pass:[key_password] --out "[reinforce_aligned_signed_apk_path]" "[reinforce_aligned_apk_path]"
# 签名验证
apksigner verify -v [reinforce_aligned_signed_apk_path]

# 安装APK
adb install -r -t [reinforce_aligned_signed_apk_path]
```
> 加固后常见错误
> 1. `INSTALL_PARSE_FAILED_NO_CERTIFICATES`：APK 无有效签名（签名被刷或签名流程未正确执行）。
> 2. `INSTALL_FAILED_INVALID_APK: Failed to extract native libraries, res=-2`：APK 中的 .so被压缩或未对齐，系统无法提取原生库。
> 3. `Failure [-124 … Targeting R+ … resources.arsc …]`：Android 11+ 要求 resources.arsc必须未压缩且按 4 字节边界对齐。


## 配置Gradle使用国内镜像
```
1. 修改项目中的 gradle-wrapper.properties 文件
在您的项目目录中找到 gradle/wrapper/gradle-wrapper.properties 文件，将distributionUrl修改为国内镜像地址：

properties
# 将原来的URL注释或替换
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-4.10.3-all.zip

# 或者使用其他镜像
# distributionUrl=https\://mirrors.aliyun.com/gradle/gradle-4.10.3-all.zip
# distributionUrl=https\://repo.huaweicloud.com/gradle/gradle-4.10.3-all.zip
```

## 配置国内Maven镜像
修改项目的 build.gradle 文件
在项目的 build.gradle 文件中添加国内镜像源：
```groovy
buildscript {
    repositories {
        // 阿里云镜像
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        // 中央仓库和Google仓库
        mavenCentral()
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.3.2' // 根据你的版本调整
    }
}

allprojects {
    repositories {
        // 阿里云镜像
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        // 中央仓库和Google仓库
        mavenCentral()
        google()
    }
}
```

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