package main.java.dropbox;

import java.util.*;
import com.dropbox.core.*;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.FullAccount;

import javax.swing.plaf.synth.SynthRootPaneUI;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Scanner;

public class Dropbox {
    private String key;
    private String secret;
    private String accessToken;
    private DbxAuthFinish userInfo;
//    private DbxClientV2 client;
    private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;


    public Dropbox(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public DbxAuthFinish getUserInfo() {
        return userInfo;
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
    public String getPath(List<String> locations, String fileName) {

        String currDir="";
        if(locations.size()==0)
            return fileName.length()>0 ? "/"+fileName: "";
        currDir="/";
        for (String s:locations) {
            currDir+=s+'/';
        }
        if (fileName.length()!=0) {
            currDir+=fileName;
        } else if(currDir.length()>1)
            return currDir.substring(0, currDir.length() - 1);
        return currDir;
    }

    private static String uploadFileToDropbox(DbxClientV2 dbxClient, File localFile, String dropboxPath) throws Status {
        String returnString="";
        try  {
            InputStream in = new FileInputStream(localFile);
            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(in);
            returnString = metadata.toStringMultiline();

        } catch (UploadErrorException ex) {
            throw new Status(404,"Error uploading to Dropbox: " + ex.getMessage());

        } catch (DbxException ex) {
            throw new Status(404,"Error uploading to Dropbox: " + ex.getMessage());
        } catch (IOException ex) {
            throw new Status(404,"Error reading from file \"" + localFile + "\": " + ex.getMessage());
        }
        return returnString;
    }
    private static String chunkedUploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) throws Status {
        long size = localFile.length();
        String returnString="";
        if (size < CHUNKED_UPLOAD_CHUNK_SIZE) {
            throw new Status(404,"File too small, use upload() instead.");

        }

        long uploaded = 0L;
        DbxException thrown = null;
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
            if (i > 0) {
                System.out.printf("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS);
            }

            try  {
                InputStream in = new FileInputStream(localFile);
                // if this is a retry, make sure seek to the correct offset
                in.skip(uploaded);

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart()
                            .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE)
                            .getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                }

                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
                    dbxClient.files().uploadSessionAppendV2(cursor)
                            .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                    cursor = new UploadSessionCursor(sessionId, uploaded);
                }

                // (3) Finish
                long remaining = size - uploaded;
                CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath)
                        .withMode(WriteMode.ADD)
                        .withClientModified(new Date(localFile.lastModified()))
                        .build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
                        .uploadAndFinish(in, remaining);
                returnString = metadata.toStringMultiline();

                return returnString;
            } catch (RetryException ex) {
                thrown = ex;
                try {
                    sleepQuietly(ex.getBackoffMillis());
                } catch (Status e) {
                    throw e;
                }
                continue;
            } catch (NetworkIOException ex) {
                thrown = ex;

                continue;
            } catch (UploadSessionLookupErrorException ex) {
                if (ex.errorValue.isIncorrectOffset()) {
                    thrown = ex;
                    uploaded = ex.errorValue
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                } else {
                    // Some other error occurred, give up.
                    throw new Status(404,"Error uploading to Dropbox: " + ex.getMessage());
                }
            } catch (UploadSessionFinishErrorException ex) {
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                            .getLookupFailedValue()
                            .getIncorrectOffsetValue()
                            .getCorrectOffset();
                    continue;
                } else {
                    // some other error occurred, give up.
                    throw new Status(404,"Error uploading to Dropbox: " + ex.getMessage());

                }
            } catch (DbxException ex) {
                throw new Status(404,"Error uploading to Dropbox: " + ex.getMessage());
            } catch (IOException ex) {
                throw new Status(404,"Error reading from file \"" + localFile + "\": " + ex.getMessage());

            }

        }
        throw new Status(404,"Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());
    }

    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    private static void sleepQuietly(long millis) throws Status {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // just exit
            throw new Status(404,"Error uploading to Dropbox: interrupted during backoff.");

        }
    }
    private DbxRequestConfig checkConnectionAndGetConfig() throws Status {
        InetAddress ip;
        try {
            ip = InetAddress.getByName("proxy.iiit.ac.in");
        } catch (Exception e) {
            throw (new Status(4040, e.getMessage()));
        }
        StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder().withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, 8080))).build();
        StandardHttpRequestor requ = new StandardHttpRequestor(config);
        return DbxRequestConfig.newBuilder("test").withHttpRequestor(requ).build();//change
    }

    ;

    public String fetchAccessToken(Dropbox db) throws Status {
        // Read app info file (contains app key and app secret)
        DbxAppInfo appInfo;
        appInfo = new DbxAppInfo(db.getKey(), db.getSecret());
        DbxRequestConfig requestConfig;
        try {
            requestConfig = checkConnectionAndGetConfig();
        } catch (Status e) {
            throw e;
        }

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
            throw new Status(1, "invalid input");
        }
        code = code.trim();

        try {
            this.userInfo = webAuth.finishFromCode(code);
        } catch (DbxException ex) {
            throw new Status(404, ex.getMessage());
        }

        System.out.println("Authorization complete\n");
        return this.userInfo.getAccessToken();
    }

    private DbxClientV2 getClient(Dropbox db) throws Status {
        DbxRequestConfig requestConfig;
        try {
            requestConfig = checkConnectionAndGetConfig();
        } catch (Status e) {
            throw e;
        }
        return new DbxClientV2(requestConfig, db.getAccessToken());
    }

    public String listFiles(String dir, Dropbox db) throws DbxException, IOException, Status {

        DbxClientV2 client = getClient(db);
        ListFolderResult result = client.files().listFolder(dir);//handle error
        String filesList = "";
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                filesList += metadata.getPathLower() + '\n';
            }

            if (!result.getHasMore()) {
                break;
            }
            result = client.files().listFolderContinue(result.getCursor());
        }
        return filesList;
    }

    public String downloadFile(String inputFilePath, String outputFilePath, Dropbox db) throws Status, DbxException, IOException{

        DbxClientV2 client ;
        try {
            client = getClient(db);
        } catch(Status e) {
            throw e;
        }
        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(outputFilePath);
            try {
                FileMetadata metadata = client.files().downloadBuilder(inputFilePath).download(outputStream); //handle error
                return metadata.toString();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw e;
        }
    }
    public String deleteFile(String fileName, Dropbox db) throws Status,DbxException {

        DbxClientV2 client ;
        try {
            client = getClient(db);
        } catch(Status e) {
            throw e;
        }

        try {
            FileOpsResult result = client.files().deleteV2(fileName);
            System.out.println(result.toString());
        } catch (DbxException dbxe) {
            throw  dbxe;
        }
        return "File Deleted Successfully";
    }
    public String uploadFile(List<String> locations, String localPath,String outputFileName, Dropbox db) {

        String dropboxPath = getPath(locations,outputFileName);
        String pathError = DbxPathV2.findError(dropboxPath);
        if (pathError != null) {
            return "Invalid <dropbox-path>: " + pathError;
        }

        File localFile = new File(localPath);
        if (!localFile.exists()) {
            return "Invalid <local-path>: file does not exist.";
//            throw (new Status(404, "Invalid <local-path>: file does not exist."));
        }

        if (!localFile.isFile()) {
            return "Invalid <local-path>: not a file.";
//            throw (new Status(404, ));
        }
        DbxClientV2 client ;
        try {
            client = getClient(db);
        } catch(Status e) {
            return e.getMessage();
        }
        String returnString="";
        if (localFile.length() <= (2 * CHUNKED_UPLOAD_CHUNK_SIZE)) {
            try {

                returnString = uploadFileToDropbox(client, localFile, dropboxPath);

            } catch (Status e) {
                return e.getMessage();
            }
        } else {
            try {
                chunkedUploadFile(client, localFile, dropboxPath);
            } catch(Status e) {
                return e.getMessage();
            }
        }
        return returnString;
    }
    public String handleListFiles(List<String> locations, Dropbox db, Scanner scan) { //change - pass cmd inputs
        System.out.print("Enter directory/file name : ");
        String name = scan.nextLine();
        System.out.print("Enter destination location : ");
        String outputFileName = scan.nextLine();
        String currDir=getPath(locations, name);
        String returnString="";
        System.out.println(currDir);
        try {
            returnString="File Downloaded Successfully\n"+db.downloadFile(currDir, outputFileName, db)+"\n";
        } catch (Exception e) {
            returnString = e.getMessage();
        }
        return returnString;
    }
    public String handleDeleteFiles(List<String> locations, Dropbox db, Scanner scan) { //change - pass cmd inputs
        System.out.print("Enter directory/file name : ");
        String name = scan.nextLine();
        String currDir=getPath(locations,name);
        System.out.println(currDir);
        String returnString="";
        try {
            returnString=db.deleteFile(currDir, db);
        } catch (Exception e) {
            returnString = e.getMessage();
        }

        return returnString;
    }
}
