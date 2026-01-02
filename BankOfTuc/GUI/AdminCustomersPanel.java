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
        setBackground(Color.WHITE);

        // Header
        JLabel lblTitle = new JLabel("Διαχείριση Πελατών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(BRAND_COLOR);
        add(lblTitle, "wrap");

        // Table Setup
        String[] columns = {"ID", "Username", "Ονοματεπώνυμο", "Email", "ΑΦΜ", "Ρόλος"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable(table); // Εφαρμογή του κόκκινου στυλ

        loadUsersData();
        add(new JScrollPane(table), "grow, wrap");

        // Buttons Panel
        JPanel btnPanel = new JPanel(new MigLayout("insets 0", "[][][][]"));
        btnPanel.setOpaque(false);

        btnPanel.add(createBtn("Ανανέωση", e -> loadUsersData()));
        btnPanel.add(createBtn("Επεξεργασία Στοιχείων", e -> editSelectedUser()));
        btnPanel.add(createBtn("Reset Password", e -> resetPassword()));
        btnPanel.add(createBtn("Διαγραφή Πελάτη", e -> deleteSelectedUser()));

        add(btnPanel, "right"); // Κουμπιά δεξιά
    }

    private void loadUsersData() {
        model.setRowCount(0);
        for (User u : ufm.getAllUsers()) {
            Customer c = cfm.getCustomerByUsername(u.getUsername());
            if (c == null) continue; // Skip non-customers (e.g. Admins)
            model.addRow(new Object[]{u.getid(), u.getUsername(), u.getFullname(), u.getEmail(), c.getVatID(), u.getRole()});
        }
    }

    // --- ΛΟΓΙΚΗ ΕΠΕΞΕΡΓΑΣΙΑΣ (ΞΕΧΩΡΙΣΤΑ ΠΕΔΙΑ) ---
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

        // Επιλογές για το τι θέλει να αλλάξει
        Object[] options = {"Username", "Ονοματεπώνυμο", "Email"};
        
        int choice = JOptionPane.showOptionDialog(this,
                "Επιλέξτε το στοιχείο που θέλετε να επεξεργαστείτε:",
                "Επεξεργασία Πελάτη - " + c.getFullname(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]); // Default selection: Fullname

        switch (choice) {
            case 0: // Username
                String newUsername = JOptionPane.showInputDialog(this, "Νέο Username:", c.getUsername());
                if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(c.getUsername())) {
                    // Πρέπει να ενημερώσουμε και τα δύο αντικείμενα
                    c.setUsername(newUsername);
                    u.setUsername(newUsername);
                    saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Username ενημερώθηκε!");
                }
                break;

            case 1: // Ονοματεπώνυμο
                String newName = JOptionPane.showInputDialog(this, "Νέο Ονοματεπώνυμο:", c.getFullname());
                if (newName != null && !newName.trim().isEmpty()) {
                    c.setFullname(newName);
                    u.setFullname(newName);
                    saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Όνομα ενημερώθηκε!");
                }
                break;

            case 2: // Email
                String newEmail = JOptionPane.showInputDialog(this, "Νέο Email:", c.getEmail());
                if (newEmail != null && !newEmail.trim().isEmpty()) {
                    c.setEmail(newEmail);
                    u.setEmail(newEmail);
                    saveChanges(c, u);
                    JOptionPane.showMessageDialog(this, "Το Email ενημερώθηκε!");
                }
                break;
                
            default:
                // Ο χρήστης έκλεισε το παράθυρο ή πάτησε Χ
                break;
        }
    }

    private void saveChanges(Customer c, User u) {
        cfm.updateCustomer(c);
        ufm.updateUser(u);
        loadUsersData(); // Ανανέωση του πίνακα
    }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε πελάτη.");
            return;
        }
        
        String username = (String) model.getValueAt(row, 1);
        User u = ufm.getUserByUsername(username);

        String newPass = CLIUtils.generateUnicodePassword(8);
        u.setPassword(newPass);
        ufm.updateUser(u);
        
        JTextArea area = new JTextArea("Νέος Κωδικός για " + username + ":\n\n" + newPass);
        area.setFont(new Font("Monospaced", Font.BOLD, 16));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Επαναφορά Κωδικού", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ επιλέξτε πελάτη.");
            return;
        }
        
        String username = (String) model.getValueAt(row, 1);
        int userId = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Είστε σίγουροι ότι θέλετε να διαγράψετε τον πελάτη '" + username + "';\nΑυτή η ενέργεια δεν αναιρείται.", 
            "Επιβεβαίωση Διαγραφής", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Customer c = cfm.getCustomerByUsername(username);
                if (c != null) cfm.deleteCustomer(c);
                ufm.deleteUser(userId);
                loadUsersData();
                JOptionPane.showMessageDialog(this, "Ο χρήστης διαγράφηκε επιτυχώς.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Σφάλμα κατά τη διαγραφή: " + ex.getMessage());
            }
        }
    }

    // --- Helpers για Styling ---
    private JButton createBtn(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(BRAND_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238)); // Απαλό κόκκινο selection
        table.setSelectionForeground(Color.BLACK);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(BRAND_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}