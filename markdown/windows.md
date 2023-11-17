## windows

## 有盘符切换的cd
``` sh
cd /d "X:/YYY/ZZZ"
```

## 刷新环境变量
``` sh
refreshenv
```

## 常用变量
``` sh
# 执行文件完整路径为 XXX\X.bat, 调用执行文件的工作目录为 YYY\
%cd% # YYY
%~dp0 # XXX\
%~dpnx0 # XXX\X.bat
%~f0 # XXX\X.bat
```

## 全局环境变量设置
``` sh
# 添加
setx <key> <value>
# 如：设置执行文件所在目录为环境变量DLDL_PUB_TOOLS_DIR的值
setx DLDL_PUB_TOOLS_DIR %~dp0

# 移除
REG delete HKCU\Environment /F /V <key>
```
## PATH 变量
``` bash
# 打印PATH变量
echo %PATH%
# 添加新路径到当前会话的PATH变量
set PATH="%PATH%;<NEW_PATH>"
```
### setx 有 1024 的长度限制，path太长会被截断，不推荐下面的方法, PATH 长度最大为 2048
``` bash
# 添加新路径到当前用户的PATH变量
setx path "%PATH%;<NEW_PATH>"
# 添加新路径到系统（所有用户）的PATH变量
setx /M path "%PATH%;<NEW_PATH>"
# 添加新路径到当前用户的PATH变量
setx path "%PATH%;%cd%"
```
## 设置 HTTP 代理
``` sh
set HTTP_PROXY=http://127.0.0.1:6149
set HTTPS_PROXY=http://127.0.0.1:6149
```

## 你不能访问此共享文件夹，因为你组织的安全策略阻止未经身份验证的来宾访问
> `gpedit.msc` > `计算机配置` > `管理模板` > `网络` > `Lanman工作站` > 设置 `启用不安全的来宾登录` 为启用

## ssh-agent unable to start ssh-agent service, error :1058
``` powershell
# 以管理员身份运行
Set-Service -Name ssh-agent -StartupType automatic
```

## 安装常用工具
``` shell

# chocolatey
# https://chocolatey.org/install
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# scoop
# https://github.com/lukesampson/scoop
Invoke-Expression (New-Object System.Net.WebClient).DownloadString('https://get.scoop.sh')
# 或者
iwr -useb get.scoop.sh | iex

# make
choco install make

```
## 查看资源占用
- 任务管理器 - 性能 - 打开资源监视器
- 资源监视器 - CPU - 关联的句柄 - 搜索句柄
## 关闭 Windows Defender
- gpedit.msc
- 计算机配置 - 管理模板 - Windows组件 - Windows Defender 防病毒程序
- 关闭 Windows Defender 防病毒程序 - 启用
## 开机启动项目录
``` sh
# 运行(或者在地址栏输入)
shell:startup
# 通常结果为： C:\Users\Administrator\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup
# 把需要启动的程序快捷方式复制到该目录
```
## 安装 chocolatey
``` sh
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
```
##  bat参数

```
Calling

for /?
in the command-line gives help about this syntax (which can be used outside FOR, too, this is just the place where help can be found).

In addition, substitution of FOR variable references has been enhanced. You can now use the following optional syntax:

%~I         - expands %I removing any surrounding quotes (")
%~fI        - expands %I to a fully qualified path name
%~dI        - expands %I to a drive letter only
%~pI        - expands %I to a path only
%~nI        - expands %I to a file name only
%~xI        - expands %I to a file extension only
%~sI        - expanded path contains short names only
%~aI        - expands %I to file attributes of file
%~tI        - expands %I to date/time of file
%~zI        - expands %I to size of file
%~$PATH:I   - searches the directories listed in the PATH
               environment variable and expands %I to the
               fully qualified name of the first one found.
               If the environment variable name is not
               defined or the file is not found by the
               search, then this modifier expands to the
               empty string
The modifiers can be combined to get compound results:

%~dpI       - expands %I to a drive letter and path only
%~nxI       - expands %I to a file name and extension only
%~fsI       - expands %I to a full path name with short names only
%~dp$PATH:I - searches the directories listed in the PATH
               environment variable for %I and expands to the
               drive letter and path of the first one found.
%~ftzaI     - expands %I to a DIR like output line
In the above examples %I and PATH can be replaced by other valid values. The %~ syntax is terminated by a valid FOR variable name. Picking upper case variable names like %I makes it more readable and avoids confusion with the modifiers, which are not case sensitive.

There are different letters you can use like f for "full path name", d for drive letter, p for path, and they can be combined. %~ is the beginning for each of those sequences and a number I denotes it works on the parameter %I (where %0 is the complete name of the batch file, just like you assumed).
```
##  在此系统中禁止执行脚本
``` powershell
# 管理员身份运行 PowerShell 
set-executionpolicy remotesigned
# 查看脚本执行策略 - 本地机器(LocalMachine)
Get-ExecutionPolicy
# 查看脚本执行策略 - 列表
Get-ExecutionPolicy -List
```
> Powershell脚本5种执行权限, 要删除特定范围的执行策略，请将执行策略设置为Undefined。
1. Restricted
默认的设置， 不允许任何脚本运行；
2. AllSigned
只能运行经过数字证书签名的脚本；
3. RemoteSigned
运行本地的脚本不需要数字签名，但是运行从网络上下载的脚本就必须要有数字签名；
4. Unrestricted
允许所有的脚本运行；
5. Undefined
在Windows10下这是默认的值，表示未设置任何执行权限。这个值一般是用来删除执行策略的。

##  查看端口占用
``` sh
netstat -ano | findstr "LISTENING" | findstr "8888"
```
## 端口访问错误 EACCES
``` bash
# On Windows System, restarting the service "Host Network Service", resolved the issue.
# 在Windows系统中，『services』 > 重启『主机网络服务』
```
## 查看指定pid信息
``` bash
tasklist | findstr "PID"
```
## 关闭(nginx)任务
``` bash
taskkill /f /t /im nginx.exe
```


##  创建快捷方式
> 可能会被某些杀毒软件（如：360）等拦截
``` sh
mklink /j "./subfolder" "../folder"
# 如在 out 目录创建，上层 resource 目录的链接
mklink /j "resource" "../resource"
```
> It's not a keyboard shortcut, but holding ctrl+clicking on the icon in the taskbar repeatedly will cycle through that program's open windows.
##  查看硬盘媒体类型
优化驱动器 > 媒体类型
##  右键菜单
``` sh
HKEY_CLASSES_ROOT\Directory\Background\shell
```
##  去除Visual Studio右键菜单
> Run regedit.exe, go to `HKEY_CLASSES_ROOT\Directory\Background\shell\AnyCode`, take ownership of this key, change the permisions for your account and add a `DWORD (32Bit)` with the name `HideBasedOnVelocityId` and set the value to `006698a6 (hex)`:
##  Windows Scroll Reverse 
`计算机\HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Enum\HID\VID_05ac&PID_0277&MI_02&Col01\6&ef182fa&0&0000\Device Parameters`
不需要新建项，通常是已经存在的项
``` sh
DWORD
FlipFlopWheel = 1 
FlipFlopHWheel = 1
reboot
```
## 安装系统时出现"Windows无法安装到这个磁盘"
### 原因分析
win8/ windows10系统均添加快速启动功能，预装的win8/windows10电脑默认都是`UEFI引导`和`GPT硬盘`，传统的引导方式为`Legacy引导`和`MBR硬盘`

UEFI必须跟GPT对应，同理Legacy必须跟MBR对应。如果BIOS开启UEFI，而硬盘分区表格式为MBR则无法安装；BIOS关闭UEFI而硬盘分区表格式为GPT也是无法安装Windows

### 解决办法
#### 其一 改启动引导项(推荐)
可以在主板设置选项里面改，根据需要改引导方式为`UEFI引导`或`Legacy引导`
#### 其二 转硬盘格式
1. 在当前安装界面按住 <kbd>SHIFT</kbd>+<kbd>F10</kbd>调出命令提示符窗口；
2. 输入`diskpart`，按回车执行;
3. 进入**DISKPART命令模式**，输入`list disk`回车，列出当前磁盘信息；
4. 要转换磁盘0格式，则输入`select disk 0`回车，输入`clean`，删除磁盘分区；
5. 输入`convert gpt`则转为GPT；或者输入`convert MBR` 转换为 MBR格式；
6. 最后输入两次 `exit` 回车退出命令提示符，返回安装界面继续安装系统。

## 定时关机
``` sh
# 12小时候自动关机
shutdown -s -t 43200
```

## 服务相关
``` sh
# 设置服务依赖关系
sc config "Jenkins" depend="gogs"

# 移除所有依赖
sc config "Jenkins" depend=/

# 创建服务(*生效但似乎非正常启动)
sc create "Atlassian Crucible 4.8.0" binpath= E:\tools\fecru-4.8.0\bin\run.bat

# 删除服务
sc delete "Atlassian Crucible 4.8.0"
```

## windows10 激活
``` sh
slmgr /ipk W269N-WFGWX-YVC9B-4J6C9-T83GX

slmgr /skms kms.03k.org

slmgr /ato

```
## 睡眠等待
``` bat
timeout /t 30
```

### The timeout would get interrupted if the user hits any key; however, the command also accepts the optional switch /nobreak, which effectively ignores anything the user may press, except an explicit CTRL-C:
``` bat
timeout /t 30 /nobreak`
```

### Additionally, if you don't want the command to print its countdown on the screen, you can redirect its output to NUL:
``` bat
timeout /t 30 /nobreak > NUL`
```

## 设置临时环境变量
``` sh
set NODE_ENV=development

%NODE_ENV%
```
``` ps1
$env:NODE_ENV="development"

$env:NODE_ENV
```

## VBScript 代码示例
``` vb
Dim args
args = ""
For x = 0 to Wscript.Arguments.Count - 1
    args = args + " " + Wscript.Arguments(x)
Next
CreateObject("Wscript.Shell").Run "node . ./cfg/tsubasa/hotkeys_tortoise.yml" + args, 6
```

## 设置账户自动登录
```sh
control userpasswords2
```

## Powershell 连续执行命令
```sh
git status; git log
```

## Powershell 环境变量
```ps1
#查看所有环境变量  
ls env:

#搜索环境变量   
ls env:NODE*

#查看单个环境变量 
$env:NODE_ENV

#添加/更新环境变量 
$env:NODE_ENV=development

#删除环境变量        
del evn:NODE_ENV
```

## cmd 环境变量
```sh
#查看所有环境变量     
set

#查看单个环境变量     
set NODE_ENV

#添加/更新环境变量     
set NODE_ENV=development

#删除环境变量         
set NODE_ENV=
```

## 远程桌面连接 RDPWrap
- [RDPWrap](https://github.com/stascorp/rdpwrap/releases)
- [rdpwrap.ini](https://raw.githubusercontent.com/sebaxakerhtc/rdpwrap.ini/master/rdpwrap.ini)

## 关闭进程
```sh
# 指定名称关闭
taskkill /f /im node.exe
```
