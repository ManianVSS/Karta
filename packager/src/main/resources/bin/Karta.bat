@echo off
rem This is the Karta executable
java -cp "../lib/*" org.mvss.karta.framework.runtime.KartaMain %*
exit /B %errorlevel%