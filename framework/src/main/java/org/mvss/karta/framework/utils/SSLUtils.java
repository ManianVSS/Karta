package org.mvss.karta.framework.utils;

import lombok.Setter;

/**
 * Utility class to work with Java SSL properties
 * 
 * @author Manian
 */
public class SSLUtils
{
   public static final String   JAVAX_NET_SSL_TRUSTSTORETYPE     = "javax.net.ssl.trustStoreType";
   public static final String   JAVAX_NET_SSL_TRUSTSTORE         = "javax.net.ssl.trustStore";
   public static final String   JAVAX_NET_SSL_TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
   public static final String   JAVAX_NET_SSL_KEYSTORETYPE       = "javax.net.ssl.keyStoreType";
   public static final String   JAVAX_NET_SSL_KEYSTORE           = "javax.net.ssl.keyStore";
   public static final String   JAVAX_NET_SSL_KEYSTOREPASSWORD   = "javax.net.ssl.keyStorePassword";

   @Setter
   private static SSLProperties defaultSSLProperties             = new SSLProperties();

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
    * @param sslProperties
    */
   public static void setSslProperties( SSLProperties sslProperties )
   {
      System.setProperty( JAVAX_NET_SSL_TRUSTSTORETYPE, sslProperties.getTrustStoreType() );
      System.setProperty( JAVAX_NET_SSL_TRUSTSTORE, sslProperties.getTrustStore() );
      System.setProperty( JAVAX_NET_SSL_TRUSTSTOREPASSWORD, sslProperties.getTrustStorePassword() );

      System.setProperty( JAVAX_NET_SSL_KEYSTORETYPE, sslProperties.getKeyStoreType() );
      System.setProperty( JAVAX_NET_SSL_KEYSTORE, sslProperties.getKeyStore() );
      System.setProperty( JAVAX_NET_SSL_KEYSTOREPASSWORD, sslProperties.getKeyStorePassword() );
   }

   /**
    * Clears the Java SSL properties
    */
   public static void clearSslProperties()
   {
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTORETYPE );
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTORE );
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTOREPASSWORD );

      System.clearProperty( JAVAX_NET_SSL_KEYSTORETYPE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTORE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTOREPASSWORD );
   }
}
