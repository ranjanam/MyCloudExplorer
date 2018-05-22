package main;

import java.io.*;
import java.net.Socket;
import org.json.JSONObject;
public class Connection {
    private final String configFileName = "./src/main/data/config.json";
    JSONObject config;
    private String readFile(String fileName) throws Exception {
        String data = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            data = sb.toString();
            return data;
        } catch (Exception e) {
            throw e;
        }
    }
    public Connection() throws Exception {
        try {
            String fileData = readFile(configFileName);
            config = new JSONObject(fileData);
        } catch (Exception e) {
            throw e;
        }
    }
    public JSONObject prepareJSON(JSONObject data, String cloudName, String service) throws Exception {
        try {
            JSONObject json = new JSONObject();
            json.put("cloud_params", data);
            json.put("cloud_name", cloudName);
            json.put("service_name", service);
            return json;
        } catch (Exception e) {
            throw e;
        }

    }
    private JSONObject sendAndReceive(JSONObject sysConfig, JSONObject uploadData) throws Exception  {
        try {

            Socket sock = new Socket(sysConfig.getString("ip"), sysConfig.getInt("port"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String send = uploadData.toString()+"\n";
            writer.write(send);
            writer.flush();
            String finalResult = reader.readLine();
            JSONObject json = new JSONObject(finalResult);
            System.out.println(finalResult);
            return json;
        } catch (Exception e) {
            throw e;
        }
    }
    public JSONObject performOperation(JSONObject data, String cloudName, String service) throws Exception {
        try {
            JSONObject sendConfig = config.getJSONObject("apiGateway");
            JSONObject sendData = prepareJSON(data, cloudName, service);
            System.out.println("sendData : "+sendData.toString());
            return sendAndReceive(sendConfig, sendData);
        } catch (Exception e) {
            throw e;
        }
    }

}
