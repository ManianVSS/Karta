package org.mvss.karta.framework.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.enums.ContextType;
import org.mvss.karta.framework.utils.AnnotatedFieldConsumer;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.DataUtils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

//TODO: Bean and property injection into bean with dependency tree
//TODO: Reload bean definitions if changed in annotated locations

/**
 * A registry for named objects which are used throughout test life cycle.
 * 
 * @author Manian
 */
@Log4j2
@Getter
public class BeanRegistry
{
   private HashMap<String, Object>                  globalBeans         = new HashMap<String, Object>();

   private HashMap<Thread, HashMap<String, Object>> threadContextBeanMap  = new HashMap<Thread, HashMap<String, Object>>();

   private HashMap<String, HashMap<String, Object>> namedContextBeanMap = new HashMap<String, HashMap<String, Object>>();

   /**
    * Constructor: Add self to the registry with class name.
    */
   public BeanRegistry()
   {
      globalBeans.put( BeanRegistry.class.getName(), this );
      initThreadContextRegistry();
   }

   /**
    * Initialize the thread specific registry for calling thread
    */
   public void initThreadContextRegistry()
   {
      initThreadContextRegistry( Thread.currentThread() );
   }

   /**
    * Initialize the thread specific registry for a thread
    * 
    * @param thread
    */
   public void initThreadContextRegistry( Thread thread )
   {
      if ( ( thread != null ) && !threadContextBeanMap.containsKey( thread ) )
      {
         HashMap<String, Object> threadBeanRegistry = new HashMap<String, Object>();
         threadContextBeanMap.put( thread, threadBeanRegistry );
         threadBeanRegistry.put( BeanRegistry.class.getName(), this );
      }
   }

   /**
    * Initialize the named context specific registry for context name
    * 
    * @param contextName
    */
   public void initNamedContextRegistry( String contextName )
   {
      if ( ( contextName != null ) && !namedContextBeanMap.containsKey( contextName ) )
      {
         HashMap<String, Object> contextBeanRegistry = new HashMap<String, Object>();
         namedContextBeanMap.put( contextName, contextBeanRegistry );
         contextBeanRegistry.put( BeanRegistry.class.getName(), this );
      }
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
      return globalBeans.put( beanName, bean );
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
      if ( globalBeans.containsKey( beanName ) )
      {
         return false;
      }
      globalBeans.put( beanName, bean );
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
      return globalBeans.get( beanName );
   }

   /**
    * Get the bean by class
    * 
    * @param <T>
    * @param beanClass
    * @return
    */
   @SuppressWarnings( "unchecked" )
   public <T> T get( Class<T> beanClass )
   {
      return (T) globalBeans.get( beanClass.getName() );
   }

   /**
    * Checks if the bean name key is mapped and return true if it does.
    * 
    * @param beanName
    * @return
    */
   public boolean containsKey( String beanName )
   {
      return globalBeans.containsKey( beanName );
   }

   /**
    * Gets the bean map based on the wiring context type.
    * 
    * @param contextType
    * @param contextName
    * @return
    */
   public HashMap<String, Object> getBeanMap( ContextType contextType, String contextName )
   {
      switch ( contextType )
      {
         case NAMED:
            return namedContextBeanMap.get( DataUtils.pickString( StringUtils::isNotEmpty, contextName, Constants.EMPTY_STRING ) );

         case THREAD:
            return threadContextBeanMap.get( Thread.currentThread() );

         default:
         case GLOBAL:
            return globalBeans;
      }
   }

   /**
    * Gets the bean map based on the wiring context type.
    * 
    * @param kartaAutoWired
    * @return
    */
   public HashMap<String, Object> getBeanMap( KartaAutoWired kartaAutoWired )
   {
      return getBeanMap( kartaAutoWired.contextType(), kartaAutoWired.contextName() );
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
      loadBeans( object, object.getClass(), this );
   }

   /**
    * Sets the value for a field of a object/class based on the registry and auto wiring type.
    * 
    * @param object
    * @param processAsType
    * @param field
    * @param kartaAutoWired
    */
   public void setFieldValue( Object object, Class<?> processAsType, Field field, KartaAutoWired kartaAutoWired )
   {
      try
      {
         HashMap<String, Object> beanMap = getBeanMap( kartaAutoWired );

         if ( beanMap != null )
         {
            field.setAccessible( true );
            Class<?> fieldClass = field.getType();
            String beanName = DataUtils.pickString( StringUtils::isNotEmpty, kartaAutoWired.value(), kartaAutoWired.name(), fieldClass.getName() );
            Object valueToSet = beanMap.get( beanName );

            if ( valueToSet != null )
            {
               field.set( object, valueToSet );
            }
         }
      }
      catch ( IllegalArgumentException | IllegalAccessException e )
      {
         log.error( "", e );
      }
   }

   /**
    * Wires the instance fields mapped in the object using KartaAutoWired annotation to values from the bean map.
    * The superclass to wire is provided by the parameter theClassOfObject
    * Bean map is provided by parameter beans
    * 
    * @param object
    * @param theClassOfObject
    * @param beanRegistry
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public static void loadBeans( Object object, Class<?> theClassOfObject, BeanRegistry beanRegistry )
            throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() )
           || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }

      AnnotationScanner.forEachField( theClassOfObject, KartaAutoWired.class, AnnotationScanner.IS_NON_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), AnnotationScanner.IS_NON_VOID_TYPE, new AnnotatedFieldConsumer()
               {
                  @Override
                  public void accept( Class<?> type, Field field, Annotation annotationObject )
                  {
                     beanRegistry.setFieldValue( object, type, field, (KartaAutoWired) annotationObject );
                  }
               } );

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( superClass.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      else
      {
         loadBeans( object, superClass, beanRegistry );
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
      loadStaticBeans( theClassOfObject, this );
   }

   /**
    * Wires the static field mapped in the object using KartaAutoWired annotation to values from the bean map.
    * 
    * @param theClassOfObject
    * @param beanRegistry
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public static void loadStaticBeans( Class<?> theClassOfObject, BeanRegistry beanRegistry ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) )
      {
         return;
      }

      AnnotationScanner.forEachField( theClassOfObject, KartaAutoWired.class, AnnotationScanner.IS_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), AnnotationScanner.IS_NON_VOID_TYPE, new AnnotatedFieldConsumer()
               {
                  @Override
                  public void accept( Class<?> type, Field field, Annotation annotationObject )
                  {
                     beanRegistry.setFieldValue( null, type, field, (KartaAutoWired) annotationObject );
                  }
               } );

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( superClass.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      else
      {
         loadStaticBeans( superClass, beanRegistry );
      }
   }
}
