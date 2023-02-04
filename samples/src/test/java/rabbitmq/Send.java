package rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.mvss.karta.framework.models.event.GenericTestEvent;

public class Send {

    private final static String QUEUE_NAME = "KartaEvents";

    public static void main(String[] argv) throws Exception {
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

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, SerializationUtils.serialize(new GenericTestEvent("TestRun", "Hello world!!")));
        }
    }
}