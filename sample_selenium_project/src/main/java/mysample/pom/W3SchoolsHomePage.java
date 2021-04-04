package mysample.pom;

import org.mvss.karta.framework.runtime.TestFailureException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import automation.framework.webui.AbstractPage;

public class W3SchoolsHomePage extends AbstractPage
{
   @FindBy( xpath = "/html/body/nav[1]/div/a[1]" )
   private WebElement learnHtmlButton;

   @FindBy( xpath = "/html/body/nav[1]/div/a[2]" )
   private WebElement learnCSSButton;

   public W3SchoolsHomePage( WebDriver driver ) throws TestFailureException
   {
      super( driver );
   }

   @Override
   public boolean validate()
   {
      return waitForVisibility( learnHtmlButton );
   }

   public W3SLearnHTMLHomePage clickOnLeanHTML() throws TestFailureException
   {
      clickElement( learnHtmlButton );
      W3SLearnHTMLHomePage w3slhHomePage = new W3SLearnHTMLHomePage( driver );
      return w3slhHomePage;
   }
}
