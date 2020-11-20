package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestExecutionContext;
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
   public FeatureResult runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                                    int numberOfIterationsInParallel )
            throws RemoteException
   {
      return kartaRuntime.runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, feature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
   }

   @Override
   public long scheduleJob( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, TestJob job ) throws RemoteException
   {
      try
      {
         return kartaRuntime.scheduleJob( stepRunnerPlugin, testDataSourcePlugins, runName, featureName, job );
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
   public ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, long iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, long scenarioIterationNumber )
            throws RemoteException
   {
      return kartaRuntime.runTestScenario( stepRunnerPlugin, testDataSourcePlugins, runName, featureName, iterationIndex, scenarioSetupSteps, testScenario, scenarioTearDownSteps, scenarioIterationNumber );
   }

   @Override
   public StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException
   {
      try
      {
         return kartaRuntime.runStep( stepRunnerPlugin, step, context );
      }
      catch ( TestFailureException e )
      {
         return StandardStepResults.failure( e );
      }
   }

   @Override
   public StepResult performChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws RemoteException
   {
      try
      {
         return kartaRuntime.runChaosAction( stepRunnerPlugin, chaosAction, testExecutionContext );
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
