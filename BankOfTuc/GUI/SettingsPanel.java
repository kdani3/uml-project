package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.User;
import BankOfTuc.Auth.QrUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class SettingsPanel extends JPanel {
    private final User currentUser;
    private final UserFileManagement ufm;
    
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public SettingsPanel(User user, UserFileManagement ufm) {
        this.currentUser = user;
        this.ufm = ufm;
        // Layout: Center
        setLayout(new MigLayout("fill, insets 50", "[center]", "[center]"));
        setBackground(Color.WHITE);

        // Card Panel
        JPanel card = new JPanel(new MigLayout("wrap 1, insets 30, fillx", "[fill, 350!]", "[]30[]15[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BRAND_COLOR, 1), 
            "Ρυθμίσεις Ασφαλείας", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Segoe UI", Font.BOLD, 14), 
            BRAND_COLOR
        ));

        // User Info
        JLabel userLbl = new JLabel("Χρήστης: " + user.getUsername());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userLbl.setForeground(Color.DARK_GRAY);
        userLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(userLbl, "center");

        // Buttons
        JButton btnPass = createBtn("Αλλαγή Κωδικού");
        btnPass.addActionListener(e -> changePass());
        card.add(btnPass, "h 45!");

        JButton btn2FA = createBtn("Ενεργοποίηση 2FA (QR)");
        btn2FA.addActionListener(e -> setup2FA());
        card.add(btn2FA, "h 45!");

        add(card);
    }
    
    private JButton createBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void changePass() {
        String p = JOptionPane.showInputDialog(this, "Εισάγετε τον νέο κωδικό:");
        if (p != null && !p.trim().isEmpty()) {
            currentUser.setPassword(p.trim());
            ufm.updateUser(currentUser);
            JOptionPane.showMessageDialog(this, "Ο κωδικός άλλαξε επιτυχώς!");
        }
    }

    private void setup2FA() {
        try {
            // 1. Δημιουργία του QR Code και του Secret
            // qr[0] = Data URI (εικόνα), qr[1] = Secret Key
            String[] qr = QrUtils.createQr(currentUser.getUsername());
            String dataUri = qr[0];
            String secretKey = qr[1];
            
            // 2. Δημιουργία Panel για το Popup
            JPanel qrPanel = new JPanel(new BorderLayout(10, 10));
            qrPanel.setBackground(Color.WHITE);

            JLabel lblInst = new JLabel("<html><center>Σκανάρετε το παρακάτω QR Code<br>με την εφαρμογή Authenticator:</center></html>");
            lblInst.setHorizontalAlignment(SwingConstants.CENTER);
            lblInst.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            qrPanel.add(lblInst, BorderLayout.NORTH);

            // 3. Αποκωδικοποίηση και εμφάνιση της εικόνας από το Data URI
            try {
                // Το string είναι της μορφής "data:image/png;base64,....."
                // Παίρνουμε το κομμάτι μετά το κόμμα
                String base64Image = dataUri.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                
                ImageIcon icon = new ImageIcon(imageBytes);
                // Resize για να φαίνεται ωραία (200x200)
                Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                JLabel lblImage = new JLabel(new ImageIcon(img));
                lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                qrPanel.add(lblImage, BorderLayout.CENTER);
            } catch (Exception e) {
                qrPanel.add(new JLabel("(Αδυναμία φόρτωσης εικόνας)"), BorderLayout.CENTER);
                e.printStackTrace();
            }

            // 4. Εμφάνιση του κλειδιού (Backup)
            JTextField txtSecret = new JTextField(secretKey);
            txtSecret.setEditable(false);
            txtSecret.setHorizontalAlignment(SwingConstants.CENTER);
            txtSecret.setFont(new Font("Monospaced", Font.BOLD, 16));
            txtSecret.setBorder(BorderFactory.createTitledBorder("Ή εισάγετε αυτό το κλειδί:"));
            
            qrPanel.add(txtSecret, BorderLayout.SOUTH);
            
            // 5. Εμφάνιση Διαλόγου
            JOptionPane.showMessageDialog(this, qrPanel, "Ενεργοποίηση 2FA", JOptionPane.PLAIN_MESSAGE);
            
            // 6. Αποθήκευση στον χρήστη
            currentUser.setQrCode(secretKey);
            ufm.updateUser(currentUser);
            
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Σφάλμα κατά τη δημιουργία QR: " + e.getMessage());
        }
    }
}