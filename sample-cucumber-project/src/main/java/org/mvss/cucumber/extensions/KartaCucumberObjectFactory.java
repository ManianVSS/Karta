package org.mvss.cucumber.extensions;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.core.backend.ObjectFactory;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.KartaDependencyInjector;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.dependencyinjection.utils.PropertyUtils;

import java.util.ArrayList;

@Log4j2
public class KartaCucumberObjectFactory implements ObjectFactory {
    public static final String PROPERTIES_FOLDER = "properties";

    private static final TypeReference<ArrayList<String>> arrayListOfStringType = new TypeReference<>() {
    };

    private static KartaDependencyInjector kartaDependencyInjector;

    static void closeDependencyInjector() {
        if (kartaDependencyInjector != null) {
            try {
                kartaDependencyInjector.close();
            } catch (Exception e) {
                log.error("Error while closing KartaDependencyInjector: ", e);
            }
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(KartaCucumberObjectFactory::closeDependencyInjector));
    }

    @Override
    public synchronized void start() {
        if (kartaDependencyInjector == null) {
            kartaDependencyInjector = new KartaDependencyInjector();
            kartaDependencyInjector.mergePropertiesFiles(PropertyUtils.getSystemOrEnvProperty("PROPERTIES_FOLDER", PROPERTIES_FOLDER));
            ArrayList<String> scanPackages = ParserUtils.convertValue(DataFormat.YAML, kartaDependencyInjector.configurator.getPropertyValue("KartaDependencyInjector", "configurationScanPackages"), arrayListOfStringType);
            if (scanPackages != null) {
                kartaDependencyInjector.addBeansFromPackages(scanPackages);
            }
        }
    }

    @Override
    public synchronized void stop() {
        closeDependencyInjector();
    }

    @Override
    public synchronized boolean addClass(Class<?> aClass) {
        if (kartaDependencyInjector != null) {
            kartaDependencyInjector.beanRegistry.loadBeans(aClass);
        }
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> aClass) {
        try {
            kartaDependencyInjector.beanRegistry.initThreadContextRegistry();
            @SuppressWarnings("unchecked")
            T instance = (T) kartaDependencyInjector.beanRegistry.getForThread(aClass.getName());

            if (instance == null) {
                instance = aClass.getDeclaredConstructor().newInstance();
                kartaDependencyInjector.injectIntoObject(instance);
                kartaDependencyInjector.beanRegistry.putForThread(instance);
            }
            return instance;
        } catch (Throwable t) {
            log.error("Error while instantiating and initializing object for class " + aClass);
        }
        return null;
    }
}
