package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import BankOfTuc.CLI.CLIUtils;
import BankOfTuc.Customer;
import BankOfTuc.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class AdminCustomersPanel extends JPanel {
    private final UserFileManagement ufm;
    private final CustomerFileManager cfm;
    private JTable table;
    private DefaultTableModel model;

    public AdminCustomersPanel(UserFileManagement ufm, CustomerFileManager cfm) {
        this.ufm = ufm;
        this.cfm = cfm;
        setLayout(new BorderLayout());

        // Table Setup
        String[] columns = {"ID", "Username", "Full Name", "Email", "VatID", "Role"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        loadUsersData();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Toolbar Buttons
        JToolBar toolBar = new JToolBar();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnEdit = new JButton("Edit Details (Case 1.2)");
        JButton btnResetPass = new JButton("Reset Pass (Case 1.1)");
        JButton btnDelete = new JButton("Remove User (Case 7)");

        btnRefresh.addActionListener(e -> loadUsersData());
        btnEdit.addActionListener(e -> editSelectedUser());
        btnResetPass.addActionListener(e -> resetPassword());
        btnDelete.addActionListener(e -> deleteSelectedUser());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnEdit);
        toolBar.add(btnResetPass);
        toolBar.addSeparator();
        toolBar.add(btnDelete); // Καλύπτει το Case 7

        add(toolBar, BorderLayout.NORTH);
    }

    private void loadUsersData() {
        model.setRowCount(0);
        for (User u : ufm.getAllUsers()) {
            Customer c = cfm.getCustomerByUsername(u.getUsername());
            if (c == null) continue; // Skip non-customers
            model.addRow(new Object[]{u.getid(), u.getUsername(), u.getFullname(), u.getEmail(), c.getVatID(), u.getRole()});
        }
    }

    private void editSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String username = (String) model.getValueAt(row, 1);
        Customer c = cfm.getCustomerByUsername(username);
        User u = ufm.getUserByUsername(username);

        String newName = JOptionPane.showInputDialog(this, "Enter new Full Name:", c.getFullname());
        if (newName != null && !newName.isEmpty()) {
            c.setFullname(newName);
            u.setFullname(newName);
            cfm.updateCustomer(c);
            ufm.updateUser(u);
            loadUsersData();
            JOptionPane.showMessageDialog(this, "Details Updated!");
        }
    }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String username = (String) model.getValueAt(row, 1);
        User u = ufm.getUserByUsername(username);

        String newPass = CLIUtils.generateUnicodePassword(8);
        u.setPassword(newPass);
        ufm.updateUser(u);
        
        JTextArea area = new JTextArea("New Password for " + username + ":\n" + newPass);
        area.setFont(new Font("Monospaced", Font.BOLD, 14));
        JOptionPane.showMessageDialog(this, area);
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String username = (String) model.getValueAt(row, 1);
        int userId = (int) model.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Delete " + username + "?") == JOptionPane.YES_OPTION) {
            try {
                Customer c = cfm.getCustomerByUsername(username);
                if (c != null) cfm.deleteCustomer(c);
                ufm.deleteUser(userId);
                loadUsersData();
                JOptionPane.showMessageDialog(this, "User Deleted!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}