@echo off
setlocal enabledelayedexpansion

:: BatchGotAdmin
:-------------------------------------
REM  --> Check for permissions
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"

REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Requesting administrative privileges...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c %~s0 %params%", "", "runas", 1 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    pushd "%CD%"
    CD /D "%~dp0"
:--------------------------------------

for /f "delims=" %%i in ('where node') do (
    set NODE_EXE_PATH=%%i
)

if "!NODE_EXE_PATH!"=="" (
    echo 未找到Node.js
    pause
    exit /b 1
)
set NODE_DIR_PATH=!NODE_EXE_PATH!
rem 移除路径中的文件名和尾部的 \，以得到纯净的目录路径
set NODE_DIR_PATH=!NODE_DIR_PATH:node.exe=!
set NODE_DIR_PATH=!NODE_DIR_PATH:~0,-1!
echo 开始卸载...

@REM 删除 Node.js 安装目录
rmdir /s /q "!NODE_DIR_PATH!"

echo 卸载完成!
pause
endlocal