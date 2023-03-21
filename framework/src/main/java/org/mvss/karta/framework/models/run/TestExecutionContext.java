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

    private HashMap<String, Serializable> data;

    @Builder.Default
    private HashMap<String, Serializable> variables = new HashMap<>();

    @JsonIgnore
    private transient BeanRegistry contextBeanRegistry;

    public TestExecutionContext(String runName, String featureName, int iterationIndex, String scenarioName, String stepIdentifier,
                                HashMap<String, Serializable> data, HashMap<String, Serializable> variables) {
        super();
        this.runName = runName;
        this.featureName = featureName;
        this.iterationIndex = iterationIndex;
        this.scenarioName = scenarioName;
        this.stepIdentifier = stepIdentifier;
        this.data = data;
        this.variables = variables;
    }

    public void mergeTestData(HashMap<String, Serializable> stepTestData, HashMap<String, ArrayList<Serializable>> testDataSet,
                              ArrayList<TestDataSource> testDataSources) throws Throwable {
        data = new HashMap<>();

        if (testDataSources != null) {
            for (TestDataSource tds : testDataSources) {
                HashMap<String, Serializable> testData = tds.getData(this);
                data.putAll(testData);
            }
        }

        long iterationIndexForData = (this.iterationIndex < 0) ? 0 : this.iterationIndex;

        if (testDataSet != null) {
            for (String dataKey : testDataSet.keySet()) {
                ArrayList<Serializable> possibleValues = testDataSet.get(dataKey);
                if ((possibleValues != null) && !possibleValues.isEmpty()) {
                    int valueIndex = (int) (iterationIndexForData % possibleValues.size());
                    data.put(dataKey, possibleValues.get(valueIndex));
                }
            }
        }

        if (stepTestData != null) {
            data.putAll(stepTestData);
        }
    }
}
