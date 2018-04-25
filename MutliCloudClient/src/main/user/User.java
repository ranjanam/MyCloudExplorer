package main.user;


import org.json.JSONObject;
import main.Connection;
import java.util.Scanner;

public class User {
    public String userName;
    public String userId;
    public String token;
    public CloudUser cloudUser;


    public User(){
        setToken("");
        setUserID("");
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

            JSONObject json = conn.performOperation(params,"", "login");
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
