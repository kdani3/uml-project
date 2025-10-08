package BankOfTuc;

public class Customer extends User{
    String vatID;
    public Customer( String username, String passwordToHash, String fullname,String vatID, String email, Role role, boolean isActive)
    {
        super( username,  passwordToHash,  fullname,  email,  role,  isActive);
        this.vatID=vatID;

    }
    public Customer(){}

    @Override
    public String toJSON() {
        return "{"
            + "\"username\":\"" + username + "\","
            + "\"fullname\":\"" + (fullname != null ? fullname : "") + "\","
            + "\"vatID\":\"" + (vatID != null ? vatID : "") + "\","
            + "\"email\":\"" + (email != null ? email : "") + "\","
            + "\"isActive\":" + isActive + ","
            + "\"role\":\"" + role + "\","
            + "\"hashedPassword\":\"" + hashedPassword + "\","
            + "\"saltBase64\":\"" + saltBase64 + "\""
            + "}";
    }
}