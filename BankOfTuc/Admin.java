package BankOfTuc;

public class Admin extends User{

    
    public Admin(String username, String passwordToHash, String fullname, String email, boolean isActive)
        {
            super(username,  passwordToHash,  fullname,  email,  Role.ADMIN,  isActive);

        }
    public Admin(){ this.setRole(Role.ADMIN);}

}
