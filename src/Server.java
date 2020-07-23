import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static Map<AppUsers, ClientHandler> users;
    public static JSONObject json = JsonUtils.getJsonObjectFromFile("data_base.json");
    public static JSONArray userJsonArray;
    public static Set<AppUsers> setOfUsers = new HashSet<>();


    public static void main(String[] args) throws Exception {
        userJsonArray = json.getJSONArray("user_info");
        setUsersFromFile();
        ServerSocket serverSocket = new ServerSocket(3000);
        users = new ConcurrentHashMap<>();
//        while (true) {
//            Socket socket = serverSocket.accept();      // wait for a client to connect
//            ClientHandler temp = new ClientHandler(socket);
//            (new Thread(temp)).start();
//        }

    }

    public static void setUsersFromFile() {
        for (Object val : userJsonArray) {
            JSONObject jOb = (JSONObject) val;
            AppUsers appUser = new AppUsers(jOb.getString("username"), jOb.getString("password"));
            String strOfFriends = "";
            ArrayList<String> friendsArrayList = new ArrayList<>();
            if (JsonUtils.objectExists(jOb, "friends")) {
                strOfFriends = jOb.getString("friends");
                String[] friends = strOfFriends.split("\\+");
                friendsArrayList = new ArrayList<>(Arrays.asList(friends));
            }
            appUser.setStringFriends(friendsArrayList);


            if (JsonUtils.objectExists(jOb, "chats")) {
                JSONObject jChat = jOb.getJSONObject("chats");
                Set<String> chattedNames = jChat.keySet();
                for (String str:chattedNames) {
                    appUser.getAllMessages().put(str, jChat.getString(str));
                }
            }

            setOfUsers.add(appUser);
        }
        for (AppUsers currAppUser : setOfUsers) {
            for (String val : currAppUser.getStringFriends()) {
                currAppUser.addNewFriend(getAppUserByUsername(setOfUsers, val));
            }
        }

    }

    public static AppUsers getAppUserByUsername(Set<AppUsers> setOfUsers, String name) {
        for (AppUsers val : setOfUsers) {
            if (val.getUsername().equals(name)) {
                return val;
            }
        }
        return new AppUsers("default", "default");
    }
}
