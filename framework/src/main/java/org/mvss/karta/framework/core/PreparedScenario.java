package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@AllArgsConstructor
@NoArgsConstructor
public class PreparedScenario implements Serializable
{
   /**
    * 
    */
   private static final long              serialVersionUID = 1L;

   private String                         name;

   private String                         description;

   @Builder.Default
   private ArrayList<PreparedStep>        setupSteps       = new ArrayList<PreparedStep>();

   @Builder.Default
   private ArrayList<PreparedChaosAction> chaosActions     = new ArrayList<PreparedChaosAction>();

   @Builder.Default
   private ArrayList<PreparedStep>        executionSteps   = new ArrayList<PreparedStep>();

   @Builder.Default
   private ArrayList<PreparedStep>        tearDownSteps    = new ArrayList<PreparedStep>();
}
