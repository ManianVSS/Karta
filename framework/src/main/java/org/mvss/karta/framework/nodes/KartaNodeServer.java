package org.mvss.karta.framework.nodes;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.RMIUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * This is the entry point for RMI based Karta node server.
 * 
 * @author Manian
 */
@Log4j2
@Getter
public class KartaNodeServer
{
   private KartaRuntime           kartaRuntime;
   private Registry               rmiRegistry;
   private KartaNode              kartaNode;

   private boolean                started    = false;

   @PropertyMapping( "node.config" )
   private KartaNodeConfiguration nodeConfig = new KartaNodeConfiguration();

   /**
    * Creates the node server object with the RMI server
    * 
    * @param kartaRuntime
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public KartaNodeServer( KartaRuntime kartaRuntime ) throws IllegalArgumentException, IllegalAccessException
   {
      this.kartaRuntime = kartaRuntime;
      kartaRuntime.initializeObject( this );
   }

   /**
    * Start the node server
    * 
    * @throws RemoteException
    * @throws AlreadyBoundException
    */
   public synchronized void startServer() throws RemoteException, AlreadyBoundException
   {
      if ( !started )
      {
         rmiRegistry = RMIUtils.createNewRegistry( nodeConfig.getHost(), nodeConfig.getPort(), nodeConfig.isEnableSSL() );

         kartaNode = new KartaNodeImpl( kartaRuntime );

         rmiRegistry.bind( KartaNode.class.getName(), kartaNode );

         Runtime.getRuntime().addShutdownHook( new Thread()
         {
            public void run()
            {
               log.info( "Deregistering karta node from RMI registry" );
               try
               {
                  rmiRegistry.unbind( KartaNode.class.getName() );
               }
               catch ( Throwable e )
               {
                  log.error( e );
               }
            }
         } );
         started = true;
      }
   }
}
