## docker

## Docker Desktop 安装到D盘
1. 下载 [Docker Desktop Installer.exe](https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe)
2. `mklink /j “C:\Program Files\Docker” “D:\Program Files\Docker”` 安装路径建立软连接
3. `mklink /j “C:\Users\Administrator\AppData\Local\Docker” “D:\Program Data\Docker”` 镜像存储路径建立软连接
4. `start /w "" "Docker Desktop Installer.exe" install --installation-dir=D:\Program Files\Docker`安装