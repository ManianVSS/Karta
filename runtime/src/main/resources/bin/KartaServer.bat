@echo off
rem This is the Karta server executable

SET SCRIPT_DIR=%~dp0\..

if "%KARTA_HOME%"=="" (
	for %%i in ("%SCRIPT_DIR%") do SET "KARTA_HOME=%%~fi"
)

echo Karta home directory is %KARTA_HOME%
java -Dloader.path=".,%KARTA_HOME%/bin/,%KARTA_HOME%/lib/" -jar %KARTA_HOME%\bin\server.jar %*
exit /B %errorlevel%