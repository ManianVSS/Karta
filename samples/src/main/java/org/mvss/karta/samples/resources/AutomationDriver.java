package org.mvss.karta.samples.resources;

public interface AutomationDriver extends AutoCloseable
{
   void click( String locator );
}
