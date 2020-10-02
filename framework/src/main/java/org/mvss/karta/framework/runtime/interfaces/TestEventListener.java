package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;

public interface TestEventListener extends Plugin
{
   void runStarted( String runName );

   void featureStarted( String runName, TestFeature feature );

   void featureSetupStepStarted( String runName, TestFeature feature, TestStep setupStep );

   void featureSetupStepComplete( String runName, TestFeature feature, TestStep setupStep, StepResult result );

   void scenarioStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario );

   void scenarioSetupStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep );

   void scenarioSetupStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep, StepResult result );

   void scenarioStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep );

   void scenarioStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep, StepResult result );

   void scenarioTearDownStepStarted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep );

   void scenarioTearDownStepCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep, StepResult result );

   void scenarioCompleted( String runName, TestFeature feature, long iterationNumber, TestScenario scenario );

   void featureTearDownStepStarted( String runName, TestFeature feature, TestStep tearDownStep );

   void featureTearDownStepComplete( String runName, TestFeature feature, TestStep tearDownStep, StepResult result );

   void featureCompleted( String runName, TestFeature feature );

   void runCompleted( String runName );
}
