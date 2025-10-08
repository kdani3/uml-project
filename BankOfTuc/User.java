package BankOfTuc;

import java.util.Base64;

//The User must have a username,password, fullname, email(optional), isActive boolean and an ENUM for the Role(extends in each role appropriately)
public  class User {
    String username;
    String fullname;
    String email;
    boolean isActive;  
    Role role;
    String saltBase64;

    String hashedPassword;
    byte[] salt;//salt for verification afterwards

    public static enum Role {
        ADMIN,
        INDIVIDUAL,
        COMPANY
    }

    public User(String username, String password, String fullname, String email, Role role, boolean isActive) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.isActive = isActive;

        this.salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);

        //password hashing
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(), this.salt);
    }

    public User() {} // for deserialization

    //password verification
    public boolean verifyPassword(String inputPassword) {
        return PasswordUtils.verifyPassword(inputPassword.toCharArray(), this.salt, this.hashedPassword);
    }

    public String toJSON() {
        return "{"
                + "\"username\":\"" + username + "\","
                + "\"fullname\":\"" + (fullname != null ? fullname : "") + "\","
                + "\"email\":\"" + (email != null ? email : "") + "\","
                + "\"isActive\":" + isActive + ","
                + "\"role\":\"" + role + "\","
                + "\"hashedPassword\":\"" + hashedPassword + "\","
                + "\"saltBase64\":\"" + saltBase64 + "\""
                + "}";
    }

    
}
