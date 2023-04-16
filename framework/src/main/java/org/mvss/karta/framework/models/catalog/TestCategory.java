package org.mvss.karta.framework.models.catalog;

import lombok.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.interfaces.FeatureSourceParserLookup;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.plugins.FeatureSourceParser;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    private static ConcurrentHashMap<Pattern, ConcurrentHashMap<String, Matcher>> patternMatcherMap = new ConcurrentHashMap<>();
    private String name;
    private String description;
    @Builder.Default
    private ArrayList<String> tags = new ArrayList<>();
    @Builder.Default
    private ArrayList<String> featureSourceParsers = new ArrayList<>();
    @Builder.Default
    private ArrayList<String> stepRunners = new ArrayList<>();
    @Builder.Default
    private ArrayList<String> testDataSources = new ArrayList<>();
    @Builder.Default
    private ArrayList<TestCategory> subCategories = new ArrayList<>();
    @Builder.Default
    private ArrayList<Test> tests = new ArrayList<>();
    private String threadGroup;

    @Builder.Default
    private ArrayList<String> featureFiles = new ArrayList<>();

    public void mergeFeatureFiles(FeatureSourceParserLookup featureSourceParserLookup) throws Throwable {

        for (String featureFile : featureFiles) {
            mergeFeatureFile(featureSourceParserLookup, featureFile);
        }
        for (TestCategory testCategory : subCategories) {
            testCategory.mergeFeatureFiles(featureSourceParserLookup);
        }
    }

    public void mergeFeatureFile(FeatureSourceParserLookup featureSourceParserLookup, String featureFileName) throws Throwable {

        if (featureSourceParserLookup == null) {
            return;
        }

        ArrayList<FeatureSourceParser> featureSourceParserObjects = featureSourceParserLookup.lookup(this.featureSourceParsers);
        if ((featureSourceParserObjects == null) || featureSourceParserObjects.isEmpty()) {
            return;
        }

        Path featureFilePath = Paths.get(featureFileName);

        if (!Files.exists(featureFilePath)) {
            return;
        }

        if (Files.isDirectory(featureFilePath)) {
            for (File featureFileInDirectory : FileUtils.listFiles(featureFilePath.toFile(), null, true)) {
                mergeFeatureFile(featureSourceParserLookup, featureFileInDirectory.getAbsolutePath());
            }
            return;
        }

        for (FeatureSourceParser featureSourceParserObject : featureSourceParserObjects) {
            if (!featureSourceParserObject.isValidFeatureFile(featureFileName)) {
                continue;
            }

            TestFeature testFeature = featureSourceParserObject.parseFeatureFile(featureFileName);
            Test test = new Test();
            test.setName(testFeature.getName());
            test.setDescription(testFeature.getDescription());
            test.setFeatureFileName(featureFileName);
            test.setTestType(TestType.FEATURE);
            test.setTags(testFeature.getTags());
            mergeTest(test);
            break;
        }
    }

    public Test findTestByName(String name) {
        for (Test test : tests) {
            if (test.getName().contentEquals(name)) {
                return test;
            }
        }
        return null;
    }

    public synchronized void filterTestsByTag(ArrayList<Test> outputFilteredTests, HashSet<Pattern> tagPatterns) {
        for (Pattern tagPattern : tagPatterns) {
            if (!patternMatcherMap.containsKey(tagPattern)) {
                patternMatcherMap.put(tagPattern, new ConcurrentHashMap<>());
            }
            ConcurrentHashMap<String, Matcher> matcherMap = patternMatcherMap.get(tagPattern);
            for (String tag : this.tags) {
                Matcher matcher = matcherMap.get(tag);
                if (matcher == null) {
                    matcherMap.put(tag, matcher = tagPattern.matcher(tag));
                }
                if (matcher.matches()) {
                    addAllTestsToList(outputFilteredTests);
                    return;
                }
            }

            for (Test test : tests) {
                for (String tag : test.getTags()) {
                    Matcher matcher = matcherMap.get(tag);
                    if (matcher == null) {
                        matcherMap.put(tag, matcher = tagPattern.matcher(tag));
                    }
                    if (matcher.matches()) {
                        outputFilteredTests.add(test);
                    }
                }
            }
        }

        for (TestCategory subCategory : subCategories) {
            subCategory.filterTestsByTag(outputFilteredTests, tagPatterns);
        }

    }

    public void addAllTestsToList(ArrayList<Test> outputTests) {
        outputTests.addAll(tests);

        for (TestCategory subCategory : subCategories) {
            subCategory.addAllTestsToList(outputTests);
        }
    }

    public TestCategory findTestCategoryByName(String name) {
        for (TestCategory testCategory : subCategories) {
            if (testCategory.getName().contentEquals(name)) {
                return testCategory;
            }
        }
        return null;
    }

    public TestCategory addFeatureSourceParser(String featureSourceParser) {
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

    public TestCategory addFeatureSourceParser(ArrayList<String> featureSourceParsers) {
        if (featureSourceParsers != null) {
            featureSourceParsers.forEach(this::addFeatureSourceParser);
        }
        return this;
    }

    public TestCategory addStepRunner(String stepRunner) {
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

    public TestCategory addStepRunner(ArrayList<String> stepRunners) {
        if (stepRunners != null) {
            stepRunners.forEach(this::addStepRunner);
        }
        return this;
    }

    public TestCategory addTestDataSources(String testDataSources) {
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

    public TestCategory addTestDataSources(ArrayList<String> testDataSources) {
        if (testDataSources != null) {
            testDataSources.forEach(this::addTestDataSources);
        }
        return this;
    }

    public TestCategory addTags(String tag) {
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

    public TestCategory addTags(ArrayList<String> tags) {
        if (tags != null) {
            tags.forEach(this::addTags);
        }
        return this;
    }

    public void propagateAttributes(String sourceArchive, ArrayList<String> featureSourceParsers, ArrayList<String> stepRunners, ArrayList<String> testDataSources, String threadGroup, ArrayList<String> tags) {

        addFeatureSourceParser(featureSourceParsers).addStepRunner(stepRunners).addTestDataSources(testDataSources).addTags(tags);

        if (StringUtils.isEmpty(this.threadGroup) && StringUtils.isNotEmpty(threadGroup)) {
            this.threadGroup = threadGroup;
        }

        for (TestCategory testCategory : subCategories) {
            testCategory.propagateAttributes(sourceArchive, featureSourceParsers, stepRunners, testDataSources, threadGroup, tags);
        }

        for (Test test : tests) {
            test.propagateAttributes(sourceArchive, featureSourceParsers, stepRunners, testDataSources, threadGroup, tags);
        }
    }

    public void mergeWithTestCategory(TestCategory testCategory) {
        if (testCategory == null) {
            return;
        }

        if (StringUtils.isEmpty(name) && StringUtils.isNotEmpty(testCategory.name)) {
            name = testCategory.name;
        }

        if (StringUtils.isEmpty(description) && StringUtils.isNotEmpty(testCategory.description)) {
            description = testCategory.description;
        }

        if (StringUtils.isEmpty(threadGroup) && StringUtils.isNotEmpty(testCategory.threadGroup)) {
            threadGroup = testCategory.threadGroup;
        }

        addTags(testCategory.tags).addFeatureSourceParser(testCategory.featureSourceParsers).addStepRunner(testCategory.stepRunners).addTestDataSources(testCategory.testDataSources);

        for (TestCategory testSubCatToAdd : testCategory.getSubCategories()) {
            mergeTestCategory(testSubCatToAdd);
        }

        for (Test test : testCategory.getTests()) {
            mergeTest(test);
        }


    }

    public void mergeTestCategory(TestCategory testCategory) {
        if (testCategory == null) {
            return;
        }

        TestCategory existingTestCategory = findTestCategoryByName(testCategory.getName());

        if (existingTestCategory == null) {
            subCategories.add(testCategory);
        } else {
            existingTestCategory.mergeWithTestCategory(testCategory);
        }
    }

    public void mergeTest(Test test) {
        if (test == null) {
            return;
        }

        Test testToEdit = findTestByName(test.getName());

        if (testToEdit == null) {
            tests.add(test);
        } else {
            testToEdit.mergeWithTest(test);
        }
    }
}
