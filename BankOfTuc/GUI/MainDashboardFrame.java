package BankOfTuc.GUI;

import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainDashboardFrame extends JFrame {
    private final User currentUser;
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private JTabbedPane tabbedPane;
    
    // Branding Colors
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color BG_COLOR = new Color(250, 250, 250);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);

    public MainDashboardFrame(User user, UserFileManagement ufm, CustomerFileManager cfm) {
        this.currentUser = user;
        this.ufm = ufm;
        this.cfm = cfm;

        setTitle("TUC Bank - Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Styling του TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_PRIMARY);

        // --- TABS ---
        
        // 0. Αρχική
        tabbedPane.addTab("Admin Dashboard", createHomePanel());

        if (user.getRole().toString().equals("ADMIN")) {
            // Admin Tabs (Indices: 1, 2, 3, 4)
            tabbedPane.addTab("Διαχείριση Πελατών", new AdminCustomersPanel(ufm, cfm));
            tabbedPane.addTab("Πληρωμές", new AdminPaymentsPanel(cfm));
            tabbedPane.addTab("Μεταφορές", new AdminTransfersPanel(cfm, ufm));
            tabbedPane.addTab("Προσομοίωση Χρόνου", new TimeSimulationPanel(cfm));
        } else {
            tabbedPane.addTab("Οι Λογαριασμοί μου", createPlaceholderPanel("Οι Λογαριασμοί μου")); 
        }

        // 5. Ρυθμίσεις
        tabbedPane.addTab("Ρυθμίσεις", new SettingsPanel(currentUser, ufm));

        // 6. Έξοδος
        JPanel logoutPanel = new JPanel(new MigLayout("fill, insets 0", "[center]", "[center]"));
        logoutPanel.setBackground(Color.WHITE);
        
        JButton btnLogout = new JButton("Αποσύνδεση");
        styleButton(btnLogout);
        btnLogout.setBackground(BRAND_COLOR); 
        
        btnLogout.addActionListener(e -> {
            LoginManager loginManager = new LoginManager(ufm);
            loginManager.logout(currentUser.getUsername());
            new LoginFrame(ufm, cfm).setVisible(true);
            this.dispose();
        });
        
        logoutPanel.add(btnLogout, "w 250!, h 50!");
        tabbedPane.addTab("Έξοδος", logoutPanel);

        add(tabbedPane);
    }

    private JPanel createHomePanel() {
        // Layout: 3 Στήλες (33% η καθεμία) για τα πάνω κουτιά
        JPanel home = new JPanel(new MigLayout("fill, insets 40, wrap 3", "[33%][33%][33%]", "[]30[]30[grow]"));
        home.setBackground(BG_COLOR);

        // Header
        JLabel lblWelcome = new JLabel("Πίνακας Διαχείρισης");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(Color.GRAY);
        home.add(lblWelcome, "span 3, wrap");

        // --- TOP CARDS ---

        // 1. Συνολικοί Χρήστες (Από users.json)
        int userCount = ufm.getAllUsers().size();
        home.add(createStatCard("ΣΥΝΟΛΙΚΟΙ ΧΡΗΣΤΕΣ", String.valueOf(userCount), "users_icon.png"), "grow");

        // 2. Ενεργές Συναλλαγές (Από recurring_payments.csv)
        int recurringCount = countLines("data/recurring_payments.csv");
        home.add(createStatCard("ΕΝΕΡΓΕΣ ΣΥΝΑΛΛΑΓΕΣ", String.valueOf(recurringCount), "sync_icon.png"), "grow");

        // 3. Audits (Από payments.csv + transfers.csv)
        int auditsCount = countLines("logs/payments.csv") + countLines("logs/transfers.csv");
        home.add(createStatCard("AUDITS / LOGS", String.valueOf(auditsCount), "log_icon.png"), "grow");


        // --- BOTTOM LARGE BOX (Admin Actions) ---
        JPanel adminPanel = new JPanel(new MigLayout("fill, insets 30", "[grow][grow]", "[]20[]"));
        adminPanel.setBackground(Color.WHITE);
        adminPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        JLabel lblAdminTitle = new JLabel("Εργαλεία Διαχείρισης");
        lblAdminTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAdminTitle.setForeground(Color.GRAY);
        adminPanel.add(lblAdminTitle, "span 2, wrap");

        // Buttons που σε πηγαίνουν στα Tabs
        JButton btnCust = createNavButton("Διαχείριση Πελατών", "Επεξεργασία, Διαγραφή, Reset Password", 1);
        JButton btnPay = createNavButton("Διαχείριση Πληρωμών", "Εξόφληση λογαριασμών πελατών", 2);
        JButton btnTrans = createNavButton("Εκτέλεση Μεταφορών", "Εμβάσματα SWIFT, SEPA, InterBank", 3);
        JButton btnTime = createNavButton("Προσομοίωση Χρόνου", "Μετάβαση σε μελλοντική ημερομηνία", 4);
        
        // ΝΕΟ ΚΟΥΜΠΙ: Ρυθμίσεις (Index 5)
        JButton btnSettings = createNavButton("Ρυθμίσεις Ασφαλείας", "Αλλαγή Κωδικού, 2FA", 5);

        adminPanel.add(btnCust, "grow, h 80!");
        adminPanel.add(btnPay, "grow, h 80!, wrap");
        adminPanel.add(btnTrans, "grow, h 80!");
        adminPanel.add(btnTime, "grow, h 80!, wrap");
        
        // Το κουμπί ρυθμίσεων πιάνει όλο το πλάτος κάτω (span 2)
        adminPanel.add(btnSettings, "span 2, grow, h 80!"); 

        home.add(adminPanel, "span 3, grow, push");

        return home;
    }

    // --- Helpers ---

    private JPanel createStatCard(String title, String value, String iconStub) {
        JPanel card = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]5[]"));
        card.setBackground(Color.WHITE);
        // Κόκκινη μπάρα αριστερά
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createMatteBorder(0, 6, 0, 0, BRAND_COLOR) 
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(TEXT_PRIMARY);

        card.add(lblTitle, "wrap");
        card.add(lblValue);
        return card;
    }

    private JButton createNavButton(String title, String subtitle, int tabIndex) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(new Color(245, 245, 245));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lTitle.setForeground(TEXT_PRIMARY);
        
        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSub.setForeground(Color.GRAY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(lTitle);
        textPanel.add(lSub);
        
        btn.add(textPanel, BorderLayout.CENTER);
        
        // Action: Switch Tab
        btn.addActionListener(e -> tabbedPane.setSelectedIndex(tabIndex));
        
        return btn;
    }

    // Helper για μέτρηση γραμμών σε αρχεία (CSV logs)
    private int countLines(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return 0;
        
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            while (reader.readLine() != null) lines++;
            if (lines > 0) lines--; // Header
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new MigLayout("fill, insets 50", "[center]", "[center]"));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(text + " - Υπό Κατασκευή");
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(Color.LIGHT_GRAY);
        p.add(l);
        return p;
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}