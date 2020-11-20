package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.javatest.Feature;
import org.mvss.karta.framework.core.javatest.FeatureSetup;
import org.mvss.karta.framework.core.javatest.FeatureTearDown;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.randomization.GenericObjectWithChance;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JavaFeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownStartEvent;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurrenceEvent;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class JavaFeatureRunner implements Callable<FeatureResult>
{
   private KartaRuntime              kartaRuntime;
   private ArrayList<TestDataSource> testDataSources;
   private String                    runName;
   private String                    javaTest;
   private String                    javaTestJarFile;

   @Builder.Default
   private long                      numberOfIterations            = 1;

   @Builder.Default
   private int                       numberOfIterationsInParallel  = 1;

   @Builder.Default
   private Boolean                   chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean                   exclusiveScenarioPerIteration = false;

   private Consumer<FeatureResult>   resultConsumer;

   private FeatureResult             result;

   private synchronized void accumulateIterationResult( HashMap<String, ScenarioResult> iterationResult )
   {
      result.addIterationResult( iterationResult );
   }

   @Override
   public FeatureResult call()
   {
      try
      {
         result = new FeatureResult();

         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         HashMap<String, HashMap<String, Serializable>> testProperties = kartaRuntime.getConfigurator().getPropertiesStore();
         BeanRegistry beanRegistry = kartaRuntime.getBeanRegistry();
         Random random = kartaRuntime.getRandom();
         Class<?> testCaseClass = StringUtils.isNotBlank( javaTestJarFile ) ? (Class<?>) DynamicClassLoader.loadClass( javaTestJarFile, javaTest ) : (Class<?>) Class.forName( javaTest );

         Feature featureAnnotation = testCaseClass.getAnnotation( Feature.class );

         if ( featureAnnotation == null )
         {
            throw new KartaFrameworkException( "The class " + testCaseClass + " is not annotated as a feature. " );
         }

         String featureName = featureAnnotation.value();
         String featureDescription = featureAnnotation.description();

         Method[] classMethods = testCaseClass.getMethods();
         TreeMap<Integer, ArrayList<Method>> featureSetupMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<Method>> scenarioSetupMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<GenericObjectWithChance<Method>>> scenarioMethodsMap = new TreeMap<Integer, ArrayList<GenericObjectWithChance<Method>>>();
         TreeMap<Integer, ArrayList<Method>> scenarioTearDownMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<Method>> featureTearDownMethodsMap = new TreeMap<Integer, ArrayList<Method>>();

         Object testCaseObject = testCaseClass.newInstance();
         beanRegistry.loadBeans( testCaseObject );

         for ( Method classMethod : classMethods )
         {
            Parameter[] parameters = classMethod.getParameters();
            if ( ( parameters.length == 1 ) && ( parameters[0].getType() == TestExecutionContext.class ) )
            {
               if ( classMethod.isAnnotationPresent( FeatureSetup.class ) )
               {
                  FeatureSetup annotation = classMethod.getAnnotation( FeatureSetup.class );
                  DataUtils.addItemToTreeMapInSequence( classMethod, featureSetupMethodsMap, annotation.sequence() );
               }
               if ( classMethod.isAnnotationPresent( ScenarioSetup.class ) )
               {
                  ScenarioSetup annotation = classMethod.getAnnotation( ScenarioSetup.class );
                  DataUtils.addItemToTreeMapInSequence( classMethod, scenarioSetupMethodsMap, annotation.sequence() );
               }
               if ( classMethod.isAnnotationPresent( Scenario.class ) )
               {
                  Scenario annotation = classMethod.getAnnotation( Scenario.class );
                  DataUtils.addItemToTreeMapInSequence( new GenericObjectWithChance<Method>( classMethod, annotation.probability() ), scenarioMethodsMap, annotation.sequence() );
               }
               if ( classMethod.isAnnotationPresent( ScenarioTearDown.class ) )
               {
                  ScenarioTearDown annotation = classMethod.getAnnotation( ScenarioTearDown.class );
                  DataUtils.addItemToTreeMapInSequence( classMethod, scenarioTearDownMethodsMap, annotation.sequence() );
               }
               if ( classMethod.isAnnotationPresent( FeatureTearDown.class ) )
               {
                  FeatureTearDown annotation = classMethod.getAnnotation( FeatureTearDown.class );
                  DataUtils.addItemToTreeMapInSequence( classMethod, featureTearDownMethodsMap, annotation.sequence() );
               }
            }
         }

         ArrayList<Method> featureSetupMethods = DataUtils.generateSequencedList( featureSetupMethodsMap );
         ArrayList<Method> scenarioSetupMethods = DataUtils.generateSequencedList( scenarioSetupMethodsMap );
         ArrayList<GenericObjectWithChance<Method>> scenarioMethods = DataUtils.generateSequencedList( scenarioMethodsMap );
         ArrayList<Method> scenarioTearDownMethods = DataUtils.generateSequencedList( scenarioTearDownMethodsMap );
         ArrayList<Method> featureTearDownMethods = DataUtils.generateSequencedList( featureTearDownMethodsMap );

         Configurator.loadProperties( testProperties, testCaseObject );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         long iterationIndex = -1;
         TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, Constants.__FEATURE_SETUP__, Constants.__GENERIC_STEP__, testData, variables );

         HashMap<Method, AtomicLong> scenarioIterationIndexMap = new HashMap<Method, AtomicLong>();
         scenarioMethods.forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario.getObject(), new AtomicLong() ) );

         eventProcessor.raiseEvent( new JavaFeatureStartEvent( runName, featureName ) );

         for ( Method methodToInvoke : featureSetupMethods )
         {
            String scenarioPrefixName = testExecutionContext.getScenarioName();
            if ( StringUtils.isEmpty( scenarioPrefixName ) )
            {
               scenarioPrefixName = Constants.__GENERIC_FEATURE__;
            }
            scenarioPrefixName = scenarioPrefixName + Constants._SETUP_;
            testExecutionContext.setScenarioName( scenarioPrefixName + methodToInvoke.getName() );

            FeatureSetup annotation = methodToInvoke.getAnnotation( FeatureSetup.class );
            String stepName = methodToInvoke.getName();
            if ( annotation != null )
            {
               if ( StringUtils.isNotBlank( annotation.value() ) )
               {
                  stepName = annotation.value();
               }
            }
            eventProcessor.raiseEvent( new JavaFeatureSetupStartEvent( runName, featureName, stepName ) );

            StepResult stepResult = runTestMethod( eventProcessor, testDataSources, runName, featureName, Constants.__FEATURE_SETUP__, testCaseObject, testExecutionContext, methodToInvoke, iterationIndex, stepName );

            eventProcessor.raiseEvent( new JavaFeatureSetupCompleteEvent( runName, featureName, stepName, stepResult ) );
            result.getSetupResultMap().put( stepName, stepResult.isPassed() );
            result.getIncidents().addAll( stepResult.getIncidents() );

            if ( !stepResult.isPassed() )
            {
               eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName, result ) );
               result.setSuccessful( false );

               if ( resultConsumer != null )
               {
                  resultConsumer.accept( result );
               }

               return result;
            }
         }

         ExecutorService iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfIterationsInParallel ) );

         for ( iterationIndex = 0; ( numberOfIterations <= 0 ) || ( iterationIndex < numberOfIterations ); iterationIndex++ )
         {
            ArrayList<Method> scenariosMethodsToRun = new ArrayList<Method>();;

            if ( chanceBasedScenarioExecution )
            {
               if ( exclusiveScenarioPerIteration )
               {
                  GenericObjectWithChance<Method> scenarioMethodSelected = RandomizationUtils.generateNextMutexComposition( random, scenarioMethods );
                  if ( scenarioMethodSelected != null )
                  {
                     scenariosMethodsToRun.add( scenarioMethodSelected.object );
                  }
                  else
                  {
                     continue;
                  }
               }
               else
               {
                  scenariosMethodsToRun.addAll( GenericObjectWithChance.extractObjects( RandomizationUtils.generateNextComposition( random, scenarioMethods ) ) );
               }
            }
            else
            {
               scenariosMethodsToRun = GenericObjectWithChance.extractObjects( scenarioMethods );
            }

            JavaIterationRunner iterationRunner = JavaIterationRunner.builder().kartaRuntime( kartaRuntime ).testDataSources( testDataSources ).testCaseObject( testCaseObject ).scenarioSetupMethods( scenarioSetupMethods )
                     .scenariosMethodsToRun( scenariosMethodsToRun ).scenarioTearDownMethods( scenarioTearDownMethods ).runName( runName ).featureName( featureName ).featureDescription( featureDescription ).iterationIndex( iterationIndex )
                     .scenarioIterationIndexMap( scenarioIterationIndexMap ).variables( DataUtils.cloneMap( variables ) ).resultConsumer( ( iterationResult ) -> accumulateIterationResult( iterationResult ) ).build();

            if ( numberOfIterationsInParallel == 1 )
            {
               iterationRunner.call();
            }
            else
            {
               iterationExecutionService.submit( iterationRunner );
            }
         }

         iterationExecutionService.shutdown();

         try
         {
            iterationExecutionService.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
         }
         catch ( InterruptedException ie )
         {
            // Ignore termination and continue on interruption
         }

         scenarioMethods.forEach( ( scenario ) -> scenarioIterationIndexMap.get( scenario.getObject() ).set( 0 ) );

         iterationIndex = -1;
         testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, Constants.__FEATURE_TEARDOWN__, Constants.__GENERIC_STEP__, testData, variables );

         for ( Method methodToInvoke : featureTearDownMethods )
         {
            String scenarioPrefixName = testExecutionContext.getScenarioName();
            if ( StringUtils.isEmpty( scenarioPrefixName ) )
            {
               scenarioPrefixName = Constants.__GENERIC_FEATURE__;
            }
            scenarioPrefixName = scenarioPrefixName + Constants._TEARDOWN_;
            testExecutionContext.setScenarioName( scenarioPrefixName + methodToInvoke.getName() );

            FeatureTearDown annotation = methodToInvoke.getAnnotation( FeatureTearDown.class );
            String stepName = methodToInvoke.getName();

            if ( annotation != null )
            {
               if ( StringUtils.isNotBlank( annotation.value() ) )
               {
                  stepName = annotation.value();
               }
            }
            eventProcessor.raiseEvent( new JavaFeatureTearDownStartEvent( runName, featureName, stepName ) );

            StepResult stepResult = runTestMethod( eventProcessor, testDataSources, runName, featureName, Constants.__FEATURE_TEARDOWN__, testCaseObject, testExecutionContext, methodToInvoke, iterationIndex, stepName );

            eventProcessor.raiseEvent( new JavaFeatureTearDownCompleteEvent( runName, featureName, stepName, stepResult ) );
            result.getTearDownResultMap().put( stepName, stepResult.isPassed() );
            result.getIncidents().addAll( stepResult.getIncidents() );

            if ( !stepResult.isPassed() )
            {
               eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName, result ) );
               result.setSuccessful( false );

               if ( resultConsumer != null )
               {
                  resultConsumer.accept( result );
               }

               return result;
            }
         }

         eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName, result ) );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         result.setError( true );
      }

      if ( resultConsumer != null )
      {
         resultConsumer.accept( result );
      }

      return result;
   }

   public static StepResult runTestMethod( EventProcessor eventProcessor, ArrayList<TestDataSource> testDataSources, String runName, String featureName, String scenarioName, Object testCaseObject,

                                           TestExecutionContext testExecutionContext, Method methodToInvoke, long iterationIndex, String stepName )
            throws Throwable

   {
      StepResult stepResult;
      HashMap<String, Serializable> testData = KartaRuntime.getMergedTestData( runName, null, null, testDataSources, new ExecutionStepPointer( featureName, Constants.__FEATURE_TEARDOWN__, stepName, iterationIndex, -1 ) );
      testExecutionContext.setData( testData );

      Object resultReturned = methodToInvoke.invoke( testCaseObject, testExecutionContext );

      Class<?> returnType = methodToInvoke.getReturnType();
      if ( returnType == StepResult.class )
      {
         stepResult = (StepResult) resultReturned;
      }
      else
      {
         stepResult = StepResult.builder().successful( ( returnType == boolean.class ) ? ( (boolean) resultReturned ) : true ).build();
      }

      for ( TestIncident incident : stepResult.getIncidents() )
      {
         eventProcessor.raiseEvent( new TestIncidentOccurrenceEvent( runName, featureName, iterationIndex, scenarioName, stepName, incident ) );
      }

      for ( Event stepEvent : stepResult.getEvents() )
      {
         eventProcessor.raiseEvent( stepEvent );
      }

      DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
      return stepResult;
   }
}
