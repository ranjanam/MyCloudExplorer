package main.java;


import java.util.*;

import main.java.dropbox.*;
public class init {
    public static void main(String args[]) {
        System.out.println("Welcome to MyCloudExplorer");
        System.out.println("-----------------------------------");
        //TODO : Add app login
        System.out.println("Select your cloud\n 1.Amazon \n 2.Dropbox \n 3.Gcloud");
        Scanner scan = new Scanner(System.in);
        int option = scan.nextInt();
        switch(option) {
            case 2:
                System.out.println("Create an app and provide app key and secret. If already created provide app key and secret");
                boolean isAppConfigured=false;
                int currentOpt=0;
                Dropbox db=null;
                while (!isAppConfigured && currentOpt!=2) {
                    try {
                        System.out.print("App key : ");
                        String user_key = scan.next();
                        System.out.print("App secret : ");
                        String user_secret = scan.next();
                        db = new Dropbox(user_key, user_secret);
                        db.setAccessToken(db.fetchAccessToken(db));
                        isAppConfigured = true;
                    } catch (Status e) {
                        System.out.println(e.getMessage());
                        System.out.println("1.Re-enter information\n2.Exit");
                        currentOpt = scan.nextInt();
                    }
                }
                if (!isAppConfigured) {
                    break;
                }
                currentOpt = 0;

                while(currentOpt!=5) {
                    List<String> locations = new ArrayList<>();
                    System.out.println("Perform following operations\n1.List Files\n2.Upload file\n3.Download file\n4.Delete file\n5.Exit");
                    System.out.print("Enter option : ");
                    currentOpt=scan.nextInt();
                    scan.nextLine();
                    switch (currentOpt) {
                        case 1:
                            locations.clear();

//                            locations.add("/");
                            String filesList="";
                            int index=0;
                            String currDir="", name;
                            while(index>=0) {
                                try {
                                    currDir = db.getPath(locations,"");
                                    filesList = db.listFiles(currDir, db);
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                                System.out.println(filesList);
                                System.out.println("1.Goto Directory 2.Upload files 3.Download file 4.Delete File 5.Go Back");
                                System.out.print("Enter option : ");
                                int choice = scan.nextInt();
                                scan.nextLine();

                                if (choice ==1) {
                                    System.out.print("Enter directory/file name : ");
                                    name = scan.nextLine();
                                    index++;
                                    locations.add(name);
                                } else if (choice == 2) {
                                    System.out.print("Enter local file location : ");
                                    name = scan.nextLine();
                                    System.out.print("Enter output file name : ");
                                    String outputFileName = scan.nextLine();
                                    System.out.println(db.uploadFile(locations, name, outputFileName, db));
                                } else if (choice ==3) {
                                    System.out.println(db.handleListFiles(locations, db, scan));
                                } else if (choice == 4) {
                                    System.out.println(db.handleDeleteFiles(locations, db, scan));
                                } else if (choice == 5) {

                                    index--;
                                    if(index<0)
                                        break;
                                    locations.remove(index);
                                } else {
                                    System.out.println("Invalid option");
                                }
                            }

                            break;
                        case 2:
                            locations.clear();
//                            locations.add("/");
                            System.out.print("Enter local file location : ");
                            name = scan.nextLine();
                            System.out.print("Enter output file name : ");
                            String outputFileName = scan.nextLine();
                            System.out.println(db.uploadFile(locations, name, outputFileName,db));
                            break;
                        case 3:
                            locations.clear();
//                            locations.add("/");
                            System.out.println(db.handleListFiles(locations, db, scan));
                            break;
                        case 4:
                            locations.clear();
                            System.out.println(db.handleDeleteFiles(locations, db, scan));
                            break;
                        case 5:System.out.println("Exiting dropbox");
                                break;
                        default:System.out.println("Invalid Option");
                            break;
                    }
                }
//                System.out.println(db.toString());
//                break;
        }
    }
}
