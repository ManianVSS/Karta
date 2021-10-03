package org.mvss.karta.framework.runtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.testcatalog.Test;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashSet;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder( toBuilder = true )
public class RunInfo implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String runName;

   private String release;

   private String build;

   private HashSet<String> tags;

   @Builder.Default
   private boolean remotelyCalled = false;

   private String featureSourceParserPlugin;

   private String stepRunnerPluginName;

   private HashSet<String> testDataSourcePlugins;

   @Builder.Default
   private boolean runAllScenarioParallely = false;

   @Builder.Default
   private boolean chanceBasedScenarioExecution = false;

   @Builder.Default
   private boolean exclusiveScenarioPerIteration = false;

   @Builder.Default
   private long numberOfIterations = 1;

   @JsonFormat( shape = Shape.STRING )
   private Duration runDuration;

   @JsonFormat( shape = Shape.STRING )
   private Duration coolDownBetweenIterations;

   @Builder.Default
   private long iterationsPerCoolDownPeriod = 1;

   @Builder.Default
   private int numberOfIterationsInParallel = 1;

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
      String featureSourceParserPlugin = test.getFeatureSourceParser();
      if ( StringUtils.isNotBlank( featureSourceParserPlugin ) )
      {
         this.featureSourceParserPlugin = featureSourceParserPlugin;
      }

      String stepRunnerPluginName = test.getStepRunner();
      if ( StringUtils.isNotBlank( stepRunnerPluginName ) )
      {
         this.stepRunnerPluginName = stepRunnerPluginName;
      }

      HashSet<String> testDataSourcePlugins = test.getTestDataSources();
      if ( ( testDataSourcePlugins != null ) && !testDataSourcePlugins.isEmpty() )
      {
         this.testDataSourcePlugins = testDataSourcePlugins;
      }
   }

   public RunInfo getRunInfoForTest( Test test )
   {
      RunInfo runInfo = this.toBuilder().tags( test.getTags() ).runAllScenarioParallely( test.getRunAllScenarioParallely() )
               .chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() )
               .exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() ).numberOfIterations( test.getNumberOfIterations() )
               .runDuration( test.getRunDuration() ).coolDownBetweenIterations( test.getCoolDownBetweenIterations() )
               .iterationsPerCoolDownPeriod( test.getIterationsPerCoolDownPeriod() ).numberOfIterationsInParallel( test.getNumberOfThreads() )
               .build();

      runInfo.setPlugins( test );

      return runInfo;
   }
}
