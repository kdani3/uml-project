package BankOfTuc.Transfers;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public abstract class Transfer {

    // --- BRIDGE PATTERN START ---
  
    protected TransferProcessor processor;

  
    public Transfer(TransferProcessor processor) {
        this.processor = processor;
    }
    // --- BRIDGE PATTERN END ---


    /* private String senderIBAN;
       private */ String receiverIBAN;
    private Customer receiverCustomer;

    // Κρατάμε τον default constructor για συμβατότητα με τις παλιές κλάσεις (InterBank, SelfTransfer)
    public Transfer() {
    }

    public String getReceivingFullname() {
        return receiverCustomer.getFullname();
    }

    abstract public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex,String BIC, String receiverIBAN,String receiverName,CustomerFileManager cfm,double amount,String details,int feeChoice);

    /* abstract public boolean sendMoney(Customer customer, BankAccount accountfoo, BankAccount accountbar,CustomerFileManager cfm, double amount); */
}