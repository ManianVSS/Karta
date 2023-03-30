package org.mvss.cucumber.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import lombok.extern.log4j.Log4j2;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        //"org.mvss.cucumber.extensions.CustomReporterPlugin:sample.json"
        plugin = {"json:cucumber-json-report.json",
                "html:cucumber-html-report.html",
                "pretty:cucumber-pretty-report.txt",
                "junit:cucumber-junit-report.xml"},
        glue = {"org.mvss.cucumber.tests.stepdefs"},
        features = {"../classes/features/"}
)
@Log4j2
public class TestRunner {
    @BeforeClass
    public static void setup() {
        log.info("Running setup");
    }

    @AfterClass
    public static void tearDown() {
        log.info("Running tearDown");
    }
}
