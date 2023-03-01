## networks

## curl
``` bash
# 使用 -k 或 --insecure 参数（不验证 TSL 证书）
curl -X GET https://seirei-jp-web1.pro.g123-cpp.com/hotgame/test.php?src=1 -k
```

## 访问 raw.githubusercontent.com
``` bash
# 详情见 https://blog.csdn.net/bryong/article/details/108374261
# 添加 DNS
8.8.8.8
# 国内
114.114.114.114
# 国内 阿里
223.5.5.5
```

## 可能会引起安卓机器无法正常连接的域名(RFC 952规范)
``` log
美国国防部互联网主机表规范中的相关条文
A "name" (Net, Host, Gateway, or Domain name) is a text string
up to 24 characters drawn from the alphabet (A-Z), digits (0-9),minus sign (-), and period (.)
规范指出域名只能是字母、数字和短线（-）还有点（.）。
2022-08-19 16:55:12.486 26260-28710/? E/EgretNative: J: WS: error (exception: java.lang.IllegalArgumentException: Invalid input to toASCII: ks-dl-bt2_01.hotgamehl.com)
```