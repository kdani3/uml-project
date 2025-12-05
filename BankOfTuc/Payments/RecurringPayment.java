// File: BankOfTuc/Payments/RecurringPayment.java
package BankOfTuc.Payments;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Objects;

import BankOfTuc.Customer;
import BankOfTuc.EmailUtils;
import BankOfTuc.EnvReader;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
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
    private double standardFee = 2.5;
    private double maxFee = 3.5;
    // New: support for pause/resume
    private boolean paused = false;
    //private boolean active = true;

    public RecurringPayment(String rfCode, BankAccount payerAccount, double monthlyAmount, LocalDate startDate) {
        this.rfCode = Objects.requireNonNull(rfCode);
        this.payerAccount = Objects.requireNonNull(payerAccount);
        this.payerVatID = payerAccount.getHolderID();
        this.payerIban = payerAccount.getIban();
        this.monthlyAmount = monthlyAmount;
        this.nextDueDate = startDate.with(TemporalAdjusters.firstDayOfNextMonth());
    }

     public RecurringPayment(String rfCode, String payerVatID, String payerIban, double monthlyAmount, LocalDate nextDueDate) {
        this.rfCode = rfCode;
        this.payerVatID = payerVatID;
        this.payerIban = payerIban;
        this.monthlyAmount = monthlyAmount;
        this.nextDueDate = nextDueDate;
    }

    // Main method: called daily (e.g., at login or by scheduler)
    public boolean attemptIfDue(CustomerFileManager cfm) {
        if ( paused ) {
            return false;
        }

        if (!LocalDate.now().isBefore(nextDueDate)) {
            boolean success = executePayment(cfm);
            currentAttempts++;

            if (success) {
                nextDueDate = nextDueDate.with(TemporalAdjusters.firstDayOfNextMonth());
                currentAttempts = 0;
                return true;
            } /* else if (currentAttempts >= maxAttempts) {
                active = false;
            } */
            // Keep due date same for retry
            return success;
        }
        return false;
    }

    private void sendFailedPaymentEmail(Customer customer,String rfCode,double amount){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String attemptDateTime = now.format(formatter); 
        Map<String, String> env = null;
        try {
            env = EnvReader.loadEnv(".env");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        String api_key = env.get("MAIL_API_KEY");

        String html = EmailUtils.recurringPaymentFailedHTML(customer.getFullname(), rfCode, attemptDateTime, amount);

        try {
            EmailUtils.sendEmail(api_key, "info@bankoftuc.denmoukaneito.click", "Bank Of Tuc", 
                    customer.getEmail(), "Export Statement",html);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    private boolean executePayment(CustomerFileManager cfm) {
        try {
            if(payerAccount==null){
                this.payerAccount = cfm.findAccountByIBAN(payerIban);
            }
            Bill bill = BillFileStore.findByRFCode(rfCode);
            if (bill == null) return false;
            BillStatus status = bill.getStatus();

            if (status.equals(BillStatus.CANCELLED)||status.equals(BillStatus.FROZEN)||status.equals(BillStatus.PAID)){
                return false;
            }
            
            var payer = cfm.getCustomerByIBAN(payerAccount.getIban());
            var company = cfm.getCustomerByUsername(bill.getIssuerUsername());
            if (payer == null || company == null) return false;

            if (payerAccount.getBalance() < monthlyAmount+standardFee){
                sendFailedPaymentEmail( payer, rfCode, monthlyAmount+standardFee);
                currentAttempts++;  
            } 
            
            if(currentAttempts<=maxAttempts){
                payerAccount.reduceBalance(monthlyAmount+standardFee);
            }
            else{
                if(payerAccount.getBalance() > monthlyAmount+maxFee){
                    payerAccount.reduceBalance(monthlyAmount+maxFee);
                }
                else{
                    sendFailedPaymentEmail( payer, rfCode, monthlyAmount+standardFee);
                    currentAttempts++;  
                    return false;
                }
            }

            // Perform payment (same logic as your Payment.pay())
            var compAccount = company.getBankAccounts().get(0);
            compAccount.addBalance(monthlyAmount);

            double newPaid = bill.getPaidAmount() + monthlyAmount;
            bill.setPaidAmount(newPaid);
            bill.setPayDate(LocalDate.now());
            bill.setPaidInstallments(bill.getPaidInstallments()+1);

            if (newPaid == bill.getAmount()&& bill.getInstallments()==bill.getPaidInstallments()) {
                bill.setStatus(BillStatus.PAID);
                bill.setPaid(true);
            } else if(bill.getMonthlyAmount()<monthlyAmount){
                bill.setStatus(BillStatus.PARTIALLY_PAID);
            }
            else {
                bill.setStatus(BillStatus.MONTHLY_PAID);
            }

            cfm.updateCustomer(payer);
            cfm.updateCustomer(company);
            PaymentLogger.logTransfer(payer.getVatID(), payerAccount.getIban(),bill.getRfcode(), "MONTHLY", company.getVatID(), compAccount.getIban(), bill.getAmount(), bill.getPaidAmount(), bill.getStatus().toString(), payerAccount.getBalance(), compAccount.getBalance(),bill.getPaidInstallments());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // === PAUSE / RESUME ===
    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
        this.currentAttempts = 0; // reset attempts on resume
    }

    // === Getters ===
    public String getPayerVatID() { return payerVatID; }
    public String getRfCode() { return rfCode; }
    public BankAccount getPayerAccount() { return payerAccount; }
    public double getmonthlyAmount() { return monthlyAmount; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public int getCurrentAttempts() { return currentAttempts; }
    public boolean isPaused() { return paused; }
    public String getPayerIban() {
        return payerIban;
    }

    public void setPayerIban(String payerIban) {
        this.payerIban = payerIban;
    }

    //public boolean isActive() { return active; }

    // For CSV loading
    public void restoreState(LocalDate nextDue, int attempts, boolean paused) {
        this.nextDueDate = nextDue;
        this.currentAttempts = attempts;
        this.paused = paused;
        //this.active = active;
    }
}