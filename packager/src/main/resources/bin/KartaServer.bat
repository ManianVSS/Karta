@echo off
rem This is the Karta executable
java -Dloader.path=".,../lib/" -jar server.jar %*
exit /B %errorlevel%