package BankOfTuc;

public class IndividualCustomer extends Customer {
    
    public IndividualCustomer( String username, String passwordToHash, String fullname, String vatID, String email, boolean isActive){
        super(  username,  passwordToHash,  fullname, vatID, email,  Role.INDIVIDUAL,  isActive);
    }
    public IndividualCustomer(){ this.setRole(Role.INDIVIDUAL);}

    @Override
    public boolean addBankAccount(BankOfTuc.Accounting.BankAccount account) {
        if (getBankAccounts().size() >= 5) {
            return false; // max 5 accounts for individuals
        }

        var type = account.getType();
        if (type == null) return false;
        if (type == BankOfTuc.Accounting.BankAccount.AccountType.COMPANY) {
            return false; // individuals cannot have COMPANY type
        }

        return super.addBankAccount(account);
    }
}

