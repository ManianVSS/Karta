package org.mvss.karta.framework.web;

import org.openqa.selenium.support.FindBy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface FindByWrapper {
    FindBy[] shadowRoots();

    FindBy value();
}
