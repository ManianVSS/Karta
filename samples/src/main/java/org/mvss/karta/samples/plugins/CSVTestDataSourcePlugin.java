package org.mvss.karta.samples.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.annotations.Initializer;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.plugins.TestDataSource;
import org.mvss.karta.framework.properties.PropertyMapping;
import org.mvss.karta.framework.utils.ParserUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

@Log4j2
public class CSVTestDataSourcePlugin implements TestDataSource {
    public static final String PLUGIN_NAME = "CSVTestDataSourcePlugin";
    private static final ObjectMapper objectMapper = ParserUtils.getYamlObjectMapper();
    @PropertyMapping(group = PLUGIN_NAME, value = "csvFileName")
    private String csvFileName = "TestData.csv";
    private boolean initialized = false;
    private CSVReader csvReader;
    private String[] headerRecord;
    private final Object writeLock = new Object();

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    private void resetCSV() throws Throwable {
        synchronized (writeLock) {
            File file = new File(csvFileName);
            FileReader filereader = new FileReader(file);
            csvReader = new CSVReader(filereader);
            headerRecord = csvReader.readNext();

            if ((headerRecord == null) || (headerRecord.length == 0)) {
                filereader.close();
                throw new Exception("CSV file does not have data or headers " + csvFileName);
            }
        }
    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + " plugin");

        resetCSV();
        initialized = true;
        return true;
    }

    @Override
    public HashMap<String, Serializable> getData(TestExecutionContext testExecutionContext) {
        HashMap<String, Serializable> testData = new HashMap<>();
        try {

            // TODO: retrieve record for the execution step pointer instead of cycling

            String[] nextRecord;

            synchronized (writeLock) {
                if ((nextRecord = csvReader.readNext()) == null) {
                    resetCSV();
                    if ((nextRecord = csvReader.readNext()) == null) {
                        return testData;
                    }
                }
            }

            for (int i = 0; i < headerRecord.length; i++) {
                testData.put(headerRecord[i], objectMapper.readValue(nextRecord[i], Serializable.class));
            }

        } catch (Throwable t) {
            log.error(Constants.EMPTY_STRING, t);
        }
        return testData;
    }

    @Override
    public void close() {
        log.info("Closing " + PLUGIN_NAME + " ...");

        if (csvReader != null) {
            try {
                csvReader.close();
                csvReader = null;
            } catch (IOException ioe) {
                log.error(ioe);
            }
        }
    }

}
