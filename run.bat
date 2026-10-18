@echo off
setlocal
echo ========================================================
echo   Launching DuesGuard 1.0.0
echo   Payment Recovery Command Centre
echo ========================================================
cd /d "%~dp0"
call "..\apache-maven-3.9.6\bin\mvn.cmd" javafx:run
pause
