package automation.framework.webui;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mysample.stepdefinitions.Browser;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class WebDriverOptions implements Serializable
{
   private static final long             serialVersionUID    = 1L;

   public static final ScreenSize        DEFAULT_SCREEN_SIZE = new ScreenSize();

   @Builder.Default
   private Browser                       browser             = Browser.CHROME;

   private HashMap<String, Serializable> proxyConfiguration;

   @Builder.Default
   private boolean                       headless            = false;

   @Builder.Default
   private boolean                       ignoreCertificates  = true;

   @Builder.Default
   private ScreenSize                    screenSize          = DEFAULT_SCREEN_SIZE;

   @Builder.Default
   private Duration                      implicitWaitTime    = Duration.ofSeconds( 5 );
}
