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
import BankOfTuc.Services.TimeService;

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
        try {
            // reload fresh bill to avoid stale state
            Bill fresh = BillFileStore.findByRFCode(this.rf);
            if (fresh == null) return false;
            this.bill = fresh;
        } catch (IOException e) {
            return false;
        }

        IndividualCustomer customer = (IndividualCustomer) cfm.getCustomerByIBAN(account.getIban());
        CompanyCustomer company = (CompanyCustomer) cfm.getCustomerByUsername(bill.getIssuerUsername());
        if (company == null || customer == null) return false;
        List<BankAccount> compaccs = company.getBankAccounts();
        if (compaccs == null || compaccs.isEmpty()) return false;

        // Enforce that only the designated payer can pay this bill
        String designatedPayerVat = bill.getpayerID();
        if (designatedPayerVat != null && !designatedPayerVat.isEmpty()) {
            if (!designatedPayerVat.equals(customer.getVatID())) {
                return false;
            }
        }

        // Reject frozen, already paid, or expired bills
        if (bill.getStatus() == BillStatus.FROZEN || bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.EXPIRED) {
            return false;
        }

        // Prevent paying more than remaining amount
        double remaining = bill.getAmount() - bill.getPaidAmount();
        if (amount <= 0 || amount > remaining) return false;

        // Check available funds before mutating
        if (account.getBalance() < amount) return false;

        // Perform transfer: debit payer, credit company
        account.reduceBalance(amount);
        BankAccount compacc = compaccs.get(0);
        compacc.addBalance(amount);

        // Update bill state
        double newPaid = bill.getPaidAmount() + amount;
        bill.setPaidAmount(newPaid);

        // Recompute paidInstallments deterministically from paid amount to avoid double-counting
        if (Math.abs(newPaid - bill.getAmount()) < 0.0001) {
            bill.setStatus(BillStatus.PAID);
            bill.setPaid(true);
            bill.setPaidInstallments(bill.getInstallments());
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
            if (bill.getMonthlyAmount() > 0 && bill.getInstallments() > 1) {
                int computed = (int) Math.floor(bill.getPaidAmount() / bill.getMonthlyAmount());
                if (computed < 0) computed = 0;
                if (computed > bill.getInstallments()) computed = bill.getInstallments();
                // ensure at least one installment counted for a non-zero paid amount
                if (computed == 0 && bill.getPaidAmount() > 0) computed = 1;
                bill.setPaidInstallments(computed);
            } else {
                // Non-monthly bills: mark 1 installment as paid for any partial payment
                bill.setPaidInstallments(Math.min(bill.getInstallments(), Math.max(1, bill.getPaidInstallments())));
            }
        }

        this.date = TimeService.getInstance().today();
        bill.setPayDate(date);

        // Persist changes — ensure bill persistence succeeds, otherwise rollback balances
        cfm.updateCustomer(customer);
        cfm.updateCustomer(company);
        try {
            BillFileStore.updateBill(bill);
        } catch (IOException e) {
            // rollback balances
            compacc.addBalance(-amount);
            account.addBalance(amount);
            // persist rollback
            try { cfm.updateCustomer(customer); } catch (Exception ignored) {}
            try { cfm.updateCustomer(company); } catch (Exception ignored) {}
            return false;
        }

        PaymentLogger.logTransfer(customer.getVatID(), account.getIban(), bill.getRfcode(), "ONE-TIME", company.getVatID(), compacc.getIban(), bill.getAmount(), bill.getPaidAmount(), bill.getStatus().toString(), account.getBalance(), compacc.getBalance(), bill.getPaidInstallments());
        return true;
    }
}
