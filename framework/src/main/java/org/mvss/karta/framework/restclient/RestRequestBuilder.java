package org.mvss.karta.framework.restclient;

import java.io.Serializable;
import java.util.HashMap;

public interface RestRequestBuilder extends Serializable
{
   RestRequestBuilder url( String url );

   RestRequestBuilder header( String key, Serializable value );

   RestRequestBuilder headers( HashMap<String, Serializable> headers );

   RestRequestBuilder param( String key, Serializable value );

   RestRequestBuilder params( HashMap<String, Serializable> params );

   RestRequestBuilder accept( ContentType contentType );

   RestRequestBuilder contentType( ContentType contentType );

   RestRequestBuilder body( Serializable body );

   RestRequest build();
}
