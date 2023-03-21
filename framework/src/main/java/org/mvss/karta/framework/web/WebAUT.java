package org.mvss.karta.framework.web;

import lombok.Getter;
import lombok.ToString;

import java.util.function.Consumer;

@Getter
@ToString
public abstract class WebAUT implements AutoCloseable {
    protected String name;
    protected WebDriverOptions webDriverOptions;
    protected WebDriverWrapper driver;
    protected AbstractPage currentPage;

    protected boolean initialized = false;

    protected Consumer<Object> dependencyInjector;

    public WebAUT(Consumer<Object> dependencyInjector, String name, WebDriverOptions webDriverOptions) {
        this.dependencyInjector = dependencyInjector;
        this.name = name;
        this.webDriverOptions = webDriverOptions;
        dependencyInjector.accept(this);
    }

    public WebAUT(Consumer<Object> dependencyInjector, String name, WebDriverOptions webDriverOptions, WebDriverWrapper driver) {
        this(dependencyInjector, name, webDriverOptions);
        this.driver = driver;
    }

    public abstract void openStartPage() throws PageException;

    public AbstractPage init() throws PageException {
        if (!initialized) {
            close();
            driver = SimpleWebDriverFactory.createWebDriverWrapper(webDriverOptions);
            initialized = true;
            openStartPage();
            return currentPage;
        } else {
            openStartPage();
        }

        return currentPage;
    }

    @Override
    public void close() throws PageException {
        if (currentPage != null) {
            currentPage.close();
            currentPage = null;
        }

        if (driver != null) {
            driver.close();
        }

        driver = null;
        currentPage = null;
        initialized = false;
    }

    public AbstractPage reset() throws PageException {
        close();
        return init();
    }
}
