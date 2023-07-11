package framework;

import org.mvss.karta.dependencyinjection.utils.PropertyUtils;

public class PropertyUtilsTest {

    public static void main(String[] args) {
        PropertyUtils.systemProperties.getEnvPropertyMap().put("VALUE", "_DEF");
        PropertyUtils.systemProperties.getSystemPropertyMap().put("A_PROP_DEF", "prop value");
        System.out.println(PropertyUtils.systemProperties.expandPropertiesIntoText("${A_PROP${VALUE}}"));
        System.out.println(PropertyUtils.systemProperties.expandPropertiesIntoText("Path value inside unknown property is  ${UNKNOWN_PROP_${PATH}}"));
        System.out.println(PropertyUtils.systemProperties.expandPropertiesIntoText("Java home is ${JAVA_HOME} and Karta home is  ${KARTA_HOME}"));
    }

}
