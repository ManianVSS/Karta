package org.mvss.karta.framework.restclient;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.IOException;
import java.util.HashMap;

@Getter
@Setter
public class ApacheRestClient implements RestClient
{
   private CloseableHttpClient httpClient;
   private BasicCookieStore    cookieStore = new BasicCookieStore();
   private HttpClientContext   context     = HttpClientContext.create();

   private String              baseUrl;

   public ApacheRestClient()
   {
      context.setAttribute( HttpClientContext.COOKIE_STORE, cookieStore );
      httpClient = HttpClients.createDefault();
   }

   public ApacheRestClient( boolean relaxedHTTPSValidation )
   {
      context.setAttribute( HttpClientContext.COOKIE_STORE, cookieStore );
      if ( relaxedHTTPSValidation )
      {
         httpClient = ApacheHTTPClientUtils.getInsecureHTTPSClient();
      }
      else
      {
         httpClient = HttpClients.createDefault();
      }
   }

   public ApacheRestClient( SSLConnectionSocketFactory sslConnectionSocketFactory )
   {
      context.setAttribute( HttpClientContext.COOKIE_STORE, cookieStore );
      httpClient = HttpClients.custom().setSSLSocketFactory( sslConnectionSocketFactory ).build();
   }

   public ApacheRestClient( String baseUrl )
   {
      this();
      this.baseUrl = baseUrl;
   }

   public ApacheRestClient( String baseUrl, boolean relaxedHTTPSValidation )
   {
      this( relaxedHTTPSValidation );
      this.baseUrl = baseUrl;
   }

   public ApacheRestClient( String baseUrl, SSLConnectionSocketFactory sslConnectionSocketFactory )
   {
      this( sslConnectionSocketFactory );
      this.baseUrl = baseUrl;
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

   @Override
   public HashMap<String, String> getCookies()
   {
      HashMap<String, String> cookies = new HashMap<>();
      cookieStore.getCookies().forEach( cookie -> cookies.put( cookie.getName(), cookie.getValue() ) );
      return cookies;
   }

   @Override
   public void setCookies( HashMap<String, String> cookies )
   {
      cookies.forEach( ( key, value ) -> cookieStore.addCookie( new BasicClientCookie( key, value ) ) );
   }

   @FunctionalInterface
   private interface RequestBuilderMethod
   {
      RequestBuilder httpMethod( String path );
   }

   public CloseableHttpResponse execute( HttpUriRequest httpUriRequest ) throws IOException
   {
      if ( httpClient == null )
      {
         return null;
      }
      return httpClient.execute( httpUriRequest, context );
   }

   public RestResponse runMethod( RequestBuilderMethod method, ApacheRestRequest ahRequest, String suffixPath ) throws IOException
   {
      setCookies( ahRequest.getCookies() );
      RequestBuilder requestBuilder = method.httpMethod( ahRequest.getFullURL( baseUrl, suffixPath ) );
      HttpUriRequest httpUriRequest = ahRequest.prepareRequest( requestBuilder ).build();

      CloseableHttpResponse closeableHttpResponse = execute( httpUriRequest );
      return new ApacheRestResponse( closeableHttpResponse );
   }

   @Override
   public RestResponse get( RestRequest request ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::get, ahRequest, null );
   }

   @Override
   public RestResponse get( RestRequest request, String path ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::get, ahRequest, path );
   }

   @Override
   public RestResponse post( RestRequest request ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::post, ahRequest, null );
   }

   @Override
   public RestResponse post( RestRequest request, String path ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::post, ahRequest, path );
   }

   @Override
   public RestResponse put( RestRequest request ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::put, ahRequest, null );
   }

   @Override
   public RestResponse put( RestRequest request, String path ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::put, ahRequest, path );
   }

   @Override
   public RestResponse patch( RestRequest request ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::patch, ahRequest, null );
   }

   @Override
   public RestResponse patch( RestRequest request, String path ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::patch, ahRequest, path );
   }

   @Override
   public RestResponse delete( RestRequest request ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::delete, ahRequest, null );
   }

   @Override
   public RestResponse delete( RestRequest request, String path ) throws Exception
   {
      ApacheRestRequest ahRequest = (ApacheRestRequest) request;
      return runMethod( RequestBuilder::delete, ahRequest, path );
   }
}
