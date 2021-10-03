package org.mvss.karta.framework.web;

import lombok.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class WebDriverOptions implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Builder.Default
   private String webDriverLocation = "chromedriver";

   public static final ScreenSize DEFAULT_SCREEN_SIZE = new ScreenSize();

   @Builder.Default
   private Browser browser = Browser.CHROME;

   private HashMap<String, Serializable> proxyConfiguration;

   @Builder.Default
   private boolean headless = true;

   @Builder.Default
   private boolean ignoreCertificates = true;

   @Builder.Default
   private ScreenSize screenSize = DEFAULT_SCREEN_SIZE;

   @Builder.Default
   private Duration implicitWaitTime = Duration.ofSeconds( 5 );

   @Builder.Default
   private Duration waitTimeout = Duration.ofMinutes( 1 );

   @Builder.Default
   private Duration longWaitTimeout = Duration.ofMinutes( 3 );
}
