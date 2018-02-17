package dropbox;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.util.Dumpable.*;
import com.dropbox.core.*;

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

public class downloadfile {
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

        FileOutputStream outputStream = new FileOutputStream("/home/ranjana/Project2-2/download3/*");
        try {
            FileMetadata metadata  = client.files().downloadBuilder("/camera uploads/").download(outputStream);
            System.out.println("Metadata: " + metadata.toString());
        } finally {
            outputStream.close();
        }
    }
}