package org.mvss.karta.samples.pom.w3s;

import org.mvss.karta.framework.web.AbstractPage;
import org.mvss.karta.framework.web.PageException;
import org.mvss.karta.framework.web.WebAUT;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends AbstractPage
{
   @FindBy( xpath = "//a[@title='Home']//i" )
   private WebElement logo;

   @FindBy( xpath = "//div[@id='main']//a[contains(text(),'Learn HTML')]" )
   private WebElement learnHTMLButton;

   public HomePage( WebAUT webAUT ) throws PageException
   {
      super( webAUT );
   }

   @Override
   public boolean validate()
   {
      return driver.isElementAvailable( learnHTMLButton );
   }

   public LearnHTMLHomePage clickOnLearnHTML() throws PageException
   {
      driver.clickElement( learnHTMLButton );
      return new LearnHTMLHomePage( webAUT );
   }
}
