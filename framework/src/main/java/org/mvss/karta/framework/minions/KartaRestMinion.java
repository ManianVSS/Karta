package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.TestExecutionContext;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import lombok.Getter;

@Getter
public class KartaRestMinion implements KartaMinion
{
   private RequestSpecBuilder requestSpecBuilder;

   public KartaRestMinion( String url )
   {
      requestSpecBuilder = new RequestSpecBuilder();
      requestSpecBuilder.setBaseUri( url );

      requestSpecBuilder.addHeader( Constants.CONTENT_TYPE, Constants.APPLICATION_JSON );
      requestSpecBuilder.addHeader( Constants.ACCEPT, Constants.APPLICATION_JSON );
   }

   public KartaRestMinion( String url, boolean disableCertificateCheck )
   {
      this( url );
      if ( disableCertificateCheck )
      {
         requestSpecBuilder.setRelaxedHTTPSValidation();
      }
   }

   @Override
   public FeatureResult runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                                    int numberOfIterationsInParallel )
            throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.STEP_RUNNER_PLUGIN, stepRunnerPlugin );
      parameters.put( Constants.TEST_DATA_SOURCE_PLUGINS, testDataSourcePlugins );
      parameters.put( Constants.RUN_NAME, runName );
      parameters.put( Constants.FEATURE, feature );
      parameters.put( Constants.CHANCE_BASED_SCENARIO_EXECUTION, chanceBasedScenarioExecution );
      parameters.put( Constants.EXCLUSIVE_SCENARIO_PER_ITERATION, exclusiveScenarioPerIteration );
      parameters.put( Constants.NUMBER_OF_ITERATIONS, numberOfIterations );
      parameters.put( Constants.NUMBER_OF_ITERATIONS_IN_PARALLEL, numberOfIterationsInParallel );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_FEATURE );

      FeatureResult result = null;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( FeatureResult.class );
      }
      return result;
   }

   @Override
   public long scheduleJob( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, TestJob job ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.STEP_RUNNER_PLUGIN, stepRunnerPlugin );
      parameters.put( Constants.TEST_DATA_SOURCE_PLUGINS, testDataSourcePlugins );
      parameters.put( Constants.RUN_NAME, runName );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.JOB, job );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_JOB );

      long result = -1;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( Long.class );
      }
      return result;
   }

   @Override
   public boolean deleteJob( Long jobId ) throws RemoteException
   {
      Response response = RestAssured.given( requestSpecBuilder.build() ).delete( Constants.PATH_RUN_JOB + Constants.SLASH + jobId );

      boolean result = false;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( Boolean.class );
      }
      return result;
   }

   @Override
   public ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, long iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, long scenarioIterationNumber )
            throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.STEP_RUNNER_PLUGIN, stepRunnerPlugin );
      parameters.put( Constants.TEST_DATA_SOURCE_PLUGINS, testDataSourcePlugins );
      parameters.put( Constants.RUN_NAME, runName );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.ITERATION_INDEX, iterationIndex );
      parameters.put( Constants.SCENARIO_SETUP_STEPS, scenarioSetupSteps );
      parameters.put( Constants.TEST_SCENARIO, testScenario );
      parameters.put( Constants.SCENARIO_TEAR_DOWN_STEPS, scenarioTearDownSteps );
      parameters.put( Constants.SCENARIO_ITERATION_NUMBER, scenarioIterationNumber );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_SCENARIO );

      ScenarioResult result = null;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( ScenarioResult.class );
      }
      return result;
   }

   @Override
   public StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext testExecutionContext ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.STEP_RUNNER_PLUGIN, stepRunnerPlugin );
      parameters.put( Constants.TEST_STEP, step );
      parameters.put( Constants.TEST_EXECUTION_CONTEXT, testExecutionContext );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_STEP );

      StepResult result = null;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( StepResult.class );
      }
      return result;
   }

   @Override
   public StepResult performChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.STEP_RUNNER_PLUGIN, stepRunnerPlugin );
      parameters.put( Constants.CHAOS_ACTION, chaosAction );
      parameters.put( Constants.TEST_EXECUTION_CONTEXT, testExecutionContext );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_CHAOS_ACTION );

      StepResult result = null;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( StepResult.class );
      }
      return result;
   }

   @Override
   public boolean healthCheck() throws RemoteException
   {
      Response response = RestAssured.given( requestSpecBuilder.build() ).get( Constants.PATH_HEALTH );
      int statusCode = response.getStatusCode();
      return ( statusCode == 200 ) ? response.as( Boolean.class ) : false;
   }
}
