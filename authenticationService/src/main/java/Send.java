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

        JSONObject Signupparams = new JSONObject();

        Signupparams.put("username", "ranjana");
        Signupparams.put("password", "ranjana");
        Signupparams.put("email_id", "ranjana.ranjana@gmail.com");

        JSONObject Signuprequest = new JSONObject();
        Signuprequest.put("request_id", 123456);
        Signuprequest.put("service_name", "signup");
        Signuprequest.put("params", Signupparams);

//        --------------------------------------------------------------------
        JSONObject Signinparams = new JSONObject();

        Signinparams.put("username", "ranjana");
        Signinparams.put("password", "ranjana");

        JSONObject Signinrequest = new JSONObject();
        Signinrequest.put("request_id", 123456);
        Signinrequest.put("service_name", "signin");
        Signinrequest.put("params", Signinparams);
//        --------------------------------------------------------------------
        JSONObject Validateparams = new JSONObject();

        Validateparams.put("user_id", "5ad1e8fea7b80141400f0aa9");
        Validateparams.put("token", "1523706410");

        JSONObject Validaterequest = new JSONObject();
        Validaterequest.put("request_id", 123456);
        Validaterequest.put("service_name", "validate");
        Validaterequest.put("params", Validateparams);
//        --------------------------------------------------------------------

//        String message = Signuprequest.toString();
        String message = Signinrequest.toString();
//        String message = Validaterequest.toString();

        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}