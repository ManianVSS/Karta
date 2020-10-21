package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepResult implements Serializable
{
   /**
    * 
    */
   private static final long             serialVersionUID = 1L;

   @Builder.Default
   private boolean                       successsful      = false;

   private TestIncident                  incident;

   @Builder.Default
   private HashMap<String, Serializable> results          = new HashMap<String, Serializable>();

}
