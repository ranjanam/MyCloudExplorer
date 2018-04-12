package main.user;


import org.json.JSONObject;
import main.Connection;
import java.util.Scanner;

public class User {
    public String userName;
    public String userId;
    public CloudUser cloudUser;

//    AmazonUser dbUser;
//    GCloudUser dbUser;
    //TODO
    public void userLogin(JSONObject params, Connection conn) throws  Exception{
        //if
        try {
            conn.performOperation(params,"", "logIn");
        } catch (Exception e) {
            throw e;
        }
    }
    //TODO
    public void userSignUp(JSONObject params, Connection conn) throws  Exception{
        try {
            conn.performOperation(params,"", "signUp");
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
