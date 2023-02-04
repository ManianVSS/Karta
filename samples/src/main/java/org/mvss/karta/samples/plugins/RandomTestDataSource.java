package org.mvss.karta.samples.plugins;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.Initializer;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.plugins.TestDataSource;
import org.mvss.karta.framework.properties.PropertyMapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

@Log4j2
public class RandomTestDataSource implements TestDataSource {
    @Getter
    private static final String PLUGIN_NAME = "RandomTestDataSource";

    @PropertyMapping(group = PLUGIN_NAME, value = "seed")
    private Integer seed = null;

    private Random random;

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

        if (seed != null) {
            random = new Random(seed);
        } else {
            random = new Random();
        }

        initialized = true;
        return true;
    }

    @Override
    public HashMap<String, Serializable> getData(TestExecutionContext testExecutionContext) {
        HashMap<String, Serializable> testData = new HashMap<>();
        testData.put("randomInt", random.nextInt());
        return testData;
    }

}
