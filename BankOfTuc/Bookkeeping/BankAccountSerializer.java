package BankOfTuc.Bookkeeping;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import BankOfTuc.Accounting.BankAccount;

public class BankAccountSerializer 
        implements JsonSerializer<BankAccount>, JsonDeserializer<BankAccount> {

    @Override
    public JsonElement serialize(BankAccount src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("iban", src.getIban());
        obj.addProperty("balance", src.getBalance());
        if (src.getType() != null) obj.addProperty("type", src.getType().name());
        return obj;
    }

    @Override
    public BankAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject obj = json.getAsJsonObject();
        String iban = obj.has("iban") && !obj.get("iban").isJsonNull() ? obj.get("iban").getAsString() : null;
        double balance = obj.has("balance") && !obj.get("balance").isJsonNull() ? obj.get("balance").getAsDouble() : 0.0;
        String typeStr = obj.has("type") && !obj.get("type").isJsonNull() ? obj.get("type").getAsString() : null;

        BankAccount.AccountType type = BankAccount.AccountType.CHECKING; // default
        if (typeStr != null) {
            switch (typeStr.toUpperCase()) {
                case "CHECKING":
                case "CURRENT":
                    type = BankAccount.AccountType.CHECKING;
                    break;
                case "SAVINGS":
                    type = BankAccount.AccountType.SAVINGS;
                    break;
                case "COMPANY":
                    type = BankAccount.AccountType.COMPANY;
                    break;
                case "BANKACCOUNT": // legacy value present in data
                    type = BankAccount.AccountType.CHECKING;
                    break;
                default:
                    try {
                        type = BankAccount.AccountType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        type = BankAccount.AccountType.CHECKING;
                    }
            }
        }

        BankAccount account = new BankAccount();
        if (iban != null) account.setIban(iban);
        account.setBalance(balance);
        account.setType(type);
        return account;
    }
}