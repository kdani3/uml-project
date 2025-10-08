package BankOfTuc;

public class CompanyCustomer extends Customer{
   public CompanyCustomer( String username, String passwordToHash, String fullname,String vatID, String email, boolean isActive){
        super( username,  passwordToHash,  fullname,  vatID, email,  Role.COMPANY,  isActive);

    }

    public CompanyCustomer(){ this.role = Role.COMPANY;}
}
