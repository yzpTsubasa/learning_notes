@echo off
@REM 获取管理员权限
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit
cd /d "%~dp0"

@REM 文件
@reg delete "HKEY_CLASSES_ROOT\*\shell\Sublime Text"       /f

@REM 文件夹
@reg delete "HKEY_CLASSES_ROOT\Directory\shell\Sublime Text"       /f

@REM 文件夹空白区域
@reg delete "HKEY_CLASSES_ROOT\directory\background\shell\Sublime Text"       /f
 
pause