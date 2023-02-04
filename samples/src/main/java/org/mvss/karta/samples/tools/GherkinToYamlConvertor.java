package org.mvss.karta.samples.tools;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.models.catalog.Test;
import org.mvss.karta.framework.models.catalog.TestCategory;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestScenario;
import org.mvss.karta.samples.utils.GherkinUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;

@Log4j2
public class GherkinToYamlConvertor {
    public static final String[] featureExtension = {"feature"};
    private static final String GHERKIN_TO_YAML_CONVERTER = "Gherkin to YAML converter";
    private static final String HELP = "help";
    private static final String FEATURE_FILE = "featureFile";
    private static final String FEATURE_FILES_DIRECTORY = "featureFilesDirectory";
    private static final String OUTPUT = "output";
    private static final String GENERATE_TEST_CATALOG = "generateTestCatalog";
    private static final String CONVERTED = "Converted-";
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper kriyaYamlObjectMapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));

    public static void convertFeatureFile(File featureFile, String outputYamlFileName, TestCategory testCategory) throws Throwable {
        ArrayList<String> featureTagList = new ArrayList<>();
        String featureFileSource = FileUtils.readFileToString(featureFile, Charset.defaultCharset());
        TestFeature feature = GherkinUtils.parseFeatureSource(featureFileSource, featureTagList);

        if (feature.getTestJobs().isEmpty()) {
            feature.setTestJobs(null);
        }

        if (feature.getSetupSteps().isEmpty()) {
            feature.setSetupSteps(null);
        }

        if (feature.getScenarioSetupSteps().isEmpty()) {
            feature.setScenarioSetupSteps(null);
        }

        if (feature.getTestScenarios().isEmpty()) {
            feature.setTestScenarios(null);
        } else {
            for (TestScenario scenario : feature.getTestScenarios()) {
                if (StringUtils.isBlank(scenario.getDescription())) {
                    scenario.setDescription(null);
                }
                if (scenario.getSetupSteps().isEmpty()) {
                    scenario.setSetupSteps(null);
                }
                if (scenario.getTearDownSteps().isEmpty()) {
                    scenario.setTearDownSteps(null);
                }
            }
        }

        if (feature.getScenarioTearDownSteps().isEmpty()) {
            feature.setScenarioTearDownSteps(null);
        }

        if (feature.getTearDownSteps().isEmpty()) {
            feature.setTearDownSteps(null);
        }

        if (testCategory != null) {
            Test test = new Test();
            test.setName(feature.getName());
            test.setDescription(feature.getDescription());
            test.setPriority(null);
            test.getTags().addAll(featureTagList);
            test.setTestDataSources(null);
            test.setFeatureFileName(outputYamlFileName);
            test.setChanceBasedScenarioExecution(null);
            test.setExclusiveScenarioPerIteration(null);
            testCategory.getTests().add(test);
        }
        String yamlString = kriyaYamlObjectMapper.writeValueAsString(feature);
        FileUtils.write(new File(outputYamlFileName), yamlString, Charset.defaultCharset());
    }

    public static void main(String[] args) {
        yamlObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        yamlObjectMapper.setSerializationInclusion(Include.NON_NULL);

        kriyaYamlObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        kriyaYamlObjectMapper.setSerializationInclusion(Include.NON_NULL);

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        DefaultParser parser = new DefaultParser();

        options.addOption("f", FEATURE_FILE, true, "feature file to convert");
        options.addOption("d", FEATURE_FILES_DIRECTORY, true, "feature file directory to convert");
        options.addOption("o", OUTPUT, true, "output YAML feature file name. Defaults to <fileName>.yaml");
        options.addOption("g", GENERATE_TEST_CATALOG, true, "generate test catalog file KartaTestCatalogFragment.yaml. Defaults to true.");
        options.addOption(null, HELP, false, "prints this help message");

        boolean generateTestCatalog = true;

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(GENERATE_TEST_CATALOG)) {
                generateTestCatalog = Boolean.parseBoolean(cmd.getOptionValue(GENERATE_TEST_CATALOG));
            }

            if (cmd.hasOption(HELP)) {
                formatter.printHelp(GHERKIN_TO_YAML_CONVERTER, options);
                System.exit(0);
            } else if (cmd.hasOption(FEATURE_FILE)) {
                String featureFileName = cmd.getOptionValue(FEATURE_FILE);
                File featureFile = new File(featureFileName);

                if (!featureFile.exists() || !featureFile.isFile()) {
                    System.err.println("Feature file could not be found: " + featureFileName);
                    System.exit(-3);
                }

                String outputYamlFile = FilenameUtils.removeExtension(featureFileName) + Constants.DOT + Constants.YAML;

                if (cmd.hasOption(OUTPUT)) {
                    outputYamlFile = cmd.getOptionValue(OUTPUT);
                }

                TestCategory testCategory = null;
                if (generateTestCatalog) {
                    testCategory = new TestCategory();
                    testCategory.setName(CONVERTED + Instant.now().toEpochMilli());
                    testCategory.setDescription("Tests coverted from Gherkin to YAML format on " + Instant.now().toString() + ".");
                    // testCategory.setTags( null );
                    testCategory.setTestDataSources(null);
                    testCategory.setSubCategories(null);
                }
                convertFeatureFile(featureFile, outputYamlFile, testCategory);
                if (generateTestCatalog) {
                    String fileName = Constants.TEST_CATALOG_FRAGMENT_FILE_NAME;

                    int prefix = 0;
                    while (Files.exists(Paths.get(fileName))) {
                        fileName = "(" + (++prefix) + ")" + Constants.TEST_CATALOG_FRAGMENT_FILE_NAME;
                    }

                    String yamlString = yamlObjectMapper.writeValueAsString(testCategory);
                    FileUtils.writeStringToFile(new File(fileName), yamlString, Charset.defaultCharset());
                }
            } else if (cmd.hasOption(FEATURE_FILES_DIRECTORY)) {
                String featureFileDirectoryName = cmd.getOptionValue(FEATURE_FILES_DIRECTORY);
                File featureFileDirectory = new File(featureFileDirectoryName);

                if (!featureFileDirectory.exists() || !featureFileDirectory.isDirectory()) {
                    System.err.println("Feature files directory could not be found: " + featureFileDirectoryName);
                    System.exit(-3);
                }
                TestCategory testCategory = null;
                if (generateTestCatalog) {
                    testCategory = new TestCategory();
                    testCategory.setName(CONVERTED + Instant.now().toEpochMilli());
                    testCategory.setDescription("Tests coverted from Gherkin to YAML format on " + Instant.now().toString() + ".");
                    // testCategory.setTags( null );
                    testCategory.setTestDataSources(null);
                    testCategory.setSubCategories(null);
                }

                for (File featureFile : FileUtils.listFiles(featureFileDirectory, featureExtension, true)) {
                    String outputYamlFile = FilenameUtils.removeExtension(featureFile.getAbsolutePath()) + Constants.DOT + Constants.YAML;
                    convertFeatureFile(featureFile, outputYamlFile, testCategory);
                }

                if (generateTestCatalog) {
                    String fileName = Constants.TEST_CATALOG_FRAGMENT_FILE_NAME;

                    int prefix = 0;
                    while (Files.exists(Paths.get(fileName))) {
                        fileName = "(" + (++prefix) + ")" + Constants.TEST_CATALOG_FRAGMENT_FILE_NAME;
                    }

                    String yamlString = yamlObjectMapper.writeValueAsString(testCategory);
                    FileUtils.writeStringToFile(new File(fileName), yamlString, Charset.defaultCharset());
                }

            } else {
                System.err.println("Missing option");
                formatter.printHelp(GHERKIN_TO_YAML_CONVERTER, options);
                System.exit(-1);
            }
        } catch (Throwable t) {
            log.info("Exception occured when running " + GHERKIN_TO_YAML_CONVERTER, t);
            System.exit(-2);
        }
    }
}
