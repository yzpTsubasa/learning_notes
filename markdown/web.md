## web

## 使用 node_http_server 反代理(仅支持 http 协议)
> 以`https://game.juefeng.com/view/jfGame_wap/H5_game.html?agentId=298&gameId=81430`为例
- 找一个干净的位置，新建 `proxy` 文件夹,并且在目录中使用参数 `--http_port 18686` 启动 `node_http_server`
- 切换到 `http` 协议，即 `http://game.juefeng.com/view/jfGame_wap/H5_game.html?agentId=298&gameId=81430`
- 打开控制台，查看元素,右键根节点 `html`, 选择`编辑为 HTML`, 内容保存到 `<proxy>/jfGame_wap/H5_game.html`。并且根据需要修改内容
- 浏览器插件`SwitchyOmega`添加自动切换模式项，条件类型 `网址通配符`，条件设置 `http://game.juefeng.com/view/jfGame_wap/H5_game.html?agentId=298&gameId=81430`，情景模式 代理服务器 `localhost:18686`

## 元素置顶全屏
```css
element.style {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    min-width: 0 !important;
    min-height: 0 !important;
    width: 100% !important;
    height: 100% !important;
    max-width: 100% !important;
    max-height: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    visibility: visible !important;
    border-width: 0 !important;
    background: black !important;
    opacity: 1 !important;
}
```