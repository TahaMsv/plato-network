import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {

    private static Socket socket;
    private static DataInputStream dis;
    private static DataOutputStream dos;
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
                } else if (message.startsWith("gameHangmanRank")){
                    gameHangmanRank(message);
                } else if (message.startsWith("gameLeaderBoard")) {
                    gameLeaderBoard(message);
                } else if (message.startsWith("chosenWordHangMan")) {
                    onePlayerChoseTheWordForHangman(message);
                } else if (message.startsWith("waitingRoomForHangman")) {
                    waitToStartGame();
                } else if (message.startsWith("HangmanWinMsg")) {
                    winRoundOfHangman(message);
                } else if (message.startsWith("hangmanChooserWait")){
                    hangmanChooserWait();
                }
                else if(message.startsWith("chatList")){
                    chatList(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chatList(String message) {
        currUser=getAppUserByUsername(message.substring(8));
        String chatListString=currUser.getChatList();
        try {
            String serverMessage="a" + chatListString;
            System.out.println(serverMessage);
            dos.writeUTF(serverMessage);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String checkSignUpValidation(String mess) {
        boolean isUsernameOk = false;
        String username = mess.substring(15);
        while (!isUsernameOk) {
            Set allUsers = Server.users.keySet();
            Set<String> usernames = new HashSet<>();
            for (Object allUser : allUsers) {
                AppUsers appUsers = (AppUsers) allUser;
                usernames.add(appUsers.getUsername());
            }
            if (usernames.contains(username)) {
                String errorMessage = "err: UserName already exists... please enter another one: ";
                try {
                    dos.writeUTF(errorMessage);
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                String okMessage = "correctUsername";
                try {
                    dos.writeUTF(okMessage);
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isUsernameOk = true;
        }
        return username;
    }

    public void signUp(String username, String password) {
        currUser = new AppUsers(username, password);
        Server.users.put(currUser, this);
        System.out.println("username : " + username + " password : " + password);

    }

    public void signIn() {
        String username = "", password = "";
        try {
            username = ClientHandler.dis.readUTF();
            password = ClientHandler.dis.readUTF();
            System.out.println("user " + username + " " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String answer = getAppUser(username, password);
        try {
            ClientHandler.dos.writeUTF(answer);
            ClientHandler.dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getAppUser(String name, String password) {
        Set keySets = Server.users.keySet();
        for (Object keySet : keySets) {
            AppUsers appUsers = (AppUsers) keySet;
            if (appUsers.getUsername().equals(name)) {
                if (appUsers.getPassword().equals(password)) {
                    currUser = appUsers;
                    return "okSignIn";
                } else
                    return "wrongPassword";
            }
        }
        return "wrongUsername";
    }

    private void addFriend(String clMessage) {
        System.out.println("at add friend");
        String[] addFriendParts = clMessage.split("\\+");
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
//                addNewFriendsToFile(currentAppUser.getUsername(), friendUsername);
                return "ok " + appUser.getUsername() + " added to your friends successfully";
            }
        }
        return "err " + friendUsername + " not found";
    }

    private String changeUsername(String clMsg) {
        String[] splitedString = clMsg.split("\\+");
        currUser = getAppUserByUsername(splitedString[1]);
        String newUsername = splitedString[2];
//        changeTheUsernameInFile(currentAppUser.getUsername(), newUsername);
        currUser.setUsername(newUsername);
        return newUsername;
    }

    private String changePassword(String clMsg) {
        String [] splitedStr = clMsg.split("\\+");
        currUser = getAppUserByUsername(splitedStr[1]);
        String newPassword = splitedStr[2];
//        changeUserPasswordInFile(currentAppUser.getUsername(), password, newPassword);
        currUser.setPassword(newPassword);
        return newPassword;
    }

    private void friendList(String clMsg) {
        currUser = getAppUserByUsername(clMsg.substring(10));
        System.out.println(currUser.friendsStringListString());
        try {
            String serverMessage="a+" + currUser.friendsStringListString();
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
        String jafar = currUser.getAllMessages().get(friend);
        System.out.println(jafar);
        try {
            if (jafar != null) {
                System.out.println(171);
                dos.writeUTF(jafar);
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
        String[] data = clMsg.split("\\+");  // data[1]=sender ,  data[2]=receiver, data[3]=message
        currUser = getAppUserByUsername(data[1]);
        String currentMessage = data[1] + "," + data[2] + "," + data[3];
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
            }
        }
    }

    private AppUsers getAppUserByUsername(String username){
        Set users = Server.users.keySet();
        for (Object o:users) {
            AppUsers a = (AppUsers) o;
            if (a.getUsername().equals(username)){
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
                        if (currUser.getUsername().compareTo(appUser.getUsername()) > 0){
                            dos.writeUTF("startXO" + "1");
                        } else{
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
                        }
                        else if (timesOfPlay == 1){
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
        while (wordHangman.equals(""));
        try {
            dos.writeUTF("hangmanWordPlay" + wordHangman);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void winRoundOfHangman(String clMsg){
        String winnerUsername = clMsg.substring(13);
        currUser = getAppUserByUsername(winnerUsername);
        currUser.setHangmanScore();
        wordHangman = "";
        ///
    }

    private void hangmanChooserWait(){
        while (!wordHangman.equals(""));
        try {
            dos.writeUTF("finishedPlayingHangman");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gameLeaderBoard(String clMsg){
        System.out.println(clMsg);
    }

}
