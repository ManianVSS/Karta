package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.GenericTestEvent;
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
   private ArrayList<TestDataSource>                      testDataSources;
   private HashMap<String, HashMap<String, Serializable>> testProperties;
   private EventProcessor                                 eventProcessor;
   private KartaMinionRegistry                            minionRegistry;

   private Object                                         testCaseObject;
   private ArrayList<Method>                              scenarioSetupMethods;
   private ArrayList<Method>                              scenariosMethodsToRun;
   private ArrayList<Method>                              scenarioTearDownMethods;

   private String                                         runName;
   private String                                         featureName;
   private String                                         featureDescription;

   private long                                           iterationIndex;

   @Override
   public void run()
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

      nextScenarioMethod: for ( Method scenarioMethod : scenariosMethodsToRun )
      {
         String scenarioName = "Unnamed";
         if ( scenarioMethod.isAnnotationPresent( Scenario.class ) )
         {
            scenarioName = scenarioMethod.getAnnotation( Scenario.class ).value();
         }

         StepResult result;
         try
         {
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario started " + scenarioName ) );

            if ( scenarioSetupMethods != null )
            {
               if ( !JavaFeatureRunner.runTestMethods( eventProcessor, testDataSources, runName, featureName, featureDescription, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario "
                                                                                                                                  + " setup", testCaseObject, testExecutionContext, scenarioSetupMethods ) )
               {
                  continue nextScenarioMethod;
               }
            }

            eventProcessor.raiseEvent( new GenericTestEvent( runName, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario steps started " + featureName + " " + scenarioMethod.getName() ) );

            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
            testExecutionContext.setData( testData );

            Object resultReturned = scenarioMethod.invoke( testCaseObject, testExecutionContext );

            Class<?> returnType = scenarioMethod.getReturnType();
            if ( returnType == StepResult.class )
            {
               result = (StepResult) resultReturned;
            }
            else
            {
               result = new StepResult( ( returnType == boolean.class ) ? ( (boolean) resultReturned ) : true, null, null, null );
            }

            eventProcessor.raiseEvent( new GenericTestEvent( runName, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario completed" + featureName + " " + scenarioMethod.getName() + " Result: " + result.toString() ) );

            if ( scenarioTearDownMethods != null )
            {
               if ( !JavaFeatureRunner.runTestMethods( eventProcessor, testDataSources, runName, featureName, featureDescription, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario "
                                                                                                                                  + " tearDown", testCaseObject, testExecutionContext, scenarioTearDownMethods ) )
               {
                  eventProcessor.raiseEvent( new GenericTestEvent( runName, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario completed" + featureName + " " + scenarioMethod.getName() + " Result: " + result.toString() ) );
                  continue nextScenarioMethod;
               }
            }
         }
         catch ( Throwable t )
         {
            result = new StepResult( false, t.getMessage(), t, null );
            log.error( t );
         }
         eventProcessor.raiseEvent( new GenericTestEvent( runName, "feature:" + featureName + " iteration [" + iterationIndex + "] scenario completed" + scenarioName + " " + scenarioMethod.getName() + " Result: " + result.toString() ) );
      }
   }
}
