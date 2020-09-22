package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeConfiguration implements Serializable
{
   /**
    * 
    */
   private static final long                              serialVersionUID = 1L;

   private String                                         defaultFeatureSourceParserPlugin;

   private String                                         defaultStepRunnerPlugin;

   private HashSet<String>                                defaultTestDataSourcePlugins;

   private HashMap<String, HashMap<String, Serializable>> pluginConfiguration;

   private HashSet<String>                                propertyFiles;

   private HashSet<String>                                testRepositorydirectories;

   private HashSet<String>                                testCatalogFiles;
}
