package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class StepDefinitionsCollection2
{
   @StepDefinition( "a binary operation is perfomed on the calculator" )
   public StepResult a_binary_operation_is_performed_on_the_calculator( TestExecutionContext context ) throws Throwable
   {
      log.info( "a binary operation is perfomed on the calculator " + context.getData() );
      context.getVariables().put( "BinaryOperationResult", context.getProperties() );
      return StepResult.builder().successsful( true ).results( context.getVariables() ).build();
   }
}
