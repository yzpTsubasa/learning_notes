## docker

## Docker Desktop 安装到其他盘符
1. 下载 [Docker Desktop Installer.exe](https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe)
2. `mklink /j “C:\Program Files\Docker” “D:\Program Files\Docker”` 安装路径建立软连接
3. `mklink /j “C:\Users\Administrator\AppData\Local\Docker” “D:\Program Data\Docker”` 镜像存储路径建立软连接
4. `start /w "" "Docker Desktop Installer.exe" install --installation-dir=D:\Program Files\Docker`安装
> 第3条，镜像存储路径建立软连接。好像没有效果。
> 
> 可以修改 `Resources/Disk image location` 位置到其他位置。**注意新的位置的权限问题**


## 国内镜像加速
[渡渡鸟镜像同步站](https://docker.aityp.com/)
```jsonc
{
    // ...
    "registry-mirrors": [
        "https://swr.cn-north-4.myhuaweicloud.com"
    ],
    // ...
}
```