package org.mvss.karta.framework.runtime;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.mvss.karta.framework.core.KartaAutoWired;

public class BeanRegistry
{
   private HashSet<Object> beans = new HashSet<Object>();

   public BeanRegistry()
   {
      beans.add( this );
   }

   public boolean add( Object bean )
   {
      return beans.add( bean );
   }

   public void loadBeans( Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      loadBeans( object, object.getClass(), beans );
   }

   public static void loadBeans( Object object, Class<?> theClassOfObject, HashSet<Object> beans ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }
      for ( Field field : theClassOfObject.getDeclaredFields() )
      {
         field.setAccessible( true );

         KartaAutoWired propertyMapping = field.getDeclaredAnnotation( KartaAutoWired.class );

         if ( propertyMapping != null )
         {
            Class<?> fieldClass = field.getType();

            for ( Object bean : beans )
            {
               if ( fieldClass == bean.getClass() )
               {
                  field.set( object, bean );
                  break;
               }
            }
         }
      }

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( superClass.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      else
      {
         loadBeans( object, superClass, beans );
      }
   }

}
