@echo off
rem This is the Karta executable
java -cp "*;../bin/*;../lib/*" org.mvss.karta.cli.KartaMain -t samples -runName TestRun
exit /B %errorlevel%