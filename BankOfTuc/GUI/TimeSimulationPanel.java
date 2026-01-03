package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.CustomerPaymentService;
import BankOfTuc.TimeService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeSimulationPanel extends JPanel {
    private final CustomerFileManager cfm;
    private final TimeService timeService = TimeService.getInstance();
    private JLabel lblCurrentDate;
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public TimeSimulationPanel(CustomerFileManager cfm) {
        this.cfm = cfm;
        // Layout: Center
        setLayout(new MigLayout("fill, insets 50", "[center]", "[center]"));
        setBackground(BRAND_COLOR); // <--- ΑΛΛΑΓΗ: BRAND_COLOR Background
        
        initComponents();
    }

    private void initComponents() {
        // Κάρτα Προσομοίωσης (Λευκό κουτί)
        JPanel card = new JPanel(new MigLayout("wrap 1, insets 30, fillx", "[350!]", "[]20[]10[]20[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(BRAND_COLOR, 2));

        // Header
        JLabel title = new JLabel("Προσομοίωση Χρόνου");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(BRAND_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "center");

        // Current Date
        lblCurrentDate = new JLabel("Τρέχουσα: " + timeService.today().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        lblCurrentDate.setFont(new Font("Consolas", Font.BOLD, 18));
        lblCurrentDate.setForeground(Color.DARK_GRAY);
        lblCurrentDate.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblCurrentDate, "center");

        // Input
        card.add(new JLabel("Ημερομηνία Στόχος (dd-MM-yyyy):"));
        JTextField txtDate = new JTextField();
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtDate.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(txtDate, "growx, h 40!");

        // Button
        JButton btnSimulate = new JButton("Έναρξη Προσομοίωσης");
        btnSimulate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimulate.setBackground(BRAND_COLOR);
        btnSimulate.setForeground(Color.black);
        btnSimulate.setFocusPainted(false);
        btnSimulate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(btnSimulate, "growx, h 45!");

        add(card);

        // Logic
        btnSimulate.addActionListener(e -> {
            String dateStr = txtDate.getText();
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate targetDate = LocalDate.parse(dateStr, fmt);
                
                if (targetDate.isAfter(timeService.today())) {
                    runSimulation(targetDate);
                } else {
                    JOptionPane.showMessageDialog(this, "Η ημερομηνία πρέπει να είναι μελλοντική.", "Σφάλμα", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Μη έγκυρη μορφή ημερομηνίας (dd-MM-yyyy)!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void runSimulation(LocalDate targetDate) {
        // Run in thread to prevent UI freezing
        new Thread(() -> {
            try {
                CustomerPaymentService payService = new CustomerPaymentService("ADMIN_SIM", cfm);
                LocalDate current = timeService.today();

                if (!timeService.isSimulated()) timeService.startSimulation();

                while (current.isBefore(targetDate)) {
                    timeService.advanceHours(1);
                    if (timeService.now().getHour() == 8) {
                        payService.processDuePayments();
                    }
                    current = timeService.today();
                    
                    String dateStr = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    SwingUtilities.invokeLater(() -> lblCurrentDate.setText("Προσομοίωση: " + dateStr));
                    
                    Thread.sleep(15); 
                }
                
                SwingUtilities.invokeLater(() -> {
                    lblCurrentDate.setText("Τρέχουσα: " + timeService.today().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                    JOptionPane.showMessageDialog(this, "Η προσομοίωση ολοκληρώθηκε!");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}