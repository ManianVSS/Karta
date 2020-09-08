package org.mvss.karta.samples.runner;

public class StepDefinitions
{
   @StepDefinition( "the calculator is powered on" )
   public static void the_calculator_is_powered_on() throws Throwable
   {
      System.out.println( "the calculator is powered on" );
   }

   @StepDefinition( "the all clear button is pressed" )
   public static void the_all_clear_button_is_cleared() throws Throwable
   {
      System.out.println( "the all clear button is pressed" );
   }

   @StepDefinition( "the calculator should display \"\"" )
   public static void the_calculator_should_display( int displayNumber ) throws Throwable
   {
      System.out.println( "the calculator should display \"" + displayNumber + "\"" );
   }

   @StepDefinition( "the button \"\" is pressed" )
   public static void the_button_is_pressed( String button ) throws Throwable
   {
      System.out.println( "the button \"" + button + "\" is pressed " );
   }

   @StepDefinition( "dummy teardown step" )
   public static void dummy_teardown_step() throws Throwable
   {
      System.out.println( "dummy teardown step" );
   }

   @StepDefinition( "the calculator is powered off" )
   public static void the_calculator_is_powered_off() throws Throwable
   {
      System.out.println( "the calculator is powered off" );
   }

}
