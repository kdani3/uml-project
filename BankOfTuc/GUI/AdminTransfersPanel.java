package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Customer;
import BankOfTuc.Accounting.BankAccount;

import javax.swing.*;
import java.awt.*;

public class AdminTransfersPanel extends JPanel {
    private final CustomerFileManager cfm;
    private JComboBox<String> customerSelector;
    private JLabel lblBalance;
    private Customer selectedCustomer;

    public AdminTransfersPanel(CustomerFileManager cfm, UserFileManagement ufm) {
        this.cfm = cfm;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Select Customer
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Perform Transfer for:"), gbc);

        customerSelector = new JComboBox<>();
        for (Customer c : cfm.getAllCustomers()) {
            customerSelector.addItem(c.getUsername());
        }
        customerSelector.addActionListener(e -> updateInfo());
        gbc.gridx = 1;
        add(customerSelector, gbc);

        // Balance Info
        lblBalance = new JLabel("Balance: -");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        add(lblBalance, gbc);

        // Buttons for Transfer Types
        JButton btnSelf = new JButton("1. Self Transfer");
        JButton btnInter = new JButton("2. InterBank Transfer");
        JButton btnSwift = new JButton("3. SWIFT Transfer");
        JButton btnSepa = new JButton("4. SEPA Transfer");

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0; add(btnSelf, gbc);
        gbc.gridx = 1; add(btnInter, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; add(btnSwift, gbc);
        gbc.gridx = 1; add(btnSepa, gbc);

        // Actions
        btnSelf.addActionListener(e -> showTransferDialog("Self Transfer"));
        btnInter.addActionListener(e -> showTransferDialog("InterBank"));
        btnSwift.addActionListener(e -> showTransferDialog("SWIFT"));
        btnSepa.addActionListener(e -> showTransferDialog("SEPA"));
    }

    private void updateInfo() {
        String username = (String) customerSelector.getSelectedItem();
        selectedCustomer = cfm.getCustomerByUsername(username);
        if (selectedCustomer != null && !selectedCustomer.getBankAccounts().isEmpty()) {
            double bal = selectedCustomer.getBankAccounts().get(0).getBalance();
            lblBalance.setText("Main Account Balance: " + bal + "€");
        } else {
            lblBalance.setText("No accounts found.");
        }
    }

    private void showTransferDialog(String type) {
        if (selectedCustomer == null) return;
        
        // Απλό Dialog για Demo της ροής
        String amountStr = JOptionPane.showInputDialog(this, type + "\nEnter Amount:");
        if (amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                // Εδώ θα καλούσες την αντίστοιχη Service (π.χ. SwiftTransferService)
                // Για τώρα δείχνουμε μήνυμα επιτυχίας
                JOptionPane.showMessageDialog(this, type + " of " + amount + "€ initiated for " + selectedCustomer.getFullname());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Amount");
            }
        }
    }
}