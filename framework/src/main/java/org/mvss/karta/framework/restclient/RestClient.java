package org.mvss.karta.framework.restclient;

import java.util.HashMap;

public interface RestClient extends AutoCloseable
{
   HashMap<String, String> getCookies();

   void setCookies( HashMap<String, String> cookies );

   String getBaseUrl();

   void setBaseUrl( String baseUrl );

   RestResponse get( RestRequest request ) throws Exception;

   RestResponse get( RestRequest request, String path ) throws Exception;

   RestResponse post( RestRequest request ) throws Exception;

   RestResponse post( RestRequest request, String path ) throws Exception;

   RestResponse put( RestRequest request ) throws Exception;

   RestResponse put( RestRequest request, String path ) throws Exception;

   RestResponse patch( RestRequest request ) throws Exception;

   RestResponse patch( RestRequest request, String path ) throws Exception;

   RestResponse delete( RestRequest request ) throws Exception;

   RestResponse delete( RestRequest request, String path ) throws Exception;
}
