package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DefaultTestEventListener implements TestEventListener
{
   public static final String PLUGIN_NAME  = "KartaDefaultTestEventListener";

   public static final String SPACE        = " ";
   public static final String RUN          = "Run:";
   public static final String FEATURE      = "Feature:";
   public static final String SCENARIO     = "Scenario:";
   public static final String STEP         = "Step:";
   public static final String SETUPSTEP    = "SetupStep:";
   public static final String TEARDOWNSTEP = "TearDownStep:";
   public static final String STARTED      = "started";
   public static final String FAILED       = "failed";
   public static final String PASSED       = "passed";
   public static final String COMPLETED    = "completed";

   private boolean            initialized  = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      Configurator.loadProperties( properties, this );

      log.debug( "Initializing Yerkin plugin with " + properties );

      initialized = true;
      return true;
   }

   @Override
   public void runStarted( String runName )
   {
      log.info( "{" + runName + "}" + SPACE + STARTED );
   }

   @Override
   public void featureStarted( String runName, TestFeature feature )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}" + SPACE + STARTED );
   }

   @Override
   public void featureSetupStepStarted( String runName, TestFeature feature, TestStep setupStep )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{setup(" + setupStep.getIdentifier() + ")}" + SPACE + STARTED );
   }

   @Override
   public void featureSetupStepComplete( String runName, TestFeature feature, TestStep setupStep, StepResult result )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{setup(" + setupStep.getIdentifier() + ")}" + SPACE + ( result.isSuccesssful() ? PASSED : FAILED ) );
   }

   @Override
   public void scenarioStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + iterationNumber + "}{" + scenario.getName() + "}" + SPACE + STARTED );
   }

   @Override
   public void scenarioSetupStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{setup(" + scenarioSetupStep.getIdentifier() + ")}" + SPACE + STARTED );
   }

   @Override
   public void scenarioSetupStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep, StepResult result )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{setup(" + scenarioSetupStep.getIdentifier() + ")}" + SPACE + ( result.isSuccesssful() ? PASSED : FAILED ) );
   }

   @Override
   public void scenarioStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{step(" + scenarioStep.getIdentifier() + ")}" + SPACE + STARTED );
   }

   @Override
   public void scenarioStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep, StepResult result )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{step(" + scenarioStep.getIdentifier() + ")}" + SPACE + ( result.isSuccesssful() ? PASSED : FAILED ) );
   }

   @Override
   public void scenarioTearDownStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{tearDown(" + scenarioTearDownStep.getIdentifier() + ")}" + SPACE + STARTED );
   }

   @Override
   public void scenarioTearDownStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep, StepResult result )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + scenario.getName() + "}{" + iterationNumber + "}{tearDown(" + scenarioTearDownStep.getIdentifier() + ")}" + SPACE + ( result.isSuccesssful() ? PASSED : FAILED ) );
   }

   @Override
   public void scenarioCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{" + iterationNumber + "}{" + scenario.getName() + "}" + SPACE + COMPLETED );
   }

   @Override
   public void featureTearDownStepStarted( String runName, TestFeature feature, TestStep tearDownStep )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{tearDown(" + tearDownStep.getIdentifier() + ")}" + SPACE + STARTED );
   }

   @Override
   public void featureTearDownStepComplete( String runName, TestFeature feature, TestStep tearDownStep, StepResult result )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}{tearDown(" + tearDownStep.getIdentifier() + ")}" + SPACE + ( result.isSuccesssful() ? PASSED : FAILED ) );
   }

   @Override
   public void featureCompleted( String runName, TestFeature feature )
   {
      log.info( "{" + runName + "}{" + feature.getName() + "}" + SPACE + COMPLETED );

   }

   @Override
   public void runCompleted( String runName )
   {
      log.info( "{" + runName + "}" + SPACE + COMPLETED );

   }

}
