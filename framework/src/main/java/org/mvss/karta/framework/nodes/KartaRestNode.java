package org.mvss.karta.framework.nodes;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.core.*;
import org.mvss.karta.framework.nodes.dto.*;
import org.mvss.karta.framework.restclient.*;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.RunInfo;

import java.rmi.RemoteException;

/**
 * Default REST based Karta Node implementation.
 * This class is only the REST Client
 * The server side is implemented in Karta Server project using matching REST API
 *
 * @author Manian
 */
@Log4j2
@Getter
public class KartaRestNode implements KartaNode
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
      FeatureRunInfo featureRunInfo = FeatureRunInfo.builder().runInfo( runInfo ).testFeature( feature ).build();

      FeatureResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( featureRunInfo ).build();
         try (RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_FEATURE ))
         {
            int statusCode = response.getStatusCode();
            if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
            {
               result = response.getBodyAs( FeatureResult.class );
            }
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
      JobIterationRunInfo jobIterationRunInfo = JobIterationRunInfo.builder().runInfo( runInfo ).featureName( featureName ).testJob( job )
               .iterationIndex( iterationIndex ).build();

      TestJobResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( jobIterationRunInfo ).build();
         try (RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_JOB_ITERATION ))
         {

            int statusCode = response.getStatusCode();
            if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
            {
               result = response.getBodyAs( TestJobResult.class );
            }
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
                                          long scenarioIterationNumber ) throws RemoteException
   {
      ScenarioRunInfo scenarioRunInfo = ScenarioRunInfo.builder().runInfo( runInfo ).featureName( featureName ).iterationIndex( iterationIndex )
               .preparedScenario( testScenario ).scenarioIterationNumber( scenarioIterationNumber ).build();

      ScenarioResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( scenarioRunInfo ).build();
         try (RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_SCENARIO ))
         {
            int statusCode = response.getStatusCode();
            if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
            {
               result = response.getBodyAs( ScenarioResult.class );
            }
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
      StepRunInfo stepRunInfo = StepRunInfo.builder().runInfo( runInfo ).preparedStep( step ).build();

      StepResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( stepRunInfo ).build();
         try (RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_STEP ))
         {
            int statusCode = response.getStatusCode();
            if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
            {
               result = response.getBodyAs( StepResult.class );
            }
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
      ChaosActionRunInfo chaosActionRunInfo = ChaosActionRunInfo.builder().runInfo( runInfo ).preparedChaosAction( chaosAction ).build();

      StepResult result = null;

      try
      {
         RestRequest restRequest = ApacheRestRequest.requestBuilder().header( Constants.ACCEPT, Constants.APPLICATION_JSON )
                  .contentType( ContentType.APPLICATION_JSON ).body( chaosActionRunInfo ).build();
         try (RestResponse response = restClient.post( restRequest, Constants.PATH_RUN_CHAOS_ACTION ))
         {
            int statusCode = response.getStatusCode();
            if ( ( statusCode == 200 ) || ( statusCode == 201 ) )
            {
               result = response.getBodyAs( StepResult.class );
            }
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

         try (RestResponse response = restClient.get( restRequest, Constants.PATH_HEALTH ))
         {
            int statusCode = response.getStatusCode();
            return ( statusCode == 200 ) ? response.getBodyAs( Boolean.class ) : false;
         }
      }
      catch ( Exception e )
      {
         log.error( "", e );
         return false;
      }
   }
}
