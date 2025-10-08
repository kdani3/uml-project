package BankOfTuc;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String filePath = "users.json";

        User admin = new Admin("admin1", "adminpass", "Super Admin", "admin@example.com", true);
        Customer individual = new IndividualCustomer("john_doe", "password123", "John Doe","1234", "john@example.com", true);
        Customer company = new CompanyCustomer("acme_inc","secret456","ACME Inc.",  "21314", "contact@acme.com", true);

        JSONUtils.addUser(admin, filePath);
        JSONUtils.addUser(individual, filePath);
        JSONUtils.addUser(company, filePath);

        User retrieved = JSONUtils.getUserByUsername("john_doe", filePath);
        System.out.println("Found: " + retrieved.username + " | Role: " + retrieved.role);

        List<User> allUsers = JSONUtils.loadUsers(filePath);
        for (User u : allUsers) {
            System.out.println(u.username + " | " + u.role + " | Active: " + u.isActive);
        }
    }
}
