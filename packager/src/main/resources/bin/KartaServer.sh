#!/bin/bash
# This is the Karta server executable
java -Dloader.path=".,../lib/" -jar ../bin/server.jar $@