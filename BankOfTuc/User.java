package BankOfTuc;

import java.util.Base64;
import BankOfTuc.auth.PasswordUtils;

//The User must have a username,password, fullname, email(optional), isActive boolean and an ENUM for the Role(extends in each role appropriately)
public abstract class User {

    protected String username;
    protected String fullname;
    protected String email;
    protected boolean isActive;  
    protected Role role;
    protected String saltBase64;

    protected String hashedPassword;
    protected String qrSecret;

    protected byte[] salt;//salt for verification afterwards
    
    public String getHashedPassword(){
        return hashedPassword;
    }

    public byte[] getSalt() {
        if (salt == null && saltBase64 != null) {
            salt = Base64.getDecoder().decode(saltBase64);
        }
        return salt.clone();
    }

    public void setQrCode(String qrSecret ){
        this.qrSecret = qrSecret;
        JSONUtils.updateUser(this);
    }
    
    public String getQrCode(){
        return qrSecret;
    }
    public static enum Role {
        ADMIN,
        INDIVIDUAL,
        COMPANY
    }

      public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        JSONUtils.updateUser(this);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        JSONUtils.updateUser(this);
    }
    
    public void setPassword(String password){
        this.salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(), this.salt);

        JSONUtils.updateUser(this);
    }

    public User(String username, String password, String fullname, String email, Role role, boolean isActive) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.isActive = isActive;

        this.salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);
        this.qrSecret = null;
        //password hashing
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(), this.salt);
    }


      public User(String username, String password, String qrSecret ,String fullname, String email, Role role, boolean isActive) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.isActive = isActive;

        this.salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);

        //password hashing
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(), this.salt);
        this.qrSecret = qrSecret;
    }

    public User() {} // for deserialization

   


   public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
        .append("\"username\":\"").append(username).append("\",")
        .append("\"fullname\":\"").append(fullname != null ? fullname : "").append("\",")
        .append("\"email\":\"").append(email != null ? email : "").append("\",")
        .append("\"isActive\":").append(isActive).append(",")
        .append("\"role\":\"").append(role).append("\",")
        .append("\"hashedPassword\":\"").append(hashedPassword).append("\",")
        .append("\"saltBase64\":\"").append(saltBase64).append("\",")
        .append("\"qrSecret\":\"").append(qrSecret).append("\"")
        .append("}");
        return sb.toString();
    }


    
}
