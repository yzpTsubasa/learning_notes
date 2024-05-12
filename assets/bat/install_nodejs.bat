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

set NODE_VERSION=16.20.0
@REM set NODE_URL=https://nodejs.org/dist/v%NODE_VERSION%/node-v%NODE_VERSION%-x64.msi
set NODE_URL=https://registry.npmmirror.com/-/binary/node/v%NODE_VERSION%/node-v%NODE_VERSION%-x64.msi
set NODE_MSI=%TEMP%\node-v%NODE_VERSION%-x64.msi

echo (1/3) 下载安装程序
@REM bitsadmin /transfer "下载Node.js安装程序" /download /priority normal %NODE_URL% %NODE_MSI%
powershell -Command "Invoke-WebRequest -Uri '%NODE_URL%' -OutFile '%NODE_MSI%'"

echo (2/3) 开始安装
msiexec /i "%NODE_MSI%" /qn /norestart

@REM 判断是否有 npx 命令
where npx >nul 2>&1
if not %ERRORLEVEL% == 0 (
    echo 安装失败，请手动安装Node.js。
) else (
    echo ^(3/3^) 安装成功!
)
pause
endlocal