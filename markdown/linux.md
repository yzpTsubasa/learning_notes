## linux
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