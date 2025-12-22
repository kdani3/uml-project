package BankOfTuc.Payments;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import BankOfTuc.TimeService;

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
//Billid,rfcode,issuer username,Total Amount,Monthly Amount,Paid amount,issueDate,dueDate,isPaid,payDate,status,installments,paid installments\n
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
//Billid,rfcode,issuer username,Total Amount,Monthly Amount,Paid amount,issueDate,dueDate,isPaid,payDate,status,installments,paid installments\n
//(String Billid, double amount, LocalDate issueDate, LocalDate dueDate, int installments, String issuerUsername)

private static Bill fromCSV(String line) {

        String[] data = line.split(",");

        Bill b = new Bill(
                data[0],
                Double.parseDouble(data[4]),
                LocalDate.parse(data[7]),
                LocalDate.parse(data[8]),
                Integer.parseInt(data[13]),
                data[2],
                data[3]
        );
        if (!data[10].isEmpty()) 
            b.setPayDate(LocalDate.parse(data[10]));
        if (!data[5].isEmpty()) 
            b.setMonthlyAmount(Double.parseDouble(data[5]));
        b.setPaidAmount(Double.parseDouble(data[6]));
        b.setRfcode(data[1]);
        b.setPaid(Boolean.parseBoolean(data[9]));
        b.setStatus(Bill.BillStatus.valueOf(data[11]));
        b.setPaidInstallments(Integer.parseInt(data[13]));
        b.setIssuerUsername(data[2]);
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

            if (bill.getStatus() == Bill.BillStatus.MONTHLY_PAID &&
                bill.getPayDate().getMonth()!=now.getMonth()) {

                bill.setStatus(Bill.BillStatus.ACTIVE);
                changed = true;
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
}
