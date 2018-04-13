package main.java;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
//import com.mongodb.MongoCredential;

public class Main {


    public static void main(String[] args) throws Exception {
	// write your code here
        classAuthService authServ = new classAuthService("Auth_Req_Queue","Auth_Resp_Queue");
        authServ.initService();
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        while(true);
//        authServ.exitAuthService();


    }
}
