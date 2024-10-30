package org.mvss.karta.framework.web;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WebDriverOptions implements Serializable {
    public static final ScreenSize DEFAULT_SCREEN_SIZE = new ScreenSize();
    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private String webDriverLocation = "chromedriver";
    @Builder.Default
    private Browser browser = Browser.CHROME;

    private HashMap<String, Serializable> proxyConfiguration;

    @Builder.Default
    private boolean remote = false;

    @Builder.Default
    private boolean useService = false;

    @Builder.Default
    private boolean deleteAllCookies = false;

    private ArrayList<String> serviceArguments;

    @Builder.Default
    private boolean headless = false;

    @Builder.Default
    private boolean ignoreCertificates = true;


    @Builder.Default
    private boolean resize = false;

    @Builder.Default
    private ScreenSize screenSize = DEFAULT_SCREEN_SIZE;

    @Builder.Default
    private Duration implicitWaitTime = Duration.ofSeconds(5);

    @Builder.Default
    private Duration waitTimeout = Duration.ofMinutes(1);

    @Builder.Default
    private Duration longWaitTimeout = Duration.ofMinutes(3);

    private ArrayList<String> additionalArguments;

    private LinkedHashMap<String, Serializable> capabilities;

    @Builder.Default
    private String webDriverWrapperClass = "org.mvss.karta.framework.web.WebDriverWrapper";
}
