package BankOfTuc.Bookkeeping;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import BankOfTuc.User;

public class UserFileManagement {

    private String filePath;

    private final Gson gson;
    private UserStore store;
    private ArrayList<User> users = new ArrayList<>();

    private static volatile UserFileManagement instance;

    private UserFileManagement(String filePath) throws IOException {
        this.filePath = filePath;
        this.gson = GsonProvider.get();
        
        load();
    }

    public static UserFileManagement getInstance(String filePath) throws IOException {
        if (instance == null) {
            synchronized (UserFileManagement.class) {
                if (instance == null) {
                    instance = new UserFileManagement(filePath);
                }
            }
        }
        return instance;
    }

    public static UserFileManagement getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserFileManagement not initialized. Call getInstance(filePath) first.");
        }
        return instance;
    }

    public UserStore getStore() {
        return store;
    }

    public ArrayList<User> getUsers (){
        return users;
    }

     
    private void load() {
        try (FileReader reader = new FileReader(filePath)) {
            store = gson.fromJson(reader, UserStore.class);

            //handle nulls in JSON
            if (store == null || store.users == null) {
                store = new UserStore();
            }
    
        } catch (IOException e) {
            //file doesn't exist or can't be read — initialize empty store
            store = new UserStore();
        }
    }

    private  void save()  {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(store, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserById(int id) {
        if (id < 0 || id >= store.users.size()) return null;
        return store.users.get(id);
    }

    public User addUser(User user)  {
    //check if user already exists by username
        for (User u : store.users) {
            if (u.getUsername().equals(user.getUsername()) || u.getFullname().equals(user.getFullname()) || u.getEmail().equals(user.getEmail())) {
                return u; 
            }
        }
        // Assign ID = index
        user.setid(store.users.size());
        store.users.add(user);

        save();
        return user;
    }

    public  boolean updateUser(User user) {
        int id = user.getid();
        if (id < 0 || id >= store.users.size()) return false;
        store.users.set(id, user);
        save();
        return true;
    }

    public boolean deleteUser(int id) throws IOException {
        if (id < 0 || id >= store.users.size()) return false;
        store.users.remove(id);

        // Reindex IDs
        for (int i = 0; i < store.users.size(); i++) {
            store.users.get(i).setid(i); 
        }

        save();
        return true;
    }

    public  List<User> getAllUsers() {
        return store.users;
    }

    public int getMaxId() {
        return store.users.size() - 1;
    }

    public  User getUserByUsername(String username) {
        if (username == null) return null;

        for (User u : store.users) {
            if (username.equals(u.getUsername())) {
                return u;
            }
        }
        return null; 
    }


    public  User getUserByEmail(String email) {
        if (email == null) return null;

        for (User u : store.users) {
            if (email.equals(u.getEmail())) {
                return u;
            }
        }
        return null; 
    }
    

}
