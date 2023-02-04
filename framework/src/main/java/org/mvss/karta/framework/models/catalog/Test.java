package org.mvss.karta.framework.models.catalog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashSet;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test implements Serializable, Comparable<Test> {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private TestType testType = TestType.FEATURE;

    private String name;
    private String description;

    @Builder.Default
    private Integer priority = Integer.MAX_VALUE;

    @Builder.Default
    private HashSet<String> tags = new HashSet<>();

    private String sourceArchive;

    private String featureSourceParser;
    private String stepRunner;

    @Builder.Default
    private HashSet<String> testDataSources = new HashSet<>();

    private String featureFileName;

    private String javaTestClass;

    @Builder.Default
    private Boolean runAllScenarioParallely = false;

    @Builder.Default
    private Boolean chanceBasedScenarioExecution = false;

    @Builder.Default
    private Boolean exclusiveScenarioPerIteration = false;

    @JsonFormat(shape = Shape.STRING)
    private Duration runDuration;

    @JsonFormat(shape = Shape.STRING)
    private Duration coolDownBetweenIterations;

    @Builder.Default
    private long iterationsPerCoolDownPeriod = 1;

    private String threadGroup;

    @Builder.Default
    private long numberOfIterations = 1;

    @Builder.Default
    private int numberOfThreads = 1;

    public void propagateAttributes(String sourceArchive, String featureSourceParser, String stepRunner, HashSet<String> testDataSources,
                                    String threadGroup, HashSet<String> tags) {
        if (StringUtils.isEmpty(this.sourceArchive) && StringUtils.isNotEmpty(sourceArchive)) {
            this.sourceArchive = sourceArchive;
        }

        if (StringUtils.isEmpty(this.featureSourceParser) && StringUtils.isNotEmpty(featureSourceParser)) {
            this.featureSourceParser = featureSourceParser;
        }

        if (StringUtils.isEmpty(this.stepRunner) && StringUtils.isNotEmpty(stepRunner)) {
            this.stepRunner = stepRunner;
        }

        if (testDataSources != null) {
            this.testDataSources.addAll(testDataSources);
        }

        if (StringUtils.isEmpty(this.threadGroup) && StringUtils.isNotEmpty(threadGroup)) {
            this.threadGroup = threadGroup;
        }

        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    public void mergeWithTest(Test test) {
        if (test == null) {
            return;
        }

        if (tags.isEmpty() && !test.tags.isEmpty()) {
            tags.addAll(test.tags);
        }

        if (StringUtils.isEmpty(sourceArchive) && StringUtils.isNotEmpty(test.sourceArchive)) {
            sourceArchive = test.sourceArchive;
        }

        if (StringUtils.isEmpty(featureSourceParser) && StringUtils.isNotEmpty(test.featureSourceParser)) {
            featureSourceParser = test.featureSourceParser;
        }

        if (StringUtils.isEmpty(stepRunner) && StringUtils.isNotEmpty(test.stepRunner)) {
            stepRunner = test.stepRunner;
        }

        if (testDataSources.isEmpty() && !test.testDataSources.isEmpty()) {
            testDataSources.addAll(test.testDataSources);
        }

        if (StringUtils.isEmpty(threadGroup) && StringUtils.isNotEmpty(test.threadGroup)) {
            threadGroup = test.threadGroup;
        }
    }

    @Override
    public int compareTo(Test other) {
        int lhs = (priority == null) ? 0 : priority;
        int rhs = (other.priority == null) ? 0 : other.priority;
        return lhs - rhs;
    }
}
