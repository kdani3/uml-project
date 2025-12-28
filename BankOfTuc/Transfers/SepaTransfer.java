package BankOfTuc.Transfers;

import java.util.List;

import BankOfTuc.Customer;
import BankOfTuc.TimeService;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Bookkeeping.CustomerFileManager;
import BankOfTuc.Logging.TransferLogger;

/**
 * Abstraction for SEPA transfers. Uses a TransferGateway (bridge) to perform the actual transfer.
 */
public class SepaTransfer extends Transfer {
    public static final double FEE = 2.50;
    private final TransferGateway gateway;

    /**
     * Default constructor — uses the real SepaTransferGateway backed by SepaTransferService.
     */
    public SepaTransfer() {
        this.gateway = new SepaTransferGateway(new BankOfTuc.SepaTransferService());
    }

    /**
     * Constructor for dependency injection / testing.
     */
    public SepaTransfer(TransferGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public int sendMoney(Customer sendingCustomer, int sendingCustomerBankIndex, String BIC, String receiverIBAN, String receiverName, CustomerFileManager cfm, double amount, String details, int feeChoice) {
        double withFees;
        List<BankAccount> accounts = sendingCustomer.getBankAccounts();

        // Prevent transfer to a sending customer's own IBAN
        for (BankAccount bankAccount : accounts) {
            if (bankAccount.getIban().equals(receiverIBAN)) {
                return 0; // cannot transfer to own account
            }
        }

        BankAccount account = accounts.get(sendingCustomerBankIndex);

        switch (feeChoice) {
            case 1 -> withFees = amount + FEE;
            case 2 -> withFees = amount + FEE / 2;
            default -> withFees = amount + FEE;
        }

        if (account.getBalance() < withFees) {
            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                account.getIban(),
                "SEPA",
                "Not Provided",
                BIC,
                receiverName,
                receiverIBAN,
                amount,
                false,
                account.getBalance(),
                0,
                "The amount exceeds the account's balance"
            );
            return -1;
        }

        boolean apiResponse = gateway.sendTransfer(
            amount,
            receiverName,
            receiverIBAN,
            BIC,
            TimeService.getInstance().today().toString(),
            (feeChoice == 1) ? "OUR" : "SHA"
        );

        if (apiResponse) {
            account.reduceBalance(withFees);
            cfm.updateCustomer(sendingCustomer);

            TransferLogger.logTransfer(
                sendingCustomer.getVatID(),
                account.getIban(),
                "SEPA",
                "Not Provided",
                BIC,
                receiverName,
                receiverIBAN,
                amount,
                true,
                account.getBalance(),
                amount,
                details
            );
            return 1;
        }

        TransferLogger.logTransfer(
            sendingCustomer.getVatID(),
            account.getIban(),
            "SEPA",
            "Not Provided",
            BIC,
            receiverName,
            receiverIBAN,
            amount,
            false,
            account.getBalance(),
            0,
            "The request got rejected"
        );
        return -2;
    }
}
