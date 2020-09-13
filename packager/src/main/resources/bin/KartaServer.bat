@echo off
rem This is the Karta executable
java -Dloader.path=".,../lib/,../plugins/" -jar server.jar %*
exit /B %errorlevel%