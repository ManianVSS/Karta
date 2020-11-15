package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
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
   public boolean runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                              int numberOfIterationsInParallel )
            throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( "stepRunnerPlugin", stepRunnerPlugin );
      parameters.put( "testDataSourcePlugins", testDataSourcePlugins );
      parameters.put( "runName", runName );
      parameters.put( "feature", feature );
      parameters.put( "chanceBasedScenarioExecution", chanceBasedScenarioExecution );
      parameters.put( "exclusiveScenarioPerIteration", exclusiveScenarioPerIteration );
      parameters.put( "numberOfIterations", numberOfIterations );
      parameters.put( "numberOfIterationsInParallel", numberOfIterationsInParallel );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_FEATURE );

      int statusCode = response.getStatusCode();
      return ( ( statusCode == 200 ) || ( statusCode == 201 ) );
   }

   @Override
   public ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, int iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, int scenarioIterationNumber )
            throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( "stepRunnerPlugin", stepRunnerPlugin );
      parameters.put( "testDataSourcePlugins", testDataSourcePlugins );
      parameters.put( "runName", runName );
      parameters.put( "featureName", featureName );
      parameters.put( "iterationIndex", iterationIndex );
      parameters.put( "scenarioSetupSteps", scenarioSetupSteps );
      parameters.put( "testScenario", testScenario );
      parameters.put( "scenarioTearDownSteps", scenarioTearDownSteps );
      parameters.put( "scenarioIterationNumber", scenarioIterationNumber );

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
      parameters.put( "stepRunnerPlugin", stepRunnerPlugin );
      parameters.put( "testStep", step );
      parameters.put( "testExecutionContext", testExecutionContext );

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
      parameters.put( "stepRunnerPlugin", stepRunnerPlugin );
      parameters.put( "chaosAction", chaosAction );
      parameters.put( "testExecutionContext", testExecutionContext );

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
