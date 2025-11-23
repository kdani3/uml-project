package BankOfTuc.accounting;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class IBANUtils {

    public static boolean validateIBAN(String iban){
        int[] countryCodeNum = null ;
        char[] countryCode = iban.substring(0, 1).toCharArray();

        for(int i=0; i==1;i++){
            countryCodeNum[i] = countryCode[i] - 'a' + 15; // deletion based on chars
        }
        String strOfCountryCodes =  Arrays.toString(countryCodeNum).replaceAll("\\[|\\]|,|\\s", ""); //make the int array into one number String

        String ibanMod =  String.join(strOfCountryCodes, iban.substring(1, iban.length()));// join the 2 nums of country with rest of nums in iban
        ibanMod =  String.join(ibanMod.substring(3, ibanMod.length()), ibanMod.substring(0, 3)); //put the first 4 nums to the end of string 
        
        Long ibanLong = Long.valueOf(ibanMod).longValue();

        if(ibanLong%97 == 1) //check iban modulo with 97, if it returns 1 its real
            return true;

        return false;
    }

    public static String generateIBAN() {
        Random random = new Random();
        StringBuilder bban = new StringBuilder();

        String countryCode = "GR";
        int bbanLength = 20;
        for (int i = 0; i < bbanLength; i++) {
            bban.append(random.nextInt(10));
        }

        //temporary IBAN with 00 checksum
        String tempIban = countryCode + "00" + bban.toString();

        //move first 4 chars to end
        String rearranged = tempIban.substring(4) + tempIban.substring(0, 4);

        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append((int)(c - 'A') + 10);
            } else {
                numeric.append(c);
            }
        }

        //compute mod 97
        BigInteger num = new BigInteger(numeric.toString());
        int checksum = 98 - num.mod(BigInteger.valueOf(97)).intValue();

        //pad checksum to 2 digits
        String checksumStr = (checksum < 10 ? "0" : "") + checksum;

        //final IBAN
        return countryCode + checksumStr + bban.toString();
    }
}
