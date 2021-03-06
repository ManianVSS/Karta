package org.mvss.karta.framework.utils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
public class SSLProperties implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID   = 1L;

   @Builder.Default
   private String            trustStoreType     = "jks";
   @Builder.Default
   private String            trustStore         = "trustStore.jks";
   @Builder.Default
   private String            trustStorePassword = "changeit";
   @Builder.Default
   private String            keyStoreType       = "pkcs12";
   @Builder.Default
   private String            keyStore           = "keyStore.p12";
   @Builder.Default
   private String            keyStorePassword   = "changeit";

   public synchronized void expandSystemAndEnvProperties()
   {
      trustStoreType = PropertyUtils.expandEnvVars( trustStoreType );
      trustStore = PropertyUtils.expandEnvVars( trustStore );
      trustStorePassword = PropertyUtils.expandEnvVars( trustStorePassword );
      keyStoreType = PropertyUtils.expandEnvVars( keyStoreType );
      keyStore = PropertyUtils.expandEnvVars( keyStore );
      keyStorePassword = PropertyUtils.expandEnvVars( keyStorePassword );
   }
}
