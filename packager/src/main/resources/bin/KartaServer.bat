@echo off
rem This is the Karta server executable
rem java -Dloader.path=".,../lib/" -jar server.jar %*
java -jar server.jar %*
exit /B %errorlevel%