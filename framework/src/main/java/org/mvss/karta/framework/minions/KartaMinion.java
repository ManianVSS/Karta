package org.mvss.karta.framework.minions;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestExecutionContext;

public interface KartaMinion extends Remote
{
   // TODO: Add passing minion name information to implement minion routing and gateway.
   // TODO: Events and incidents should be passed back to caller rather than being raised locally
   FeatureResult runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                             int numberOfIterationsInParallel )
            throws RemoteException;

   long scheduleJob( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, TestJob job ) throws RemoteException;

   boolean deleteJob( Long jobId ) throws RemoteException;

   ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, long iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                   ArrayList<TestStep> scenarioTearDownSteps, long scenarioIterationNumber )
            throws RemoteException;

   StepResult runStep( String stepRunnerPlugin, TestStep testStep, TestExecutionContext testExecutionContext ) throws RemoteException;

   StepResult performChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws RemoteException;

   boolean healthCheck() throws RemoteException;
}
