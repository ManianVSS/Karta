package org.mvss.karta.samples.config;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired;
import org.mvss.karta.dependencyinjection.annotations.KartaBean;
import org.mvss.karta.samples.stepdefinitions.Employee;

@Log4j2
public class BeanDefinitionClass {
    @KartaAutoWired
    private static TestProperties testProperties;

    @KartaBean("EmployeeBean")
    public static Employee getEmployee() {
        if (testProperties == null) {
            log.error("TestProperties not initialized.");
        }

        log.info("Creating new bean");
        return new Employee("AdminBeanEmployee", "admin", "NA", false, "admin", 0, "NA");
    }
}
