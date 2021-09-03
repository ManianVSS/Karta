package org.mvss.karta.framework.restclient;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.HashMap;

@Log4j2
public abstract class RestRequestBuilder implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected       String                        url;
   protected final HashMap<String, Serializable> headers = new HashMap<>();
   protected final HashMap<String, Serializable> params  = new HashMap<>();
   protected       ContentType                   contentType;
   protected       ContentType                   accept;
   protected       Serializable                  body;
   protected final HashMap<String, String>       cookies = new HashMap<>();

   public RestRequestBuilder url( String url )
   {
      this.url = url;
      return this;
   }

   public RestRequestBuilder params( HashMap<String, Serializable> params )
   {
      this.params.putAll( params );
      return this;
   }

   public RestRequestBuilder param( String key, Serializable value )
   {
      this.params.put( key, value );
      return this;
   }

   public RestRequestBuilder headers( HashMap<String, Serializable> headers )
   {
      this.headers.putAll( headers );
      return this;
   }

   public RestRequestBuilder header( String key, Serializable value )
   {
      this.headers.put( key, value );
      return this;
   }

   public RestRequestBuilder cookie( String key, String value )
   {
      this.cookies.put( key, value );
      return this;
   }

   public RestRequestBuilder cookies( HashMap<String, String> cookies )
   {
      this.cookies.putAll( cookies );
      return this;
   }

   public RestRequestBuilder contentType( ContentType contentType )
   {
      this.contentType = contentType;
      this.header( Constants.CONTENT_TYPE, contentType.mimeType );
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
            this.body = body;
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

   public RestRequestBuilder accept( ContentType contentType )
   {
      this.accept = contentType;
      this.header( Constants.ACCEPT, contentType.mimeType );
      return this;
   }

   public abstract RestRequest build();
}
