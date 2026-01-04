package BankOfTuc.Transfers;

import BankOfTuc.Services.SwiftTransferService;
import BankOfTuc.Services.TimeService;

public class SwiftProcessor implements TransferProcessor {
    @Override
    public boolean process(double amount, String recipientIban, String recipientName, String swiftCode) {
        SwiftTransferService swift = new SwiftTransferService();
        String date = TimeService.getInstance().today().toString();

        //System.out.println("[BRIDGE] Using SWIFT Implementation...");
        return swift.sendSwiftTransferRequest(amount, recipientName, recipientIban, swiftCode, date, "SHARED");
    }
}