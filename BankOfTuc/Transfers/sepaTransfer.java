package BankOfTuc.Transfers;

import java.time.LocalDate;
import java.util.List;

import BankOfTuc.Customer;
import BankOfTuc.SepaTransferService;
import BankOfTuc.TimeService;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;

public class sepaTransfer  extends Transfer{
    public static final double fee = 2.50;

    @Override
    public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex,String BIC, String receiverIBAN, String receiverName, CustomerFileManager cfm, double amount, String details,int feeChoice) {
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
            default: withfees = amount + fee;

        }
        if(account.getBalance()<withfees){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                account.getIban(),
                "SEPA",
                "Not Provided",
                BIC,
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
        SepaTransferService service = new SepaTransferService();
        boolean apiResponse = service.sendSepaTransferRequest(
            amount, 
            receiverName, 
            receiverName, 
            BIC, 
            TimeService.getInstance().today().toString(), 
            (feeChoice == 1) ? "OUR" : "SHA"
            );
        
        if(apiResponse){
            account.reduceBalance(withfees);
            cfm.updateCustomer(sendingCustomer);

            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                account.getIban(),
                "SEPA",
                "Not Provided",
                BIC,
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

        TransferLogger.logTransfer(
            sendingCustomer.getVatID(),
            account.getIban(),
            "SEPA",
            "Not Provided",
            BIC,
            receiverName,
            receiverIBAN,
            amount,
            false,
            account.getBalance(),
            0,
            "The request got rejected"
        );
        return -2;
    
    }
}
