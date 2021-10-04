package org.mvss.karta.samples.pom.w3s;

import org.mvss.karta.framework.web.AbstractPage;
import org.mvss.karta.framework.web.PageException;
import org.mvss.karta.framework.web.WebAUT;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LearnHTMLHomePage extends AbstractPage
{
   @FindBy( xpath = "//a[@title='Home']//i" )
   private WebElement logo;

   @FindBy( xpath = "//a[contains(text(),'HTML HOME')]" )
   private WebElement learnHTMLHomeLink;

   @FindBy( xpath = "//a[contains(text(),'HTML Introduction')]" )
   private WebElement htmlIntroductionLink;

   public LearnHTMLHomePage( WebAUT webAUT ) throws PageException
   {
      super( webAUT );
   }

   @Override
   public boolean validate()
   {
      return driver.isElementAvailable( learnHTMLHomeLink );
   }

   public HomePage goToW3SchoolsHome() throws PageException
   {
      driver.clickElement( logo );
      return new HomePage( webAUT );
   }

   public HTMLIntroductionPage goToHTMLIntroductionPage() throws PageException
   {
      driver.clickElement( htmlIntroductionLink );
      return new HTMLIntroductionPage( webAUT );
   }
}
