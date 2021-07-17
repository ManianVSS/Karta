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
import org.mvss.karta.framework.restclient.ApacheRestClient;
import org.mvss.karta.framework.restclient.ApacheRestRequest;
import org.mvss.karta.framework.restclient.ContentType;
import org.mvss.karta.framework.restclient.RestRequest;
import org.mvss.karta.framework.restclient.RestResponse;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.RunInfo;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Default REST based Karta Node implementation.
 * This class is only the REST Client
 * The server side is implemented in Karta Server project using matching REST API
 * 
 * @author Manian
 */
@Log4j2
@Getter
public class KartaRestNode implements KartaNode, AutoCloseable
{
   private ApacheRestClient restClient;

   public KartaRestNode( String url )
   {
      this.restClient = new ApacheRestClient( url, true );
   }

   public KartaRestNode( String url, boolean disableCertificateCheck )
   {
      this.restClient = new ApacheRestClient( url, disableCertificateCheck );
   }

   @Override
   public void close() throws Exception
   {
      if ( this.restClient != null )
      {
         this.restClient.close();
         this.restClient = null;
      }
   }

   @Override
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE, feature );

      FeatureResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( parameters ).build();
         RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_FEATURE );

         int statusCode = response.getStatusCode();
         if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
         {
            result = response.getBodyAs( FeatureResult.class );
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return result;
      }

      return result;
   }

   @Override
   public TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, int iterationIndex ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.JOB, job );
      parameters.put( Constants.ITERATION_INDEX, iterationIndex );

      TestJobResult result = null;

      try
      {

         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( parameters ).build();
         RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_JOB_ITERATION );

         int statusCode = response.getStatusCode();
         if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
         {
            result = response.getBodyAs( TestJobResult.class );
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return result;
      }

      return result;
   }

   @Override
   public ScenarioResult runTestScenario( RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario,
                                          long scenarioIterationNumber )
            throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.FEATURE_NAME, featureName );
      parameters.put( Constants.ITERATION_INDEX, iterationIndex );
      parameters.put( Constants.TEST_SCENARIO, testScenario );
      parameters.put( Constants.SCENARIO_ITERATION_NUMBER, scenarioIterationNumber );

      ScenarioResult result = null;

      try
      {

         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( parameters ).build();
         RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_SCENARIO );

         int statusCode = response.getStatusCode();
         if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
         {
            result = response.getBodyAs( ScenarioResult.class );
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return result;
      }

      return result;
   }

   @Override
   public StepResult runStep( RunInfo runInfo, PreparedStep step ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.TEST_STEP, step );

      StepResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( parameters ).build();
         RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_STEP );

         int statusCode = response.getStatusCode();
         if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
         {
            result = response.getBodyAs( StepResult.class );
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return result;
      }
      return result;
   }

   @Override
   public StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException
   {
      HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
      parameters.put( Constants.RUN_INFO, runInfo );
      parameters.put( Constants.CHAOS_ACTION, chaosAction );

      StepResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( parameters ).build();
         RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_CHAOS_ACTION );

         int statusCode = response.getStatusCode();
         if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
         {
            result = response.getBodyAs( StepResult.class );
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return result;
      }
      return result;
   }

   @Override
   public boolean healthCheck() throws RemoteException
   {
      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).build();

         RestResponse response = restClient.get( restRequest, Constants.PATH_HEALTH );
         int statusCode = response.getStatusCode();
         return ( statusCode == 200 ) ? response.getBodyAs( Boolean.class ) : false;
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return false;
      }
   }

}
