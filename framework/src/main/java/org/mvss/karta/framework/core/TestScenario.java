package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.randomization.ObjectWithChance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder( toBuilder = true )
@AllArgsConstructor
@NoArgsConstructor
public class TestScenario implements Serializable, ObjectWithChance
{
   /**
    * 
    */
   private static final long   serialVersionUID = 1L;

   private String              name;

   private String              description;

   @Builder.Default
   private float               probability      = 1.0f;

   @Builder.Default
   private ArrayList<TestStep> setupSteps       = new ArrayList<TestStep>();

   private ChaosActionTreeNode chaosConfiguration;

   @Builder.Default
   private ArrayList<TestStep> executionSteps   = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep> tearDownSteps    = new ArrayList<TestStep>();
}
