package org.mvss.karta.framework.runtime;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.minions.KartaMinion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class RemoteFeatureRunner implements Callable<FeatureResult>
{
   private KartaRuntime    kartaRuntime;
   private String          stepRunner;
   private HashSet<String> testDataSources;
   private String          runName;
   private TestFeature     testFeature;

   @Builder.Default
   private long            numberOfIterations            = 1;
   @Builder.Default
   private int             numberOfIterationsInParallel  = 1;

   @Builder.Default
   private Boolean         chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean         exclusiveScenarioPerIteration = false;

   @Builder.Default
   private KartaMinion     minionToUse                   = null;

   private FeatureResult   result;

   @Override
   public FeatureResult call()
   {
      try
      {
         result = minionToUse.runFeature( stepRunner, testDataSources, runName, testFeature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         result.setSuccessful( false );
      }
      return result;
   }
}