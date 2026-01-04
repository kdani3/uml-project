package BankOfTuc.Transfers;

public interface TransferProcessor {
    boolean process(double amount, String recipientIban, String recipientName, String bicOrSwift);
}