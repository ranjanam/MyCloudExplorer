package main.java;



import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.rabbitmq.client.*;

import java.io.IOException;

public class classAuthService {
    String consumerQueueName;
    Channel consumerChannel;

    String producerQueueName;
    Channel producerChannel;

    Connection connection;
    Consumer consumer;

    String username, password,userID,token,emailID;
    int ErrorCode;


    public void setProducerQueueName(String producerQueueName){
        this.producerQueueName = producerQueueName;
    }

    public String getProducerQueueName(){
        return this.producerQueueName;
    }


    public void setConsumerQueueName(String consumerQueueName){
        this.consumerQueueName = consumerQueueName;
    }

    public String getConsumerQueueName(){
        return this.consumerQueueName;
    }

    public void setErrorCode(int errorCode){
        this.ErrorCode = errorCode;
    }

    public int getErrorCode(){
        return this.ErrorCode;
    }
    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password =password;
    }

    public String getEmailID(){
        return this.emailID;
    }
    public void setEmailID(String emailID){
        this.emailID =emailID;
    }

    public void setUserID(String userID){
        this.userID=userID;
    }
    public String getUserID(){
        return this.userID;
    }
    public void setToken(String token){
        this.token=token;
    }
    public String getToken(){
        return this.token;
    }

    classAuthService(String CQname, String PQname) throws Exception
    {
        setConsumerQueueName(CQname);
        setProducerQueueName(PQname);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        consumerChannel = connection.createChannel();

        consumerChannel.queueDeclare(getConsumerQueueName(), true, false, false, null);

        producerChannel = connection.createChannel();
        producerChannel.queueDeclare(getProducerQueueName(), true, false, false, null);


    }
    public void initService() throws Exception{
        consumer = new DefaultConsumer(consumerChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                // thread creation
                String message = new String(body, "UTF-8");
//                System.out.println(" [x] Received '" + message + "'");
                processRequest(message);
            }
        };
        consumerChannel.basicConsume(getConsumerQueueName(), true, consumer);
    }

    public void processRequest(String message)
    {
//        String replyMessage = "";
        System.out.println(" [x] " + message);
        int retVal=0;
        JSONObject jsonResp = new JSONObject();

        try {
//            JSONObject jsonResp = new JSONObject();

            Object obj=JSONValue.parse(message);
            JSONObject jsonReq = (JSONObject) obj;
            Long requestID = (long) jsonReq.get("requestID");
            String service_name =  (String) jsonReq.get("service_name");
            JSONObject params = (JSONObject)jsonReq.get("params");

            jsonResp.put("requestID", new Long(requestID));
            setUsername((String)params.get("username"));
//        String password=(String)params.get("password");

            if (service_name.equals("signup"))
            {
                setPassword((String)params.get("password"));
                setEmailID((String)params.get("email_id"));
                newUserRegistration();
                jsonResp.put("userID",getUserID());
                jsonResp.put("token",getToken());
                jsonResp.put("message","success");

            }
            else if (service_name.equals("signin"))
            {
                setPassword((String)params.get("password"));
//                retVal=signInUser(params);            // generate token
            }
            else if (service_name.equals("validate"))
            {
                setToken((String) params.get("token"));
//                retVal=validateToken(params);
            }
            else
            {
                System.out.println("wrong service name in the request");
                setErrorCode(100);
            }
            jsonResp.put("errorcode",getErrorCode());


            sendResponse(jsonResp);

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("[ERROR] error in processing");
            jsonResp.put("errorcode",999);
            jsonResp.put("message","fail, server side error");

            sendResponse(jsonResp);
        }

    }
    public void sendResponse(JSONObject response)
    {
        try{
            String message = response.toString();
            producerChannel.basicPublish("", getProducerQueueName(), null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("[ERROR] error in sendResponse");
        }
    }
    public void newUserRegistration() throws Exception
    {
        int retVal=0;
//        String username=(String)params.get("username");
//        String password=(String)params.get("password");
        try{
            DBoperations DBobj= new DBoperations();
            DBobj.setUsername(getUsername());
            DBobj.setPassword(getPassword());
            DBobj.setEmailID(getEmailID());
            DBobj.createUser();
//            DBobj.getErrorCode();
            if(DBobj.getErrorCode() == 0)
            {
                setUserID(DBobj.getUserID());
                setToken(DBobj.getToken());
            }
            else if (DBobj.ErrorCode == 100)
            {
                System.out.println("[ERROR] Username is not unique. Operation failed.");
                retVal = 100;

            }
            else if (DBobj.ErrorCode == 111)
            {
                System.out.println("[ERROR] Operation failed because of backend error");
                retVal=111;
            }

        }catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("[ERROR] error in newUserRegistration");
            retVal =999;
            setErrorCode(retVal);
            throw new Exception("newUserRegistration failed");

        }
    }
//    public void run(){
//        System.out.println("thread is running...");
//    }

    public void exitAuthService() throws Exception
    {
        consumerChannel.close();
        producerChannel.close();
        connection.close();
    }
}