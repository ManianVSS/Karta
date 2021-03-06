package org.mvss.karta.framework.nodes;

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
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunInfo;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.TestJobRunner;

import lombok.extern.log4j.Log4j2;

/**
 * The default RMI based Karta node implementation
 * 
 * @author Manian
 */
@Log4j2
public class KartaNodeImpl extends UnicastRemoteObject implements KartaNode, Serializable
{
   private static final long      serialVersionUID = 1L;

   private transient KartaRuntime kartaRuntime;

   public KartaNodeImpl( KartaRuntime kartaRuntime ) throws RemoteException
   {
      this.kartaRuntime = kartaRuntime;
   }

   @Override
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException
   {
      return kartaRuntime.runFeature( runInfo, feature );
   }

   @Override
   public TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, long iterationIndex ) throws RemoteException
   {
      try
      {
         return TestJobRunner.run( kartaRuntime, runInfo, featureName, job, iterationIndex, null );
      }
      catch ( Throwable e )
      {
         throw new RemoteException( "Exception while running job iteration", e );
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
