package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StepDefinitionsCollection1
{
   @PropertyMapping( group = "groupName", value = "variable1" )
   private String             username = "default";

   @PropertyMapping( group = "groupName", value = "variable2" )
   private SamplePropertyType variable2;

   @StepDefinition( "the calculator is powered on" )
   public void the_calculator_is_powered_on( TestExecutionContext context ) throws Throwable
   {
      context.getVariables().put( "CalculatorState", "On" );
      log.info( "the calculator is powered on by " + username + " employee:" + ParserUtils.getObjectMapper().convertValue( context.getData().get( "employee" ), Employee.class ) );
   }

   @StepDefinition( "the all clear button is pressed" )
   public void the_all_clear_button_is_cleared( TestExecutionContext context ) throws Throwable
   {
      log.info( "the all clear button is pressed. Test data is " + context.getData() + " employee: " + ParserUtils.getObjectMapper().readValue( context.getData().get( "csvEmployee" ).toString(), Employee.class ) );
   }

   @StepDefinition( "the calculator should display \"\"" )
   public void the_calculator_should_display( TestExecutionContext context, double displayNumber ) throws Throwable
   {
      log.info( "the calculator should display \"" + displayNumber + "\"" );
   }

   @StepDefinition( "the button \"\" is pressed" )
   public void the_button_is_pressed( TestExecutionContext context, String button ) throws Throwable
   {
      log.info( "the button \"" + button + "\" is pressed " );
   }

   @StepDefinition( "dummy teardown step" )
   public void dummy_teardown_step( TestExecutionContext context ) throws Throwable
   {
      log.info( "dummy teardown step " + variable2 );
   }

   @StepDefinition( "the calculator is powered off" )
   public void the_calculator_is_powered_off( TestExecutionContext context ) throws Throwable
   {
      log.info( "the calculator is powered off by " + username );
   }

}
