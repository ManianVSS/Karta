package org.mvss.karta.framework.runtime.interfaces;

import java.util.HashSet;

import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.TestFeature;

public interface TestLifeCycleHook extends Plugin
{
   void runStart( String runName, HashSet<String> tags );

   void featureStart( String runName, TestFeature feature, HashSet<String> tags );

   void scenarioStart( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags );

   void scenarioStop( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags );

   void featureStop( String runName, TestFeature feature, HashSet<String> tags );

   void runStop( String runName, HashSet<String> tags );
}
