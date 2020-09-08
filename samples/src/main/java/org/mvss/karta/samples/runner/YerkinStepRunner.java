package org.mvss.karta.samples.runner;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.StepRunner;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class YerkinStepRunner implements StepRunner
{
   private HashMap<String, Method> stepMap      = new HashMap<String, Method>();
   private ObjectMapper            objectMapper = new ObjectMapper();

   @Override
   public void initStepRepository( HashMap<String, Serializable> testProperties ) throws Throwable
   {
      log.info( "Initializing step repository with " + testProperties );
      @SuppressWarnings( "unchecked" )
      ArrayList<String> stepDefinitionClassNames = (ArrayList<String>) testProperties.get( "stepDefinitionClassNames" );

      for ( String stepDefinitionClassName : stepDefinitionClassNames )
      {
         Class<?> stepDefinitionClass = Class.forName( stepDefinitionClassName );

         for ( Method candidateStepDefinitionMethod : stepDefinitionClass.getMethods() )
         {
            if ( Modifier.isPublic( candidateStepDefinitionMethod.getModifiers() ) && Modifier.isStatic( candidateStepDefinitionMethod.getModifiers() ) )
            {
               for ( StepDefinition stepDefinition : candidateStepDefinitionMethod.getAnnotationsByType( StepDefinition.class ) )
               {
                  String methodDescription = candidateStepDefinitionMethod.toString();
                  String stepDefString = stepDefinition.value();
                  int expectedParameterCount = StringUtils.countMatches( stepDefString, "\"\"" );

                  if ( candidateStepDefinitionMethod.getParameterCount() != expectedParameterCount )
                  {
                     log.error( "Step definition method " + methodDescription + " does not match the expected number of parameters as per step definition" );
                     continue;
                  }

                  // for ( Class<?> paramType : candidateStepDefinitionMethod.getParameterTypes() )
                  // {
                  // if ( !paramType.getName().equals( String.class.getName() ) )
                  // {
                  // log.error( "Step definition method " + methodDescription + " should contain only String parameters" );
                  // continue;
                  // }
                  // }

                  log.info( "Mapping stepdef " + stepDefString + " to " + methodDescription );
                  stepMap.put( stepDefString, candidateStepDefinitionMethod );
               }
            }
         }
      }

   }

   @Override
   public boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      log.debug( "Step run" + testStep + " with context " + testExecutionContext );

      // String conjuction = testStep.getConjunction();
      String stepRef = testStep.getIdentifier();

      stepRef = stepRef.replaceAll( YerkinTestDataSource.INLINE_TEST_DATA_PATTERN, "\"\"" );

      if ( !stepMap.containsKey( stepRef ) )
      {
         return false;
      }

      try
      {
         HashMap<String, Serializable> testData = testExecutionContext.getTestData();
         ArrayList<Object> values = new ArrayList<Object>();

         Class<?>[] parameters = stepMap.get( stepRef ).getParameterTypes();

         for ( int i = 0; i < testData.values().size(); i++ )
         {
            String stringValue = (String) testData.get( "param[" + i + "]" );
            Object value = objectMapper.readValue( stringValue, parameters[i] );
            values.add( value );
         }

         if ( values.isEmpty() )
         {
            stepMap.get( stepRef ).invoke( null );
         }
         else
         {
            stepMap.get( stepRef ).invoke( null, values.toArray() );
         }
      }
      catch ( Throwable t )
      {
         return false;
      }

      return true;
   }

   void step1() throws Throwable
   {
      log.info( "Running step1" );
   }

   void step2() throws Throwable
   {
      log.info( "Running step2" );
   }

   void step3() throws Throwable
   {
      log.info( "Running step3" );
   }

}
