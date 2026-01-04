package BankOfTuc.CLI;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import java.util.Scanner;

public class PayBillCommand implements Command {
    private final Customer customer;
    private final CustomerFileManager cfm;

    public PayBillCommand(Customer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;
    }

    @Override
    public void execute(Scanner sc) {
        PaymentCLI.managePayments(sc, customer, cfm);
    }

    @Override
    public String getDescription() {
        return "Payments";
    }
}