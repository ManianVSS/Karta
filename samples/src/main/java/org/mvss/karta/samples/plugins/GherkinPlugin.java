package org.mvss.karta.samples.plugins;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.Initializer;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.plugins.FeatureSourceParser;
import org.mvss.karta.samples.utils.GherkinUtils;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class GherkinPlugin implements FeatureSourceParser {
    public static final String PLUGIN_NAME = "Gherkin";

    public static final List<String> conjunctions = Arrays.asList("Given ", "When ", "Then ", "And ", "But ");

    private boolean initialized = false;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Initializer
    public boolean initialize() {
        if (initialized) {
            return true;
        }
        log.info("Initializing " + PLUGIN_NAME + " plugin");
        initialized = true;
        return true;
    }

    @Override
    public TestFeature parseFeatureSource(String sourceCode) throws Throwable {
        return GherkinUtils.parseFeatureSource(sourceCode, null);
    }
}
