package org.mvss.karta.framework.utils;

import lombok.*;
import org.mvss.karta.dependencyinjection.utils.PropertyUtils;

import java.io.Serializable;

/**
 * This class groups all Java SSL properties
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SSLProperties implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String trustStoreType = "jks";
    @Builder.Default
    private String trustStore = "trustStore.jks";
    @Builder.Default
    private String trustStorePassword = "changeit";
    @Builder.Default
    private String keyStoreType = "pkcs12";
    @Builder.Default
    private String keyStore = "keyStore.p12";
    @Builder.Default
    private String keyStorePassword = "changeit";

    public synchronized void expandSystemAndEnvProperties() {
        trustStoreType = PropertyUtils.systemProperties.expandPropertiesIntoText(trustStoreType);
        trustStore = PropertyUtils.systemProperties.expandPropertiesIntoText(trustStore);
        trustStorePassword = PropertyUtils.systemProperties.expandPropertiesIntoText(trustStorePassword);
        keyStoreType = PropertyUtils.systemProperties.expandPropertiesIntoText(keyStoreType);
        keyStore = PropertyUtils.systemProperties.expandPropertiesIntoText(keyStore);
        keyStorePassword = PropertyUtils.systemProperties.expandPropertiesIntoText(keyStorePassword);
    }
}
