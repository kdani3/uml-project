package BankOfTuc.Auth;
//simple listener for login states
public interface LoginListener {
    void onLogin(String username);
    void onLogout(String username);
    void onTimeout(String username);
}
