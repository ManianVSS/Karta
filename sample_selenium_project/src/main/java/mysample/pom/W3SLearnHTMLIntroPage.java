package mysample.pom;

import org.mvss.karta.framework.runtime.TestFailureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import automation.framework.webui.AbstractPage;

public class W3SLearnHTMLIntroPage extends AbstractPage
{
   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/div[2]/a[1]" )
   private WebElement previousButton;

   @FindBy( xpath = "/html/body/div[1]/a" )
   private WebElement homeButton;

   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/h1[text()='HTML ']/span[text()='Introduction']" )
   private WebElement verifyText;

   protected W3SLearnHTMLIntroPage( WebDriver driver ) throws TestFailureException
   {
      super( driver );
   }

   @Override
   public boolean validate()
   {
      return waitForVisibility( verifyText );
   }

   public W3SLearnHTMLHomePage clickOnPreviousButton() throws TestFailureException
   {
      clickElement( previousButton );
      W3SLearnHTMLHomePage w3sLHHomePage = new W3SLearnHTMLHomePage( driver );
      return w3sLHHomePage;
   }

   public W3SchoolsHomePage clickOnHomeButton() throws TestFailureException
   {
      clickElement( homeButton );
      W3SchoolsHomePage w3sHomePage = new W3SchoolsHomePage( driver );
      return w3sHomePage;
   }

}
