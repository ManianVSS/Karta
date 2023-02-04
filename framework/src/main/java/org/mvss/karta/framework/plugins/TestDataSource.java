package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.run.TestExecutionContext;

import java.io.Serializable;
import java.util.HashMap;

//TODO: Add locale to support localization tests 
public interface TestDataSource extends Plugin {
    HashMap<String, Serializable> getData(TestExecutionContext testExecutionContext) throws Throwable;
}
