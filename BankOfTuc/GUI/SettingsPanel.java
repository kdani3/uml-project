package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.CLI.CLIUtils;
import BankOfTuc.User;
import BankOfTuc.Auth.QrUtils;
import dev.samstevens.totp.exceptions.QrGenerationException;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final User currentUser;
    private final UserFileManagement ufm;

    public SettingsPanel(User user, UserFileManagement ufm) {
        this.currentUser = user;
        this.ufm = ufm;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("Settings for " + user.getUsername());
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        JButton btnChangePass = new JButton("Change Password");
        JButton btnSetup2FA = new JButton("Setup 2FA (QR Code)");

        gbc.gridy = 1; gbc.gridwidth = 1;
        add(btnChangePass, gbc);

        gbc.gridx = 1;
        add(btnSetup2FA, gbc);

        btnChangePass.addActionListener(e -> changePassword());
        btnSetup2FA.addActionListener(e -> setup2FA());
    }

    private void changePassword() {
        String newPass = JOptionPane.showInputDialog(this, "Enter New Password:");
        if (newPass != null && !newPass.isEmpty()) {
            currentUser.setPassword(newPass);
            ufm.updateUser(currentUser);
            JOptionPane.showMessageDialog(this, "Password Changed Successfully!");
        }
    }

    private void setup2FA() {
        try {
            String[] qrResult = QrUtils.createQr(currentUser.getUsername());
            String qrUri = qrResult[0];
            String secret = qrResult[1];
            
            // Δείχνουμε το secret (κανονικά θα έπρεπε να δείξουμε την εικόνα QR)
            JTextArea area = new JTextArea("Scan this logic in Google Auth:\nSecret Key: " + secret + "\n\n(Image saved as QR.png)");
            JOptionPane.showMessageDialog(this, area);
            
            String code = JOptionPane.showInputDialog(this, "Enter Code from App to verify:");
            if (code != null && QrUtils.verifyQrCode(secret, code)) {
                currentUser.setQrCode(secret);
                ufm.updateUser(currentUser);
                JOptionPane.showMessageDialog(this, "2FA Enabled!");
            } else {
                JOptionPane.showMessageDialog(this, "Verification Failed.");
            }

        } catch (QrGenerationException e) {
            e.printStackTrace();
        }
    }
}