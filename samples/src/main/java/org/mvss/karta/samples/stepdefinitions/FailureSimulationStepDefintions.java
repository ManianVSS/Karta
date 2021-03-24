package org.mvss.karta.samples.stepdefinitions;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestData;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.runtime.TestFailureException;

public class FailureSimulationStepDefintions
{
   @StepDefinition( "running pass simulation step" )
   public boolean running_pass_simulation_step()
   {
      return true;
   }

   @StepDefinition( "running failure simulation step" )
   public StepResult running_failure_simulation_step( @TestData( "failTheStep" ) Boolean failTheStep, @TestData( "failureType" ) String failureType, @TestData( "failureMessage" ) String failureMessage ) throws TestFailureException
   {
      if ( ( failTheStep == null ) || !failTheStep )
      {
         return StandardStepResults.passed();
      }

      if ( StringUtils.isBlank( failureMessage ) )
      {
         failureMessage = "Unknown failure";
      }

      switch ( failureType )
      {
         case "test failure exception":
            throw new TestFailureException( failureMessage );

         case "incident":
            StepResult stepResult = new StepResult();
            stepResult.addIncident( TestIncident.builder().message( failureMessage ).build() );
            return stepResult;

         case "simple":
         default:
            return StandardStepResults.failed();
      }
   }
}
