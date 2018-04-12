package main;
import main.user.CloudUser;

import org.json.JSONObject;
import main.Dropbox.*;
import main.Connection;
import main.user.User;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Startup {

    private static boolean listFilesDropbox(CloudUser currentCloudUser,List<String> locations) {
        String filesList="";
        try {
            JSONObject obj = new JSONObject();
            obj.put("location", locations);
            filesList = currentCloudUser.listFiles(obj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        System.out.println(filesList);
        return true;
    }
    private static boolean appLogin(Scanner scan, User userObj, Connection conn) {
        boolean isUserLoggedIn = false;
        while(true) {
            System.out.println("1.Login 2.SignUp 3.Exit");
            System.out.print("Enter option : ");
            int userOption = scan.nextInt();
            scan.nextLine();
            if (userOption==3)
                break;
            while(true) {
                if (userOption==1) {
                    String keys[] = {"username", "password"};
                    JSONObject params = new JSONObject();
                    for(int i=0;i<keys.length;) {
                        System.out.print("Enter "+keys[i]+" :");
                        try {
                            String value = scan.nextLine();
                            if (value.equals(""))
                                System.out.println("Please enter proper value");
                            else {
                                params.put(keys[i], value);
                                i++;

                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    try {
                        userObj.userLogin(params, conn);
                        System.out.println("Success");
                        isUserLoggedIn = true;

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    if (isUserLoggedIn)
                        break;
                    System.out.println("1.Try again 2.Exit");
                    System.out.print("Enter option : ");
                    int currentOption = scan.nextInt();
                    scan.nextLine();
                    if (currentOption==2)
                        break;
                } else if (userOption==2) {
                    String keys[] = {"username", "password", "email"};
                    JSONObject params = new JSONObject();
                    int i;
                    for(i=0;i<keys.length;) {
                        System.out.print("Enter "+keys[i]+" :");
                        try {
                            String value = scan.nextLine();
                            if (value.isEmpty())
                                System.out.println("Please enter proper value");
                            else {
                                params.put(keys[i], value);
                                i++;
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    try {
                        userObj.userSignUp(params,conn);
                        System.out.println("Success");
                        isUserLoggedIn = true;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    if (isUserLoggedIn)
                        break;
                    System.out.println("1.Try again 2.Exit");
                    System.out.print("Enter option : ");
                    int currentOption = scan.nextInt();
                    scan.nextLine();
                    if (currentOption==2)
                        break;
                }
            }
            if (isUserLoggedIn)
                return isUserLoggedIn;
        }
        return isUserLoggedIn;
    }
    public static void main(String args[]) {
        Scanner scan = new Scanner(System.in);
        Connection conn;
        User userObj = new User();
        try {
            conn = new Connection();
        } catch (Exception e) {
            System.out.println("Cannot read Config file "+e.getMessage());
            return;
        }
        System.out.println("My Cloud Explorer");
        System.out.println("-----------------------------");
//        boolean isUserLoggedIn = appLogin(scan, userObj, conn);
        boolean isUserLoggedIn = true; //**********change******
        if (!isUserLoggedIn) {
            System.out.println("Bye...");
            return;
        }

        while(true) {
            System.out.println("1.Amazon 2.Dropbox 3.Google Cloud");
            System.out.print("Enter option : ");
            int option = scan.nextInt();
            scan.nextLine();
            boolean isAppConfigured=false;
            switch (option) {
                case 1:
                    break;
                case 2:
                    int currentOpt;
                    try {
                        userObj.cloudUser = new Dropbox(userObj.userId, scan, conn);
                        isAppConfigured = true;
                    } catch (Exception e) {
                        isAppConfigured = false;
                        System.out.println(e.getMessage());
                    }

                    if (!isAppConfigured)
                        continue;
                    CloudUser currentCloud = userObj.cloudUser;

                    List<String> locations = new ArrayList<>();
                    boolean isExitCloud = false;
                    while(true) {
                        System.out.println("1.List Current Directory 2.Goto Directory 3.Upload 4.Download 5.Delete 6.Go Back");
                        System.out.print("Enter option : ");
                        currentOpt=scan.nextInt();
                        scan.nextLine();
                        switch (currentOpt) {
                            case 1:
                                if (!listFilesDropbox(currentCloud, locations)) { //check
                                    locations.remove(locations.size() - 1);
                                }

                                break;
                            case 2: System.out.println("Enter Directory name : ");
                                String currDir = scan.nextLine();
                                locations.add(currDir);
                                if (!listFilesDropbox(currentCloud, locations)) {
                                    locations.remove(locations.size() - 1);
                                }
                            case 3:
                                System.out.print("Enter local file location : ");
                                String inputFileName = scan.nextLine();
                                System.out.print("Enter output file name : ");
                                String outputFileName = scan.nextLine();
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("location", locations);
                                    obj.put("outputFileName", outputFileName);
                                    obj.put("inputFileName", inputFileName);
                                    System.out.println(currentCloud.uploadFile(obj));
                                } catch (Exception e) {
                                    System.out.println("Upload failed");

                                }
                                break;
                            case 4:
                                System.out.print("Enter directory/file name : ");
                                String name = scan.nextLine();
                                System.out.print("Enter destination location : ");
                                outputFileName = scan.nextLine();
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("location", locations);
                                    obj.put("outputFileName", outputFileName);
                                    obj.put("inputFileName", name);
                                    System.out.println(currentCloud.downloadFile(obj));
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }

                                break;
                            case 5:
                                System.out.print("Enter directory/file name : ");
                                name = scan.nextLine();
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("location", locations);
                                    obj.put("fileName", name);
                                    System.out.println(currentCloud.deleteFile(obj));
                                } catch(Exception e) {
                                    System.out.println(e.getMessage());
                                }

                                break;
                            case 6:System.out.println("Going back");
                                if (locations.size()>0) {
                                    locations.remove(locations.size() - 1);
                                } else {
                                    isExitCloud = true;
                                }
                                break;
                            default:System.out.println("Invalid Option");
                                break;
                        }
                        if (isExitCloud)
                            break;
                    }

                    break;
                case 3:
                    break;
                default:System.out.println("Enter valid option");
                    break;
            }



        }
    }


}
