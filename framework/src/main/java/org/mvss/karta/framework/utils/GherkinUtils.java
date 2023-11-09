package org.mvss.karta.framework.utils;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.utils.DataUtils;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestScenario;
import org.mvss.karta.framework.models.test.TestStep;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings({"UnusedAssignment", "ReassignedVariable", "ConstantConditions"})
public class GherkinUtils {
    public static final List<String> conjunctions = Arrays.asList("Given ", "When ", "Then ", "And ", "But ");
    public static final String LINE_BREAK = "\n";

    public static final String FEATURE = "Feature:";
    public static final String AT_THE_RATE = "@";
    public static final String SCENARIO = "Scenario:";
    public static final String SCENARIO_OUTLINE = "Scenario Outline:";
    public static final String BACKGROUND = "Background:";
    public static final String HASH = "#";
    public static final String EXAMPLES = "Examples:";
    public static final String VERTICAL_LINE = "|";
    public static final String REGEX_VERTICAL_LINE = "[|]";


    public static TestFeature parseFeatureSource(String sourceCode, ArrayList<String> featureTagList) throws Throwable {
        TestFeature feature = new TestFeature();
        String[] lines = sourceCode.split(LINE_BREAK);
        int linePointer = 0;

        boolean featureFound = false;

        while (!featureFound && (linePointer < lines.length)) {
            String line = lines[linePointer++];
            line = line.trim();

            if (line.startsWith(FEATURE)) {
                feature.setName(line.substring(FEATURE.length()).trim());
                feature.setDescription(Constants.EMPTY_STRING);
                featureFound = true;
                break;
            } else if (line.stripLeading().startsWith(AT_THE_RATE)) {

                for (String word : line.split(Constants.REGEX_WHITESPACE)) {
                    word = word.trim();

                    if (StringUtils.isBlank(word)) {
                        continue;
                    }

                    if (word.startsWith(AT_THE_RATE) && (word.length() > 1)) {
                        String tag = word.substring(1).trim();
                        if (featureTagList != null) {
                            featureTagList.add(tag);
                        }
                        feature.getTags().add(tag);
                    }

                }
            } else {
                throw new Exception("Unexpected sentence: " + line);
            }
        }

        if (!featureFound) {
            throw new Exception("Feature source is missing feature declaration ");
        }

        boolean scenarioFound = false;
        boolean isScenarioOutline = false;
        TestScenario testScenario = null;
        ArrayList<TestStep> stepsContainer = null;
        Stack<ArrayList<TestStep>> stepContainerStack = new Stack<>();
        TestStep currentStep = null;
        boolean inExamples = false;
        boolean inDataTable = false;
        ArrayList<String> headerList = null;
        HashMap<String, ArrayList<Serializable>> testData = null;
        boolean inStepScope = false;

        while (linePointer < lines.length) {
            String line = lines[linePointer++];
            line = line.trim();

            if (line.startsWith(VERTICAL_LINE) && inStepScope) {
                inStepScope = false;
                inDataTable = true;
            }

            if (inDataTable || inExamples) {
                if (line.startsWith(VERTICAL_LINE)) {
                    if (headerList == null) {
                        headerList = new ArrayList<>();
                        testData = new HashMap<>();
                        for (String item : line.split(REGEX_VERTICAL_LINE)) {
                            String trimmedItem = item.trim();
                            if (StringUtils.isNotEmpty(trimmedItem)) {
                                headerList.add(trimmedItem);
                                testData.put(trimmedItem, new ArrayList<>());
                            }
                        }
                    } else {
                        int i = 0;
                        for (String item : line.split(REGEX_VERTICAL_LINE)) {
                            String trimmedItem = item.trim();
                            if (StringUtils.isNotEmpty(trimmedItem)) {
                                testData.get(headerList.get(i)).add(trimmedItem);
                                i++;
                            }
                        }
                    }
                    continue;
                } else {
                    if (inDataTable) {
                        if (currentStep.getTestDataSet() == null) {
                            currentStep.setTestDataSet(new HashMap<>());
                        }
                        DataUtils.mergeMapInto(testData, currentStep.getTestDataSet());
                    } else {
                        if (testScenario == null) {
                            if (feature.getTestDataSet() == null) {
                                feature.setTestDataSet(new HashMap<>());
                            }
                            DataUtils.mergeMapInto(testData, feature.getTestDataSet());
                        } else {
                            if (testScenario.getTestDataSet() == null) {
                                testScenario.setTestDataSet(new HashMap<>());
                            }
                            DataUtils.mergeMapInto(testData, testScenario.getTestDataSet());
                        }
                    }

                    headerList = null;
                    testData = null;
                    inExamples = false;
                    inDataTable = false;
                }
            }

            if (StringUtils.isEmpty(line)) {
                isScenarioOutline = false;
                inStepScope = false;
                inExamples = false;
                inDataTable = false;
            }

            if (line.startsWith(SCENARIO) || line.startsWith(SCENARIO_OUTLINE)) {
                inStepScope = false;
                isScenarioOutline = line.startsWith(SCENARIO_OUTLINE);
                testScenario = new TestScenario();
                testScenario.setName(line.substring((isScenarioOutline ? SCENARIO_OUTLINE : SCENARIO).length()).trim());
                testScenario.setDescription(Constants.EMPTY_STRING);
                feature.getTestScenarios().add(testScenario);
                if (stepsContainer != null) {
                    stepContainerStack.push(stepsContainer);
                }
                stepsContainer = testScenario.getExecutionSteps();
                scenarioFound = true;
                continue;
            }

            if (line.startsWith(BACKGROUND)) {
                inStepScope = false;
                isScenarioOutline = false;
                if (stepsContainer != null) {
                    stepContainerStack.push(stepsContainer);
                }
                stepsContainer = feature.getScenarioSetupSteps();
                continue;
            }

            if (line.startsWith(HASH)) {
                continue;
            }

            if (line.startsWith(EXAMPLES)) {
                inStepScope = false;
                inExamples = true;
                isScenarioOutline = false;
                continue;
            }

            if (line.equals("parallel{")) {
                if (currentStep == null) {
                    throw new Exception("Can't define parallel steps without a base step");
                }
                currentStep.setRunStepsInParallel(true);

                if (stepsContainer != null) {
                    stepContainerStack.push(stepsContainer);
                }
                stepsContainer = currentStep.getSteps();
            }
            if (line.equals("}")) {
                if (stepContainerStack.isEmpty()) {
                    throw new Exception("Unexpected delimiter for parallel steps");
                }
                stepsContainer = stepContainerStack.pop();
            }

            boolean stepFound = false;
            for (String conjunction : conjunctions) {
                if (line.startsWith(conjunction)) {
                    stepFound = true;
                    currentStep = TestStep.builder().step(line.substring(conjunction.length()).trim()).build();
                    stepsContainer.add(currentStep);
                    inStepScope = true;
                    isScenarioOutline = false;
                    break;
                }
            }
            if (!stepFound) {
                inStepScope = false;
                isScenarioOutline = false;
                if (scenarioFound) {
                    testScenario.setDescription((testScenario.getDescription() + line + LINE_BREAK).trim());
                } else {
                    feature.setDescription((feature.getDescription() + line + LINE_BREAK).trim());
                }
            }
        }

        return feature;
    }
}
