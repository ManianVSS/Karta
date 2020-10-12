@echo off
rem This is the Karta executable
java -cp "*;../bin/*;../lib/*" org.mvss.karta.cli.KartaMain %*
exit /B %errorlevel%