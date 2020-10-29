package org.mvss.karta.framework.runtime.reports;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestFeature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunReport implements Serializable
{
   /**
    * 
    */
   private static final long                   serialVersionUID = 1L;

   // private RunTarget runTarget;

   @Builder.Default
   private HashMap<TestFeature, FeatureReport> featureReports   = new HashMap<TestFeature, FeatureReport>();

   public FeatureReport getOrCreateFeatureReport( TestFeature feature )
   {
      if ( featureReports == null )
      {
         featureReports = new HashMap<TestFeature, FeatureReport>();
      }

      FeatureReport featureReport = featureReports.get( feature );

      if ( featureReport == null )
      {
         featureReports.put( feature, featureReport = new FeatureReport() );
      }

      return featureReport;
   }
}
