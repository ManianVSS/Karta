package org.mvss.karta.framework.restclient;

import org.mvss.karta.Constants;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;

@SuppressWarnings("unused")
public interface RestRequest extends Serializable {

    Base64.Encoder encoder = Base64.getEncoder();

    String getUrl();

    void setUrl(String url);

    HashMap<String, String> getHeaders();

    void setHeaders(HashMap<String, String> headers);

    HashMap<String, String> getParams();

    void setParams(HashMap<String, String> params);

    ContentType getAccept();

    void setAccept(ContentType accept);

    ContentType getContentType();

    void setContentType(ContentType contentType);

    Serializable getBody();

    void setBody(Serializable body);

    default void basicAuth(String userName, String password) {
        getHeaders().put(Constants.AUTHORIZATION, Constants.BASIC + encoder.encodeToString((userName + Constants.COLON + password).getBytes()));
    }

    default void tokenAuth(String tokenPrefix, String token) {
        getHeaders().put(Constants.AUTHORIZATION, tokenPrefix + " " + token);
    }

    default void bearerTokenAuth(String bearerToken) {
        getHeaders().put(Constants.AUTHORIZATION, Constants.BEARER + bearerToken);
    }
}
