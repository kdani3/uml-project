package BankOfTuc.CLI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Transfers.InterBank;
import BankOfTuc.Transfers.sepaTransfer;
import BankOfTuc.Transfers.swiftTransfer;
import BankOfTuc.Transfers.SelfTransfer;
import BankOfTuc.Transfers.Transfer;

public class TransferCLI {

    public static boolean TransferMenu(Scanner sc, LoginManager login,Customer customer ,CustomerFileManager cfm){
        System.out.println("Choose Transfer Type");
        System.out.println("\n--- Transfer Menu (" + customer.getUsername() + ") ---");
        System.out.println("1. Self-Transfer");
        System.out.println("2. InterBank");
        System.out.println("3. SWIFT");
        System.out.println("4. SEPA");
        System.out.println("5. Return");
        System.out.print("> ");

        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                TransferCLI.selfTransferCLI(customer, sc, cfm);
                break;

            case "2":
                TransferCLI.InterBankCLI(customer, sc, cfm);
                break;

            case "3":
                TransferCLI.swiftCLI(customer, sc, cfm);
                break;

            case "4":
                TransferCLI.sepaCLI(customer, sc, cfm);
                break;
        }
    return true;
    }

    public static void selfTransferCLI(Customer customer, Scanner sc,CustomerFileManager cfm){
        List<BankAccount> accounts = customer.getBankAccounts();

        if(accounts.size()==1){
            System.out.println("Invalid Transfer for single account");
            return;
        }
        for(int i=1;i<accounts.size()+1;i++){
            BankAccount account = accounts.get(i-1);
            System.out.println(i+". " + account.getIban() + "  " + account.getBalance());
        }
        System.out.println("Select Sending Account Number: ");

        System.out.print("> ");
        String sendingAccChoiceString = sc.nextLine().trim();
        int sendingChoice = Integer.parseInt(sendingAccChoiceString);

        if(sendingChoice>accounts.size()+1 || sendingChoice < 1){
            System.out.println("Invalid account number");
            return;
        }

        System.out.println("Select Receiving Account Number: ");

        System.out.print("> ");
        String receivingAccChoiceString = sc.nextLine().trim();
        int receivingChoice = Integer.parseInt(receivingAccChoiceString);

        if(receivingChoice>accounts.size()+1 || receivingChoice < 1){
            System.out.println("Invalid account number");
            return;
        }
        if(receivingChoice==sendingChoice){
            System.out.println("Cannot transfer to the same account");
            return;
        }
        BankAccount sendingSelfAccount = accounts.get(sendingChoice-1);
        BankAccount receivingSelfAccount = accounts.get(receivingChoice-1);

        System.out.println("Enter amount to send.");
        System.out.print("> "); 
        
        String amountSelfString = sc.nextLine();
        double amountSelf;

        try {
            amountSelf = Double.parseDouble(amountSelfString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            return;
        }

        if(sendingSelfAccount.getBalance()<amountSelf){
            System.out.println("The requested amount exceeds the account's amount");
            return;
        }

        SelfTransfer selfTransfer = new SelfTransfer();

        if(!selfTransfer.sendMoney(customer,sendingSelfAccount,receivingSelfAccount,cfm,amountSelf)){
            System.err.println("There has been an error.\nPlease try again later");  
            return;
        }  
        System.out.print("\tAccount "+sendingAccChoiceString);
        for(int i=0;i<5;i++){
            System.out.print(" -");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("waiting for Transaction printing has made an oopsy");
                e.printStackTrace();
            }
        }
        System.out.print(" >\t" +" Account "+receivingAccChoiceString+"\n");

        System.out.println("\nAmount transfered successfully");
    }


    public static void InterBankCLI(Customer customer, Scanner sc,CustomerFileManager cfm){
        
        List<BankAccount> accounts = customer.getBankAccounts();
        System.out.println("Select Account Number: ");
        accounts = customer.getBankAccounts();
        for(int i=1;i<accounts.size()+1;i++){
            BankAccount account = accounts.get(i-1);
            System.out.println(i+". " + account.getIban() + "  " + account.getBalance());
        }
        System.out.print("> ");
        String accChoiceString = sc.nextLine().trim();
        int accChoice = Integer.parseInt(accChoiceString);

        if(accChoice>accounts.size()+1 || accChoice < 1){
            System.out.println("Invalid account number");
            return;
        }

        //BankAccount sendingAccount = accounts.get(accChoice-1);

        System.out.println("Enter receiving IBAN for interbank");
        System.out.print("> ");
        String receivingIBAN = sc.nextLine().trim();

        if(receivingIBAN.length()!=24 || !receivingIBAN.startsWith("GR")){
            System.out.println("Wrong receiving IBAN");
            return;
        }

        System.out.println("Enter receiver's Fullname");
        System.out.print("> ");
        String receiverFullname = sc.nextLine().trim();


        System.out.println("Enter amount to send.");
        System.out.print("> ");

        String amountString = sc.nextLine();
        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            return;
        }
        if(amount>customer.getBankAccounts().get(accChoice-1).getBalance()){
             System.out.println("You don't have the capacities for that :( \n Want some free money? \n (Y)es or (N)o");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("System is Malfunctioning");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("\u001B[32mYou take the blue pill, the story ends,\nyou wake up in your bed and believe whatever you want to believe.\nYou take the red pill—you stay in Wonderland,\nand I show you how deep the rabbit hole goes.\u001B[37m");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String easter = sc.nextLine().strip();
            if(easter.equalsIgnoreCase("y")){
                System.out.println("\u001B[32mYou have to understand, most people are not ready to be unplugged.");
                    try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Purging User's memories");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Erasing Deja Vu's");
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Injecting backed up versions");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Rebooting...");
                System.exit(0);
            }
            else if(easter.equalsIgnoreCase("n")){
                System.out.println("\u001B[32mDo you think that's air you're breathing now?");
                return;
            }
        }
        System.out.println("Do you wish to enter details to the Receiver?\n(Y)es or (N)o?");
        System.out.print("> ");

        String details = "no message";
        String detailsChoice = sc.nextLine();

        if(detailsChoice.equalsIgnoreCase("y")){
            System.out.println("Please enter the details below");
            System.out.print("> ");
            details = sc.nextLine();
        }   

        System.out.println("Attempting InterBank Transaction");

        Transfer transaction = new InterBank();
        int sendAttempt = transaction.sendMoney(customer, accChoice-1, receivingIBAN ,receiverFullname, cfm, amount,details,0);
        if(sendAttempt==1){
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e.printStackTrace();
                }
            }
            System.out.print(" >\t" + transaction.getReceivingFullname()+"\n");
            System.out.println("\nTransaction was succesfully made");
        }  
        else if(sendAttempt==0){
            System.out.println("Cannot Transfer InterBank to the same Individual\nTry Self-Transfer from the Transactions Menu");
        }
        else if(sendAttempt==-1){
            System.out.println("Receiver's details didn' match\nAccount might be deactivated.");
        }
        else if(sendAttempt==-2){

           
        } else if(sendAttempt==-3){
            System.out.println("The provided Receiver details were incorrect");
        }
        else if(sendAttempt==-4){
            System.out.println("The provided IBAN was incorrect");
        }
        else if(sendAttempt==-5){
            System.out.println("Couldn't find receiver account");
        }
    }

    public static void sepaCLI(Customer customer, Scanner sc, CustomerFileManager cfm){

        List<BankAccount> accounts = customer.getBankAccounts();
        System.out.println("Select Account Number: ");

        accounts = customer.getBankAccounts();
        for(int i=1;i<accounts.size()+1;i++){
            BankAccount account = accounts.get(i-1);
            System.out.println(i+". " + account.getIban() + "  " + account.getBalance());
        }

        System.out.print("> ");
        String accChoiceString = sc.nextLine().trim();
        int accChoice = Integer.parseInt(accChoiceString);

        if(accChoice>accounts.size()+1 || accChoice < 1){
            System.out.println("Invalid account number");
            return;
        }

        System.out.println("Enter Recipient IBAN for interbank");
        System.out.print("> ");

        String receivingIBAN = sc.nextLine().trim();
        String sendCountry =sepaCountryByIBAN(receivingIBAN);

        if(sendCountry==null){
            System.out.println("Invalid IBAN for SEPA transfer.\nCheck the IBAN and try again");
            return;
        }  

        System.out.println("Enter Recipient's Fullname");
        System.out.print("> ");
        String receiverFullname = sc.nextLine().trim();


        System.out.println("Enter amount to send.");
        System.out.print("> ");

        String amountString = sc.nextLine();
        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            return;
        }

        if(amount>customer.getBankAccounts().get(accChoice-1).getBalance()){
            System.out.println("Desired amount exceeds selected account's balance");
        }

        System.out.println("Do you wish to enter details to the Receiver?\n(Y)es or (N)o?");
        System.out.print("> ");

        String details = "no message";
        String detailsChoice = sc.nextLine();

        if(detailsChoice.equalsIgnoreCase("y")){
            System.out.println("Please enter the details below");
            System.out.print("> ");
            details = sc.nextLine();
        }
        
        int feechoice;
        while (true) {
            System.out.println("Choose Fees:\n1.SENDER\n2.SHARED\n3.RECIPIENT");
            System.out.print("> ");

            String choice = sc.nextLine();

            feechoice = Integer.parseInt(choice);
            if(feechoice>=1 || feechoice<=3)
                break;
        }
      
        Transfer transfer = new sepaTransfer();
        int send  = transfer.sendMoney(customer, accChoice-1, receivingIBAN, receiverFullname, cfm, amount, details,feechoice);
        if(send==0){
            System.out.println("You attempted a SEPA transfer for your own account.\nPlease use the Self Transfer tab");
            return;
        }
        if(send==-1){
            System.out.println("Your balance bounced");
            return;
        }
        if(send==1){
            System.out.println("\t Greece \t\t\t"+sendCountry);
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e.printStackTrace();
                }
            }
            
            System.out.print(" >\t" + receiverFullname+"\n");
            System.out.println("\nTransaction was succesfully made");

        }
    }

    public static void swiftCLI(Customer customer, Scanner sc, CustomerFileManager cfm){

        List<BankAccount> accounts = customer.getBankAccounts();
        System.out.println("Select Account Number: ");

        accounts = customer.getBankAccounts();
        for(int i=1;i<accounts.size()+1;i++){
            BankAccount account = accounts.get(i-1);
            System.out.println(i+". " + account.getIban() + "  " + account.getBalance());
        }

        System.out.print("> ");
        String accChoiceString = sc.nextLine().trim();
        int accChoice = Integer.parseInt(accChoiceString);

        if(accChoice>accounts.size()+1 || accChoice < 1){
            System.out.println("Invalid account number");
            return;
        }

        System.out.println("Enter Recipient IBAN for interbank");
        System.out.print("> ");

        String receivingIBAN = sc.nextLine().trim();
        String sendCountry =swiftCountryByIBAN(receivingIBAN);

        if(sendCountry==null){
            System.out.println("Invalid IBAN for SWIFT transfer.\nCheck the IBAN and try again");
            return;
        }  

        if(isSEPAByCountry(sendCountry)){
            System.out.println("The provided IBAN supports SEPA.\nPlease transfer by SEPA");
            return;
        }
        System.out.println("Enter Recipient's Fullname");
        System.out.print("> ");
        String receiverFullname = sc.nextLine().trim();


        System.out.println("Enter amount to send.");
        System.out.print("> ");

        String amountString = sc.nextLine();
        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            return;
        }


        if(amount>customer.getBankAccounts().get(accChoice-1).getBalance()){
            System.out.println("Desired amount exceeds selected account's balance");
        }

        System.out.println("Do you wish to enter details to the Receiver?\n(Y)es or (N)o?");
        System.out.print("> ");

        String details = "no message";
        String detailsChoice = sc.nextLine();

        if(detailsChoice.equalsIgnoreCase("y")){
            System.out.println("Please enter the details below");
            System.out.print("> ");
            details = sc.nextLine();
        }
        int feechoice;
        while (true) {
            System.out.println("Choose Fees:\n1.SENDER\n2.SHARED\n3.RECIPIENT");
            System.out.print("> ");

            String choice = sc.nextLine();

            feechoice = Integer.parseInt(choice);
            if(feechoice>=1 || feechoice<=3)
                break;
        }
      
        Transfer transfer = new swiftTransfer();
        int send  = transfer.sendMoney(customer, accChoice-1, receivingIBAN, receiverFullname, cfm, amount, details,feechoice);
        if(send==0){
            System.out.println("You attempted a SEPA transfer for your own account.\nPlease use the Self Transfer tab");
            return;
        }
        if(send==-1){
            System.out.println("Your balance bounced");
            return;
        }
        if(send==1){
            System.out.println("\t Greece \t\t\t"+sendCountry);
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e.printStackTrace();
                }
            }
            
            System.out.print(" >\t" + receiverFullname+"\n");
            System.out.println("\nTransaction was succesfully made");

        }
    }


    static final String[][] SEPA_COUNTRY_IBAN_ARRAY = {
        {"AT", "Austria"},
        {"BE", "Belgium"},
        {"BG", "Bulgaria"},
        {"HR", "Croatia"},
        {"CY", "Cyprus"},
        {"CZ", "Czech Republic"},
        {"DK", "Denmark"},
        {"EE", "Estonia"},
        {"FI", "Finland"},
        {"FR", "France"},
        {"DE", "Germany"},
        {"GR" , "Greece"},
        {"HU", "Hungary"},
        {"IE", "Ireland"},
        {"IT", "Italy"},
        {"LV", "Latvia"},
        {"LT", "Lithuania"},
        {"LU", "Luxembourg"},
        {"MT", "Malta"},
        {"NL", "Netherlands"},
        {"PL", "Poland"},
        {"PT", "Portugal"},
        {"RO", "Romania"},
        {"SK", "Slovakia"},
        {"SI", "Slovenia"},
        {"ES", "Spain"},
        {"SE", "Sweden"},

        {"IS", "Iceland"},
        {"LI", "Liechtenstein"},
        {"NO", "Norway"},
        {"CH", "Switzerland"},

        {"AD", "Andorra"},
        {"MC", "Monaco"},
        {"SM", "San Marino"},
        {"VA", "Vatican City"},

        {"GB", "United Kingdom"},

        {"AL", "Albania"},
        {"ME", "Montenegro"},
        {"MD", "Moldova"},
        {"MK", "North Macedonia"},
        {"RS", "Serbia"}
    };


    static final Map<String, String> SEPA_IBAN_MAP = new HashMap<>();

    static {
        for (String[] entry : SEPA_COUNTRY_IBAN_ARRAY) {
            SEPA_IBAN_MAP.put(entry[0], entry[1]);
        }
    }

    static String sepaCountryByIBAN(String iban) {
        if (iban == null || iban.length() < 2) return null;
        String code = iban.substring(0, 2).toUpperCase();
        return SEPA_IBAN_MAP.get(code);
    }

    static boolean isSEPAByCountry(String country) {
        return SEPA_IBAN_MAP.containsValue(country);
    }


    static final String[][] SWIFT_COUNTRY_IBAN_ARRAY = {
        {"AD", "Andorra"},
        {"AE", "United Arab Emirates"},
        {"AF", "Afghanistan"},
        {"AG", "Antigua and Barbuda"},
        {"AI", "Anguilla"},
        {"AL", "Albania"},
        {"AM", "Armenia"},
        {"AO", "Angola"},
        {"AR", "Argentina"},
        {"AT", "Austria"},
        {"AU", "Australia"},
        {"AZ", "Azerbaijan"},
        {"BA", "Bosnia and Herzegovina"},
        {"BB", "Barbados"},
        {"BD", "Bangladesh"},
        {"BE", "Belgium"},
        {"BF", "Burkina Faso"},
        {"BG", "Bulgaria"},
        {"BH", "Bahrain"},
        {"BI", "Burundi"},
        {"BJ", "Benin"},
        {"BM", "Bermuda"},
        {"BN", "Brunei Darussalam"},
        {"BO", "Bolivia"},
        {"BR", "Brazil"},
        {"BS", "Bahamas"},
        {"BT", "Bhutan"},
        {"BW", "Botswana"},
        {"BY", "Belarus"},
        {"BZ", "Belize"},
        {"CA", "Canada"},
        {"CH", "Switzerland"},
        {"CL", "Chile"},
        {"CN", "China"},
        {"CO", "Colombia"},
        {"CR", "Costa Rica"},
        {"CU", "Cuba"},
        {"CY", "Cyprus"},
        {"CZ", "Czech Republic"},
        {"DE", "Germany"},
        {"DK", "Denmark"},
        {"DO", "Dominican Republic"},
        {"DZ", "Algeria"},
        {"EC", "Ecuador"},
        {"EE", "Estonia"},
        {"EG", "Egypt"},
        {"ES", "Spain"},
        {"FI", "Finland"},
        {"FR", "France"},
        {"GB", "United Kingdom"},
        {"GR", "Greece"},
        {"HK", "Hong Kong"},
        {"HR", "Croatia"},
        {"HU", "Hungary"},
        {"ID", "Indonesia"},
        {"IE", "Ireland"},
        {"IL", "Israel"},
        {"IN", "India"},
        {"IS", "Iceland"},
        {"IT", "Italy"},
        {"JP", "Japan"},
        {"KR", "South Korea"},
        {"KW", "Kuwait"},
        {"KZ", "Kazakhstan"},
        {"LB", "Lebanon"},
        {"LI", "Liechtenstein"},
        {"LT", "Lithuania"},
        {"LU", "Luxembourg"},
        {"LV", "Latvia"},
        {"MC", "Monaco"},
        {"MD", "Moldova"},
        {"ME", "Montenegro"},
        {"MK", "North Macedonia"},
        {"MT", "Malta"},
        {"MU", "Mauritius"},
        {"MX", "Mexico"},
        {"MY", "Malaysia"},
        {"NL", "Netherlands"},
        {"NO", "Norway"},
        {"NZ", "New Zealand"},
        {"PL", "Poland"},
        {"PT", "Portugal"},
        {"RO", "Romania"},
        {"RS", "Serbia"},
        {"RU", "Russia"},
        {"SA", "Saudi Arabia"},
        {"SE", "Sweden"},
        {"SG", "Singapore"},
        {"SI", "Slovenia"},
        {"SK", "Slovakia"},
        {"TH", "Thailand"},
        {"TR", "Turkey"},
        {"UA", "Ukraine"},
        {"US", "United States"},
        {"VN", "Vietnam"},
        {"ZA", "South Africa"}
    };

    static final Map<String, String> SWIFT_IBAN_MAP = new HashMap<>();

        static {
            for (String[] entry : SWIFT_COUNTRY_IBAN_ARRAY) {
                SWIFT_IBAN_MAP.put(entry[0], entry[1]);
            }
        }

        static String swiftCountryByIBAN(String iban) {
            if (iban == null || iban.length() < 2) return null;
            String code = iban.substring(0, 2).toUpperCase();
            return SWIFT_IBAN_MAP.get(code);
        }

        static boolean isSwiftByCountry(String country) {
            return SWIFT_IBAN_MAP.containsValue(country);
        }
}
