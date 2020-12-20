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
import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.Pair;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.core.StepParam;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.BeanRegistry;
import org.mvss.karta.framework.runtime.Configurator;
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
   private BeanRegistry                                      beanRegistry;

   @KartaAutoWired
   private Configurator                                      configurator;

   @KartaAutoWired
   private EventProcessor                                    eventProcessor;

   @KartaAutoWired
   private KartaMinionRegistry                               minionRegistry;

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
                                                                     Parameter[] params = candidateStepDefinitionMethod.getParameters();

                                                                     if ( !( ( params.length > 0 ) && ( TestExecutionContext.class == params[0].getType() ) ) )
                                                                     {
                                                                        log.error( "Step definition method " + methodDescription + " should have the first parameter type as TestExecutionContext" );
                                                                        continue;
                                                                     }

                                                                     int positionalArgumentsCount = 0;
                                                                     for ( int i = 1; i < params.length; i++ )
                                                                     {
                                                                        if ( params[i].getAnnotation( StepParam.class ) == null )
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
                                                                     if ( !beanRegistry.containsKey( stepDefinitionClass.getName() ) )
                                                                     {
                                                                        beanRegistry.loadStaticBeans( stepDefinitionClass );
                                                                        Object stepDefClassObj = stepDefinitionClass.newInstance();
                                                                        configurator.loadProperties( stepDefClassObj );
                                                                        beanRegistry.loadBeans( stepDefClassObj );
                                                                        beanRegistry.add( stepDefClassObj );
                                                                     }
                                                                     stepHandlerMap.put( stepDefString, new Pair<Object, Method>( beanRegistry.get( stepDefinitionClass.getName() ), candidateStepDefinitionMethod ) );
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
                                                                     Class<?>[] params = candidateChaosActionMethod.getParameterTypes();

                                                                     if ( !( ( params.length >= 2 ) && ( TestExecutionContext.class == params[0] ) && ( PreparedChaosAction.class == params[1] ) ) )
                                                                     {
                                                                        log.error( "Chaos action definition method " + methodDescription + " should have first two parameters of types(" + TestExecutionContext.class.getName() + ", "
                                                                                   + PreparedChaosAction.class.getName() + ")" );
                                                                        continue;
                                                                     }

                                                                     log.debug( "Mapping chaos action definition " + chaosActionName + " to " + methodDescription );

                                                                     Class<?> chaosActionDefinitionClass = candidateChaosActionMethod.getDeclaringClass();
                                                                     if ( !beanRegistry.containsKey( chaosActionDefinitionClass.getName() ) )
                                                                     {
                                                                        beanRegistry.loadStaticBeans( chaosActionDefinitionClass );
                                                                        Object chaosActionDefClassObj = chaosActionDefinitionClass.newInstance();
                                                                        configurator.loadProperties( chaosActionDefClassObj );
                                                                        beanRegistry.loadBeans( chaosActionDefClassObj );
                                                                        beanRegistry.add( chaosActionDefClassObj );
                                                                     }
                                                                     chaosActionHandlerMap.put( chaosActionName, new Pair<Object, Method>( beanRegistry.get( chaosActionDefinitionClass.getName() ), candidateChaosActionMethod ) );
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
      if ( !beanRegistry.containsKey( hookClass.getName() ) )
      {
         configurator.loadProperties( hookClass );
         beanRegistry.loadStaticBeans( hookClass );
         Object hookObj = hookClass.newInstance();
         configurator.loadProperties( hookObj );
         beanRegistry.loadBeans( hookObj );
         beanRegistry.add( hookObj );
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

         Pair<Object, Method> hookdef = new Pair<Object, Method>( beanRegistry.get( hookClass.getName() ), hookMethod );

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

   @Override
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
         log.error( "Suggestion:\r\n   @StepDefinition( \"" + StringEscapeUtils.escapeJava( stepIdentifier ) + "\" )\r\n" + "   public StepResult " + stepIdentifier.replaceAll( "\\s", "_" ) + "( TestExecutionContext context " + positionalParameters
                    + ") throws Throwable\r\n" + "   {\r\n...\r\n   }" );
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

         values.add( testExecutionContext );

         if ( parametersObj.length > 1 )
         {
            for ( int i = 1, positionalArg = 0; i < parametersObj.length; i++ )
            {
               String name = parametersObj[i].getName();
               StepParam paramaterNameInfo = parametersObj[i].getAnnotation( StepParam.class );

               if ( paramaterNameInfo == null )
               {
                  values.add( ParserUtils.getObjectMapper().readValue( inlineStepDefinitionParameterNames.get( positionalArg++ ), parametersObj[i++].getType() ) );
               }
               else
               {
                  name = paramaterNameInfo.value();

                  switch ( paramaterNameInfo.mapto() )
                  {
                     case CONTEXT_BEAN:
                        BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();
                        if ( beanRegistry != null )
                        {
                           values.add( beanRegistry.get( name ) );
                        }
                        else
                        {
                           values.add( null );
                        }
                        break;

                     case TESTDATA:
                        values.add( objectMapper.convertValue( testData.get( name ), parametersObj[i].getType() ) );
                        break;

                     case VARIABLE:
                        values.add( objectMapper.convertValue( variables.get( name ), parametersObj[i].getType() ) );
                        break;

                  }
               }
            }
         }

         Class<?> returnType = stepDefMethodToInvoke.getReturnType();
         Object returnValue = stepDefMethodToInvoke.invoke( stepDefObject, values.toArray() );

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
         String errorMessage = "Exception occured while running chaos action " + testStep;
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
            log.error( "Suggestion:\r\n   @ChaosActionDefinition( \"" + StringEscapeUtils.escapeJava( chaosActionName ) + "\" )\r\n" + "   public StepResult " + chaosActionName.replaceAll( "\\s", "_" )
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
                  StepParam paramaterNameInfo = parametersObj[i].getAnnotation( StepParam.class );

                  if ( paramaterNameInfo != null )
                  {
                     name = paramaterNameInfo.value();

                     switch ( paramaterNameInfo.mapto() )
                     {
                        case CONTEXT_BEAN:
                           BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();
                           if ( beanRegistry != null )
                           {
                              values.add( beanRegistry.get( name ) );
                           }
                           else
                           {
                              values.add( null );
                           }
                           break;

                        case TESTDATA:
                           values.add( objectMapper.convertValue( testData.get( name ), parametersObj[i].getType() ) );
                           break;

                        case VARIABLE:
                           values.add( objectMapper.convertValue( variables.get( name ), parametersObj[i].getType() ) );
                           break;

                     }
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

   public void invokeTaggedMethods( HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedHooksList, HashSet<String> tags, Object... parameters )
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
                  }
               }
            }
         }

      }
   }

   @Override
   public void runStart( String runName, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedRunStartHooks, tags, runName );
      }
   }

   @Override
   public void runStop( String runName, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedRunStopHooks, tags, runName );
      }
   }

   @Override
   public void featureStart( String runName, TestFeature feature, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedFeatureStartHooks, tags, runName, feature );
      }
   }

   @Override
   public void scenarioStart( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedScenarioStartHooks, tags, runName, featureName, scenario );
      }
   }

   @Override
   public void scenarioStop( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedScenarioStopHooks, tags, runName, featureName, scenario );
      }
   }

   @Override
   public void featureStop( String runName, TestFeature feature, HashSet<String> tags )
   {
      if ( tags != null )
      {
         invokeTaggedMethods( taggedFeatureStopHooks, tags, runName, feature );
      }
   }

}
