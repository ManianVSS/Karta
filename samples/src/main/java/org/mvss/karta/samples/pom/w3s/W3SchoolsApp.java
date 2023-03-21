package org.mvss.karta.samples.pom.w3s;

import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.web.PageException;
import org.mvss.karta.framework.web.WebAUT;
import org.mvss.karta.framework.web.WebDriverOptions;

import java.util.function.Consumer;

public class W3SchoolsApp extends WebAUT {
    public static final String W_3_SCHOOLS_APP = "W3SchoolsApp";

    @PropertyMapping(group = "WebAutomation", value = "w3SchoolsURL")
    protected String w3SchoolsURL = "https://www.w3schools.com/";

    public W3SchoolsApp(Consumer<Object> dependencyInjector, WebDriverOptions webDriverOptions) {
        super(dependencyInjector, "W3 Schools App", webDriverOptions);
    }

    @Override
    public void openStartPage() throws PageException {
        driver.navigateTo(w3SchoolsURL);
        new HomePage(this);
    }
}
