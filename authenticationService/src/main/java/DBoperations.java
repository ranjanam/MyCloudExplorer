package main.java;

import java.time.Instant;

import java.util.Iterator;
//import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;

//import com.journaldev.mongodb.model.User;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.json.JSONObject;


public class DBoperations {
    String username, password,userID,token,emailID;
    JSONObject cloud_info;
    int ErrorCode;
    long timeout= 300 ; // 5mins
    MongoClient mongo;
    DB database;
    Instant instant;

    DBoperations() throws Exception{
        mongo = new MongoClient("localhost");
        cloud_info = new JSONObject();

        // Creating Credentials
        System.out.println("Connected to the database successfully");

        // Accessing the database
        database = mongo.getDB("cloud");
//        DB database = mongo.getDB("TheDatabaseName");


    }

    public void setCloud_info(String key, String value) throws Exception{
        cloud_info.put(key, value);
    }

    public String getCloud_info(String key) throws Exception{
        return ((String) cloud_info.get(key));
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
    public void signIn(){
        DBCollection users = database.getCollection("users");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("username", this.getUsername());
        BasicDBObject fields = new BasicDBObject();
        fields.put("password", 1);
        fields.put("_id", 1);
        DBObject User = users.findOne(whereQuery,fields);

        if(User==null)
        {
            System.out.println("no user found");
            setErrorCode(101);
        }
        else if(User.get("password").equals(this.getPassword())){
            setErrorCode(200);

            setUserID(User.get("_id").toString());
            instant = Instant.now();
            long tokenval=instant.getEpochSecond();
            System.out.println("token generated: "+ tokenval);

            setToken(Long.toString(tokenval+timeout));

            BasicDBObject set = new BasicDBObject("$set", new BasicDBObject("token", this.getToken()));
            users.update(whereQuery, set);
        }
        else{
            System.out.println("wrong password");
            setErrorCode(100);
        }
    }
    public void validateToken(){
        DBCollection users = database.getCollection("users");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", new ObjectId(this.getUserID()));
        BasicDBObject fields = new BasicDBObject();
        fields.put("token", 1);
        DBObject User = users.findOne(whereQuery,fields);
        if(User==null)
        {
            System.out.println("no user found");
            setErrorCode(101);
        }
        else if(User.get("token").equals(this.getToken())){     // token verified
            setErrorCode(200);

//        check token validity

            instant = Instant.now();
            long currTokenVal=instant.getEpochSecond();
            System.out.println("current token value: "+ currTokenVal);
            long reqTokenVal = Long.parseLong(this.getToken());
            if (reqTokenVal<currTokenVal){
                System.out.println("token expired.. new token in the response");

                setToken(Long.toString(currTokenVal+timeout));

                BasicDBObject set = new BasicDBObject("$set", new BasicDBObject("token", this.getToken()));
                users.update(whereQuery, set);
            }
        }
        else{

            setErrorCode(100);
        }
    }

    public void createUser(){

        DBCollection users = database.getCollection("users");

// TODO: Hash password, unique username
//        setPassword(getPassword());
//        generate token

        instant = Instant.now();
        long tokenval=instant.getEpochSecond();
        System.out.println("token generated: "+ tokenval);

        setToken(Long.toString(tokenval+timeout));
        try
        {
            this.setCloud_info("aws", "false");
            this.setCloud_info("dropbox", "false");
            this.setCloud_info("gcloud", "false");

            BasicDBObject cloud_info = new BasicDBObject();
            cloud_info.put("aws", this.getCloud_info("aws"));
            cloud_info.put("dropbox", this.getCloud_info("dropbox"));
            cloud_info.put("gcloud", this.getCloud_info("gcloud"));

            BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();

            docBuilder.append("username", this.getUsername());
            docBuilder.append("password", this.getPassword());
            docBuilder.append("email_id", this.getEmailID());
            docBuilder.append("cloud_info", cloud_info);
            docBuilder.append("token", this.getToken());
            DBObject doc =docBuilder.get();

            WriteResult result = users.insert(doc);
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("username", this.getUsername());
            BasicDBObject fields = new BasicDBObject();
            fields.put("_id", 1);

            DBObject newUser = users.findOne(whereQuery,fields);

            System.out.println("[INFO] user created successfully with userID: "+ newUser.get("_id"));
            setUserID(newUser.get("_id").toString());

            setErrorCode(200);
//            System.out.println("finished");
        }

        catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            setErrorCode(111);
        }

    }

}
