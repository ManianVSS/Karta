package org.mvss.karta.framework.nodes;

import java.io.Serializable;

import org.mvss.karta.framework.enums.NodeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
   private String            name             = "local";

   /**
    * The host name/IP for the node.
    */
   @Builder.Default
   private String            host             = "localhost";

   /**
    * The TCP/IP port for the node.
    */
   @Builder.Default
   private int               port             = 17171;

   /**
    * Indicates if SSL is to be used for connection.
    */
   @Builder.Default
   private boolean           enableSSL        = true;

   /**
    * Indicates if the node is a minion. Minions are used for load sharing to run feature iterations.
    */
   @Builder.Default
   private boolean           minion           = false;

   /**
    * The node type RMI or REST.
    */
   @Builder.Default
   private NodeType          nodeType         = NodeType.RMI;

}
