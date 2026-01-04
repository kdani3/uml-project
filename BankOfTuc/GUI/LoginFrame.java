package BankOfTuc.GUI;

import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame {
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private final LoginManager loginManager;

    // Colors
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color TEXT_COLOR = new Color(50, 50, 50);

    public LoginFrame(UserFileManagement ufm, CustomerFileManager cfm) {
        this.ufm = ufm;
        this.cfm = cfm;
        this.loginManager = new LoginManager(ufm);

        setTitle("TUC Bank - Secure Login");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout: Split 50% - 50%
        setLayout(new MigLayout("fill, insets 0, gap 0", "[50%][50%]", "[grow]"));

        initComponents();
    }

    private void initComponents() {
        // --- LEFT PANEL (Red Branding) ---
        JPanel leftPanel = new JPanel(new MigLayout("fill, wrap 1, insets 50", "[center]", "[center]"));
        leftPanel.setBackground(BRAND_COLOR);

        // --- 0. ASCII Logo ---
        String logo ="""    
                      
        ###################################                                
       #################################                                
                            ######                                      
           **************** ###### *** ************* ######   ** ###### ******** ##              
                ********* ####### ** ****** #####   #####  ##  ##           
                   ****** ######       **** ##  ##  ## ##  #####            
                          ######  ** ****** #### #  ##  #  ##  ## #####           
                          ######   """;

        JTextArea asciiLogo = new JTextArea(logo);
        asciiLogo.setFont(new Font("Monospaced", Font.BOLD, 10)); 
        asciiLogo.setForeground(new Color(255, 255, 255, 180));   
        asciiLogo.setOpaque(false);                               
        asciiLogo.setEditable(false);
        asciiLogo.setFocusable(false);
        
        leftPanel.add(asciiLogo, "gapbottom 20"); 

        // 1. Brand Name
        JLabel lblBrand = new JLabel("TUC Bank");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblBrand.setForeground(Color.WHITE);
        leftPanel.add(lblBrand, "gapbottom 10");

        // 2. Slogan
        JLabel lblSlogan = new JLabel("Τραπεζικές υπηρεσίες απλές και ασφαλείς");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSlogan.setForeground(new Color(255, 255, 255, 220));
        leftPanel.add(lblSlogan, "gapbottom 40");

        // 3. List
        JPanel listPanel = new JPanel(new MigLayout("wrap 1, insets 0", "[left]"));
        listPanel.setOpaque(false);
        addBulletPoint(listPanel, "24/7 Πρόσβαση στους λογαριασμούς σας");
        addBulletPoint(listPanel, "Ασφαλείς συναλλαγές");
        addBulletPoint(listPanel, "Άμεσες ειδοποιήσεις");

        leftPanel.add(listPanel);
        add(leftPanel, "grow");

        // --- RIGHT PANEL (White Login Form) ---
        JPanel rightPanel = new JPanel(new MigLayout("fill, wrap 1, insets 50 80 50 80", "[fill]", "[center]"));
        rightPanel.setBackground(Color.WHITE);

        // Header
        JLabel lblLoginTitle = new JLabel("Καλωσήρθατε");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        lblLoginTitle.setForeground(Color.GRAY);
        rightPanel.add(lblLoginTitle, "gapbottom 5");

        // Subheader
        JLabel lblLoginSub = new JLabel("Συνδεθείτε στον λογαριασμό σας");
        lblLoginSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLoginSub.setForeground(Color.GRAY);
        rightPanel.add(lblLoginSub, "gapbottom 30");

        // Field 1: Username or Email
        JLabel lblUser = new JLabel("Username or Email");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(TEXT_COLOR);
        rightPanel.add(lblUser);

        JTextField txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        rightPanel.add(txtInput, "h 40!, gapbottom 15");

        // Field 2: Password
        JLabel lblPass = new JLabel("Κωδικός");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_COLOR);
        rightPanel.add(lblPass);

        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        rightPanel.add(txtPassword, "h 40!, gapbottom 20");

        // Button: LOGIN
        JButton btnLogin = new JButton("Σύνδεση");
        stylePrimaryButton(btnLogin);

        btnLogin.addActionListener(e -> {
            String input = txtInput.getText();
            String password = new String(txtPassword.getPassword());
            int status = loginManager.login(input, password);
            try {
                handleLogin(status, input);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        rightPanel.add(btnLogin, "h 45!, gapbottom 10");

        // --- NEW BUTTON: REGISTER ---
        JButton btnRegister = new JButton("Δημιουργία Λογαριασμού");
        styleSecondaryButton(btnRegister);
        
        btnRegister.addActionListener(e -> {
            // Άνοιγμα του παραθύρου εγγραφής
            new RegisterFrame(ufm, cfm).setVisible(true);
        });
        rightPanel.add(btnRegister, "h 40!, gapbottom 10");

        // Link: Forgot Password
        JLabel lblHelp = new JLabel("<html><u>Ξεχάσατε τον κωδικό;</u></html>");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHelp.setForeground(Color.GRAY);
        lblHelp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHelp.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(lblHelp, "center");

        add(rightPanel, "grow");
    }

    // --- Styling Helpers ---
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(BRAND_COLOR);
        btn.setBorder(BorderFactory.createLineBorder(BRAND_COLOR));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addBulletPoint(JPanel panel, String text) {
        JLabel lbl = new JLabel("•  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, "gapbottom 12");
    }

    private void handleLogin(int status, String inputIdentifier) throws IOException {
        switch (status) {
            case 1 -> proceed(inputIdentifier);
            case 2 -> {
                String qr = JOptionPane.showInputDialog(this, "Enter 2FA Code:");
    
                if (qr != null && loginManager.qrCodeLogin(inputIdentifier, qr)) proceed(inputIdentifier);
                else JOptionPane.showMessageDialog(this, "Invalid Code", "Error", JOptionPane.ERROR_MESSAGE);
            }
            case 3 -> proceed(inputIdentifier); // Already logged in -> Proceed
            case 4 -> JOptionPane.showMessageDialog(this, "This account is inactive.", "Account Locked", JOptionPane.ERROR_MESSAGE);
            case 6 -> JOptionPane.showMessageDialog(this, "Account temporarily locked due to failed attempts.", "Locked", JOptionPane.ERROR_MESSAGE);
            default -> JOptionPane.showMessageDialog(this, "Λάθος Username ή Κωδικός.", "Αποτυχία Σύνδεσης", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void proceed(String identifier) throws IOException {

        User user = ufm.getUserByUsernameOrEmail(identifier); 

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Critical Error: User found during login but null during retrieval.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

      
        if (user.getRole().toString().equals("ADMIN")) {
            new MainDashboardFrame(user, ufm, cfm).setVisible(true);
        } 
        else if (user.getRole().toString().equals("COMPANY")) {
            new CompanyDashboardFrame(user, ufm, cfm).setVisible(true);
        }
        else {
            new CustomerDashboardFrame(user, ufm, cfm).setVisible(true);
        }

        this.dispose(); 
    }
}