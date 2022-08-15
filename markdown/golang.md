## golang

## vscode 插件
``` bash
go env -w GOPROXY=https://goproxy.cn
# 清空缓存
go clean --modcache

# 按ctrl+shift+p 调出命令面板，输入go install tools 选Go: Install/Update Tools
```

## 平台的自动判断的智能表达式 32 << (^uint(0) >> 63)
``` go
// 32位系统下 0xFFFFFFFF
^uint(0)
// 64位系统下 0xFFFFFFFF_FFFFFFFF
^uint(0)
```