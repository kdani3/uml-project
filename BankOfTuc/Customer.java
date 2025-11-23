package BankOfTuc;

import java.util.ArrayList;
import java.util.List;

import BankOfTuc.accounting.BankAccount;

public class Customer extends User{
    private String vatID;
    private List<BankAccount> bankAccounts;  
    
    public Customer( String username, String passwordToHash, String fullname,String vatID, String email, Role role, boolean isActive)
    {
        super( username,  passwordToHash,  fullname,  email,  role,  isActive);
        this.vatID=vatID;
        bankAccounts = new ArrayList<>();
    }
    public Customer(){}

    public void addBankAccount(BankAccount account){
        this.bankAccounts.add(account);
    }
    public String getVatID() {
        return vatID;
    }
    public void setVatID(String vatID) {
        this.vatID = vatID;
    }
    public List<BankAccount> getBankAccounts() {
        return bankAccounts;
    }
    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }
}