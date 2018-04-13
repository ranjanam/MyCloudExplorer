package main.java;

import java.time.Instant;

import java.util.Iterator;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

public class DBoperations {
    String username, password,userID,token,emailID;
    int ErrorCode;
    long timeout= 300 ; // 5mins
    MongoClient mongo;
    MongoDatabase database;
    Instant instant;

    DBoperations(){
        mongo = new MongoClient();

        // Creating Credentials
        System.out.println("Connected to the database successfully");

        // Accessing the database
        database = mongo.getDatabase("cloud");


    }
    public void setErrorCode(int errorCode){
        this.ErrorCode = errorCode;
    }

    public int getErrorCode(){
        return this.ErrorCode;
    }

    public void setUsername(String username){
        this.username=username;
    }
    public String getUsername(){
        return this.username;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public String getPassword(){
        return this.password;
    }

    public void setEmailID(String emailID){
        this.emailID=emailID;
    }

    public String getEmailID(){
        return this.emailID;
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

    public void createUser(){
        MongoCollection<Document> collection = this.database.getCollection("users");

// TODO: Hash password, unique username
//        setPassword(getPassword());
//        generate token

        instant = Instant.now();
        long tokenval=instant.getEpochSecond();
        System.out.println("token generated: "+ tokenval);

        setToken(Long.toString(tokenval+timeout));

        Document document = new Document("username", this.getUsername())
                .append("password", this.getPassword())
                .append("email_id", this.getEmailID())
                .append("token",this.getToken());
        collection.insertOne(document);

        System.out.println("[INFO] user created successfully with userID: "+ document.get("_id"));
//        setUserID(document.get("_id").toString());
        setUserID(document.get("_id").toString());

        setErrorCode(0);
    }
}
