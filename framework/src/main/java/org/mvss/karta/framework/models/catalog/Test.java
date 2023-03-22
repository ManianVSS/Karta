package org.mvss.karta.framework.models.catalog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;

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
    private ArrayList<String> tags = new ArrayList<>();

    private String sourceArchive;

    @Builder.Default
    private ArrayList<String> featureSourceParsers = new ArrayList<>();
    @Builder.Default
    private ArrayList<String> stepRunners = new ArrayList<>();

    @Builder.Default
    private ArrayList<String> testDataSources = new ArrayList<>();

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

    public void propagateAttributes(String sourceArchive, ArrayList<String> featureSourceParsers, ArrayList<String> stepRunners, ArrayList<String> testDataSources, String threadGroup, ArrayList<String> tags) {
        if (StringUtils.isEmpty(this.sourceArchive) && StringUtils.isNotEmpty(sourceArchive)) {
            this.sourceArchive = sourceArchive;
        }

        if (featureSourceParsers != null) {
            featureSourceParsers.forEach(item -> {
                if (!this.featureSourceParsers.contains(item)) this.featureSourceParsers.add(item);
            });
        }

        if (stepRunners != null) {
            stepRunners.forEach(item -> {
                if (!this.stepRunners.contains(item)) this.stepRunners.add(item);
            });
        }

        if (testDataSources != null) {
            testDataSources.forEach(item -> {
                if (!this.testDataSources.contains(item)) this.testDataSources.add(item);
            });
        }

        if (StringUtils.isEmpty(this.threadGroup) && StringUtils.isNotEmpty(threadGroup)) {
            this.threadGroup = threadGroup;
        }

        if (tags != null) {
            tags.forEach(item -> {
                if (!this.tags.contains(item)) this.tags.add(item);
            });
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

        if (featureSourceParsers.isEmpty() && !test.featureSourceParsers.isEmpty()) {
            test.featureSourceParsers.forEach(item -> {
                if (!this.featureSourceParsers.contains(item)) this.featureSourceParsers.add(item);
            });
        }

        if (stepRunners.isEmpty() && !test.stepRunners.isEmpty()) {
            test.stepRunners.forEach(item -> {
                if (!this.stepRunners.contains(item)) this.stepRunners.add(item);
            });
        }

        if (testDataSources.isEmpty() && !test.testDataSources.isEmpty()) {
            test.testDataSources.forEach(item -> {
                if (!this.testDataSources.contains(item)) this.testDataSources.add(item);
            });

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
