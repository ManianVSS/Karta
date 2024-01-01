#!/bin/bash

#Install dependencies
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
apt-get install -y ./google-chrome-stable_current_amd64.deb
dpkg --force-depends --remove google-chrome-stable
# mv /usr/local/bin/chrome /usr/local/bin/chrome-latest
# apt-get install -y unzip xvfb libxi6 libgconf-2-4 jq libjq1 libonig5 libxkbcommon0 libxss1 libglib2.0-0 libnss3 \
#   libfontconfig1 libatk-bridge2.0-0 libatspi2.0-0 libgtk-3-0 libpango-1.0-0 libgdk-pixbuf2.0-0 libxcomposite1 \
#   libxcursor1 libxdamage1 libxtst6 libappindicator3-1 libasound2 libatk1.0-0 libc6 libcairo2 libcups2 libxfixes3 \
#   libdbus-1-3 libexpat1 libgcc1 libnspr4 libgbm1 libpangocairo-1.0-0 libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxext6 \
#   libxrandr2 libxrender1 gconf-service ca-certificates fonts-liberation libappindicator1 lsb-release xdg-utils

# Google Chrome


LATEST_CHROME_RELEASE=$(curl -s https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json | jq '.channels.Stable')
LATEST_CHROME_URL=$(echo "$LATEST_CHROME_RELEASE" | jq -r '.downloads.chrome[] | select(.platform == "linux64") | .url')
wget -N "$LATEST_CHROME_URL" -P ~/
unzip ~/chrome-linux64.zip -d ~/
mv ~/chrome-linux64 ~/chrome
ln -s ~/chrome/chrome /usr/local/bin/chrome
chmod +x ~/chrome
rm ~/chrome-linux64.zip

#ChromeDriver
LATEST_CHROMEDRIVER_RELEASE=$(curl -s https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json | jq '.channels.Stable')
LATEST_CHROMEDRIVER_URL=$(echo "$LATEST_CHROMEDRIVER_RELEASE" | jq -r '.downloads.chromedriver[] | select(.platform == "linux64") | .url')
wget -N "$LATEST_CHROMEDRIVER_URL" -P ~/
unzip ~/chromedriver-linux64.zip -d ~/
mv ~/chromedriver-linux64 ~/chromedriver
ln -s ~/chromedriver/chromedriver /usr/local/bin/chromedriver
chmod +x ~/chromedriver
rm ~/chromedriver-linux64.zip
