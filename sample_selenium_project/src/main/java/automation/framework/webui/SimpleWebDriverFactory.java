package automation.framework.webui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.mvss.karta.framework.core.LoadConfiguration;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
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

@LoadConfiguration
public class SimpleWebDriverFactory
{
   @PropertyMapping( group = "WebAutomation", value = "webDriverOptions" )
   private static WebDriverOptions webDriverOptions = new WebDriverOptions();

   public static WebDriver createWebDriver()
   {
      HashMap<String, Serializable> proxyConfig = webDriverOptions.getProxyConfiguration();
      Proxy proxy = proxyConfig == null ? null : new Proxy( proxyConfig );

      WebDriver webDriver = null;
      switch ( webDriverOptions.getBrowser() )
      {
         case CHROME:
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments( "--no-sandbox" );
            chromeOptions.setAcceptInsecureCerts( webDriverOptions.isIgnoreCertificates() );
            chromeOptions.setHeadless( webDriverOptions.isHeadless() );

            if ( proxy != null )
            {
               chromeOptions.setProxy( proxy );
            }

            webDriver = new ChromeDriver( chromeOptions );
            break;

         case FIREFOX:
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setAcceptInsecureCerts( webDriverOptions.isIgnoreCertificates() );
            firefoxOptions.setHeadless( webDriverOptions.isHeadless() );

            if ( proxy != null )
            {
               firefoxOptions.setProxy( proxy );
            }

            webDriver = new FirefoxDriver( firefoxOptions );
            break;

         case EDGE:
            EdgeOptions edgeOptions = new EdgeOptions();
            edgeOptions.setCapability( CapabilityType.SUPPORTS_JAVASCRIPT, true );
            edgeOptions.setCapability( CapabilityType.ACCEPT_INSECURE_CERTS, true );

            if ( webDriverOptions.isHeadless() )
            {
               HashMap<String, Object> options = new HashMap<String, Object>();
               ArrayList<String> args = new ArrayList<String>();
               args.add( "--headless" );
               args.add( "--disable-gpu" );
               options.put( "args", args );
               edgeOptions.setCapability( "ms:edgeOptions", options );
            }

            edgeOptions.setCapability( "ms:edgeChromium", true );

            if ( proxy != null )
            {
               edgeOptions.setProxy( proxy );
            }

            webDriver = new EdgeDriver( edgeOptions );
            break;

         case SAFARI:
            SafariOptions safariOptions = new SafariOptions();
            safariOptions.setCapability( CapabilityType.SUPPORTS_JAVASCRIPT, true );
            safariOptions.setCapability( CapabilityType.ACCEPT_INSECURE_CERTS, true );

            if ( proxy != null )
            {
               safariOptions.setProxy( proxy );
            }

            webDriver = new SafariDriver( safariOptions );
            break;
      }

      webDriver.manage().deleteAllCookies();

      ScreenSize screenSize = webDriverOptions.getScreenSize();
      if ( screenSize == null )
      {
         screenSize = WebDriverOptions.DEFAULT_SCREEN_SIZE;
      }
      if ( screenSize.isFullscreen() )
      {
         webDriver.manage().window().fullscreen();
      }
      else
      {
         if ( screenSize.isMaximized() )
         {
            webDriver.manage().window().maximize();
         }
         else
         {
            webDriver.manage().window().setSize( new Dimension( screenSize.getWidth(), screenSize.getHeight() ) );
         }
      }
      webDriver.manage().timeouts().implicitlyWait( 10, TimeUnit.SECONDS );

      return webDriver;
   }
}
