package org.mvss.karta.framework.nodes;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.RunInfo;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import lombok.Getter;

/**
 * Default REST based Karta Node implementation.
 * This class is only the REST Client
 * The server side is implemented in Karta Server project using matching REST API
 * 
 * @author Manian
 */
@Getter
public class KartaRestNode implements KartaNode
{
   private RequestSpecBuilder requestSpecBuilder;

   public KartaRestNode( String url )
   {
      requestSpecBuilder = new RequestSpecBuilder();
      requestSpecBuilder.setBaseUri( url );

      requestSpecBuilder.addHeader( Constants.CONTENT_TYPE, Constants.APPLICATION_JSON );
      requestSpecBuilder.addHeader( Constants.ACCEPT, Constants.APPLICATION_JSON );
   }

   public KartaRestNode( String url, boolean disableCertificateCheck )
   {
      this( url );
      if ( disableCertificateCheck )
      {
         requestSpecBuilder.setRelaxedHTTPSValidation();
      }
   }

   @Override
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE, feature );

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
   public TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, long iterationIndex ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.JOB, job );
      parameters.put( Constants.ITERATION_INDEX, iterationIndex );

      Response response = RestAssured.given( requestSpecBuilder.build() ).body( parameters ).post( Constants.PATH_RUN_JOB_ITERATION );

      TestJobResult result = null;
      int statusCode = response.getStatusCode();
      if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
      {
         result = response.getBody().as( TestJobResult.class );
      }
      return result;
   }

   @Override
   public ScenarioResult runTestScenario( RunInfo runInfo, String featureName, long iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.ITERATION_INDEX, iterationIndex );
      parameters.put( Constants.TEST_SCENARIO, testScenario );
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
   public StepResult runStep( RunInfo runInfo, PreparedStep step ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.TEST_STEP, step );

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
   public StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.CHAOS_ACTION, chaosAction );

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
