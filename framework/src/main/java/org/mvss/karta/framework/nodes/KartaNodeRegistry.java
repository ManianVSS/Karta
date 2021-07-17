package org.mvss.karta.framework.nodes;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.RMIUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * This registry class can help register Karta nodes and minions and cycle between minions for load sharing.
 * 
 * @author Manian
 */
@Log4j2
public class KartaNodeRegistry implements AutoCloseable
{
   @Getter
   private ArrayList<KartaNode>       minions            = new ArrayList<KartaNode>();

   @Getter
   private HashMap<String, KartaNode> nodes              = new HashMap<String, KartaNode>();

   private volatile int               lastMinonIndexUsed = -1;

   private Object                     lock               = new Object();

   @Override
   public void close() throws Exception
   {
      if ( this.nodes != null )
      {
         for ( KartaNode node : nodes.values() )
         {
            node.close();
         }
         this.nodes.clear();
      }

   }

   /**
    * Add a karta node/minion by configuration
    * 
    * @param nodeConfiguration
    * @return
    */
   public boolean addNode( KartaNodeConfiguration nodeConfiguration )
   {
      try
      {
         synchronized ( lock )
         {
            String name = nodeConfiguration.getName();
            if ( !nodes.containsKey( name ) )
            {
               KartaNode kartaNode = null;

               switch ( nodeConfiguration.getNodeType() )
               {
                  // TODO: Handle local node
                  case RMI:
                     Registry nodeRegistry = RMIUtils
                              .getRemoteRegistry( nodeConfiguration.getHost(), nodeConfiguration.getPort(), nodeConfiguration.isEnableSSL() );
                     kartaNode = (KartaNode) nodeRegistry.lookup( KartaNode.class.getName() );
                     break;

                  case REST:
                     String url = ( nodeConfiguration.isEnableSSL() ? Constants.HTTPS : Constants.HTTP ) + nodeConfiguration.getHost()
                                  + Constants.COLON + nodeConfiguration.getPort();
                     kartaNode = new KartaRestNode( url, true );
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

               if ( nodeConfiguration.isMinion() )
               {
                  minions.add( kartaNode );
                  log.info( "Added minion " + kartaNode );
               }

               log.info( "Added node " + name + " with config " + nodeConfiguration );
               return true;
            }
         }
      }
      catch ( RemoteException ce )
      {
         log.error( "Connection could not be estabished to node " + nodeConfiguration );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to connect to node " + nodeConfiguration, t );
      }

      return false;
   }

   /**
    * Remove a karta name from registry by name.
    * 
    * @param name
    * @return
    */
   public boolean removeNode( String name )
   {
      boolean nodeRemoved = false;

      synchronized ( lock )
      {
         KartaNode node = nodes.remove( name );
         nodeRemoved = ( node != null );

         if ( nodeRemoved )
         {
            minions.remove( node );
         }
      }

      return nodeRemoved;
   }

   /**
    * Get a karta node in registry by name.
    * 
    * @param name
    * @return
    */
   public KartaNode getNode( String name )
   {
      synchronized ( lock )
      {
         return nodes.get( name );
      }
   }

   /**
    * Gets the next minion to use by cycling through the list of minions.
    * 
    * @return
    */
   public KartaNode getNextMinion()
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
