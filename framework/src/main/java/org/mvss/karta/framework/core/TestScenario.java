package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.randomization.ObjectWithChance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestScenario implements Serializable, ObjectWithChance
{
   /**
    * 
    */
   private static final long   serialVersionUID       = 1L;

   private String              name;

   private Integer             probability;

   @Builder.Default
   private ArrayList<TestStep> scenarioSetupSteps     = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep> scenarioExecutionSteps = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep> scenarioTearDownSteps  = new ArrayList<TestStep>();

   @Override
   public int getProbabilityOfOccurrence()
   {
      return probability == null ? 100 : probability;
   }

   //
   // @Builder.Default
   // private HashSet<String> tags = new HashSet<String>();
}
