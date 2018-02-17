package dropbox;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.callbacks.DbxGlobalCallbackFactory;
import com.dropbox.core.v2.callbacks.DbxRouteErrorCallback;
import com.dropbox.core.v2.callbacks.DbxNetworkErrorCallback;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class getfiles {
    private static final String ACCESS_TOKEN = "MG4b5Nnr6zEAAAAAAAAEWDk7sWbJ5rsKr0SsexvKcavsWsBIeubnfMBsS9LX-_sj";

    public static void main(String args[]) throws DbxException, IOException {
        // Create Dropbox client
        InetAddress ip = InetAddress.getByName("proxy.iiit.ac.in");
        StandardHttpRequestor.Config config = StandardHttpRequestor.Config.builder().withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, 8080))).build();
        StandardHttpRequestor requ = new StandardHttpRequestor(config);

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("test").withHttpRequestor(requ).build();
//        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
        DbxClientV2 client = new DbxClientV2(requestConfig, ACCESS_TOKEN);

        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder("/camera uploads");//configure folder name
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
//
//        // Upload "test.txt" to Dropbox
//        try {
//            InputStream in = new FileInputStream("test.txt");
//            FileMetadata metadata = client.files().uploadBuilder("/test.txt")
//                    .uploadAndFinish(in);
//        } catch(Exception e) {
//            //change
//        }
//
//        DbxDownloader<FileMetadata> downloader = client.files().download("/test.txt");
//        try {
//            FileOutputStream out = new FileOutputStream("test.txt");
//            downloader.download(out);
//            out.close();
//        } catch (DbxException ex) {
//            System.out.println(ex.getMessage());
//        }
    }
}