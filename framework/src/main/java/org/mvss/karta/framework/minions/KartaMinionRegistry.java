package org.mvss.karta.framework.minions;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.utils.RMIUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaMinionRegistry
{
   @Getter
   private ArrayList<KartaMinion>       minions            = new ArrayList<KartaMinion>();

   @Getter
   private HashMap<String, KartaMinion> nodes              = new HashMap<String, KartaMinion>();

   private volatile int                 lastMinonIndexUsed = -1;

   private Object                       lock               = new Object();

   public boolean addNode( String name, KartaMinionConfiguration minionConfiguration )
   {
      try
      {
         synchronized ( lock )
         {
            if ( !nodes.containsKey( name ) )
            {
               KartaMinion kartaNode = null;

               switch ( minionConfiguration.getNodeType() )
               {
                  case RMI:

                     Registry nodeRegistry = RMIUtils.getRemoteRegistry( minionConfiguration.getHost(), minionConfiguration.getPort(), minionConfiguration.isEnableSSL() );
                     kartaNode = (KartaMinion) nodeRegistry.lookup( KartaMinion.class.getName() );
                     break;

                  case REST:
                     String url = ( minionConfiguration.isEnableSSL() ? "https://" : "http//" ) + minionConfiguration.getHost() + ":" + minionConfiguration.getPort();
                     kartaNode = new KartaRestMinion( url, true );
                     break;

                  default:
                     break;
               }

               if ( kartaNode.healthCheck() )
               {
                  nodes.put( name, kartaNode );
               }
               else
               {
                  log.error( "Could not add node " + name + " due to health check failure" );
                  return false;
               }

               if ( minionConfiguration.isMinion() )
               {
                  minions.add( kartaNode );
                  log.info( "Added minion " + kartaNode );
               }

               log.info( "Added node " + name + " with config " + minionConfiguration );
               return true;
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to connect to node " + minionConfiguration, t );
      }

      return false;
   }

   public boolean removeNode( String name )
   {
      boolean nodeRemoved = false;

      synchronized ( lock )
      {
         KartaMinion node = nodes.remove( name );
         nodeRemoved = ( node != null );

         if ( nodeRemoved )
         {
            minions.remove( node );
         }
      }

      return nodeRemoved;
   }

   public KartaMinion getNode( String name )
   {
      synchronized ( lock )
      {
         return nodes.get( name );
      }
   }

   public KartaMinion getNextMinion()
   {
      synchronized ( lock )
      {
         int numberOfMinions = minions.size();

         if ( numberOfMinions == 0 )
         {
            return null;
         }
         else if ( numberOfMinions == 1 )
         {
            lastMinonIndexUsed = 0;
            return minions.get( 0 );
         }
         else
         {
            lastMinonIndexUsed = ( lastMinonIndexUsed + 1 ) % numberOfMinions;
            return minions.get( lastMinonIndexUsed );
         }
      }
   }
}
