package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestScenario implements Serializable
{
   /**
    * 
    */
   private static final long   serialVersionUID        = 1L;

   private String              scenarioReference;
   private int                 probabilityOfOccurrence = 100;

   private ArrayList<TestStep> scenarioSetupSteps      = new ArrayList<TestStep>();
   private ArrayList<TestStep> scenarioExecutionSteps  = new ArrayList<TestStep>();
   private ArrayList<TestStep> scenarioTearDownSteps   = new ArrayList<TestStep>();
}
