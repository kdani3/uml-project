package BankOfTuc.Transfers;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public abstract class Transfer {

/*     private String senderIBAN;
    private */ String receiverIBAN;
    private Customer receiverCustomer;
    public Transfer(){
    }

    public String getReceivingFullname(){
        return receiverCustomer.getFullname();
    }

    abstract public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex,String BIC, String receiverIBAN,String receiverName,CustomerFileManager cfm,double amount,String details,int feeChoice);

    /* abstract public boolean sendMoney(Customer customer, BankAccount accountfoo, BankAccount accountbar,CustomerFileManager cfm, double amount); */
}
