package BankOfTuc.Transfers;

/**
 * Transfer gateway (Implementor in Bridge pattern).
 * Provides a thin abstraction over external transfer providers (SEPA, SWIFT, etc.).
 */
public interface TransferGateway {
    boolean sendTransfer(double amount, String creditorName, String creditorIban, String creditorBic, String requestedDate, String charges);
}
