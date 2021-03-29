package mysample.pom;

import org.mvss.karta.framework.runtime.TestFailureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import automation.framework.webui.AbstractPage;

public class W3SLHHomePage extends AbstractPage
{
   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/div[2]/a[1]" )
   private WebElement homeButton;

   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/div[2]/a[2]" )
   private WebElement nextButton;

   @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/h1[text()='HTML']/span[text()=' Tutorial']" )
   private WebElement verifyText;

   protected W3SLHHomePage( WebDriver driver ) throws TestFailureException
   {
      super( driver );
   }

   @Override
   public boolean validate()
   {
      return waitForVisibility( verifyText );
   }

   public W3SHomePage clickOnHomeButton() throws TestFailureException
   {
      clickElement( homeButton );
      W3SHomePage w3sHomePage = new W3SHomePage( driver );
      return w3sHomePage;
   }

   public W3SLHIntroPage clickOnNextButton() throws TestFailureException
   {
      clickElement( nextButton );
      W3SLHIntroPage w3slhIntroPage = new W3SLHIntroPage( driver );
      return w3slhIntroPage;
   }

}
