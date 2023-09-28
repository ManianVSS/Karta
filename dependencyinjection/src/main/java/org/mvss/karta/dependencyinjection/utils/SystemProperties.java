package org.mvss.karta.dependencyinjection.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
public class SystemProperties implements Serializable, PropertiesInterface {
    private HashMap<String, String> envPropertyMap = new HashMap<>();
    private HashMap<String, String> systemPropertyMap = new HashMap<>();

    public SystemProperties() {
        envPropertyMap.putAll(System.getenv());
        System.getProperties().forEach((key, value) -> systemPropertyMap.put(key.toString(), value.toString()));
    }

    public void mergeEnvValuesIntoMap(HashMap<String, String> propertyMap) {
        if ((propertyMap != envPropertyMap) && (propertyMap != systemPropertyMap)) {
            propertyMap.putAll(envPropertyMap);
            propertyMap.putAll(systemPropertyMap);
        }
    }

    @Override
    public boolean containsKey(String key) {
        return systemPropertyMap.containsKey(key) || envPropertyMap.containsKey(key);
    }

    @Override
    public String get(String key) {
        return systemPropertyMap.getOrDefault(key, envPropertyMap.get(key));
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return systemPropertyMap.getOrDefault(key, envPropertyMap.getOrDefault(key, defaultValue));
    }
}
