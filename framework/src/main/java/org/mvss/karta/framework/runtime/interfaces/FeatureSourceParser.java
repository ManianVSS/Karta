package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.core.TestFeature;

public interface FeatureSourceParser extends Plugin
{
   TestFeature parseFeatureSource( String sourceCode ) throws Throwable;
}
