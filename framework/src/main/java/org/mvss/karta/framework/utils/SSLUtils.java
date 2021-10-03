package org.mvss.karta.framework.utils;

import lombok.Setter;

/**
 * Utility class to work with Java SSL properties
 *
 * @author Manian
 */
public class SSLUtils
{
   public static final String JAVAX_NET_SSL_TRUST_STORE_TYPE     = "javax.net.ssl.trustStoreType";
   public static final String JAVAX_NET_SSL_TRUST_STORE          = "javax.net.ssl.trustStore";
   public static final String JAVAX_NET_SSL_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
   public static final String JAVAX_NET_SSL_KEYSTORE_TYPE        = "javax.net.ssl.keyStoreType";
   public static final String JAVAX_NET_SSL_KEYSTORE             = "javax.net.ssl.keyStore";
   public static final String JAVAX_NET_SSL_KEYSTORE_PASSWORD    = "javax.net.ssl.keyStorePassword";

   @Setter
   private static SSLProperties defaultSSLProperties = new SSLProperties();

   /**
    * Set Java SSL properties to the default values
    */
   public static void setSslProperties()
   {
      setSslProperties( defaultSSLProperties );
   }

   /**
    * Set Java SSL properties as specified by the SSLProperties object
    *
    * @param sslProperties SSLProperties
    */
   public static void setSslProperties( SSLProperties sslProperties )
   {
      System.setProperty( JAVAX_NET_SSL_TRUST_STORE_TYPE, sslProperties.getTrustStoreType() );
      System.setProperty( JAVAX_NET_SSL_TRUST_STORE, sslProperties.getTrustStore() );
      System.setProperty( JAVAX_NET_SSL_TRUST_STORE_PASSWORD, sslProperties.getTrustStorePassword() );

      System.setProperty( JAVAX_NET_SSL_KEYSTORE_TYPE, sslProperties.getKeyStoreType() );
      System.setProperty( JAVAX_NET_SSL_KEYSTORE, sslProperties.getKeyStore() );
      System.setProperty( JAVAX_NET_SSL_KEYSTORE_PASSWORD, sslProperties.getKeyStorePassword() );
   }

   /**
    * Clears the Java SSL properties
    */
   public static void clearSslProperties()
   {
      System.clearProperty( JAVAX_NET_SSL_TRUST_STORE_TYPE );
      System.clearProperty( JAVAX_NET_SSL_TRUST_STORE );
      System.clearProperty( JAVAX_NET_SSL_TRUST_STORE_PASSWORD );

      System.clearProperty( JAVAX_NET_SSL_KEYSTORE_TYPE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTORE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTORE_PASSWORD );
   }
}
