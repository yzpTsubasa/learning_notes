@echo off
setlocal

set NODE_VERSION=16.20.0
set NODE_URL=https://nodejs.org/dist/v%NODE_VERSION%/node-v%NODE_VERSION%-x64.msi
set NODE_MSI=%TEMP%\node-v%NODE_VERSION%-x64.msi

echo 开始下载Node.js安装程序...
bitsadmin /transfer "下载Node.js安装程序" /download /priority normal %NODE_URL% %NODE_MSI%

echo 下载完成，开始安装Node.js...
msiexec /i "%NODE_MSI%" /quiet /qn /norestart

if exist "%PROGRAMFILES%\nodejs\node.exe" (
    echo 安装成功，Node.js已安装到 %PROGRAMFILES%\nodejs\node.exe
) else (
    echo 安装失败，找不到 %PROGRAMFILES%\nodejs\node.exe
)
pause
endlocal