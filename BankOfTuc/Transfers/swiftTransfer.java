package BankOfTuc.Transfers;

import java.util.List;

import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;

public class swiftTransfer extends Transfer{
 static final double fee = 2.50;

    @Override
    public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex, String receiverIBAN, String receiverName, CustomerFileManager cfm, double amount, String details,int feeChoice) {
        double withfees;
        List<BankAccount> accounts = sendingCustomer.getBankAccounts();
        
        for (BankAccount bankAccount : accounts) {
            if(bankAccount.getIban().equals(receiverIBAN)){
                return 0;
            }
        }

        BankAccount account = accounts.get(sendingCustomerBankIndex);

        switch(feeChoice){
            case 1:
                withfees = amount + fee;
                break;
            case 2: withfees = amount + fee/2;
                break;
            default: withfees = amount;
        }
        if(account.getBalance()<withfees){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                account.getIban(),
                "SWIFT",
                "Not Provided",
                receiverName,
                receiverIBAN,
                amount,
                false,
                account.getBalance(),
                0,
                "The amount exceeds the account's balance"
            );
            return -1;
        }

        account.reduceBalance(withfees);
        cfm.updateCustomer(sendingCustomer);
        TransferLogger.logTransfer(
            sendingCustomer.getVatID(),
            account.getIban(),
            "SWIFT",
            "Not Provided",
            receiverName,
            receiverIBAN,
            amount,
            true,
            account.getBalance(),
            amount,
            details
        );
        return 1;
    }
}
