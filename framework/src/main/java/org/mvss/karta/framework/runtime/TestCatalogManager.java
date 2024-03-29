package org.mvss.karta.framework.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.utils.ClassPathLoaderUtils;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.interfaces.FeatureSourceParserLookup;
import org.mvss.karta.framework.models.catalog.Test;
import org.mvss.karta.framework.models.catalog.TestCategory;
import org.mvss.karta.framework.utils.DynamicClassLoader;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Log4j2
@Getter
@NoArgsConstructor
@SuppressWarnings("unused")
public class TestCatalogManager {
    private static final ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();
    private static final ConcurrentHashMap<String, Pattern> patternsMap = new ConcurrentHashMap<>();
    private final TestCategory testCatalog = new TestCategory();

    public void mergeFeatureFiles(FeatureSourceParserLookup featureSourceParserLookup) throws Throwable {
        testCatalog.mergeFeatureFiles(featureSourceParserLookup);
        testCatalog.propagateAttributes(null, testCatalog.getFeatureSourceParsers(), testCatalog.getStepRunners(), testCatalog.getTestDataSources(), null, testCatalog.getTags());
    }

    public void mergeWithCatalog(TestCategory updatesToRootCategory) {
        updatesToRootCategory.propagateAttributes(null, updatesToRootCategory.getFeatureSourceParsers(), updatesToRootCategory.getStepRunners(), updatesToRootCategory.getTestDataSources(), updatesToRootCategory.getThreadGroup(), updatesToRootCategory.getTags());
        testCatalog.mergeWithTestCategory(updatesToRootCategory);
    }

    public void mergeWithCatalog(String sourceArchive) throws Throwable {
        InputStream fileStream;

        if (StringUtils.isNotBlank(sourceArchive)) {
            ClassLoader loader = DynamicClassLoader.getClassLoaderForJar(sourceArchive);

            if (loader == null) {
                return;
            }
            fileStream = loader.getResourceAsStream(Constants.TEST_CATALOG_FRAGMENT_FILE_NAME);
        } else {
            fileStream = ClassPathLoaderUtils.getFileStream(Constants.TEST_CATALOG_FRAGMENT_FILE_NAME);
        }

        if (fileStream == null) {
            return;
        }

        TestCategory updatesToRootCategory = yamlObjectMapper.readValue(IOUtils.toString(fileStream, Charset.defaultCharset()), TestCategory.class);
        updatesToRootCategory.propagateAttributes(sourceArchive, updatesToRootCategory.getFeatureSourceParsers(), updatesToRootCategory.getStepRunners(), updatesToRootCategory.getTestDataSources(), updatesToRootCategory.getThreadGroup(), updatesToRootCategory.getTags());
        testCatalog.mergeWithTestCategory(updatesToRootCategory);
    }

    public void mergeRepositoryDirectoryIntoCatalog(File repositoryDirectory) {
        try {
            mergeWithCatalog((String) null);
        } catch (Throwable t) {
            log.error("Failed to load test catalog from classpath", t);
        }

        for (File jarFile : FileUtils.listFiles(repositoryDirectory, Constants.jarExtension, true)) {
            try {
                mergeWithCatalog(jarFile.getAbsolutePath());
            } catch (Throwable t) {
                log.error("Failed to load test repository jar: " + jarFile.getAbsolutePath(), t);
            }
        }
    }

    public void mergeSubCategory(TestCategory testCategoryToMerge) {
        testCatalog.mergeTestCategory(testCategoryToMerge);
    }

    public void addTest(Test testToMerge) {
        testCatalog.mergeTest(testToMerge);
    }

    public void addTest(String sourceArchive, Test testToMerge) {
        testCatalog.mergeTest(testToMerge);
    }

    public synchronized ArrayList<Test> filterTestsByTag(ArrayList<String> tags) {
        ArrayList<Test> outputFilteredTests = new ArrayList<>();
        HashSet<Pattern> tagPatterns = new HashSet<>();

        for (String tag : tags) {
            if (!patternsMap.containsKey(tag)) {
                patternsMap.put(tag, Pattern.compile(tag));
            }
            Pattern tagPattern = patternsMap.get(tag);
            tagPatterns.add(tagPattern);
        }
        testCatalog.filterTestsByTag(outputFilteredTests, tagPatterns);
        return outputFilteredTests;
    }
}
