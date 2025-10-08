package BankOfTuc;

import java.io.*;
import java.util.*;

public class JSONUtils {

   public static void saveUsers(List<User> users, String filePath) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        bw.write("[\n");

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            bw.write(user.toJSON());

            if (i < users.size() - 1) bw.write(",");

            bw.write("\n"); // new line after each JSON object
        }

        bw.write("]");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public static List<User> loadUsers(String filePath) {
        List<User> users = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return users;

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String[] entries = content.replace("[", "").replace("]", "").split("\\},\\s*\\{");

            for (int i = 0; i < entries.length; i++) {
                String json = entries[i];
                if (!json.startsWith("{")) json = "{" + json;
                if (!json.endsWith("}")) json = json + "}";
                User u = fromJSON(json);
                if (u != null) users.add(u);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return users;
    }

    public static void addUser(User user, String filePath) {
        List<User> users = loadUsers(filePath);
        if (!UserExists(user,filePath)){ //if user doesnt exist already
            users.add(user);
            saveUsers(users, filePath);
        }
        
    }

    public static User getUserByUsername(String username, String filePath) {
        List<User> users = loadUsers(filePath);
        for (User u : users) if (u.username.equals(username)) return u;
        return null;
    }

    public static boolean UserExists(User user, String filePath){
        List<User> users = loadUsers(filePath);
        for (User u : users) 
        if (u.username.equals(user.username) //check username
        || (u.email.equals(user.email)) || (u.email.equals(user.email)) //check email
        ||  (u.fullname.equals(user.fullname))  //check fullname
        || (user instanceof Customer && u instanceof Customer //if instance of Customer check vatID
        &&  ((Customer) user).vatID.equals(((Customer) u).vatID))) {
            return true; 
        }
        return false;
    }

    // Deserialize JSON string based on role
   public static User fromJSON(String json) {
    try {
        String username = json.split("\"username\":\"")[1].split("\"")[0];
        String fullname = json.split("\"fullname\":\"")[1].split("\"")[0];
        String email = json.split("\"email\":\"")[1].split("\"")[0];
        boolean isActive = Boolean.parseBoolean(json.split("\"isActive\":")[1].split(",")[0]);
        User.Role role = User.Role.valueOf(json.split("\"role\":\"")[1].split("\"")[0]);
        String hashedPassword = json.split("\"hashedPassword\":\"")[1].split("\"")[0];
        byte[] salt = Base64.getDecoder().decode(json.split("\"saltBase64\":\"")[1].split("\"")[0]);

        switch (role) {
        case ADMIN:
            Admin a = new Admin();
            a.username = username; a.fullname = fullname; a.email = email; a.isActive = isActive;
            a.hashedPassword = hashedPassword; a.saltBase64 = Base64.getEncoder().encodeToString(salt);
            return a;
        case INDIVIDUAL:
            String vatID = json.contains("\"vatID\":\"") ? json.split("\"vatID\":\"")[1].split("\"")[0] : "";
            IndividualCustomer i = new IndividualCustomer();
            i.username = username; i.fullname = fullname; i.email = email; i.isActive = isActive;
            i.vatID = vatID;
            i.hashedPassword = hashedPassword; i.saltBase64 = Base64.getEncoder().encodeToString(salt);
            return i;
        case COMPANY:
            vatID = json.contains("\"vatID\":\"") ? json.split("\"vatID\":\"")[1].split("\"")[0] : "";
            CompanyCustomer c = new CompanyCustomer();
            c.username = username; c.fullname = fullname; c.email = email; c.isActive = isActive;
            c.vatID = vatID;
            c.hashedPassword = hashedPassword; c.saltBase64 = Base64.getEncoder().encodeToString(salt);
            return c;
        default:return null;
}

    } catch (Exception e) { e.printStackTrace(); return null; }
}

}
