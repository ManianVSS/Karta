package org.mvss.karta.framework.utils;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

/**
 * This class extends BeanUtilsBean for copying bean properties ignoring null values for source properties.
 *
 * @author Manian
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {
    public static <T> T getOverriddenValue(T originalValue, T overriddenValue) {
        return (overriddenValue == null) ? originalValue : overriddenValue;
    }

    @Override
    public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value != null) {
            super.copyProperty(dest, name, value);
        }
    }
}