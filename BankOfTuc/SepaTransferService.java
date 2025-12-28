package BankOfTuc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SepaTransferService {

    private static final String API_URL = "http://147.27.70.44:3020/transfer/sepa";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean sendSepaTransferRequest(
            double amount,
            String creditorName,
            String creditorIban,
            String creditorBic,
            String requestedDate,
            String charges) {
        
        String jsonPayload = buildJsonPayload(amount, creditorName, creditorIban, creditorBic, requestedDate, charges);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Request failed: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private String buildJsonPayload(
            double amount,
            String creditorName,
            String creditorIban,
            String creditorBic,
            String requestedDate,
            String charges) {

        return String.format("""
            {
              "amount": %.1f,
              "creditor": {
                "name": "%s",
                "iban": "%s"
              },
              "creditorBank": {
                "bic": "%s"
              },
              "execution": {
                "requestedDate": "%s",
                "charges": "%s"
              }
            }
            """, amount, creditorName, creditorIban, creditorBic, requestedDate, charges);
    }
}