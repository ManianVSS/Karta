package org.mvss.karta.framework.restclient;

import java.io.Serializable;
import java.util.HashMap;

public interface RestRequest extends Serializable
{
   String getUrl();

   void setUrl( String url );

   HashMap<String, String> getHeaders();

   void setHeaders( HashMap<String, String> headers );

   HashMap<String, String> getParams();

   void setParams( HashMap<String, String> params );

   ContentType getAccept();

   void setAccept( ContentType accept );

   ContentType getContentType();

   void setContentType( ContentType contentType );

   Serializable getBody();

   void setBody( Serializable body );

   void basicAuth( String userName, String password ) throws Exception;

   void bearerTokenAuth( String bearerToken ) throws Exception;
}
