package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.JavaTestRunner;
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
   // private static final String JAVA_TEST_RUN_URL = RUNURL + "JavaTestCase";
   private static final String FEATURE_RUN_URL        = RUNURL + "Feature";
   private static final String FEATURE_STRING_RUN_URL = RUNURL + "FeatureSource";
   private static final String RUN_TARGET_PATH        = RUNURL + "Target";

   @Autowired
   private JavaTestRunner      testRunner;

   @Autowired
   private KartaRuntime        kartaRuntime;

   // @Autowired
   // private FeatureRunner featureRunner;

   // @ResponseStatus( HttpStatus.OK )
   // @RequestMapping( method = RequestMethod.POST, value = JAVA_TEST_RUN_URL )
   // public boolean startJavaTestRun( @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   // {
   // return testRunner.run( runTarget.getJavaTest(), runTarget.getJavaTestJarFile() );
   // }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = RUN_TARGET_PATH )
   public boolean startFeatureFileRun( @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   {
      if ( runTarget.getFeatureFile() != null )
      {
         return kartaRuntime.runFeatureFile( runTarget.getFeatureFile() );
      }
      else if ( runTarget.getJavaTest() != null )
      {
         return testRunner.run( runTarget.getJavaTest(), runTarget.getJavaTestJarFile() );
      }
      else if ( ( runTarget.getTags() != null ) && !runTarget.getTags().isEmpty() )
      {
         String[] tags = new String[runTarget.getTags().size()];
         runTarget.getTags().toArray( tags );
         return kartaRuntime.runTestsWithTags( tags );
      }
      else
      {
         return false;
      }
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
