package org.mvss.karta.samples.tests;

import java.util.HashSet;

import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.javatest.Feature;
import org.mvss.karta.framework.core.javatest.FeatureSetup;
import org.mvss.karta.framework.core.javatest.FeatureTearDown;
import org.mvss.karta.framework.core.javatest.Scenario;
import org.mvss.karta.framework.core.javatest.ScenarioSetup;
import org.mvss.karta.framework.core.javatest.ScenarioTearDown;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.samples.stepdefinitions.SamplePropertyType;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Feature( "Sample Java Test Feature" )
public class Test1
{
   @PropertyMapping( group = "groupName", value = "variable1" )
   private String             username = "default";

   @PropertyMapping( group = "groupName", value = "variable1" )
   private String             variable1;

   @PropertyMapping( group = "groupName", value = "variable2" )
   private SamplePropertyType variable2;

   @KartaAutoWired
   EventProcessor             eventProcessor;

   @FeatureSetup
   public StepResult myFeatureSetup( TestExecutionContext testExecutionContext )
   {
      log.info( "testData " + testExecutionContext.getData() );
      return StandardStepResults.passed;
   }

   @ScenarioSetup
   public StepResult myScenarioSetupMethod( TestExecutionContext testExecutionContext )
   {
      log.info( "testData " + testExecutionContext.getData() );
      return StandardStepResults.passed;
   }

   @Scenario( value = "Scenario2", sequence = 2 )
   public StepResult myScenarioMethod2( TestExecutionContext testExecutionContext )
   {
      log.info( username + " " + variable2 );
      return StandardStepResults.passed;
   }

   @Scenario( value = "Scenario1", sequence = 1 )
   public StepResult myScenarioMethod( TestExecutionContext testExecutionContext )
   {
      log.info( username + " " + variable1 );
      return StandardStepResults.passed;
   }

   @Scenario( value = "Scenario3" )
   public StepResult myScenarioMethod3( TestExecutionContext context )
   {
      log.info( username + " " + variable2 );
      HashSet<String> failureTags = new HashSet<String>();
      failureTags.add( "sample" );
      failureTags.add( "failure" );
      failureTags.add( "java" );
      failureTags.add( "tags" );
      eventProcessor.raiseIncident( context.getRunName(), context.getFeatureName(), context.getIterationIndex(), context.getScenarioName(), context.getStepIdentifier(), TestIncident.builder().message( "Sample test incident" ).tags( failureTags ).build() );
      return StandardStepResults.passed;
   }

   @ScenarioTearDown
   public StepResult myScenarioTearDownMethod( TestExecutionContext testExecutionContext )
   {
      log.info( "testData " + testExecutionContext.getData() );
      return StandardStepResults.passed;
   }

   @FeatureTearDown
   public StepResult myFeatureTearDownMethod( TestExecutionContext testExecutionContext )
   {
      log.info( "testData " + testExecutionContext.getData() );
      return StandardStepResults.passed;
   }
}
