package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class ClassPathLoaderUtils
{
   public static InputStream loadClassPathResourceFile( File jarFile, String fileName ) throws MalformedURLException
   {
      ClassLoader loader = URLClassLoader.newInstance( new URL[] {jarFile.toURI().toURL()}, ClassPathLoaderUtils.class.getClassLoader() );
      return loader.getResourceAsStream( fileName );
   }

   public static String readAllText( String fileName ) throws IOException, URISyntaxException
   {
      File fileToLoad = new File( fileName );

      if ( fileToLoad.exists() )
      {
         return FileUtils.readFileToString( fileToLoad, Charset.defaultCharset() );
      }

      URL url = ClassPathLoaderUtils.class.getResource( fileName );

      if ( url != null )
      {
         return new String( Files.readAllBytes( Paths.get( url.toURI() ) ) );
      }

      throw new IOException( "File or resource not found: " + fileName );
   }

}