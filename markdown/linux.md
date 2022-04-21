## linux

## Cent OS 7 [CentOS-7-x86_64-DVD-2009.iso]

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