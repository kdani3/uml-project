// File: BankOfTuc/Services/CustomerPaymentService.java
package BankOfTuc.Payments;

import java.io.IOException;
import java.util.List;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Services.TimeService;
public class CustomerPaymentService {
    private final RecurringPaymentScheduler scheduler;
    private final String customerVatID;

    public CustomerPaymentService(String customerVatID, CustomerFileManager cfm) throws IOException {
        this.customerVatID = customerVatID;
        this.scheduler = new RecurringPaymentScheduler(cfm);
    }

    //get ONLY this customer's payments
    public List<RecurringPayment> getPayments() {
        return scheduler.getPaymentsForCustomer(customerVatID);
    }

    //create a new recurring payment for this customer
    public void addPayment(String rfCode, String iban, double amount,String vatid) throws IOException {
        RecurringPayment rp = new RecurringPayment(
        rfCode,
        vatid,   
        iban,
        amount,
        TimeService.getInstance().today()
        );
        scheduler.addRecurringPayment(rp);

    }
    

    public boolean pausePayment(String rfCode) throws IOException {
        RecurringPayment payment = findPayment(rfCode);
        if (payment != null && !payment.isPaused()) {
            payment.pause();
            scheduler.save();
            return true;
        }
        return false;
    }

    public boolean resumePayment(String rfCode) throws IOException {
        RecurringPayment payment = findPayment(rfCode);
        if (payment != null && payment.isPaused()) {
            payment.resume();
            scheduler.save();
            return true;
        }
        return false;
    }

    public boolean cancelPayment(String rfCode) throws IOException {
        return scheduler.cancelPayment(rfCode, customerVatID);
    }

    //faily auto-check for due payments
    public void processDuePayments() throws IOException {
        scheduler.dailyCheck(); 
    }

    private RecurringPayment findPayment(String rfCode) {
        return getPayments().stream()
            .filter(p -> p.getRfCode().equals(rfCode))
            .findFirst()
            .orElse(null);
    }
}