@echo off
rem This is the Karta executable
java -cp "*;../lib/*;../plugins/*" org.mvss.karta.cli.KartaMain %*
exit /B %errorlevel%