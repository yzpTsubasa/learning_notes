## android
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