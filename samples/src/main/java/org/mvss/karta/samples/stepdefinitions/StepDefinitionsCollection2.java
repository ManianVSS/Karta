package org.mvss.karta.samples.stepdefinitions;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.StepDefinition;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class StepDefinitionsCollection2
{
   @StepDefinition( "a binary operation is perfomed on the calculator" )
   public void a_binary_operation_is_performed_on_the_calculator( HashMap<String, Serializable> parameters ) throws Throwable
   {
      log.info( "a binary operation is perfomed on the calculator " + parameters );
   }
}
