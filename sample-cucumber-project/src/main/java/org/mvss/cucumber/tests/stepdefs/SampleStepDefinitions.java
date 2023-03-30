package org.mvss.cucumber.tests.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.log4j.Log4j2;
import org.mvss.cucumber.tests.hooks.Employee;
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.utils.WaitUtil;

@Log4j2
public class SampleStepDefinitions {
    @KartaAutoWired("EmployeeBean")
    private Employee employee;

    @PropertyMapping(group = "MyTest", name = "myEmployeeObject")
    private Employee employeeFromProperties;

    @Given("print {string}")
    public void print(String string) {
        log.info("Employee from properties = " + employeeFromProperties);
        log.info(string);
    }

    @Then("sleep {long}")
    public void sleep(long time) {
        try {
            log.info("Employee bean = " + employee);
            log.info("Going to sleep for " + time + " milliseconds...");
            WaitUtil.sleep(time, true, true);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

}
