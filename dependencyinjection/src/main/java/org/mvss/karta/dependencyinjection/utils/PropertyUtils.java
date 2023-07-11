package org.mvss.karta.dependencyinjection.utils;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.Constants;

import java.io.*;
import java.util.Properties;

/**
 * Utility class to load bean properties from property files (YAML, JSON or XML)
 *
 * @author Manian
 */
@Log4j2
@SuppressWarnings("unused")
public class PropertyUtils {

    /**
     * Cached environment and system properties with system properties having higher precedence
     */
    public static final SystemProperties systemProperties = new SystemProperties();


    public static String evaluatePropertyValue(String key, Properties properties, String defaultValue) {
        return systemProperties.getOrDefault(key, properties.getProperty(key, defaultValue));
    }

    public static void updatePropertyFileFromEnvironment(String propertyFileName) throws IOException {
        File propertyFile = new File(propertyFileName);

        Properties properties = new Properties();

        if (!propertyFile.exists()) {
            InputStream is = PropertyUtils.class.getResourceAsStream(Constants.SLASH + propertyFileName);
            if (is == null) {
                throw new IOException("Properties file:" + propertyFileName + " could not found.");
            }
            try (is) {
                properties.load(is);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(propertyFile)) {
                properties.load(fis);
            }
        }

        for (Object key : properties.keySet()) {
            String keyStr = (String) key;

            properties.setProperty(keyStr, evaluatePropertyValue(keyStr, properties, null));
        }

        try (FileOutputStream fos = new FileOutputStream(propertyFile)) {
            properties.store(fos, "Modified env properties after loading from environment");
        }
    }
}
