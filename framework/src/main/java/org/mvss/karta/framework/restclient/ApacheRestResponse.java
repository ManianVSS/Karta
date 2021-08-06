package org.mvss.karta.framework.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Getter
@Log4j2
public class ApacheRestResponse implements RestResponse
{
   private static final long       serialVersionUID = 1L;

   private String                  protocolVersion;
   private int                     statusCode;
   private String                  reasonPhrase;
   private HashMap<String, String> headers;
   private ContentType             contentType;

   private InputStream             contentStream;

   public ApacheRestResponse( CloseableHttpResponse response ) throws UnsupportedOperationException, IOException
   {
      StatusLine statusLine = response.getStatusLine();
      this.protocolVersion = statusLine.getProtocolVersion().toString();
      this.statusCode = statusLine.getStatusCode();
      this.reasonPhrase = statusLine.getReasonPhrase();

      this.headers = new HashMap<String, String>();
      if ( headers != null )
      {
         for ( Header header : response.getAllHeaders() )
         {
            this.headers.put( header.getName(), header.getValue() );

            if ( header.getName().equals( Constants.CONTENT_TYPE ) )
            {
               this.contentType = ContentType.getByMimeType( header.getValue() );
            }
         }
      }

      if ( this.contentType == null )
      {
         this.contentType = ContentType.APPLICATION_OCTET_STREAM;
      }

      HttpEntity entity = response.getEntity();

      if ( entity != null )
      {
         this.contentStream = entity.getContent();
      }
   }

   @Override
   @SuppressWarnings( "unchecked" )
   public <T> T getBodyAs( Class<T> type ) throws IOException
   {
      if ( contentStream == null )
      {
         return null;
      }

      if ( type == InputStream.class )
      {
         return (T) contentStream;
      }

      byte[] body = contentStream.readAllBytes();

      if ( type == byte[].class )
      {
         return (T) body;
      }

      try
      {
         switch ( contentType )
         {
            case APPLICATION_XML:
            case APPLICATION_ATOM_XML:
            case APPLICATION_XHTML_XML:
            case APPLICATION_SOAP_XML:
            case APPLICATION_SVG_XML:
               String bytesStr = new String( body );
               return ParserUtils.convertValue( DataFormat.XML, body, type );

            case APPLICATION_YML:
            case APPLICATION_YAML:
            case APPLICATION_X_YAML:
            case TEXT_YAML:
               bytesStr = new String( body );
               return ParserUtils.readValue( DataFormat.YAML, bytesStr, type );

            case TEXT_HTML:
            case TEXT_PLAIN:
            case TEXT_XML:
            case APPLICATION_JSON:
            default:
               bytesStr = new String( body );
               return ParserUtils.readValue( DataFormat.JSON, bytesStr, type );

            case APPLICATION_FORM_URLENCODED:
            case APPLICATION_OCTET_STREAM:
            case IMAGE_BMP:
            case IMAGE_GIF:
            case IMAGE_JPEG:
            case IMAGE_PNG:
            case IMAGE_SVG:
            case IMAGE_TIFF:
            case IMAGE_WEBP:
            case MULTIPART_FORM_DATA:
               return ParserUtils.convertValue( DataFormat.JSON, body, type );

         }
      }
      catch ( Exception exception )
      {
         log.error( "", exception );
         return null;
      }
   }

   @Override
   @SuppressWarnings( "unchecked" )
   public <T> T getBodyAs( TypeReference<T> type ) throws IOException
   {
      return (T) getBodyAs( TypeFactory.rawClass( type.getType() ) );
   }

   @Override
   public void close() throws Exception
   {
      if ( contentStream != null )
      {
         contentStream.close();
         contentStream = null;
      }
   }

   @Override
   public String getBody() throws IOException
   {
      return getBodyAs( String.class );
   }

   @Override
   public InputStream getStream() throws IOException
   {
      return contentStream;
   }
}
