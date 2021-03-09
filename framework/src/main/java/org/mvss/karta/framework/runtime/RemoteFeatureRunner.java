package org.mvss.karta.framework.runtime;

import java.util.concurrent.Callable;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.nodes.KartaNode;

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
@Builder
public class RemoteFeatureRunner implements Callable<FeatureResult>
{
   private RunInfo       runInfo;
   private TestFeature   testFeature;

   @Builder.Default
   private KartaNode     minionToUse = null;

   private FeatureResult result;

   @Override
   public FeatureResult call()
   {
      try
      {
         result = minionToUse.runFeature( runInfo, testFeature );
         result.processRemoteResults();
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         result.setSuccessful( false );
      }
      return result;
   }
}
