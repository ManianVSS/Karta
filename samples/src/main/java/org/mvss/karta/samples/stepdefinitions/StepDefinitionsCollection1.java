package org.mvss.karta.samples.stepdefinitions;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.*;
import org.mvss.karta.framework.enums.StepOutputType;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.event.GenericTestEvent;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.web.PageException;
import org.mvss.karta.samples.pom.w3s.HTMLIntroductionPage;
import org.mvss.karta.samples.pom.w3s.HomePage;
import org.mvss.karta.samples.pom.w3s.LearnHTMLHomePage;
import org.mvss.karta.samples.pom.w3s.W3SchoolsApp;

import java.io.IOException;

@Log4j2
public class StepDefinitionsCollection1
{
   private static final String OFF = "Off";

   private static final String ON = "On";

   @PropertyMapping( group = "groupName", value = "variable1" )
   private String username = "default";

   @PropertyMapping( group = "groupName", value = "variable2" )
   private SamplePropertyType variable2 = null;

   @KartaAutoWired( "EmployeeBean" )
   private Employee employee;

   @ConditionDefinition( value = "the calculator is powered \"\"" )
   public boolean is_the_calculator_is_powered_on( @ContextVariable( "CalculatorState" ) String calculatorState, String expectedState )
   {
      if ( StringUtils.isBlank( calculatorState ) )
      {
         return StringUtils.isBlank( expectedState );
      }
      else if ( StringUtils.isBlank( expectedState ) )
      {
         return false;
      }
      else
      {
         return expectedState.equals( calculatorState );
      }
   }

   @StepDefinition( value = "the calculator is powered on", outputName = "CalculatorState", outputType = StepOutputType.VARIABLE )
   public String the_calculator_is_powered_on( TestExecutionContext context, @TestData( "employee" ) Employee employee )
   {
      log.info( "the calculator is powered on by " + username + " employee:" + employee + " and test data=" + context.getData() );
      return ON;
   }

   @StepDefinition( value = "the all clear button is pressed" )
   public StepResult the_all_clear_button_is_cleared( TestExecutionContext context, @TestData( "csvEmployee" ) Employee csvEmployee )
   {
      log.info( "the all clear button is pressed. Employee from CSV: " + csvEmployee );
      StepResult result = new StepResult();
      result.getEvents().add( new GenericTestEvent( context.getRunName(), "Sample test event" ) );
      return result;
   }

   @StepDefinition( "the calculator should display \"\"" )
   public void the_calculator_should_display( double displayNumber, TestExecutionContext context )
   {
      log.info( "the calculator should display \"" + displayNumber + "\" with testdata " + context.getData() );
   }

   @StepDefinition( "the W3Schools site is launched in the browser" ) //
   public void the_w3schools_site_is_launched_in_the_browser( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      w3SchoolsApp.init();
   }

   @StepDefinition( "close W3Schools page" )
   public void close_the_admin_console( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp ) throws PageException
   {
      w3SchoolsApp.close();
   }

   @StepDefinition( "take W3Schools screenshot" )
   public void take_admin_console_screenshot( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp ) throws IOException
   {
      w3SchoolsApp.getDriver().takeSnapshot( "Screenshot-" );
   }

   @StepDefinition( "Learn HTML button from Home page is clicked" )
   public void learn_html_button_from_home_page_is_clicked( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      HomePage homePage = (HomePage) w3SchoolsApp.getCurrentPage();
      homePage.clickOnLearnHTML();
   }

   @StepDefinition( "HTML introduction link from Learn HTML Home page is clicked" )
   public void html_introduction_link_from_learn_html_home_page_is_clicked( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      LearnHTMLHomePage learnHTMLHomePage = (LearnHTMLHomePage) w3SchoolsApp.getCurrentPage();
      learnHTMLHomePage.goToHTMLIntroductionPage();
   }

   @StepDefinition( "Learn HTML home link from HTML introduction page is clicked" )
   public void learn_html_home_link_from_html_introduction_page_is_clicked( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      HTMLIntroductionPage htmlIntroductionPage = (HTMLIntroductionPage) w3SchoolsApp.getCurrentPage();
      htmlIntroductionPage.goToLearnHTMLHome();
   }

   @StepDefinition( "W3 Schools home button from Learn HTML Home page is clicked" )
   public void w3_schools_home_button_from_learn_html_home_page_is_clicked( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      LearnHTMLHomePage learnHTMLHomePage = (LearnHTMLHomePage) w3SchoolsApp.getCurrentPage();
      learnHTMLHomePage.goToW3SchoolsHome();
   }

   @StepDefinition( "fluent navigation on W3 Schools site is demonstrated" )
   public void fluent_navigation_on_w3_schools_site_is_demonstrated( @ContextBean( W3SchoolsApp.W_3_SCHOOLS_APP ) W3SchoolsApp w3SchoolsApp )
            throws PageException
   {
      assert ( (HomePage) w3SchoolsApp.init() ).clickOnLearnHTML().goToHTMLIntroductionPage().goToLearnHTMLHome().goToW3SchoolsHome()
               .clickOnLearnHTML().goToHTMLIntroductionPage().goToW3SchoolsHome().validate();
   }

   @StepDefinition( "the button \"\" is pressed" )
   public void the_button_is_pressed( TestExecutionContext context, String button )
   {
      log.info( "the button \"" + button + "\" is pressed with testData" + context.getData() );
   }

   @StepDefinition( "dummy teardown step" )
   public void dummy_teardown_step()
   {
      log.info( "dummy teardown step " + variable2 + " bean employee= " + employee );
   }

   @StepDefinition( value = "the calculator is powered off", outputName = "CalculatorState", outputType = StepOutputType.VARIABLE )
   public String the_calculator_is_powered_off( TestExecutionContext context )
   {
      log.info( "the calculator is powered off by " + username + " and test data=" + context.getData() );
      return OFF;
   }

}
