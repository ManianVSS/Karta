package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestScenario implements Serializable
{
   /**
    * 
    */
   private static final long   serialVersionUID        = 1L;

   private String              name;

   @Builder.Default
   private Integer             probabilityOfOccurrence = 100;

   @Builder.Default
   private Long                numberOfIterations      = 1l;

   @Builder.Default
   private ArrayList<String>   tags                    = new ArrayList<String>();

   @Builder.Default
   private ArrayList<TestStep> scenarioSetupSteps      = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep> scenarioExecutionSteps  = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep> scenarioTearDownSteps   = new ArrayList<TestStep>();
}
