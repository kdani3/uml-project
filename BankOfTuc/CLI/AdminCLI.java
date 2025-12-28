package BankOfTuc.CLI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import BankOfTuc.Admin;
import BankOfTuc.Customer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.TimeService;
import BankOfTuc.User;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Logging.TransactionHistoryService;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class AdminCLI {
    static TimeService timeService = TimeService.getInstance();
public static void loggedInMenu(Scanner sc, LoginManager login, User user,UserFileManagement ufm,CustomerFileManager cfm) throws QrGenerationException, URISyntaxException {
        String username = user.getUsername();
        while (login.isLoggedIn(username)) {

            System.out.println("\n--- Admin Menu (" + username + "/" + user.getRole() + ") ---");
            System.out.println("1. Customer Management");
            System.out.println("2. Payments Management");
            System.out.println("3. Transfers Management");
            System.out.println("4. Time Simulation");
            System.out.println("5. Settings");
            System.out.println("6. Logout");


            Admin admin = (Admin) ufm.getUserByUsername(username);            
            
            System.out.print("> ");

            String input = sc.nextLine();

            if (!login.isLoggedIn(username)) {
                System.out.println("Session timed out. Returning to main menu.");
                return;
            }

            login.activity(username);

            if (!login.isLoggedIn(username)) {
                System.out.println("Session timed out. Returning to main menu.");
                return;
            }

            switch (input) {
                case "1":
/*                     //boolean transact = TransferCLI.TransferMenu(sc,login,customer,cfm);
                    if(transact)
                        break;
 */
                case "2":
/*                     PaymentCLI.managePayments(sc, customer, cfm);
                    break;
               
*/              case "4":
                        setTargetDate();
                    break;
                    
                case "5":
                    boolean settings = CLIUtils.SettingsMenu(sc, login,user,ufm);
                    if(settings)
                        break;
                case "6":
                    login.logout(username);
                    return;
               
                default:
                    System.out.println("Invalid option");
            }

        }
    }
    
    public static void CustomersManagement(Scanner sc, LoginManager login, User user,UserFileManagement ufm,CustomerFileManager cfm)  {
        System.out.println("Enter Customer username or VatID");
        System.out.print("> ");
        String searchInput = sc.next();
        Customer cust;
        boolean isNumeric = searchInput.matches("-?\\d+(\\.\\d+)?");

        cust = isNumeric 
            ? cfm.getCustomerbyVatid(searchInput) 
            : cfm.getCustomerByUsername(searchInput);

        if(cust == null){
            System.out.println("Could not find desired user");
            return;
        }

        System.out.println("1. Reset Password");
        System.out.println("2. Edit Customer's Details");
        System.out.println("3. Edit Recurring Payments");
        System.out.println("4. Customer's Transfers ");
        System.out.println("5. Return");

        System.out.print("> ");
        String select = sc.next();
        switch (select){
            case "1":
                String p = CLIUtils.generateUnicodePassword(8);
                User custUser = (User) cust;
                custUser.setPassword(p);
                ufm.updateUser(custUser);
                System.out.println("New user password: "+p);
                break;
            case "2":
                editCustomerDetail(sc, cust, cfm, ufm);
                break;
            case "3":
                PaymentCLI.manageRecurringPayments(sc, cust, cfm);
                break;


        }

    }

    static void editCustomerDetail(Scanner sc,Customer customer,CustomerFileManager cfm,UserFileManagement ufm){
        System.out.println("What would you like to change?");
        System.out.println("1. Username");
        System.out.println("2. FullName");
        System.out.println("3. Email");
        System.out.println("4. Return");

        System.out.print("> ");
        User user  = (User) customer;
        String select = sc.next();
        switch(select) {
            case "1" -> updateField(sc, "Username", user.getUsername(), user::setUsername);
            case "2" -> updateField(sc, "Fullname", user.getFullname(), user::setFullname);
            case "3" -> updateField(sc, "Email", user.getEmail(), user::setEmail);
            default  -> {   System.out.println("Invalid selection"); 
                            return;
                        }
        }
    }

    private static void updateField(Scanner sc, String fieldName, String currentVal, Consumer<String> setter) {
    System.out.println("Current " + fieldName + ": " + currentVal);
    System.out.println("Enter new " + fieldName);
    System.out.print("> ");
    
    String newValue = sc.next();
    setter.accept(newValue);
    System.out.println(fieldName + " updated in memory.");
    }

    private static void setTargetDate() {
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Request target date input
        System.out.println("Enter the target date for simulation (dd-MM-yyyy): ");
        String targetDateInput = scanner.nextLine();
        LocalDate targetDate = null;
        try {
            // Try to parse the input string into LocalDate
            targetDate = LocalDate.parse(targetDateInput, formatter);
            System.out.println("Target date for simulation is: " + targetDate);
        } catch (Exception e) {
            // Handle invalid input format
            System.out.println("Invalid date format. Please enter the date in DD-MM-YYYY format.");
        }
        // Get the current system date (real-time)
        LocalDate currentDate = LocalDate.now();
        if(targetDate==null){
            System.out.println("Error getting target date");
            return;
        }
        // Compare the target date with current date
        if (targetDate.isAfter(currentDate)) {
            System.out.println("Target date is after the current date, simulating time...");
            simulateToTargetDate(targetDate);
        } else {
            System.out.println("Target date is in the past or today. No simulation needed.");
        }
    }

    private static void simulateToTargetDate(LocalDate targetDate) {
        LocalDate currentDate = timeService.today();
        LocalDateTime currentDateTime = timeService.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        if (!timeService.isSimulated()) {
            timeService.startSimulation();
        }
        while (currentDate.isBefore(targetDate)) {
            //timeService.advanceDays(1); 
            timeService.advanceHours(1);
            currentDate = timeService.today();
            currentDateTime = timeService.now();
            System.out.println("Simulated date: " + currentDateTime.format(formatter));

        }

        System.out.println("Target date " + targetDate + " reached. Stopping simulation.");
    }
}

