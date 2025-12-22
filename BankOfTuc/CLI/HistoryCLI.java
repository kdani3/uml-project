package BankOfTuc.CLI;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import BankOfTuc.Customer;
import BankOfTuc.EmailUtils;
import BankOfTuc.EnvReader;
import BankOfTuc.TransactionPdfGenerator;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import BankOfTuc.Transfers.InterBank;
import BankOfTuc.Transfers.SelfTransfer;
import BankOfTuc.Transfers.Transfer;
import BankOfTuc.Transfers.sepaTransfer;

public class HistoryCLI {
public static void showTransactionHistory(Scanner sc, Customer customer,List<TransactionHistoryService.TransactionEntry> history  ,CustomerFileManager cfm) {
        try {
            String vatId = customer.getVatID();

            if (history.isEmpty()) {
                System.out.println("No successful transactions found.");
                return;
            }

            while (true) {
                System.out.println("\n--- Transaction History ---");
                for (int i = 0; i < history.size(); i++) {
                    var e = history.get(i);
                    System.out.printf("%d. %s | %s %s | %s | %s\n",
                        i + 1,
                        e.datetime,
                        e.getAmountDisplay(),
                        e.getIbanDisplay(vatId),
                        e.counterpartyName,
                        e.type
                    );
                }
                System.out.println("\n");
                System.out.println( "1. Back");
                System.out.println("2. Manage a Transaction");
                System.out.print("> ");

                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    return;
                } else if (choice.equals("2")) {
                    System.out.print("Enter transaction number to Manage: ");
                    int redoNum = Integer.parseInt(sc.nextLine().trim());
                    if (redoNum >= 1 && redoNum <= history.size()) {
                        manageTransaction(sc,history.get(redoNum-1), customer, cfm);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }
     
    static void manageTransaction(Scanner sc,TransactionHistoryService.TransactionEntry e,Customer customer,CustomerFileManager cfm){
        System.out.println("\n\nTransaction Type: " + e.type);
        System.out.println();
        System.out.println("Date - Time: "+e.datetime);

        if (e.isOutgoing) {
            //send money
            System.out.println("Ordering Customer: " + customer.getFullname());
            System.out.println("Ordering Account:  " + e.senderIban);
            System.out.println("Recipient:         " + e.counterpartyName);
            System.out.println("Recipient IBAN:    " + e.counterpartyIban);
            System.out.println("Amount Sent:       " + e.getAmountDisplay() + " €");
        } else {
            //received money
            System.out.println("Ordering Customer: " + e.counterpartyName);
            System.out.println("Ordering Account:  " + e.senderIban);
            System.out.println("Beneficiary:       " + customer.getFullname());
            System.out.println("Beneficiary IBAN:  " + e.receiverIban); // or e.counterpartyIban if stored
            System.out.println("Amount Received:   " + e.getAmountDisplay() + " €");
        }
        
        System.out.println("\n\n1. Export Statement");
        System.out.println("2. Send Statement to Email");
        if(e.isOutgoing&&!e.type.equalsIgnoreCase("PAYMENT"))
            System.out.println("3. Redo");
        System.out.print(">");
        String choice = sc.nextLine();
        switch(choice){
            case "1": 
                JFrame parentComponent = new JFrame();
                JFileChooser fileChooser= new JFileChooser();
                // Some init code, if you need one, like setting title
                int returnVal = fileChooser.showOpenDialog(parentComponent);
                if ( returnVal == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        TransactionPdfGenerator.generateTransactionPdf(e, customer, fileToSave.getPath(),false);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                break;

            case "2":
                if(customer.getEmail()==null){
                    System.out.println("Not registered email");
                    break;
                }
                String pdfBase64 = null;
                
                try {
                    pdfBase64 = TransactionPdfGenerator.generateTransactionPdf(e, customer, "meow.pdf",true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }              

                Map<String, String> env = null;

                try {
                    env = EnvReader.loadEnv(".env");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                String api_key = env.get("MAIL_API_KEY");

                if(pdfBase64==null||pdfBase64.isEmpty()||api_key.isEmpty()){
                    System.out.println("Error");
                    return;
                }
                String html = EmailUtils.statementEmailHTML(customer.getFullname());

                try {
                    EmailUtils.sendEmailWithPdfAttachment(api_key, "info@bankoftuc.denmoukaneito.click", "Bank Of Tuc", 
                            customer.getEmail(), "Export Statement", html,pdfBase64,"Statement"+e.datetime+".pdf");
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                break;

            case "3":
                if(!e.isOutgoing||e.type.equalsIgnoreCase("PAYMENT"))
                    return;

                switch(e.type){
                    case "SEPA":
                        redoSEPA(sc, e, customer, cfm);
                        break;
                    case "SWIFT":
                        redoSWIFT(sc, e, customer, cfm);
                        break;
                    case "INTERNAL":
                        redoInter(sc, e, customer, cfm);
                        break;
                    case "SELF":
                        redoSelf(sc, e, customer, cfm);
                }

                break;
        }
        return;
    }

    static void redoSEPA(Scanner sc,TransactionHistoryService.TransactionEntry e,Customer customer,CustomerFileManager cfm){
        List<BankAccount> accounts = customer.getBankAccounts();
        int accIndex = -1;
        for(int i=0;i<accounts.size();i++){
            if(accounts.get(i).getIban().equals(e.senderIban)){
                accIndex=i;
                break;
            }       
        }

        if(accIndex==-1){
            System.out.println("Could not find IBAN matching the sending one");
            return;
        }
        double amount = e.amount;
        System.out.println("Use the previous amount: "+amount+" (Y)es or (N)o?");
        String ynchoice = sc.nextLine();

        if(ynchoice.equalsIgnoreCase("n")){
            System.out.println("Enter new amount to send.");
            System.out.print("> ");

            String amountString = sc.nextLine();
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e1) {
                System.out.println("Invalid input. Please enter a numeric value.");
                return;
            }
        }

        if(accounts.get(accIndex).getBalance()<amount){
            System.out.println("Inadequate Balance.");
            return;
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
            System.out.println("Choose Fees:\n1.OUR\n2.SHA");
            System.out.print("> ");

            String choice2 = sc.nextLine();

            feechoice = Integer.parseInt(choice2);
            if(feechoice>=1 || feechoice<=3)
                break;
        }
    
        Transfer transfer = new sepaTransfer();
        int send  = transfer.sendMoney(customer, accIndex,e.bankCode, e.counterpartyIban, e.counterpartyName, cfm, amount, details,feechoice);
        if(send==0){
        System.out.println("You attempted a SEPA transfer for your own account.\nPlease use the Self Transfer tab");
        return;
        }
        if(send==-1){
            System.out.println("Your balance bounced");
            return;
        }
        if(send==1){
            String sendCountry =TransferCLI.sepaCountryByIBAN(e.counterpartyIban);
            System.out.println("\t Greece \t\t\t"+sendCountry);
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e1) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e1.printStackTrace();
                }
            }
            System.out.print(" >\t" + e.counterpartyName+"\n");
            System.out.println("\nTransaction was succesfully made");
        }
        return;
    }


    static void redoSWIFT(Scanner sc,TransactionHistoryService.TransactionEntry e,Customer customer,CustomerFileManager cfm){
        List<BankAccount> accounts = customer.getBankAccounts();
        int accIndex = -1;
        for(int i=0;i<accounts.size();i++){
            if(accounts.get(i).getIban().equals(e.senderIban)){
                accIndex=i;
                break;
            }       
        }

        if(accIndex==-1){
            System.out.println("Could not find IBAN matching the sending one");
            return;
        }
        double amount = e.amount;
        System.out.println("Use the previous amount: "+amount+" (Y)es or (N)o?");
        String ynchoice = sc.nextLine();

        if(ynchoice.equalsIgnoreCase("n")){
            System.out.println("Enter new amount to send.");
            System.out.print("> ");

            String amountString = sc.nextLine();
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e1) {
                System.out.println("Invalid input. Please enter a numeric value.");
                return;
            }
        }

        if(accounts.get(accIndex).getBalance()<amount){
            System.out.println("Inadequate Balance.");
            return;
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
            System.out.println("Choose Fees:\n1.OUR\n2.SHA");
            System.out.print("> ");

            String choice2 = sc.nextLine();

            feechoice = Integer.parseInt(choice2);
            if(feechoice>=1 || feechoice<=3)
                break;
        }
    
        Transfer transfer = new sepaTransfer();
        int send  = transfer.sendMoney(customer, accIndex,e.bankCode, e.counterpartyIban, e.counterpartyName, cfm, amount, details,feechoice);
        if(send==0){
        System.out.println("You attempted a SEPA transfer for your own account.\nPlease use the Self Transfer tab");
        return;
        }
        
        if(send==-1){
            System.out.println("Your balance bounced");
            return;
        }
        
        if(send==1){
            String sendCountry =TransferCLI.swiftCountryByIBAN(e.counterpartyIban);
            System.out.println("\t Greece \t\t\t"+sendCountry);
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e1) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e1.printStackTrace();
                }
            }
            System.out.print(" >\t" + e.counterpartyName+"\n");
            System.out.println("\nTransaction was succesfully made");
        }
        return;
    }

    static void redoSelf(Scanner sc,TransactionHistoryService.TransactionEntry e,Customer customer,CustomerFileManager cfm){
        List<BankAccount> accounts = customer.getBankAccounts();
        int acc1Index =-1 ,acc2Index = -1;
        for(int i=0;i<accounts.size();i++){
            if(accounts.get(i).getIban().equals(e.senderIban)){
                acc1Index=i;
            }
            if(accounts.get(i).getIban().equals(e.receiverIban)){
                acc2Index=i;
            }
            if(acc1Index!=-1&&acc2Index!=-2){
                break;
            }
        }

        if(acc1Index==-1||acc2Index==-1){
            System.out.println("Could not find IBANs matching to records");
            return;
        }

        double amount = e.amount;
        System.out.println("Use the previous amount: "+amount+" (Y)es or (N)o?");
        String ynchoice = sc.nextLine();

        if(ynchoice.equalsIgnoreCase("n")){
            System.out.println("Enter new amount to send.");
            System.out.print("> ");

            String amountString = sc.nextLine();
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e1) {
                System.out.println("Invalid input. Please enter a numeric value.");
                return;
            }
        }

        BankAccount sendingSelfAccount = accounts.get(acc1Index);
        BankAccount receivingSelfAccount = accounts.get(acc2Index);

        if(sendingSelfAccount.getBalance()<amount){
            System.out.println("The requested amount exceeds the account's amount");
            return;
        }

        SelfTransfer selfTransfer = new SelfTransfer();

        if(!selfTransfer.sendMoney(customer,sendingSelfAccount,receivingSelfAccount,cfm,amount)){
            System.err.println("There has been an error.\nPlease try again later");  
            return;
        }  
        System.out.print("\tAccount "+acc1Index+1);
        for(int i=0;i<5;i++){
            System.out.print(" -");
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e1) {
                System.err.println("waiting for Transaction printing has made an oopsy");
                e1.printStackTrace();
            }
        }
        System.out.print(" >\t" +" Account "+acc2Index+1+"\n");

        System.out.println("\nAmount transfered successfully");

        
    }

    static void redoInter(Scanner sc,TransactionHistoryService.TransactionEntry e,Customer customer,CustomerFileManager cfm){
        List<BankAccount> accounts = customer.getBankAccounts();
        int accIndex = -1;
        for(int i=0;i<accounts.size();i++){
            if(accounts.get(i).getIban().equals(e.senderIban)){
                accIndex=i;
                break;
            }       
        }

        if(accIndex==-1){
            System.out.println("Could not find IBAN matching the sending one");
            return;
        }
        double amount = e.amount;
        System.out.println("Use the previous amount: "+amount+" (Y)es or (N)o?");
        String ynchoice = sc.nextLine();

        if(ynchoice.equalsIgnoreCase("n")){
            System.out.println("Enter new amount to send.");
            System.out.print("> ");

            String amountString = sc.nextLine();
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e1) {
                System.out.println("Invalid input. Please enter a numeric value.");
                return;
            }
        }

        if(accounts.get(accIndex).getBalance()<amount){
            System.out.println("Inadequate Balance.");
            return;
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

        Customer sendCustomer = cfm.getCustomerByIBAN(e.counterpartyIban);
        Transfer transaction = new InterBank();
        int sendAttempt = transaction.sendMoney(customer, accIndex,e.bankCode, e.receiverIban ,sendCustomer.getFullname(), cfm, amount,details,0);
        if(sendAttempt==1){
            System.out.print("\t"+customer.getFullname());
            System.out.print("    ");
            for(int i=0;i<5;i++){
                System.out.print(" -");
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e2) {
                    System.err.println("waiting for Transaction printing has made an oopsy");
                    e2.printStackTrace();
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
                                        
}
