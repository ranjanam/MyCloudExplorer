package main.java;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import com.rabbitmq.client.*;
import jdk.nashorn.internal.runtime.ECMAException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ServerService {
    public static Map<String, String> map = new HashMap<>();


//
    public static String processRequestBody(JSONObject requested_service_params) throws Exception{
        //readJsonObject

        String req_send_queue="";
        String requested_service_name =(String) requested_service_params.get("service_name");
        String file_path="/home/bhavana/ServerSide/src/main/java/queues.json";
        JSONArray jsonarray_obj;
        InputStream is = new FileInputStream(file_path);
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        jsonarray_obj = new JSONArray(jsonTxt);
        for (int i=0;i<jsonarray_obj.length();i++)
        {
            JSONObject service= jsonarray_obj.getJSONObject(i);
            String service_name =(String) service.get("service_name");
            if(service_name.equals(requested_service_name))
            {
                req_send_queue=(String) service.get("queue_name");
            }
        }
        return req_send_queue;
    }
    public static void recvResponseFromService(String message_queue,JSONObject requested_service_params) throws Exception
    {
        String resp_recv_queue = "response_queue";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("test");
        factory.setPassword("test");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(resp_recv_queue, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body);
                System.out.println(" [x] Received '" + message + "'");
                try {
                    String reply=new String(message.toString());
                    sendReplyToClient(reply);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(resp_recv_queue, true, consumer);
    }
    public static void sendRequestToService(String message_queue,JSONObject requested_service_params) throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.1.99.70");
        factory.setUsername("test");
        factory.setPassword("test");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(message_queue, false, false, false, null);

        channel.basicPublish("", message_queue, null, requested_service_params.toString().getBytes());
        recvResponseFromService(message_queue,requested_service_params);
        channel.close();
        connection.close();
    }
    public static void sendReplyToClient(String reply) throws Exception
    {
        String resp_send_queue = "server_client";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.1.99.70");
        factory.setUsername("test");
        factory.setPassword("test");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(resp_send_queue, false, false, false, null);
//        String message = "Hello World!";
        channel.basicPublish("", resp_send_queue, null, reply.getBytes());
//        System.out.println(" [x] Sent '" +  obj.toJSONString()+ "'");
        System.out.println("Sent"+reply);

        channel.close();
        connection.close();
    }
    public static void processResponseBody(String reply,String message_queue,JSONObject requested_service_params) throws Exception{
        JSONObject response = new JSONObject(reply);
        String request_id=(String) response.getString("request_id");
        String service=map.get(request_id);
        if(service.equals("signup") || service.equals("signin"))
        {
            sendReplyToClient(reply);
        }
        else
        {
            String status=(String) response.getString("status");
            if(status.equals("200"))
            {
                sendRequestToService(message_queue,requested_service_params);
            }
        }
    }

    public static void recvResponseFromAuthService(String message_queue,JSONObject requested_service_params) throws Exception
    {
        String resp_recv_queue = "auth_response";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("test");
        factory.setPassword("test");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(resp_recv_queue, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body);
                System.out.println(" [x] Received '" + message + "'");
                try {
                    String reply=new String(message.toString());
                    processResponseBody(reply,message_queue,requested_service_params);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(resp_recv_queue, true, consumer);
    }
    public static void sendRequestToAuthService(String message_queue,JSONObject requested_service_params) throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.1.99.70");
        factory.setUsername("test");
        factory.setPassword("test");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(message_queue, false, false, false, null);

        JSONObject send_params=new JSONObject();
        String requested_service_name=(String) requested_service_params.getString("service_name");
        String request_id=(String) requested_service_params.getString("request_id");
        map.put("request_id",requested_service_name);
        if(requested_service_name.equals("signup"))
        {
            channel.basicPublish("", message_queue, null, requested_service_params.toString().getBytes());
        }
        else if(requested_service_name.equals("signin"))
        {
            channel.basicPublish("", message_queue, null, requested_service_params.toString().getBytes());
        }
        else
        {
            send_params.put("request_id",request_id);
            send_params.put("service_name","validate");
            JSONObject params=(JSONObject) requested_service_params.getJSONObject("params");
            String user_id=(String) params.getString("user_id");
            String token=(String) params.getString("token");
            JSONObject obj=new JSONObject();
            obj.put("user_id",user_id);
            obj.put("token",token);
            send_params.put("params",obj);
            channel.basicPublish("", "auth_request", null, send_params.toString().getBytes());
        }
        recvResponseFromAuthService(message_queue,requested_service_params);
        channel.close();
        connection.close();
    }


    public static void recvRequestFromClient() throws Exception
    {
        String clientReq_recv_queue = "client_server";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setPort(5672);
        factory.setUsername("test");
        factory.setPassword("test");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(clientReq_recv_queue, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body);
                System.out.println(" [x] Received '" + message + "'");
                try {
                    String json=new String(message.toString());
                    JSONObject requested_service_params = new JSONObject(json);
                    String req_send_queue = processRequestBody(requested_service_params);
                    sendRequestToAuthService(req_send_queue,requested_service_params);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(clientReq_recv_queue, true, consumer);
    }

    public static void main(String[] args) throws Exception{

        recvRequestFromClient();
    }
}
