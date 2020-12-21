package org.mvss.karta.framework.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.KartaBean;
import org.mvss.karta.framework.utils.AnnotationScanner;

import lombok.extern.log4j.Log4j2;

//TODO: Bean and property injection into bean with dependency tree 
//TODO: Reload bean definitions if changed in annotated locations
@Log4j2
public class BeanRegistry
{
   private HashMap<String, Object> beans                 = new HashMap<String, Object>();

   private Configurator            configurator;

   private static List<Class<?>>   configuredBeanClasses = Collections.synchronizedList( new ArrayList<Class<?>>() );

   public BeanRegistry()
   {
      beans.put( BeanRegistry.class.getName(), this );
   }

   public BeanRegistry( Configurator configurator )
   {
      this();
      this.configurator = configurator;
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

   private final Consumer<Method> processBeanDefinition = new Consumer<Method>()
   {
      @Override
      public void accept( Method candidateBeanDefinitionMethod )
      {
         try
         {
            for ( KartaBean kartaBean : candidateBeanDefinitionMethod.getAnnotationsByType( KartaBean.class ) )
            {

               Class<?> beanDeclaringClass = candidateBeanDefinitionMethod.getDeclaringClass();

               if ( !configuredBeanClasses.contains( beanDeclaringClass ) )
               {
                  try
                  {
                     if ( configurator != null )
                     {
                        configurator.loadProperties( beanDeclaringClass );
                     }
                     loadStaticBeans( beanDeclaringClass );
                  }
                  catch ( IllegalArgumentException | IllegalAccessException e )
                  {
                     log.error( "", e );
                  }
                  configuredBeanClasses.add( beanDeclaringClass );
               }

               String beanName = kartaBean.value();

               Class<?>[] paramTypes = candidateBeanDefinitionMethod.getParameterTypes();

               Object beanObj = null;

               if ( paramTypes.length == 0 )
               {
                  beanObj = candidateBeanDefinitionMethod.invoke( null );
               }
               else
               {
                  continue;
               }

               if ( StringUtils.isAllBlank( beanName ) )
               {
                  beanName = beanObj.getClass().getName();
               }

               if ( !add( beanName, beanObj ) )
               {
                  log.error( "Bean: " + beanName + " is already registered." );
               }
               else
               {
                  log.info( "Bean: " + beanName + " registered." );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing bean definition from method  " + candidateBeanDefinitionMethod.getName(), t );
         }

      }
   };

   public void addBeansFromPackages( ArrayList<String> configurationScanPackageNames )
   {
      AnnotationScanner.forEachMethod( configurationScanPackageNames, KartaBean.class, AnnotationScanner.IS_PUBLIC_AND_STATIC, AnnotationScanner.IS_NON_VOID_RETURN_TYPE, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, processBeanDefinition );
   }
}
