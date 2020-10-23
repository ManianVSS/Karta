#!/bin/bash
# This is the Karta server executable
java -Dloader.path=".,../bin,../lib/" -jar ../bin/server.jar $@