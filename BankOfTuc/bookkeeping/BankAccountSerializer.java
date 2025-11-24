package BankOfTuc.bookkeeping;

import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import BankOfTuc.accounting.BankAccount;

public class BankAccountSerializer 
        implements JsonSerializer<BankAccount>, JsonDeserializer<BankAccount> {

    @Override
    public JsonElement serialize(BankAccount src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("iban", src.getIban());
        obj.addProperty("balance", src.getBalance());
        obj.addProperty("type", src.getClass().getSimpleName());
        return obj;
    }

    @Override
    public BankAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {

    JsonObject obj = json.getAsJsonObject();

    // Extract safely
    String iban = obj.has("iban") ? obj.get("iban").getAsString() : null;
    double balance = obj.has("balance") ? obj.get("balance").getAsDouble() : 0.0;

    // Fallback for older JSON that used vatID as the IBAN (your constructor does!)
    if (iban == null && obj.has("vatID")) {
        iban = obj.get("vatID").getAsString();
    }

    // If still no IBAN, JSON is invalid → show full JSON
    if (iban == null) {
    }

    BankAccount acc = new BankAccount(iban);
    acc.setBalance(balance);
    return acc;
}
}
