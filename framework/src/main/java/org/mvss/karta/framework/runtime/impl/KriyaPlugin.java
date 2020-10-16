package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
import org.mvss.karta.framework.core.NamedParameter;
import org.mvss.karta.framework.core.ParameterMapping;
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
import org.mvss.karta.framework.utils.ParserUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KriyaPlugin implements FeatureSourceParser, StepRunner
{
   public static final String                           PLUGIN_NAME                            = "Kriya";

   public static final String                           INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
   public static final String                           WORD_FETCH_REGEX                       = "\\W+";

   public static final String                           INLINE_TEST_DATA_PATTERN               = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";

   private HashMap<String, MutablePair<Object, Method>> stepHandlerMap                         = new HashMap<String, MutablePair<Object, Method>>();
   private HashMap<String, MutablePair<Object, Method>> chaosActionHandlerMap                  = new HashMap<String, MutablePair<Object, Method>>();

   private static Pattern                               testDataPattern                        = Pattern.compile( INLINE_TEST_DATA_PATTERN );

   public static final List<String>                     conjunctions                           = Arrays.asList( "Given", "When", "Then", "And", "But" );

   private static ObjectMapper                          objectMapper                           = ParserUtils.getObjectMapper();

   private boolean                                      initialized                            = false;

   @PropertyMapping( group = PLUGIN_NAME, value = "stepDefinitionPackageNames" )
   private ArrayList<String>                            stepDefinitionPackageNames             = new ArrayList<String>();

   @PropertyMapping( group = PLUGIN_NAME, value = "chaosActionDefinitionPackageNames" )
   private ArrayList<String>                            chaosActionDefinitionPackageNames      = new ArrayList<String>();

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

      log.debug( "Initializing " + PLUGIN_NAME + " plugin with " + properties );

      // HashSet<String> repoDirectories = new HashSet<String>();
      // repoDirectories.addAll( kartaRuntimeConfiguration.getTestRepositorydirectories() );
      //
      // HashSet<String> jarFilesToScan = new HashSet<String>();
      //
      // for ( String rpeoDirectory : repoDirectories )
      // {
      // for ( File file : FileUtils.listFiles( new File( rpeoDirectory ), Constants.jarExtention, true ) )
      // {
      // jarFilesToScan.add( file.getAbsolutePath() );
      // }
      // }
      //
      // HashSet<ClassLoader> classLoadersToLoadFrom = new HashSet<ClassLoader>();
      //
      // classLoadersToLoadFrom.add( this.getClass().getClassLoader() );
      //
      // for ( String jarFile : jarFilesToScan )
      // {
      // classLoadersToLoadFrom.add( DynamicClassLoader.getClassLoaderForJar( jarFile ) );
      // }

      // ClassLoader[] classLoaderArrToLoadFrom = new ClassLoader[classLoadersToLoadFrom.size()];
      // classLoadersToLoadFrom.toArray( classLoaderArrToLoadFrom );
      // Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( ClasspathHelper.forClassLoader( classLoaderArrToLoadFrom ) ).addClassLoaders( classLoaderArrToLoadFrom ).setScanners( new MethodAnnotationsScanner() ) );

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

                     if ( stepDefinition.parameterMapping() == ParameterMapping.POSITIONAL )
                     {
                        if ( params.length != ( StringUtils.countMatches( stepDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING ) + 1 ) )
                        {
                           log.error( "Step definition method " + methodDescription + " does not match the argument count as per the identifier" );
                           continue;
                        }
                     }

                     log.debug( "Mapping stepdef " + stepDefString + " to " + methodDescription );

                     Class<?> stepDefinitionClass = candidateStepDefinitionMethod.getDeclaringClass();
                     if ( !stepDefinitionClassObjectMap.containsKey( stepDefinitionClass ) )
                     {
                        Object stepDefClassObj = stepDefinitionClass.newInstance();
                        Configurator.loadProperties( properties, stepDefClassObj );
                        stepDefinitionClassObjectMap.put( stepDefinitionClass, stepDefClassObj );
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

         for ( String actionGroupPackageName : chaosActionDefinitionPackageNames )
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
                           Object chaosActionDefClassObj = chaosActionDefinitionClass.newInstance();
                           Configurator.loadProperties( properties, chaosActionDefClassObj );
                           chaosActionDefinitionClassObjectMap.put( chaosActionDefinitionClass, chaosActionDefClassObj );
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
      }

      initialized = true;
      return true;
   }

   @Override
   public TestFeature parseFeatureSource( String sourceString ) throws Throwable
   {
      return ParserUtils.getYamlObjectMapper().readValue( sourceString, TestFeature.class );
   }

   @Override
   public String sanitizeStepDefinition( String stepIdentifier )
   {
      stepIdentifier = stepIdentifier.trim();
      String words[] = stepIdentifier.split( WORD_FETCH_REGEX );
      String conjuctionUsed = null;
      if ( conjunctions.contains( words[0] ) )
      {
         conjuctionUsed = words[0];
         stepIdentifier = stepIdentifier.substring( conjuctionUsed.length() ).trim();
      }
      stepIdentifier = stepIdentifier.replaceAll( INLINE_TEST_DATA_PATTERN, INLINE_STEP_DEF_PARAM_INDICATOR_STRING );

      return stepIdentifier;
   }

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

      String stepIdentifier = sanitizeStepDefinition( testStep.getIdentifier() );

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

         Parameter[] parametersObj = stepDefMethodToInvoke.getParameters();

         values.add( testExecutionContext );

         // Fetch the positional argument names
         ArrayList<String> inlineStepDefinitionParameterNames = new ArrayList<String>();
         Matcher matcher = testDataPattern.matcher( testStep.getIdentifier().trim() );
         while ( matcher.find() )
         {
            inlineStepDefinitionParameterNames.add( matcher.group() );
         }

         if ( parametersObj.length > 1 )
         {
            StepDefinition definition = stepDefMethodToInvoke.getAnnotationsByType( StepDefinition.class )[0];

            if ( definition.parameterMapping() == ParameterMapping.NAMED )
            {
               for ( int i = 1; i < parametersObj.length; i++ )
               {
                  String name = parametersObj[i].getName();
                  NamedParameter[] paramaterNameInfo = parametersObj[i].getAnnotationsByType( NamedParameter.class );
                  if ( ( paramaterNameInfo != null ) && ( paramaterNameInfo.length >= 1 ) )
                  {
                     name = paramaterNameInfo[0].value();
                  }
                  Serializable parameterValue = testData.get( name );
                  values.add( ( parameterValue == null ) ? null : objectMapper.convertValue( parameterValue, parametersObj[i++].getType() ) );

               }
            }
            else // ( definition.parameterMapping() == ParameterMapping.POSITIONAL )
            {
               int i = 1;
               for ( String positionalParam : inlineStepDefinitionParameterNames )
               {
                  values.add( ParserUtils.getObjectMapper().readValue( positionalParam, parametersObj[i++].getType() ) );
               }
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
}
