package org.mvss.karta.framework.runtime.interfaces;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

public interface TestDataSource extends Plugin, AutoCloseable
{
   HashMap<String, Serializable> getData( ExecutionStepPointer executionStepPointer ) throws Throwable;
}
