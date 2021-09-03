package org.mvss.karta.framework.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

public class PingUtil
{
   static
   {
      X509TrustManager trustManger = new X509TrustManager()
      {
         public java.security.cert.X509Certificate[] getAcceptedIssuers()
         {
            return null;
         }

         public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType )
         {
         }

         public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType )
         {
         }
      };

      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] {trustManger};

      // Install the all-trusting trust manager
      try
      {
         SSLContext sc = SSLContext.getInstance( "SSL" );
         sc.init( null, trustAllCerts, new java.security.SecureRandom() );
         HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
      }
      catch ( Exception e )
      {
      }

   }

   public HttpClient getInsecureHttpClient() throws GeneralSecurityException
   {
      TrustStrategy trustStrategy = new TrustStrategy()
      {
         @Override
         public boolean isTrusted( X509Certificate[] chain, String authType )
         {
            return true;
         }
      };

      HostnameVerifier hostnameVerifier = new HostnameVerifier()
      {
         @Override
         public boolean verify( String hostname, SSLSession session )
         {
            return true;
         }
      };

      return HttpClients.custom().setSSLSocketFactory( new SSLConnectionSocketFactory( new SSLContextBuilder().loadTrustMaterial( trustStrategy ).build(), hostnameVerifier ) ).build();
   }

   public static boolean toggleFunction( boolean toggle, boolean value )
   {
      return toggle ? !value : value;
   }

   public static boolean isURLAvailable( String url )
   {
      return checkURLAvalability( url, false );
   }

   public static boolean isURLAvailable( String url, long consistentRetries )
   {
      for ( long i = 0; i < consistentRetries; i++ )
      {
         if ( !checkURLAvalability( url, false ) )
         {
            return false;
         }
      }
      return true;
   }

   public static boolean isURLUnavailable( String url )
   {
      return checkURLAvalability( url, true );
   }

   public static boolean checkURLAvalability( String url, boolean negativeCheck )
   {
      HttpURLConnection connection = null;

      try
      {
         // TODO: Capture response time
         connection = (HttpURLConnection) new URL( url ).openConnection();
         // connection.setRequestMethod( "HEAD" );
         connection.setReadTimeout( 10000 );
         int responseCode = connection.getResponseCode();

         return toggleFunction( negativeCheck, ( ( ( responseCode >= 200 ) && ( responseCode <= 403 ) ) ) );
      }
      catch ( IOException ioe )
      {
         return negativeCheck;
      }
      finally
      {
         if ( connection != null )
         {
            try
            {
               connection.getInputStream().close();
            }
            catch ( IOException e )
            {

            }
            connection.disconnect();
         }
      }
   }
}
