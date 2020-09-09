package org.mvss.karta.samples.runner;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.StepRunner;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class YerkinStepRunner implements StepRunner
{
   private HashMap<String, MutablePair<Object, Method>> stepMap                                = new HashMap<String, MutablePair<Object, Method>>();

   private ObjectMapper                                 objectMapper                           = new ObjectMapper();

   public static final String                           INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
   public static final String                           WORD_FETCH_REGEX                       = "\\W+";

   public static final List<String>                     conjunctions                           = Arrays.asList( "Given", "When", "Then", "And", "But" );

   @Override
   public void initStepRepository( HashMap<String, Serializable> testProperties ) throws Throwable
   {
      log.info( "Initializing step repository with " + testProperties );
      @SuppressWarnings( "unchecked" )
      ArrayList<String> stepDefinitionClassNames = (ArrayList<String>) testProperties.get( "stepDefinitionClassNames" );

      for ( String stepDefinitionClassName : stepDefinitionClassNames )
      {
         try
         {
            Class<?> stepDefinitionClass = Class.forName( stepDefinitionClassName );

            Object stepDefinitionClassObj = stepDefinitionClass.newInstance();

            for ( Method candidateStepDefinitionMethod : stepDefinitionClass.getMethods() )
            {
               if ( Modifier.isPublic( candidateStepDefinitionMethod.getModifiers() ) )// && Modifier.isStatic( candidateStepDefinitionMethod.getModifiers() ) )
               {
                  for ( StepDefinition stepDefinition : candidateStepDefinitionMethod.getAnnotationsByType( StepDefinition.class ) )
                  {
                     String methodDescription = candidateStepDefinitionMethod.toString();
                     String stepDefString = stepDefinition.value();
                     Class<?>[] params = candidateStepDefinitionMethod.getParameterTypes();

                     if ( !( ( ( params.length == 1 ) && ( HashMap.class.isAssignableFrom( params[0] ) ) ) || ( params.length == StringUtils.countMatches( stepDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING ) ) ) )
                     {
                        log.error( "Step definition method " + methodDescription + " neither accepts a HashMap data object nor matches the expected number of parameters as per step definition" );
                        continue;
                     }

                     log.info( "Mapping stepdef " + stepDefString + " to " + methodDescription );
                     stepMap.put( stepDefString, new MutablePair<Object, Method>( stepDefinitionClassObj, candidateStepDefinitionMethod ) );

                  }
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing step definition class " + stepDefinitionClassName, t );
            continue;
         }
      }

   }

   @SuppressWarnings( "unchecked" )
   @Override
   public boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      log.debug( "Step run" + testStep + " with context " + testExecutionContext );

      String stepIdentifier = testStep.getIdentifier().trim();

      if ( StringUtils.isAllEmpty( stepIdentifier ) )
      {
         log.error( "Empty step definition identifier for step " + testStep );
         return false;
      }

      String words[] = stepIdentifier.split( WORD_FETCH_REGEX );

      String conjuctionUsed = null;

      if ( conjunctions.contains( words[0] ) )
      {
         conjuctionUsed = words[0];
         stepIdentifier = stepIdentifier.substring( conjuctionUsed.length() ).trim();
      }

      stepIdentifier = stepIdentifier.replaceAll( YerkinTestDataSource.INLINE_TEST_DATA_PATTERN, INLINE_STEP_DEF_PARAM_INDICATOR_STRING );

      if ( !stepMap.containsKey( stepIdentifier ) )
      {
         return false;
      }

      try
      {
         HashMap<String, Serializable> testData = testExecutionContext.getTestData();

         ArrayList<Object> values = new ArrayList<Object>();

         MutablePair<Object, Method> stepDefObjectMethodPairToInvoke = stepMap.get( stepIdentifier );

         Object stepDefObject = stepDefObjectMethodPairToInvoke.getLeft();
         Method stepDefMethodToInvoke = stepDefObjectMethodPairToInvoke.getRight();

         Class<?>[] params = stepDefMethodToInvoke.getParameterTypes();

         if ( ( ( params.length == 1 ) && ( HashMap.class.isAssignableFrom( params[0] ) ) ) )
         {
            stepDefMethodToInvoke.invoke( stepDefObject, testData );
         }
         else
         {
            Class<?>[] parameters = stepDefMethodToInvoke.getParameterTypes();

            int i = 0;
            for ( String positionalParam : (ArrayList<String>) testData.get( YerkinTestDataSource.INLINE_STEP_DEF_PARAMTERS ) )
            {
               values.add( objectMapper.readValue( positionalParam, parameters[i++] ) );
            }

            stepDefMethodToInvoke.invoke( stepDefObject, values.isEmpty() ? null : values.toArray() );
         }
      }
      catch ( Throwable t )
      {
         return false;
      }

      return true;
   }
}
