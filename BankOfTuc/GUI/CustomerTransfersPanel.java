package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;
import BankOfTuc.Services.SepaTransferService;
import BankOfTuc.Services.SwiftTransferService;
import BankOfTuc.Services.TimeService;
import BankOfTuc.Transfers.InterBank;
import BankOfTuc.Transfers.SelfTransfer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerTransfersPanel extends JPanel {
    private final IndividualCustomer customer;
    private final CustomerFileManager cfm;
    
    // UI Components
    private JComboBox<String> accountSelector;
    private JLabel lblBalance;
    private BankAccount selectedAccount;
    
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public CustomerTransfersPanel(IndividualCustomer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;
        
        setLayout(new MigLayout("fillx, insets 40", "[][grow]", "[]15[]15[]30[]"));
        setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ: BRAND_COLOR

        // Τίτλος
        JLabel title = new JLabel("Εκτέλεση Μεταφοράς");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(title, "span 2, wrap");

        // 1. Επιλογή Λογαριασμού
        JLabel lblSelAcc = new JLabel("Από Λογαριασμό:");
        lblSelAcc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelAcc.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(lblSelAcc);

        accountSelector = new JComboBox<>();
        accountSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountSelector.addActionListener(e -> updateBalance());
        add(accountSelector, "growx, wrap");

        // 2. Υπόλοιπο
        lblBalance = new JLabel("Διαθέσιμο Υπόλοιπο: -");
        lblBalance.setFont(new Font("Consolas", Font.BOLD, 18));
        lblBalance.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό
        add(lblBalance, "span 2, center, wrap");

        // 3. Κουμπιά Ενεργειών
        JPanel btnPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow]", "[50!][50!]"));
        btnPanel.setOpaque(false);
        
        btnPanel.add(createBtn("1. Ίδια Μεταφορά (Self)", e -> performSelfTransfer()), "grow");
        btnPanel.add(createBtn("2. Διατραπεζική (InterBank)", e -> performInterBankTransfer()), "grow, wrap");
        btnPanel.add(createBtn("3. Έμβασμα SWIFT", e -> performSwiftTransfer()), "grow");
        btnPanel.add(createBtn("4. Έμβασμα SEPA", e -> performSepaTransfer()), "grow");

        add(btnPanel, "span 2, growx, gaptop 20");
        
        loadAccounts();
    }

    private void loadAccounts() {
        accountSelector.removeAllItems();
        if (customer.getBankAccounts().isEmpty()) {
            lblBalance.setText("Δεν βρέθηκαν λογαριασμοί");
            return;
        }
        for (BankAccount acc : customer.getBankAccounts()) {
            accountSelector.addItem(acc.getIban() + " (" + acc.getType() + ")");
        }
        // Trigger update logic
        if (accountSelector.getItemCount() > 0) accountSelector.setSelectedIndex(0);
    }

    private void updateBalance() {
        int index = accountSelector.getSelectedIndex();
        if (index >= 0 && index < customer.getBankAccounts().size()) {
            selectedAccount = customer.getBankAccounts().get(index);
            lblBalance.setText("Υπόλοιπο: " + String.format("%.2f", selectedAccount.getBalance()) + " €");
        }
    }

    // --- TRANSFER LOGIC ---

    private void performSelfTransfer() {
        if (selectedAccount == null) return;
        List<BankAccount> accounts = customer.getBankAccounts();
        if (accounts.size() < 2) {
            JOptionPane.showMessageDialog(this, "Χρειάζεστε τουλάχιστον 2 λογαριασμούς για Ίδια Μεταφορά.", "Σφάλμα", JOptionPane.WARNING_MESSAGE);
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
        panel.add(new JLabel("Προς Λογαριασμό:")); panel.add(targetBox);
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);

        int res = JOptionPane.showConfirmDialog(this, panel, "Ίδια Μεταφορά", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                String targetStr = (String) targetBox.getSelectedItem();
                BankAccount targetAccount = accounts.stream().filter(a -> targetStr.contains(a.getIban())).findFirst().orElse(null);

                if (targetAccount != null) {
                    SelfTransfer st = new SelfTransfer();
                    if (st.sendMoney(customer, selectedAccount, targetAccount, cfm, amount)) {
                        JOptionPane.showMessageDialog(this, "Επιτυχής Μεταφορά!");
                        updateBalance();
                    } else {
                        JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο.");
                    }
                }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Λάθος είσοδος."); }
        }
    }

    private void performInterBankTransfer() {
        if (selectedAccount == null) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField();
        JTextField txtIban = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtBic = new JTextField("DIAS"); // Default
        
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("IBAN Παραλήπτη:")); panel.add(txtIban);
        panel.add(new JLabel("Όνομα Παραλήπτη:")); panel.add(txtName);
        panel.add(new JLabel("BIC / Σύστημα:")); panel.add(txtBic);

        if (JOptionPane.showConfirmDialog(this, panel, "InterBank Transfer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                InterBank ib = new InterBank();
                int idx = customer.getBankAccounts().indexOf(selectedAccount);
                int status = ib.sendMoney(customer, idx, txtBic.getText(), txtIban.getText(), txtName.getText(), cfm, amount, "Web Transfer", 1);
                
                if (status == 1) {
                    JOptionPane.showMessageDialog(this, "Επιτυχία!");
                    updateBalance();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία. Κωδικός: " + status);
                }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα εισόδου."); }
        }
    }

    private void performSwiftTransfer() {
        if (selectedAccount == null) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtAccNum = new JTextField();
        JTextField txtSwift = new JTextField();
        JTextField txtCountry = new JTextField();
        
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("Όνομα:")); panel.add(txtName);
        panel.add(new JLabel("Λογαριασμός/IBAN:")); panel.add(txtAccNum);
        panel.add(new JLabel("Swift Code:")); panel.add(txtSwift);
        panel.add(new JLabel("Χώρα:")); panel.add(txtCountry);

        if (JOptionPane.showConfirmDialog(this, panel, "SWIFT Transfer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                if (selectedAccount.getBalance() < amount) {
                    JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο."); return;
                }
                SwiftTransferService swift = new SwiftTransferService();
                if (swift.sendSwiftTransferRequest(amount, txtName.getText(), txtAccNum.getText(), txtSwift.getText(), TimeService.getInstance().today().toString(), "SHARED")) {
                    selectedAccount.reduceBalance(amount);
                    finishAndLog("SWIFT", selectedAccount.getIban(), txtAccNum.getText(), txtName.getText(), txtSwift.getText(), amount);
                    JOptionPane.showMessageDialog(this, "Το SWIFT εστάλη!");
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα API.");
                }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage()); }
        }
    }

    private void performSepaTransfer() {
        if (selectedAccount == null) return;
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtAmount = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtIban = new JTextField();
        JTextField txtBic = new JTextField();
        
        panel.add(new JLabel("Ποσό (€):")); panel.add(txtAmount);
        panel.add(new JLabel("Όνομα:")); panel.add(txtName);
        panel.add(new JLabel("IBAN:")); panel.add(txtIban);
        panel.add(new JLabel("BIC:")); panel.add(txtBic);

        if (JOptionPane.showConfirmDialog(this, panel, "SEPA Transfer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(txtAmount.getText());
                if (selectedAccount.getBalance() < amount) {
                    JOptionPane.showMessageDialog(this, "Ανεπαρκές υπόλοιπο."); return;
                }
                SepaTransferService sepa = new SepaTransferService();
                if (sepa.sendSepaTransferRequest(amount, txtName.getText(), txtIban.getText(), txtBic.getText(), TimeService.getInstance().today().toString(), "SHARED")) {
                    selectedAccount.reduceBalance(amount);
                    finishAndLog("SEPA", selectedAccount.getIban(), txtIban.getText(), txtName.getText(), txtBic.getText(), amount);
                    JOptionPane.showMessageDialog(this, "Το SEPA εστάλη!");
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα API.");
                }
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Σφάλμα: " + e.getMessage()); }
        }
    }

    private void finishAndLog(String type, String from, String to, String name, String bankCode, double amount) {
        cfm.updateCustomer(customer);
        updateBalance();
        TransferLogger.logTransfer(customer.getVatID(), from, type, "-", name, bankCode, to, amount, true, selectedAccount.getBalance(), 0, "Online Transfer");
    }

    private JButton createBtn(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.black);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }
}