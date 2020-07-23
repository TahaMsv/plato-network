import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {

    private static Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    public static String wordHangman;
    String message;
    AppUsers currUser;

    public ClientHandler(Socket socket) {
        ClientHandler.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            currUser = new AppUsers("default", "default");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        try {
            String username = "", password = "";
            while (true) {
                message = "";
                message = dis.readUTF();
                System.out.println(message);
                if (message.startsWith("signUPUsername:")) {
                    username = checkSignUpValidation(message);
                } else if (message.startsWith("signUpPassword:")) {
                    password = message.substring(15);
                } else if (message.startsWith("SignUpButton")) {
                    System.out.println(username + " " + password);
                    signUp(username, password);
                } else if (message.startsWith("signInButton:")) {
                    signIn();
                } else if (message.startsWith("addFriend:")) {
                    addFriend(message);
                } else if (message.startsWith("changeUsername")) {
                    username = changeUsername(message);
                } else if (message.startsWith("changePassword")) {
                    password = changePassword(message);
                } else if (message.startsWith("friendList")) {
                    friendList(message);
                } else if (message.startsWith("loadMessages")) {
                    loadMessages(message);
                } else if (message.startsWith("chatSenderMessage")) {
                    chatSenderMessage(message);
                } else if (message.startsWith("gameXORank")) {
                    gameXORank(message);
                } else if (message.startsWith("gameHangmanRank")) {
                    gameHangmanRank(message);
                } else if (message.startsWith("gameLeaderBoard")) {
                    gameLeaderBoard(message);
                } else if (message.startsWith("chosenWordHangMan")) {
                    onePlayerChoseTheWordForHangman(message);
                } else if (message.startsWith("waitingRoomForHangman")) {
                    waitToStartGame();
                } else if (message.startsWith("HangmanWinMsg")) {
                    winRoundOfHangman(message);
                } else if (message.startsWith("hangmanChooserWait")) {
                    hangmanChooserWait();
                } else if (message.startsWith("chatList")) {
                    chatList(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String checkSignUpValidation(String mess) {
        boolean isUsernameOk = false;
        String username = mess.substring(15);
        while (!isUsernameOk) {
            Set allUsers = Server.users.keySet();
            Set<String> usernames = new HashSet<>();
            for (Object allUser : allUsers) {
                AppUsers appUsers = (AppUsers) allUser;
                usernames.add(appUsers.getUsername());
            }
            String serverMessage = "";
            if (usernames.contains(username)) {
                serverMessage = "err: UserName already exists... please enter another one: ";
            } else {
                serverMessage = "correctUsername";
            }

            if (serverMessage.startsWith("err:")) {

                if (findUserInFileWhileSignIn(username)) {
                    serverMessage = "err: UserName already exists... please enter another one: ";
                } else {
                    serverMessage = "correctUsername";
                }
            }

            try {
                dos.writeUTF(serverMessage);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isUsernameOk = true;
        }
        return username;
    }

    public void signUp(String username, String password) {
        currUser = new AppUsers(username, password);
        Server.users.put(currUser, this);
        System.out.println("username : " + username + " password : " + password);
        addNewUserToJsonFile(username, password);
        Server.setOfUsers.add(currUser);
    }

    public void signIn() {
        String username = "", password = "";
        try {
            username = dis.readUTF();
            password = dis.readUTF();
            System.out.println("user " + username + " " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String answer = getAppUser(username, password);
        try {
            dos.writeUTF(answer);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getAppUser(String name, String password) {
        int status = 0;

        Set keySets = Server.users.keySet();
        for (Object keySet : keySets) {
            AppUsers appUsers = (AppUsers) keySet;
            if (appUsers.getUsername().equals(name)) {
                if (appUsers.getPassword().equals(password)) {
                    currUser = appUsers;

                    for (AppUsers appUser:Server.setOfUsers) {
                        if (currUser.getUsername().equals(appUser.getUsername())){
                            currUser.setFriends(appUser.getFriends());
                            currUser.setAllMessages(appUser.getAllMessages());
                        }
                    }


                    Server.users.put(currUser, this);
                    status = 2;
                } else {
                    status = 1;
                }
            }
        }
        if (status == 0) {
            if (findUserInFileWhileSignIn(name)) {
                if (foundUserHasTruePass(name, password)) {
                    status = 3;
                } else {
                    status = 4;
                }
            }
        }

        if (status == 0) {
            return "wrongUsername";
        } else if (status == 1) {
            return "wrongPassword";
        } else if (status == 2) {
            return "okSignIn";
        } else if (status == 3) {
            return "okSignIn";
        } else {
            return "wrongPassword";
        }
    }

    private void addFriend(String clMessage) {
        System.out.println("at add friend");
        String[] addFriendParts = clMessage.split("\\+");
        String username = addFriendParts[2];
        currUser = getAppUserByUsername(addFriendParts[2]);
        String answer = searchForFriend(addFriendParts[1]);
        System.out.println(currUser.getUsername());
        System.out.println(answer);
        try {
            dos.writeUTF(answer);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String searchForFriend(String friendUsername) {

        ArrayList<AppUsers> currentUserFriends = currUser.getFriends();
        if (!currentUserFriends.isEmpty()) {
            for (AppUsers curr : currentUserFriends) {
                if (curr.getUsername().equals(friendUsername)) {
                    return "this friend already exists";
                }
            }
        }
        Set keySets = Server.users.keySet();
        for (Object keySet : keySets) {
            AppUsers appUser = (AppUsers) keySet;
            if (appUser.getUsername().equals(friendUsername)) {
                currUser.addNewFriend(appUser);
                appUser.addNewFriend(currUser);
                addNewFriendToUserFriendListFile(currUser.getUsername(), friendUsername);
                addNewFriendToUserFriendListFile(friendUsername, currUser.getUsername());
                return "ok " + appUser.getUsername() + " added to your friends successfully";
            }
        }
        return "err " + friendUsername + " not found";
    }

    private String changeUsername(String clMsg) {
        String[] splitedString = clMsg.split("\\+");
        currUser = getAppUserByUsername(splitedString[1]);
        String newUsername = splitedString[2];
        changeUsernameInJson(currUser.getUsername(), newUsername);
        currUser.setUsername(newUsername);
        return newUsername;
    }

    private String changePassword(String clMsg) {
        String[] splitedStr = clMsg.split("\\+");
        currUser = getAppUserByUsername(splitedStr[1]);
        String newPassword = splitedStr[2];
        changPasswordInJson(currUser.getUsername(), newPassword);
        currUser.setPassword(newPassword);
        return newPassword;
    }

    private void friendList(String clMsg) {
        currUser = getAppUserByUsername(clMsg.substring(10));
        System.out.println(currUser.friendsStringListString());
        try {
            String serverMessage = "a+" + currUser.friendsStringListString();
            System.out.println(serverMessage);
            dos.writeUTF(serverMessage);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chatList(String message) {
        currUser = getAppUserByUsername(message.substring(8));
        String chatListString = currUser.getChatList();
        try {
            String serverMessage = "a+" + chatListString;
            System.out.println(serverMessage);
            dos.writeUTF(serverMessage);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadMessages(String clMsg) {
        String[] data = clMsg.split("\\+");
        String friend = data[1];
        currUser = getAppUserByUsername(data[2]);
        String currentFriendMessages = currUser.getAllMessages().get(friend);
        System.out.println(currentFriendMessages);
        try {
            if (currentFriendMessages != null) {
                System.out.println(171);
                dos.writeUTF(currentFriendMessages);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                dos.writeUTF(" ");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chatSenderMessage(String clMsg) {
        String[] data = clMsg.split("\\+");  // data[1]=sender ,  data[2]=receiver, data[3]=message , data[4]=time
        currUser = getAppUserByUsername(data[1]);
        String currentMessage = data[1] + "," + data[2] + "," + data[3] + "," + data[4];
        Set set = Server.users.keySet();
        String m = currUser.getAllMessages().get(data[2]);
        m += ("+" + currentMessage);
        currUser.getAllMessages().put(data[2], m);
        AppUsers receiverAppUser;
        for (Object o : set) {
            AppUsers t = (AppUsers) o;
            if (t.getUsername().equals(data[2])) {
                receiverAppUser = t;
                receiverAppUser.getAllMessages().put(data[1], m);
                addChatToJsonFile(currUser.getUsername(), receiverAppUser.getUsername(), currentMessage);
                addChatToJsonFile(receiverAppUser.getUsername(), currUser.getUsername(), currentMessage);
            }
        }
    }

    private AppUsers getAppUserByUsername(String username) {
        Set users = Server.users.keySet();
        for (Object o : users) {
            AppUsers a = (AppUsers) o;
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return new AppUsers("default", "default");
    }

    private void gameXORank(String clMsg) {
        String username = clMsg.substring(10);
        currUser = getAppUserByUsername(username);
        currUser.setWantToPlayXORank(true);
        boolean foundSomeOneToPlay = false;
        while (!foundSomeOneToPlay) {
            Set appUsers = Server.users.keySet();
            for (Object val : appUsers) {
                AppUsers appUser = (AppUsers) val;
                if (appUser.isWantToPlayXORank() && !appUser.equals(currUser)) {
                    try {
                        currUser.setWantToPlayXORank(false);
                        foundSomeOneToPlay = true;
                        if (currUser.getUsername().compareTo(appUser.getUsername()) > 0) {
                            dos.writeUTF("startXO" + "1");
                        } else {
                            dos.writeUTF("startXO" + "0");
                        }
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void gameHangmanRank(String clMsg) {
        String[] strings = clMsg.split("\\+");
        String username = strings[1];
        int timesOfPlay = Integer.parseInt(strings[2]);
        currUser = getAppUserByUsername(username);
        currUser.setWantToPlayHangmanRank(true);
        boolean foundSomeOneToPlay = false;
        while (!foundSomeOneToPlay) {
            Set appUsers = Server.users.keySet();
            for (Object val : appUsers) {
                AppUsers appUser = (AppUsers) val;
                if (appUser.isWantToPlayHangmanRank() && !appUser.equals(currUser)) {
                    try {
                        currUser.setWantToPlayHangmanRank(false);
                        foundSomeOneToPlay = true;
                        if (timesOfPlay == 2) {
                            if (currUser.getUsername().compareTo(appUser.getUsername()) > 0) {
                                String chooserPlayer = currUser.getUsername();
                                String playerPlayer = appUser.getUsername();
                                dos.writeUTF("startHangman+" + "Chooser+" + chooserPlayer + "+" + playerPlayer);
                            } else {
                                String chooserPlayer = appUser.getUsername();
                                String playerPlayer = currUser.getUsername();
                                dos.writeUTF("startHangman+" + "Player+" + chooserPlayer + "+" + playerPlayer);
                            }
                        } else if (timesOfPlay == 1) {
                            if (currUser.getUsername().compareTo(appUser.getUsername()) < 0) {
                                String chooserPlayer = currUser.getUsername();
                                String playerPlayer = appUser.getUsername();
                                dos.writeUTF("startHangman+" + "Chooser+" + chooserPlayer + "+" + playerPlayer);
                            } else {
                                String chooserPlayer = appUser.getUsername();
                                String playerPlayer = currUser.getUsername();
                                dos.writeUTF("startHangman+" + "Player+" + chooserPlayer + "+" + playerPlayer);
                            }
                        }
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void onePlayerChoseTheWordForHangman(String clMsg) {
        String[] word = clMsg.split("\\+");
        wordHangman = word[2];
    }

    private void waitToStartGame() {
        while (wordHangman.equals("")) ;
        try {
            dos.writeUTF("hangmanWordPlay" + wordHangman);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void winRoundOfHangman(String clMsg) {
        String winnerUsername = clMsg.substring(13);
        currUser = getAppUserByUsername(winnerUsername);
        currUser.setHangmanScore();
        wordHangman = "";
        ///
    }

    private void hangmanChooserWait() {
        while (!wordHangman.equals("")) ;
        try {
            dos.writeUTF("finishedPlayingHangman");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gameLeaderBoard(String clMsg) {
        System.out.println(clMsg);
    }

    private void addNewUserToJsonFile(String username, String password) {
        JSONObject newUser = new JSONObject();
        newUser.put("username", username);
        newUser.put("password", password);
        Server.userJsonArray.put(newUser);
        writeToFile();
    }

    private void addNewFriendToUserFriendListFile(String username, String friend) {
        for (Object val : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) val;
            if (eachPerson.getString("username").equals(username)) {
                if (eachPerson.keySet().contains("friends")) {
                    String listOfFriends = eachPerson.getString("friends");
                    listOfFriends += friend + "+";
                    eachPerson.put("friends", listOfFriends);
                } else {
                    eachPerson.put("friends", friend + "+");
                }
                break;
            }
        }
        writeToFile();
    }

    private void addChatToJsonFile(String username, String friendName, String newMessage) {
        for (Object val : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) val;
            if (eachPerson.getString("username").equals(username)) {
                if (eachPerson.keySet().contains("chats")) {
                    JSONObject allChats = eachPerson.getJSONObject("chats");
                    if (allChats.keySet().contains(friendName)) {
                        String prevChats = allChats.getString(friendName);
                        prevChats += newMessage + "+";
                        allChats.put(friendName, prevChats);
                        eachPerson.put("chats", allChats);
                    } else {
                        allChats.put(friendName, newMessage);
                        eachPerson.put("chats", allChats);
                    }
                } else {
                    JSONObject onePersonChat = new JSONObject();
                    onePersonChat.put(friendName, newMessage + "+");
                    eachPerson.put("chats", onePersonChat);
                }
//                Server.userJsonArray.put(eachPerson);
                break;
            }
        }
        writeToFile();
    }

    private void writeToFile() {
        try (FileWriter file = new FileWriter("D:\\android\\net\\src\\data_base.json")) {

            file.write(Server.json.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean findUserInFileWhileSignIn(String username) {
        for (Object o : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) o;
            if (eachPerson.getString("username").equals(username)) {
                return true;
            }
        }
        return false;
    }

    private boolean foundUserHasTruePass(String username, String password) {
        for (Object o : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) o;
            if (eachPerson.getString("username").equals(username)) {
                if (eachPerson.getString("password").equals(password))
                    return true;
                else return false;
            }
        }
        return false;
    }

    private String returnListOfFriends(String username) {
        String listOfFriends = "";
        for (Object o : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) o;
            if (eachPerson.getString("username").equals(username)) {
                if (JsonUtils.objectExists(eachPerson, "friends")) {
                    listOfFriends = eachPerson.getString("friends");
                }
            }
        }
        return listOfFriends;
    }

    private void changeUsernameInJson(String oldUser, String newUser) {
        for (Object val : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) val;
            if (eachPerson.getString("username").equals(oldUser)) {
                String allFile = Server.json.toString();
                allFile = allFile.replace(oldUser, newUser);
                Server.json = new JSONObject(allFile);
            }
        }
    }

    private void changPasswordInJson(String username, String newPass) {
        for (Object val : Server.userJsonArray) {
            JSONObject eachPerson = (JSONObject) val;
            if (eachPerson.getString("username").equals(username)) {
                eachPerson.put("password", newPass);
            }
        }
    }


}
