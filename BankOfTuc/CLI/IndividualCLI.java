package BankOfTuc.CLI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Logging.TransactionHistoryService;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class IndividualCLI {

public static void loggedInMenu(Scanner sc, LoginManager login, User user,UserFileManagement ufm,CustomerFileManager cfm) throws QrGenerationException, URISyntaxException {
        String username = user.getUsername();
        while (login.isLoggedIn(username)) {

            System.out.println("\n--- User Menu (" + username + "/" + user.getRole() + ") ---");
            System.out.println("1. Transfer");
            System.out.println("2. Payments");
            System.out.println("3. Transactions History");
            System.out.println("4. Settings");
            System.out.println("5. Logout");


            IndividualCustomer customer = (IndividualCustomer) cfm.getCustomerByUsername(username);
            cfm.updateCustomer(customer);

            List<BankAccount> accounts =customer.getBankAccounts();
            if(!accounts.isEmpty()){
                System.out.println("\n--- Bank Accounts ---");

                for(int i=0;i<accounts.size();i++){
                    BankAccount account = accounts.get(i);
                    System.out.println(i+1+". "+account.getIban() + " | " + account.getBalance()+ " €");
                }
            }
            

            List<TransactionHistoryService.TransactionEntry> history = new ArrayList<>();
            try {
                history = TransactionHistoryService.getHistoryForCustomer(customer.getVatID(), cfm);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (history.isEmpty()) {
                System.out.println("No successful transactions found.");
            }
            else {
                System.out.println("\n--- Transaction History ---");
                int displayCount = Math.min(history.size(), 7);
                for (int i = 0; i < displayCount; i++) {
                    var e = history.get(i);
                    System.out.printf("%d. %s | %s %s | %s | %s\n",
                        i + 1,
                        e.datetime,
                        e.getAmountDisplay(),
                        e.getIbanDisplay(customer.getVatID()),
                        e.counterpartyName,
                        e.type
                    );
                }
            }
       
            
            System.out.print("> ");

            String input = sc.nextLine();

            login.activity(username);

            switch (input) {
                case "1":
                    boolean transact = TransferCLI.TransferMenu(sc,login,customer,cfm);
                    if(transact)
                        break;

                case "2":
                    PaymentCLI.managePayments(sc, customer, cfm);
                    break;
               
                case "3":
                    HistoryCLI.showTransactionHistory(sc, customer, history, cfm);
                    break;
                    
                case "4":
                    boolean settings = CLIUtils.SettingsMenu(sc, login,user,ufm);
                    if(settings)
                        break;
                case "5":
                    login.logout(username);
                    return;
               
                default:
                    System.out.println("Invalid option");
            }

        }
    }

    // In your CLI class
    
}
