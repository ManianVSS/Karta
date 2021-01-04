package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.ParameterMapping;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepParam;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.event.GenericTestEvent;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.samples.resources.AutomationDriver;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StepDefinitionsCollection1
{
   @PropertyMapping( group = "groupName", value = "variable1" )
   private String             username = "default";

   @PropertyMapping( group = "groupName", value = "variable2" )
   private SamplePropertyType variable2;

   @KartaAutoWired( "EmployeeBean" )
   private Employee           employee;

   @StepDefinition( value = "the calculator is powered on" )
   public void the_calculator_is_powered_on( TestExecutionContext context, @StepParam( "employee" ) Employee employee ) throws Throwable
   {
      context.getVariables().put( "CalculatorState", "On" );
      log.info( "the calculator is powered on by " + username + " employee:" + employee + " and testdata=" + context.getData() );
   }

   @StepDefinition( value = "the all clear button is pressed" )
   public StepResult the_all_clear_button_is_cleared( TestExecutionContext context, @StepParam( "csvEmployee" ) Employee csvEmployee ) throws Throwable
   {
      log.info( "the all clear button is pressed. Employee from CSV: " + csvEmployee );

      // HashSet<String> failureTags = new HashSet<String>();
      // failureTags.add( "sample" );
      // failureTags.add( "failure" );
      // failureTags.add( "tags" );
      StepResult result = new StepResult();
      // result.getIncidents().add( TestIncident.builder().message( "Sample test incident" ).tags( failureTags ).build() );
      result.getEvents().add( new GenericTestEvent( context.getRunName(), "Sample test event" ) );
      return result;
   }

   @StepDefinition( "the calculator should display \"\"" )
   public void the_calculator_should_display( double displayNumber, TestExecutionContext context ) throws Throwable
   {
      log.info( "the calculator should display \"" + displayNumber + "\" with testdata " + context.getData() );
   }

   @StepDefinition( "the button \"\" is pressed" )
   public void the_button_is_pressed( TestExecutionContext context, String button ) throws Throwable
   {
      log.info( "the button \"" + button + "\" is pressed with testData" + context.getData() );
   }

   @StepDefinition( "the UI button \"\" is pressed" )
   public void the_UI_button_is_pressed( TestExecutionContext context, @StepParam( value = "AutomationDriverObject", mapto = ParameterMapping.CONTEXT_BEAN ) AutomationDriver driver, String button ) throws Throwable
   {
      log.info( "the UI button \"" + button + "\" is pressed with testData" + context.getData() );
      driver.click( button );
   }

   @StepDefinition( "dummy teardown step" )
   public void dummy_teardown_step() throws Throwable
   {
      log.info( "dummy teardown step " + variable2 + " bean employee= " + employee );
   }

   @StepDefinition( "the calculator is powered off" )
   public void the_calculator_is_powered_off( TestExecutionContext context ) throws Throwable
   {
      log.info( "the calculator is powered off by " + username + " and testdata=" + context.getData() );
   }

}
