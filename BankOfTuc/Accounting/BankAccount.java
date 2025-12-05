package BankOfTuc.Accounting;


public class BankAccount {
    private String holderVatID;
    private String iban;
    private double balance;

    public BankAccount(String holderVatID) {
        this.holderVatID = holderVatID;
        this.iban = IBANUtils.generateIBAN();
        this.balance = 0;
    }

    public BankAccount() {}

    public String getHolderID() {
        return holderVatID;
    }

    public void setHolderID( String holderVatID) {
        this.holderVatID = holderVatID;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addBalance(double balance){
        this.balance += balance;
    }

    public void reduceBalance(double amount){
        this.balance -= amount;
    }
  
}
