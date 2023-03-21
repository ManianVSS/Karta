package framework;

import org.mvss.karta.dependencyinjection.utils.PropertyUtils;

public class PropertyUtilsTest {

    public static void main(String[] args) {
        PropertyUtils.systemPropertyMap.put("VALUE", "_DEF");
        PropertyUtils.systemPropertyMap.put("A_PROP_DEF", "prop value");
        System.out.println(PropertyUtils.expandEnvVars("${A_PROP${VALUE}}"));
        System.out.println(PropertyUtils.expandEnvVars("Path value inside unknown property is  ${UNKNOWN_PROP_${PATH}}"));
        System.out.println(PropertyUtils.expandEnvVars("Java home is ${JAVA_HOME} and Karta home is  ${KARTA_HOME}"));
    }

}
