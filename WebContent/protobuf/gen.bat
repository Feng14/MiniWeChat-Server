@echo off
rem 查找文件
for /f "delims=" %%i in ('dir /b ".\*.proto"') do echo %%i
for /f "delims=" %%i in ('dir /b/a ".\*.proto"') do protoc --java_out=./ %%i
pause