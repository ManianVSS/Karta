FROM ubuntu

LABEL maintainer="Manian VSS<manianvss@hotmail.com>"

# Update OS
RUN apt-get update && apt-get -y upgrade

# Install JDK
RUN apt-get -y install openjdk-21-jdk wget curl maven jq libjq1 unzip xvfb

#Install Python3 and PIP
RUN apt-get -y install python3 python-is-python3 python3-pip

# Google Chrome and Driver
COPY download_chrome.sh .
RUN chmod +x download_chrome.sh
RUN bash download_chrome.sh

RUN apt clean