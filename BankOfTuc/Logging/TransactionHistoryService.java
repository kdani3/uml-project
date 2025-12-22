// File: BankOfTuc/Services/TransactionHistoryService.java
package BankOfTuc.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;

public class TransactionHistoryService {

    public static class TransactionEntry {
        public final String datetime;       
        public final String type;         
        public final String counterpartyName;
        public final String counterpartyIban;
        public final double amount;
        public final boolean isOutgoing;     
        public final String rfCode;          
        public final String details;         

        public final String senderVatId;
        public final String senderIban;
        public final String receiverVatId;
        public final String receiverIban;
        public final String bankCode;
        public TransactionEntry(String datetime, String type,
                                String counterpartyName, String bankCode, String counterpartyIban,
                                double amount, boolean isOutgoing,
                                String rfCode, String details,
                                String senderVatId, String senderIban,
                                String receiverVatId, String receiverIban) {
            this.datetime = datetime;
            this.type = type;
            this.counterpartyName = counterpartyName;
            this.counterpartyIban = counterpartyIban;
            this.amount = amount;
            this.isOutgoing = isOutgoing;
            this.rfCode = rfCode;
            this.details = details;
            this.senderVatId = senderVatId;
            this.senderIban = senderIban;
            this.receiverVatId = receiverVatId;
            this.receiverIban = receiverIban;
            this.bankCode = bankCode;
        }

        public String getAmountDisplay() {
            if(type.equalsIgnoreCase("SELF")){
                return ("%.2f").formatted(amount);
            }
            return (isOutgoing ? "-%.2f" : "+%.2f").formatted(amount);
        }

        public String getIbanDisplay(String ownVatId) {
            if (senderVatId.equals(ownVatId)) {
                return "→ " + (type.equals("PAYMENT") ? rfCode : counterpartyIban); 
            } else {
                return "← " + (type.equals("PAYMENT") ? rfCode : counterpartyIban);
            }
        }
    }

    public static List<TransactionEntry> getHistoryForCustomer(String customerVatId, CustomerFileManager cfm) throws IOException {
        List<TransactionEntry> history = new ArrayList<>();
        loadPayments(customerVatId, cfm, history);
        loadTransfers(customerVatId, cfm, history);
        
        // Sort: newest first
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy,HH:mm:ss");
        history.sort(
            Comparator.comparing((TransactionHistoryService.TransactionEntry e) ->
                LocalDateTime.parse(e.datetime.replace(" ", ","), formatter)
            ).reversed()
        );

        return history;
    }

    private static String getNameByVatId(String vatId, CustomerFileManager cfm) {
        Customer customer = cfm.getCustomerbyVatid(vatId); 
        if (customer != null) {
            return customer.getFullname();
        }
        return "Unknown (" + vatId + ")";
    }

    private static void loadPayments(String vatId, CustomerFileManager cfm, List<TransactionEntry> list) throws IOException {
        var path = Paths.get("logs/payments.csv");
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 14) continue;

                String status = cols[10];
                if (!"PAID".equals(status) && !"PARTIALLY_PAID".equals(status)) continue;

                String senderVat = cols[2];
                String companyVat = cols[6];
                boolean isOutgoing = vatId.equals(senderVat);

                if (!isOutgoing && !vatId.equals(companyVat)) continue; 

                String datetime = cols[0] + " " + cols[1];
                double amount = Double.parseDouble(cols[9]);
                String rfCode = cols[4];
                String senderIban = cols[3];
                String receiverIban = cols[7];

                String counterpartyName = getNameByVatId(isOutgoing ? companyVat : senderVat, cfm);
                String counterpartyIban = isOutgoing ? receiverIban : senderIban;

                list.add(new TransactionEntry(
                    datetime, "PAYMENT",
                    counterpartyName,"", counterpartyIban,
                    amount, isOutgoing,
                    rfCode, "",
                    senderVat, senderIban,
                    companyVat, receiverIban
                ));
            }
        }
    }

    private static void loadTransfers(String vatId, CustomerFileManager cfm, List<TransactionEntry> list) throws IOException {
        var path = Paths.get("logs/transfers.csv");
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 12) continue; 
            
                if (!"SUCCESS".equals(cols[10])) continue;
            
                String senderVat = cols[2];
                String receiverVat = cols[5];
                boolean isOutgoing = vatId.equals(senderVat);
            
                if (!isOutgoing && !vatId.equals(receiverVat)) continue;
            
                String datetime = cols[0] + " " + cols[1];
                String transferType = cols[4];
                String senderIban = cols[3];
                
                String receiverName = cols[6];  
                String bankCode = cols[7];
                String receiverIban = cols[8];  
                double amount = Double.parseDouble(cols[9]); 
                String details = cols[13];      
            
                String counterpartyName = isOutgoing ? receiverName : getNameByVatId(senderVat, cfm);
                String counterpartyIban = isOutgoing ? receiverIban : senderIban;
            
                list.add(new TransactionEntry(
                    datetime, transferType,
                    counterpartyName,bankCode, counterpartyIban,
                    amount, isOutgoing,
                    "", details,
                    senderVat, senderIban,
                    receiverVat, receiverIban
                ));
            }
        }
    }
}