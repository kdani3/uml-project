package BankOfTuc;

public class IndividualCustomer extends Customer {
    
    public IndividualCustomer( String username, String passwordToHash, String fullname, String vatID, String email, boolean isActive){
        super(  username,  passwordToHash,  fullname, vatID, email,  Role.INDIVIDUAL,  isActive);
    }
    public IndividualCustomer(){ this.setRole(Role.INDIVIDUAL);}
}
