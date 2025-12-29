package BankOfTuc.Bookkeeping;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import BankOfTuc.Admin;
import BankOfTuc.CompanyCustomer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;

public class UserDeserializer implements JsonDeserializer<User>,JsonSerializer<User>{

    @Override
    public JsonElement serialize(User user, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Explicitly serialize the fields you want
        jsonObject.addProperty("id", user.getid());
        jsonObject.addProperty("username", user.getUsername());
        jsonObject.addProperty("fullname", user.getFullname());
        jsonObject.addProperty("email", user.getEmail());
        jsonObject.addProperty("isActive", user.getActive());
        jsonObject.addProperty("role", user.getRole().toString());
        jsonObject.addProperty("saltBase64", user.getSaltBase64());
        jsonObject.addProperty("hashedPassword", user.getHashedPassword());
        jsonObject.addProperty("qrSecret", user.getQrCode());
        
        // Exclude the password field manually if needed
        // You can also modify other fields here if necessary
        return jsonObject;
    }
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        final Gson RAW_GSON = new Gson();

        String role = jsonObject.get("role").getAsString();
        
        if ("INDIVIDUAL".equals(role)) {
            return RAW_GSON.fromJson(json, IndividualCustomer.class);
        }
        else if("COMPANY".equals(role)){
            return RAW_GSON.fromJson(json, CompanyCustomer.class);
        }
        else {
            return RAW_GSON.fromJson(json, Admin.class);
        }
    }
}