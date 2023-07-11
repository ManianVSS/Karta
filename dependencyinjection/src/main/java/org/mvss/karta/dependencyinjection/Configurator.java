package org.mvss.karta.dependencyinjection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class is used to load properties (Serializable fields) of an object or class.
 * The properties' serialization format supported are YAML, JSON and XML.
 * The properties are merged into a properties store which is a map of property group
 * to map of property name to property values.
 *
 * @author Manian
 */
@Log4j2
@SuppressWarnings("unused")
public class Configurator implements PropertiesInterface {
    public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType = new TypeReference<>() {
    };

    /**
     * Cached map of environment and system properties.
     */
    public SystemProperties systemProperties = new SystemProperties();


    /**
     * Property store is a mapping of group name to the map of property names to Serializable property values.
     */
    @Getter
    private final HashMap<String, HashMap<String, Serializable>> propertiesStore = new HashMap<>();


    @Override
    public boolean containsKey(String key) {
        return systemProperties.containsKey(key) || (get(key) != null);
    }

    @Override
    public Serializable get(String key) {
        if (key == null) {
            return null;
        }

        key = StringUtils.strip(key, Constants.DOT);

        if (key.isEmpty()) {
            return null;
        }

        Serializable propertyFromEnvOrSys = systemProperties.get(key);

        if (propertyFromEnvOrSys != null) {
            return yamlObjectMapper.convertValue(propertyFromEnvOrSys, Serializable.class);
        }

        String group = Constants.KARTA, keyInGroup = key;
        int keyLength = key.length();
        int splitIndex = key.indexOf(Constants.DOT);
        if (splitIndex != -1) {
            group = key.substring(0, splitIndex);
            keyInGroup = key.substring(splitIndex + 1);
        }

        HashMap<String, Serializable> groupStore = propertiesStore.get(group);
        return (groupStore == null) ? null : groupStore.get(keyInGroup);
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
    public static void mergeProperty(HashMap<String, HashMap<String, Serializable>> propertiesStore, String propertyGroupToMerge, String propertyToMerge, String propertyValue) {
        if (!propertiesStore.containsKey(propertyGroupToMerge)) {
            propertiesStore.put(propertyGroupToMerge, new HashMap<>());

            HashMap<String, Serializable> propertiesStoreGroup = propertiesStore.get(propertyGroupToMerge);
            propertiesStoreGroup.put(propertyToMerge, propertyValue);

        }
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


    /**
     * Merges a property store into the configurator's property store.
     */
    public void mergeProperties(HashMap<String, HashMap<String, Serializable>> propertiesToMerge) {
        mergeProperties(propertiesStore, propertiesToMerge);
    }

    /**
     * Merge property store parsed from the string based on the data format
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean mergePropertiesString(DataFormat dataFormat, String propertiesDataString) {
        try {
            HashMap<String, HashMap<String, Serializable>> propertiesToMerge = readPropertiesFromString(dataFormat, propertiesDataString);
            mergeProperties(propertiesToMerge);
            return true;
        } catch (IOException e) {
            log.error("Error while parsing property file", e);
            return false;
        }
    }

    /**
     * Merge multiple property files to the data store deducing data format from the file extension.
     */
    public boolean mergePropertiesFiles(String... propertyFiles) {
        if (propertyFiles == null) {
            return true;
        }

        for (String propertyFile : propertyFiles) {
            try {
                // TODO: Load from class-path folders and files for properties

                Path propertyFilePath = Paths.get(propertyFile);

                if (!Files.exists(propertyFilePath)) {
                    log.warn("Property file " + propertyFile + " does not exist");
                    continue;
                }

                if (Files.isDirectory(propertyFilePath)) {
                    for (File propFileInDirectory : FileUtils.listFiles(propertyFilePath.toFile(), Constants.propertyFileExtensions, true)) {
                        String propertyFileContents = FileUtils.readFileToString(propFileInDirectory, Charset.defaultCharset());

                        if (propertyFileContents != null) {
                            if (!mergePropertiesString(ParserUtils.getFileDataFormat(propFileInDirectory.getName()), propertyFileContents)) {
                                log.error("Error while parsing properties from file " + propertyFile);
                                return false;
                            }
                        }
                    }
                    continue;
                }

                String propertyFileContents = ClassPathLoaderUtils.readAllText(propertyFile);

                if (propertyFileContents != null) {
                    if (!mergePropertiesString(ParserUtils.getFileDataFormat(propertyFile), propertyFileContents)) {
                        log.error("Error while parsing properties from file " + propertyFile);
                        return false;
                    }
                }

            } catch (IOException | URISyntaxException e) {
                log.error("Error while parsing properties from file " + propertyFile, e);
                return false;
            }
        }
        return true;

    }

    /**
     * Fetch property value by group name and property name.
     */
    public Serializable getPropertyValue(String group, String name) {

        String keyForEnvOrSys = group + Constants.UNDERSCORE + name;
        String propertyFromEnvOrSys = systemProperties.get(keyForEnvOrSys.toUpperCase());

        if (propertyFromEnvOrSys != null) {
            return yamlObjectMapper.convertValue(propertyFromEnvOrSys, Serializable.class);
        }

        HashMap<String, Serializable> groupStore = propertiesStore.get(group);
        return (groupStore == null) ? null : groupStore.get(name);
    }

    /**
     * Load properties into multiple objects.
     */
    public void loadProperties(Object... objects) throws IllegalArgumentException {
        for (Object object : objects) {
            AnnotationScanner.forEachField(object.getClass(), PropertyMapping.class, AnnotationScanner.IS_NON_STATIC.and(AnnotationScanner.IS_NON_FINAL), (type, field, annotation) -> setFieldValue(object, field, (PropertyMapping) annotation));
        }
    }

    /**
     * Load properties to static fields of the multiple class
     */
    public void loadPropertiesIntoClasses(Class<?>... classesToLoadPropertiesWith) throws IllegalArgumentException {
        for (Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith) {
            AnnotationScanner.forEachField(classToLoadPropertiesWith, PropertyMapping.class, AnnotationScanner.IS_STATIC.and(AnnotationScanner.IS_NON_FINAL), (type, field, annotation) -> setFieldValue(null, field, (PropertyMapping) annotation));
        }
    }

    /**
     * Converts the properties store into a properties map which can be used to substitute values
     */
    public HashMap<String, String> convertToPropertyMap() {
        HashMap<String, String> propertyMap = new HashMap<>();

        for (Map.Entry<String, HashMap<String, Serializable>> propertyStoreEntry : propertiesStore.entrySet()) {
            for (Map.Entry<String, Serializable> properties : propertyStoreEntry.getValue().entrySet()) {
                try {
                    propertyMap.put(propertyStoreEntry.getKey() + Constants.DOT + properties.getKey(), objectMapper.writeValueAsString(properties.getValue()));
                } catch (JsonProcessingException e) {
                    propertyMap.put(propertyStoreEntry.getKey() + Constants.DOT + properties.getKey(), properties.getValue().toString());
                }
            }
        }

        systemProperties.mergeEnvValuesIntoMap(propertyMap);

        return propertyMap;
    }

    public void createFileFromTemplate(String templateFileName, String fileToCreate) throws IOException {
        String templateString = FileUtils.readFileToString(new File(templateFileName), Charset.defaultCharset());
        FileUtils.writeStringToFile(new File(fileToCreate), expandPropertiesIntoText(templateString), Charset.defaultCharset());
    }

    /**
     * Sets the value of an object's field by matching property from a property store based on PropertyMapping annotation
     * Note: A property store is a HashMap of property group name to HashMap of property names and values for the group.
     */
    public void setFieldValue(Object object, Field field, PropertyMapping propertyMapping) {
        try {
            String propertyGroup = propertyMapping.group();
            String propertyName = DataUtils.pickString(StringUtils::isNotEmpty, propertyMapping.name(), propertyMapping.value(), field.getName());

            Serializable propertyValue = getPropertyValue(propertyGroup, propertyName);
            JavaType covertToTypeTo = objectMapper.getTypeFactory().constructType((Object.class == propertyMapping.type()) ? field.getGenericType() : propertyMapping.type());
            setFieldValue(object, field, propertyValue, covertToTypeTo);
        } catch (Throwable t) {
            log.error("Error setting field: " + field + " value for object: " + object, t);
        }
    }

    /**
     * Sets the value of an object's field based on the field type converting from the serializable value (property matched)
     */
    public void setFieldValue(Object object, Field field, Serializable propertyValue, JavaType castAsType) {
        try {
            field.setAccessible(true);

            if (propertyValue != null) {
                if (castAsType == null) {
                    castAsType = objectMapper.getTypeFactory().constructType(field.getType());
                }

                if ((castAsType.getRawClass() == Pattern.class) && (propertyValue.getClass() == String.class)) {
                    field.set(object, Pattern.compile((String) propertyValue));
                } else {
                    field.set(object, objectMapper.convertValue(propertyValue, castAsType));
                }
            }
        } catch (Throwable t) {
            log.error("Error setting field: " + field + " value: " + propertyValue + " for object: " + object, t);
        }
    }


}
