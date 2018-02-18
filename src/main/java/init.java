package main.java;
import java.util.Scanner;

import main.java.dropbox.*;
public class init {
    public static void main(String args[]) {
        System.out.println("Welcome to MyCloudExplorer");
        System.out.println("-----------------------------------");
        //TODO : Add app login
        System.out.println("Select your cloud\n 1.Amazon \n 2.Dropbox \n 3.Gcloud");
        Scanner scan = new Scanner(System.in);
//        Integer option = scan.nextInt();
        int option = scan.nextInt();
        switch(option) {
            case 2:
                System.out.println("Create an app and provide app key and secret. If already created provide app key and secret");
                System.out.print("App key : ");
                String user_key = scan.next();
                System.out.print("App secret : ");
                String user_secret = scan.next();
                Dropbox db = new Dropbox(user_key, user_secret);
                Status returnStatus = db.fetchAccessToken(db);
                if (returnStatus.getCode()!=1) {
                    System.out.println(returnStatus.getMessage());
                    break;
                }
                db.setAccessToken(returnStatus.getMessage());
                int currentOpt=0;
                while(currentOpt!=4) {
                    System.out.println("Perform following operations\n1.List Files\n2.goto directory\n3.Upload file\n4.Download file\n5.Delete file\n");
                    System.out.println("Enter option : ");
                    currentOpt=scan.nextInt();
                    switch (currentOpt) {
                        case 1:

                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                        default:System.out.println("Invalid Option");
                            break;
                    }
                }
                System.out.println(db.toString());
                break;
        }
    }
}
