@echo off
rem This is the Karta executable
java -cp "*;../lib/*;../plugins/samples.jar" org.mvss.karta.cli.KartaMain -t samples -runName TestRun
exit /B %errorlevel%