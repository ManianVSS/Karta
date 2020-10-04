package org.mvss.karta.framework.minions;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestExecutionContext;

public interface KartaMinion extends Remote
{
   StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException;

   StepResult performChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws RemoteException;

   boolean healthCheck() throws RemoteException;
}
