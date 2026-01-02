package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class CustomerAccountsPanel extends JPanel {
    private final IndividualCustomer customer;
    private final CustomerFileManager cfm;
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public CustomerAccountsPanel(IndividualCustomer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;
        
        setLayout(new MigLayout("fill, insets 30", "[grow]", "[][grow][]"));
        setBackground(Color.WHITE);

        initComponents();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Κατάσταση Λογαριασμών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.DARK_GRAY);
        add(lblTitle, "wrap");

        // Table Setup
        String[] columns = {"IBAN", "Τύπος", "Υπόλοιπο (€)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (BankAccount acc : customer.getBankAccounts()) {
            addAccountRow(model, acc);
        }

        JTable table = new JTable(model);
        styleTable(table);
        add(new JScrollPane(table), "grow, wrap");

        // Add Account Button
        JButton btnAdd = new JButton("Άνοιγμα Νέου Λογαριασμού");
        styleButton(btnAdd);

        btnAdd.addActionListener(e -> handleAddAccount(model));
        add(btnAdd, "right, h 40!");
    }

    private void handleAddAccount(DefaultTableModel model) {
        if (customer.getBankAccounts().size() >= 5) {
            JOptionPane.showMessageDialog(this, "Έχετε φτάσει το όριο των 5 λογαριασμών.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"CHECKING", "SAVINGS"};
        int choice = JOptionPane.showOptionDialog(this, "Επιλέξτε τύπο λογαριασμού:", "Νέος Λογαριασμός",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice >= 0) {
            BankAccount.AccountType type = (choice == 0) ? BankAccount.AccountType.CHECKING : BankAccount.AccountType.SAVINGS;
            BankAccount newAcc = new BankAccount(customer.getVatID(), type);
            customer.addBankAccount(newAcc);

            if (cfm.updateCustomer(customer)) {
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός δημιουργήθηκε: " + newAcc.getIban());
                addAccountRow(model, newAcc);
            } else {
                JOptionPane.showMessageDialog(this, "Αποτυχία αποθήκευσης.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addAccountRow(DefaultTableModel model, BankAccount acc) {
        model.addRow(new Object[]{
            acc.getIban(),
            acc.getType(),
            String.format("%.2f", acc.getBalance())
        });
    }

    // --- Styling Helpers ---

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(220, 220, 220)); // Γκρι Header
        header.setForeground(Color.DARK_GRAY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(BRAND_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}