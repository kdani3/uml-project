package BankOfTuc;

import BankOfTuc.Bookkeeping.UserFileManagement;
import dev.samstevens.totp.exceptions.QrGenerationException;

import java.io.IOException;
import java.net.URISyntaxException;

public class FallbackAdminCreation {
//In case you've been FOOed and there is no admin user, create one
    public static void main(String[] args) throws IOException {
        String filePath = "data/users.json";
        UserFileManagement ufm = UserFileManagement.getInstance(filePath);
        for (int i = 0; i <=4 ; i++) {
                User admin = new Admin("admin" + i, "adminpass" + i, "Super Admin " + i, "admin" + i + "@example.com", true);
                System.out.println("Created fallback admin user: " + admin.getUsername() + " with password: " + "adminpass" + i);
                ufm.addUser(admin);
        }
    }
}
