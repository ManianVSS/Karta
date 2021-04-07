package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.BeanRegistry;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.utils.DataUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes a prepared scenario iteration ready for execution.
 * Scenario preparation needed as it allows better remote scenario iteration execution.
 * Contains only prepared items like PreparedSteps and PreparedChaosActions.
 * Prepared items contain test execution context packing test data and variables and don't contain fields which aren't necessary for execution.
 * TestScenario is like a template and PreparedScenario is an iteration instance ready for execution built using the test scenario.
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@AllArgsConstructor
@NoArgsConstructor
public class PreparedScenario implements Serializable
{
   private static final long              serialVersionUID    = 1L;

   /**
    * The name of the scenario
    */
   @Builder.Default
   private String                         name                = null;

   /**
    * The scenario description
    */
   @Builder.Default
   private String                         description         = null;

   /**
    * The list of scenario specific prepared setup steps.
    */
   @Builder.Default
   private ArrayList<PreparedStep>        setupSteps          = new ArrayList<PreparedStep>();

   /**
    * The list of prepared chaos actions to run.
    */
   @Builder.Default
   private ArrayList<PreparedChaosAction> chaosActions        = new ArrayList<PreparedChaosAction>();

   /**
    * The list of prepared execution steps.
    */
   @Builder.Default
   private ArrayList<PreparedStep>        executionSteps      = new ArrayList<PreparedStep>();

   /**
    * The list of prepared tear-down steps.
    */
   @Builder.Default
   private ArrayList<PreparedStep>        tearDownSteps       = new ArrayList<PreparedStep>();

   /**
    * The bean registry for the scenario execution context.
    * This can be used for scenario specific setup and cleanup of resources.
    * Best example is like UI automation drivers which need to be initialized
    * at the possibly remote node before scenario starts and used in the scenario
    * and cleaned up at the remote node after the scenario ends.
    */
   @JsonIgnore
   @Builder.Default
   private transient BeanRegistry         contextBeanRegistry = null;

   /**
    * Creates a context bean registry with the Configurator provided and propagates
    * the bean registry to all the test execution contexts of prepared steps and chaos
    * actions. This method is to be called before running a scenario iteration at the
    * possibly remote node (after before scenario hooks are called) to initialize
    * resources for the scenario execution.
    * 
    * @param configurator
    */
   public void propogateContextBeanRegistry()
   {
      if ( contextBeanRegistry == null )
      {
         contextBeanRegistry = new BeanRegistry();
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

   public void normalizeVariables()
   {
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      for ( PreparedStep step : tearDownSteps )
      {
         TestExecutionContext testExecutionContext = step.getTestExecutionContext();
         DataUtils.mergeMapInto( testExecutionContext.getVariables(), variables );
         testExecutionContext.setVariables( variables );
      }

      for ( PreparedStep step : executionSteps )
      {
         TestExecutionContext testExecutionContext = step.getTestExecutionContext();
         DataUtils.mergeMapInto( testExecutionContext.getVariables(), variables );
         testExecutionContext.setVariables( variables );
      }

      for ( PreparedChaosAction chaosAction : chaosActions )
      {
         TestExecutionContext testExecutionContext = chaosAction.getTestExecutionContext();
         DataUtils.mergeMapInto( testExecutionContext.getVariables(), variables );
         testExecutionContext.setVariables( variables );
      }

      for ( PreparedStep step : setupSteps )
      {
         TestExecutionContext testExecutionContext = step.getTestExecutionContext();
         DataUtils.mergeMapInto( testExecutionContext.getVariables(), variables );
         testExecutionContext.setVariables( variables );
      }

   }
}
