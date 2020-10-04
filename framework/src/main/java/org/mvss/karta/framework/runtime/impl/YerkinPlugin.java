package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.ChaosActionDefinition;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;
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
import org.mvss.karta.framework.utils.ParserUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class YerkinPlugin implements FeatureSourceParser, StepRunner, TestDataSource
{
   public static final String                           PLUGIN_NAME                            = "Yerkin";

   public static final String                           INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
   public static final String                           WORD_FETCH_REGEX                       = "\\W+";

   public static final String                           INLINE_TEST_DATA_PATTERN               = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";
   public static final String                           INLINE_STEP_DEF_PARAMTERS              = "inlineStepDefinitionParameters";

   private HashMap<String, MutablePair<Object, Method>> stepHandlerMap                         = new HashMap<String, MutablePair<Object, Method>>();
   private HashMap<String, MutablePair<Object, Method>> chaosActionHandlerMap                  = new HashMap<String, MutablePair<Object, Method>>();

   private static Pattern                               testDataPattern                        = Pattern.compile( INLINE_TEST_DATA_PATTERN );

   public static final List<String>                     conjunctions                           = Arrays.asList( "Given", "When", "Then", "And", "But" );

   private boolean                                      initialized                            = false;

   @PropertyMapping( group = PLUGIN_NAME, value = "stepDefinitionPackageNames" )
   private ArrayList<String>                            stepDefinitionPackageNames             = new ArrayList<String>();

   @PropertyMapping( group = PLUGIN_NAME, value = "actionGroupPackageNames" )
   private ArrayList<String>                            actionGroupPackageNames                = new ArrayList<String>();

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

      for ( String stepDefinitionPackageName : stepDefinitionPackageNames )
      {
         try
         {
            Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( ClasspathHelper.forPackage( stepDefinitionPackageName ) ).setScanners( new MethodAnnotationsScanner() ) );
            Set<Method> stepDefinitionMethods = reflections.getMethodsAnnotatedWith( StepDefinition.class );
            HashMap<Class<?>, Object> stepDefinitionClassObjectMap = new HashMap<Class<?>, Object>();

            for ( Method candidateStepDefinitionMethod : stepDefinitionMethods )
            {
               if ( Modifier.isPublic( candidateStepDefinitionMethod.getModifiers() ) )
               {
                  for ( StepDefinition stepDefinition : candidateStepDefinitionMethod.getAnnotationsByType( StepDefinition.class ) )
                  {
                     String methodDescription = candidateStepDefinitionMethod.toString();
                     String stepDefString = stepDefinition.value();
                     Class<?>[] params = candidateStepDefinitionMethod.getParameterTypes();

                     if ( !( ( params.length >= 1 ) && ( TestExecutionContext.class == params[0] ) ) )
                     {
                        log.error( "Step definition method " + methodDescription + " should have the first parameter type as TestExecutionContext" );
                        continue;
                     }

                     if ( ( params.length > 1 ) && !( params.length == StringUtils.countMatches( stepDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING ) + 1 ) )
                     {
                        log.error( "Step definition method " + methodDescription + " does not match the argument count as per the identifier" );
                        continue;
                     }

                     log.debug( "Mapping stepdef " + stepDefString + " to " + methodDescription );

                     Class<?> stepDefinitionClass = candidateStepDefinitionMethod.getDeclaringClass();
                     if ( !stepDefinitionClassObjectMap.containsKey( stepDefinitionClass ) )
                     {
                        stepDefinitionClassObjectMap.put( stepDefinitionClass, stepDefinitionClass.newInstance() );
                     }
                     stepHandlerMap.put( stepDefString, new MutablePair<Object, Method>( stepDefinitionClassObjectMap.get( stepDefinitionClass ), candidateStepDefinitionMethod ) );
                  }
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing step definition package " + stepDefinitionPackageName, t );
         }
      }

      for ( String actionGroupPackageName : actionGroupPackageNames )
      {
         try
         {
            Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( ClasspathHelper.forPackage( actionGroupPackageName ) ).setScanners( new MethodAnnotationsScanner() ) );
            Set<Method> chaosActionDefinitionMethods = reflections.getMethodsAnnotatedWith( ChaosActionDefinition.class );
            HashMap<Class<?>, Object> chaosActionDefinitionClassObjectMap = new HashMap<Class<?>, Object>();

            for ( Method candidateChaosActionMethod : chaosActionDefinitionMethods )
            {
               if ( Modifier.isPublic( candidateChaosActionMethod.getModifiers() ) )
               {
                  for ( ChaosActionDefinition chaosActionDefinition : candidateChaosActionMethod.getAnnotationsByType( ChaosActionDefinition.class ) )
                  {
                     String methodDescription = candidateChaosActionMethod.toString();
                     String chaosActionName = chaosActionDefinition.value();
                     Class<?>[] params = candidateChaosActionMethod.getParameterTypes();

                     if ( !( ( params.length == 2 ) && ( TestExecutionContext.class == params[0] ) && ( ChaosAction.class == params[1] ) ) )
                     {
                        log.error( "Chaos action definition method " + methodDescription + " should have two parameters of types(" + TestExecutionContext.class.getName() + ", " + ChaosAction.class.getName() + ")" );
                        continue;
                     }

                     log.debug( "Mapping choas action definition " + chaosActionName + " to " + methodDescription );

                     Class<?> chaosActionDefinitionClass = candidateChaosActionMethod.getDeclaringClass();
                     if ( !chaosActionDefinitionClassObjectMap.containsKey( chaosActionDefinitionClass ) )
                     {
                        chaosActionDefinitionClassObjectMap.put( chaosActionDefinitionClass, chaosActionDefinitionClass.newInstance() );
                     }
                     chaosActionHandlerMap.put( chaosActionName, new MutablePair<Object, Method>( chaosActionDefinitionClassObjectMap.get( chaosActionDefinitionClass ), candidateChaosActionMethod ) );
                  }
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing step definition package " + actionGroupPackageName, t );
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
   public StepResult runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      StepResult result = new StepResult();

      log.debug( "Step run" + testStep + " with context " + testExecutionContext );

      if ( StringUtils.isBlank( testStep.getIdentifier() ) )
      {
         log.error( "Empty step definition identifier for step " + testStep );
         return result;
      }

      String stepIdentifier = testStep.getIdentifier().trim();

      String words[] = stepIdentifier.split( WORD_FETCH_REGEX );

      String conjuctionUsed = null;

      if ( conjunctions.contains( words[0] ) )
      {
         conjuctionUsed = words[0];
         stepIdentifier = stepIdentifier.substring( conjuctionUsed.length() ).trim();
      }

      stepIdentifier = stepIdentifier.replaceAll( INLINE_TEST_DATA_PATTERN, INLINE_STEP_DEF_PARAM_INDICATOR_STRING );

      if ( !stepHandlerMap.containsKey( stepIdentifier ) )
      {
         return result;
      }

      try
      {
         HashMap<String, Serializable> testData = testExecutionContext.getData();

         ArrayList<Object> values = new ArrayList<Object>();

         MutablePair<Object, Method> stepDefHandlerObjectMethodPair = stepHandlerMap.get( stepIdentifier );

         Object stepDefObject = stepDefHandlerObjectMethodPair.getLeft();
         Method stepDefMethodToInvoke = stepDefHandlerObjectMethodPair.getRight();

         Class<?>[] params = stepDefMethodToInvoke.getParameterTypes();

         Class<?>[] parameters = stepDefMethodToInvoke.getParameterTypes();

         values.add( testExecutionContext );
         int i = 1;

         if ( params.length > 1 )
         {
            for ( String positionalParam : (ArrayList<String>) testData.get( INLINE_STEP_DEF_PARAMTERS ) )
            {
               values.add( ParserUtils.getObjectMapper().readValue( positionalParam, parameters[i++] ) );
            }
         }

         if ( stepDefMethodToInvoke.getReturnType().equals( StepResult.class ) )
         {
            result = (StepResult) stepDefMethodToInvoke.invoke( stepDefObject, values.toArray() );
         }
         else
         {
            stepDefMethodToInvoke.invoke( stepDefObject, values.toArray() );
            result.setSuccesssful( true );
            result.setVariables( testExecutionContext.getVariables() );
         }

      }
      catch ( Throwable t )
      {
         result.setSuccesssful( false );
         result.setMessage( t.getMessage() );
         result.setErrorThrown( t );
      }

      return result;
   }

   @Override
   public StepResult performChaosAction( ChaosAction chaosAction, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      StepResult result = new StepResult();

      log.debug( "Chaos actions run" + chaosAction + " with context " + testExecutionContext );

      if ( StringUtils.isBlank( chaosAction.getName() ) )
      {
         log.error( "Empty chaos action name " + chaosAction );
         return result;
      }

      String choasActionName = chaosAction.getName();

      try
      {
         if ( chaosActionHandlerMap.containsKey( choasActionName ) )
         {
            MutablePair<Object, Method> chaosActionHandlerObjectMethodPair = chaosActionHandlerMap.get( choasActionName );

            Object chaosActionHandlerObject = chaosActionHandlerObjectMethodPair.getLeft();
            Method choasActionHandlerMethodToInvoke = chaosActionHandlerObjectMethodPair.getRight();

            if ( choasActionHandlerMethodToInvoke.getReturnType().equals( StepResult.class ) )
            {
               result = (StepResult) choasActionHandlerMethodToInvoke.invoke( chaosActionHandlerObject, testExecutionContext, chaosAction );
            }
            else
            {
               choasActionHandlerMethodToInvoke.invoke( chaosActionHandlerObject, testExecutionContext, chaosAction );
               result.setSuccesssful( true );
               result.setVariables( testExecutionContext.getVariables() );
            }
         }
      }
      catch ( Throwable t )
      {
         result.setSuccesssful( false );
         result.setMessage( t.getMessage() );
         result.setErrorThrown( t );
      }

      return result;
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
