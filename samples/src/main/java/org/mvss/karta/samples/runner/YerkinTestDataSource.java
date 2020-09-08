package org.mvss.karta.samples.runner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvss.karta.framework.runtime.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

public class YerkinTestDataSource implements TestDataSource
{
   public static final String INLINE_TEST_DATA_PATTERN = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";

   private Pattern            testDataPattern          = Pattern.compile( INLINE_TEST_DATA_PATTERN );

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

      String stepDefinition = executionStepPointer.getStepDefinition();
      Matcher matcher = testDataPattern.matcher( stepDefinition );

      int i = 0;

      while ( matcher.find() )
      {
         // String valueEnclosedInQuotes = matcher.group().substring( 1 );
         // valueEnclosedInQuotes = valueEnclosedInQuotes.substring( 0, valueEnclosedInQuotes.length() - 1 );
         testData.put( "param[" + i + "]", matcher.group() );
      }

      return testData;
   }

}
