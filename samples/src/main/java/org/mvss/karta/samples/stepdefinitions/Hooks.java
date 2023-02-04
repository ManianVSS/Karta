package org.mvss.karta.samples.stepdefinitions;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.annotations.KartaAutoWired;
import org.mvss.karta.framework.annotations.ScenarioFailed;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.plugins.impl.kriya.*;
import org.mvss.karta.framework.properties.Configurator;
import org.mvss.karta.framework.properties.PropertyMapping;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.web.WebDriverOptions;
import org.mvss.karta.framework.web.WebDriverWrapper;
import org.mvss.karta.samples.pom.w3s.W3SchoolsApp;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class defines the life cycle hooks for Karta using Kriya plug-in. </br>
 *
 * @author Manian
 */
@Log4j2
public class Hooks {
    @KartaAutoWired
    private KartaRuntime kartaRuntime;

    @KartaAutoWired
    private Configurator configurator;

    @PropertyMapping(group = "WebAutomation", value = "webDriverOptions")
    private WebDriverOptions webDriverOptions = new WebDriverOptions();

    @PropertyMapping(group = "Kriya", value = "stepDefinitionPackageNames")
    private ArrayList<String> stepDefinitionPackageNames = null;

    @BeforeRun
    public void beforeRun(String runName) {
        log.info("@BeforeRun Kriya tag check " + runName);
    }

    @BeforeFeature("cy.*")
    public void beforeCycleFeatures(String runName, TestFeature feature) {
        log.info("@BeforeFeature Kriya tag check " + runName + " " + feature.getName());
    }

    @BeforeScenario(Constants.UI)
    public void beforeUIScenarios(String runName, String featureName, PreparedScenario scenario) {
        log.info("@BeforeScenario load web driver " + runName + Constants.SPACE + featureName + Constants.SPACE + scenario.getName());
        W3SchoolsApp w3SchoolsApp = new W3SchoolsApp(kartaRuntime, webDriverOptions);
        scenario.getContextBeanRegistry().put(W3SchoolsApp.W_3_SCHOOLS_APP, w3SchoolsApp);
    }

    @ScenarioFailed(Constants.UI)
    public void takeScreenshotForUIScenarioFailure(String runName, String featureName, PreparedScenario scenario, ScenarioResult scenarioResult) {
        log.info("@ScenarioFailed called for " + runName + Constants.SPACE + featureName + Constants.SPACE + scenario.getName());

        W3SchoolsApp w3SchoolsApp = (W3SchoolsApp) scenario.getContextBeanRegistry().get(W3SchoolsApp.W_3_SCHOOLS_APP);

        log.info("Scenario result is " + scenarioResult);

        if (w3SchoolsApp != null) {
            WebDriverWrapper driver = w3SchoolsApp.getDriver();
            if (driver != null) {
                try {
                    driver.takeSnapshot("FailureScreenshot-");
                } catch (IOException e) {
                    log.warn("Could not take Screen shot", e);
                }
            }
        }
    }

    @AfterScenario(Constants.UI)
    public void afterUIScenarios(String runName, String featureName, PreparedScenario scenario) {
        log.info("@AfterScenario close web driver " + runName + Constants.SPACE + featureName + Constants.SPACE + scenario.getName());
        try {
            W3SchoolsApp w3SchoolsApp = (W3SchoolsApp) scenario.getContextBeanRegistry().get(W3SchoolsApp.W_3_SCHOOLS_APP);
            if (w3SchoolsApp != null) {
                w3SchoolsApp.close();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @AfterFeature("cy.*")
    public void afterCycleFeatures(String runName, TestFeature feature) {
        log.info("@AfterFeature Kriya tag check " + runName + " " + feature.getName());
    }

    @AfterRun
    public void afterRun(String runName) {
        log.info("@AfterRun Kriya tag check " + runName);
    }

}
