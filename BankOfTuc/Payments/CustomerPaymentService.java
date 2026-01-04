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

    // Get ONLY this customer's payments
    public List<RecurringPayment> getPayments() {
        return scheduler.getPaymentsForCustomer(customerVatID);
    }

    // Create a new recurring payment for this customer
    public void addPayment(String rfCode, String iban, double amount,String vatid) throws IOException {
        RecurringPayment rp = new RecurringPayment(
        rfCode,
        vatid,   // ← stored in service
        iban,
        amount,
        TimeService.getInstance().today()
        );
        scheduler.addRecurringPayment(rp);

    }
    

    // Pause a payment
    public boolean pausePayment(String rfCode) throws IOException {
        RecurringPayment payment = findPayment(rfCode);
        if (payment != null && !payment.isPaused()) {
            payment.pause();
            scheduler.save();
            return true;
        }
        return false;
    }

    // Resume a payment
    public boolean resumePayment(String rfCode) throws IOException {
        RecurringPayment payment = findPayment(rfCode);
        if (payment != null && payment.isPaused()) {
            payment.resume();
            scheduler.save();
            return true;
        }
        return false;
    }

    // Cancel a payment
    public boolean cancelPayment(String rfCode) throws IOException {
        return scheduler.cancelPayment(rfCode, customerVatID);
    }

    // Daily auto-check (optional: call at login)
    public void processDuePayments() throws IOException {
        scheduler.dailyCheck(); // safe: only affects due payments
    }

    // Helper
    private RecurringPayment findPayment(String rfCode) {
        return getPayments().stream()
            .filter(p -> p.getRfCode().equals(rfCode))
            .findFirst()
            .orElse(null);
    }
}