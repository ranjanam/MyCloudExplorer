package main.user;


import org.json.JSONObject;
import main.Connection;
import java.util.Scanner;

public class User {
    public String userName;
    public String userId;
    public String token;
    public CloudUser cloudUser;
    JSONObject cloud_info;


    public User(){
        setToken("");
        setUserID("");
        cloud_info = new JSONObject();
    }

    public void setCloud_info(String key, String value) throws Exception{
        cloud_info.put(key, value);
    }

    public String getCloud_info(String key) throws Exception{
        return ((String) cloud_info.get(key));
    }

    public void setUserID(String userID){
        this.userId=userID;
    }
    public String getUserID(){
        return this.userId;
    }
    public void setToken(String token){
        this.token=token;
    }
    public String getToken(){
        return this.token;
    }
//    AmazonUser dbUser;
//    GCloudUser dbUser;
    //TODO


    public void userLogin(JSONObject params, Connection conn) throws  Exception{
        //if
        try {

            JSONObject json = conn.performOperation(params,"", "signin");
            String status = (String) json.get("status");
            if (status.equals("200")){
                JSONObject data = (JSONObject) json.get("data");
                setToken((String) data.get("token"));
                setUserID((String) data.get("userID"));
            }
        } catch (Exception e) {
            throw e;
        }
    }
    //TODO
    public void userSignUp(JSONObject params, Connection conn) throws  Exception{
        try {
            JSONObject json = conn.performOperation(params,"", "signup");
            String status = (String) json.get("status");
            if (status.equals("200")){
                JSONObject data = (JSONObject) json.get("data");
                setToken((String) data.get("token"));
                setUserID((String) data.get("userID"));
                JSONObject cloud_info = (JSONObject) data.get("cloud_info");
                setCloud_info("aws",(String) cloud_info.get("aws"));
                setCloud_info("dropbox",(String) cloud_info.get("dropbox"));
                setCloud_info("gcloud",(String) cloud_info.get("gcloud"));

            }
        } catch (Exception e) {
            throw e;
        }
    }
//    public int initCloudUser(String cloudName, Scanner scan, Connection conn) throws Status {
//        switch (cloudName) {
//            case "Amazon":  try {
//                cloudUser =  new Dropbox(userId, scan, conn); //change
//            } catch (Status e) {
//                throw e;
//            }
//            case "Dropbox":
//                try {
//                    cloudUser =  new Dropbox(userId, scan, conn);
//                } catch (Status e) {
//                    throw e;
//                }
//                break;
//            case "GCloud":  try {
//                cloudUser =  new Dropbox(userId, scan, conn); //change
//            } catch (Status e) {
//                throw e;
//            }
//
//        }
//        return 1;
//    }
}
