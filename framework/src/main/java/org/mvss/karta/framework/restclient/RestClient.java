package org.mvss.karta.framework.restclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RestClient implements AutoCloseable
{
   private CloseableHttpClient httpClient;

   public RestClient()
   {
      httpClient = HttpClients.createDefault();
   }

   public RestClient( boolean relaxedHTTPSValidation )
   {
      if ( relaxedHTTPSValidation )
      {
         httpClient = ApacheHTTPClientUtils.getInsecureHTTPSClient();
      }
      else
      {
         httpClient = HttpClients.createDefault();
      }
   }

   public RestClient( SSLConnectionSocketFactory sslConnectionSocketFactory )
   {
      httpClient = HttpClients.custom().setSSLSocketFactory( sslConnectionSocketFactory ).build();
   }

   @Override
   public void close() throws Exception
   {
      if ( httpClient != null )
      {
         httpClient.close();
         httpClient = null;
      }
   }

   public CloseableHttpResponse execute( HttpUriRequest httpUriRequest ) throws ClientProtocolException, IOException
   {
      if ( httpClient == null )
      {
         return null;
      }
      return httpClient.execute( httpUriRequest );
   }

   public Response get( RestRequest restRequest ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.get();
      return new Response( execute( httpUriRequest ) );
   }

   public Response post( RestRequest restRequest ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.post();
      return new Response( execute( httpUriRequest ) );
   }

   public Response put( RestRequest restRequest ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.put();
      return new Response( execute( httpUriRequest ) );
   }

   public Response patch( RestRequest restRequest ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.patch();
      return new Response( execute( httpUriRequest ) );
   }

   public Response delete( RestRequest restRequest ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.delete();
      return new Response( execute( httpUriRequest ) );
   }

   public Response get( RestRequest restRequest, String path ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.get( path );
      return new Response( execute( httpUriRequest ) );
   }

   public Response post( RestRequest restRequest, String path ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.post( path );
      return new Response( execute( httpUriRequest ) );
   }

   public Response put( RestRequest restRequest, String path ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.put( path );
      return new Response( execute( httpUriRequest ) );
   }

   public Response patch( RestRequest restRequest, String path ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.patch( path );
      return new Response( execute( httpUriRequest ) );
   }

   public Response delete( RestRequest restRequest, String path ) throws ClientProtocolException, IOException
   {
      HttpUriRequest httpUriRequest = restRequest.delete( path );
      return new Response( execute( httpUriRequest ) );
   }

}
