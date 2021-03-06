package org.mvss.karta.framework.runtime.interfaces;

import java.util.HashSet;

import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.TestFeature;

public interface TestLifeCycleHook extends Plugin
{
   boolean runStart( String runName, HashSet<String> tags );

   boolean featureStart( String runName, TestFeature feature, HashSet<String> tags );

   boolean scenarioStart( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags );

   boolean scenarioStop( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags );

   boolean featureStop( String runName, TestFeature feature, HashSet<String> tags );

   boolean runStop( String runName, HashSet<String> tags );
}
