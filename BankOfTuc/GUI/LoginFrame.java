package BankOfTuc.GUI;

import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private final LoginManager loginManager;

    public LoginFrame(UserFileManagement ufm, CustomerFileManager cfm) {
        this.ufm = ufm;
        this.cfm = cfm;
        this.loginManager = new LoginManager(ufm);

        setTitle("Bank of Tuc - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Κέντρο οθόνης
        setLayout(new GridBagLayout());

        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label & Field
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);

        JTextField txtUsername = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        add(txtUsername, gbc);

        // Password Label & Field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);

        JPasswordField txtPassword = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            // Κλήση της σωστής μεθόδου login που επιστρέφει int
            int status = loginManager.login(username, password);

            switch (status) {
                case 1: // Επιτυχία (Normal Login)
                    proceedToDashboard(username);
                    break;
                
                case 2: // Σωστά στοιχεία, αλλά απαιτείται QR Code
                    handleQrLogin(username);
                    break;
                
                case 3: // Ήδη συνδεδεμένος
                    JOptionPane.showMessageDialog(this, 
                        "User is already logged in. Please logout from other sessions first.", 
                        "Login Error", JOptionPane.WARNING_MESSAGE);
                    break;
                
                case 4: // Ανενεργός χρήστης
                    JOptionPane.showMessageDialog(this, 
                        "This account is deactivated.", 
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                    break;
                
                case 0: // Λάθος στοιχεία
                default:
                    JOptionPane.showMessageDialog(this, 
                        "Invalid username or password.", 
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        });
    }

    // Βοηθητική μέθοδος για το QR Login
    private void handleQrLogin(String username) {
        String qrCode = JOptionPane.showInputDialog(this, "Enter 2FA QR Code:");
        
        if (qrCode != null && !qrCode.trim().isEmpty()) {
            if (loginManager.qrCodeLogin(username, qrCode.trim())) {
                proceedToDashboard(username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid QR Code.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Άνοιγμα του Dashboard
    private void proceedToDashboard(String username) {
        User user = ufm.getUserByUsername(username);
        if (user != null) {
            new MainDashboardFrame(user, ufm, cfm).setVisible(true);
            this.dispose(); // Κλείσιμο του Login παραθύρου
        }
    }
}