package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.CustomerPaymentService;
import BankOfTuc.TimeService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeSimulationPanel extends JPanel {
    private final CustomerFileManager cfm;
    private final TimeService timeService = TimeService.getInstance();
    private JLabel lblCurrentDate;

    public TimeSimulationPanel(CustomerFileManager cfm) {
        this.cfm = cfm;
        setLayout(new GridBagLayout());
        
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Current Date Display
        lblCurrentDate = new JLabel("Current Date: " + timeService.today());
        lblCurrentDate.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblCurrentDate, gbc);

        // Input Target Date
        gbc.gridwidth = 1; gbc.gridy = 1;
        add(new JLabel("Target Date (dd-MM-yyyy):"), gbc);
        
        JTextField txtDate = new JTextField(10);
        gbc.gridx = 1;
        add(txtDate, gbc);

        // Run Button
        JButton btnSimulate = new JButton("Run Simulation");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(btnSimulate, gbc);

        // Logic
        btnSimulate.addActionListener(e -> {
            String dateStr = txtDate.getText();
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate targetDate = LocalDate.parse(dateStr, fmt);
                
                if (targetDate.isAfter(timeService.today())) {
                    runSimulation(targetDate);
                } else {
                    JOptionPane.showMessageDialog(this, "Date must be in the future.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format!");
            }
        });
    }

    private void runSimulation(LocalDate targetDate) {
        // Τρέχουμε το simulation σε νέο Thread για να μην παγώσει το GUI
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
                    
                    // Update UI Label
                    String dateStr = current.toString();
                    SwingUtilities.invokeLater(() -> lblCurrentDate.setText("Simulating: " + dateStr));
                    
                    Thread.sleep(10); // Μικρή καθυστέρηση για εφε
                }
                
                SwingUtilities.invokeLater(() -> {
                    lblCurrentDate.setText("Current Date: " + timeService.today());
                    JOptionPane.showMessageDialog(this, "Simulation Complete!");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}