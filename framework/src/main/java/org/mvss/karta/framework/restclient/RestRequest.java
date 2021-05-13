package org.mvss.karta.framework.restclient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RestRequest
{
   private String                  url;
   private HashMap<String, String> headers    = new HashMap<String, String>();
   private HashMap<String, String> parameters = new HashMap<String, String>();
   private ContentType             contentType;
   private Serializable            body;

   private RestRequest()
   {

   }

   public static RestRequestBuilder builder()
   {
      return new RestRequestBuilder();
   }

   public RequestBuilder prepareRequest( RequestBuilder requestBuilder )
   {
      for ( Entry<String, String> header : headers.entrySet() )
      {
         requestBuilder.addHeader( header.getKey(), header.getValue() );
      }

      for ( Entry<String, String> parameter : parameters.entrySet() )
      {
         requestBuilder.addParameter( parameter.getKey(), parameter.getValue() );
      }

      if ( body != null )
      {
         if ( contentType == ContentType.APPLICATION_OCTET_STREAM )
         {
            requestBuilder.setEntity( new SerializableEntity( body ) );
         }
         else
         {
            requestBuilder.setEntity( new StringEntity( (String) body, contentType ) );
         }
      }

      return requestBuilder;
   }

   public String getFullURL( String path )
   {
      String fullUrl = url;

      if ( StringUtils.isNotEmpty( fullUrl ) && !fullUrl.endsWith( "/" ) )
      {
         fullUrl = fullUrl + "/";
      }

      if ( StringUtils.isNotEmpty( path ) && path.startsWith( "/" ) && path.length() > 1 )
      {
         fullUrl = fullUrl + path.substring( 1 );
      }
      return fullUrl;
   }

   public HttpUriRequest get()
   {
      RequestBuilder requestBuilder = RequestBuilder.get( url );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest get( String path )
   {
      RequestBuilder requestBuilder = RequestBuilder.get( getFullURL( path ) );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest post()
   {
      RequestBuilder requestBuilder = RequestBuilder.post( url );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest post( String path )
   {
      RequestBuilder requestBuilder = RequestBuilder.post( getFullURL( path ) );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest put()
   {
      RequestBuilder requestBuilder = RequestBuilder.put( url );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest put( String path )
   {
      RequestBuilder requestBuilder = RequestBuilder.put( getFullURL( path ) );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest patch()
   {
      RequestBuilder requestBuilder = RequestBuilder.patch( url );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest patch( String path )
   {
      RequestBuilder requestBuilder = RequestBuilder.patch( getFullURL( path ) );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest delete()
   {
      RequestBuilder requestBuilder = RequestBuilder.delete( url );
      return prepareRequest( requestBuilder ).build();
   }

   public HttpUriRequest delete( String path )
   {
      RequestBuilder requestBuilder = RequestBuilder.delete( getFullURL( path ) );
      return prepareRequest( requestBuilder ).build();
   }

   public static class RestRequestBuilder
   {
      private String                        url;
      private HashMap<String, Serializable> headers     = new HashMap<String, Serializable>();
      private HashMap<String, Serializable> parameters  = new HashMap<String, Serializable>();
      private ContentType                   contentType = ContentType.APPLICATION_JSON;
      private Serializable                  body;

      private RestRequestBuilder()
      {

      }

      public RestRequestBuilder url( String url )
      {
         this.url = url;
         return this;
      }

      public RestRequestBuilder header( String key, Serializable value )
      {
         this.headers.put( key, value );
         return this;
      }

      public RestRequestBuilder headers( HashMap<String, Serializable> headers )
      {
         DataUtils.mergeMapInto( headers, this.headers );
         return this;
      }

      public RestRequestBuilder parameter( String key, Serializable value )
      {
         this.parameters.put( key, value );
         return this;
      }

      public RestRequestBuilder parameters( HashMap<String, Serializable> parameters )
      {
         DataUtils.mergeMapInto( parameters, this.parameters );
         return this;
      }

      public RestRequestBuilder contentType( ContentType contentType )
      {
         this.contentType = contentType;
         this.header( Constants.CONTENT_TYPE, contentType.toString() );
         return this;
      }

      public RestRequestBuilder body( Serializable body )
      {
         try
         {
            if ( body == null )
            {
               this.body = null;
               return this;
            }
            else if ( body.getClass().equals( byte[].class ) )
            {
               this.body = new String( (byte[]) body );
            }
            else if ( body.getClass().equals( String.class ) )
            {
               this.body = (String) body;
            }
            else
            {
               if ( contentType != null )
               {
                  switch ( contentType.toString() )
                  {
                     case "text/html":
                     case "text/plain":
                        this.body = body.toString();
                        break;

                     default:
                     case "application/json":
                        this.body = ParserUtils.getObjectMapper().writeValueAsString( body );
                        break;
                     case "application/xml":
                        this.body = ParserUtils.getXmlMapper().writeValueAsString( body );
                        break;
                     case "application/yaml":
                        this.body = ParserUtils.getYamlObjectMapper().writeValueAsString( body );
                        break;
                     case "application/octet-stream":
                        this.body = SerializationUtils.serialize( body );
                        break;
                  }

               }

            }
         }
         catch ( Throwable t )
         {
            log.error( "", t );
         }
         return this;
      }

      public RestRequest build()
      {
         RestRequest restClient = new RestRequest();

         restClient.url = this.url;

         for ( Entry<String, Serializable> header : this.headers.entrySet() )
         {
            restClient.headers.put( header.getKey(), ParserUtils.serializableToString( header.getValue() ) );
         }

         for ( Entry<String, Serializable> parameter : this.parameters.entrySet() )
         {
            restClient.parameters.put( parameter.getKey(), ParserUtils.serializableToString( parameter.getValue() ) );
         }

         restClient.body = this.body;
         restClient.contentType = this.contentType;
         return restClient;
      }
   }
}
