setlocal enabledelayedexpansion
rem Subversion sends through the path to the repository and transaction id
set REPOS=%1
set TXN=%2
rem check for an empty log message
echo %REPOS% 1>&2
set SVN_LOOK="svnlook.exe"
set PYTHON="C:\Python27\python"
set NODE="C:\Program Files (x86)\nodejs\node.exe"
@REM 获取提交的第一个文件
set GET_CHANGELIST="%SVN_LOOK% changed %REPOS% -t %TXN%"
FOR /F "tokens=*" %%i IN (' %GET_CHANGELIST% ') DO SET CHANGE_FILE=%%i
@REM python仅对 trunk/DLDL 目录生效
echo %CHANGE_FILE% | findstr trunk/DLDL > nul
if !ERRORLEVEL! equ 0 (
    @REM 使用nodejs
    %NODE% %REPOS%\hooks\pre-commit\main.js %REPOS% %TXN% %SVN_LOOK%
    exit !ERRORLEVEL!
    @REM 使用golang
    %REPOS%\hooks\golang\pre-commit\pre-commit %REPOS% %TXN% %SVN_LOOK%
    exit !ERRORLEVEL!
    @REM 使用python
    %PYTHON% %REPOS%\hooks\pre-commit.py %REPOS% %TXN% %SVN_LOOK%
    exit !ERRORLEVEL!
)
%SVN_LOOK% log %REPOS% -t %TXN% | findstr . > nul
if !ERRORLEVEL! gtr 0 (goto err) else exit 0
:err
echo. 1>&2
echo Your commit has been blocked because you didn't give any log message 1>&2
echo 提交日志不能为空 1>&2
exit 1