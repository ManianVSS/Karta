package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathFileLoader
{
   public InputStream loadClassPathResourceFile( File jarFile, String fileName ) throws MalformedURLException
   {
      ClassLoader loader = URLClassLoader.newInstance( new URL[] {jarFile.toURI().toURL()}, getClass().getClassLoader() );
      return loader.getResourceAsStream( fileName );
   }
}