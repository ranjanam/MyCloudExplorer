import com.rabbitmq.client.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.util.Map;


public class RequestHandler implements Runnable {


    private Socket socket;
    private Channel channel;
    Map<String, Socket> mapping;
    private static final String QUEUE_NAME="client_server";
    private static final String FROM_QUEUE_NAME = "server_client";


    public RequestHandler(Socket socket, Channel channel, Map<String, Socket> mapping) {
        this.socket = socket;
        this.channel = channel;
        this.mapping = mapping;
    }

    public void run() {
        try {
            JSONObject req = HttpRequest(socket.getInputStream());
            Instant instant = Instant.now();
            Long timeStampMillis = instant.toEpochMilli();
            String currentTime = timeStampMillis.toString();
            JSONObject params = req.getJSONObject("cloud_params");
            req.put("request_id", currentTime);
            mapping.put(currentTime, socket);
            String message = req.toString();
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));

            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private JSONObject HttpRequest(InputStream is) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String finalResult = reader.readLine();
        JSONObject request=null;
        try {
            request = new JSONObject(finalResult);
//            JSONObject file = (JSONObject) request.get("cloud_params");
//            byte[] objAsBytes = file.get("fileData").toString().getBytes("UTF-8");
//            try (FileOutputStream o = new FileOutputStream("test")) {
//                o.write(objAsBytes);
//                o.close();
//            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

}