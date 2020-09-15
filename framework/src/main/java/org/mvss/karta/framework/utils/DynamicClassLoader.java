package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DynamicClassLoader
{
   public static ClassLoader getClassLoaderForJar( String jarFileName ) throws MalformedURLException, URISyntaxException
   {
      return URLClassLoader.newInstance( new URL[] {ClassPathLoaderUtils.getFileOrResourceURI( jarFileName ).toURL()}, DynamicClassLoader.class.getClassLoader() );
   }

   public static ClassLoader getClassLoaderForJar( File jarFile ) throws MalformedURLException, URISyntaxException
   {
      return URLClassLoader.newInstance( new URL[] {jarFile.toURI().toURL()}, DynamicClassLoader.class.getClassLoader() );
   }

   public static Class<?> LoadClass( File jarFile, String className )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFile );
      Class<?> clazz = Class.forName( className, true, loader );
      return clazz;
   }

   public static ArrayList<Class<?>> LoadClasses( List<String> classNames )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
      for ( String className : classNames )
      {
         classes.add( Class.forName( className ) );
      }
      return classes;
   }

   public static ArrayList<Class<?>> LoadClasses( File jarFile, List<String> classNames )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFile );
      ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
      for ( String className : classNames )
      {
         classes.add( Class.forName( className, true, loader ) );
      }
      return classes;
   }

   public static Class<?> LoadClass( String jarFileName, String className )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFileName );
      Class<?> clazz = Class.forName( className, true, loader );
      return clazz;
   }

   public static InputStream getClassPathResourceInJarAsStream( String jarFileName, String resourceName ) throws MalformedURLException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFileName );
      return loader.getResourceAsStream( resourceName );
   }

   public static InputStream getClassPathResourceInJarAsStream( File jarFile, String resourceName ) throws MalformedURLException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFile );
      return loader.getResourceAsStream( resourceName );
   }
}