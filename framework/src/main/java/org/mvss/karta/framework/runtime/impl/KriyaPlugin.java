package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.mvss.karta.framework.core.AfterFeature;
import org.mvss.karta.framework.core.AfterRun;
import org.mvss.karta.framework.core.AfterScenario;
import org.mvss.karta.framework.core.BeforeFeature;
import org.mvss.karta.framework.core.BeforeRun;
import org.mvss.karta.framework.core.BeforeScenario;
import org.mvss.karta.framework.core.ChaosActionDefinition;
import org.mvss.karta.framework.core.ContextBean;
import org.mvss.karta.framework.core.ContextVariable;
import org.mvss.karta.framework.core.Initializer;
import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.Pair;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestData;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.nodes.KartaNodeRegistry;
import org.mvss.karta.framework.runtime.BeanRegistry;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestLifeCycleHook;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KriyaPlugin implements FeatureSourceParser, StepRunner, TestLifeCycleHook
{
   public static final String                                PLUGIN_NAME                            = "Kriya";

   public static final String                                INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
   public static final String                                WORD_FETCH_REGEX                       = "\\W+";

   public static final String                                INLINE_TEST_DATA_PATTERN               = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";

   private HashMap<String, Pattern>                          tagPatternMap                          = new HashMap<String, Pattern>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedRunStartHooks                    = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedRunStopHooks                     = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedFeatureStartHooks                = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedFeatureStopHooks                 = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedScenarioStartHooks               = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();
   private HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedScenarioStopHooks                = new HashMap<Pattern, ArrayList<Pair<Object, Method>>>();

   private HashMap<String, Pair<Object, Method>>             stepHandlerMap                         = new HashMap<String, Pair<Object, Method>>();
   private HashMap<String, Pair<Object, Method>>             chaosActionHandlerMap                  = new HashMap<String, Pair<Object, Method>>();

   private static Pattern                                    testDataPattern                        = Pattern.compile( INLINE_TEST_DATA_PATTERN );

   public static final List<String>                          conjunctions                           = Arrays.asList( "Given", "When", "Then", "And", "But" );

   private static ObjectMapper                               objectMapper                           = ParserUtils.getObjectMapper();

   private boolean                                           initialized                            = false;

   @PropertyMapping( group = PLUGIN_NAME, value = "stepDefinitionPackageNames" )
   private ArrayList<String>                                 stepDefinitionPackageNames             = new ArrayList<String>();

   @PropertyMapping( group = PLUGIN_NAME, value = "chaosActionDefinitionPackageNames" )
   private ArrayList<String>                                 chaosActionDefinitionPackageNames      = new ArrayList<String>();

   @KartaAutoWired
   private KartaRuntime                                      kartaRuntime;

   private BeanRegistry                                      initializedClassesRegistry             = new BeanRegistry();

   @KartaAutoWired
   private EventProcessor                                    eventProcessor;

   @KartaAutoWired
   private KartaNodeRegistry                                 minionRegistry;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   private final Consumer<Method> processStepDefinition  = new Consumer<Method>()
                                                         {
                                                            @Override
                                                            public void accept( Method candidateStepDefinitionMethod )
                                                            {
                                                               try
                                                               {
                                                                  for ( StepDefinition stepDefinition : candidateStepDefinitionMethod.getAnnotationsByType( StepDefinition.class ) )
                                                                  {
                                                                     String methodDescription = candidateStepDefinitionMethod.toString();
                                                                     String stepDefString = stepDefinition.value();

                                                                     if ( stepHandlerMap.containsKey( stepDefString ) )
                                                                     {
                                                                        log.error( "Step definition is duplicate " + methodDescription );
                                                                        continue;
                                                                     }

                                                                     Parameter[] params = candidateStepDefinitionMethod.getParameters();

                                                                     int positionalArgumentsCount = 0;
                                                                     for ( int i = 0; i < params.length; i++ )
                                                                     {
                                                                        if ( ( params[i].getType() != TestExecutionContext.class ) && ( params[i].getAnnotation( TestData.class ) == null ) && ( params[i].getAnnotation( ContextBean.class ) == null )
                                                                             && ( params[i].getAnnotation( ContextVariable.class ) == null ) )
                                                                        {
                                                                           positionalArgumentsCount++;
                                                                        }
                                                                     }

                                                                     if ( positionalArgumentsCount != StringUtils.countMatches( stepDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING ) )
                                                                     {
                                                                        log.error( "Step definition method " + methodDescription + " does not match the argument count as per the identifier" );
                                                                        continue;
                                                                     }

                                                                     log.debug( "Mapping stepdef " + stepDefString + " to " + methodDescription );

                                                                     Class<?> stepDefinitionClass = candidateStepDefinitionMethod.getDeclaringClass();

                                                                     Object stepDefClassObj = initializedClassesRegistry.get( stepDefinitionClass.getName() );
                                                                     if ( stepDefClassObj == null )
                                                                     {
                                                                        stepDefClassObj = stepDefinitionClass.newInstance();
                                                                        kartaRuntime.initializeObject( stepDefClassObj );
                                                                        initializedClassesRegistry.add( stepDefClassObj );
                                                                     }

                                                                     if ( stepDefClassObj != null )
                                                                     {
                                                                        stepHandlerMap.put( stepDefString, new Pair<Object, Method>( stepDefClassObj, candidateStepDefinitionMethod ) );
                                                                     }
                                                                  }
                                                               }
                                                               catch ( Throwable t )
                                                               {
                                                                  log.error( "Exception while parsing step definition from method  " + candidateStepDefinitionMethod.getName(), t );
                                                               }

                                                            }
                                                         };

   private final Consumer<Method> processChaosDefinition = new Consumer<Method>()
                                                         {
                                                            @Override
                                                            public void accept( Method candidateChaosActionMethod )
                                                            {
                                                               try
                                                               {
                                                                  for ( ChaosActionDefinition chaosActionDefinition : candidateChaosActionMethod.getAnnotationsByType( ChaosActionDefinition.class ) )
                                                                  {
                                                                     String methodDescription = candidateChaosActionMethod.toString();
                                                                     String chaosActionName = chaosActionDefinition.value();

                                                                     if ( chaosActionHandlerMap.containsKey( chaosActionName ) )
                                                                     {
                                                                        log.error( "Chaos action definition is duplicate " + methodDescription );
                                                                        continue;
                                                                     }

                                                                     Parameter[] params = candidateChaosActionMethod.getParameters();

                                                                     if ( !( ( params.length >= 2 ) && ( TestExecutionContext.class == params[0].getType() ) && ( PreparedChaosAction.class == params[1].getType() ) ) )
                                                                     {
                                                                        log.error( "Chaos action definition method " + methodDescription + " should have first two parameters of types(" + TestExecutionContext.class.getName() + ", "
                                                                                   + PreparedChaosAction.class.getName() + ")" );
                                                                        continue;
                                                                     }

                                                                     for ( int paramNo = 2; paramNo < params.length; paramNo++ )
                                                                     {
                                                                        TestData testDataAnnotation = params[paramNo].getAnnotation( TestData.class );
                                                                        ContextBean contextBeanAnnotation = params[paramNo].getAnnotation( ContextBean.class );
                                                                        ContextVariable contextVariableAnnotation = params[paramNo].getAnnotation( ContextVariable.class );

                                                                        if ( ( testDataAnnotation == null ) && ( contextBeanAnnotation == null ) && ( contextVariableAnnotation == null ) )
                                                                        {
                                                                           log.error( "Chaos action definition method " + methodDescription + "'s parameter is not mapped mapped with an appropriate annotation(" + TestData.class.getName() + ", "
                                                                                      + ContextBean.class.getName() + ", " + ContextVariable.class.getName() + ")" );
                                                                           continue;
                                                                        }
                                                                     }

                                                                     log.debug( "Mapping chaos action definition " + chaosActionName + " to " + methodDescription );

                                                                     Class<?> chaosActionDefinitionClass = candidateChaosActionMethod.getDeclaringClass();

                                                                     Object chaosActionDefClassObj = initializedClassesRegistry.get( chaosActionDefinitionClass.getName() );
                                                                     if ( chaosActionDefClassObj == null )
                                                                     {
                                                                        chaosActionDefClassObj = chaosActionDefinitionClass.newInstance();
                                                                        kartaRuntime.initializeObject( chaosActionDefClassObj );
                                                                        initializedClassesRegistry.add( chaosActionDefClassObj );
                                                                     }

                                                                     if ( chaosActionDefClassObj != null )
                                                                     {
                                                                        chaosActionHandlerMap.put( chaosActionName, new Pair<Object, Method>( chaosActionDefClassObj, candidateChaosActionMethod ) );
                                                                     }
                                                                  }
                                                               }
                                                               catch ( Throwable t )
                                                               {
                                                                  log.error( "Exception while parsing chaos action definition from method  " + candidateChaosActionMethod.getName(), t );
                                                               }
                                                            }

                                                         };

   private void processTaggedHook( HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedHooks, String[] tags, Method hookMethod, Class<?>... parameters ) throws InstantiationException, IllegalAccessException
   {
      String methodDescription = hookMethod.toString();
      Class<?>[] params = hookMethod.getParameterTypes();

      if ( parameters != null )
      {
         boolean error = ( params.length != parameters.length );

         for ( int i = 0; !error && ( i < params.length ); i++ )
         {
            if ( params[i] != parameters[i] )
            {
               error = true;
            }
         }

         if ( error )
         {
            log.error( "Hook method " + methodDescription + " does not match requried parameters " + Arrays.asList( parameters ) );
            return;
         }
      }

      log.debug( "Mapping hook for " + tags + " to " + methodDescription );

      Class<?> hookClass = hookMethod.getDeclaringClass();
      Object hookObj = initializedClassesRegistry.get( hookClass.getName() );

      if ( hookObj == null )
      {
         hookObj = hookClass.newInstance();
         kartaRuntime.initializeObject( hookObj );
         initializedClassesRegistry.add( hookObj );
      }

      for ( String tag : tags )
      {
         if ( !tagPatternMap.containsKey( tag ) )
         {
            tagPatternMap.put( tag, Pattern.compile( tag ) );
         }

         Pattern tagPattern = tagPatternMap.get( tag );

         if ( !taggedHooks.containsKey( tagPattern ) )
         {
            taggedHooks.put( tagPattern, new ArrayList<Pair<Object, Method>>() );
         }

         ArrayList<Pair<Object, Method>> hooksDef = taggedHooks.get( tagPattern );

         Pair<Object, Method> hookdef = new Pair<Object, Method>( hookObj, hookMethod );

         hooksDef.add( hookdef );
      }

   }

   private final Consumer<Method> processTaggedRunStartHook      = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method runStartHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( BeforeRun beforeRun : runStartHookMethod.getAnnotationsByType( BeforeRun.class ) )
                                                                          {
                                                                             String[] tags = beforeRun.value();
                                                                             processTaggedHook( taggedRunStartHooks, tags, runStartHookMethod, String.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing run start hook from method  " + runStartHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   private final Consumer<Method> processTaggedRunStopHook       = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method runStopHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( AfterRun afterRun : runStopHookMethod.getAnnotationsByType( AfterRun.class ) )
                                                                          {
                                                                             String[] tags = afterRun.value();
                                                                             processTaggedHook( taggedRunStopHooks, tags, runStopHookMethod, String.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing run stop hook from method  " + runStopHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   private final Consumer<Method> processTaggedFeatureStartHook  = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method featureStartHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( BeforeFeature beforeFeature : featureStartHookMethod.getAnnotationsByType( BeforeFeature.class ) )
                                                                          {
                                                                             String[] tags = beforeFeature.value();
                                                                             processTaggedHook( taggedFeatureStartHooks, tags, featureStartHookMethod, String.class, TestFeature.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing feature start hook from method  " + featureStartHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   private final Consumer<Method> processTaggedFeatureStopHook   = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method featureStopHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( AfterFeature afterFeature : featureStopHookMethod.getAnnotationsByType( AfterFeature.class ) )
                                                                          {
                                                                             String[] tags = afterFeature.value();
                                                                             processTaggedHook( taggedFeatureStopHooks, tags, featureStopHookMethod, String.class, TestFeature.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing feature stop hook from method  " + featureStopHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   private final Consumer<Method> processTaggedScenarioStartHook = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method scenarioStartHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( BeforeScenario beforeScenario : scenarioStartHookMethod.getAnnotationsByType( BeforeScenario.class ) )
                                                                          {
                                                                             String[] tags = beforeScenario.value();
                                                                             processTaggedHook( taggedScenarioStartHooks, tags, scenarioStartHookMethod, String.class, String.class, PreparedScenario.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing scenario start hook from method  " + scenarioStartHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   private final Consumer<Method> processTaggedScenarioStopHook  = new Consumer<Method>()
                                                                 {
                                                                    @Override
                                                                    public void accept( Method scenarioStopHookMethod )
                                                                    {
                                                                       try
                                                                       {
                                                                          for ( AfterScenario afterScenario : scenarioStopHookMethod.getAnnotationsByType( AfterScenario.class ) )
                                                                          {
                                                                             String[] tags = afterScenario.value();
                                                                             processTaggedHook( taggedScenarioStopHooks, tags, scenarioStopHookMethod, String.class, String.class, PreparedScenario.class );
                                                                          }
                                                                       }
                                                                       catch ( Throwable t )
                                                                       {
                                                                          log.error( "Exception while parsing scenario stop hook from method  " + scenarioStopHookMethod.getName(), t );
                                                                       }
                                                                    }
                                                                 };

   @Initializer
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }
      log.info( "Initializing " + PLUGIN_NAME + " plugin" );

      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, StepDefinition.class, AnnotationScanner.IS_PUBLIC, null, null, processStepDefinition );
      AnnotationScanner.forEachMethod( chaosActionDefinitionPackageNames, ChaosActionDefinition.class, AnnotationScanner.IS_PUBLIC, null, null, processChaosDefinition );

      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, BeforeRun.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedRunStartHook );
      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, AfterRun.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedRunStopHook );
      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, BeforeFeature.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedFeatureStartHook );
      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, AfterFeature.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedFeatureStopHook );
      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, BeforeScenario.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedScenarioStartHook );
      AnnotationScanner.forEachMethod( stepDefinitionPackageNames, AfterScenario.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedScenarioStopHook );

      initialized = true;
      return true;
   }

   @Override
   public TestFeature parseFeatureSource( String sourceString ) throws Throwable
   {
      return ParserUtils.getYamlObjectMapper().readValue( sourceString, TestFeature.class );
   }

   @Override
   public String sanitizeStepIdentifier( String stepIdentifier )
   {
      // Handle null stepIdentifier
      if ( StringUtils.isBlank( stepIdentifier ) )
      {
         return stepIdentifier;
      }

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
   public StepResult runStep( PreparedStep testStep ) throws TestFailureException
   {
      StepResult result = new StepResult();
      TestExecutionContext testExecutionContext = testStep.getTestExecutionContext();

      log.debug( "Step run" + testStep );

      if ( StringUtils.isBlank( testStep.getIdentifier() ) )
      {
         log.error( "Empty step definition identifier for step " + testStep );
         return result;
      }

      String stepIdentifier = testStep.getIdentifier();

      // Fetch the positional argument names
      ArrayList<String> inlineStepDefinitionParameterNames = new ArrayList<String>();
      Matcher matcher = testDataPattern.matcher( testStep.getIdentifier().trim() );
      while ( matcher.find() )
      {
         inlineStepDefinitionParameterNames.add( matcher.group() );
      }

      stepIdentifier = sanitizeStepIdentifier( stepIdentifier );
      if ( !stepHandlerMap.containsKey( stepIdentifier ) )
      {
         // TODO: Handling undefined step to ask manual action(other configured handlers) if possible
         String errorMessage = "Missing step definition: " + stepIdentifier;
         log.error( errorMessage );
         String positionalParameters = "";

         int i = 0;
         for ( String inlineStepDefinitionParameterName : inlineStepDefinitionParameterNames )
         {
            positionalParameters = positionalParameters + ", Serializable posArg" + ( i++ ) + " /*= " + inlineStepDefinitionParameterName + "*/";
         }
         log.error( "Suggestion:\r\n   @StepDefinition( \"" + StringEscapeUtils.escapeJava( stepIdentifier ) + "\" )\r\n" + "   public StepResult " + stepIdentifier.replaceAll( Constants.REGEX_WHITESPACE, Constants.UNDERSCORE )
                    + "( TestExecutionContext context " + positionalParameters + ") throws Throwable\r\n" + "   {\r\n...\r\n   }" );
         return StandardStepResults.error( errorMessage );
      }

      try
      {
         HashMap<String, Serializable> testData = testExecutionContext.getData();
         HashMap<String, Serializable> variables = testExecutionContext.getVariables();

         ArrayList<Object> values = new ArrayList<Object>();

         Pair<Object, Method> stepDefHandlerObjectMethodPair = stepHandlerMap.get( stepIdentifier );

         Object stepDefObject = stepDefHandlerObjectMethodPair.getLeft();
         Method stepDefMethodToInvoke = stepDefHandlerObjectMethodPair.getRight();

         Parameter[] parametersObj = stepDefMethodToInvoke.getParameters();

         for ( int i = 0, positionalArg = 0; i < parametersObj.length; i++ )
         {
            String name = parametersObj[i].getName();

            Class<?> paramType = parametersObj[i].getType();
            if ( paramType == TestExecutionContext.class )
            {
               values.add( testExecutionContext );
               continue;
            }

            TestData testDataAnnotation = parametersObj[i].getAnnotation( TestData.class );
            ContextBean contextBeanAnnotation = parametersObj[i].getAnnotation( ContextBean.class );
            ContextVariable contextVariableAnnotation = parametersObj[i].getAnnotation( ContextVariable.class );

            if ( testDataAnnotation != null )
            {
               name = testDataAnnotation.value();
               values.add( objectMapper.convertValue( testData.get( name ), parametersObj[i].getType() ) );
            }
            else if ( contextBeanAnnotation != null )
            {
               name = contextBeanAnnotation.value();
               BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();
               if ( beanRegistry != null )
               {
                  values.add( beanRegistry.get( name ) );
               }
               else
               {
                  values.add( null );
               }
            }
            else if ( contextVariableAnnotation != null )
            {
               name = contextVariableAnnotation.value();
               values.add( objectMapper.convertValue( variables.get( name ), parametersObj[i].getType() ) );
            }
            else
            {
               values.add( ParserUtils.getObjectMapper().readValue( inlineStepDefinitionParameterNames.get( positionalArg++ ), paramType ) );
            }
         }

         Class<?> returnType = stepDefMethodToInvoke.getReturnType();
         Object returnValue = values.isEmpty() ? stepDefMethodToInvoke.invoke( stepDefObject ) : stepDefMethodToInvoke.invoke( stepDefObject, values.toArray() );

         if ( returnType.equals( StepResult.class ) )
         {
            result = (StepResult) returnValue;
         }
         else if ( boolean.class.isAssignableFrom( returnType ) )
         {
            result = StepResult.builder().successful( (boolean) returnValue ).build();
         }
         else
         {
            result.setSuccessful( true );
         }
      }
      catch ( Throwable t )
      {
         String errorMessage = "Exception occured while running step definition " + testStep;
         log.error( errorMessage, t );
         result = StandardStepResults.error( errorMessage, t );
      }

      result.setEndTime( new Date() );
      return result;
   }

   @Override
   public StepResult performChaosAction( PreparedChaosAction preparedChaosAction ) throws TestFailureException
   {
      StepResult result = new StepResult();

      TestExecutionContext testExecutionContext = preparedChaosAction.getTestExecutionContext();
      HashMap<String, Serializable> testData = testExecutionContext.getData();
      HashMap<String, Serializable> variables = testExecutionContext.getVariables();

      log.debug( "Chaos actions run" + preparedChaosAction );

      if ( StringUtils.isBlank( preparedChaosAction.getName() ) )
      {
         log.error( "Empty chaos action name " + preparedChaosAction );
         return result;
      }

      String chaosActionName = preparedChaosAction.getName();

      try
      {
         if ( !chaosActionHandlerMap.containsKey( chaosActionName ) )
         {
            // TODO: Handling undefined chaos action to ask manual action(other configured handlers) if possible
            String errorMessage = "Missing chaos action handler definition: " + chaosActionName;
            log.error( errorMessage );
            log.error( "Suggestion:\r\n   @ChaosActionDefinition( \"" + StringEscapeUtils.escapeJava( chaosActionName ) + "\" )\r\n" + "   public StepResult " + chaosActionName.replaceAll( Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE )
                       + "( TestExecutionContext context, PreparedChaosAction actionToPerform) throws Throwable\r\n" + "   {\r\n...\r\n   }" );
            return StandardStepResults.error( errorMessage );
         }

         if ( chaosActionHandlerMap.containsKey( chaosActionName ) )
         {
            Pair<Object, Method> chaosActionHandlerObjectMethodPair = chaosActionHandlerMap.get( chaosActionName );

            Object chaosActionHandlerObject = chaosActionHandlerObjectMethodPair.getLeft();
            Method chaosActionHandlerMethodToInvoke = chaosActionHandlerObjectMethodPair.getRight();

            Parameter[] parametersObj = chaosActionHandlerMethodToInvoke.getParameters();
            ArrayList<Object> values = new ArrayList<Object>();
            values.add( testExecutionContext );
            values.add( preparedChaosAction );

            if ( parametersObj.length > 2 )
            {
               for ( int i = 2; i < parametersObj.length; i++ )
               {
                  String name = parametersObj[i].getName();
                  // TestData paramaterNameInfo = parametersObj[i].getAnnotation( TestData.class );

                  TestData testDataAnnotation = parametersObj[i].getAnnotation( TestData.class );
                  ContextBean contextBeanAnnotation = parametersObj[i].getAnnotation( ContextBean.class );
                  ContextVariable contextVariableAnnotation = parametersObj[i].getAnnotation( ContextVariable.class );

                  if ( testDataAnnotation != null )
                  {
                     name = testDataAnnotation.value();
                     values.add( objectMapper.convertValue( testData.get( name ), parametersObj[i].getType() ) );
                  }
                  else if ( contextBeanAnnotation != null )
                  {
                     name = contextBeanAnnotation.value();
                     BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();
                     if ( beanRegistry != null )
                     {
                        values.add( beanRegistry.get( name ) );
                     }
                     else
                     {
                        values.add( null );
                     }
                  }
                  else if ( contextVariableAnnotation != null )
                  {
                     name = contextVariableAnnotation.value();
                     values.add( objectMapper.convertValue( variables.get( name ), parametersObj[i].getType() ) );
                  }
               }
            }

            Class<?> returnType = chaosActionHandlerMethodToInvoke.getReturnType();
            Object returnValue = chaosActionHandlerMethodToInvoke.invoke( chaosActionHandlerObject, values.toArray() );

            if ( returnType.equals( StepResult.class ) )
            {
               result = (StepResult) returnValue;
            }
            else if ( boolean.class.isAssignableFrom( returnType ) )
            {
               result = StepResult.builder().successful( (boolean) returnValue ).build();
            }
            else
            {
               result.setSuccessful( true );
            }
         }
      }
      catch ( Throwable t )
      {
         String errorMessage = "Exception occured while running chaos action " + preparedChaosAction;
         log.error( errorMessage, t );
         result = StandardStepResults.error( errorMessage, t );
      }

      result.setEndTime( new Date() );
      return result;
   }

   public boolean invokeTaggedMethods( HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedHooksList, HashSet<String> tags, Object... parameters )
   {
      HashSet<Method> alreadyInvokedMethods = new HashSet<Method>();

      for ( String tag : tags )
      {
         for ( Entry<Pattern, ArrayList<Pair<Object, Method>>> patternHooksEntrySet : taggedHooksList.entrySet() )
         {
            Pattern tagPattern = patternHooksEntrySet.getKey();

            if ( tagPattern.matcher( tag ).matches() )
            {
               for ( Pair<Object, Method> objectMethodPair : patternHooksEntrySet.getValue() )
               {
                  Object hookObject = objectMethodPair.getLeft();
                  Method hookMethodToInvoke = objectMethodPair.getRight();

                  if ( alreadyInvokedMethods.contains( hookMethodToInvoke ) )
                  {
                     // Already called feature start method for another tag
                     continue;
                  }

                  alreadyInvokedMethods.add( hookMethodToInvoke );

                  try
                  {
                     hookMethodToInvoke.invoke( hookObject, parameters );
                  }
                  catch ( Throwable e )
                  {
                     log.error( "", e );
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   @Override
   public boolean runStart( String runName, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedRunStartHooks, tags, runName );
      }
      return true;
   }

   @Override
   public boolean runStop( String runName, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedRunStopHooks, tags, runName );
      }
      return true;
   }

   @Override
   public boolean featureStart( String runName, TestFeature feature, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedFeatureStartHooks, tags, runName, feature );
      }
      return true;
   }

   @Override
   public boolean scenarioStart( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedScenarioStartHooks, tags, runName, featureName, scenario );
      }
      return true;
   }

   @Override
   public boolean scenarioStop( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedScenarioStopHooks, tags, runName, featureName, scenario );
      }
      return true;
   }

   @Override
   public boolean featureStop( String runName, TestFeature feature, HashSet<String> tags )
   {
      if ( tags != null )
      {
         return invokeTaggedMethods( taggedFeatureStopHooks, tags, runName, feature );
      }
      return true;
   }

}
