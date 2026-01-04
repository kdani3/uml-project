package BankOfTuc.Bookkeeping;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
//import java.util.Map;
import java.util.stream.Collectors;
//import java.util.HashMap;
import com.google.gson.Gson;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Customer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.Accounting.BankAccount;

public class CustomerFileManager {
    private String filePath;

    private final Gson gson;
    private CustomerStore store;

    private static volatile CustomerFileManager instance;

    private CustomerFileManager(String filePath) throws IOException {
        this.filePath = filePath;

        this.gson = GsonProvider.get();

        load();
    }

    public static CustomerFileManager getInstance(String filePath) throws IOException {
        if (instance == null) {
            synchronized (CustomerFileManager.class) {
                if (instance == null) {
                    instance = new CustomerFileManager(filePath);
                }
            }
        }
        return instance;
    }

    public static CustomerFileManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CustomerFileManager not initialized. Call getInstance(filePath) first.");
        }
        return instance;
    }

    public BankAccount findAccountByIBAN(String iban) {
    //customer who owns this IBAN
    var customer = getCustomerByIBAN(iban);
    if (customer == null) {
        return null; //no customer 
    }

    //search through their accounts
    for (BankAccount account : customer.getBankAccounts()) {
        if (iban.equals(account.getIban())) {
            return account;
        }
    }

    return null; //iban not found in customer's accounts
}

    public Customer getCustomerByIBAN(String IBAN){
        //Map<Integer, Customer> map = new HashMap<>();

        for(Customer c: store.customers){
            List<BankAccount> accounts = c.getBankAccounts();
            for(BankAccount account:accounts){
                if(account.getIban().equals(IBAN)){
                    //map.put(i,cust);
                    return c;
                }
            }
        }
        return null;
    }
     
    private void load() {
        try (FileReader reader = new FileReader(filePath)) {
            store = gson.fromJson(reader, CustomerStore.class);

            //handle nulls in JSON
            if (store == null || store.customers == null) {
                store = new CustomerStore();
            }
        } catch (IOException e) {
            //file doesn't exist or can't be read — initialize empty store
            store = new CustomerStore();
        }
    }

    public void reloadCustomers() {
        load();
    }

    private  void save()  {
        File f = new File(filePath);
        File parentDir = f .getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); //create all missing parent folders
        }
        try (FileWriter writer = new FileWriter(filePath,false)) {
            gson.toJson(store, writer);
            writer.flush(); 
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }

    public Customer addCustomer(Customer c) {
        //check if user already exists by username
        for (Customer customer : store.customers) {
            boolean usernameMatch = c.getUsername().equals(customer.getUsername());
            boolean fullnameMatch = c.getFullname().equals(customer.getFullname());
            
            // Ασφαλής έλεγχος email (null-safe)
            boolean emailMatch = (c.getEmail() != null && customer.getEmail() != null) 
                                 && c.getEmail().equals(customer.getEmail());

            if (usernameMatch || fullnameMatch || emailMatch) {
                return c; 
            }
        }
        store.customers.add(c);

        save();
        return c;
    }


    public  boolean updateCustomer(Customer customer) {
        int id = getCustomerIndex(customer);
        if (id < 0 || id >= store.customers.size()) return false;
        store.customers.set(id, customer);
        save();
        return true;
    }

    public boolean updateCustomerByIndex(int i,Customer customer){
        if(store.customers.get(i)!=null){
            store.customers.set(i, customer);
            save();
            return true;
        }
        return false;

    }

    public boolean updateCustomerById(int i,Customer customer ){
        if(i>=0){
            store.customers.set(i, customer);
            save();
            return true;
        }
        return false;
    }

    public int getCustomerIndex(Customer customer){
        for (int i = 0; i < store.customers.size(); i++) {

            Customer existingCustomer = store.customers.get(i);
            
            //check if the username or fullname matches
            if (existingCustomer.getUsername().equals(customer.getUsername()) || 
                existingCustomer.getFullname().equals(customer.getFullname())) {
                return i;  // Return the index of the found customer
            }
        }
        
        //return -1 if no matching customer was found
        return -1;
    }
    

    public boolean deleteCustomer(Customer customer) throws IOException {
        int i = getCustomerIndex(customer);
        if (i<0) return false;
        store.customers.remove(i);

        save();
        return true;
    }

    public  List<Customer> getAllCustomers() {
        return store.customers;
    }

    public Customer getCustomerByUsername(String username) {
        if (username == null) {
            return null;
        }

        for (Customer c : store.customers) {
            if (username.equals(c.getUsername())) {
                return c;
            }
        }
        return null;
    }

    public Customer getCustomerbyVatid(String vatid) {
    if (vatid == null) {
        return null;
    }

    
    for (Customer c : store.customers) {
        if (vatid.equals(c.getVatID())) {
            return c;
        }
    }
    
    return null;
}

    
    public void getIndividualCustomerBankAccounts(IndividualCustomer Customer) {
         store.customers.stream()
            .flatMap(user -> ((IndividualCustomer) user).getBankAccounts().stream())
            .collect(Collectors.toList());
    }

    public List<BankAccount> getCompanyCustomerBankAccounts() {
        return store.customers.stream()
            .filter(user -> user instanceof CompanyCustomer)
            .flatMap(user -> ((CompanyCustomer) user).getBankAccounts().stream())
            .collect(Collectors.toList());
    }

    public void updateBankAccountForCustomer(BankAccount a, Customer c) {

        //find customer
        for (Customer cust : store.customers) {
            if (cust.getUsername().equals(c.getUsername())) {

                //find existing bank account
                for (BankAccount acc : cust.getBankAccounts()) {

                    if (acc.getIban().equals(a.getIban())) {
                        acc.setBalance(a.getBalance());
                        save();
                        return;
                    }
                }

                return;
            }
        }
    }

}
