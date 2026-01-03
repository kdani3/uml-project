package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CustomerHomePanel extends JPanel {
    private final IndividualCustomer customer;
    private final CustomerFileManager cfm;
    private final JTabbedPane parentTabs;

    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);
    
    // Privacy Mode Variables
    private boolean isBalanceHidden = false;
    private double totalBalance = 0.0;
    private JLabel lblBalanceValue;
    private JButton btnPrivacy;

    public CustomerHomePanel(IndividualCustomer customer, CustomerFileManager cfm, JTabbedPane parentTabs) {
        this.customer = customer;
        this.cfm = cfm;
        this.parentTabs = parentTabs;

        setLayout(new MigLayout("fill, insets 40, wrap 3", "[33%][33%][33%]", "[]30[]30[grow]"));
        setBackground(BRAND_COLOR);

        initComponents();
    }

    private void initComponents() {
        // Header
        JLabel lblWelcome = new JLabel("Καλωσήρθατε, " + customer.getFullname());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(Color.WHITE);
        add(lblWelcome, "span 3, wrap");

        // --- Stats Cards ---
        
        // 1. Balance Card (Custom με Privacy Button)
        totalBalance = customer.getBankAccounts().stream()
                .mapToDouble(BankAccount::getBalance).sum();
        add(createBalanceCard("ΣΥΝΟΛΙΚΟ ΥΠΟΛΟΙΠΟ"), "grow");

        // 2. Accounts Count
        int accountCount = customer.getBankAccounts().size();
        add(createStatCard("ΛΟΓΑΡΙΑΣΜΟΙ", String.valueOf(accountCount)), "grow");

        // 3. Last Activity
        String lastActivity = "N/A";
        try {
            var history = TransactionHistoryService.getHistoryForCustomer(customer.getVatID(), cfm);
            if (!history.isEmpty()) {
                lastActivity = history.get(0).datetime.split(" ")[0];
            }
        } catch (IOException e) { e.printStackTrace(); }
        add(createStatCard("ΤΕΛΕΥΤΑΙΑ ΚΙΝΗΣΗ", lastActivity), "grow");

        // --- Quick Actions ---
        JPanel actionsPanel = new JPanel(new MigLayout("fill, insets 30", "[grow][grow]", "[]20[]"));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel lblActionsTitle = new JLabel("Γρήγορες Ενέργειες");
        lblActionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblActionsTitle.setForeground(Color.GRAY);
        actionsPanel.add(lblActionsTitle, "span 2, wrap");

        actionsPanel.add(createNavButton("Οι Λογαριασμοί μου", "Προβολή & Νέος λογαριασμός", 1), "grow, h 80!");
        actionsPanel.add(createNavButton("Νέα Μεταφορά", "Εμβάσματα σε τρίτους", 2), "grow, h 80!, wrap");
        actionsPanel.add(createNavButton("Πληρωμές", "Εξόφληση λογαριασμών", 3), "grow, h 80!");
        actionsPanel.add(createNavButton("Ιστορικό Κινήσεων", "Αναλυτική κατάσταση", 4), "grow, h 80!");

        add(actionsPanel, "span 3, grow, push");
    }

    // --- Helper Formatting Method ---
    private String formatCurrency(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return df.format(amount) + " €";
    }

    // --- Balance Card Logic ---
    private JPanel createBalanceCard(String title) {
        JPanel card = new JPanel(new MigLayout("fill, insets 25", "[grow][]", "[]5[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createMatteBorder(0, 6, 0, 0, Color.BLACK)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.GRAY);

        btnPrivacy = new JButton("Hide");
        btnPrivacy.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnPrivacy.setBackground(Color.WHITE);
        btnPrivacy.setForeground(Color.BLACK);
        btnPrivacy.setFocusPainted(false);
        btnPrivacy.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        btnPrivacy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnPrivacy.addActionListener(e -> togglePrivacy());

        // Use formatter here
        lblBalanceValue = new JLabel(formatCurrency(totalBalance));
        lblBalanceValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblBalanceValue.setForeground(TEXT_PRIMARY);

        card.add(lblTitle);
        card.add(btnPrivacy, "align right, h 25!, w 50!, wrap");
        card.add(lblBalanceValue, "span 2");
        return card;
    }

    private void togglePrivacy() {
        isBalanceHidden = !isBalanceHidden;
        if (isBalanceHidden) {
            lblBalanceValue.setText("**** €");
            btnPrivacy.setText("Show");
        } else {
            lblBalanceValue.setText(formatCurrency(totalBalance)); // Use formatter here
            btnPrivacy.setText("Hide");
        }
    }

    // --- Standard Cards ---
    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new MigLayout("fill, insets 25", "[grow]", "[]5[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createMatteBorder(0, 6, 0, 0, Color.BLACK) 
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
        btn.addActionListener(e -> parentTabs.setSelectedIndex(tabIndex));
        return btn;
    }
}