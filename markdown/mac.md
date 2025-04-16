## mac

## 添加 PATH
``` sh
# 全局,一行一个路径
/etc/paths
```

## 查看隐藏的文件
<kbd>command</kbd> + <kbd>shift</kbd> + <kbd>.</kbd>

## 处理 zsh compinit: insecure directories, run compaudit for list
``` bash
sudo chmod -R 755 /usr/local/share/zsh/site-functions
```
## 切换系统偏好设置上的红点提示
``` bash
# 关闭
defaults write com.apple.systempreferences AttentionPrefBundleIDs 0 && killall Dock
# 恢复
defaults write com.apple.systempreferences AttentionPrefBundleIDs 1 && killall Dock
```
## 安装 homebrew
``` bash
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
# 可能存在网络无法连接raw.githubusercontent.com 的问题
# 可参考 networks.md 进行处理
```
## 查看可执行文件的位置
``` bash
which <target>
# 如
which egret
# > /usr/local/bin/egret
```
## zsh 配置环境变量
``` bash
# 创建文件 ~/.zshrc
touch ~/.zshrc
# 导出环境变量
export DLDL_PUB_TOOLS_DIR="/Users/tsubasa/Documents/projects/HGPubTools/dist/"
export DLDL_PROJECTS_ROOT="/Users/tsubasa/Documents/projects/DLDL_WX/"
export EGRET_CMD="/usr/local/bin/egret"
export DLDL_VERSION_MD="/Users/tsubasa/Documents/projects/notes/markdown/版本详情.md"
export PS1="%~>"
# 重启terminal生效
```
##  启动 mongodb
```
sudo mongod --dbpath=/Users/tsubasa/data/db
```
##  symlink 使用“符号链接"安装库
```
sudo ln -s /usr/bin/python2.7 /usr/local/bin/python2
```
##  查看端口占用情况及杀死进程
``` bash
# 查看端口占用情况命令
# 冒号后面就是你需要查看的端口号。
sudo lsof -i:<PORT>
lsof -P -itcp:<PORT>
# 杀掉占用当前端口号的进程
# -9后面加一个空格，然后加上占用端口的进程PID
sudo kill -9 <PORT>
```
##  Tomcat 权限问题 Permission Denied
> 出现的错误提示如下：
```
Error running Tomcat 8.0.18: Cannot run program "/Users/horse_leo/Documents/apache-tomcat-8.0.18/bin/catalina.sh" (in directory "/Users/horse_leo/Documents/apache-tomcat-8.0.18/bin"): error=13, Permission denied
```

> 提示的主要问题是权限不足
>
> 解决办法
>
> 打开终端，进入`tomcat\bin`目录，然后执行
```
chmod 777 *.sh
```
> 这个世界安静了，问题解决了。
##  Xcode Build Settings
``` bash
- Build Setting
    - Search Paths
        - Header Search Paths
            /usr/local/include
        - Library Search Paths
            /usr/local/lib
# 引入静态库 *.a 
- Build Phases
    - Link Binary With Libraries
# 引入动态库 *.dylib
- Build Setting
    - Linking
        - Other Linker Flag
        添加 -l< 所需 dylib 的名称 > 
```
##  把文件设置为可执行文件
``` bash
chmod 777 filename
```
##  终止 istream >> var
`ctrl + D`
##   软件文件已损坏
> 1、macOS High Seirra 安装破解软件打开后提示“xxx”已损坏，打不开，您应该将它移至垃圾篓的解决方法：
>
> 打开终端，然后输入以下命令：
```
sudo spctl --master-disable
```
> 然后回车，输入系统密码后按回车（这里输入密码不会显示），如果没有提示即操作成功。
>
> 打开系统偏好设置进入“安全性与隐私”，查看“允许从以下位置下载的应用”是否选中“任何来源”，如果已选中即操作成功，再打开破解软件就不会再有打不开的提示了。
>
> 2、App 在macOS Catalina下提示已损坏无法打开解决办法：
打开终端；
输入以下命令，回车；
``` bash
sudo xattr -d com.apple.quarantine /Applications/xxxx.app
```
> 注意：`/Applications/xxxx.app` 换成你的App路径
重启App即可。
##  对动态库重新签名 *.dylib
> code signature in (*.dylib) not valid for use in process using Library Validation: mapped file has no cdhash, completely unsigned? Code has to be at least ad-hoc signed.
``` bash
codesign -f -s "Apple Development: 865500815@qq.com" libyaml-cpp.0.6.3.dylib
```

## zsh: permission denied问题的解决办法
### 问题原因:
用户没有权限，所以才出现了这个错误，所以只需要用chmod修改一下权限就可以了
### 解决方法
`chmod u+x *.sh`
### 说明
chmod是权限管理命令change the permissions mode of a file的缩写。
u代表所有者。x代表执行权限。’+’ 表示增加权限。
chmod u+x file.sh 就表示对当前目录下的file.sh文件的所有者增加可执行权限。

## 解决 homebrew 更新或者安装太慢的问题
> `~/.bash_profile` 添加以下内容
``` bash

#THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

####### brew安装镜像加速
# 
# brew安装仓库加速配置 这里可选配置阿里,ustc或者清华的加速地址, 他们的加速地址前缀如下:
# 阿里: https://mirrors.aliyun.com/homebrew   这个加速地址,结果测试无法git clone 访问,在克隆taps时可能会出现问题
# tuna: https://mirrors.tuna.tsinghua.edu.cn
# ustc: https://mirrors.ustc.edu.cn
# 加速地址的后面部分都是一样的,修改前缀即可
# 
export HOMEBREW_BREW_GIT_REMOTE="https://mirrors.ustc.edu.cn/brew.git"
export HOMEBREW_CORE_GIT_REMOTE="https://mirrors.ustc.edu.cn/homebrew-core.git"
export HOMEBREW_BOTTLE_DOMAIN="https://mirrors.ustc.edu.cn/homebrew-bottles"
# brew4.x API加速
export HOMEBREW_API_DOMAIN="https://mirrors.ustc.edu.cn/homebrew-bottles/api"
# hide
export HOMEBREW_NO_ENV_HINTS="1"
```
