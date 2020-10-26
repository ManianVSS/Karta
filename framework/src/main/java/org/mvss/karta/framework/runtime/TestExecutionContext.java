package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestExecutionContext implements Serializable
{

   /**
    * 
    */
   private static final long                              serialVersionUID = 1L;

   private String                                         runName;
   private String                                         featureName;

   @Builder.Default
   private int                                            iterationIndex   = -1;

   private String                                         scenarioName;
   private String                                         stepIdentifier;

   @Builder.Default
   private HashMap<String, HashMap<String, Serializable>> properties       = null;

   @Builder.Default
   private HashMap<String, Serializable>                  data             = null;

   @Builder.Default
   private HashMap<String, Serializable>                  variables        = new HashMap<String, Serializable>();
}
