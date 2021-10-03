package org.mvss.karta.framework.runtime;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class is used to load properties (Serializable fields) of an object or class.
 * The properties serialization format supported are YAML, JSON and XML.
 * The properties are merged into a properties store which is a map of property group
 * to map of property name to property values.
 *
 * @author Manian
 */
@Log4j2
public class Configurator
{
   public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType = new TypeReference<>()
   {
   };

   /**
    * Property store is a mapping of group name to the map of property names to Serializable property values.
    */
   @Getter
   private final HashMap<String, HashMap<String, Serializable>> propertiesStore = new HashMap<>();

   /**
    * Merges a property store into the configurator's property store.
    */
   public void mergeProperties( HashMap<String, HashMap<String, Serializable>> propertiesToMerge )
   {
      mergeProperties( propertiesStore, propertiesToMerge );
   }

   /**
    * Merge a properties store into the destination
    */
   public static void mergeProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore,
                                       HashMap<String, HashMap<String, Serializable>> propertiesToMerge )
   {
      if ( propertiesToMerge == null )
      {
         return;
      }

      for ( String propertyGroupToMerge : propertiesToMerge.keySet() )
      {
         if ( !propertiesStore.containsKey( propertyGroupToMerge ) )
         {
            propertiesStore.put( propertyGroupToMerge, new HashMap<>() );
         }

         HashMap<String, Serializable> propertiesStoreGroup      = propertiesStore.get( propertyGroupToMerge );
         HashMap<String, Serializable> propertiesToMergeForGroup = propertiesToMerge.get( propertyGroupToMerge );

         for ( String propertyToMerge : propertiesToMergeForGroup.keySet() )
         {
            propertiesStoreGroup.put( propertyToMerge, propertiesToMergeForGroup.get( propertyToMerge ) );
         }
      }
   }

   /**
    * Merge a property to a property store based on group and key name
    */
   public static void mergeProperty( HashMap<String, HashMap<String, Serializable>> propertiesStore, String propertyGroupToMerge,
                                     String propertyToMerge, String propertyValue )
   {
      if ( !propertiesStore.containsKey( propertyGroupToMerge ) )
      {
         propertiesStore.put( propertyGroupToMerge, new HashMap<>() );

         HashMap<String, Serializable> propertiesStoreGroup = propertiesStore.get( propertyGroupToMerge );
         propertiesStoreGroup.put( propertyToMerge, propertyValue );

      }
   }

   /**
    * Read property store from String based on the data format
    */
   public static HashMap<String, HashMap<String, Serializable>> readPropertiesFromString( DataFormat dataFormat, String propertiesDataString )
            throws IOException
   {
      if ( dataFormat == DataFormat.PROPERTIES )
      {
         try (StringReader stringReader = new StringReader( propertiesDataString ))
         {
            Properties properties = new Properties();
            properties.load( stringReader );

            HashMap<String, HashMap<String, Serializable>> propertiesStore = new HashMap<>();

            for ( Object propertyKeyObj : properties.keySet() )
            {
               String propertyKey   = (String) propertyKeyObj;
               String propertyGroup = Constants.KARTA;
               String propertyValue = properties.getProperty( propertyKey );

               int pivotIndex = propertyKey.indexOf( Constants.DOT );
               if ( DataUtils.inRange( pivotIndex, 0, propertyKey.length() - 2 ) )
               {
                  propertyGroup = propertyKey.substring( 0, pivotIndex );
                  propertyKey   = propertyKey.substring( pivotIndex + 1 );
               }

               mergeProperty( propertiesStore, propertyGroup, propertyKey, propertyValue );
            }
            return propertiesStore;
         }
      }

      return ParserUtils.readValue( dataFormat, propertiesDataString, propertiesType );
   }

   /**
    * Merge property store parsed from the string based on the data format
    */
   @SuppressWarnings( "BooleanMethodIsAlwaysInverted" )
   public boolean mergePropertiesString( DataFormat dataFormat, String propertiesDataString )
   {
      try
      {
         HashMap<String, HashMap<String, Serializable>> propertiesToMerge = readPropertiesFromString( dataFormat, propertiesDataString );
         mergeProperties( propertiesToMerge );
         return true;
      }
      catch ( IOException e )
      {
         log.error( "Error while parsing property file", e );
         return false;
      }
   }

   /**
    * Merge multiple property files to the data store inferencing data format from the file extension.
    */
   public boolean mergePropertiesFiles( String... propertyFiles )
   {
      if ( propertyFiles == null )
      {
         return true;
      }

      for ( String propertyFile : propertyFiles )
      {
         try
         {
            // TODO: Load from class-path folders and files for properties

            Path propertyFilePath = Paths.get( propertyFile );

            if ( !Files.exists( propertyFilePath ) )
            {
               log.warn( "Property file " + propertyFile + " does not exist" );
               continue;
            }

            if ( Files.isDirectory( propertyFilePath ) )
            {
               for ( File propFileInDirectory : FileUtils.listFiles( propertyFilePath.toFile(), Constants.propertyFileExtensions, true ) )
               {
                  String propertyFileContents = FileUtils.readFileToString( propFileInDirectory, Charset.defaultCharset() );

                  if ( propertyFileContents != null )
                  {
                     if ( !mergePropertiesString( ParserUtils.getFileDataFormat( propFileInDirectory.getName() ), propertyFileContents ) )
                     {
                        log.error( "Error while parsing properties from file " + propertyFile );
                        return false;
                     }
                  }
               }
               continue;
            }

            String propertyFileContents = ClassPathLoaderUtils.readAllText( propertyFile );

            if ( propertyFileContents != null )
            {
               if ( !mergePropertiesString( ParserUtils.getFileDataFormat( propertyFile ), propertyFileContents ) )
               {
                  log.error( "Error while parsing properties from file " + propertyFile );
                  return false;
               }
            }

         }
         catch ( IOException | URISyntaxException e )
         {
            log.error( "Error while parsing properties from file " + propertyFile, e );
            return false;
         }
      }
      return true;

   }

   /**
    * Cached map of environment properties.
    */
   public static Map<String, String> envPropMap = System.getenv();

   /**
    * Cached map of system properties.
    */
   public static Properties systemPropMap = System.getProperties();

   /**
    * Fetch property value by group name and property name.
    */
   public Serializable getPropertyValue( String group, String name )
   {
      return PropertyUtils.getPropertyValue( propertiesStore, group, name );
   }

   /**
    * Load properties into the object.
    */
   public static void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object ) throws IllegalArgumentException
   {
      AnnotationScanner.forEachField( object.getClass(), PropertyMapping.class, AnnotationScanner.IS_NON_STATIC.and( AnnotationScanner.IS_NON_FINAL ),
               ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, object, field, (PropertyMapping) annotation ) );
   }

   /**
    * Load properties into multiple objects.
    */
   public void loadProperties( Object... objects ) throws IllegalArgumentException
   {
      for ( Object object : objects )
      {
         loadProperties( propertiesStore, object );
      }
   }

   /**
    * Load properties to static fields of the class
    */
   public void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Class<?> classToLoadPropertiesWith )
            throws IllegalArgumentException
   {
      AnnotationScanner.forEachField( classToLoadPropertiesWith, PropertyMapping.class,
               AnnotationScanner.IS_STATIC.and( AnnotationScanner.IS_NON_FINAL ),
               ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, null, field, (PropertyMapping) annotation ) );
   }

   /**
    * Load properties to static fields of the multiple class
    */
   public void loadProperties( Class<?>... classesToLoadPropertiesWith ) throws IllegalArgumentException
   {
      for ( Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith )
      {
         loadProperties( propertiesStore, classToLoadPropertiesWith );
      }
   }

   public String expandPropertiesIntoText( String text )
   {
      return PropertyUtils.expandPropertiesStoreIntoText( propertiesStore, text );
   }

   public void createFileFromTemplate( String templateFileName, String fileToCreate ) throws IOException
   {
      String templateString = FileUtils.readFileToString( new File( templateFileName ), Charset.defaultCharset() );
      FileUtils.writeStringToFile( new File( fileToCreate ), expandPropertiesIntoText( templateString ), Charset.defaultCharset() );
   }
}
