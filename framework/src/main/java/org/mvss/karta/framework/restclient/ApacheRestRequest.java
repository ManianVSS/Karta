package org.mvss.karta.framework.restclient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
public class ApacheRestRequest implements RestRequest
{
   private static final long       serialVersionUID = 1L;

   private String                  url;
   private HashMap<String, String> headers          = new HashMap<String, String>();
   private HashMap<String, String> cookies          = new HashMap<String, String>();
   private HashMap<String, String> params           = new HashMap<String, String>();
   private ContentType             contentType;
   private ContentType             accept;
   private Serializable            body;

   public static RestRequestBuilder requestBuilder()
   {
      RestRequestBuilder ahRestRequestBuilder = new RestRequestBuilder()
      {
         private static final long             serialVersionUID = 1L;

         private String                        url;
         private HashMap<String, Serializable> headers          = new HashMap<String, Serializable>();
         private HashMap<String, Serializable> params           = new HashMap<String, Serializable>();
         private ContentType                   contentType;
         private ContentType                   accept;
         private Serializable                  body;
         private HashMap<String, String>       cookies          = new HashMap<String, String>();

         @Override
         public RestRequestBuilder url( String url )
         {
            this.url = url;
            return this;
         }

         @Override
         public RestRequestBuilder params( HashMap<String, Serializable> params )
         {
            this.params.putAll( params );
            return this;
         }

         @Override
         public RestRequestBuilder param( String key, Serializable value )
         {
            this.params.put( key, value );
            return this;
         }

         @Override
         public RestRequestBuilder headers( HashMap<String, Serializable> headers )
         {
            this.headers.putAll( headers );
            return this;
         }

         @Override
         public RestRequestBuilder header( String key, Serializable value )
         {
            this.headers.put( key, value );
            return this;
         }

         @Override
         public RestRequestBuilder contentType( ContentType contentType )
         {
            this.contentType = contentType;
            this.header( Constants.CONTENT_TYPE, contentType.mimeType );
            return this;
         }

         @Override
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
                     switch ( contentType )
                     {
                        case TEXT_HTML:
                        case TEXT_PLAIN:
                           this.body = body.toString();
                           break;
                        default:
                        case APPLICATION_JSON:
                           this.body = ParserUtils.getObjectMapper().writeValueAsString( body );
                           break;
                        case APPLICATION_XML:
                           this.body = ParserUtils.getXmlMapper().writeValueAsString( body );
                           break;
                        case APPLICATION_YAML:
                        case APPLICATION_YML:
                           this.body = ParserUtils.getYamlObjectMapper().writeValueAsString( body );
                           break;
                        case APPLICATION_OCTET_STREAM:
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

         @Override
         public RestRequestBuilder accept( ContentType contentType )
         {
            this.accept = contentType;
            this.header( Constants.ACCEPT, contentType.mimeType );
            return this;
         }

         @Override
         public RestRequest build()
         {
            ApacheRestRequest request = new ApacheRestRequest();

            request.url = url;
            headers.forEach( ( key, value ) -> request.headers.put( key, ParserUtils.serializableToString( value ) ) );
            params.forEach( ( key, value ) -> request.params.put( key, ParserUtils.serializableToString( value ) ) );
            request.contentType = contentType;
            request.accept = accept;
            request.body = body;
            request.cookies.putAll( cookies );

            return request;
         }

      };
      return ahRestRequestBuilder;
   }

   protected String getFullURL( String baseUrl, String path )
   {
      return DataUtils.constructURL( baseUrl, url, path );
   }

   protected RequestBuilder prepareRequest( RequestBuilder requestBuilder )
   {
      for ( Entry<String, String> header : headers.entrySet() )
      {
         requestBuilder.addHeader( header.getKey(), header.getValue() );
      }

      for ( Entry<String, String> parameter : params.entrySet() )
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
            requestBuilder.setEntity( new StringEntity( (String) body, org.apache.http.entity.ContentType.getByMimeType( contentType.mimeType ) ) );
         }
      }

      return requestBuilder;
   }
}
