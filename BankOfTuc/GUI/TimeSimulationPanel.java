package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Payments.CustomerPaymentService;
import BankOfTuc.Services.TimeService;
import BankOfTuc.Bookkeeping.BankRevenue; 
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeSimulationPanel extends JPanel {
    private final CustomerFileManager cfm;
    private final TimeService timeService = TimeService.getInstance();
    private JLabel lblCurrentDate;
    private JTextArea logArea; 
    
    // Branding
    private final Color BRAND_COLOR = new Color(159, 13, 64);

    public TimeSimulationPanel(CustomerFileManager cfm) {
        this.cfm = cfm;
        // Layout: Χωρίζουμε σε πάνω μέρος (Input) και κάτω μέρος (Log)
        setLayout(new MigLayout("fill, insets 20", "[center]", "[][]"));
        setBackground(BRAND_COLOR); 
        
        initComponents();
    }

    private void initComponents() {
      
        JPanel inputCard = new JPanel(new MigLayout("wrap 1, insets 30, fillx", "[350!]", "[]20[]10[]20[]"));
        inputCard.setBackground(Color.WHITE);
        inputCard.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Header
        JLabel title = new JLabel("Προσομοίωση Χρόνου");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(BRAND_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        inputCard.add(title, "center");

        // Current Date Display
        lblCurrentDate = new JLabel("Τρέχουσα: " + timeService.today().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        lblCurrentDate.setFont(new Font("Consolas", Font.BOLD, 18));
        lblCurrentDate.setForeground(Color.DARK_GRAY);
        lblCurrentDate.setHorizontalAlignment(SwingConstants.CENTER);
        inputCard.add(lblCurrentDate, "center");

        // Input Field
        inputCard.add(new JLabel("Ημερομηνία Στόχος (dd-MM-yyyy):"));
        JTextField txtDate = new JTextField();
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtDate.setHorizontalAlignment(SwingConstants.CENTER);
        inputCard.add(txtDate, "growx, h 40!");

        // Action Button
        JButton btnSimulate = new JButton("Έναρξη Προσομοίωσης");
        btnSimulate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimulate.setBackground(BRAND_COLOR);
        btnSimulate.setForeground(Color.black);
        btnSimulate.setFocusPainted(false);
        btnSimulate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inputCard.add(btnSimulate, "growx, h 45!");

        add(inputCard, "wrap, gapbottom 20");

        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setForeground(new Color(50, 50, 50));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Simulation Log"));
        
        add(scrollPane, "grow, h 250!");

 
        btnSimulate.addActionListener(e -> {
            String dateStr = txtDate.getText();
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate targetDate = LocalDate.parse(dateStr, fmt);
                
                if (targetDate.isAfter(timeService.today())) {
                    // Start simulation logic
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
   
        logArea.setText("Starting simulation...\n");

        // Run in thread to prevent UI freezing
        new Thread(() -> {
            try {
              
                BankRevenue.reset(); 
                
                CustomerPaymentService payService = new CustomerPaymentService("ADMIN_SIM", cfm);
                LocalDate current = timeService.today();

                if (!timeService.isSimulated()) timeService.startSimulation();
         
                String startDateStr = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                while (current.isBefore(targetDate)) {
                    // Advance time logic (e.g., skip 1 hour)
                    timeService.advanceHours(1);
                    
                    // Trigger daily check at 8:00 AM
                    if (timeService.now().getHour() == 8) {
                        payService.processDuePayments();
                        
                        // Optional: Log daily progress
                        final String dayLog = "Processed day: " + timeService.today() + "\n";
                        SwingUtilities.invokeLater(() -> logArea.append(dayLog));
                    }
                    
                    current = timeService.today();
                    
                    // Update UI Label
                    String dateStr = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    SwingUtilities.invokeLater(() -> lblCurrentDate.setText("Προσομοίωση: " + dateStr));
                    
                    Thread.sleep(10); // Speed up simulation (lower sleep)
                }
                
          
        
                double totalFees = BankRevenue.getTotalFees();
                String endDateStr = timeService.today().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                SwingUtilities.invokeLater(() -> {
                    lblCurrentDate.setText("Τρέχουσα: " + endDateStr);
                    
                    // Append Report to Log
                    logArea.append("\n=== TIME SIMULATION REPORT ===\n");
                    logArea.append("Period: " + startDateStr + " to " + endDateStr + "\n");
                    logArea.append("Total Bank Fees Collected: " + String.format("%.2f €", totalFees) + "\n");
                    logArea.append("=============================\n");
                    
                    // Show Popup
                    JOptionPane.showMessageDialog(this, 
                        "Η προσομοίωση ολοκληρώθηκε!",
                        "Simulation Report", 
                        JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}