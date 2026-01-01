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
import BankOfTuc.Logging.TransactionHistoryService.TransactionEntry;
import BankOfTuc.Payments.CustomerPaymentService;
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
            System.out.println("6. Remove User");
            System.out.println("7. Logout");


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
                    //boolean transact = TransferCLI.TransferMenu(sc,login,customer,cfm);
                    //if(transact)
                     //   break;
                    CustomersManagement(sc, login, admin, ufm, cfm);
                    break;
 
                case "2":
                    System.out.println("Enter Customer username or VatID to manage payments:");
                    System.out.print("> ");
                    String payQuery = sc.nextLine();
                    
                    // Αναζήτηση πελάτη (όπως και στο case 1)
                    boolean isNum = payQuery.matches("-?\\d+(\\.\\d+)?");
                    Customer payCust = isNum 
                        ? cfm.getCustomerbyVatid(payQuery) 
                        : cfm.getCustomerByUsername(payQuery);

                    if (payCust != null) {
                        System.out.println("Managing payments for: " + payCust.getFullname());
                        // Τώρα μπορούμε να καλέσουμε τη μέθοδο
                        PaymentCLI.managePayments(sc, payCust, cfm);
                    } else 
                        System.out.println("Customer not found.");
                    
                    break;
                case "3":
                    System.out.println("Enter Customer username or VatID to perform transfers:");
                    System.out.print("> ");
                    String transQuery = sc.nextLine();
                    
                    // Αναζήτηση πελάτη
                    boolean isNumTrans = transQuery.matches("-?\\d+(\\.\\d+)?");
                    Customer transCust = isNumTrans 
                        ? cfm.getCustomerbyVatid(transQuery) 
                        : cfm.getCustomerByUsername(transQuery);

                    if (transCust != null) {
                        System.out.println("Initiating Transfer Menu for: " + transCust.getFullname());
                        
                        // Κλήση του υπάρχοντος TransferCLI
                        // Περνάμε το login του admin (τυπικά), αλλά το transCust είναι αυτό που μετράει
                        TransferCLI.TransferMenu(sc, login, transCust, cfm);
                    } else 
                        System.out.println("Customer not found.");
                    
                    break;
               
               case "4":
                        setTargetDate(sc, cfm);
                    break;
                    
                case "5":
                    boolean settings = CLIUtils.SettingsMenu(sc, login,user,ufm);
                    if(settings)
                        break;
                case "6": 
                    removeUser(sc, username, ufm, cfm);
                    break;
                case "7":
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
        String searchInput = sc.nextLine();
        Customer cust;
        boolean isNumeric = searchInput.matches("-?\\d+(\\.\\d+)?");

        cust = isNumeric 
            ? cfm.getCustomerbyVatid(searchInput) 
            : cfm.getCustomerByUsername(searchInput);

        if(cust == null){
            System.out.println("Could not find desired user");
            return;
        }

        System.out.println("\n--- Managing: " + cust.getFullname() + " ---");
        System.out.println("1. Reset Password");
        System.out.println("2. Edit Customer's Details");
        System.out.println("3. Edit Recurring Payments");
        System.out.println("4. Customer's Transfers ");
        System.out.println("5. Return");

        System.out.print("> ");
        String select = sc.nextLine();
        switch (select){
            case "1":
                String p = CLIUtils.generateUnicodePassword(8);
                User userToUpdate = ufm.getUserByUsername(cust.getUsername());
                if(userToUpdate == null) {
                    System.out.println("Error: User found in customers.json but not in users.json!");
                    break;
                }
                //User custUser = (User) cust;
                userToUpdate.setPassword(p);
                ufm.updateUser(userToUpdate);
                System.out.println("New user password: "+p);
                break;
            case "2":
                editCustomerDetail(sc, cust, cfm, ufm);
                break;
            case "3":
                PaymentCLI.manageRecurringPayments(sc, cust, cfm);
                break;
            case "4":
                viewCustomerTransfers(cust, cfm);
                break;
            case "5":
                break;
            default:
                System.out.println("Invalid selection");
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
        String select = sc.nextLine();

        User linkedUser = ufm.getUserByUsername(customer.getUsername());

        if(linkedUser == null) {
            System.out.println("Error: Could not find linked user in users.json!");
            return;
        }

        switch (select) {
            case "1" -> {
                // Username Change
                String newVal = getNewValue(sc, "Username", customer.getUsername());
                // Ενημέρωση μνήμης και στα δύο αντικείμενα
                customer.setUsername(newVal);
                linkedUser.setUsername(newVal);
                // Αποθήκευση
                saveChanges(customer, linkedUser, cfm, ufm);
            }
            case "2" -> {
                // Fullname Change
                String newVal = getNewValue(sc, "Fullname", customer.getFullname());
                customer.setFullname(newVal);
                linkedUser.setFullname(newVal);
                saveChanges(customer, linkedUser, cfm, ufm);
            }
            case "3" -> {
                // Email Change
                String newVal = getNewValue(sc, "Email", customer.getEmail());
                customer.setEmail(newVal);
                linkedUser.setEmail(newVal);
                saveChanges(customer, linkedUser, cfm, ufm);
            }
            case "4" -> {
                // Return
            }
            default -> System.out.println("Invalid selection");
        }
    }

    // Βοηθητική μέθοδος για την ανάγνωση της νέας τιμής (αντικαθιστά την updateField για πιο καθαρό έλεγχο)
    private static String getNewValue(Scanner sc, String fieldName, String currentVal) {
        System.out.println("Current " + fieldName + ": " + currentVal);
        System.out.println("Enter new " + fieldName + ": ");
        System.out.print("> ");
        return sc.nextLine();
    }

    // Βοηθητική μέθοδος για την αποθήκευση και στα δύο αρχεία
    private static void saveChanges(Customer customer, User linkedUser, CustomerFileManager cfm, UserFileManagement ufm) {
        // 1. Αποθήκευση στο customers.json
        cfm.updateCustomer(customer);
        
        // 2. Αποθήκευση στο users.json (χρησιμοποιώντας το linkedUser που έχει το σωστό ID)
        ufm.updateUser(linkedUser);
    }

    private static void setTargetDate(Scanner sc, CustomerFileManager cfm) { // <-- Προσθήκη cfm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        System.out.println("Enter the target date for simulation (dd-MM-yyyy): ");
        System.out.print("> ");
        
        String targetDateInput = sc.nextLine().trim();
        
        LocalDate targetDate = null;
        try {
            targetDate = LocalDate.parse(targetDateInput, formatter);
            System.out.println("Target date for simulation is: " + targetDate);
        } catch (Exception e) {
            System.out.println("Invalid date format. Please enter the date in DD-MM-YYYY format.");
            return;
        }

        LocalDate currentDate = timeService.today();

        if (targetDate.isAfter(currentDate)) {
            System.out.println("Target date is after the current date, simulating time...");
            simulateToTargetDate(targetDate, cfm); // <-- Πέρασμα του cfm
        } else {
            System.out.println("Target date is in the past or today. No simulation needed.");
        }
    }

    private static void simulateToTargetDate(LocalDate targetDate, CustomerFileManager cfm) { // <-- Προσθήκη cfm
        LocalDate currentDate = timeService.today();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        if (!timeService.isSimulated()) {
            timeService.startSimulation();
        }

        // Αρχικοποίηση του Service για έλεγχο πληρωμών
        CustomerPaymentService payService;
        try {
            // Χρησιμοποιούμε ένα dummy ID γιατί ο scheduler φορτώνει ΟΛΕΣ τις πληρωμές
            payService = new CustomerPaymentService("ADMIN_SIM", cfm);
        } catch (IOException e) {
            System.out.println("Error initializing payment service: " + e.getMessage());
            return;
        }

        System.out.println("Simulation started. Advancing time...");

        while (currentDate.isBefore(targetDate)) {
            timeService.advanceHours(1);
            
            // Κάθε μέρα στις 08:00 το πρωί εκτελούμε τις πάγιες εντολές
            if (timeService.now().getHour() == 8) {
                currentDate = timeService.today();
                System.out.println("New Day: " + currentDate + " - Checking recurring payments...");
                
                try {
                    // Αυτή η μέθοδος καλεί το scheduler.dailyCheck() που ελέγχει ΟΛΕΣ τις πληρωμές
                    payService.processDuePayments();
                } catch (IOException e) {
                    System.out.println("Error processing payments: " + e.getMessage());
                }
            }
            
            currentDate = timeService.today();
        }

        System.out.println("Target date " + targetDate + " reached. Stopping simulation.");
        System.out.println("Current Simulated Time: " + timeService.now().format(formatter));
    }

    /**
     * View Customer's Transfers/History
     * Uses TransactionHistoryService to fetch data.
     */
    private static void viewCustomerTransfers(Customer cust, CustomerFileManager cfm) {
        System.out.println("\n--- Transfer History for " + cust.getFullname() + " (" + cust.getVatID() + ") ---");
        
        try {
            // Fetch history using the existing service
            List<TransactionEntry> history = TransactionHistoryService.getHistoryForCustomer(cust.getVatID(), cfm);

            if (history.isEmpty()) {
                System.out.println("No transfers found for this customer.");
            } else {
                // Print Table Header
                System.out.printf("%-20s %-15s %-25s %-15s %-30s%n", 
                    "DATE", "TYPE", "COUNTERPARTY", "AMOUNT", "DETAILS");
                System.out.println("-------------------------------------------------------------------------------------------------------------");
                
                // Print Rows
                for (TransactionEntry entry : history) {
                    // Filter: Get ONLY transfers (and not bill payments):
                    // if (entry.type.equals("PAYMENT")) continue; 
                    // However, usually 'Transfers' in a broad sense includes payments too.

                    System.out.printf("%-20s %-15s %-25s %-15s %-30s%n",
                        entry.datetime,
                        entry.type,
                        entry.counterpartyName.length() > 24 ? entry.counterpartyName.substring(0, 21) + "..." : entry.counterpartyName,
                        entry.getAmountDisplay(), // Returns formatted string like "+50.00" or "-20.00"
                        entry.details.length() > 29 ? entry.details.substring(0, 26) + "..." : entry.details
                    );
                }
                System.out.println("-------------------------------------------------------------------------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("Error fetching customer history: " + e.getMessage());
        }
    }
    private static void removeUser(Scanner sc, String currentUsername, UserFileManagement ufm, CustomerFileManager cfm) {
        System.out.println("Enter username to delete:");
        System.out.print("> ");
        String usernameDel = sc.nextLine().trim();

        // Αποτροπή διαγραφής του εαυτού του
        if (usernameDel.equals(currentUsername)) {
            System.out.println("Error: You cannot delete your own account while logged in.");
            return;
        }

        User userDel = ufm.getUserByUsername(usernameDel);
        if (userDel == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("Are you sure you want to delete user '" + usernameDel + "' and all associated data? (Y/N)");
        System.out.print("> ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        // 1. Διαγραφή από customers.json (αν υπάρχει)
        Customer custDel = cfm.getCustomerByUsername(usernameDel);
        if (custDel != null) {
            try {
                cfm.deleteCustomer(custDel);
                System.out.println("Associated customer data removed.");
            } catch (IOException e) {
                System.out.println("Error removing customer data: " + e.getMessage());
            }
        }

        // 2. Διαγραφή από users.json
        try {
            boolean deleted = ufm.deleteUser(userDel.getid());
            if (deleted) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("Failed to delete user.");
            }
        } catch (IOException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
}

