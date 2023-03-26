package org.mvss.karta.framework.plugins.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.plugins.FeatureSourceParser;
import org.mvss.karta.framework.utils.GherkinUtils;

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
    public boolean isValidFeatureFile(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        return fileExtension.endsWith("feature");
    }

    @Override
    public TestFeature parseFeatureSource(String sourceCode) throws Throwable {
        return GherkinUtils.parseFeatureSource(sourceCode, null);
    }
}
