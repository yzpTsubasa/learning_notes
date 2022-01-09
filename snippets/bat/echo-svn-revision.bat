@echo off
FOR /F "tokens=*" %%i IN (' "svn info | findstr /R Revision:" ') DO SET REVISION=%%i
echo "%REVISION:~10%"
pause