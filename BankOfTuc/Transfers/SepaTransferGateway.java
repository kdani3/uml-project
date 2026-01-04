package BankOfTuc.Transfers;

import BankOfTuc.Services.SepaTransferService;

/**
 * Concrete implementor that delegates SEPA transfer requests to SepaTransferService.
 */
public class SepaTransferGateway implements TransferGateway {
    private final SepaTransferService service;

    public SepaTransferGateway(SepaTransferService service) {
        this.service = service;
    }

    @Override
    public boolean sendTransfer(double amount, String creditorName, String creditorIban, String creditorBic, String requestedDate, String charges) {
        return service.sendSepaTransferRequest(amount, creditorName, creditorIban, creditorBic, requestedDate, charges);
    }
}
