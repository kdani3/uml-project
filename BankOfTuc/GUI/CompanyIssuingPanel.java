package BankOfTuc.GUI;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.BillFileStore;
import BankOfTuc.Services.TimeService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.UUID;

public class CompanyIssuingPanel extends JPanel {
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public CompanyIssuingPanel(CompanyCustomer company, CustomerFileManager cfm) {
        setLayout(new MigLayout("fillx, insets 40", "[right][grow]", "[]15[]15[]15[]30[]"));
        setBackground(BRAND_COLOR); 

        JLabel title = new JLabel("Έκδοση Νέου Λογαριασμού (Χρέωση Πελάτη)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE); 
        add(title, "span 2, center, wrap");

        // Form Fields
        add(createLabel("AFM Πελάτη (Payer VAT):"));
        JTextField txtPayerVat = new JTextField();
        add(txtPayerVat, "growx, wrap");

        add(createLabel("Ποσό (€):"));
        JTextField txtAmount = new JTextField();
        add(txtAmount, "growx, wrap");
        
        add(createLabel("Δόσεις (0 για εφάπαξ):"));
        JTextField txtInstallments = new JTextField("0");
        add(txtInstallments, "growx, wrap");

        JButton btnIssue = new JButton("Έκδοση Λογαριασμού");
        styleButton(btnIssue); 
        
        btnIssue.addActionListener(e -> {
            try {
                String payer = txtPayerVat.getText();
                double amount = Double.parseDouble(txtAmount.getText());
                int installments = Integer.parseInt(txtInstallments.getText());
                
                String billId = UUID.randomUUID().toString().substring(0, 8);
                LocalDate issueDate = TimeService.getInstance().today();
                LocalDate dueDate = issueDate.plusDays(30);

                //Bill newBill = new Bill(billId, amount, issueDate, dueDate, installments, company.getUsername(), payer);
                Bill newBill = new Bill.Builder()
                    .setBillId(billId)
                    .setAmount(amount)
                    .setIssueDate(issueDate)
                    .setDueDate(dueDate)
                    .setInstallments(installments)
                    .setIssuerUsername(company.getUsername())
                    .setPayerID(payer)
                    .build();
                BillFileStore.saveBill(newBill);
                
                JOptionPane.showMessageDialog(this, "Ο λογαριασμός εκδόθηκε επιτυχώς!\nRF Code: " + newBill.getRfcode());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Σφάλμα: " + ex.getMessage());
            }
        });
        
        add(btnIssue, "span 2, center, gaptop 20");
    }
    
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE); 
        return lbl;
    }
    
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(Color.WHITE); 
        btn.setForeground(Color.BLACK); 
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}