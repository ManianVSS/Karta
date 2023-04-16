package org.mvss.karta.dependencyinjection;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.KartaBean;
import org.mvss.karta.dependencyinjection.annotations.LoadConfiguration;
import org.mvss.karta.dependencyinjection.interfaces.ClassMethodConsumer;
import org.mvss.karta.dependencyinjection.interfaces.DependencyInjector;
import org.mvss.karta.dependencyinjection.interfaces.ObjectMethodConsumer;
import org.mvss.karta.dependencyinjection.utils.AnnotationScanner;
import org.mvss.karta.dependencyinjection.utils.DataUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

@Log4j2
public class KartaDependencyInjector implements DependencyInjector {

    public Configurator configurator;
    public BeanRegistry beanRegistry;
    private List<Class<?>> configuredBeanClasses;
    private final ObjectMethodConsumer callObjectInitializer = (object, method) -> {
        try {
            method.invoke(object);
        } catch (Throwable t) {
            log.error("Exception while parsing bean definition from method  " + method.getName(), t);
        }
    };
    private final ClassMethodConsumer callClassInitializer = (classToWorkWith, beanDefinitionMethod) -> {
        try {
            beanDefinitionMethod.invoke(null);
        } catch (Throwable t) {
            log.error("Exception while calling initialization method for " + classToWorkWith.getName() + Constants.DOT + beanDefinitionMethod.getName(), t);
        }
    };
    private final Consumer<Method> processBeanDefinition = beanDefinitionMethod -> {
        try {
            for (KartaBean kartaBean : beanDefinitionMethod.getAnnotationsByType(KartaBean.class)) {
                Class<?> beanDeclaringClass = beanDefinitionMethod.getDeclaringClass();
                injectIntoClass(beanDeclaringClass);
                String beanName = DataUtils.pickString(StringUtils::isNotEmpty, kartaBean.name(), kartaBean.value(), beanDefinitionMethod.getReturnType().getName());
                Class<?>[] paramTypes = beanDefinitionMethod.getParameterTypes();

                Object beanObj;

                if (paramTypes.length == 0) {
                    beanObj = beanDefinitionMethod.invoke(null);
                } else {
                    continue;
                }

                if (StringUtils.isAllBlank(beanName)) {
                    beanName = beanObj.getClass().getName();
                }

                if (!beanRegistry.add(beanName, beanObj)) {
                    log.error("Bean: " + beanName + " is already registered.");
                } else {
                    log.info("Bean: " + beanName + " registered.");
                }
            }
        } catch (Throwable t) {
            log.error("Exception while parsing bean definition from method  " + beanDefinitionMethod.getName(), t);
        }

    };

    public static KartaDependencyInjector instance;

    public synchronized static KartaDependencyInjector getInstance() {
        if (instance == null) {
            instance = new KartaDependencyInjector();
        }
        return instance;
    }

    public KartaDependencyInjector() {
        if (instance == null) {
            beanRegistry = new BeanRegistry();
            configurator = new Configurator();
            configuredBeanClasses = Collections.synchronizedList(new ArrayList<>());
            beanRegistry.add(this);
            beanRegistry.add(DependencyInjector.class.getName(), this);
            beanRegistry.add(beanRegistry);
            beanRegistry.add(configurator);
        } else {
            beanRegistry = instance.beanRegistry;
            configurator = instance.configurator;
            configuredBeanClasses = instance.configuredBeanClasses;
        }
    }

    @Override
    public void mergeProperties(HashMap<String, HashMap<String, Serializable>> propertiesToMerge) {
        configurator.mergeProperties(propertiesToMerge);
    }

    @Override
    public void mergePropertiesFiles(String... propertyFiles) {
        configurator.mergePropertiesFiles(propertyFiles);
    }

    @Override
    public void addBean(String name, Object bean) {
        beanRegistry.put(name, bean);
    }

    @Override
    public void addBeans(Object... beans) {
        for (Object bean : beans) {
            beanRegistry.put(bean);
        }
    }

    @Override
    public void addBeansFromPackages(Collection<String> packageNames) {
        AnnotationScanner.forEachMethod(packageNames, KartaBean.class, AnnotationScanner.IS_PUBLIC_AND_STATIC, AnnotationScanner.IS_NON_VOID_TYPE, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, processBeanDefinition);
        AnnotationScanner.forEachClass(packageNames, LoadConfiguration.class, AnnotationScanner.IS_PUBLIC, this::injectIntoClass);
    }

    @Override
    public synchronized void injectIntoClass(Class<?> classToInject) {
        try {
            if (!configuredBeanClasses.contains(classToInject)) {
                configurator.loadProperties(classToInject);
                beanRegistry.loadStaticBeans(classToInject);

                AnnotationScanner.forEachMethod(classToInject, Initializer.class, AnnotationScanner.IS_STATIC, null, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callClassInitializer);
                configuredBeanClasses.add(classToInject);
            }
        } catch (Throwable t) {
            log.error("Exception while initializing object", t);
        }
    }

    @Override
    public void injectIntoObject(Object objectToInject) {

        try {
            Class<?> classOfObject = objectToInject.getClass();
            injectIntoClass(classOfObject);
            // Not checking one time initialization for object level here which can prevent garbage collection
            configurator.loadProperties(objectToInject);
            beanRegistry.loadBeans(objectToInject);
            AnnotationScanner.forEachMethod(objectToInject, Initializer.class, AnnotationScanner.IS_NON_STATIC, null, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callObjectInitializer);
        } catch (Throwable t) {
            log.error("Exception while initializing object", t);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
