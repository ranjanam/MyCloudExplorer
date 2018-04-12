package main.Dropbox;

import java.net.ConnectException;
import java.util.*;
//import com.dropbox.core.*;
//import com.dropbox.core.http.StandardHttpRequestor;
//import com.dropbox.core.json.JsonReader;
//import com.dropbox.core.v2.DbxClientV2;
//import com.dropbox.core.v2.DbxPathV2;
//import com.dropbox.core.v2.files.*;
//import com.dropbox.core.v2.users.FullAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Scanner;
import main.user.CloudUser;
import main.Connection;

import javax.swing.plaf.synth.SynthRootPaneUI;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;

public class Dropbox extends CloudUser {
    private String appKey;
    private String appSecret;
    private String accessToken;
    //    private DbxAuthFinish userInfo;
    public Connection conn;
    //    private DbxClientV2 client;
    private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;


    public Dropbox(String userId, Scanner scan, Connection conn) throws Exception {
        scan = new Scanner(System.in);
        this.conn = conn;
        System.out.println("Create an app and provide app key and secret. If already created provide app key and secret");
        int currentOpt = 0;
        boolean isAppConfigured = false;
        String appKey = "";
        String appSecret = "";
        String accessToken = "";
        Dropbox db = null;
        while (!isAppConfigured && currentOpt != 2) {
            try {
                System.out.print("App key :");
                appKey = scan.next();
                appKey = "mdcz7ip12s32912";
                System.out.print("App secret :");
                appSecret = scan.next();
                appSecret = "mz1mj7symlhkugd";
//                System.out.println(appSecret);
                accessToken = getAccessToken(appKey, appSecret);
                isAppConfigured = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("1.Re-enter information\n2.Exit");
                currentOpt = scan.nextInt();
            }
        }
        if (currentOpt == 2) {
            throw new Exception();
        }
        this.appSecret = appSecret;
        this.appKey = appKey;
        this.accessToken = accessToken;
    }

    private String getAccessToken(String appKey, String appSecret) throws Exception {
        Scanner scan = new Scanner(System.in);
        try {
            JSONObject params = new JSONObject();
            params.put("appKey", appKey);
            params.put("appSecret", appSecret);
            params.put("option", "1");
            JSONObject response = conn.performOperation(params, "Dropbox", "logIn");
            System.out.println(response.toString());
            if (response.getInt("status") != 200) {
                return response.getString("message");
            }
            System.out.println(response.get("data")); //check key - add in server , also used status to check success or fail //print response from server
            String code = null;
            while (code == null) {
                code = scan.nextLine();
            }
            params.put("code", code);
            params.put("option", "2");
            response = conn.performOperation(params, "Dropbox", "logIn");
            return response.getString("accessToken");
        } catch (Exception e) {
            throw e;
        }

    }

    private String getPath(List<String> locations, String fileName) {

        String currDir = "";
        if (locations.size() == 0)
            return fileName.length() > 0 ? "/" + fileName : "";
        currDir = "/";
        for (String s : locations) {
            currDir += s + '/';
        }
        if (fileName.length() != 0) {
            currDir += fileName;
        } else if (currDir.length() > 1)
            return currDir.substring(0, currDir.length() - 1);
        return currDir;
    }

    public String listFiles(JSONObject data) throws Exception {
        return "";

    }
    public  String uploadFile(JSONObject data) throws Exception {
        return "";

    }
    public  String downloadFile(JSONObject data) throws Exception {
        return "";

    }
    public  String deleteFile(JSONObject data) throws Exception {
        return "";

    }
}