package org.mvss.karta.framework.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

/**
 * Utility class to load classes and resources from jar files
 * 
 * @author Manian
 */
public class DynamicClassLoader
{
   @Getter
   private static HashMap<String, ClassLoader> fileNameToLoaderMap = new HashMap<String, ClassLoader>();

   @Getter
   private static HashMap<File, ClassLoader>   fileToLoaderMap     = new HashMap<File, ClassLoader>();

   /**
    * Returns class loader(cached) to load resources and classes from the jar file
    * 
    * @param jarFileName
    * @return
    * @throws MalformedURLException
    * @throws URISyntaxException
    */
   public static synchronized ClassLoader getClassLoaderForJar( String jarFileName ) throws MalformedURLException, URISyntaxException
   {
      ClassLoader loaderToReturn = fileNameToLoaderMap.get( jarFileName );

      if ( loaderToReturn == null )
      {
         URI jarFileURI = ClassPathLoaderUtils.getFileOrResourceURI( jarFileName );

         if ( jarFileURI == null )
         {
            return null;
         }

         loaderToReturn = URLClassLoader.newInstance( new URL[] {jarFileURI.toURL()}, DynamicClassLoader.class.getClassLoader() );
         if ( loaderToReturn != null )
         {
            fileNameToLoaderMap.put( jarFileName, loaderToReturn );
         }
      }

      return loaderToReturn;
   }

   /**
    * Returns class loader(cached) to load resources and classes from the jar file
    * 
    * @param jarFile
    * @return
    * @throws MalformedURLException
    * @throws URISyntaxException
    */
   public static ClassLoader getClassLoaderForJar( File jarFile ) throws MalformedURLException, URISyntaxException
   {
      if ( jarFile == null )
      {
         return null;
      }

      ClassLoader loaderToReturn = fileToLoaderMap.get( jarFile );

      if ( loaderToReturn == null )
      {
         loaderToReturn = URLClassLoader.newInstance( new URL[] {jarFile.toURI().toURL()}, DynamicClassLoader.class.getClassLoader() );
         if ( loaderToReturn != null )
         {
            fileToLoaderMap.put( jarFile, loaderToReturn );
         }
      }

      return loaderToReturn;
   }

   /**
    * Load a class by name from a jar file
    * 
    * @param jarFile
    * @param className
    * @return
    * @throws ClassNotFoundException
    * @throws MalformedURLException
    * @throws NoSuchMethodException
    * @throws SecurityException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws URISyntaxException
    */
   public static Class<?> loadClass( File jarFile, String className )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFile );
      Class<?> clazz = Class.forName( className, true, loader );
      return clazz;
   }

   /**
    * Load multiple classes by name preserving respective order
    * 
    * @param classNames
    * @return
    * @throws ClassNotFoundException
    * @throws MalformedURLException
    * @throws NoSuchMethodException
    * @throws SecurityException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws URISyntaxException
    */
   public static ArrayList<Class<?>> loadClasses( List<String> classNames )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
      for ( String className : classNames )
      {
         classes.add( Class.forName( className ) );
      }
      return classes;
   }

   /**
    * Load multiple classes by name from a jar file preserving respective order
    * 
    * @param jarFile
    * @param classNames
    * @return
    * @throws ClassNotFoundException
    * @throws MalformedURLException
    * @throws NoSuchMethodException
    * @throws SecurityException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws URISyntaxException
    */
   public static ArrayList<Class<?>> loadClasses( File jarFile, List<String> classNames )
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

   /**
    * Load a class form a jar file by class name
    * 
    * @param jarFileName
    * @param className
    * @return
    * @throws ClassNotFoundException
    * @throws MalformedURLException
    * @throws NoSuchMethodException
    * @throws SecurityException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    * @throws URISyntaxException
    */
   public static Class<?> loadClass( String jarFileName, String className )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFileName );
      Class<?> clazz = Class.forName( className, true, loader );
      return clazz;
   }

   /**
    * Get a resource file as input stream from a jar by resource name.
    * 
    * @param jarFileName
    * @param resourceName
    * @return
    * @throws MalformedURLException
    * @throws URISyntaxException
    */
   public static InputStream getClassPathResourceInJarAsStream( String jarFileName, String resourceName ) throws MalformedURLException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFileName );

      if ( loader == null )
      {
         return null;
      }

      return loader.getResourceAsStream( resourceName );
   }

   /**
    * Get a resource file as input stream from a jar by resource name.
    * 
    * @param jarFile
    * @param resourceName
    * @return
    * @throws MalformedURLException
    * @throws URISyntaxException
    */
   public static InputStream getClassPathResourceInJarAsStream( File jarFile, String resourceName ) throws MalformedURLException, URISyntaxException
   {
      ClassLoader loader = getClassLoaderForJar( jarFile );

      if ( loader == null )
      {
         return null;
      }

      return loader.getResourceAsStream( resourceName );
   }
}