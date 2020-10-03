package org.mvss.karta.framework.minions;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.RMIUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public class KartaMinionServer
{
   private KartaRuntime             kartaRuntime;
   private Registry                 rmiRegistry;
   private KartaMinion              kartaMinion;

   @PropertyMapping( "minion.config" )
   private KartaMinionConfiguration minionConfig = new KartaMinionConfiguration();

   public KartaMinionServer( KartaRuntime kartaRuntime ) throws IllegalArgumentException, IllegalAccessException, RemoteException, AlreadyBoundException
   {
      kartaRuntime.getConfigurator().loadProperties( this );

      rmiRegistry = RMIUtils.createNewRegistry( minionConfig.getHost(), minionConfig.getPort(), minionConfig.isEnableSSL() );

      kartaMinion = new KartaMinionImpl( kartaRuntime );

      rmiRegistry.bind( KartaMinion.class.getName(), kartaMinion );

      Runtime.getRuntime().addShutdownHook( new Thread()
      {
         public void run()
         {
            log.info( "Deregistering karta minion from RMI registry" );
            try
            {
               rmiRegistry.unbind( KartaMinion.class.getName() );
            }
            catch ( Throwable e )
            {
               log.error( e );
            }
         }
      } );
   }
}
