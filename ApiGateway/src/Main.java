
import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

//src : https://github.com/dasanjos/java-WebServer
//include logging
public class Main {
    private static final int DEFAULT_PORT = 8080;
    private static final int N_THREADS = 3;
    private static final String TO_QUEUE_NAME = "client_server";
    private static final String FROM_QUEUE_NAME = "server_client";

    public static void main(String args[]) {
        Map<String, Socket> mapping = new HashMap<String, Socket>();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createClientListener(DEFAULT_PORT, mapping);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    createServerListener(mapping);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        try {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void createClientListener(int port, Map<String, Socket> mapping) throws Exception {
        ServerSocket s = new ServerSocket(port);
        System.out.println("Web server listening on port " + port + " (press CTRL-C to quit)");
        Connection connection = null;
        Channel channel = null;
//        FileWriter writer = new FileWriter("./src/data/ipMapping.txt");

        try {
            ConnectionFactory factory = new ConnectionFactory();

//            factory.setHost("10.1.99.83"); //get from config
            factory.setHost("localhost");
            factory.setUsername("test");
            factory.setPassword("test");
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(TO_QUEUE_NAME, false, false, false, null);
            ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
            while (true) {
                executor.submit(new RequestHandler(s.accept(), channel, mapping));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                channel.close();

                connection.close();
            } catch (Exception e) {
                //handle
                throw  e;
            }
        }
    }
    public static void createServerListener(Map<String, Socket> mapping) {
        try {
//            FileReader reader= new FileReader("./src/data/ipMapping.txt");

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("test");
            factory.setPassword("test");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(FROM_QUEUE_NAME, false, false, false, null);
            Consumer consumer = new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    try {

                        JSONObject readData = new JSONObject( new String(body));
                        System.out.println(readData.toString());
                        String line = readData.getString("request_id");
                        Socket socket = mapping.get(line);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        readData.remove("request_id");
                        writer.write(readData.toString()+"\n");
                        writer.flush();
                        mapping.remove(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            channel.basicConsume(FROM_QUEUE_NAME, true, consumer);

        } catch (Exception e) {

        }
    }

}


