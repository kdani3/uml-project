package BankOfTuc.Auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    //d parameters
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; //bits

    //generate a random salt
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; //16 bytes = 128 bits
        random.nextBytes(salt);
        return salt;
    }

    //salt hashing
    public static String hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password: " + e.getMessage(), e);
        }
    }

    //verify
    public static boolean verifyPassword(char[] password, byte[] salt, String expectedHash) {
        String hash = hashPassword(password, salt);
        return hash.equals(expectedHash);
    }
}