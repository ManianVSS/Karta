#!/bin/bash
# This is the Karta server executable
java -Dloader.path=".,${KARTA_HOME}/bin/,${KARTA_HOME}/lib/" -jar ${KARTA_HOME}/bin/server.jar $@