// File: BankOfTuc/Payments/RecurringPaymentCsvStore.java
package BankOfTuc.Payments;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public class RecurringPaymentCsvStore {

    private static final String FILE = "data/recurring_payments.csv";

    private static final String HEADER = "rfCode,payerVatID,payerIban,amount,nextDueDate,attempts,paused";

    
    public static void save(List<RecurringPayment> payments) throws IOException {
        File file = new File(FILE);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); //creates all missing parent folders
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
            writer.println(HEADER);
            for (RecurringPayment p : payments) {

                String vatId = (p.getPayerVatID());
                writer.printf("%s,%s,%s,%.2f,%s,%d,%s%n",
                    p.getRfCode(),
                    vatId,
                    p.getPayerIban(),
                    p.getmonthlyAmount(), 
                    p.getNextDueDate(),
                    p.getCurrentAttempts(),
                    p.isPaused()
                );
            }
        }
    }

 
    public static List<RecurringPayment> load() throws IOException {
        List<RecurringPayment> payments = new ArrayList<>();
        if (!Files.exists(Path.of(FILE))) {
            return payments;
        }

        try (BufferedReader reader = Files.newBufferedReader(Path.of(FILE))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String[] parts = line.split(",", -1); //-1 preserves empty trailing fields
                if (parts.length < 7) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    String rfCode = parts[0];
                    String payerVatID = parts[1];
                    String payerIban = parts[2];
                    double amount = Double.parseDouble(parts[3]);
                    LocalDate nextDueDate = LocalDate.parse(parts[4]);
                    int attempts = Integer.parseInt(parts[5]);
                    boolean paused = Boolean.parseBoolean(parts[6]);

                    RecurringPayment rp = new RecurringPayment(rfCode, payerVatID, payerIban, amount, nextDueDate);
                    rp.restoreState(nextDueDate, attempts, paused);
                    payments.add(rp);

                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        }
        return payments;
    }

    public static BankAccount resolveBankAccount(RecurringPayment rp, CustomerFileManager cfm) {
        return cfm.findAccountByIBAN(rp.getPayerIban());
    }
}