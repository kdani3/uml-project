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
        tabbedPane.addTab("Επισκόπηση", new CustomerHomePanel(customer, cfm, tabbedPane));
        tabbedPane.addTab("Λογαριασμοί", new CustomerAccountsPanel(customer, cfm));
        tabbedPane.addTab("Μεταφορές", new CustomerTransfersPanel(customer, cfm));
        tabbedPane.addTab("Πληρωμές", new CustomerPaymentsPanel(customer, cfm));
        tabbedPane.addTab("Ιστορικό", new CustomerHistoryPanel(customer, cfm));
        tabbedPane.addTab("Ρυθμίσεις", new SettingsPanel(currentUser, ufm));
        tabbedPane.addTab("Έξοδος", createLogoutPanel());

        add(tabbedPane);
    }

    private JPanel createLogoutPanel() {
        JPanel logoutPanel = new JPanel(new MigLayout("fill, insets 0", "[center]", "[center]"));
        logoutPanel.setBackground(BRAND_COLOR); 

        JButton btnLogout = new JButton("Αποσύνδεση");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 15));
  
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setBackground(Color.WHITE);
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
}