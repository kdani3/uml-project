package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Customer;
import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.BillFileStore;
import BankOfTuc.Payments.Bill.BillStatus;
import BankOfTuc.Accounting.BankAccount;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AdminPaymentsPanel extends JPanel {
    private final CustomerFileManager cfm;
    private JComboBox<String> customerSelector;
    private JTable billsTable;
    private DefaultTableModel billsModel;
    private Customer selectedCustomer;

    public AdminPaymentsPanel(CustomerFileManager cfm) {
        this.cfm = cfm;
        setLayout(new BorderLayout());

        // Top: Select Customer
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Customer:"));
        customerSelector = new JComboBox<>();
        loadCustomers();
        customerSelector.addActionListener(e -> loadCustomerBills());
        topPanel.add(customerSelector);
        
        JButton btnPay = new JButton("Pay Selected Bill");
        btnPay.addActionListener(e -> paySelectedBill());
        topPanel.add(btnPay);

        add(topPanel, BorderLayout.NORTH);

        // Center: Bills Table
        String[] cols = {"Bill ID", "Issuer", "Amount", "Status", "Due Date"};
        billsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billsTable = new JTable(billsModel);
        add(new JScrollPane(billsTable), BorderLayout.CENTER);
    }

    private void loadCustomers() {
        for (Customer c : cfm.getAllCustomers()) {
            if (c.getRole().toString().equals("INDIVIDUAL")) {
                customerSelector.addItem(c.getUsername());
            }
        }
    }

    private void loadCustomerBills() {
        String username = (String) customerSelector.getSelectedItem();
        if (username == null) return;
        selectedCustomer = cfm.getCustomerByUsername(username);
        
        billsModel.setRowCount(0);
        
        try {
            // Χρήση της νέας μεθόδου findByPayee
            List<Bill> bills = BillFileStore.findByPayee(selectedCustomer.getVatID());
            
            for (Bill b : bills) {
                // Διόρθωση getter: getBillid() αντί για getId()
                billsModel.addRow(new Object[]{b.getBillid(), b.getIssuerUsername(), b.getAmount(), b.getStatus(), b.getDueDate()});
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bills: " + e.getMessage());
        }
    }

    private void paySelectedBill() {
        int row = billsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first.");
            return;
        }

        String billId = (String) billsModel.getValueAt(row, 0);
        
        try {
            // Χρήση της νέας μεθόδου findById
            Bill bill = BillFileStore.findById(billId);

            if (bill == null) {
                JOptionPane.showMessageDialog(this, "Bill not found!");
                return;
            }

            if (bill.getStatus() == BillStatus.PAID) {
                JOptionPane.showMessageDialog(this, "Bill is already paid!");
                return;
            }

            if (selectedCustomer.getBankAccounts().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Customer has no bank accounts!");
                return;
            }

            BankAccount acc = selectedCustomer.getBankAccounts().get(0);
            if (acc.getBalance() >= bill.getAmount()) {
                acc.reduceBalance(bill.getAmount());
                bill.setStatus(BillStatus.PAID);
                bill.setPaid(true);
                
                // Ενημέρωση πελάτη
                cfm.updateCustomer(selectedCustomer);
                
                // Διόρθωση: Χρήση updateBill αντί για save()
                BillFileStore.updateBill(bill); 
                
                loadCustomerBills();
                JOptionPane.showMessageDialog(this, "Bill Paid Successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient Funds!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage());
        }
    }
}