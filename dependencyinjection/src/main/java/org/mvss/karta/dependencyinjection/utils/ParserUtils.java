package org.mvss.karta.dependencyinjection.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Utility class for parsing data and serializing to string for different formats (YAML, JSON, XML)
 *
 * @author Manian
 */
public class ParserUtils {
    public static final TypeReference<HashMap<String, Serializable>> genericHashMapObjectType = new TypeReference<>() {
    };

    private static final TypeReference<ArrayList<String>> arrayListOfStringType = new TypeReference<>() {
    };

    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private static final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    @Getter
    private static final XmlMapper xmlMapper = new XmlMapper();

    @Getter
    private static final BeanUtilsBean nullAwareBeanUtils = new NullAwareBeanUtilsBean();

    public static final String[] YAML_STR_TO_ESCAPE = {",", ":", "&", "*", "?", "|", ">", "%", "@"};

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.findAndRegisterModules();

        yamlObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        yamlObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        yamlObjectMapper.findAndRegisterModules();

        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.findAndRegisterModules();
    }

    /**
     * Parses a list of string from String (JSON source)
     */
    public static ArrayList<String> parseListOfStringFromJson(String source) throws JsonProcessingException {
        return objectMapper.readValue(source, arrayListOfStringType);
    }

    /**
     * Parses a list of string from String (YAML source)
     */
    public static ArrayList<String> parseListOfStringFromYaml(String source) throws JsonProcessingException {
        return yamlObjectMapper.readValue(source, arrayListOfStringType);
    }

    /**
     * Generic method to parse a serializable object of type T based on data format and type reference from the string source.
     */
    public static <T> T readValue(DataFormat format, String content, TypeReference<T> valueTypeRef) throws IOException {
        switch (format) {
            case JSON:
                return objectMapper.readValue(content, valueTypeRef);
            case XML:
                return xmlMapper.readValue(content, valueTypeRef);

            case PROPERTIES:
                try (StringReader stringReader = new StringReader(content)) {
                    Properties properties = new Properties();
                    properties.load(stringReader);
                    return objectMapper.readValue(objectMapper.writeValueAsString(properties), valueTypeRef);
                }

            default:
            case YAML:
                return yamlObjectMapper.readValue(content, valueTypeRef);
        }
    }

    /**
     * Generic method to parse a serializable object of type T based on data format and class from the string source.
     */
    public static <T> T readValue(DataFormat format, String content, Class<T> valueType) throws IOException {
        switch (format) {
            case JSON:
                return objectMapper.readValue(content, valueType);
            case XML:
                return xmlMapper.readValue(content, valueType);

            case PROPERTIES:
                try (StringReader stringReader = new StringReader(content)) {
                    Properties properties = new Properties();
                    properties.load(stringReader);
                    return objectMapper.readValue(objectMapper.writeValueAsString(properties), valueType);
                }

            default:
            case YAML:
                return yamlObjectMapper.readValue(content, valueType);
        }
    }

    /**
     * Generic method to convert an object to type T based on data format from another value of type reference .
     */
    public static <T> T convertValue(DataFormat format, Object fromValue, TypeReference<T> valueTypeRef) {
        switch (format) {
            case JSON:
                return objectMapper.convertValue(fromValue, valueTypeRef);
            case XML:
                return xmlMapper.convertValue(fromValue, valueTypeRef);

            default:
            case YAML:
                return yamlObjectMapper.convertValue(fromValue, valueTypeRef);
        }
    }

    /**
     * Generic method to convert an object to type T based on data format from another value of different class compatible with respect to object properties.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(DataFormat format, Object fromValue, Class<T> toValueType) {
        if (fromValue.getClass() == toValueType) {
            return (T) fromValue;
        }

        switch (format) {
            case JSON:
                return objectMapper.convertValue(fromValue, toValueType);
            case XML:
                return xmlMapper.convertValue(fromValue, toValueType);

            default:
            case YAML:
                return yamlObjectMapper.convertValue(fromValue, toValueType);
        }
    }

    /**
     * Get data format for file by name based on the file extension
     */
    public static DataFormat getFileDataFormat(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        // ( fileName.contains( Constants.DOT ) && !fileName.endsWith( Constants.DOT ) ) ? fileName.toLowerCase().substring( fileName.lastIndexOf( Constants.DOT ) + 1 ) : Constants.EMPTY_STRING;
        if (fileExtension.equals(Constants.JSON)) {
            return DataFormat.JSON;
        } else if (fileExtension.equals(Constants.XML)) {
            return DataFormat.XML;
        } else if (fileExtension.contentEquals(Constants.PROPERTIES)) {
            return DataFormat.PROPERTIES;
        } else// if ( fileExtension.equals( Constants.YAML ) || fileExtension.equals( Constants.YML ) )
        {
            return DataFormat.YAML;
        }
    }

    public static String serializableToString(Serializable serializable) {
        if (serializable.getClass() == String.class) {
            return (String) serializable;
        }

        try {
            return objectMapper.writeValueAsString(serializable);
        } catch (JsonProcessingException jpe) {
            return null;
        }
    }

    public static <T> T readValue(String content, Class<T> valueType) throws JsonProcessingException {

        String contentTrim = content.trim();

        if (StringUtils.startsWithAny(contentTrim, YAML_STR_TO_ESCAPE)
                || (contentTrim.startsWith("-") && !RegexUtil.isNumeric(contentTrim))) {
            return yamlObjectMapper.readValue("\"" + content + "\"", valueType);
        }

        return yamlObjectMapper.readValue(content, valueType);
    }
}
