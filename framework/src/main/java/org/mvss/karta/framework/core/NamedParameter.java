package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.PARAMETER )
@Retention( RetentionPolicy.RUNTIME )
public @interface NamedParameter
{
   public String value();
}
