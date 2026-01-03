package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.*;
import BankOfTuc.Payments.Bill.BillStatus;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CustomerPaymentsPanel extends JPanel {
    private final IndividualCustomer customer;
    private final CustomerFileManager cfm;
    private final CustomerPaymentService paymentService;
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    // Tables
    private DefaultTableModel billsModel;
    private DefaultTableModel recurringModel;
    private JTable recurringTable;

    public CustomerPaymentsPanel(IndividualCustomer customer, CustomerFileManager cfm) throws IOException {
        this.customer = customer;
        this.cfm = cfm;
        this.paymentService = new CustomerPaymentService(customer.getVatID(), cfm);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JTabbedPane internalTabs = new JTabbedPane();
        internalTabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        internalTabs.addTab("Εξόφληση Λογαριασμών", createBillsPanel());
        internalTabs.addTab("Πάγιες Εντολές", createRecurringPanel());

        add(internalTabs, BorderLayout.CENTER);
    }

    // --- TAB 1: ONE-TIME BILL PAYMENTS ---
    private JPanel createBillsPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        panel.setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ

        JLabel lblTitle = new JLabel("Οι Λογαριασμοί μου (Απλήρωτοι)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ
        panel.add(lblTitle, "wrap");

        String[] cols = {"RF Code", "Εκδότης", "Ποσό (€)", "Ημ. Λήξης"};
        billsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable billsTable = new JTable(billsModel);
        styleTable(billsTable);
        panel.add(new JScrollPane(billsTable), "grow, wrap");

        // Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false); // Διαφανές
        
        JButton btnPay = new JButton("Πληρωμή Επιλεγμένου");
        styleButton(btnPay);
        btnPay.addActionListener(e -> paySelectedBill(billsTable));
        
        JButton btnPayManual = new JButton("Πληρωμή με RF (Χειροκίνητα)");
        styleButton(btnPayManual);
        btnPayManual.addActionListener(e -> payManualRF());

        actions.add(btnPayManual);
        actions.add(btnPay);
        panel.add(actions, "growx");

        loadBills();
        return panel;
    }

    private void loadBills() {
        billsModel.setRowCount(0);
        try {
            List<Bill> bills = BillFileStore.findByPayee(customer.getVatID());
            for (Bill b : bills) {
                if (b.getStatus() != BillStatus.PAID) {
                    billsModel.addRow(new Object[]{b.getRfcode(), b.getIssuerUsername(), b.getAmount(), b.getDueDate()});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void paySelectedBill(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Επιλέξτε έναν λογαριασμό."); return;
        }
        String rf = (String) billsModel.getValueAt(row, 0);
        processPayment(rf);
    }

    private void payManualRF() {
        String rf = JOptionPane.showInputDialog(this, "Εισάγετε κωδικό RF:");
        if (rf != null && !rf.trim().isEmpty()) {
            processPayment(rf.trim());
        }
    }

    private void processPayment(String rf) {
        try {
            Bill bill = BillFileStore.findByRFCode(rf);
            if (bill == null) { JOptionPane.showMessageDialog(this, "Ο κωδικός RF δεν βρέθηκε."); return; }
            if (bill.getStatus() == BillStatus.PAID) { JOptionPane.showMessageDialog(this, "Εξοφλημένος."); return; }

            if (customer.getBankAccounts().isEmpty()) { JOptionPane.showMessageDialog(this, "Δεν έχετε λογαριασμούς."); return; }

            // Επιλογή λογαριασμού χρέωσης
            BankAccount acc = customer.getBankAccounts().get(0); // Default first
            if (customer.getBankAccounts().size() > 1) {
                // ... (Logic for selecting account dialog could be added here similar to transfers)
            }

            if (acc.getBalance() >= bill.getAmount()) {
                acc.reduceBalance(bill.getAmount());
                bill.setStatus(BillStatus.PAID);
                bill.setPaid(true);
                cfm.updateCustomer(customer);
                BillFileStore.updateBill(bill);
                loadBills();
                JOptionPane.showMessageDialog(this, "Επιτυχής Πληρωμή!");
            } else {
                JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- TAB 2: RECURRING PAYMENTS ---
    private JPanel createRecurringPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        panel.setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ

        JLabel lblTitle = new JLabel("Διαχείριση Πάγιων Εντολών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ
        panel.add(lblTitle, "wrap");

        String[] cols = {"RF Code", "Επόμενη Πληρωμή", "Κατάσταση", "Προσπάθειες"};
        recurringModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recurringTable = new JTable(recurringModel);
        styleTable(recurringTable);
        panel.add(new JScrollPane(recurringTable), "grow, wrap");

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnAdd = new JButton("Νέα Πάγια"); styleButton(btnAdd); 
        JButton btnPause = new JButton("Παύση"); styleButton(btnPause);
        JButton btnResume = new JButton("Συνέχιση"); styleButton(btnResume);
        JButton btnCancel = new JButton("Ακύρωση"); styleButton(btnCancel); 

        btnAdd.addActionListener(e -> addRecurring());
        btnPause.addActionListener(e -> toggleStatus(true));
        btnResume.addActionListener(e -> toggleStatus(false));
        btnCancel.addActionListener(e -> cancelRecurring());

        btnPanel.add(btnAdd); btnPanel.add(btnPause); btnPanel.add(btnResume); btnPanel.add(btnCancel);
        panel.add(btnPanel, "growx");

        loadRecurring();
        return panel;
    }

    private void loadRecurring() {
        recurringModel.setRowCount(0);
        try {
            List<RecurringPayment> list = paymentService.getPayments();
            for (RecurringPayment p : list) {
                String status = p.isPaused() ? "PAUSED" : "ACTIVE";
                recurringModel.addRow(new Object[]{p.getRfCode(), p.getNextDueDate(), status, p.getCurrentAttempts()});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addRecurring() {
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtRF = new JTextField();
        JComboBox<String> accBox = new JComboBox<>();
        customer.getBankAccounts().forEach(a -> accBox.addItem(a.getIban()));

        form.add(new JLabel("RF Code:")); form.add(txtRF);
        form.add(new JLabel("Λογαριασμός Χρέωσης:")); form.add(accBox);

        if (JOptionPane.showConfirmDialog(this, form, "Νέα Πάγια Εντολή", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String rf = txtRF.getText().trim();
                String iban = (String) accBox.getSelectedItem();
                
                Bill b = BillFileStore.findByRFCode(rf);
                if (b != null && b.getpayerID().equals(customer.getVatID())) {
                    paymentService.addPayment(rf, iban, b.getAmount(), customer.getVatID());
                    loadRecurring();
                    JOptionPane.showMessageDialog(this, "Προστέθηκε!");
                } else {
                    JOptionPane.showMessageDialog(this, "Λάθος RF ή δεν ανήκει σε εσάς.");
                }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage()); }
        }
    }

    private void toggleStatus(boolean pause) {
        int row = recurringTable.getSelectedRow();
        if (row == -1) return;
        String rf = (String) recurringModel.getValueAt(row, 0);
        try {
            if (pause) paymentService.pausePayment(rf);
            else paymentService.resumePayment(rf);
            loadRecurring();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cancelRecurring() {
        int row = recurringTable.getSelectedRow();
        if (row == -1) return;
        String rf = (String) recurringModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Σίγουρα θέλετε διαγραφή;") == JOptionPane.YES_OPTION) {
            try {
                paymentService.cancelPayment(rf);
                loadRecurring();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- STYLING HELPERS ---
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.BLACK);
        btn.setBackground(Color.WHITE);
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
        header.setBackground(Color.WHITE);
        header.setForeground(BRAND_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}