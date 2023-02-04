package org.mvss.karta.framework.restclient;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mvss.karta.framework.configuration.ProxyOptions;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RARestClient implements RestClient {
    private RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
    private String baseUrl;

    private HashMap<String, String> cookies = new HashMap<>();

    public RARestClient(boolean relaxedHTTPSValidation) {
        this();
        if (relaxedHTTPSValidation) {
            requestSpecBuilder.setRelaxedHTTPSValidation();
        }
    }

    public RARestClient(String baseUrl) {
        this();
        this.requestSpecBuilder.setBaseUri(baseUrl);
        this.baseUrl = baseUrl;
    }

    public RARestClient(String baseUrl, boolean relaxedHTTPSValidation) {
        this(relaxedHTTPSValidation);
        this.requestSpecBuilder.setBaseUri(baseUrl);
        this.baseUrl = baseUrl;
    }

    @Override
    public void close() throws Exception {
        if (cookies != null) {
            cookies.clear();
        }
    }

    @Override
    public void setCookies(HashMap<String, String> cookies) {
        this.cookies.putAll(cookies);
    }

    @Override
    public RestResponse get(RestRequest request) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).get().then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse get(RestRequest request, String path) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).get(path).then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse post(RestRequest request) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).post().then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse post(RestRequest request, String path) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).post(path).then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse put(RestRequest request) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).put().then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse put(RestRequest request, String path) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).put(path).then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse patch(RestRequest request) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).patch().then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse patch(RestRequest request, String path) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).patch(path).then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse delete(RestRequest request) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).delete().then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    @Override
    public RestResponse delete(RestRequest request, String path) {
        RARestRequest raRestRequest = (RARestRequest) request;
        RequestSpecification requestSpecification = raRestRequest.prepare(this);
        Response response = RestAssured.given(requestSpecification).delete(path).then().extract().response();
        cookies.putAll(response.getCookies());
        return new RARestResponse(response);
    }

    public static class Builder implements Serializable {
        private String baseUrl;
        private boolean relaxedHTTPSValidation;
        private ProxyOptions proxyOptions;
        private HashMap<String, String> cookies = new HashMap<>();

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder relaxedHTTPSValidation(boolean relaxedHTTPSValidation) {
            this.relaxedHTTPSValidation = relaxedHTTPSValidation;
            return this;
        }

        public Builder proxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public Builder cookies(HashMap<String, String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public RARestClient build() {
            RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();

            if (relaxedHTTPSValidation) {
                requestSpecBuilder.setRelaxedHTTPSValidation();
            }

            if (proxyOptions != null) {
                requestSpecBuilder.setProxy(proxyOptions.getHost(), proxyOptions.getPort(), proxyOptions.getProtocol());
            }

            requestSpecBuilder.setBaseUri(baseUrl);

            return new RARestClient(requestSpecBuilder, baseUrl, cookies);
        }
    }
}
