package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.javatest.Feature;
import org.mvss.karta.framework.core.javatest.FeatureSetup;
import org.mvss.karta.framework.core.javatest.FeatureTearDown;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.GenericTestEvent;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
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
   private ArrayList<TestDataSource>                      testDataSources;
   private HashMap<String, HashMap<String, Serializable>> testProperties;
   private EventProcessor                                 eventProcessor;
   private KartaMinionRegistry                            minionRegistry;

   public boolean run( String runName, String javaTest, String javaTestJarFile )
   {
      try
      {
         Class<?> testCaseClass = StringUtils.isNotBlank( javaTestJarFile ) ? (Class<?>) DynamicClassLoader.loadClass( javaTestJarFile, javaTest ) : (Class<?>) Class.forName( javaTest );

         Feature[] featureAnnotations = testCaseClass.getAnnotationsByType( Feature.class );

         if ( ( featureAnnotations == null ) || ( featureAnnotations.length == 0 ) )
         {
            // TODO: Error not an feature
            return false;
         }

         String featureName = featureAnnotations[0].value();
         String featureDescription = featureAnnotations[0].description();

         Method[] classMethods = testCaseClass.getMethods();
         HashMap<Integer, Method> featureSetupMethods = new HashMap<Integer, Method>();
         HashMap<Integer, Method> scenarioSetupMethods = new HashMap<Integer, Method>();
         HashMap<Integer, Method> scenarioMethods = new HashMap<Integer, Method>();
         HashMap<Integer, Method> scenarioTearDownMethods = new HashMap<Integer, Method>();
         HashMap<Integer, Method> featureTearDownMethods = new HashMap<Integer, Method>();
         // TreeMap<Integer, Method> sortedMethods=new TreeMap<Integer, Method>();

         for ( Method classMethod : classMethods )
         {
            if ( classMethod.getReturnType() == StepResult.class )
            {
               Parameter[] parameters = classMethod.getParameters();
               if ( ( parameters.length == 1 ) && ( parameters[0].getType() == TestExecutionContext.class ) )
               {
                  if ( classMethod.isAnnotationPresent( FeatureSetup.class ) )
                  {
                     FeatureSetup annotation = classMethod.getAnnotationsByType( FeatureSetup.class )[0];
                     int sequence = ( annotation.sequence() == 0 ) ? featureSetupMethods.keySet().size() + 1 : annotation.sequence();
                     featureSetupMethods.put( sequence, classMethod );
                  }
                  if ( classMethod.isAnnotationPresent( ScenarioSetup.class ) )
                  {
                     ScenarioSetup annotation = classMethod.getAnnotationsByType( ScenarioSetup.class )[0];
                     int sequence = ( annotation.sequence() == 0 ) ? scenarioSetupMethods.keySet().size() + 1 : annotation.sequence();
                     scenarioSetupMethods.put( sequence, classMethod );
                  }
                  if ( classMethod.isAnnotationPresent( Scenario.class ) )
                  {
                     Scenario annotation = classMethod.getAnnotationsByType( Scenario.class )[0];
                     int sequence = ( annotation.sequence() == 0 ) ? scenarioMethods.keySet().size() + 1 : annotation.sequence();
                     scenarioMethods.put( sequence, classMethod );
                  }
                  if ( classMethod.isAnnotationPresent( ScenarioTearDown.class ) )
                  {
                     ScenarioTearDown annotation = classMethod.getAnnotationsByType( ScenarioTearDown.class )[0];
                     int sequence = ( annotation.sequence() == 0 ) ? scenarioTearDownMethods.keySet().size() + 1 : annotation.sequence();
                     scenarioTearDownMethods.put( sequence, classMethod );
                  }
                  if ( classMethod.isAnnotationPresent( FeatureTearDown.class ) )
                  {
                     FeatureTearDown annotation = classMethod.getAnnotationsByType( FeatureTearDown.class )[0];
                     int sequence = ( annotation.sequence() == 0 ) ? featureTearDownMethods.keySet().size() + 1 : annotation.sequence();
                     featureTearDownMethods.put( sequence, classMethod );
                  }
               }
            }
         }

         Object testCaseObject = testCaseClass.newInstance();
         Configurator.loadProperties( testProperties, testCaseObject );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

         eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature started " + featureName + " " + featureDescription ) );

         ArrayList<Integer> featureSetupSequenceList = new ArrayList<Integer>( featureSetupMethods.keySet() );
         Collections.sort( featureSetupSequenceList );
         for ( Integer sequence : featureSetupSequenceList )
         {
            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
            testExecutionContext.setData( testData );

            Method setupMethodToInvoke = featureSetupMethods.get( sequence );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature setup started " + featureName + " " + setupMethodToInvoke.getName() ) );
            StepResult result = (StepResult) setupMethodToInvoke.invoke( testCaseObject, testExecutionContext );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature setup completed " + featureName + " " + setupMethodToInvoke.getName() + " Result: " + result.toString() ) );

            if ( !result.isSuccesssful() )
            {
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature completed " + featureName + " " + featureDescription ) );
               return false;
            }
         }

         ArrayList<Integer> sequenceList = new ArrayList<Integer>( scenarioMethods.keySet() );
         Collections.sort( sequenceList );
         nextScenario: for ( Integer sequence : sequenceList )
         {
            ArrayList<Integer> setupSequenceList = new ArrayList<Integer>( scenarioSetupMethods.keySet() );
            Collections.sort( setupSequenceList );
            for ( Integer setupSequence : setupSequenceList )
            {
               testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
               testExecutionContext.setData( testData );

               Method setupMethodToInvoke = scenarioSetupMethods.get( setupSequence );
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario setup started " + featureName + " " + setupMethodToInvoke.getName() ) );
               StepResult setupResult = (StepResult) setupMethodToInvoke.invoke( testCaseObject, testExecutionContext );
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario setup completed " + featureName + " " + setupMethodToInvoke.getName() + " Result: " + setupResult.toString() ) );

               if ( !setupResult.isSuccesssful() )
               {
                  eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature completed " + featureName + " " + featureDescription ) );
                  continue nextScenario;
               }
            }

            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
            testExecutionContext.setData( testData );

            Method scenarioMethodToInvoke = scenarioMethods.get( sequence );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario started " + featureName + " " + scenarioMethodToInvoke.getName() ) );
            StepResult result = (StepResult) scenarioMethodToInvoke.invoke( testCaseObject, testExecutionContext );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario completed " + featureName + " " + scenarioMethodToInvoke.getName() + " Result: " + result.toString() ) );

            if ( !result.isSuccesssful() )
            {
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature completed " + featureName + " " + featureDescription ) );
               continue nextScenario;
            }

            ArrayList<Integer> tearDownSequenceList = new ArrayList<Integer>( scenarioSetupMethods.keySet() );
            Collections.sort( tearDownSequenceList );
            for ( Integer tearDownSequence : tearDownSequenceList )
            {
               testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
               testExecutionContext.setData( testData );

               Method tearDownMethodToInvoke = scenarioTearDownMethods.get( tearDownSequence );
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario teardown started " + featureName + " " + tearDownMethodToInvoke.getName() ) );
               StepResult tearDownResult = (StepResult) tearDownMethodToInvoke.invoke( testCaseObject, testExecutionContext );
               eventProcessor.raiseEvent( new GenericTestEvent( runName, "Scenario teardown completed " + featureName + " " + tearDownMethodToInvoke.getName() + " Result: " + tearDownResult.toString() ) );

               if ( !tearDownResult.isSuccesssful() )
               {
                  eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature completed " + featureName + " " + featureDescription ) );
                  continue nextScenario;
               }
            }

         }

         ArrayList<Integer> featureTearDownsequenceList = new ArrayList<Integer>( featureTearDownMethods.keySet() );
         Collections.sort( featureTearDownsequenceList );
         for ( Integer sequence : featureTearDownsequenceList )
         {
            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( featureName, null, null, 0, 0 ) );
            testExecutionContext.setData( testData );

            Method tearDownMethodToInvoke = featureTearDownMethods.get( sequence );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature teardown started " + featureName + " " + tearDownMethodToInvoke.getName() ) );
            StepResult result = (StepResult) tearDownMethodToInvoke.invoke( testCaseObject, testExecutionContext );
            eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature teardown completed " + featureName + " " + tearDownMethodToInvoke.getName() + " Result: " + result.toString() ) );
         }

         eventProcessor.raiseEvent( new GenericTestEvent( runName, "Feature completed " + featureName + " " + featureDescription ) );

      }
      catch ( ClassNotFoundException cnfe )
      {
         log.error( "class " + javaTest + " could not be loaded" );
         return false;
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run java feature test", t );
         return false;
      }
      return true;
   }
}
