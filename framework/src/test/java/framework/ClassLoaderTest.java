package framework;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.mvss.karta.framework.utils.DynamicClassLoader;

public class ClassLoaderTest
{
   public static void main( String[] args )
   {
      if ( args.length < 2 )
      {
         System.out.println( " <jarFile> <ClassFile>" );
         return;
      }

      try
      {
         Class<?> classToLoad = DynamicClassLoader.loadClass( new File( args[0] ), args[1] );

         System.out.println( "Loaded class: " + classToLoad );
         for ( Method method : classToLoad.getDeclaredMethods() )
         {
            System.out.println( "Methods found: " + method.getName() );
         }
      }
      catch ( ClassNotFoundException | MalformedURLException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | URISyntaxException e )
      {
         e.printStackTrace();
      }

   }
}
