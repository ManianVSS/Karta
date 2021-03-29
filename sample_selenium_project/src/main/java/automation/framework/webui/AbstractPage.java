package automation.framework.webui;

import java.time.Duration;

import org.mvss.karta.framework.core.LoadConfiguration;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LoadConfiguration
public abstract class AbstractPage
{
   @PropertyMapping( group = "WebAutomation", value = "waitTimeout" )
   private static Duration DEFAULT_OBJECT_WAIT_TIMEOUT = Duration.ofSeconds( 60 );

   @PropertyMapping( group = "WebAutomation", value = "longWaitTimeout" )
   private static Duration DEFAULT_OBJECT_LONG_TIMEOUT = Duration.ofSeconds( 180 );

   protected WebDriver     driver                      = null;
   private WebDriverWait   waitTimeout                 = null;
   private WebDriverWait   longWaitTimeOut             = null;

   public AbstractPage( WebDriver driver ) throws TestFailureException
   {
      this.driver = driver;
      waitTimeout = new WebDriverWait( driver, DEFAULT_OBJECT_WAIT_TIMEOUT.getSeconds() );
      longWaitTimeOut = new WebDriverWait( driver, DEFAULT_OBJECT_LONG_TIMEOUT.getSeconds() );

      PageFactory.initElements( driver, this );

      if ( !validate() )
      {
         throw new TestFailureException( "Validation for " + this.getClass().getName() + " failed." );
      }
   }

   public abstract boolean validate();

   public boolean waitForVisibility( WebElement element )
   {
      return waitTimeout.until( ExpectedConditions.visibilityOf( element ) ) != null;
   }

   public String getCurrentURL()
   {
      return driver.getCurrentUrl();
   }

   public byte[] getScreenShot()
   {
      return ( (TakesScreenshot) driver ).getScreenshotAs( OutputType.BYTES );
   }

   public void get( String url )
   {
      driver.get( url );
   }

   public void navigateToURL( String url )
   {
      driver.navigate().to( url );
   }

   public void clickElement( WebElement element )
   {
      waitForVisibility( element );
      element.click();
   }
}
