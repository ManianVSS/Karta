@echo off
rem This is the Karta server executable
java -Dloader.path=".,../bin,../lib/" -jar ..\bin\server.jar %*
exit /B %errorlevel%