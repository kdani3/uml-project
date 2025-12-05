package BankOfTuc.Payments;

import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BillUtils {

    public static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Random rnd = new Random();

    public static String generateRFNumeric() {

        int length = 21;
        String reference = randomDigits(length);

        String rearranged = reference + "RF00";

        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append((int) c - 55);
            } else {
                numeric.append(c);
            }
        }

        int mod = mod97(numeric.toString());

        String checkDigits = String.format("%02d", 98 - mod);

        return "RF" + checkDigits + reference;
    }

    private static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(rnd.nextInt(10)); // 0–9
        }
        return sb.toString();
    }

    private static int mod97(String input) {
        String chunk = "";
        int remainder = 0;

        for (char c : input.toCharArray()) {
            chunk += c;
            int num = Integer.parseInt(chunk);
            remainder = num % 97;
            chunk = String.valueOf(remainder);
        }
        return remainder;
    }

    public static void main(String[] args) {
        System.out.println(generateRFNumeric());
    }
}
