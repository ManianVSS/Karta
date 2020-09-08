package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestFeature;

public interface FeatureSourceParser
{
   void initFeatureParser( HashMap<String, Serializable> properties ) throws Throwable;

   TestFeature parseFeatureSource( String sourceCode ) throws Throwable;
}
