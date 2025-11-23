package BankOfTuc;

import java.util.Base64;
import BankOfTuc.auth.PasswordUtils;

//The User must have a username,password, fullname, email(optional), isActive boolean and an ENUM for the Role(extends in each role appropriately)
public  class User {
    private int id;
    private String username;
    private String fullname;
    private String email;
    private boolean isActive;  
    private Role role;
    private String saltBase64;

    private String hashedPassword;
    private String qrSecret;

    //private byte[] salt;
    
    public void setid(int id){
        this.id = id;
    }

    public int getid(){
        return id;
    }

    public void setRole(Role role){
        this.role = role;
    }
    public Role getRole(){
        return role;
    }

    public void setActive(boolean active){
        this.isActive = active;
    }

    public boolean getActive(){
        return isActive;
    }
    public String getHashedPassword(){
        return hashedPassword;
    }

    public byte[] getSalt() {
        byte [] salt = null;
        if (saltBase64 != null) {
            salt = Base64.getDecoder().decode(saltBase64);
        }
        return salt.clone();
    }

    public void setQrCode(String qrSecret ){
        this.qrSecret = qrSecret;
    }
    
    public String getQrCode(){
        return qrSecret;
    }
    
    public String getFullname(){
        return fullname;
    }

    public void setFullname(String fulname){
        this.fullname = fulname;
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
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password){
        byte [] salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(), salt);

    }

    public boolean hasQR(){
        return !(qrSecret == null || qrSecret.trim().isEmpty() || qrSecret.trim().equalsIgnoreCase("null"));
    }

    public User(String username, String password, String fullname, String email, Role role, boolean isActive) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        
        byte[] salt = PasswordUtils.generateSalt();
        this.saltBase64 = Base64.getEncoder().encodeToString(salt);
        this.qrSecret = null;
        //password hashing
        this.hashedPassword = PasswordUtils.hashPassword(password.toCharArray(),salt);
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
