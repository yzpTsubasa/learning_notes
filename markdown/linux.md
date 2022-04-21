## linux

## Cent OS 7 [CentOS-7-x86_64-DVD-2009.iso]

### 安装 nodejs
``` bash
# 前往 http://nodejs.cn/download/，选择对应架构的 Linux 二进制文件
# 如 https://npmmirror.com/mirrors/node/v16.14.2/node-v16.14.2-linux-x64.tar.xz
# 方便起见，前往目录 ~
cd ~
# 先确保 wget 已安装
yum -y install wget
# 下载二进制文件压缩包
wget https://npmmirror.com/mirrors/node/v16.14.2/node-v16.14.2-linux-x64.tar.xz
# 解压压缩包
# --strip-components 1 可以去除1层目录结构
# -xzvf 压缩包没有用gzip格式压缩，所以不用加z参数
# -x或--extract或--get 从压缩包中还原文件
# -z或--gzip或--ungzip 通过gzip指令处理压缩包
# -v或--verbose 显示指令执行过程
# -f<压缩包>或--file=<压缩包> 指定压缩包

# -C<目的目录>或--directory=<目的目录> 切换到指定的目录
tar --strip-components 1 -xvf node-v* -C /usr/local
# 测试安装是否成功
node --version
```

### 网络设置 - 自启动
``` bash
[root@localhost ~]# cat /etc/sysconfig/network-scripts/ifcfg-enp0s3
TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=dhcp
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=enp0s3
UUID=4048a2a1-f428-434d-9bd5-567d5f5c5f6d
DEVICE=enp0s3
#ONBOOT=no

ONBOOT=yes
[root@localhost ~]# systemctl restart network
```

### 网络设置 - 静态ip,自启动,掩码,网关,DNS
``` bash
[root@localhost ~]# cat /etc/sysconfig/network-scripts/ifcfg-enp0s3
TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
#BOOTPROTO=dhcp
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
#IPV6_ADDR_GEN_MODE=stable-privacy
NAME=enp0s3
UUID=4048a2a1-f428-434d-9bd5-567d5f5c5f6d
DEVICE=enp0s3
#ONBOOT=no

BOOTPROTO=static
ONBOOT=yes
IPADDR=192.168.10.117
NETMASK=255.255.255.0
GATEWAY=192.168.10.1
DNS1=114.114.114.114
[root@localhost ~]# systemctl restart network
```

##  Nginx
``` bash
# 使用新配置文件重启nginx
kill -HUP `cat logs/nginx.pid`
nginx -s reload

# 重读日志文件
kill -USR1 `cat logs/nginx.pid`
nginx -s reopen
```

##  Ubuntu安装相关
> 遇到问题一：`./configure: error: C compiler cc is not found`

> 解决方案： 
```
sudo apt-get install -y gcc
```

> 遇到问题二：`configure: error: You need a C++ compiler for C++ support.`

> 解决方案：
```
sudo apt-get install -y build-essential
```
> 再
```
./configure
```
> The yes flag has been set. This will automatically answer yes to all que
stions, which may have security implications.
##  使用 cmake / make 安装 相关库
``` bash
# 创建build目录
mkdir build
# 进入build目录
cd build
# 执行上层CMake文件
cmake ..
# 执行 make
make
# 安装
make install
```
##  安装工具(python)
``` bash
# 生成 Makefile 文件
./configure
# 生成构建
make
# 检测安装环境，分析会导致安装失败的原因
make test
# 开始安装
sudo make install
```
##  ubuntu server 修改字体
``` bash
sudo dpkg-reconfigure console-setup
```