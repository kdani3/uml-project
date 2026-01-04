package BankOfTuc.Bookkeeping;

public class BankRevenue {
    private static double totalFees = 0.0;


    public static void addFee(double amount) {
        totalFees += amount;
    }

    public static double getTotalFees() {
        return totalFees;
    }

    public static void reset() {
        totalFees = 0.0;
    }
}