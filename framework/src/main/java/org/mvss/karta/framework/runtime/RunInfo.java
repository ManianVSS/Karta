package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.testcatalog.Test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder( toBuilder = true )
public class RunInfo implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID              = 1L;

   private String            runName;

   private HashSet<String>   tags;

   @Builder.Default
   private boolean           remotelyCalled                = false;

   private String            featureSourceParserPlugin;

   private String            stepRunnerPluginName;

   private HashSet<String>   testDataSourcePlugins;

   @Builder.Default
   private boolean           chanceBasedScenarioExecution  = false;

   @Builder.Default
   private boolean           exclusiveScenarioPerIteration = false;

   @Builder.Default
   private long              numberOfIterations            = 1;

   @Builder.Default
   private int               numberOfIterationsInParallel  = 1;

   public void setDefaultPlugins( String featureSourceParserPlugin, String stepRunnerPluginName, HashSet<String> testDataSourcePlugins )
   {
      if ( StringUtils.isBlank( this.featureSourceParserPlugin ) && StringUtils.isNotBlank( featureSourceParserPlugin ) )
      {
         this.featureSourceParserPlugin = featureSourceParserPlugin;
      }

      if ( StringUtils.isBlank( this.stepRunnerPluginName ) && StringUtils.isNotBlank( stepRunnerPluginName ) )
      {
         this.stepRunnerPluginName = stepRunnerPluginName;
      }

      if ( ( testDataSourcePlugins != null ) && !testDataSourcePlugins.isEmpty() )
      {
         if ( ( this.testDataSourcePlugins == null ) || this.testDataSourcePlugins.isEmpty() )
         {
            this.testDataSourcePlugins = testDataSourcePlugins;
         }
      }
   }

   public void setPlugins( Test test )
   {
      String featureSourceParserPlugin = test.getFeatureSourceParserPlugin();
      if ( StringUtils.isNotBlank( featureSourceParserPlugin ) )
      {
         this.featureSourceParserPlugin = featureSourceParserPlugin;
      }

      String stepRunnerPluginName = test.getStepRunnerPlugin();
      if ( StringUtils.isNotBlank( stepRunnerPluginName ) )
      {
         this.stepRunnerPluginName = stepRunnerPluginName;
      }

      HashSet<String> testDataSourcePlugins = test.getTestDataSourcePlugins();
      if ( ( testDataSourcePlugins != null ) && !testDataSourcePlugins.isEmpty() )
      {
         this.testDataSourcePlugins = testDataSourcePlugins;
      }
   }

   public RunInfo getRunInfoForTest( Test test )
   {
      RunInfo runInfo = this.toBuilder().tags( test.getTags() ).chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() ).exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() )
               .numberOfIterations( test.getNumberOfIterations() ).numberOfIterationsInParallel( test.getNumberOfThreads() ).build();

      runInfo.setPlugins( test );

      return runInfo;
   }
}
