package org.mvss.karta.samples.chaosdefinitions;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.ChaosActionDefinition;
import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class ChaosActionDefinitionCollection
{
   @ChaosActionDefinition( "the calculator is powered off" )
   public void the_calculator_is_powered_on( TestExecutionContext context, ChaosAction actionToPerform ) throws Throwable
   {
      log.info( "Chaos action called " + actionToPerform );
   }
}
