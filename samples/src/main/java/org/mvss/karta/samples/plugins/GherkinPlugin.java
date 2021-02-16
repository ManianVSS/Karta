package org.mvss.karta.samples.plugins;

import java.util.Arrays;
import java.util.List;

import org.mvss.karta.framework.core.Initializer;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.samples.utils.GherkinUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GherkinPlugin implements FeatureSourceParser
{
   public static final String       PLUGIN_NAME  = "Gherkin";

   public static final List<String> conjunctions = Arrays.asList( "Given ", "When ", "Then ", "And ", "But " );

   private boolean                  initialized  = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Initializer
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }
      log.info( "Initializing " + PLUGIN_NAME + " plugin" );

      return true;
   }

   @Override
   public TestFeature parseFeatureSource( String sourceCode ) throws Throwable
   {
      TestFeature feature = GherkinUtils.parseFeatureSource( sourceCode, null );
      return feature;
   }
}
