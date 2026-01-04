package BankOfTuc.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SwiftTransferService {

    private static final String API_URL = "http://147.27.70.44:3020/transfer/swift";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean sendSwiftTransferRequest(
            double amount,
            String creditorName,
            String creditorIban,
            String creditorSwiftCode,
            String requestedDate,
            String charges) {
        
        String jsonPayload = buildJsonPayload(amount, creditorName, creditorIban, creditorSwiftCode, requestedDate, charges);

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
            return "Exception occurred: " + e.getMessage() != null;
        }
    }

    private String buildJsonPayload(
            double amount,
            String creditorName,
            String creditorIban,
            String creditorSwiftCode,
            String requestedDate,
            String charges) {

        return String.format("""
            {
              "currency": "EUR",
              "amount": %.1f,
              "beneficiary": {
                "name": "%s",
                "account": "%s"
              },
              "beneficiaryBank": {
                "swiftCode": "%s"
              },
              "fees": {
                "chargingModel": "%s"
              },
              "correspondentBank": {
                "required": false
                }
            }
            """, amount, creditorName, creditorIban, creditorSwiftCode, requestedDate, charges);
    }
}
