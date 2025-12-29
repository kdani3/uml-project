package BankOfTuc.Bookkeeping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.Customer;
import BankOfTuc.User;

public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls() // ⭐ ΑΥΤΟ ΕΙΝΑΙ ΤΟ FIX
            .registerTypeAdapter(BankAccount.class, new BankAccountSerializer())
            .registerTypeAdapter(Customer.class, new CustomerSerialiser())
            .registerTypeAdapter(User.class, new UserDeserializer())
            .setPrettyPrinting()
            .create();

    private GsonProvider() {}

    public static Gson get() {
        return GSON;
    }
}
