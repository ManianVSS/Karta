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

    public void propagateAttributes(String sourceArchive, ArrayList<String> inFeatureSourceParsers, ArrayList<String> srp, ArrayList<String> inTestDataSources, String tg, ArrayList<String> tags) {

        if (inFeatureSourceParsers != null) {
            inFeatureSourceParsers.forEach(item -> {
                if (!featureSourceParsers.contains(item)) featureSourceParsers.add(item);
            });
        }

        if (srp != null) {
            srp.forEach(item -> {
                if (!stepRunners.contains(item)) stepRunners.add(item);
            });
        }

        if (inTestDataSources != null) {
            inTestDataSources.forEach(item -> {
                if (!testDataSources.contains(item)) testDataSources.add(item);
            });
        }

        if (StringUtils.isEmpty(threadGroup) && StringUtils.isNotEmpty(tg)) {
            threadGroup = tg;
        }

        if (tags != null) {
            tags.forEach(item -> {
                if (!this.tags.contains(item)) this.tags.add(item);
            });
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

        testCategory.tags.forEach(item -> {
            if (!this.tags.contains(item)) this.tags.add(item);
        });

        if (featureSourceParsers.isEmpty() && !testCategory.featureSourceParsers.isEmpty()) {
            testCategory.featureSourceParsers.forEach(item -> {
                if (!featureSourceParsers.contains(item)) featureSourceParsers.add(item);
            });
        }

        if (stepRunners.isEmpty() && !testCategory.stepRunners.isEmpty()) {
            testCategory.stepRunners.forEach(item -> {
                if (!stepRunners.contains(item)) stepRunners.add(item);
            });
        }

        if (testDataSources.isEmpty() && !testCategory.testDataSources.isEmpty()) {
            testCategory.testDataSources.forEach(item -> {
                if (!testDataSources.contains(item)) testDataSources.add(item);
            });
        }

        for (TestCategory testSubCatToAdd : testCategory.getSubCategories()) {
            mergeTestCategory(testSubCatToAdd);
        }

        for (Test test : testCategory.getTests()) {
            mergeTest(test);
        }

        if (StringUtils.isEmpty(threadGroup) && StringUtils.isNotEmpty(testCategory.threadGroup)) {
            threadGroup = testCategory.threadGroup;
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
