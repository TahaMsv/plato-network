import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static Map<AppUsers, ClientHandler> users;
    public static JSONObject json = JsonUtils.getJsonObjectFromFile("data_base.json");
    public static JSONArray userJsonArray;
    public static void main(String[] args) throws Exception {
        userJsonArray = json.getJSONArray("user_info");
        ServerSocket serverSocket = new ServerSocket(3000);
        users = new ConcurrentHashMap<>();
        while (true) {
            Socket socket = serverSocket.accept();      // wait for a client to connect
            ClientHandler temp = new ClientHandler(socket);
            (new Thread(temp)).start();
        }

    }
}
