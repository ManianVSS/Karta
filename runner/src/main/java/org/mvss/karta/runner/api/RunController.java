package org.mvss.karta.runner.api;

import java.util.HashMap;

import org.mvss.karta.runner.core.FeatureRunner;
import org.mvss.karta.runner.core.JavaTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RunController
{
   private static final String RUNURL               = "/api/run";
   private static final String JAVA_TEST_RUN_URL    = RUNURL + "JavaTestCase";
   private static final String FEATURE_TEST_RUN_URL = RUNURL + "FeatureTestCase";

   @Autowired
   ObjectMapper                objectMapper;

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = JAVA_TEST_RUN_URL )
   public boolean startJavaTestRun( @RequestBody HashMap<String, Object> runConfiguration )
   {
      JavaTestRunner testRunner = objectMapper.convertValue( runConfiguration, JavaTestRunner.class );
      new Thread( testRunner ).run();
      return true;
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = FEATURE_TEST_RUN_URL )
   public boolean startFeatureRun( @RequestBody HashMap<String, Object> runConfiguration )
   {
      FeatureRunner featureRunner = objectMapper.convertValue( runConfiguration, FeatureRunner.class );
      new Thread( featureRunner ).run();
      return true;
   }
}
