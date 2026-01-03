package BankOfTuc.GUI;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CompanyHomePanel extends JPanel {
    private final CompanyCustomer company;
    private final CustomerFileManager cfm;
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);

    // Privacy Mode Variables
    private boolean isBalanceHidden = false;
    private double totalBalance = 0.0;
    private JLabel lblBalanceValue;
    private JButton btnPrivacy;

    public CompanyHomePanel(CompanyCustomer company, CustomerFileManager cfm) {
        this.company = company;
        this.cfm = cfm;

        setLayout(new MigLayout("fill, insets 30, wrap 3", "[33%][33%][33%]", "[]20[]20[grow]"));
        setBackground(BRAND_COLOR);

        initComponents();
    }

    private void initComponents() {
        // --- Header ---
        JLabel lblWelcome = new JLabel("Business Overview: " + company.getFullname());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(Color.WHITE);
        add(lblWelcome, "span 3, wrap");

        // --- STATS CARDS ---
        
        // 1. Total Balance
        totalBalance = company.getBankAccounts().stream()
                .mapToDouble(BankAccount::getBalance).sum();
        add(createBalanceCard("ΣΥΝΟΛΙΚΟ ΚΕΦΑΛΑΙΟ"), "grow");

        // 2. Active Accounts
        int accountCount = company.getBankAccounts().size();
        add(createStatCard("ΕΝΕΡΓΟΙ ΛΟΓΑΡΙΑΣΜΟΙ", String.valueOf(accountCount)), "grow");

        // 3. Last Activity
        String lastActivity = "N/A";
        try {
            var history = TransactionHistoryService.getHistoryForCustomer(company.getVatID(), cfm);
            if (!history.isEmpty()) {
                lastActivity = history.get(0).datetime.split(" ")[0];
            }
        } catch (IOException e) { e.printStackTrace(); }
        add(createStatCard("ΤΕΛΕΥΤΑΙΑ ΚΙΝΗΣΗ", lastActivity), "grow, wrap");


        // --- TABLES SECTIONS ---
        
        JLabel lblAcc = new JLabel("Bank Accounts");
        lblAcc.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAcc.setForeground(Color.WHITE);
        add(lblAcc, "span 3, gaptop 20, wrap");

        JTable accTable = createAccountsTable();
        add(new JScrollPane(accTable), "span 3, growx, h 100!, wrap");

        JLabel lblHist = new JLabel("Recent Transactions");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHist.setForeground(Color.WHITE);
        add(lblHist, "span 3, gaptop 20, wrap");

        JTable histTable = createHistoryTable();
        add(new JScrollPane(histTable), "span 3, grow, push");
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
        JPanel card = new JPanel(new MigLayout("fill, insets 20", "[grow][]", "[]5[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createMatteBorder(0, 5, 0, 0, Color.BLACK) 
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        lblBalanceValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
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

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[]5[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createMatteBorder(0, 5, 0, 0, Color.BLACK) 
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(TEXT_PRIMARY);

        card.add(lblTitle, "wrap");
        card.add(lblValue);
        return card;
    }

    private JTable createAccountsTable() {
        String[] cols = {"IBAN", "Τύπος", "Υπόλοιπο (€)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (BankAccount acc : company.getBankAccounts()) {
            model.addRow(new Object[]{acc.getIban(), acc.getType(), String.format("%.2f", acc.getBalance())});
        }
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private JTable createHistoryTable() {
        String[] cols = {"Ημερομηνία", "Ποσό", "IBAN / RF", "Όνομα", "Τύπος"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            var history = TransactionHistoryService.getHistoryForCustomer(company.getVatID(), cfm);
            int limit = Math.min(history.size(), 10);
            for (int i = 0; i < limit; i++) {
                var h = history.get(i);
                model.addRow(new Object[]{
                    h.datetime, 
                    h.getAmountDisplay(), 
                    h.getIbanDisplay(company.getVatID()), 
                    h.counterpartyName, 
                    h.type
                });
            }
        } catch (IOException e) { e.printStackTrace(); }
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(BRAND_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
    }
}