package org.mvss.karta.framework.minions;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;

public interface KartaMinion extends Remote
{
   // TODO: Add passing minion name information to implement minion routing and gateway.
   // TODO: Events and incidents should be passed back to caller rather than being raised locally
   FeatureResult runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                             int numberOfIterationsInParallel )
            throws RemoteException;

   long scheduleJob( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, TestJob job ) throws RemoteException;

   boolean deleteJob( Long jobId ) throws RemoteException;

   ScenarioResult runTestScenario( String stepRunnerPlugin, String runName, String featureName, long iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber ) throws RemoteException;

   StepResult runStep( String stepRunnerPlugin, PreparedStep testStep ) throws RemoteException;

   StepResult performChaosAction( String stepRunnerPlugin, PreparedChaosAction chaosAction ) throws RemoteException;

   boolean healthCheck() throws RemoteException;
}
