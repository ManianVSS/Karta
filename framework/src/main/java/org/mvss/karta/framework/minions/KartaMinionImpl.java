package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunInfo;
import org.mvss.karta.framework.runtime.TestFailureException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaMinionImpl extends UnicastRemoteObject implements KartaMinion, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private KartaRuntime      kartaRuntime;

   public KartaMinionImpl( KartaRuntime kartaRuntime ) throws RemoteException
   {
      this.kartaRuntime = kartaRuntime;
   }

   @Override
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException
   {
      return kartaRuntime.runFeature( runInfo, feature );
   }

   @Override
   public long scheduleJob( RunInfo runInfo, String featureName, TestJob job ) throws RemoteException
   {
      try
      {
         return kartaRuntime.scheduleJob( runInfo, featureName, job );
      }
      catch ( Throwable e )
      {
         throw new RemoteException( "Exception while scheduling job", e );
      }
   }

   @Override
   public boolean deleteJob( Long jobId ) throws RemoteException
   {
      try
      {
         return kartaRuntime.deleteJob( jobId );
      }
      catch ( Throwable e )
      {
         throw new RemoteException( "Exception while deleting job", e );
      }
   }

   @Override
   public ScenarioResult runTestScenario( RunInfo runInfo, String featureName, long iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber ) throws RemoteException
   {
      return kartaRuntime.runTestScenario( runInfo, featureName, iterationIndex, testScenario, scenarioIterationNumber );
   }

   @Override
   public StepResult runStep( RunInfo runInfo, PreparedStep step ) throws RemoteException
   {
      try
      {
         return kartaRuntime.runStep( runInfo, step );
      }
      catch ( TestFailureException e )
      {
         return StandardStepResults.failure( e );
      }
   }

   @Override
   public StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException
   {
      try
      {
         return kartaRuntime.runChaosAction( runInfo, chaosAction );
      }
      catch ( TestFailureException e )
      {
         return StandardStepResults.failure( e );
      }
   }

   @Override
   public boolean healthCheck() throws RemoteException
   {
      log.debug( "Health check ping" );
      return true;
   }
}
