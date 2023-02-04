package org.mvss.karta.framework.restclient;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;

@Log4j2
@Getter
@Setter
public class RARestRequest implements RestRequest {
    private static final long serialVersionUID = 1L;

    private static final Base64.Encoder encoder = Base64.getEncoder();
    protected boolean multiPartEnabled;
    protected HashMap<String, Object> multiParts = new HashMap<>();
    private String url;
    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> cookies = new HashMap<>();
    private HashMap<String, String> params = new HashMap<>();
    private ContentType contentType;
    private ContentType accept;
    private Serializable body;

    public static RestRequestBuilder requestBuilder() {
        return new RestRequestBuilder() {
            public RestRequest build() {
                RARestRequest request = new RARestRequest();

                request.url = url;
                headers.forEach((key, value) -> request.headers.put(key, ParserUtils.serializableToString(value)));
                params.forEach((key, value) -> request.params.put(key, ParserUtils.serializableToString(value)));
                request.contentType = contentType;
                request.accept = accept;
                request.body = body;
                request.cookies.putAll(cookies);
                request.multiPartEnabled = multiPartEnabled;
                request.multiParts = multiParts;

                return request;
            }
        };
    }

    public RequestSpecification prepare(RARestClient restClient) {
        if (restClient == null) {
            //noinspection resource
            restClient = new RARestClient();
        }

        RequestSpecification baseSpecification = restClient.getRequestSpecBuilder().build();
        RequestSpecification requestSpecification = (baseSpecification == null) ? RestAssured.given() : baseSpecification;
        requestSpecification.cookies(restClient.getCookies());

        requestSpecification.accept(accept.toString()).headers(headers).cookies(cookies).params(params);

        if (multiPartEnabled) {
            multiParts.forEach(requestSpecification::multiPart);
        } else {
            requestSpecification.contentType(contentType.toString()).body(body);
        }

        requestSpecification.baseUri(DataUtils.constructURL(restClient.getBaseUrl(), this.url));

        return requestSpecification;
    }

    @Override
    public void basicAuth(String userName, String password) {
        headers.put(Constants.AUTHORIZATION, Constants.BASIC + encoder.encodeToString((userName + Constants.COLON + password).getBytes()));
    }

    @Override
    public void bearerTokenAuth(String bearerToken) {
        headers.put(Constants.AUTHORIZATION, Constants.BEARER + bearerToken);
    }
}
