@echo off
@REM 获取管理员权限
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit
cd /d "%~dp0"

SET sublimeTextPath=%~dp0sublime_text.exe
 
@REM 文件
@reg add "HKEY_CLASSES_ROOT\*\shell\Sublime Text"         /t REG_SZ /v "" /d "通过 Sublime Text 打开"   /f
@reg add "HKEY_CLASSES_ROOT\*\shell\Sublime Text"         /t REG_EXPAND_SZ /v "Icon" /d "%sublimeTextPath%" /f
@reg add "HKEY_CLASSES_ROOT\*\shell\Sublime Text\command" /t REG_SZ /v "" /d "%sublimeTextPath% \"%%1\"" /f

@REM 文件夹
@reg add "HKEY_CLASSES_ROOT\Directory\shell\Sublime Text"         /t REG_SZ /v "" /d "通过 Sublime Text 打开"   /f
@reg add "HKEY_CLASSES_ROOT\Directory\shell\Sublime Text"         /t REG_EXPAND_SZ /v "Icon" /d "%sublimeTextPath%" /f
@reg add "HKEY_CLASSES_ROOT\Directory\shell\Sublime Text\command" /t REG_SZ /v "" /d "%sublimeTextPath% \"%%V\"" /f

@REM 文件夹空白区域
@reg add "HKEY_CLASSES_ROOT\directory\background\shell\Sublime Text"         /t REG_SZ /v "" /d "通过 Sublime Text 打开"   /f
@reg add "HKEY_CLASSES_ROOT\directory\background\shell\Sublime Text"         /t REG_EXPAND_SZ /v "Icon" /d "%sublimeTextPath%" /f
@reg add "HKEY_CLASSES_ROOT\directory\background\shell\Sublime Text\command" /t REG_SZ /v "" /d "%sublimeTextPath% \"%%V\"" /f
 
pause