package org.mvss.karta.framework.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;

public interface RestResponse extends Serializable, AutoCloseable
{
   String getProtocolVersion();

   int getStatusCode();

   String getReasonPhrase();

   HashMap<String, String> getHeaders();

   ContentType getContentType();

   String getBody() throws IOException;

   <T> T getBodyAs( Class<T> type ) throws IOException;

   <T> T getBodyAs( TypeReference<T> type ) throws IOException;

   InputStream getStream() throws IOException;
}
