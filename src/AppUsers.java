import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppUsers {
    private String username;
    private String password;
    private ArrayList<AppUsers> friends;
    private ArrayList<String> stringFriends;
    private Map<String,String > allMessages;
    private boolean wantToPlayXORank;
    private boolean wantToPlayHangmanRank;
    private int XOScore;
    private int HangmanScore;

    public AppUsers(String username, String password) {
        this.username = username;
        this.password = password;
        this.friends = new ArrayList<>();
        this.stringFriends = new ArrayList<>();
        this.allMessages = new ConcurrentHashMap<>();
        this.wantToPlayXORank = false;
        this.wantToPlayHangmanRank = false;
        this.XOScore = 0;
        this.HangmanScore = 0;
    }

    public Map<String, String> getAllMessages() {
        return allMessages;
    }

    public void setAllMessages(Map<String, String> allMessages) {
        this.allMessages = allMessages;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public ArrayList<AppUsers> getFriends() {
        return this.friends;
    }

    public ArrayList<String> getStringFriends() {
        return this.stringFriends;
    }

    public void addNewFriend(AppUsers friend) {
        this.friends.add(friend);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStringFriends(ArrayList<String> stringFriends) {
        this.stringFriends = stringFriends;
    }

    public void setWantToPlayHangmanRank(boolean wantToPlayHangmanRank) {
        this.wantToPlayHangmanRank = wantToPlayHangmanRank;
    }

    public void setWantToPlayXORank(boolean wantToPlayXORank) {
        this.wantToPlayXORank = wantToPlayXORank;
    }

    public boolean isWantToPlayHangmanRank() {
        return wantToPlayHangmanRank;
    }

    public boolean isWantToPlayXORank() {
        return wantToPlayXORank;
    }

    public int getXOScore() {
        return XOScore;
    }

    public int getHangmanScore() {
        return HangmanScore;
    }

    public void setXOScore() {
        this.XOScore+= 5;
    }

    public void setHangmanScore() {
        this.HangmanScore += 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUsers appUsers = (AppUsers) o;
        return this.username.equals(((AppUsers) o).username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    public ArrayList<String> friendsStringList() {
        ArrayList<String> friendString = new ArrayList<>();
        if (!friends.isEmpty()) {
            for (AppUsers val : friends) {
                friendString.add(val.getUsername());
            }
        }
        return friendString;
    }


    @Override
    public String toString() {
        String returnVal = username + " " +  password + "[";
        for (AppUsers a:friends) {
            returnVal += a.getUsername()+" ";
        }
        returnVal+="]";
        return returnVal;

    }

    public String friendsStringListString() {
        ArrayList<String> friendString = friendsStringList();
        String string = "";
        for (String val : friendString) {
            string += val + "+";
        }
        return string;
    }

    public String getChatList() {
        String chatList="";
        for (int i = 0; i <friends.size() ; i++) {
            if(allMessages.get(friends.get(i).username)!=null){
                chatList+=(friends.get(i).username+"+");
            }
        }
        return chatList;
    }
}
