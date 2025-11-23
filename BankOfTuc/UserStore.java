package BankOfTuc;

import java.util.ArrayList;

public class UserStore {
    public ArrayList<User> users = new ArrayList<>();

    public static User getUserByUsername(UserFileManagement um, String username) {
    if (username == null) return null;

    for (User u : um.getAllUsers()) {
        if (username.equals(u.getUsername())) return u;
    }
    return null;
}
}
