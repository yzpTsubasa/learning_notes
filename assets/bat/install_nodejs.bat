@echo off
setlocal

@REM 获取管理员权限
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit

set NODE_VERSION=16.20.0
set NODE_URL=https://nodejs.org/dist/v%NODE_VERSION%/node-v%NODE_VERSION%-x64.msi
@REM set NODE_URL=https://registry.npmmirror.com/-/binary/node/v%NODE_VERSION%/node-v%NODE_VERSION%-x64.msi
set NODE_MSI=%TEMP%\node-v%NODE_VERSION%-x64.msi

echo 开始下载Node.js安装程序...
bitsadmin /transfer "下载Node.js安装程序" /download /priority normal %NODE_URL% %NODE_MSI%

echo 下载完成，开始安装Node.js...
msiexec /i "%NODE_MSI%" /quiet /qn /norestart

@REM 判断是否有 npx 命令
where npx >nul 2>&1
if not %ERRORLEVEL% == 0 (
    echo 安装失败，请手动安装Node.js。
) else (
    echo 安装成功!
)
pause
endlocal