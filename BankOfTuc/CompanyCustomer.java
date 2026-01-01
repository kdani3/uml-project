package BankOfTuc;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.BillFileStore;

public class CompanyCustomer extends Customer{
   public CompanyCustomer( String username, String passwordToHash, String fullname,String vatID, String email, boolean isActive){
        super( username,  passwordToHash,  fullname,  vatID, email,  Role.COMPANY,  isActive);

    }

    public CompanyCustomer(){ this.setRole(Role.COMPANY);}

    @Override
    public boolean addBankAccount(BankOfTuc.Accounting.BankAccount account) {
        if (getBankAccounts().size() >= 1) {
            return false; // only 1 account allowed for companies
        }

        var type = account.getType();
        if (type == null) return false;
        if (type != BankOfTuc.Accounting.BankAccount.AccountType.COMPANY) {
            return false; // companies only have COMPANY account type
        }

        return super.addBankAccount(account);
    }



    public boolean issueBill(double amount,LocalDate dueDate,int installments,String payerID){

        int companyBills;
        try {
            if(getBankAccounts().isEmpty())
                return false;
            // enforce at least 1 installment
            if (installments < 1) installments = 1;

            List<Bill> bills = BillFileStore.loadBills();
            companyBills = BillFileStore.getCompanyBillsNum(getVatID(),bills);
            String newBillID = getVatID()+(companyBills+1);
            Bill bill = new Bill(newBillID,amount,TimeService.getInstance().today(),dueDate,installments,getUsername(),payerID);
            BillFileStore.saveBill(bill);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean issueBill(double amount,double monthlyAmount,LocalDate dueDate,int installments,String payerID){

        int companyBills;
        try {
            // enforce at least 1 installment
            if (installments < 1) installments = 1;

            List<Bill> bills = BillFileStore.loadBills();
            companyBills = BillFileStore.getCompanyBillsNum(getVatID(),bills);
            String newBillID = getVatID()+(companyBills+1);
            Bill bill = new Bill(newBillID,amount,TimeService.getInstance().today(),dueDate,installments,getUsername(),payerID);
            bill.setMonthlyAmount(monthlyAmount);
            BillFileStore.saveBill(bill);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
