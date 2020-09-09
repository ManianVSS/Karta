package org.mvss.karta.samples.runner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

public class YerkinTestDataSource implements TestDataSource
{
   public static final String INLINE_TEST_DATA_PATTERN  = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";
   public static final String INLINE_STEP_DEF_PARAMTERS = "inlineStepDefinitionParameters";

   private Pattern            testDataPattern           = Pattern.compile( INLINE_TEST_DATA_PATTERN );

   @Override
   public void close() throws Exception
   {
      // nothing to do
   }

   @Override
   public boolean initDataSource( HashMap<String, Serializable> properties ) throws Throwable
   {
      // nothing to do
      return true;
   }

   @Override
   public HashMap<String, Serializable> getData( ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      ArrayList<String> inlineStepDefinitionParameters = new ArrayList<String>();

      TestStep testStep = executionStepPointer.getTestStep();

      HashMap<String, Serializable> inStepTestData = testStep.getTestData();

      if ( inStepTestData != null )
      {
         inStepTestData.forEach( ( dataKey, data ) -> testData.put( dataKey, data ) );
      }

      String stepDefinition = testStep.getIdentifier();
      Matcher matcher = testDataPattern.matcher( stepDefinition );

      while ( matcher.find() )
      {
         inlineStepDefinitionParameters.add( matcher.group() );
      }

      testData.put( INLINE_STEP_DEF_PARAMTERS, inlineStepDefinitionParameters );

      return testData;
   }

}
