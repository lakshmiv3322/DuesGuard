@echo off
setlocal
echo ========================================================
echo   Running DuesGuard JUnit 5 Test Suite
echo ========================================================
cd /d "%~dp0"
call "..\apache-maven-3.9.6\bin\mvn.cmd" test
pause
