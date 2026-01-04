package BankOfTuc.CLI;

import java.util.Scanner;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public final class BankAccountCLI {

    private BankAccountCLI() {}

    public static void addCompanyAccount(Scanner sc, CompanyCustomer customer, CustomerFileManager cfm) {
        if (customer.getBankAccounts().size() >= 1) {
            System.out.println("Companies can only have one bank account.");
            return;
        }

        BankAccount newAcc = new BankAccount(customer.getVatID(), BankAccount.AccountType.COMPANY);
        customer.addBankAccount(newAcc);
        boolean ok = cfm.updateCustomer(customer);
        if (ok) {
            System.out.println("Added company account: " + newAcc.getIban() + " (" + newAcc.getType() + ")");
        } else {
            System.out.println("Failed to add account (save failed).");
        }
    }

}
