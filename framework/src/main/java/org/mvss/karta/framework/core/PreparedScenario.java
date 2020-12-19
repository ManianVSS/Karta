package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.runtime.BeanRegistry;
import org.mvss.karta.framework.runtime.Configurator;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

   @JsonIgnore
   private transient BeanRegistry         contextBeanRegistry;

   public void propogateContextBeanRegistry( Configurator configurator )
   {
      if ( contextBeanRegistry == null )
      {
         contextBeanRegistry = new BeanRegistry( configurator );
      }

      for ( PreparedStep step : setupSteps )
      {
         step.getTestExecutionContext().setContextBeanRegistry( contextBeanRegistry );
      }

      for ( PreparedChaosAction chaosAction : chaosActions )
      {
         chaosAction.getTestExecutionContext().setContextBeanRegistry( contextBeanRegistry );
      }

      for ( PreparedStep step : executionSteps )
      {
         step.getTestExecutionContext().setContextBeanRegistry( contextBeanRegistry );
      }

      for ( PreparedStep step : tearDownSteps )
      {
         step.getTestExecutionContext().setContextBeanRegistry( contextBeanRegistry );
      }
   }
}
