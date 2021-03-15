package org.mvss.karta.framework.nodes;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.runtime.RunInfo;

/**
 * The Remote interface for a Karta Node
 * 
 * @author Manian
 */
public interface KartaNode extends Remote
{
   // TODO: Add passing minion name information to implement minion routing and gateway.
   // TODO: Events and incidents should be passed back to caller rather than being raised locally
   /**
    * Run a test feature
    * 
    * @param runInfo
    * @param feature
    * @return
    * @throws RemoteException
    */
   FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException;

   /**
    * Run a test job iteration
    * 
    * @param runInfo
    * @param featureName
    * @param job
    * @param iterationIndex
    * @return
    * @throws RemoteException
    */
   TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, int iterationIndex ) throws RemoteException;

   /**
    * Run a scenario iteration
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param testScenario
    * @param scenarioIterationNumber
    * @return
    * @throws RemoteException
    */
   ScenarioResult runTestScenario( RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber ) throws RemoteException;

   /**
    * Run a prepared test step.
    * 
    * @param runInfo
    * @param testStep
    * @return
    * @throws RemoteException
    */
   StepResult runStep( RunInfo runInfo, PreparedStep testStep ) throws RemoteException;

   /**
    * Run a prepared chaos action.
    * 
    * @param runInfo
    * @param chaosAction
    * @return
    * @throws RemoteException
    */
   StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException;

   /**
    * Perform health check
    * 
    * @return
    * @throws RemoteException
    */
   boolean healthCheck() throws RemoteException;
}
