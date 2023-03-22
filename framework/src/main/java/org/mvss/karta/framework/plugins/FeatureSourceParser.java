package org.mvss.karta.framework.plugins;

import org.mvss.karta.dependencyinjection.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.models.test.TestFeature;

public interface FeatureSourceParser extends Plugin {

    boolean isValidFeatureFile(String fileName);

    TestFeature parseFeatureSource(String sourceCode) throws Throwable;

    default TestFeature parseFeatureFile(String featureFileName) throws Throwable {
        return parseFeatureSource(ClassPathLoaderUtils.readAllText(featureFileName));
    }

//    default TestFeature parseFeatureFileFromJar(String featureFileName, String jarFileName) throws Throwable {
//        InputStream jarFileStream = DynamicClassLoader.getClassPathResourceInJarAsStream(jarFileName, featureFileName);
//        if (jarFileStream == null) {
//            return null;
//        }
//        String featureSourceCode = IOUtils.toString(jarFileStream, Charset.defaultCharset());
//        return parseFeatureSource(featureSourceCode);
//    }
}
