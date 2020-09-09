package org.mvss.karta.samples.runner;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class StepDefinitionsCollection1
{
   @StepDefinition( "the calculator is powered on" )
   public void the_calculator_is_powered_on() throws Throwable
   {
      log.info( "the calculator is powered on" );
   }

   @StepDefinition( "the all clear button is pressed" )
   public void the_all_clear_button_is_cleared() throws Throwable
   {
      log.info( "the all clear button is pressed" );
   }

   @StepDefinition( "the calculator should display \"\"" )
   public void the_calculator_should_display( double displayNumber ) throws Throwable
   {
      log.info( "the calculator should display \"" + displayNumber + "\"" );
   }

   @StepDefinition( "the button \"\" is pressed" )
   public void the_button_is_pressed( String button ) throws Throwable
   {
      log.info( "the button \"" + button + "\" is pressed " );
   }

   @StepDefinition( "dummy teardown step" )
   public void dummy_teardown_step() throws Throwable
   {
      log.info( "dummy teardown step" );
   }

   @StepDefinition( "the calculator is powered off" )
   public void the_calculator_is_powered_off() throws Throwable
   {
      log.info( "the calculator is powered off" );
   }

}
