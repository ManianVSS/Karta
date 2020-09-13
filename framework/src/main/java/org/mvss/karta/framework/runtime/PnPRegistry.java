package org.mvss.karta.framework.runtime;

import java.util.HashMap;

import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.configuration.PluginClassConfig;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

public class PnPRegistry
{
   HashMap<String, FeatureSourceParser> featureSourceParserRegistry = new HashMap<String, FeatureSourceParser>();
   HashMap<String, StepRunner>          stepRunnerRegistry          = new HashMap<String, StepRunner>();
   HashMap<String, TestDataSource>      testDataSourceRegistry      = new HashMap<String, TestDataSource>();

   // private static KartaConfiguration kartaConfiguration = null;

   // static
   // {
   // kartaConfiguration = new KartaConfiguration( ( new HashMap<String, PluginClassConfig>() ), ( new HashMap<String, PluginClassConfig>() ), ( new HashMap<String, PluginClassConfig>() ) );
   // }

   public PnPRegistry( KartaConfiguration kartaConfiguration )
   {
      HashMap<String, PluginClassConfig> featureSourceParserMap = kartaConfiguration.getFeatureSourceParserConfig();

      if ( featureSourceParserMap != null )
      {
         for ( String featureSourcePluginName : featureSourceParserMap.keySet() )
         {
            PluginClassConfig featureSourcePlugin = featureSourceParserMap.get( featureSourcePluginName );
            // featureSourceParserRegistry.put( featureSourcePluginName, );
         }
      }
   }

   public FeatureSourceParser getFeatureSourceParser( String name )
   {
      return featureSourceParserRegistry.get( name );
   }

   public StepRunner getStepRunner( String name )
   {
      return stepRunnerRegistry.get( name );
   }

   public TestDataSource getTestDataSource( String name )
   {
      return testDataSourceRegistry.get( name );
   }

}
