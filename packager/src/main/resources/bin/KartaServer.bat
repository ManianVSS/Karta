@echo off
rem This is the Karta server executable
java -Dloader.path=".,../lib/" -jar server.jar %*
rem java -jar server.jar %*
exit /B %errorlevel%