## vscode
## 发布插件
1. 安装yarn, npm全局安装vsce
2. 通过azure开发者帐号获取token
   https://dev.azure.com/865500815/_usersSettings/tokens
   > 获取下面所使用的 [token]
3. 未登录的情况下先创建发布者
   > [publisher] = TsubasaYeung
    ``` bash
    vsce create-publisher [publisher]
    ```
    然后登录
    ``` bash
    vsce login [publisher]
    ```
4. 执行发布命令
   ``` bash
   vsce publish -p [token]
   ```
   或者直接打包，在[发布页](https://marketplace.visualstudio.com/manage/publishers/TsubasaYeung)上传
   ```
   vsce package
   ```

## 调试工作目录指定的文件
``` json
"configurations": [

    {
        "name": "Python: 当前文件",
        "type": "python",
        "request": "launch",
        "program": "${workspaceFolder}/main.py",
        "console": "integratedTerminal"
    }
]
```
##  提高python intelligence 效率
`settings.json`
``` json
"python.jediEnabled": false,
"python.autoComplete.extraPaths": [
    "C:\\Users\\Administrator\\AppData\\Local\\Programs\\Python\\Python37\\Lib\\site-packages",
    "C:\\Users\\Administrator\\AppData\\Local\\Programs\\Python\\Python37\\Scripts",
]
```