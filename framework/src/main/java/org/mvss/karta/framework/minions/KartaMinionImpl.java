package org.mvss.karta.framework.minions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
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
   public StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException
   {
      try
      {
         return kartaRuntime.runStep( stepRunnerPlugin, step, context );
      }
      catch ( TestFailureException e )
      {
         return new StepResult( false, e.getMessage(), e, null );
      }
      catch ( Throwable e )
      {
         throw new RemoteException( e.getMessage() );
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
         return new StepResult( false, e.getMessage(), e, null );
      }
      catch ( Throwable e )
      {
         throw new RemoteException( e.getMessage() );
      }
   }

   @Override
   public boolean healthCheck() throws RemoteException
   {
      log.debug( "Health check ping" );
      return true;
   }

}
