package org.mvss.karta.framework.restclient;

import java.util.HashMap;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RARestClient implements RestClient
{
   private RequestSpecBuilder      requestSpecBuilder;
   private String                  baseUrl;

   private HashMap<String, String> cookies = new HashMap<>();

   public RARestClient()
   {
      requestSpecBuilder = new RequestSpecBuilder();
   }

   public RARestClient( boolean relaxedHTTPSValidation )
   {
      this();
      if ( relaxedHTTPSValidation )
      {
         requestSpecBuilder.setRelaxedHTTPSValidation();
      }
   }

   public RARestClient( String baseUrl )
   {
      this();
      this.requestSpecBuilder.setBaseUri( baseUrl );
      this.baseUrl = baseUrl;
   }

   public RARestClient( String baseUrl, boolean relaxedHTTPSValidation )
   {
      this( relaxedHTTPSValidation );
      this.requestSpecBuilder.setBaseUri( baseUrl );
      this.baseUrl = baseUrl;
   }

   @Override
   public void close() throws Exception
   {
      if ( cookies != null )
      {
         cookies.clear();
      }
   }

   @Override
   public void setCookies( HashMap<String, String> cookies )
   {
      this.cookies.putAll( cookies );
   }

   @Override
   public RestResponse get( RestRequest request )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).get().then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse get( RestRequest request, String path )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).get( path ).then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse post( RestRequest request )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).post().then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse post( RestRequest request, String path )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).post( path ).then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse put( RestRequest request )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).put().then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse put( RestRequest request, String path )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).put( path ).then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse patch( RestRequest request )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).patch().then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse patch( RestRequest request, String path )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).patch( path ).then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse delete( RestRequest request )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).delete().then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }

   @Override
   public RestResponse delete( RestRequest request, String path )
   {
      RARestRequest raRestRequest = (RARestRequest) request;
      RequestSpecification requestSpecification = raRestRequest.prepare( this );
      Response response = RestAssured.given( requestSpecification ).delete( path ).then().extract().response();
      cookies.putAll( response.getCookies() );
      return new RARestResponse( response );
   }
}
