package org.mvss.karta.framework.restclient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;

@Log4j2
@Getter
@Setter
public class ApacheRestRequest implements RestRequest
{
   private static final long serialVersionUID = 1L;

   private static final Base64.Encoder encoder = Base64.getEncoder();

   private String                  url;
   private HashMap<String, String> headers = new HashMap<>();
   private HashMap<String, String> cookies = new HashMap<>();
   private HashMap<String, String> params  = new HashMap<>();
   private ContentType             contentType;
   private ContentType             accept;
   private Serializable            body;

   protected boolean                 multiPartEnabled;
   protected HashMap<String, Object> multiParts = new HashMap<>();

   public static RestRequestBuilder requestBuilder()
   {
      return new RestRequestBuilder()
      {
         public RestRequest build()
         {
            ApacheRestRequest request = new ApacheRestRequest();

            request.url = url;
            headers.forEach( ( key, value ) -> request.headers.put( key, ParserUtils.serializableToString( value ) ) );
            params.forEach( ( key, value ) -> request.params.put( key, ParserUtils.serializableToString( value ) ) );
            request.contentType = contentType;
            request.accept      = accept;
            request.body        = body;
            request.cookies.putAll( cookies );
            request.multiPartEnabled = multiPartEnabled;
            request.multiParts       = multiParts;

            return request;
         }
      };
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

      if ( multiPartEnabled )
      {
         MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
         entityBuilder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE );

         multiParts.forEach( ( name, value ) -> {
            if ( value instanceof String )
            {
               entityBuilder.addTextBody( name, (String) value );
            }
            else if ( value instanceof File )
            {
               entityBuilder.addBinaryBody( name, (File) value );
            }
            else if ( value instanceof InputStream )
            {
               entityBuilder.addBinaryBody( name, (InputStream) value );
            }
            else if ( value.getClass().isArray() && ( value.getClass().getComponentType() == byte.class ) )
            {
               entityBuilder.addBinaryBody( name, (byte[]) value );
            }
            else if ( value instanceof Serializable )
            {
               entityBuilder.addBinaryBody( name, SerializationUtils.serialize( (Serializable) value ) );
            }
         } );
         HttpEntity multiPartHttpEntity = entityBuilder.build();
         requestBuilder.setEntity( multiPartHttpEntity );
      }
      else if ( body != null )
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

   @Override
   public void basicAuth( String userName, String password )
   {
      headers.put( Constants.AUTHORIZATION, Constants.BASIC + encoder.encodeToString( ( userName + Constants.COLON + password ).getBytes() ) );
   }

   @Override
   public void bearerTokenAuth( String bearerToken )
   {
      headers.put( Constants.AUTHORIZATION, Constants.BEARER + bearerToken );
   }
}
