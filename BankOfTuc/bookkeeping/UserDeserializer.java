package BankOfTuc.bookkeeping;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Customer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.accounting.BankAccount;

public class UserDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        final Gson RAW_GSON = new Gson();

        String role = jsonObject.get("role").getAsString();
        System.out.println(role);
        
        if ("INDIVIDUAL".equals(role)) {
            return RAW_GSON.fromJson(json, IndividualCustomer.class);
        }
        else if("COMPANY".equals(role)){
            return RAW_GSON.fromJson(json, CompanyCustomer.class);
        }
        else {
            return RAW_GSON.fromJson(json, User.class);
        }
    }
}