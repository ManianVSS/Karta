package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

public interface TestDataSource extends Serializable, AutoCloseable
{
   boolean initDataSource( Serializable... args ) throws Throwable;

   HashMap<String, Serializable> getData() throws Throwable;
}
