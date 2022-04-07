package org.mvss.karta.framework.utils;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.core.ClassMethodConsumer;
import org.mvss.karta.framework.core.ObjectMethodConsumer;
import org.mvss.karta.framework.runtime.Constants;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class based on reflection to search for annotated classes, field and method and perform actions.
 *
 * @author Manian
 */
@Log4j2
public class AnnotationScanner
{
   public static final Predicate<Integer> IS_FINAL = Modifier::isFinal;

   public static final Predicate<Integer> IS_NON_FINAL = IS_FINAL.negate();

   public static final Predicate<Integer> IS_STATIC = Modifier::isStatic;

   public static final Predicate<Integer> IS_NON_STATIC = IS_STATIC.negate();

   public static final Predicate<Integer> IS_PUBLIC = Modifier::isPublic;

   public static final Predicate<Integer> IS_PRIVATE   = Modifier::isPrivate;
   public static final Predicate<Integer> IS_PROTECTED = Modifier::isProtected;

   public static final Predicate<Integer> IS_PUBLIC_AND_STATIC = IS_PUBLIC.and( IS_STATIC );

   public static final Predicate<Class<?>> IS_VOID_TYPE = type -> ( type == Void.class );

   public static final Predicate<Class<?>> IS_NON_VOID_TYPE = IS_VOID_TYPE.negate();

   public static final Predicate<Parameter[]> HAS_PARAMS = parameters -> parameters.length != 0;

   public static final Predicate<Parameter[]> DOES_NOT_HAVE_PARAMETERS = HAS_PARAMS.negate();

   /**
    * Scans every method in the packages with matching annotations, modifiers and return type and call the consumer action
    */
   public static void forEachMethod( Collection<String> annotationScanPackageNames, Class<? extends Annotation> annotation,
                                     Predicate<Integer> modifierChecks, Predicate<Class<?>> returnTypeCheck, Predicate<Parameter[]> paramsChecks,
                                     Consumer<Method> action )
   {
      for ( String annotationScanPackageName : annotationScanPackageNames )
      {
         try
         {
            Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( ClasspathHelper.forPackage( annotationScanPackageName ) )
                     .setScanners( Scanners.MethodsAnnotated ) );

            Set<Method> candidateMethods = reflections.getMethodsAnnotatedWith( annotation );
            for ( Method candidateMethod : candidateMethods )
            {
               if ( ( ( modifierChecks == null ) || modifierChecks.test(
                        candidateMethod.getModifiers() ) ) && ( ( returnTypeCheck == null ) || returnTypeCheck.test(
                        candidateMethod.getReturnType() ) ) && ( ( paramsChecks == null ) || paramsChecks.test( candidateMethod.getParameters() ) ) )
               {
                  action.accept( candidateMethod );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( t.getMessage() );
         }
      }
   }

   /**
    * Scans every method in the class with matching annotations, modifiers and return type and calls the consumer action
    */
   public static void forEachMethod( Class<?> classToWorkWith, Class<? extends Annotation> annotation, Predicate<Integer> modifierChecks,
                                     Predicate<Class<?>> returnTypeCheck, Predicate<Parameter[]> paramsChecks, ClassMethodConsumer action )
   {
      for ( Method candidateMethod : classToWorkWith.getMethods() )
      {
         try
         {
            if ( candidateMethod.isAnnotationPresent( annotation ) )
            {
               if ( ( ( modifierChecks == null ) || modifierChecks.test(
                        candidateMethod.getModifiers() ) ) && ( ( returnTypeCheck == null ) || returnTypeCheck.test(
                        candidateMethod.getReturnType() ) ) && ( ( paramsChecks == null ) || paramsChecks.test( candidateMethod.getParameters() ) ) )
               {
                  action.accept( classToWorkWith, candidateMethod );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( Constants.EMPTY_STRING, t );
         }
      }
   }

   /**
    * Scans every method in the object's class with matching annotations, modifiers and return type and calls the ObjectMethodConsumer action
    */
   public static void forEachMethod( Object objectToWorkWith, Class<? extends Annotation> annotation, Predicate<Integer> modifierChecks,
                                     Predicate<Class<?>> returnTypeCheck, Predicate<Parameter[]> paramsChecks, ObjectMethodConsumer action )
   {
      Class<?> classToWorkWith = objectToWorkWith.getClass();

      for ( Method candidateMethod : classToWorkWith.getMethods() )
      {
         try
         {
            if ( candidateMethod.isAnnotationPresent( annotation ) )
            {
               if ( ( ( modifierChecks == null ) || modifierChecks.test(
                        candidateMethod.getModifiers() ) ) && ( ( returnTypeCheck == null ) || returnTypeCheck.test(
                        candidateMethod.getReturnType() ) ) && ( ( paramsChecks == null ) || paramsChecks.test( candidateMethod.getParameters() ) ) )
               {
                  action.accept( objectToWorkWith, candidateMethod );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( Constants.EMPTY_STRING, t );
         }
      }
   }

   /**
    * Scans every class in the packages with matching annotations and modifiers and call the consumer action
    */
   public static void forEachClass( Collection<String> annotationScanPackageNames, Class<? extends Annotation> annotation,
                                    Predicate<Integer> modifierChecks, Consumer<Class<?>> action )
   {
      for ( String annotationScanPackageName : annotationScanPackageNames )
      {
         try
         {
            Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( ClasspathHelper.forPackage( annotationScanPackageName ) )
                     .setScanners( Scanners.TypesAnnotated, Scanners.SubTypes ) );
            Set<Class<?>> candidateClasses = reflections.getTypesAnnotatedWith( annotation );

            for ( Class<?> candidateMethod : candidateClasses )
            {
               if ( ( modifierChecks == null ) || modifierChecks.test( candidateMethod.getModifiers() ) )
               {
                  action.accept( candidateMethod );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( Constants.EMPTY_STRING, t );
         }
      }
   }

   /**
    * Scans every field in the class with matching annotations, modifiers and return type and call the consumer action
    */
   public static void forEachField( Class<?> classToWorkWith, Class<? extends Annotation> annotation, Predicate<Integer> modifierChecks,
                                    AnnotatedFieldConsumer action )
   {
      if ( ( classToWorkWith == null ) || classToWorkWith.getName().equals( Object.class.getName() ) )
      {
         return;
      }

      for ( Field fieldOfClass : classToWorkWith.getDeclaredFields() )
      {
         Annotation annotationObject = null;

         if ( annotation != null )
         {
            annotationObject = fieldOfClass.getAnnotation( annotation );
         }

         if ( ( ( annotation == null ) || ( annotationObject != null ) ) && ( ( modifierChecks == null ) || modifierChecks.test(
                  fieldOfClass.getModifiers() ) ) )
         {
            action.accept( classToWorkWith, fieldOfClass, annotationObject );
         }
      }

      Class<?> superClass = classToWorkWith.getSuperclass();
      if ( !superClass.getName().equals( Object.class.getName() ) )
      {
         forEachField( superClass, annotation, modifierChecks, action );
      }
   }
}
