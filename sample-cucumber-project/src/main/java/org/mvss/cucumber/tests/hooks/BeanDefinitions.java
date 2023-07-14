package org.mvss.cucumber.tests.hooks;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired;
import org.mvss.karta.dependencyinjection.annotations.KartaBean;

@Log4j2
public class BeanDefinitions {
    @KartaAutoWired
    private static TestProperties testProperties;


    @KartaBean("EmployeeBean")
    public static Employee getEmployee() {
        if (testProperties == null) {
            log.error("Test properties not initialized.");
        }

        log.info("Creating new bean");
        return new Employee(Thread.currentThread().getName(), "admin", "NA", false, "admin", 0, "NA");
    }
}
