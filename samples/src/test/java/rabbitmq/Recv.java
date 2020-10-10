package rabbitmq;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.framework.runtime.impl.DefaultTestEventListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Recv
{

   private final static String QUEUE_NAME  = "KartaEvents";

   private static String       userName    = "guest";
   private static String       password    = "guest";
   private static String       virtualHost = "/";
   private static String       hostName    = "localhost";
   private static int          portNumber  = 5672;

   public static void main( String[] argv ) throws Throwable
   {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setUsername( userName );
      factory.setPassword( password );
      factory.setVirtualHost( virtualHost );
      factory.setHost( hostName );
      factory.setPort( portNumber );
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      channel.queueDeclare( QUEUE_NAME, false, false, false, null );
      System.out.println( " [*] Waiting for messages. To exit press CTRL+C" );

      DefaultTestEventListener dtev = new DefaultTestEventListener();

      dtev.initialize( new HashMap<String, HashMap<String, Serializable>>() );

      DeliverCallback deliverCallback = ( consumerTag, delivery ) -> {
         dtev.processEvent( SerializationUtils.deserialize( delivery.getBody() ) );
      };

      channel.basicConsume( QUEUE_NAME, true, deliverCallback, consumerTag -> {
      } );

      dtev.close();
   }
}