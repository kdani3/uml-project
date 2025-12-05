package BankOfTuc.Transfers;


import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;

public class InterBank extends Transfer{

    private final double fee = 0;//check later

    public InterBank(){
    }
    private Customer receiverCustomer;

    public String getReceivingFullname(){
        return receiverCustomer.getFullname();
    }

    @Override
    public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex,String receiverIBAN,String receiverName,CustomerFileManager cfm,double amount, String details,int feeChoice) {

        Customer receiverCustomer = cfm.getCustomerByIBAN(receiverIBAN);
        this.receiverCustomer = receiverCustomer;
        //Customer receiverCustomer;
        int receiverIndex=-1;
        BankAccount sendingAccount = sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex);

        if(receiverCustomer==null){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                "no id",
                receiverName,
                "no iban",
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                0,
                 "The Receiver Couldn't be found"
            );
            return -5;
        }

        for (int k=0 ; k < receiverCustomer.getBankAccounts().size(); k++) {
            System.out.println(receiverCustomer.getBankAccounts().get(k).getIban());
            if(receiverCustomer.getBankAccounts().get(k).getIban().equals(receiverIBAN)){
                receiverIndex=k;
                break;
            }
        }
   
        
        if(receiverCustomer.getFullname().equals(sendingCustomer.getFullname())){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The Customer provided the details for a Self-Transfer, instead of an InterBank"
            );
                return 0;

        }
          
        if(!receiverCustomer.getActive() || !sendingCustomer.getActive()){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The receiving Customer's Account is Deactivated"
            );  
            return -1;
        }

        if(sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance()<amount+fee){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The provided amount exceeds the account's balance"
            );
            return -2;
        }

        if(sendingAccount.getBalance()<amount+fee){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The provided amount is too miniscule"
            );
            return -6;
        }

        
        if(!TransferUtils.equalsCustomerName(receiverCustomer.getFullname(),receiverName)){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The sender provided incorrect details for the InterBank Transaction"
            );
            return -3;
        }

        if(receiverIndex==-1){
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                false,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                "The sender provided incorrect IBAN details for the InterBank Transaction"
            );
            return -4;
        }
            
    
        sendingAccount.reduceBalance(amount);
        receiverCustomer.getBankAccounts().get(receiverIndex).addBalance(amount);

        cfm.updateCustomer(sendingCustomer);

        cfm.updateCustomer(receiverCustomer);

        TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getIban(),
                "INTERNAL",
                receiverCustomer.getVatID(),
                receiverName,
                receiverCustomer.getBankAccounts().get(receiverIndex).getIban(),
                amount,
                true,
                sendingCustomer.getBankAccounts().get(sendingCustomerBankIndex).getBalance(),
                receiverCustomer.getBankAccounts().get(receiverIndex).getBalance(),
                details
        );
        return 1;
    }

}
