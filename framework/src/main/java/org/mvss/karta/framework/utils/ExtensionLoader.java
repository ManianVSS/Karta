package org.mvss.karta.framework.utils;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionLoader<C>
{
   @SuppressWarnings( "unchecked" )
   public Class<? extends C> LoadClass( String jarFileName, String className )
            throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, URISyntaxException
   {
      ClassLoader loader = URLClassLoader.newInstance( new URL[] {ClassPathLoaderUtils.getFileOrResourceURI( jarFileName ).toURL()}, getClass().getClassLoader() );
      Class<? extends C> clazz = (Class<? extends C>) Class.forName( className, true, loader );
      return clazz;
      // Class<? extends C> newClass = clazz.asSubclass( parentClass );
   }
}