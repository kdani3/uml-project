package BankOfTuc.bookkeeping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import BankOfTuc.Customer;

public class CustomerSerialiser implements JsonSerializer<Customer> {

    
    @Override
    public JsonElement serialize(Customer customer, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Explicitly serialize the fields you want
        jsonObject.addProperty("username", customer.getUsername());
        jsonObject.addProperty("fullname", customer.getFullname());
        jsonObject.addProperty("email", customer.getEmail());
        jsonObject.addProperty("vatID", customer.getVatID());
        jsonObject.addProperty("role", customer.getRole().toString());
        jsonObject.add("bankAccounts", context.serialize(customer.getBankAccounts()));


        // Exclude the password field manually if needed
        // You can also modify other fields here if necessary
        return jsonObject;
    }
}
