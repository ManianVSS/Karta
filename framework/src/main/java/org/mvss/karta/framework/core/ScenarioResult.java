package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mvss.karta.framework.chaos.ChaosAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResult implements Serializable, Comparable<ScenarioResult>
{
   /**
    * 
    */
   private static final long                serialVersionUID   = 1L;

   @Builder.Default
   private int                              iterationIndex     = 0;

   @Builder.Default
   private Date                             startTime          = new Date();

   private Date                             endTime;

   @Builder.Default
   private boolean                          successsful        = true;

   @Builder.Default
   private boolean                          error              = false;

   @Builder.Default
   private ArrayList<TestIncident>          incidents          = new ArrayList<TestIncident>();

   @Builder.Default
   private HashMap<TestStep, StepResult>    setupResults       = new HashMap<TestStep, StepResult>();

   @Builder.Default
   private HashMap<ChaosAction, StepResult> chaosActionResults = new HashMap<ChaosAction, StepResult>();

   @Builder.Default
   private HashMap<TestStep, StepResult>    runResults         = new HashMap<TestStep, StepResult>();

   @Builder.Default
   private HashMap<TestStep, StepResult>    tearDownResults    = new HashMap<TestStep, StepResult>();

   @Override
   public int compareTo( ScenarioResult other )
   {
      return iterationIndex - other.iterationIndex;
   }
}
