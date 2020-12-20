package org.mvss.karta.framework.runtime.interfaces;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.TestExecutionContext;

//TODO: Add locale to support localization tests 
public interface TestDataSource extends Plugin
{
   HashMap<String, Serializable> getData( TestExecutionContext testExecutionContext ) throws Throwable;
}
