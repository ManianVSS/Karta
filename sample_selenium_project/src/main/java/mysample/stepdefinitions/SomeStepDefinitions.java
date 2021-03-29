package mysample.stepdefinitions;

import java.util.HashMap;

import org.mvss.karta.framework.core.ContextBean;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.enums.StepOutputType;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.openqa.selenium.WebDriver;

import mysample.pom.W3SHomePage;
import mysample.pom.W3SLHHomePage;
import mysample.pom.W3SLHIntroPage;

public class SomeStepDefinitions
{
   @PropertyMapping( group = "WebAutomation", value = "startURL" )
   private String                  startURL;

   @PropertyMapping( group = "WebAutomation", value = "xpaths" )
   private HashMap<String, String> xpathMap;

   @StepDefinition( value = "the W3schools web application is launched", outputType = StepOutputType.BEAN, outputName = "w3sHomePage" )
   public W3SHomePage the_W3schools_web_application_is_launched( @ContextBean( "WebDriverObject" ) WebDriver driver ) throws TestFailureException
   {
      driver.navigate().to( startURL );
      return new W3SHomePage( driver );
   }

   @StepDefinition( value = "the learn html button is clicked", outputType = StepOutputType.BEAN, outputName = "w3slhHomePage" )
   public W3SLHHomePage the_learn_html_button_is_clicked( @ContextBean( "w3sHomePage" ) W3SHomePage w3sHomePage ) throws TestFailureException
   {
      return w3sHomePage.clickOnLeanHTML();
   }

   @StepDefinition( value = "the next button is clicked on the learn html home page", outputType = StepOutputType.BEAN, outputName = "w3slhIntroPage" )
   public W3SLHIntroPage the_next_button_is_clicked_on_learn_html_home_page( @ContextBean( "w3slhHomePage" ) W3SLHHomePage w3slhHomePage ) throws TestFailureException
   {
      return w3slhHomePage.clickOnNextButton();
   }

   @StepDefinition( value = "the home button is clicked on the learn html home page", outputType = StepOutputType.BEAN, outputName = "w3sHomePage" )
   public W3SHomePage the_home_button_is_clicked_on_the_learn_html_home_page( @ContextBean( "w3slhHomePage" ) W3SLHHomePage w3slhHomePage ) throws TestFailureException
   {
      return w3slhHomePage.clickOnHomeButton();
   }

   @StepDefinition( value = "the previous button is clicked on the learn html intro page", outputType = StepOutputType.BEAN, outputName = "w3slhHomePage" )
   public W3SLHHomePage the_previous_button_is_clicked_on_the_learn_html_intro_page( @ContextBean( "w3slhIntroPage" ) W3SLHIntroPage w3slhIntroPage ) throws TestFailureException
   {
      return w3slhIntroPage.clickOnPreviousButton();
   }

   @StepDefinition( value = "the home button is clicked on the learn html intro page", outputType = StepOutputType.BEAN, outputName = "w3sHomePage" )
   public W3SHomePage the_home_button_is_clicked_on_the_learn_html_intro_page( @ContextBean( "w3slhIntroPage" ) W3SLHIntroPage w3slhIntroPage ) throws TestFailureException
   {
      return w3slhIntroPage.clickOnHomeButton();
   }

}
