package org.mvss.karta.framework.plugins.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.plugins.TestDataSource;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@Log4j2
public class CSVTestDataSourcePlugin implements TestDataSource {
    public static final String PLUGIN_NAME = "CSVTestDataSourcePlugin";
    private static final ObjectMapper objectMapper = ParserUtils.getYamlObjectMapper();
    private final Object writeLock = new Object();
    @PropertyMapping(group = PLUGIN_NAME, value = "csvFileName")
    private String csvFileName = "TestData.csv";
    private boolean initialized = false;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    private final ArrayList<HashMap<String, Serializable>> testDataSet = new ArrayList<>();
    private volatile int currentIndex = 0;

    private boolean readCSVData() throws Throwable {
        if (StringUtils.isNotBlank(csvFileName)) {
            synchronized (writeLock) {
                File file = new File(csvFileName);
                try (FileReader filereader = new FileReader(file)) {
                    CSVReader csvReader = new CSVReader(filereader);
                    String[] headerRecord = csvReader.readNext();

                    if ((headerRecord == null) || (headerRecord.length == 0)) {
                        filereader.close();
                        throw new Exception("CSV file does not have data or headers " + csvFileName);
                    }

                    for (String[] nextRecord = csvReader.readNext(); nextRecord != null; nextRecord = csvReader.readNext()) {
                        HashMap<String, Serializable> testData = new HashMap<>();
                        for (int j = 0; j < headerRecord.length; j++) {
                            testData.put(headerRecord[j], objectMapper.readValue(nextRecord[j], Serializable.class));
                        }
                        testDataSet.add(testData);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + " plugin");

        return (initialized = readCSVData());
    }

    @Override
    public synchronized HashMap<String, Serializable> getData(TestExecutionContext testExecutionContext) {
        if (testDataSet.isEmpty()) {
            return null;
        }

        HashMap<String, Serializable> testData = testDataSet.get(currentIndex);
        currentIndex = (currentIndex + 1) % testDataSet.size();
        return testData;
    }

}
