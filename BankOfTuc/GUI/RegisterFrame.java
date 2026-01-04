package BankOfTuc.GUI;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public RegisterFrame(UserFileManagement ufm, CustomerFileManager cfm) {
        this.ufm = ufm;
        this.cfm = cfm;

        setTitle("TUC Bank - Εγγραφή Νέου Χρήστη");
        setSize(500, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fillx, insets 40, wrap 2", "[left][grow, fill]", "[]20[]10[]10[]10[]10[]10[]10[]10[]30[]"));

        JLabel lblTitle = new JLabel("Δημιουργία Λογαριασμού");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(BRAND_COLOR);
        add(lblTitle, "span 2, center, wrap");

        add(new JLabel("Τύπος Λογαριασμού:"), "gaptop 20");
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Ιδιώτης (Individual)", "Εταιρεία (Company)"});
        add(comboType);

        add(new JLabel("Όνοματεπώνυμο / Επωνυμία:"));
        JTextField txtFullname = new JTextField();
        add(txtFullname);

        add(new JLabel("Username:"));
        JTextField txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("Email (Προαιρετικό):"));
        JTextField txtEmail = new JTextField();
        add(txtEmail);

        add(new JLabel("ΑΦΜ (VAT ID):"));
        JTextField txtVat = new JTextField();
        add(txtVat);

        add(new JLabel("Κωδικός Πρόσβασης:"));
        JPasswordField txtPass = new JPasswordField();
        add(txtPass);

        add(new JLabel("Επιβεβαίωση Κωδικού:"));
        JPasswordField txtPassConfirm = new JPasswordField();
        add(txtPassConfirm);

        JButton btnRegister = new JButton("Εγγραφή");
        styleButton(btnRegister, true);
        
        JButton btnCancel = new JButton("Ακύρωση");
        styleButton(btnCancel, false);

        add(btnRegister, "span 2, split 2, growx, h 40!");
        add(btnCancel, "growx, h 40!");

        btnCancel.addActionListener(e -> this.dispose());

        btnRegister.addActionListener(e -> {
            String type = (String) comboType.getSelectedItem();
            String fullname = txtFullname.getText().trim();
            String username = txtUsername.getText().trim();
            String emailInput = txtEmail.getText().trim(); 
            String vat = txtVat.getText().trim();
            String p1 = new String(txtPass.getPassword());
            String p2 = new String(txtPassConfirm.getPassword());

    
            if (fullname.isEmpty() || username.isEmpty() || vat.isEmpty() || p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Παρακαλώ συμπληρώστε τα υποχρεωτικά πεδία.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
  
            String emailToSave = emailInput.isEmpty() ? null : emailInput;

            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Οι κωδικοί δεν ταιριάζουν.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (ufm.getUserByUsername(username) != null) {
                JOptionPane.showMessageDialog(this, "Το username χρησιμοποιείται ήδη.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Creation
            User newUser;
            try {
                if (type.contains("Individual")) {
                    newUser = new IndividualCustomer(username, p1, fullname, vat, emailToSave, true);
                } else {
                    newUser = new CompanyCustomer(username, p1, fullname, vat, emailToSave, true);
                }

                // 3. Saving
                ufm.addUser(newUser);
                if (newUser instanceof BankOfTuc.Customer) {
                    cfm.addCustomer((BankOfTuc.Customer) newUser);
                }

                JOptionPane.showMessageDialog(this, "Ο λογαριασμός δημιουργήθηκε επιτυχώς!\nΜπορείτε να συνδεθείτε.", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Σφάλμα κατά την εγγραφή: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    private void styleButton(JButton btn, boolean primary) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (primary) {
            btn.setBackground(BRAND_COLOR);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
    }
}