package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RabbitMQTestEventListener implements TestEventListener
{
   private static final String PLUGIN_NAME = "KartaRabbitMQTestEventListener";

   @PropertyMapping( group = PLUGIN_NAME, value = "queueName" )
   private String              queueName   = "KartaEvents";

   @PropertyMapping( group = PLUGIN_NAME, value = "userName" )
   private String              userName    = "guest";

   @PropertyMapping( group = PLUGIN_NAME, value = "password" )
   private String              password    = "guest";

   @PropertyMapping( group = PLUGIN_NAME, value = "virtualHost" )
   private String              virtualHost = "/";

   @PropertyMapping( group = PLUGIN_NAME, value = "hostName" )
   private String              hostName    = "localhost";

   @PropertyMapping( group = PLUGIN_NAME, value = "portNumber" )
   private int                 portNumber  = 5672;

   private ConnectionFactory   factory;
   private Connection          connection;
   private Channel             channel;

   private boolean             initialized = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      factory = new ConnectionFactory();
      factory.setUsername( userName );
      factory.setPassword( password );
      factory.setVirtualHost( virtualHost );
      factory.setHost( hostName );
      factory.setPort( portNumber );

      connection = factory.newConnection();
      channel = connection.createChannel();

      channel.queueDeclare( queueName, false, false, false, null );

      initialized = true;
      return true;
   }

   @Override
   public void testEvent( Event event )
   {
      try
      {
         channel.basicPublish( "", queueName, null, SerializationUtils.serialize( event ) );
      }
      catch ( Throwable t )
      {
         log.error( t );
      }

   }

   @Override
   public void close()
   {
      log.info( "Closing " + PLUGIN_NAME + " ... " );
      try
      {
         if ( channel != null )
         {
            if ( channel.isOpen() )
            {
               channel.close();
            }
            channel = null;
         }
         if ( connection != null )
         {
            connection.close();
            connection = null;
         }

         factory = null;
      }
      catch ( Throwable t )
      {
         log.error( t );
      }
   }
}
