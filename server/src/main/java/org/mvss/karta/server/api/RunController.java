package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.RunResult;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunInfo;
import org.mvss.karta.framework.runtime.RunTarget;
import org.mvss.karta.framework.runtime.impl.DataFilesTestDataSource;
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
   public RunResult startFeatureFileRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + Constants.HYPHEN + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSource.PLUGIN_NAME );
      RunInfo runInfo = kartaRuntime.getDefaultRunInfo().toBuilder().runName( runName ).build();
      return kartaRuntime.runTestTarget( runInfo, runTarget );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURESOURCE )
   public FeatureResult startFeatureSourceRun( @RequestParam( defaultValue = Constants.UNNAMED ) String runName, @RequestBody String featureSourceString ) throws IllegalAccessException, InvocationTargetException
   {
      if ( runName.equals( Constants.UNNAMED ) )
      {
         runName = runName + Constants.HYPHEN + System.currentTimeMillis();
      }

      HashSet<String> testDataSourcePluginHashSet = new HashSet<String>();
      testDataSourcePluginHashSet.add( DataFilesTestDataSource.PLUGIN_NAME );
      RunInfo runInfo = kartaRuntime.getDefaultRunInfo().toBuilder().runName( runName ).build();
      return kartaRuntime.runFeatureSource( runInfo, featureSourceString );
   }
}
