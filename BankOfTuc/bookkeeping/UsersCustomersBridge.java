package BankOfTuc.bookkeeping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BankOfTuc.CompanyCustomer;
import BankOfTuc.Customer;
import BankOfTuc.IndividualCustomer;
import BankOfTuc.User;
import BankOfTuc.User.Role;

public class UsersCustomersBridge {
    List<User> users;
    List<Customer> customers;

    public UsersCustomersBridge(UserFileManagement ufm, CustomerFileManager cfm){
        users = ufm.getAllUsers();
        customers = cfm.getAllCustomers();
    }

    public void bridge() {
    Map<String, Customer> customerMap = new HashMap<>();
    for (Customer c : customers) {
        customerMap.put(c.getUsername(), c);
    }

    for (User u : users) {
        Customer c = customerMap.get(u.getUsername());
        if (c != null) {
            if (u.getRole() == Role.INDIVIDUAL) {
                IndividualCustomer custFromUser = (IndividualCustomer) u;
                custFromUser.setBankAccounts(c.getBankAccounts());
            } else if (u.getRole() == Role.COMPANY) {
                CompanyCustomer custFromUser = (CompanyCustomer) u;
                custFromUser.setBankAccounts(c.getBankAccounts());
            }
        }
    }
}
}
