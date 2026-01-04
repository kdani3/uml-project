package BankOfTuc.CLI;

import java.util.Scanner;
import java.io.IOException;
import java.util.List;

import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.BillFileStore;
import BankOfTuc.Payments.CustomerPaymentService;
import BankOfTuc.Payments.Payment;
import BankOfTuc.Payments.RecurringPayment;
import BankOfTuc.Payments.Bill.BillStatus;

public class PaymentCLI {

    public static void managePayments(Scanner sc, Customer customer, CustomerFileManager cfm) {
        while (true) {
            System.out.println("\n--- Payment Management ---");
            System.out.println("1. Make One-Time Payment");            System.out.println("2. Manage Recurring Payments");
            System.out.println("3. Back");
            System.out.print("> ");
            
            switch (sc.nextLine().trim()) {
                case "1" -> oneTimePayment(sc, customer, cfm);
                case "2" -> manageRecurringPayments(sc,customer,cfm);
                case "3" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void oneTimePayment(Scanner sc, Customer customer, CustomerFileManager cfm) {
        // Reuse your existing Payment logic
        System.out.println("Enter Bill RF Code:");
        String rf = sc.nextLine().trim();
        
        if(!rf.startsWith("RF")||rf.length()<4){
            System.out.println("Wrong RF");
            return;
        }

        Bill bill=null;
        try {
            bill = BillFileStore.findByRFCode(rf);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(bill==null){
            System.out.println("RF code doesn't match");
            return;
        }

        if(bill.getStatus().equals(BillStatus.FROZEN)){
            System.out.println("The rf code points to a frozen bill");
            return;
        }
        if(bill.isPaid()){
            System.out.println("The rf code is already paid");
            return;
        }


        List<BankAccount> accounts = customer.getBankAccounts();

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
        
        BankAccount account = accounts.get(accChoice-1);

        double amount = bill.getAmount();
        System.out.println("Use predefined amount "+(amount-bill.getPaidAmount())+"?\n(Y)es or (N)o");
        System.out.print(">");

        String ynchoice = sc.nextLine();
        if(ynchoice.equalsIgnoreCase("n")){
            System.out.println("Enter new amount");
            System.out.print(">");
            amount = Double.parseDouble(sc.nextLine());
        }

        if(account.getBalance()<amount){
            System.out.println("The account balance is inadequate");
            return;
        }

        double remainAmount = amount+bill.getPaidAmount();
        if(remainAmount>bill.getAmount()){
            System.out.println("The Enter amount exceed the Predefined amount"+remainAmount);
            return;
        }

        if(bill.getStatus().equals(BillStatus.EXPIRED)){
            System.out.println("The Bill is expired.\nThere is an additional fee of 2.5 EUR.\nContinue (Y)es or (N)o");
            if(sc.nextLine().equalsIgnoreCase("y"))
                return;
        }

        Payment payment = new Payment(account, rf);
        payment.pay(account, amount, cfm);
    }

    public static void manageRecurringPayments(Scanner sc, Customer customer, CustomerFileManager cfm) {
        try {
            CustomerPaymentService paymentService = new CustomerPaymentService(customer.getVatID(), cfm);

            while (true) {
                System.out.println("\n--- Your Recurring Payments ---");
                List<RecurringPayment> payments = paymentService.getPayments();

                if (payments.isEmpty()) {
                    System.out.println("You have no recurring payments.");
                } else {
                    printPayments(payments);
                }

                System.out.println("\n1. Add New Recurring Payment");
                System.out.println("2. Pause Payment");
                System.out.println("3. Resume Payment");
                System.out.println("4. Cancel Payment");
                System.out.println("5. Back");
                System.out.print("> ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> addNewRecurringPayment(sc, customer, paymentService, cfm);
                    case "2" -> pausePayment(sc, paymentService);
                    case "3" -> resumePayment(sc, paymentService);
                    case "4" -> cancelPayment(sc, paymentService);
                    case "5" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addNewRecurringPayment(Scanner sc, Customer customer, 
            CustomerPaymentService paymentService, CustomerFileManager cfm) throws IOException {
            // Select bank account
            var accounts = customer.getBankAccounts();
            if (accounts.isEmpty()) {
                System.out.println("You have no bank accounts.");
                return;
            }

            System.out.println("Select Payer Account:");
            for (int i = 0; i < accounts.size(); i++) {
                System.out.printf("%d. %s (Balance: %.2f)\n", 
                    i + 1, accounts.get(i).getIban(), accounts.get(i).getBalance());
            }
            System.out.print("> ");
            int accIndex = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (accIndex < 0 || accIndex >= accounts.size()) {
                System.out.println("Invalid account.");
                return;
            }
            String iban = accounts.get(accIndex).getIban();

            
            // Enter RF code
            System.out.println("Enter Bill RF Code:");
            System.out.print("> ");
            String rfCode = sc.nextLine().trim();

            // Validate bill exists and is for this customer
            var bill = BillFileStore.findByRFCode(rfCode);
            if (bill == null) {
                System.out.println("Bill not found.");
                return;
            }
            if (!bill.getpayerID().equals(customer.getVatID())) {
                System.out.println("Wrong payee");
                return;
            }

            double amount = bill.getAmount();
            System.out.printf("Bill Amount: %.2f\n", amount);
            System.out.print("Confirm recurring payment? (Y/N): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
                return;
            }

            paymentService.addPayment(rfCode,iban,bill.getMonthlyAmount(),customer.getVatID());
            System.out.println("Recurring payment created!");

    }

    private static void pausePayment(Scanner sc, CustomerPaymentService paymentService) {
        try {
            List<RecurringPayment> payments = paymentService.getPayments();
            if (payments.isEmpty()) {
                System.out.println("No payments to pause.");
                return;
            }
            printPayments(payments);
            System.out.println("Enter payment number to pause:");
            System.out.print("> ");
            int choice = Integer.parseInt(sc.nextLine().trim())-1;

            if (paymentService.pausePayment(payments.get(choice).getRfCode())) {
                System.out.println("Payment paused.");
            } else {
                System.out.println("Payment not found or already paused.");
            }
        } catch (Exception e) {
            System.err.println("Error pausing payment.");
        }
    }

    private static void resumePayment(Scanner sc, CustomerPaymentService paymentService) {
        try {
            List<RecurringPayment> payments = paymentService.getPayments();
            if (payments.isEmpty()) {
                System.out.println("No payments to resume.");
                return;
            }

            printPayments(payments);
            System.out.println("Enter payment number to pause:");
            System.out.print("> ");
            int choice = Integer.parseInt(sc.nextLine().trim())-1;


            if (paymentService.resumePayment(payments.get(choice).getRfCode())) {
                System.out.println("Payment resumed.");
            } else {
                System.out.println("Payment not found or not paused.");
            }
        } catch (Exception e) {
            System.err.println("Error resuming payment.");
        }
    }


    private static void cancelPayment(Scanner sc, CustomerPaymentService paymentService) {
        try {
            List<RecurringPayment> payments = paymentService.getPayments();
            if (payments.isEmpty()) {
                System.out.println("No payments to cancel.");
                return;
            }

            printPayments(payments);
            System.out.println("Enter payment number to cancel:");
            System.out.print("> ");
            int choice = Integer.parseInt(sc.nextLine().trim())-1;

            if (paymentService.cancelPayment(payments.get(choice).getRfCode())) {
                System.out.println("Payment canceled.");
            } else {
                System.out.println("Payment not found.");
            }
        } catch (Exception e) {
            System.err.println("Error canceling payment.");
        }
    }

    static void printPayments(List<RecurringPayment> payments){
        for (int i = 0; i < payments.size(); i++) {
            RecurringPayment p = payments.get(i);
            String status = p.isPaused() ? "⏸ PAUSED" : " ▶ ACTIVE";
            System.out.printf("%d. RF: %s | Next: %s | %s | Attempts: %d\n",
                i + 1,
                p.getRfCode(),
                p.getNextDueDate(),
                status,
                p.getCurrentAttempts()
            );
        }
    }
}