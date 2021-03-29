package mysample.pom;

import org.mvss.karta.framework.runtime.TestFailureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import automation.framework.webui.AbstractPage;

public class W3SHomePage extends AbstractPage
{
   // @FindBy( xpath = "/html/body/div[6]/div[1]/div[1]/p[text()='The language for building web pages']" )
   // private WebElement homeVerifyText;

   @FindBy( xpath = "/html/body/nav[1]/div/a[1]" )
   private WebElement learnHtmlButton;

   @FindBy( xpath = "/html/body/nav[1]/div/a[2]" )
   private WebElement learnCSSButton;

   public W3SHomePage( WebDriver driver ) throws TestFailureException
   {
      super( driver );
   }

   @Override
   public boolean validate()
   {
      return waitForVisibility( learnHtmlButton );
   }

   public W3SLHHomePage clickOnLeanHTML() throws TestFailureException
   {
      clickElement( learnHtmlButton );
      W3SLHHomePage w3slhHomePage = new W3SLHHomePage( driver );
      return w3slhHomePage;
   }
}
