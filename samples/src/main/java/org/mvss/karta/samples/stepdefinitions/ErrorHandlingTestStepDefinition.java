package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ErrorHandlingTestStepDefinition
{
   @StepDefinition( "throw a null pointer exception" )
   public StepResult throw_a_null_poiter_exception() throws Throwable
   {
      String str = null;
      int inta = Integer.parseInt( str );
      log.info( "Parsed int is " + inta );
      throw new NullPointerException();
   }

   @StepDefinition( "continue with teardown even on exception" )
   public void continue_with_teardown_even_on_exception() throws Throwable
   {
      log.info( "Continuing with teardown even on exception..." );
   }
}
