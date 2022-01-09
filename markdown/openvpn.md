## openvpn

## 记住帐号密码自动连接
在配置文件 `*.ovpn` 添加
``` bash
auth-user-pass <包含帐号密码的文件>
# 如：
auth-user-pass credential.txt
```
`credential.txt` 中第一行为帐号，第二行为密码