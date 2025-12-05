package BankOfTuc.Payments;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.PaymentLogger;
import BankOfTuc.Payments.Bill.BillStatus;

public class Payment {
    double totalAmount;
//    String vatid;
    static final double expireFee = 2.5;
    String rf;
    BankAccount account;
    LocalDate date;
    Bill bill;

    public Payment(BankAccount account,String RFcode){
        try {
            bill = BillFileStore.findByRFCode(RFcode);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.totalAmount = bill.getAmount();
        this.rf = RFcode;
        this.account = account;
  //      this.vatid = custVatID;
    }

    public boolean pay(BankAccount account,double amount,CustomerFileManager cfm){
        IndividualCustomer customer = (IndividualCustomer) cfm.getCustomerByIBAN(account.getIban());
        CompanyCustomer company = (CompanyCustomer) cfm.getCustomerByUsername(bill.getIssuerUsername());
        List<BankAccount> compaccs = company.getBankAccounts();

        if(company==null||customer==null||compaccs==null||bill.getStatus().equals(BillStatus.FROZEN)||bill.getStatus().equals(BillStatus.PAID)
        ||(amount+bill.getPaidAmount())>bill.getAmount())
            return false;

        account.reduceBalance(amount);
        if(totalAmount==amount){
            bill.setStatus(BillStatus.PAID);
            bill.setPaid(true);
        }
        else{
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }
        this.date = LocalDate.now();
        bill.setPaidAmount(amount);
        bill.setPayDate(date);
        bill.setPaidInstallments(bill.getPaidInstallments()+1);
        BankAccount compacc = compaccs.get(0);

        
        if(account.getBalance()<amount)
            return false;

        if(bill.getStatus().equals(BillStatus.EXPIRED)){
            if(account.getBalance()<amount+expireFee){
                return false;
            }
            account.reduceBalance(amount+expireFee);
        }  
        else
            account.reduceBalance(amount);

        compacc.addBalance(amount);

        cfm.updateCustomer(customer);
        cfm.updateCustomer(company);
        try {
            BillFileStore.updateBill(bill);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        PaymentLogger.logTransfer(customer.getVatID(), account.getIban(),bill.getRfcode(), "ONE-TIME", company.getVatID(), compacc.getIban(), bill.getAmount(), bill.getPaidAmount(), bill.getStatus().toString(), account.getBalance(), compacc.getBalance(),bill.getPaidInstallments());
        return true;
    }
}
