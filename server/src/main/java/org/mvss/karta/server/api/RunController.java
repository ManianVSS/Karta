package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;

import org.mvss.karta.framework.runtime.FeatureRunner;
import org.mvss.karta.framework.runtime.JavaTestRunner;
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
   private static final String RUNURL               = "/api/run";
   private static final String JAVA_TEST_RUN_URL    = RUNURL + "JavaTestCase";
   private static final String FEATURE_TEST_RUN_URL = RUNURL + "FeatureTestCase";

   @Autowired
   private JavaTestRunner      testRunner;

   @Autowired
   private FeatureRunner       featureRunner;

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = JAVA_TEST_RUN_URL )
   public boolean startJavaTestRun( @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   {
      return testRunner.run( runTarget.getJavaTest(), runTarget.getJavaTestJarFile() );
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_TEST_RUN_URL )
   public boolean startFeatureRun( @RequestBody RunTarget runTarget ) throws IllegalAccessException, InvocationTargetException
   {
      return featureRunner.run( runTarget.getFeatureFile() );
   }
}
