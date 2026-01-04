package BankOfTuc.Accounting;

import BankOfTuc.Accounting.BankAccount.AccountType;

public class BankAccountFactory {

    // Factory Method
    public static BankAccount createAccount(String ownerVat, AccountType type) {
        return new BankAccount(ownerVat, type);
    }
}