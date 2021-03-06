package org.mvss.karta.server.api;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

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
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunInfo;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.TestJobRunner;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MinionController
{
   @Autowired
   private KartaRuntime kartaRuntime;

   private ObjectMapper objectMapper = ParserUtils.getObjectMapper();

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

         RunInfo runInfo = parameters.containsKey( Constants.RUN_INFO ) ? objectMapper.convertValue( parameters.get( Constants.RUN_INFO ), RunInfo.class ) : kartaRuntime.getDefaultRunInfo();
         PreparedStep testStep = objectMapper.convertValue( parameters.get( Constants.TEST_STEP ), PreparedStep.class );

         if ( testStep == null )
         {
            return StandardStepResults.error( "Step to run missing in parameters" );
         }

         return kartaRuntime.runStep( runInfo, testStep );
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

         RunInfo runInfo = parameters.containsKey( Constants.RUN_INFO ) ? objectMapper.convertValue( parameters.get( Constants.RUN_INFO ), RunInfo.class ) : kartaRuntime.getDefaultRunInfo();
         PreparedChaosAction chaosAction = objectMapper.convertValue( parameters.get( Constants.CHAOS_ACTION ), PreparedChaosAction.class );

         if ( chaosAction == null )
         {
            return StandardStepResults.error( "Chaos action to run missing in parameters" );
         }

         return kartaRuntime.runChaosAction( runInfo, chaosAction );
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

         RunInfo runInfo = parameters.containsKey( Constants.RUN_INFO ) ? objectMapper.convertValue( parameters.get( Constants.RUN_INFO ), RunInfo.class ) : kartaRuntime.getDefaultRunInfo();
         String featureName = (String) parameters.get( Constants.FEATURE_NAME );
         long iterationIndex = DataUtils.serializableToLong( parameters.get( Constants.ITERATION_INDEX ), -1 );
         PreparedScenario testScenario = objectMapper.convertValue( parameters.get( Constants.TEST_SCENARIO ), PreparedScenario.class );
         long scenarioIterationNumber = DataUtils.serializableToLong( parameters.get( Constants.SCENARIO_ITERATION_NUMBER ), -1 );

         if ( testScenario == null )
         {
            return StandardScenarioResults.error( "Scenario to run missing in parameters" );
         }

         return kartaRuntime.runTestScenario( runInfo, featureName, iterationIndex, testScenario, scenarioIterationNumber );
      }
      catch ( Throwable t )
      {
         return StandardScenarioResults.error( t );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_JOB_ITERATION )
   public TestJobResult runJobIteration( @RequestBody HashMap<String, Serializable> parameters ) throws Throwable
   {
      if ( parameters == null )
      {
         throw new Exception( "Missing parameters in body" );
      }

      RunInfo runInfo = parameters.containsKey( Constants.RUN_INFO ) ? objectMapper.convertValue( parameters.get( Constants.RUN_INFO ), RunInfo.class ) : kartaRuntime.getDefaultRunInfo();
      String featureName = (String) parameters.get( Constants.FEATURE_NAME );
      TestJob job = objectMapper.convertValue( parameters.get( Constants.JOB ), TestJob.class );
      long iterationIndex = DataUtils.serializableToLong( parameters.get( Constants.ITERATION_INDEX ), -1 );

      if ( job == null )
      {
         throw new Exception( "Missing job to run in parameters" );
      }

      return TestJobRunner.run( kartaRuntime, runInfo, featureName, job, iterationIndex, null );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURE )
   public FeatureResult runFeature( @RequestBody HashMap<String, Serializable> parameters ) throws IllegalAccessException, InvocationTargetException
   {
      String featureName = Constants.UNNAMED;
      try
      {
         if ( parameters == null )
         {
            return StandardFeatureResults.error( featureName, "Missing parameters in body" );
         }

         RunInfo runInfo = parameters.containsKey( Constants.RUN_INFO ) ? objectMapper.convertValue( parameters.get( Constants.RUN_INFO ), RunInfo.class ) : kartaRuntime.getDefaultRunInfo();
         TestFeature feature = objectMapper.convertValue( parameters.get( Constants.FEATURE ), TestFeature.class );

         if ( feature == null )
         {
            return StandardFeatureResults.error( featureName, "Feature to run missing in parameters" );
         }
         featureName = feature.getName();

         return kartaRuntime.runFeature( runInfo, feature );
      }
      catch ( Throwable t )
      {
         return StandardFeatureResults.error( featureName, t );
      }
   }
}
