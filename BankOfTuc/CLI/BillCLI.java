package BankOfTuc.CLI;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Payments.Bill;
import BankOfTuc.Payments.Bill.BillStatus;
import BankOfTuc.Services.TimeService;
import BankOfTuc.Payments.BillFileStore;

public class BillCLI {
static void ManageBills(CompanyCustomer comp,Scanner sc) throws IOException{
        List<Bill> bills = BillFileStore.getCompanyBills(comp.getVatID());
        //System.out.println(BillFileStore.getCompanyBillsNum(null, null));

        System.out.println("\n--- Bill Menu (" + comp.getUsername() + "/" + comp.getRole() + ") ---");
        System.out.println("1. Create Bill");
        System.out.println("2. Delete Bill");
        System.out.println("3. Change Bill");
        System.out.println("4. Return\n");
        printBills(bills);
        System.out.println("\n");
        System.out.print(">");

        String choice = sc.nextLine();

        switch (choice){
            case "1":
                issueBillMenu(comp, sc);
                break;
            case "2":
                deleteBillMenu(comp,sc);
                break;
            case "3":
                changeBillMenu(comp, sc);
                break;
            default:
                System.out.println("Invalid choice");
                return;
        }
        return;
    }

    static void printBills(List<Bill> bills){
        int i =0;
        System.out.println("Bill ID\t  RF code \t\t   Status  Issue Date   Due Date   Amount    Paid Amount");
        for(Bill bill:bills){
            i++;
            System.out.println(i+". "+bill.getBillid()+" "+bill.getRfcode()+" "+bill.getStatus()+"  "+bill.getIssueDate()+"  "+bill.getDueDate()+"   "+bill.getAmount()+"\t"+bill.getPaidAmount());
        }
        return;
    }

    public static void deleteBillMenu(CompanyCustomer comp,Scanner sc) throws IOException{
        System.out.println("\nEnter BillID for bill to use");
        System.out.println(">");

        String billid = sc.nextLine();
        List<Bill> bills = BillFileStore.loadBills();
        Bill bill = BillFileStore.getBillbyID(billid,bills);
        
        if(bill==null){
            System.out.println("Can't find bill id");
            return;
        }
        if(BillFileStore.deleteBill(bill, bills)){
            System.out.println("Bill deleted");
        }
        else{
            System.out.println("Bill has not been deleted");
        }
        return;
    }
    public static void changeBillMenu(CompanyCustomer comp,Scanner sc) throws IOException{
        System.out.println("\nEnter BillID for bill to use");
        System.out.print(">");

        String billid = sc.nextLine();
        List<Bill> bills = BillFileStore.loadBills();
        Bill bill = BillFileStore.getBillbyID(billid,bills);

        if(bill==null){
            System.out.println("Can't find bill id");
            return;
        }
        while(true){
            System.out.println("1.Change Due Date");
            System.out.println("2.Change Amount");
            System.out.println("3.Change Status");
            System.out.println("4.Return");
            System.out.println(">");

            String choice = sc.nextLine();
            switch(choice){
                case "1":
                    System.out.print("Enter new due date (dd-MM-yyyy): ");
                    System.out.println(">");


                    LocalDate date = LocalDate.parse(sc.nextLine(),inputFormatter);

                    if(date.isBefore(now)){
                        System.out.println("Invalid Date.");
                        break;
                    }

                    bill.setDueDate(date);
                    BillFileStore.updateBill(bill, bills);
                    break;
                case "2":
                    System.out.println("Enter new amount: ");
                    System.out.println(">");
                    String amountString = sc.nextLine();
                    double amount;

                    try {
                        amount = Double.parseDouble(amountString);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a numeric value.");
                        return ;
                    }

                    if(amount<=0){
                        System.out.println("Invalid Amount");
                        return;
                    }

                    bill.setAmount(amount);
                    BillFileStore.updateBill(bill, bills);
                    break;
                case "3":
                    System.out.println("Enter new status:");
                    System.out.println(">");

                    System.out.println("1.ACTIVE");
                    System.out.println("2.EXPIRED");
                    System.out.println("3.FROZEN");

                    String statusChoice = sc.nextLine();
                    
                    switch(statusChoice){
                        case "1":
                            if(bill.getStatus().equals(BillStatus.EXPIRED)){
                                System.out.println("Your bill has expired, Please enter a new Due Date");

                                System.out.print("Enter new due date (dd-MM-yyyy): ");
                                System.out.println(">");
                                
                                LocalDate newdate = LocalDate.parse(sc.nextLine(),inputFormatter);

                                if(newdate.isBefore(now)){
                                    System.out.println("Invalid Date.");
                                    break;
                                }
                                bill.setDueDate(newdate);
                                bill.setStatus(BillStatus.ACTIVE);
                                BillFileStore.updateBill(bill, bills);
                            }
                            break;
                        case "2":
                            bill.setStatus(BillStatus.EXPIRED);
                            BillFileStore.updateBill(bill, bills);
                            break;
                        case "3":
                            bill.setStatus(BillStatus.FROZEN);
                            BillFileStore.updateBill(bill, bills);
                            break;
                        default:
                            return;
                    }
                default:
                    return;
            }
        }

    }
    
    static DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    static LocalDate now = TimeService.getInstance().today();

    public static void issueBillMenu(CompanyCustomer comp,Scanner sc){
        if(comp.getBankAccounts().isEmpty()){
            System.out.println("You have no bank accounts.\nCannot use primary account for payments");
            return;
        }
        System.out.println("Your primary account will be used when customers pay");
        System.out.println("Enter Desired amount for Bill");
        System.out.print(">");
        String amountString = sc.nextLine();
        double amount;

        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric value.");
            return ;
        }

        if(amount<=0){
            System.out.println("Invalid Amount");
            return;
        }
        System.out.print("Enter due date (dd-MM-yyyy): ");

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate now = TimeService.getInstance().today();

        LocalDate date = LocalDate.parse(sc.nextLine(),inputFormatter);

        if(date.isBefore(now)){
            System.out.println("Invalid Date.");
            return;
        }

        System.out.println("Select Bill Type");
        System.out.println("1.Multi Payment Bill(Installments)\n2.One Time Payment Bill");
        System.out.print(">");
        String billchoice = sc.nextLine();
        
        System.out.println("Enter Payee VAT ID");
        System.out.print(">");
        String payerID = sc.nextLine();
        switch(billchoice){
            case "1":
                int inst = Math.abs((int)ChronoUnit.MONTHS.between(now.withDayOfMonth(1), date.withDayOfMonth(1)));
                System.out.println("Calculated "+inst +" installments based on expiration date.\nWould you like different installments?(Y)es or (N)o");

                String instChoice = sc.nextLine();
                if(instChoice.equalsIgnoreCase("y")){
                    System.out.println("Enter installments:");
                    System.out.println(">");

                    String installments = sc.nextLine();
                    inst = Integer.parseInt(installments);
                    if(inst<=0){
                        System.out.println("Invalid installments number");
                        return;
                    }
                }
                Double monthlyAmount = amount/inst;
                monthlyAmount = BigDecimal.valueOf(monthlyAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
                System.out.println("Calculated Monthly Payment: "+monthlyAmount);
                comp.issueBill(amount,monthlyAmount, date, inst,payerID);
                
                System.out.println("Bill Created");
                break;
            case "2":
                comp.issueBill(amount, date,1,payerID);
                System.out.println("Bill Created");
                break;
            default:
                System.out.println("Invalid Option");
                break;
        }
        return;
    }
}
