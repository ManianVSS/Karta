package org.mvss.karta.framework.runtime.reports;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestStep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioReport implements Serializable
{
   /**
    * 
    */
   private static final long                                  serialVersionUID  = 1L;

   @Builder.Default
   private HashMap<TestStep, HashMap<Integer, StepResult>>    setupReport       = new HashMap<TestStep, HashMap<Integer, StepResult>>();

   @Builder.Default
   private HashMap<ChaosAction, HashMap<Integer, StepResult>> chaosActionReport = new HashMap<ChaosAction, HashMap<Integer, StepResult>>();

   @Builder.Default
   private HashMap<TestStep, HashMap<Integer, StepResult>>    runReport         = new HashMap<TestStep, HashMap<Integer, StepResult>>();

   @Builder.Default
   private HashMap<TestStep, HashMap<Integer, StepResult>>    tearDownReport    = new HashMap<TestStep, HashMap<Integer, StepResult>>();

}
