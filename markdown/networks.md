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

## REST 
Representational
表述性的
- method
- content-type
- statusCode

State
- /api/todo
- /api/user
状态(资源)

Transfer
转移
C <-> S

> 通过语义化的方式请求资源URL
> 并根据返回的语义来判断这次操作的返回类型和效果

## Windows中使用curl命令报错curl unmatched close brace/bracket
``` bat
curl -X POST localhost:7001/api/todo -H 'Content-Type: application/json' -d `{"text": "Post by curl"}`
```
> window的command.exe不支持单引号，所以要处理一下命令：先转义双引号，然后把单引号改为双引号
``` bat
curl -X POST localhost:7001/api/todo -H "Content-Type: application/json" -d "{\"text\": \"Post by curl\"}"
```