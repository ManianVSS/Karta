package org.mvss.karta.framework.nodes;

import org.mvss.karta.framework.enums.NodeType;
import lombok.*;

import java.io.Serializable;

/**
 * This class groups the node configuration for a Karta node
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KartaNodeConfiguration implements Serializable
{
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * Name of the Karta node. This ideally should indicate the role of the node if not a minion.
    */
   @Builder.Default
   private String name = "local";

   /**
    * The host name/IP for the node.
    */
   @Builder.Default
   private String host = "localhost";

   /**
    * The TCP/IP port for the node.
    */
   @Builder.Default
   private int port = 17171;

   /**
    * Indicates if SSL is to be used for connection.
    */
   @Builder.Default
   private boolean enableSSL = true;

   /**
    * Indicates if the node is a minion. Minions are used for load sharing to run feature iterations.
    */
   @Builder.Default
   private boolean minion = false;

   /**
    * The node type RMI or REST.
    */
   @Builder.Default
   private NodeType nodeType = NodeType.RMI;

   @Override
   public int hashCode()
   {
      final int prime  = 31;
      int       result = 1;
      result = prime * result + ( enableSSL ? 1231 : 1237 );
      result = prime * result + ( ( host == null ) ? 0 : host.hashCode() );
      result = prime * result + ( minion ? 1231 : 1237 );
      result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
      result = prime * result + ( ( nodeType == null ) ? 0 : nodeType.hashCode() );
      result = prime * result + port;
      return result;
   }

   @Override
   public boolean equals( Object obj )
   {
      if ( this == obj )
         return true;
      if ( obj == null )
         return false;
      if ( getClass() != obj.getClass() )
         return false;
      KartaNodeConfiguration other = (KartaNodeConfiguration) obj;
      if ( enableSSL != other.enableSSL )
         return false;
      if ( host == null )
      {
         if ( other.host != null )
            return false;
      }
      else if ( !host.equals( other.host ) )
         return false;
      if ( minion != other.minion )
         return false;
      if ( name == null )
      {
         if ( other.name != null )
            return false;
      }
      else if ( !name.equals( other.name ) )
         return false;
      if ( nodeType != other.nodeType )
         return false;
      return port == other.port;
   }

}
