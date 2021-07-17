package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.SerializableKVP;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Feature;
import org.mvss.karta.framework.core.javatest.FeatureSetup;
import org.mvss.karta.framework.core.javatest.FeatureTearDown;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.randomization.GenericObjectWithChance;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JavaFeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownStartEvent;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;

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
@Builder
public class JavaFeatureRunner implements Callable<FeatureResult>
{
   private KartaRuntime            kartaRuntime;
   private RunInfo                 runInfo;

   private String                  javaTest;
   private String                  javaTestJarFile;

   private Consumer<FeatureResult> resultConsumer;

   private FeatureResult           result;

   @PropertyMapping( value = "detailedResults" )
   private static boolean          detailedResults = false;

   private synchronized void accumulateIterationResult( HashMap<String, ScenarioResult> iterationResult )
   {
      result.addIterationResult( iterationResult, detailedResults );
   }

   @Override
   public FeatureResult call()
   {
      try
      {
         String runName = runInfo.getRunName();
         long numberOfIterations = runInfo.getNumberOfIterations();
         int numberOfIterationsInParallel = runInfo.getNumberOfIterationsInParallel();
         boolean chanceBasedScenarioExecution = runInfo.isChanceBasedScenarioExecution();
         boolean exclusiveScenarioPerIteration = runInfo.isExclusiveScenarioPerIteration();

         ArrayList<TestDataSource> testDataSources = kartaRuntime.getTestDataSources( runInfo );

         result = new FeatureResult();

         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         Random random = kartaRuntime.getRandom();

         boolean loadClassFromJar = StringUtils.isNotBlank( javaTestJarFile ) && Files.exists( Paths.get( javaTestJarFile ) );

         Class<?> testCaseClass = loadClassFromJar ? (Class<?>) DynamicClassLoader.loadClass( javaTestJarFile, javaTest )
                  : (Class<?>) Class.forName( javaTest );

         Feature featureAnnotation = testCaseClass.getAnnotation( Feature.class );

         if ( featureAnnotation == null )
         {
            throw new KartaFrameworkException( "The class " + testCaseClass + " is not annotated as a feature. " );
         }

         String featureName = featureAnnotation.value();
         String featureDescription = featureAnnotation.description();
         result.setFeatureName( featureName );

         Method[] classMethods = testCaseClass.getMethods();
         TreeMap<Integer, ArrayList<Method>> featureSetupMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<Method>> scenarioSetupMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<GenericObjectWithChance<Method>>> scenarioMethodsMap = new TreeMap<Integer, ArrayList<GenericObjectWithChance<Method>>>();
         TreeMap<Integer, ArrayList<Method>> scenarioTearDownMethodsMap = new TreeMap<Integer, ArrayList<Method>>();
         TreeMap<Integer, ArrayList<Method>> featureTearDownMethodsMap = new TreeMap<Integer, ArrayList<Method>>();

         Object testCaseObject = testCaseClass.getDeclaredConstructor().newInstance();
         kartaRuntime.initializeObject( testCaseObject );

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
                  DataUtils.addItemToTreeMapInSequence( new GenericObjectWithChance<Method>( classMethod, annotation
                           .probability() ), scenarioMethodsMap, annotation.sequence() );
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

         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         int iterationIndex = -1;

         HashMap<Method, AtomicInteger> scenarioIterationIndexMap = new HashMap<Method, AtomicInteger>();
         scenarioMethods.forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario.getObject(), new AtomicInteger() ) );

         eventProcessor.raiseEvent( new JavaFeatureStartEvent( runName, featureName ) );

         long stepIndex = 0;
         for ( Method methodToInvoke : featureSetupMethods )
         {
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

            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, Constants.__FEATURE_SETUP__,
                                                                                  stepName, null, variables );
            StepResult stepResult = runTestMethod( kartaRuntime, testDataSources, testCaseObject, testExecutionContext, methodToInvoke );
            stepResult.setStepIndex( stepIndex++ );
            eventProcessor.raiseEvent( new JavaFeatureSetupCompleteEvent( runName, featureName, stepName, stepResult ) );
            result.getSetupResults().add( new SerializableKVP<String, StepResult>( stepName, stepResult ) );
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

         ExecutorService iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L,
                                                                             TimeUnit.MILLISECONDS,
                                                                             new BlockingRunnableQueue( numberOfIterationsInParallel ) );

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
                  scenariosMethodsToRun
                           .addAll( GenericObjectWithChance.extractObjects( RandomizationUtils.generateNextComposition( random, scenarioMethods ) ) );
               }
            }
            else
            {
               scenariosMethodsToRun = GenericObjectWithChance.extractObjects( scenarioMethods );
            }

            JavaIterationRunner iterationRunner = JavaIterationRunner.builder().kartaRuntime( kartaRuntime ).testCaseObject( testCaseObject )
                     .scenarioSetupMethods( scenarioSetupMethods ).scenariosMethodsToRun( scenariosMethodsToRun )
                     .scenarioTearDownMethods( scenarioTearDownMethods ).runInfo( runInfo ).featureName( featureName )
                     .featureDescription( featureDescription ).iterationIndex( iterationIndex ).scenarioIterationIndexMap( scenarioIterationIndexMap )
                     .variables( DataUtils.cloneMap( variables ) )
                     .resultConsumer( ( iterationResult ) -> accumulateIterationResult( iterationResult ) ).build();

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

         stepIndex = 0;
         for ( Method methodToInvoke : featureTearDownMethods )
         {
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

            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex,
                                                                                  Constants.__FEATURE_TEARDOWN__, stepName, null, variables );
            StepResult stepResult = runTestMethod( kartaRuntime, testDataSources, testCaseObject, testExecutionContext, methodToInvoke );
            stepResult.setStepIndex( stepIndex++ );

            eventProcessor.raiseEvent( new JavaFeatureTearDownCompleteEvent( runName, featureName, stepName, stepResult ) );
            result.getTearDownResults().add( new SerializableKVP<String, StepResult>( stepName, stepResult ) );
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

   public static StepResult runTestMethod( KartaRuntime kartaRuntime, ArrayList<TestDataSource> testDataSources, Object testCaseObject,
                                           TestExecutionContext testExecutionContext, Method methodToInvoke )
            throws Throwable
   {
      Date startTime = new Date();
      StepResult stepResult;
      testExecutionContext.mergeTestData( null, null, testDataSources );

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

      if ( stepResult.getEndTime() == null )
      {
         stepResult.setEndTime( new Date() );
      }

      kartaRuntime.processStepResult( startTime, stepResult, testExecutionContext );
      return stepResult;
   }
}
