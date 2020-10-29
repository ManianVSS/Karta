package org.mvss.karta.framework.minions;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestExecutionContext;

public interface KartaMinion extends Remote
{
   boolean runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                       int numberOfIterationsInParallel )
            throws RemoteException;

   StepResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, int iterationIndex, TestScenario testScenario, int scenarioIterationNumber ) throws RemoteException;

   StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException;

   StepResult performChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws RemoteException;

   boolean healthCheck() throws RemoteException;
}
