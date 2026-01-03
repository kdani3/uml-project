package BankOfTuc.GUI;

import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.Customer;
import BankOfTuc.Logging.TransferLogger;
import BankOfTuc.SepaTransferService;
import BankOfTuc.SwiftTransferService;
import BankOfTuc.TimeService;
import BankOfTuc.Transfers.InterBank;
import BankOfTuc.Transfers.SelfTransfer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTransfersPanel extends JPanel {
    private final CustomerFileManager cfm;
    private final UserFileManagement ufm;
    
    // UI Components
    private JComboBox<String> customerSelector;
    private JComboBox<String> accountSelector;
    private JLabel lblBalance;
    
    // Data
    private Customer selectedCustomer;
    private BankAccount selectedAccount;
    
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public AdminTransfersPanel(CustomerFileManager cfm, UserFileManagement ufm) {
        this.cfm = cfm;
        this.ufm = ufm;
        
        setLayout(new MigLayout("fillx, insets 40", "[][grow]", "[]15[]15[]20[]30[]"));
        setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ: BRAND_COLOR

        // Τίτλος
        JLabel title = new JLabel("Εκτέλεση Μεταφοράς");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(title, "span 2, wrap");

        // 1. Επιλογή Πελάτη
        JLabel lblSelCust = new JLabel("Επιλογή Πελάτη:");
        lblSelCust.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelCust.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(lblSelCust);
        
        customerSelector = new JComboBox<>();
        customerSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cfm.getAllCustomers().forEach(c -> customerSelector.addItem(c.getUsername()));
        
        customerSelector.addActionListener(e -> loadCustomerAccounts());
        add(customerSelector, "growx, wrap");

        // 2. Επιλογή Λογαριασμού (IBAN)
        JLabel lblSelAcc = new JLabel("Επιλογή Λογαριασμού:");
        lblSelAcc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelAcc.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(lblSelAcc);

        accountSelector = new JComboBox<>();
        accountSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountSelector.addActionListener(e -> updateBalance());
        add(accountSelector, "growx, wrap");

        // 3. Υπόλοιπο Επιλεγμένου Λογαριασμού
        lblBalance = new JLabel("Διαθέσιμο Υπόλοιπο: -");
        lblBalance.setFont(new Font("Consolas", Font.BOLD, 18));
        lblBalance.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό για να φαίνεται
        add(lblBalance, "span 2, center, wrap");

        // 4. Κουμπιά Ενεργειών (Grid 2x2)
        JPanel btnPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow]", "[50!][50!]"));
        btnPanel.setOpaque(false);
        
        btnPanel.add(createBtn("1. Ίδια Μεταφορά (Self)", e -> performSelfTransfer()), "grow");
        btnPanel.add(createBtn("2. Διατραπεζική (InterBank)", e -> performInterBankTransfer()), "grow, wrap");
        btnPanel.add(createBtn("3. Έμβασμα SWIFT", e -> performSwiftTransfer()), "grow");
        btnPanel.add(createBtn("4. Έμβασμα SEPA", e -> performSepaTransfer()), "grow");

        add(btnPanel, "span 2, growx, gaptop 20");
        
        // Initial Load
        if (customerSelector.getItemCount() > 0) {
            customerSelector.setSelectedIndex(0);
        }
    }

    // --- UI UPDATE METHODS ---
    private void loadCustomerAccounts() {
        String u = (String) customerSelector.getSelectedItem();
        if (u == null) return;
        selectedCustomer = cfm.getCustomerByUsername(u);
        accountSelector.removeAllItems();
        if (selectedCustomer != null && !selectedCustomer.getBankAccounts().isEmpty()) {
            for (BankAccount acc : selectedCustomer.getBankAccounts()) {
                accountSelector.addItem(acc.getIban() + " (" + acc.getType() + ")");
            }
        } else {
            lblBalance.setText("Δεν βρέθηκαν λογαριασμοί");
        }
    }

    private void updateBalance() {
        if (selectedCustomer == null || accountSelector.getSelectedItem() == null) {
            lblBalance.setText("Διαθέσιμο Υπόλοιπο: -");
            selectedAccount = null;
            return;
        }
        int index = accountSelector.getSelectedIndex();
        if (index >= 0 && index < selectedCustomer.getBankAccounts().size()) {
            selectedAccount = selectedCustomer.getBankAccounts().get(index);
            lblBalance.setText("Υπόλοιπο: " + String.format("%.2f", selectedAccount.getBalance()) + " €");
        }
    }

    // ... (Λογική μεταφορών παραμένει ίδια: performSelfTransfer, performInterBankTransfer, performSwiftTransfer, performSepaTransfer) ...
    // Θα συμπεριλάβω μόνο τις μεθόδους, ο κώδικας λογικής δεν αλλάζει.

    private void performSelfTransfer() {
         if (!validateSelection()) return;
        List<BankAccount> accounts = selectedCustomer.getBankAccounts();
        if (accounts.size() < 2) {
            JOptionPane.showMessageDialog(this, "Ο πελάτης διαθέτει μόνο έναν λογαριασμό.\nΔεν μπορεί να γίνει Ίδια Μεταφορά.", "Σφάλμα", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JComboBox<String> targetBox = new JComboBox<>();
        for (BankAccount acc : accounts) {
            if (!acc.getIban().equals(selectedAccount.getIban())) {
                targetBox.addItem(acc.getIban() + " (" + acc.getType() + ")");
            }
        }
        JTextField txtAmount = new JTextField();
        panel.add(new JLabel("Από Λογαριασμό: " + selectedAccount.getIban()));
        panel.add(new JLabel("Προς Λογαριασμό:")); panel.add(targetBox);
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        int result = JOptionPane.showConfirmDialog(this, panel, "Εκτέλεση Ίδιας Μεταφοράς", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                String selectedTargetStr = (String) targetBox.getSelectedItem();
                BankAccount targetAccount = null;
                for (BankAccount acc : accounts) {
                    if (selectedTargetStr.contains(acc.getIban())) { targetAccount = acc; break; }
                }
                if (targetAccount != null) {
                    SelfTransfer st = new SelfTransfer();
                    boolean success = st.sendMoney(selectedCustomer, selectedAccount, targetAccount, cfm, amount);
                    if (success) { updateBalance(); JOptionPane.showMessageDialog(this, "Η μεταφορά ολοκληρώθηκε επιτυχώς!"); } 
                    else { JOptionPane.showMessageDialog(this, "Η μεταφορά απέτυχε (π.χ. ανεπαρκές υπόλοιπο)."); }
                }
            } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Μη έγκυρο ποσό."); }
        }
    }

    private void performInterBankTransfer() {
        if (!validateSelection()) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField(); JTextField txtIban = new JTextField();
        JTextField txtName = new JTextField(); JTextField txtBic = new JTextField("DIAS");
        panel.add(new JLabel("Από: " + selectedAccount.getIban()));
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("IBAN Παραλήπτη:")); panel.add(txtIban);
        panel.add(new JLabel("Όνομα Παραλήπτη:")); panel.add(txtName);
        panel.add(new JLabel("BIC / Σύστημα:")); panel.add(txtBic);
        int result = JOptionPane.showConfirmDialog(this, panel, "InterBank Transfer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                int accIndex = selectedCustomer.getBankAccounts().indexOf(selectedAccount);
                InterBank ib = new InterBank();
                int status = ib.sendMoney(selectedCustomer, accIndex, txtBic.getText(), txtIban.getText(), txtName.getText(), cfm, amount, "Admin Action", 1);
                handleInterBankStatus(status); updateBalance();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα στα στοιχεία εισόδου."); }
        }
    }

    private void performSwiftTransfer() {
        if (!validateSelection()) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField(); JTextField txtName = new JTextField();
        JTextField txtAccNum = new JTextField(); JTextField txtSwiftCode = new JTextField();
        JTextField txtCountry = new JTextField();
        panel.add(new JLabel("Από: " + selectedAccount.getIban()));
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("Όνομα Παραλήπτη:")); panel.add(txtName);
        panel.add(new JLabel("IBAN / Λογαριασμός:")); panel.add(txtAccNum);
        panel.add(new JLabel("Swift Code (BIC):")); panel.add(txtSwiftCode);
        panel.add(new JLabel("Χώρα:")); panel.add(txtCountry);
        int result = JOptionPane.showConfirmDialog(this, panel, "SWIFT Transfer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                if (selectedAccount.getBalance() < amount) { JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο."); return; }
                SwiftTransferService swift = new SwiftTransferService();
                String date = TimeService.getInstance().today().toString();
                boolean apiSuccess = swift.sendSwiftTransferRequest(amount, txtName.getText(), txtAccNum.getText(), txtSwiftCode.getText(), date, "SHARED");
                if (apiSuccess) {
                    selectedAccount.reduceBalance(amount);
                    finishAndLog("SWIFT", selectedAccount.getIban(), txtAccNum.getText(), txtName.getText(), "-", txtSwiftCode.getText(), amount, selectedAccount.getBalance(), 0, "Country: " + txtCountry.getText());
                    JOptionPane.showMessageDialog(this, "Το έμβασμα SWIFT εστάλη επιτυχώς!");
                } else { JOptionPane.showMessageDialog(this, "Το αίτημα SWIFT απέτυχε (API Error)."); }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage()); }
        }
    }

    private void performSepaTransfer() {
        if (!validateSelection()) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField(); JTextField txtName = new JTextField();
        JTextField txtIban = new JTextField(); JTextField txtBic = new JTextField();
        JTextField txtCountry = new JTextField();
        panel.add(new JLabel("Από: " + selectedAccount.getIban()));
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("Όνομα Παραλήπτη:")); panel.add(txtName);
        panel.add(new JLabel("IBAN Παραλήπτη:")); panel.add(txtIban);
        panel.add(new JLabel("BIC Τράπεζας:")); panel.add(txtBic);
        panel.add(new JLabel("Χώρα:")); panel.add(txtCountry);
        int result = JOptionPane.showConfirmDialog(this, panel, "SEPA Transfer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                if (selectedAccount.getBalance() < amount) { JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο."); return; }
                SepaTransferService sepa = new SepaTransferService();
                String date = TimeService.getInstance().today().toString();
                boolean apiSuccess = sepa.sendSepaTransferRequest(amount, txtName.getText(), txtIban.getText(), txtBic.getText(), date, "SHARED");
                if (apiSuccess) {
                    selectedAccount.reduceBalance(amount);
                    finishAndLog("SEPA", selectedAccount.getIban(), txtIban.getText(), txtName.getText(), "-", txtBic.getText(), amount, selectedAccount.getBalance(), 0, "SEPA Transfer");
                    JOptionPane.showMessageDialog(this, "Το έμβασμα SEPA εστάλη επιτυχώς!");
                } else { JOptionPane.showMessageDialog(this, "Το αίτημα SEPA απέτυχε (API Error)."); }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage()); }
        }
    }

    private void handleInterBankStatus(int status) {
        switch (status) {
            case 1: JOptionPane.showMessageDialog(this, "Επιτυχία!"); break;
            case -5: JOptionPane.showMessageDialog(this, "Ο παραλήπτης δεν βρέθηκε (Local DB)."); break;
            case -2: JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο."); break;
            case -3: JOptionPane.showMessageDialog(this, "Λάθος όνομα παραλήπτη."); break;
            default: JOptionPane.showMessageDialog(this, "Η μεταφορά απέτυχε. Κωδικός: " + status); break;
        }
    }

    private void finishAndLog(String transferType, String fromAccount, String toAccount, String recipientName, String receiverVatId, String bankCode, double amount, double fromBalance, double toBalance, String details) {
        cfm.updateCustomer(selectedCustomer); updateBalance();
        TransferLogger.logTransfer(selectedCustomer.getVatID(), fromAccount, transferType, receiverVatId, recipientName, bankCode, toAccount, amount, true, fromBalance, toBalance, details);
    }

    private boolean validateSelection() {
        if (selectedCustomer == null || selectedAccount == null) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε Πελάτη και Λογαριασμό."); return false;
        } return true;
    }

    private JButton createBtn(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Αλλαγή: Λευκό κουμπί, Κόκκινα γράμματα
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }
}