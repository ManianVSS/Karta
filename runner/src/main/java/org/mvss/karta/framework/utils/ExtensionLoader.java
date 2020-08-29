package org.mvss.karta.framework.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionLoader<C>
{

   public Class<? extends C> LoadClass( File jarFile, String classpath, Class<C> parentClass )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      ClassLoader loader = URLClassLoader.newInstance( new URL[] {jarFile.toURI().toURL()}, getClass().getClassLoader() );
      Class<?> clazz = Class.forName( classpath, true, loader );
      Class<? extends C> newClass = clazz.asSubclass( parentClass );
      // Apparently its bad to use Class.newInstance, so we use
      // newClass.getConstructor() instead
      return newClass;
      // Constructor<? extends C> constructor = newClass.getConstructor();
      // return constructor.newInstance();
   }
}