package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.SerializableKVP;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JavaScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownStartEvent;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

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
public class JavaIterationRunner implements Callable<HashMap<String, ScenarioResult>>
{
   private KartaRuntime                              kartaRuntime;

   private Object                                    testCaseObject;
   private ArrayList<Method>                         scenarioSetupMethods;
   private ArrayList<Method>                         scenariosMethodsToRun;
   private ArrayList<Method>                         scenarioTearDownMethods;

   private RunInfo                                   runInfo;
   private String                                    featureName;
   private String                                    featureDescription;

   private int                                       iterationIndex;

   private HashMap<Method, AtomicInteger>            scenarioIterationIndexMap;

   @Builder.Default
   private HashMap<String, Serializable>             variables = new HashMap<String, Serializable>();

   private HashMap<String, ScenarioResult>           result;

   private Consumer<HashMap<String, ScenarioResult>> resultConsumer;

   @Override
   public HashMap<String, ScenarioResult> call()
   {
      result = new HashMap<String, ScenarioResult>();

      ArrayList<TestDataSource> testDataSources = kartaRuntime.getTestDataSources( runInfo );
      String runName = runInfo.getRunName();

      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();

      long stepIndex = 0;
      nextScenarioMethod: for ( Method scenarioMethod : scenariosMethodsToRun )
      {
         int scenarioIterationNumber = ( ( scenarioIterationIndexMap != null ) && ( scenarioIterationIndexMap.containsKey( scenarioMethod ) ) ) ? scenarioIterationIndexMap.get( scenarioMethod ).getAndIncrement() : 0;

         String scenarioName = scenarioMethod.getName();
         Scenario scenarioAnnotation = scenarioMethod.getAnnotation( Scenario.class );
         if ( scenarioAnnotation != null )
         {
            if ( StringUtils.isNotBlank( scenarioAnnotation.value() ) )
            {
               scenarioName = scenarioAnnotation.value();
            }
         }

         ScenarioResult scenarioResult = new ScenarioResult();
         scenarioResult.setIterationIndex( scenarioIterationNumber );
         result.put( scenarioName, scenarioResult );
         try
         {
            eventProcessor.raiseEvent( new JavaScenarioStartEvent( runName, featureName, iterationIndex, scenarioName ) );

            if ( scenarioSetupMethods != null )
            {
               long setupStepIndex = 0;
               for ( Method methodToInvoke : scenarioSetupMethods )
               {
                  String stepName = methodToInvoke.getName();
                  ScenarioSetup annotation = methodToInvoke.getAnnotation( ScenarioSetup.class );
                  if ( annotation != null )
                  {
                     if ( StringUtils.isNotBlank( annotation.value() ) )
                     {
                        stepName = annotation.value();
                     }
                  }

                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, scenarioName, stepName, null, variables );
                  eventProcessor.raiseEvent( new JavaScenarioSetupStartEvent( runName, featureName, scenarioIterationNumber, scenarioName, stepName ) );
                  StepResult stepResult = JavaFeatureRunner.runTestMethod( kartaRuntime, testDataSources, testCaseObject, testExecutionContext, methodToInvoke );
                  stepResult.setStepIndex( setupStepIndex++ );
                  eventProcessor.raiseEvent( new JavaScenarioSetupCompleteEvent( runName, featureName, scenarioIterationNumber, scenarioName, stepName, stepResult ) );
                  scenarioResult.getSetupResults().add( new SerializableKVP<String, StepResult>( stepName, stepResult ) );
                  scenarioResult.getIncidents().addAll( stepResult.getIncidents() );

                  if ( !stepResult.isPassed() )
                  {
                     scenarioResult.setSuccessful( false );
                     continue nextScenarioMethod;
                  }
               }

            }

            String stepName = scenarioName;
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, scenarioName, stepName, null, variables );
            StepResult stepResult = JavaFeatureRunner.runTestMethod( kartaRuntime, testDataSources, testCaseObject, testExecutionContext, scenarioMethod );
            stepResult.setStepIndex( stepIndex++ );
            scenarioResult.getRunResults().add( new SerializableKVP<String, StepResult>( stepName, stepResult ) );
            scenarioResult.getIncidents().addAll( stepResult.getIncidents() );

            if ( !stepResult.isPassed() )
            {
               scenarioResult.setSuccessful( false );
            }

            if ( scenarioTearDownMethods != null )
            {
               long teardownStepIndex = 0;
               for ( Method methodToInvoke : scenarioTearDownMethods )
               {
                  ScenarioTearDown annotation = methodToInvoke.getAnnotation( ScenarioTearDown.class );
                  stepName = methodToInvoke.getName();
                  if ( annotation != null )
                  {
                     if ( StringUtils.isNotBlank( annotation.value() ) )
                     {
                        stepName = annotation.value();
                     }
                  }

                  testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, scenarioName, stepName, null, variables );
                  eventProcessor.raiseEvent( new JavaScenarioTearDownStartEvent( runName, featureName, scenarioIterationNumber, scenarioName, stepName ) );
                  stepResult = JavaFeatureRunner.runTestMethod( kartaRuntime, testDataSources, testCaseObject, testExecutionContext, methodToInvoke );
                  stepResult.setStepIndex( teardownStepIndex++ );
                  eventProcessor.raiseEvent( new JavaScenarioTearDownCompleteEvent( runName, featureName, scenarioIterationNumber, scenarioName, stepName, stepResult ) );
                  scenarioResult.getTearDownResults().add( new SerializableKVP<String, StepResult>( stepName, stepResult ) );
                  scenarioResult.getIncidents().addAll( stepResult.getIncidents() );

                  if ( !stepResult.isPassed() )
                  {
                     scenarioResult.setSuccessful( false );
                     continue nextScenarioMethod;
                  }
               }
            }

            eventProcessor.raiseEvent( new JavaScenarioCompleteEvent( runName, featureName, iterationIndex, scenarioName, scenarioResult ) );
         }
         catch ( Throwable t )
         {
            log.error( Constants.EMPTY_STRING, t );
            scenarioResult.setError( true );
         }
      }

      if ( resultConsumer != null )
      {
         resultConsumer.accept( result );
      }

      return result;
   }
}
