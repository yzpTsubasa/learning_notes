## electron

## 配置镜像
在项目根目录创建 `.npmrc` 文件，添加以下内容：
```ini
registry=https://registry.npmmirror.com/
electron_mirror=https://cdn.npmmirror.com/binaries/electron/
electron_builder_binaries_mirror=https://npmmirror.com/mirrors/electron-builder-binaries/
```

## 打包绿色免安装包
``` bat
npm install --save-dev electron-packager
npx electron-packager . egret_ui_editor --platform win32 --arch x64
```

## ipc(inter process communication)
![Alt text](../assets/electron/ipc.png)
```js
// 使用 preload.js
webPreferences: {
    preload: join(__dirname, "preload.js"),
}

const { ipcRenderer, contextBridge } = require("electron");

function sendToMain(channel, ...args) {
    ipcRenderer.send(channel, ...args);
}
// 导出 sendToMain 给 web 使用
contextBridge.exposeInMainWorld("sendToMain", sendToMain);
```

## 在网页环境集成node
```js
webPreferences: {
    nodeIntegration: true,
    contextIsolation: false,
}
```