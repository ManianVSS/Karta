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

   /**
    * Add a bean to the registry returning the overridden existing bean by same name if any.
    * Mapped to the class name.
    * 
    * @param bean
    * @return
    */
   public Object put( Object bean )
   {
      return put( bean.getClass().getName(), bean );
   }

   /**
    * Add a bean to bean registry with bean name returning the overridden existing bean by same name if any.
    * 
    * @param beanName
    * @param bean
    * @return
    */
   public Object put( String beanName, Object bean )
   {
      return beans.put( beanName, bean );
   }

   /**
    * Add a bean to the registry if not mapped already.
    * Returns false if bean name already registered.
    * Bean name is the class name.
    * 
    * @param bean
    * @return
    */
   public boolean add( Object bean )
   {
      return add( bean.getClass().getName(), bean );
   }

   /**
    * Add a bean to the registry by name if not mapped already.
    * Returns false if bean name already registered.
    * 
    * @param beanName
    * @param bean
    * @return
    */
   public boolean add( String beanName, Object bean )
   {
      if ( beans.containsKey( beanName ) )
      {
         return false;
      }
      beans.put( beanName, bean );
      return true;
   }

   /**
    * Get the bean in the bean registry by name.
    * Returns null for non existent bean names.
    * 
    * @param beanName
    * @return
    */
   public Object get( String beanName )
   {
      return beans.get( beanName );
   }

   /**
    * Checks if the bean name key is mapped and return true if it does.
    * 
    * @param beanName
    * @return
    */
   public boolean containsKey( String beanName )
   {
      return beans.containsKey( beanName );
   }

   /**
    * Wires the object's mapped in the object using KartaAutoWired annotation to values from the bean registry.
    * 
    * @see KartaAutoWired
    * @param object
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public void loadBeans( Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      loadBeans( object, object.getClass(), beans );
   }

   /**
    * Wires the instance fields mapped in the object using KartaAutoWired annotation to values from the bean map.
    * The superclass to wire is provided by the parameter theClassOfObject
    * Bean map is provided by parameter beans
    * 
    * @param object
    * @param theClassOfObject
    * @param beans
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
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

   /**
    * Wires the static field mapped in the object using KartaAutoWired annotation to values from the bean registry.
    * 
    * @param theClassOfObject
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public void loadStaticBeans( Class<?> theClassOfObject ) throws IllegalArgumentException, IllegalAccessException
   {
      loadStaticBeans( theClassOfObject, beans );
   }

   /**
    * Wires the static field mapped in the object using KartaAutoWired annotation to values from the bean map.
    * 
    * @param theClassOfObject
    * @param beans
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
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
