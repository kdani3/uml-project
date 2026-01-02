package BankOfTuc.GUI;

import BankOfTuc.IndividualCustomer;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;

public class CustomerHistoryPanel extends JPanel {
    private final IndividualCustomer customer;
    private final CustomerFileManager cfm;
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public CustomerHistoryPanel(IndividualCustomer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;

        setLayout(new MigLayout("fill, insets 30", "[grow]", "[][grow]"));
        setBackground(Color.WHITE);

        initComponents();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Ιστορικό Συναλλαγών");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.DARK_GRAY);
        add(lblTitle, "wrap");

        String[] columns = {"A/A", "Ημερομηνία", "Ποσό", "IBAN", "Αντισυμβαλλόμενος", "Τύπος"};
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
        table.setRowHeight(35); // Λίγο πιο ψηλές γραμμές για άνεση
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(255, 235, 238)); // Απαλό κόκκινο/ροζ
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(220, 220, 220)); // Γκρι φόντο
        header.setForeground(Color.DARK_GRAY);          // Σκούρα γράμματα
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
    }
}