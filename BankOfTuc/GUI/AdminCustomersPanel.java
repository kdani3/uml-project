package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.CLI.CLIUtils;
import BankOfTuc.Customer;
import BankOfTuc.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;

public class AdminCustomersPanel extends JPanel {
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private JTable table;
    private DefaultTableModel model;
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public AdminCustomersPanel(UserFileManagement ufm, CustomerFileManager cfm) {
        this.ufm = ufm;
        this.cfm = cfm;
        
        // Layout & Styling
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ: BRAND_COLOR

        // Header
        JLabel lblTitle = new JLabel("Διαχείριση Πελατών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE); // <--- ΑΛΛΑΓΗ: Λευκό κείμενο
        add(lblTitle, "wrap");

        // Table Setup
        String[] columns = {"ID", "Username", "Ονοματεπώνυμο", "Email", "ΑΦΜ", "Ρόλος"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable(table); 

        loadUsersData();
        add(new JScrollPane(table), "grow, wrap");

        // Buttons Panel
        JPanel btnPanel = new JPanel(new MigLayout("insets 0", "[][][][]"));
        btnPanel.setOpaque(false); // Διαφανές για να φαίνεται το κόκκινο από πίσω

        // Κουμπιά (Κρατάμε το στυλ τους, αλλά μπορεί να χρειαστεί να είναι λευκά με κόκκινα γράμματα)
        // Θα τα κάνουμε Λευκά με BRAND_COLOR γράμματα για αντίθεση
        btnPanel.add(createBtn("Ανανέωση", e -> loadUsersData()));
        btnPanel.add(createBtn("Επεξεργασία Στοιχείων", e -> editSelectedUser()));
        btnPanel.add(createBtn("Reset Password", e -> resetPassword()));
        btnPanel.add(createBtn("Remove QR", e -> removeQrSelectedUser()));
        btnPanel.add(createBtn("Διαγραφή Πελάτη", e -> deleteSelectedUser()));

        add(btnPanel, "right");
    }

    // ... (Οι μέθοδοι loadUsersData, editSelectedUser, saveChanges, resetPassword, deleteSelectedUser παραμένουν ίδιες) ...
    private void loadUsersData() {
        model.setRowCount(0);
        for (User u : ufm.getAllUsers()) {
            Customer c = cfm.getCustomerByUsername(u.getUsername());
            if (c == null) continue;
            model.addRow(new Object[]{u.getid(), u.getUsername(), u.getFullname(), u.getEmail(), c.getVatID(), u.getRole()});
        }
    }
    
    private void editSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε έναν πελάτη από τη λίστα.");
            return;
        }

        String currentUsername = (String) model.getValueAt(row, 1);
        Customer c = cfm.getCustomerByUsername(currentUsername);
        User u = ufm.getUserByUsername(currentUsername);

        if (c == null || u == null) {
            JOptionPane.showMessageDialog(this, "Σφάλμα: Δεν βρέθηκαν τα δεδομένα του χρήστη.");
            return;
        }

        Object[] options = {"Username", "Ονοματεπώνυμο", "Email"};
        int choice = JOptionPane.showOptionDialog(this,
                "Επιλέξτε το στοιχείο που θέλετε να επεξεργαστείτε:",
                "Επεξεργασία Πελάτη - " + c.getFullname(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        switch (choice) {
            case 0:
                String newUsername = JOptionPane.showInputDialog(this, "Νέο Username:", c.getUsername());
                if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(c.getUsername())) {
                    c.setUsername(newUsername); u.setUsername(newUsername); saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Username ενημερώθηκε!");
                }
                break;
            case 1:
                String newName = JOptionPane.showInputDialog(this, "Νέο Ονοματεπώνυμο:", c.getFullname());
                if (newName != null && !newName.trim().isEmpty()) {
                    c.setFullname(newName); u.setFullname(newName); saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Όνομα ενημερώθηκε!");
                }
                break;
            case 2:
                String newEmail = JOptionPane.showInputDialog(this, "Νέο Email:", c.getEmail());
                if (newEmail != null && !newEmail.trim().isEmpty()) {
                    c.setEmail(newEmail); u.setEmail(newEmail); saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Email ενημερώθηκε!");
                }
                break;
        }
    }

    private void saveChanges(Customer c, User u) { cfm.updateCustomer(c); ufm.updateUser(u); loadUsersData(); }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε πελάτη."); return; }
        String username = (String) model.getValueAt(row, 1);
        User u = ufm.getUserByUsername(username);
        String newPass = CLIUtils.generateUnicodePassword(8);
        u.setPassword(newPass);
        ufm.updateUser(u);
        JTextArea area = new JTextArea("Νέος Κωδικός για " + username + ":\n\n" + newPass);
        area.setFont(new Font("Monospaced", Font.BOLD, 16)); area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Επαναφορά Κωδικού", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε πελάτη."); return; }
        String username = (String) model.getValueAt(row, 1);
        int userId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '" + username + "';", "Επιβεβαίωση Διαγραφής", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Customer c = cfm.getCustomerByUsername(username);
                if (c != null) cfm.deleteCustomer(c);
                ufm.deleteUser(userId);
                loadUsersData();
                JOptionPane.showMessageDialog(this, "Ο χρήστης διαγράφηκε επιτυχώς.");
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
    private void removeQrSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε πελάτη."); return; }
        String username = (String) model.getValueAt(row, 1);
        User u = ufm.getUserByUsername(username);
        if (u == null) { JOptionPane.showMessageDialog(this, "Σφάλμα: δεν βρέθηκε ο χρήστης."); return; }
        if (!u.hasQR()) { JOptionPane.showMessageDialog(this, "Ο χρήστης δεν έχει QR."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove QR for '" + username + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            u.setQrCode(null);
            ufm.updateUser(u);
            loadUsersData();
            JOptionPane.showMessageDialog(this, "QR removed for user: " + username);
        }
    }
    // --- Helpers για Styling ---
    private JButton createBtn(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Αλλαγή: Λευκό κουμπί με κόκκινα γράμματα
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238));
        table.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE); // Λευκό Header για αντίθεση
        header.setForeground(BRAND_COLOR); // Κόκκινα γράμματα
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}