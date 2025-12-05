package BankOfTuc.Transfers;

import java.util.HashMap;
import java.util.Map;

public class TransferUtils {

    public static boolean equalsCustomerName(String str1, String str2){
        str1 = str1.replaceAll("\\s+","");
        str2 = str2.replaceAll("\\s+","");

        if(str1.length()!=str2.length())
            return false;

        if(str1.equalsIgnoreCase(str2))
            return true;

        if(ignoreLang(str1,str2))
            return true;

        return false;
    }

    public static boolean ignoreLang(String foo, String bar){
        int fooLang = detectLang(foo);
        int barLang = detectLang(bar);

        if(fooLang==1)
            return greekToEnglishCompare(foo, bar);

        else if(barLang==1)
            return greekToEnglishCompare(bar, foo);
        return false;
    }

    public static int detectLang(String foo){
        if (foo.matches("(?i).*[abcdefghijklmnopqrstuvxyz].*")) return 0;
        return 1;
    }

    public static boolean greekToEnglishCompare(String foo,String bar){
        foo = transliterate(foo);

        char[] fooChars = foo.toLowerCase().toCharArray();
        char[] barChars = bar.toLowerCase().toCharArray();
        boolean flag = true;

        for(int k=0;k<fooChars.length;k++){
            boolean fooIsUY = (fooChars[k] == 'u' || fooChars[k] == 'y');
            boolean barIsUY = (barChars[k] == 'u' || barChars[k] == 'y');

            if (fooIsUY && !barIsUY) {
                flag = false;
                break;
            }

            if (!fooIsUY && fooChars[k] != barChars[k]) {
                flag = false;
                break;
            }
        }
        return flag;
    }
    
    private static final Map<Character, String> MAP = new HashMap<>();

    static {
        MAP.put('α',"a"); MAP.put('β',"v"); MAP.put('γ',"g"); MAP.put('δ',"d");
        MAP.put('ε',"e"); MAP.put('ζ',"z"); MAP.put('η',"i"); MAP.put('θ',"th");
        MAP.put('ι',"i"); MAP.put('κ',"k"); MAP.put('λ',"l"); MAP.put('μ',"m");
        MAP.put('ν',"n"); MAP.put('ξ',"x"); MAP.put('ο',"o"); MAP.put('π',"p");
        MAP.put('ρ',"r"); MAP.put('σ',"s"); MAP.put('ς',"s"); MAP.put('τ',"t");
        MAP.put('υ',"y"); MAP.put('φ',"f"); MAP.put('χ',"h"); MAP.put('ψ',"ps");
        MAP.put('ω',"o");
    }

    public static String transliterate(String greek) {
        StringBuilder sb = new StringBuilder();

        for (char c : greek.toCharArray()) {
            sb.append(MAP.getOrDefault(c, String.valueOf(c)));
        }

        return sb.toString();
    }
}

