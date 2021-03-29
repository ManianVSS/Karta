package mysample.pom;

import org.mvss.karta.framework.runtime.TestFailureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import automation.framework.webui.AbstractPage;

public class W3SLHIntroPage extends AbstractPage
{
   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/div[2]/a[1]" )
   private WebElement previousButton;

   @FindBy( xpath = "/html/body/div[1]/a" )
   private WebElement homeButton;

   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/h1[text()='HTML ']/span[text()='Introduction']" )
   private WebElement verifyText;

   protected W3SLHIntroPage( WebDriver driver ) throws TestFailureException
   {
      super( driver );
   }

   @Override
   public boolean validate()
   {
      return waitForVisibility( verifyText );
   }

   public W3SLHHomePage clickOnPreviousButton() throws TestFailureException
   {
      clickElement( previousButton );
      W3SLHHomePage w3sLHHomePage = new W3SLHHomePage( driver );
      return w3sLHHomePage;
   }

   public W3SHomePage clickOnHomeButton() throws TestFailureException
   {
      clickElement( homeButton );
      W3SHomePage w3sHomePage = new W3SHomePage( driver );
      return w3sHomePage;
   }

}
