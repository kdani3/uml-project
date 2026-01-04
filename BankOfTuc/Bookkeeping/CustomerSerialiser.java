package BankOfTuc.Bookkeeping;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Customer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;
import BankOfTuc.User.Role;

public class CustomerSerialiser implements JsonSerializer<Customer> ,  JsonDeserializer<Customer>{

    
    @Override
    public JsonElement serialize(Customer customer, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        if (customer.getUsername() != null) jsonObject.addProperty("username", customer.getUsername());
        if (customer.getFullname() != null) jsonObject.addProperty("fullname", customer.getFullname());
        if (customer.getEmail() != null) jsonObject.addProperty("email", customer.getEmail());
        if (customer.getVatID() != null) jsonObject.addProperty("vatID", customer.getVatID());
        if (customer.getRole() != null) jsonObject.addProperty("role", customer.getRole().toString());

        Type listType = new com.google.gson.reflect.TypeToken<List<BankAccount>>() {}.getType();
        jsonObject.add("bankAccounts", context.serialize(customer.getBankAccounts(), listType));

        return jsonObject;
    }

    @Override
    public Customer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        JsonObject obj = json.getAsJsonObject();

        // Safe extraction of fields (handle nulls)
        String username = obj.has("username") ? obj.get("username").getAsString() : null;
        String fullname = obj.has("fullname") ? obj.get("fullname").getAsString() : null;
        

        String email = (obj.has("email") && !obj.get("email").isJsonNull()) ? obj.get("email").getAsString() : null;
        
        String vatID = obj.has("vatID") ? obj.get("vatID").getAsString() : null;
        Role role = obj.has("role") ? Role.valueOf(obj.get("role").getAsString()) : Role.INDIVIDUAL; // Default fallback

        Customer customer;

        if ( role == Role.INDIVIDUAL) {
            customer = new IndividualCustomer(username, "...", fullname, vatID, email, true);
        } else {
            customer = new CompanyCustomer(username, "...", fullname, vatID, email, true);
        }

        JsonElement accountsEl = obj.get("bankAccounts");
        List<BankAccount> accounts = new ArrayList<>();
        if (accountsEl != null && accountsEl.isJsonArray()) {
            for (JsonElement elt : accountsEl.getAsJsonArray()) {
                try {
                    BankAccount ba = ctx.deserialize(elt, BankAccount.class);
                    if (ba != null) accounts.add(ba);
                } catch (Exception e) {
                }
            }
        }
        customer.setBankAccounts(accounts);

        return customer;
    }

}