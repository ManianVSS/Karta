package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunTarget;
import org.mvss.karta.framework.runtime.impl.DataFilesTestDataSourcePlugin;
import org.mvss.karta.framework.runtime.impl.YerkinPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunController
{
   private static final String RUNURL                 = "/api/run";
   private static final String FEATURE_RUN_URL        = RUNURL + "Feature";
   private static final String FEATURE_STRING_RUN_URL = RUNURL + "FeatureSource";
   private static final String RUN_TARGET_PATH        = RUNURL + "Target";

   @Autowired
   private KartaRuntime        kartaRuntime;

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = RUN_TARGET_PATH )
   public boolean startFeatureFileRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = YerkinPlugin.PLUGIN_NAME ) String pluginName, @RequestBody RunTarget runTarget )
            throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + "-" + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSourcePlugin.PLUGIN_NAME );
      return kartaRuntime.runTestTarget( runName, pluginName, pluginName, testDataSourcePluginHashSet, runTarget );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_STRING_RUN_URL )
   public boolean startFeatureSourceRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = YerkinPlugin.PLUGIN_NAME ) String pluginName, @RequestBody String featureSourceString )
            throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + "-" + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSourcePlugin.PLUGIN_NAME );
      return kartaRuntime.runFeatureSource( runName, pluginName, pluginName, testDataSourcePluginHashSet, featureSourceString, 1, 1 );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_RUN_URL )
   public boolean startFeatureRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = YerkinPlugin.PLUGIN_NAME ) String pluginName, @RequestBody TestFeature feature )
            throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + "-" + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSourcePlugin.PLUGIN_NAME );
      return kartaRuntime.run( runName, pluginName, testDataSourcePluginHashSet, feature, 1, 1 );
   }
}
