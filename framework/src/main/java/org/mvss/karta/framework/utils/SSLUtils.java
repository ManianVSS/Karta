package org.mvss.karta.framework.utils;

import lombok.Setter;

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

   public static void setSSLProperties()
   {
      setSSLProperties( defaultSSLProperties );
   }

   public static void setSSLProperties( SSLProperties sslProperties )
   {
      System.setProperty( JAVAX_NET_SSL_TRUSTSTORETYPE, sslProperties.getTrustStoreType() );
      System.setProperty( JAVAX_NET_SSL_TRUSTSTORE, sslProperties.getTrustStore() );
      System.setProperty( JAVAX_NET_SSL_TRUSTSTOREPASSWORD, sslProperties.getTrustStorePassword() );

      System.setProperty( JAVAX_NET_SSL_KEYSTORETYPE, sslProperties.getKeyStoreType() );
      System.setProperty( JAVAX_NET_SSL_KEYSTORE, sslProperties.getKeyStore() );
      System.setProperty( JAVAX_NET_SSL_KEYSTOREPASSWORD, sslProperties.getKeyStorePassword() );
   }

   public static void clearSSLProperties()
   {
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTORETYPE );
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTORE );
      System.clearProperty( JAVAX_NET_SSL_TRUSTSTOREPASSWORD );

      System.clearProperty( JAVAX_NET_SSL_KEYSTORETYPE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTORE );
      System.clearProperty( JAVAX_NET_SSL_KEYSTOREPASSWORD );
   }
}
