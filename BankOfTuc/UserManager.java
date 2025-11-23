package BankOfTuc;

import java.io.IOException;

public class UserManager {
    public void updateUser(User user,UserFileManagement ufm) throws IOException{
        
        ufm.updateUser(user);
    }
}
