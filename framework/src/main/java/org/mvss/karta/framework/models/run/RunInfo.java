package org.mvss.karta.framework.models.run;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.*;
import org.mvss.karta.framework.models.catalog.Test;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RunInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String runName;

    private String release;

    private String build;

    private ArrayList<String> tags;

    @Builder.Default
    private boolean remotelyCalled = false;

    private ArrayList<String> featureSourceParserPlugins;

    private ArrayList<String> stepRunnerPlugins;

    private ArrayList<String> testDataSourcePlugins;

    @Builder.Default
    private boolean runAllScenarioParallely = false;

    @Builder.Default
    private boolean chanceBasedScenarioExecution = false;

    @Builder.Default
    private boolean exclusiveScenarioPerIteration = false;

    @Builder.Default
    private long numberOfIterations = 1;

    @JsonFormat(shape = Shape.STRING)
    private Duration runDuration;

    @JsonFormat(shape = Shape.STRING)
    private Duration coolDownBetweenIterations;

    @Builder.Default
    private long iterationsPerCoolDownPeriod = 1;

    @Builder.Default
    private int numberOfIterationsInParallel = 1;

    public void setDefaultPlugins(ArrayList<String> featureSourceParserPlugins, ArrayList<String> stepRunnerPlugins, ArrayList<String> testDataSourcePlugins) {

        if ((featureSourceParserPlugins != null) && !featureSourceParserPlugins.isEmpty()) {
            if ((this.featureSourceParserPlugins == null) || this.featureSourceParserPlugins.isEmpty()) {
                this.featureSourceParserPlugins = featureSourceParserPlugins;
            }
        }


        if ((stepRunnerPlugins != null) && !stepRunnerPlugins.isEmpty()) {
            if ((this.stepRunnerPlugins == null) || this.stepRunnerPlugins.isEmpty()) {
                this.stepRunnerPlugins = stepRunnerPlugins;
            }
        }

        if ((testDataSourcePlugins != null) && !testDataSourcePlugins.isEmpty()) {
            if ((this.testDataSourcePlugins == null) || this.testDataSourcePlugins.isEmpty()) {
                this.testDataSourcePlugins = testDataSourcePlugins;
            }
        }
    }

    public void addPluginsFromTest(Test test) {

        ArrayList<String> featureSourceParserPlugins = test.getFeatureSourceParsers();
        if ((featureSourceParserPlugins != null) && !featureSourceParserPlugins.isEmpty()) {
            this.featureSourceParserPlugins = featureSourceParserPlugins;
        }

        ArrayList<String> stepRunnerPlugins = test.getStepRunners();
        if ((stepRunnerPlugins != null) && !stepRunnerPlugins.isEmpty()) {
            this.stepRunnerPlugins = stepRunnerPlugins;
        }

        ArrayList<String> testDataSourcePlugins = test.getTestDataSources();
        if ((testDataSourcePlugins != null) && !testDataSourcePlugins.isEmpty()) {
            this.testDataSourcePlugins = testDataSourcePlugins;
        }
    }

    public RunInfo getRunInfoForTest(Test test) {
        RunInfo runInfo = this.toBuilder().tags(test.getTags()).featureSourceParserPlugins(test.getFeatureSourceParsers()).stepRunnerPlugins(test.getStepRunners()).testDataSourcePlugins(test.getTestDataSources()).runAllScenarioParallely(test.getRunAllScenarioParallely()).chanceBasedScenarioExecution(test.getChanceBasedScenarioExecution()).exclusiveScenarioPerIteration(test.getExclusiveScenarioPerIteration()).numberOfIterations(test.getNumberOfIterations()).runDuration(test.getRunDuration()).coolDownBetweenIterations(test.getCoolDownBetweenIterations()).iterationsPerCoolDownPeriod(test.getIterationsPerCoolDownPeriod()).numberOfIterationsInParallel(test.getNumberOfThreads()).build();

        runInfo.addPluginsFromTest(test);

        return runInfo;
    }
}
