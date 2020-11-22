package org.mvss.karta.server.api;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardFeatureResults;
import org.mvss.karta.framework.core.StandardScenarioResults;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
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
   private static TypeReference<HashSet<String>> hashSetOfStringTypeRef = new TypeReference<HashSet<String>>()
                                                                        {
                                                                        };

   @Autowired
   private KartaRuntime                          kartaRuntime;

   private ObjectMapper                          objectMapper           = ParserUtils.getObjectMapper();

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
         if ( parameters == null )
         {
            return StandardStepResults.error( "Missing parameters in body" );
         }

         String stepRunnerPlugin = parameters.containsKey( Constants.STEP_RUNNER_PLUGIN ) ? (String) parameters.get( Constants.STEP_RUNNER_PLUGIN ) : kartaRuntime.getKartaConfiguration().getDefaultStepRunnerPlugin();
         PreparedStep testStep = objectMapper.convertValue( parameters.get( Constants.TEST_STEP ), PreparedStep.class );

         if ( testStep == null )
         {
            return StandardStepResults.error( "Step to run missing in parameters" );
         }

         return kartaRuntime.runStep( stepRunnerPlugin, testStep );
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
         if ( parameters == null )
         {
            return StandardStepResults.error( "Missing parameters in body" );
         }

         String stepRunnerPlugin = parameters.containsKey( Constants.STEP_RUNNER_PLUGIN ) ? (String) parameters.get( Constants.STEP_RUNNER_PLUGIN ) : kartaRuntime.getKartaConfiguration().getDefaultStepRunnerPlugin();
         PreparedChaosAction chaosAction = objectMapper.convertValue( parameters.get( Constants.CHAOS_ACTION ), PreparedChaosAction.class );

         if ( chaosAction == null )
         {
            return StandardStepResults.error( "Chaos action to run missing in parameters" );
         }

         return kartaRuntime.runChaosAction( stepRunnerPlugin, chaosAction );
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
         if ( parameters == null )
         {
            return StandardScenarioResults.error( "Missing parameters in body" );
         }

         String stepRunnerPlugin = parameters.containsKey( Constants.STEP_RUNNER_PLUGIN ) ? (String) parameters.get( Constants.STEP_RUNNER_PLUGIN ) : kartaRuntime.getKartaConfiguration().getDefaultStepRunnerPlugin();
         String runName = (String) parameters.get( Constants.RUN_NAME );
         String featureName = (String) parameters.get( Constants.FEATURE_NAME );
         long iterationIndex = DataUtils.serializableToLong( parameters.get( Constants.ITERATION_INDEX ), -1 );
         PreparedScenario testScenario = objectMapper.convertValue( parameters.get( Constants.TEST_SCENARIO ), PreparedScenario.class );
         long scenarioIterationNumber = DataUtils.serializableToLong( parameters.get( Constants.SCENARIO_ITERATION_NUMBER ), -1 );

         if ( testScenario == null )
         {
            return StandardScenarioResults.error( "Scenario to run missing in parameters" );
         }

         return kartaRuntime.runTestScenario( stepRunnerPlugin, runName, featureName, iterationIndex, testScenario, scenarioIterationNumber );
      }
      catch ( Throwable t )
      {
         return StandardScenarioResults.error( t );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_JOB )
   public long runJob( @RequestBody HashMap<String, Serializable> parameters ) throws Throwable
   {
      if ( parameters == null )
      {
         throw new Exception( "Missing parameters in body" );
      }

      String stepRunnerPlugin = parameters.containsKey( Constants.STEP_RUNNER_PLUGIN ) ? (String) parameters.get( Constants.STEP_RUNNER_PLUGIN ) : kartaRuntime.getKartaConfiguration().getDefaultStepRunnerPlugin();
      HashSet<String> testDataSourcePlugins = parameters.containsKey( Constants.TEST_DATA_SOURCE_PLUGINS ) ? objectMapper.convertValue( parameters.get( Constants.TEST_DATA_SOURCE_PLUGINS ), hashSetOfStringTypeRef )
               : kartaRuntime.getKartaConfiguration().getDefaultTestDataSourcePlugins();
      String runName = (String) parameters.get( Constants.RUN_NAME );
      String featureName = (String) parameters.get( Constants.FEATURE_NAME );
      TestJob job = objectMapper.convertValue( parameters.get( Constants.JOB ), TestJob.class );

      if ( job == null )
      {
         throw new Exception( "Missing job to run in parameters" );
      }

      return kartaRuntime.scheduleJob( stepRunnerPlugin, testDataSourcePlugins, runName, featureName, job );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.DELETE, value = Constants.PATH_RUN_JOB_ID )
   public boolean deleteJob( @PathVariable long id )
   {
      return kartaRuntime.deleteJob( id );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURE )
   public FeatureResult runFeature( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      try
      {
         if ( parameters == null )
         {
            return StandardFeatureResults.error( "Missing parameters in body" );
         }

         String stepRunnerPlugin = parameters.containsKey( Constants.STEP_RUNNER_PLUGIN ) ? (String) parameters.get( Constants.STEP_RUNNER_PLUGIN ) : kartaRuntime.getKartaConfiguration().getDefaultStepRunnerPlugin();
         HashSet<String> testDataSourcePlugins = parameters.containsKey( Constants.TEST_DATA_SOURCE_PLUGINS ) ? objectMapper.convertValue( parameters.get( Constants.TEST_DATA_SOURCE_PLUGINS ), hashSetOfStringTypeRef )
                  : kartaRuntime.getKartaConfiguration().getDefaultTestDataSourcePlugins();
         String runName = (String) parameters.get( Constants.RUN_NAME );
         TestFeature feature = objectMapper.convertValue( parameters.get( Constants.FEATURE ), TestFeature.class );

         if ( feature == null )
         {
            return StandardFeatureResults.error( "Feature to run missing in parameters" );
         }

         boolean chanceBasedScenarioExecution = parameters.containsKey( Constants.CHANCE_BASED_SCENARIO_EXECUTION ) ? (boolean) parameters.get( Constants.CHANCE_BASED_SCENARIO_EXECUTION ) : false;
         boolean exclusiveScenarioPerIteration = parameters.containsKey( Constants.EXCLUSIVE_SCENARIO_PER_ITERATION ) ? (boolean) parameters.get( Constants.EXCLUSIVE_SCENARIO_PER_ITERATION ) : false;
         long numberOfIterations = DataUtils.serializableToLong( parameters.get( Constants.NUMBER_OF_ITERATIONS ), 1 );
         int numberOfIterationsInParallel = parameters.containsKey( Constants.NUMBER_OF_ITERATIONS_IN_PARALLEL ) ? (int) parameters.get( Constants.NUMBER_OF_ITERATIONS_IN_PARALLEL ) : 1;

         return kartaRuntime.runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, feature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         return StandardFeatureResults.error( t );
      }
   }
}
