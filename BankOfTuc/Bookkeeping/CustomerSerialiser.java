package BankOfTuc.Bookkeeping;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

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

        //explicit serailisation
        jsonObject.addProperty("username", customer.getUsername());
        jsonObject.addProperty("fullname", customer.getFullname());
        jsonObject.addProperty("email", customer.getEmail());
        jsonObject.addProperty("vatID", customer.getVatID());
        jsonObject.addProperty("role", customer.getRole().toString());
        jsonObject.add("bankAccounts", context.serialize(customer.getBankAccounts()));
        

        return jsonObject;
    }

    @Override
    public Customer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        JsonObject obj = json.getAsJsonObject();

        String username = obj.get("username").getAsString();
        String fullname = obj.get("fullname").getAsString();
        String email = obj.get("email").getAsString();
        String vatID = obj.get("vatID").getAsString();
        Role role = Role.valueOf(obj.get("role").getAsString());

        Customer customer;

        if ( role == Role.INDIVIDUAL) {
            customer = new IndividualCustomer(username, "...", fullname, vatID, email, true);
        } else {
            customer = new CompanyCustomer(username, "...", fullname, vatID, email, true);
        }

        List<BankAccount> accounts =
            ctx.deserialize(obj.get("bankAccounts"),
            new TypeToken<List<BankAccount>>(){}.getType());

        customer.setBankAccounts(accounts != null ? accounts : new ArrayList<>());

        return customer;
    }

}
