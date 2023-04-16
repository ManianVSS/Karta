#!/bin/sh
#Run cucumber Tests
#"org.mvss.cucumber.extensions.CustomReporterPlugin:sample.json"
java -DPROPERTIES_FOLDER=properties -Dlog4j.configurationFile=log4j2.xml -classpath "*:lib/*" io.cucumber.core.cli.Main --glue "org.mvss.cucumber.tests.stepdefs" --plugin "org.mvss.cucumber.extensions.ShaniDashboardPlugin:properties" --plugin "json:cucumber-json-report.json"  --plugin "html:cucumber-html-report.html" --plugin "pretty:cucumber-pretty-report.txt" --plugin "junit:cucumber-junit-report.xml" --threads 4 "features" --tags "$@" || true
exit $?
