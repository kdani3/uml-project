package BankOfTuc.Transfers;

import BankOfTuc.Services.SepaTransferService;
import BankOfTuc.Services.TimeService;

public class SepaProcessor implements TransferProcessor {
    @Override
    public boolean process(double amount, String recipientIban, String recipientName, String bic) {
        SepaTransferService sepa = new SepaTransferService();
        String date = TimeService.getInstance().today().toString();
   
        //System.out.println("[BRIDGE] Using SEPA Implementation...");
        return sepa.sendSepaTransferRequest(amount, recipientName, recipientIban, bic, date, "SHARED");
    }
}