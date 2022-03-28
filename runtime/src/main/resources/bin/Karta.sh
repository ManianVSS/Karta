#!/bin/bash
#This is the Karta executable
java -Dloader.path=".,${KARTA_HOME}/bin/,${KARTA_HOME}/lib/" -Dspring.main.web-application-type=NONE -jar ${KARTA_HOME}/bin/server.jar $@