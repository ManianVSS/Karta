package org.mvss.karta.framework.restclient;

import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.HashMap;

@Log4j2
@Getter
@Setter
public class RARestRequest implements RestRequest
{
   private static final long serialVersionUID = 1L;

   private String                  url;
   private HashMap<String, String> headers = new HashMap<String, String>();
   private HashMap<String, String> cookies = new HashMap<String, String>();
   private HashMap<String, String> params  = new HashMap<String, String>();
   private ContentType             contentType;
   private ContentType             accept;
   private Serializable            body;

   public static RestRequestBuilder requestBuilder()
   {
      return new RestRequestBuilder()
      {
         public RestRequest build()
         {
            RARestRequest request = new RARestRequest();

            request.url = url;
            headers.forEach( ( key, value ) -> request.headers.put( key, ParserUtils.serializableToString( value ) ) );
            params.forEach( ( key, value ) -> request.params.put( key, ParserUtils.serializableToString( value ) ) );
            request.contentType = contentType;
            request.accept      = accept;
            request.body        = body;
            request.cookies.putAll( cookies );

            return request;
         }
      };
   }

   public RequestSpecification prepare( RARestClient restClient )
   {
      if ( restClient != null )
      {
         restClient = new RARestClient();
      }

      RequestSpecification baseSpecification    = restClient.getRequestSpecBuilder().build();
      RequestSpecification requestSpecification = ( baseSpecification == null ) ? RestAssured.given() : baseSpecification;
      baseSpecification.cookies( restClient.getCookies() );
      requestSpecification.contentType( contentType.toString() ).accept( accept.toString() ).headers( headers ).cookies( cookies ).params( params )
               .body( body );

      requestSpecification.baseUri( DataUtils.constructURL( restClient.getBaseUrl(), this.url ) );

      return requestSpecification;
   }
}
