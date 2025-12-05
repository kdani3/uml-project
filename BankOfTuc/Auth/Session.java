package BankOfTuc.Auth;
//Session Class for activity and timing
public class Session {
    public  String username;
    private long lastActivity;


    public Session(String username) {
        this.username = username;
        touch(); // set current time
    }
    public Session() {}

    public void touch() {
        lastActivity = System.currentTimeMillis();
    }

    public long getLastActivity() {
        return lastActivity;
    }


    public String getUsername() {
        return username;
    }

}
