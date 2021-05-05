package org.mvss.karta.framework.restclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ApacheHTTPClientUtils
{
   @Getter
   private static TrustStrategy              trustStrategy    = new TrustStrategy()
                                                              {
                                                                 @Override
                                                                 public boolean isTrusted( X509Certificate[] x509Certificates, String s ) throws CertificateException
                                                                 {
                                                                    return true;
                                                                 }
                                                              };

   @Getter
   private static HostnameVerifier           hostnameVerifier = new HostnameVerifier()
                                                              {
                                                                 @Override
                                                                 public boolean verify( String s, SSLSession sslSession )
                                                                 {
                                                                    return true;
                                                                 }
                                                              };

   @Getter
   private static SSLConnectionSocketFactory sslConnectionSocketFactory;

   static
   {
      try
      {
         sslConnectionSocketFactory = new SSLConnectionSocketFactory( new SSLContextBuilder().loadTrustMaterial( trustStrategy ).build(), hostnameVerifier );
      }
      catch ( Exception e )
      {
         log.error( "", e );
      }
   }

   public static CloseableHttpClient getInsecureHTTPSClient()
   {
      return HttpClients.custom().setSSLSocketFactory( sslConnectionSocketFactory ).build();
   }

}
