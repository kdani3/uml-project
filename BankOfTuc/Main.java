package BankOfTuc;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import BankOfTuc.User.Role;
import BankOfTuc.accounting.BankAccount;
import BankOfTuc.auth.LoginListener;
import BankOfTuc.auth.LoginManager;
import BankOfTuc.auth.PasswordUtils;
import BankOfTuc.auth.QrUtils;
import BankOfTuc.bookkeeping.CustomerFileManager;
import BankOfTuc.bookkeeping.UserFileManagement;
import BankOfTuc.bookkeeping.UsersCustomersBridge;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class Main {
    public static void main(String[] args) throws QrGenerationException, URISyntaxException, IOException {
        String filePath = "users.json";
        String customer_filePath = "customers.json";

        CustomerFileManager cfm = new CustomerFileManager(customer_filePath);
        UserFileManagement ufm = new UserFileManagement(filePath);
        UsersCustomersBridge ucb = new UsersCustomersBridge(ufm, cfm);

        User admin = new Admin("admin1", "adminpass", "Super Admin", "admin@example.com", true);
        Customer individual = new IndividualCustomer("john_doe", "password123", "John Doe","1234", "john@example.com", true);
        Customer company = new CompanyCustomer("acme_inc","secret456","ACME Inc.",  "21314", "contact@acme.com", true);



        //BankAccount account1 = new BankAccount(individual.getVatID());
        //BankAccount account2 = new BankAccount(company.getVatID());
        //BankAccount account3 = new BankAccount(company.getVatID());

        
        //individual.addBankAccount(account1);
        //company.addBankAccount(account2);
        //company.addBankAccount(account3);
        cfm.addCustomer(individual);
        cfm.addCustomer(company);

        ufm.addUser(admin);
        ufm.addUser(individual);
        ufm.addUser(company);
        //BankAccount account1 = individual.getBankAccounts().get(1);

        ucb.bridge();

//        cfm.updateCustomer(individual);

        User retrieved = ufm.getUserByUsername("john_doe");
        if (retrieved!=null)
            System.out.println("Found: " + retrieved.getUsername() + " | Role: " + retrieved.getRole());

        List<User> allUsers = ufm.getAllUsers();

        for (User u : allUsers) {
            System.out.println(u.getUsername() + " | " + u.getRole() + " | Active: " + u.getActive());
        }


        Scanner sc = new Scanner(System.in);
        LoginManager login = new LoginManager(ufm);

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
                //String password = readPasswordWithMasking();
                int result = login.login(username, password);

                switch (result) {
                    case 1 -> loggedInMenu(sc, login, username, ufm,cfm);

                    case 2 -> {
                        System.out.print("Enter TOTP code: ");
                        String code = sc.nextLine();
                        if (login.qrCodeLogin(username, code)) {loggedInMenu(sc, login, username, ufm,cfm);}
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

    private static void loggedInMenu(Scanner sc, LoginManager login, String username,UserFileManagement ufm,CustomerFileManager cfm) throws QrGenerationException, URISyntaxException {

        while (login.isLoggedIn(username)) {
            User user = ufm.getUserByUsername(username);

            System.out.println("\n--- User Menu (" + username + "/" + user.getRole() + ") ---");
            System.out.println("1. Do action (refresh activity)");
            System.out.println("2. Create Qr Code");
            System.out.println("3. Settings");
            System.out.println("4. Logout");
            System.out.print("> ");

            if(user.getRole() == Role.INDIVIDUAL){
                Customer customer = (Customer) cfm.getCustomerByUsername(username);
                BankAccount accounts = customer.getBankAccounts().get(1);
                accounts.reduceBalance(1);
                for(BankAccount account : customer.getBankAccounts()){
                    System.out.println(account.getIban() + "  " + account.getBalance());
                }
            }
            else if(user.getRole() == Role.COMPANY){
                CompanyCustomer customer = (CompanyCustomer) user;
                for(BankAccount account : customer.getBankAccounts()){
                    System.out.println(account.getIban() + "  " + account.getBalance());
                }
            }
            String input = sc.nextLine();

            //every time user interacts, update activity timestamp
            login.activity(username);

            switch (input) {
                case "1":
                    System.out.println("Some action performed!");
                    break;

                case "2":
                    if (!user.hasQR()){
                        String[] qrResult = QrUtils.createQr(username);
                        String qrUriString = qrResult[0];
                        String qrSecret = qrResult[1];

                        ConsoleImagePrinter.showQrImage(qrUriString,"Qr"); 
                        while(true){

                            System.out.println("Enter Qr Code");
                            String qrString = sc.nextLine();
                            
                            if(QrUtils.verifyQrCode(qrSecret, qrString)){
                                user.setQrCode(qrSecret);
                                System.out.println("Qr Code Created");
                                break;
                            }
                            else{
                                System.out.println("Wrong Qr Code \n Retry");
                            }
                        }
                    }
                    else {
                        System.out.println("Qr Code Already in Use");
                    }
                    break;
                case "3":
                    SettingsMenu(sc, login,user,ufm);
                case "4":
                    login.logout(username);
                    return;

                default:
                    System.out.println("Invalid option");
            }

        }
    }
    public static boolean verifyUserIdentity(User user, Scanner sc){
         System.out.println("Enter  Password:");
            String password = sc.nextLine();
            if(!PasswordUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword()))
                return false;
        
            if(user.hasQR()){
                System.out.println("Enter Qr Code");
                String qrcode = sc.nextLine();

                if(!QrUtils.verifyQrCode(user.getQrCode(), qrcode))
                    return false;
            }
            return true;

    }
    public static void SettingsMenu(Scanner sc, LoginManager login, User user,UserFileManagement ufm){
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
                case "1":  //reset password
                    if (!verifyUserIdentity(user, sc)) break;

                    while (true) {
                        System.out.println("Enter new password:");
                        String p1 = sc.nextLine();

                        System.out.println("Enter again:");
                        String p2 = sc.nextLine();

                        if (p1.equals(p2)) {
                            user.setPassword(p1);
                            ufm.updateUser(user);

                            System.out.println("Password updated!");
                            break;
                        }

                        System.out.println("Passwords do not match. Try again? (y/n)");
                        if (!sc.nextLine().equalsIgnoreCase("y")) continue;
                    }
                    break;
                
                case "2":
                    if (!verifyUserIdentity(user, sc)) break;

                    System.out.println("Enter new email");
                    String email = sc.nextLine();
                    user.setEmail(email);
                    System.out.println("Email Updated");
                    ufm.updateUser(user);
                    break;

                case "3":
                    if (!verifyUserIdentity(user, sc)) break;

                    System.out.println("Enter new username");
                    String username = sc.nextLine();
                    user.setUsername(username);
                    System.out.println("username Updated");
                    ufm.updateUser(user);
                    break;
                
                case "4":
                    return;


            }
        }     
    }

}

