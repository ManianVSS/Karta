package org.mvss.karta.dependencyinjection.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Utility class to load bean properties from property files (YAML, JSON or XML)
 *
 * @author Manian
 */
@Log4j2
@SuppressWarnings("unused")
public class PropertyUtils {

    public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType = new TypeReference<>() {
    };

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

    /**
     * Merge a properties store into the destination
     */
    public static void mergeProperties(HashMap<String, HashMap<String, Serializable>> propertiesStore, HashMap<String, HashMap<String, Serializable>> propertiesToMerge) {
        if (propertiesToMerge == null) {
            return;
        }

        for (String propertyGroupToMerge : propertiesToMerge.keySet()) {
            if (!propertiesStore.containsKey(propertyGroupToMerge)) {
                propertiesStore.put(propertyGroupToMerge, new HashMap<>());
            }

            HashMap<String, Serializable> propertiesStoreGroup = propertiesStore.get(propertyGroupToMerge);
            HashMap<String, Serializable> propertiesToMergeForGroup = propertiesToMerge.get(propertyGroupToMerge);

            for (String propertyToMerge : propertiesToMergeForGroup.keySet()) {
                propertiesStoreGroup.put(propertyToMerge, propertiesToMergeForGroup.get(propertyToMerge));
            }
        }
    }

    /**
     * Merge a property to a property store based on group and key name
     */
    public static void mergeProperty(HashMap<String, HashMap<String, Serializable>> propertiesStore, String propertyGroupToMerge, String propertyToMerge, Serializable propertyValue) {
        if (!propertiesStore.containsKey(propertyGroupToMerge)) {
            propertiesStore.put(propertyGroupToMerge, new HashMap<>());
        }

        HashMap<String, Serializable> propertiesStoreGroup = propertiesStore.get(propertyGroupToMerge);
        propertiesStoreGroup.put(propertyToMerge, propertyValue);
    }

    /**
     * Read property store from String based on the data format
     */
    public static HashMap<String, HashMap<String, Serializable>> readPropertiesFromString(DataFormat dataFormat, String propertiesDataString) throws IOException {
        if (dataFormat == DataFormat.PROPERTIES) {
            try (StringReader stringReader = new StringReader(propertiesDataString)) {
                Properties properties = new Properties();
                properties.load(stringReader);

                HashMap<String, HashMap<String, Serializable>> propertiesStore = new HashMap<>();

                for (Object propertyKeyObj : properties.keySet()) {
                    String propertyKey = (String) propertyKeyObj;
                    String propertyGroup = Constants.KARTA;
                    String propertyValue = properties.getProperty(propertyKey);

                    int pivotIndex = propertyKey.indexOf(Constants.DOT);
                    if (DataUtils.inRange(pivotIndex, 0, propertyKey.length() - 2)) {
                        propertyGroup = propertyKey.substring(0, pivotIndex);
                        propertyKey = propertyKey.substring(pivotIndex + 1);
                    }

                    mergeProperty(propertiesStore, propertyGroup, propertyKey, propertyValue);
                }
                return propertiesStore;
            }
        }

        return ParserUtils.readValue(dataFormat, propertiesDataString, propertiesType);
    }

}
