## server

## 使用mkcert工具生成受信任的SSL证书，解决局域网本地https访问问题
- 下载 [mkcert](https://github.com/FiloSottile/mkcert/releases) 工具
- 以管理员身份执行 `mkcert -install` 命令安装到本地
- 执行 `mkcert -key-file private.pem -cert-file file.crt localhost 127.0.0.1 local.host` 命令生成证书