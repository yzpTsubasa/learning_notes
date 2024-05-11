@echo off
setlocal

@REM 获取管理员权限
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit

@REM Node.js 默认安装路径
set "NODE_PATH=%PROGRAMFILES%\nodejs\"

@REM 检查 Node.js 是否安装在默认路径
if not exist "%NODE_PATH%" (
    echo Node.js 没有在默认路径中找到，请手动删除。
    exit /b
)

@REM 删除 Node.js 安装目录
rmdir /s /q "%NODE_PATH%"

echo Node.js 卸载完成
pause
endlocal