package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.test.TestFeature;

public interface FeatureSourceParser extends Plugin {
    TestFeature parseFeatureSource(String sourceCode) throws Throwable;
}
