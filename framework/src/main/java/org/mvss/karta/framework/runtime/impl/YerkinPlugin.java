package org.mvss.karta.framework.runtime.impl;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class YerkinPlugin implements FeatureSourceParser, StepRunner, TestDataSource
{
   public static final String                           PLUGIN_NAME                            = "Yerkin";

   public static final String                           INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
   public static final String                           WORD_FETCH_REGEX                       = "\\W+";

   public static final String                           INLINE_TEST_DATA_PATTERN               = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";
   public static final String                           INLINE_STEP_DEF_PARAMTERS              = "inlineStepDefinitionParameters";

   private HashMap<String, MutablePair<Object, Method>> stepMap                                = new HashMap<String, MutablePair<Object, Method>>();

   private static Pattern                               testDataPattern                        = Pattern.compile( INLINE_TEST_DATA_PATTERN );

   public static final List<String>                     conjunctions                           = Arrays.asList( "Given", "When", "Then", "And", "But" );

   private boolean                                      initialized                            = false;

   @PropertyMapping( group = PLUGIN_NAME, propertyName = "stepDefinitionJar" )
   private String                                       stepDefinitionJar                      = null;

   @PropertyMapping( group = PLUGIN_NAME, propertyName = "stepDefinitionClassNames" )
   private ArrayList<String>                            stepDefinitionClassNames               = new ArrayList<String>();

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      Configurator.loadProperties( properties, this );

      log.debug( "Initializing Yerkin plugin with " + properties );

      ArrayList<Class<?>> stepDefClasses = StringUtils.isNotBlank( stepDefinitionJar ) ? DynamicClassLoader.loadClasses( new File( stepDefinitionJar ), stepDefinitionClassNames ) : DynamicClassLoader.loadClasses( stepDefinitionClassNames );

      for ( Class<?> stepDefinitionClass : stepDefClasses )
      {
         try
         {
            Object stepDefinitionClassObj = stepDefinitionClass.newInstance();
            Configurator.loadProperties( properties, stepDefinitionClassObj );

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

                     log.debug( "Mapping stepdef " + stepDefString + " to " + methodDescription );
                     stepMap.put( stepDefString, new MutablePair<Object, Method>( stepDefinitionClassObj, candidateStepDefinitionMethod ) );
                  }
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing step definition class " + stepDefinitionClass.getName(), t );
         }
      }
      initialized = true;
      return true;
   }

   @Override
   public TestFeature parseFeatureSource( String sourceString ) throws Throwable
   {
      return ParserUtils.getYamlObjectMapper().readValue( sourceString, TestFeature.class );
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

      stepIdentifier = stepIdentifier.replaceAll( INLINE_TEST_DATA_PATTERN, INLINE_STEP_DEF_PARAM_INDICATOR_STRING );

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
            for ( String positionalParam : (ArrayList<String>) testData.get( INLINE_STEP_DEF_PARAMTERS ) )
            {
               values.add( ParserUtils.getObjectMapper().readValue( positionalParam, parameters[i++] ) );
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

   @Override
   public void close() throws Exception
   {
      // nothing to do
   }
}
