package org.mvss.karta.framework.restclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response implements Serializable
{
   private static final long       serialVersionUID = 1L;

   private ProtocolVersion         protocolVersion;
   private int                     statusCode;
   private String                  reasonPhrase;
   private Serializable            body;

   @Builder.Default
   private HashMap<String, String> headers          = new HashMap<String, String>();

   public Response( CloseableHttpResponse response ) throws UnsupportedOperationException, IOException
   {
      StatusLine statusLine = response.getStatusLine();
      this.protocolVersion = statusLine.getProtocolVersion();
      this.statusCode = statusLine.getStatusCode();
      this.reasonPhrase = statusLine.getReasonPhrase();

      this.body = ParserUtils.getObjectMapper().readValue( response.getEntity().getContent(), Serializable.class );

      this.headers = new HashMap<String, String>();
      Header[] headers = response.getAllHeaders();

      if ( headers != null )
      {
         for ( Header header : headers )
         {
            this.headers.put( header.getName(), header.getValue() );
         }
      }
   }

   @SuppressWarnings( "unchecked" )
   public <T> T getBodyAs( Class<T> type )
   {
      if ( body.getClass() == type )
      {
         return (T) body;
      }
      else
      {
         return ParserUtils.getObjectMapper().convertValue( body, type );
      }
   }
}
