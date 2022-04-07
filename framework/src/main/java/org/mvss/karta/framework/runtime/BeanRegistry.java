package org.mvss.karta.framework.runtime;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.enums.ContextType;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.DataUtils;

import java.lang.reflect.Field;
import java.util.HashMap;

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
   private final HashMap<String, Object> globalBeans = new HashMap<>();

   private final HashMap<Thread, HashMap<String, Object>> threadContextBeanMap = new HashMap<>();

   private final HashMap<String, HashMap<String, Object>> namedContextBeanMap = new HashMap<>();

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
    * @param thread Thread
    */
   public void initThreadContextRegistry( Thread thread )
   {
      if ( ( thread != null ) && !threadContextBeanMap.containsKey( thread ) )
      {
         HashMap<String, Object> threadBeanRegistry = new HashMap<>();
         threadContextBeanMap.put( thread, threadBeanRegistry );
         threadBeanRegistry.put( BeanRegistry.class.getName(), this );
      }
   }

   /**
    * Initialize the named context specific registry for context name
    *
    * @param contextName String
    */
   public void initNamedContextRegistry( String contextName )
   {
      if ( ( contextName != null ) && !namedContextBeanMap.containsKey( contextName ) )
      {
         HashMap<String, Object> contextBeanRegistry = new HashMap<>();
         namedContextBeanMap.put( contextName, contextBeanRegistry );
         contextBeanRegistry.put( BeanRegistry.class.getName(), this );
      }
   }

   /**
    * Close the context registry mapped by the name.
    *
    * @param contextName Object - Context name
    * @return HashMap<String, Object> - The objects for the mapped context
    */
   public HashMap<String, Object> closeContextRegistry( String contextName )
   {
      return namedContextBeanMap.remove( contextName );
   }

   /**
    * Add a bean to the registry returning the overridden existing bean by same name if any.
    * Mapped to the class name.
    *
    * @param bean Object
    * @return Previous Object
    */
   public Object put( Object bean )
   {
      return put( bean.getClass().getName(), bean );
   }

   /**
    * Add a bean to bean registry with bean name returning the overridden existing bean by same name if any.
    *
    * @param beanName String
    * @param bean     Object
    * @return Previous Object
    */
   public Object put( String beanName, Object bean )
   {
      return put( ContextType.GLOBAL, null, beanName, bean );
   }

   public Object put( ContextType contextType, String contextName, String beanName, Object bean )
   {
      HashMap<String, Object> beanMap = getBeanMap( contextType, contextName );
      return beanMap.put( beanName, bean );
   }

   /**
    * Add a bean to the registry if not mapped already.
    * Returns false if bean name already registered.
    * Bean name is the class name.
    *
    * @param bean Object
    * @return boolean
    */
   public boolean add( Object bean )
   {
      return add( bean.getClass().getName(), bean );
   }

   /**
    * Add a bean to the registry by name if not mapped already.
    * Returns false if bean name already registered.
    *
    * @param beanName String
    * @param bean     Object
    * @return boolean
    */
   public boolean add( String beanName, Object bean )
   {
      return add( ContextType.GLOBAL, null, beanName, bean );
   }

   public boolean add( ContextType contextType, String contextName, String beanName, Object bean )
   {
      HashMap<String, Object> beanMap = getBeanMap( contextType, contextName );

      if ( beanMap.containsKey( beanName ) )
      {
         return false;
      }
      beanMap.put( beanName, bean );
      return true;
   }

   /**
    * Get the bean in the bean registry by name.
    * Returns null for non existent bean names.
    *
    * @param beanName String
    * @return Object
    */
   public Object get( String beanName )
   {
      return globalBeans.get( beanName );
   }

   /**
    * Get the bean by class
    *
    * @param beanClass Class<T>
    * @return <T>
    */
   @SuppressWarnings( "unchecked" )
   public <T> T get( Class<T> beanClass )
   {
      return (T) globalBeans.get( beanClass.getName() );
   }

   /**
    * Checks if the bean name key is mapped and return true if it does.
    *
    * @param beanName String
    * @return boolean
    */
   public boolean containsKey( String beanName )
   {
      return globalBeans.containsKey( beanName );
   }

   /**
    * Gets the bean map based on the wiring context type.
    *
    * @param contextType ContextType
    * @param contextName String
    * @return HashMap<String, Object>
    */
   private HashMap<String, Object> getBeanMap( ContextType contextType, String contextName )
   {
      switch ( contextType )
      {
         case NAMED:
            initNamedContextRegistry( contextName );
            return namedContextBeanMap.get( DataUtils.pickNonNull( contextName, Constants.EMPTY_STRING ) );

         case THREAD:
            initThreadContextRegistry();
            return threadContextBeanMap.get( Thread.currentThread() );

         default:
         case GLOBAL:
            return globalBeans;
      }
   }

   /**
    * Gets the bean map based on the wiring context type.
    *
    * @param kartaAutoWired KartaAutoWired
    * @return HashMap<String, Object>
    */
   public HashMap<String, Object> getBeanMap( KartaAutoWired kartaAutoWired )
   {
      return getBeanMap( kartaAutoWired.contextType(), kartaAutoWired.contextName() );
   }

   /**
    * Wires the object's mapped in the object using KartaAutoWired annotation to values from the bean registry.
    *
    * @param object Object
    * @throws IllegalArgumentException IllegalArgumentException
    * @see KartaAutoWired
    */
   public void loadBeans( Object object ) throws IllegalArgumentException
   {
      loadBeans( object, object.getClass() );
   }

   /**
    * Sets the value for a field of a object/class based on the registry and auto wiring type.
    *
    * @param object         Object
    * @param field          Field
    * @param kartaAutoWired KartaAutoWired
    */
   public void setFieldValue( Object object, Field field, KartaAutoWired kartaAutoWired )
   {
      try
      {
         HashMap<String, Object> beanMap = getBeanMap( kartaAutoWired );

         if ( beanMap != null )
         {
            field.setAccessible( true );
            Class<?> fieldClass = field.getType();
            String   beanName   = DataUtils.pickString( StringUtils::isNotEmpty, kartaAutoWired.name(), kartaAutoWired.value(),
                     fieldClass.getName() );
            Object   valueToSet = beanMap.get( beanName );

            if ( valueToSet != null )
            {
               field.set( object, valueToSet );
            }
         }
      }
      catch ( IllegalArgumentException | IllegalAccessException e )
      {
         log.error( Constants.EMPTY_STRING, e );
      }
   }

   /**
    * Wires the instance fields mapped in the object using KartaAutoWired annotation to values from the bean map.
    * The superclass to wire is provided by the parameter theClassOfObject
    * Bean map is provided by parameter beans
    *
    * @param object           Object
    * @param theClassOfObject Class<?>
    * @throws IllegalArgumentException IllegalArgumentException
    */
   public void loadBeans( Object object, Class<?> theClassOfObject ) throws IllegalArgumentException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName()
               .equals( Object.class.getName() ) || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }

      AnnotationScanner.forEachField( theClassOfObject, KartaAutoWired.class, AnnotationScanner.IS_NON_STATIC.and( AnnotationScanner.IS_NON_FINAL ),
               ( type, field, annotationObject ) -> setFieldValue( object, field, (KartaAutoWired) annotationObject ) );

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( !superClass.getName().equals( Object.class.getName() ) )
      {
         loadBeans( object, superClass );
      }
   }

   /**
    * Wires the static field mapped in the object using KartaAutoWired annotation to values from the bean registry.
    *
    * @param theClassOfObject Class<?>
    * @throws IllegalArgumentException IllegalArgumentException
    */
   public void loadStaticBeans( Class<?> theClassOfObject ) throws IllegalArgumentException
   {
      if ( ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) )
      {
         return;
      }

      AnnotationScanner.forEachField( theClassOfObject, KartaAutoWired.class, AnnotationScanner.IS_STATIC.and( AnnotationScanner.IS_NON_FINAL ),
               ( type, field, annotationObject ) -> setFieldValue( null, field, (KartaAutoWired) annotationObject ) );

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( !superClass.getName().equals( Object.class.getName() ) )
      {
         loadStaticBeans( superClass );
      }
   }
}
