package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JavaScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioStartEvent;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

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
public class JavaIterationRunner implements Runnable
{
   private KartaRuntime                   kartaRuntime;
   private ArrayList<TestDataSource>      testDataSources;

   private Object                         testCaseObject;
   private ArrayList<Method>              scenarioSetupMethods;
   private ArrayList<Method>              scenariosMethodsToRun;
   private ArrayList<Method>              scenarioTearDownMethods;

   private String                         runName;
   private String                         featureName;
   private String                         featureDescription;

   private int                            iterationIndex;

   private HashMap<Method, AtomicInteger> scenarioIterationIndexMap;

   @Builder.Default
   private HashMap<String, Serializable>  variables = new HashMap<String, Serializable>();

   @Override
   public void run()
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      // HashMap<String, HashMap<String, Serializable>> testProperties = kartaRuntime.getConfigurator().getPropertiesStore();

      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();

      nextScenarioMethod: for ( Method scenarioMethod : scenariosMethodsToRun )
      {
         int scenarioIterationNumber = ( ( scenarioIterationIndexMap != null ) && ( scenarioIterationIndexMap.containsKey( scenarioMethod ) ) ) ? scenarioIterationIndexMap.get( scenarioMethod ).getAndIncrement() : 0;

         String scenarioName = Constants.__GENERIC_SCENARIO__;
         if ( scenarioMethod.isAnnotationPresent( Scenario.class ) )
         {
            scenarioName = scenarioMethod.getAnnotation( Scenario.class ).value();
         }

         TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, scenarioName, Constants.__GENERIC_STEP__, testData, variables );

         StepResult result;
         try
         {
            if ( scenarioSetupMethods != null )
            {
               if ( !JavaFeatureRunner.runTestMethods( eventProcessor, testDataSources, runName, featureName, scenarioName, false, true, testCaseObject, testExecutionContext, scenarioSetupMethods, scenarioIterationNumber ) )
               {
                  continue nextScenarioMethod;
               }
            }

            eventProcessor.raiseEvent( new JavaScenarioStartEvent( scenarioName, featureName, iterationIndex, scenarioMethod.getName(), scenarioName ) );

            testData = KartaRuntime.getMergedTestData( runName, null, null, testDataSources, new ExecutionStepPointer( featureName, scenarioName, null, scenarioIterationNumber, 0 ) );
            testExecutionContext.setData( testData );

            Object resultReturned = scenarioMethod.invoke( testCaseObject, testExecutionContext );

            Class<?> returnType = scenarioMethod.getReturnType();
            if ( returnType == StepResult.class )
            {
               result = (StepResult) resultReturned;
            }
            else
            {
               result = StepResult.builder().successsful( ( returnType == boolean.class ) ? ( (boolean) resultReturned ) : true ).build();
            }

            eventProcessor.raiseEvent( new JavaScenarioCompleteEvent( scenarioName, scenarioName, iterationIndex, scenarioMethod.getName(), scenarioName, result ) );

            if ( scenarioTearDownMethods != null )
            {
               if ( !JavaFeatureRunner.runTestMethods( eventProcessor, testDataSources, runName, featureName, scenarioName, false, false, testCaseObject, testExecutionContext, scenarioTearDownMethods, scenarioIterationNumber ) )
               {
                  continue nextScenarioMethod;
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( t );
            log.error( ExceptionUtils.getStackTrace( t ) );
            result = StandardStepResults.failure( t );
         }
      }
   }
}
