package org.mvss.karta.framework.plugins.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.dependencyinjection.utils.PropertyUtils;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.plugins.TestDataSource;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

@Log4j2
public class DataFilesTestDataSource implements TestDataSource {
    public static final String PLUGIN_NAME = "DataFilesTestDataSource";

    public static final TypeReference<HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>>> testDataStoreType = new TypeReference<>() {
    };

    private final HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>> dataStore = new HashMap<>();

    @PropertyMapping(group = PLUGIN_NAME, value = "dataPath")
    private ArrayList<String> dataPath = new ArrayList<>();

    private boolean initialized = false;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    public void mergeDataStore(HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>> dataStoreToMerge) {
        // TODO: Handle nested steps data as well.

        for (String featureDataKey : dataStoreToMerge.keySet()) {
            if (!dataStore.containsKey(featureDataKey)) {
                dataStore.put(featureDataKey, new HashMap<>());
            }

            HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureData = dataStore.get(featureDataKey);
            HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureDataToMerge = dataStoreToMerge.get(featureDataKey);

            for (String scenarioDataKey : featureDataToMerge.keySet()) {
                if (!featureData.containsKey(scenarioDataKey)) {
                    featureData.put(scenarioDataKey, new HashMap<>());
                }

                HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioData = featureData.get(scenarioDataKey);
                HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioDataToMerge = featureDataToMerge.get(scenarioDataKey);

                for (String stepDataKey : scenarioDataToMerge.keySet()) {
                    if (!scenarioData.containsKey(stepDataKey)) {
                        scenarioData.put(stepDataKey, new HashMap<>());
                    }

                    HashMap<String, ArrayList<Serializable>> stepData = scenarioData.get(stepDataKey);
                    HashMap<String, ArrayList<Serializable>> stepDataToMerge = scenarioDataToMerge.get(stepDataKey);

                    for (String testDataKey : stepDataToMerge.keySet()) {
                        stepData.put(testDataKey, stepDataToMerge.get(testDataKey));
                    }
                }

            }
        }
    }

    public void mergeDataStore(File dataFileToMerge) throws IOException {
        mergeDataStore(ParserUtils.readValue(ParserUtils.getFileDataFormat(dataFileToMerge.getName()),
                FileUtils.readFileToString(dataFileToMerge, Charset.defaultCharset()), testDataStoreType));
    }

    public void mergeDataDirectory(File pathFile) throws IOException {
        for (File dataFile : FileUtils.listFiles(pathFile, Constants.dataFileExtensions, true)) {
            mergeDataStore(dataFile);
        }
    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + " plugin");
        PropertyUtils.systemProperties.expandEnvVars(dataPath);

        for (String dataPathEntry : dataPath) {
            File pathFile = new File(dataPathEntry);

            if (pathFile.exists()) {
                if (pathFile.isDirectory()) {
                    mergeDataDirectory(pathFile);
                } else {
                    mergeDataStore(pathFile);
                }
            }
        }

        initialized = true;
        return true;
    }

    @Override
    public HashMap<String, Serializable> getData(TestExecutionContext testExecutionContext) {
        HashMap<String, Serializable> testData = new HashMap<>();

        if (testExecutionContext != null) {
            try {
                String featureName = testExecutionContext.getFeatureName();
                HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureData = (featureName != null) && dataStore.containsKey(
                        featureName) ? dataStore.get(featureName) : dataStore.get(Constants.__GENERIC_FEATURE__);

                if (featureData != null) {
                    String scenarioName = testExecutionContext.getScenarioName();
                    HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioData = (scenarioName != null) && featureData.containsKey(
                            scenarioName) ? featureData.get(scenarioName) : featureData.get(Constants.__GENERIC_SCENARIO__);

                    if (scenarioData != null) {
                        String stepName = testExecutionContext.getStepIdentifier();
                        HashMap<String, ArrayList<Serializable>> stepData = (stepName != null) && scenarioData.containsKey(stepName) ?
                                scenarioData.get(stepName) :
                                scenarioData.get(Constants.__GENERIC_STEP__);

                        if (stepData != null) {
                            for (String dataKey : stepData.keySet()) {
                                ArrayList<Serializable> possibleValues = stepData.get(dataKey);

                                if ((possibleValues != null) && !possibleValues.isEmpty()) {
                                    long iterationIndex = testExecutionContext.getIterationIndex();
                                    if (iterationIndex <= 0) {
                                        iterationIndex = 0;
                                    }

                                    int valueIndex = (int) (iterationIndex % possibleValues.size());
                                    testData.put(dataKey, possibleValues.get(valueIndex));
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                log.error(Constants.EMPTY_STRING, t);
            }
        }
        return testData;
    }

    @Override
    public void close() {
        log.info("Closing " + PLUGIN_NAME + " ...");
    }
}
