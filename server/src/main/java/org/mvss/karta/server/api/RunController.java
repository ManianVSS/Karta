package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
   public boolean startFeatureFileRun( @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   {
      return kartaRuntime.runTestTarget( runTarget );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_STRING_RUN_URL )
   public boolean startFeatureSourceRun( @RequestBody String featureSourceString ) throws IllegalAccessException, InvocationTargetException
   {
      return kartaRuntime.runFeatureSource( featureSourceString );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_RUN_URL )
   public boolean startFeatureRun( @RequestBody TestFeature feature ) throws IllegalAccessException, InvocationTargetException
   {
      return kartaRuntime.run( feature );
   }
}
