package BankOfTuc;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import BankOfTuc.auth.LoginListener;
import BankOfTuc.auth.LoginManager;
import BankOfTuc.auth.PasswordUtils;
import BankOfTuc.auth.QrUtils;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class Main {
    public static void main(String[] args) throws QrGenerationException, URISyntaxException {
        String filePath = "users.json";
        JSONUtils.setFilePath(filePath);

        User admin = new Admin("admin1", "adminpass", "Super Admin", "admin@example.com", true);
        Customer individual = new IndividualCustomer("john_doe", "password123", "John Doe","1234", "john@example.com", true);
        Customer company = new CompanyCustomer("acme_inc","secret456","ACME Inc.",  "21314", "contact@acme.com", true);

        JSONUtils.addUser(admin);
        JSONUtils.addUser(individual);
        JSONUtils.addUser(company);

        User retrieved = JSONUtils.getUserByUsername("john_doe");
        System.out.println("Found: " + retrieved.username + " | Role: " + retrieved.role);

        List<User> allUsers = JSONUtils.loadUsers();
        for (User u : allUsers) {
            System.out.println(u.username + " | " + u.role + " | Active: " + u.isActive);
        }



        Scanner sc = new Scanner(System.in);
        LoginManager login = new LoginManager();

        login.addListener(new LoginListener() {
            @Override public void onLogin(String u) {
                System.out.println("[+] " + u + " logged in");
            }

            @Override public void onLogout(String u) {
                System.out.println("[*] " + u + " logged out");
            }

            @Override public void onTimeout(String u) {
                System.out.println("[!] " + u + " logged out due to inactivity");
            }
        });

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Quit");
            System.out.print("> ");

            String choice = sc.nextLine();

            if (choice.equals("1")) {

                System.out.print("Enter username: ");
                String username = sc.nextLine();

                System.out.print("Enter password: ");
                String password = sc.nextLine();

                int result = login.login(username, password);

                switch (result) {
                    case 1 -> loggedInMenu(sc, login, username);

                    case 2 -> {
                        System.out.print("Enter TOTP code: ");
                        String code = sc.nextLine();
                        if (login.qrCodeLogin(username, code)) {loggedInMenu(sc, login, username);}
                    }

                    case 3 -> System.out.println("User already logged in");

                    default -> System.out.println("Wrong username or password");
                }


            } else if (choice.equals("2")) {
                System.out.println("Goodbye!");
                sc.close();
                System.exit(0);
            } 
        }
    }

    private static void loggedInMenu(Scanner sc, LoginManager login, String username) throws QrGenerationException, URISyntaxException {

        while (login.isLoggedIn(username)) {
            User user = JSONUtils.getUserByUsername(username);

            System.out.println("\n--- User Menu (" + username + "/" + user.role + ") ---");
            System.out.println("1. Do action (refresh activity)");
            System.out.println("2. Create Qr Code");
            System.out.println("3. Settings");
            System.out.println("4. Logout");
            System.out.print("> ");

            String input = sc.nextLine();

            //every time user interacts, update activity timestamp
            login.activity(username);

            switch (input) {
                case "1":
                    System.out.println("Some action performed!");
                    break;

                case "2":
                    String qr = user.getQrCode();
                    if (qr == null || qr.trim().isEmpty() || qr.trim().equalsIgnoreCase("null")){
                        String[] qrResult = QrUtils.createQr(username);
                        String qrUriString = qrResult[0];
                        String qrSecret = qrResult[1];

                        ConsoleImagePrinter.showQrImage(qrUriString,"Qr"); 

                        System.out.println("Enter Qr Code");
                        String qrString = sc.nextLine();
                        
                        if(QrUtils.verifyQrCode(qrSecret, qrString)){
                            user.setQrCode(qrSecret);
                            System.out.println("Qr Code Created");
                        }
                        else{
                            System.out.println("Wrong Qr Code");
                        }
                    }
                    else {
                        System.out.println("Qr Code Already in Use");
                    }
                    break;
                case "3":
                    SettingsMenu(sc, login,user);
                case "4":
                    login.logout(username);
                    return;

                default:
                    System.out.println("Invalid option");
            }

        }
    }
    public static boolean userAuthenticate(User user, Scanner sc){
         System.out.println("Enter  Password:");
            String password = sc.nextLine();
            if(!PasswordUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword()))
                return false;
        
            if(user.getQrCode()!=null){
                System.out.println("Enter Qr Code");
                String qrcode = sc.nextLine();

                if(!QrUtils.verifyQrCode(user.getQrCode(), qrcode))
                    return false;
            }
            return true;

    }
    public static void SettingsMenu(Scanner sc, LoginManager login, User user){
        while (login.isLoggedIn(user.getUsername())) {

            System.out.println("\n--- Settings Menu (" + user + ") ---");
            System.out.println("1. Reset Password");
            System.out.println("2. Change Email");
            System.out.println("3. Change Usermame");
            System.out.println("4. Return");
            System.out.print("> ");

            String input = sc.nextLine();

            login.activity(user.getUsername());

            switch(input) {
                case "1": 
                    System.out.println("Enter Current Password:");
                    

                            while(true){
                                System.out.println("Verification Complete \n Please Enter new Password");
                                String attempt1 = sc.nextLine();
                                System.out.println("Enter password again");
                                String attempt2 = sc.nextLine();

                                if(attempt1.equals(attempt2)){
                                    user.setPassword(attempt1);
                                }
                                else{
                                    System.out.println("Passwords Do Not Match \n  Would you like to => \n 1. Try again \n 2. Return to Settings Menu");
                                    String choice = sc.nextLine();
                                    switch (choice) {
                                        case "1":
                                            continue;  
                                    
                                        case "2":
                                            SettingsMenu(sc, login, user);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                  
            }

}

