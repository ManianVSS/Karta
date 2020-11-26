package org.mvss.karta.framework.minions;

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
import org.mvss.karta.framework.runtime.RunInfo;

public interface KartaMinion extends Remote
{
   // TODO: Add passing minion name information to implement minion routing and gateway.
   // TODO: Events and incidents should be passed back to caller rather than being raised locally
   FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws RemoteException;

   long scheduleJob( RunInfo runInfo, String featureName, TestJob job ) throws RemoteException;

   boolean deleteJob( Long jobId ) throws RemoteException;

   ScenarioResult runTestScenario( RunInfo runInfo, String featureName, long iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber ) throws RemoteException;

   StepResult runStep( RunInfo runInfo, PreparedStep testStep ) throws RemoteException;

   StepResult performChaosAction( RunInfo runInfo, PreparedChaosAction chaosAction ) throws RemoteException;

   boolean healthCheck() throws RemoteException;
}
