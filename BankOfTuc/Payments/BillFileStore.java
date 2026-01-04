package BankOfTuc.Payments;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import BankOfTuc.Services.TimeService;

public class BillFileStore {

    private static final String FILE = "data/bills.csv";

    public static void saveBill(Bill bill) throws IOException {
        File file = new File(FILE);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); //creates all missing parent folders
        }

        boolean fileExists = file.exists();

        try (FileWriter fw = new FileWriter(FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            if (!fileExists) {
                bw.write("Billid,rfcode,issuer username,payee username,Total Amount,Monthly Amount,Paid amount,issueDate,dueDate,isPaid,payDate,status,installments,paid installments\n");
            }

            bw.write(toCSV(bill));
            bw.newLine();
        }
    }

    public static List<Bill> loadBills() throws IOException {
        List<Bill> bills = new ArrayList<>();
        File file = new File(FILE);

        if (!file.exists()) return bills;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line = br.readLine(); 

            while ((line = br.readLine()) != null) {
                bills.add(fromCSV(line));
            }
        }
        expireOverdueBills(bills);//auto expire on loading
        updateMonthlyBills(bills);//change monthly bills if the month has changed
        return bills;
    }

    public static boolean deleteBill(Bill billToDelete, List<Bill> bills) throws IOException {

        boolean removed = bills.removeIf(
            bill -> bill.getBillid().equals(billToDelete.getBillid())
        );
        if (!removed) {
            return false;
        }
        overwriteFile(bills);
        return true;
    }

    public static void updateBill(Bill updatedBill,List<Bill> bills) throws IOException {

        for (int i = 0; i < bills.size(); i++) {
            if (bills.get(i).getBillid().equals(updatedBill.getBillid())) {
                bills.set(i, updatedBill);
                break;
            }
        }
        overwriteFile(bills);
    }

    public static void updateBill(Bill updatedBill) throws IOException {
        List<Bill> bills = loadBills();
        for (int i = 0; i < bills.size(); i++) {
            if (bills.get(i).getBillid().equals(updatedBill.getBillid())) {
                bills.set(i, updatedBill);
                break;
            }
        }
        overwriteFile(bills);
    }

    private static void overwriteFile(List<Bill> bills) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE))) {
            bw.write("Billid,rfcode,issuer username,payee username,Total Amount,Monthly Amount,Paid amount,issueDate,dueDate,isPaid,payDate,status,installments,paid installments\n");

            for (Bill bill : bills) {
                bw.write(toCSV(bill));
                bw.newLine();
            }
        }
    }

    private static String toCSV(Bill b) {
        String payDateStr = (b.getPayDate() == null) ? "" : b.getPayDate().toString();
        String monthlyAmount = (b.getMonthlyAmount() == 0) ? "" : String.valueOf(b.getMonthlyAmount());
        return String.join(",",
                b.getBillid(),
                b.getRfcode(),
                b.getIssuerUsername(),
                b.getpayerID(),
                String.valueOf(b.getAmount()),
                monthlyAmount,
                String.valueOf(b.getPaidAmount()),
                b.getIssueDate().toString(),
                b.getDueDate().toString(),
                String.valueOf(b.isPaid()),
                payDateStr,
                b.getStatus().name(),
                String.valueOf(b.getInstallments()),
                String.valueOf(b.getPaidInstallments())
        );
    }

    private static Bill fromCSV(String line) {

        String[] data = line.split(",", -1); // keep trailing empty fields

        // helper to safely get field value
        java.util.function.IntFunction<String> g = (i) -> (i >= 0 && i < data.length) ? data[i] : "";

        String billid = g.apply(0);
        String rf = g.apply(1);
        String issuer = g.apply(2);
        String payee = g.apply(3);

        double amount = 0.0;
        try { amount = Double.parseDouble(g.apply(4)); } catch (Exception ignored) {}

        java.time.LocalDate issue = TimeService.getInstance().today();
        try { if (!g.apply(7).isEmpty()) issue = LocalDate.parse(g.apply(7)); } catch (Exception ignored) {}

        java.time.LocalDate due = TimeService.getInstance().today();
        try { if (!g.apply(8).isEmpty()) due = LocalDate.parse(g.apply(8)); } catch (Exception ignored) {}

        int installments = 1;
        try { if (!g.apply(12).isEmpty()) installments = Integer.parseInt(g.apply(12)); } catch (Exception ignored) {}

        Bill b = new Bill(billid, amount, issue, due, installments, issuer, payee);

        try { if (!g.apply(10).isEmpty()) b.setPayDate(LocalDate.parse(g.apply(10))); } catch (Exception ignored) {}
        try { if (!g.apply(5).isEmpty()) b.setMonthlyAmount(Double.parseDouble(g.apply(5))); } catch (Exception ignored) {}
        try { b.setPaidAmount(Double.parseDouble(g.apply(6))); } catch (Exception ignored) {}
        if (!rf.isEmpty()) b.setRfcode(rf);
        try { b.setPaid(Boolean.parseBoolean(g.apply(9))); } catch (Exception ignored) {}
        try { if (!g.apply(11).isEmpty()) b.setStatus(Bill.BillStatus.valueOf(g.apply(11))); } catch (Exception ignored) {}
        try { b.setPaidInstallments(Integer.parseInt(g.apply(13))); } catch (Exception ignored) {}
        b.setIssuerUsername(issuer);
        return b;
    }

    public static Bill findByRFCode(String rfcode) throws IOException {
        List<Bill> bills = loadBills();

        for (Bill bill : bills) {
            if (bill.getRfcode().equals(rfcode)) {
                return bill;
            }
        }
        return null; 
    }

    public static void expireOverdueBills(List<Bill> bills) throws IOException {

        LocalDate now = TimeService.getInstance().today();
        boolean changed = false;

        for (Bill bill : bills) {

            if (bill.getStatus() == Bill.BillStatus.ACTIVE &&
                bill.getDueDate().isBefore(now)) {

                bill.setStatus(Bill.BillStatus.EXPIRED);
                changed = true;
            }
        }

        if (changed) {
            overwriteFile(bills); 
        }
    }

    public static void updateMonthlyBills(List<Bill> bills) throws IOException {

        LocalDate now = TimeService.getInstance().today();
        boolean changed = false;

        for (Bill bill : bills) {

            if (bill.getStatus() == Bill.BillStatus.MONTHLY_PAID) {
                if (bill.getPayDate() == null || bill.getPayDate().getMonth() != now.getMonth()) {
                    bill.setStatus(Bill.BillStatus.ACTIVE);
                    changed = true;
                }
            }
        }

        if (changed) {
            overwriteFile(bills); 
        }
    }

    public static int getCompanyBillsNum(String compVatID,List<Bill> bills) throws IOException{
        int companyBills=0;

        int idlength =  compVatID.length();
        for (Bill bill : bills) {
            String billid = bill.getBillid();
            if (billid == null) continue;
            if (billid.length() < idlength) continue;
            String compidfrombill = billid.substring(0,idlength);
            if (compidfrombill.equals(compVatID)) {
                companyBills++;
            }
        }
        return companyBills;
    }

    public static List<Bill> getCompanyBills(String compVatID) throws IOException{
        List<Bill> compBills = new ArrayList<Bill>();
        List<Bill> bills = loadBills();

        int idlength =  compVatID.length();
        for (Bill bill : bills) {
            String billid = bill.getBillid();
            if (billid == null) continue;
            if (billid.length() < idlength) continue;
            String compidfrombill = billid.substring(0,idlength);
            if (compidfrombill.equals(compVatID)) {
                compBills.add(bill);
            }
        }
        return compBills;
    }

    public static Bill getBillbyID(String billID,List<Bill> bills){

        for (Bill bill : bills) {
            if (bill.getBillid().equals(billID)) {
                return bill;
            }
        }
        return null;
    }

    // --- ΝΕΕΣ ΜΕΘΟΔΟΙ ΓΙΑ ΤΟ GUI ---

    // Βρίσκει λογαριασμούς με βάση το ID του πληρωτή (Payee/Customer VAT)
    public static List<Bill> findByPayee(String payeeVatID) throws IOException {
        List<Bill> allBills = loadBills();
        List<Bill> result = new ArrayList<>();
        
        for (Bill b : allBills) {
            // Το getpayerID() αντιστοιχεί στο Payee Username/VatID στο CSV
            if (b.getpayerID().equals(payeeVatID)) {
                result.add(b);
            }
        }
        return result;
    }

    // Βρίσκει έναν λογαριασμό με βάση το Bill ID (φορτώνει αυτόματα)
    public static Bill findById(String billId) throws IOException {
        List<Bill> bills = loadBills();
        return getBillbyID(billId, bills);
    }
}