package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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
      return IOUtils.toString( getFileStream( fileName ), Charset.defaultCharset() );
   }

   public static InputStream loadClassPathResourceFile( String jarFileName, String fileName ) throws URISyntaxException, IOException
   {
      URI uri = getFileOrResourceURI( jarFileName );

      if ( uri == null )
      {
         throw new IOException( "Jar file/resource not found " );
      }

      ClassLoader loader = URLClassLoader.newInstance( new URL[] {uri.toURL()}, ClassPathLoaderUtils.class.getClassLoader() );
      return loader.getResourceAsStream( fileName );
   }

}