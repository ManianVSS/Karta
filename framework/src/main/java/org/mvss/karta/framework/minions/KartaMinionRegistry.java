package org.mvss.karta.framework.minions;

import java.rmi.registry.Registry;
import java.util.HashMap;

import org.mvss.karta.framework.utils.RMIUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaMinionRegistry
{
   private HashMap<String, KartaMinion> minions = new HashMap<String, KartaMinion>();

   public boolean addMinion( String name, KartaMinionConfiguration minionConfiguration )
   {
      try
      {
         if ( !minions.containsKey( name ) )
         {
            Registry nodeRegistry = RMIUtils.getRemoteRegistry( minionConfiguration.getHost(), minionConfiguration.getPort(), minionConfiguration.isEnableSSL() );
            KartaMinion kartaMinion = (KartaMinion) nodeRegistry.lookup( KartaMinion.class.getName() );

            if ( kartaMinion.healthCheck() )
            {
               minions.put( name, kartaMinion );
            }
            return true;
         }
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to connect to minion " + minionConfiguration, t );
      }
      return false;
   }

   public boolean removeMinion( String name )
   {
      return minions.remove( name ) != null;
   }

   public KartaMinion getMinion( String name )
   {
      return minions.get( name );
   }
}
