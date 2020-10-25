package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class ClassPathLoaderUtils
{
   public static URI getFileOrResourceURI( String fileName ) throws URISyntaxException
   {
      File fileToLoad = new File( fileName );

      if ( fileToLoad.exists() )
      {
         return fileToLoad.toURI();
      }

      URL url = ClassPathLoaderUtils.class.getResource( "/" + fileName );

      return ( url == null ) ? null : url.toURI();
   }

   public static InputStream getFileStream( String fileName ) throws IOException, URISyntaxException
   {
      File fileToLoad = new File( fileName );

      if ( fileToLoad.exists() )
      {
         return new FileInputStream( fileToLoad );
      }

      return ClassPathLoaderUtils.class.getResourceAsStream( "/" + fileName );
   }

   public static String readAllText( String fileName ) throws IOException, URISyntaxException
   {
      InputStream fileInputStream = getFileStream( fileName );

      if ( fileInputStream == null )
      {
         // throw new IOException( "Resource " + fileName + " not found" );
         return null;
      }
      return IOUtils.toString( fileInputStream, Charset.defaultCharset() );
   }

}