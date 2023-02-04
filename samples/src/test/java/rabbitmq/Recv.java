package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.framework.plugins.impl.LoggingTestEventListener;

public class Recv {

    private final static String QUEUE_NAME = "KartaEvents";

    public static void main(String[] argv) throws Throwable {
        ConnectionFactory factory = new ConnectionFactory();
        String userName = "guest";
        factory.setUsername(userName);
        String password = "guest";
        factory.setPassword(password);
        String virtualHost = "/";
        factory.setVirtualHost(virtualHost);
        String hostName = "localhost";
        factory.setHost(hostName);
        int portNumber = 5672;
        factory.setPort(portNumber);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        LoggingTestEventListener dtev = new LoggingTestEventListener();

        dtev.initialize();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> dtev.processEvent(SerializationUtils.deserialize(delivery.getBody()));

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

        dtev.close();
    }
}