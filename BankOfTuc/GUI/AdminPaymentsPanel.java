package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Customer;
import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.BillFileStore;
import BankOfTuc.Payments.Bill.BillStatus;
import BankOfTuc.Accounting.BankAccount;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class AdminPaymentsPanel extends JPanel {
    private final CustomerFileManager cfm;
    private JComboBox<String> customerSelector;
    private JTable billsTable;
    private DefaultTableModel billsModel;
    private Customer selectedCustomer;
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public AdminPaymentsPanel(CustomerFileManager cfm) {
        this.cfm = cfm;
        // Layout
        setLayout(new MigLayout("fill, insets 30", "[grow]", "[][grow]"));
        setBackground(Color.WHITE);

        // --- Top Bar (Φίλτρα & Actions) ---
        JPanel topBar = new JPanel(new MigLayout("insets 0", "[][grow][]"));
        topBar.setBackground(Color.WHITE);

        JLabel lblCust = new JLabel("Επιλογή Πελάτη:");
        lblCust.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCust.setForeground(BRAND_COLOR);
        topBar.add(lblCust);
        
        customerSelector = new JComboBox<>();
        customerSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cfm.getAllCustomers().stream()
           .filter(c -> "INDIVIDUAL".equals(c.getRole().toString()))
           .forEach(c -> customerSelector.addItem(c.getUsername()));
        customerSelector.addActionListener(e -> loadCustomerBills());
        topBar.add(customerSelector, "w 250!");

        JButton btnPay = new JButton("Εξόφληση Λογαριασμού");
        styleButton(btnPay);
        btnPay.setBackground(new Color(39, 174, 96)); // Πράσινο για την πληρωμή
        btnPay.addActionListener(e -> paySelectedBill());
        topBar.add(btnPay, "pushx, align right");

        add(topBar, "growx, wrap");

        // --- Table ---
        String[] cols = {"Bill ID", "Εκδότης", "Ποσό (€)", "Κατάσταση", "Ημ. Λήξης"};
        billsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        billsTable = new JTable(billsModel);
        styleTable(billsTable);
        
        add(new JScrollPane(billsTable), "grow, push");
    }

    private void loadCustomerBills() {
        try {
            String u = (String) customerSelector.getSelectedItem();
            if (u == null) return;
            selectedCustomer = cfm.getCustomerByUsername(u);
            billsModel.setRowCount(0);
            List<Bill> bills = BillFileStore.findByPayee(selectedCustomer.getVatID());
            for (Bill b : bills) {
                billsModel.addRow(new Object[]{b.getBillid(), b.getIssuerUsername(), b.getAmount(), b.getStatus(), b.getDueDate()});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void paySelectedBill() {
        int row = billsTable.getSelectedRow();
        if (row == -1) {
             JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε έναν λογαριασμό."); return;
        }
        try {
            String id = (String) billsModel.getValueAt(row, 0);
            Bill bill = BillFileStore.findById(id);
            if (bill.getStatus() == BillStatus.PAID) {
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός είναι ήδη εξοφλημένος!"); return;
            }
            if (!selectedCustomer.getBankAccounts().isEmpty()) {
                BankAccount acc = selectedCustomer.getBankAccounts().get(0);
                if (acc.getBalance() >= bill.getAmount()) {
                    acc.reduceBalance(bill.getAmount());
                    bill.setStatus(BillStatus.PAID);
                    bill.setPaid(true);
                    cfm.updateCustomer(selectedCustomer);
                    BillFileStore.updateBill(bill);
                    loadCustomerBills();
                    JOptionPane.showMessageDialog(this, "Η πληρωμή ολοκληρώθηκε επιτυχώς!");
                } else JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο.");
            } else {
                JOptionPane.showMessageDialog(this, "Ο πελάτης δεν διαθέτει τραπεζικό λογαριασμό.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238));
        table.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}