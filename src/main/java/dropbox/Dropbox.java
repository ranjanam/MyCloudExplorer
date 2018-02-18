package main.java.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.v2.DbxClientV2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;

public class Dropbox {
    private String key;
    private String secret;
    private String accessToken;
    private DbxAuthFinish userInfo;
    private DbxClientV2 client;
    public Dropbox(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }
    public DbxAuthFinish getUserInfo() {
        return userInfo;
    }
    public DbxClientV2 getClient() {
        return client;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getKey() {
        return key;
    }
    public String getSecret() {
        return secret;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public Status fetchAccessToken(Dropbox db) {
        // Read app info file (contains app key and app secret)
        DbxAppInfo appInfo;
        appInfo = new DbxAppInfo(db.getKey(), db.getSecret());
        InetAddress ip;
        try {
            ip = InetAddress.getByName("proxy.iiit.ac.in");
        } catch (Exception e) {
            return (new Status(4040, e.getMessage()));
        }
        StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder().withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, 8080))).build();
        StandardHttpRequestor requ = new StandardHttpRequestor(config);
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("test").withHttpRequestor(requ).build();//change

        DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .build();

        String authorizeUrl = webAuth.authorize(webAuthRequest);
        System.out.println("1. Go to " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        System.out.println("3. Copy the authorization code.");
        System.out.print("Enter the authorization code here: ");

        Scanner scan = new Scanner(System.in);
        String code = scan.next();
        if (code == null) {
            System.exit(1); return new Status(1,"invalid input");
        }
        code = code.trim();

        try {
            this.userInfo = webAuth.finishFromCode(code);
        } catch (DbxException ex) {
            return new Status(404, ex.getMessage());
        }

        System.out.println("Authorization complete");
        return new Status(400,this.userInfo.getAccessToken());
    }


}
