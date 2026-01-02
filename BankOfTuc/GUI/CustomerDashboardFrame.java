package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CustomerDashboardFrame extends JFrame {
    private final User currentUser;
    private final IndividualCustomer customer;
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private JTabbedPane tabbedPane;

    // Branding Colors
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);

    public CustomerDashboardFrame(User user, UserFileManagement ufm, CustomerFileManager cfm) throws IOException {
        this.currentUser = user;
        this.ufm = ufm;
        this.cfm = cfm;
        this.customer = (IndividualCustomer) cfm.getCustomerByUsername(user.getUsername());

        setTitle("TUC Bank - e-Banking: " + user.getUsername());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Styling TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_PRIMARY);

        // --- TABS ---

        // 0. Επισκόπηση (Περνάμε το tabbedPane για να δουλεύουν τα κουμπιά πλοήγησης)
        tabbedPane.addTab("Επισκόπηση", new CustomerHomePanel(customer, cfm, tabbedPane));

        // 1. Λογαριασμοί
        tabbedPane.addTab("Λογαριασμοί", new CustomerAccountsPanel(customer, cfm));

        // 2. Μεταφορές
        tabbedPane.addTab("Μεταφορές", new CustomerTransfersPanel(customer, cfm));

        // 3. Πληρωμές 
        tabbedPane.addTab("Πληρωμές", new CustomerPaymentsPanel(customer, cfm));

        // 4. Ιστορικό
        tabbedPane.addTab("Ιστορικό", new CustomerHistoryPanel(customer, cfm));

        // 5. Ρυθμίσεις (Υπάρχει ήδη από το Admin project)
        tabbedPane.addTab("Ρυθμίσεις", new SettingsPanel(currentUser, ufm));

        // 6. Έξοδος
        tabbedPane.addTab("Έξοδος", createLogoutPanel());

        add(tabbedPane);
    }

    private JPanel createLogoutPanel() {
        JPanel logoutPanel = new JPanel(new MigLayout("fill, insets 0", "[center]", "[center]"));
        logoutPanel.setBackground(Color.WHITE);

        JButton btnLogout = new JButton("Αποσύνδεση");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(BRAND_COLOR);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            LoginManager loginManager = new LoginManager(ufm);
            loginManager.logout(currentUser.getUsername());
            new LoginFrame(ufm, cfm).setVisible(true);
            this.dispose();
        });

        logoutPanel.add(btnLogout, "w 250!, h 50!");
        return logoutPanel;
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
}