package BankOfTuc.Accounting;


public class BankAccount {
    public enum AccountType { CHECKING, SAVINGS, COMPANY }

    private String holderVatID;
    private String iban;
    private double balance;
    private AccountType type;

    public BankAccount(String holderVatID, AccountType type) {
        this.holderVatID = holderVatID;
        this.iban = IBANUtils.generateIBAN();
        this.balance = 0;
        this.type = type;
    }

    // default constructor for deserializers
    public BankAccount() {
    }

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

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f) [%s]", iban != null ? iban : "", balance, type != null ? type.name() : BankAccount.AccountType.CHECKING.name());
    }
  
}
