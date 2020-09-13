package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunConfiguration
{
   @Builder.Default
   private String                        featureSourceParser      = "org.mvss.karta.framework.runtime.impl.YamlFeatureFileParser";
   private String                        featureSourceParserJarFile;
   @Builder.Default
   private HashMap<String, Serializable> featureParserProperties  = new HashMap<String, Serializable>();

   @Builder.Default
   private String                        stepRunner               = "org.mvss.karta.samples.runner.YerkinStepRunner";
   private String                        stepRunnerJarFile;
   @Builder.Default
   private HashMap<String, Serializable> stepRunnerProperties     = new HashMap<String, Serializable>();

   @Builder.Default
   private String                        testDataSource           = "org.mvss.karta.samples.runner.YerkinTestDataSource";
   private String                        testDataSourceJarFile;
   @Builder.Default
   private HashMap<String, Serializable> testDataSourceProperties = new HashMap<String, Serializable>();

   @Builder.Default
   private String                        featureFile              = "SampleFeatureFile.yaml";

   private String                        javaTest;
   private String                        javaTestJarFile;

}
