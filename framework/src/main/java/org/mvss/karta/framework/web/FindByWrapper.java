package org.mvss.karta.framework.web;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.openqa.selenium.support.FindBy;

@Retention( RUNTIME )
@Target( FIELD )
public @interface FindByWrapper
{
   FindBy[] shadowRoots();

   FindBy value();
}
