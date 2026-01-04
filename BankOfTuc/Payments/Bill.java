package BankOfTuc.Payments;

import java.time.LocalDate;

public class Bill {
    String Billid;
    String rfcode;
    double monthlyAmount;
    double totalAmount;
    double paidAmount;
    LocalDate issueDate;
    LocalDate dueDate;
    boolean isPaid;
    LocalDate payDate;
    int installments;
    int paidInstallments;
    String issuerUsername;
    String payerID;
    BillStatus status;

    public static enum BillStatus {
        ACTIVE, PAID, FROZEN, MONTHLY_PAID, EXPIRED, CANCELLED, PARTIALLY_PAID
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Bill(String Billid, double amount, LocalDate issueDate, LocalDate dueDate, int installments, String issuerUsername, String payeeString) {
        this.Billid = Billid;
        this.rfcode = BillUtils.generateRFNumeric();
        this.totalAmount = round(amount); 
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.installments = installments;
        this.isPaid = false;
        this.status = BillStatus.ACTIVE;
        this.issuerUsername = issuerUsername;
        this.payerID = payeeString;
        this.monthlyAmount = round(this.totalAmount / (double)installments);
    }

    public void setAmount(double amount) {
        this.totalAmount = round(amount); 
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = round(paidAmount); 
        if (Math.abs(this.totalAmount - this.paidAmount) < 0.005) {
            this.paidAmount = this.totalAmount;
            this.isPaid = true;
            this.status = BillStatus.PAID;
        }
    }

    public double getAmount() { return totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public String getBillid() { return Billid; }
    public void setBillid(String billid) { Billid = billid; }
    public String getRfcode() { return rfcode; }
    public void setRfcode(String rfcode) { this.rfcode = rfcode; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean isPaid) { this.isPaid = isPaid; }
    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }
    public LocalDate getPayDate() { return payDate; }
    public void setPayDate(LocalDate payDate) { this.payDate = payDate; }
    public int getInstallments() { return installments; }
    public void setInstallments(int installments) { this.installments = installments; }
    public int getPaidInstallments() { return paidInstallments; }
    public void setPaidInstallments(int paidInstallments) { this.paidInstallments = paidInstallments; }
    public String getIssuerUsername() { return issuerUsername; }
    public void setIssuerUsername(String issuerUsername) { this.issuerUsername = issuerUsername; }
    public double getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(double monthlyAmount) { this.monthlyAmount = round(monthlyAmount); }
    public String getpayerID() { return payerID; }
    public void setpayerID(String payerID) { this.payerID = payerID; }
}
