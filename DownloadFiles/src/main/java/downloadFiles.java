package main.java;

import com.rabbitmq.client.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JsonArray;
//import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

class downloadThreads extends Thread
{
    JSONObject required_service_params;
    public Channel sendChannel;
    public String reply_to_server_queue;
    downloadThreads(JSONObject required_service_params,Channel sendChannel,String reply_to_server_queue)
    {
        System.out.println(required_service_params);

        this.required_service_params=required_service_params;
        this.sendChannel=sendChannel;
        this.reply_to_server_queue=reply_to_server_queue;
    }
    public static void invokeJarMethod(String jar_file_path,JSONObject cloud_params,Channel sendChannel,String reply_to_server_queue) throws Exception{
//        String dirPath="/home/bhavana/ServerSide/src/"+service_name+cloud_name+".jar";
        String jarPath="file:"+jar_file_path;
        URL url = new URL(jarPath);
        URLClassLoader loader = new URLClassLoader(new URL[]{url});
        String[] bits = jar_file_path.split("/");
        StringTokenizer classtokens = new StringTokenizer(bits[bits.length - 1], ".");
        String className = classtokens.nextToken();
//        System.out.println(jar_file_path);
//        System.out.println(className);
//        String jarMethodName = "listFilesGCloud";
        Class<?> jarclass = Class.forName(className, true, loader);
//        Method jarMethod = jarclass.getDeclaredMethod(jarMethodName,JSONObject.class);
        Constructor<?> jarClassConstruct = jarclass.getDeclaredConstructor(JSONObject.class,Channel.class,String.class);
        Object whatInstance = jarClassConstruct.newInstance(cloud_params,sendChannel,reply_to_server_queue);

//        jarMethod.invoke(whatInstance, new Object[]{cloud_params});
//            loader.close();

    }

    public void run()
    {
        try
        {
            // Displaying the thread that is running
//            System.out.println ("Thread " +Thread.currentThread().getId() + " is running");
            String requested_service_name=(String) required_service_params.get("service_name");
            String requested_cloud_name=(String) required_service_params.get("cloud_name");
            String jar_file_path="";
            InputStream is = new FileInputStream("/home/bhavana/ServerSide/src/jarfilepaths.json");
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            JSONArray serviceObj = new JSONArray(jsonTxt);
            for (int i=0;i<serviceObj.length();i++)
            {
                JSONObject service = serviceObj.getJSONObject(i);
                String service_name =(String) service.get("service_name");
                if(service_name.equals(requested_service_name))
                {
                    JSONArray cloud_obj = (JSONArray) service.get("jar_files");
                    for(int j=0;j<cloud_obj.length();j++)
                    {
                        JSONObject jar_file_params = cloud_obj.getJSONObject(j);
                        String cloud_name=(String) jar_file_params.get("cloud_name");
                        if(cloud_name.equals(requested_cloud_name))
                        {
                            jar_file_path=(String) jar_file_params.get("file_path");
                        }
                    }
                }
            }
            JSONObject cloud_params=(JSONObject) required_service_params.get("cloud_params");
            invokeJarMethod(jar_file_path,cloud_params,sendChannel,reply_to_server_queue);
        }
        catch (Exception e)
        {
            // Throwing an exception
            System.out.println ("Exception is caught");
        }
    }
}
public class downloadFiles {

    private final static String recv_req_queue = "download_queue";
    public static void receiveRequestFromService(Channel sendChannel,String reply_to_server_queue) throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("test");
        factory.setPassword("test");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(recv_req_queue, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body);
//                System.out.println(" [x] Received '" + message.toString() + "'");
                String json=new String(message.toString());
                try {
                    JSONObject jsonobj = new JSONObject(json);
                    System.out.println(" [x] Received '" + jsonobj.toString() + "'");
                    downloadThreads object = new downloadThreads(jsonobj,sendChannel,reply_to_server_queue);
                    object.start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(recv_req_queue, true, consumer);
    }

    public static void main(String[] args)throws Exception{
        String reply_to_server_queue="response_queue";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("test");
        factory.setPassword("test");

        Connection connection = factory.newConnection();
        Channel sendChannel = connection.createChannel();

        sendChannel.queueDeclare(reply_to_server_queue, false, false, false, null);
        receiveRequestFromService(sendChannel,reply_to_server_queue);
    }
}

