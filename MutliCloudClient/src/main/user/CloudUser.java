package main.user;
import main.Connection;
import org.json.JSONObject;

abstract public class CloudUser {
    public Connection conn;
    public abstract String uploadFile(JSONObject data) throws Exception;
    public abstract String downloadFile(JSONObject data) throws Exception;
    public abstract String deleteFile(JSONObject data) throws Exception;
    public abstract String listFiles(JSONObject data) throws Exception;

}
