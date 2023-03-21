package org.mvss.karta.framework.utils;

import lombok.Getter;
import org.mvss.karta.dependencyinjection.utils.ClassPathLoaderUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to load classes and resources from jar files
 *
 * @author Manian
 */
public class DynamicClassLoader {
    @Getter
    private static final HashMap<String, ClassLoader> fileNameToLoaderMap = new HashMap<>();

    @Getter
    private static final HashMap<File, ClassLoader> fileToLoaderMap = new HashMap<>();

    /**
     * Returns class loader(cached) to load resources and classes from the jar file
     */
    public static synchronized ClassLoader getClassLoaderForJar(String jarFileName) throws MalformedURLException, URISyntaxException {
        ClassLoader loaderToReturn = fileNameToLoaderMap.get(jarFileName);

        if (loaderToReturn == null) {
            URI jarFileURI = ClassPathLoaderUtils.getFileOrResourceURI(jarFileName);

            if (jarFileURI == null) {
                return null;
            }

            loaderToReturn = URLClassLoader.newInstance(new URL[]{jarFileURI.toURL()}, DynamicClassLoader.class.getClassLoader());
            if (loaderToReturn != null) {
                fileNameToLoaderMap.put(jarFileName, loaderToReturn);
            }
        }

        return loaderToReturn;
    }

    /**
     * Returns class loader(cached) to load resources and classes from the jar file
     */
    public static ClassLoader getClassLoaderForJar(File jarFile) throws MalformedURLException {
        if (jarFile == null) {
            return null;
        }

        ClassLoader loaderToReturn = fileToLoaderMap.get(jarFile);

        if (loaderToReturn == null) {
            loaderToReturn = URLClassLoader.newInstance(new URL[]{jarFile.toURI().toURL()}, DynamicClassLoader.class.getClassLoader());
            if (loaderToReturn != null) {
                fileToLoaderMap.put(jarFile, loaderToReturn);
            }
        }

        return loaderToReturn;
    }

    /**
     * Load a class by name from a jar file
     */
    public static Class<?> loadClass(File jarFile, String className)
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException {
        ClassLoader loader = getClassLoaderForJar(jarFile);
        return Class.forName(className, true, loader);
    }

    /**
     * Load multiple classes by name preserving respective order
     */
    public static ArrayList<Class<?>> loadClasses(List<String> classNames) throws ClassNotFoundException, SecurityException, IllegalArgumentException {
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            classes.add(Class.forName(className));
        }
        return classes;
    }

    /**
     * Load multiple classes by name from a jar file preserving respective order
     */
    public static ArrayList<Class<?>> loadClasses(File jarFile, List<String> classNames)
            throws ClassNotFoundException, MalformedURLException, SecurityException, IllegalArgumentException {
        ClassLoader loader = getClassLoaderForJar(jarFile);
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            classes.add(Class.forName(className, true, loader));
        }
        return classes;
    }

    /**
     * Load a class form a jar file by class name
     */
    public static Class<?> loadClass(String jarFileName, String className)
            throws ClassNotFoundException, MalformedURLException, SecurityException, IllegalArgumentException, URISyntaxException {
        ClassLoader loader = getClassLoaderForJar(jarFileName);
        return Class.forName(className, true, loader);
    }

    /**
     * Get a resource file as input stream from a jar by resource name.
     */
    public static InputStream getClassPathResourceInJarAsStream(String jarFileName, String resourceName)
            throws MalformedURLException, URISyntaxException {
        ClassLoader loader = getClassLoaderForJar(jarFileName);

        if (loader == null) {
            return null;
        }

        return loader.getResourceAsStream(resourceName);
    }

    /**
     * Get a resource file as input stream from a jar by resource name.
     */
    public static InputStream getClassPathResourceInJarAsStream(File jarFile, String resourceName) throws MalformedURLException {
        ClassLoader loader = getClassLoaderForJar(jarFile);

        if (loader == null) {
            return null;
        }

        return loader.getResourceAsStream(resourceName);
    }
}