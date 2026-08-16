@echo off
setlocal
cd /d %~dp0\..

call mvnw.cmd -DskipTests package
if errorlevel 1 exit /b 1

cd frontend
call npm install
call npm run build
cd ..

call mvnw.cmd -DskipTests package
if errorlevel 1 exit /b 1

cd frontend
call npm run electron:pack
endlocal
