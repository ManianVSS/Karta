package org.mvss.karta.dependencyinjection.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvss.karta.dependencyinjection.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PropertiesInterface extends Serializable {

    ObjectMapper objectMapper = ParserUtils.getObjectMapper();
    ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();

    /**
     * The compiled regex patter for matching property references in format ${propertyName}
     */
    Pattern propertyPattern = Pattern.compile("\\$\\{([_A-Za-z0-9]+)}");
    //"\\$\\{([_A-Za-z0-9]+)\\}"

    boolean containsKey(String key);

    Serializable get(String key);

    default Serializable getOrDefault(String key, String defaultValue) {
        Serializable evaluatedProperty = get(key);
        return (evaluatedProperty != null) ? evaluatedProperty : defaultValue;
    }

    default String expandPropertiesIntoText(String text) {
        if (text == null) {
            return null;
        }

        boolean found;
        do {
            found = false;
            Matcher matcher = propertyPattern.matcher(text);

            while (matcher.find()) {
                String propValue = null;
                Serializable value = get(matcher.group(1).toUpperCase());

                if (value != null) {
                    try {
                        propValue = objectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        propValue = value.toString();
                    }
                }
                if (propValue != null) {
                    found = true;
                    propValue = propValue.replace(Constants.BACKSLASH, Constants.DOUBLE_BACKSLASH);
                    Pattern subExpression = Pattern.compile(Pattern.quote(matcher.group(0)));
                    text = subExpression.matcher(text).replaceAll(propValue);
                }
            }
        } while (found);

        return text;
    }

    /**
     * Expands system properties in format ${propertyName} in keys of the map
     */
    default <V> void expandEnvVarsForMap(Map<String, V> valueMap) {
        HashMap<String, V> expandedValue = new HashMap<>();
        valueMap.forEach((key, value) -> expandedValue.put(expandPropertiesIntoText(key), value));
        valueMap.clear();
        valueMap.putAll(expandedValue);
    }

    /**
     * Expands system properties in format ${propertyName} in keys and values of the map
     */
    default void expandEnvVars(Map<String, String> valueMap) {
        HashMap<String, String> expandedValue = new HashMap<>();
        valueMap.forEach((key, value) -> expandedValue.put(expandPropertiesIntoText(key), expandPropertiesIntoText(value)));
        valueMap.clear();
        valueMap.putAll(expandedValue);
    }

    /**
     * Expands system properties in format ${propertyName} in the collection values
     */
    default void expandEnvVars(Collection<String> valueList) {
        ArrayList<String> expandedValue = new ArrayList<>();
        valueList.forEach((value) -> expandedValue.add(expandPropertiesIntoText(value)));
        valueList.clear();
        valueList.addAll(expandedValue);
    }
}
