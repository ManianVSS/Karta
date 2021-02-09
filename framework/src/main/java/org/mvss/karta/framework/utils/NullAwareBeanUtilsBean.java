package org.mvss.karta.framework.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * This class extends BeanUtilsBean for copying bean properties ignoring null values for source properties.
 * 
 * @author Manian
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean
{
   @Override
   public void copyProperty( Object dest, String name, Object value ) throws IllegalAccessException, InvocationTargetException
   {
      if ( value != null )
      {
         super.copyProperty( dest, name, value );
      }
   }

   public static <T> T getOverridenValue( T originalValue, T overriddenValue )
   {
      if ( overriddenValue == null )
      {
         return originalValue;
      }
      else
      {
         return overriddenValue;
      }
   }
}