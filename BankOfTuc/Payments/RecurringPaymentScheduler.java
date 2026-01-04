// File: BankOfTuc/Payments/RecurringPaymentScheduler.java
package BankOfTuc.Payments;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public class RecurringPaymentScheduler {
    private List<RecurringPayment> allPayments;
    private final CustomerFileManager cfm;

    public RecurringPaymentScheduler(CustomerFileManager cfm) throws IOException {
        this.cfm = cfm;
        this.allPayments = RecurringPaymentCsvStore.load();
    }

    // Save all payments back to CSV
    public void save() throws IOException {
        RecurringPaymentCsvStore.save(allPayments);
    }

    // Add a new recurring payment
    public void addRecurringPayment(RecurringPayment payment) throws IOException {
        allPayments.add(payment);
        save();
    }

    // Run daily check for ALL payments (e.g., at system startup or via background task)
    public void dailyCheck() throws IOException {
        boolean changed = false;
        for (RecurringPayment rp : allPayments) {
            // Resolve BankAccount for this payment
            BankAccount account = RecurringPaymentCsvStore.resolveBankAccount(rp, cfm);
            if (account != null) {
                // Temporarily attach account or use an overloaded method
                boolean success = rp.attemptIfDue(cfm);
                if (success) {
                    changed = true;
                }
            }
        }
        if (changed) {
            save(); // Only save if state changed (attempts, nextDueDate, etc.)
        }
    }

    // Get payments for a specific customer (e.g., for CLI display)
    public List<RecurringPayment> getPaymentsForCustomer(String vatId) {
        List<RecurringPayment> customerPayments = new ArrayList<>();
        for (RecurringPayment rp : allPayments) {
            if (rp.getPayerVatID().equals(vatId)) {
                customerPayments.add(rp);
            }
        }
        return customerPayments;
    }

    // Cancel a payment (e.g., from CLI)
    public boolean cancelPayment(String rfCode, String vatId) throws IOException {
        boolean removed = allPayments.removeIf(p -> 
            p.getRfCode().equals(rfCode) && p.getPayerVatID().equals(vatId)
        );
        if (removed) {
            save();
        }
        return removed;
    }
}