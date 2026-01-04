package BankOfTuc.CLI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Accounting.BankAccountFactory;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Logging.TransactionHistoryService;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class IndividualCLI {

    public static void loggedInMenu(Scanner sc, LoginManager login, User user, UserFileManagement ufm, CustomerFileManager cfm) throws QrGenerationException, URISyntaxException {
        String username = user.getUsername();
        IndividualCustomer customer = (IndividualCustomer) cfm.getCustomerByUsername(username);

        // --- COMMAND PATTERN SETUP ---
        Map<String, Command> commands = new HashMap<>();

        // 1. Transfer Command (Inline implementation for brevity)
        commands.put("1", new Command() {
            @Override
            public void execute(Scanner sc) {
                TransferCLI.TransferMenu(sc, login, customer, cfm);
            }
            @Override
            public String getDescription() { return "Transfer Money"; }
        });

        // 2. Pay Bill Command (Using the class we created)
        commands.put("2", new PayBillCommand(customer, cfm));

        // 3. History Command (Using the class we created)
        commands.put("3", new ViewHistoryCommand(customer, cfm));

        // 4. Settings Command (Inline)
        commands.put("4", new Command() {
            @Override
            public void execute(Scanner sc) {
                try {
                    CLIUtils.SettingsMenu(sc, login, user, ufm);
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override
            public String getDescription() { return "Settings"; }
        });

        // 5. Add Bank Account Command (Wraps the local method)
        commands.put("5", new Command() {
            @Override
            public void execute(Scanner sc) {
                addBankAccount(sc, customer, cfm);
            }
            @Override
            public String getDescription() { return "Add Bank Account"; }
        });

        // 6. Logout Command
        commands.put("6", new Command() {
            @Override
            public void execute(Scanner sc) {
                login.logout(username);
            }
            @Override
            public String getDescription() { return "Logout"; }
        });
        // -----------------------------

        while (login.isLoggedIn(username)) {
            // Refresh customer data
            cfm.updateCustomer(customer);

            System.out.println("\n--- User Menu (" + username + "/" + user.getRole() + ") ---");
            
            // Print Bank Accounts (View Only Logic)
            List<BankAccount> accounts = customer.getBankAccounts();
            if (!accounts.isEmpty()) {
                System.out.println("\n--- Bank Accounts ---");
                for (int i = 0; i < accounts.size(); i++) {
                    BankAccount account = accounts.get(i);
                    System.out.println((i + 1) + ". " + account.getIban() + " (" + account.getType() + ") | " + account.getBalance() + " €");
                }
            }

            // Print Short History (View Only Logic)
            printRecentHistory(customer, cfm);

            // --- MENU OPTIONS FROM COMMAND MAP ---
            System.out.println("\n--- Options ---");

            commands.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println(entry.getKey() + ". " + entry.getValue().getDescription()));

            System.out.print("> ");
            String input = sc.nextLine().trim();

            // Session check
            if (!login.isLoggedIn(username)) {
                System.out.println("Session timed out.");
                return;
            }
            login.activity(username);

            // --- EXECUTE COMMAND ---
            Command cmd = commands.get(input);
            if (cmd != null) {
                cmd.execute(sc);

                if (input.equals("6")) return; 
            } else {
                System.out.println("Invalid option");
            }
        }
    }

    private static void printRecentHistory(IndividualCustomer customer, CustomerFileManager cfm) {
        try {
            var history = TransactionHistoryService.getHistoryForCustomer(customer.getVatID(), cfm);
            if (history.isEmpty()) {
                System.out.println("No successful transactions found.");
            } else {
                System.out.println("\n--- Recent History ---");
                int displayCount = Math.min(history.size(), 5); // Show top 5
                for (int i = 0; i < displayCount; i++) {
                    var e = history.get(i);
                    System.out.printf("%d. %s | %s %s | %s | %s\n",
                            i + 1, e.datetime, e.getAmountDisplay(),
                            e.getIbanDisplay(customer.getVatID()), e.counterpartyName, e.type);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading history.");
        }
    }

    private static void addBankAccount(Scanner sc, IndividualCustomer customer, CustomerFileManager cfm) {
        if (customer.getBankAccounts().size() >= 5) {
            System.out.println("You already have 5 bank accounts. Cannot add more.");
            return;
        }

        System.out.println("Choose account type: 1) CHECKING  2) SAVINGS");
        System.out.print("> ");
        String choice = sc.nextLine().trim();
        BankAccount.AccountType type;
        
        if ("1".equals(choice)) type = BankAccount.AccountType.CHECKING;
        else if ("2".equals(choice)) type = BankAccount.AccountType.SAVINGS;
        else {
            System.out.println("Invalid choice - cancelled.");
            return;
        }

        // BankAccount newAcc = new BankAccount(customer.getVatID(), type);
        BankAccount newAcc = BankAccountFactory.createAccount(customer.getVatID(), type);
        
        customer.addBankAccount(newAcc);
        if (cfm.updateCustomer(customer)) {
            System.out.println("Added account: " + newAcc.getIban() + " (" + newAcc.getType() + ")");
        } else {
            System.out.println("Failed to add account (save failed).");
        }
    }
}