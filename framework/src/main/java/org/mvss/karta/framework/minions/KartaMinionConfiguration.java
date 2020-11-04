package org.mvss.karta.framework.minions;

import java.io.Serializable;

import org.mvss.karta.framework.enums.NodeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KartaMinionConfiguration implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Builder.Default
   private String            host             = "localhost";

   @Builder.Default
   private int               port             = 17171;

   @Builder.Default
   private boolean           enableSSL        = true;

   @Builder.Default
   private boolean           minion           = false;

   @Builder.Default
   private NodeType          nodeType         = NodeType.RMI;
}
