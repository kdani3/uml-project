package BankOfTuc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class EmailUtils {

 public static void sendEmail(String apiKey, String fromEmail, String fromName,
                                 String toEmail, String subject, String content) throws Exception {

        // Create JSON payload using Gson
        JsonObject sender = new JsonObject();
        sender.addProperty("name", fromName);
        sender.addProperty("email", fromEmail);

        JsonObject to = new JsonObject();
        to.addProperty("email", toEmail);

        JsonArray toArray = new JsonArray();
        toArray.add(to);

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.add("sender", sender);
        jsonPayload.add("to", toArray);
        jsonPayload.addProperty("subject", subject);
        jsonPayload.addProperty("htmlContent", content);

        Gson gson = new Gson();
        String json = gson.toJson(jsonPayload);

        // Create HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Create HttpRequest using URI
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Email Response Code: " + response.statusCode());
        System.out.println("Email Response: " + response.body());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("Failed to send email: " + response.body());
        } else {
            System.out.println("Email sent successfully! Check your inbox (and spam folder).");
        }
    }

public static void sendEmailWithPdfAttachment(
        String apiKey,
        String fromEmail,
        String fromName,
        String toEmail,
        String subject,
        String htmlContent,
        String pdfBase64,
        String attachmentFileName) throws Exception {


    //sender
    JsonObject sender = new JsonObject();
    sender.addProperty("name", fromName);
    sender.addProperty("email", fromEmail);

    //recipient
    JsonObject to = new JsonObject();
    to.addProperty("email", toEmail);
    JsonArray toArray = new JsonArray();
    toArray.add(to);

    //attachment
    JsonObject attachment = new JsonObject();
    attachment.addProperty("name", attachmentFileName);      // e.g., "statement.pdf"
    attachment.addProperty("content", pdfBase64);

    JsonArray attachmentsArray = new JsonArray();
    attachmentsArray.add(attachment);

    //payload
    JsonObject jsonPayload = new JsonObject();
    jsonPayload.add("sender", sender);
    jsonPayload.add("to", toArray);
    jsonPayload.addProperty("subject", subject);
    jsonPayload.addProperty("htmlContent", htmlContent);
    jsonPayload.add("attachment", attachmentsArray); // 👈 critical: array of attachments

    //serialize to json
    String json = new Gson().toJson(jsonPayload);

    //http request
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .header("api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200 && response.statusCode() != 201) {
        throw new IOException("Failed to send email with attachment: " + response.body());
    } 
}


    public static String passwordResetHTML(String username, int mailCode){
        String html = """
            <!DOCTYPE html>
            <html lang="el">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Επαναφορά Κωδικού Πρόσβασης - TUC Bank</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: white;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #ad013e;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 30px;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                    }
                    .code-box {
                        display: inline-block;
                        background-color: #ad013e;
                        color: white;
                        padding: 12px 24px;
                        border-radius: 4px;
                        margin: 20px 0;
                        font-weight: bold;
                        font-size: 24px;
                        letter-spacing: 3px;
                    }
                    .divider {
                        height: 1px;
                        background-color: #eee;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>TUC Bank</h1>
                        <p>Τραπεζικές υπηρεσίες απλές και ασφαλείς</p>
                    </div>
                    
                    <div class="content">
                        <h2>Επαναφορά Κωδικού Πρόσβασης</h2>
                        <p>Λάβαμε ένα αίτημα για επαναφορά του κωδικού πρόσβασης για τον λογαριασμό <strong>{username}</strong>.</p>
                        
                        <p>Για να δημιουργήσετε νέο κωδικό πρόσβασης, χρησιμοποιήστε τον παρακάτω κωδικό επαλήθευσης:</p>
                        
                        <div style="text-align: center;">
                            <div class="code-box">{code}</div>
                        </div>
                        
                        <p>Εισαγάγετε αυτόν τον κωδικό στην εφαρμογή για να συνεχίσετε με την επαναφορά του κωδικού πρόσβασής σας.</p>
                        
                        <p>Αν δεν ζητήσατε εσείς αυτή την αλλαγή, μπορείτε να αγνοήσετε αυτό το μήνυμα. Ο κωδικός πρόσβασής σας θα παραμείνει αμετάβλητος.</p>
                        
                        <div class="divider"></div>
                        
                        <p><strong>Σημαντική Παρατήρηση:</strong></p>
                        <ul>
                            <li>Παρακαλούμε μην μοιραστείτε αυτό το email με κανέναν.</li>
                            <li>Αν αντιμετωπίζετε προβλήματα, επικοινωνήστε με την εξυπηρέτηση πελατών.</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 TUC Bank. Όλα τα δικαιώματα διατηρούνται.</p>
                        <p>Αυτό το μήνυμα στάλθηκε αυτόματα. Παρακαλούμε μην απαντήσετε σε αυτό το email.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        
        html = html.replace("{username}", username)
                   .replace("{code}", Integer.toString(mailCode));
        return html;
    }

    public static String statementEmailHTML(String customerName) {
        return """
            <!DOCTYPE html>
            <html lang="el">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Αίτημα Αναλυτικής Κίνησης - TUC Bank</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: white;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #ad013e;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 30px;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                    }
                    .divider {
                        height: 1px;
                        background-color: #eee;
                        margin: 24px 0;
                    }
                    .note {
                        background-color: #fff8e1;
                        border-left: 4px solid #ffc107;
                        padding: 12px 16px;
                        margin: 20px 0;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>TUC Bank</h1>
                        <p>Τραπεζικές υπηρεσίες απλές και ασφαλείς</p>
                    </div>
                    
                    <div class="content">
                        <h2>Επιβεβαίωση Αιτήματος Αναλυτικής Κίνησης</h2>
                        <p>Αγαπητέ/ή <strong>%s</strong>,</p>
                        
                        <p>Επιβεβαιώνουμε ότι λάβαμε το αίτημά σας για αναλυτική κίνηση λογαριασμού.</p>
                        
                        <p>Η αναλυτική σας έκθεση επισυνάπτεται σε αυτό το email ως αρχείο PDF.</p>
                        
                        <div class="note">
                            <strong>Σημαντικό:</strong> Το έγγραφο περιέχει ευαίσθητες τραπεζικές πληροφορίες.  
                            Παρακαλούμε να το διατηρήσετε ασφαλές και να μην το διαμοιράσετε.
                        </div>
                        
                        <div class="divider"></div>
                        
                        <p>Ευχαριστούμε που επιλέξατε την TUC Bank για τις τραπεζικές σας ανάγκες.</p>
                        
                        <p>Με εκτίμηση,<br>
                        <strong>Η Ομάδα Εξυπηρέτησης Πελατών<br>
                        TUC Bank</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 TUC Bank. Όλα τα δικαιώματα διατηρούνται.</p>
                        <p>Αυτό το μήνυμα στάλθηκε αυτόματα. Παρακαλούμε μην απαντήσετε σε αυτό το email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName);
    }

    public static String recurringPaymentFailedHTML(String customerName,String rfCode,String attemptDateTime,double amount) {

        return """
            <!DOCTYPE html>
            <html lang="el">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Αποτυχία Επαναλαμβανόμενης Πληρωμής - TUC Bank</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: white;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #d32f2f; /* Red for alert */
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 30px;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                    }
                    .divider {
                        height: 1px;
                        background-color: #eee;
                        margin: 24px 0;
                    }
                    .alert-box {
                        background-color: #ffebee;
                        border-left: 4px solid #f44336;
                        padding: 16px;
                        margin: 20px 0;
                        border-radius: 4px;
                        font-size: 14px;
                    }
                    .details-table {
                        width: 100%;
                        margin: 20px 0;
                        border-collapse: collapse;
                    }
                    .details-table td {
                        padding: 8px 0;
                        vertical-align: top;
                    }
                    .details-table td:first-child {
                        font-weight: 600;
                        color: #333;
                        width: 40%;
                    }
                    .details-table td:last-child {
                        color: #000;
                        width: 60%;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>TUC Bank</h1>
                        <p>Ειδοποίηση Αποτυχίας Πληρωμής</p>
                    </div>
                    
                    <div class="content">
                        <h2>Η πάγια πληρωμή σας απέτυχε</h2>
                        <p>Αγαπητέ/ή <strong>%s</strong>,</p>
                        
                        <p>Κατά την προσπάθεια αυτόματης εκτέλεσης της επαναλαμβανόμενης πληρωμής σας, παρουσιάστηκε σφάλμα.</p>
                        
                        <div class="alert-box">
                            <strong>Παρακαλούμε ενεργήστε άμεσα:</strong>
                            <ul style="margin-top: 8px; margin-bottom: 0; padding-left: 20px;">
                                <li>Ελέγξτε το διαθέσιμο υπόλοιπο του λογαριασμού σας</li>
                                <li>Βεβαιωθείτε ότι η πληρωμή δεν έχει ακυρωθεί</li>
                                <li>Επισκεφθείτε την εφαρμογή TUC Bank για λεπτομέρειες</li>
                            </ul>
                        </div>

                        <h3>Λεπτομέρειες Προσπάθειας Πληρωμής</h3>
                        <table class="details-table">
                            <tr>
                                <td>Κωδικός Τιμολογίου (RF):</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Ημερομηνία & Ώρα Προσπάθειας:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Προσπαθούμενο Ποσό:</td>
                                <td>%.2f €</td>
                            </tr>
                        </table>

                        <p>Η πληρωμή θα επαναπροσπαθηθεί αυτόματα σύμφωνα με τις ρυθμίσεις σας.</p>
                        <p>Για άμεση βοήθεια, επικοινωνήστε με την Εξυπηρέτηση Πελατών.</p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 TUC Bank. Όλα τα δικαιώματα διατηρούνται.</p>
                        <p>Αυτό το μήνυμα στάλθηκε αυτόματα. Παρακαλούμε μην απαντήσετε σε αυτό το email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(customerName, rfCode, attemptDateTime, amount);
    }
}
