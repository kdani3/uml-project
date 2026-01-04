package BankOfTuc.Payments;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Objects;

import BankOfTuc.Customer;
import BankOfTuc.FileIO.EmailUtils;
import BankOfTuc.FileIO.EnvReader;
import BankOfTuc.Services.TimeService;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.BankRevenue; 
import BankOfTuc.Logging.PaymentLogger;
import BankOfTuc.Payments.Bill.BillStatus;

public class RecurringPayment {
    
    private final String rfCode;
    private final String payerVatID;
    private BankAccount payerAccount;
    private String payerIban;
    
    private final double monthlyAmount;
    private LocalDate nextDueDate;
    private int maxAttempts = 3;
    private int currentAttempts = 0;
    private final double standardFee = 2.5;
    private final double maxFee = 3.5;
    private boolean paused;


    public RecurringPayment(String rfCode, String payerVatID, String payerIban, double monthlyAmount, LocalDate nextDueDate) {
        this.rfCode = rfCode;
        this.payerVatID = payerVatID;
        this.payerIban = payerIban;
        this.monthlyAmount = round(monthlyAmount);
        this.nextDueDate = nextDueDate;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public boolean attemptIfDue(CustomerFileManager cfm) {
        if (paused) return false;

        if (!TimeService.getInstance().today().isBefore(nextDueDate)) {
            boolean success = executePayment(cfm);

            if (success) {
                if (paused) return true; 
                nextDueDate = nextDueDate.with(TemporalAdjusters.firstDayOfNextMonth());
                currentAttempts = 0;
                return true;
            } else {
                if (!paused) currentAttempts++;
                return false;
            }
        }
        return false;
    }

    private void sendFailedPaymentEmail(Customer customer, String rfCode, double amount) {
        LocalDateTime now = TimeService.getInstance().now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String attemptDateTime = now.format(formatter); 
        try {
            Map<String, String> env = EnvReader.loadEnv(".env");
            String api_key = env.get("MAIL_API_KEY");
            String html = EmailUtils.recurringPaymentFailedHTML(customer.getFullname(), rfCode, attemptDateTime, amount);
            EmailUtils.sendEmail(api_key, "info@bankoftuc.denmoukaneito.click", "Bank Of Tuc", 
                    customer.getEmail(), "Failed Recurring Payment", html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean executePayment(CustomerFileManager cfm) {
        try {
            if (payerAccount == null) {
                this.payerAccount = cfm.findAccountByIBAN(payerIban);
            }
            
            Bill bill = BillFileStore.findByRFCode(rfCode);
            if (bill == null) return false;
            
            double billTotal = round(bill.getAmount());
            double billPaid = round(bill.getPaidAmount());
            double remainingTotal = round(billTotal - billPaid);

            if (bill.isPaid() || bill.getStatus() == BillStatus.PAID || 
                remainingTotal < 0.01 || bill.getPaidInstallments() >= bill.getInstallments()) {
                
                this.pause();
                bill.setPaid(true);
                bill.setStatus(BillStatus.PAID);
                BillFileStore.updateBill(bill);
                return false;
            }

            if (bill.getStatus() == BillStatus.CANCELLED || bill.getStatus() == BillStatus.FROZEN) {
                return false;
            }
            
            var payer = cfm.getCustomerByIBAN(payerAccount.getIban());
            var company = cfm.getCustomerByUsername(bill.getIssuerUsername());
            if (payer == null || company == null) return false;

            double paymentAmount = round(Math.min(this.monthlyAmount, remainingTotal));
            
         
            double fee = (currentAttempts <= maxAttempts) ? standardFee : maxFee;
            double totalCharge = round(paymentAmount + fee);

            if (payerAccount.getBalance() < totalCharge) {
                sendFailedPaymentEmail(payer, rfCode, totalCharge);
                return false;
            }

            payerAccount.reduceBalance(totalCharge);
            var compAccount = company.getBankAccounts().get(0);
            compAccount.addBalance(paymentAmount);

            BankRevenue.addFee(fee); 

            double updatedPaidTotal = round(billPaid + paymentAmount);
            bill.setPaidAmount(updatedPaidTotal);
            bill.setPayDate(TimeService.getInstance().today());
            bill.setPaidInstallments(bill.getPaidInstallments() + 1);

            if (round(billTotal - updatedPaidTotal) < 0.01 || bill.getPaidInstallments() >= bill.getInstallments()) {
                bill.setPaidAmount(billTotal);
                bill.setStatus(BillStatus.PAID);
                bill.setPaid(true);
                this.pause();
            } else {
                bill.setStatus(BillStatus.MONTHLY_PAID);
            }

            cfm.updateCustomer(payer);
            cfm.updateCustomer(company);
            BillFileStore.updateBill(bill);

            PaymentLogger.logTransfer(payer.getVatID(), payerAccount.getIban(), bill.getRfcode(), "MONTHLY", 
                company.getVatID(), compAccount.getIban(), paymentAmount, bill.getPaidAmount(), 
                bill.getStatus().toString(), payerAccount.getBalance(), compAccount.getBalance(), bill.getPaidInstallments());
            
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void pause() { this.paused = true; }
    public void resume() { this.paused = false; }
    public String getPayerVatID() { return payerVatID; }
    public String getRfCode() { return rfCode; }
    public BankAccount getPayerAccount() { return payerAccount; }
    public double getmonthlyAmount() { return monthlyAmount; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public int getCurrentAttempts() { return currentAttempts; }
    public boolean isPaused() { return paused; }
    public String getPayerIban() { return payerIban; }
    public void setPayerIban(String payerIban) { this.payerIban = payerIban; }

    public void restoreState(LocalDate nextDue, int attempts, boolean paused) {
        this.nextDueDate = nextDue;
        this.currentAttempts = attempts;
        this.paused = paused;
    }
}