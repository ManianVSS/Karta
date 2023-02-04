#!/bin/bash
#This is the Karta executable
SCRIPT_PATH=`readlink -f "${BASH_SOURCE:-$0}"`
SCRIPT_PATH="$(dirname "$SCRIPT_PATH")"
KARTA_HOME=`readlink -f "${SCRIPT_PATH}/../"`
echo "KARTA_HOME is $KARTA_HOME"
java -Dloader.path=".,${KARTA_HOME}/bin/,${KARTA_HOME}/lib/" -Dspring.main.web-application-type=NONE -jar ${KARTA_HOME}/bin/server.jar $@