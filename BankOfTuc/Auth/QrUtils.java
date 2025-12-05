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

//the whole QrUtils Class is based on the Totp library by SamStevens

public class QrUtils {
    //the whole process for new Qrcreation
    public static String[] createQr(String username) throws QrGenerationException{
            String secret = generateQrSecret();
            QrData data =  generateQrCode( secret, username);
            byte[] bytes = generateQrImage(data);

            String dataUri = getDataUriForImage(bytes, "image/png");            //get the qr image uri from image bytes
        return new String[]{dataUri,secret};
    }
    //generate only the Secret
    public static String generateQrSecret(){
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        return secretGenerator.generate();
    }
    //generate qr data
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
    //qr image bytes
    public static byte[] generateQrImage(QrData data) throws QrGenerationException{
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data); 
        return imageData;
    }

    public static boolean verifyQrCode(String secret, String code){
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6); //6 digits
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        verifier.setTimePeriod(30);//we set the qr t = 30
        //secret = the shared secret for the user
        //code = the code submitted by the user
        boolean successful = verifier.isValidCode(secret, code);
        return successful;
    }
}
