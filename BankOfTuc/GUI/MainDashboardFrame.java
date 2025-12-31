package BankOfTuc.GUI;

import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager; // <-- ΠΡΟΣΘΗΚΗ IMPORT
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;

import javax.swing.*;
import java.awt.*;

public class MainDashboardFrame extends JFrame {
    private final User currentUser;
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;

    public MainDashboardFrame(User user, UserFileManagement ufm, CustomerFileManager cfm) {
        this.currentUser = user;
        this.ufm = ufm;
        this.cfm = cfm;

        setTitle("Bank of Tuc - " + user.getFullname() + " (" + user.getRole() + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Home Tab (Welcome)
        tabbedPane.addTab("Home", createHomePanel());

        if (user.getRole().toString().equals("ADMIN")) {
            // Case 1 & 7: Customer Management (Edit, Delete, Details)
            tabbedPane.addTab("Customers", new AdminCustomersPanel(ufm, cfm));

            // Case 2: Payments Management
            tabbedPane.addTab("Payments", new AdminPaymentsPanel(cfm));

            // Case 3: Transfers Management
            tabbedPane.addTab("Transfers", new AdminTransfersPanel(cfm, ufm));

            // Case 4: Time Simulation
            tabbedPane.addTab("Time Simulation", new TimeSimulationPanel(cfm));
        } else {
            // Tabs για απλούς πελάτες (θα τα δούμε αργότερα)
            tabbedPane.addTab("My Accounts", new JPanel());
        }

        // Case 5: Settings (Password, 2FA)
        tabbedPane.addTab("Settings", new SettingsPanel(currentUser, ufm));

        // Case 6: Logout
        JPanel logoutPanel = new JPanel();
        JButton btnLogout = new JButton("Logout");
        
        // --- ΔΙΟΡΘΩΣΗ LOGOUT ---
        btnLogout.addActionListener(e -> {
            // 1. Δημιουργία LoginManager για να εκτελέσουμε το logout
            LoginManager loginManager = new LoginManager(ufm);
            
            // 2. Αφαίρεση του χρήστη από το sessions.json
            loginManager.logout(currentUser.getUsername());
            
            // 3. Επιστροφή στο Login Screen
            new LoginFrame(ufm, cfm).setVisible(true);
            this.dispose();
        });
        // -----------------------
        
        logoutPanel.add(btnLogout);
        tabbedPane.addTab("Logout", logoutPanel);

        add(tabbedPane);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome back, " + currentUser.getFullname(), SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcome, BorderLayout.CENTER);
        return panel;
    }
}