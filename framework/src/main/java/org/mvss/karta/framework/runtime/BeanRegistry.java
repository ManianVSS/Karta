package org.mvss.karta.framework.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.KartaAutoWired;

//TODO: Bean and property injection into bean with dependency tree
//TODO: Reload bean definitions if changed in annotated locations

/**
 * A registry for named objects which are used throughout test life cycle.
 * 
 * @author Manian
 */
// @Log4j2
public class BeanRegistry
{
   private HashMap<String, Object> beans = new HashMap<String, Object>();

   /**
    * Constructor: Add self to the registry with class name.
    */
   public BeanRegistry()
   {
      beans.put( BeanRegistry.class.getName(), this );
   }

   public Object put( Object bean )
   {
      return put( bean.getClass().getName(), bean );
   }

   public Object put( String beanName, Object bean )
   {
      return beans.put( beanName, bean );
   }

   public boolean add( Object bean )
   {
      return add( bean.getClass().getName(), bean );
   }

   public boolean add( String beanName, Object bean )
   {
      if ( beans.containsKey( beanName ) )
      {
         return false;
      }
      beans.put( beanName, bean );
      return true;
   }

   public Object get( String beanName )
   {
      return beans.get( beanName );
   }

   public boolean containsKey( String beanName )
   {
      return beans.containsKey( beanName );
   }

   public void loadBeans( Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      loadBeans( object, object.getClass(), beans );
   }

   public static void loadBeans( Object object, Class<?> theClassOfObject, HashMap<String, Object> beans ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }
      for ( Field field : theClassOfObject.getDeclaredFields() )
      {
         int modifiers = field.getModifiers();
         if ( !Modifier.isStatic( modifiers ) && !Modifier.isFinal( modifiers ) )
         {
            field.setAccessible( true );

            KartaAutoWired propertyMapping = field.getDeclaredAnnotation( KartaAutoWired.class );

            if ( propertyMapping != null )
            {
               Class<?> fieldClass = field.getType();
               String beanName = propertyMapping.value();

               if ( StringUtils.isEmpty( beanName ) )
               {
                  beanName = fieldClass.getName();
               }

               Object valueToSet = beans.get( beanName );

               if ( valueToSet != null )
               {
                  field.set( object, valueToSet );
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

   public void loadStaticBeans( Class<?> theClassOfObject ) throws IllegalArgumentException, IllegalAccessException
   {
      loadStaticBeans( theClassOfObject, beans );
   }

   public static void loadStaticBeans( Class<?> theClassOfObject, HashMap<String, Object> beans ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      for ( Field field : theClassOfObject.getDeclaredFields() )
      {
         int modifiers = field.getModifiers();
         if ( Modifier.isStatic( modifiers ) && !Modifier.isFinal( modifiers ) )
         {
            field.setAccessible( true );

            KartaAutoWired propertyMapping = field.getDeclaredAnnotation( KartaAutoWired.class );

            if ( propertyMapping != null )
            {
               Class<?> fieldClass = field.getType();
               String beanName = propertyMapping.value();

               if ( StringUtils.isEmpty( beanName ) )
               {
                  beanName = fieldClass.getName();
               }

               Object valueToSet = beans.get( beanName );

               if ( valueToSet != null )
               {
                  field.set( null, valueToSet );
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
         loadStaticBeans( superClass, beans );
      }
   }
}
