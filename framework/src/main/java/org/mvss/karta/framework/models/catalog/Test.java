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
@ToString(callSuper = true)
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

    public Test addFeatureSourceParser(String featureSourceParser) {
        if (this.featureSourceParsers == null) {
            this.featureSourceParsers = new ArrayList<>();
        }
        if (featureSourceParser != null) {
            if (!this.featureSourceParsers.contains(featureSourceParser)) {
                this.featureSourceParsers.add(featureSourceParser);
            }
        }
        return this;
    }

    public Test addFeatureSourceParser(ArrayList<String> featureSourceParsers) {
        if (featureSourceParsers != null) {
            featureSourceParsers.forEach(this::addFeatureSourceParser);
        }
        return this;
    }

    public Test addStepRunner(String stepRunner) {
        if (this.stepRunners == null) {
            this.stepRunners = new ArrayList<>();
        }
        if (stepRunner != null) {
            if (!this.stepRunners.contains(stepRunner)) {
                this.stepRunners.add(stepRunner);
            }
        }
        return this;
    }

    public Test addStepRunner(ArrayList<String> stepRunners) {
        if (stepRunners != null) {
            stepRunners.forEach(this::addStepRunner);
        }
        return this;
    }

    public Test addTestDataSources(String testDataSources) {
        if (this.testDataSources == null) {
            this.testDataSources = new ArrayList<>();
        }
        if (testDataSources != null) {
            if (!this.testDataSources.contains(testDataSources)) {
                this.testDataSources.add(testDataSources);
            }
        }
        return this;
    }

    public Test addTestDataSources(ArrayList<String> testDataSources) {
        if (testDataSources != null) {
            testDataSources.forEach(this::addTestDataSources);
        }
        return this;
    }

    public Test addTags(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (tag != null) {
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        }
        return this;
    }

    public Test addTags(ArrayList<String> tags) {
        if (tags != null) {
            tags.forEach(this::addTags);
        }
        return this;
    }


    public void propagateAttributes(String sourceArchive, ArrayList<String> featureSourceParsers, ArrayList<String> stepRunners, ArrayList<String> testDataSources, String threadGroup, ArrayList<String> tags) {
        if (StringUtils.isEmpty(this.sourceArchive) && StringUtils.isNotEmpty(sourceArchive)) {
            this.sourceArchive = sourceArchive;
        }

        addFeatureSourceParser(featureSourceParsers).addStepRunner(stepRunners).addTestDataSources(testDataSources).addTags(tags);

        if (StringUtils.isEmpty(this.threadGroup) && StringUtils.isNotEmpty(threadGroup)) {
            this.threadGroup = threadGroup;
        }
    }

    public void mergeWithTest(Test test) {
        if (test == null) {
            return;
        }

        addTags(test.tags).addFeatureSourceParser(test.featureSourceParsers).addStepRunner(test.stepRunners).addTestDataSources(test.testDataSources);

        if (StringUtils.isEmpty(sourceArchive) && StringUtils.isNotEmpty(test.sourceArchive)) {
            sourceArchive = test.sourceArchive;
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
