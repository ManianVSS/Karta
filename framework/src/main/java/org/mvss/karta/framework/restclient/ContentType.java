package org.mvss.karta.framework.restclient;

import java.util.HashMap;

public enum ContentType
{
   TEXT_PLAIN( "text/plain" ), TEXT_HTML( "text/html" ), APPLICATION_JSON( "application/json" ), APPLICATION_XML( "application/xml" ), APPLICATION_YAML( "application/yaml" ), BINARY( "" );

   private final String                        text;

   private static HashMap<String, ContentType> stringToValueMap = new HashMap<String, ContentType>();

   static
   {
      stringToValueMap.put( "text/plain", TEXT_PLAIN );
      stringToValueMap.put( "text/html", TEXT_HTML );
      stringToValueMap.put( "application/json", APPLICATION_JSON );
      stringToValueMap.put( "application/xml", APPLICATION_XML );
      stringToValueMap.put( "application/yaml", APPLICATION_YAML );
      stringToValueMap.put( "", BINARY );
   }

   private ContentType( final String text )
   {
      this.text = text;
   }

   @Override
   public String toString()
   {
      return text;
   }

   public static ContentType tryValueOf( String strType )
   {
      if ( strType.isEmpty() )
      {
         return BINARY;
      }
      return stringToValueMap.get( strType );
   }
}
