package BankOfTuc.Bookkeeping;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import BankOfTuc.Accounting.BankAccount;

public class BankAccountSerializer 
        implements JsonSerializer<BankAccount> {

    @Override
    public JsonElement serialize(BankAccount src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("iban", src.getIban());
        obj.addProperty("balance", src.getBalance());
        obj.addProperty("type", src.getClass().getSimpleName());
        return obj;
    }

    
}
