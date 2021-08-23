package org.mvss.karta.framework.restclient;

import java.io.InputStream;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RARestResponse implements RestResponse
{
   private static final long serialVersionUID = 1L;

   private Response          response;

   @Override
   public String getProtocolVersion()
   {
      return response.getStatusLine().split( Constants.REGEX_WHITESPACE_MULTIPLE )[0];
   }

   @Override
   public int getStatusCode()
   {
      return response.getStatusCode();
   }

   @Override
   public String getReasonPhrase()
   {
      return response.getStatusLine().split( Constants.REGEX_WHITESPACE_MULTIPLE )[2];
   }

   @Override
   public HashMap<String, String> getHeaders()
   {
      HashMap<String, String> headers = new HashMap<>();
      for ( Header header : response.getHeaders() )
      {
         headers.put( header.getName(), header.getValue() );
      }
      return headers;
   }

   @Override
   public ContentType getContentType()
   {
      return ContentType.valueOf( response.getContentType() );
   }

   @Override
   public String getBody()
   {
      return response.getBody().asString();
   }

   @Override
   public <T> T getBodyAs( Class<T> type )
   {
      return response.as( type );
   }

   @SuppressWarnings( "unchecked" )
   @Override
   public <T> T getBodyAs( TypeReference<T> type )
   {
      return (T) response.as( TypeFactory.rawClass( type.getType() ) );
   }

   @Override
   public InputStream getStream()
   {
      return response.asInputStream();
   }

   @Override
   public void close() throws Exception
   {
      response.asInputStream().close();
   }
}
