package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Feature;
import org.mvss.karta.framework.core.javatest.FeatureSetup;
import org.mvss.karta.framework.core.javatest.FeatureTearDown;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.randomization.GenericObjectWithChance;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JavaFeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownStartEvent;
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
public class JavaFeatureRunner
{
   private static Random             random                        = new Random();

   private KartaRuntime              kartaRuntime;
   private ArrayList<TestDataSource> testDataSources;

   @Builder.Default
   private Boolean                   chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean                   exclusiveScenarioPerIteration = false;

   public boolean run( String runName, String javaTest, String javaTestJarFile )
            throws KartaFrameworkException, ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      return run( runName, javaTest, javaTestJarFile, 1, 1 );
   }

   public boolean run( String runName, String javaTest, String javaTestJarFile, long numberOfIterations, int numberOfIterationsInParallel )
            throws KartaFrameworkException, ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      HashMap<String, HashMap<String, Serializable>> testProperties = kartaRuntime.getConfigurator().getPropertiesStore();
      KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

      HashSet<Object> beans = new HashSet<Object>();

      beans.add( kartaRuntime.getConfigurator() );
      beans.add( eventProcessor );
      beans.add( nodeRegistry );

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
      Configurator.loadBeans( testCaseObject, beans );

      for ( Method classMethod : classMethods )
      {
         if ( classMethod.getReturnType() == StepResult.class )
         {
            Parameter[] parameters = classMethod.getParameters();
            if ( ( parameters.length == 1 ) && ( parameters[0].getType() == TestExecutionContext.class ) )
            {
               if ( classMethod.isAnnotationPresent( FeatureSetup.class ) )
               {
                  FeatureSetup annotation = classMethod.getAnnotation( FeatureSetup.class );
                  addMethodToMapInSequence( classMethod, featureSetupMethodsMap, annotation.sequence(), 1.0f );
               }
               if ( classMethod.isAnnotationPresent( ScenarioSetup.class ) )
               {
                  ScenarioSetup annotation = classMethod.getAnnotation( ScenarioSetup.class );
                  addMethodToMapInSequence( classMethod, scenarioSetupMethodsMap, annotation.sequence(), 1.0f );
               }
               if ( classMethod.isAnnotationPresent( Scenario.class ) )
               {
                  Scenario annotation = classMethod.getAnnotation( Scenario.class );
                  addMethodToMapInSequence( new GenericObjectWithChance<Method>( classMethod, annotation.probability() ), scenarioMethodsMap, annotation.sequence(), annotation.probability() );
               }
               if ( classMethod.isAnnotationPresent( ScenarioTearDown.class ) )
               {
                  ScenarioTearDown annotation = classMethod.getAnnotation( ScenarioTearDown.class );
                  addMethodToMapInSequence( classMethod, scenarioTearDownMethodsMap, annotation.sequence(), 1.0f );
               }
               if ( classMethod.isAnnotationPresent( FeatureTearDown.class ) )
               {
                  FeatureTearDown annotation = classMethod.getAnnotation( FeatureTearDown.class );
                  addMethodToMapInSequence( classMethod, featureTearDownMethodsMap, annotation.sequence(), 1.0f );
               }
            }
         }
      }

      ArrayList<Method> featureSetupMethods = generateStageMethodSequence( featureSetupMethodsMap );
      ArrayList<Method> scenarioSetupMethods = generateStageMethodSequence( scenarioSetupMethodsMap );
      ArrayList<GenericObjectWithChance<Method>> scenarioMethods = generateStageMethodSequence( scenarioMethodsMap );
      ArrayList<Method> scenarioTearDownMethods = generateStageMethodSequence( scenarioTearDownMethodsMap );
      ArrayList<Method> featureTearDownMethods = generateStageMethodSequence( featureTearDownMethodsMap );

      Configurator.loadProperties( testProperties, testCaseObject );

      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      int iterationIndex = -1;
      TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, Constants.FEATURE_SETUP, Constants.GENERIC_STEP, testData, variables );

      HashMap<Method, AtomicInteger> scenarioIterationIndexMap = new HashMap<Method, AtomicInteger>();
      scenarioMethods.forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario.getObject(), new AtomicInteger() ) );

      eventProcessor.raiseEvent( new JavaFeatureStartEvent( runName, featureName ) );

      if ( !runTestMethods( eventProcessor, testDataSources, runName, featureName, Constants.FEATURE_SETUP, true, true, testCaseObject, testExecutionContext, featureSetupMethods, iterationIndex ) )
      {
         eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName ) );
         return false;
      }

      boolean scenariosResult = true;

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
                  .scenarioIterationIndexMap( scenarioIterationIndexMap ).variables( DataUtils.cloneMap( variables ) ).build();

         if ( numberOfIterationsInParallel == 1 )
         {
            iterationRunner.run();
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
      testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, Constants.FEATURE_TEARDOWN, Constants.GENERIC_STEP, testData, variables );

      if ( !runTestMethods( eventProcessor, testDataSources, runName, featureName, Constants.FEATURE_TEARDOWN, true, false, testCaseObject, testExecutionContext, featureTearDownMethods, iterationIndex ) )
      {
         eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName ) );
         return false;
      }

      eventProcessor.raiseEvent( new JavaFeatureCompleteEvent( runName, featureName ) );
      return scenariosResult;
   }

   public static <T> void addMethodToMapInSequence( T method, TreeMap<Integer, ArrayList<T>> map, Integer sequence, float probability )
   {
      if ( map == null )
      {
         map = new TreeMap<Integer, ArrayList<T>>();
      }

      if ( !map.containsKey( sequence ) )
      {
         map.put( sequence, new ArrayList<T>() );
      }

      map.get( sequence ).add( method );
   }

   public static <T> ArrayList<T> generateStageMethodSequence( TreeMap<Integer, ArrayList<T>> stageMethodMap )
   {
      ArrayList<T> stageMethodSequence = new ArrayList<T>();

      if ( stageMethodMap.containsKey( 0 ) )
      {
         stageMethodMap.put( stageMethodMap.size(), stageMethodMap.remove( 0 ) );
      }

      for ( ArrayList<T> methodsOfSequence : stageMethodMap.values() )
      {
         stageMethodSequence.addAll( methodsOfSequence );
      }

      return stageMethodSequence;
   }

   public static boolean runTestMethod( EventProcessor eventProcessor, ArrayList<TestDataSource> testDataSources, String runName, String featureName, String scenarioName, boolean featureOrScenarioLevel, boolean setupOrTearDown, Object testCaseObject,
                                        TestExecutionContext testExecutionContext, Method methodToInvoke, int iterationNumber )
   {
      StepResult result;

      eventProcessor.raiseEvent( featureOrScenarioLevel ? ( setupOrTearDown ? new JavaFeatureSetupStartEvent( runName, featureName, methodToInvoke.getName() ) : new JavaFeatureTearDownStartEvent( runName, featureName, methodToInvoke.getName() ) )
               : ( setupOrTearDown ? new JavaScenarioSetupStartEvent( runName, featureName, iterationNumber, methodToInvoke.getName(), scenarioName )
                        : new JavaScenarioTearDownStartEvent( runName, featureName, iterationNumber, methodToInvoke.getName(), scenarioName ) ) );

      try
      {
         HashMap<String, Serializable> testData = KartaRuntime.getMergedTestData( runName, null, testDataSources, new ExecutionStepPointer( featureName, methodToInvoke.getName(), null, iterationNumber, -1 ) );
         testExecutionContext.setData( testData );

         Object resultReturned = methodToInvoke.invoke( testCaseObject, testExecutionContext );

         Class<?> returnType = methodToInvoke.getReturnType();
         if ( returnType == StepResult.class )
         {
            result = (StepResult) resultReturned;
         }
         else
         {
            result = StepResult.builder().successsful( ( returnType == boolean.class ) ? ( (boolean) resultReturned ) : true ).build();
         }
      }
      catch ( Throwable t )
      {
         result = StandardStepResults.failure( t );
         log.error( t );
      }

      eventProcessor.raiseEvent( featureOrScenarioLevel
               ? ( setupOrTearDown ? new JavaFeatureSetupCompleteEvent( runName, featureName, methodToInvoke.getName(), result ) : new JavaFeatureTearDownCompleteEvent( runName, featureName, methodToInvoke.getName(), result ) )
               : ( setupOrTearDown ? new JavaScenarioSetupCompleteEvent( runName, featureName, iterationNumber, methodToInvoke.getName(), scenarioName, result )
                        : new JavaScenarioTearDownCompleteEvent( runName, featureName, iterationNumber, methodToInvoke.getName(), scenarioName, result ) ) );
      return result.isSuccesssful();
   }

   public static boolean runTestMethods( EventProcessor eventProcessor, ArrayList<TestDataSource> testDataSources, String runName, String featureName, String scenarioName, boolean featureOrScenarioLevel, boolean setupOrTearDown, Object testCaseObject,
                                         TestExecutionContext testExecutionContext, ArrayList<Method> methodsOfSequence, int iterationNumber )
   {

      for ( Method methodToInvoke : methodsOfSequence )
      {
         String scenarioPrefixName = testExecutionContext.getScenarioName();
         if ( StringUtils.isEmpty( scenarioPrefixName ) )
         {
            scenarioPrefixName = ( featureOrScenarioLevel ? Constants.GENERIC_FEATURE : Constants.GENERIC_SCENARIO );
         }
         scenarioPrefixName = scenarioPrefixName + ( setupOrTearDown ? Constants._SETUP_ : Constants._TEARDOWN_ );
         testExecutionContext.setScenarioName( scenarioPrefixName + methodToInvoke.getName() );
         if ( !runTestMethod( eventProcessor, testDataSources, runName, featureName, scenarioName, featureOrScenarioLevel, setupOrTearDown, testCaseObject, testExecutionContext, methodToInvoke, iterationNumber ) )
         {
            return false;
         }
      }

      return true;
   }
}
