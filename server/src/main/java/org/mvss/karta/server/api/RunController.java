package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunTarget;
import org.mvss.karta.framework.runtime.impl.DataFilesTestDataSource;
import org.mvss.karta.framework.runtime.impl.KriyaPlugin;
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

   @Autowired
   private KartaRuntime kartaRuntime;

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_TARGET )
   public boolean startFeatureFileRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = KriyaPlugin.PLUGIN_NAME ) String pluginName, @RequestBody RunTarget runTarget )
            throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + "-" + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSource.PLUGIN_NAME );
      return kartaRuntime.runTestTarget( runName, pluginName, pluginName, testDataSourcePluginHashSet, runTarget );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURESOURCE )
   public boolean startFeatureSourceRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = KriyaPlugin.PLUGIN_NAME ) String pluginName, @RequestBody String featureSourceString )
            throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + "-" + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSource.PLUGIN_NAME );
      return kartaRuntime.runFeatureSource( runName, pluginName, pluginName, testDataSourcePluginHashSet, featureSourceString, false, false, 1, 1 );
   }

   // @ResponseStatus( HttpStatus.OK )
   // @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_TARGET )
   // public boolean startFeatureRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestParam( defaultValue = KriyaPlugin.PLUGIN_NAME ) String pluginName, @RequestBody TestFeature feature )
   // throws IllegalAccessException, InvocationTargetException
   // {
   // if ( runName.equals( Constants.UNNAMED ) )
   // {
   // runName = runName + "-" + System.currentTimeMillis();
   // }
   //
   // HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
   // testDataSourcePluginHashSet.add( DataFilesTestDataSource.PLUGIN_NAME );
   // return kartaRuntime.runFeature( pluginName, testDataSourcePluginHashSet, runName, feature, false, false, 1, 1 );
   // }
}
