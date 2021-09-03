package org.mvss.karta.framework.restclient;

import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

@Log4j2
@Getter
@Setter
public class ApacheRestRequest implements RestRequest
{
   private static final long serialVersionUID = 1L;

   private String                  url;
   private HashMap<String, String> headers = new HashMap<>();
   private HashMap<String, String> cookies = new HashMap<>();
   private HashMap<String, String> params  = new HashMap<>();
   private ContentType             contentType;
   private ContentType             accept;
   private Serializable            body;

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
