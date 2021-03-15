package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.TestData;

public class FailureSimulationStepDefintions
{
   @StepDefinition( "running pass simulation step" )
   public boolean running_pass_simulation_step()
   {
      return true;
   }

   @StepDefinition( "running failure simulation step" )
   public boolean running_failure_simulation_step( @TestData( "failTheStep" ) Boolean failTheStep )
   {
      return ( failTheStep == null ) || !failTheStep;
   }
}
