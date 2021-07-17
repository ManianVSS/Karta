package org.mvss.karta.framework.restclient;

import java.io.Serializable;
import java.util.HashMap;

public interface RestResponse extends Serializable
{
   String getProtocolVersion();

   int getStatusCode();

   String getReasonPhrase();

   HashMap<String, String> getHeaders();

   ContentType getContentType();

   byte[] getBody();

   <T> T getBodyAs( Class<T> type );
}
