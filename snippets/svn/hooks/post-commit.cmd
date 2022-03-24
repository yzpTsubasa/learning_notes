setlocal enabledelayedexpansion
rem Subversion sends through the path to the repository and transaction id
rem the path to this repository
set REPOS_PATH=%1
rem the number of the revison just committed
set REV=%2
rem the name of the transaction that has been just committed
set TXT_NAME=%3
rem check for an empty log message
echo %REPOS_PATH% 1>&2
set SVN_LOOK="svnlook.exe"
set PYTHON="C:\Python27\python"
set NODE="node"
@REM 获取提交的第一个文件
set GET_CHANGELIST="%SVN_LOOK% changed %REPOS_PATH% --revision %REV%"
FOR /F "tokens=*" %%i IN (' %GET_CHANGELIST% ') DO SET CHANGE_FILE=%%i
FOR /F "tokens=*" %%i IN (' %GET_CHANGELIST% ') DO echo %%i 1>&2
FOR /F "tokens=*" %%a in (' "%SVN_LOOK% dirs-changed %REPOS_PATH% --revision %REV%" ') do SET CHANGED_DIR=%%a
@REM python仅对 trunk/DLDL 目录生效
echo %CHANGE_FILE% | findstr trunk/DLDL > nul
if !ERRORLEVEL! equ 0 (
    @REM 使用nodejs
    %NODE% %REPOS_PATH%\hooks\nodejs_tool\post-commit\index.js %REPOS_PATH% %REV% %TXT_NAME% %SVN_LOOK%
    exit 1
)
exit 1