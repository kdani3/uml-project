package BankOfTuc.GUI;

import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Bookkeeping.UserFileManagement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GuiMain {
    public static void main(String[] args) {
        // Ρύθμιση για να φαίνεται λίγο πιο μοντέρνο (σαν τα Windows/Mac)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Φόρτωση δεδομένων
                UserFileManagement ufm = UserFileManagement.getInstance("data/users.json");
                CustomerFileManager cfm = CustomerFileManager.getInstance("data/customers.json");

                // Εκκίνηση Login Window
                new LoginFrame(ufm, cfm).setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}