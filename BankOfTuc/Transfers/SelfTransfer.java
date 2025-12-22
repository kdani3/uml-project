package BankOfTuc.Transfers;

import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;

public  class SelfTransfer  {

    //@Override
    public boolean sendMoney(Customer customer, BankAccount accountfoo, BankAccount accountbar,CustomerFileManager cfm, double amount) {
        if(customer==null){
            return false;
        }
        if(accountfoo.getBalance()<amount){
            TransferLogger.logTransfer(
                customer.getVatID(),
                accountfoo.getIban(),
                "SELF",
                customer.getVatID(),
                customer.getFullname(),
                "TUCGRAAXX",
                accountbar.getIban(),
                amount,
                false,
                accountfoo.getBalance(),
                accountbar.getBalance(),
                "The amount exceeds the account's balance"
            );
            return true;
            
        }
        accountfoo.reduceBalance(amount);
        accountbar.addBalance(amount);
        cfm.updateCustomer(customer);
        TransferLogger.logTransfer(
            customer.getVatID(),
            accountfoo.getIban(),
            "SELF",
            customer.getVatID(),
            customer.getFullname(),
            "TUCGRAAXX",
            accountbar.getIban(),
            amount,
            true,
            accountfoo.getBalance(),
            accountbar.getBalance(),
            ""
        );
        cfm.updateCustomer(customer);
        return true;

    }
}
