package BankOfTuc.Auth;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;



public class QrUtils {
    
    public static String[] createQr(String username) throws QrGenerationException{
            String secret = generateQrSecret();
            QrData data =  generateQrCode( secret, username);
            byte[] bytes = generateQrImage(data);

            String dataUri = getDataUriForImage(bytes, "image/png");            
        return new String[]{dataUri,secret};
    }
/*******************************************************************************
* Function: generateQrSecret
*
* Description:
*   Function generateQrSecret description.
*
* Parameters:
*   none
*
* Returns:
*   void
*******************************************************************************/

    
    public static String generateQrSecret(){
        

        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        
        return secretGenerator.generate();
    
        
}

    
    public static QrData generateQrCode(String secret,String username){
        

        QrData data =  new QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer("Bank Of Tuc")
            .algorithm(HashingAlgorithm.SHA1) 
            .digits(6)
            .period(30)
            .build();
        
        return data;
    
        
}

    
    public static byte[] generateQrImage(QrData data) throws QrGenerationException{
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data); 
        return imageData;
    }

    public static boolean verifyQrCode(String secret, String code){
        // Trim and remove whitespace from the code input
        String trimmedCode = code.trim().replaceAll("\\s+", "");
        
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6); 
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        verifier.setTimePeriod(30);
        
        // Check current time and ±1 windows (±30 seconds) for clock skew tolerance
        boolean successful = verifier.isValidCode(secret, trimmedCode);
        
        return successful;
    }
}