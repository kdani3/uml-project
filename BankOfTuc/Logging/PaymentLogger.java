package BankOfTuc.Logging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import BankOfTuc.TimeService;

import java.text.DecimalFormat;

public class PaymentLogger {

    private static final String FILE = "logs/payments.csv";
    private static final String HEADER =
            "Date,Time,Sender Vat ID,Sender Account IBAN,RF Code,Bill Type,Company Vat ID,Receiver Account IBAN,Total Bill Amount,Paid Amount,Bill Status,Sender New Balance,Recipient New Balance,Installments Paid";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DecimalFormat MONEY =
            new DecimalFormat("0.00");

    public static synchronized void logTransfer(
            String senderVatId,
            String fromAccount,
            String RFCode,
            String BillType,
            String companyVAT,
            String toAccount,
            double totalAmount,
            double amount,
            String BillStatus,
            double fromBalance,
            double toBalance,
            int installments
    ) {
        ensureLogsDirectoryExists();
        ensureFileExists();
        String date = TimeService.getInstance().now().format(DATE_FORMAT);
        String time = TimeService.getInstance().now().format(TIME_FORMAT);

        String row = toCSV(
                date,
                time,
                senderVatId,
                fromAccount,
                RFCode,
                BillType,
                companyVAT,
                toAccount,
                MONEY.format(totalAmount),
                MONEY.format(amount),
                BillStatus,
                MONEY.format(fromBalance),
                MONEY.format(toBalance),
                String.valueOf(installments)
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(FILE),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        )) {
            writer.write(row);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace(); // Replace with proper logging if needed
        }
    }

    private static void ensureFileExists() {
        Path path = Paths.get(FILE);

        if (Files.exists(path)) return;

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE
        )) {
            writer.write(HEADER);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ensureLogsDirectoryExists() {
        Path logsDir = Paths.get("logs");
        if (!Files.exists(logsDir)) {
            try {
                Files.createDirectories(logsDir);
            } catch (IOException e) {
                System.err.println("Failed to create logs directory: " + e.getMessage());
            }
        }
    }

    private static String toCSV(String... values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            String v = values[i];

            if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                v = "\"" + v.replace("\"", "\"\"") + "\"";
            }

            sb.append(v);

            if (i < values.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
