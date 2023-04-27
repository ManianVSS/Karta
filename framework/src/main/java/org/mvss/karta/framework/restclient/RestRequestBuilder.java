package org.mvss.karta.framework.restclient;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;

@Log4j2
public abstract class RestRequestBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Base64.Encoder encoder = Base64.getEncoder();
    protected final HashMap<String, Serializable> headers = new HashMap<>();
    protected final HashMap<String, Serializable> params = new HashMap<>();
    protected final HashMap<String, String> cookies = new HashMap<>();
    protected String url;
    protected ContentType contentType;
    protected ContentType accept;
    protected Serializable body;
    protected boolean multiPartEnabled;
    protected HashMap<String, Object> multiParts = new HashMap<>();

    public RestRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public RestRequestBuilder params(HashMap<String, Serializable> params) {
        this.params.putAll(params);
        return this;
    }

    public RestRequestBuilder param(String key, Serializable value) {
        this.params.put(key, value);
        return this;
    }

    public RestRequestBuilder headers(HashMap<String, Serializable> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public RestRequestBuilder header(String key, Serializable value) {
        this.headers.put(key, value);
        return this;
    }

    public RestRequestBuilder cookie(String key, String value) {
        this.cookies.put(key, value);
        return this;
    }

    public RestRequestBuilder cookies(HashMap<String, String> cookies) {
        this.cookies.putAll(cookies);
        return this;
    }

    public RestRequestBuilder contentType(ContentType contentType) {
        this.contentType = contentType;
        this.header(Constants.CONTENT_TYPE, contentType.mimeType);
        return this;
    }

    public RestRequestBuilder body(Serializable body) {
        try {
            if (body == null) {
                this.body = null;
                return this;
            } else if (body.getClass().equals(byte[].class)) {
                this.body = new String((byte[]) body);
            } else if (body.getClass().equals(String.class)) {
                this.body = body;
            } else {
                if (contentType != null) {
                    switch (contentType) {
                        case TEXT_HTML:
                        case TEXT_PLAIN:
                            this.body = body.toString();
                            break;
                        default:
                        case APPLICATION_JSON:
                            this.body = ParserUtils.getObjectMapper().writeValueAsString(body);
                            break;
                        case APPLICATION_XML:
                            this.body = ParserUtils.getXmlMapper().writeValueAsString(body);
                            break;
                        case APPLICATION_YAML:
                        case APPLICATION_YML:
                            this.body = ParserUtils.getYamlObjectMapper().writeValueAsString(body);
                            break;
                        case APPLICATION_OCTET_STREAM:
                            this.body = SerializationUtils.serialize(body);
                            break;
                    }

                }

            }
        } catch (Throwable t) {
            log.error("", t);
        }
        return this;
    }

    public RestRequestBuilder accept(ContentType contentType) {
        this.accept = contentType;
        this.header(Constants.ACCEPT, contentType.mimeType);
        return this;
    }

    public RestRequestBuilder multiPart(String name, Object multiPartObject) {
        multiPartEnabled = true;
        multiParts.put(name, multiPartObject);
        return this;
    }

    public RestRequestBuilder basicAuth(String userName, String password) {
        headers.put(Constants.AUTHORIZATION, Constants.BASIC + encoder.encodeToString((userName + Constants.COLON + password).getBytes()));
        return this;
    }

    public RestRequestBuilder tokenAuth(String tokenPrefix, String token) {
        headers.put(Constants.AUTHORIZATION, tokenPrefix + " " + token);
        return this;
    }

    public RestRequestBuilder bearerTokenAuth(String bearerToken) {
        headers.put(Constants.AUTHORIZATION, Constants.BEARER + bearerToken);
        return this;
    }

    public abstract RestRequest build();
}
