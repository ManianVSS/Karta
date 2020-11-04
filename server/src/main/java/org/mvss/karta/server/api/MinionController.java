package org.mvss.karta.server.api;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardScenarioResults;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.utils.ParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MinionController
{
   private static TypeReference<ArrayList<TestStep>> stepListTypeRef        = new TypeReference<ArrayList<TestStep>>()
                                                                            {
                                                                            };

   private static TypeReference<HashSet<String>>     hashSetOfStringTypeRef = new TypeReference<HashSet<String>>()
                                                                            {
                                                                            };

   @Autowired
   private KartaRuntime                              kartaRuntime;

   private ObjectMapper                              objectMapper           = ParserUtils.getObjectMapper();

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.GET, value = Constants.PATH_HEALTH )
   public boolean healthCheck()
   {
      return true;
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_STEP )
   public StepResult runStep( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      try
      {
         String stepRunnerPlugin = (String) parameters.get( "stepRunnerPlugin" );
         TestStep testStep = objectMapper.convertValue( parameters.get( "testStep" ), TestStep.class );
         TestExecutionContext testExecutionContext = objectMapper.convertValue( parameters.get( "testExecutionContext" ), TestExecutionContext.class );
         return kartaRuntime.runStep( stepRunnerPlugin, testStep, testExecutionContext );
      }
      catch ( TestFailureException e )
      {
         return StandardStepResults.failure( e );
      }
      catch ( Throwable t )
      {
         return StandardStepResults.error( t );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_CHAOS_ACTION )
   public StepResult runChaosAction( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      try
      {
         String stepRunnerPlugin = (String) parameters.get( "stepRunnerPlugin" );
         ChaosAction chaosAction = objectMapper.convertValue( parameters.get( "chaosAction" ), ChaosAction.class );
         TestExecutionContext testExecutionContext = objectMapper.convertValue( parameters.get( "testExecutionContext" ), TestExecutionContext.class );
         return kartaRuntime.runChaosAction( stepRunnerPlugin, chaosAction, testExecutionContext );
      }
      catch ( TestFailureException e )
      {
         return StandardStepResults.failure( e );
      }
      catch ( Throwable t )
      {
         return StandardStepResults.error( t );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_SCENARIO )
   public ScenarioResult runScenario( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      try
      {
         HashSet<String> testDataSourcePlugins = objectMapper.convertValue( parameters.get( "testDataSourcePlugins" ), hashSetOfStringTypeRef );
         String stepRunnerPlugin = (String) parameters.get( "stepRunnerPlugin" );
         String runName = (String) parameters.get( "runName" );
         String featureName = (String) parameters.get( "featureName" );
         int iterationIndex = (int) parameters.get( "iterationIndex" );
         ArrayList<TestStep> scenarioSetupSteps = objectMapper.convertValue( parameters.get( "scenarioSetupSteps" ), stepListTypeRef );
         TestScenario testScenario = objectMapper.convertValue( parameters.get( "testScenario" ), TestScenario.class );
         ArrayList<TestStep> scenarioTearDownSteps = objectMapper.convertValue( parameters.get( "scenarioTearDownSteps" ), stepListTypeRef );
         int scenarioIterationNumber = (int) parameters.get( "scenarioIterationNumber" );

         return kartaRuntime.runTestScenario( stepRunnerPlugin, testDataSourcePlugins, runName, featureName, iterationIndex, scenarioSetupSteps, testScenario, scenarioTearDownSteps, scenarioIterationNumber );
      }
      catch ( Throwable t )
      {
         return StandardScenarioResults.error( t );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURE )
   public boolean runFeature( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      try
      {
         HashSet<String> testDataSourcePlugins = objectMapper.convertValue( parameters.get( "testDataSourcePlugins" ), hashSetOfStringTypeRef );
         String stepRunnerPlugin = (String) parameters.get( "stepRunnerPlugin" );
         String runName = (String) parameters.get( "runName" );
         TestFeature feature = objectMapper.convertValue( parameters.get( "feature" ), TestFeature.class );
         boolean chanceBasedScenarioExecution = (boolean) parameters.get( "chanceBasedScenarioExecution" );
         boolean exclusiveScenarioPerIteration = (boolean) parameters.get( "exclusiveScenarioPerIteration" );
         long numberOfIterations = (int) parameters.get( "numberOfIterations" );
         int numberOfIterationsInParallel = (int) parameters.get( "numberOfIterationsInParallel" );

         return kartaRuntime.runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, feature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         return false;
      }
   }
}
