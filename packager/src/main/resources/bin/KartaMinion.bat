@echo off
rem This is the Karta executable
java -cp "*;../lib/*" org.mvss.karta.cli.KartaMain -startMinion
exit /B %errorlevel%