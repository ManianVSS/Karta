package org.mvss.karta.samples.stepdefinitions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.mvss.karta.framework.core.NamedParameter;
import org.mvss.karta.framework.core.ParameterMapping;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.utils.WaitUtil;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class StepDefinitionsCollection2
{
   private Random random = new Random();
   private String token  = RandomizationUtils.randomAlphaNumericString( random, 10 );

   @StepDefinition( "a binary operation is perfomed on the calculator" )
   public StepResult a_binary_operation_is_performed_on_the_calculator( TestExecutionContext context ) throws Throwable
   {
      log.info( "a binary operation is perfomed on the calculator with token " + token + " and  data: " + context.getData() );
      WaitUtil.sleep( 1000 );
      HashMap<String, Serializable> results = new HashMap<String, Serializable>();
      results.put( "BinaryOperationResult", random.nextInt( 100 ) );
      return StepResult.builder().successsful( true ).results( results ).build();
   }

   @StepDefinition( value = "authenticate to get new token", parameterMapping = ParameterMapping.NAMED )
   public void authenticate_to_get_new_token( TestExecutionContext context, @NamedParameter( "userName" ) String userName, @NamedParameter( "password" ) String password ) throws Throwable
   {
      log.info( "authenticating with username " + userName + " and password " + password + " . Test data passed is" + context.getData() );
      String oldToken = token;
      token = RandomizationUtils.randomAlphaNumericString( random, 10 );
      log.info( "new token: " + token );
      context.getVariables().put( "oldToken", oldToken );
   }

   @StepDefinition( "reserve use of calculator for next \"\" milliseconds" )
   public void reserve_use_of_calculator_for_next_milliseconds( TestExecutionContext context, long seconds ) throws Throwable
   {
      log.info( "reserving calculator for " + seconds + " seconds. Test data passed is" + context.getData() );
   }

   @StepDefinition( value = "logoff the old token" )
   public void logoff_the_old_token( TestExecutionContext context ) throws Throwable
   {
      String oldToken = (String) context.getVariables().get( "oldToken" );
      log.info( "Logging off old token: " + oldToken );
   }

}
