package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
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
   public boolean runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                              int numberOfIterationsInParallel )
            throws RemoteException
   {
      return kartaRuntime.runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, feature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
   }

   @Override
   public StepResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, int iterationIndex, TestScenario testScenario, int scenarioIterationNumber ) throws RemoteException
   {
      return kartaRuntime.runTestScenario( stepRunnerPlugin, testDataSourcePlugins, runName, feature, iterationIndex, testScenario, scenarioIterationNumber );
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
