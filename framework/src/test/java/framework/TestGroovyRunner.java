package framework;


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;

import java.io.File;

class TestGroovyRunner {
    static void runWithGroovyShell(String[] args) throws Exception {
//        System.out.println(new GroovyShell().parse(new File("test.groovy")).run());
        System.out.println(new GroovyShell().run(new File("test.groovy"), args));
    }

    static void runWithGroovyClassLoader(String[] args) throws Exception {
        // Declaring a class to conform to a java interface class would get rid of
        // a lot of the reflection here
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
            Class<?> scriptClass = groovyClassLoader.parseClass(new File("test.groovy"));
            Object scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
            System.out.println(scriptClass.getDeclaredMethod("hello_world", new Class[]{String[].class}).invoke(scriptInstance, (Object) args));
        }
    }

    static void runWithGroovyScriptEngine(String[] args) throws Exception {
        // Declaring a class to conform to a java interface class would get rid of
        // a lot of the reflection here
        Class<?> scriptClass = new GroovyScriptEngine(".").loadScriptByName("test.groovy");
        Object scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
        System.out.println(scriptClass.getDeclaredMethod("hello_world", new Class[]{String[].class}).invoke(scriptInstance, (Object) args));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("1");
        runWithGroovyShell(args);
        System.out.println("2");
        runWithGroovyClassLoader(args);
        System.out.println("3");
        runWithGroovyScriptEngine(args);
    }
}
