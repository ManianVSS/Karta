package org.mvss.karta.framework.models.run;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.dependencyinjection.BeanRegistry;
import org.mvss.karta.framework.plugins.TestDataSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

//TODO: Thread specific bean registry to share control objects for a thread/runtime level
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestExecutionContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private String runName;
    private String featureName;

    @Builder.Default
    private int iterationIndex = -1;

    private String scenarioName;
    private String stepIdentifier;

    private HashMap<String, Serializable> testData;

    @Builder.Default
    private HashMap<String, Serializable> contextData = new HashMap<>();

    @JsonIgnore
    private transient BeanRegistry contextBeanRegistry;

    public TestExecutionContext(String runName, String featureName, int iterationIndex, String scenarioName, String stepIdentifier, HashMap<String, Serializable> testData, HashMap<String, Serializable> contextData) {
        super();
        this.runName = runName;
        this.featureName = featureName;
        this.iterationIndex = iterationIndex;
        this.scenarioName = scenarioName;
        this.stepIdentifier = stepIdentifier;
        this.testData = testData;
        this.contextData = contextData;
    }

    public void mergeTestData(HashMap<String, Serializable> stepTestData, HashMap<String, ArrayList<Serializable>> testDataSet, ArrayList<TestDataSource> testDataSources) throws Throwable {
        this.testData = new HashMap<>();

        if (testDataSources != null) {
            for (TestDataSource tds : testDataSources) {
                HashMap<String, Serializable> testData = tds.getData(this);
                this.testData.putAll(testData);
            }
        }

        long iterationIndexForData = (this.iterationIndex < 0) ? 0 : this.iterationIndex;

        if (testDataSet != null) {
            for (String dataKey : testDataSet.keySet()) {
                ArrayList<Serializable> possibleValues = testDataSet.get(dataKey);
                if ((possibleValues != null) && !possibleValues.isEmpty()) {
                    int valueIndex = (int) (iterationIndexForData % possibleValues.size());
                    this.testData.put(dataKey, possibleValues.get(valueIndex));
                }
            }
        }

        if (stepTestData != null) {
            this.testData.putAll(stepTestData);
        }
    }
}
