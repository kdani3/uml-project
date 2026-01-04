package BankOfTuc.CLI;

import BankOfTuc.Customer;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransactionHistoryService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ViewHistoryCommand implements Command {
    private final Customer customer;
    private final CustomerFileManager cfm;

    public ViewHistoryCommand(Customer customer, CustomerFileManager cfm) {
        this.customer = customer;
        this.cfm = cfm;
    }

    @Override
    public void execute(Scanner sc) {

        List<TransactionHistoryService.TransactionEntry> history = new ArrayList<>();
        try {
            history = TransactionHistoryService.getHistoryForCustomer(customer.getVatID(), cfm);
        } catch (IOException e) {
            System.out.println("Error loading history: " + e.getMessage());
            return;
        }

        HistoryCLI.showTransactionHistory(sc, customer, history, cfm);
    }

    @Override
    public String getDescription() {
        return "History";
    }
}