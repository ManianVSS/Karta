package org.mvss.karta.dependencyinjection.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public class SystemProperties implements Serializable, PropertiesInterface {
    private HashMap<String, String> envPropertyMap = new HashMap<>();
    private HashMap<String, String> systemPropertyMap = new HashMap<>();
    private HashMap<String, String> mergedPropertyMap = new HashMap<>();

    public SystemProperties() {
        envPropertyMap.putAll(System.getenv());
        System.getProperties().forEach((key, value) -> systemPropertyMap.put(key.toString(), value.toString()));

        mergedPropertyMap.putAll(envPropertyMap);
        mergedPropertyMap.putAll(systemPropertyMap);
    }

    public void mergeEnvValuesIntoMap(HashMap<String, String> propertyMap) {
        if ((propertyMap != envPropertyMap) && (propertyMap != systemPropertyMap) && (propertyMap != mergedPropertyMap)) {
            propertyMap.putAll(mergedPropertyMap);
        }
    }

    @Override
    public boolean containsKey(String key) {
        return mergedPropertyMap.containsKey(key);
    }

    @Override
    public String get(String key) {
        return mergedPropertyMap.get(key);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return mergedPropertyMap.getOrDefault(key, defaultValue);
    }
}
