package BankOfTuc;

public class Customer extends User{
    String vatID;
    public Customer( String username, String passwordToHash, String fullname,String vatID, String email, Role role, boolean isActive)
    {
        super( username,  passwordToHash,  fullname,  email,  role,  isActive);
        this.vatID=vatID;

    }
    public Customer(){}

}