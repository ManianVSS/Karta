package rabbitmq;

import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.framework.runtime.event.GenericTestEvent;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send
{

   private final static String QUEUE_NAME  = "KartaEvents";

   private static String       userName    = "guest";
   private static String       password    = "guest";
   private static String       virtualHost = "/";
   private static String       hostName    = "localhost";
   private static int          portNumber  = 5672;

   public static void main( String[] argv ) throws Exception
   {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setUsername( userName );
      factory.setPassword( password );
      factory.setVirtualHost( virtualHost );
      factory.setHost( hostName );
      factory.setPort( portNumber );

      try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel())
      {
         channel.queueDeclare( QUEUE_NAME, false, false, false, null );
         channel.basicPublish( "", QUEUE_NAME, null, SerializationUtils.serialize( new GenericTestEvent( "TestRun", "Hello world!!" ) ) );
      }
   }
}