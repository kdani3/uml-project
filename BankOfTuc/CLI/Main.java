package BankOfTuc.CLI;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import BankOfTuc.Auth.LoginListener;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.CompanyCustomer;
import BankOfTuc.EmailUtils;
import BankOfTuc.EnvReader;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.User.Role;
//import BankOfTuc.bookkeeping.UsersCustomersBridge;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class Main {
        static Thread currentTimerThread = null;
        static boolean timerRunning = false;

    public static void main(String[] args) throws QrGenerationException, URISyntaxException, IOException {
        String filePath = "data/users.json";
        String customer_filePath = "data/customers.json";

        CustomerFileManager cfm = new CustomerFileManager(customer_filePath);
        UserFileManagement ufm = new UserFileManagement(filePath);
        //UsersCustomersBridge ucb = new UsersCustomersBridge(ufm, cfm);

       /*  User admin = new Admin("admin1", "adminpass", "Super Admin", "admin@example.com", true);
        Customer individual = new IndividualCustomer("john_doe", "password123", "John Doe","1234", "kntanakas@tuc.gr", true);
        Customer company = new CompanyCustomer("acme_inc","secret456","ACME Inc.",  "21314", "contact@acme.com", true);

        BankAccount indAccount = new BankAccount(individual.getVatID());
        BankAccount companyAccount = new BankAccount(company.getVatID());
 */
/*         indAccount.addBalance(400);

        individual.addBankAccount(indAccount);
        company.addBankAccount(companyAccount);

 
        cfm.addCustomer(individual);
        cfm.addCustomer(company);

        ufm.addUser(admin);
        ufm.addUser(individual);
        ufm.addUser(company);
 */
        //ucb.bridge();

       /*  User retrieved = ufm.getUserByUsername("john_doe");
        if (retrieved!=null)
            System.out.println("Found: " + retrieved.getUsername() + " | Role: " + retrieved.getRole());

        List<User> allUsers = ufm.getAllUsers(); */
/* 
        for (User u : allUsers) {
            System.out.println(u.getUsername() + " | " + u.getRole() + " | Active: " + u.getActive());
        }
 */

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

        CLIUtils.asciiLogo();
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Quit");
            System.out.println("3. Reset password");
            System.out.println("4. Create Account");
            System.out.print("> ");

            String choice = sc.nextLine();

            if (choice.equals("1")) {

                System.out.print("Enter username: ");
                String username = sc.nextLine();

                System.out.print("Enter password: ");
                String password = sc.nextLine();

                int result = login.login(username, password);

                switch (result) {
                    case 1 -> loggedInMenu(sc, login, username, ufm,cfm);

                    case 2 -> {
                        System.out.print("Enter TOTP code: ");
                        String code = sc.nextLine();
                        if (login.qrCodeLogin(username, code)) {loggedInMenu(sc, login, username, ufm,cfm);}
                    }

                    case 3 -> {
                        System.out.println("User already logged in");
                    }

                    case 5 -> {
                        System.out.println("Account is deactivated.\nPlease contact our support.");
                    }

                    default -> System.out.println("Wrong username or password");
                }


            } else if (choice.equals("2")) {
                System.out.println("Goodbye!");
                sc.close();
                System.exit(0);
            } else if(choice.equals("3")){
                resetPasswordMenu(sc, ufm);
            } else if(choice.equals("4")){
                createUser(sc,ufm,cfm);
            }
        }
    }

    public static void createUser(Scanner sc, UserFileManagement ufm, CustomerFileManager cfm){
        System.out.println("Enter FullName (i.e. Ilias Bouras)");
        System.out.print("> ");
        String fullname = sc.nextLine().trim();
        
        if (!fullname.matches("^[a-zA-Z]+( [a-zA-Z]+)+$")) {
            System.out.println("Invalid Name");
            return;
        }

        fullname = Arrays.stream(fullname.split("\\s+"))
                        .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));
        
        System.out.println("Enter Vat ID");
        System.out.print("> ");
        String vatid = sc.nextLine().trim();

        if (!vatid.matches("[0-9]+") || vatid.length()>9) {
            System.out.println("Invalid Vat ID");
            return;
        }

        if(cfm.getCustomerbyVatid(vatid)!=null){
            System.out.println("Already Have an Account");
            return;
        }

        System.out.println("Enter Customer Type");
        System.out.println("1. INDIVIDUAL");
        System.out.println("2. COMPANY");
        System.out.print(">");

        String roledec = sc.nextLine().trim();
        int roleint = Integer.parseInt(roledec);

        if(roleint>2 || roleint<1){
            System.out.println("Invalid decision");
            return;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        String email;

        while(true) {
            System.out.println("Enter Email (optional)");
            System.out.print(">");
            email = sc.nextLine().trim();
        
            if (email.isEmpty()) {
                System.out.println("Without an email you will not be able to reset password\n or receive updates.\nContinue without an email?\n(Y)es or (N)o");
                String ch = sc.nextLine();
                if (ch.equalsIgnoreCase("y")) {
                    break; 
                }
            } 
            else if (!email.matches(emailRegex)) {
                System.out.println("Error: '" + email + "' is not a valid email format. Please try again.");
            } 
            else {
                System.out.println("Email accepted.");
                break;
            }
        }

        String username;

        while(true){
            System.out.println("Enter desired username");
            System.out.print(">");
            username = sc.nextLine().trim();

            if(ufm.getUserByUsername(username)!=null){
                System.out.println("Unavailable username.\nTry another one");
                continue;
            }
            else{
                break;
            }
        }
        String password;

        while (true) {
            System.out.println("Enter desired password.\nThe length must be at least 8 characters");
            password= sc.nextLine().trim();

            if(password.equals(username)){
                System.out.println("The username cannot be used password");
                continue;
            }
            
            if(password.length()<8){
                System.out.println("The length must be at least 8 characters");
                continue;
            }
            else{
                System.out.println("Retype Password");
                String rePassword = sc.nextLine().trim();
                if(rePassword.equals(password)){
                    System.out.println("Password set correctly");
                    break;
                }
                else{
                    System.out.println("Passwords do not match");
                    continue;
                }
            }
        }
        if(roleint==1){
            IndividualCustomer customer = new IndividualCustomer(username, password, fullname, vatid, email, true);
            ufm.addUser(customer);
            cfm.addCustomer(customer);
        }
        else{
            CompanyCustomer customer = new CompanyCustomer(username, password, fullname, vatid, email, true);
            ufm.addUser(customer);
            cfm.addCustomer(customer);
        }
        System.out.println("Account Created");
        return;
    }
    public static void loggedInMenu(Scanner sc, LoginManager login, String username,UserFileManagement ufm,CustomerFileManager cfm) throws QrGenerationException, URISyntaxException{
        User user = ufm.getUserByUsername(username);
        Role role = user.getRole();

        if(role.equals(Role.INDIVIDUAL)){
            IndividualCLI.loggedInMenu( sc,  login,  user, ufm, cfm);
        }
        else if(role.equals(Role.COMPANY)){
            CompanyCLI.loggedInMenu(sc, login, user, ufm, cfm);
        }
        else if(role.equals(Role.ADMIN)){
            AdminCLI.loggedInMenu(sc, login, user, ufm, cfm);
        }

    }
    

    public static void resetPasswordMenu(Scanner sc, UserFileManagement ufm) {
        System.out.println("Enter username");
        String username = sc.nextLine();
        System.out.println("If the user exists, an email will be sent for verification.");
        
        User user = ufm.getUserByUsername(username);

        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            Random rnd = new Random();
            int mailCode = 100000 + rnd.nextInt(900000);
            
            try {
                Map<String, String> env = EnvReader.loadEnv(".env");
                String api_key = env.get("MAIL_API_KEY");
                
                String html = EmailUtils.passwordResetHTML(username,mailCode);

                EmailUtils.sendEmail(api_key, "info@bankoftuc.denmoukaneito.click", "Bank Of Tuc", 
                        user.getEmail(), "Password Reset", html);
                
                System.out.println("Verification code sent to your email.");
                System.out.println("Enter the code:");
                
                String input_code = sc.nextLine();
                
                if (Integer.parseInt(input_code) == mailCode) {
                    System.out.println("Correct Code");
                    
                    while (true) {
                        System.out.println("Enter new password:");
                        String p1 = sc.nextLine();

                        System.out.println("Enter again:");
                        String p2 = sc.nextLine();

                        if (p1.equals(p2)) {
                            user.setPassword(p1);
                            ufm.updateUser(user);
                            System.out.println("Password updated successfully!");
                            break;
                            
                        } else {
                            System.out.println("Passwords don't match. Please try again.");
                        }
                    }
                } else {
                    System.out.println("Invalid verification code.");
                }
                
            } catch (IOException e) {
                System.err.println("Error sending email: " + e.getMessage());
                e.printStackTrace();

            } catch (NumberFormatException e) {
                System.err.println("Invalid code format entered.");

            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("User not found or no email associated with this account.");
        }
    }    

}

