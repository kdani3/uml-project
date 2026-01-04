// File: BankOfTuc.Pdf.TransactionPdfGenerator.java
package BankOfTuc.FileIO;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;

import BankOfTuc.Customer;
import BankOfTuc.Logging.TransactionHistoryService;

public class TransactionPdfGenerator {
    //tuc logo
    private static final String LOGO_BASE64  =
        "data:image/png;base64,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Ms8XsDo3jliLlCm0dpl98CpW6xBskQLSMqVIZRzju4Ac5XpVDCC2gA7grqx2HHM7bdQkBTGR4tKVMZHK0H6xFCLjEF0XGxjn7KY4rr9svq18zHctItI3hTGRytB+sRQs6dEmpfmTs/jiudb6Z9+FiEhpFMZE8hcBk3wXkaNlvgsQAO5yxtVzafml70JEZGAUxkTydygwyncROXkceN53ETWs3Zy7oS62a6+k5QHfxYhIeRTGRPKnJcq0jNvMMdbBwb4nXBAPYfy4nsHXf8WWr/ddjIiko6PeRPL3TeA830XkZDYwv1IvNoeJ44z4PTg7DXgzMNj3G1BBrc5xs4ujBVeyapXvYkQkOwpjIvm7HzjSdxE5mQos9vHCjUwato32t1pkJ2KcCBxFWFf724B7zWwR2C3zaW32XZCI5ENhTCRfQ4GNwCDfheQgBvYANvkuBOAi3jx0MJsmGfam2LljHEwBDvBd1wBscnA3ZksM1zQc/tJIyw7fRYlI/hTGRPJ1LLDEdxE5aQEm+C6iL+dz8KhhDJnQRXwYkZvgjDcYHOSSkOYlIBtscdCKo5WYVsNaY+LWUTy4plHHFInUpJAu6YsUkZq9enQdazYB9wD37BpzZjCj7vWs2r+T+rEO28dgHyJeA7wmMvaMYbRL7oAdDQwn2Zs2iORrZj3QSbKM2N790Qa0O2gHtznGnsfxnItZZ7AugnUxti6i/pl5rHwaAHWxFRERqYifknzbDfHj077fXBGREOhsSpF8hdzWovBXxkREqoH2jInkZy9gne8icrKDZBmv3XchIiLVTlfGRPIT8lWxB1AQExHJhMKYSH5C3rxfmc77IiI1QGFMJD8hhzHtFxMRyYjCmEg+HEnT0VApjImIZERhTCQfbyDpTh+iLcBDvosQEQmFwphIPkJeolwKdPkuQkQkFApjIvkIOYxpiVJEJEMKYyL5CLmthcKYiEiGdDalSPbqgRHAoyQHP28sY4w2YHsZz9vEwJcQu7qfV6q7M3iPRERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERET697/ARbyk9/OgpgAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAyNS0xMi0wNVQxNToyMjo1NyswMDowMH1heuAAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMjUtMTItMDVUMTU6MjI6NTcrMDA6MDAMPMJcAAAAKHRFWHRkYXRlOnRpbWVzdGFtcAAyMDI1LTEyLTA1VDE1OjIzOjI1KzAwOjAwKbGQjQAAAABJRU5ErkJggg==";
    public static String generateTransactionPdf(TransactionHistoryService.TransactionEntry entry, Customer customer, String outputPath, boolean emailFlag) throws Exception {
        String orderingCustomer;
        String orderingAccount;
        String recipientCustomer;
        String recipientIban;
        String amountDisplay = entry.getAmountDisplay() + " €";
        String bicLabel = "";
        String bicValue = "";
        String detailsLabel = "";
        String detailsValue = "";
    
        if (entry.isOutgoing) {
            orderingCustomer = customer.getFullname();
            orderingAccount = entry.senderIban;
            recipientCustomer = entry.counterpartyName;
            recipientIban = entry.counterpartyIban;
        } else {
            orderingCustomer = entry.counterpartyName;
            orderingAccount = entry.senderIban;
            recipientCustomer = customer.getFullname();
            recipientIban = entry.receiverIban;
        }
    
        // Check for SEPA or SWIFT transaction types and adjust accordingly
        if (entry.type.equals("SEPA")) {
            bicLabel = "BIC";
            bicValue = entry.bankCode != null && !entry.bankCode.isEmpty() ? entry.bankCode : "Δεν παρέχεται";
            if (entry.details != null && !entry.details.isEmpty() &&  !entry.details.equals("no message")) {
                detailsLabel = "Λεπτομέρειες:";
                detailsValue = entry.details;
            }
        } else if (entry.type.equals("SWIFT")) {
            bicLabel = "SWIFT CODE";
            bicValue = entry.bankCode != null && !entry.bankCode.isEmpty() ? entry.bankCode : "Δεν παρέχεται";
            if (entry.details != null && !entry.details.isEmpty() && !entry.details.equals("no message")) {
                detailsLabel = "Λεπτομέρειες:";
                detailsValue = entry.details;
            }
        }
    
        String html = """
            <!DOCTYPE html>
            <html lang="el">
            <head>
                <meta charset="UTF-8" />
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: "DejaVu Sans", Arial, Helvetica, sans-serif;
                        background: #ffffff;
                        color: #333;
                        line-height: 1.6;
                        padding: 20px;
                        font-size: 12px;
                    }
                    @page { size: A4; margin: 15mm; }
                    @media print { body { padding: 0; } }
                    .container {
                        max-width: 700px;
                        margin: 0 auto;
                        background: white;
                        border: 1px solid #e0e0e0;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0,0,0,0.08);
                    }
                    .header {
                        display: flex;
                        align-items: center;
                        padding: 28px 32px;
                        background: white;
                        border-bottom: 1px solid #eee;
                    }
                    .logo {
                        height: 72px;
                        margin-right: 24px;
                    }
                    .bank-info h1 {
                        font-size: 22px;
                        font-weight: 700;
                        color: #ad013e;
                        margin-bottom: 4px;
                    }
                    .bank-info p {
                        font-size: 13px;
                        color: #666;
                    }
                    .doc-header {
                        text-align: center;
                        padding: 20px 32px;
                        background: #f9f9f9;
                        border-bottom: 1px solid #eee;
                    }
                    .doc-date {
                        color: #ad013e;
                        font-weight: 600;
                        font-size: 14px;
                        margin-top: 4px;
                    }
                    .details { padding: 0 32px 24px; }
                    .detail-row {
                        display: flex;
                        margin-bottom: 16px;
                        padding-bottom: 16px;
                        border-bottom: 1px dashed #eee;
                    }
                    .detail-label {
                        width: 200px;
                        font-weight: 600;
                        color: #555;
                        font-size: 12px;
                    }
                    .detail-value {
                        flex: 1;
                        font-size: 13px;
                    }
                    .iban {
                        font-family: monospace;
                        background: #f8f8f8;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-size: 12px;
                    }
                    .amount {
                        font-weight: 700;
                        font-size: 14px;
                    }
                    .amount.out { color: #d32f2f; }
                    .amount.in { color: #2e7d32; }
                    .footer {
                        padding: 20px 32px;
                        background: #f9f9f9;
                        border-top: 1px solid #eee;
                        font-size: 10px;
                        color: #777;
                        text-align: center;
                    }
                    .footer p { margin: 4px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="%s" alt="TUC Bank" class="logo" />
                        <div class="bank-info">
                            <p>Απόδειξη Τραπεζικής Συναλλαγής</p>
                        </div>
                    </div>
    
                    <div class="doc-header">
                        <h2>Λεπτομέρειες Συναλλαγής</h2>
                        <div class="doc-date">%s</div>
                    </div>
    
                    <div class="details">
                        <div class="detail-row">
                            <div class="detail-label">Τύπος Συναλλαγής:</div>
                            <div class="detail-value">%s</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Εντολέας:</div>
                            <div class="detail-value">%s</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Λογαριασμός Εντολέα:</div>
                            <div class="detail-value"><span class="iban">%s</span></div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Παραλήπτης:</div>
                            <div class="detail-value">%s</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">%s:</div>
                            <div class="detail-value"><span class="iban">%s</span></div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Ποσό Συναλλαγής:</div>
                            <div class="detail-value">
                                <span class="%s">%s</span>
                            </div>
                        </div>
                        <!-- Add BIC/SWIFT Code if applicable -->
                        %s
                        <!-- Add details if available -->
                        %s
                    </div>
    
                    <div class="footer">
                        <p>© 2025 TUC Bank. Όλα τα δικαιώματα διατηρούνται.</p>
                        <p>Αυτή η απόδειξη δημιουργήθηκε αυτόματα και είναι έγκυρη χωρίς υπογραφή.</p>
                        <p>Η ηλεκτρονική έκδοση έχει την ίδια ισχύ με την έντυπη.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
            LOGO_BASE64,              //logo 
            entry.datetime,           //date of the transaction
            entry.type,               //transaction type (SEPA, SWIFT, etc.)
            orderingCustomer,         //ordering customer name
            orderingAccount,          //ordering account (IBAN)
            recipientCustomer,        //recipient customer name
            (entry.type.equals("PAYMENT") ? "RF code" : "IBAN Παραλήπτη"), //IBAN or RF code label
            (entry.type.equals("PAYMENT") ? entry.rfCode : entry.counterpartyIban), //IBAN or RF code value
            entry.isOutgoing ? "amount out" : "amount in", // Transaction type: out or in
            amountDisplay,             // Transaction amount
            bicLabel.isEmpty() ? "" : """
                <div class="detail-row">
                    <div class="detail-label">%s</div>
                    <div class="detail-value">%s</div>
                </div>
            """.formatted(bicLabel, bicValue), //BIC/SWIFT Code row (only for SEPA or SWIFT)
            detailsLabel.isEmpty() ? "" : """
                <div class="detail-row">
                    <div class="detail-label">%s</div>
                    <div class="detail-value">%s</div>
                </div>
            """.formatted(detailsLabel, detailsValue) 
        );

        if(emailFlag){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                try {
                    ITextRenderer renderer = new ITextRenderer();
                    ITextFontResolver fontResolver = renderer.getFontResolver();

                    String fontPath = "resources/DejaVuSans.ttf";
                    fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    renderer.setDocumentFromString(html);
                    renderer.layout();
                    renderer.createPDF(baos); //write to memory
                } finally {
                    baos.close();
                }

                //base64
                byte[] pdfBytes = baos.toByteArray();
                return Base64.getEncoder().encodeToString(pdfBytes);

        }

        try (FileOutputStream os = new FileOutputStream(outputPath)) {
            ITextRenderer renderer = new ITextRenderer();
            ITextFontResolver fontResolver = renderer.getFontResolver();

            String fontPath = "resources/DejaVuSans.ttf"; 
            fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }
        return null;
    }
}