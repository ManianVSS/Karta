package org.mvss.karta.framework.web;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

@Log4j2
public class SimpleWebDriverFactory {
    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String WEBDRIVER_GECKO_DRIVER = "webdriver.gecko.driver";
    private static final String WEBDRIVER_EDGE_DRIVER = "webdriver.edge.driver";

    private static final String MS_EDGE_CHROMIUM = "ms:edgeChromium";
    private static final String MS_EDGE_OPTIONS = "ms:edgeOptions";
    private static final String ARGS = "args";
    private static final String DISABLE_GPU = "--disable-gpu";
    private static final String HEADLESS = "--headless";
    private static final String NO_SANDBOX = "--no-sandbox";
    private static final String WINDOWS = "Windows";
    private static final String OS_NAME = "os.name";
    private static final String EXE = ".exe";

    public static WebDriverOptions DEFAULT_WEB_DRIVER_OPTIONS = new WebDriverOptions();

    public static void setupWebDriver(WebDriverOptions webDriverOptions) {
        Browser browser = webDriverOptions.getBrowser();
        String driverFileName = webDriverOptions.getWebDriverLocation();
        String webDriverProperty;

        switch (browser) {
            case FIREFOX:
                webDriverProperty = WEBDRIVER_GECKO_DRIVER;
                break;

            case EDGE:
                webDriverProperty = WEBDRIVER_EDGE_DRIVER;
                break;

            case SAFARI:
                return;

            default:
            case CHROME:
                webDriverProperty = WEBDRIVER_CHROME_DRIVER;
                break;
        }

        synchronized (SimpleWebDriverFactory.class) {
            if (System.getProperty(webDriverProperty) == null) {
                if (System.getProperty(OS_NAME).contains(WINDOWS)) {
                    System.setProperty(webDriverProperty, driverFileName.endsWith(EXE) ? driverFileName : driverFileName + EXE);
                } else {
                    System.setProperty(webDriverProperty, driverFileName);
                    File driverFile = new File(driverFileName);
                    if (!driverFile.setExecutable(true, false)) {
                        log.warn("Could not change web driver file \"" + driverFileName + "\" permissions as executable.");
                    }
                }
            }
        }
    }

    public static WebDriverWrapper createWebDriverWrapper(WebDriverOptions webDriverOptions) {
        if (webDriverOptions == null) {
            webDriverOptions = DEFAULT_WEB_DRIVER_OPTIONS;
        }
        String webDriverWrapperClass = webDriverOptions.getWebDriverWrapperClass();
        if (webDriverWrapperClass.equals(WebDriverWrapper.class.getName())) {
            return new WebDriverWrapper(createWebDriver(webDriverOptions), webDriverOptions.getWaitTimeout(), webDriverOptions.getLongWaitTimeout());
        } else {
            try {
                Class<?> webDriverOptionsClass = Class.forName(webDriverWrapperClass);
                if (WebDriverWrapper.class.isAssignableFrom(webDriverOptionsClass)) {
                    return (WebDriverWrapper) webDriverOptionsClass.getDeclaredConstructor(WebDriver.class, Duration.class, Duration.class).newInstance(createWebDriver(webDriverOptions), webDriverOptions.getWaitTimeout(), webDriverOptions.getLongWaitTimeout());
                } else {
                    throw new RuntimeException("Not a valid WebDriverWrapper implementation class: " + webDriverWrapperClass);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static WebDriverWrapper createWebDriverWrapper() {
        return createWebDriverWrapper(DEFAULT_WEB_DRIVER_OPTIONS);
    }

    public static WebDriver createWebDriver(WebDriverOptions webDriverOptions) {
        setupWebDriver(webDriverOptions);

        HashMap<String, Serializable> proxyConfig = webDriverOptions.getProxyConfiguration();
        Proxy proxy = (proxyConfig == null) ? null : new Proxy(proxyConfig);

        WebDriver webDriver = null;
        switch (webDriverOptions.getBrowser()) {
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(NO_SANDBOX);
                chromeOptions.setAcceptInsecureCerts(webDriverOptions.isIgnoreCertificates());
                chromeOptions.setHeadless(webDriverOptions.isHeadless());

                if (webDriverOptions.getAdditionalArguments() != null) {
                    chromeOptions.addArguments(webDriverOptions.getAdditionalArguments());
                }

                if (proxy != null) {
                    chromeOptions.setProxy(proxy);
                }

                webDriver = new ChromeDriver(chromeOptions);
                break;

            case FIREFOX:
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(webDriverOptions.isIgnoreCertificates());
                firefoxOptions.setHeadless(webDriverOptions.isHeadless());

                if (webDriverOptions.getAdditionalArguments() != null) {
                    firefoxOptions.addArguments(webDriverOptions.getAdditionalArguments());
                }

                if (proxy != null) {
                    firefoxOptions.setProxy(proxy);
                }

                webDriver = new FirefoxDriver(firefoxOptions);
                break;

            case EDGE:
                EdgeOptions edgeOptions = getEdgeOptions(webDriverOptions, proxy);

                webDriver = new EdgeDriver(edgeOptions);
                break;

            case SAFARI:
                SafariOptions safariOptions = new SafariOptions();
                safariOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

                if (proxy != null) {
                    safariOptions.setProxy(proxy);
                }

                webDriver = new SafariDriver(safariOptions);
                break;
        }

        webDriver.manage().deleteAllCookies();

        ScreenSize screenSize = webDriverOptions.getScreenSize();
        if (screenSize == null) {
            screenSize = WebDriverOptions.DEFAULT_SCREEN_SIZE;
        }

        webDriver.manage().window().setSize(new Dimension(screenSize.getWidth(), screenSize.getHeight()));

        if (screenSize.isFullscreen()) {
            webDriver.manage().window().fullscreen();
        } else {
            if (screenSize.isMaximized()) {
                webDriver.manage().window().maximize();
            }
        }
        webDriver.manage().timeouts().implicitlyWait(webDriverOptions.getImplicitWaitTime());

        return webDriver;
    }

    private static EdgeOptions getEdgeOptions(WebDriverOptions webDriverOptions, Proxy proxy) {
        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (webDriverOptions.isHeadless()) {
            HashMap<String, Object> options = new HashMap<>();
            ArrayList<String> args = new ArrayList<>();
            args.add(HEADLESS);
            args.add(DISABLE_GPU);
            options.put(ARGS, args);
            edgeOptions.setCapability(MS_EDGE_OPTIONS, options);
        }

        edgeOptions.setCapability(MS_EDGE_CHROMIUM, true);

        if (proxy != null) {
            edgeOptions.setProxy(proxy);
        }
        return edgeOptions;
    }
}
