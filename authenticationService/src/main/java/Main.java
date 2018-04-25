package main.java;


public class Main {


    public static void main(String[] args) throws Exception {
	// write your code here
        classAuthService authServ = new classAuthService("localhost","auth_request","auth_response");
        authServ.initService();
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        while(true);
//        authServ.exitAuthService();


    }
}
