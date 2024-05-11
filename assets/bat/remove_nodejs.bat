@echo off
setlocal

@REM Node.js 默认安装路径
set "NODE_PATH=%PROGRAMFILES%\nodejs\"

@REM 检查 Node.js 是否安装在默认路径
if not exist "%NODE_PATH%" (
    echo Node.js is not installed in the default directory.
    echo Please modify the script with the correct Node.js installation path.
    exit /b
)

@REM 删除 Node.js 安装目录
rmdir /s /q "%NODE_PATH%"

@REM 从环境变量中移除 Node.js
set "NODE_VARS=NODE_PATH NODE_EXE NODE_PREFIX PATH"

for %%i in (%NODE_VARS%) do (
    if defined %%i (
        setx %%i ""
    )
)

echo Node.js has been uninstalled silently.

endlocal