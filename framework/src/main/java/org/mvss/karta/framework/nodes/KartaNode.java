package org.mvss.karta.framework.nodes;

import org.mvss.karta.framework.core.*;
import org.mvss.karta.framework.runtime.RunInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Remote interface for a Karta Node
 *
 * @author Manian
 */
public interface KartaNode extends Remote, AutoCloseable
{
   // TODO: Add passing minion name information to implement minion routing and gateway.
   // TODO: Events and incidents should be passed back to caller rather than being raised locally

   /**
    * Run a test feature
    */
   FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException;

   /**
    * Run a test job iteration
    */
   TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, int iterationIndex ) throws RemoteException;

   /**
    * Run a scenario iteration
    */
   ScenarioResult runTestScenario( RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario,
                                   long scenarioIterationNumber ) throws RemoteException;

   /**
    * Run a prepared test step.
    */
   StepResult runStep( RunInfo runInfo, PreparedStep testStep ) throws RemoteException;

   /**
    * Run a prepared chaos action.
    */
   StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException;

   /**
    * Perform health check
    */
   boolean healthCheck() throws RemoteException;
}
