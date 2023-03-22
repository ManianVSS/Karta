package org.mvss.karta.dependencyinjection.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public interface DependencyInjector extends AutoCloseable {

    /**
     * Merges properties groups to the dependency injector
     *
     * @param propertiesToMerge The property groups to merge
     */
    void mergeProperties(HashMap<String, HashMap<String, Serializable>> propertiesToMerge);

    /**
     * Merges property files into the dependency injector
     *
     * @param propertyFiles The property files to load from and merge
     */
    void mergePropertiesFiles(String... propertyFiles);

    /**
     * Adds a bean by name to the dependency injector
     *
     * @param name The name of the bean
     * @param bean The bean to add
     */
    void addBean(String name, Object bean);

    /**
     * Adds beans to the dependency injector
     *
     * @param beans the beans to add
     */
    void addBeans(Object... beans);


    /**
     * This interface method should load beans from packages
     *
     * @param packageNames The package names from which to load beans from
     */
    void addBeansFromPackages(Collection<String> packageNames);

    /**
     * This interface method should inject dependencies into a class statics
     *
     * @param classToInject The class into which static dependencies should be injected into.
     */
    void injectIntoClass(Class<?> classToInject);

    /**
     * This interface method should inject dependencies into an object
     *
     * @param objectToInject The object into which static dependencies should be injected into.
     */
    void injectIntoObject(Object objectToInject);

}
