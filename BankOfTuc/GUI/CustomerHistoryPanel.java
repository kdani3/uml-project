package BankOfTuc.GUI;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;

public class CustomerHistoryPanel extends JPanel {
    private final Customer customer;
    private final CustomerFileManager cfm;
    private final Color BRAND_COLOR = new Color(159, 13, 64);
    
    public CustomerHistoryPanel(Customer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;

        setLayout(new MigLayout("fill, insets 30", "[grow]", "[][grow]"));
        setBackground(BRAND_COLOR); 

        initComponents();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Ιστορικό Συναλλαγών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        add(lblTitle, "wrap");

        String[] columns = {"", "Ημερομηνία", "Ποσό", "IBAN/RF", "Αντισυμβαλλόμενος", "Τύπος"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        try {
            var history = TransactionHistoryService.getHistoryForCustomer(customer.getVatID(), cfm);
            int count = 1;
            for (var h : history) {
                model.addRow(new Object[]{
                    count++,
                    h.datetime,
                    h.getAmountDisplay(),
                    h.getIbanDisplay(customer.getVatID()),
                    h.counterpartyName,
                    h.type
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        styleTable(table);
        
        add(new JScrollPane(table), "grow");
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE); 
        header.setForeground(BRAND_COLOR); 
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
    }
}