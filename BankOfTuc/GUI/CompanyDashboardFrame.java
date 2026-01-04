package BankOfTuc.GUI;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class CompanyDashboardFrame extends JFrame {
    private final User currentUser;
    private final CompanyCustomer company;
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private JTabbedPane tabbedPane;

    // Branding Colors
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);

    public CompanyDashboardFrame(User user, UserFileManagement ufm, CustomerFileManager cfm) {
        this.currentUser = user;
        this.ufm = ufm;
        this.cfm = cfm;
        this.company = (CompanyCustomer) cfm.getCustomerByUsername(user.getUsername());

        setTitle("TUC Bank - Business Banking: " + company.getFullname());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Styling TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(TEXT_PRIMARY);

        tabbedPane.addTab("Επισκόπηση", new CompanyHomePanel(company, cfm));

        tabbedPane.addTab("Λογαριασμός", new CustomerAccountsPanel(company, cfm));

        tabbedPane.addTab("Έκδοση Λογαριασμών", new CompanyIssuingPanel(company, cfm));

        tabbedPane.addTab("Ιστορικό", new CustomerHistoryPanel(company, cfm));


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