package main.java;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

public class Send {

    private final static String QUEUE_NAME = "Auth_Req_Queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        JSONObject params = new JSONObject();
        params.put("username", "ranjana");
        params.put("password", "ranjana");
        params.put("email_id", "ranjana.ranjana@gmail.com");

        JSONObject request = new JSONObject();
        request.put("requestID", 123456);
        request.put("service_name", "signup");
        request.put("params", params);


//        String message = "Hello World!";
        String message = request.toString();
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}